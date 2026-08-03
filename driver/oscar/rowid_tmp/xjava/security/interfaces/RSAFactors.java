/*
 * Decompiled with CFR 0.152.
 */
package xjava.security.interfaces;

import java.math.BigInteger;

public interface RSAFactors {
    public BigInteger getP();

    public BigInteger getQ();

    public BigInteger getInverseOfQModP();
}

