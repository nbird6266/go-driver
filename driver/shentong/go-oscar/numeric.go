package oscar

import (
	"database/sql/driver"
	"math/big"
)

var bigHundred = big.NewInt(100)

// isNumericType reports whether the server typeOID belongs to the int family,
// which is encoded on the wire with the internal base-100 number format
// (mirrors JDBC: oscarType 23 -> NumberConverter.convertBytesToLong). Numeric
// and float types (oscarType 34) are sent as ASCII strings instead.
func isNumericType(typeOID int) bool {
	return oscarTypeForOID(typeOID) == 23
}

// isBoolType reports whether the server typeOID is the boolean type, which is
// encoded on the wire as a single byte (0/1).
func isBoolType(typeOID int) bool {
	return oscarTypeForOID(typeOID) == 33
}

// numberValue returns an int64 when n fits, otherwise an exact decimal string
// so that values beyond int64 range never lose precision (the driver.Value
// set has no arbitrary-precision integer type).
func numberValue(n *big.Int) driver.Value {
	if n.IsInt64() {
		return n.Int64()
	}
	return n.String()
}

// decodeOscarNumber converts Oscar's internal numeric byte encoding (see
// NumberConverter.convertBytesToInt / convertBytesToDouble) into an int64
// when the value is integral and fits, an exact decimal string when it
// exceeds int64, or a float64 when it carries a fraction. Integer decoding
// uses math/big so large values are never truncated by int64 overflow.
func decodeOscarNumber(val []byte) driver.Value {
	if len(val) == 0 {
		return int64(0)
	}
	// 0x80 is the single-byte encoding of zero.
	if len(val) == 1 && val[0] == 0x80 {
		return int64(0)
	}

	marker := val[0]
	if marker&0xC0 == 0xC0 {
		// Positive value: realLen = int8(marker) + 65.
		realLen := int(int8(marker)) + 65
		n := new(big.Int)
		i := 1
		if realLen >= len(val) {
			for ; i < len(val); i++ {
				n.Mul(n, bigHundred)
				n.Add(n, big.NewInt(int64(val[i]-1)))
			}
			for ; i < realLen; i++ {
				n.Mul(n, bigHundred)
			}
		} else {
			for ; i < realLen; i++ {
				n.Mul(n, bigHundred)
				n.Add(n, big.NewInt(int64(val[i]-1)))
			}
		}
		if i < len(val) {
			// Fractional part: 2 decimal digits per trailing byte.
			var f float64
			for j := len(val) - 1; j >= i; j-- {
				f += float64(val[j] - 1)
				f /= 100.0
			}
			fv, _ := new(big.Float).SetInt(n).Float64()
			return fv + f
		}
		return numberValue(n)
	}

	// Negative value: realLen = 65 - int8(marker), trailing 0x66 terminator.
	realLen := 65 - int(int8(marker))
	n := new(big.Int)
	i := 1
	if realLen >= len(val) {
		for ; i < len(val)-1; i++ {
			n.Mul(n, bigHundred)
			n.Add(n, big.NewInt(int64(101-val[i])))
		}
		for ; i < realLen-1; i++ {
			n.Mul(n, bigHundred)
		}
	} else {
		for ; i < realLen-1; i++ {
			n.Mul(n, bigHundred)
			n.Add(n, big.NewInt(int64(101-val[i])))
		}
	}
	n.Neg(n)
	i++
	if i < len(val) {
		// Fractional part.
		var f float64
		for j := len(val) - 1; j >= i; j-- {
			f += float64(101 - val[j])
			f /= 100.0
		}
		f = -f
		fv, _ := new(big.Float).SetInt(n).Float64()
		return fv + f
	}
	return numberValue(n)
}
