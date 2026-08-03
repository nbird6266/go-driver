/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.provider.key.RawSecretKey;
import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.SecureRandom;
import xjava.security.ExtendedKeyGenerator;
import xjava.security.KeyGenerator;
import xjava.security.SecretKey;
import xjava.security.WeakKeyException;

public class RawKeyGenerator
extends KeyGenerator
implements ExtendedKeyGenerator,
Cloneable {
    private SecureRandom source;
    private int minLength;
    private int seedLength;
    private int maxLength;
    private boolean weakAllowed;

    protected RawKeyGenerator(String algorithm, int seedlength) throws IllegalArgumentException {
        super(algorithm);
        if (seedlength <= 0) {
            throw new IllegalArgumentException("seedlength <= 0");
        }
        this.minLength = seedlength;
        this.seedLength = seedlength;
        this.maxLength = seedlength;
    }

    protected RawKeyGenerator(String algorithm, int minlength, int defaultlength, int maxlength) throws IllegalArgumentException {
        super(algorithm);
        if (minlength <= 0 || minlength > defaultlength || defaultlength > maxlength) {
            throw new IllegalArgumentException("!(0 < minlength && minlength <= defaultlength && defaultlength <= maxlength)");
        }
        this.minLength = minlength;
        this.seedLength = defaultlength;
        this.maxLength = maxlength;
    }

    public SecretKey generateKey() {
        if (this.source == null) {
            this.source = new SecureRandom();
        }
        byte[] seed = new byte[this.seedLength];
        while (true) {
            this.source.nextBytes(seed);
            try {
                byte[] keybytes = this.engineGenerateKey(seed);
                return new RawSecretKey(this.getAlgorithm(), keybytes);
            }
            catch (KeyException keyException) {
                continue;
            }
            break;
        }
    }

    public void initialize(SecureRandom random) {
        this.source = random;
    }

    public void initialize(SecureRandom random, int length) {
        if (!this.isValidKeyLength(length)) {
            throw new IllegalArgumentException("invalid key length for " + this.getAlgorithm() + ": " + length + " bytes");
        }
        this.source = random;
        this.seedLength = length;
    }

    public boolean isWeakAllowed() {
        return this.weakAllowed;
    }

    public void setWeakAllowed(boolean allowWeak) {
        this.weakAllowed = allowWeak;
    }

    public int getMinimumKeyLength() {
        return this.minLength;
    }

    public int getDefaultKeyLength() {
        return this.seedLength;
    }

    public int getMaximumKeyLength() {
        return this.maxLength;
    }

    public boolean isValidKeyLength(int length) {
        return length >= this.minLength && length <= this.maxLength;
    }

    public SecretKey generateKey(byte[] data) throws WeakKeyException, InvalidKeyException {
        if (!this.isValidKeyLength(data.length)) {
            throw new InvalidKeyException("invalid key length for " + this.getAlgorithm() + ": " + data.length + " bytes");
        }
        byte[] keybytes = this.engineGenerateKey((byte[])data.clone());
        return new RawSecretKey(this.getAlgorithm(), keybytes);
    }

    protected byte[] engineGenerateKey(byte[] seed) throws WeakKeyException, InvalidKeyException {
        if (!this.isWeakAllowed() && this.isWeak(seed)) {
            throw new WeakKeyException(this.getAlgorithm());
        }
        return seed;
    }

    protected boolean isWeak(byte[] key) {
        return false;
    }
}

