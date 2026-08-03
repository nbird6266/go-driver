/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.pool;

import com.oscar.core.BaseConnection;
import com.oscar.dispatcher.pool.OSCARObjectFactory;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

public abstract class DispatchBaseDataSource
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
    private int sessionID;

    public Connection getConnection() throws SQLException {
        return this.getConnection(this.user, this.password);
    }

    public Connection getConnection(String user, String password) throws SQLException {
        try {
            Connection con = null;
            if (!this.isSSL) {
                con = DriverManager.getConnection(this.getUrl(), user, password);
                if (this.logger != null) {
                    this.logger.println("Created a non-pooled connection for " + user + " at " + this.getUrl());
                }
            } else {
                if (this.getUser() != null && this.getPassword() != null) {
                    this.info.setProperty("USER", this.getUser());
                    this.info.setProperty("PASSWORD", this.getPassword());
                }
                this.info.setProperty("SSL", "t");
                this.info.setProperty("NOSSLVERSION", "f");
                con = DriverManager.getConnection(this.getUrl(), this.info);
                if (this.logger != null) {
                    this.logger.println("Created a non-pooled connection for " + user + " at " + this.getUrl());
                }
            }
            this.sessionID = ((BaseConnection)((Object)con)).getPlanID();
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
        return 0;
    }

    public void setLoginTimeout(int i) throws SQLException {
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

    public int getSessionID() {
        return this.sessionID;
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

