/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.md;

import cryptix.provider.md.BlockMessageDigest;
import cryptix.util.core.Debug;
import java.io.PrintWriter;

public final class SHA1
extends BlockMessageDigest
implements Cloneable {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel = Debug.getLevel("SHA-1");
    private static final PrintWriter err = Debug.getOutput();
    private static final int HASH_LENGTH = 20;
    private static final int DATA_LENGTH = 64;
    private int[] data;
    private int[] digest;
    private byte[] tmp;
    private int[] w;
    private static String[] texts = new String[]{"abc", "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq", ""};
    private static byte[][] hashs = new byte[][]{{-87, -103, 62, 54, 71, 6, -127, 106, -70, 62, 37, 113, 120, 80, -62, 108, -100, -48, -40, -99}, {-124, -104, 62, 68, 28, 59, -46, 110, -70, -82, 74, -95, -7, 81, 41, -27, -27, 70, 112, -15}, {-38, 57, -93, -18, 94, 107, 75, 13, 50, 85, -65, -17, -107, 96, 24, -112, -81, -40, 7, 9}};

    private static void debug(String s) {
        err.println("SHA-1: " + s);
    }

    protected int engineGetDigestLength() {
        return 20;
    }

    protected int engineGetDataLength() {
        return 64;
    }

    public SHA1() {
        super("SHA-1");
        this.java_init();
        this.reset();
    }

    private void java_init() {
        this.digest = new int[5];
        this.data = new int[16];
        this.tmp = new byte[64];
        this.w = new int[80];
    }

    private SHA1(SHA1 md) {
        this();
        this.data = (int[])md.data.clone();
        this.digest = (int[])md.digest.clone();
        this.tmp = (byte[])md.tmp.clone();
        this.w = (int[])md.w.clone();
    }

    public Object clone() {
        return new SHA1(this);
    }

    protected void engineReset() {
        super.engineReset();
        this.java_reset();
    }

    private void java_reset() {
        this.digest[0] = 1732584193;
        this.digest[1] = -271733879;
        this.digest[2] = -1732584194;
        this.digest[3] = 271733878;
        this.digest[4] = -1009589776;
    }

    protected void engineTransform(byte[] in) {
        this.java_transform(in);
    }

    private void java_transform(byte[] in) {
        SHA1.byte2int(in, 0, this.data, 0, 16);
        this.transform(this.data);
    }

    protected byte[] engineDigest(byte[] in, int length) {
        byte[] b = this.java_digest(in, length);
        this.engineReset();
        return b;
    }

    private byte[] java_digest(byte[] in, int pos) {
        if (pos != 0) {
            System.arraycopy(in, 0, this.tmp, 0, pos);
        }
        this.tmp[pos++] = -128;
        if (pos > 56) {
            while (pos < 64) {
                this.tmp[pos++] = 0;
            }
            SHA1.byte2int(this.tmp, 0, this.data, 0, 16);
            this.transform(this.data);
            pos = 0;
        }
        while (pos < 56) {
            this.tmp[pos++] = 0;
        }
        SHA1.byte2int(this.tmp, 0, this.data, 0, 14);
        long bc = this.bitcount();
        this.data[14] = (int)(bc >>> 32);
        this.data[15] = (int)bc;
        this.transform(this.data);
        byte[] buf = new byte[20];
        int off = 0;
        int i = 0;
        while (i < 5) {
            int d = this.digest[i];
            buf[off++] = (byte)(d >>> 24);
            buf[off++] = (byte)(d >>> 16);
            buf[off++] = (byte)(d >>> 8);
            buf[off++] = (byte)d;
            ++i;
        }
        return buf;
    }

    private static int f1(int a, int b, int c) {
        return (c ^ a & (b ^ c)) + 1518500249;
    }

    private static int f2(int a, int b, int c) {
        return (a ^ b ^ c) + 1859775393;
    }

    private static int f3(int a, int b, int c) {
        return (a & b | c & (a | b)) + -1894007588;
    }

    private static int f4(int a, int b, int c) {
        return (a ^ b ^ c) + -899497514;
    }

    private void transform(int[] X) {
        int A = this.digest[0];
        int B = this.digest[1];
        int C = this.digest[2];
        int D = this.digest[3];
        int E = this.digest[4];
        if (debuglevel > 5) {
            SHA1.debug("A=" + A + " B=" + B + " C=" + C + " D=" + D + " E=" + E);
        }
        int[] W = this.w;
        int i = 0;
        while (i < 16) {
            W[i] = X[i];
            if (debuglevel > 8) {
                SHA1.debug("       " + i + "=" + X[i]);
            }
            ++i;
        }
        i = 16;
        while (i < 80) {
            int j;
            W[i] = j = W[i - 16] ^ W[i - 14] ^ W[i - 8] ^ W[i - 3];
            W[i] = j << 1 | j >>> -1;
            ++i;
        }
        E += (A << 5 | A >>> -5) + SHA1.f1(B, C, D) + W[0];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f1(A, B, C) + W[1];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f1(E, A, B) + W[2];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f1(D, E, A) + W[3];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f1(C, D, E) + W[4];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f1(B, C, D) + W[5];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f1(A, B, C) + W[6];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f1(E, A, B) + W[7];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f1(D, E, A) + W[8];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f1(C, D, E) + W[9];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f1(B, C, D) + W[10];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f1(A, B, C) + W[11];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f1(E, A, B) + W[12];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f1(D, E, A) + W[13];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f1(C, D, E) + W[14];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f1(B, C, D) + W[15];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f1(A, B, C) + W[16];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f1(E, A, B) + W[17];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f1(D, E, A) + W[18];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f1(C, D, E) + W[19];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f2(B, C, D) + W[20];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f2(A, B, C) + W[21];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f2(E, A, B) + W[22];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f2(D, E, A) + W[23];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f2(C, D, E) + W[24];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f2(B, C, D) + W[25];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f2(A, B, C) + W[26];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f2(E, A, B) + W[27];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f2(D, E, A) + W[28];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f2(C, D, E) + W[29];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f2(B, C, D) + W[30];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f2(A, B, C) + W[31];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f2(E, A, B) + W[32];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f2(D, E, A) + W[33];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f2(C, D, E) + W[34];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f2(B, C, D) + W[35];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f2(A, B, C) + W[36];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f2(E, A, B) + W[37];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f2(D, E, A) + W[38];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f2(C, D, E) + W[39];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f3(B, C, D) + W[40];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f3(A, B, C) + W[41];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f3(E, A, B) + W[42];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f3(D, E, A) + W[43];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f3(C, D, E) + W[44];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f3(B, C, D) + W[45];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f3(A, B, C) + W[46];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f3(E, A, B) + W[47];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f3(D, E, A) + W[48];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f3(C, D, E) + W[49];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f3(B, C, D) + W[50];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f3(A, B, C) + W[51];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f3(E, A, B) + W[52];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f3(D, E, A) + W[53];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f3(C, D, E) + W[54];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f3(B, C, D) + W[55];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f3(A, B, C) + W[56];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f3(E, A, B) + W[57];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f3(D, E, A) + W[58];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f3(C, D, E) + W[59];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f4(B, C, D) + W[60];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f4(A, B, C) + W[61];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f4(E, A, B) + W[62];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f4(D, E, A) + W[63];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f4(C, D, E) + W[64];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f4(B, C, D) + W[65];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f4(A, B, C) + W[66];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f4(E, A, B) + W[67];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f4(D, E, A) + W[68];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f4(C, D, E) + W[69];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f4(B, C, D) + W[70];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f4(A, B, C) + W[71];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f4(E, A, B) + W[72];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f4(D, E, A) + W[73];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f4(C, D, E) + W[74];
        C = C << 30 | C >>> -30;
        E += (A << 5 | A >>> -5) + SHA1.f4(B, C, D) + W[75];
        B = B << 30 | B >>> -30;
        D += (E << 5 | E >>> -5) + SHA1.f4(A, B, C) + W[76];
        A = A << 30 | A >>> -30;
        C += (D << 5 | D >>> -5) + SHA1.f4(E, A, B) + W[77];
        E = E << 30 | E >>> -30;
        B += (C << 5 | C >>> -5) + SHA1.f4(D, E, A) + W[78];
        D = D << 30 | D >>> -30;
        A += (B << 5 | B >>> -5) + SHA1.f4(C, D, E) + W[79];
        C = C << 30 | C >>> -30;
        this.digest[0] = this.digest[0] + A;
        this.digest[1] = this.digest[1] + B;
        this.digest[2] = this.digest[2] + C;
        this.digest[3] = this.digest[3] + D;
        this.digest[4] = this.digest[4] + E;
        if (debuglevel > 5) {
            SHA1.debug("d1=" + this.digest[0] + " d1=" + this.digest[1] + " d2=" + this.digest[2] + " d3=" + this.digest[3] + " d4=" + this.digest[4]);
        }
    }

    private static void byte2int(byte[] src, int srcOffset, int[] dst, int dstOffset, int length) {
        while (length-- > 0) {
            dst[dstOffset++] = src[srcOffset++] << 24 | (src[srcOffset++] & 0xFF) << 16 | (src[srcOffset++] & 0xFF) << 8 | src[srcOffset++] & 0xFF;
        }
    }

    public static final void main(String[] argv) {
        try {
            SHA1.self_test();
        }
        catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static final void self_test() throws Exception {
        int length = hashs[0].length;
        int i = 0;
        while (i < texts.length) {
            SHA1 hash = new SHA1();
            byte[] text = texts[i].getBytes();
            int j = 0;
            while (j < texts[i].length()) {
                hash.engineUpdate(text[j]);
                ++j;
            }
            if (SHA1.notEquals(hash.engineDigest(), hashs[i])) {
                throw new Exception("hash #" + i + " failed");
            }
            ++i;
        }
    }

    private static final boolean notEquals(byte[] a, byte[] b) {
        int i = 0;
        while (i < a.length) {
            if (a[i] != b[i]) {
                return true;
            }
            ++i;
        }
        return false;
    }
}

