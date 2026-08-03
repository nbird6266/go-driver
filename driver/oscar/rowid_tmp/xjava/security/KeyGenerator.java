/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import xjava.security.IJCE;
import xjava.security.InvalidParameterTypeException;
import xjava.security.NoSuchParameterException;
import xjava.security.Parameterized;
import xjava.security.SecretKey;

public abstract class KeyGenerator
implements Parameterized {
    private String algorithm;

    protected KeyGenerator(String algorithm) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        this.algorithm = algorithm;
    }

    public static KeyGenerator getInstance(String algorithm) throws NoSuchAlgorithmException {
        return (KeyGenerator)IJCE.getImplementation(algorithm, "KeyGenerator");
    }

    public static KeyGenerator getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        return (KeyGenerator)IJCE.getImplementation(algorithm, provider, "KeyGenerator");
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public abstract void initialize(SecureRandom var1);

    public abstract SecretKey generateKey();

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

    protected void engineSetParameter(String param, Object value) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        throw new NoSuchParameterException(String.valueOf(this.getAlgorithm()) + ": " + param);
    }

    protected Object engineGetParameter(String param) throws NoSuchParameterException, InvalidParameterException {
        throw new NoSuchParameterException(String.valueOf(this.getAlgorithm()) + ": " + param);
    }

    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }

    public String toString() {
        return String.valueOf(super.toString()) + " KeyGenerator [" + this.algorithm + "]";
    }

    public static String[] getAlgorithms(Provider provider) {
        return IJCE.getAlgorithms(provider, "KeyGenerator");
    }

    public static String[] getAlgorithms() {
        return IJCE.getAlgorithms("KeyGenerator");
    }
}

