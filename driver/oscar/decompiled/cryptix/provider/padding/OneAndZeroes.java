/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.padding;

import xjava.security.PaddingScheme;

public final class OneAndZeroes
extends PaddingScheme {
    public OneAndZeroes() {
        super("OneAndZeroes");
    }

    protected int enginePad(byte[] in, int offset, int length) {
        int padLen = this.padLength(length);
        int j = offset + length;
        in[j++] = -128;
        int i = 1;
        while (i < padLen) {
            in[j++] = 0;
            ++i;
        }
        return padLen;
    }

    protected int engineUnpad(byte[] in, int offset, int length) {
        int i = offset + length - 1;
        while (i > 0 && in[i] == 0) {
            --i;
        }
        return i < 0 ? 0 : i;
    }

    protected boolean engineIsValidBlockSize(int size) {
        return true;
    }
}

