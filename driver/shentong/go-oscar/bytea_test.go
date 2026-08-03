package oscar

import (
	"bytes"
	"database/sql"
	"fmt"
	"testing"
)

// TestByteaParam verifies that []byte parameters bound to a bytea column are
// stored as binary data and round-trip byte-for-byte. NUL and backslash bytes
// are escaped as '\ooo' before binding (Oscar stores bytea as text), and the
// driver decodes the escape sequences on read.
func TestByteaParam(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	_, _ = db.Exec(`drop table go_driver_bytea`)
	if _, err := db.Exec(`create table go_driver_bytea(id int, b bytea)`); err != nil {
		t.Fatalf("create failed: %v", err)
	}
	defer func() { _, _ = db.Exec(`drop table go_driver_bytea`) }()

	cases := [][]byte{
		{0x68, 0x69, 0xff, 0x61},                   // "hi\xffa"
		{0x00, 0x01, 0x02, 0x7f, 0x80, 0xfe, 0xff}, // NUL + control + non-ASCII
		{0x5c, 0x00, 0x5c, 0x33, 0x37, 0x37},       // backslash, NUL, literal `\377`
		[]byte("plain-text-value"),
		make([]byte, 500), // > 240 bytes: exercises 0xFE chunked encoding
	}
	// make the big case non-zero so a length mismatch is visible
	for i := range cases[4] {
		cases[4][i] = byte(i*7 + 1)
	}

	for i, want := range cases {
		if _, err := db.Exec(`insert into go_driver_bytea values (?, ?)`, i, want); err != nil {
			t.Fatalf("insert case %d failed: %v", i, err)
		}
		var got []byte
		if err := db.QueryRow(`select b from go_driver_bytea where id = ?`, i).Scan(&got); err != nil {
			t.Fatalf("select case %d failed: %v", i, err)
		}
		if !bytes.Equal(got, want) {
			t.Fatalf("case %d mismatch: want %x, got %x", i, want, got)
		}
		fmt.Printf("bytea[%d] -> %x ok\n", i, got)
	}
}

// TestByteaPreparedReuse verifies the parameter type cache on a prepared
// statement: after the first execution the server-inferred types are reused
// for subsequent executions with different []byte values.
func TestByteaPreparedReuse(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	_, _ = db.Exec(`drop table go_driver_bytea2`)
	if _, err := db.Exec(`create table go_driver_bytea2(id int, b bytea)`); err != nil {
		t.Fatalf("create failed: %v", err)
	}
	defer func() { _, _ = db.Exec(`drop table go_driver_bytea2`) }()

	stmt, err := db.Prepare(`insert into go_driver_bytea2 values (?, ?)`)
	if err != nil {
		t.Fatalf("prepare failed: %v", err)
	}
	defer stmt.Close()

	vals := [][]byte{
		{0xde, 0xad, 0xbe, 0xef},
		{0x01, 0x02, 0x03},
		[]byte("reuse"),
	}
	for i, v := range vals {
		if _, err := stmt.Exec(i, v); err != nil {
			t.Fatalf("exec case %d failed: %v", i, err)
		}
		var got []byte
		if err := db.QueryRow(`select b from go_driver_bytea2 where id = ?`, i).Scan(&got); err != nil {
			t.Fatalf("select case %d failed: %v", i, err)
		}
		if !bytes.Equal(got, v) {
			t.Fatalf("case %d mismatch: want %x, got %x", i, v, got)
		}
	}
	fmt.Printf("bytea prepared reuse ok\n")
}

// TestBlobParamStillHex verifies the blob path still binds []byte as hex text
// and round-trips (the hex binding is correct for blob columns).
func TestBlobParamStillHex(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	_, _ = db.Exec(`drop table go_driver_blobhex`)
	if _, err := db.Exec(`create table go_driver_blobhex(id int, bl blob)`); err != nil {
		t.Fatalf("create failed: %v", err)
	}
	defer func() { _, _ = db.Exec(`drop table go_driver_blobhex`) }()

	want := []byte{0x00, 0x68, 0x69, 0xff, 0x10} // includes NUL: blob is a true LOB
	if _, err := db.Exec(`insert into go_driver_blobhex values (?, ?)`, 1, want); err != nil {
		t.Fatalf("insert failed: %v", err)
	}
	var got []byte
	if err := db.QueryRow(`select bl from go_driver_blobhex where id = 1`).Scan(&got); err != nil {
		t.Fatalf("select failed: %v", err)
	}
	if !bytes.Equal(got, want) {
		t.Fatalf("blob mismatch: want %x, got %x", want, got)
	}
	fmt.Printf("blob hex binding still ok\n")
}

// TestByteaNull verifies a nil []byte still binds as NULL.
func TestByteaNull(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	_, _ = db.Exec(`drop table go_driver_bytean`)
	if _, err := db.Exec(`create table go_driver_bytean(id int, b bytea)`); err != nil {
		t.Fatalf("create failed: %v", err)
	}
	defer func() { _, _ = db.Exec(`drop table go_driver_bytean`) }()

	if _, err := db.Exec(`insert into go_driver_bytean values (?, ?)`, 1, nil); err != nil {
		t.Fatalf("insert nil failed: %v", err)
	}
	var b sql.NullByte // placeholder; scan into []byte pointer instead
	_ = b
	var got []byte
	if err := db.QueryRow(`select b from go_driver_bytean where id = 1`).Scan(&got); err != nil {
		t.Fatalf("select failed: %v", err)
	}
	if got != nil {
		t.Fatalf("expected NULL, got %x", got)
	}
	fmt.Printf("bytea null ok\n")
}
