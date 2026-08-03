/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLHandshake;
import com.claymoresystems.ptls.SSLHandshakeHashes;
import java.security.MessageDigest;

class SSLv3CertificateVerify {
    SSLv3CertificateVerify() {
    }

    static byte[] computeToBeSigned(SSLHandshake hs, boolean mine) {
        int i;
        SSLHandshakeHashes hashes = mine ? hs.hashes : hs.save_hashes;
        MessageDigest md5 = hashes.getMD5Digest();
        MessageDigest sha = hashes.getSHADigest();
        md5.update(hs.master_secret);
        for (i = 0; i < 48; ++i) {
            md5.update(SSLHandshake.pad_1);
        }
        byte[] md5_bytes = md5.digest();
        md5.update(hs.master_secret);
        for (i = 0; i < 48; ++i) {
            md5.update(SSLHandshake.pad_2);
        }
        md5.update(md5_bytes);
        md5_bytes = md5.digest();
        sha.update(hs.master_secret);
        for (i = 0; i < 40; ++i) {
            sha.update(SSLHandshake.pad_1);
        }
        byte[] sha_bytes = sha.digest();
        sha.update(hs.master_secret);
        for (i = 0; i < 40; ++i) {
            sha.update(SSLHandshake.pad_2);
        }
        sha.update(sha_bytes);
        sha_bytes = sha.digest();
        byte[] result = new byte[36];
        System.arraycopy(md5_bytes, 0, result, 0, 16);
        System.arraycopy(sha_bytes, 0, result, 16, 20);
        return result;
    }
}

