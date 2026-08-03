/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

import java.security.InvalidKeyException;
import java.security.SecureRandom;
import xjava.security.SecretKey;
import xjava.security.WeakKeyException;

public interface ExtendedKeyGenerator {
    public void initialize(SecureRandom var1);

    public SecretKey generateKey();

    public void initialize(SecureRandom var1, int var2);

    public SecretKey generateKey(byte[] var1) throws WeakKeyException, InvalidKeyException;

    public boolean isWeakAllowed();

    public void setWeakAllowed(boolean var1);

    public int getMinimumKeyLength();

    public int getDefaultKeyLength();

    public int getMaximumKeyLength();

    public boolean isValidKeyLength(int var1);
}

