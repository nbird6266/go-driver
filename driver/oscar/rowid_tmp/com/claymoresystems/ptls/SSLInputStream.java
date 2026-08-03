/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLRecord;
import com.claymoresystems.ptls.SSLRecordReader;
import com.claymoresystems.util.Silo;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

class SSLInputStream
extends InputStream {
    Vector d = new Vector();
    SSLRecordReader rdr;
    Silo silo = new Silo(1024);

    public SSLInputStream(SSLRecordReader r) {
        this.rdr = r;
    }

    public void write(SSLRecord r) {
        this.silo.write(r.data.value);
    }

    public int read() throws IOException {
        int rv;
        if (this.rdr.conn.invalid) {
            throw new IOException("Can't read from a connection where an error has occurred");
        }
        do {
            int r;
            if ((r = this.silo.read()) >= 0) {
                return r;
            }
            SSLDebug.debug(1, "Silo empty, reading a record" + r);
        } while ((rv = this.rdr.readRecord()) != -1);
        return -1;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int rv;
        if (this.rdr.conn.invalid) {
            throw new IOException("Can't read from a connection where an error has occurred");
        }
        do {
            int br;
            if ((br = this.silo.read(b, off, len)) >= 0) {
                return br;
            }
            SSLDebug.debug(1, "Silo empty, reading a record");
        } while ((rv = this.rdr.readRecord()) != -1);
        return -1;
    }

    public int available() throws IOException {
        return this.silo.bytesAvailable();
    }
}

