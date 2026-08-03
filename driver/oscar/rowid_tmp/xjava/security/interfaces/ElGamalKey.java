/*
 * Decompiled with CFR 0.152.
 */
package xjava.security.interfaces;

import java.math.BigInteger;

public interface ElGamalKey {
    public BigInteger getP();

    public BigInteger getG();

    public BigInteger getY();
}

