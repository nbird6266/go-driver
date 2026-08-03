package main

import (
	"fmt"
	"github.com/nbird6266/go-driver/db_shentong"
	"testing"
)

func TestShenTong(t *testing.T) {

	dsn := db_shentong.GetDSN("SYSDBA", "szoscar55", "127.0.0.1", 2003, "OSRDB")
	db, err := db_shentong.Open(dsn)
	if err != nil {
		panic(err)
	}
	sql := `select 2`
	var count int
	rows, err := db.Query(sql)
	if err != nil {
		panic(err)
	}
	rows.Next()
	err = rows.Scan(&count)
	if err != nil {
		panic(err)
	}
	fmt.Printf("result:%d\n", count)
	if count == 2 {
		fmt.Println("test success")
	} else {
		panic("test fail")
	}
}
