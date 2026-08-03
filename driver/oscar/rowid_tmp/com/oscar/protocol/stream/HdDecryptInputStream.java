/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.stream;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.util.HdEncrypt;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HdDecryptInputStream
extends FilterInputStream {
    private static final int sizeOfPacketLen = 2;
    private byte[] buffer;
    private InputStream in;
    private byte[] packetLengthBuffer = new byte[2];
    private int pos = 0;
    private BaseConnection con;
    boolean logFlag = Driver.getLogLevel() >= 4;

    protected HdDecryptInputStream(InputStream in, BaseConnection con) {
        super(in);
        this.in = in;
        this.con = con;
    }

    public int available() throws IOException {
        if (this.buffer == null) {
            return this.in.available();
        }
        return this.buffer.length - this.pos + this.in.available();
    }

    public void close() throws IOException {
        this.in.close();
        this.buffer = null;
    }

    private void getNextPacketFromServer() throws IOException {
        byte[] decryptedData = null;
        int lengthRead = this.readFully(this.packetLengthBuffer, 0, 2);
        if (lengthRead < 2) {
            throw new IOException("Unexpected end of input stream");
        }
        int encryptedPacketLength = (this.packetLengthBuffer[0] & 0xFF) << 8 | this.packetLengthBuffer[1] & 0xFF;
        if (encryptedPacketLength < 0) {
            throw new IOException("\u6570\u636e\u5305\u957f\u5ea6\u4e3a\u8d1f\u503c:" + encryptedPacketLength + "----hb:" + this.packetLengthBuffer[0] + "---lb:" + this.packetLengthBuffer[1]);
        }
        decryptedData = new byte[encryptedPacketLength];
        this.readFully(decryptedData, 0, encryptedPacketLength);
        decryptedData = this.decryptData(this.con.getHdSymEncryptKey(), decryptedData);
        if (this.buffer != null && this.pos < this.buffer.length) {
            int remaining = this.buffer.length - this.pos;
            byte[] newBuffer = new byte[remaining + decryptedData.length];
            int newIndex = this.buffer.length - this.pos;
            System.arraycopy(this.buffer, this.pos, newBuffer, 0, newIndex);
            System.arraycopy(decryptedData, 0, newBuffer, newIndex, decryptedData.length);
            decryptedData = newBuffer;
        }
        this.pos = 0;
        this.buffer = decryptedData;
    }

    private byte[] decryptData(byte[] pstKey, byte[] data) {
        byte[] res = HdEncrypt.instance().hdSymDecrypt(pstKey, data);
        return res;
    }

    private void getNextPacketIfRequired(int numBytes) throws Exception {
        if (this.buffer == null || this.pos + numBytes > this.buffer.length) {
            this.getNextPacketFromServer();
        }
    }

    public synchronized int read() throws IOException {
        try {
            this.getNextPacketIfRequired(1);
        }
        catch (IOException ioEx) {
            return -1;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return this.buffer[this.pos++] & 0xFF;
    }

    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len <= 0) {
            return 0;
        }
        try {
            this.getNextPacketIfRequired(len);
        }
        catch (IOException ioEx) {
            return -1;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        int bufferLen = this.buffer.length - this.pos;
        if (bufferLen < len) {
            System.arraycopy(this.buffer, this.pos, b, off, bufferLen);
            this.pos += bufferLen;
            return bufferLen;
        }
        System.arraycopy(this.buffer, this.pos, b, off, len);
        this.pos += len;
        return len;
    }

    private final int readFully(byte[] b, int off, int len) throws IOException {
        int n;
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int count = 0;
        for (n = 0; n < len; n += count) {
            count = this.in.read(b, off + n, len - n);
            if (count >= 0) continue;
            throw new EOFException();
        }
        return n;
    }

    public long skip(long n) throws IOException {
        long count = 0L;
        int bytesRead = 0;
        for (long i = 0L; i < n && (bytesRead = this.read()) != -1; ++i) {
            ++count;
        }
        return count;
    }

    private void append(StringBuffer sb, byte[] value) {
        if (value == null) {
            sb.append("null");
        } else {
            for (int i = 0; i < value.length; ++i) {
                sb.append(value[i]).append(" ");
            }
        }
    }
}

