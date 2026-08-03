/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbcx.optional;

import com.oscar.Driver;
import com.oscar.jdbcx.optional.BaseDataSource;
import com.oscar.jdbcx.optional.PooledConnectionImpl;
import java.io.Serializable;
import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public class ConnectionPool
extends BaseDataSource
implements Serializable,
ConnectionPoolDataSource {
    private boolean defaultAutoCommit = true;

    public String getDescription() {
        return "ConnectionPoolDataSource from " + Driver.getVersion();
    }

    public PooledConnection getPooledConnection() throws SQLException {
        return new PooledConnectionImpl(this.getConnection(), this.defaultAutoCommit);
    }

    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        return new PooledConnectionImpl(this.getConnection(user, password), this.defaultAutoCommit);
    }

    public boolean isDefaultAutoCommit() {
        return this.defaultAutoCommit;
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }
}

