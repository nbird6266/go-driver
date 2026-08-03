/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLopaque;
import com.claymoresystems.ptls.SSLvector;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SSLCertificate
extends SSLPDU {
    SSLvector certificate_list = new SSLvector(-16777215, new SSLopaque(-16777215));

    SSLCertificate() {
    }

    public int decode(SSLConn conn, InputStream s) throws IOException {
        return this.certificate_list.decode(conn, s);
    }

    public int encode(SSLConn conn, OutputStream s) throws IOException {
        return this.certificate_list.encode(conn, s);
    }
}

