/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.mode;

import java.security.InvalidParameterException;
import xjava.security.Cipher;
import xjava.security.FeedbackCipher;
import xjava.security.Mode;
import xjava.security.SymmetricCipher;

abstract class FeedbackMode
extends Mode
implements FeedbackCipher,
SymmetricCipher {
    protected byte[] ivBlock;
    protected byte[] ivStart;
    protected int currentByte;
    protected int length;

    protected FeedbackMode(boolean implBuffering, boolean implPadding, String provider) {
        super(implBuffering, implPadding, provider);
    }

    protected void engineSetCipher(Cipher cipher) {
        super.engineSetCipher(cipher);
        this.length = cipher.blockSize();
        this.ivStart = null;
        this.ivBlock = new byte[this.length];
        this.currentByte = 0;
    }

    public void setInitializationVector(byte[] iv) throws InvalidParameterException {
        if (this.ivStart != null) {
            throw new InvalidParameterException(String.valueOf(this.getMode()) + ": Initialization vector is already set");
        }
        if (iv.length != this.length) {
            throw new InvalidParameterException(String.valueOf(this.getMode()) + ": Initialization vector length = " + iv.length + ", should be " + this.length);
        }
        this.ivStart = (byte[])iv.clone();
        this.ivBlock = (byte[])this.ivStart.clone();
        this.currentByte = this.length;
    }

    public byte[] getInitializationVector() {
        return this.ivStart == null ? null : (byte[])this.ivStart.clone();
    }

    public int getInitializationVectorLength() {
        return this.length;
    }
}

