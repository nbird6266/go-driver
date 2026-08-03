/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.mode;

import cryptix.CryptixException;
import cryptix.provider.mode.FeedbackMode;
import cryptix.util.core.ArrayUtil;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyException;
import xjava.security.Cipher;

public class PCBC
extends FeedbackMode {
    private byte[] xorBlock;

    public PCBC() {
        super(false, false, "Cryptix");
    }

    public PCBC(Cipher cipher) {
        this();
        this.engineSetCipher(cipher);
    }

    public PCBC(Cipher cipher, byte[] iv) {
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
        ArrayUtil.clear(this.xorBlock);
    }

    protected void engineInitDecrypt(Key newkey) throws KeyException {
        this.cipher.initDecrypt(newkey);
        if (this.ivStart != null) {
            System.arraycopy(this.ivStart, 0, this.ivBlock, 0, this.length);
        }
        ArrayUtil.clear(this.xorBlock);
    }

    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        if (this.ivBlock == null) {
            throw new InvalidParameterException("PCBC: IV is not set");
        }
        if (inLen <= 0) {
            return 0;
        }
        switch (this.getState()) {
            case 1: {
                int i = 0;
                while (i < this.length) {
                    int n = i;
                    this.ivBlock[n] = (byte)(this.ivBlock[n] ^ in[inOffset + i]);
                    int n2 = i;
                    this.xorBlock[n2] = (byte)(this.xorBlock[n2] ^ this.ivBlock[i]);
                    ++i;
                }
                this.cipher.update(this.xorBlock, 0, this.length, this.ivBlock, 0);
                System.arraycopy(this.ivBlock, 0, out, outOffset, this.length);
                System.arraycopy(in, inOffset, this.xorBlock, 0, this.length);
                break;
            }
            case 2: {
                this.cipher.update(in, inOffset, this.length, this.xorBlock, 0);
                int i = 0;
                while (i < this.length) {
                    out[outOffset + i] = (byte)(this.ivBlock[i] ^ this.xorBlock[i]);
                    this.ivBlock[i] = (byte)(in[inOffset + i] ^ out[outOffset + i]);
                    ++i;
                }
                break;
            }
            default: {
                throw new CryptixException("PCBC: Cipher not initialized");
            }
        }
        return this.length;
    }
}

