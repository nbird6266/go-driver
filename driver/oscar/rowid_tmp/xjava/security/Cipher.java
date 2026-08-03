/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import xjava.security.IJCE;
import xjava.security.IJCE_Traceable;
import xjava.security.IllegalBlockSizeException;
import xjava.security.InvalidParameterTypeException;
import xjava.security.Mode;
import xjava.security.NoSuchParameterException;
import xjava.security.PaddingScheme;
import xjava.security.Parameterized;

public abstract class Cipher
extends IJCE_Traceable
implements Parameterized {
    private static final boolean DEBUG = true;
    private static int debuglevel = IJCE.getDebugLevel("Cipher");
    private static PrintWriter err = IJCE.getDebugOutput();
    public static final int UNINITIALIZED = 0;
    public static final int ENCRYPT = 1;
    public static final int DECRYPT = 2;
    private boolean implBuffering;
    private byte[] buffer;
    private int buffered;
    private int inputSize;
    private int outputSize;
    private String provider;
    private String cipherName;
    private String modeName;
    private String paddingName;
    private PaddingScheme padding;
    private int state;

    private static void debug(String s) {
        err.println("Cipher: " + s);
    }

    private static String dump(byte[] b) {
        if (b == null) {
            return "null";
        }
        return b.toString();
    }

    protected Cipher() {
        super("Cipher");
    }

    protected Cipher(boolean implBuffering, boolean implPadding, String provider) {
        super("Cipher");
        if (implPadding) {
            throw new IllegalArgumentException("IJCE does not support ciphers for which implPadding == true");
        }
        this.implBuffering = implBuffering;
        this.provider = provider;
    }

    protected Cipher(boolean implBuffering, String provider, String algorithm) {
        super("Cipher");
        this.implBuffering = implBuffering;
        this.provider = provider;
        this.parseAlgorithm(algorithm);
    }

    private void parseAlgorithm(String algorithm) {
        int p = algorithm.indexOf(47);
        if (p == -1) {
            this.cipherName = algorithm;
        } else {
            this.cipherName = algorithm.substring(0, p);
            int q = algorithm.indexOf(47, p + 1);
            if (q == -1) {
                this.modeName = algorithm.substring(p + 1);
            } else {
                this.modeName = algorithm.substring(p + 1, q);
                this.paddingName = algorithm.substring(q + 1);
            }
        }
    }

    private void setNames(String cipherName, String modeName, String paddingName, String provider) {
        if (this.cipherName == null) {
            this.cipherName = cipherName;
        }
        if (this.modeName == null) {
            this.modeName = modeName;
        }
        if (this.paddingName == null) {
            this.paddingName = paddingName;
        }
        if (this.provider == null) {
            this.provider = provider;
        }
    }

    protected final PaddingScheme getPaddingScheme() {
        return this.padding;
    }

    public static Cipher getInstance(String algorithm) throws NoSuchAlgorithmException {
        try {
            return Cipher.getInstance(algorithm, null);
        }
        catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(e.getMessage());
        }
    }

    public static Cipher getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        String cipherName = algorithm;
        String modeName = "ECB";
        String paddingName = "NONE";
        int p = algorithm.indexOf(47);
        if (p != -1) {
            cipherName = algorithm.substring(0, p);
            int q = algorithm.indexOf(47, p + 1);
            if (q == -1) {
                modeName = algorithm.substring(p + 1);
            } else {
                modeName = algorithm.substring(p + 1, q);
                paddingName = algorithm.substring(q + 1);
            }
        }
        return Cipher.getInstance(cipherName, modeName, paddingName, provider);
    }

    private static Cipher getInstance(String cipherName, String modeName, String paddingName, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Cipher result;
        PaddingScheme padding;
        Cipher nested;
        block10: {
            if (debuglevel >= 3) {
                Cipher.debug("Entered getInstance(\"" + cipherName + "\", \"" + modeName + "\", \"" + paddingName + "\", \"" + provider + "\")");
            }
            cipherName = IJCE.getStandardName(cipherName, "Cipher");
            modeName = IJCE.getStandardName(modeName, "Mode");
            paddingName = IJCE.getStandardName(paddingName, "PaddingScheme");
            nested = null;
            padding = null;
            try {
                result = (Cipher)IJCE.getImplementation(String.valueOf(cipherName) + "/" + modeName + "/" + paddingName, provider, "Cipher");
            }
            catch (NoSuchAlgorithmException e) {
                if (modeName.equals("ECB")) {
                    result = (Cipher)IJCE.getImplementation(cipherName, provider, "Cipher");
                } else {
                    try {
                        result = (Cipher)IJCE.getImplementation(String.valueOf(cipherName) + "/" + modeName, provider, "Cipher");
                    }
                    catch (NoSuchAlgorithmException e2) {
                        nested = (Cipher)IJCE.getImplementation(cipherName, provider, "Cipher");
                        nested.setNames(cipherName, "ECB", "NONE", provider);
                        result = (Cipher)IJCE.getImplementation(modeName, provider, "Mode");
                    }
                }
                if (paddingName.equals("NONE")) break block10;
                padding = (PaddingScheme)IJCE.getImplementation(paddingName, provider, "PaddingScheme");
            }
        }
        result.setNames(cipherName, modeName, paddingName, provider);
        if (nested != null) {
            ((Mode)result).engineSetCipher(nested);
        }
        if (padding != null) {
            result.engineSetPaddingScheme(padding);
        }
        if (debuglevel >= 3) {
            Cipher.debug("Created cipher [1]: " + result);
        }
        return result;
    }

    public static Cipher getInstance(Cipher cipher, Mode mode, PaddingScheme padding) {
        Cipher result;
        if (cipher == null) {
            throw new NullPointerException("cipher == null");
        }
        String cipherName = cipher.getAlgorithm();
        String modeName = mode == null ? "ECB" : mode.getAlgorithm();
        String paddingName = padding == null ? "NONE" : padding.getAlgorithm();
        String provider = cipher.getProvider();
        Cipher nested = null;
        if (mode == null) {
            result = cipher;
        } else {
            nested = cipher;
            result = mode;
        }
        result.setNames(cipherName, modeName, paddingName, provider);
        if (nested != null) {
            ((Mode)result).engineSetCipher(nested);
        }
        if (padding != null) {
            result.engineSetPaddingScheme(padding);
        }
        if (debuglevel >= 3) {
            Cipher.debug("Created cipher [2]: " + result);
        }
        return result;
    }

    public final int getState() {
        return this.state;
    }

    public final String getAlgorithm() {
        return this.cipherName;
    }

    public final String getMode() {
        return this.modeName == null ? "ECB" : this.modeName;
    }

    public final String getPadding() {
        return this.paddingName == null ? "NONE" : this.paddingName;
    }

    public final String getProvider() {
        return this.provider;
    }

    public final boolean isPaddingBlockCipher() {
        return this.getPlaintextBlockSize() > 1 && this.getPaddingScheme() != null;
    }

    public final int outBufferSize(int inLen) {
        return this.outBufferSizeInternal(inLen, false);
    }

    public final int outBufferSizeFinal(int inLen) {
        return this.outBufferSizeInternal(inLen, true);
    }

    public final int inBufferSize(int outLen) {
        return this.inBufferSizeInternal(outLen, false);
    }

    public final int inBufferSizeFinal(int outLen) {
        return this.inBufferSizeInternal(outLen, true);
    }

    public final int blockSize() {
        int blocksize = this.enginePlaintextBlockSize();
        if (blocksize != this.engineCiphertextBlockSize()) {
            throw new IllegalBlockSizeException("blockSize() called when plaintext and ciphertext block sizes differ");
        }
        return blocksize;
    }

    public final int getInputBlockSize() {
        switch (this.getState()) {
            case 1: {
                return this.enginePlaintextBlockSize();
            }
            case 2: {
                return this.engineCiphertextBlockSize();
            }
            default: {
                IJCE.reportBug("invalid Cipher state: " + this.getState());
            }
            case 0: 
        }
        throw new Error("cipher uninitialized");
    }

    public final int getOutputBlockSize() {
        switch (this.getState()) {
            case 1: {
                return this.engineCiphertextBlockSize();
            }
            case 2: {
                return this.enginePlaintextBlockSize();
            }
            default: {
                IJCE.reportBug("invalid Cipher state: " + this.getState());
            }
            case 0: 
        }
        throw new Error("cipher uninitialized");
    }

    public final int getPlaintextBlockSize() {
        return this.enginePlaintextBlockSize();
    }

    public final int getCiphertextBlockSize() {
        return this.engineCiphertextBlockSize();
    }

    public final void initEncrypt(Key key) throws KeyException {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        if (this.tracing) {
            this.traceVoidMethod("engineInitEncrypt(<" + key + ">)");
        }
        this.engineInitEncrypt(key);
        this.state = 1;
        this.inputSize = this.enginePlaintextBlockSize();
        this.outputSize = this.engineCiphertextBlockSize();
        if (this.inputSize < 1 || this.outputSize < 1) {
            this.state = 0;
            throw new Error("input or output block size < 1");
        }
        this.buffer = !this.implBuffering && this.inputSize > 1 ? new byte[this.inputSize] : null;
        this.buffered = 0;
        if (this.padding != null) {
            this.padding.engineSetBlockSize(this.inputSize);
        }
    }

    public final void initDecrypt(Key key) throws KeyException {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        if (this.tracing) {
            this.traceVoidMethod("engineInitDecrypt(<" + key + ">)");
        }
        this.engineInitDecrypt(key);
        this.state = 2;
        this.inputSize = this.engineCiphertextBlockSize();
        this.outputSize = this.enginePlaintextBlockSize();
        if (this.inputSize < 1 || this.outputSize < 1) {
            this.state = 0;
            throw new Error("input or output block size < 1");
        }
        this.buffer = !this.implBuffering && this.inputSize > 1 ? new byte[this.inputSize] : null;
        this.buffered = 0;
        if (this.padding != null) {
            this.padding.engineSetBlockSize(this.outputSize);
        }
    }

    public final byte[] update(byte[] in) {
        return this.update(in, 0, in.length);
    }

    public final byte[] update(byte[] in, int offset, int length) {
        byte[] out = new byte[this.outBufferSizeInternal(length, false)];
        int outlen = this.updateInternal(in, offset, length, out, 0, false);
        if (outlen != out.length) {
            byte[] newout = new byte[outlen];
            System.arraycopy(out, 0, newout, 0, outlen);
            return newout;
        }
        return out;
    }

    public final int update(byte[] in, int inOffset, int inLen, byte[] out) {
        return this.updateInternal(in, inOffset, inLen, out, 0, false);
    }

    public final int update(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        return this.updateInternal(in, inOffset, inLen, out, outOffset, false);
    }

    public final byte[] crypt(byte[] in) throws IllegalBlockSizeException {
        return this.crypt(in, 0, in.length);
    }

    public final byte[] crypt(byte[] in, int offset, int length) throws IllegalBlockSizeException {
        byte[] out = new byte[this.outBufferSizeInternal(length, true)];
        int outlen = this.updateInternal(in, offset, length, out, 0, true);
        if (outlen != out.length) {
            byte[] newout = new byte[outlen];
            System.arraycopy(out, 0, newout, 0, outlen);
            return newout;
        }
        return out;
    }

    public final int crypt(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) throws IllegalBlockSizeException {
        return this.updateInternal(in, inOffset, inLen, out, outOffset, true);
    }

    public final byte[] doFinal(byte[] in) throws IllegalBlockSizeException {
        return this.crypt(in, 0, in.length);
    }

    public final byte[] doFinal(byte[] in, int offset, int length) throws IllegalBlockSizeException {
        return this.crypt(in, offset, length);
    }

    public final int doFinal(byte[] in, int inOffset, int inLen, byte[] out) throws IllegalBlockSizeException {
        return this.crypt(in, inOffset, inLen, out, 0);
    }

    public final int doFinal(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) throws IllegalBlockSizeException {
        return this.crypt(in, inOffset, inLen, out, outOffset);
    }

    private int outBufferSizeInternal(int inLen, boolean isFinal) {
        if (inLen < 0) {
            throw new IllegalArgumentException("inLen < 0");
        }
        if (!this.implBuffering) {
            int remainder = (inLen += this.buffered) % this.inputSize;
            inLen -= remainder;
            if (isFinal && this.state == 1 && (this.padding != null || remainder > 0)) {
                inLen += this.inputSize;
            }
        }
        if (inLen < 0) {
            IJCE.reportBug("inLen < 0");
        }
        if (this.tracing) {
            this.traceMethod("engineOutBufferSize(" + inLen + ", " + isFinal + ")");
        }
        int result = this.engineOutBufferSize(inLen, isFinal);
        if (this.tracing) {
            this.traceResult(result);
        }
        return result;
    }

    private int inBufferSizeInternal(int outLen, boolean isFinal) {
        int remainder;
        if (!this.implBuffering && (remainder = outLen % this.outputSize) > 0) {
            outLen += this.outputSize - remainder;
        }
        if (this.tracing) {
            this.traceMethod("engineInBufferSize(" + outLen + ", " + isFinal + ")");
        }
        int result = this.engineInBufferSize(outLen, isFinal);
        if (this.tracing) {
            this.traceResult(result);
        }
        if (!this.implBuffering) {
            if (isFinal && this.state == 1 && this.padding != null) {
                result -= this.inputSize;
            }
            result -= this.buffered;
        }
        if (result < 0) {
            result = 0;
        }
        return result;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private int updateInternal(byte[] in, int inOffset, int inLen, byte[] out, int outOffset, boolean isFinal) {
        if (debuglevel >= 5 && this.tracing) {
            this.traceMethod("updateInternal(<" + Cipher.dump(in) + ">, " + inOffset + ", " + inLen + ", <" + Cipher.dump(out) + ">, " + outOffset + ", " + isFinal + ")");
        }
        boolean exception = false;
        int outStart = outOffset;
        try {
            if (this.state == 0) {
                throw new IllegalStateException("cipher uninitialized");
            }
            if (inLen < 0) {
                throw new IllegalArgumentException("inLen < 0");
            }
            if (inOffset < 0 || outOffset < 0 || (long)inOffset + (long)inLen > (long)in.length) {
                if (debuglevel < 1) throw new ArrayIndexOutOfBoundsException("inOffset < 0  || outOffset < 0 || (long)inOffset+inLen > in.length");
                Cipher.debug("inOffset = " + inOffset + ", inLen = " + inLen + ", outOffset = " + outOffset + ", in.length = " + in.length);
                throw new ArrayIndexOutOfBoundsException("inOffset < 0  || outOffset < 0 || (long)inOffset+inLen > in.length");
            }
            if (out == null) {
                throw new NullPointerException();
            }
            if (this.buffer == null) {
                if (this.tracing) {
                    this.traceMethod("engineUpdate(<" + Cipher.dump(in) + ">, " + inOffset + ", " + inLen + ", <" + Cipher.dump(out) + ">, " + outOffset + ")");
                    int result = this.engineUpdate(in, inOffset, inLen, out, outOffset);
                    this.traceResult(result);
                    outOffset += result;
                    if (isFinal && this.implBuffering) {
                        this.traceMethod("engineCrypt(<" + Cipher.dump(out) + ">, " + outOffset + ")");
                        result = this.engineCrypt(out, outOffset);
                        this.traceResult(result);
                        outOffset += result;
                    }
                } else {
                    outOffset += this.engineUpdate(in, inOffset, inLen, out, outOffset);
                    if (isFinal && this.implBuffering) {
                        outOffset += this.engineCrypt(out, outOffset);
                    }
                }
                int n = outOffset - outStart;
                return n;
            }
            if (in == out && (outOffset >= inOffset && (long)outOffset < (long)inOffset + (long)inLen || inOffset >= outOffset && (long)inOffset < (long)outOffset + (long)this.outBufferSizeInternal(inLen, isFinal))) {
                byte[] newin = new byte[inLen];
                System.arraycopy(in, inOffset, newin, 0, inLen);
                in = newin;
                inOffset = 0;
            }
            if (isFinal) {
                if (this.state == 1) {
                    outOffset += this.updateInternal(in, inOffset, inLen, out, outOffset, false);
                    if (this.padding == null) {
                        if (this.buffered > 0) {
                            this.buffered = 0;
                            throw new IllegalBlockSizeException(String.valueOf(this.getAlgorithm()) + ": Non-padding cipher in ENCRYPT state with an incomplete final block");
                        }
                        int n = outOffset - outStart;
                        return n;
                    }
                    this.padding.pad(this.buffer, 0, this.buffered);
                    this.buffered = 0;
                    if (this.tracing) {
                        this.traceMethod("engineUpdate(<" + Cipher.dump(this.buffer) + ">, 0, " + this.inputSize + ", <" + Cipher.dump(out) + ">, " + outOffset + ")");
                    }
                    int result = this.engineUpdate(this.buffer, 0, this.inputSize, out, outOffset);
                    if (this.tracing) {
                        this.traceResult(result);
                    }
                    int n = (outOffset += result) - outStart;
                    return n;
                }
                if (this.padding != null) {
                    if (inLen == 0) {
                        return 0;
                    }
                    outOffset += this.updateInternal(in, inOffset, inLen - 1, out, outOffset, false);
                    if (this.buffered != this.inputSize - 1) {
                        this.buffered = 0;
                        throw new IllegalBlockSizeException(String.valueOf(this.getAlgorithm()) + ": Cipher in DECRYPT state with an incomplete final block");
                    }
                    this.buffer[this.buffered] = in[inOffset + inLen - 1];
                    this.buffered = 0;
                    byte[] temp = new byte[this.outBufferSizeInternal(this.inputSize, false)];
                    if (this.tracing) {
                        this.traceMethod("engineUpdate(<" + Cipher.dump(this.buffer) + ">, 0, " + this.inputSize + ", <" + Cipher.dump(temp) + ">, 0)");
                    }
                    int result = this.engineUpdate(this.buffer, 0, this.inputSize, temp, 0);
                    if (this.tracing) {
                        this.traceResult(result);
                    }
                    int len = this.padding.unpad(temp, 0, temp.length);
                    System.arraycopy(temp, 0, out, outOffset, len);
                    int n = (outOffset += len) - outStart;
                    return n;
                }
            }
            if (this.buffered > 0) {
                if ((long)inLen + (long)this.buffered < (long)this.inputSize) {
                    System.arraycopy(in, inOffset, this.buffer, this.buffered, inLen);
                    this.buffered += inLen;
                    return 0;
                }
                int remainder = this.inputSize - this.buffered;
                System.arraycopy(in, inOffset, this.buffer, this.buffered, remainder);
                inOffset += remainder;
                inLen -= remainder;
                if (this.tracing) {
                    this.traceMethod("engineUpdate(<" + Cipher.dump(this.buffer) + ">, 0, " + this.inputSize + ", <" + Cipher.dump(out) + ">, " + outOffset + ")");
                }
                int result = this.engineUpdate(this.buffer, 0, this.inputSize, out, outOffset);
                if (this.tracing) {
                    this.traceResult(result);
                }
                outOffset += result;
            }
            this.buffered = inLen % this.inputSize;
            if (this.buffered > 0) {
                System.arraycopy(in, inOffset + inLen - this.buffered, this.buffer, 0, this.buffered);
                inLen -= this.buffered;
            }
            while (inLen > 0) {
                if (this.tracing) {
                    this.traceMethod("engineUpdate(<" + Cipher.dump(in) + ">, " + inOffset + ", " + this.inputSize + ", <" + Cipher.dump(out) + ">, " + outOffset + ")");
                }
                int result = this.engineUpdate(in, inOffset, this.inputSize, out, outOffset);
                if (this.tracing) {
                    this.traceResult(result);
                }
                outOffset += result;
                inOffset += this.inputSize;
                inLen -= this.inputSize;
            }
            int n = outOffset - outStart;
            return n;
        }
        catch (RuntimeException e) {
            if (this.tracing) {
                e.printStackTrace();
            }
            exception = true;
            throw e;
        }
        finally {
            if (debuglevel >= 5 && this.tracing && !exception) {
                this.traceResult(outOffset - outStart);
            }
        }
    }

    public void setParameter(String param, Object value) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        if (param == null) {
            throw new NullPointerException("param == null");
        }
        if (this.tracing) {
            this.traceVoidMethod("engineSetParameter(\"" + param + "\", <" + value + ">)");
        }
        this.engineSetParameter(param, value);
    }

    public Object getParameter(String param) throws NoSuchParameterException, InvalidParameterException {
        if (param == null) {
            throw new NullPointerException("param == null");
        }
        if (this.tracing) {
            this.traceMethod("engineGetParameter(\"" + param + "\")");
        }
        Object result = this.engineGetParameter(param);
        if (this.tracing) {
            this.traceResult("<" + result + ">");
        }
        return result;
    }

    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }

    public String toString() {
        return "Cipher [" + this.getProvider() + " " + this.getAlgorithm() + "/" + this.getMode() + "/" + this.getPadding() + "]";
    }

    protected void engineSetPaddingScheme(PaddingScheme padding) {
        if (this.state != 0) {
            throw new IllegalStateException("Cipher is already initialized");
        }
        this.padding = padding;
    }

    protected int engineBlockSize() {
        throw new Error("cipher classes must implement either engineBlockSize, or enginePlaintextBlockSize and engineCiphertextBlockSize");
    }

    protected int enginePlaintextBlockSize() {
        return this.engineBlockSize();
    }

    protected int engineCiphertextBlockSize() {
        return this.engineBlockSize();
    }

    protected int engineOutBufferSize(int inLen, boolean isFinal) {
        return inLen / this.inputSize * this.outputSize;
    }

    protected int engineInBufferSize(int outLen, boolean isFinal) {
        return outLen / this.outputSize * this.inputSize;
    }

    protected abstract void engineInitEncrypt(Key var1) throws KeyException;

    protected abstract void engineInitDecrypt(Key var1) throws KeyException;

    protected abstract int engineUpdate(byte[] var1, int var2, int var3, byte[] var4, int var5);

    protected int engineCrypt(byte[] out, int outOffset) {
        return 0;
    }

    protected void engineSetParameter(String param, Object value) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        throw new NoSuchParameterException(String.valueOf(this.getAlgorithm()) + ": " + param);
    }

    protected Object engineGetParameter(String param) throws NoSuchParameterException, InvalidParameterException {
        throw new NoSuchParameterException(String.valueOf(this.getAlgorithm()) + ": " + param);
    }

    public static String[] getAlgorithms(Provider provider) {
        return IJCE.getAlgorithms(provider, "Cipher");
    }

    public static String[] getAlgorithms() {
        return IJCE.getAlgorithms("Cipher");
    }
}

