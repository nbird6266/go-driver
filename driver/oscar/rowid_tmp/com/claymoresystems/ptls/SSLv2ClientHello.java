/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLopaque;
import com.claymoresystems.ptls.SSLuint16;
import com.claymoresystems.ptls.SSLuint24;
import com.claymoresystems.ptls.SSLvector;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

class SSLv2ClientHello
extends SSLPDU {
    SSLuint16 len = new SSLuint16();
    SSLuint16 client_version = new SSLuint16();
    SSLopaque session_id;
    SSLopaque challenge;
    SSLvector cipher_specs;
    byte[] message_value;

    SSLv2ClientHello() {
    }

    public int decode(SSLConn conn, InputStream s) throws IOException {
        SSLuint16 cs_len = new SSLuint16();
        SSLuint16 sid_len = new SSLuint16();
        SSLuint16 chall_len = new SSLuint16();
        int rb = 0;
        rb += this.len.decode(conn, s);
        byte[] tmp = new byte[this.len.value & Short.MAX_VALUE];
        rb += s.read(tmp);
        ByteArrayInputStream is = new ByteArrayInputStream(tmp);
        int msg_num = is.read();
        this.client_version.decode(conn, is);
        cs_len.decode(conn, is);
        sid_len.decode(conn, is);
        chall_len.decode(conn, is);
        this.cipher_specs = new SSLvector(cs_len.value, new SSLuint24());
        this.cipher_specs.decode(conn, is);
        this.session_id = new SSLopaque(sid_len.value);
        this.session_id.decode(conn, is);
        this.challenge = new SSLopaque(chall_len.value);
        this.challenge.decode(conn, is);
        this.message_value = tmp;
        return rb;
    }
}

