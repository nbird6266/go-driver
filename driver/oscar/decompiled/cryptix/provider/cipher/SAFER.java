/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.cipher;

import cryptix.CryptixException;
import cryptix.provider.cipher.NativeLink;
import cryptix.util.core.Debug;
import cryptix.util.core.LinkStatus;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyException;
import java.security.Security;
import xjava.security.Cipher;
import xjava.security.InvalidParameterTypeException;
import xjava.security.NoSuchParameterException;
import xjava.security.SymmetricCipher;

public final class SAFER
extends Cipher
implements SymmetricCipher {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel = Debug.getLevel("SAFER");
    private static final PrintWriter err = Debug.getOutput();
    private static NativeLink linkStatus = new NativeLink("SAFER", 2, 3);
    private long native_cookie;
    private Object native_lock;
    public static final int SK128_VARIANT = 0;
    public static final int SK64_VARIANT = 1;
    public static final int K128_VARIANT = 2;
    public static final int K64_VARIANT = 3;
    private static final int K64_DEFAULT_NOF_ROUNDS = 6;
    private static final int K128_DEFAULT_NOF_ROUNDS = 10;
    private static final int SK64_DEFAULT_NOF_ROUNDS = 8;
    private static final int SK128_DEFAULT_NOF_ROUNDS = 10;
    private static final int MAX_NOF_ROUNDS = 13;
    private static final int BLOCK_SIZE = 8;
    private static final int KEY_LENGTH = 217;
    private static final int TAB_LEN = 256;
    private int[] sKey = new int[217];
    private int rounds = 10;
    private int variant = 0;
    private static final int[] EXP = new int[256];
    private static final int[] LOG = new int[256];

    static {
        int exp = 1;
        int i = 0;
        while (i < 256) {
            SAFER.EXP[i] = exp & 0xFF;
            SAFER.LOG[SAFER.EXP[i]] = i;
            exp = exp * 45 % 257;
            ++i;
        }
    }

    private static void debug(String s) {
        err.println("SAFER: " + s);
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
                        linkStatus.checkVersion(SAFER.getLibMajorVersion(), SAFER.getLibMinorVersion());
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
                    SAFER.debug(e.getMessage());
                }
            }
            if (debuglevel > 2) {
                SAFER.debug("Using native library? " + (this.native_lock != null));
            }
        }
    }

    private static native int getLibMajorVersion();

    private static native int getLibMinorVersion();

    private native String native_clinit();

    private native String native_init();

    private native String native_ks(long var1, byte[] var3, byte[] var4, int var5, boolean var6);

    private native int native_crypt(long var1, byte[] var3, int var4, byte[] var5, int var6, boolean var7);

    private native String native_finalize();

    public SAFER() {
        super(false, false, "Cryptix");
        String ps2;
        try {
            ps2 = Security.getAlgorithmProperty("SAFER", "variant");
            if (ps2 != null) {
                this.setVariant(ps2);
            }
        }
        catch (Exception ps2) {
            // empty catch block
        }
        switch (this.variant) {
            case 0: {
                this.rounds = 10;
                break;
            }
            case 1: {
                this.rounds = 8;
                break;
            }
            case 2: {
                this.rounds = 10;
                break;
            }
            case 3: {
                this.rounds = 6;
            }
        }
        try {
            ps2 = Security.getAlgorithmProperty("SAFER", "rounds");
            if (ps2 != null) {
                this.setRounds(Integer.parseInt(ps2));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
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
                    SAFER.debug(String.valueOf(error) + " in native_finalize");
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

    public void engineInitEncrypt(Key key) throws KeyException {
        this.makeKey(key);
    }

    public void engineInitDecrypt(Key key) throws KeyException {
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
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected void engineSetParameter(String param, Object value) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        if (param.equalsIgnoreCase("rounds")) {
            if (!(value instanceof Integer)) throw new InvalidParameterTypeException("rounds.SAFER");
            this.setRounds((Integer)value);
            return;
        } else {
            if (!param.equalsIgnoreCase("variant")) throw new NoSuchParameterException(String.valueOf(param) + ".SAFER");
            if (!(value instanceof String)) throw new InvalidParameterTypeException("variant.SAFER");
            this.setVariant((String)value);
        }
    }

    protected Object engineGetParameter(String param) throws NoSuchParameterException, InvalidParameterException {
        if (param.equalsIgnoreCase("rounds")) {
            return new Integer(this.rounds);
        }
        if (param.equalsIgnoreCase("variant")) {
            return this.getVariant();
        }
        throw new NoSuchParameterException(String.valueOf(param) + ".SAFER");
    }

    public void setRounds(int rounds) {
        if (this.getState() != 0) {
            throw new IllegalStateException("Cipher not in UNINITIALIZED state");
        }
        if (rounds <= 0 || rounds > 13) {
            throw new InvalidParameterException();
        }
        this.rounds = rounds;
    }

    public int getRounds() {
        return this.rounds;
    }

    public void setVariant(String ps) {
        if (this.getState() != 0) {
            throw new IllegalStateException("Cipher not in UNINITIALIZED state");
        }
        if (ps.equalsIgnoreCase("SK128") || ps.equalsIgnoreCase("SK-128")) {
            this.variant = 0;
        } else if (ps.equalsIgnoreCase("SK64") || ps.equalsIgnoreCase("SK-64")) {
            this.variant = 1;
        } else if (ps.equalsIgnoreCase("K128") || ps.equalsIgnoreCase("K-128")) {
            this.variant = 2;
        } else if (ps.equalsIgnoreCase("K64") || ps.equalsIgnoreCase("K-64")) {
            this.variant = 3;
        } else {
            throw new InvalidParameterException();
        }
    }

    public String getVariant() {
        switch (this.variant) {
            case 0: {
                return "SK-128";
            }
            case 1: {
                return "SK-64";
            }
            case 2: {
                return "K-128";
            }
            case 3: {
                return "K-64";
            }
        }
        throw new InternalError("variant = " + this.variant);
    }

    private void blockEncrypt(byte[] in, int inOff, byte[] out, int outOff) {
        int round;
        int k = 0;
        if (13 < (round = this.sKey[k++])) {
            round = 13;
        }
        int a = in[inOff++];
        int b = in[inOff++];
        int c = in[inOff++];
        int d = in[inOff++];
        int e = in[inOff++];
        int f = in[inOff++];
        int g = in[inOff++];
        int h = in[inOff++];
        int i = 0;
        while (i < round) {
            a ^= this.sKey[k++];
            b += this.sKey[k++];
            c += this.sKey[k++];
            d ^= this.sKey[k++];
            e ^= this.sKey[k++];
            f += this.sKey[k++];
            g += this.sKey[k++];
            h ^= this.sKey[k++];
            a = EXP[a & 0xFF] + this.sKey[k++];
            b = LOG[b & 0xFF] ^ this.sKey[k++];
            c = LOG[c & 0xFF] ^ this.sKey[k++];
            d = EXP[d & 0xFF] + this.sKey[k++];
            e = EXP[e & 0xFF] + this.sKey[k++];
            f = LOG[f & 0xFF] ^ this.sKey[k++];
            g = LOG[g & 0xFF] ^ this.sKey[k++];
            h = EXP[h & 0xFF] + this.sKey[k++];
            b += a;
            a += b;
            d += c;
            c += d;
            f += e;
            h += g;
            g += h;
            c += a;
            a += c;
            g += (e += f);
            e += g;
            d += b;
            h += f;
            f += h;
            e += a;
            a += e;
            f += (b += d);
            g += c;
            h += d;
            int t = b += f;
            b = e;
            e = c += g;
            c = t;
            t = d += h;
            d = f;
            f = g;
            g = t;
            ++i;
        }
        out[outOff++] = (byte)(a ^ this.sKey[k++]);
        out[outOff++] = (byte)(b + this.sKey[k++]);
        out[outOff++] = (byte)(c + this.sKey[k++]);
        out[outOff++] = (byte)(d ^ this.sKey[k++]);
        out[outOff++] = (byte)(e ^ this.sKey[k++]);
        out[outOff++] = (byte)(f + this.sKey[k++]);
        out[outOff++] = (byte)(g + this.sKey[k++]);
        out[outOff++] = (byte)(h ^ this.sKey[k++]);
    }

    private void blockDecrypt(byte[] in, int inOff, byte[] out, int outOff) {
        int round = this.sKey[0];
        if (13 < round) {
            round = 13;
        }
        int a = in[inOff++];
        int b = in[inOff++];
        int c = in[inOff++];
        int d = in[inOff++];
        int e = in[inOff++];
        int f = in[inOff++];
        int g = in[inOff++];
        int h = in[inOff++];
        int k = 8 * (1 + 2 * round);
        h ^= this.sKey[k];
        g -= this.sKey[--k];
        f -= this.sKey[--k];
        e ^= this.sKey[--k];
        d ^= this.sKey[--k];
        c -= this.sKey[--k];
        b -= this.sKey[--k];
        a ^= this.sKey[--k];
        int i = 0;
        while (i < round) {
            int t = e;
            e = b;
            b = c;
            c = t;
            t = f;
            f = d;
            d = g;
            g = t;
            a -= e;
            e -= a;
            b -= f;
            f -= b;
            c -= g;
            g -= c;
            d -= h;
            h -= d;
            a -= c;
            c -= a;
            e -= g;
            g -= e;
            b -= d;
            d -= b;
            f -= h;
            h -= f;
            a -= b;
            b -= a;
            c -= d;
            d -= c;
            e -= f;
            f -= e;
            g -= h;
            h -= g;
            h -= this.sKey[--k];
            g ^= this.sKey[--k];
            f ^= this.sKey[--k];
            e -= this.sKey[--k];
            d -= this.sKey[--k];
            c ^= this.sKey[--k];
            b ^= this.sKey[--k];
            a -= this.sKey[--k];
            h = LOG[h & 0xFF] ^ this.sKey[--k];
            g = EXP[g & 0xFF] - this.sKey[--k];
            f = EXP[f & 0xFF] - this.sKey[--k];
            e = LOG[e & 0xFF] ^ this.sKey[--k];
            d = LOG[d & 0xFF] ^ this.sKey[--k];
            c = EXP[c & 0xFF] - this.sKey[--k];
            b = EXP[b & 0xFF] - this.sKey[--k];
            a = LOG[a & 0xFF] ^ this.sKey[--k];
            ++i;
        }
        out[outOff++] = (byte)a;
        out[outOff++] = (byte)b;
        out[outOff++] = (byte)c;
        out[outOff++] = (byte)d;
        out[outOff++] = (byte)e;
        out[outOff++] = (byte)f;
        out[outOff++] = (byte)g;
        out[outOff++] = (byte)h;
    }

    private synchronized void makeKey(Key key) throws KeyException {
        byte[] keyBytes = key.getEncoded();
        if (keyBytes == null) {
            throw new KeyException("Invalid SAFER key");
        }
        byte[] userKey = new byte[16];
        int keyLen = keyBytes.length;
        int len = 16;
        int userKeyLenSoFar = 0;
        while (len >= keyLen) {
            System.arraycopy(keyBytes, 0, userKey, userKeyLenSoFar, keyLen);
            len -= keyLen;
            userKeyLenSoFar += keyLen;
        }
        System.arraycopy(keyBytes, 0, userKey, userKeyLenSoFar, len);
        byte[] key1 = new byte[8];
        byte[] key2 = new byte[8];
        System.arraycopy(userKey, 0, key1, 0, 8);
        System.arraycopy(userKey, 8, key2, 0, 8);
        this.Safer_Expand_Userkey(key1, key2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void Safer_Expand_Userkey(byte[] userkey_1, byte[] userkey_2) {
        if (this.native_lock != null) {
            Object object = this.native_lock;
            synchronized (object) {
                try {
                    linkStatus.check(this.native_ks(this.native_cookie, userkey_1, userkey_2, this.rounds, this.isStrong()));
                    return;
                }
                catch (Error error) {
                    this.native_finalize();
                    this.native_lock = null;
                    if (debuglevel > 0) {
                        SAFER.debug(error + ". Will use 100% Java.");
                    }
                }
            }
        }
        byte[] ka = new byte[9];
        byte[] kb = new byte[9];
        int k = 0;
        this.sKey[k++] = (byte)this.rounds;
        int j = 0;
        while (j < 8) {
            ka[j] = (byte)(userkey_1[j] << 5 | (userkey_1[j] & 0xFF) >>> 3);
            ka[8] = (byte)(ka[8] ^ ka[j]);
            this.sKey[k++] = userkey_2[j];
            kb[j] = userkey_2[j];
            kb[8] = (byte)(kb[8] ^ kb[j]);
            ++j;
        }
        int i = 1;
        while (i <= this.rounds) {
            int j2 = 0;
            while (j2 < 9) {
                ka[j2] = (byte)(ka[j2] << 6 | (ka[j2] & 0xFF) >>> 2);
                kb[j2] = (byte)(kb[j2] << 6 | (kb[j2] & 0xFF) >>> 2);
                ++j2;
            }
            j2 = 0;
            while (j2 < 8) {
                this.sKey[k++] = this.isStrong() ? ka[(j2 + 2 * i - 1) % 9] + EXP[EXP[18 * i + j2 + 1]] & 0xFF : ka[j2] + EXP[EXP[18 * i + j2 + 1]] & 0xFF;
                ++j2;
            }
            j2 = 0;
            while (j2 < 8) {
                this.sKey[k++] = this.isStrong() ? kb[(j2 + 2 * i) % 9] + EXP[EXP[18 * i + j2 + 10]] & 0xFF : kb[j2] + EXP[EXP[18 * i + j2 + 10]] & 0xFF;
                ++j2;
            }
            ++i;
        }
    }

    private boolean isStrong() {
        return this.variant < 2;
    }
}

