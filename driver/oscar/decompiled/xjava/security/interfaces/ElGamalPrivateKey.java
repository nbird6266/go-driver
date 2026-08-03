/*
 * Decompiled with CFR 0.152.
 */
package xjava.security.interfaces;

import java.math.BigInteger;
import java.security.PrivateKey;
import xjava.security.interfaces.ElGamalKey;

public interface ElGamalPrivateKey
extends ElGamalKey,
PrivateKey {
    public BigInteger getX();
}

