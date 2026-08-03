/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.cipher;

import cryptix.CryptixException;
import cryptix.provider.cipher.NativeLink;
import cryptix.util.core.Debug;
import cryptix.util.core.LinkStatus;
import java.io.PrintWriter;
import java.security.Key;
import java.security.KeyException;
import xjava.security.Cipher;
import xjava.security.SymmetricCipher;

public final class LOKI91
extends Cipher
implements SymmetricCipher {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel = Debug.getLevel("LOKI91");
    private static final PrintWriter err = Debug.getOutput();
    private static NativeLink linkStatus = new NativeLink("LOKI91", 2, 3);
    private long native_cookie;
    private Object native_lock;
    private static final int BLOCK_SIZE = 8;
    private static final int ROUNDS = 16;
    private static final byte[] S = new byte[4096];
    private static final int[] P = new int[256];
    private int[] sKey = new int[16];

    static {
        int[] sGen = new int[]{375, 379, 391, 395, 397, 415, 419, 425, 433, 445, 451, 463, 471, 477, 487, 499};
        int i = 0;
        while (i < 4096) {
            int r = i >>> 8 & 0xC | i & 3;
            int c = i >>> 2 & 0xFF;
            int t = c + (r * 17 ^ 0xFF) & 0xFF;
            LOKI91.S[i] = LOKI91.exp31(t, sGen[r]);
            ++i;
        }
        int[] nArray = new int[32];
        nArray[0] = 31;
        nArray[1] = 23;
        nArray[2] = 15;
        nArray[3] = 7;
        nArray[4] = 30;
        nArray[5] = 22;
        nArray[6] = 14;
        nArray[7] = 6;
        nArray[8] = 29;
        nArray[9] = 21;
        nArray[10] = 13;
        nArray[11] = 5;
        nArray[12] = 28;
        nArray[13] = 20;
        nArray[14] = 12;
        nArray[15] = 4;
        nArray[16] = 27;
        nArray[17] = 19;
        nArray[18] = 11;
        nArray[19] = 3;
        nArray[20] = 26;
        nArray[21] = 18;
        nArray[22] = 10;
        nArray[23] = 2;
        nArray[24] = 25;
        nArray[25] = 17;
        nArray[26] = 9;
        nArray[27] = 1;
        nArray[28] = 24;
        nArray[29] = 16;
        nArray[30] = 8;
        int[] Po = nArray;
        int i2 = 0;
        while (i2 < 256) {
            int s = 0;
            int j = 0;
            while (j < 32) {
                s |= (i2 >>> Po[j] & 1) << 31 - j;
                ++j;
            }
            LOKI91.P[i2] = s;
            ++i2;
        }
    }

    private static void debug(String s) {
        err.println("LOKI91: " + s);
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
                        linkStatus.checkVersion(LOKI91.getLibMajorVersion(), LOKI91.getLibMinorVersion());
                        linkStatus.check(this.native_clinit());
                    }
                    if (linkStatus.useNative()) {
                        linkStatus.check(this.native_init());
                        this.native_lock = new Object();
                    }
                }
                catch (UnsatisfiedLinkError e) {
                    linkStatus.fail(e);
                    if (debuglevel <= 2) break block8;
                    LOKI91.debug(e.getMessage());
                }
            }
            if (debuglevel > 2) {
                LOKI91.debug("Using native library? " + (this.native_lock != null));
            }
        }
    }

    private static native int getLibMajorVersion();

    private static native int getLibMinorVersion();

    private native String native_clinit();

    private native String native_init();

    private native String native_ks(long var1, byte[] var3);

    private native int native_crypt(long var1, byte[] var3, int var4, byte[] var5, int var6, boolean var7);

    private native String native_finalize();

    private static final byte exp31(int b, int g) {
        if (b == 0) {
            return 0;
        }
        int r = b;
        b = LOKI91.mult(b, b, g);
        r = LOKI91.mult(r, b, g);
        b = LOKI91.mult(b, b, g);
        r = LOKI91.mult(r, b, g);
        b = LOKI91.mult(b, b, g);
        r = LOKI91.mult(r, b, g);
        b = LOKI91.mult(b, b, g);
        return (byte)LOKI91.mult(r, b, g);
    }

    private static final int mult(int a, int b, int g) {
        int p = 0;
        while (b != 0) {
            if ((b & 1) != 0) {
                p ^= a;
            }
            if ((a <<= 1) > 255) {
                a ^= g;
            }
            b >>>= 1;
        }
        return p;
    }

    public LOKI91() {
        super(false, false, "Cryptix");
        this.link();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void finalize() {
        if (this.native_lock != null) {
            Object object = this.native_lock;
            synchronized (object) {
                String error = this.native_finalize();
                if (error != null) {
                    LOKI91.debug(String.valueOf(error) + " in native_finalize");
                }
            }
        }
    }

    public final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    protected int engineBlockSize() {
        return 8;
    }

    protected void engineInitEncrypt(Key key) throws KeyException {
        this.makeKey(key);
    }

    protected void engineInitDecrypt(Key key) throws KeyException {
        this.makeKey(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        Object newin;
        boolean doEncrypt;
        if (inLen < 0) {
            throw new IllegalArgumentException("inLen < 0");
        }
        int blockCount = inLen / 8;
        inLen = blockCount * 8;
        boolean bl = doEncrypt = this.getState() == 1;
        if (in == out && (outOffset >= inOffset && (long)outOffset < (long)inOffset + (long)inLen || inOffset >= outOffset && (long)inOffset < (long)outOffset + (long)inLen)) {
            newin = new byte[inLen];
            System.arraycopy(in, inOffset, newin, 0, inLen);
            in = newin;
            inOffset = 0;
        }
        if (this.native_lock != null) {
            newin = this.native_lock;
            synchronized (newin) {
                if (inOffset < 0 || (long)inOffset + (long)inLen > (long)((byte[])in).length || outOffset < 0 || (long)outOffset + (long)inLen > (long)out.length) {
                    throw new ArrayIndexOutOfBoundsException(String.valueOf(this.getAlgorithm()) + ": Arguments to native_crypt would cause a buffer overflow");
                }
                int i = 0;
                while (i < blockCount) {
                    if (this.native_crypt(this.native_cookie, (byte[])in, inOffset, out, outOffset, doEncrypt) == 0) {
                        throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Error in native code");
                    }
                    inOffset += 8;
                    outOffset += 8;
                    ++i;
                }
            }
        } else if (doEncrypt) {
            int i = 0;
            while (i < blockCount) {
                this.blockEncrypt((byte[])in, inOffset, out, outOffset);
                inOffset += 8;
                outOffset += 8;
                ++i;
            }
        } else {
            int i = 0;
            while (i < blockCount) {
                this.blockDecrypt((byte[])in, inOffset, out, outOffset);
                inOffset += 8;
                outOffset += 8;
                ++i;
            }
        }
        return inLen;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized void makeKey(Key key) throws KeyException {
        byte[] userkey = key.getEncoded();
        if (userkey == null) {
            throw new KeyException("Null LOKI91 key");
        }
        if (userkey.length < 8) {
            throw new KeyException("Invalid LOKI91 user key length");
        }
        if (this.native_lock != null) {
            Object object = this.native_lock;
            synchronized (object) {
                try {
                    linkStatus.check(this.native_ks(this.native_cookie, userkey));
                    return;
                }
                catch (Error error) {
                    this.native_finalize();
                    this.native_lock = null;
                    if (debuglevel > 0) {
                        LOKI91.debug(error + ". Will use 100% Java.");
                    }
                }
            }
        }
        this.sKey[0] = (userkey[0] & 0xFF) << 24 | (userkey[1] & 0xFF) << 16 | (userkey[2] & 0xFF) << 8 | userkey[3] & 0xFF;
        this.sKey[1] = this.sKey[0] << 12 | this.sKey[0] >>> 20;
        this.sKey[2] = (userkey[4] & 0xFF) << 24 | (userkey[5] & 0xFF) << 16 | (userkey[6] & 0xFF) << 8 | userkey[7] & 0xFF;
        this.sKey[3] = this.sKey[2] << 12 | this.sKey[2] >>> 20;
        int i = 4;
        while (i < 16) {
            this.sKey[i] = this.sKey[i - 3] << 13 | this.sKey[i - 3] >>> 19;
            this.sKey[i + 1] = this.sKey[i] << 12 | this.sKey[i] >>> 20;
            this.sKey[i + 2] = this.sKey[i - 1] << 13 | this.sKey[i - 1] >>> 19;
            this.sKey[i + 3] = this.sKey[i + 2] << 12 | this.sKey[i + 2] >>> 20;
            i += 4;
        }
    }

    private void blockEncrypt(byte[] in, int off, byte[] out, int outOff) {
        int L = (in[off++] & 0xFF) << 24 | (in[off++] & 0xFF) << 16 | (in[off++] & 0xFF) << 8 | in[off++] & 0xFF;
        int R = (in[off++] & 0xFF) << 24 | (in[off++] & 0xFF) << 16 | (in[off++] & 0xFF) << 8 | in[off] & 0xFF;
        int i = 0;
        while (i < 16) {
            int a = R ^ this.sKey[i++];
            L ^= P[S[a & 0xFFF] & 0xFF] | P[S[a >>> 8 & 0xFFF] & 0xFF] << 1 | P[S[a >>> 16 & 0xFFF] & 0xFF] << 2 | P[S[(a >>> 24 | a << 8) & 0xFFF] & 0xFF] << 3;
            a = L ^ this.sKey[i++];
            R ^= P[S[a & 0xFFF] & 0xFF] | P[S[a >>> 8 & 0xFFF] & 0xFF] << 1 | P[S[a >>> 16 & 0xFFF] & 0xFF] << 2 | P[S[(a >>> 24 | a << 8) & 0xFFF] & 0xFF] << 3;
        }
        out[outOff++] = (byte)(R >>> 24);
        out[outOff++] = (byte)(R >>> 16);
        out[outOff++] = (byte)(R >>> 8);
        out[outOff++] = (byte)R;
        out[outOff++] = (byte)(L >>> 24);
        out[outOff++] = (byte)(L >>> 16);
        out[outOff++] = (byte)(L >>> 8);
        out[outOff] = (byte)L;
    }

    private void blockDecrypt(byte[] in, int off, byte[] out, int outOff) {
        int L = (in[off++] & 0xFF) << 24 | (in[off++] & 0xFF) << 16 | (in[off++] & 0xFF) << 8 | in[off++] & 0xFF;
        int R = (in[off++] & 0xFF) << 24 | (in[off++] & 0xFF) << 16 | (in[off++] & 0xFF) << 8 | in[off] & 0xFF;
        int i = 16;
        while (i > 0) {
            int a = R ^ this.sKey[--i];
            L ^= P[S[a & 0xFFF] & 0xFF] | P[S[a >>> 8 & 0xFFF] & 0xFF] << 1 | P[S[a >>> 16 & 0xFFF] & 0xFF] << 2 | P[S[(a >>> 24 | a << 8) & 0xFFF] & 0xFF] << 3;
            a = L ^ this.sKey[--i];
            R ^= P[S[a & 0xFFF] & 0xFF] | P[S[a >>> 8 & 0xFFF] & 0xFF] << 1 | P[S[a >>> 16 & 0xFFF] & 0xFF] << 2 | P[S[(a >>> 24 | a << 8) & 0xFFF] & 0xFF] << 3;
        }
        out[outOff++] = (byte)(R >>> 24);
        out[outOff++] = (byte)(R >>> 16);
        out[outOff++] = (byte)(R >>> 8);
        out[outOff++] = (byte)R;
        out[outOff++] = (byte)(L >>> 24);
        out[outOff++] = (byte)(L >>> 16);
        out[outOff++] = (byte)(L >>> 8);
        out[outOff] = (byte)L;
    }
}

