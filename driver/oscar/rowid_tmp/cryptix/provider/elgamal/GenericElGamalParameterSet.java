/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.elgamal;

import cryptix.provider.elgamal.BaseElGamalParams;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import xjava.security.interfaces.ElGamalParams;

public class GenericElGamalParameterSet {
    private int[] primeLengths;
    private String[][] precomputed;

    protected GenericElGamalParameterSet(int[] primeLengths, String[][] precomputed) {
        if (precomputed.length != primeLengths.length) {
            throw new IllegalArgumentException("array lengths do not match");
        }
        this.primeLengths = primeLengths;
        this.precomputed = precomputed;
    }

    public ElGamalParams getParameters(int primeLength) {
        int i = 0;
        while (i < this.primeLengths.length) {
            if (primeLength == this.primeLengths[i]) {
                return new BaseElGamalParams(new BigInteger(this.precomputed[i][0], 16), this.precomputed[i][1] != null ? new BigInteger(this.precomputed[i][1], 16) : null);
            }
            ++i;
        }
        return null;
    }

    public void checkSane() throws InvalidParameterException {
        int i = 0;
        while (i < this.primeLengths.length) {
            BigInteger p = new BigInteger(this.precomputed[i][0]);
            if (p.bitLength() < this.primeLengths[i]) {
                throw new InvalidParameterException(p + " has incorrect bit length");
            }
            BigInteger g = new BigInteger(this.precomputed[i][1]);
            if (!p.isProbablePrime(80)) {
                throw new InvalidParameterException(p + " is not prime");
            }
            if (g.compareTo(p) >= 0) {
                throw new InvalidParameterException("g >= p");
            }
            ++i;
        }
    }
}

