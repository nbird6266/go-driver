/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.provider;

import com.claymoresystems.crypto.Blindable;
import com.claymoresystems.crypto.PKCS1Pad;
import com.claymoresystems.provider.RSAAlgorithmBlind;
import cryptix.util.core.ArrayUtil;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import xjava.security.IllegalBlockSizeException;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.CryptixRSAPublicKey;
import xjava.security.interfaces.RSAFactors;

public class RSASignature
extends Signature
implements Blindable {
    private byte[] data = null;
    private BigInteger n;
    private BigInteger exp;
    private BigInteger publicExp;
    private BigInteger p;
    private BigInteger q;
    private BigInteger u;
    private SecureRandom blindingRNG = null;

    public RSASignature() {
        super("RawRSAPKCS#1");
    }

    protected void engineInitVerify(PublicKey key) throws InvalidKeyException {
        if (!(key instanceof CryptixRSAPublicKey)) {
            throw new InvalidKeyException(this.getAlgorithm() + ": Not a RSA Public Key");
        }
        CryptixRSAPublicKey rsa = (CryptixRSAPublicKey)key;
        this.n = rsa.getModulus();
        this.exp = rsa.getExponent();
    }

    protected void engineInitSign(PrivateKey key) throws InvalidKeyException {
        if (!(key instanceof CryptixRSAPrivateKey)) {
            throw new InvalidKeyException(this.getAlgorithm() + ": Not a RSA Private Key");
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
    }

    protected void engineUpdate(byte b) throws SignatureException {
        throw new SignatureException(this.getAlgorithm() + ": Must be called with a complete input");
    }

    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        if (this.state != 3 && this.state != 2) {
            throw new SignatureException(this.getAlgorithm() + ": Not initialized");
        }
        if (this.data != null) {
            throw new SignatureException(this.getAlgorithm() + ": Raw RSA may only be updated once");
        }
        this.data = new byte[len];
        System.arraycopy(b, off, this.data, 0, len);
    }

    protected byte[] engineSign() throws SignatureException {
        if (this.state != 2) {
            throw new SignatureException(this.getAlgorithm() + ": Not initialized");
        }
        if (this.data == null) {
            throw new SignatureException(this.getAlgorithm() + ": Must supply input");
        }
        byte[] toBeSigned = PKCS1Pad.pkcs1PadBuf(this.data, this.n, 2);
        BigInteger padded = new BigInteger(1, toBeSigned);
        BigInteger sval = RSAAlgorithmBlind.rsa(padded, this.n, this.exp, this.publicExp, this.p, this.q, this.u, this.blindingRNG);
        byte[] signed = this.sigToBytes(sval);
        return signed;
    }

    protected boolean engineVerify(byte[] signature) throws SignatureException {
        if (this.state != 3) {
            throw new SignatureException(this.getAlgorithm() + ": Not initialized");
        }
        if (this.data == null) {
            throw new SignatureException(this.getAlgorithm() + ": Must supply input");
        }
        if (signature.length != this.modLength()) {
            throw new IllegalBlockSizeException("Wrong input length");
        }
        BigInteger sval = new BigInteger(1, signature);
        if (sval.compareTo(this.n) > 0) {
            throw new IllegalBlockSizeException("Signature greater than modulus");
        }
        byte[] buf = RSAAlgorithmBlind.rsa(sval, this.n, this.exp, null, this.p, this.q, this.u, null).toByteArray();
        byte[] data_prime = null;
        try {
            data_prime = PKCS1Pad.pkcs1UnpadBuf(buf, 2, this.n);
        }
        catch (IllegalBlockSizeException e) {
            return false;
        }
        return ArrayUtil.areEqual(data_prime, this.data);
    }

    public void setBlindingInfo(SecureRandom rng, CryptixRSAPublicKey pubKey) {
        this.blindingRNG = rng;
        this.publicExp = pubKey.getExponent();
    }

    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
        throw new InvalidParameterException(this.getAlgorithm() + ": No settable parameters");
    }

    protected Object engineGetParameter(String param) throws InvalidParameterException {
        throw new InvalidParameterException(this.getAlgorithm() + ": No settable parameters");
    }

    protected byte[] sigToBytes(BigInteger num) {
        int length;
        byte[] tmp = num.toByteArray();
        if (tmp.length == (length = this.modLength())) {
            return tmp;
        }
        byte[] ntmp = new byte[length];
        if (tmp.length < length) {
            int i;
            for (i = 0; i < length - tmp.length; ++i) {
                ntmp[i] = 0;
            }
            System.arraycopy(tmp, 0, ntmp, i, tmp.length);
        } else {
            int i;
            for (i = 0; i < tmp.length - length; ++i) {
                if (tmp[i] == 0) continue;
                throw new InternalError("RSA signature error");
            }
            System.arraycopy(tmp, i, ntmp, 0, length);
        }
        return ntmp;
    }

    private int modLength() {
        int length = this.n.bitLength() / 8;
        return length += this.n.bitLength() % 8 > 0 ? 1 : 0;
    }
}

