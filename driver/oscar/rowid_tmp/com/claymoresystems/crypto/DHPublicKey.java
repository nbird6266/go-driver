/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.crypto;

import java.math.BigInteger;
import java.security.PublicKey;

public class DHPublicKey
implements PublicKey {
    private BigInteger Y = null;
    private BigInteger g = null;
    private BigInteger p = null;

    public DHPublicKey(BigInteger g_, BigInteger p_, BigInteger Y_) {
        this.g = g_;
        this.p = p_;
        this.Y = Y_;
    }

    public DHPublicKey(BigInteger Y_) {
        this.Y = Y_;
    }

    public BigInteger getY() {
        return this.Y;
    }

    public BigInteger getg() {
        return this.g;
    }

    public BigInteger getp() {
        return this.p;
    }

    public byte[] getEncoded() {
        return null;
    }

    public String getAlgorithm() {
        return "DH";
    }

    public String getFormat() {
        return "None";
    }
}

