/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.provider;

import java.security.Provider;

public final class ClaymoreProvider
extends Provider {
    public ClaymoreProvider() {
        super("ClaymoreProvider", 1.0, "Claymore Provider 1.0, implements RawDSA,RawRSAPKCS#1");
        this.put("Signature.RawDSA", "com.claymoresystems.provider.RawDSASignature");
        this.put("Signature.SHA/DSA", "com.claymoresystems.provider.DSASignature");
        this.put("Signature.DSA", "com.claymoresystems.provider.DSASignature");
        this.put("Signature.RawRSA", "com.claymoresystems.provider.RSASignature");
        this.put("Cipher.RSABlind", "com.claymoresystems.provider.RawRSACipher");
    }
}

