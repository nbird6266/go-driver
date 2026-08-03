package oscar

import (
	"context"
	"database/sql"
	"database/sql/driver"
	"fmt"
)

const DriverName = "oscar"

func init() {
	sql.Register(DriverName, &oscarDriver{})
}

type oscarDriver struct{}

func (d *oscarDriver) Open(name string) (driver.Conn, error) {
	cfg, err := parseDSN(name)
	if err != nil {
		return nil, err
	}
	return openConn(cfg)
}

type tx struct {
	conn *conn
}

func (t *tx) Commit() error {
	_, err := t.conn.ExecContext(context.Background(), "commit", nil)
	if err == nil {
		t.conn.markTxOpen(false)
	}
	return err
}

func (t *tx) Rollback() error {
	_, err := t.conn.ExecContext(context.Background(), "rollback", nil)
	if err == nil {
		t.conn.markTxOpen(false)
	}
	return err
}

type result struct {
	rowsAffected int64
	lastInsertID int64
}

func (r result) LastInsertId() (int64, error) {
	if r.lastInsertID == 0 {
		return 0, fmt.Errorf("oscar: last insert id is unavailable")
	}
	return r.lastInsertID, nil
}

func (r result) RowsAffected() (int64, error) {
	return r.rowsAffected, nil
}

// Compile-time interface assertions: every optional database/sql driver
// interface the connection implements is verified here.
var (
	_ driver.Conn               = (*conn)(nil)
	_ driver.ConnBeginTx        = (*conn)(nil)
	_ driver.ConnPrepareContext = (*conn)(nil)
	_ driver.ExecerContext      = (*conn)(nil)
	_ driver.QueryerContext     = (*conn)(nil)
	_ driver.Pinger             = (*conn)(nil)
	_ driver.SessionResetter    = (*conn)(nil)
	_ driver.Validator          = (*conn)(nil)
	_ driver.NamedValueChecker  = (*conn)(nil)
	_ driver.Stmt               = (*oscarStmt)(nil)
	_ driver.StmtExecContext    = (*oscarStmt)(nil)
	_ driver.StmtQueryContext   = (*oscarStmt)(nil)
	_ driver.Rows               = (*oscarRows)(nil)
	_ driver.RowsColumnTypeDatabaseTypeName = (*oscarRows)(nil)
	_ driver.RowsColumnTypeScanType         = (*oscarRows)(nil)
	_ driver.RowsColumnTypeLength           = (*oscarRows)(nil)
	_ driver.RowsColumnTypeNullable         = (*oscarRows)(nil)
	_ driver.Tx                 = (*tx)(nil)
	_ driver.Result             = result{}
)
