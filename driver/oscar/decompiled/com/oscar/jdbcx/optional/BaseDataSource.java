/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbcx.optional;

import com.oscar.jdbcx.optional.OSCARObjectFactory;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

public abstract class BaseDataSource
implements Referenceable {
    private transient PrintWriter logger;
    private String serverName = "localhost";
    private String databaseName;
    private String user;
    private boolean isSSL = false;
    private Properties info;
    private String password;
    private int portNumber;
    private String url;

    public Connection getConnection() throws SQLException {
        return this.getConnection(this.user, this.password);
    }

    public Connection getConnection(String user, String password) throws SQLException {
        try {
            Connection con = null;
            if (this.info == null) {
                this.info = new Properties();
            }
            if (user != null) {
                this.info.setProperty("USER", user);
            }
            if (password != null) {
                this.info.setProperty("PASSWORD", password);
            }
            if (this.isSSL) {
                this.info.setProperty("SSL", "t");
                this.info.setProperty("NOSSLVERSION", "f");
            }
            con = DriverManager.getConnection(this.getUrl(), this.info);
            if (this.logger != null) {
                this.logger.println("Created a non-pooled connection for " + user + " at " + this.getUrl());
            }
            return con;
        }
        catch (SQLException e) {
            if (this.logger != null) {
                this.logger.println("Failed to create a non-pooled connection for " + user + " at " + this.getUrl() + ": " + e);
            }
            throw e;
        }
    }

    public int getLoginTimeout() throws SQLException {
        throw new SQLException("This DataSource does not support a configurable login timeout.");
    }

    public void setLoginTimeout(int i) throws SQLException {
        throw new SQLException("This DataSource does not support a configurable login timeout.");
    }

    public PrintWriter getLogWriter() throws SQLException {
        return this.logger;
    }

    public void setLogWriter(PrintWriter printWriter) throws SQLException {
        this.logger = printWriter;
    }

    public String getServerName() {
        return this.serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName == null || serverName.equals("") ? "localhost" : serverName;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public abstract String getDescription();

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPortNumber() {
        return this.portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public void setSSL(boolean useSSL) {
        this.isSSL = useSSL;
    }

    public void setPropertity(Properties p) {
        this.info = p;
    }

    public String getUrl() {
        if (this.url != null && !"".equals(this.url)) {
            return this.url;
        }
        return "jdbc:oscar://" + this.serverName + (this.portNumber == 0 ? "" : ":" + this.portNumber) + "/" + this.databaseName;
    }

    public Properties getPropertity() {
        return this.info;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    protected Reference createReference() {
        return new Reference(this.getClass().getName(), OSCARObjectFactory.class.getName(), null);
    }

    public Reference getReference() throws NamingException {
        Reference ref = this.createReference();
        ref.add(new StringRefAddr("serverName", this.serverName));
        if (this.portNumber != 0) {
            ref.add(new StringRefAddr("portNumber", Integer.toString(this.portNumber)));
        }
        ref.add(new StringRefAddr("databaseName", this.databaseName));
        if (this.user != null) {
            ref.add(new StringRefAddr("user", this.user));
        }
        if (this.password != null) {
            ref.add(new StringRefAddr("password", this.password));
        }
        return ref;
    }

    static {
        try {
            Class.forName("com.oscar.Driver");
        }
        catch (ClassNotFoundException e) {
            System.err.println("oscar DataSource unable to load oscar JDBC Driver");
        }
    }
}

