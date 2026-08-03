/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.provider;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

public class RawDSASignature
extends Signature {
    private BigInteger p;
    private BigInteger q;
    private BigInteger g;
    private BigInteger XY;
    byte[] digest = null;
    SecureRandom rng;

    public RawDSASignature(String name) {
        super(name);
    }

    public RawDSASignature() {
        super("RawDSA");
    }

    protected void engineInitVerify(PublicKey key) throws InvalidKeyException {
        if (!(key instanceof DSAPublicKey)) {
            throw new InvalidKeyException(this.getAlgorithm() + ": Not a DSA Public Key");
        }
        DSAPublicKey dsa = (DSAPublicKey)key;
        this.extractParams(dsa.getParams());
        this.XY = dsa.getY();
    }

    protected void engineInitSign(PrivateKey key) throws InvalidKeyException {
        if (!(key instanceof DSAPrivateKey)) {
            throw new InvalidKeyException(this.getAlgorithm() + ": Not a DSA Private Key");
        }
        DSAPrivateKey dsa = (DSAPrivateKey)key;
        this.extractParams(dsa.getParams());
        this.XY = dsa.getX();
    }

    protected void engineUpdate(byte b) throws SignatureException {
        throw new SignatureException(this.getAlgorithm() + ": Must be called with a SHA-1 digest for input");
    }

    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        if (this.state != 3 && this.state != 2) {
            throw new SignatureException(this.getAlgorithm() + ": Not initialized");
        }
        if (this.digest != null) {
            throw new SignatureException(this.getAlgorithm() + ": Raw DSA may only be updated once");
        }
        if (len != 20) {
            throw new SignatureException(this.getAlgorithm() + ": Raw DSA must have a 20 byte input");
        }
        this.digest = new byte[20];
        System.arraycopy(b, off, this.digest, 0, len);
    }

    protected byte[] engineSign() throws SignatureException {
        if (this.state != 2) {
            throw new SignatureException(this.getAlgorithm() + ": Not initialized");
        }
        if (this.digest == null) {
            throw new SignatureException(this.getAlgorithm() + ": Must supply digest");
        }
        if (this.rng == null) {
            this.rng = new SecureRandom();
        }
        BigInteger G = new BigInteger(180, this.rng);
        BigInteger k = G.mod(this.q);
        BigInteger kinv = k.modInverse(this.q);
        BigInteger r = this.g.modPow(k, this.p).mod(this.q);
        BigInteger digestBig = new BigInteger(1, this.digest);
        BigInteger tmp = digestBig.add(this.XY.multiply(r));
        tmp = tmp.multiply(kinv);
        BigInteger s = tmp.mod(this.q);
        byte[] r_bytes = r.toByteArray();
        byte[] s_bytes = s.toByteArray();
        byte[] encoding = new byte[r_bytes.length + s_bytes.length + 6];
        encoding[0] = 48;
        encoding[1] = (byte)(r_bytes.length + s_bytes.length + 4 & 0xFF);
        int offset = 2;
        encoding[offset++] = 2;
        encoding[offset++] = (byte)(r_bytes.length & 0xFF);
        System.arraycopy(r_bytes, 0, encoding, offset, r_bytes.length);
        offset += r_bytes.length;
        encoding[offset++] = 2;
        encoding[offset++] = (byte)(s_bytes.length & 0xFF);
        System.arraycopy(s_bytes, 0, encoding, offset, s_bytes.length);
        return encoding;
    }

    protected boolean engineVerify(byte[] signature) throws SignatureException {
        int offset = 0;
        int left = signature.length;
        if (this.state != 3) {
            throw new SignatureException(this.getAlgorithm() + ": Not initialized");
        }
        if (this.digest == null) {
            throw new SignatureException(this.getAlgorithm() + ": Must supply digest");
        }
        BigInteger M = new BigInteger(1, this.digest);
        this.encodeAssert(signature, offset++, 48, "Tag: expecting sequence");
        this.encodeAssert(signature, offset++, signature.length - 2, "length");
        this.encodeAssert(signature, offset++, 2, "Tag: expecting integer");
        byte r_length = signature[offset++];
        if (r_length > (left -= 4)) {
            throw new SignatureException("r longer than total encoding");
        }
        byte[] r_bytes = new byte[r_length];
        System.arraycopy(signature, offset, r_bytes, 0, r_length);
        left -= r_length;
        offset += r_length;
        this.encodeAssert(signature, offset++, 2, "Tag: expecting integer");
        byte s_length = signature[offset++];
        if (s_length != (left -= 2)) {
            throw new SignatureException("incorrect length for than total encoding");
        }
        byte[] s_bytes = new byte[s_length];
        System.arraycopy(signature, offset, s_bytes, 0, s_length);
        BigInteger r = new BigInteger(r_bytes);
        BigInteger s = new BigInteger(s_bytes);
        BigInteger zero = new BigInteger("0");
        if (r.compareTo(zero) <= 0) {
            return false;
        }
        if (r.compareTo(this.q) >= 0) {
            return false;
        }
        if (s.compareTo(zero) <= 0) {
            return false;
        }
        if (s.compareTo(this.q) >= 0) {
            return false;
        }
        BigInteger w = s.modInverse(this.q);
        BigInteger u1 = M.multiply(w);
        BigInteger u1q = u1.mod(this.q);
        BigInteger u2 = r.multiply(w);
        BigInteger u2q = u2.mod(this.q);
        BigInteger gu1 = this.g.modPow(u1q, this.p);
        BigInteger yu2 = this.XY.modPow(u2q, this.p);
        BigInteger vtmp = gu1.multiply(yu2);
        BigInteger vtmpp = vtmp.mod(this.p);
        BigInteger v = vtmpp.mod(this.q);
        return v.equals(r);
    }

    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
        if (param.equals("SecureRandom")) {
            this.rng = (SecureRandom)value;
            return;
        }
        throw new InvalidParameterException(this.getAlgorithm() + ": No settable parameters");
    }

    protected Object engineGetParameter(String param) throws InvalidParameterException {
        throw new InvalidParameterException(this.getAlgorithm() + ": No settable parameters");
    }

    private void encodeAssert(byte[] arr, int offset, int value, String error) throws SignatureException {
        if (arr[offset] != value) {
            throw new SignatureException(this.getAlgorithm() + ": Encoding error. Bad " + error);
        }
    }

    private void extractParams(DSAParams params) {
        this.p = params.getP();
        this.q = params.getQ();
        this.g = params.getG();
    }
}

