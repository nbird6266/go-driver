/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLRecord;
import java.io.IOException;
import java.io.OutputStream;

class SSLOutputStream
extends OutputStream {
    SSLConn conn;

    public SSLOutputStream(SSLConn c) {
        this.conn = c;
    }

    public void write(int b) throws IOException {
        if (this.conn.invalid) {
            throw new IOException("Can't write to connection where an error has occurred");
        }
        byte[] buf = new byte[]{(byte)(b & 0xFF)};
        this.write(buf, 0, 1);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (this.conn.invalid) {
            throw new IOException("Can't write to connection where an error has occurred");
        }
        SSLDebug.debug(2, "Writing buffer of length " + len);
        while (len > 0) {
            int towrite = len > 16000 ? 16000 : len;
            byte[] tmp_buf = new byte[towrite];
            System.arraycopy(b, off, tmp_buf, 0, towrite);
            SSLRecord r = new SSLRecord(this.conn, 23, tmp_buf);
            r.send(this.conn);
            len -= towrite;
            off += towrite;
        }
        this.conn.sock_out.flush();
    }
}

