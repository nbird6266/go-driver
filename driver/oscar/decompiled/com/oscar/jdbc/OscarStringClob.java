/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

public class OscarStringClob
implements Clob {
    private String content;

    public OscarStringClob(String content) {
        this.content = content;
    }

    public InputStream getAsciiStream() throws SQLException {
        if (this.content != null) {
            return new ByteArrayInputStream(this.content.getBytes());
        }
        return null;
    }

    public Reader getCharacterStream() throws SQLException {
        if (this.content != null) {
            return new StringReader(this.content);
        }
        return null;
    }

    public long length() throws SQLException {
        return this.content.length();
    }

    public String toString() {
        return this.content;
    }

    public int getBufferSize() throws SQLException {
        return this.content.length();
    }

    public byte[] getDataInternal(long pos, int length) throws SQLException {
        int poss = new Long(pos).intValue();
        return this.content.substring(poss - 1, poss - 1 + length).getBytes();
    }

    public String getSubString(long pos, int length) throws SQLException {
        if (pos < 1L) {
            throw new SQLException("CLOB start position can not be < 1");
        }
        int startPos = (int)pos - 1;
        int endIndex = startPos + length;
        if (this.content != null) {
            if (endIndex > this.content.length()) {
                throw new SQLException("Starting position for search is past end of CLOB");
            }
            return this.content.substring(startPos, endIndex);
        }
        return null;
    }

    public long position(String searchstr, long start) throws SQLException {
        if (start < 1L) {
            throw new SQLException("CLOB start position can not be < 1");
        }
        if (this.content != null) {
            if (start - 1L > (long)this.content.length()) {
                throw new SQLException("Starting position for search is past end of CLOB");
            }
            int pos = this.content.indexOf(searchstr, (int)(start - 1L));
            return pos == -1 ? -1L : (long)(pos + 1);
        }
        return -1L;
    }

    public long position(Clob searchstr, long start) throws SQLException {
        return this.position(searchstr.getSubString(1L, (int)searchstr.length()), start);
    }

    public int setString(long pos, String str) throws SQLException {
        if (pos < 1L) {
            throw new SQLException("CLOB start position can not be < 1");
        }
        if (str == null) {
            throw new SQLException("String to set can not be NULL");
        }
        StringBuilder charBuf = new StringBuilder(this.content);
        int strLength = str.length();
        charBuf.replace((int)(--pos), (int)(pos + (long)strLength), str);
        this.content = charBuf.toString();
        return strLength;
    }

    public int setString(long pos, String str, int offset, int len) throws SQLException {
        if (pos < 1L) {
            throw new SQLException("CLOB start position can not be < 1");
        }
        if (str == null) {
            throw new SQLException("String to set can not be NULL");
        }
        StringBuilder charBuf = new StringBuilder(this.content);
        String replaceString = str.substring(offset, offset + len);
        charBuf.replace((int)(--pos), (int)(pos + (long)replaceString.length()), replaceString);
        this.content = charBuf.toString();
        return len;
    }

    public OutputStream setAsciiStream(long pos) throws SQLException {
        if (pos < 1L) {
            throw new SQLException("CLOB start position can not be < 1");
        }
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        if (pos > 0L) {
            bytesOut.write(this.content.getBytes(), 0, (int)(pos - 1L));
        }
        return bytesOut;
    }

    public Writer setCharacterStream(long pos) throws SQLException {
        if (pos < 1L) {
            throw new SQLException("CLOB start position can not be < 1");
        }
        CharArrayWriter writer = new CharArrayWriter();
        if (pos > 1L) {
            writer.write(this.content, 0, (int)(pos - 1L));
        }
        return writer;
    }

    public void truncate(long length) throws SQLException {
        if (length > (long)this.content.length()) {
            throw new SQLException("Cannot truncate CLOB of length " + this.content.length() + " to length of " + length);
        }
        this.content = this.content.substring(0, (int)length);
    }
}

