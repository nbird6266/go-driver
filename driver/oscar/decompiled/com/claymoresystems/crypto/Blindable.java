/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.crypto;

import java.security.SecureRandom;
import xjava.security.interfaces.CryptixRSAPublicKey;

public interface Blindable {
    public void setBlindingInfo(SecureRandom var1, CryptixRSAPublicKey var2);
}

