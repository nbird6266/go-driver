/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.crypto;

import java.math.BigInteger;
import java.security.interfaces.DSAParams;

public class RawDSAParams
implements DSAParams {
    BigInteger p;
    BigInteger q;
    BigInteger g;

    public RawDSAParams(BigInteger p, BigInteger q, BigInteger g) {
        this.p = p;
        this.q = q;
        this.g = g;
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getQ() {
        return this.q;
    }

    public BigInteger getG() {
        return this.g;
    }
}

