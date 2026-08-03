package oscar

import (
	"errors"
	"testing"
)

// TestErrorClassification drives server-side failures through the real
// protocol and checks that the driver exposes them as *OscarError with the
// correct SQLSTATE, and that the Is* classification helpers behave.
func TestErrorClassification(t *testing.T) {
	db := openTestDB(t)
	defer db.Close()

	_, _ = db.Exec(`drop table go_driver_errtest`)
	if _, err := db.Exec(`create table go_driver_errtest(
		id int not null,
		code varchar(20) unique)`); err != nil {
		t.Fatalf("create failed: %v", err)
	}
	defer func() { _, _ = db.Exec(`drop table go_driver_errtest`) }()

	// seed one row for the unique-violation case
	if _, err := db.Exec(`insert into go_driver_errtest values (1, 'a')`); err != nil {
		t.Fatalf("seed failed: %v", err)
	}

	t.Run("not_null", func(t *testing.T) {
		_, err := db.Exec(`insert into go_driver_errtest (code) values ('b')`)
		if err == nil {
			t.Fatal("expected NOT NULL violation")
		}
		// OSCAR reports NOT NULL as the generic class-23 SQLSTATE 23000;
		// classification is message-based.
		assertOscarError(t, err, "23000")
		if !IsIntegrityViolation(err) {
			t.Errorf("IsIntegrityViolation: want true")
		}
		if !IsNotNullViolation(err) {
			t.Errorf("IsNotNullViolation: want true")
		}
		if IsUniqueViolation(err) || IsForeignKeyViolation(err) {
			t.Errorf("unexpected violation classification: %v", err)
		}
		t.Logf("not-null error: %v", err)
	})

	t.Run("unique", func(t *testing.T) {
		_, err := db.Exec(`insert into go_driver_errtest values (2, 'a')`)
		if err == nil {
			t.Fatal("expected unique violation")
		}
		assertOscarError(t, err, "23000")
		if !IsUniqueViolation(err) {
			t.Errorf("IsUniqueViolation: want true")
		}
		if IsNotNullViolation(err) {
			t.Errorf("IsNotNullViolation: want false for unique error")
		}
		t.Logf("unique error: %v", err)
	})

	t.Run("undefined_table", func(t *testing.T) {
		_, err := db.Exec(`select * from go_driver_no_such_table`)
		if err == nil {
			t.Fatal("expected undefined table error")
		}
		assertOscarError(t, err, "42S02")
		if !IsUndefinedTable(err) {
			t.Errorf("IsUndefinedTable: want true")
		}
		t.Logf("undefined table error: %v", err)
	})

	t.Run("syntax", func(t *testing.T) {
		_, err := db.Exec(`selct 1`) // intentional typo
		if err == nil {
			t.Fatal("expected syntax error")
		}
		if !IsSyntaxError(err) {
			t.Errorf("IsSyntaxError: want true for %v", err)
		}
		t.Logf("syntax error: %v", err)
	})

	t.Run("errors_as", func(t *testing.T) {
		// *OscarError must be reachable through errors.As even when wrapped.
		_, err := db.Exec(`select * from go_driver_no_such_table`)
		if err == nil {
			t.Fatal("expected error")
		}
		oe, ok := AsError(err)
		if !ok {
			t.Fatalf("AsError: want ok=true, err=%v", err)
		}
		if oe.Code == 0 || oe.SQLState == "" || oe.Message == "" {
			t.Fatalf("OscarError fields empty: %+v", oe)
		}
		var viaAs *OscarError
		if !errors.As(err, &viaAs) {
			t.Fatalf("errors.As: want ok=true")
		}
		if viaAs != oe {
			t.Fatalf("errors.As returned different instance")
		}
		t.Logf("OscarError: %+v", oe)
	})
}

func assertOscarError(t *testing.T, err error, wantState string) {
	t.Helper()
	oe, ok := AsError(err)
	if !ok {
		t.Fatalf("expected *OscarError, got %T: %v", err, err)
	}
	if oe.SQLState != wantState {
		t.Fatalf("SQLState: want %s, got %s (err=%v)", wantState, oe.SQLState, err)
	}
	if oe.Message == "" {
		t.Fatalf("message empty for %v", err)
	}
}
