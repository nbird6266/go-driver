package oscar

import (
	"context"
	"database/sql/driver"
	"encoding/hex"
	"fmt"
	"math"
	"strconv"
	"time"
)

// Internal Oscar parameter bind types (dbType). Mirrors the JDBC driver's
// bind type constants used in OscarStatement.set* methods.
const (
	bindTypeNumeric   = 23 // NUMBER/INTEGER etc., base-100 binary encoding
	bindTypeString    = 24 // CHAR/VARCHAR, raw bytes
	bindTypeDate      = 25
	bindTypeTime      = 26
	bindTypeTimestamp = 28
	bindTypeBool      = 33
	bindTypeBlob      = 50
	bindTypeClob      = 51
	bindTypeBfile     = 52
)

const (
	prepareExecuteTag = 0x0B // ExecutePacket carrying "PREPARE <name> AS <sql>"
	executeTag        = 0x0D // ExecutePacket without prepare SQL (reuse prepared)
)

// convertBindValues turns []driver.NamedValue into the parallel bindTypes /
// bindDatas arrays consumed by sendExecutePacket. paramOIDs carries the
// server-inferred type OID of each parameter (from ParamInfo) and selects the
// encoding for []byte values: binary-string types (bytea/binary/varbinary)
// are bound as raw bytes, everything else as the "0x"+hex text that BLOB
// columns parse. nil entries mean the type is unknown and the default hex
// encoding is used.
func convertBindValues(args []driver.NamedValue, paramOIDs []int) (bindTypes []int, bindDatas [][]byte, err error) {
	bindTypes = make([]int, len(args))
	bindDatas = make([][]byte, len(args))
	for i, arg := range args {
		var oid int
		if i < len(paramOIDs) {
			oid = paramOIDs[i]
		}
		data, bt, err := convertBindValue(arg.Value, oid)
		if err != nil {
			return nil, nil, err
		}
		bindTypes[i] = bt
		bindDatas[i] = data
	}
	return bindTypes, bindDatas, nil
}

// isBinaryParamOID reports whether the server-inferred parameter type is a
// binary-string type whose value is stored byte-for-byte, so []byte binds
// must be the raw bytes (not "0x"+hex text).
func isBinaryParamOID(oid int) bool {
	switch oid {
	case 17, 1365, 3100: // bytea, binary, varbinary
		return true
	}
	return false
}

// convertBindValue encodes a single parameter. The type mapping follows the
// JDBC driver with the session settings used by initSession
// (NET_DATA_BY_STR=FALSE, SEND_FLOATINGNUMBER_KEEP_PRECISION=TRUE):
//
//   - integers  -> type 23 (base-100 numeric encoding)
//   - floats    -> type 24 string (numericKeepPrecision)
//   - booleans  -> type 24 "1"/"0" (JDBC setBoolean -> setString)
//   - strings   -> type 24 raw bytes (chunked when > 240 bytes)
//   - []byte    -> type 24; raw bytes for binary-string parameters
//     (bytea/binary/varbinary, paramOID 17/1365/3100), otherwise "0x" hex
//     text which BLOB columns parse
//   - time.Time -> type 28 timestamp binary
//   - nil       -> type 24, null data
func convertBindValue(v driver.Value, paramOID int) (data []byte, bindType int, err error) {
	switch val := v.(type) {
	case nil:
		return nil, bindTypeString, nil
	case bool:
		if val {
			return []byte("1"), bindTypeString, nil
		}
		return []byte("0"), bindTypeString, nil
	case int:
		return convertLongToBytes(int64(val)), bindTypeNumeric, nil
	case int8:
		return convertLongToBytes(int64(val)), bindTypeNumeric, nil
	case int16:
		return convertLongToBytes(int64(val)), bindTypeNumeric, nil
	case int32:
		return convertLongToBytes(int64(val)), bindTypeNumeric, nil
	case int64:
		return convertLongToBytes(val), bindTypeNumeric, nil
	case uint:
		return []byte(strconv.FormatUint(uint64(val), 10)), bindTypeString, nil
	case uint8:
		return convertLongToBytes(int64(val)), bindTypeNumeric, nil
	case uint16:
		return convertLongToBytes(int64(val)), bindTypeNumeric, nil
	case uint32:
		return convertLongToBytes(int64(val)), bindTypeNumeric, nil
	case uint64:
		return []byte(strconv.FormatUint(val, 10)), bindTypeString, nil
	case float32:
		return []byte(strconv.FormatFloat(float64(val), 'f', -1, 32)), bindTypeString, nil
	case float64:
		return []byte(strconv.FormatFloat(val, 'f', -1, 64)), bindTypeString, nil
	case string:
		return convertByteArr([]byte(val)), bindTypeString, nil
	case []byte:
		if isBinaryParamOID(paramOID) {
			// bytea/binary/varbinary are stored as text on the server: a NUL
			// byte terminates the value and a lone backslash or invalid escape
			// sequence is rejected. Both are escaped as '\ooo' (the same
			// format the server uses in its output); the server stores the
			// escape text verbatim and the driver decodes it back on read.
			return convertByteArr(escapeBytea(val)), bindTypeString, nil
		}
		return convertByteArr([]byte("0x" + hex.EncodeToString(val))), bindTypeString, nil
	case time.Time:
		return convertTimestampToBytes(val), bindTypeTimestamp, nil
	default:
		// Fallback: stringify, mirroring the JDBC driver's setObject default.
		return convertByteArr([]byte(fmt.Sprintf("%v", val))), bindTypeString, nil
	}
}

// execPrepared sends an ExecutePacket (0x0B with the PREPARE SQL on the first
// run, 0x0D afterwards) and drains the response. The prepare SQL is cleared
// after the first execution so the server-side prepared statement is reused.
// The row description from the first execution is cached on the statement:
// on the reuse path the server omits the 'T' RowDescription packet.
func (c *conn) execPrepared(ctx context.Context, s *oscarStmt, bindTypes []int, bindDatas [][]byte) (queryResult, error) {
	prepareSQL := s.prepareSQL
	res, err := c.queryPacket(ctx, s.fields, func() error {
		return c.sendExecutePacket(prepareSQL, s.statementName, bindTypes, bindDatas)
	})
	if err != nil {
		return res, err
	}
	if len(res.fields) == 0 {
		res.fields = s.fields
	}
	if len(res.fields) > 0 {
		s.fields = res.fields
	}
	if prepareSQL != "" {
		s.prepareSQL = ""
		s.prepared = true
	}
	return res, nil
}

// sendExecutePacket writes an ExecutePacket. Wire format (V2):
//
//	prepareSQL != "" : 0x0B | marked(2) | totalLen(2/4) | sql | 0x00
//	prepareSQL == "" : 0x0D | totalLen(2/4)
//	then: paramCount(1/2) | bindTypes... | nameLen(1) | name | bindDatas...
//
// totalLen covers everything after the packet length field. For protocol
// type >= 3 the length is 4 bytes and the param count 2 bytes (plus one
// extra byte in totalLen), mirroring ExecutePacket.java.
func (c *conn) sendExecutePacket(prepareSQL, statementName string, bindTypes []int, bindDatas [][]byte) error {
	sendTypes := len(bindTypes) > 0

	totalLen := 0
	if prepareSQL != "" {
		totalLen += len(prepareSQL) + 1
	}
	totalLen += len(statementName) + 1
	if sendTypes {
		totalLen += len(bindTypes) + 1
	} else {
		totalLen++
	}
	for i, d := range bindDatas {
		bt := bindType(i, bindTypes)
		switch {
		case d == nil:
			totalLen++
		case len(d) == 0:
			totalLen += 2
		case bt == bindTypeString && len(d) > 240:
			totalLen += len(d)
		case bt != bindTypeString && d[0] == 0xFD:
			totalLen += len(d)
		case bt == bindTypeBlob || bt == bindTypeClob || bt == bindTypeBfile:
			totalLen += len(d) + 5
		default:
			totalLen += len(d) + 1
		}
	}

	packetLenSize := 2
	paramCountSize := 1
	if c.protoVersion >= protoV3 {
		packetLenSize = 4
		paramCountSize = 2
		totalLen++
	}

	if prepareSQL != "" {
		if err := c.writeByte(prepareExecuteTag); err != nil {
			return err
		}
		if err := c.writeUint16(0); err != nil { // marked
			return err
		}
		if err := c.writePacketLen(totalLen, packetLenSize); err != nil {
			return err
		}
		if _, err := c.bw.Write([]byte(prepareSQL)); err != nil {
			return err
		}
		if err := c.writeByte(0); err != nil {
			return err
		}
	} else {
		if err := c.writeByte(executeTag); err != nil {
			return err
		}
		if err := c.writePacketLen(totalLen, packetLenSize); err != nil {
			return err
		}
	}

	if sendTypes {
		if err := c.writeParamCount(len(bindTypes), paramCountSize); err != nil {
			return err
		}
		for _, t := range bindTypes {
			if err := c.writeByte(byte(t)); err != nil {
				return err
			}
		}
	} else {
		if err := c.writeParamCount(0, paramCountSize); err != nil {
			return err
		}
	}

	if err := c.writeByte(byte(len(statementName))); err != nil {
		return err
	}
	if _, err := c.bw.Write([]byte(statementName)); err != nil {
		return err
	}

	for i, d := range bindDatas {
		bt := bindType(i, bindTypes)
		if d == nil {
			if err := c.writeByte(0); err != nil {
				return err
			}
			continue
		}
		if len(d) == 0 {
			d = []byte{0}
		}
		switch {
		case bt == bindTypeString && len(d) > 240:
			if _, err := c.bw.Write(d); err != nil {
				return err
			}
		case bt != bindTypeString && d[0] == 0xFD:
			if _, err := c.bw.Write(d); err != nil {
				return err
			}
		case bt == bindTypeBlob:
			if err := c.writeByte(251); err != nil {
				return err
			}
			if err := c.writeUint32(uint32(len(d))); err != nil {
				return err
			}
			if _, err := c.bw.Write(d); err != nil {
				return err
			}
		case bt == bindTypeClob:
			if err := c.writeByte(250); err != nil {
				return err
			}
			if err := c.writeUint32(uint32(len(d))); err != nil {
				return err
			}
			if _, err := c.bw.Write(d); err != nil {
				return err
			}
		case bt == bindTypeBfile:
			if err := c.writeByte(252); err != nil {
				return err
			}
			if err := c.writeUint32(uint32(len(d))); err != nil {
				return err
			}
			if _, err := c.bw.Write(d); err != nil {
				return err
			}
		default:
			if err := c.writeByte(byte(len(d))); err != nil {
				return err
			}
			if _, err := c.bw.Write(d); err != nil {
				return err
			}
		}
	}
	return c.bw.Flush()
}

func (c *conn) writePacketLen(v, size int) error {
	if size == 4 {
		return c.writeUint32(uint32(v))
	}
	return c.writeUint16(uint16(v))
}

func (c *conn) writeParamCount(v, size int) error {
	if size == 2 {
		return c.writeUint16(uint16(v))
	}
	return c.writeByte(byte(v))
}

func bindType(i int, bindTypes []int) int {
	if i < len(bindTypes) {
		return bindTypes[i]
	}
	return bindTypeString
}

// convertLongToBytes encodes an int64 using Oscar's base-100 numeric format
// (marker byte + 2-digit groups + optional 0x66 terminator). Mirrors
// NumberConverter.convertLongToBytes / TypeConverter.getByteLength.
func convertLongToBytes(v int64) []byte {
	if v == 0 {
		return []byte{0x80}
	}
	if v == math.MinInt64 {
		return []byte{53, 92, 79, 68, 29, 98, 33, 47, 24, 43, 93, 102}
	}
	neg := v < 0
	if neg {
		v = -v
	}
	byteLen := getByteLen(v)
	realLen := getRealLen(v, byteLen)

	out := make([]byte, realLen+2)
	j := byteLen
	k := 0
	if neg {
		out[0] = byte(63 - byteLen)
		for {
			m := int(v % 100)
			if k == 0 {
				if m != 0 {
					out[j+1] = 102
					k = j + 2
					out[j] = byte(101 - m)
				}
			} else {
				out[j] = byte(101 - m)
			}
			j--
			if j != 0 {
				v /= 100
				continue
			}
			break
		}
		return out[:realLen+2]
	}

	out = make([]byte, realLen+1)
	out[0] = byte(192 + byteLen)
	for {
		m := int(v % 100)
		if k == 0 {
			if m != 0 {
				out[j] = byte(m + 1)
				k = j + 1
			}
		} else {
			out[j] = byte(m + 1)
		}
		j--
		if j == 0 {
			break
		}
		v /= 100
	}
	return out
}

// getByteLen mirrors TypeConverter.getByteLength(long): the number of
// base-100 digits (groups of two decimal digits) needed for v.
func getByteLen(v int64) int {
	i := 10
	if v/1000000000000000000 == 0 {
		i--
		if v/10000000000000000 == 0 {
			i--
			if v/100000000000000 == 0 {
				i--
				if v/1000000000000 == 0 {
					i--
					if v/10000000000 == 0 {
						i--
						if v/100000000 == 0 {
							i--
							if v/1000000 == 0 {
								i--
								if v/10000 == 0 {
									i--
									if v/100 == 0 {
										i--
									}
								}
							}
						}
					}
				}
			}
		}
	}
	return i
}

// getRealLen mirrors TypeConverter.getByteLength(long, int): byteLen minus
// the number of trailing base-100 groups that are zero.
func getRealLen(v int64, length int) int {
	div := int64(1)
	for {
		if (v/div)%100 != 0 {
			return length
		}
		length--
		div *= 100
	}
}

// convertTimestampToBytes encodes time.Time as an 11-byte timestamp
// (type 28). Mirrors TimestampConverter.convertTimestampToBytes.
func convertTimestampToBytes(t time.Time) []byte {
	y, mo, d := t.Date()
	h, mi, s := t.Clock()
	micros := t.Nanosecond() / 1000
	out := make([]byte, 11)
	out[0] = byte(y/100 + 100)
	out[1] = byte(y%100 + 100)
	out[2] = byte(int(mo))
	out[3] = byte(d)
	out[4] = byte(h + 1)
	out[5] = byte(mi + 1)
	out[6] = byte(s + 1)
	out[7] = byte(micros >> 24)
	out[8] = byte(micros >> 16)
	out[9] = byte(micros >> 8)
	out[10] = byte(micros)
	return out
}

// escapeBytea escapes every byte of a binary value as '\ooo' octal text, the
// escape format Oscar uses for bytea input and output. The server parses the
// escapes (a bare backslash or an unterminated escape is rejected with "Bad
// input string for type bytea", and a backslash followed by octal digits such
// as "\134377" is re-parsed as "\134" + "\377"), so escaping everything
// guarantees an unambiguous round-trip: the driver's decodeByteaEscape
// restores the original bytes on read.
func escapeBytea(b []byte) []byte {
	if len(b) == 0 {
		return b
	}
	out := make([]byte, 0, len(b)*4)
	for _, c := range b {
		out = append(out, '\\', '0'+c/64, '0'+c/8%8, '0'+c%8)
	}
	return out
}

// convertByteArr applies the long-varchar chunking for strings longer than
// 240 bytes: 0xFE header, 0xF0-prefixed 240-byte blocks, a final block
// prefixed with its real length and a trailing 0x00. Mirrors
// ByteConverter.convertByteArr / convertVarcharData.
func convertByteArr(data []byte) []byte {
	if data == nil || len(data) <= 240 {
		return data
	}
	out := make([]byte, 0, len(data)+1+(len(data)+240-1)/240+1)
	out = append(out, 0xFE)
	src := 0
	for {
		remaining := len(data) - src
		if remaining == 240 {
			out = append(out, 0xF0)
			out = append(out, data[src:src+240]...)
			break
		}
		if remaining > 240 {
			out = append(out, 0xF0)
			out = append(out, data[src:src+240]...)
			src += 240
			continue
		}
		out = append(out, byte(remaining))
		out = append(out, data[src:]...)
		break
	}
	out = append(out, 0)
	return out
}
