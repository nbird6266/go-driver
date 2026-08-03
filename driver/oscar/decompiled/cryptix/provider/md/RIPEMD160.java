/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.md;

import cryptix.provider.md.NativeLink;
import cryptix.util.core.Debug;
import cryptix.util.core.LinkStatus;
import java.io.PrintWriter;
import java.security.MessageDigest;

public class RIPEMD160
extends MessageDigest
implements Cloneable {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static int debuglevel = Debug.getLevel("RIPEMD160");
    private static final PrintWriter err = Debug.getOutput();
    private static NativeLink linkStatus = new NativeLink("RIPEMD", 2, 3);
    private boolean native_ok;
    private static final int BLOCK_LENGTH = 64;
    private static final int CONTEXT_LENGTH = 5;
    private int[] context = new int[5];
    private long count;
    private byte[] buffer = new byte[64];
    private int[] X = new int[16];
    private static final int[] R;
    private static final int[] Rp;
    private static final int[] S;
    private static final int[] Sp;

    static {
        int[] nArray = new int[80];
        nArray[1] = 1;
        nArray[2] = 2;
        nArray[3] = 3;
        nArray[4] = 4;
        nArray[5] = 5;
        nArray[6] = 6;
        nArray[7] = 7;
        nArray[8] = 8;
        nArray[9] = 9;
        nArray[10] = 10;
        nArray[11] = 11;
        nArray[12] = 12;
        nArray[13] = 13;
        nArray[14] = 14;
        nArray[15] = 15;
        nArray[16] = 7;
        nArray[17] = 4;
        nArray[18] = 13;
        nArray[19] = 1;
        nArray[20] = 10;
        nArray[21] = 6;
        nArray[22] = 15;
        nArray[23] = 3;
        nArray[24] = 12;
        nArray[26] = 9;
        nArray[27] = 5;
        nArray[28] = 2;
        nArray[29] = 14;
        nArray[30] = 11;
        nArray[31] = 8;
        nArray[32] = 3;
        nArray[33] = 10;
        nArray[34] = 14;
        nArray[35] = 4;
        nArray[36] = 9;
        nArray[37] = 15;
        nArray[38] = 8;
        nArray[39] = 1;
        nArray[40] = 2;
        nArray[41] = 7;
        nArray[43] = 6;
        nArray[44] = 13;
        nArray[45] = 11;
        nArray[46] = 5;
        nArray[47] = 12;
        nArray[48] = 1;
        nArray[49] = 9;
        nArray[50] = 11;
        nArray[51] = 10;
        nArray[53] = 8;
        nArray[54] = 12;
        nArray[55] = 4;
        nArray[56] = 13;
        nArray[57] = 3;
        nArray[58] = 7;
        nArray[59] = 15;
        nArray[60] = 14;
        nArray[61] = 5;
        nArray[62] = 6;
        nArray[63] = 2;
        nArray[64] = 4;
        nArray[66] = 5;
        nArray[67] = 9;
        nArray[68] = 7;
        nArray[69] = 12;
        nArray[70] = 2;
        nArray[71] = 10;
        nArray[72] = 14;
        nArray[73] = 1;
        nArray[74] = 3;
        nArray[75] = 8;
        nArray[76] = 11;
        nArray[77] = 6;
        nArray[78] = 15;
        nArray[79] = 13;
        R = nArray;
        int[] nArray2 = new int[80];
        nArray2[0] = 5;
        nArray2[1] = 14;
        nArray2[2] = 7;
        nArray2[4] = 9;
        nArray2[5] = 2;
        nArray2[6] = 11;
        nArray2[7] = 4;
        nArray2[8] = 13;
        nArray2[9] = 6;
        nArray2[10] = 15;
        nArray2[11] = 8;
        nArray2[12] = 1;
        nArray2[13] = 10;
        nArray2[14] = 3;
        nArray2[15] = 12;
        nArray2[16] = 6;
        nArray2[17] = 11;
        nArray2[18] = 3;
        nArray2[19] = 7;
        nArray2[21] = 13;
        nArray2[22] = 5;
        nArray2[23] = 10;
        nArray2[24] = 14;
        nArray2[25] = 15;
        nArray2[26] = 8;
        nArray2[27] = 12;
        nArray2[28] = 4;
        nArray2[29] = 9;
        nArray2[30] = 1;
        nArray2[31] = 2;
        nArray2[32] = 15;
        nArray2[33] = 5;
        nArray2[34] = 1;
        nArray2[35] = 3;
        nArray2[36] = 7;
        nArray2[37] = 14;
        nArray2[38] = 6;
        nArray2[39] = 9;
        nArray2[40] = 11;
        nArray2[41] = 8;
        nArray2[42] = 12;
        nArray2[43] = 2;
        nArray2[44] = 10;
        nArray2[46] = 4;
        nArray2[47] = 13;
        nArray2[48] = 8;
        nArray2[49] = 6;
        nArray2[50] = 4;
        nArray2[51] = 1;
        nArray2[52] = 3;
        nArray2[53] = 11;
        nArray2[54] = 15;
        nArray2[56] = 5;
        nArray2[57] = 12;
        nArray2[58] = 2;
        nArray2[59] = 13;
        nArray2[60] = 9;
        nArray2[61] = 7;
        nArray2[62] = 10;
        nArray2[63] = 14;
        nArray2[64] = 12;
        nArray2[65] = 15;
        nArray2[66] = 10;
        nArray2[67] = 4;
        nArray2[68] = 1;
        nArray2[69] = 5;
        nArray2[70] = 8;
        nArray2[71] = 7;
        nArray2[72] = 6;
        nArray2[73] = 2;
        nArray2[74] = 13;
        nArray2[75] = 14;
        nArray2[77] = 3;
        nArray2[78] = 9;
        nArray2[79] = 11;
        Rp = nArray2;
        S = new int[]{11, 14, 15, 12, 5, 8, 7, 9, 11, 13, 14, 15, 6, 7, 9, 8, 7, 6, 8, 13, 11, 9, 7, 15, 7, 12, 15, 9, 11, 7, 13, 12, 11, 13, 6, 7, 14, 9, 13, 15, 14, 8, 13, 6, 5, 12, 7, 5, 11, 12, 14, 15, 14, 15, 9, 8, 9, 14, 5, 6, 8, 6, 5, 12, 9, 15, 5, 11, 6, 8, 13, 12, 5, 12, 13, 14, 11, 8, 5, 6};
        Sp = new int[]{8, 9, 9, 11, 13, 15, 15, 5, 7, 7, 8, 11, 14, 14, 12, 6, 9, 13, 15, 7, 12, 8, 9, 11, 7, 7, 12, 7, 6, 15, 13, 11, 9, 7, 15, 11, 8, 6, 6, 14, 12, 13, 5, 14, 13, 13, 7, 5, 15, 5, 8, 11, 14, 14, 6, 14, 6, 9, 12, 9, 12, 5, 15, 8, 8, 5, 12, 9, 12, 5, 14, 6, 8, 13, 6, 5, 15, 13, 11, 11};
    }

    private static void debug(String s) {
        err.println("RIPEMD160: " + s);
    }

    public static LinkStatus getLinkStatus() {
        return linkStatus;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void link() {
        NativeLink nativeLink = linkStatus;
        synchronized (nativeLink) {
            block8: {
                try {
                    if (linkStatus.attemptLoad()) {
                        linkStatus.checkVersion(RIPEMD160.getLibMajorVersion(), RIPEMD160.getLibMinorVersion());
                    }
                    if (linkStatus.useNative()) {
                        this.native_ok = true;
                    }
                }
                catch (UnsatisfiedLinkError e) {
                    linkStatus.fail(e);
                    if (debuglevel <= 2) break block8;
                    RIPEMD160.debug(e.getMessage());
                }
            }
            if (debuglevel > 2) {
                RIPEMD160.debug("Using native library? " + this.native_ok);
            }
        }
    }

    private static native int getLibMajorVersion();

    private static native int getLibMinorVersion();

    private static native String native_hash(int[] var0, byte[] var1, int var2);

    public RIPEMD160() {
        super("RIPEMD160");
        this.engineReset();
        this.link();
    }

    private RIPEMD160(RIPEMD160 md) {
        this();
        this.context = (int[])md.context.clone();
        this.buffer = (byte[])md.buffer.clone();
        this.count = md.count;
    }

    public Object clone() {
        return new RIPEMD160(this);
    }

    protected void engineReset() {
        this.context[0] = 1732584193;
        this.context[1] = -271733879;
        this.context[2] = -1732584194;
        this.context[3] = 271733878;
        this.context[4] = -1009589776;
        this.count = 0L;
        int i = 0;
        while (i < 64) {
            this.buffer[i] = 0;
            ++i;
        }
    }

    protected void engineUpdate(byte input) {
        int i = (int)(this.count % 64L);
        ++this.count;
        this.buffer[i] = input;
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

    protected byte[] engineDigest() {
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
        byte[] result = new byte[20];
        int i2 = 0;
        while (i2 < 5) {
            int j = 0;
            while (j < 4) {
                result[i2 * 4 + j] = (byte)(this.context[i2] >>> 8 * j & 0xFF);
                ++j;
            }
            ++i2;
        }
        this.engineReset();
        return result;
    }

    protected int engineGetDigestLength() {
        return 20;
    }

    private void transform(byte[] block, int offset) {
        int T;
        int s;
        int Ep;
        int Dp;
        int Cp;
        int Bp;
        int Ap;
        if (this.native_ok) {
            if (this.context.length != 5 || offset < 0 || (long)offset + 64L > (long)block.length) {
                throw new InternalError(String.valueOf(this.getAlgorithm()) + ": context.length != " + 5 + " || offset < 0 || " + "(long)offset + " + 64 + " > block.length");
            }
            linkStatus.check(RIPEMD160.native_hash(this.context, block, offset));
            return;
        }
        int i = 0;
        while (i < 16) {
            this.X[i] = block[offset++] & 0xFF | (block[offset++] & 0xFF) << 8 | (block[offset++] & 0xFF) << 16 | (block[offset++] & 0xFF) << 24;
            ++i;
        }
        int A = Ap = this.context[0];
        int B = Bp = this.context[1];
        int C = Cp = this.context[2];
        int D = Dp = this.context[3];
        int E = Ep = this.context[4];
        i = 0;
        while (i < 16) {
            s = S[i];
            T = A + (B ^ C ^ D) + this.X[i];
            A = E;
            E = D;
            D = C << 10 | C >>> 22;
            C = B;
            B = (T << s | T >>> 32 - s) + A;
            s = Sp[i];
            T = Ap + (Bp ^ (Cp | ~Dp)) + this.X[Rp[i]] + 1352829926;
            Ap = Ep;
            Ep = Dp;
            Dp = Cp << 10 | Cp >>> 22;
            Cp = Bp;
            Bp = (T << s | T >>> 32 - s) + Ap;
            ++i;
        }
        i = 16;
        while (i < 32) {
            s = S[i];
            T = A + (B & C | ~B & D) + this.X[R[i]] + 1518500249;
            A = E;
            E = D;
            D = C << 10 | C >>> 22;
            C = B;
            B = (T << s | T >>> 32 - s) + A;
            s = Sp[i];
            T = Ap + (Bp & Dp | Cp & ~Dp) + this.X[Rp[i]] + 1548603684;
            Ap = Ep;
            Ep = Dp;
            Dp = Cp << 10 | Cp >>> 22;
            Cp = Bp;
            Bp = (T << s | T >>> 32 - s) + Ap;
            ++i;
        }
        i = 32;
        while (i < 48) {
            s = S[i];
            T = A + ((B | ~C) ^ D) + this.X[R[i]] + 1859775393;
            A = E;
            E = D;
            D = C << 10 | C >>> 22;
            C = B;
            B = (T << s | T >>> 32 - s) + A;
            s = Sp[i];
            T = Ap + ((Bp | ~Cp) ^ Dp) + this.X[Rp[i]] + 1836072691;
            Ap = Ep;
            Ep = Dp;
            Dp = Cp << 10 | Cp >>> 22;
            Cp = Bp;
            Bp = (T << s | T >>> 32 - s) + Ap;
            ++i;
        }
        i = 48;
        while (i < 64) {
            s = S[i];
            T = A + (B & D | C & ~D) + this.X[R[i]] + -1894007588;
            A = E;
            E = D;
            D = C << 10 | C >>> 22;
            C = B;
            B = (T << s | T >>> 32 - s) + A;
            s = Sp[i];
            T = Ap + (Bp & Cp | ~Bp & Dp) + this.X[Rp[i]] + 2053994217;
            Ap = Ep;
            Ep = Dp;
            Dp = Cp << 10 | Cp >>> 22;
            Cp = Bp;
            Bp = (T << s | T >>> 32 - s) + Ap;
            ++i;
        }
        i = 64;
        while (i < 80) {
            s = S[i];
            T = A + (B ^ (C | ~D)) + this.X[R[i]] + -1454113458;
            A = E;
            E = D;
            D = C << 10 | C >>> 22;
            C = B;
            B = (T << s | T >>> 32 - s) + A;
            s = Sp[i];
            T = Ap + (Bp ^ Cp ^ Dp) + this.X[Rp[i]];
            Ap = Ep;
            Ep = Dp;
            Dp = Cp << 10 | Cp >>> 22;
            Cp = Bp;
            Bp = (T << s | T >>> 32 - s) + Ap;
            ++i;
        }
        T = this.context[1] + C + Dp;
        this.context[1] = this.context[2] + D + Ep;
        this.context[2] = this.context[3] + E + Ap;
        this.context[3] = this.context[4] + A + Bp;
        this.context[4] = this.context[0] + B + Cp;
        this.context[0] = T;
    }
}

