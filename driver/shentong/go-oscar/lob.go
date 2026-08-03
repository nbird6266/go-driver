package oscar

import (
	"database/sql/driver"
	"fmt"
)

// LOB content fetching. OSCAR returns LOB columns (blob/clob/bfile) in result
// rows as a locator string; the actual content is read back through the
// Fastpath protocol (FunctionCallPacketV2, tag 0x02), mirroring the JDBC
// driver's OscarLob.length()/getDataInternal().

// LOB data types used by the Fastpath function registry.
const (
	lobBlob  = 1
	lobClob  = 2
	lobBfile = 3
)

// lobTypeForOscarType maps an internal OSCAR type code to the Fastpath LOB
// data type (0 if the type is not a LOB).
func lobTypeForOscarType(oscarType int) int {
	switch oscarType {
	case 50:
		return lobBlob
	case 51:
		return lobClob
	case 52:
		return lobBfile
	}
	return 0
}

// lobFuncOID returns the server function OID for the given Fastpath LOB type
// and function name (protocol main version 4, see Fastpath.java).
func lobFuncOID(lobType int, fn string) int {
	switch fn {
	case "GETPRECISELENGTH":
		switch lobType {
		case lobBlob:
			return 2970
		case lobClob:
			return 2971
		case lobBfile:
			return 2972
		}
	case "GET_CHUNKSIZE":
		switch lobType {
		case lobBlob:
			return 2968
		case lobClob:
			return 2969
		}
	case "READ":
		switch lobType {
		case lobBlob:
			return 2976
		case lobClob:
			return 2977
		case lobBfile:
			return 2978
		}
	case "READCOMPRESS":
		return 3018 // blob only
	}
	return 0
}

// functionCall invokes a server function through the Fastpath protocol and
// returns the raw result bytes. isNull reports whether the server returned
// NULL. The response loop handles 'V' (FunctionResponsePacket), 'E', 'N' and
// terminates at 'Z' (ReadyForQuery).
func (c *conn) functionCall(funcOID int, params [][]byte) (result []byte, isNull bool, err error) {
	if c.closed || c.netConn == nil {
		return nil, false, driver.ErrBadConn
	}
	if err := c.writeByte(0x02); err != nil {
		return nil, false, err
	}
	if err := c.writeByte(0x00); err != nil { // query num
		return nil, false, err
	}
	if err := c.writeUint32(uint32(funcOID)); err != nil {
		return nil, false, err
	}
	if err := c.writeUint32(uint32(len(params))); err != nil {
		return nil, false, err
	}
	for _, p := range params {
		if err := c.writeUint32(uint32(len(p))); err != nil {
			return nil, false, err
		}
		if _, err := c.bw.Write(p); err != nil {
			return nil, false, err
		}
	}
	if err := c.bw.Flush(); err != nil {
		return nil, false, err
	}

	for {
		tag, err := c.readByte()
		if err != nil {
			return nil, false, err
		}
		switch tag {
		case 'V':
			n, err := c.readByte()
			if err != nil {
				return nil, false, err
			}
			if n != 'G' { // 71 == 'G' means not null
				isNull = true
				continue
			}
			sz, err := c.readUint32()
			if err != nil {
				return nil, false, err
			}
			result, err = c.readN(int(sz))
			if err != nil {
				return nil, false, err
			}
			if _, err := c.readByte(); err != nil { // unused
				return nil, false, err
			}
		case 'E':
			return nil, false, c.readErrorPacket()
		case 'N':
			if _, err := c.readNoticePacket(); err != nil {
				return nil, false, err
			}
		case 'Z':
			if err := c.readReadyForQuery(); err != nil {
				return nil, false, err
			}
			return result, isNull, nil
		default:
			return nil, false, fmt.Errorf("oscar: unexpected function response tag 0x%X", tag)
		}
	}
}

// readLobContent fetches the content of a LOB column identified by its raw
// row value (an ASCII hex locator string for protocol V3+). It mirrors JDBC
// OscarLob: GETPRECISELENGTH to learn the size, GET_CHUNKSIZE for the read
// granularity, then chunked READ calls (READCOMPRESS + zlib for blobs when
// compression is active).
func (c *conn) readLobContent(f field, locator []byte) ([]byte, error) {
	lobType := lobTypeForOscarType(f.OscarType)
	if lobType == 0 {
		return locator, nil
	}
	if len(locator) == 0 {
		return []byte{}, nil
	}
	binLoc, err := hexStringToBytes(string(locator))
	if err != nil {
		return nil, err
	}

	res, isNull, err := c.functionCall(lobFuncOID(lobType, "GETPRECISELENGTH"), [][]byte{binLoc})
	if err != nil {
		return nil, err
	}
	if isNull || len(res) == 0 {
		return nil, nil
	}
	length := int64(0)
	for _, b := range res {
		length = length<<8 | int64(b)
	}
	if length <= 0 {
		return []byte{}, nil
	}

	chunkSize := int64(65536)
	if cs, _, err := c.functionCall(lobFuncOID(lobType, "GET_CHUNKSIZE"), [][]byte{binLoc}); err == nil && len(cs) >= 4 {
		chunkSize = int64(cs[0])<<24 | int64(cs[1])<<16 | int64(cs[2])<<8 | int64(cs[3])
	}
	if chunkSize <= 0 {
		chunkSize = 65536
	}

	readFn := lobFuncOID(lobType, "READ")
	lenBuf := make([]byte, 4)
	posBuf := make([]byte, 8)

	var content []byte
	for pos := int64(1); pos <= length; {
		remaining := length - pos + 1
		n := chunkSize
		if remaining < n {
			n = remaining
		}
		lenBuf[0] = byte(n >> 24)
		lenBuf[1] = byte(n >> 16)
		lenBuf[2] = byte(n >> 8)
		lenBuf[3] = byte(n)
		posBuf[0] = byte(pos >> 56)
		posBuf[1] = byte(pos >> 48)
		posBuf[2] = byte(pos >> 40)
		posBuf[3] = byte(pos >> 32)
		posBuf[4] = byte(pos >> 24)
		posBuf[5] = byte(pos >> 16)
		posBuf[6] = byte(pos >> 8)
		posBuf[7] = byte(pos)

		data, _, err := c.functionCall(readFn, [][]byte{binLoc, lenBuf, posBuf})
		if err != nil {
			return nil, err
		}
		if len(data) == 0 {
			break
		}
		content = append(content, data...)
		pos += int64(len(data))
	}
	return content, nil
}

// hexStringToBytes decodes an ASCII hex string into bytes (mirrors JDBC
// Hex.parserStringToByte). Hex digit pairs produce one byte each; a trailing
// odd digit is dropped.
func hexStringToBytes(s string) ([]byte, error) {
	out := make([]byte, 0, len(s)/2)
	var cur byte
	for i := 0; i < len(s); i++ {
		var half byte
		switch {
		case s[i] >= '0' && s[i] <= '9':
			half = s[i] - '0'
		case s[i] >= 'A' && s[i] <= 'F':
			half = s[i] - 'A' + 10
		case s[i] >= 'a' && s[i] <= 'f':
			half = s[i] - 'a' + 10
		default:
			return nil, fmt.Errorf("oscar: invalid hex LOB locator %q", s)
		}
		if i%2 == 1 {
			out = append(out, cur<<4|half)
		} else {
			cur = half
		}
	}
	return out, nil
}

// fetchLobContents replaces LOB locator values in result rows with the actual
// content. Fields come from res.fields or, on the prepared-statement reuse
// path, from the cached knownFields.
func (c *conn) fetchLobContents(res *queryResult, knownFields []field) error {
	fields := res.fields
	if len(fields) == 0 {
		fields = knownFields
	}
	hasLob := false
	for _, f := range fields {
		if lobTypeForOscarType(f.OscarType) != 0 {
			hasLob = true
			break
		}
	}
	if !hasLob {
		return nil
	}
	for ri := range res.rows {
		row := res.rows[ri]
		for ci := range fields {
			if ci >= len(row) {
				break
			}
			if lobTypeForOscarType(fields[ci].OscarType) == 0 || row[ci] == nil {
				continue
			}
			loc, ok := row[ci].([]byte)
			if !ok {
				continue
			}
			content, err := c.readLobContent(fields[ci], loc)
			if err != nil {
				return err
			}
			row[ci] = content
		}
	}
	return nil
}
