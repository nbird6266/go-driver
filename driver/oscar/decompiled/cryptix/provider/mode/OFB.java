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

public class OFB
extends FeedbackMode {
    public OFB() {
        super(true, false, "Cryptix");
    }

    public OFB(Cipher cipher) {
        this();
        this.engineSetCipher(cipher);
    }

    public OFB(Cipher cipher, byte[] iv) {
        this(cipher);
        this.setInitializationVector(iv);
    }

    protected int engineBlockSize() {
        return 1;
    }

    protected void engineInitEncrypt(Key newkey) throws KeyException {
        this.cipher.initEncrypt(newkey);
        if (this.ivStart != null) {
            System.arraycopy(this.ivStart, 0, this.ivBlock, 0, this.length);
        }
        this.currentByte = this.length;
    }

    protected void engineInitDecrypt(Key newkey) throws KeyException {
        this.cipher.initEncrypt(newkey);
        if (this.ivStart != null) {
            System.arraycopy(this.ivStart, 0, this.ivBlock, 0, this.length);
        }
        this.currentByte = this.length;
    }

    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        if (this.ivBlock == null) {
            throw new InvalidParameterException("OFB: IV is not set");
        }
        if (this.getState() == 0) {
            throw new CryptixException("OFB: Cipher not initialized");
        }
        if (in == out && outOffset > inOffset && (long)outOffset < (long)inOffset + (long)inLen) {
            byte[] newin = new byte[inLen];
            System.arraycopy(in, inOffset, newin, 0, inLen);
            in = newin;
            inOffset = 0;
        }
        int i = 0;
        while (i < inLen) {
            out[i + outOffset] = this.cryptByte(in[i + inOffset]);
            ++i;
        }
        return inLen;
    }

    private byte cryptByte(byte b) {
        if (this.currentByte >= this.length) {
            this.currentByte = 0;
            this.cipher.update(this.ivBlock, 0, this.length, this.ivBlock, 0);
        }
        b = (byte)(b ^ this.ivBlock[this.currentByte]);
        ++this.currentByte;
        return b;
    }
}

