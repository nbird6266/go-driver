/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.provider.key.DESKeyGenerator;

public class DES2XKeyGenerator
extends DESKeyGenerator {
    public DES2XKeyGenerator() {
        super("DES2X", 32);
    }

    protected boolean isWeak(byte[] key) {
        return this.isWeak(key, 0);
    }
}

