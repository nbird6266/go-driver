package oscar

import (
	"fmt"
	"testing"
)

func TestDebugInsert(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	_, _ = db.Exec("drop table go_driver_test")

	_, err := db.Exec("create table go_driver_test(id int, name varchar(50))")
	fmt.Printf("create err: %v\n", err)

	res, err := db.Exec("insert into go_driver_test values (1, 'hello')")
	ra, _ := res.RowsAffected()
	fmt.Printf("insert err: %v, rowsAffected: %d\n", err, ra)

	var cnt int
	err = db.QueryRow("select count(*) from go_driver_test").Scan(&cnt)
	fmt.Printf("count err: %v, cnt=%d\n", err, cnt)

	rows, err := db.Query("select id, name from go_driver_test")
	fmt.Printf("query err: %v\n", err)
	if err == nil {
		for rows.Next() {
			var id int
			var name string
			scanErr := rows.Scan(&id, &name)
			fmt.Printf("row: id=%d name=%s scanErr=%v\n", id, name, scanErr)
		}
		fmt.Printf("rows.Err: %v\n", rows.Err())
	}
}

func TestTxManual(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	_, _ = db.Exec("drop table go_driver_test")
	if _, err := db.Exec("create table go_driver_test(id int, name varchar(50))"); err != nil {
		t.Fatalf("create failed: %v", err)
	}

	tx, err := db.Begin()
	if err != nil {
		t.Fatalf("begin failed: %v", err)
	}
	if _, err := tx.Exec("insert into go_driver_test values (1, 'hello')"); err != nil {
		t.Fatalf("tx insert failed: %v", err)
	}
	if err := tx.Commit(); err != nil {
		t.Fatalf("commit failed: %v", err)
	}

	var id int
	var name string
	if err := db.QueryRow("select id, name from go_driver_test").Scan(&id, &name); err != nil {
		t.Fatalf("select failed: %v", err)
	}
	if id != 1 || name != "hello" {
		t.Fatalf("unexpected row: id=%d name=%q", id, name)
	}
	fmt.Printf("tx row -> id=%d name=%s\n", id, name)
}
