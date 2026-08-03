package oscar

import (
	"database/sql/driver"
	"time"
)

// Internal Oscar types for the date/time family, as returned by
// oscarTypeForOID. bindTypeDate/Time/Timestamp are defined in bind.go;
// Timetz and Timestamptz are defined here.
const (
	oscarTypeTimetz     = 27
	oscarTypeTimestamptz = 29
)

// isDateTimeType reports whether the server typeOID belongs to the date/time
// family, whose row values arrive in a fixed binary layout (see
// TimestampConverter / DateConverter / TimeConverter / TimetzConverter).
func isDateTimeType(typeOID int) bool {
	switch oscarTypeForOID(typeOID) {
	case bindTypeDate, bindTypeTime, oscarTypeTimetz, bindTypeTimestamp, oscarTypeTimestamptz:
		return true
	}
	return false
}

// isInfinitySentinel reports whether val is the 2-byte infinity encoding
// (TimestampConverter.POSITIVE_INFINITY = {-3, 2}, NEGATIVE_INFINITY = {-3, 3}).
func isInfinitySentinel(val []byte) bool {
	return len(val) == 2 && val[0] == 0xFD && (val[1] == 0x02 || val[1] == 0x03)
}

// decodeOscarDateTime decodes a binary date/time value (internal Oscar types
// 25..29) into a time.Time driver value. Values that cannot be represented
// (infinity sentinels, BC years, unexpected lengths) are returned as the raw
// bytes, preserving the previous behavior for those edge cases.
func decodeOscarDateTime(val []byte, oscarType int) (driver.Value, error) {
	switch oscarType {
	case bindTypeDate:
		return decodeOscarDate(val)
	case bindTypeTime:
		return decodeOscarTime(val)
	case bindTypeTimestamp, oscarTypeTimestamptz:
		return decodeOscarTimestamp(val, oscarType == oscarTypeTimestamptz)
	case oscarTypeTimetz:
		// The timetz wire layout is not yet confirmed against the server
		// (the JDBC writer emits 5 bytes while the reader expects 9 bytes).
		// Return the raw value for now.
		return cloneBytes(val), nil
	default:
		return cloneBytes(val), nil
	}
}

// oscarTimestampYear decodes the 2-byte year field: ((b0&0xFF)-100)*100 +
// (b1&0xFF)-100 (TimestampConverter.convertBytesToTimeStamp).
func oscarTimestampYear(val []byte) int {
	return (int(val[0]&0xFF)-100)*100 + int(val[1]&0xFF) - 100
}

// oscarNanos decodes the 4-byte big-endian fractional field at off, applying
// the JDBC getNanos heuristic: a value below one second was stored in
// microseconds and must be scaled up to nanoseconds.
func oscarNanos(val []byte, off int) int {
	i := int32(val[off])<<24 | int32(val[off+1])<<16 | int32(val[off+2])<<8 | int32(val[off+3])
	if i/100000000 <= 0 {
		i *= 1000
	}
	return int(i)
}

// oscarZoneSeconds decodes the 2-byte zone field: hour = b[off]-20,
// min = b[off+1]-60, result in seconds (TypeConverter.getZone).
func oscarZoneSeconds(val []byte, off int) int {
	hour := int(val[off]) - 20
	min := int(val[off+1]) - 60
	return hour*3600 + min*60
}

// decodeOscarDate decodes a date value: the full 7-byte timestamp form (time
// fields encoded as 0 -> 1) or the compact 4-byte form [y/100+100, y%100+100,
// mon, day] (DateConverter.convertBytesToDate).
func decodeOscarDate(val []byte) (driver.Value, error) {
	if isInfinitySentinel(val) {
		return cloneBytes(val), nil
	}
	var year, month, day int
	switch len(val) {
	case 7, 4:
		year = oscarTimestampYear(val)
		// val[2] is a 1-based month (encoder writes Calendar.get(MONTH)+1).
		month = int(val[2])
		day = int(val[3])
	default:
		return cloneBytes(val), nil
	}
	if year < 0 {
		// BC years are not representable by time.Time.
		return cloneBytes(val), nil
	}
	return time.Date(year, time.Month(month), day, 0, 0, 0, 0, time.Local), nil
}

// decodeOscarTime decodes a time value: [h+1, mi+1, s+1], with an optional
// 7-byte variant carrying nanoseconds at [3..6] (TimeConverter).
func decodeOscarTime(val []byte) (driver.Value, error) {
	if len(val) < 3 {
		return cloneBytes(val), nil
	}
	hour := int(val[0]) - 1
	min := int(val[1]) - 1
	sec := int(val[2]) - 1
	var nano int
	if len(val) >= 7 {
		nano = oscarNanos(val, 3)
	}
	return time.Date(1970, 1, 1, hour, min, sec, nano, time.Local), nil
}

// decodeOscarTimestamp decodes a timestamp (11 bytes) or timestamptz
// (13 bytes, zone at [11..12]) value:
//
//	[y/100+100, y%100+100, mon, day, h+1, mi+1, s+1, micros(4 big-endian), zone?]
//
// Mirrors TimestampConverter.convertBytesToTimeStamp.
func decodeOscarTimestamp(val []byte, withZone bool) (driver.Value, error) {
	if isInfinitySentinel(val) {
		return cloneBytes(val), nil
	}
	if len(val) < 7 {
		return cloneBytes(val), nil
	}
	year := oscarTimestampYear(val)
	if year < 0 {
		// BC years are not representable by time.Time.
		return cloneBytes(val), nil
	}
	// val[2] is a 1-based month (encoder writes Calendar.get(MONTH)+1).
	month := int(val[2])
	day := int(val[3])
	hour := int(val[4]) - 1
	min := int(val[5]) - 1
	sec := int(val[6]) - 1
	var nano int
	if len(val) >= 11 {
		nano = oscarNanos(val, 7)
	}
	loc := time.Local
	if withZone && len(val) >= 13 {
		loc = time.FixedZone("", oscarZoneSeconds(val, 11))
	}
	return time.Date(year, time.Month(month), day, hour, min, sec, nano, loc), nil
}
