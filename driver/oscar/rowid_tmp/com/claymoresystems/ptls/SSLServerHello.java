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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SSLServerHello
extends SSLPDU {
    SSLuint16 server_version = new SSLuint16();
    SSLopaque random = new SSLopaque(32);
    SSLopaque session_id = new SSLopaque(-32);
    SSLuint16 cipher_suite = new SSLuint16();
    SSLuint8 compression_method = new SSLuint8();

    SSLServerHello() {
    }

    public int decode(SSLConn conn, InputStream s) throws IOException {
        SSLDebug.debug(2, "Decoding server hello");
        int br = 0;
        br += this.server_version.decode(conn, s);
        br += this.random.decode(conn, s);
        br += this.session_id.decode(conn, s);
        br += this.cipher_suite.decode(conn, s);
        return br += this.compression_method.decode(conn, s);
    }

    public int encode(SSLConn conn, OutputStream s) throws IOException {
        SSLDebug.debug(2, "Encoding server hello");
        int br = 0;
        br += this.server_version.encode(conn, s);
        br += this.random.encode(conn, s);
        br += this.session_id.encode(conn, s);
        br += this.cipher_suite.encode(conn, s);
        return br += this.compression_method.encode(conn, s);
    }
}

