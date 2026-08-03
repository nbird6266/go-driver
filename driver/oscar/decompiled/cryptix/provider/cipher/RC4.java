/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.cipher;

import cryptix.CryptixException;
import cryptix.provider.cipher.NativeLink;
import cryptix.util.core.Debug;
import cryptix.util.core.LinkStatus;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import xjava.security.Cipher;
import xjava.security.SymmetricCipher;

public final class RC4
extends Cipher
implements SymmetricCipher {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel = Debug.getLevel("RC4");
    private static final PrintWriter err = Debug.getOutput();
    private static NativeLink linkStatus = new NativeLink("RC4", 2, 3);
    private long native_cookie;
    private Object native_lock;
    private int[] sBox = new int[256];
    private int x;
    private int y;
    private static final int BLOCK_SIZE = 1;

    private static void debug(String s) {
        err.println("RC4: " + s);
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
                        linkStatus.checkVersion(RC4.getLibMajorVersion(), RC4.getLibMinorVersion());
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
                    RC4.debug(e.getMessage());
                }
            }
            if (debuglevel > 2) {
                RC4.debug("Using native library? " + (this.native_lock != null));
            }
        }
    }

    private static native int getLibMajorVersion();

    private static native int getLibMinorVersion();

    private native String native_clinit();

    private native String native_init();

    private native String native_ks(long var1, byte[] var3);

    private native int native_crypt(long var1, byte[] var3, int var4, int var5, byte[] var6, int var7);

    private native String native_finalize();

    public RC4() {
        super(false, false, "Cryptix");
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
                    RC4.debug(String.valueOf(error) + " in native_finalize");
                }
            }
        }
    }

    public final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public int engineBlockSize() {
        return 1;
    }

    public void engineInitEncrypt(Key key) throws InvalidKeyException {
        this.makeKey(key);
    }

    public void engineInitDecrypt(Key key) throws InvalidKeyException {
        this.makeKey(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        boolean doEncrypt;
        if (inLen < 0) {
            throw new IllegalArgumentException("inLen < 0");
        }
        boolean bl = doEncrypt = this.getState() == 1;
        if (in == out && (outOffset >= inOffset && (long)outOffset < (long)inOffset + (long)inLen || inOffset >= outOffset && (long)inOffset < (long)outOffset + (long)inLen)) {
            byte[] newin = new byte[inLen];
            System.arraycopy(in, inOffset, newin, 0, inLen);
            in = newin;
            inOffset = 0;
        }
        if (this.native_lock != null) {
            Object object = this.native_lock;
            synchronized (object) {
                if (inOffset < 0 || (long)inOffset + (long)inLen > (long)in.length || outOffset < 0 || (long)outOffset + (long)inLen > (long)out.length) {
                    throw new ArrayIndexOutOfBoundsException(String.valueOf(this.getAlgorithm()) + ": Arguments to native_crypt would cause a buffer overflow");
                }
                if (this.native_crypt(this.native_cookie, in, inOffset, inLen, out, outOffset) == 0) {
                    throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Error in native code");
                }
            }
        } else {
            this.rc4(in, inOffset, inLen, out, outOffset);
        }
        return inLen;
    }

    private void rc4(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        int i = 0;
        while (i < inLen) {
            this.x = this.x + 1 & 0xFF;
            this.y = this.sBox[this.x] + this.y & 0xFF;
            int t = this.sBox[this.x];
            this.sBox[this.x] = this.sBox[this.y];
            this.sBox[this.y] = t;
            int xorIndex = this.sBox[this.x] + this.sBox[this.y] & 0xFF;
            out[outOffset++] = (byte)(in[inOffset++] ^ this.sBox[xorIndex]);
            ++i;
        }
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
        if (len == 0) {
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
                        RC4.debug(error + ". Will use 100% Java.");
                    }
                }
            }
        }
        this.y = 0;
        this.x = 0;
        int i = 0;
        while (i < 256) {
            this.sBox[i] = i;
            ++i;
        }
        int i1 = 0;
        int i2 = 0;
        int i3 = 0;
        while (i3 < 256) {
            i2 = (userkey[i1] & 0xFF) + this.sBox[i3] + i2 & 0xFF;
            int t = this.sBox[i3];
            this.sBox[i3] = this.sBox[i2];
            this.sBox[i2] = t;
            i1 = (i1 + 1) % len;
            ++i3;
        }
    }
}

