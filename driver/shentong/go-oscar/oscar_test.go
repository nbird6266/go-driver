package oscar

import (
	"bytes"
	"context"
	"database/sql"
	"database/sql/driver"
	"fmt"
	"testing"
	"time"
)

func openTestDB(t *testing.T) *sql.DB {
	t.Helper()
	dsn := "SYSDBA/szoscar55@127.0.0.1:2003/OSRDB"
	db, err := sql.Open(DriverName, dsn)
	if err != nil {
		t.Fatalf("open failed: %v", err)
	}
	return db
}

func TestSelect2(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	var count int
	if err := db.QueryRow("select 2").Scan(&count); err != nil {
		t.Fatalf("query select 2 failed: %v", err)
	}
	if count != 2 {
		t.Fatalf("expect 2, got %d", count)
	}
	fmt.Printf("select 2 -> %d\n", count)
}

func TestPing(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	if err := db.Ping(); err != nil {
		t.Fatalf("ping failed: %v", err)
	}
}

func TestInsertSelect(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	drop := `drop table go_driver_test`
	_, _ = db.Exec(drop)

	create := `create table go_driver_test(id int, name varchar(50))`
	if _, err := db.Exec(create); err != nil {
		t.Fatalf("create table failed: %v", err)
	}
	defer func() {
		_, _ = db.Exec(drop)
	}()

	insert := `insert into go_driver_test values (1, 'hello')`
	if _, err := db.Exec(insert); err != nil {
		t.Fatalf("insert failed: %v", err)
	}

	var id int
	var name string
	if err := db.QueryRow(`select id, name from go_driver_test`).Scan(&id, &name); err != nil {
		t.Fatalf("select failed: %v", err)
	}
	if id != 1 || name != "hello" {
		t.Fatalf("unexpected row: id=%d name=%q", id, name)
	}
	fmt.Printf("row -> id=%d name=%s\n", id, name)
}

// TestPreparedSelect exercises the parameterized query path: Conn.QueryContext
// with args goes through the ExecutePacket (0x0B first, 0x0D on reuse).
func TestPreparedSelect(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	var n int
	if err := db.QueryRow("select ? + 1", 41).Scan(&n); err != nil {
		t.Fatalf("select with int param failed: %v", err)
	}
	if n != 42 {
		t.Fatalf("expect 42, got %d", n)
	}
	fmt.Printf("select ? + 1 with 41 -> %d\n", n)

	var s string
	if err := db.QueryRow("select ?::varchar(100)", "hello-param").Scan(&s); err != nil {
		t.Fatalf("select with string param failed: %v", err)
	}
	if s != "hello-param" {
		t.Fatalf("expect 'hello-param', got %q", s)
	}
	fmt.Printf("select ?::varchar(100) with 'hello-param' -> %s\n", s)

	var f float64
	if err := db.QueryRow("select ?::float8", 2.5).Scan(&f); err != nil {
		t.Fatalf("select with float param failed: %v", err)
	}
	if f != 2.5 {
		t.Fatalf("expect 2.5, got %v", f)
	}
	fmt.Printf("select ?::float8 with 2.5 -> %v\n", f)
}

// TestPreparedExec covers INSERT with parameters and the explicit
// db.Prepare + stmt path (including statement reuse / DEALLOCATE).
func TestPreparedExec(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	drop := `drop table go_driver_ptest`
	_, _ = db.Exec(drop)
	create := `create table go_driver_ptest(id int, name varchar(50), score float8)`
	if _, err := db.Exec(create); err != nil {
		t.Fatalf("create table failed: %v", err)
	}
	defer func() {
		_, _ = db.Exec(drop)
	}()

	res, err := db.Exec("insert into go_driver_ptest values (?, ?, ?)", 1, "alpha", 2.5)
	if err != nil {
		t.Fatalf("insert with params failed: %v", err)
	}
	affected, err := res.RowsAffected()
	if err != nil {
		t.Fatalf("rows affected: %v", err)
	}
	if affected != 1 {
		t.Fatalf("expect 1 row affected, got %d", affected)
	}

	// second insert (new connection may reuse a prepared stmt name)
	if _, err := db.Exec("insert into go_driver_ptest values (?, ?, ?)", 2, "beta", 3.75); err != nil {
		t.Fatalf("second insert with params failed: %v", err)
	}

	var id int
	var name string
	var score float64
	if err := db.QueryRow("select id, name, score from go_driver_ptest where id = ?", 1).
		Scan(&id, &name, &score); err != nil {
		t.Fatalf("select by id failed: %v", err)
	}
	if id != 1 || name != "alpha" || score != 2.5 {
		t.Fatalf("unexpected row: id=%d name=%q score=%v", id, name, score)
	}
	fmt.Printf("row -> id=%d name=%s score=%v\n", id, name, score)

	// explicit Prepare: first QueryRow prepares (0x0B), second reuses (0x0D)
	stmt, err := db.Prepare("select id from go_driver_ptest where id = ?")
	if err != nil {
		t.Fatalf("prepare failed: %v", err)
	}
	var got int
	if err := stmt.QueryRow(2).Scan(&got); err != nil {
		t.Fatalf("stmt query 1 failed: %v", err)
	}
	if got != 2 {
		t.Fatalf("expect 2, got %d", got)
	}
	if err := stmt.QueryRow(1).Scan(&got); err != nil {
		t.Fatalf("stmt query 2 failed: %v", err)
	}
	if got != 1 {
		t.Fatalf("expect 1, got %d", got)
	}
	if err := stmt.Close(); err != nil {
		t.Fatalf("stmt close failed: %v", err)
	}
	fmt.Printf("prepared stmt reuse ok\n")
}

// TestPreparedTimestamp binds a time.Time parameter (bind type 28) and reads
// it back as time.Time through the binary date/time decoders (P2).
func TestPreparedTimestamp(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	drop := `drop table go_driver_tstest`
	_, _ = db.Exec(drop)
	create := `create table go_driver_tstest(ts timestamp, d date, tm time)`
	if _, err := db.Exec(create); err != nil {
		t.Fatalf("create table failed: %v", err)
	}
	defer func() {
		_, _ = db.Exec(drop)
	}()

	ts := time.Date(2024, 5, 6, 7, 8, 9, 123456000, time.Local)
	if _, err := db.Exec("insert into go_driver_tstest values (?, ?, ?)", ts,
		time.Date(2024, 5, 6, 0, 0, 0, 0, time.Local),
		time.Date(2024, 5, 6, 7, 8, 9, 0, time.Local)); err != nil {
		t.Fatalf("insert timestamp failed: %v", err)
	}

	// timestamp column decodes to time.Time (2024-05-06 07:08:09.123456)
	var gotTS time.Time
	if err := db.QueryRow("select ts from go_driver_tstest").Scan(&gotTS); err != nil {
		t.Fatalf("select ts failed: %v", err)
	}
	if !gotTS.Equal(ts) {
		t.Fatalf("timestamp mismatch: want %v, got %v", ts, gotTS)
	}

	// date column comes back as a 7-byte timestamp form with time fields 0
	var gotD time.Time
	if err := db.QueryRow("select d from go_driver_tstest").Scan(&gotD); err != nil {
		t.Fatalf("select d failed: %v", err)
	}
	wantD := time.Date(2024, 5, 6, 0, 0, 0, 0, time.Local)
	if !gotD.Equal(wantD) {
		t.Fatalf("date mismatch: want %v, got %v", wantD, gotD)
	}

	// time column decodes on the 1970-01-01 base date
	var gotTm time.Time
	if err := db.QueryRow("select tm from go_driver_tstest").Scan(&gotTm); err != nil {
		t.Fatalf("select tm failed: %v", err)
	}
	hh, mm, ss := gotTm.Clock()
	if hh != 7 || mm != 8 || ss != 9 {
		t.Fatalf("time mismatch: want 07:08:09, got %02d:%02d:%02d", hh, mm, ss)
	}
	fmt.Printf("ts -> %v, d -> %v, tm -> %02d:%02d:%02d\n", gotTS, gotD, hh, mm, ss)
}

// TestTimestampDecode round-trips several timestamps with sub-second
// precision (microseconds and nanoseconds) across year boundaries.
func TestTimestampDecode(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	drop := `drop table go_driver_ts2`
	_, _ = db.Exec(drop)
	if _, err := db.Exec("create table go_driver_ts2(id int, ts timestamp, d date)"); err != nil {
		t.Fatalf("create table failed: %v", err)
	}
	defer func() {
		_, _ = db.Exec(drop)
	}()

	cases := []time.Time{
		time.Date(2024, 5, 6, 7, 8, 9, 123456000, time.Local),
		time.Date(2024, 5, 6, 7, 8, 9, 999999000, time.Local),
		time.Date(1999, 12, 31, 23, 59, 59, 999999000, time.Local),
		time.Date(2000, 1, 1, 0, 0, 0, 0, time.Local),
		time.Date(2030, 6, 15, 12, 0, 0, 500000000, time.Local),
	}
	for i, want := range cases {
		if _, err := db.Exec("insert into go_driver_ts2 values (?, ?, ?)", i, want,
			time.Date(want.Year(), want.Month(), want.Day(), 0, 0, 0, 0, time.Local)); err != nil {
			t.Fatalf("insert case %d failed: %v", i, err)
		}
		var got time.Time
		if err := db.QueryRow("select ts from go_driver_ts2 where id = ?", i).Scan(&got); err != nil {
			t.Fatalf("select case %d failed: %v", i, err)
		}
		if !got.Equal(want) {
			t.Fatalf("case %d mismatch: want %v, got %v", i, want, got)
		}
		fmt.Printf("ts[%d] -> %v\n", i, got)
	}
}

// TestLobRead verifies that CLOB/BLOB columns are read back as their actual
// content (fetched through the Fastpath READ function), not the raw locator.
func TestLobRead(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	drop := `drop table go_driver_lobtest`
	_, _ = db.Exec(drop)
	create := `create table go_driver_lobtest(id int, c clob, b blob)`
	if _, err := db.Exec(create); err != nil {
		t.Fatalf("create table failed: %v", err)
	}
	defer func() {
		_, _ = db.Exec(drop)
	}()

	if _, err := db.Exec("insert into go_driver_lobtest values (1, 'clob-data-内容', '686900ff')"); err != nil {
		t.Fatalf("insert lob failed: %v", err)
	}

	var c string
	var b []byte
	if err := db.QueryRow("select c, b from go_driver_lobtest").Scan(&c, &b); err != nil {
		t.Fatalf("select lob failed: %v", err)
	}
	if c != "clob-data-内容" {
		t.Fatalf("clob mismatch: got %q", c)
	}
	if !bytes.Equal(b, []byte{0x68, 0x69, 0x00, 0xff}) {
		t.Fatalf("blob mismatch: got %x", b)
	}
	fmt.Printf("lob -> clob=%q blob=%x\n", c, b)

	// empty lob
	if _, err := db.Exec("insert into go_driver_lobtest values (2, '', '')"); err != nil {
		t.Fatalf("insert empty lob failed: %v", err)
	}
	var ec string
	if err := db.QueryRow("select c from go_driver_lobtest where id = 2").Scan(&ec); err != nil {
		t.Fatalf("select empty clob failed: %v", err)
	}
	if ec != "" {
		t.Fatalf("empty clob mismatch: got %q", ec)
	}
	fmt.Printf("empty lob ok\n")
}

// TestLobLarge verifies multi-chunk LOB reads: a ~1.5MB CLOB/BLOB exceeds the
// server chunk size (600000), so the driver must issue several Fastpath READ
// calls and concatenate the results.
func TestLobLarge(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	drop := `drop table go_driver_lobbig`
	_, _ = db.Exec(drop)
	create := `create table go_driver_lobbig(id int, c clob, b blob)`
	if _, err := db.Exec(create); err != nil {
		t.Fatalf("create table failed: %v", err)
	}
	defer func() {
		_, _ = db.Exec(drop)
	}()

	const size = 1500 * 1024 // 1.5MB, several times the server chunk size
	clobData := make([]byte, size)
	for i := range clobData {
		clobData[i] = 'a' + byte(i%26)
	}
	blobData := make([]byte, size)
	for i := range blobData {
		blobData[i] = byte(i * 31)
	}

	if _, err := db.Exec("insert into go_driver_lobbig values (1, ?, ?)", string(clobData), blobData); err != nil {
		t.Fatalf("insert big lob failed: %v", err)
	}

	var c string
	var b []byte
	if err := db.QueryRow("select c, b from go_driver_lobbig").Scan(&c, &b); err != nil {
		t.Fatalf("select big lob failed: %v", err)
	}
	if len(c) != size {
		t.Fatalf("clob length mismatch: want %d, got %d", size, len(c))
	}
	if len(b) != size {
		t.Fatalf("blob length mismatch: want %d, got %d", size, len(b))
	}
	// spot-check content at several offsets to ensure chunks are contiguous
	for _, i := range []int{0, 1, size / 3, size / 2, size - 2, size - 1} {
		if c[i] != clobData[i] {
			t.Fatalf("clob mismatch at %d: want %q, got %q", i, clobData[i], c[i])
		}
		if b[i] != blobData[i] {
			t.Fatalf("blob mismatch at %d: want 0x%02x, got 0x%02x", i, blobData[i], b[i])
		}
	}
	if !bytes.Equal([]byte(c[:16]), clobData[:16]) || !bytes.Equal(b[:16], blobData[:16]) {
		t.Fatalf("head mismatch")
	}
	fmt.Printf("big lob ok: clob=%d bytes, blob=%d bytes\n", len(c), len(b))
}

// TestPreparedNull binds a nil parameter.
func TestPreparedNull(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	drop := `drop table go_driver_ntest`
	_, _ = db.Exec(drop)
	create := `create table go_driver_ntest(v varchar(20))`
	if _, err := db.Exec(create); err != nil {
		t.Fatalf("create table failed: %v", err)
	}
	defer func() {
		_, _ = db.Exec(drop)
	}()

	if _, err := db.Exec("insert into go_driver_ntest values (?)", nil); err != nil {
		t.Fatalf("insert null failed: %v", err)
	}
	var s sql.NullString
	if err := db.QueryRow("select v from go_driver_ntest").Scan(&s); err != nil {
		t.Fatalf("select null failed: %v", err)
	}
	if s.Valid {
		t.Fatalf("expect null, got %q", s.String)
	}
	fmt.Printf("null ok\n")
}

// TestNamedValue verifies driver.NamedValueChecker: driver.Valuer values such
// as sql.NullString are unwrapped before binding.
func TestNamedValue(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	drop := `drop table go_driver_nvtest`
	_, _ = db.Exec(drop)
	if _, err := db.Exec("create table go_driver_nvtest(id int, v varchar(20))"); err != nil {
		t.Fatalf("create table failed: %v", err)
	}
	defer func() {
		_, _ = db.Exec(drop)
	}()

	if _, err := db.Exec("insert into go_driver_nvtest values (?, ?)", 1,
		sql.NullString{String: "a", Valid: true}); err != nil {
		t.Fatalf("insert valid failed: %v", err)
	}
	if _, err := db.Exec("insert into go_driver_nvtest values (?, ?)", 2,
		sql.NullString{Valid: false}); err != nil {
		t.Fatalf("insert null failed: %v", err)
	}

	var v1 string
	if err := db.QueryRow("select v from go_driver_nvtest where id = 1").Scan(&v1); err != nil {
		t.Fatalf("select 1 failed: %v", err)
	}
	if v1 != "a" {
		t.Fatalf("expect a, got %q", v1)
	}
	var nv sql.NullString
	if err := db.QueryRow("select v from go_driver_nvtest where id = 2").Scan(&nv); err != nil {
		t.Fatalf("select 2 failed: %v", err)
	}
	if nv.Valid {
		t.Fatalf("expect null, got %q", nv.String)
	}
	fmt.Printf("named value null ok\n")
}

// TestColumnTypes verifies the driver.RowsColumnType* metadata interfaces:
// column names, database type names, scan types, length and nullability.
func TestColumnTypes(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	rows, err := db.Query(`select 1::int as "i", 'abc'::varchar(30) as "v", 2.5::float8 as "f", true as "b", cast('2024-01-02 03:04:05' as timestamp) as "ts"`)
	if err != nil {
		t.Fatalf("query failed: %v", err)
	}
	defer rows.Close()

	cols, err := rows.ColumnTypes()
	if err != nil {
		t.Fatalf("column types failed: %v", err)
	}
	if len(cols) != 5 {
		t.Fatalf("expect 5 columns, got %d", len(cols))
	}

	names := []string{"i", "v", "f", "b", "ts"}
	expectType := map[string]string{
		"i":  "int",
		"v":  "varchar",
		"f":  "double precision",
		"b":  "boolean",
		"ts": "timestamp",
	}
	for i, c := range cols {
		if c.Name() != names[i] {
			t.Fatalf("col %d name: want %q, got %q", i, names[i], c.Name())
		}
		want := expectType[names[i]]
		if got := c.DatabaseTypeName(); got != want {
			t.Fatalf("col %q db type: want %q, got %q", names[i], want, got)
		}
		if _, ok := c.Length(); ok {
			t.Fatalf("col %q Length should be unknown", names[i])
		}
		if _, ok := c.Nullable(); ok {
			t.Fatalf("col %q Nullable should be unknown", names[i])
		}
	}
	// scan types: numeric -> int64, bool -> bool, float -> float64, others -> []byte
	if got := cols[0].ScanType(); got.String() != "int64" {
		t.Fatalf("col i scan type: want int64, got %v", got)
	}
	if got := cols[1].ScanType(); got.String() != "[]uint8" {
		t.Fatalf("col v scan type: want []uint8, got %v", got)
	}
	if got := cols[2].ScanType(); got.String() != "float64" {
		t.Fatalf("col f scan type: want float64, got %v", got)
	}
	if got := cols[3].ScanType(); got.String() != "bool" {
		t.Fatalf("col b scan type: want bool, got %v", got)
	}
	if got := cols[4].ScanType(); got.String() != "time.Time" {
		t.Fatalf("col ts scan type: want time.Time, got %v", got)
	}

	var i int
	var v string
	var f float64
	var b bool
	var ts time.Time
	if !rows.Next() {
		t.Fatalf("expected one row")
	}
	if err := rows.Scan(&i, &v, &f, &b, &ts); err != nil {
		t.Fatalf("scan failed: %v", err)
	}
	if i != 1 || v != "abc" || f != 2.5 || !b {
		t.Fatalf("unexpected values: i=%d v=%q f=%v b=%v", i, v, f, b)
	}
	// timestamp decodes to time.Time (2024-01-02 03:04:05)
	wantTS := time.Date(2024, 1, 2, 3, 4, 5, 0, time.Local)
	if !ts.Equal(wantTS) {
		t.Fatalf("unexpected ts: want %v, got %v", wantTS, ts)
	}
	fmt.Printf("column types ok: %v\n", names)
}

// TestTxIsolation verifies BeginTx with a non-default isolation level and the
// read-only option: the transaction starts, DML works, and commit persists.
func TestTxIsolation(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	_, _ = db.Exec(`drop table go_driver_txiso`)
	if _, err := db.Exec(`create table go_driver_txiso(id int)`); err != nil {
		t.Fatalf("create failed: %v", err)
	}
	defer func() { _, _ = db.Exec(`drop table go_driver_txiso`) }()

	tx, err := db.BeginTx(context.Background(), &sql.TxOptions{
		Isolation: sql.LevelSerializable,
	})
	if err != nil {
		t.Fatalf("begin serializable failed: %v", err)
	}
	if _, err := tx.Exec(`insert into go_driver_txiso values (1)`); err != nil {
		_ = tx.Rollback()
		t.Fatalf("insert in tx failed: %v", err)
	}
	if err := tx.Commit(); err != nil {
		t.Fatalf("commit failed: %v", err)
	}
	var cnt int
	if err := db.QueryRow(`select count(*) from go_driver_txiso`).Scan(&cnt); err != nil {
		t.Fatalf("count failed: %v", err)
	}
	if cnt != 1 {
		t.Fatalf("expect 1 row after commit, got %d", cnt)
	}

	// read-only transaction rejects writes.
	tx2, err := db.BeginTx(context.Background(), &sql.TxOptions{ReadOnly: true})
	if err != nil {
		t.Fatalf("begin read-only failed: %v", err)
	}
	if _, err := tx2.Exec(`insert into go_driver_txiso values (2)`); err == nil {
		_ = tx2.Rollback()
		t.Fatalf("insert into read-only tx should fail")
	} else {
		fmt.Printf("read-only tx rejects write as expected: %v\n", err)
	}
	if err := tx2.Rollback(); err != nil {
		t.Fatalf("rollback failed: %v", err)
	}
	fmt.Printf("tx isolation ok\n")
}

// TestPoolReuseAfterRollback verifies that implementing SessionResetter and
// Validator lets database/sql keep the connection in the pool after a
// transaction rollback instead of closing it (keepConnOnRollback).
func TestPoolReuseAfterRollback(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	db.SetMaxOpenConns(1)
	db.SetMaxIdleConns(1)
	db.SetConnMaxLifetime(0)

	if err := db.Ping(); err != nil {
		t.Fatalf("ping failed: %v", err)
	}
	if got := db.Stats().OpenConnections; got != 1 {
		t.Fatalf("expect 1 open conn after ping, got %d", got)
	}

	tx, err := db.Begin()
	if err != nil {
		t.Fatalf("begin failed: %v", err)
	}
	if _, err := tx.Exec(`select 1`); err != nil {
		t.Fatalf("exec in tx failed: %v", err)
	}
	if err := tx.Rollback(); err != nil {
		t.Fatalf("rollback failed: %v", err)
	}

	// With SessionResetter+Validator the same connection is returned to the
	// pool; without them it would be closed (OpenConnections drops to 0).
	if got := db.Stats().OpenConnections; got != 1 {
		t.Fatalf("conn was not reused after rollback: open=%d (want 1)", got)
	}
	if err := db.Ping(); err != nil {
		t.Fatalf("ping after rollback failed: %v", err)
	}
	fmt.Printf("pool reuse after rollback ok\n")
}

// TestResetSession exercises the driver-level ResetSession: an unfinished
// transaction is rolled back when the connection is reset for reuse.
func TestResetSession(t *testing.T) {
	cfg, err := parseDSN("SYSDBA/szoscar55@127.0.0.1:2003/OSRDB")
	if err != nil {
		t.Fatalf("parse dsn: %v", err)
	}
	c, err := openConn(cfg)
	if err != nil {
		t.Fatalf("open conn: %v", err)
	}
	defer c.Close()

	if _, err := c.ExecContext(context.Background(), `create table go_driver_txreset(id int)`, nil); err != nil {
		t.Fatalf("create: %v", err)
	}
	defer func() {
		_, _ = c.ExecContext(context.Background(), `drop table go_driver_txreset`, nil)
	}()

	trx, err := c.BeginTx(context.Background(), driver.TxOptions{})
	if err != nil {
		t.Fatalf("begin: %v", err)
	}
	if _, err := trx.(*tx).conn.ExecContext(context.Background(), `insert into go_driver_txreset values (1)`, nil); err != nil {
		t.Fatalf("insert in tx: %v", err)
	}
	// Leave the transaction open, then reset the session: the insert must be
	// rolled back so the table is empty afterwards.
	if err := c.ResetSession(context.Background()); err != nil {
		t.Fatalf("reset session: %v", err)
	}
	res, err := c.query(context.Background(), `select count(*) from go_driver_txreset`)
	if err != nil {
		t.Fatalf("count after reset: %v", err)
	}
	cnt, _ := res.rows[0][0].(int64)
	if cnt != 0 {
		t.Fatalf("open tx was not rolled back by ResetSession: count=%d", cnt)
	}
	fmt.Printf("reset session rolls back open tx ok\n")
}
