/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.xa;

import com.oscar.Driver;
import com.oscar.jdbcx.optional.BaseDataSource;
import com.oscar.xa.Jdbc3XAConnection;
import com.oscar.xa.OSCARXADataSourceFactory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

public class Jdbc3XADataSource
extends BaseDataSource
implements Referenceable,
XADataSource {
    public String getDescription() {
        return "JDBC3 XA-enabled DataSource from " + Driver.getVersion();
    }

    public XAConnection getXAConnection() throws SQLException {
        return this.getXAConnection(this.getUser(), this.getPassword());
    }

    public XAConnection getXAConnection(String user, String password) throws SQLException {
        Connection con = super.getConnection(user, password);
        return new Jdbc3XAConnection(con);
    }

    protected Reference createReference() {
        return new Reference(this.getClass().getName(), OSCARXADataSourceFactory.class.getName(), null);
    }
}

