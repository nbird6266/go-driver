/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLConn;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

interface SSLEncoded {
    public int encode(SSLConn var1, OutputStream var2) throws Error, IOException;

    public int decode(SSLConn var1, InputStream var2) throws IOException;

    public void print(SSLConn var1, PrintWriter var2);
}

