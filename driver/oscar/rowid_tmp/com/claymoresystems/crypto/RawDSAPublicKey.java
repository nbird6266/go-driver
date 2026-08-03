/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.crypto;

import com.claymoresystems.crypto.BaseDSAPublicKey;
import java.math.BigInteger;
import java.security.interfaces.DSAParams;

public class RawDSAPublicKey
extends BaseDSAPublicKey {
    public RawDSAPublicKey(BigInteger Y, DSAParams params) {
        this.Y = Y;
        this.params = params;
    }
}

