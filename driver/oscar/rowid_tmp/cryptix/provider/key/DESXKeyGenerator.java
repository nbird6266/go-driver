/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.provider.key.DESKeyGenerator;

public class DESXKeyGenerator
extends DESKeyGenerator {
    public DESXKeyGenerator() {
        super("DESX", 16);
    }

    protected boolean isWeak(byte[] key) {
        return this.isWeak(key, 0);
    }
}

