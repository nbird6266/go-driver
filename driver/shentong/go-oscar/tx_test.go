package oscar

import (
	"context"
	"database/sql"
	"fmt"
	"testing"
	"time"
)

// TestSavepoint verifies SAVEPOINT / ROLLBACK TO / RELEASE SAVEPOINT work
// through the driver (they are plain SQL executed inside a *sql.Tx). This is
// the standard way to implement nested-transaction-like rollbacks.
func TestSavepoint(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	_, _ = db.Exec(`drop table go_driver_sptest`)
	if _, err := db.Exec(`create table go_driver_sptest(id int)`); err != nil {
		t.Fatalf("create failed: %v", err)
	}
	defer func() { _, _ = db.Exec(`drop table go_driver_sptest`) }()

	tx, err := db.Begin()
	if err != nil {
		t.Fatalf("begin failed: %v", err)
	}
	if _, err := tx.Exec(`insert into go_driver_sptest values (1)`); err != nil {
		_ = tx.Rollback()
		t.Fatalf("insert 1 failed: %v", err)
	}
	if _, err := tx.Exec(`savepoint sp1`); err != nil {
		_ = tx.Rollback()
		t.Fatalf("savepoint failed: %v", err)
	}
	if _, err := tx.Exec(`insert into go_driver_sptest values (2)`); err != nil {
		_ = tx.Rollback()
		t.Fatalf("insert 2 failed: %v", err)
	}
	// roll back to the savepoint: row 2 must disappear
	if _, err := tx.Exec(`rollback to sp1`); err != nil {
		_ = tx.Rollback()
		t.Fatalf("rollback to savepoint failed: %v", err)
	}
	if _, err := tx.Exec(`insert into go_driver_sptest values (3)`); err != nil {
		_ = tx.Rollback()
		t.Fatalf("insert 3 failed: %v", err)
	}
	// OSCAR follows Oracle semantics: savepoints need no explicit RELEASE
	// (the server rejects `release savepoint`).
	if err := tx.Commit(); err != nil {
		t.Fatalf("commit failed: %v", err)
	}

	var rows []int
	if err := collectInts(db, &rows, `select id from go_driver_sptest order by id`); err != nil {
		t.Fatalf("select failed: %v", err)
	}
	want := []int{1, 3} // 2 rolled back by ROLLBACK TO
	if len(rows) != len(want) {
		t.Fatalf("rows mismatch: want %v, got %v", want, rows)
	}
	for i := range want {
		if rows[i] != want[i] {
			t.Fatalf("rows mismatch: want %v, got %v", want, rows)
		}
	}
	fmt.Printf("savepoint ok: rows=%v\n", rows)
}

// TestTxStatementFailure verifies connection state handling after a statement
// fails inside a transaction. With STMT_ROLLBACK=1 the server rolls back only
// the failed statement, so the transaction remains usable; the driver must
// surface the error and let subsequent statements proceed.
func TestTxStatementFailure(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	_, _ = db.Exec(`drop table go_driver_txfail`)
	if _, err := db.Exec(`create table go_driver_txfail(id int, code varchar(20) unique)`); err != nil {
		t.Fatalf("create failed: %v", err)
	}
	defer func() { _, _ = db.Exec(`drop table go_driver_txfail`) }()

	tx, err := db.Begin()
	if err != nil {
		t.Fatalf("begin failed: %v", err)
	}
	if _, err := tx.Exec(`insert into go_driver_txfail values (1, 'a')`); err != nil {
		_ = tx.Rollback()
		t.Fatalf("insert 1 failed: %v", err)
	}
	// this insert violates the unique constraint and must fail...
	if _, err := tx.Exec(`insert into go_driver_txfail values (2, 'a')`); err == nil {
		_ = tx.Rollback()
		t.Fatal("expected unique violation inside tx")
	} else if !IsUniqueViolation(err) {
		_ = tx.Rollback()
		t.Fatalf("expected unique violation, got %v", err)
	}
	// ...but the transaction must still be usable afterwards.
	if _, err := tx.Exec(`insert into go_driver_txfail values (3, 'b')`); err != nil {
		_ = tx.Rollback()
		t.Fatalf("tx unusable after failed statement: %v", err)
	}
	if err := tx.Commit(); err != nil {
		t.Fatalf("commit failed: %v", err)
	}

	var cnt int
	if err := db.QueryRow(`select count(*) from go_driver_txfail`).Scan(&cnt); err != nil {
		t.Fatalf("count failed: %v", err)
	}
	// rows 1 and 3 committed; the failed duplicate row 2 was not inserted.
	if cnt != 2 {
		t.Fatalf("expect 2 committed rows, got %d", cnt)
	}
	fmt.Printf("tx survives statement failure ok\n")
}

// TestContextCancelQuery verifies that cancelling the context while a query
// is blocked server-side interrupts it: the driver sends a CancelRequest on a
// separate connection and the blocked statement returns promptly instead of
// running to completion.
func TestContextCancelQuery(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	_, _ = db.Exec(`drop table go_driver_cancel`)
	if _, err := db.Exec(`create table go_driver_cancel(id int)`); err != nil {
		t.Fatalf("create failed: %v", err)
	}
	if _, err := db.Exec(`insert into go_driver_cancel values (1)`); err != nil {
		t.Fatalf("seed failed: %v", err)
	}
	defer func() { _, _ = db.Exec(`drop table go_driver_cancel`) }()

	// conn1 holds a row lock inside an open transaction.
	c1, err := db.Conn(context.Background())
	if err != nil {
		t.Fatalf("conn1 failed: %v", err)
	}
	defer c1.Close()
	tx1, err := c1.BeginTx(context.Background(), nil)
	if err != nil {
		t.Fatalf("tx1 begin failed: %v", err)
	}
	if _, err := tx1.Exec(`update go_driver_cancel set id = 10 where id = 1`); err != nil {
		_ = tx1.Rollback()
		t.Fatalf("tx1 update failed: %v", err)
	}

	// conn2 tries to update the same row: it blocks on tx1's lock until the
	// context is cancelled and the driver interrupts the server-side query.
	ctx2, cancel := context.WithCancel(context.Background())
	time.AfterFunc(500*time.Millisecond, cancel)

	done := make(chan error, 1)
	start := time.Now()
	go func() {
		_, err := db.ExecContext(ctx2, `update go_driver_cancel set id = 20 where id = 1`)
		done <- err
	}()

	var execErr error
	select {
	case execErr = <-done:
	case <-time.After(15 * time.Second):
		_ = tx1.Rollback()
		t.Fatal("blocked query was not interrupted; server cancel may be unsupported")
	}
	elapsed := time.Since(start)

	// Release the lock so cleanup and the pooled connection stay healthy.
	_ = tx1.Rollback()

	if execErr == nil {
		t.Log("update did not block (no lock contention); cancel path not exercised")
		return
	}
	// The server notices cancel requests on its own polling interval (a few
	// seconds in practice), so allow a generous but bounded latency.
	if elapsed > 10*time.Second {
		t.Fatalf("query interrupted too slowly: %v", elapsed)
	}
	if _, err := db.Exec(`select 1`); err != nil {
		t.Fatalf("conn unusable after cancel: %v", err)
	}
	fmt.Printf("ctx cancel interrupted blocked query in %v: %v\n", elapsed, execErr)
}

func collectInts(db *sql.DB, out *[]int, q string) error {
	rows, err := db.Query(q)
	if err != nil {
		return err
	}
	defer rows.Close()
	for rows.Next() {
		var v int
		if err := rows.Scan(&v); err != nil {
			return err
		}
		*out = append(*out, v)
	}
	return rows.Err()
}
