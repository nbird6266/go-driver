/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.crypto;

import com.claymoresystems.crypto.RawDSAParams;
import java.math.BigInteger;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;

public class BaseDSAPrivateKey
implements DSAPrivateKey {
    protected BigInteger X = null;
    protected DSAParams params = null;

    protected BaseDSAPrivateKey() {
    }

    public BaseDSAPrivateKey(DSAPrivateKey key) {
        this.X = key.getX();
        DSAParams p = key.getParams();
        this.params = new RawDSAParams(p.getP(), p.getQ(), p.getG());
    }

    public BigInteger getX() {
        return this.X;
    }

    public BigInteger getY() {
        return this.params.getG().modPow(this.X, this.params.getP());
    }

    public DSAParams getParams() {
        return this.params;
    }

    public byte[] getEncoded() {
        return new byte[2];
    }

    public String getFormat() {
        return "foo";
    }

    public String getAlgorithm() {
        return "DSA";
    }
}

