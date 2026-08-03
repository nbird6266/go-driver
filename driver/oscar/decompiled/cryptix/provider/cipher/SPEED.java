/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.cipher;

import cryptix.CryptixException;
import cryptix.provider.cipher.NativeLink;
import cryptix.provider.key.RawSecretKey;
import cryptix.util.core.Debug;
import cryptix.util.core.Hex;
import cryptix.util.core.LinkStatus;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.Security;
import xjava.security.Cipher;
import xjava.security.InvalidParameterTypeException;
import xjava.security.NoSuchParameterException;
import xjava.security.SymmetricCipher;

public final class SPEED
extends Cipher
implements SymmetricCipher {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel = Debug.getLevel("SPEED");
    private static final PrintWriter err = Debug.getOutput();
    private static NativeLink linkStatus = new NativeLink("SPEED", 2, 3);
    private long native_cookie;
    private Object native_lock;
    private static final int MIN_NOF_ROUNDS = 32;
    private static final int MIN_USER_KEY_LENGTH = 6;
    private static final int MAX_USER_KEY_LENGTH = 32;
    private int key_length;
    private int rounds;
    private int block_size;
    private int key_bits;
    private int data_bits;
    private int s0;
    private int s1;
    private int s2;
    private int f_wd_len;
    private int h_wd_len;
    private int f_wd_mask;
    private int h_wd_mask;
    private int v_shift;
    private int kb_bits;
    private int[] round_key;
    private int key_len_dbyte;
    private int[] kb;

    private static void debug(String s) {
        err.println("SPEED: " + s);
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
                        linkStatus.checkVersion(SPEED.getLibMajorVersion(), SPEED.getLibMinorVersion());
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
                    SPEED.debug(e.getMessage());
                }
            }
            if (debuglevel > 2) {
                SPEED.debug("Using native library? " + (this.native_lock != null));
            }
        }
    }

    private static native int getLibMajorVersion();

    private static native int getLibMinorVersion();

    private native String native_clinit();

    private native String native_init();

    private native String native_ks(long var1, byte[] var3);

    private native int native_crypt(long var1, byte[] var3, int var4, byte[] var5, int var6, boolean var7, int var8, int var9);

    private native String native_finalize();

    public SPEED() {
        block7: {
            String ps;
            block6: {
                super(false, false, "Cryptix");
                this.key_length = 16;
                this.rounds = 64;
                this.block_size = 8;
                this.link();
                try {
                    ps = Security.getAlgorithmProperty("SPEED", "rounds");
                    if (ps != null) {
                        this.setRounds(Integer.parseInt(ps));
                    }
                }
                catch (Exception e) {
                    if (debuglevel <= 0) break block6;
                    SPEED.debug("Could not set number of rounds");
                }
            }
            try {
                ps = Security.getAlgorithmProperty("SPEED", "blockSize");
                if (ps != null) {
                    this.setBlockSize(Integer.parseInt(ps));
                }
            }
            catch (Exception e) {
                if (debuglevel <= 0) break block7;
                SPEED.debug("Could not set block size");
            }
        }
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
                    SPEED.debug(String.valueOf(error) + " in native_finalize");
                }
            }
        }
    }

    public final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    protected int engineBlockSize() {
        return this.block_size;
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
        int BLOCK_SIZE = this.block_size;
        if (inLen < 0) {
            throw new IllegalArgumentException("inLen < 0");
        }
        int blockCount = inLen / BLOCK_SIZE;
        inLen = blockCount * BLOCK_SIZE;
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
                    if (this.native_crypt(this.native_cookie, (byte[])in, inOffset, out, outOffset, doEncrypt, this.rounds, BLOCK_SIZE) == 0) {
                        throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Error in native code");
                    }
                    inOffset += BLOCK_SIZE;
                    outOffset += BLOCK_SIZE;
                    ++i;
                }
            }
        } else if (doEncrypt) {
            int i = 0;
            while (i < blockCount) {
                this.blockEncrypt((byte[])in, inOffset, out, outOffset);
                inOffset += BLOCK_SIZE;
                outOffset += BLOCK_SIZE;
                ++i;
            }
        } else {
            int i = 0;
            while (i < blockCount) {
                this.blockDecrypt((byte[])in, inOffset, out, outOffset);
                inOffset += BLOCK_SIZE;
                outOffset += BLOCK_SIZE;
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
            if (!(value instanceof Integer)) throw new InvalidParameterTypeException("rounds.SPEED");
            this.setRounds((Integer)value);
            return;
        } else {
            if (!param.equalsIgnoreCase("blockSize")) throw new NoSuchParameterException(String.valueOf(param) + ".SPEED");
            if (!(value instanceof Integer)) throw new InvalidParameterTypeException("blockSize.SPEED");
            this.setBlockSize((Integer)value);
        }
    }

    protected Object engineGetParameter(String param) throws NoSuchParameterException, InvalidParameterException {
        if (param.equalsIgnoreCase("rounds")) {
            return new Integer(this.rounds);
        }
        if (param.equalsIgnoreCase("blockSize")) {
            return new Integer(this.block_size);
        }
        throw new NoSuchParameterException(String.valueOf(param) + ".SPEED");
    }

    public void setRounds(int rounds) {
        if (this.getState() != 0) {
            throw new IllegalStateException(String.valueOf(this.getAlgorithm()) + ": Cipher not in UNINITIALIZED state");
        }
        if (rounds < 32 || rounds % 4 != 0) {
            throw new IllegalArgumentException(String.valueOf(this.getAlgorithm()) + ": Invalid number of rounds");
        }
        this.rounds = rounds;
    }

    public int getRounds() {
        return this.rounds;
    }

    public void setBlockSize(int blocksize) {
        if (this.getState() != 0) {
            throw new IllegalStateException(String.valueOf(this.getAlgorithm()) + ": Cipher not in UNINITIALIZED state");
        }
        if (blocksize != 8 && blocksize != 16 && blocksize != 32) {
            throw new IllegalArgumentException(String.valueOf(this.getAlgorithm()) + ": Invalid block size");
        }
        this.block_size = blocksize;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void makeKey(Key key) throws InvalidKeyException {
        byte[] userkey = key.getEncoded();
        if (userkey == null) {
            throw new InvalidKeyException(String.valueOf(this.getAlgorithm()) + ": Null user key");
        }
        int len = userkey.length;
        if (len < 6 || len > 32) {
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
                        SPEED.debug(error + ". Will use 100% Java.");
                    }
                }
            }
        }
        this.set_constants(userkey.length);
        this.kb = new int[this.kb_bits];
        this.round_key = new int[this.rounds];
        int i = 0;
        while (i < this.key_len_dbyte) {
            this.kb[i] = userkey[2 * i] | userkey[2 * i + 1] << 8;
            ++i;
        }
        i = this.key_len_dbyte;
        while (i < this.kb_bits) {
            int t = this.s2 & this.s1 ^ this.s1 & this.s0 ^ this.s0 & this.s2;
            t = t << 5 | t >>> 11;
            t += this.s2 + this.kb[i % this.key_len_dbyte];
            this.s2 = this.s1;
            this.s1 = this.s0;
            this.s0 = this.kb[i] = (t &= 0xFFFF);
            ++i;
        }
        if (debuglevel >= 5) {
            SPEED.debug("kb_bits=" + this.kb_bits + ", kb.length=" + this.kb.length + ", round_key.length=" + this.round_key.length);
        }
        switch (this.data_bits) {
            case 256: {
                i = 0;
                while (i < this.kb_bits / 2) {
                    this.round_key[i] = this.kb[2 * i] | this.kb[2 * i + 1] << 16;
                    ++i;
                }
                break;
            }
            case 128: {
                i = 0;
                while (i < this.kb_bits) {
                    this.round_key[i] = this.kb[i];
                    ++i;
                }
                break;
            }
            case 64: {
                i = 0;
                while (i < this.kb_bits) {
                    this.round_key[2 * i] = this.kb[i] & 0xFF;
                    this.round_key[2 * i + 1] = this.kb[i] >>> 8 & 0xFF;
                    ++i;
                }
                break;
            }
            default: {
                throw new CryptixException("SPEED: " + this.data_bits + " illegal in key_schedule?");
            }
        }
    }

    private void set_constants(int key_length) {
        this.key_length = key_length;
        this.key_bits = key_length * 8;
        this.key_len_dbyte = key_length / 2;
        this.set_sqrt_15(this.key_bits);
        this.data_bits = this.block_size * 8;
        this.f_wd_len = this.data_bits / 8;
        this.h_wd_len = this.f_wd_len / 2;
        switch (this.data_bits) {
            case 256: {
                this.f_wd_mask = -1;
                this.h_wd_mask = 65535;
                this.v_shift = 11;
                this.kb_bits = 2 * this.rounds;
                break;
            }
            case 128: {
                this.f_wd_mask = 65535;
                this.h_wd_mask = 255;
                this.v_shift = 4;
                this.kb_bits = this.rounds;
                break;
            }
            case 64: {
                this.f_wd_mask = 255;
                this.h_wd_mask = 15;
                this.v_shift = 1;
                this.kb_bits = this.rounds / 2;
                break;
            }
            default: {
                throw new CryptixException("SPEED: " + this.data_bits + " is bad data size (not 64/128/256)");
            }
        }
    }

    private void set_sqrt_15(int size) {
        switch (size) {
            case 48: {
                this.s0 = 57211;
                this.s1 = 54825;
                this.s2 = 59867;
                return;
            }
            case 64: {
                this.s0 = 13871;
                this.s1 = 23808;
                this.s2 = 61967;
                return;
            }
            case 80: {
                this.s0 = 50129;
                this.s1 = 8146;
                this.s2 = 22683;
                return;
            }
            case 96: {
                this.s0 = 17170;
                this.s1 = 37355;
                this.s2 = 29070;
                return;
            }
            case 112: {
                this.s0 = 48938;
                this.s1 = 7805;
                this.s2 = 45655;
                return;
            }
            case 128: {
                this.s0 = 30630;
                this.s1 = 5716;
                this.s2 = 27434;
                return;
            }
            case 144: {
                this.s0 = 3483;
                this.s1 = 43475;
                this.s2 = 26255;
                return;
            }
            case 160: {
                this.s0 = 6590;
                this.s1 = 63573;
                this.s2 = 28056;
                return;
            }
            case 176: {
                this.s0 = 557;
                this.s1 = 58594;
                this.s2 = 53271;
                return;
            }
            case 192: {
                this.s0 = 59951;
                this.s1 = 30066;
                this.s2 = 50101;
                return;
            }
            case 208: {
                this.s0 = 4230;
                this.s1 = 18444;
                this.s2 = 15014;
                return;
            }
            case 224: {
                this.s0 = 40096;
                this.s1 = 39159;
                this.s2 = 53476;
                return;
            }
            case 240: {
                this.s0 = 9532;
                this.s1 = 51457;
                this.s2 = 22003;
                return;
            }
            case 256: {
                this.s0 = 39924;
                this.s1 = 63065;
                this.s2 = 55148;
                return;
            }
        }
        throw new CryptixException("SPEED: " + size + " is bad key length (not 48 .. 256 % 16)");
    }

    void dump() {
        if (this.data_bits == 0) {
            err.println("no data set yet");
            return;
        }
        err.println("KEY SCHEDULE");
        err.println(" data_bits " + this.data_bits);
        err.println(" kb_bits " + this.kb_bits);
        err.println(" kb.length " + this.kb.length);
        err.println(" f_wd_mask " + Hex.intToString(this.f_wd_mask));
        err.println(" h_wd_mask " + Hex.intToString(this.h_wd_mask));
        err.println(" v_shift " + this.v_shift);
        err.println(" double byte buffer");
        int i = 0;
        while (i < this.key_len_dbyte) {
            err.print(" " + Hex.intToString(this.kb[i]));
            ++i;
        }
        err.println();
        switch (this.data_bits) {
            case 256: {
                i = 0;
                while (i < this.kb_bits / 2) {
                    err.print(" " + Hex.intToString(this.round_key[i]));
                    ++i;
                }
                break;
            }
            case 128: {
                i = 0;
                while (i < this.kb_bits) {
                    err.print(" " + Hex.shortToString(this.round_key[i]));
                    ++i;
                }
                break;
            }
            case 64: {
                i = 0;
                while (i < this.kb_bits * 2) {
                    err.print(" " + Hex.byteToString(this.round_key[i]));
                    ++i;
                }
                break;
            }
            default: {
                throw new CryptixException("SPEED: data_bits=" + this.data_bits + " illegal in key_schedule?");
            }
        }
        err.println();
    }

    private void to_internal(byte[] in, int offset, int[] buf) {
        switch (this.data_bits) {
            case 256: {
                int i = 0;
                while (i < 8) {
                    buf[i] = in[offset + 4 * i] & 0xFF | in[offset + 4 * i + 1] << 8 & 0xFF00 | in[offset + 4 * i + 2] << 16 & 0xFF0000 | in[offset + 4 * i + 3] << 24 & 0xFF000000;
                    ++i;
                }
                break;
            }
            case 128: {
                int i = 0;
                while (i < 8) {
                    buf[i] = in[offset + 2 * i] & 0xFF | in[offset + 2 * i + 1] << 8 & 0xFF00;
                    ++i;
                }
                break;
            }
            case 64: {
                int i = 0;
                while (i < 8) {
                    buf[i] = in[offset + i] & 0xFF;
                    ++i;
                }
                break;
            }
            default: {
                throw new CryptixException("SPEED: " + this.data_bits + " illegal in key_schedule?");
            }
        }
    }

    protected void blockEncrypt(byte[] in, int in_offset, byte[] out, int out_offset) {
        int[] big_in = new int[8];
        int[] big_out = new int[8];
        this.to_internal(in, in_offset, big_in);
        this.encrypt(big_in, big_out);
        this.from_internal(big_out, out, out_offset);
    }

    private void from_internal(int[] buf, byte[] out, int offset) {
        switch (this.data_bits) {
            case 256: {
                int i = 0;
                while (i < 8) {
                    out[4 * i + offset] = (byte)(buf[i] & 0xFF);
                    out[4 * i + 1 + offset] = (byte)(buf[i] >>> 8 & 0xFF);
                    out[4 * i + 2 + offset] = (byte)(buf[i] >>> 16 & 0xFF);
                    out[4 * i + 3 + offset] = (byte)(buf[i] >>> 24 & 0xFF);
                    ++i;
                }
                break;
            }
            case 128: {
                int i = 0;
                while (i < 8) {
                    out[2 * i + offset] = (byte)(buf[i] & 0xFF);
                    out[2 * i + 1 + offset] = (byte)(buf[i] >>> 8 & 0xFF);
                    ++i;
                }
                break;
            }
            case 64: {
                int i = 0;
                while (i < 8) {
                    out[i + offset] = (byte)(buf[i] & 0xFF);
                    ++i;
                }
                break;
            }
            default: {
                throw new CryptixException("SPEED: data_bits=" + this.data_bits + " illegal in key_schedule?");
            }
        }
    }

    protected void blockDecrypt(byte[] in, int in_offset, byte[] out, int out_offset) {
        int[] big_in = new int[8];
        int[] big_out = new int[8];
        this.to_internal(in, in_offset, big_in);
        this.decrypt(big_in, big_out);
        this.from_internal(big_out, out, out_offset);
    }

    private void encrypt(int[] in, int[] out) {
        int rot2;
        int rot1;
        int vv;
        int temp;
        int t0 = in[0];
        int t1 = in[1];
        int t2 = in[2];
        int t3 = in[3];
        int t4 = in[4];
        int t5 = in[5];
        int t6 = in[6];
        int t7 = in[7];
        int k = 0;
        int quarter_rounds = this.rounds / 4;
        int i = 0;
        while (i < quarter_rounds) {
            temp = t6 & t3 ^ t5 & t1 ^ t4 & t2 ^ t1 & t0 ^ t0;
            vv = ((temp >>> this.h_wd_len) + temp & this.h_wd_mask) >>> this.v_shift;
            rot1 = (t7 &= this.f_wd_mask) >>> this.h_wd_len - 1 | t7 << this.f_wd_len - (this.h_wd_len - 1);
            rot2 = (temp &= this.f_wd_mask) >>> vv | temp << this.f_wd_len - vv;
            temp = rot1 + rot2 + this.round_key[k++];
            t7 = t6;
            t6 = t5;
            t5 = t4;
            t4 = t3;
            t3 = t2;
            t2 = t1;
            t1 = t0;
            t0 = temp & this.f_wd_mask;
            ++i;
        }
        i = 0;
        while (i < quarter_rounds) {
            temp = t6 & t4 & t0 ^ t4 & t3 & t0 ^ t5 & t2 ^ t4 & t3 ^ t4 & t1 ^ t3 & t0 ^ t1;
            vv = ((temp >>> this.h_wd_len) + temp & this.h_wd_mask) >>> this.v_shift;
            rot1 = (t7 &= this.f_wd_mask) >>> this.h_wd_len - 1 | t7 << this.f_wd_len - (this.h_wd_len - 1);
            rot2 = (temp &= this.f_wd_mask) >>> vv | temp << this.f_wd_len - vv;
            temp = rot1 + rot2 + this.round_key[k++];
            t7 = t6;
            t6 = t5;
            t5 = t4;
            t4 = t3;
            t3 = t2;
            t2 = t1;
            t1 = t0;
            t0 = temp & this.f_wd_mask;
            ++i;
        }
        i = 0;
        while (i < quarter_rounds) {
            temp = t5 & t4 & t0 ^ t6 & t4 ^ t5 & t2 ^ t3 & t0 ^ t1 & t0 ^ t3;
            vv = ((temp >>> this.h_wd_len) + temp & this.h_wd_mask) >>> this.v_shift;
            rot1 = (t7 &= this.f_wd_mask) >>> this.h_wd_len - 1 | t7 << this.f_wd_len - (this.h_wd_len - 1);
            rot2 = (temp &= this.f_wd_mask) >>> vv | temp << this.f_wd_len - vv;
            temp = rot1 + rot2 + this.round_key[k++];
            t7 = t6;
            t6 = t5;
            t5 = t4;
            t4 = t3;
            t3 = t2;
            t2 = t1;
            t1 = t0;
            t0 = temp & this.f_wd_mask;
            ++i;
        }
        i = 0;
        while (i < quarter_rounds) {
            temp = t6 & t4 & t2 & t0 ^ t6 & t5 ^ t4 & t3 ^ t3 & t2 ^ t1 & t0 ^ t2;
            vv = ((temp >>> this.h_wd_len) + temp & this.h_wd_mask) >>> this.v_shift;
            rot1 = (t7 &= this.f_wd_mask) >>> this.h_wd_len - 1 | t7 << this.f_wd_len - (this.h_wd_len - 1);
            rot2 = (temp &= this.f_wd_mask) >>> vv | temp << this.f_wd_len - vv;
            temp = rot1 + rot2 + this.round_key[k++];
            t7 = t6;
            t6 = t5;
            t5 = t4;
            t4 = t3;
            t3 = t2;
            t2 = t1;
            t1 = t0;
            t0 = temp & this.f_wd_mask;
            ++i;
        }
        out[0] = t0;
        out[1] = t1;
        out[2] = t2;
        out[3] = t3;
        out[4] = t4;
        out[5] = t5;
        out[6] = t6;
        out[7] = t7;
    }

    private void decrypt(int[] in, int[] out) {
        int rot2;
        int vv;
        int temp;
        int new7;
        int t0 = in[0] & 0xFFFFFFFF;
        int t1 = in[1] & 0xFFFFFFFF;
        int t2 = in[2] & 0xFFFFFFFF;
        int t3 = in[3] & 0xFFFFFFFF;
        int t4 = in[4] & 0xFFFFFFFF;
        int t5 = in[5] & 0xFFFFFFFF;
        int t6 = in[6] & 0xFFFFFFFF;
        int t7 = in[7] & 0xFFFFFFFF;
        int k = this.rounds - 1;
        int quarter_rounds = this.rounds / 4;
        int i = 0;
        while (i < quarter_rounds) {
            new7 = t0;
            t0 = t1;
            t1 = t2;
            t2 = t3;
            t3 = t4;
            t4 = t5;
            t5 = t6;
            t6 = t7;
            temp = t6 & t4 & t2 & t0 ^ t6 & t5 ^ t4 & t3 ^ t3 & t2 ^ t1 & t0 ^ t2;
            vv = ((temp >>> this.h_wd_len) + temp & this.h_wd_mask) >>> this.v_shift;
            rot2 = (temp &= this.f_wd_mask) >>> vv | temp << this.f_wd_len - vv;
            new7 -= rot2 + this.round_key[k--];
            t7 = (new7 &= this.f_wd_mask) << this.h_wd_len - 1 | new7 >>> this.f_wd_len - (this.h_wd_len - 1);
            ++i;
        }
        i = 0;
        while (i < quarter_rounds) {
            new7 = t0;
            t0 = t1;
            t1 = t2;
            t2 = t3;
            t3 = t4;
            t4 = t5;
            t5 = t6;
            t6 = t7;
            temp = t5 & t4 & t0 ^ t6 & t4 ^ t5 & t2 ^ t3 & t0 ^ t1 & t0 ^ t3;
            vv = ((temp >>> this.h_wd_len) + temp & this.h_wd_mask) >>> this.v_shift;
            rot2 = (temp &= this.f_wd_mask) >>> vv | temp << this.f_wd_len - vv;
            new7 -= rot2 + this.round_key[k--];
            t7 = (new7 &= this.f_wd_mask) << this.h_wd_len - 1 | new7 >>> this.f_wd_len - (this.h_wd_len - 1);
            ++i;
        }
        i = 0;
        while (i < quarter_rounds) {
            new7 = t0;
            t0 = t1;
            t1 = t2;
            t2 = t3;
            t3 = t4;
            t4 = t5;
            t5 = t6;
            t6 = t7;
            temp = t6 & t4 & t0 ^ t4 & t3 & t0 ^ t5 & t2 ^ t4 & t3 ^ t4 & t1 ^ t3 & t0 ^ t1;
            vv = ((temp >>> this.h_wd_len) + temp & this.h_wd_mask) >>> this.v_shift;
            rot2 = (temp &= this.f_wd_mask) >>> vv | temp << this.f_wd_len - vv;
            new7 -= rot2 + this.round_key[k--];
            t7 = (new7 &= this.f_wd_mask) << this.h_wd_len - 1 | new7 >>> this.f_wd_len - (this.h_wd_len - 1);
            ++i;
        }
        i = 0;
        while (i < quarter_rounds) {
            new7 = t0;
            t0 = t1;
            t1 = t2;
            t2 = t3;
            t3 = t4;
            t4 = t5;
            t5 = t6;
            t6 = t7;
            temp = t6 & t3 ^ t5 & t1 ^ t4 & t2 ^ t1 & t0 ^ t0;
            vv = ((temp >>> this.h_wd_len) + temp & this.h_wd_mask) >>> this.v_shift;
            rot2 = (temp &= this.f_wd_mask) >>> vv | temp << this.f_wd_len - vv;
            new7 -= rot2 + this.round_key[k--];
            t7 = (new7 &= this.f_wd_mask) << this.h_wd_len - 1 | new7 >>> this.f_wd_len - (this.h_wd_len - 1);
            ++i;
        }
        out[0] = t0;
        out[1] = t1;
        out[2] = t2;
        out[3] = t3;
        out[4] = t4;
        out[5] = t5;
        out[6] = t6;
        out[7] = t7;
    }

    public static final void main(String[] argv) {
        try {
            SPEED.self_test(new PrintWriter(System.err), argv);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void self_test(PrintWriter out, String[] argv) throws Exception {
        out.println("Note: hex strings are printed in conventional order, not the order");
        out.println("      in the SPEED paper.");
        out.println();
        SPEED.test(out, 64, "0000000000000000", "0000000000000000", "2E008019BC26856D");
        SPEED.test(out, 128, "00000000000000000000000000000000", "00000000000000000000000000000000", "A44FBF29EDF6CBF8D7A2DFD57163B909");
        SPEED.test(out, 128, "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "6C13E4B9C3171571AB54D816915BC4E8");
        SPEED.test(out, 48, "504F4E4D4C4B4A494847464544434241", "1F1E1D1C1B1A191817161514131211100F0E0D0C0B0A09080706050403020100", "90C5981EF6A3D21BC178CACDAD6BF39B2E51CDB70A6EE875A73BF5ED883E3692");
        SPEED.test(out, 256, "0000000000000000000000000000000000000000000000000000000000000000", "0000000000000000000000000000000000000000000000000000000000000000", "6CD44D2B49BC6AA7E95FD1C4AF713A2C0AFA1701308D56298CDF27A02EB09BF5");
        SPEED.test(out, 256, "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "C8F3E864263FAF24222E38227BEBC022CF4A9A0ECE89FB81CA1B9BA3BA93D0C5");
        SPEED.test(out, 256, "605F5E5D5C5B5A595857565554535251504F4E4D4C4B4A494847464544434241", "1F1E1D1C1B1A191817161514131211100F0E0D0C0B0A09080706050403020100", "3DE16CFA9A626847434E1574693FEC1B3FAA558A296B61D708B131CCBA311068");
    }

    private static void test(PrintWriter out, int rounds, String keyStr, String plainStr, String cipherStr) throws Exception {
        byte[] keyBytes = Hex.fromReversedString(keyStr);
        byte[] plain = Hex.fromReversedString(plainStr);
        byte[] cipher = Hex.fromReversedString(cipherStr);
        SPEED speed = new SPEED();
        speed.setBlockSize(plain.length);
        speed.setRounds(rounds);
        RawSecretKey key = new RawSecretKey("SPEED", keyBytes);
        speed.initEncrypt(key);
        byte[] encP = speed.crypt(plain);
        out.println("    key:" + Hex.toString(keyBytes));
        out.println("  plain:" + Hex.toString(plain));
        String a = Hex.toString(encP);
        out.println("    enc:" + a);
        String b = Hex.toString(cipher);
        if (a.equals(b)) {
            out.print("encryption good; ");
        } else {
            out.println("   calc:" + b);
            out.println(" ********* SPEED ENCRYPTION FAILED ********* ");
            speed.dump();
        }
        speed.initDecrypt(key);
        byte[] decC = speed.crypt(encP);
        a = Hex.toString(decC);
        b = Hex.toString(plain);
        if (a.equals(b)) {
            out.println("decryption good");
        } else {
            out.println();
            out.println("    enc:" + Hex.toString(encP));
            out.println("    dec:" + a);
            out.println("   calc:" + b);
            out.println(" ********* SPEED DECRYPTION FAILED ********* ");
            speed.dump();
        }
    }
}

