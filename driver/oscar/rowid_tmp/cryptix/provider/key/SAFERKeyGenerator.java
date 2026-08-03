/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.provider.key.RawKeyGenerator;

public class SAFERKeyGenerator
extends RawKeyGenerator {
    public SAFERKeyGenerator() {
        super("SAFER", 8, 16, 16);
    }

    public boolean isValidKeyLength(int length) {
        return length == 8 || length == 16;
    }
}

