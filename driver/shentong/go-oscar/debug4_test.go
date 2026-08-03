package oscar

import (
	"fmt"
	"testing"
)

func TestDebugPool(t *testing.T) {
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

	var id int
	var name string
	err = db.QueryRow("select id, name from go_driver_test").Scan(&id, &name)
	fmt.Printf("select err: %v, id=%d name=%q\n", err, id, name)
}

func TestInsertRowsAffected(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	_, _ = db.Exec("drop table go_driver_test")
	if _, err := db.Exec("create table go_driver_test(id int, name varchar(50))"); err != nil {
		t.Fatalf("create failed: %v", err)
	}
	res, err := db.Exec("insert into go_driver_test values (1, 'hello'), (2, 'world')")
	if err != nil {
		t.Fatalf("insert failed: %v", err)
	}
	ra, err := res.RowsAffected()
	if err != nil {
		t.Fatalf("rowsAffected err: %v", err)
	}
	if ra != 2 {
		t.Fatalf("expect rowsAffected 2, got %d", ra)
	}
	fmt.Printf("rowsAffected=%d\n", ra)
}
