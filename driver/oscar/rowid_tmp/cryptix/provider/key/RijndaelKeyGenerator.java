/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.provider.key.RawKeyGenerator;

public class RijndaelKeyGenerator
extends RawKeyGenerator {
    public RijndaelKeyGenerator() {
        super("Rijndael", 16, 16, 32);
    }

    public boolean isValidKeyLength(int length) {
        return length == 16 || length == 24 || length == 32;
    }
}

