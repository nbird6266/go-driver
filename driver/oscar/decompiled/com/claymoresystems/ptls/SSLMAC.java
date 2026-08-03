/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLCipherState;
import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLv3MAC;
import com.claymoresystems.ptls.TLSMAC;

class SSLMAC {
    SSLMAC() {
    }

    public static byte[] calcMAC(SSLConn conn, SSLCipherState st, int contentType, long sequenceNum, byte[] buf) {
        byte[] value;
        switch (conn.ssl_version) {
            case 768: {
                value = SSLv3MAC.calcMAC(st, contentType, conn.ssl_version, sequenceNum, buf);
                break;
            }
            case 769: {
                value = TLSMAC.calcMAC(st, contentType, conn.ssl_version, sequenceNum, buf);
                break;
            }
            default: {
                throw new Error("TLS version " + conn.ssl_version + "not supported");
            }
        }
        return value;
    }
}

