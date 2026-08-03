/*
 * Decompiled with CFR 0.152.
 */
package xjava.security.interfaces;

import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.SecureRandom;

public interface RSAKeyPairGenerator {
    public void initialize(int var1, BigInteger var2, SecureRandom var3) throws InvalidParameterException;
}

