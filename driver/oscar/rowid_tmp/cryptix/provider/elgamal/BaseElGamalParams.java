/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.elgamal;

import java.math.BigInteger;
import xjava.security.interfaces.ElGamalParams;

public class BaseElGamalParams
implements ElGamalParams {
    protected BigInteger p;
    protected BigInteger g;

    public BaseElGamalParams(BigInteger p, BigInteger g) {
        this.p = p;
        this.g = g;
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getG() {
        return this.g;
    }
}

