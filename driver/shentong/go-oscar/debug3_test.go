package oscar

import (
	"context"
	"database/sql"
	"fmt"
	"testing"
)

func TestDebugSingleConn(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	conn, err := db.Conn(context.Background())
	if err != nil {
		t.Fatalf("conn failed: %v", err)
	}
	defer conn.Close()

	_, _ = conn.ExecContext(context.Background(), "drop table go_driver_test")

	if _, err := conn.ExecContext(context.Background(), "create table go_driver_test(id int, name varchar(50))"); err != nil {
		t.Fatalf("create failed: %v", err)
	}
	fmt.Println("create ok")

	res, err := conn.ExecContext(context.Background(), "insert into go_driver_test values (1, 'hello')")
	if err != nil {
		t.Fatalf("insert failed: %v", err)
	}
	ra, _ := res.RowsAffected()
	fmt.Printf("insert ok, rowsAffected=%d\n", ra)

	rows, err := conn.QueryContext(context.Background(), "select id, name from go_driver_test")
	if err != nil {
		t.Fatalf("select failed: %v", err)
	}
	defer rows.Close()
	cols, _ := rows.Columns()
	fmt.Printf("columns: %v\n", cols)
	for rows.Next() {
		var id int
		var name string
		if err := rows.Scan(&id, &name); err != nil {
			t.Fatalf("scan failed: %v", err)
		}
		fmt.Printf("row: id=%d name=%q\n", id, name)
	}
	if err := rows.Err(); err != nil {
		t.Fatalf("rows err: %v", err)
	}

	var cnt int
	if err := conn.QueryRowContext(context.Background(), "select count(*) from go_driver_test").Scan(&cnt); err != nil {
		t.Fatalf("count failed: %v", err)
	}
	fmt.Printf("count=%d\n", cnt)
}

func TestRawSelect(t *testing.T) {
	dsn := "SYSDBA/szoscar55@127.0.0.1:2003/OSRDB"
	db, err := sql.Open(DriverName, dsn)
	if err != nil {
		t.Fatal(err)
	}
	defer db.Close()
	rows, err := db.Query("select id, name from go_driver_test")
	if err != nil {
		t.Fatalf("query err: %v", err)
	}
	cols, _ := rows.Columns()
	fmt.Printf("columns: %v\n", cols)
	for rows.Next() {
		vals := make([]sql.RawBytes, len(cols))
		dest := make([]any, len(cols))
		for i := range vals {
			dest[i] = &vals[i]
		}
		if err := rows.Scan(dest...); err != nil {
			t.Fatalf("scan err: %v", err)
		}
		for i, v := range vals {
			fmt.Printf("col %d: raw=%q\n", i, string(v))
		}
	}
	if err := rows.Err(); err != nil {
		t.Fatalf("rows err: %v", err)
	}
}
