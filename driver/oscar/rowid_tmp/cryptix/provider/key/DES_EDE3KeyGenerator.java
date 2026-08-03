/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.provider.key.DESKeyGenerator;

public class DES_EDE3KeyGenerator
extends DESKeyGenerator {
    public DES_EDE3KeyGenerator() {
        super("DES-EDE3", 24);
    }

    protected boolean isWeak(byte[] key) {
        if (this.isWeak(key, 0) || this.isWeak(key, 8) || this.isWeak(key, 16)) {
            return true;
        }
        long k1 = ((long)key[0] & 0xFEL) << 56 | ((long)key[1] & 0xFEL) << 48 | ((long)key[2] & 0xFEL) << 40 | ((long)key[3] & 0xFEL) << 32 | ((long)key[4] & 0xFEL) << 24 | ((long)key[5] & 0xFEL) << 16 | ((long)key[6] & 0xFEL) << 8 | (long)key[7] & 0xFEL;
        long k2 = ((long)key[8] & 0xFEL) << 56 | ((long)key[9] & 0xFEL) << 48 | ((long)key[10] & 0xFEL) << 40 | ((long)key[11] & 0xFEL) << 32 | ((long)key[12] & 0xFEL) << 24 | ((long)key[13] & 0xFEL) << 16 | ((long)key[14] & 0xFEL) << 8 | (long)key[15] & 0xFEL;
        long k3 = ((long)key[16] & 0xFEL) << 56 | ((long)key[17] & 0xFEL) << 48 | ((long)key[18] & 0xFEL) << 40 | ((long)key[19] & 0xFEL) << 32 | ((long)key[20] & 0xFEL) << 24 | ((long)key[21] & 0xFEL) << 16 | ((long)key[22] & 0xFEL) << 8 | (long)key[23] & 0xFEL;
        return k1 == k2 || k2 == k3 || k1 == k3;
    }
}

