/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseConnection;
import com.oscar.core.BaseResultSet;
import com.oscar.core.Field;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.jdbc.OscarResultSet;
import com.oscar.jdbc.OscarStatement;
import com.oscar.jdbc.PreparedInfo;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class OscarPreparedStatement
extends OscarStatement
implements PreparedStatement {
    public OscarPreparedStatement(OscarJdbc2Connection connection, String sql) throws SQLException {
        super((BaseConnection)connection, sql);
        this.setStatementType(1);
    }

    public OscarPreparedStatement(OscarJdbc2Connection connection, PreparedInfo pInfo) throws SQLException {
        super(connection, pInfo);
        this.setStatementType(1);
    }

    public BaseResultSet createResultSet(Field[] fields, List tuples, String status, int updateCount, long insertOID) throws SQLException {
        return new OscarResultSet(this, fields, tuples, status, updateCount, insertOID, this.fetchSize, this.maxrows);
    }

    public void exitImplicitCacheToClose() throws SQLException {
        this.cacheState = (short)3;
        this.hardClose();
    }

    public void exitImplicitCacheToActive() throws SQLException {
        this.cacheState = 1;
        this.isClosed = false;
    }
}

