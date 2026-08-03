/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.provider.key.RawKeyGenerator;

public class SPEEDKeyGenerator
extends RawKeyGenerator {
    public SPEEDKeyGenerator() {
        super("SPEED", 6, 16, 32);
    }

    public boolean isValidKeyLength(int length) {
        return length >= 6 && length <= 32 && length % 2 == 0;
    }
}

