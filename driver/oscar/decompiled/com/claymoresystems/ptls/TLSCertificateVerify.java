/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLHandshake;
import com.claymoresystems.ptls.SSLHandshakeHashes;

class TLSCertificateVerify {
    TLSCertificateVerify() {
    }

    static byte[] computeToBeSigned(SSLHandshake hs, boolean mine) {
        SSLHandshakeHashes hashes = mine ? hs.hashes : hs.save_hashes;
        byte[] md5 = hashes.getMD5Value();
        byte[] sha = hashes.getSHAValue();
        byte[] result = new byte[36];
        System.arraycopy(md5, 0, result, 0, 16);
        System.arraycopy(sha, 0, result, 16, 20);
        return result;
    }
}

