package oscar

import (
	"database/sql"
	"fmt"
	"testing"
)

func TestDebugColumns(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	cases := []string{
		"select 1 as a, 2 as b",
		"select 'x' as a, 'y' as b",
		"select 1 as a, 'y' as b",
		"select count(*) as c from go_driver_test",
	}
	for _, q := range cases {
		fmt.Printf("=== %s\n", q)
		rows, err := db.Query(q)
		fmt.Printf("  query err: %v\n", err)
		if err != nil {
			continue
		}
		cols, _ := rows.Columns()
		fmt.Printf("  columns: %v (%d)\n", cols, len(cols))
		for rows.Next() {
			vals := make([]sql.RawBytes, len(cols))
			dest := make([]any, len(cols))
			for i := range vals {
				dest[i] = &vals[i]
			}
			if err := rows.Scan(dest...); err != nil {
				fmt.Printf("  scan err: %v\n", err)
				break
			}
			out := make([]string, len(cols))
			for i, v := range vals {
				out[i] = string(v)
			}
			fmt.Printf("  row: %v\n", out)
		}
		fmt.Printf("  rows.Err: %v\n", rows.Err())
	}
}
