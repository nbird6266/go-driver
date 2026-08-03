/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.stream;

import com.oscar.core.BaseConnection;
import com.oscar.util.HdEncrypt;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HdEncryptOutputStream
extends BufferedOutputStream {
    private static int sizeOfPacketLen = 2;
    private static final int K = 1024;
    private static final int defaultBufferSize = 131072;
    private static final int slice = 4096;
    private BaseConnection con;
    private OutputStream out;
    private byte[] buffer;
    private int pos = 0;

    public HdEncryptOutputStream(OutputStream out, BaseConnection con) {
        this(out, con, 131072);
    }

    public HdEncryptOutputStream(OutputStream out, BaseConnection con, int size) {
        super(out);
        this.out = out;
        this.con = con;
        this.buffer = new byte[size];
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (len > this.buffer.length) {
            this.flushBuffer();
            this.writeWithEncrypted(b, off, len);
            return;
        }
        if (this.pos + len > this.buffer.length) {
            this.flushBuffer();
        }
        System.arraycopy(b, off, this.buffer, this.pos, len);
        this.pos += len;
    }

    private void flushBuffer() throws IOException {
        if (this.pos > 0) {
            this.writeWithEncrypted(this.buffer, 0, this.pos);
            this.pos = 0;
        }
    }

    private void writeWithEncrypted(byte[] b, int off, int len) throws IOException {
        int remainLen;
        int offset = off;
        byte[] buf = new byte[4096];
        for (remainLen = len; remainLen > 4096; remainLen -= 4096) {
            System.arraycopy(b, offset, buf, 0, 4096);
            this.sendSlice(buf);
            offset += 4096;
        }
        if (remainLen > 0) {
            buf = new byte[remainLen];
            System.arraycopy(b, offset, buf, 0, remainLen);
            this.sendSlice(buf);
        }
    }

    private void sendSlice(byte[] buf) throws IOException {
        byte[] encryptedData = HdEncrypt.instance().hdSymEncrypt(this.con.getHdSymEncryptKey(), buf);
        int encryptedDataLen = encryptedData.length;
        this.writeInteger(encryptedDataLen, sizeOfPacketLen);
        this.out.write(encryptedData);
    }

    private void writeChar(int c) throws IOException {
        byte[] charBuf = new byte[]{(byte)c};
        this.out.write(charBuf);
    }

    private void writeInteger(int val, int size) throws IOException {
        byte[] integerBuf = new byte[size];
        int count = size;
        while (size-- > 0) {
            integerBuf[size] = (byte)(val & 0xFF);
            val >>= 8;
        }
        this.out.write(integerBuf, 0, count);
    }

    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    public void write(int b) throws IOException {
        byte[] buf = new byte[]{(byte)b};
        this.write(buf);
    }

    public void flush() throws IOException {
        this.flushBuffer();
        this.out.flush();
    }

    public void close() throws IOException {
        this.out.close();
        this.buffer = null;
    }
}

