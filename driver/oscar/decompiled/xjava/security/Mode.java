/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import xjava.security.Cipher;
import xjava.security.IJCE;
import xjava.security.InvalidParameterTypeException;
import xjava.security.NoSuchParameterException;

public abstract class Mode
extends Cipher {
    protected Cipher cipher;

    protected Mode(boolean implBuffering, boolean implPadding, String provider) {
        super(implBuffering, implPadding, provider);
    }

    public static Cipher getInstance(String algorithm) throws NoSuchAlgorithmException {
        return (Cipher)IJCE.getImplementation(algorithm, "Mode");
    }

    public static Cipher getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        return (Cipher)IJCE.getImplementation(algorithm, provider, "Mode");
    }

    public static String[] getAlgorithms(Provider provider) {
        return IJCE.getAlgorithms(provider, "Mode");
    }

    public static String[] getAlgorithms() {
        return IJCE.getAlgorithms("Mode");
    }

    public String toString() {
        return "Mode [" + this.getProvider() + " " + this.getAlgorithm() + "/" + this.getMode() + "/" + this.getPadding() + "]";
    }

    protected void engineSetCipher(Cipher cipher) {
        if (cipher == null) {
            throw new NullPointerException("cipher == null");
        }
        this.cipher = cipher;
    }

    protected void engineSetParameter(String param, Object value) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        this.cipher.setParameter(param, value);
    }

    protected Object engineGetParameter(String param) throws NoSuchParameterException, InvalidParameterException {
        return this.cipher.getParameter(param);
    }
}

