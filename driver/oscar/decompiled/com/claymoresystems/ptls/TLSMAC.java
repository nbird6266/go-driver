/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLCipherState;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLMAC;
import com.claymoresystems.util.Util;
import java.security.MessageDigest;
import xjava.security.Parameterized;

class TLSMAC
extends SSLMAC {
    TLSMAC() {
    }

    public static byte[] calcMAC(SSLCipherState st, int ct, int version, long sequence, byte[] buf) {
        MessageDigest md;
        SSLDebug.debug(8, "MAC Key", st.mac_key);
        try {
            md = MessageDigest.getInstance("HMAC-" + st.cipher_suite.getDigestAlg());
            ((Parameterized)((Object)md)).setParameter("key", st.mac_key);
        }
        catch (Exception e) {
            throw new InternalError(e.toString());
        }
        byte[] tmp = Util.toBytes(sequence);
        SSLDebug.debug(8, "Sequence", tmp);
        md.update(tmp);
        byte[] ct_b = new byte[]{(byte)ct};
        md.update(ct_b);
        SSLDebug.debug(8, "Content type", ct_b);
        tmp = Util.toBytes(version, 2);
        SSLDebug.debug(8, "Version", tmp);
        md.update(tmp);
        tmp = Util.toBytes(buf.length, 2);
        SSLDebug.debug(8, "Length", tmp);
        md.update(tmp);
        SSLDebug.debug(8, "Data", buf);
        md.update(buf);
        tmp = md.digest();
        SSLDebug.debug(8, "Computed TLS MAC", tmp);
        return tmp;
    }
}

