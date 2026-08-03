/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLPrematureCloseException;
import com.claymoresystems.ptls.SSLuintX;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

class SSLopaque
extends SSLPDU {
    int length;
    byte[] value;

    public SSLopaque(int l) {
        this.length = l;
    }

    public SSLopaque(int l, byte[] v) {
        this.length = l;
        this.value = v;
    }

    public int encode(SSLConn conn, OutputStream s) throws Error, IOException {
        int written = 0;
        if (this.length < 0) {
            SSLuintX lu = new SSLuintX(-this.length, this.value.length);
            SSLDebug.debug(1, "Opaque <" + -1 * this.length + ">" + "length" + this.value.length);
            written = lu.encode(conn, s);
        } else {
            SSLDebug.debug(1, "Opaque [" + this.length + "]");
            if (this.length != this.value.length) {
                throw new Error("Array length doesn't match opaque size");
            }
        }
        s.write(this.value);
        return written += this.value.length;
    }

    public int decode(SSLConn conn, InputStream s) throws IOException {
        int rb;
        int readb = 0;
        if (this.length < 0) {
            SSLuintX lu = new SSLuintX(-this.length);
            readb = lu.decode(conn, s);
            if (lu.value > -this.length) {
                throw new IOException("Opaque length " + lu.value + " > maximum size " + -this.length);
            }
            this.value = new byte[lu.value];
        } else {
            this.value = new byte[this.length];
        }
        int off = 0;
        for (int left = this.value.length; left > 0; left -= rb) {
            rb = s.read(this.value, off, left);
            if (rb < 0) {
                throw new SSLPrematureCloseException("Short read");
            }
            off += rb;
        }
        return readb + this.value.length;
    }

    public void print(SSLConn conn, PrintWriter w) {
        w.print("Opaque ");
        if (this.length < 0) {
            w.print("max (" + -1 * this.length + ")");
        }
        w.println("length " + this.value.length);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

