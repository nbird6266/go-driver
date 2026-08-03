/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.util.ColumnData;
import com.oscar.util.ImportStream;
import com.oscar.util.OSQLException;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

public class CharacterData
extends ColumnData {
    private static final int defaultBufferSize = 16384;
    private long length = 0L;
    private Reader in;
    private String charsetName;

    public void preWrite(ImportStream out) throws SQLException {
        out.writeInteger(-2, 2);
    }

    /*
     * Loose catch block
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void endWrite(ImportStream out) throws SQLException {
        if (this.in == null || out == null) {
            throw new OSQLException("Reader handle is invalid", "00804");
        }
        int readLength = 0;
        if (this.length <= 0L) {
            char[] buffer = new char[16384];
            while ((readLength = this.in.read(buffer, 0, 16384)) != -1) {
                this.writeBuffer(out, buffer, readLength);
            }
        } else {
            long remain = this.length;
            int buffersize = 16384;
            char[] buffer = null;
            if (remain < 16384L) {
                buffersize = (int)remain;
                buffer = new char[buffersize];
            } else {
                buffer = new char[16384];
            }
            while ((readLength = this.in.read(buffer, 0, buffersize)) != -1 && remain > 0L) {
                this.writeBuffer(out, buffer, readLength);
                remain -= (long)readLength;
            }
        }
        out.writeInteger(-2, 2);
        Object var8_8 = null;
        if (this.in == null) return;
        try {
            this.in.close();
            return;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return;
        {
            catch (IOException e) {
                throw new OSQLException("OSCAR-00804", "88888", 804, e);
            }
        }
        catch (Throwable throwable) {
            Object var8_9 = null;
            if (this.in == null) throw throwable;
            try {
                this.in.close();
                throw throwable;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            throw throwable;
        }
    }

    public void clear() throws SQLException {
        this.length = 0L;
        this.in = null;
    }

    public void preWriteBlock(ImportStream out) throws SQLException {
    }

    public void read(Reader in, long length, String charsetName) throws SQLException {
        this.checkExpection(in, length);
        this.in = in;
        this.length = length;
        this.charsetName = charsetName;
    }

    public void read(Reader in, String charsetName) throws SQLException {
        this.read(in, 0L, charsetName);
    }

    private void writeBuffer(ImportStream out, char[] buffer, int len) throws SQLException, UnsupportedEncodingException {
        String str = null;
        if (buffer.length == len) {
            str = new String(buffer);
        } else {
            char[] buffer2 = new char[len];
            System.arraycopy(buffer, 0, buffer2, 0, len);
            str = new String(buffer2);
        }
        byte[] bytes = str.getBytes(this.charsetName);
        int remain = bytes.length;
        int startIndex = 0;
        do {
            if (remain > 16384) {
                out.writeInteger(16386, 2);
                out.write(bytes, startIndex, 16384);
                remain -= 16384;
                startIndex += 16384;
                continue;
            }
            out.writeInteger(remain + 2, 2);
            out.write(bytes, startIndex, remain);
            remain -= remain;
            startIndex += remain;
        } while (remain > 0);
    }

    private void checkExpection(Reader in, long length) throws SQLException {
        if (in == null) {
            throw new NullPointerException();
        }
        if (length < 0L) {
            throw new IndexOutOfBoundsException();
        }
    }
}

