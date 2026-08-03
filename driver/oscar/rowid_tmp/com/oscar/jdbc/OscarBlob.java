/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.jdbc.OscarLob;
import com.oscar.util.OSQLException;
import com.oscar.util.StreamHandle;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;

public class OscarBlob
extends OscarLob
implements Blob {
    public static OscarBlob empty_lob() {
        return new OscarBlob();
    }

    public static OscarBlob createTemporary(Connection conn, boolean cache, int duration) throws SQLException {
        BaseConnection oscarConn = null;
        if (!(conn instanceof BaseConnection)) {
            throw new OSQLException("OSCAR-00505", "88888", 505);
        }
        oscarConn = (BaseConnection)((Object)((BaseConnection)((Object)conn)).getMasterConnection());
        return new OscarBlob(oscarConn, cache, duration);
    }

    public static OscarBlob createForTable(Connection conn, long tableOid, int colIndex) throws SQLException {
        BaseConnection oscarConn = null;
        if (!(conn instanceof BaseConnection)) {
            throw new OSQLException("OSCAR-00505", "88888", 505);
        }
        oscarConn = (BaseConnection)((Object)((BaseConnection)((Object)conn)).getMasterConnection());
        return new OscarBlob(oscarConn, tableOid, colIndex);
    }

    public static OscarBlob createByLocator(Connection conn, String locatorStr) throws SQLException {
        BaseConnection oscarConn = null;
        if (!(conn instanceof BaseConnection)) {
            throw new OSQLException("OSCAR-00505", "88888", 505);
        }
        oscarConn = (BaseConnection)((Object)((BaseConnection)((Object)conn)).getMasterConnection());
        return new OscarBlob(oscarConn, locatorStr);
    }

    public static void freeTemporary(OscarBlob blob) throws SQLException {
        blob.freeTemporary();
    }

    private OscarBlob() {
    }

    private OscarBlob(BaseConnection conn, String locatorStr) throws SQLException {
        super(conn, locatorStr);
    }

    private OscarBlob(BaseConnection conn, boolean cache, int duration) throws SQLException {
        super(conn, cache, duration);
    }

    private OscarBlob(BaseConnection conn, long tableOid, int colIndex) throws SQLException {
        super(conn, tableOid, colIndex);
    }

    public InputStream getBinaryStream() throws SQLException {
        this.checkEmptyLob();
        return this.getBinaryStream(1L);
    }

    public long position(byte[] pattern, long start) throws SQLException {
        return super.positionInternal(pattern, start);
    }

    public long position(Blob pattern, long start) throws SQLException {
        return super.positionInternal((OscarLob)((Object)pattern), start);
    }

    public int setBytes(long pos, byte[] bytes) throws SQLException {
        int ret = 0;
        if (pos <= 0L || bytes == null) {
            throw new IllegalArgumentException("wrong parameter");
        }
        ret = this.setBytes(pos, bytes, 0, bytes.length);
        return ret;
    }

    public long write(long pos, InputStream is, long length) throws SQLException {
        long writeLen = 0L;
        try {
            OutputStream os = this.setBinaryStream(pos, (int)length);
            writeLen = StreamHandle.write(os, is, length, this.getBufferSize());
            os.close();
        }
        catch (IOException ex) {
            Driver.writeLog(ex);
            throw new OSQLException("OSCAR-00508", "88888", 508);
        }
        return writeLen;
    }

    public int getType() {
        return 1;
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

    public Reader getCharacterStream(long pos) throws SQLException {
        this.checkEmptyLob();
        try {
            return new InputStreamReader(this.getBinaryStream(pos), this.connection.getEncoding().getEncoding());
        }
        catch (UnsupportedEncodingException e) {
            throw new SQLException(e.getMessage());
        }
    }
}

