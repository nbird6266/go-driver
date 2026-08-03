/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLAlertX;
import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLuint8;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SSLAlert
extends SSLPDU {
    SSLuint8 level = new SSLuint8();
    SSLuint8 description = new SSLuint8();

    public SSLAlert(SSLAlertX alertx) {
        this.level.value = alertx.getLevel();
        this.description.value = alertx.getValue();
    }

    public SSLAlert() {
    }

    public int encode(SSLConn conn, OutputStream s) throws IOException {
        int written = this.level.encode(conn, s);
        return written += this.description.encode(conn, s);
    }

    public int decode(SSLConn conn, InputStream s) throws IOException {
        int readb = this.level.decode(conn, s);
        return readb += this.description.decode(conn, s);
    }
}

