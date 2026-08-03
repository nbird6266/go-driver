/*
 * Decompiled with CFR 0.152.
 */
package xjava.security.interfaces;

import java.math.BigInteger;

public interface RSAKey {
    public BigInteger getModulus();

    public BigInteger getExponent();
}

