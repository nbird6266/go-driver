/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.cert.X509Cert;
import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLopaque;
import com.claymoresystems.ptls.SSLuint8;
import com.claymoresystems.ptls.SSLvector;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

class SSLCertificateRequest
extends SSLPDU {
    SSLvector certificate_types = new SSLvector(-255, new SSLuint8());
    SSLvector distinguished_names = new SSLvector(-65535, new SSLopaque(-65535));

    SSLCertificateRequest() {
    }

    public int encode(SSLConn conn, OutputStream os) throws IOException {
        SSLuint8 cert_type_dss = new SSLuint8(2);
        SSLuint8 cert_type_rsa = new SSLuint8(1);
        this.certificate_types.value.addElement(cert_type_rsa);
        this.certificate_types.value.addElement(cert_type_dss);
        Vector roots = conn.hs.cert_ctx.getRootList();
        for (int i = 0; i < roots.size(); ++i) {
            byte[] root_dn = ((X509Cert)roots.elementAt(i)).getSubjectDER();
            SSLopaque root_opaque = new SSLopaque(-65535);
            root_opaque.value = root_dn;
            this.distinguished_names.value.addElement(root_opaque);
        }
        int wb = this.certificate_types.encode(conn, os);
        return wb += this.distinguished_names.encode(conn, os);
    }

    public int decode(SSLConn conn, InputStream is) throws IOException {
        int rb = this.certificate_types.decode(conn, is);
        return rb += this.distinguished_names.decode(conn, is);
    }
}

