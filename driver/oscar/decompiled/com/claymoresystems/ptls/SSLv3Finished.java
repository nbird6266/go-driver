/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLHandshake;
import com.claymoresystems.ptls.SSLHandshakeHashes;
import java.security.MessageDigest;

class SSLv3Finished {
    static byte[] cli = new byte[]{67, 76, 78, 84};
    static byte[] ser = new byte[]{83, 82, 86, 82};

    SSLv3Finished() {
    }

    static byte[] computeFinished(SSLHandshake hs, boolean mine) {
        int i;
        SSLHandshakeHashes hashes = mine ? hs.hashes : hs.save_hashes;
        MessageDigest md5 = hashes.getMD5Digest();
        MessageDigest sha = hashes.getSHADigest();
        byte[] output = new byte[36];
        byte[] Sender = hs.client ^ mine ? ser : cli;
        md5.update(Sender);
        md5.update(hs.master_secret);
        for (i = 0; i < 48; ++i) {
            md5.update(SSLHandshake.pad_1);
        }
        byte[] tmp = md5.digest();
        md5.update(hs.master_secret);
        for (i = 0; i < 48; ++i) {
            md5.update(SSLHandshake.pad_2);
        }
        md5.update(tmp);
        tmp = md5.digest();
        System.arraycopy(tmp, 0, output, 0, tmp.length);
        SSLDebug.debug(8, "MD5 handshake hash", tmp);
        sha.update(Sender);
        sha.update(hs.master_secret);
        for (i = 0; i < 40; ++i) {
            sha.update(SSLHandshake.pad_1);
        }
        tmp = sha.digest();
        sha.update(hs.master_secret);
        for (i = 0; i < 40; ++i) {
            sha.update(SSLHandshake.pad_2);
        }
        sha.update(tmp);
        tmp = sha.digest();
        System.arraycopy(tmp, 0, output, 16, tmp.length);
        SSLDebug.debug(8, "SHA handshake hash", tmp);
        return output;
    }
}

