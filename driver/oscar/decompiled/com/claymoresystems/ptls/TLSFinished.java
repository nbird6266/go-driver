/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLHandshake;
import com.claymoresystems.ptls.SSLHandshakeHashes;
import com.claymoresystems.ptls.TLSPRF;

class TLSFinished {
    TLSFinished() {
    }

    static byte[] computeFinished(SSLHandshake hs, boolean mine) {
        SSLHandshakeHashes hashes = mine ? hs.hashes : hs.save_hashes;
        TLSPRF prf = new TLSPRF();
        byte[] md5 = hashes.getMD5Value();
        byte[] sha = hashes.getSHAValue();
        byte[] verify_data = new byte[12];
        prf.PRF(hs.master_secret, hs.client ^ mine ? 8 : 7, md5, sha, verify_data);
        return verify_data;
    }
}

