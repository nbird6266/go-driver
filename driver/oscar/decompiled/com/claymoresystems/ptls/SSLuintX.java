/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLPrematureCloseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

class SSLuintX
extends SSLPDU {
    int value;
    short size;

    public SSLuintX(short s) {
        this.size = s;
    }

    public SSLuintX(short s, int v) {
        this.size = s;
        this.value = v;
    }

    public SSLuintX(int range) {
        this.size = 0;
        while (range > 0) {
            this.size = (short)(this.size + 1);
            range >>= 8;
        }
    }

    public SSLuintX(int range, int v) {
        this(range);
        this.value = v;
    }

    public int encode(SSLConn conn, OutputStream s) throws Error, IOException {
        SSLConn.debug(1, "Integer size " + this.size + "value " + this.value);
        switch (this.size) {
            case 4: {
                s.write(this.value >> 24 & 0xFF);
            }
            case 3: {
                s.write(this.value >> 16 & 0xFF);
            }
            case 2: {
                s.write(this.value >> 8 & 0xFF);
            }
            case 1: {
                s.write(this.value & 0xFF);
                break;
            }
            default: {
                throw new Error("Bad size for uintX");
            }
        }
        return this.size;
    }

    public int decode(SSLConn conn, InputStream s) throws IOException {
        this.value = 0;
        SSLConn.debug(1, "Reading a " + this.size + "byte integer");
        for (int i = 0; i < this.size; ++i) {
            this.value <<= 8;
            int x = s.read();
            if (x < 0) {
                throw new SSLPrematureCloseException("Short read");
            }
            this.value += x;
            SSLConn.debug(1, "Read byte " + x);
        }
        SSLConn.debug(1, "Read Integer size " + this.size + "value " + this.value);
        return this.size;
    }

    public void print(SSLConn conn, PrintWriter w) {
        w.println("Integer[" + this.size + "] =" + this.value);
    }
}

