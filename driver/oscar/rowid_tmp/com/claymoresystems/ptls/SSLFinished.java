/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLAlertX;
import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLHandshake;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLopaque;
import com.claymoresystems.ptls.SSLv3Finished;
import com.claymoresystems.ptls.TLSFinished;
import cryptix.util.core.ArrayUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SSLFinished
extends SSLPDU {
    SSLopaque finished;
    byte[] value;

    public SSLFinished(SSLConn conn, SSLHandshake hs, boolean mine) {
        switch (conn.ssl_version) {
            case 768: {
                this.value = SSLv3Finished.computeFinished(hs, mine);
                break;
            }
            case 769: {
                this.value = TLSFinished.computeFinished(hs, mine);
                break;
            }
            default: {
                throw new Error("Unsupported version");
            }
        }
        this.finished = new SSLopaque(this.value.length);
    }

    public int encode(SSLConn conn, OutputStream s) throws Error, IOException {
        this.finished.value = this.value;
        return this.finished.encode(conn, s);
    }

    public int decode(SSLConn conn, InputStream s) throws IOException {
        int rb = this.finished.decode(conn, s);
        if (!ArrayUtil.areEqual(this.value, this.finished.value)) {
            conn.alert(SSLAlertX.TLS_ALERT_BAD_RECORD_MAC);
        }
        return rb;
    }
}

