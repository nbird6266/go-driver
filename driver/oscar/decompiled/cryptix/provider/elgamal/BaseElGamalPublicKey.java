/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.elgamal;

import java.math.BigInteger;
import xjava.security.interfaces.ElGamalParams;
import xjava.security.interfaces.ElGamalPublicKey;

public class BaseElGamalPublicKey
implements ElGamalPublicKey {
    protected BigInteger p;
    protected BigInteger g;
    protected BigInteger y;

    public BaseElGamalPublicKey(BigInteger p, BigInteger g, BigInteger y) {
        if (p == null) {
            throw new NullPointerException("p == null");
        }
        if (g == null) {
            throw new NullPointerException("g == null");
        }
        if (y == null) {
            throw new NullPointerException("y == null");
        }
        this.p = p;
        this.g = g;
        this.y = y;
    }

    public BaseElGamalPublicKey(ElGamalParams params, BigInteger y) {
        this(params.getP(), params.getG(), y);
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getG() {
        return this.g;
    }

    public BigInteger getY() {
        return this.y;
    }

    public String getAlgorithm() {
        return "ElGamal";
    }

    public String getFormat() {
        return null;
    }

    public byte[] getEncoded() {
        return null;
    }
}

