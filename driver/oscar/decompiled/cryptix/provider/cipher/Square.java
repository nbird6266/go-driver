/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.cipher;

import cryptix.CryptixException;
import cryptix.provider.cipher.NativeLink;
import cryptix.provider.key.RawSecretKey;
import cryptix.util.core.ArrayUtil;
import cryptix.util.core.Debug;
import cryptix.util.core.Hex;
import cryptix.util.core.LinkStatus;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import xjava.security.Cipher;
import xjava.security.SymmetricCipher;

public final class Square
extends Cipher
implements SymmetricCipher {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel;
    private static final PrintWriter err;
    private static NativeLink linkStatus;
    private long native_cookie;
    private Object native_lock;
    private static final byte[] SE;
    private static final byte[] SD;
    private static final int[] TE;
    private static final int[] TD;
    private static final int BLOCK_SIZE = 16;
    private static final int R = 8;
    private int[][] sKey = new int[9][4];
    private static final int ROOT = 501;
    private static final int[] OFFSET;
    private static final String[][] tests;

    static {
        int j;
        debuglevel = Debug.getLevel("Square");
        err = Debug.getOutput();
        linkStatus = new NativeLink("Square", 2, 3);
        SE = new byte[256];
        SD = new byte[256];
        TE = new int[256];
        TD = new int[256];
        OFFSET = new int[8];
        byte[] exp = new byte[256];
        byte[] log = new byte[256];
        exp[0] = 1;
        int i = 1;
        while (i < 256) {
            j = exp[i - 1] << 1;
            if ((j & 0x100) != 0) {
                j ^= 0x1F5;
            }
            exp[i] = (byte)j;
            log[j & 0xFF] = (byte)i;
            ++i;
        }
        Square.SE[0] = 0;
        Square.SE[1] = 1;
        i = 2;
        while (i < 256) {
            Square.SE[i] = exp[255 - log[i] & 0xFF];
            ++i;
        }
        int[] trans = new int[]{1, 3, 5, 15, 31, 61, 123, 214};
        i = 0;
        while (i < 256) {
            int v = 177;
            j = 0;
            while (j < 8) {
                int u = SE[i] & trans[j] & 0xFF;
                u ^= u >>> 4;
                u ^= u >>> 2;
                u ^= u >>> 1;
                v ^= (u &= 1) << j;
                ++j;
            }
            Square.SE[i] = (byte)v;
            Square.SD[v] = (byte)i;
            ++i;
        }
        Square.OFFSET[0] = 1;
        i = 1;
        while (i < 8) {
            Square.OFFSET[i] = Square.mul(OFFSET[i - 1], 2);
            int n = i - 1;
            OFFSET[n] = OFFSET[n] << 24;
            ++i;
        }
        OFFSET[7] = OFFSET[7] << 24;
        i = 0;
        while (i < 256) {
            int se = SE[i] & 0xFF;
            int sd = SD[i] & 0xFF;
            Square.TE[i] = SE[i & 3] == 0 ? 0 : Square.mul(se, 2) << 24 | se << 16 | se << 8 | Square.mul(se, 3);
            Square.TD[i] = SD[i & 3] == 0 ? 0 : Square.mul(sd, 14) << 24 | Square.mul(sd, 9) << 16 | Square.mul(sd, 13) << 8 | Square.mul(sd, 11);
            ++i;
        }
        tests = new String[][]{{"000102030405060708090a0b0c0d0e0f", "000102030405060708090a0b0c0d0e0f", "7C3491D94994E70F0EC2E7A5CCB5A14F"}, {"000102030405060708090a0b0c0d0e0f", "000102030405060708090a0b0c0d0e0f000102030405060708090a0b0c0d0e0f", "7C3491D94994E70F0EC2E7A5CCB5A14F7C3491D94994E70F0EC2E7A5CCB5A14F"}};
    }

    private static void debug(String s) {
        err.println("Square: " + s);
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
                        linkStatus.checkVersion(Square.getLibMajorVersion(), Square.getLibMinorVersion());
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
                    Square.debug(e.getMessage());
                }
            }
            if (debuglevel > 2) {
                Square.debug("Using native library? " + (this.native_lock != null));
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

    public Square() {
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
                    Square.debug(String.valueOf(error) + " in native_finalize");
                }
            }
        }
    }

    public final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public int engineBlockSize() {
        return 16;
    }

    protected void engineInitEncrypt(Key key) throws InvalidKeyException {
        this.makeKey(key, true);
    }

    protected void engineInitDecrypt(Key key) throws InvalidKeyException {
        this.makeKey(key, false);
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
        int blockCount = inLen / 16;
        inLen = blockCount * 16;
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
                    inOffset += 16;
                    outOffset += 16;
                    ++i;
                }
            }
        } else if (doEncrypt) {
            int i = 0;
            while (i < blockCount) {
                this.square((byte[])in, inOffset, out, outOffset, TE, SE);
                inOffset += 16;
                outOffset += 16;
                ++i;
            }
        } else {
            int i = 0;
            while (i < blockCount) {
                this.square((byte[])in, inOffset, out, outOffset, TD, SD);
                inOffset += 16;
                outOffset += 16;
                ++i;
            }
        }
        return inLen;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void makeKey(Key key, boolean doEncrypt) throws InvalidKeyException {
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
                        Square.debug(error + ". Will use 100% Java.");
                    }
                }
            }
        }
        int j = 0;
        if (doEncrypt) {
            int i = 0;
            while (i < 4) {
                this.sKey[0][i] = (userkey[j++] & 0xFF) << 24 | (userkey[j++] & 0xFF) << 16 | (userkey[j++] & 0xFF) << 8 | userkey[j++] & 0xFF;
                ++i;
            }
            i = 1;
            while (i < 9) {
                j = i - 1;
                this.sKey[i][0] = this.sKey[j][0] ^ Square.rot32L(this.sKey[j][3], 8) ^ OFFSET[j];
                this.sKey[i][1] = this.sKey[j][1] ^ this.sKey[i][0];
                this.sKey[i][2] = this.sKey[j][2] ^ this.sKey[i][1];
                this.sKey[i][3] = this.sKey[j][3] ^ this.sKey[i][2];
                Square.transform(this.sKey[j], this.sKey[j]);
                ++i;
            }
        } else {
            int[][] tKey = new int[9][4];
            int i = 0;
            while (i < 4) {
                tKey[0][i] = (userkey[j++] & 0xFF) << 24 | (userkey[j++] & 0xFF) << 16 | (userkey[j++] & 0xFF) << 8 | userkey[j++] & 0xFF;
                ++i;
            }
            i = 1;
            while (i < 9) {
                j = i - 1;
                tKey[i][0] = tKey[j][0] ^ Square.rot32L(tKey[j][3], 8) ^ OFFSET[j];
                tKey[i][1] = tKey[j][1] ^ tKey[i][0];
                tKey[i][2] = tKey[j][2] ^ tKey[i][1];
                tKey[i][3] = tKey[j][3] ^ tKey[i][2];
                ++i;
            }
            i = 0;
            while (i < 8) {
                System.arraycopy(tKey[8 - i], 0, this.sKey[i], 0, 4);
                ++i;
            }
            Square.transform(tKey[0], this.sKey[8]);
        }
    }

    private static void transform(int[] in, int[] out) {
        int i = 0;
        while (i < 4) {
            int l3 = in[i];
            int l2 = l3 >>> 8;
            int l1 = l3 >>> 16;
            int l0 = l3 >>> 24;
            int m = ((Square.mul(l0, 2) ^ Square.mul(l1, 3) ^ l2 ^ l3) & 0xFF) << 24;
            m ^= ((l0 ^ Square.mul(l1, 2) ^ Square.mul(l2, 3) ^ l3) & 0xFF) << 16;
            m ^= ((l0 ^ l1 ^ Square.mul(l2, 2) ^ Square.mul(l3, 3)) & 0xFF) << 8;
            out[i] = m ^= (Square.mul(l0, 3) ^ l1 ^ l2 ^ Square.mul(l3, 2)) & 0xFF;
            ++i;
        }
    }

    private static int rot32L(int x, int s) {
        return x << s | x >>> 32 - s;
    }

    private static int rot32R(int x, int s) {
        return x >>> s | x << 32 - s;
    }

    private static final int mul(int a, int b) {
        if (a == 0) {
            return 0;
        }
        a &= 0xFF;
        b &= 0xFF;
        int p = 0;
        while (b != 0) {
            if ((b & 1) != 0) {
                p ^= a;
            }
            if ((a <<= 1) > 255) {
                a ^= 0x1F5;
            }
            b >>>= 1;
        }
        return p & 0xFF;
    }

    private void square(byte[] in, int off, byte[] out, int outOff, int[] T, byte[] S) {
        int a = (in[off++] & 0xFF) << 24 | (in[off++] & 0xFF) << 16 | (in[off++] & 0xFF) << 8 | in[off++] & 0xFF;
        int b = (in[off++] & 0xFF) << 24 | (in[off++] & 0xFF) << 16 | (in[off++] & 0xFF) << 8 | in[off++] & 0xFF;
        int c = (in[off++] & 0xFF) << 24 | (in[off++] & 0xFF) << 16 | (in[off++] & 0xFF) << 8 | in[off++] & 0xFF;
        int d = (in[off++] & 0xFF) << 24 | (in[off++] & 0xFF) << 16 | (in[off++] & 0xFF) << 8 | in[off++] & 0xFF;
        a ^= this.sKey[0][0];
        b ^= this.sKey[0][1];
        c ^= this.sKey[0][2];
        d ^= this.sKey[0][3];
        int i = 1;
        while (i < 8) {
            int aa = T[a >>> 24 & 0xFF] ^ Square.rot32R(T[b >>> 24 & 0xFF], 8) ^ Square.rot32R(T[c >>> 24 & 0xFF], 16) ^ Square.rot32R(T[d >>> 24 & 0xFF], 24) ^ this.sKey[i][0];
            int bb = T[a >>> 16 & 0xFF] ^ Square.rot32R(T[b >>> 16 & 0xFF], 8) ^ Square.rot32R(T[c >>> 16 & 0xFF], 16) ^ Square.rot32R(T[d >>> 16 & 0xFF], 24) ^ this.sKey[i][1];
            int cc = T[a >>> 8 & 0xFF] ^ Square.rot32R(T[b >>> 8 & 0xFF], 8) ^ Square.rot32R(T[c >>> 8 & 0xFF], 16) ^ Square.rot32R(T[d >>> 8 & 0xFF], 24) ^ this.sKey[i][2];
            int dd = T[a & 0xFF] ^ Square.rot32R(T[b & 0xFF], 8) ^ Square.rot32R(T[c & 0xFF], 16) ^ Square.rot32R(T[d & 0xFF], 24) ^ this.sKey[i][3];
            a = aa;
            b = bb;
            c = cc;
            d = dd;
            ++i;
        }
        i = 0;
        int j = 24;
        while (i < 4) {
            int k = (S[a >>> j & 0xFF] & 0xFF) << 24 | (S[b >>> j & 0xFF] & 0xFF) << 16 | (S[c >>> j & 0xFF] & 0xFF) << 8 | S[d >>> j & 0xFF] & 0xFF;
            out[outOff++] = (byte)((k ^= this.sKey[8][i]) >>> 24 & 0xFF);
            out[outOff++] = (byte)(k >>> 16 & 0xFF);
            out[outOff++] = (byte)(k >>> 8 & 0xFF);
            out[outOff++] = (byte)(k & 0xFF);
            ++i;
            j -= 8;
        }
    }

    public static final void main(String[] args) {
        try {
            Square.self_test();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void self_test() throws Exception {
        Cipher cryptor = Cipher.getInstance("Square", "Cryptix");
        int i = 0;
        while (i < tests.length) {
            RawSecretKey userKey = new RawSecretKey("Square", Hex.fromString(tests[i][0]));
            byte[] pt = Hex.fromString(tests[i][1]);
            byte[] ct = Hex.fromString(tests[i][2]);
            cryptor.initEncrypt(userKey);
            byte[] tmp = cryptor.crypt(pt);
            if (!ArrayUtil.areEqual(ct, tmp)) {
                System.out.println("     input: " + Hex.toString(pt));
                System.out.println("  computed: " + Hex.toString(tmp));
                System.out.println(" certified: " + Hex.toString(ct));
                throw new CryptixException("encrypt #" + i + " failed");
            }
            cryptor.initDecrypt(userKey);
            tmp = cryptor.crypt(ct);
            if (!ArrayUtil.areEqual(pt, tmp)) {
                throw new CryptixException("decrypt #" + i + " failed");
            }
            ++i;
        }
        if (debuglevel > 0) {
            Square.debug("Self-test OK");
        }
    }
}

