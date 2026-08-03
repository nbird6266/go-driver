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

public final class DES
extends Cipher
implements SymmetricCipher {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel;
    private static final PrintWriter err;
    private static NativeLink linkStatus;
    private long native_cookie;
    private Object native_lock;
    private static final int ROUNDS = 16;
    private static final int BLOCK_SIZE = 8;
    private static final int KEY_LENGTH = 8;
    private static final int INTERNAL_KEY_LENGTH = 32;
    private static final int[] SKB;
    private int[] sKey = new int[32];
    private static final int[] SP_TRANS;
    private static final String[][] tests;

    static {
        int j;
        int bit;
        debuglevel = Debug.getLevel("DES");
        err = Debug.getOutput();
        linkStatus = new NativeLink("DES", 2, 3);
        SKB = new int[512];
        SP_TRANS = new int[512];
        String cd = "D]PKESYM`UBJ\\@RXA`I[T`HC`LZQ\\PB]TL`[C`JQ@Y`HSXDUIZRAM`EK";
        int count = 0;
        int offset = 0;
        int i = 0;
        while (i < cd.length()) {
            int s = cd.charAt(i) - 64;
            if (s != 32) {
                bit = 1 << count++;
                j = 0;
                while (j < 64) {
                    if ((bit & j) != 0) {
                        int n = offset + j;
                        SKB[n] = SKB[n] | 1 << s;
                    }
                    ++j;
                }
                if (count == 6) {
                    offset += 64;
                    count = 0;
                }
            }
            ++i;
        }
        String spt = "g3H821:80:H03BA0@N1290BAA88::3112aIH8:8282@0@AH0:1W3A8P810@22;22A18^@9H9@129:<8@822`?:@0@8PH2H81A19:G1@03403A0B1;:0@1g192:@919AA0A109:W21492H@0051919811:215011139883942N8::3112A2:31981jM118::A101@I88:1aN0<@030128:X;811`920:;H0310D1033@W980:8A4@804A3803o1A2021B2:@1AH023GA:8:@81@@12092B:098042P@:0:A0HA9>1;289:@1804:40Ph=1:H0I0HP0408024bC9P8@I808A;@0@0PnH0::8:19J@818:@iF0398:8A9H0<13@001@11<8;@82B01P0a2989B:0AY0912889bD0A1@B1A0A0AB033O91182440A9P8@I80n@1I03@1J828212A`A8:12B1@19A9@9@8^B:0@H00<82AB030bB840821Q:8310A302102::A1::20A1;8";
        offset = 0;
        int i2 = 0;
        while (i2 < 32) {
            int k = -1;
            bit = 1 << i2;
            j = 0;
            while (j < 32) {
                int c = spt.charAt(offset >> 1) - 48 >> (offset & 1) * 3 & 7;
                ++offset;
                if (c < 5) {
                    int n = k += c + 1;
                    SP_TRANS[n] = SP_TRANS[n] | bit;
                } else {
                    int param = spt.charAt(offset >> 1) - 48 >> (offset & 1) * 3 & 7;
                    ++offset;
                    if (c == 5) {
                        int n = k += param + 6;
                        SP_TRANS[n] = SP_TRANS[n] | bit;
                    } else if (c == 6) {
                        int n = k += (param << 6) + 1;
                        SP_TRANS[n] = SP_TRANS[n] | bit;
                    } else {
                        k += param << 6;
                        --j;
                    }
                }
                ++j;
            }
            ++i2;
        }
        tests = new String[][]{{"0101010101010101", "95f8a5e5dd31d900", "8000000000000000"}, {"0101010101010101", "dd7f121ca5015619", "4000000000000000"}, {"0101010101010101", "2e8653104f3834ea", "2000000000000000"}};
    }

    private static void debug(String s) {
        err.println("DES: " + s);
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
                        linkStatus.checkVersion(DES.getLibMajorVersion(), DES.getLibMinorVersion());
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
                    DES.debug(e.getMessage());
                }
            }
            if (debuglevel > 2) {
                DES.debug("Using native library? " + (this.native_lock != null));
            }
        }
    }

    private static native int getLibMajorVersion();

    private static native int getLibMinorVersion();

    private native String native_clinit();

    private native String native_init();

    private native String native_ks(long var1, byte[] var3);

    private native int native_crypt(long var1, byte[] var3, int var4, byte[] var5, int var6, boolean var7);

    private native int[] native_crypt3(long var1, int var3, int var4);

    private native String native_finalize();

    public DES() {
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
                    DES.debug(String.valueOf(error) + " in native_finalize");
                }
            }
        }
    }

    public final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public int engineBlockSize() {
        return 8;
    }

    public void engineInitEncrypt(Key key) throws InvalidKeyException {
        this.makeKey(key);
    }

    public void engineInitDecrypt(Key key) throws InvalidKeyException, CryptixException {
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
        } else {
            int i = 0;
            while (i < blockCount) {
                this.des((byte[])in, inOffset, out, outOffset, doEncrypt);
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
    private void makeKey(Key key) throws InvalidKeyException {
        byte[] userkey = key.getEncoded();
        if (userkey == null) {
            throw new InvalidKeyException(String.valueOf(this.getAlgorithm()) + ": Null user key");
        }
        if (userkey.length != 8) {
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
                        DES.debug(error + ". Will use 100% Java.");
                    }
                }
            }
        }
        int i = 0;
        int c = userkey[i++] & 0xFF | (userkey[i++] & 0xFF) << 8 | (userkey[i++] & 0xFF) << 16 | (userkey[i++] & 0xFF) << 24;
        int d = userkey[i++] & 0xFF | (userkey[i++] & 0xFF) << 8 | (userkey[i++] & 0xFF) << 16 | (userkey[i++] & 0xFF) << 24;
        int t = (d >>> 4 ^ c) & 0xF0F0F0F;
        c ^= t;
        d ^= t << 4;
        t = (c << 18 ^ c) & 0xCCCC0000;
        c ^= t ^ t >>> 18;
        t = (d << 18 ^ d) & 0xCCCC0000;
        d ^= t ^ t >>> 18;
        t = (d >>> 1 ^ c) & 0x55555555;
        c ^= t;
        d ^= t << 1;
        t = (c >>> 8 ^ d) & 0xFF00FF;
        d ^= t;
        c ^= t << 8;
        t = (d >>> 1 ^ c) & 0x55555555;
        d ^= t << 1;
        d = (d & 0xFF) << 16 | d & 0xFF00 | (d & 0xFF0000) >>> 16 | ((c ^= t) & 0xF0000000) >>> 4;
        c &= 0xFFFFFFF;
        int j = 0;
        i = 0;
        while (i < 16) {
            if ((32508 >> i & 1) == 1) {
                c = (c >>> 2 | c << 26) & 0xFFFFFFF;
                d = (d >>> 2 | d << 26) & 0xFFFFFFF;
            } else {
                c = (c >>> 1 | c << 27) & 0xFFFFFFF;
                d = (d >>> 1 | d << 27) & 0xFFFFFFF;
            }
            int s = SKB[c & 0x3F] | SKB[0x40 | (c >>> 6 & 3 | c >>> 7 & 0x3C)] | SKB[0x80 | (c >>> 13 & 0xF | c >>> 14 & 0x30)] | SKB[0xC0 | (c >>> 20 & 1 | c >>> 21 & 6 | c >>> 22 & 0x38)];
            t = SKB[0x100 | d & 0x3F] | SKB[0x140 | (d >>> 7 & 3 | d >>> 8 & 0x3C)] | SKB[0x180 | d >>> 15 & 0x3F] | SKB[0x1C0 | (d >>> 21 & 0xF | d >>> 22 & 0x30)];
            this.sKey[j++] = t << 16 | s & 0xFFFF;
            s = s >>> 16 | t & 0xFFFF0000;
            this.sKey[j++] = s << 4 | s >>> 28;
            ++i;
        }
    }

    protected void des(byte[] in, int inOffset, byte[] out, int outOffset, boolean encrypt) {
        int[] lr = new int[]{in[inOffset++] & 0xFF | (in[inOffset++] & 0xFF) << 8 | (in[inOffset++] & 0xFF) << 16 | (in[inOffset++] & 0xFF) << 24, in[inOffset++] & 0xFF | (in[inOffset++] & 0xFF) << 8 | (in[inOffset++] & 0xFF) << 16 | (in[inOffset] & 0xFF) << 24};
        DES.initialPermutation(lr);
        if (encrypt) {
            this.encrypt_base(lr);
        } else {
            this.decrypt_base(lr);
        }
        DES.finalPermutation(lr);
        int R = lr[0];
        int L = lr[1];
        out[outOffset++] = (byte)L;
        out[outOffset++] = (byte)(L >> 8);
        out[outOffset++] = (byte)(L >> 16);
        out[outOffset++] = (byte)(L >> 24);
        out[outOffset++] = (byte)R;
        out[outOffset++] = (byte)(R >> 8);
        out[outOffset++] = (byte)(R >> 16);
        out[outOffset] = (byte)(R >> 24);
    }

    private void encrypt_base(int[] io) {
        int L = io[0];
        int R = io[1];
        int u = R << 1 | R >>> 31;
        R = L << 1 | L >>> 31;
        L = u;
        int i = 0;
        while (i < 32) {
            u = R ^ this.sKey[i++];
            int t = R ^ this.sKey[i++];
            t = t >>> 4 | t << 28;
            L ^= SP_TRANS[0x40 | t & 0x3F] | SP_TRANS[0xC0 | t >>> 8 & 0x3F] | SP_TRANS[0x140 | t >>> 16 & 0x3F] | SP_TRANS[0x1C0 | t >>> 24 & 0x3F] | SP_TRANS[u & 0x3F] | SP_TRANS[0x80 | u >>> 8 & 0x3F] | SP_TRANS[0x100 | u >>> 16 & 0x3F] | SP_TRANS[0x180 | u >>> 24 & 0x3F];
            u = L ^ this.sKey[i++];
            t = L ^ this.sKey[i++];
            t = t >>> 4 | t << 28;
            R ^= SP_TRANS[0x40 | t & 0x3F] | SP_TRANS[0xC0 | t >>> 8 & 0x3F] | SP_TRANS[0x140 | t >>> 16 & 0x3F] | SP_TRANS[0x1C0 | t >>> 24 & 0x3F] | SP_TRANS[u & 0x3F] | SP_TRANS[0x80 | u >>> 8 & 0x3F] | SP_TRANS[0x100 | u >>> 16 & 0x3F] | SP_TRANS[0x180 | u >>> 24 & 0x3F];
        }
        io[0] = R >>> 1 | R << 31;
        io[1] = L >>> 1 | L << 31;
    }

    private void decrypt_base(int[] io) {
        int L = io[0];
        int R = io[1];
        int u = R << 1 | R >>> 31;
        R = L << 1 | L >>> 31;
        L = u;
        int i = 31;
        while (i > 0) {
            int t = R ^ this.sKey[i--];
            u = R ^ this.sKey[i--];
            t = t >>> 4 | t << 28;
            L ^= SP_TRANS[0x40 | t & 0x3F] | SP_TRANS[0xC0 | t >>> 8 & 0x3F] | SP_TRANS[0x140 | t >>> 16 & 0x3F] | SP_TRANS[0x1C0 | t >>> 24 & 0x3F] | SP_TRANS[u & 0x3F] | SP_TRANS[0x80 | u >>> 8 & 0x3F] | SP_TRANS[0x100 | u >>> 16 & 0x3F] | SP_TRANS[0x180 | u >>> 24 & 0x3F];
            t = L ^ this.sKey[i--];
            u = L ^ this.sKey[i--];
            t = t >>> 4 | t << 28;
            R ^= SP_TRANS[0x40 | t & 0x3F] | SP_TRANS[0xC0 | t >>> 8 & 0x3F] | SP_TRANS[0x140 | t >>> 16 & 0x3F] | SP_TRANS[0x1C0 | t >>> 24 & 0x3F] | SP_TRANS[u & 0x3F] | SP_TRANS[0x80 | u >>> 8 & 0x3F] | SP_TRANS[0x100 | u >>> 16 & 0x3F] | SP_TRANS[0x180 | u >>> 24 & 0x3F];
        }
        io[0] = R >>> 1 | R << 31;
        io[1] = L >>> 1 | L << 31;
    }

    private static void initialPermutation(int[] io) {
        int L = io[0];
        int R = io[1];
        int t = (R >>> 4 ^ L) & 0xF0F0F0F;
        L ^= t;
        R ^= t << 4;
        t = (L >>> 16 ^ R) & 0xFFFF;
        R ^= t;
        L ^= t << 16;
        t = (R >>> 2 ^ L) & 0x33333333;
        L ^= t;
        R ^= t << 2;
        t = (L >>> 8 ^ R) & 0xFF00FF;
        R ^= t;
        L ^= t << 8;
        t = (R >>> 1 ^ L) & 0x55555555;
        io[0] = L ^ t;
        io[1] = R ^ t << 1;
    }

    private static void finalPermutation(int[] io) {
        int L = io[1];
        int R = io[0];
        int t = (R >>> 1 ^ L) & 0x55555555;
        L ^= t;
        R ^= t << 1;
        t = (L >>> 8 ^ R) & 0xFF00FF;
        R ^= t;
        L ^= t << 8;
        t = (R >>> 2 ^ L) & 0x33333333;
        L ^= t;
        R ^= t << 2;
        t = (L >>> 16 ^ R) & 0xFFFF;
        R ^= t;
        L ^= t << 16;
        t = (R >>> 4 ^ L) & 0xF0F0F0F;
        io[1] = L ^ t;
        io[0] = R ^ t << 4;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int[] crypt3(int E0, int E1) {
        int t;
        if (this.native_lock != null) {
            Object object = this.native_lock;
            synchronized (object) {
                int[] result = this.native_crypt3(this.native_cookie, E0, E1);
                if (result == null) {
                    throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Error in native code");
                }
                return result;
            }
        }
        int L = 0;
        int R = 0;
        int i = 0;
        while (i < 25) {
            int j = 0;
            while (j < 32) {
                int v = R ^ R >>> 16;
                int u = v & E0;
                u ^= u << 16 ^ R ^ this.sKey[j++];
                t = (v &= E1) ^ v << 16 ^ R ^ this.sKey[j++];
                t = t >>> 4 | t << 28;
                v = (L ^= SP_TRANS[0x40 | t & 0x3F] | SP_TRANS[0xC0 | t >>> 8 & 0x3F] | SP_TRANS[0x140 | t >>> 16 & 0x3F] | SP_TRANS[0x1C0 | t >>> 24 & 0x3F] | SP_TRANS[u & 0x3F] | SP_TRANS[0x80 | u >>> 8 & 0x3F] | SP_TRANS[0x100 | u >>> 16 & 0x3F] | SP_TRANS[0x180 | u >>> 24 & 0x3F]) ^ L >>> 16;
                u = v & E0;
                u ^= u << 16 ^ L ^ this.sKey[j++];
                t = (v &= E1) ^ v << 16 ^ L ^ this.sKey[j++];
                t = t >>> 4 | t << 28;
                R ^= SP_TRANS[0x40 | t & 0x3F] | SP_TRANS[0xC0 | t >>> 8 & 0x3F] | SP_TRANS[0x140 | t >>> 16 & 0x3F] | SP_TRANS[0x1C0 | t >>> 24 & 0x3F] | SP_TRANS[u & 0x3F] | SP_TRANS[0x80 | u >>> 8 & 0x3F] | SP_TRANS[0x100 | u >>> 16 & 0x3F] | SP_TRANS[0x180 | u >>> 24 & 0x3F];
            }
            t = L;
            L = R;
            R = t;
            ++i;
        }
        t = L;
        L = R >>> 1 | R << 31;
        R = t >>> 1 | t << 31;
        t = (R >>> 1 ^ L) & 0x55555555;
        L ^= t;
        R ^= t << 1;
        t = (L >>> 8 ^ R) & 0xFF00FF;
        R ^= t;
        L ^= t << 8;
        t = (R >>> 2 ^ L) & 0x33333333;
        L ^= t;
        R ^= t << 2;
        t = (L >>> 16 ^ R) & 0xFFFF;
        R ^= t;
        L ^= t << 16;
        t = (R >>> 4 ^ L) & 0xF0F0F0F;
        int[] result = new int[]{L ^ t, R ^ t << 4};
        return result;
    }

    public static void main(String[] argv) {
        try {
            DES.self_test();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void self_test() throws Exception {
        Cipher cryptor = Cipher.getInstance("DES", "Cryptix");
        int i = 0;
        while (i < tests.length) {
            RawSecretKey userKey = new RawSecretKey("DES", Hex.fromString(tests[i][0]));
            byte[] pt = Hex.fromString(tests[i][1]);
            byte[] ct = Hex.fromString(tests[i][2]);
            cryptor.initEncrypt(userKey);
            byte[] tmp = cryptor.crypt(pt);
            if (!ArrayUtil.areEqual(ct, tmp)) {
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
            DES.debug("Self-test OK");
        }
    }
}

