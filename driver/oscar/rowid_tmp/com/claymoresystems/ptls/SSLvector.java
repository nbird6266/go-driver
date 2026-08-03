/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLuintX;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

class SSLvector
extends SSLPDU {
    Vector value;
    SSLPDU prototype;
    int size;

    public SSLvector(int s, Vector v) {
        this.size = s;
        this.value = v;
    }

    public SSLvector(int s, SSLPDU p) {
        this.size = s;
        this.value = new Vector();
        this.prototype = p;
    }

    public int encode(SSLConn conn, OutputStream s) throws IOException {
        ByteArrayOutputStream bs = null;
        OutputStream save = null;
        int byteswritten = 0;
        if (this.size < 0) {
            save = s;
            bs = new ByteArrayOutputStream();
            s = bs;
            SSLConn.debug(1, "Vector, max length " + -1 * this.size);
        }
        int last = this.value.size();
        for (int i = 0; i < last; ++i) {
            SSLPDU el = (SSLPDU)this.value.elementAt(i);
            byteswritten += el.encode(conn, s);
        }
        if (this.size < 0) {
            SSLuintX lu = new SSLuintX(-this.size);
            lu.value = byteswritten;
            byteswritten += lu.encode(conn, save);
            bs.writeTo(save);
            SSLConn.debug(1, "Actual length " + lu.value);
        }
        return byteswritten;
    }

    public int decode(SSLConn conn, InputStream s) throws IOException, Error {
        int length;
        int bytesread = 0;
        if (this.size < 0) {
            SSLuintX lu = new SSLuintX(-this.size);
            bytesread = lu.decode(conn, s);
            length = lu.value;
        } else {
            length = this.size;
        }
        while (length > 0) {
            SSLPDU n;
            try {
                n = (SSLPDU)this.prototype.clone();
            }
            catch (CloneNotSupportedException e) {
                throw new Error("This SHOULD be cloneable...");
            }
            int br = n.decode(conn, s);
            length -= br;
            bytesread += br;
            this.value.addElement(n);
        }
        return bytesread;
    }
}

