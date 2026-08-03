/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.provider.key.RawKeyGenerator;

public class LOKI91KeyGenerator
extends RawKeyGenerator {
    private static final long[] weakKeys;

    static {
        long[] lArray = new long[16];
        lArray[1] = -1L;
        lArray[2] = 0x55555555AAAAAAAAL;
        lArray[3] = -6148914692668172971L;
        lArray[4] = 0x55555555L;
        lArray[5] = 0xAAAAAAAAL;
        lArray[6] = 0xFFFFFFFFL;
        lArray[7] = 0x5555555500000000L;
        lArray[8] = 0x5555555555555555L;
        lArray[9] = 0x55555555FFFFFFFFL;
        lArray[10] = -6148914694099828736L;
        lArray[11] = -6148914691236517206L;
        lArray[12] = -6148914689804861441L;
        lArray[13] = -4294967296L;
        lArray[14] = -2863311531L;
        lArray[15] = -1431655766L;
        weakKeys = lArray;
    }

    public LOKI91KeyGenerator() {
        super("LOKI91", 8);
    }

    public boolean isWeak(byte[] key) {
        long a = ((long)key[0] & 0xFFL) << 56 | ((long)key[1] & 0xFFL) << 48 | ((long)key[2] & 0xFFL) << 40 | ((long)key[3] & 0xFFL) << 32 | ((long)key[4] & 0xFFL) << 24 | ((long)key[5] & 0xFFL) << 16 | ((long)key[6] & 0xFFL) << 8 | (long)key[7] & 0xFFL;
        int i = 0;
        while (i < weakKeys.length) {
            if (weakKeys[i] == a) {
                return true;
            }
            ++i;
        }
        return false;
    }
}

