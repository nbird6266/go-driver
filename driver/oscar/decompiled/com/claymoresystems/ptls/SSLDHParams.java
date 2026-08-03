/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.crypto.DHPrivateKey;
import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLopaque;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SSLDHParams
extends SSLPDU {
    SSLopaque DH_p = new SSLopaque(-65535);
    SSLopaque DH_g = new SSLopaque(-65535);
    SSLopaque DH_Ys = new SSLopaque(-65535);

    public SSLDHParams() {
    }

    public SSLDHParams(DHPrivateKey key) {
        this.DH_p.value = key.getpBytes();
        this.DH_g.value = key.getgBytes();
        this.DH_Ys.value = key.getYBytes();
    }

    public int decode(SSLConn conn, InputStream s) throws Error, IOException {
        int r = 0;
        r = this.DH_p.decode(conn, s);
        r += this.DH_g.decode(conn, s);
        return r += this.DH_Ys.decode(conn, s);
    }

    public int encode(SSLConn conn, OutputStream s) throws IOException {
        int r = 0;
        r = this.DH_p.encode(conn, s);
        r += this.DH_g.encode(conn, s);
        return r += this.DH_Ys.encode(conn, s);
    }
}

