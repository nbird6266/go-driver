/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLopaque;
import com.claymoresystems.ptls.SSLuint16;
import com.claymoresystems.ptls.SSLuint8;
import com.claymoresystems.ptls.SSLvector;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SSLClientHello
extends SSLPDU {
    SSLuint16 client_version = new SSLuint16();
    SSLopaque random = new SSLopaque(32);
    SSLopaque session_id = new SSLopaque(-32);
    SSLvector cipher_suites;
    SSLvector compression_methods;

    SSLClientHello() {
    }

    public int encode(SSLConn conn, OutputStream s) throws IOException {
        int written = 0;
        SSLDebug.debug(2, "Encoding client hello");
        written = this.client_version.encode(conn, s);
        written += this.random.encode(conn, s);
        written += this.session_id.encode(conn, s);
        written += this.cipher_suites.encode(conn, s);
        return written += this.compression_methods.encode(conn, s);
    }

    public int decode(SSLConn conn, InputStream s) throws IOException {
        int rb = 0;
        SSLDebug.debug(2, "Decoding client hello");
        this.cipher_suites = new SSLvector(-65535, new SSLuint16());
        this.compression_methods = new SSLvector(-255, new SSLuint8());
        rb = this.client_version.decode(conn, s);
        rb += this.random.decode(conn, s);
        rb += this.session_id.decode(conn, s);
        rb += this.cipher_suites.decode(conn, s);
        return rb += this.compression_methods.decode(conn, s);
    }
}

