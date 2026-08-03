/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbcx;

import com.oscar.Driver;
import com.oscar.jdbcx.Jdbc3ObjectFactory;
import com.oscar.jdbcx.Jdbc3PooledConnection;
import com.oscar.jdbcx.optional.ConnectionPool;
import java.sql.SQLException;
import javax.naming.Reference;
import javax.sql.PooledConnection;

public class Jdbc3ConnectionPool
extends ConnectionPool {
    public String getDescription() {
        return "Jdbc3ConnectionPool from " + Driver.getVersion();
    }

    public PooledConnection getPooledConnection() throws SQLException {
        return new Jdbc3PooledConnection(this.getConnection(), this.isDefaultAutoCommit());
    }

    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        return new Jdbc3PooledConnection(this.getConnection(user, password), this.isDefaultAutoCommit());
    }

    protected Reference createReference() {
        return new Reference(this.getClass().getName(), Jdbc3ObjectFactory.class.getName(), null);
    }
}

