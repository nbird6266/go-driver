/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.X509RSAPrivateKey;
import java.io.IOException;

public class EAYRSAPrivateKey
extends X509RSAPrivateKey {
    public EAYRSAPrivateKey(byte[] encoding) throws IOException {
        super("1.2.840.113549.1.1", null, encoding);
    }
}

