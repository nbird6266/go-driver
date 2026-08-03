/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.stream;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xerial.snappy.Snappy;

public class CompressedInputStream
extends FilterInputStream {
    private static final byte compressedTag = -94;
    private static final byte unCompressedTag = -93;
    private byte[] buffer;
    private InputStream in;
    private byte[] packetHeaderBuffer = new byte[3];
    private int pos = 0;
    private BaseConnection con;
    private byte[] lock = new byte[0];
    StringBuffer sb = new StringBuffer();
    boolean logFlag = Driver.getLogLevel() >= 4;

    public CompressedInputStream(InputStream streamFromServer, BaseConnection con) {
        super(streamFromServer);
        this.in = streamFromServer;
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    private void getNextPacketFromServer() throws Exception {
        byte[] uncompressedData = null;
        int lengthRead = this.readFully(this.packetHeaderBuffer, 0, 3);
        if (lengthRead < 3) {
            throw new IOException("Unexpected end of input stream");
        }
        int compressedPacketLength = (this.packetHeaderBuffer[1] & 0xFF) << 8 | this.packetHeaderBuffer[2] & 0xFF;
        if (compressedPacketLength < 0) {
            throw new Exception("\u6570\u636e\u5305\u957f\u5ea6\u4e3a\u8d1f\u503c:" + compressedPacketLength + "----hb:" + this.packetHeaderBuffer[1] + "---lb:" + this.packetHeaderBuffer[2]);
        }
        uncompressedData = new byte[compressedPacketLength];
        this.readFully(uncompressedData, 0, compressedPacketLength);
        try {
            if (this.packetHeaderBuffer[0] == -94) {
                byte[] byArray = this.lock;
                // MONITORENTER : this.lock
                uncompressedData = Snappy.uncompress(uncompressedData);
                // MONITOREXIT : byArray
            }
        }
        catch (Throwable throwable) {
            throw new Exception(throwable);
        }
        if (this.buffer != null && this.pos < this.buffer.length) {
            int remaining = this.buffer.length - this.pos;
            byte[] newBuffer = new byte[remaining + uncompressedData.length];
            int newIndex = this.buffer.length - this.pos;
            System.arraycopy(this.buffer, this.pos, newBuffer, 0, newIndex);
            System.arraycopy(uncompressedData, 0, newBuffer, newIndex, uncompressedData.length);
            uncompressedData = newBuffer;
        }
        this.pos = 0;
        this.buffer = uncompressedData;
        if (!this.logFlag) return;
        this.sb.delete(0, this.sb.length());
        this.sb.append("***********************************************************").append("\n");
        this.sb.append("session: ").append(this.con.getSessionID()).append(", net read: ").append("\n");
        this.sb.append("received Packet Header: ");
        this.append(this.sb, this.packetHeaderBuffer);
        this.sb.append("\n");
        if (this.packetHeaderBuffer[0] == -94) {
            this.sb.append("compressed Packet: ").append(this.packetHeaderBuffer[0]);
        } else {
            this.sb.append("normal Packet: ").append(this.packetHeaderBuffer[0]);
        }
        this.sb.append(", packet Len: ").append(compressedPacketLength).append("\n");
        this.sb.append("received Data: ");
        this.append(this.sb, uncompressedData);
        this.sb.append("\n");
        this.sb.append("***********************************************************").append("\n");
        Driver.writeLog(this.sb.toString());
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
        int bufferLen = this.buffer.length;
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

