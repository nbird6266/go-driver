/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLDebug;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class SSLHandshakeHashes {
    private MessageDigest md5;
    private MessageDigest sha;

    public SSLHandshakeHashes() {
        try {
            this.md5 = MessageDigest.getInstance("MD5");
            this.sha = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            throw new Error("Inconsistency. Shouldn't be missing MD5 or SHA");
        }
    }

    public void update(byte[] buf) {
        SSLDebug.debug(8, "Handshake hash update", buf);
        this.md5.update(buf);
        this.sha.update(buf);
    }

    public MessageDigest getMD5Digest() {
        try {
            return (MessageDigest)this.md5.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new Error("Hey, this should be cloneable");
        }
    }

    public MessageDigest getSHADigest() {
        try {
            return (MessageDigest)this.sha.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new Error("Hey, this should be cloneable");
        }
    }

    public byte[] getMD5Value() {
        return this.getMD5Value(new byte[0]);
    }

    public byte[] getSHAValue() {
        return this.getSHAValue(new byte[0]);
    }

    public byte[] getMD5Value(byte[] buf) {
        try {
            MessageDigest n_md5 = (MessageDigest)this.md5.clone();
            n_md5.update(buf);
            return n_md5.digest();
        }
        catch (CloneNotSupportedException e) {
            throw new Error("Hey, this should be cloneable");
        }
    }

    public byte[] getSHAValue(byte[] buf) {
        try {
            MessageDigest n_sha = (MessageDigest)this.sha.clone();
            n_sha.update(buf);
            return n_sha.digest();
        }
        catch (CloneNotSupportedException e) {
            throw new Error("Hey, this should be cloneable");
        }
    }

    public Object clone() throws CloneNotSupportedException {
        SSLHandshakeHashes hh = new SSLHandshakeHashes();
        hh.md5 = this.getMD5Digest();
        hh.sha = this.getSHADigest();
        return hh;
    }

    public void reset() {
        this.md5.reset();
        this.sha.reset();
    }
}

