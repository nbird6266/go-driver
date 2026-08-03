/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.cipher;

import cryptix.CryptixException;
import cryptix.provider.cipher.NativeLink;
import cryptix.provider.key.RawSecretKey;
import cryptix.util.core.ArrayUtil;
import cryptix.util.core.Debug;
import cryptix.util.core.LinkStatus;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import xjava.security.Cipher;
import xjava.security.SymmetricCipher;

public final class IDEA
extends Cipher
implements SymmetricCipher {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel = Debug.getLevel("IDEA");
    private static final PrintWriter err = Debug.getOutput();
    private static NativeLink linkStatus = new NativeLink("IDEA", 2, 3);
    private long native_cookie;
    private Object native_lock;
    private static final int ROUNDS = 8;
    private static final int BLOCK_SIZE = 8;
    private static final int KEY_LENGTH = 16;
    private static final int INTERNAL_KEY_LENGTH = 52;
    private short[] ks = new short[52];
    private static final byte[][][] tests;

    static {
        byte[][][] byArrayArray = new byte[3][][];
        byte[][] byArrayArray2 = new byte[3][];
        byte[] byArray = new byte[16];
        byArray[1] = 1;
        byArray[3] = 2;
        byArray[5] = 3;
        byArray[7] = 4;
        byArray[9] = 5;
        byArray[11] = 6;
        byArray[13] = 7;
        byArray[15] = 8;
        byArrayArray2[0] = byArray;
        byte[] byArray2 = new byte[8];
        byArray2[3] = 1;
        byArray2[5] = 2;
        byArray2[7] = 3;
        byArrayArray2[1] = byArray2;
        byArrayArray2[2] = new byte[]{17, -5, -19, 43, 1, -104, 109, -27};
        byArrayArray[0] = byArrayArray2;
        byte[][] byArrayArray3 = new byte[3][];
        byte[] byArray3 = new byte[16];
        byArray3[0] = 58;
        byArray3[1] = -104;
        byArray3[2] = 78;
        byArray3[3] = 32;
        byArray3[5] = 25;
        byArray3[6] = 93;
        byArray3[7] = -77;
        byArray3[8] = 46;
        byArray3[9] = -27;
        byArray3[10] = 1;
        byArray3[11] = -56;
        byArray3[12] = -60;
        byArray3[13] = 124;
        byArray3[14] = -22;
        byArray3[15] = 96;
        byArrayArray3[0] = byArray3;
        byArrayArray3[1] = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        byArrayArray3[2] = new byte[]{-105, -68, -40, 32, 7, -128, -38, -122};
        byArrayArray[1] = byArrayArray3;
        byte[][] byArrayArray4 = new byte[3][];
        byte[] byArray4 = new byte[16];
        byArray4[1] = 100;
        byArray4[3] = -56;
        byArray4[4] = 1;
        byArray4[5] = 44;
        byArray4[6] = 1;
        byArray4[7] = -112;
        byArray4[8] = 1;
        byArray4[9] = -12;
        byArray4[10] = 2;
        byArray4[11] = 88;
        byArray4[12] = 2;
        byArray4[13] = -68;
        byArray4[14] = 3;
        byArray4[15] = 32;
        byArrayArray4[0] = byArray4;
        byArrayArray4[1] = new byte[]{5, 50, 10, 100, 20, -56, 25, -6};
        byArrayArray4[2] = new byte[]{101, -66, -121, -25, -94, 83, -118, -19};
        byArrayArray[2] = byArrayArray4;
        tests = byArrayArray;
    }

    private static void debug(String s) {
        err.println("IDEA: " + s);
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
                        linkStatus.checkVersion(IDEA.getLibMajorVersion(), IDEA.getLibMinorVersion());
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
                    IDEA.debug(e.getMessage());
                }
            }
            if (debuglevel > 2) {
                IDEA.debug("Using native library? " + (this.native_lock != null));
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

    public IDEA() {
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
                    IDEA.debug(String.valueOf(error) + " in native_finalize");
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

    protected void engineInitEncrypt(Key key) throws InvalidKeyException, CryptixException {
        this.makeKey(key);
    }

    protected void engineInitDecrypt(Key key) throws InvalidKeyException, CryptixException {
        this.makeKey(key);
        this.invertKey();
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
    private void makeKey(Key key) throws InvalidKeyException, CryptixException {
        byte[] userkey = key.getEncoded();
        if (userkey == null) {
            throw new InvalidKeyException(String.valueOf(this.getAlgorithm()) + ": Null user key");
        }
        if (userkey.length != 16) {
            throw new InvalidKeyException(String.valueOf(this.getAlgorithm()) + ": Invalid user key length");
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
                        IDEA.debug(error + ". Will use 100% Java.");
                    }
                }
            }
        }
        this.ks[0] = (short)((userkey[0] & 0xFF) << 8 | userkey[1] & 0xFF);
        this.ks[1] = (short)((userkey[2] & 0xFF) << 8 | userkey[3] & 0xFF);
        this.ks[2] = (short)((userkey[4] & 0xFF) << 8 | userkey[5] & 0xFF);
        this.ks[3] = (short)((userkey[6] & 0xFF) << 8 | userkey[7] & 0xFF);
        this.ks[4] = (short)((userkey[8] & 0xFF) << 8 | userkey[9] & 0xFF);
        this.ks[5] = (short)((userkey[10] & 0xFF) << 8 | userkey[11] & 0xFF);
        this.ks[6] = (short)((userkey[12] & 0xFF) << 8 | userkey[13] & 0xFF);
        this.ks[7] = (short)((userkey[14] & 0xFF) << 8 | userkey[15] & 0xFF);
        int i = 0;
        int zoff = 0;
        int j = 8;
        while (j < 52) {
            this.ks[++i + 7 + zoff] = (short)(this.ks[(i & 7) + zoff] << 9 | this.ks[(i + 1 & 7) + zoff] >>> 7 & 0x1FF);
            zoff += i & 8;
            i &= 7;
            ++j;
        }
    }

    private void invertKey() {
        if (this.native_lock == null) {
            int j = 4;
            int k = 51;
            short[] temp = new short[52];
            temp[k--] = IDEA.inv(this.ks[3]);
            temp[k--] = -this.ks[2];
            temp[k--] = -this.ks[1];
            temp[k--] = IDEA.inv(this.ks[0]);
            int i = 1;
            while (i < 8) {
                temp[k--] = this.ks[j + 1];
                temp[k--] = this.ks[j];
                temp[k--] = IDEA.inv(this.ks[j + 5]);
                temp[k--] = -this.ks[j + 3];
                temp[k--] = -this.ks[j + 4];
                temp[k--] = IDEA.inv(this.ks[j + 2]);
                ++i;
                j += 6;
            }
            temp[k--] = this.ks[j + 1];
            temp[k--] = this.ks[j];
            temp[k--] = IDEA.inv(this.ks[j + 5]);
            temp[k--] = -this.ks[j + 4];
            temp[k--] = -this.ks[j + 3];
            temp[k--] = IDEA.inv(this.ks[j + 2]);
            System.arraycopy(temp, 0, this.ks, 0, 52);
        }
    }

    private void blockEncrypt(byte[] in, int inOffset, byte[] out, int outOffset) {
        short s2;
        short x1 = (short)((in[inOffset++] & 0xFF) << 8 | in[inOffset++] & 0xFF);
        short x2 = (short)((in[inOffset++] & 0xFF) << 8 | in[inOffset++] & 0xFF);
        short x3 = (short)((in[inOffset++] & 0xFF) << 8 | in[inOffset++] & 0xFF);
        short x4 = (short)((in[inOffset++] & 0xFF) << 8 | in[inOffset] & 0xFF);
        int i = 0;
        int round = 8;
        while (round-- > 0) {
            x1 = IDEA.mul(x1, this.ks[i++]);
            x2 = (short)(x2 + this.ks[i++]);
            x3 = (short)(x3 + this.ks[i++]);
            x4 = IDEA.mul(x4, this.ks[i++]);
            short s3 = x3;
            x3 = IDEA.mul(x1 ^ x3, this.ks[i++]);
            s2 = x2;
            x2 = IDEA.mul(x3 + (x2 ^ x4), this.ks[i++]);
            x3 = (short)(x3 + x2);
            x1 = (short)(x1 ^ x2);
            x4 = (short)(x4 ^ x3);
            x2 = (short)(x2 ^ s3);
            x3 = (short)(x3 ^ s2);
        }
        s2 = IDEA.mul(x1, this.ks[i++]);
        out[outOffset++] = (byte)(s2 >>> 8);
        out[outOffset++] = (byte)s2;
        s2 = (short)(x3 + this.ks[i++]);
        out[outOffset++] = (byte)(s2 >>> 8);
        out[outOffset++] = (byte)s2;
        s2 = (short)(x2 + this.ks[i++]);
        out[outOffset++] = (byte)(s2 >>> 8);
        out[outOffset++] = (byte)s2;
        s2 = IDEA.mul(x4, this.ks[i]);
        out[outOffset++] = (byte)(s2 >>> 8);
        out[outOffset] = (byte)s2;
    }

    private void blockDecrypt(byte[] in, int inOffset, byte[] out, int outOffset) {
        this.blockEncrypt(in, inOffset, out, outOffset);
    }

    private static short mul(int a, int b) {
        b &= 0xFFFF;
        if ((a &= 0xFFFF) != 0) {
            if (b != 0) {
                int p = a * b;
                return (short)(b - a + ((b = p & 0xFFFF) < (a = p >>> 16) ? 1 : 0));
            }
            return (short)(1 - a);
        }
        return (short)(1 - b);
    }

    private static short inv(short xx) {
        int x = xx & 0xFFFF;
        if (x <= 1) {
            return (short)x;
        }
        int t1 = 65537 / x;
        int y = 65537 % x;
        if (y == 1) {
            return (short)(1 - t1);
        }
        int t0 = 1;
        do {
            int q = x / y;
            t0 += q * t1;
            if ((x %= y) == 1) {
                return (short)t0;
            }
            q = y / x;
            t1 += q * t0;
        } while ((y %= x) != 1);
        return (short)(1 - t1);
    }

    public static void main(String[] argv) {
        try {
            IDEA.self_test();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void self_test() throws Throwable {
        Cipher cryptor = Cipher.getInstance("IDEA", "Cryptix");
        int i = 0;
        while (i < tests.length) {
            RawSecretKey userKey = new RawSecretKey("IDEA", tests[i][0]);
            cryptor.initEncrypt(userKey);
            byte[] tmp = cryptor.crypt(tests[i][1]);
            if (!ArrayUtil.areEqual(tests[i][2], tmp)) {
                throw new CryptixException("encrypt #" + i + " failed");
            }
            cryptor.initDecrypt(userKey);
            tmp = cryptor.crypt(tests[i][2]);
            if (!ArrayUtil.areEqual(tests[i][1], tmp)) {
                throw new CryptixException("decrypt #" + i + " failed");
            }
            ++i;
        }
        if (debuglevel > 0) {
            IDEA.debug("Self-test OK");
        }
    }
}

