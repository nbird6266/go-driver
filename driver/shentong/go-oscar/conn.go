package oscar

import (
	"bufio"
	"context"
	"crypto/md5"
	"database/sql/driver"
	"encoding/hex"
	"errors"
	"fmt"
	"io"
	"net"
	"os"
	"reflect"
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/golang/snappy"
)

const (
	protoV1 = 1
	protoV2 = 2
	protoV3 = 3
)

var debugTrace = os.Getenv("OSCAR_DEBUG") == "1"

func tracef(format string, args ...any) {
	if debugTrace {
		fmt.Printf("[oscar-trace] "+format+"\n", args...)
	}
}

func printableTag(tag byte) byte {
	if tag >= 32 && tag < 127 {
		return tag
	}
	return '.'
}

type conn struct {
	cfg                config
	loginUser          string
	loginDatabase      string
	netConn            net.Conn
	br                 *bufio.Reader
	bw                 *bufio.Writer
	closed             bool
	protoVersion       int
	receiveStringByLen bool
	compress           bool
	pending            []byte
	pendingPos         int
	pid                int
	ckey               int
	transStatus        byte
	prepareCount       int
	mu                 sync.Mutex
	txOpen             bool
}

type field struct {
	Name      string
	TypeOID   int
	OscarType int
}

// oscarTypeForOID maps a server type OID to the internal OSCAR type code used
// on the wire. It mirrors JDBC OscarJdbc2Connection.oscarTypeCache. A return
// value of 0 means the OID is unknown and the raw OID is used instead.
func oscarTypeForOID(oid int) int {
	switch oid {
	case 20, 21, 23, 972: // bigint, smallint, int, tinyint
		return 23 // int family (base-100 number format)
	case 16: // boolean
		return 33
	case 700, 701, 1700, 2174, 2175, 2315: // real, double, numeric, lpfloat, hpfloat, decimal
		return 34 // numeric family (ASCII string format)
	case 17, 19, 25, 26, 1042, 1043, 1560, 1790, 2278, 3304: // bytea, name, text, oid, char, varchar, bit, refcursor, void, json
		return 24 // string family
	case 1365, 3100: // binary, varbinary
		return 35
	case 1082: // date
		return 25
	case 1083: // time
		return 26
	case 1266: // timetz
		return 27
	case 1114: // timestamp
		return 28
	case 1184: // timestamptz
		return 29
	case 1186: // interval year to month
		return 30
	case 1188: // interval day to second
		return 31
	case 3000: // blob
		return 50
	case 3001: // clob
		return 51
	case 3002: // bfile
		return 52
	case 1009, 1034: // varchar array, _aclitem
		return 2003
	}
	return 0
}

// dbTypeName maps a server type OID to its database type name, mirroring the
// JDBC driver's DBTypeCache (OscarJdbc2Connection.java). Unknown OIDs return
// an empty string.
func dbTypeName(oid int) string {
	switch oid {
	case 972:
		return "tinyint"
	case 21:
		return "smallint"
	case 23:
		return "int"
	case 26:
		return "OID"
	case 20:
		return "bigint"
	case 1560:
		return "bit"
	case 16:
		return "boolean"
	case 1700:
		return "numeric"
	case 2315:
		return "decimal"
	case 700:
		return "real"
	case 2174, 2175:
		return "float"
	case 701:
		return "double precision"
	case 1042:
		return "char"
	case 1043:
		return "varchar"
	case 25:
		return "text"
	case 19:
		return "name"
	case 18:
		return "bpchar"
	case 1365:
		return "binary"
	case 3100:
		return "varbinary"
	case 1082:
		return "date"
	case 1083:
		return "time"
	case 1114:
		return "timestamp"
	case 1266:
		return "timetz"
	case 1184:
		return "timestamptz"
	case 1186:
		return "INTERVALYTM"
	case 1188:
		return "INTERVALDTS"
	case 3000:
		return "blob"
	case 3001:
		return "clob"
	case 3002:
		return "bfile"
	case 17:
		return "BYTEA"
	case 2278:
		return "VOID"
	case 1790:
		return "REFCURSOR"
	case 3304:
		return "json"
	}
	return ""
}

type queryResult struct {
	fields       []field
	rows         [][]driver.Value
	command      string
	rowsAffected int64
	lastInsertID int64
	paramOIDs    []int // server-inferred parameter type OIDs from 'p' packets
}

type oscarRows struct {
	columns []string
	fields  []field
	values  [][]driver.Value
	index   int
}

func openConn(cfg config) (*conn, error) {
	c := &conn{
		cfg:                cfg,
		loginUser:          normalizeIdentifier(cfg.User),
		loginDatabase:      normalizeIdentifier(cfg.Database),
		receiveStringByLen: true,
	}
	if err := c.startup(); err != nil {
		return nil, err
	}
	if err := c.initSession(); err != nil {
		_ = c.closeNet()
		return nil, err
	}
	return c, nil
}

// initSession mirrors the JDBC driver's post-connect setup, most importantly
// turning on server-side autocommit (the server default does not commit
// statements automatically), binary data transfer and full numeric precision.
func (c *conn) initSession() error {
	required := []string{
		"SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL READ COMMITTED",
		"SET AUTOCOMMIT TO TRUE",
		"SET NET_DATA_BY_STR=FALSE",
		"SET SEND_FLOATINGNUMBER_KEEP_PRECISION=TRUE",
		"SET DISPLAY_LOBLOCATOR=ON",
	}
	for _, sql := range required {
		if _, err := c.query(context.Background(), sql); err != nil {
			return err
		}
	}
	// STMT_ROLLBACK is optional and tolerated by the JDBC driver.
	if _, err := c.query(context.Background(), "SET STMT_ROLLBACK=1"); err != nil {
		return nil
	}
	return nil
}

func (c *conn) startup() error {
	port := c.cfg.Port
	for redirects := 0; redirects < 4; redirects++ {
		c.cfg.Port = port
		if err := c.connect(); err != nil {
			return err
		}
		redirectPort, err := c.startupOnce()
		if err != nil {
			_ = c.closeNet()
			return err
		}
		if redirectPort == 0 {
			c.cfg.Port = port
			return nil
		}
		_ = c.closeNet()
		port = redirectPort
	}
	return fmt.Errorf("oscar: too many listener redirects")
}

func (c *conn) startupOnce() (int, error) {
	if err := c.sendStartupPacket(); err != nil {
		return 0, err
	}

	authDone := false
	for !authDone {
		tag, err := c.readByte()
		if err != nil {
			return 0, err
		}
		switch tag {
		case 'L':
			_, port, err := c.readListenerResponse()
			return port, err
		case 'O':
			if _, err := c.readByte(); err != nil {
				return 0, err
			}
		case 'R', 0xA1, 0xB1:
			policy, salt, version, err := c.readAuthentication(tag)
			if err != nil {
				return 0, err
			}
			if version > 0 {
				c.protoVersion = version
			}
			if policy == 0 {
				authDone = true
				continue
			}
			if err := c.sendPassword(policy, salt); err != nil {
				return 0, err
			}
		case 'E':
			return 0, c.readErrorPacket()
		case 'N':
			if _, err := c.readNoticePacket(); err != nil {
				return 0, err
			}
		default:
			return 0, fmt.Errorf("oscar: unexpected startup tag 0x%X", tag)
		}
	}

	for {
		tag, err := c.readByte()
		if err != nil {
			return 0, err
		}
		switch tag {
		case 'K':
			pid, ckey, err := c.readBackendKey()
			if err != nil {
				return 0, err
			}
			c.pid = pid
			c.ckey = ckey
		case 'N':
			if _, err := c.readNoticePacket(); err != nil {
				return 0, err
			}
		case 'E':
			return 0, c.readErrorPacket()
		case 'Z':
			if err := c.readReadyForQuery(); err != nil {
				return 0, err
			}
			if c.protoVersion >= protoV2 {
				c.compress = true
			}
			return 0, nil
		default:
			return 0, fmt.Errorf("oscar: unexpected ready tag 0x%X", tag)
		}
	}
}

func (c *conn) connect() error {
	if c.netConn != nil {
		_ = c.closeNet()
	}
	addr := net.JoinHostPort(c.cfg.Host, strconv.Itoa(c.cfg.Port))
	netConn, err := net.DialTimeout("tcp", addr, c.cfg.DialTimeout)
	if err != nil {
		return err
	}
	c.netConn = netConn
	c.br = bufio.NewReaderSize(netConn, 8192)
	c.bw = bufio.NewWriterSize(netConn, 8192)
	c.compress = false
	c.pending = nil
	c.pendingPos = 0
	return nil
}

func (c *conn) closeNet() error {
	if c.netConn == nil {
		return nil
	}
	err := c.netConn.Close()
	c.netConn = nil
	c.br = nil
	c.bw = nil
	c.pending = nil
	c.pendingPos = 0
	return err
}

func (c *conn) Prepare(query string) (driver.Stmt, error) {
	if c.closed || c.netConn == nil {
		return nil, driver.ErrBadConn
	}
	c.prepareCount++
	name := "J" + strconv.Itoa(c.prepareCount)
	return &oscarStmt{
		conn:          c,
		query:         query,
		statementName: name,
		prepareSQL:    "PREPARE " + name + " AS " + query,
	}, nil
}

// PrepareContext is the context-aware variant of Prepare. The server-side
// PREPARE is deferred until first execution, so this only needs to check the
// context and delegate.
func (c *conn) PrepareContext(ctx context.Context, query string) (driver.Stmt, error) {
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	return c.Prepare(query)
}

func (c *conn) Close() error {
	c.closed = true
	return c.closeNet()
}

func (c *conn) Begin() (driver.Tx, error) {
	return c.BeginTx(context.Background(), driver.TxOptions{})
}

// txIsolationSQL maps a database/sql isolation level to OSCAR SQL. The
// database/sql isolation constants are sequential: default=0, read
// uncommitted=1, read committed=2, repeatable read=3, serializable=4. The
// session default is READ COMMITTED, so only non-default levels issue a
// SET TRANSACTION statement.
func txIsolationSQL(iso driver.IsolationLevel) string {
	switch int(iso) {
	case 0, 2:
		return ""
	case 1:
		return "set transaction isolation level read uncommitted"
	case 3:
		return "set transaction isolation level repeatable read"
	case 4:
		return "set transaction isolation level serializable"
	default:
		return ""
	}
}

func (c *conn) BeginTx(ctx context.Context, opts driver.TxOptions) (driver.Tx, error) {
	if c.closed || c.netConn == nil {
		return nil, driver.ErrBadConn
	}
	if _, err := c.ExecContext(ctx, "begin", nil); err != nil {
		return nil, err
	}
	if sql := txIsolationSQL(opts.Isolation); sql != "" {
		if _, err := c.ExecContext(ctx, sql, nil); err != nil {
			// Abort the transaction on failure so the connection is not left
			// in a half-configured transaction state.
			_, _ = c.ExecContext(ctx, "rollback", nil)
			c.mu.Lock()
			c.txOpen = false
			c.mu.Unlock()
			return nil, err
		}
	}
	if opts.ReadOnly {
		if _, err := c.ExecContext(ctx, "set transaction read only", nil); err != nil {
			_, _ = c.ExecContext(ctx, "rollback", nil)
			c.mu.Lock()
			c.txOpen = false
			c.mu.Unlock()
			return nil, err
		}
	}
	c.mu.Lock()
	c.txOpen = true
	c.mu.Unlock()
	return &tx{conn: c}, nil
}

// IsValid reports whether the underlying network connection is still usable.
// It is part of driver.Validator, used by database/sql when a connection is
// returned to the pool.
func (c *conn) IsValid() bool {
	return !c.closed && c.netConn != nil
}

// ResetSession returns the connection to a clean state before it is reused by
// the pool: any transaction left open is rolled back. It is part of
// driver.SessionResetter. Together with IsValid it lets database/sql keep and
// reuse the connection after a transaction rollback (keepConnOnRollback).
//
// The common path (no open transaction) returns immediately without a network
// round-trip.
func (c *conn) ResetSession(ctx context.Context) error {
	c.mu.Lock()
	open := c.txOpen
	c.mu.Unlock()
	if !open {
		return nil
	}
	if _, err := c.query(ctx, "rollback"); err != nil {
		return driver.ErrBadConn
	}
	c.mu.Lock()
	c.txOpen = false
	c.mu.Unlock()
	return nil
}

func (c *conn) markTxOpen(open bool) {
	c.mu.Lock()
	c.txOpen = open
	c.mu.Unlock()
}

func (c *conn) Ping(ctx context.Context) error {
	res, err := c.query(ctx, "select 1")
	if err != nil {
		return err
	}
	if len(res.rows) == 0 {
		return errors.New("oscar: ping returned no rows")
	}
	return nil
}

// CheckNamedValue is part of driver.NamedValueChecker. driver.Valuer values
// (sql.NullString, sql.NullInt64, ...) are unwrapped through the default
// converter; the value types convertBindValue handles natively pass through
// untouched; anything else is rejected with a clear error instead of being
// silently stringified.
func (c *conn) CheckNamedValue(nv *driver.NamedValue) error {
	switch nv.Value.(type) {
	case nil, bool,
		int, int8, int16, int32, int64,
		uint, uint8, uint16, uint32, uint64,
		float32, float64, string, []byte, time.Time:
		return nil
	default:
		v, err := driver.DefaultParameterConverter.ConvertValue(nv.Value)
		if err != nil {
			return err
		}
		nv.Value = v
		return nil
	}
}

func (c *conn) ExecContext(ctx context.Context, query string, args []driver.NamedValue) (driver.Result, error) {
	if len(args) == 0 {
		res, err := c.query(ctx, query)
		if err != nil {
			return nil, err
		}
		return result{
			rowsAffected: res.rowsAffected,
			lastInsertID: res.lastInsertID,
		}, nil
	}

	// With parameters the query is sent through the prepared-statement
	// path (ExecutePacket with the PREPARE SQL on first execution).
	stmt, err := c.Prepare(query)
	if err != nil {
		return nil, err
	}
	defer stmt.Close()
	res, err := stmt.(*oscarStmt).exec(ctx, args)
	if err != nil {
		return nil, err
	}
	return result{
		rowsAffected: res.rowsAffected,
		lastInsertID: res.lastInsertID,
	}, nil
}

func (c *conn) QueryContext(ctx context.Context, query string, args []driver.NamedValue) (driver.Rows, error) {
	if len(args) == 0 {
		res, err := c.query(ctx, query)
		if err != nil {
			return nil, err
		}
		return newRows(res), nil
	}

	stmt, err := c.Prepare(query)
	if err != nil {
		return nil, err
	}
	defer stmt.Close()
	res, err := stmt.(*oscarStmt).exec(ctx, args)
	if err != nil {
		return nil, err
	}
	return newRows(res), nil
}

func newRows(res queryResult) *oscarRows {
	columns := make([]string, 0, len(res.fields))
	for _, f := range res.fields {
		columns = append(columns, f.Name)
	}
	return &oscarRows{
		columns: columns,
		fields:  res.fields,
		values:  res.rows,
	}
}

func (c *conn) query(ctx context.Context, sql string) (queryResult, error) {
	return c.queryPacket(ctx, nil, func() error { return c.sendQuery(sql) })
}

// queryPacket sends a query/execute packet via send and drains the response
// loop until ReadyForQuery. It is shared by plain queries and prepared
// executions (ExecutePacket). knownFields carries the row description cached
// from a previous execution: on the prepared-statement reuse path the server
// omits the 'T' RowDescription and sends 'D' rows directly.
func (c *conn) queryPacket(ctx context.Context, knownFields []field, send func() error) (queryResult, error) {
	var res queryResult
	if c.closed || c.netConn == nil {
		return res, driver.ErrBadConn
	}

	// Watch for context cancellation while the query is in flight. When the
	// context fires, send a cancel request to the server on a separate
	// connection so a long-running query is interrupted instead of running to
	// completion (mirrors JDBC Statement.cancel / OSCARProtocol.cancelRequest).
	if ctx.Done() != nil {
		stopCancel := make(chan struct{})
		defer close(stopCancel)
		go func() {
			select {
			case <-ctx.Done():
				if pid, ckey := c.backendKey(); pid != 0 {
					tracef("conn=%p ctx done, sending cancel request (pid=%d)", c, pid)
					_ = c.cancelRequest(pid, ckey)
				}
			case <-stopCancel:
			}
		}()
	}

	if deadline, ok := ctx.Deadline(); ok {
		if err := c.netConn.SetDeadline(deadline); err != nil {
			return res, err
		}
		defer c.netConn.SetDeadline(time.Time{})
	}

	if err := send(); err != nil {
		return res, err
	}

	var qerr error
	for {
		tag, err := c.readByte()
		if err != nil {
			return res, err
		}
		tracef("conn=%p recv tag 0x%02X ('%c')", c, tag, printableTag(tag))
		switch tag {
		case 'T':
			res.fields, err = c.readRowDescription()
			if err != nil {
				return res, err
			}
		case 'D':
			fields := res.fields
			if len(fields) == 0 {
				fields = knownFields
			}
			row, err := c.readDataRow(fields)
			if err != nil {
				return res, err
			}
			res.rows = append(res.rows, row)
		case 'C':
			res.command, err = c.readString()
			if err != nil {
				return res, err
			}
			tracef("conn=%p command: %q", c, res.command)
		case 'I':
			// empty query response has no payload
		case 'N':
			if _, err := c.readNoticePacket(); err != nil {
				return res, err
			}
		case 'E':
			if qerr == nil {
				qerr = c.readErrorPacket()
			} else {
				// already have an error; drain any additional error packet.
				_, _ = c.readNoticePacket()
			}
		case 'K':
			pid, ckey, err := c.readBackendKey()
			if err != nil {
				return res, err
			}
			c.setBackendKey(pid, ckey)
		case 0xA4: // PlanIDPacket: 2-byte plan id, ignore
			if _, err := c.readN(2); err != nil {
				return res, err
			}
		case 'p': // ParamInforPacket: parameter metadata, drain it
			if res.paramOIDs, err = c.readParamInfo(); err != nil {
				return res, err
			}
		case 'Z':
			if err := c.readReadyForQuery(); err != nil {
				return res, err
			}
			if qerr != nil {
				return res, qerr
			}
			res.rowsAffected, res.lastInsertID = parseCommandTag(res.command)
			if err := c.fetchLobContents(&res, knownFields); err != nil {
				return res, err
			}
			return res, nil
		default:
			return res, fmt.Errorf("oscar: unexpected query tag 0x%X", tag)
		}
	}
}

// readParamInfo drains a 'p' ParamInforPacket: paramCount(2) followed by
// per-parameter name (NUL-terminated), typeOID(4), typeSize(4), typeDes(4),
// isNull(1), mode(1) and, for protocol V3, tableOID(4) + columnIndex(2). It
// returns the server-inferred type OID of each parameter, which the driver
// uses to choose value encodings (e.g. raw bytes vs hex for []byte binds).
func (c *conn) readParamInfo() ([]int, error) {
	count, err := c.readUint16()
	if err != nil {
		return nil, err
	}
	oids := make([]int, 0, count)
	for i := 0; i < int(count); i++ {
		if _, err := c.readCString(); err != nil {
			return nil, err
		}
		oid, err := c.readUint32()
		if err != nil {
			return nil, err
		}
		oids = append(oids, int(oid))
		if _, err := c.readN(4); err != nil { // typeSize
			return nil, err
		}
		if _, err := c.readN(4); err != nil { // typeDes
			return nil, err
		}
		if _, err := c.readN(1); err != nil { // isNull
			return nil, err
		}
		if _, err := c.readN(1); err != nil { // mode
			return nil, err
		}
		if c.protoVersion >= protoV3 {
			if _, err := c.readN(6); err != nil { // tableOID(4) + columnIndex(2)
				return nil, err
			}
		}
	}
	return oids, nil
}

func (r *oscarRows) Columns() []string {
	return r.columns
}

func (r *oscarRows) Close() error {
	r.index = len(r.values)
	return nil
}

func (r *oscarRows) Next(dest []driver.Value) error {
	if r.index >= len(r.values) {
		return io.EOF
	}
	row := r.values[r.index]
	r.index++
	for i := range dest {
		if i < len(row) {
			dest[i] = row[i]
		} else {
			dest[i] = nil
		}
	}
	return nil
}

// ColumnTypeDatabaseTypeName returns the database type name for a column,
// mirroring JDBC's DBTypeCache (OID -> type name).
func (r *oscarRows) ColumnTypeDatabaseTypeName(index int) string {
	if index < 0 || index >= len(r.fields) {
		return ""
	}
	return dbTypeName(r.fields[index].TypeOID)
}

// ColumnTypeScanType returns the Go type used when scanning a value into a
// non-pointer destination. Numeric columns are decoded to int64; everything
// else is handed to database/sql as raw []byte for conversion.
func (r *oscarRows) ColumnTypeScanType(index int) reflect.Type {
	if index < 0 || index >= len(r.fields) {
		return nil
	}
	f := r.fields[index]
	ot := f.OscarType
	if ot == 0 {
		ot = oscarTypeForOID(f.TypeOID)
	}
	switch ot {
	case 23:
		return reflect.TypeOf(int64(0))
	case 33:
		return reflect.TypeOf(false)
	case 34:
		return reflect.TypeOf(float64(0))
	case 25, 26, 27, 28, 29: // date, time, timetz, timestamp, timestamptz
		return reflect.TypeOf(time.Time{})
	default:
		return reflect.TypeOf([]byte(nil))
	}
}

// ColumnTypeLength reports a column's declared length. The RowDescription
// carries a type modifier but its encoding is not decoded, so this reports
// "unknown" (ok=false).
func (r *oscarRows) ColumnTypeLength(index int) (int64, bool) {
	return 0, false
}

// ColumnTypeNullable reports whether a column may be null. The protocol does
// not carry nullability, so this reports "unknown" (ok=false).
func (r *oscarRows) ColumnTypeNullable(index int) (bool, bool) {
	return true, false
}

func (c *conn) sendStartupPacket() error {
	dbSize := 64
	userSize := 32
	dbBytes := []byte(c.loginDatabase)
	userBytes := []byte(c.loginUser)
	if len(dbBytes) > dbSize-1 || len(userBytes) > userSize-1 {
		dbSize = 128
		userSize = 128
	}

	totalLen := 8 + dbSize + userSize + 64 + 6 + 2 + 2 + 54 + 64 + 32 + 1940 + 4 + 4 + 4 + 64 + 32 + 4 + 64
	hostName, _ := os.Hostname()
	if hostName == "" {
		hostName = "localhost"
	}

	if err := c.writeUint32(uint32(totalLen)); err != nil {
		return err
	}
	if err := c.writeUint16(2); err != nil {
		return err
	}
	if err := c.writeUint16(0); err != nil {
		return err
	}
	if err := c.writeFixedString(c.loginDatabase, dbSize); err != nil {
		return err
	}
	if err := c.writeFixedString(c.loginUser, userSize+64); err != nil {
		return err
	}
	if err := c.writeCompatibilityBlock(); err != nil {
		return err
	}
	if err := c.writeFixedString("", 64); err != nil {
		return err
	}
	if err := c.writeFixedString("", 32); err != nil {
		return err
	}
	for i := 0; i < 20; i++ {
		if err := c.writeFixedString("", 97); err != nil {
			return err
		}
	}
	if err := c.writeUint32(0); err != nil {
		return err
	}
	if err := c.writeUint32(0); err != nil {
		return err
	}
	if err := c.writeUint32(0); err != nil {
		return err
	}
	if err := c.writeFixedString(hostName, 64); err != nil {
		return err
	}
	if err := c.writeFixedString("", 32); err != nil {
		return err
	}
	if err := c.writeUint32(0); err != nil {
		return err
	}
	if err := c.writeFixedString("go-driver", 64); err != nil {
		return err
	}
	return c.bw.Flush()
}

func (c *conn) writeCompatibilityBlock() error {
	if err := c.writeByte(0xFF); err != nil {
		return err
	}
	if err := c.writeByte(6); err != nil {
		return err
	}
	if err := c.writeByte(0); err != nil {
		return err
	}
	if err := c.writeByte(4); err != nil {
		return err
	}
	if err := c.writeByte(1); err != nil {
		return err
	}
	if err := c.writeByte(0xFF); err != nil {
		return err
	}
	if err := c.writeUint16(1); err != nil {
		return err
	}
	if err := c.writeUint16(4); err != nil {
		return err
	}
	return c.writeFixedString("", 54)
}

func (c *conn) sendPassword(policy int, salt []byte) error {
	passwordBytes := []byte(c.cfg.Password)
	switch policy {
	case 3:
		return c.sendPasswordBytes(passwordBytes)
	case 5:
		return c.sendPasswordBytes(md5Password([]byte(c.loginUser), passwordBytes, salt))
	default:
		return fmt.Errorf("oscar: unsupported auth policy %d", policy)
	}
}

func (c *conn) sendPasswordBytes(password []byte) error {
	if err := c.writeUint32(uint32(5 + len(password))); err != nil {
		return err
	}
	if _, err := c.bw.Write(password); err != nil {
		return err
	}
	if err := c.writeByte(0); err != nil {
		return err
	}
	return c.bw.Flush()
}

func (c *conn) sendQuery(sql string) error {
	tag := byte('Q')
	if c.protoVersion >= protoV2 {
		tag = 1
	}
	if err := c.writeByte(tag); err != nil {
		return err
	}
	if err := c.writeUint16(0); err != nil {
		return err
	}
	if _, err := c.bw.Write([]byte(sql)); err != nil {
		return err
	}
	if err := c.writeByte(0); err != nil {
		return err
	}
	return c.bw.Flush()
}

func (c *conn) readAuthentication(tag byte) (policy int, salt []byte, version int, err error) {
	switch tag {
	case 'R':
		version = protoV1
	case 0xA1:
		version = protoV2
	case 0xB1:
		var vn uint16
		vn, err = c.readUint16()
		if err != nil {
			return
		}
		version = int(vn)
	default:
		err = fmt.Errorf("oscar: invalid auth tag 0x%X", tag)
		return
	}
	var p uint32
	p, err = c.readUint32()
	if err != nil {
		return
	}
	policy = int(p)
	if policy == 5 {
		salt, err = c.readN(4)
	}
	return
}

func (c *conn) readListenerResponse() (version, port int, err error) {
	var v, p uint32
	v, err = c.readUint32()
	if err != nil {
		return
	}
	p, err = c.readUint32()
	if err != nil {
		return
	}
	return int(v), int(p), nil
}

func (c *conn) readBackendKey() (pid, ckey int, err error) {
	var p, k uint32
	p, err = c.readUint32()
	if err != nil {
		return
	}
	k, err = c.readUint32()
	if err != nil {
		return
	}
	return int(p), int(k), nil
}

// backendKey returns the server process id / cancel key, guarded for access
// from the context-cancellation watcher goroutine.
func (c *conn) backendKey() (pid, ckey int) {
	c.mu.Lock()
	defer c.mu.Unlock()
	return c.pid, c.ckey
}

func (c *conn) setBackendKey(pid, ckey int) {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.pid = pid
	c.ckey = ckey
}

func (c *conn) readReadyForQuery() error {
	if c.protoVersion >= protoV3 {
		b, err := c.readByte()
		if err != nil {
			return err
		}
		c.transStatus = b
		_, err = c.readN(8)
		return err
	}
	return nil
}

func (c *conn) readErrorPacket() error {
	code, err := c.readUint32()
	if err != nil {
		return err
	}
	state, err := c.readCString()
	if err != nil {
		return err
	}
	msg, err := c.readCString()
	if err != nil {
		return err
	}
	return &OscarError{
		Code:     int(code),
		SQLState: state,
		Message:  msg,
	}
}

func (c *conn) readNoticePacket() (*OscarError, error) {
	code, err := c.readUint32()
	if err != nil {
		return nil, err
	}
	state, err := c.readCString()
	if err != nil {
		return nil, err
	}
	msg, err := c.readCString()
	if err != nil {
		return nil, err
	}
	return &OscarError{
		Code:     int(code),
		SQLState: state,
		Message:  msg,
	}, nil
}

func (c *conn) readRowDescription() ([]field, error) {
	count, err := c.readUint16()
	if err != nil {
		return nil, err
	}
	fields := make([]field, 0, count)
	for i := 0; i < int(count); i++ {
		if _, err := c.readByte(); err != nil {
			return nil, err
		}
		name, err := c.readString()
		if err != nil {
			return nil, err
		}
		alias, err := c.readString()
		if err != nil {
			return nil, err
		}
		if _, err := c.readString(); err != nil {
			return nil, err
		}
		if _, err := c.readString(); err != nil {
			return nil, err
		}
		typeOID, err := c.readUint32()
		if err != nil {
			return nil, err
		}
		if _, err := c.readUint16(); err != nil {
			return nil, err
		}
		if _, err := c.readUint32(); err != nil {
			return nil, err
		}
		colName := alias
		if colName == "" {
			colName = name
		}
		oid := int(typeOID)
		fields = append(fields, field{
			Name:      colName,
			TypeOID:   oid,
			OscarType: oscarTypeForOID(oid),
		})
	}
	tracef("conn=%p row description: %d columns", c, len(fields))
	return fields, nil
}

func (c *conn) readDataRow(fields []field) ([]driver.Value, error) {
	if c.protoVersion >= protoV2 {
		return c.readDataRowV2(fields)
	}
	return c.readDataRowV1(fields)
}

func (c *conn) readDataRowV1(fields []field) ([]driver.Value, error) {
	count := len(fields)
	bitmask, err := c.readN((count + 7) / 8)
	if err != nil {
		return nil, err
	}
	row := make([]driver.Value, count)
	whichBit := byte(128)
	whichByte := 0
	for i := 0; i < count; i++ {
		isNull := bitmask[whichByte]&whichBit == 0
		whichBit >>= 1
		if whichBit == 0 {
			whichBit = 128
			whichByte++
		}
		if isNull {
			row[i] = nil
			continue
		}
		l, err := c.readUint32()
		if err != nil {
			return nil, err
		}
		if l < 4 {
			row[i] = []byte{}
			continue
		}
		value, err := c.readN(int(l - 4))
		if err != nil {
			return nil, err
		}
		if isByteaType(fields[i].TypeOID) {
			row[i] = decodeByteaEscape(value)
		} else {
			row[i] = cloneBytes(value)
		}
	}
	return row, nil
}

func (c *conn) readDataRowV2(fields []field) ([]driver.Value, error) {
	count := len(fields)
	bitmask, err := c.readN((count + 7) / 8)
	if err != nil {
		return nil, err
	}
	row := make([]driver.Value, count)
	whichBit := byte(128)
	whichByte := 0
	for i := 0; i < count; i++ {
		isNull := bitmask[whichByte]&whichBit == 0
		whichBit >>= 1
		if whichBit == 0 {
			whichBit = 128
			whichByte++
		}
		if isNull {
			row[i] = nil
			continue
		}
		value, err := c.readValueV2(fields[i])
		if err != nil {
			return nil, err
		}
		switch {
		case isNumericType(fields[i].TypeOID):
			row[i] = decodeOscarNumber(value)
			tracef("conn=%p col %d type=%d decoded=%v", c, i, fields[i].TypeOID, row[i])
		case isBoolType(fields[i].TypeOID):
			row[i] = len(value) > 0 && value[0] != 0
			tracef("conn=%p col %d type=%d bool=%v", c, i, fields[i].TypeOID, row[i])
		case isDateTimeType(fields[i].TypeOID):
			row[i], err = decodeOscarDateTime(value, fields[i].OscarType)
			if err != nil {
				return nil, err
			}
			tracef("conn=%p col %d type=%d time=%v", c, i, fields[i].TypeOID, row[i])
		default:
			if isByteaType(fields[i].TypeOID) {
				// bytea/binary/varbinary values arrive as '\ooo' escaped text.
				row[i] = decodeByteaEscape(value)
			} else {
				row[i] = cloneBytes(value)
			}
			tracef("conn=%p col %d type=%d raw=%q", c, i, fields[i].TypeOID, value)
		}
	}
	return row, nil
}

// isByteaType reports whether a server type OID is a binary-string type whose
// values are transferred as escaped text ('\ooo' octal escapes for non-ASCII
// bytes) rather than raw binary.
func isByteaType(oid int) bool {
	switch oid {
	case 17, 1365, 3100: // bytea, binary, varbinary
		return true
	}
	return false
}

// decodeByteaEscape decodes Oscar's bytea text output. The server emits every
// byte as an octal escape '\ooo' except for printable ASCII, and escapes a
// literal backslash as '\\'. The driver applies the inverse on read so binary
// values round-trip byte-for-byte. A backslash not followed by a valid escape
// is kept as-is.
func decodeByteaEscape(b []byte) []byte {
	if len(b) == 0 {
		return b
	}
	hasEscape := false
	for i := 0; i < len(b); i++ {
		if b[i] == '\\' {
			hasEscape = true
			break
		}
	}
	if !hasEscape {
		return cloneBytes(b)
	}
	out := make([]byte, 0, len(b))
	for i := 0; i < len(b); i++ {
		if b[i] == '\\' && i+1 < len(b) {
			if b[i+1] == '\\' {
				out = append(out, '\\')
				i++
				continue
			}
			if i+3 < len(b) && isOctalDigit(b[i+1]) && isOctalDigit(b[i+2]) && isOctalDigit(b[i+3]) {
				out = append(out, (b[i+1]-'0')<<6|(b[i+2]-'0')<<3|(b[i+3]-'0'))
				i += 3
				continue
			}
		}
		out = append(out, b[i])
	}
	return out
}

func isOctalDigit(c byte) bool {
	return c >= '0' && c <= '7'
}

func (c *conn) readValueV2(f field) ([]byte, error) {
	ot := f.OscarType
	if ot == 0 {
		ot = oscarTypeForOID(f.TypeOID)
	}
	switch ot {
	case 50, 51, 52:
		l, err := c.readUint32()
		if err != nil {
			return nil, err
		}
		if l < 4 {
			return []byte{}, nil
		}
		return c.readN(int(l - 4))
	default:
		firstLen, err := c.readByte()
		if err != nil {
			return nil, err
		}
		if firstLen == 253 {
			next, err := c.readByte()
			if err != nil {
				return nil, err
			}
			return []byte{253, next}, nil
		}
		if firstLen > 240 {
			var value []byte
			for {
				chunkLen, err := c.readByte()
				if err != nil {
					return nil, err
				}
				if chunkLen == 0 {
					break
				}
				chunk, err := c.readN(int(chunkLen))
				if err != nil {
					return nil, err
				}
				value = append(value, chunk...)
			}
			return value, nil
		}
		return c.readN(int(firstLen))
	}
}

func (c *conn) readString() (string, error) {
	if c.receiveStringByLen {
		l, err := c.readByte()
		if err != nil {
			return "", err
		}
		buf, err := c.readN(int(l))
		if err != nil {
			return "", err
		}
		return string(buf), nil
	}

	var buf []byte
	for {
		b, err := c.readByte()
		if err != nil {
			return "", err
		}
		if b == 0 {
			return string(buf), nil
		}
		buf = append(buf, b)
	}
}

// readCString reads a NUL-terminated string (used by 'E'/'N' packets, which
// are always NUL-terminated regardless of receiveStringByLen).
func (c *conn) readCString() (string, error) {
	var buf []byte
	for {
		b, err := c.readByte()
		if err != nil {
			return "", err
		}
		if b == 0 {
			return string(buf), nil
		}
		buf = append(buf, b)
	}
}

func (c *conn) readN(n int) ([]byte, error) {
	buf := make([]byte, n)
	if err := c.readFull(buf); err != nil {
		return nil, err
	}
	return buf, nil
}

func (c *conn) readFull(dst []byte) error {
	if !c.compress {
		_, err := io.ReadFull(c.br, dst)
		return err
	}

	offset := 0
	for offset < len(dst) {
		if c.pendingPos >= len(c.pending) {
			if err := c.fillCompressedPacket(); err != nil {
				return err
			}
		}
		n := copy(dst[offset:], c.pending[c.pendingPos:])
		offset += n
		c.pendingPos += n
	}
	return nil
}

func (c *conn) readByte() (byte, error) {
	if !c.compress {
		return c.br.ReadByte()
	}
	if c.pendingPos >= len(c.pending) {
		if err := c.fillCompressedPacket(); err != nil {
			return 0, err
		}
	}
	b := c.pending[c.pendingPos]
	c.pendingPos++
	return b, nil
}

func (c *conn) fillCompressedPacket() error {
	header := make([]byte, 3)
	if _, err := io.ReadFull(c.br, header); err != nil {
		return err
	}
	length := int(header[1])<<8 | int(header[2])
	payload := make([]byte, length)
	if _, err := io.ReadFull(c.br, payload); err != nil {
		return err
	}
	switch header[0] {
	case 0xA2:
		decoded, err := snappy.Decode(nil, payload)
		if err != nil {
			return err
		}
		c.pending = decoded
	case 0xA3:
		c.pending = payload
	default:
		return fmt.Errorf("oscar: unsupported compressed frame 0x%X", header[0])
	}
	c.pendingPos = 0
	return nil
}

func (c *conn) readUint16() (uint16, error) {
	b, err := c.readN(2)
	if err != nil {
		return 0, err
	}
	return uint16(b[0])<<8 | uint16(b[1]), nil
}

func (c *conn) readUint32() (uint32, error) {
	b, err := c.readN(4)
	if err != nil {
		return 0, err
	}
	return uint32(b[0])<<24 | uint32(b[1])<<16 | uint32(b[2])<<8 | uint32(b[3]), nil
}

func (c *conn) writeByte(v byte) error {
	return c.bw.WriteByte(v)
}

func (c *conn) writeUint16(v uint16) error {
	_, err := c.bw.Write([]byte{byte(v >> 8), byte(v)})
	return err
}

func (c *conn) writeUint32(v uint32) error {
	_, err := c.bw.Write([]byte{byte(v >> 24), byte(v >> 16), byte(v >> 8), byte(v)})
	return err
}

func (c *conn) writeFixedString(s string, size int) error {
	buf := make([]byte, size)
	copy(buf, []byte(s))
	_, err := c.bw.Write(buf)
	return err
}

func md5Password(user, password, salt []byte) []byte {
	sum1 := md5.Sum(append(cloneBytes(password), user...))
	hex1 := make([]byte, 32)
	hex.Encode(hex1, sum1[:])
	raw := append(hex1, salt...)
	sum2 := md5.Sum(raw)
	out := make([]byte, 35)
	copy(out, []byte("md5"))
	hex.Encode(out[3:], sum2[:])
	return out
}

// parseRowidField decodes an Oscar bitmap-encoded rowid/updatecount field.
// The first byte is an 8-bit bitmap; bit i (tested LSB-first) marks whether
// byte i of the big-endian 8-byte result is present in the following bytes.
// Mirrors RowidConverter.convertToRowID / convertToUpdateCount.
func parseRowidField(b []byte, off int) (value int64, nextOff int) {
	if off >= len(b) {
		return 0, off
	}
	bitMap := b[off]
	off++
	var result int64
	for i := 0; i < 8; i++ {
		result <<= 8
		if bitMap&1 == 1 && off < len(b) {
			result |= int64(b[off])
			off++
		}
		bitMap >>= 1
	}
	return result, off
}

// parseCommandCount parses a command-tag count field. V1 servers send plain
// ASCII decimal; V2/V3 servers send the bitmap encoding above.
func parseCommandCount(b []byte, off int) (count int64, nextOff int) {
	if off >= len(b) {
		return 0, off
	}
	if b[off] >= '0' && b[off] <= '9' {
		end := off
		for end < len(b) && b[end] >= '0' && b[end] <= '9' {
			end++
		}
		count, _ = strconv.ParseInt(string(b[off:end]), 10, 64)
		return count, end
	}
	return parseRowidField(b, off)
}

// parseCommandTag parses the 'C' CompleteResponse command string.
// V1 format is ASCII decimal; V2/V3 fields are bitmap encoded
// (see RowidConverter). INSERT is "21 <count> <tid>"; UPDATE/DELETE are
// "20 <count>" / "25 <count>"; DDL and other no-row commands are "0X".
func parseCommandTag(command string) (rowsAffected int64, lastInsertID int64) {
	if len(command) < 2 {
		return 0, 0
	}
	tag1 := command[0]
	tag2 := command[1]
	cmd := []byte(command)
	switch {
	case tag1 == '2' && (tag2 == '0' || tag2 == '5'):
		// "20 <count>" / "25 <count>": UPDATE/DELETE affected rows.
		if idx := strings.IndexByte(command, ' '); idx >= 0 {
			rowsAffected, _ = parseCommandCount(cmd, idx+1)
		}
	case tag1 == '2' && tag2 == '1':
		// "21 <count> <tid>": INSERT affected rows and last insert id.
		first := strings.IndexByte(command, ' ')
		if first >= 0 {
			count, tidOff := parseCommandCount(cmd, first+1)
			rowsAffected = count
			if tidOff+1 < len(cmd) {
				lastInsertID, _ = parseRowidField(cmd, tidOff+1)
			}
		}
	case tag1 == '0':
		// "0X" / "0 <count>": DDL and other commands, no affected rows.
		rowsAffected = 0
	}
	return rowsAffected, lastInsertID
}

func cloneBytes(b []byte) []byte {
	if b == nil {
		return nil
	}
	out := make([]byte, len(b))
	copy(out, b)
	return out
}
