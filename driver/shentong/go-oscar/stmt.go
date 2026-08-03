package oscar

import (
	"context"
	"database/sql/driver"
)

// oscarStmt is a server-side prepared statement. The PREPARE SQL is not sent
// until the first execution, when it is delivered together with the bound
// parameters inside a single ExecutePacket (tag 0x0B); later executions reuse
// the server-side statement via tag 0x0D. Mirrors OscarStatementV2.
//
// When the statement is first executed with []byte parameters, the driver
// instead prepares explicitly ("PREPARE <name> AS <sql>" plus
// "GET PARAMINFO FOR <name>") so the server-inferred parameter types are
// known before binding: binary-string parameters (bytea/binary/varbinary)
// must be bound as raw bytes, not "0x"+hex text.
type oscarStmt struct {
	conn          *conn
	query         string
	statementName string
	prepareSQL    string // "PREPARE <name> AS <sql>", cleared after first execution
	prepared      bool   // whether the server-side statement exists
	fields        []field
	paramOIDs     []int // server-inferred parameter type OIDs (from ParamInfo)
	closed        bool
}

func (s *oscarStmt) Close() error {
	if s.closed {
		return nil
	}
	s.closed = true
	// Nothing to release if we never executed (and thus never prepared)
	// or the connection is already gone.
	if !s.prepared || s.conn == nil || s.conn.closed || s.conn.netConn == nil {
		return nil
	}
	_, err := s.conn.query(context.Background(), "DEALLOCATE PREPARE "+s.statementName)
	return err
}

func (s *oscarStmt) NumInput() int {
	return -1
}

func (s *oscarStmt) Exec(args []driver.Value) (driver.Result, error) {
	res, err := s.exec(context.Background(), toNamedValues(args))
	if err != nil {
		return nil, err
	}
	return result{
		rowsAffected: res.rowsAffected,
		lastInsertID: res.lastInsertID,
	}, nil
}

func (s *oscarStmt) Query(args []driver.Value) (driver.Rows, error) {
	res, err := s.exec(context.Background(), toNamedValues(args))
	if err != nil {
		return nil, err
	}
	return newRows(res), nil
}

func (s *oscarStmt) ExecContext(ctx context.Context, args []driver.NamedValue) (driver.Result, error) {
	res, err := s.exec(ctx, args)
	if err != nil {
		return nil, err
	}
	return result{
		rowsAffected: res.rowsAffected,
		lastInsertID: res.lastInsertID,
	}, nil
}

func (s *oscarStmt) QueryContext(ctx context.Context, args []driver.NamedValue) (driver.Rows, error) {
	res, err := s.exec(ctx, args)
	if err != nil {
		return nil, err
	}
	return newRows(res), nil
}

func (s *oscarStmt) exec(ctx context.Context, args []driver.NamedValue) (queryResult, error) {
	if s.closed {
		return queryResult{}, driver.ErrBadConn
	}
	// A fresh statement (not yet prepared) whose first execution carries
	// []byte parameters needs the server-inferred parameter types before the
	// values can be encoded correctly: prepare explicitly instead of using the
	// combined 0x0B prepare+execute packet.
	if !s.prepared && hasBytesArg(args) {
		if err := s.conn.prepareStatement(ctx, s); err != nil {
			return queryResult{}, err
		}
	}
	bindTypes, bindDatas, err := convertBindValues(args, s.paramOIDs)
	if err != nil {
		return queryResult{}, err
	}
	return s.conn.execPrepared(ctx, s, bindTypes, bindDatas)
}

// hasBytesArg reports whether any argument is a []byte, which is the only
// value type whose wire encoding depends on the parameter type.
func hasBytesArg(args []driver.NamedValue) bool {
	for _, a := range args {
		if _, ok := a.Value.([]byte); ok {
			return true
		}
	}
	return false
}

// prepareStatement prepares s on the server without executing it (plain
// "PREPARE <name> AS <sql>" query) and then fetches the server-inferred
// parameter type OIDs via "GET PARAMINFO FOR <name>". Afterwards executions
// use the reuse packet (0x0D) with the type-aware binding. Mirrors the JDBC
// driver's OscarStatement.prepare + GET PARAMINFO flow.
func (c *conn) prepareStatement(ctx context.Context, s *oscarStmt) error {
	if s.prepared {
		return nil
	}
	if _, err := c.query(ctx, "PREPARE "+s.statementName+" AS "+s.query); err != nil {
		return err
	}
	res, err := c.query(ctx, "GET PARAMINFO FOR "+s.statementName)
	if err != nil {
		return err
	}
	s.paramOIDs = res.paramOIDs
	s.prepared = true
	s.prepareSQL = "" // never use the combined 0x0B prepare+execute packet
	return nil
}

func toNamedValues(args []driver.Value) []driver.NamedValue {
	out := make([]driver.NamedValue, len(args))
	for i, v := range args {
		out[i] = driver.NamedValue{Ordinal: i + 1, Value: v}
	}
	return out
}
