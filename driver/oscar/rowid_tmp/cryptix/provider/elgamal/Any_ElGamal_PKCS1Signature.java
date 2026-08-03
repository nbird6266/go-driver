/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.elgamal;

import cryptix.CryptixException;
import cryptix.provider.elgamal.ElGamalAlgorithm;
import cryptix.util.core.BI;
import cryptix.util.core.Debug;
import cryptix.util.core.Hex;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Random;
import xjava.security.interfaces.ElGamalPrivateKey;
import xjava.security.interfaces.ElGamalPublicKey;

public abstract class Any_ElGamal_PKCS1Signature
extends Signature {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("ElGamal", "Any_ElGamal_PKCS1Signature");
    private static final PrintWriter err = Debug.getOutput();
    private static final int MIN_BITLENGTH = 256;
    private static final int POSITIVE = 1;
    private static final BigInteger ZERO = BigInteger.valueOf(0L);
    private static final BigInteger ONE = BigInteger.valueOf(1L);
    private BigInteger p;
    private BigInteger g;
    private BigInteger x;
    private BigInteger y;
    private int primeLen;
    private Random rng;
    private MessageDigest md;

    private static void debug(String s) {
        err.println("Any_ElGamal_PKCS1Signature: " + s);
    }

    protected Any_ElGamal_PKCS1Signature(String mdAlgorithm) {
        super(String.valueOf(mdAlgorithm) + "/ElGamal/PKCS#1");
        try {
            this.md = MessageDigest.getInstance(mdAlgorithm);
        }
        catch (Exception e) {
            throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Unable to instantiate the " + mdAlgorithm + " MessageDigest\n" + e);
        }
    }

    protected void engineInitSign(PrivateKey key) throws InvalidKeyException {
        if (!(key instanceof ElGamalPrivateKey)) {
            throw new InvalidKeyException("ElGamal: signing key does not implement java.security.interfaces.ElGamalPrivateKey");
        }
        ElGamalPrivateKey privateKey = (ElGamalPrivateKey)key;
        BigInteger newX = privateKey.getX();
        if (newX == null) {
            throw new InvalidKeyException("ElGamal: getX() == null");
        }
        this.initInternal(privateKey.getP(), privateKey.getG(), newX, privateKey.getY());
        if (this.rng == null) {
            this.rng = new SecureRandom();
        }
    }

    protected void engineInitVerify(PublicKey key) throws InvalidKeyException {
        if (!(key instanceof ElGamalPublicKey)) {
            throw new InvalidKeyException("ElGamal: verification key does not implement java.security.interfaces.ElGamalPublicKey");
        }
        ElGamalPublicKey publicKey = (ElGamalPublicKey)key;
        this.initInternal(publicKey.getP(), publicKey.getG(), null, publicKey.getY());
        if (this.rng == null) {
            this.rng = new SecureRandom();
        }
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
        if (newP.bitLength() < 256) {
            throw new InvalidKeyException("ElGamal: getP().bitLength() < 256");
        }
        if (newP.compareTo(ONE) <= 0) {
            throw new InvalidKeyException("ElGamal: getP() < 2");
        }
        this.p = newP;
        this.g = newG;
        this.x = newX;
        this.y = newY;
        this.primeLen = (this.p.bitLength() + 7) / 8;
        this.md.reset();
    }

    protected void engineUpdate(byte b) throws SignatureException {
        if (this.state != 3 && this.state != 2) {
            throw new SignatureException(String.valueOf(this.getAlgorithm()) + ": Not initialized");
        }
        this.md.update(b);
    }

    protected void engineUpdate(byte[] in, int offset, int length) throws SignatureException {
        if (this.state != 3 && this.state != 2) {
            throw new SignatureException(String.valueOf(this.getAlgorithm()) + ": Not initialized");
        }
        this.md.update(in, offset, length);
    }

    protected byte[] engineSign() throws SignatureException {
        if (this.state != 2) {
            throw new SignatureException(String.valueOf(this.getAlgorithm()) + ": Not initialized for signing");
        }
        BigInteger pkcs = this.makePKCS1();
        BigInteger[] ab = new BigInteger[2];
        ElGamalAlgorithm.sign(pkcs, ab, this.p, this.g, this.x, this.rng);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BI.toStream(ab[0], baos);
            BI.toStream(ab[1], baos);
        }
        catch (IOException e) {
            throw new SignatureException("BI.toStream() failed");
        }
        return baos.toByteArray();
    }

    protected boolean engineVerify(byte[] signature) throws SignatureException {
        BigInteger b;
        BigInteger a;
        if (this.state != 3) {
            throw new SignatureException(String.valueOf(this.getAlgorithm()) + ": Not initialized for verification");
        }
        BigInteger pkcs = this.makePKCS1();
        ByteArrayInputStream bais = new ByteArrayInputStream(signature);
        try {
            a = BI.fromStream(bais);
            b = BI.fromStream(bais);
        }
        catch (IOException e) {
            throw new SignatureException("BI.fromStream() failed");
        }
        return ElGamalAlgorithm.verify(pkcs, a, b, this.p, this.g, this.y);
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

    private BigInteger makePKCS1() {
        byte[] theMD = this.md.digest();
        int mdl = theMD.length;
        byte[] r = new byte[this.primeLen];
        r[1] = 1;
        byte[] aid = this.getAlgorithmEncoding();
        int aidl = aid.length;
        int padLen = this.primeLen - 3 - aidl - mdl;
        int i = 0;
        while (i < padLen) {
            r[2 + i] = -1;
            ++i;
        }
        System.arraycopy(aid, 0, r, padLen + 3, aidl);
        System.arraycopy(theMD, 0, r, this.primeLen - mdl, mdl);
        if (debuglevel >= 4) {
            Any_ElGamal_PKCS1Signature.debug("PKCS#1 frame = " + Hex.dumpString(r));
        }
        return new BigInteger(r);
    }

    protected abstract byte[] getAlgorithmEncoding();
}

