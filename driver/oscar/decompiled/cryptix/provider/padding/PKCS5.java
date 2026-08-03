/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.padding;

import cryptix.CryptixException;
import xjava.security.PaddingScheme;

public class PKCS5
extends PaddingScheme {
    public PKCS5() {
        super("PKCS#5");
    }

    protected int enginePad(byte[] in, int offset, int length) {
        int padLen = this.padLength(length);
        byte padChar = (byte)padLen;
        int j = offset + length;
        int i = 0;
        while (i < padLen) {
            in[j++] = padChar;
            ++i;
        }
        return padLen;
    }

    protected int engineUnpad(byte[] in, int offset, int length) {
        int n = offset + length - 1;
        if (n >= 0) {
            if (in[n] > 8) {
                throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Invalid number of padding bytes");
            }
            return offset + length - (in[offset + length - 1] & 0xFF);
        }
        return 0;
    }

    protected boolean engineIsValidBlockSize(int size) {
        return size == 8;
    }
}

