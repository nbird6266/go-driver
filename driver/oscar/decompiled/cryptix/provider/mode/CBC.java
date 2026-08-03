/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.mode;

import cryptix.CryptixException;
import cryptix.provider.mode.FeedbackMode;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyException;
import xjava.security.Cipher;

public class CBC
extends FeedbackMode {
    protected byte[] xorBlock;

    public CBC() {
        super(false, false, "Cryptix");
    }

    public CBC(Cipher cipher) {
        this();
        this.engineSetCipher(cipher);
    }

    public CBC(Cipher cipher, byte[] iv) {
        this(cipher);
        this.setInitializationVector(iv);
    }

    protected void engineSetCipher(Cipher cipher) {
        super.engineSetCipher(cipher);
        this.xorBlock = new byte[this.length];
    }

    protected int engineBlockSize() {
        return this.length;
    }

    protected void engineInitEncrypt(Key newkey) throws KeyException {
        this.cipher.initEncrypt(newkey);
        if (this.ivStart != null) {
            System.arraycopy(this.ivStart, 0, this.ivBlock, 0, this.length);
        }
    }

    protected void engineInitDecrypt(Key newkey) throws KeyException {
        this.cipher.initDecrypt(newkey);
        if (this.ivStart != null) {
            System.arraycopy(this.ivStart, 0, this.ivBlock, 0, this.length);
        }
    }

    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        if (this.ivBlock == null) {
            throw new InvalidParameterException("CBC: IV is not set");
        }
        switch (this.getState()) {
            case 1: {
                int i = 0;
                while (i < inLen) {
                    int n = i++;
                    this.ivBlock[n] = (byte)(this.ivBlock[n] ^ in[inOffset++]);
                }
                this.cipher.update(this.ivBlock, 0, inLen, this.ivBlock, 0);
                System.arraycopy(this.ivBlock, 0, out, outOffset, inLen);
                break;
            }
            case 2: {
                byte[] ivTmp = new byte[inLen];
                System.arraycopy(in, inOffset, ivTmp, 0, inLen);
                this.cipher.update(in, inOffset, inLen, this.xorBlock, 0);
                int i = 0;
                while (i < inLen) {
                    out[i + outOffset] = (byte)(this.xorBlock[i] ^ this.ivBlock[i]);
                    ++i;
                }
                System.arraycopy(ivTmp, 0, this.ivBlock, 0, inLen);
                ivTmp = null;
                break;
            }
            default: {
                throw new CryptixException("CBC: Cipher not initialized");
            }
        }
        return inLen;
    }
}

