/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.mime;

import cryptix.util.checksum.ChecksumException;
import cryptix.util.checksum.PRZ24;
import java.io.CharConversionException;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Checksum;

public class Base64InputStream
extends FilterInputStream {
    private boolean finished;
    private Checksum crc;
    private int crcLength;
    private byte[] inBuf = new byte[4];
    private byte[] outBuf = new byte[3];
    private int inOff;
    private int outOff;
    private int outBufMax;
    static final int CONV_WHITE = -1;
    static final int CONV_PAD = -2;
    static final int CONV_OTHER = -3;

    public Base64InputStream(InputStream is, boolean check) {
        super(is);
        this.init(check ? new PRZ24() : null, 3);
    }

    public Base64InputStream(InputStream is) {
        super(is);
        this.init(null, 0);
    }

    public Base64InputStream(InputStream is, Checksum checksum, int length) {
        super(is);
        checksum.reset();
        this.init(checksum, length);
    }

    public synchronized void close() throws IOException {
        this.finished = true;
        this.outOff = 0;
        super.close();
    }

    public synchronized int read() throws IOException {
        if (this.outOff == 0) {
            if (this.finished) {
                return -1;
            }
            int inByte = 0;
            int n = -1;
            while (n == -1) {
                inByte = this.in.read();
                if (inByte < 0) {
                    return -1;
                }
                n = this.toNumber(inByte);
            }
            if (n < 0) {
                if (this.crc == null || n == -3) {
                    throw new CharConversionException();
                }
                long computed = this.crc.getValue();
                long actual = 0L;
                this.crc = null;
                int i = 0;
                while (i < this.crcLength) {
                    inByte = this.read();
                    if (inByte < 0) {
                        throw new EOFException();
                    }
                    actual = actual << 8 | (long)inByte;
                    ++i;
                }
                this.finished = true;
                this.outOff = 0;
                if (actual != computed) {
                    throw new ChecksumException();
                }
                return -1;
            }
            int i = 0;
            while (i < 4) {
                if (n == -2) {
                    if (i < 2) {
                        throw new CharConversionException();
                    }
                } else {
                    if (n < 0) {
                        throw new CharConversionException();
                    }
                    this.inBuf[this.inOff++] = (byte)n;
                }
                if (i != 3) {
                    inByte = this.in.read();
                    if (inByte < 0) {
                        throw new EOFException();
                    }
                    n = this.toNumber(inByte);
                }
                ++i;
            }
            this.writeTriplet();
        }
        int b = this.outBuf[this.outOff++] & 0xFF;
        if (this.outOff == this.outBufMax) {
            this.outOff = 0;
        }
        if (this.crc != null) {
            this.crc.update(b);
        }
        return b;
    }

    public synchronized int read(byte[] buffer, int offset, int length) throws IOException {
        int i = 0;
        while (i < length) {
            int c = this.read();
            if (c < 0) {
                return i == 0 ? -1 : i;
            }
            buffer[offset++] = (byte)c;
            ++i;
        }
        return length;
    }

    public synchronized long skip(long n) throws IOException {
        long i = 0L;
        while (i < n) {
            if (this.read() < 0) {
                return i;
            }
            ++i;
        }
        return n;
    }

    public synchronized int available() throws IOException {
        return this.outOff == 0 ? 0 : this.outBufMax - this.outOff;
    }

    public void mark(int readlimit) {
    }

    public void reset() throws IOException {
        throw new IOException("Base64InputStream does not support mark/reset");
    }

    public boolean markSupported() {
        return false;
    }

    private void init(Checksum checksum, int length) {
        if (length < 0 || length > 8) {
            throw new IllegalArgumentException("length < 0 || length > 8");
        }
        this.outBufMax = 3;
        this.outOff = 0;
        this.inOff = 0;
        this.finished = false;
        this.crc = checksum;
        this.crcLength = length;
    }

    private void writeTriplet() {
        this.outBufMax = 0;
        this.outBuf[this.outBufMax++] = (byte)(this.inBuf[0] << 2 | this.inBuf[1] >>> 4);
        if (this.inOff > 2) {
            this.outBuf[this.outBufMax++] = (byte)(this.inBuf[1] << 4 | this.inBuf[2] >>> 2);
        }
        if (this.inOff > 3) {
            this.outBuf[this.outBufMax++] = (byte)(this.inBuf[2] << 6 | this.inBuf[3]);
        }
        this.inOff = 0;
    }

    private int toNumber(int inByte) {
        if (inByte >= 97 & inByte <= 122) {
            return inByte - 97 + 26;
        }
        if (inByte >= 65 & inByte <= 90) {
            return inByte - 65;
        }
        if (inByte >= 48 & inByte <= 57) {
            return inByte - 48 + 52;
        }
        if (inByte == 43) {
            return 62;
        }
        if (inByte == 47) {
            return 63;
        }
        if (inByte == 61) {
            return -2;
        }
        if (inByte == 10 || inByte == 13 || inByte == 32 || inByte == 9) {
            return -1;
        }
        return -3;
    }
}

