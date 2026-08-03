/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.rsa;

import cryptix.CryptixException;
import cryptix.provider.rsa.RSAAlgorithm;
import cryptix.util.core.ArrayUtil;
import cryptix.util.core.BI;
import cryptix.util.core.Debug;
import cryptix.util.core.Hex;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import xjava.security.AsymmetricCipher;
import xjava.security.Cipher;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.CryptixRSAPublicKey;
import xjava.security.interfaces.RSAFactors;

public class RawRSACipher
extends Cipher
implements AsymmetricCipher,
Cloneable {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("RSA", "RawRSACipher");
    private static final PrintWriter err = Debug.getOutput();
    private static final boolean WIPE = true;
    private static final int POSITIVE = 1;
    private static final BigInteger ZERO = BigInteger.valueOf(0L);
    private static final BigInteger ONE = BigInteger.valueOf(1L);
    private BigInteger n;
    private BigInteger exp;
    private BigInteger p;
    private BigInteger q;
    private BigInteger u;
    private int blockSize;
    private byte[] temp;

    private static void debug(String s) {
        err.println("RawRSACipher: " + s);
    }

    public RawRSACipher() {
        super(false, false, "Cryptix");
    }

    protected void engineInitEncrypt(Key key) throws InvalidKeyException {
        if (!(key instanceof CryptixRSAPublicKey)) {
            throw new InvalidKeyException(String.valueOf(this.getAlgorithm()) + ": Not an RSA public key");
        }
        CryptixRSAPublicKey rsa = (CryptixRSAPublicKey)key;
        this.n = rsa.getModulus();
        this.exp = rsa.getExponent();
        if (key instanceof RSAFactors) {
            RSAFactors factors = (RSAFactors)((Object)key);
            this.p = factors.getP();
            this.q = factors.getQ();
            this.u = factors.getInverseOfQModP();
        }
        this.initInternal();
    }

    protected void engineInitDecrypt(Key key) throws InvalidKeyException {
        if (!(key instanceof CryptixRSAPrivateKey)) {
            throw new InvalidKeyException(String.valueOf(this.getAlgorithm()) + ": Not an RSA private key");
        }
        CryptixRSAPrivateKey rsa = (CryptixRSAPrivateKey)key;
        this.n = rsa.getModulus();
        this.exp = rsa.getExponent();
        if (key instanceof RSAFactors) {
            RSAFactors factors = (RSAFactors)((Object)key);
            this.p = factors.getP();
            this.q = factors.getQ();
            this.u = factors.getInverseOfQModP();
        }
        this.initInternal();
    }

    private void initInternal() {
        this.blockSize = BI.getMagnitude(this.n).length;
        this.temp = new byte[this.blockSize];
    }

    protected int enginePlaintextBlockSize() {
        if (this.blockSize == 0) {
            throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Block size is not valid until key is set");
        }
        return this.blockSize - 1;
    }

    protected int engineCiphertextBlockSize() {
        if (this.blockSize == 0) {
            throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Block size is not valid until key is set");
        }
        return this.blockSize;
    }

    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        if (inLen < 0) {
            throw new IllegalArgumentException("inLen < 0");
        }
        int inBlockSize = this.getState() == 1 ? this.enginePlaintextBlockSize() : this.engineCiphertextBlockSize();
        int outBlockSize = this.getState() == 1 ? this.engineCiphertextBlockSize() : this.enginePlaintextBlockSize();
        int blockCount = inLen / inBlockSize;
        int i = 0;
        while (i < blockCount) {
            ArrayUtil.clear(this.temp);
            System.arraycopy(in, inOffset, this.temp, this.temp.length - inBlockSize, inBlockSize);
            BigInteger I = new BigInteger(1, this.temp);
            if (I.compareTo(this.n) >= 0) {
                throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Input block value is out of range (>= modulus)");
            }
            BigInteger O = RSAAlgorithm.rsa(I, this.n, this.exp, this.p, this.q, this.u);
            byte[] buffer = BI.getMagnitude(O);
            if (buffer.length > outBlockSize) {
                throw new ArrayIndexOutOfBoundsException("Decryption failed, wrong key?");
            }
            ArrayUtil.clear(this.temp);
            System.arraycopy(buffer, 0, this.temp, outBlockSize - buffer.length, buffer.length);
            System.arraycopy(this.temp, 0, out, outOffset, outBlockSize);
            inOffset += inBlockSize;
            outOffset += outBlockSize;
            ++i;
        }
        return blockCount * outBlockSize;
    }

    public static final void main(String[] args) {
        try {
            RawRSACipher.self_test(new PrintWriter(System.out, true));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void self_test(PrintWriter out) throws Exception {
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA", "Cryptix");
        SecureRandom random = new SecureRandom();
        long start = System.currentTimeMillis();
        keygen.initialize(1024, random);
        KeyPair keypair = keygen.generateKeyPair();
        long duration = System.currentTimeMillis() - start;
        out.println("Keygen: " + (float)duration / 1000.0f + " seconds");
        RawRSACipher raw = new RawRSACipher();
        raw.test(out, keypair, random);
    }

    private void test(PrintWriter out, KeyPair keypair, SecureRandom random) throws KeyException {
        CryptixRSAPrivateKey privateKey = (CryptixRSAPrivateKey)keypair.getPrivate();
        CryptixRSAPublicKey publicKey = (CryptixRSAPublicKey)keypair.getPublic();
        long start = System.currentTimeMillis();
        this.initEncrypt(publicKey);
        BigInteger e = this.exp;
        byte[] M = new byte[this.getPlaintextBlockSize()];
        random.nextBytes(M);
        byte[] C = this.crypt(M);
        long midpoint = System.currentTimeMillis();
        this.initDecrypt(privateKey);
        byte[] Mdash = this.crypt(C);
        long end = System.currentTimeMillis();
        out.println("         n = " + BI.dumpString(this.n));
        out.println("         e = " + BI.dumpString(e));
        out.println("         d = " + BI.dumpString(this.exp));
        out.println("         p = " + BI.dumpString(this.p));
        out.println("         q = " + BI.dumpString(this.q));
        out.println("q^-1 mod p = " + BI.dumpString(this.u));
        out.println(" plaintext = " + Hex.toString(M) + "\n");
        out.println("ciphertext = " + Hex.toString(C) + "\n");
        if (!ArrayUtil.areEqual(M, Mdash)) {
            out.println("DECRYPTION FAILED!\n");
            out.println("  computed = " + Hex.toString(Mdash) + "\n");
        }
        out.println("Encrypt: " + (float)(midpoint - start) / 1000.0f + " seconds");
        out.println("Decrypt: " + (float)(end - midpoint) / 1000.0f + " seconds");
    }
}

