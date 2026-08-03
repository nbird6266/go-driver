/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseConnection;
import com.oscar.fastpath.FastpathArg;
import com.oscar.jdbc.OscarLob;
import com.oscar.util.OSQLException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;

public class OscarBfile
extends OscarLob {
    public OscarBfile(BaseConnection conn, String locatorStr) throws SQLException {
        super((BaseConnection)((Object)conn.getMasterConnection()), locatorStr);
    }

    public static OscarBfile createByLocator(Connection conn, String locatorStr) throws SQLException {
        BaseConnection oscarConn = null;
        if (!(conn instanceof BaseConnection)) {
            throw new OSQLException("OSCAR-00505", "88888", 505);
        }
        oscarConn = (BaseConnection)((Object)((BaseConnection)((Object)conn)).getMasterConnection());
        return new OscarBfile(oscarConn, locatorStr);
    }

    public InputStream getAsciiStream() throws SQLException {
        return this.getAsciiStream(1L);
    }

    public InputStream getAsciiStream(long pos) throws SQLException {
        return this.getBinaryStream(pos);
    }

    public InputStream getBinaryStream(long pos) throws SQLException {
        return new OscarLob.LobInputStream(this, pos);
    }

    public InputStream getBinaryStream() throws SQLException {
        return this.getBinaryStream(1L);
    }

    public Reader getCharacterStream() throws SQLException {
        return this.getCharacterStream(1L);
    }

    public Reader getCharacterStream(long pos) throws SQLException {
        try {
            return new InputStreamReader(this.getBinaryStream(pos), this.connection.getEncoding().getEncoding());
        }
        catch (UnsupportedEncodingException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public long position(byte[] pattern, long start) throws SQLException {
        return super.positionInternal(pattern, start);
    }

    public long position(OscarBfile pattern, long start) throws SQLException {
        return super.positionInternal(pattern, start);
    }

    public boolean isExists() throws SQLException {
        FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.locator)};
        int i = this.fastpath.getInteger(3, "EXISTS", args);
        return i != 0;
    }

    public String getName() throws SQLException {
        String name = null;
        FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.locator)};
        byte[] data = this.fastpath.getData(3, "GET_NAME", args);
        name = this.connection.getEncoding().decode(data);
        return name;
    }

    public String getDirAlias() throws SQLException {
        String dirAlias = null;
        FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.locator)};
        byte[] data = this.fastpath.getData(3, "GET_NAME", args);
        dirAlias = this.connection.getEncoding().decode(data);
        return dirAlias;
    }

    public long write(long pos, InputStream is, long length) throws SQLException {
        throw new SQLException("not implemented");
    }

    public int getType() {
        return 3;
    }

    public Connection getConnection() {
        return (Connection)((Object)this.connection);
    }
}

