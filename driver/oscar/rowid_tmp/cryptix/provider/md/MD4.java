/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.md;

import java.security.MessageDigest;

public class MD4
extends MessageDigest
implements Cloneable {
    private static final int BLOCK_LENGTH = 64;
    private int[] context = new int[4];
    private long count;
    private byte[] buffer = new byte[64];
    private int[] X = new int[16];

    public MD4() {
        super("MD4");
        this.engineReset();
    }

    private MD4(MD4 md) {
        this();
        this.context = (int[])md.context.clone();
        this.buffer = (byte[])md.buffer.clone();
        this.count = md.count;
    }

    public Object clone() {
        return new MD4(this);
    }

    public void engineReset() {
        this.context[0] = 1732584193;
        this.context[1] = -271733879;
        this.context[2] = -1732584194;
        this.context[3] = 271733878;
        this.count = 0L;
        int i = 0;
        while (i < 64) {
            this.buffer[i] = 0;
            ++i;
        }
    }

    public void engineUpdate(byte b) {
        int i = (int)(this.count % 64L);
        ++this.count;
        this.buffer[i] = b;
        if (i == 63) {
            this.transform(this.buffer, 0);
        }
    }

    public void engineUpdate(byte[] input, int offset, int len) {
        if (offset < 0 || len < 0 || (long)offset + (long)len > (long)input.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int bufferNdx = (int)(this.count % 64L);
        this.count += (long)len;
        int partLen = 64 - bufferNdx;
        int i = 0;
        if (len >= partLen) {
            System.arraycopy(input, offset, this.buffer, bufferNdx, partLen);
            this.transform(this.buffer, 0);
            i = partLen;
            while (i + 64 - 1 < len) {
                this.transform(input, offset + i);
                i += 64;
            }
            bufferNdx = 0;
        }
        if (i < len) {
            System.arraycopy(input, offset + i, this.buffer, bufferNdx, len - i);
        }
    }

    public byte[] engineDigest() {
        int bufferNdx = (int)(this.count % 64L);
        int padLen = bufferNdx < 56 ? 56 - bufferNdx : 120 - bufferNdx;
        byte[] tail = new byte[padLen + 8];
        tail[0] = -128;
        int i = 0;
        while (i < 8) {
            tail[padLen + i] = (byte)(this.count * 8L >>> 8 * i);
            ++i;
        }
        this.engineUpdate(tail, 0, tail.length);
        byte[] result = new byte[16];
        int i2 = 0;
        while (i2 < 4) {
            int j = 0;
            while (j < 4) {
                result[i2 * 4 + j] = (byte)(this.context[i2] >>> 8 * j);
                ++j;
            }
            ++i2;
        }
        this.engineReset();
        return result;
    }

    private void transform(byte[] block, int offset) {
        int i = 0;
        while (i < 16) {
            this.X[i] = block[offset++] & 0xFF | (block[offset++] & 0xFF) << 8 | (block[offset++] & 0xFF) << 16 | (block[offset++] & 0xFF) << 24;
            ++i;
        }
        int A = this.context[0];
        int B = this.context[1];
        int C = this.context[2];
        int D = this.context[3];
        A = this.FF(A, B, C, D, this.X[0], 3);
        D = this.FF(D, A, B, C, this.X[1], 7);
        C = this.FF(C, D, A, B, this.X[2], 11);
        B = this.FF(B, C, D, A, this.X[3], 19);
        A = this.FF(A, B, C, D, this.X[4], 3);
        D = this.FF(D, A, B, C, this.X[5], 7);
        C = this.FF(C, D, A, B, this.X[6], 11);
        B = this.FF(B, C, D, A, this.X[7], 19);
        A = this.FF(A, B, C, D, this.X[8], 3);
        D = this.FF(D, A, B, C, this.X[9], 7);
        C = this.FF(C, D, A, B, this.X[10], 11);
        B = this.FF(B, C, D, A, this.X[11], 19);
        A = this.FF(A, B, C, D, this.X[12], 3);
        D = this.FF(D, A, B, C, this.X[13], 7);
        C = this.FF(C, D, A, B, this.X[14], 11);
        B = this.FF(B, C, D, A, this.X[15], 19);
        A = this.GG(A, B, C, D, this.X[0], 3);
        D = this.GG(D, A, B, C, this.X[4], 5);
        C = this.GG(C, D, A, B, this.X[8], 9);
        B = this.GG(B, C, D, A, this.X[12], 13);
        A = this.GG(A, B, C, D, this.X[1], 3);
        D = this.GG(D, A, B, C, this.X[5], 5);
        C = this.GG(C, D, A, B, this.X[9], 9);
        B = this.GG(B, C, D, A, this.X[13], 13);
        A = this.GG(A, B, C, D, this.X[2], 3);
        D = this.GG(D, A, B, C, this.X[6], 5);
        C = this.GG(C, D, A, B, this.X[10], 9);
        B = this.GG(B, C, D, A, this.X[14], 13);
        A = this.GG(A, B, C, D, this.X[3], 3);
        D = this.GG(D, A, B, C, this.X[7], 5);
        C = this.GG(C, D, A, B, this.X[11], 9);
        B = this.GG(B, C, D, A, this.X[15], 13);
        A = this.HH(A, B, C, D, this.X[0], 3);
        D = this.HH(D, A, B, C, this.X[8], 9);
        C = this.HH(C, D, A, B, this.X[4], 11);
        B = this.HH(B, C, D, A, this.X[12], 15);
        A = this.HH(A, B, C, D, this.X[2], 3);
        D = this.HH(D, A, B, C, this.X[10], 9);
        C = this.HH(C, D, A, B, this.X[6], 11);
        B = this.HH(B, C, D, A, this.X[14], 15);
        A = this.HH(A, B, C, D, this.X[1], 3);
        D = this.HH(D, A, B, C, this.X[9], 9);
        C = this.HH(C, D, A, B, this.X[5], 11);
        B = this.HH(B, C, D, A, this.X[13], 15);
        A = this.HH(A, B, C, D, this.X[3], 3);
        D = this.HH(D, A, B, C, this.X[11], 9);
        C = this.HH(C, D, A, B, this.X[7], 11);
        B = this.HH(B, C, D, A, this.X[15], 15);
        this.context[0] = this.context[0] + A;
        this.context[1] = this.context[1] + B;
        this.context[2] = this.context[2] + C;
        this.context[3] = this.context[3] + D;
    }

    private int FF(int a, int b, int c, int d, int x, int s) {
        int t = a + (b & c | ~b & d) + x;
        return t << s | t >>> 32 - s;
    }

    private int GG(int a, int b, int c, int d, int x, int s) {
        int t = a + (b & (c | d) | c & d) + x + 1518500249;
        return t << s | t >>> 32 - s;
    }

    private int HH(int a, int b, int c, int d, int x, int s) {
        int t = a + (b ^ c ^ d) + x + 1859775393;
        return t << s | t >>> 32 - s;
    }
}

