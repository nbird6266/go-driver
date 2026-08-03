/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.provider.key.RawKeyGenerator;

public class IDEAKeyGenerator
extends RawKeyGenerator {
    public IDEAKeyGenerator() {
        super("IDEA", 16);
    }

    public boolean isWeak(byte[] key) {
        if (key[0] != 0 || key[1] != 0 || key[2] != 0 || (key[3] & 0xC0) != 0 || (key[5] & 0x7F) != 0 || key[6] != 0 || key[7] != 0 || key[8] != 0 || (key[10] & 0xF) != 0 || key[11] != 0 || (key[12] & 0xE0) != 0) {
            return false;
        }
        if ((key[3] & 7) == 0 && key[4] == 0 && key[5] == 0 && (key[9] & 0x1F) == 0 && key[10] == 0 && key[12] == 0 && (key[13] & 0xFE) == 0) {
            return true;
        }
        return (key[15] & 0x1F) == 0;
    }
}

