/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.elgamal;

import cryptix.provider.elgamal.BaseElGamalPublicKey;
import java.math.BigInteger;
import xjava.security.interfaces.ElGamalParams;
import xjava.security.interfaces.ElGamalPrivateKey;

public class BaseElGamalPrivateKey
extends BaseElGamalPublicKey
implements ElGamalPrivateKey {
    protected BigInteger x;

    public BaseElGamalPrivateKey(BigInteger p, BigInteger g, BigInteger x, BigInteger y) {
        super(p, g, y);
        if (x == null) {
            throw new NullPointerException("x == null");
        }
        this.x = x;
    }

    public BaseElGamalPrivateKey(BigInteger p, BigInteger g, BigInteger x) {
        this(p, g, x, g.modPow(x, p));
    }

    protected BaseElGamalPrivateKey(ElGamalParams params, BigInteger x) {
        this(params.getP(), params.getG(), x);
    }

    public BigInteger getX() {
        return this.x;
    }
}

