/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLuint24;
import com.claymoresystems.ptls.SSLuint8;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SSLHandshakeHdr
extends SSLPDU {
    SSLuint8 ct = new SSLuint8();
    SSLuint24 length = new SSLuint24();

    public SSLHandshakeHdr() {
    }

    public SSLHandshakeHdr(int msgtype, int size) {
        this.ct.value = msgtype;
        this.length.value = size;
    }

    public int encode(SSLConn conn, OutputStream s) throws Error, IOException {
        SSLDebug.debug(2, "Encoding handshake header");
        int written = 0;
        written = this.ct.encode(conn, s);
        return written += this.length.encode(conn, s);
    }

    public int decode(SSLConn conn, InputStream s) throws IOException {
        SSLDebug.debug(2, "Decoding handshake header");
        int rb = 0;
        rb = this.ct.decode(conn, s);
        return rb += this.length.decode(conn, s);
    }
}

