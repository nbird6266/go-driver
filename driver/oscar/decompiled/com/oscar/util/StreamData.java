/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.util.ColumnData;
import com.oscar.util.ImportStream;
import com.oscar.util.OSCARbyte;
import com.oscar.util.OSQLException;
import com.oscar.util.ReaderInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.SQLException;

public class StreamData
extends ColumnData {
    private static final int defaultBufferSize = 16384;
    private int length = 0;
    private InputStream in;
    private boolean toOscarString;

    public void read(InputStream in, int o_length) throws SQLException {
        this.read(in, o_length, false);
    }

    public void read(InputStream in, int o_length, boolean toOscarString) throws SQLException {
        if (in == null) {
            throw new NullPointerException();
        }
        if (o_length < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.in = in;
        this.length = o_length;
        this.toOscarString = toOscarString;
    }

    public void read(InputStream in) throws SQLException {
        this.read(in, false);
    }

    public void read(InputStream in, boolean toOscarString) throws SQLException {
        if (in == null) {
            throw new NullPointerException();
        }
        this.toOscarString = toOscarString;
    }

    public void read(Reader in, int o_length, String connEncoding) throws SQLException {
        if (in == null) {
            throw new NullPointerException();
        }
        if (o_length < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.in = new ReaderInputStream(in, Charset.forName(connEncoding));
        this.length = o_length;
    }

    public void read(Reader in, String connEncoding) throws SQLException {
        if (in == null) {
            throw new NullPointerException();
        }
        this.in = new ReaderInputStream(in, Charset.forName(connEncoding));
    }

    private static byte[] getOscarHexBytes(byte[] bytes) {
        String hexStr = null;
        try {
            hexStr = OSCARbyte.toOSCARString(bytes);
        }
        catch (SQLException sQLException) {
            // empty catch block
        }
        return hexStr.getBytes();
    }

    public void preWrite(ImportStream out) throws SQLException {
        out.writeInteger(-2, 2);
    }

    public void endWrite(ImportStream out) throws SQLException {
        if (this.in == null || out == null) {
            throw new OSQLException("InputStream handle is invalid", "00804");
        }
        try {
            int readLength = 0;
            if (this.length <= 0) {
                byte[] buffer = new byte[16384];
                while ((readLength = this.in.read(buffer, 0, 16384)) != -1) {
                    this.writeBuffer(out, buffer, readLength);
                }
            } else {
                int remain = this.length;
                int buffersize = 16384;
                byte[] buffer = null;
                if (remain < 16384) {
                    buffersize = remain;
                    buffer = new byte[remain];
                } else {
                    buffer = new byte[16384];
                }
                while ((readLength = this.in.read(buffer, 0, buffersize)) != -1 && remain > 0) {
                    this.writeBuffer(out, buffer, readLength);
                    remain -= readLength;
                }
            }
            out.writeInteger(-2, 2);
        }
        catch (IOException e) {
            throw new OSQLException("OSCAR-00804", "88888", 804, e);
        }
        finally {
            if (this.in != null) {
                try {
                    this.in.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeBuffer(ImportStream out, byte[] buffer, int length) throws SQLException {
        if (this.toOscarString) {
            byte[] oldBuffer = new byte[length];
            System.arraycopy(buffer, 0, oldBuffer, 0, length);
            byte[] newBuffer = StreamData.getOscarHexBytes(oldBuffer);
            out.writeInteger(newBuffer.length + 2, 2);
            out.write(newBuffer);
        } else {
            out.writeInteger(length + 2, 2);
            out.write(buffer, 0, length);
        }
    }

    public void clear() throws SQLException {
        this.length = 0;
        this.in = null;
        this.toOscarString = false;
    }

    public void preWriteBlock(ImportStream out) throws SQLException {
    }

    public int getLength() {
        return this.length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}

