/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.jdbc.OscarBulkInsertPrepareStatement;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.util.OSQLException;
import com.oscar.util.OscarSqlProcessor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class OscarJdbc2BulkConnection
extends OscarJdbc2Connection
implements BaseConnection,
Connection {
    private int bufferSize = 30;

    public void openConnection(String host, int port, Properties info, String database, String url, Driver driver) throws SQLException {
        super.openConnection(host, port, info, database, url, driver);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (this.isInsertSql(sql)) {
            OscarBulkInsertPrepareStatement s = new OscarBulkInsertPrepareStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            return s;
        }
        return super.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    private boolean isInsertSql(String sql) {
        if (sql == null || sql.length() <= 0) {
            return false;
        }
        try {
            OscarSqlProcessor.ParseResult pr = OscarSqlProcessor.parsing(sql);
            return pr.isInsertSql();
        }
        catch (OSQLException e) {
            String tmpSql = sql.trim().toUpperCase();
            tmpSql = tmpSql.replaceAll("\r\n", " ").replaceAll("\n", " ");
            return tmpSql.matches("^\\s*INSERT\\s+INTO\\s+.+VALUES.+");
        }
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}

