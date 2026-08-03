/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.rsa;

import cryptix.CryptixException;
import cryptix.provider.rsa.RSAAlgorithm;
import cryptix.util.core.Debug;
import cryptix.util.core.Hex;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.CryptixRSAPublicKey;
import xjava.security.interfaces.RSAFactors;

public abstract class Any_RSA_PKCS1Signature
extends Signature {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("RSA", "Any_RSA_PKCS1Signature");
    private static final PrintWriter err = Debug.getOutput();
    private static final int POSITIVE = 1;
    private static final BigInteger ZERO = BigInteger.valueOf(0L);
    private static final BigInteger ONE = BigInteger.valueOf(1L);
    private BigInteger n;
    private BigInteger exp;
    private BigInteger p;
    private BigInteger q;
    private BigInteger u;
    private MessageDigest md;

    private static void debug(String s) {
        err.println("Any_RSA_PKCS1Signature: " + s);
    }

    protected Any_RSA_PKCS1Signature(String mdAlgorithm) {
        super(String.valueOf(mdAlgorithm) + "/RSA/PKCS#1");
        try {
            this.md = MessageDigest.getInstance(mdAlgorithm);
        }
        catch (Exception e) {
            throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Unable to instantiate the " + mdAlgorithm + " MessageDigest\n" + e);
        }
    }

    protected void engineInitSign(PrivateKey key) throws InvalidKeyException {
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
        this.md.reset();
        int mdl = this.md.digest().length;
        int length = (this.n.bitLength() + 7) / 8;
        int aidl = this.getAlgorithmEncoding().length;
        int padLen = length - 3 - aidl - mdl;
        if (padLen < 0) {
            throw new InvalidKeyException("Signer's public key modulus too short.");
        }
    }

    protected void engineInitVerify(PublicKey key) throws InvalidKeyException {
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
        this.md.reset();
        int mdl = this.md.digest().length;
        int length = (this.n.bitLength() + 7) / 8;
        int aidl = this.getAlgorithmEncoding().length;
        int padLen = length - 3 - aidl - mdl;
        if (padLen < 0) {
            throw new InvalidKeyException("Signer's public key modulus too short.");
        }
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
        long t = 0L;
        if (debuglevel >= 7) {
            t = System.currentTimeMillis();
        }
        BigInteger pkcs = this.makePKCS1();
        BigInteger result = RSAAlgorithm.rsa(pkcs, this.n, this.exp, this.p, this.q, this.u);
        if (debuglevel >= 7) {
            t = System.currentTimeMillis() - t;
            Any_RSA_PKCS1Signature.debug(" ...engineSign() completed in " + t + " ms.");
        }
        return result.toByteArray();
    }

    protected boolean engineVerify(byte[] signature) throws SignatureException {
        if (this.state != 3) {
            throw new SignatureException(String.valueOf(this.getAlgorithm()) + ": Not initialized for verification");
        }
        long t = 0L;
        if (debuglevel >= 7) {
            t = System.currentTimeMillis();
        }
        BigInteger M = new BigInteger(1, signature);
        BigInteger computed = RSAAlgorithm.rsa(M, this.n, this.exp, this.p, this.q, this.u);
        BigInteger actual = this.makePKCS1();
        boolean ok = computed.equals(actual);
        if (debuglevel >= 7) {
            t = System.currentTimeMillis() - t;
            Any_RSA_PKCS1Signature.debug(" ...engineVerify() completed in " + t + " ms.");
            if (!ok) {
                Any_RSA_PKCS1Signature.debug("   Computed: " + Hex.dumpString(computed.toByteArray()));
                Any_RSA_PKCS1Signature.debug("     Actual: " + Hex.dumpString(actual.toByteArray()));
            }
        }
        return ok;
    }

    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
        throw new InvalidParameterException(String.valueOf(this.getAlgorithm()) + ": " + param);
    }

    protected Object engineGetParameter(String param) throws InvalidParameterException {
        throw new InvalidParameterException(String.valueOf(this.getAlgorithm()) + ": " + param);
    }

    private BigInteger makePKCS1() throws SignatureException {
        byte[] theMD = this.md.digest();
        int mdl = theMD.length;
        int length = (this.n.bitLength() + 7) / 8;
        byte[] r = new byte[length];
        r[1] = 1;
        byte[] aid = this.getAlgorithmEncoding();
        int aidl = aid.length;
        int padLen = length - 3 - aidl - mdl;
        if (padLen < 0) {
            throw new SignatureException("Signer's public key modulus too short.");
        }
        int i = 0;
        while (i < padLen) {
            r[2 + i++] = -1;
        }
        System.arraycopy(aid, 0, r, padLen + 3, aidl);
        System.arraycopy(theMD, 0, r, length - mdl, mdl);
        if (debuglevel >= 4) {
            Any_RSA_PKCS1Signature.debug("PKCS#1 frame = " + Hex.dumpString(r));
        }
        return new BigInteger(r);
    }

    protected abstract byte[] getAlgorithmEncoding();
}

