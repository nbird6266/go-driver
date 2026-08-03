/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.elgamal;

import cryptix.CryptixException;
import cryptix.provider.elgamal.BaseElGamalKeyPairGenerator;
import cryptix.provider.elgamal.ElGamalAlgorithm;
import cryptix.util.core.ArrayUtil;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Random;
import xjava.security.AsymmetricCipher;
import xjava.security.Cipher;
import xjava.security.IllegalBlockSizeException;
import xjava.security.interfaces.ElGamalPrivateKey;
import xjava.security.interfaces.ElGamalPublicKey;

public class RawElGamalCipher
extends Cipher
implements AsymmetricCipher,
Cloneable {
    private static final int POSITIVE = 1;
    private static final BigInteger ONE = BigInteger.valueOf(1L);
    private BigInteger p;
    private BigInteger p_minus_1;
    private BigInteger g;
    private BigInteger x;
    private BigInteger y;
    private int primeLen;
    private Random rng;

    public RawElGamalCipher() {
        super(false, true, "Cryptix");
    }

    protected void engineInitEncrypt(Key key) throws KeyException {
        if (!(key instanceof ElGamalPublicKey)) {
            throw new InvalidKeyException("ElGamal: encryption key does not implement java.security.interfaces.ElGamalPublicKey");
        }
        ElGamalPublicKey elgamalKey = (ElGamalPublicKey)key;
        this.initInternal(elgamalKey.getP(), elgamalKey.getG(), null, elgamalKey.getY());
        if (this.rng == null) {
            this.rng = new SecureRandom();
        }
    }

    protected void engineInitDecrypt(Key key) throws KeyException {
        if (!(key instanceof ElGamalPrivateKey)) {
            throw new InvalidKeyException("ElGamal: decryption key does not implement java.security.interfaces.ElGamalPrivateKey");
        }
        ElGamalPrivateKey elgamalKey = (ElGamalPrivateKey)key;
        BigInteger newX = elgamalKey.getX();
        if (newX == null) {
            throw new InvalidKeyException("ElGamal: getX() == null");
        }
        this.initInternal(elgamalKey.getP(), elgamalKey.getG(), newX, elgamalKey.getY());
    }

    private void initInternal(BigInteger newP, BigInteger newG, BigInteger newX, BigInteger newY) throws InvalidKeyException {
        if (newP == null) {
            throw new InvalidKeyException("ElGamal: getP() == null");
        }
        if (newG == null) {
            throw new InvalidKeyException("ElGamal: getG() == null");
        }
        if (newY == null) {
            throw new InvalidKeyException("ElGamal: getY() == null");
        }
        this.p = newP;
        this.g = newG;
        this.x = newX;
        this.y = newY;
        this.primeLen = (this.p.bitLength() - 1) / 8;
    }

    protected int enginePlaintextBlockSize() {
        if (this.primeLen == 0) {
            throw new CryptixException("ElGamal: plaintext block size is not valid until key is set");
        }
        return this.primeLen;
    }

    protected int engineCiphertextBlockSize() {
        if (this.primeLen == 0) {
            throw new CryptixException("ElGamal: ciphertext block size is not valid until key is set");
        }
        return this.primeLen * 2;
    }

    protected void engineSetParameter(String param, Object value) {
        if (param.equals("random")) {
            if (!(value instanceof Random)) {
                throw new InvalidParameterException("value must be an instance of java.util.Random");
            }
            this.rng = (Random)value;
            return;
        }
        throw new InvalidParameterException(param);
    }

    protected Object engineGetParameter(String param) {
        if (param.equals("random")) {
            return this.rng;
        }
        return null;
    }

    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        if (inLen <= 0) {
            return 0;
        }
        if (this.getState() == 1) {
            if (inLen != this.primeLen) {
                throw new IllegalBlockSizeException("inLen = " + inLen + ", plaintext block size = " + this.primeLen);
            }
            byte[] plaintext = new byte[this.primeLen];
            System.arraycopy(in, inOffset, plaintext, 0, this.primeLen);
            BigInteger[] ab = new BigInteger[2];
            BigInteger M = new BigInteger(1, plaintext);
            ElGamalAlgorithm.encrypt(M, ab, this.p, this.g, this.y, this.rng);
            byte[] aBytes = ab[0].toByteArray();
            byte[] bBytes = ab[1].toByteArray();
            ArrayUtil.clear(out, outOffset, this.primeLen * 2);
            System.arraycopy(aBytes, 0, out, outOffset + this.primeLen - aBytes.length, aBytes.length);
            System.arraycopy(bBytes, 0, out, outOffset + this.primeLen * 2 - bBytes.length, bBytes.length);
            ArrayUtil.clear(plaintext);
            return this.primeLen * 2;
        }
        if (inLen != this.primeLen * 2) {
            throw new IllegalBlockSizeException("inLen = " + inLen + ", ciphertext block size = " + this.primeLen * 2);
        }
        byte[] ciphertext = new byte[this.primeLen];
        System.arraycopy(in, inOffset, ciphertext, 0, this.primeLen);
        BigInteger a = new BigInteger(1, ciphertext);
        System.arraycopy(in, inOffset + this.primeLen, ciphertext, 0, this.primeLen);
        BigInteger b = new BigInteger(1, ciphertext);
        BigInteger M = ElGamalAlgorithm.decrypt(a, b, this.p, this.g, this.x);
        byte[] plaintext = M.toByteArray();
        ArrayUtil.clear(out, outOffset, this.primeLen - plaintext.length);
        System.arraycopy(plaintext, 0, out, outOffset + this.primeLen - plaintext.length, plaintext.length);
        return this.primeLen;
    }

    public static final void main(String[] args) {
        try {
            RawElGamalCipher.self_test(new PrintWriter(System.out, true));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void self_test(PrintWriter out) throws KeyException {
        BaseElGamalKeyPairGenerator keygen = new BaseElGamalKeyPairGenerator();
        SecureRandom random = new SecureRandom();
        long start = System.currentTimeMillis();
        ((KeyPairGenerator)keygen).initialize(385, random);
        KeyPair keypair = ((KeyPairGenerator)keygen).generateKeyPair();
        long duration = System.currentTimeMillis() - start;
        out.println("Keygen: " + (float)duration / 1000.0f + " seconds");
        RawElGamalCipher raw = new RawElGamalCipher();
        raw.test(out, keypair, random);
    }

    private void test(PrintWriter out, KeyPair keypair, SecureRandom random) throws KeyException {
        ElGamalPrivateKey privateKey = (ElGamalPrivateKey)keypair.getPrivate();
        ElGamalPublicKey publicKey = (ElGamalPublicKey)keypair.getPublic();
        BigInteger M = new BigInteger(privateKey.getP().bitLength() - 1, random);
        this.rng = random;
        long start = System.currentTimeMillis();
        this.initEncrypt(publicKey);
        BigInteger[] ab = new BigInteger[2];
        ElGamalAlgorithm.encrypt(M, ab, this.p, this.g, this.y, this.rng);
        long midpoint = System.currentTimeMillis();
        this.initDecrypt(privateKey);
        BigInteger Mdash = ElGamalAlgorithm.decrypt(ab[0], ab[1], this.p, this.g, this.x);
        long end = System.currentTimeMillis();
        out.println("p = " + this.p);
        out.println("g = " + this.g);
        out.println("x = " + this.x);
        out.println("y = " + this.y);
        out.println("M = " + M);
        out.println("a = " + ab[0]);
        out.println("b = " + ab[1]);
        if (!M.equals(Mdash)) {
            out.println("DECRYPTION FAILED!");
            out.println("M' = " + Mdash);
        }
        out.println("Encrypt: " + (float)(midpoint - start) / 1000.0f + " seconds");
        out.println("Decrypt: " + (float)(end - midpoint) / 1000.0f + " seconds");
    }
}

