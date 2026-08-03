/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import xjava.security.IJCE;
import xjava.security.IJCE_Traceable;
import xjava.security.IllegalBlockSizeException;
import xjava.security.InvalidParameterTypeException;
import xjava.security.NoSuchParameterException;
import xjava.security.Padding;
import xjava.security.Parameterized;

public abstract class PaddingScheme
extends IJCE_Traceable
implements Parameterized,
Padding {
    private String algorithm;
    protected int blockSize;

    protected PaddingScheme(String algorithm) {
        super("PaddingScheme");
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        this.algorithm = algorithm;
    }

    public static PaddingScheme getInstance(String algorithm) throws NoSuchAlgorithmException {
        return (PaddingScheme)IJCE.getImplementation(algorithm, "PaddingScheme");
    }

    public static PaddingScheme getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        return (PaddingScheme)IJCE.getImplementation(algorithm, provider, "PaddingScheme");
    }

    public static String[] getAlgorithms(Provider provider) {
        return IJCE.getAlgorithms(provider, "PaddingScheme");
    }

    public static String[] getAlgorithms() {
        return IJCE.getAlgorithms("PaddingScheme");
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public final int getBlockSize() {
        return this.blockSize;
    }

    public final int pad(byte[] in, int offset, int length) {
        if (offset < 0 || length < 0) {
            throw new ArrayIndexOutOfBoundsException("offset < 0 || length < 0");
        }
        int size = this.blockSize;
        int skip = length - length % size;
        if ((long)offset + (long)skip + (long)size > (long)in.length) {
            throw new ArrayIndexOutOfBoundsException("(long)offset + length + padLength(length) > in.length");
        }
        offset += skip;
        length -= skip;
        if (this.tracing) {
            this.traceMethod("enginePad(<" + in + ">, " + offset + ", " + length + ")");
        }
        int result = this.enginePad(in, offset, length);
        if (this.tracing) {
            this.traceResult(result);
        }
        return result;
    }

    public final int padLength(int length) {
        return this.blockSize - length % this.blockSize;
    }

    public final int unpad(byte[] in, int offset, int length) {
        if (length == 0) {
            return 0;
        }
        if (offset < 0 || length < 0 || (long)offset + (long)length > (long)in.length) {
            throw new ArrayIndexOutOfBoundsException("offset < 0 || length < 0 || (long)offset + length > in.length");
        }
        if (this.tracing) {
            this.traceMethod("engineUnpad(<" + in + ">, " + offset + ", " + length + ")");
        }
        int result = this.engineUnpad(in, offset, length);
        if (this.tracing) {
            this.traceResult(result);
        }
        return result;
    }

    public final String paddingScheme() {
        return this.algorithm;
    }

    public void setParameter(String param, Object value) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        if (param == null) {
            throw new NullPointerException("param == null");
        }
        this.engineSetParameter(param, value);
    }

    public Object getParameter(String param) throws NoSuchParameterException, InvalidParameterException {
        if (param == null) {
            throw new NullPointerException("param == null");
        }
        return this.engineGetParameter(param);
    }

    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }

    public String toString() {
        return "PaddingScheme [" + this.getAlgorithm() + "]";
    }

    protected void engineSetBlockSize(int size) {
        if (size < 1 || !this.engineIsValidBlockSize(size)) {
            throw new IllegalBlockSizeException(String.valueOf(this.getAlgorithm()) + ": " + size + " is not a valid block size");
        }
        this.blockSize = size;
    }

    protected abstract int enginePad(byte[] var1, int var2, int var3);

    protected abstract int engineUnpad(byte[] var1, int var2, int var3);

    protected boolean engineIsValidBlockSize(int size) {
        return true;
    }

    protected void engineSetParameter(String param, Object value) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        throw new NoSuchParameterException(String.valueOf(this.getAlgorithm()) + ": " + param);
    }

    protected Object engineGetParameter(String param) throws NoSuchParameterException, InvalidParameterException {
        throw new NoSuchParameterException(String.valueOf(this.getAlgorithm()) + ": " + param);
    }
}

