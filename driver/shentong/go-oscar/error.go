package oscar

import (
	"errors"
	"fmt"
	"strings"
)

// OscarError is the driver error type for server-reported failures. It
// carries the server error code, the SQLSTATE string and the message, and is
// returned from every query/exec path. Inspect it with errors.As:
//
//	var oe *oscar.OscarError
//	if errors.As(err, &oe) { ... oe.SQLState ... }
type OscarError struct {
	Code     int
	SQLState string
	Message  string
}

func (e *OscarError) Error() string {
	if e == nil {
		return ""
	}
	if e.Code != 0 {
		return fmt.Sprintf("OSCAR-%05d [%s] %s", e.Code, e.SQLState, e.Message)
	}
	return fmt.Sprintf("[%s] %s", e.SQLState, e.Message)
}

// AsError extracts the underlying *OscarError from err, if any.
func AsError(err error) (*OscarError, bool) {
	var oe *OscarError
	if errors.As(err, &oe) {
		return oe, true
	}
	return nil, false
}

func sqlStateClass(state string) string {
	if len(state) >= 2 {
		return state[:2]
	}
	return state
}

// IsIntegrityViolation reports whether err is a constraint violation
// (SQLSTATE class 23): NOT NULL, unique, foreign key or check.
func IsIntegrityViolation(err error) bool {
	oe, ok := AsError(err)
	return ok && sqlStateClass(oe.SQLState) == "23"
}

// isIntegrityMessage matches the server message text for the two constraint
// violations that OSCAR reports with the generic SQLSTATE 23000. The message
// is server-side Chinese text; the checks are deliberately loose.
func isIntegrityMessage(msg, keyword string) bool {
	return strings.Contains(msg, keyword)
}

// IsNotNullViolation reports whether err is a NOT NULL constraint violation.
// OSCAR reports NOT NULL violations as SQLSTATE 23000 (class 23) with a
// message like "属性ID不能为空"; the standard code 23502 is also accepted.
func IsNotNullViolation(err error) bool {
	oe, ok := AsError(err)
	if !ok || sqlStateClass(oe.SQLState) != "23" {
		return false
	}
	return oe.SQLState == "23502" || isIntegrityMessage(oe.Message, "不能为空")
}

// IsUniqueViolation reports whether err is a unique/primary-key violation.
// OSCAR reports these as SQLSTATE 23000 with a message like
// "不能向索引...中插入重复键值"; the standard code 23505 is also accepted.
func IsUniqueViolation(err error) bool {
	oe, ok := AsError(err)
	if !ok || sqlStateClass(oe.SQLState) != "23" {
		return false
	}
	return oe.SQLState == "23505" || isIntegrityMessage(oe.Message, "重复键值")
}

// IsForeignKeyViolation reports whether err is a foreign-key violation.
// OSCAR may report these as the generic 23000; the standard 23503 code is
// also accepted.
func IsForeignKeyViolation(err error) bool {
	oe, ok := AsError(err)
	if !ok || sqlStateClass(oe.SQLState) != "23" {
		return false
	}
	return oe.SQLState == "23503" || isIntegrityMessage(oe.Message, "外键") ||
		isIntegrityMessage(oe.Message, "引用完整性")
}

// IsUndefinedTable reports whether err is an undefined table error.
// OSCAR sends 42S02 for a missing relation; the standard PostgreSQL
// code 42P01 is also accepted.
func IsUndefinedTable(err error) bool {
	oe, ok := AsError(err)
	return ok && (oe.SQLState == "42P01" || oe.SQLState == "42S02")
}

// IsSyntaxError reports whether err is a syntax or access rule violation
// (SQLSTATE class 42).
func IsSyntaxError(err error) bool {
	oe, ok := AsError(err)
	return ok && sqlStateClass(oe.SQLState) == "42"
}
