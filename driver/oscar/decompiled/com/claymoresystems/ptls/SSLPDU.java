/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLEncoded;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

class SSLPDU
implements SSLEncoded,
Cloneable {
    SSLPDU() {
    }

    public int encode(SSLConn conn, OutputStream s) throws Error, IOException {
        throw new NoSuchMethodError("Encode not implemented");
    }

    public int decode(SSLConn conn, InputStream s) throws IOException {
        throw new NoSuchMethodError("Decode not implemented");
    }

    public void print(SSLConn conn, PrintWriter w) {
        throw new NoSuchMethodError("Print not implemented");
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

