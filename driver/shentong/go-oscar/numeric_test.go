package oscar

import (
	"math"
	"testing"
)

// TestDecodeOscarNumberRoundTrip encodes int64 values with the encoder and
// verifies decodeOscarNumber restores them exactly, including the int64
// boundaries (no overflow).
func TestDecodeOscarNumberRoundTrip(t *testing.T) {
	cases := []int64{0, 1, -1, 100, 12345, -987654321, 2, math.MaxInt64, math.MinInt64}
	for _, v := range cases {
		got := decodeOscarNumber(convertLongToBytes(v))
		if got != v {
			t.Fatalf("round-trip %d: want %d, got %#v", v, v, got)
		}
	}
}

// TestDecodeOscarNumberBigInt verifies values beyond int64 come back as exact
// decimal strings instead of overflowing.
func TestDecodeOscarNumberBigInt(t *testing.T) {
	// 99999999999999999999 (20 digits, > int64 max):
	// base-100 = 10 groups of 99 -> marker 0xC0+10, bytes 99+1.
	pos := []byte{0xCA}
	for i := 0; i < 10; i++ {
		pos = append(pos, 0x64)
	}
	if got := decodeOscarNumber(pos); got != "99999999999999999999" {
		t.Fatalf("positive bigint: want %q, got %#v", "99999999999999999999", got)
	}

	// -99999999999999999999: marker 63-10, 10 groups of 101-99, terminator.
	neg := []byte{0x35}
	for i := 0; i < 10; i++ {
		neg = append(neg, 0x02)
	}
	neg = append(neg, 0x66)
	if got := decodeOscarNumber(neg); got != "-99999999999999999999" {
		t.Fatalf("negative bigint: want %q, got %#v", "-99999999999999999999", got)
	}
}

// TestDecodeOscarNumberFraction verifies the fractional path: 2.5 encodes as
// integer group 2 plus fractional group 50 (0x33 = 50+1).
func TestDecodeOscarNumberFraction(t *testing.T) {
	if got := decodeOscarNumber([]byte{0xC1, 0x03, 0x33}); got != 2.5 {
		t.Fatalf("2.5: want 2.5, got %#v", got)
	}
}
