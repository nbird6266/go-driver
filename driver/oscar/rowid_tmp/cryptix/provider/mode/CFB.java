/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.mode;

import cryptix.CryptixException;
import cryptix.provider.mode.FeedbackMode;
import cryptix.util.core.Debug;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyException;
import xjava.security.Cipher;

public class CFB
extends FeedbackMode {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel = Debug.getLevel("CFB");
    private static final PrintWriter err = Debug.getOutput();
    private byte[] xorBlock;

    private static void debug(String s) {
        err.println("CFB: " + s);
    }

    public CFB() {
        super(true, false, "Cryptix");
    }

    public CFB(Cipher cipher) {
        this();
        this.engineSetCipher(cipher);
    }

    public CFB(Cipher cipher, byte[] iv) {
        this(cipher);
        this.setInitializationVector(iv);
    }

    protected void engineSetCipher(Cipher cipher) {
        super.engineSetCipher(cipher);
        this.xorBlock = new byte[this.length];
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
            throw new InvalidParameterException("CFB: IV is not set");
        }
        if (in == out && outOffset > inOffset && (long)outOffset < (long)inOffset + (long)inLen) {
            byte[] newin = new byte[inLen];
            System.arraycopy(in, inOffset, newin, 0, inLen);
            in = newin;
            inOffset = 0;
        }
        switch (this.getState()) {
            case 1: {
                int i = 0;
                while (i < inLen) {
                    out[i + outOffset] = this.encryptByte(in[i + inOffset]);
                    ++i;
                }
                break;
            }
            case 2: {
                int i = 0;
                while (i < inLen) {
                    out[i + outOffset] = this.decryptByte(in[i + inOffset]);
                    ++i;
                }
                break;
            }
            default: {
                throw new CryptixException("CFB: Cipher not initialized");
            }
        }
        return inLen;
    }

    private byte encryptByte(byte b) {
        if (this.currentByte >= this.length) {
            this.currentByte = 0;
            if (this.ivStart == null) {
                this.ivStart = new byte[this.length];
                System.arraycopy(this.ivBlock, 0, this.ivStart, 0, this.length);
            }
            this.cipher.update(this.ivBlock, 0, this.length, this.xorBlock, 0);
        }
        this.ivBlock[this.currentByte] = b = (byte)(b ^ this.xorBlock[this.currentByte]);
        ++this.currentByte;
        return b;
    }

    private byte decryptByte(byte b) {
        if (this.currentByte >= this.length) {
            this.currentByte = 0;
            if (this.ivStart == null) {
                this.ivStart = new byte[this.length];
                System.arraycopy(this.ivBlock, 0, this.ivStart, 0, this.length);
            }
            this.cipher.update(this.ivBlock, 0, this.length, this.xorBlock, 0);
        }
        this.ivBlock[this.currentByte] = b;
        b = (byte)(b ^ this.xorBlock[this.currentByte]);
        ++this.currentByte;
        return b;
    }

    protected void next_block() {
        byte[] buf = new byte[this.length];
        System.arraycopy(this.ivBlock, this.currentByte, buf, 0, this.length - this.currentByte);
        System.arraycopy(this.ivBlock, 0, buf, this.length - this.currentByte, this.currentByte);
        this.ivBlock = buf;
        this.currentByte = this.length;
    }
}

