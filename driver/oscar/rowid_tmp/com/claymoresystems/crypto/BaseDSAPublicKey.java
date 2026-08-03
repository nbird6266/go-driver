/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.crypto;

import com.claymoresystems.crypto.RawDSAParams;
import java.math.BigInteger;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;

public class BaseDSAPublicKey
implements DSAPublicKey {
    protected BigInteger Y = null;
    protected DSAParams params = null;

    protected BaseDSAPublicKey() {
    }

    public BaseDSAPublicKey(DSAPublicKey key) {
        this.Y = key.getY();
        DSAParams p = key.getParams();
        this.params = new RawDSAParams(p.getP(), p.getQ(), p.getG());
    }

    public BigInteger getY() {
        return this.Y;
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

