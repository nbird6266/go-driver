/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbcx;

import com.oscar.jdbcx.optional.PooledConnectionImpl;
import java.sql.Connection;

public class Jdbc3PooledConnection
extends PooledConnectionImpl {
    public Jdbc3PooledConnection(Connection con, boolean autoCommit) {
        super(con, autoCommit);
    }

    public Jdbc3PooledConnection(Connection con, boolean autoCommit, boolean isXA) {
        super(con, autoCommit, isXA);
    }
}

