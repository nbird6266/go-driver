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

public class DES_EDE3
extends Cipher
implements SymmetricCipher {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel = Debug.getLevel("DES-EDE3");
    private static final PrintWriter err = Debug.getOutput();
    private static final int BLOCK_SIZE = 8;
    private static final int KEY_LENGTH = 24;
    private static final int DES_KEY_LENGTH = 8;
    private Cipher des1 = new DES();
    private Cipher des2 = new DES();
    private Cipher des3 = new DES();

    private static void debug(String s) {
        err.println("DES-EDE3: " + s);
    }

    public DES_EDE3() {
        super(false, false, "Cryptix");
    }

    public int engineBlockSize() {
        return 8;
    }

    public void engineInitEncrypt(Key key) throws KeyException {
        Key[] keys = this.splitKey(key);
        this.des1.initEncrypt(keys[0]);
        this.des2.initDecrypt(keys[1]);
        this.des3.initEncrypt(keys[2]);
    }

    protected void engineInitDecrypt(Key key) throws KeyException {
        Key[] keys = this.splitKey(key);
        this.des1.initDecrypt(keys[2]);
        this.des2.initEncrypt(keys[1]);
        this.des3.initDecrypt(keys[0]);
    }

    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        byte[] temp = this.des1.update(in, inOffset, inLen);
        this.des2.update(temp, 0, temp.length, temp, 0);
        return this.des3.update(temp, 0, temp.length, out, outOffset);
    }

    private Key[] splitKey(Key key) throws InvalidKeyException {
        byte[] userkey = key.getEncoded();
        if (userkey == null) {
            throw new InvalidKeyException(String.valueOf(this.getAlgorithm()) + ": Null user key");
        }
        if (userkey.length != 24) {
            throw new InvalidKeyException(String.valueOf(this.getAlgorithm()) + ": Invalid user key length");
        }
        Key[] keys = new RawSecretKey[3];
        int i = 0;
        while (i < 3) {
            keys[i] = new RawSecretKey("DES", userkey, i * 8, 8);
            ++i;
        }
        return keys;
    }
}

