/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLCipherState;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLHandshake;
import com.claymoresystems.ptls.SSLMAC;
import com.claymoresystems.util.Util;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class SSLv3MAC
extends SSLMAC {
    SSLv3MAC() {
    }

    public static byte[] calcMAC(SSLCipherState st, int ct, int version, long sequence, byte[] buf) {
        try {
            int i;
            int pad_ct = st.cipher_suite.getDigestOutputLength() == 16 ? 48 : 40;
            SSLDebug.debug(8, "MAC Key", st.mac_key);
            MessageDigest md = MessageDigest.getInstance(st.cipher_suite.getDigestAlg());
            byte[] ct_b = new byte[]{(byte)ct};
            md.update(st.mac_key);
            for (i = 0; i < pad_ct; ++i) {
                md.update(SSLHandshake.pad_1);
            }
            byte[] tmp = Util.toBytes(sequence);
            SSLDebug.debug(8, "Sequence", tmp);
            md.update(tmp);
            md.update(ct_b);
            SSLDebug.debug(8, "Content type", ct_b);
            tmp = Util.toBytes(buf.length, 2);
            SSLDebug.debug(8, "Length", tmp);
            md.update(tmp);
            SSLDebug.debug(8, "Data", buf);
            md.update(buf);
            tmp = md.digest();
            md.update(st.mac_key);
            for (i = 0; i < pad_ct; ++i) {
                md.update(SSLHandshake.pad_2);
            }
            md.update(tmp);
            tmp = md.digest();
            SSLDebug.debug(8, "Computed SSLv3 MAC", tmp);
            return tmp;
        }
        catch (NoSuchAlgorithmException e) {
            throw new Error("Missing algorithm. This shouldn't happen");
        }
    }
}

