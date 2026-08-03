/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.mime;

import cryptix.util.checksum.PRZ24;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Checksum;

public class Base64OutputStream
extends FilterOutputStream {
    private Checksum crc;
    private int crcLength;
    private byte[] inBuf;
    private int inOff;
    private int lineLength;
    private static final int MAX_LINE_LENGTH = 64;
    private static final char[] BASE64_CHARSET = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static final char PADDING = '=';

    public Base64OutputStream(OutputStream os, boolean check) {
        super(os);
        this.init(check ? new PRZ24() : null, 3);
    }

    public Base64OutputStream(OutputStream os) {
        super(os);
        this.init(null, 0);
    }

    public Base64OutputStream(OutputStream os, Checksum checksum, int length) {
        super(os);
        checksum.reset();
        this.init(checksum, length);
    }

    public synchronized void write(int b) throws IOException {
        this.inBuf[this.inOff++] = (byte)b;
        if (this.crc != null) {
            this.crc.update(b);
        }
        if (this.inOff == 3) {
            this.writeQuadruplet();
        }
    }

    public synchronized void write(byte[] b, int offset, int length) throws IOException {
        int i = 0;
        while (i < length) {
            this.write(b[offset++]);
            ++i;
        }
    }

    public synchronized void close() throws IOException {
        this.writePadding();
        if (this.lineLength != 0) {
            this.writeln();
        }
        if (this.crc != null) {
            long cks = this.crc.getValue();
            super.write(61);
            this.crc = null;
            int i = this.crcLength - 1;
            while (i >= 0) {
                this.write((int)(cks >>> i * 8) & 0xFF);
                --i;
            }
            this.writePadding();
            this.writeln();
        }
        super.flush();
        super.close();
    }

    private void init(Checksum checksum, int length) {
        if (length < 0 || length > 8) {
            throw new IllegalArgumentException("length < 0 || length > 8");
        }
        this.inOff = 0;
        this.lineLength = 0;
        this.inBuf = new byte[3];
        this.crc = checksum;
        this.crcLength = length;
    }

    private void writePadding() throws IOException {
        if (this.inOff != 0) {
            int i = this.inOff;
            while (i < 3) {
                this.inBuf[i] = 0;
                ++i;
            }
            this.writeQuadruplet();
        }
    }

    private void writeQuadruplet() throws IOException {
        int c = BASE64_CHARSET[(this.inBuf[0] & 0xFF) >> 2];
        super.write(c);
        c = BASE64_CHARSET[(this.inBuf[0] & 3) << 4 | (this.inBuf[1] & 0xFF) >> 4];
        super.write(c);
        c = this.inOff > 1 ? BASE64_CHARSET[(this.inBuf[1] & 0xF) << 2 | (this.inBuf[2] & 0xCF) >> 6] : 61;
        super.write(c);
        c = this.inOff > 2 ? BASE64_CHARSET[this.inBuf[2] & 0x3F] : 61;
        super.write(c);
        this.inOff = 0;
        this.lineLength += 4;
        if (this.lineLength >= 64) {
            this.writeln();
        }
    }

    private void writeln() throws IOException {
        super.write(13);
        super.write(10);
        this.lineLength = 0;
    }
}

