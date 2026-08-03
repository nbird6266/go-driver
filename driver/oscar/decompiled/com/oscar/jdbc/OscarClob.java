/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseConnection;
import com.oscar.jdbc.OscarLob;
import com.oscar.util.OSQLException;
import com.oscar.util.StreamHandle;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;

public class OscarClob
extends OscarLob
implements Clob {
    public static OscarClob empty_lob() {
        return new OscarClob();
    }

    public static OscarClob createTemporary(Connection conn, boolean cache, int duration) throws SQLException {
        BaseConnection oscarConn = null;
        if (!(conn instanceof BaseConnection)) {
            throw new OSQLException("OSCAR-00505", "88888", 505);
        }
        oscarConn = (BaseConnection)((Object)((BaseConnection)((Object)conn)).getMasterConnection());
        return new OscarClob(oscarConn, cache, duration);
    }

    public static OscarClob createForTable(Connection conn, long tableOid, int colIndex) throws SQLException {
        BaseConnection oscarConn = null;
        if (!(conn instanceof BaseConnection)) {
            throw new OSQLException("OSCAR-00505", "88888", 505);
        }
        oscarConn = (BaseConnection)((Object)((BaseConnection)((Object)conn)).getMasterConnection());
        return new OscarClob(oscarConn, tableOid, colIndex);
    }

    public static OscarClob createByLocator(Connection conn, String locatorStr) throws SQLException {
        BaseConnection oscarConn = null;
        if (!(conn instanceof BaseConnection)) {
            throw new OSQLException("OSCAR-00505", "88888", 505);
        }
        oscarConn = (BaseConnection)((Object)((BaseConnection)((Object)conn)).getMasterConnection());
        return new OscarClob(oscarConn, locatorStr);
    }

    private OscarClob() {
    }

    private OscarClob(BaseConnection conn, String locatorStr) throws SQLException {
        super(conn, locatorStr);
    }

    private OscarClob(BaseConnection conn, boolean cache, int duration) throws SQLException {
        super(conn, cache, duration);
    }

    private OscarClob(BaseConnection conn, long tableOid, int colIndex) throws SQLException {
        super(conn, tableOid, colIndex);
    }

    public InputStream getAsciiStream() throws SQLException {
        this.checkEmptyLob();
        return this.getAsciiStream(1L);
    }

    public InputStream getAsciiStream(long pos) throws SQLException {
        this.checkEmptyLob();
        return this.getBinaryStream(pos);
    }

    public Reader getCharacterStream() throws SQLException {
        this.checkEmptyLob();
        return this.getCharacterStream(1L);
    }

    public Reader getCharacterStream(long pos) throws SQLException {
        this.checkEmptyLob();
        try {
            return new InputStreamReader(this.getBinaryStream(pos), this.connection.getEncoding().getEncoding());
        }
        catch (UnsupportedEncodingException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public String getSubString(long pos, int length) throws SQLException {
        String ret = null;
        this.checkEmptyLob();
        if (pos <= 0L || length < 0) {
            throw new OSQLException("OSCAR-00502", "88888", 502);
        }
        if (length == 0) {
            ret = "";
        } else {
            try {
                Reader reader = this.getCharacterStream(pos);
                ret = StreamHandle.read(reader, length, this.getBufferSize());
                reader.close();
            }
            catch (IOException ex) {
                throw new OSQLException("OSCAR-00502", "88888", 502, ex);
            }
        }
        return ret;
    }

    public long position(String pattern, long start) throws SQLException {
        long position = -1L;
        if (pattern == null || pattern.length() == 0 || start <= 0L) {
            throw new OSQLException("OSCAR-00502", "88888", 502);
        }
        if (!this.isEmptyLob()) {
            byte[] bytes = this.connection.getEncoding().encode(pattern);
            return super.positionInternal(bytes, start);
        }
        position = -1L;
        return position;
    }

    public long position(Clob pattern, long start) throws SQLException {
        return super.positionInternal((OscarLob)((Object)pattern), start);
    }

    public String toString() {
        String ret = null;
        if (this.isEmptyLob()) {
            ret = "";
        } else {
            try {
                long length = this.length();
                length = length > (long)this.getBufferSize() ? (long)this.getBufferSize() : length;
                ret = this.getSubString(1L, (int)length);
            }
            catch (SQLException e) {
                ret = "";
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    public int setString(long pos, String str) throws SQLException {
        this.checkEmptyLob();
        return this.setString(pos, str, 0, str.length());
    }

    public int setString(long pos, String str, int offset, int len) throws SQLException {
        int writeLen = 0;
        this.checkEmptyLob();
        if (str == null) {
            str = "";
        }
        if (pos <= 0L || str.length() < len || offset < 0 || offset != 0 && offset >= str.length()) {
            throw new OSQLException("OSCAR-00502", "88888", 502);
        }
        try {
            Writer writer = this.setCharacterStream(pos);
            writeLen = StreamHandle.write(writer, str, offset, len, this.getBufferSize());
            writer.close();
        }
        catch (IOException ex) {
            throw new OSQLException("OSCAR-00508", "88888", 508);
        }
        return writeLen;
    }

    public int setBytes(byte[] bytes) throws SQLException {
        this.checkEmptyLob();
        return this.setBytes(1L, bytes, 0, bytes.length);
    }

    public OutputStream setAsciiStream(long pos) throws SQLException {
        this.checkEmptyLob();
        if (pos <= 0L) {
            throw new IllegalArgumentException("wrong parameter");
        }
        return this.setBinaryStream(pos);
    }

    public Writer setCharacterStream(long pos) throws SQLException {
        this.checkEmptyLob();
        OutputStreamWriter ret = null;
        if (pos <= 0L) {
            throw new IllegalArgumentException("wrong parameter");
        }
        OutputStream os = this.setBinaryStream(pos);
        try {
            ret = new OutputStreamWriter(os, this.connection.getEncoding().getEncoding());
        }
        catch (UnsupportedEncodingException ex) {
            throw new OSQLException("OSCAR-00507", "88888", 507, this.connection.getEncoding().getEncoding());
        }
        return ret;
    }

    public Writer setCharacterStream(long pos, int bufferLength) throws SQLException {
        this.checkEmptyLob();
        OutputStreamWriter ret = null;
        if (pos <= 0L) {
            throw new IllegalArgumentException("wrong parameter");
        }
        OutputStream os = this.setBinaryStream(pos, bufferLength);
        try {
            ret = new OutputStreamWriter(os, this.connection.getEncoding().getEncoding());
        }
        catch (UnsupportedEncodingException ex) {
            throw new OSQLException("OSCAR-00507", "88888", 507, this.connection.getEncoding().getEncoding());
        }
        return ret;
    }

    public long write(long pos, Reader reader, long length) throws SQLException {
        long writeLen = 0L;
        if (reader == null) {
            throw new IllegalArgumentException("reader \u4e0d\u80fd\u4e3a null");
        }
        try {
            Writer writer = this.setCharacterStream(pos);
            writeLen = StreamHandle.write(writer, reader, length, this.getBufferSize());
            writer.close();
        }
        catch (IOException ex) {
            throw new OSQLException("OSCAR-00508", "88888", 508);
        }
        return writeLen;
    }

    public long write(long pos, InputStream is, long length) throws SQLException {
        long writeLen = 0L;
        try {
            Writer os = this.setCharacterStream(pos, (int)length);
            InputStreamReader reader = new InputStreamReader(is, "US-ASCII");
            writeLen = StreamHandle.write(os, reader, length, this.getBufferSize());
            os.close();
        }
        catch (IOException ex) {
            throw new OSQLException("OSCAR-00508", "88888", 508);
        }
        return writeLen;
    }

    public int getType() {
        return 2;
    }

    public byte[] getDataInternal(long pos, int length) throws SQLException {
        return super.getDataInternal(pos, length);
    }
}

