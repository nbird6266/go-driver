/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.cipher;

import cryptix.provider.cipher.DES;
import cryptix.provider.key.RawSecretKey;
import cryptix.util.core.Debug;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyException;
import xjava.security.Cipher;
import xjava.security.SymmetricCipher;

public class DES2X
extends Cipher
implements SymmetricCipher {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel = Debug.getLevel("DESX");
    private static final PrintWriter err = Debug.getOutput();
    private static final int BLOCK_SIZE = 8;
    private static final int KEY_LENGTH = 32;
    private static final int SINGLE_KEY_LENGTH = 8;
    private Cipher des = new DES();
    private byte[] XORkey1 = null;
    private byte[] XORkey2 = null;
    private byte[] XORkey3 = null;

    private static void debug(String s) {
        err.println("DESX: " + s);
    }

    public DES2X() {
        super(false, false, "Cryptix");
    }

    public int engineBlockSize() {
        return 8;
    }

    public void engineInitEncrypt(Key key) throws KeyException {
        byte[][] keys = this.splitKey(key);
        RawSecretKey DESkey = new RawSecretKey("DES", keys[0]);
        this.des.initEncrypt(DESkey);
        this.XORkey1 = keys[1];
        this.XORkey2 = keys[2];
        this.XORkey3 = keys[3];
    }

    protected void engineInitDecrypt(Key key) throws KeyException {
        byte[][] keys = this.splitKey(key);
        RawSecretKey DESkey = new RawSecretKey("DES", keys[0]);
        this.des.initDecrypt(DESkey);
        this.XORkey1 = keys[3];
        this.XORkey2 = keys[2];
        this.XORkey3 = keys[1];
    }

    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        byte[] temp = new byte[inLen];
        int i = 0;
        while (i < inLen) {
            temp[i] = (byte)(in[i + inOffset] ^ this.XORkey1[i % 8]);
            ++i;
        }
        this.des.update(temp, 0, inLen, temp, 0);
        i = 0;
        while (i < inLen) {
            int n = i;
            temp[n] = (byte)(temp[n] ^ this.XORkey2[i % 8]);
            ++i;
        }
        this.des.update(temp, 0, inLen, temp, 0);
        i = 0;
        while (i < inLen) {
            out[i + outOffset] = (byte)(temp[i] ^ this.XORkey3[i % 8]);
            ++i;
        }
        return inLen;
    }

    private byte[][] splitKey(Key key) throws InvalidKeyException {
        byte[] userkey = key.getEncoded();
        if (userkey == null) {
            throw new InvalidKeyException(String.valueOf(this.getAlgorithm()) + ": Null user key");
        }
        if (userkey.length != 32) {
            throw new InvalidKeyException(String.valueOf(this.getAlgorithm()) + ": Invalid user key length");
        }
        byte[][] keys = new byte[4][8];
        int i = 0;
        while (i < 4) {
            System.arraycopy(userkey, i * 8, keys[i], 0, 8);
            ++i;
        }
        return keys;
    }
}

