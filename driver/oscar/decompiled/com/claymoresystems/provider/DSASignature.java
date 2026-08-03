/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.provider;

import com.claymoresystems.provider.RawDSASignature;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;

public class DSASignature
extends RawDSASignature {
    MessageDigest dig;

    public DSASignature() {
        super("DSA");
    }

    protected void engineInitVerify(PublicKey key) throws InvalidKeyException {
        super.engineInitVerify(key);
        this.init();
    }

    protected void engineInitSign(PrivateKey key) throws InvalidKeyException {
        super.engineInitSign(key);
        this.init();
    }

    protected void engineUpdate(byte b) throws SignatureException {
        this.dig.update(b);
    }

    protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
        this.dig.update(b, off, len);
    }

    protected byte[] engineSign() throws SignatureException {
        this.finish();
        return super.engineSign();
    }

    protected boolean engineVerify(byte[] signature) throws SignatureException {
        this.finish();
        return super.engineVerify(signature);
    }

    private void init() {
        try {
            this.dig = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalError("Hey, what happened to SHA-1???");
        }
    }

    private void finish() {
        this.digest = this.dig.digest();
    }
}

