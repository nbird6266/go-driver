/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.provider.key.RawKeyGenerator;

public class RC4KeyGenerator
extends RawKeyGenerator {
    public RC4KeyGenerator() {
        super("RC4", 5, 16, 128);
    }

    protected boolean isWeak(byte[] key) {
        return key.length < 2 || (key[0] + key[1]) % 256 == 0;
    }
}

