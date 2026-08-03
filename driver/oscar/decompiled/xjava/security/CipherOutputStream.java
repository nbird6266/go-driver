/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import xjava.security.Cipher;
import xjava.security.IJCE;

public class CipherOutputStream
extends FilterOutputStream {
    private static final boolean DEBUG = true;
    private static int debuglevel = IJCE.getDebugLevel("CipherOutputStream");
    private static PrintWriter err = IJCE.getDebugOutput();
    private Cipher cipher;
    private final byte[] preallocated = new byte[256];

    private static void debug(String s) {
        err.println("CipherOutputStream: " + s);
    }

    private static String dump(byte[] b) {
        if (b == null) {
            return "null";
        }
        return b.toString();
    }

    public CipherOutputStream(OutputStream os, Cipher cipher) {
        super(os);
        if (cipher == null) {
            throw new NullPointerException("cipher == null");
        }
        int state = cipher.getState();
        if (state != 1 && state != 2) {
            throw new IllegalStateException("cipher is uninitialized");
        }
        this.cipher = cipher;
    }

    public synchronized void write(byte[] in, int offset, int length) throws IOException {
        if (this.cipher == null) {
            throw new IOException("stream closed");
        }
        if (length <= 0) {
            return;
        }
        int outLen = this.cipher.outBufferSize(length);
        byte[] buf = outLen <= this.preallocated.length ? this.preallocated : new byte[outLen];
        int n = this.cipher.update(in, offset, length, buf, 0);
        if (debuglevel >= 7) {
            CipherOutputStream.debug("  buf = <" + CipherOutputStream.dump(buf) + ">, n = " + n);
        }
        int i = 0;
        while (i < n) {
            this.out.write(buf[i]);
            ++i;
        }
    }

    public synchronized void write(int b) throws IOException {
        byte[] ba = new byte[]{(byte)b};
        this.write(ba, 0, 1);
    }

    public synchronized void flush() throws IOException {
        if (this.cipher == null) {
            throw new IOException("stream closed");
        }
        super.flush();
    }

    public synchronized void close() throws IOException {
        if (this.cipher == null) {
            throw new IOException("stream closed");
        }
        int outLen = this.cipher.outBufferSizeFinal(0);
        byte[] buf = outLen <= this.preallocated.length ? this.preallocated : new byte[outLen];
        int n = this.cipher.crypt(new byte[0], 0, 0, buf, 0);
        if (debuglevel >= 7) {
            CipherOutputStream.debug("  buf = <" + CipherOutputStream.dump(buf) + ">, n = " + n);
        }
        int i = 0;
        while (i < n) {
            this.out.write(buf[i]);
            ++i;
        }
        super.flush();
        if (debuglevel >= 5) {
            CipherOutputStream.debug("flushed stream");
        }
        super.close();
        if (debuglevel >= 5) {
            CipherOutputStream.debug("closed stream");
        }
        this.cipher = null;
    }
}

