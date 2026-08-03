/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.provider.key.RawKeyGenerator;
import java.security.InvalidKeyException;
import xjava.security.WeakKeyException;

public class DESKeyGenerator
extends RawKeyGenerator {
    public DESKeyGenerator() {
        super("DES", 8);
    }

    protected DESKeyGenerator(String algorithm, int seedlength) {
        super(algorithm, seedlength);
    }

    protected byte[] engineGenerateKey(byte[] seed) throws WeakKeyException, InvalidKeyException {
        seed = super.engineGenerateKey(seed);
        this.setParity(seed);
        return seed;
    }

    protected void setParity(byte[] array) {
        int i = 0;
        while (i < array.length) {
            byte b = array[i];
            array[i] = (byte)(b & 0xFE | (b >> 1 ^ b >> 2 ^ b >> 3 ^ b >> 4 ^ b >> 5 ^ b >> 6 ^ b >> 7) & 1);
            ++i;
        }
    }

    protected boolean isWeak(byte[] key) {
        return this.isWeak(key, 0);
    }

    protected boolean isWeak(byte[] key, int offset) {
        int a = (key[offset] & 0xFE) << 8 | key[offset + 1] & 0xFE;
        int b = (key[offset + 2] & 0xFE) << 8 | key[offset + 3] & 0xFE;
        int c = (key[offset + 4] & 0xFE) << 8 | key[offset + 5] & 0xFE;
        int d = (key[offset + 6] & 0xFE) << 8 | key[offset + 7] & 0xFE;
        return !(a != 0 && a != 65278 || b != 0 && b != 65278 || c != 0 && c != 65278 || d != 0 && d != 65278);
    }
}

