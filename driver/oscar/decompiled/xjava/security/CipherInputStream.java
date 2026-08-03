/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import xjava.security.Cipher;
import xjava.security.IJCE;

public class CipherInputStream
extends FilterInputStream {
    private static final boolean DEBUG = true;
    private static int debuglevel = IJCE.getDebugLevel("CipherInputStream");
    private static PrintWriter err = IJCE.getDebugOutput();
    private Cipher cipher;
    private final byte[] preallocated1 = new byte[256];
    private final byte[] preallocated2 = new byte[256];
    private final byte[] tempByte = new byte[1];
    private byte[] outBuf;
    private int outPtr;
    private int buffered;
    private boolean isDPBC;

    private static void debug(String s) {
        err.println("CipherInputStream: " + s);
    }

    private static String dump(byte[] b) {
        if (b == null) {
            return "null";
        }
        return b.toString();
    }

    public CipherInputStream(InputStream is, Cipher cipher) {
        super(is);
        if (cipher == null) {
            throw new NullPointerException("cipher");
        }
        int state = cipher.getState();
        if (state != 1 && state != 2) {
            throw new IllegalStateException("cipher is uninitialized");
        }
        this.outBuf = new byte[cipher.getOutputBlockSize()];
        this.buffered = 0;
        this.outPtr = 0;
        this.isDPBC = cipher.isPaddingBlockCipher() && state == 2;
        this.cipher = cipher;
    }

    public synchronized int read(byte[] out, int offset, int length) throws IOException {
        int n;
        byte[] temp;
        int l;
        byte[] in;
        if (debuglevel >= 5) {
            CipherInputStream.debug("read(<" + out + ">, " + offset + ", " + length + ") ...");
        }
        if (this.cipher == null) {
            if (debuglevel >= 7) {
                CipherInputStream.debug("... stream closed");
            }
            return -1;
        }
        if (length <= 0) {
            return 0;
        }
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("offset < 0");
        }
        int k = 0;
        if (this.buffered > 0) {
            k = this.buffered < length ? this.buffered : length;
            System.arraycopy(this.outBuf, this.outPtr, out, offset, k);
            this.outPtr += k;
            this.buffered -= k;
            offset += k;
            length -= k;
            if (debuglevel >= 7) {
                CipherInputStream.debug("  outBuf = <" + CipherInputStream.dump(this.outBuf) + ">, outPtr = " + this.outPtr + ", buffered = " + this.buffered + ", offset = " + offset + ", length = " + length);
            }
            if (this.buffered == 0) {
                this.outPtr = 0;
            }
            if (length == 0) {
                if (debuglevel >= 5) {
                    CipherInputStream.debug("... = " + k);
                }
                return k;
            }
        }
        int inLen = this.cipher.inBufferSize(length);
        if (this.isDPBC) {
            ++inLen;
        }
        byte[] byArray = in = inLen <= this.preallocated1.length ? this.preallocated1 : new byte[inLen];
        if (debuglevel >= 7) {
            CipherInputStream.debug("  inLen = " + inLen);
        }
        if ((l = this.readFully(in, 0, inLen)) < inLen) {
            Cipher _cipher = this.cipher;
            this.cipher = null;
            int tempLen = _cipher.outBufferSizeFinal(l);
            temp = tempLen <= this.preallocated2.length ? this.preallocated2 : new byte[tempLen];
            n = _cipher.crypt(in, 0, l, temp, 0);
        } else {
            int tempLen = this.cipher.outBufferSize(l);
            temp = tempLen <= this.preallocated2.length ? this.preallocated2 : new byte[tempLen];
            n = this.cipher.update(in, 0, l, temp, 0);
        }
        if (debuglevel >= 7) {
            CipherInputStream.debug("  temp = <" + CipherInputStream.dump(temp) + ">, n = " + n);
        }
        if (n > length) {
            this.buffered = n - length;
            if (this.buffered > this.outBuf.length) {
                this.outBuf = new byte[this.buffered];
            }
            System.arraycopy(temp, length, this.outBuf, 0, this.buffered);
            n = length;
            if (debuglevel >= 7) {
                CipherInputStream.debug("  buffered = " + this.buffered + ", length = " + length + ", n = " + n);
            }
        }
        System.arraycopy(temp, 0, out, offset, n);
        if ((n += k) == 0 && this.cipher == null) {
            n = -1;
        }
        if (debuglevel >= 5) {
            CipherInputStream.debug("... = " + n);
        }
        return n;
    }

    private int readFully(byte[] in, int offset, int length) throws IOException {
        int n = 0;
        int k = 0;
        do {
            k = super.read(in, n += k, length - n);
            if (debuglevel < 7) continue;
            CipherInputStream.debug("  n = " + n + ", k = " + k);
        } while (k >= 0 && n < length);
        return n;
    }

    public synchronized int read() throws IOException {
        if (this.read(this.tempByte, 0, 1) < 1) {
            return -1;
        }
        return this.tempByte[0] & 0xFF;
    }

    public synchronized long skip(long n) throws IOException {
        int FUDGE_LEN = 100000;
        int length = n < 100000L ? (int)n : 100000;
        byte[] out = new byte[length];
        long m = n;
        while (m > 0L) {
            if ((length = this.read(out, 0, length)) < 0) {
                return n - m;
            }
            int n2 = length = (m -= (long)length) < 100000L ? (int)m : 100000;
        }
        return n;
    }

    public synchronized int available() throws IOException {
        if (this.cipher == null) {
            return 0;
        }
        return this.buffered + this.cipher.outBufferSize(super.available());
    }

    public synchronized void close() throws IOException {
        this.cipher = null;
        super.close();
    }

    public void mark(int readlimit) {
    }

    public void reset() throws IOException {
        throw new IOException("CipherInputStream does not support mark/reset");
    }

    public boolean markSupported() {
        return false;
    }
}

