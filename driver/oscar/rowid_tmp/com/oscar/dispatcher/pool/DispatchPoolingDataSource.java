/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.pool;

import com.oscar.Driver;
import com.oscar.dispatcher.pool.DispatchBaseDataSource;
import com.oscar.dispatcher.pool.DispatchConnectionPool;
import com.oscar.dispatcher.pool.DispatchPooledConnectionImpl;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public class DispatchPoolingDataSource
extends DispatchBaseDataSource
implements ConnectionPoolDataSource {
    private static Map dataSources = new HashMap();
    protected String dataSourceName;
    private int initialConnections = 0;
    private int maxConnections = 0;
    private boolean initialized = false;
    private ConcurrentHashMap available = new ConcurrentHashMap();
    private ConcurrentHashMap used = new ConcurrentHashMap();
    private Object lock = new Object();
    private DispatchConnectionPool source;
    private ConnectionEventListener connectionEventListener = new ConnectionEventListener(){

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void connectionClosed(ConnectionEvent event) {
            ((PooledConnection)event.getSource()).removeConnectionEventListener(this);
            Object object = DispatchPoolingDataSource.this.lock;
            synchronized (object) {
                if (DispatchPoolingDataSource.this.available == null) {
                    return;
                }
                boolean removed = DispatchPoolingDataSource.this.used.remove(new Integer(((DispatchPooledConnectionImpl)event.getSource()).getSessionID()), event.getSource());
                if (removed) {
                    DispatchPoolingDataSource.this.available.put(new Integer(((DispatchPooledConnectionImpl)event.getSource()).getSessionID()), event.getSource());
                    DispatchPoolingDataSource.this.lock.notify();
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void connectionErrorOccurred(ConnectionEvent event) {
            ((PooledConnection)event.getSource()).removeConnectionEventListener(this);
            Object object = DispatchPoolingDataSource.this.lock;
            synchronized (object) {
                if (DispatchPoolingDataSource.this.available == null) {
                    return;
                }
                DispatchPoolingDataSource.this.used.remove(event.getSource());
                DispatchPoolingDataSource.this.lock.notify();
            }
        }
    };

    static DispatchPoolingDataSource getDataSource(String name) {
        return (DispatchPoolingDataSource)dataSources.get(name);
    }

    public String getDescription() {
        return "Pooling DataSource '" + this.dataSourceName + " from " + Driver.getVersion();
    }

    public void setServerName(String serverName) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setServerName(serverName);
    }

    public void setDatabaseName(String databaseName) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setDatabaseName(databaseName);
    }

    public void setUser(String user) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setUser(user);
    }

    public void setPassword(String password) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setPassword(password);
    }

    public void setPortNumber(int portNumber) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setPortNumber(portNumber);
    }

    public int getInitialConnections() {
        return this.initialConnections;
    }

    public void setInitialConnections(int initialConnections) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        this.initialConnections = initialConnections;
    }

    public int getMaxConnections() {
        return this.maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        this.maxConnections = maxConnections;
    }

    public String getDataSourceName() {
        return this.dataSourceName;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setDataSourceName(String dataSourceName) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        if (this.dataSourceName != null && dataSourceName != null && dataSourceName.equals(this.dataSourceName)) {
            return;
        }
        Map map = dataSources;
        synchronized (map) {
            if (DispatchPoolingDataSource.getDataSource(dataSourceName) != null) {
                throw new IllegalArgumentException("DataSource with name '" + dataSourceName + "' already exists!");
            }
            if (this.dataSourceName != null) {
                dataSources.remove(this.dataSourceName);
            }
            this.dataSourceName = dataSourceName;
            dataSources.put(dataSourceName, this);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void initialize() throws SQLException {
        Object object = this.lock;
        synchronized (object) {
            this.source = this.createConnectionPool();
            this.source.setDatabaseName(this.getDatabaseName());
            this.source.setPassword(this.getPassword());
            this.source.setPortNumber(this.getPortNumber());
            this.source.setServerName(this.getServerName());
            this.source.setUser(this.getUser());
            this.source.setUrl(this.getUrl());
            PooledConnection pConnection = null;
            while (this.available.size() < this.initialConnections) {
                pConnection = this.source.getPooledConnection();
                this.available.put(new Integer(((DispatchPooledConnectionImpl)pConnection).getSessionID()), pConnection);
            }
            this.initialized = true;
        }
    }

    protected boolean isInitialized() {
        return this.initialized;
    }

    protected DispatchConnectionPool createConnectionPool() {
        return new DispatchConnectionPool();
    }

    public Connection getConnection(String user, String password) throws SQLException {
        if (user == null || user.equals(this.getUser()) && (password == null && this.getPassword() == null || password != null && password.equals(this.getPassword()))) {
            return this.getConnection();
        }
        if (!this.initialized) {
            this.initialize();
        }
        return super.getConnection(user, password);
    }

    public Connection getConnection() throws SQLException {
        if (!this.initialized) {
            this.initialize();
        }
        return this.getPooledConnection().getConnection();
    }

    public Connection getConnection(int sessionID) throws SQLException {
        if (!this.initialized) {
            this.initialize();
        }
        if (sessionID == -1) {
            return this.getPooledConnection().getConnection();
        }
        return this.getPooledConnection(sessionID).getConnection();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() {
        Object object = this.lock;
        synchronized (object) {
            DispatchPooledConnectionImpl pci;
            Set keySet = this.available.keySet();
            for (Integer key : keySet) {
                pci = (DispatchPooledConnectionImpl)this.available.get(key);
                try {
                    pci.close();
                }
                catch (SQLException e) {}
            }
            this.available = null;
            keySet = this.used.keySet();
            for (Integer key : keySet) {
                pci = (DispatchPooledConnectionImpl)this.used.get(key);
                try {
                    pci.close();
                }
                catch (SQLException e) {}
            }
            this.used = null;
        }
        this.removeStoredDataSource();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void removeStoredDataSource() {
        Map map = dataSources;
        synchronized (map) {
            dataSources.remove(this.dataSourceName);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PooledConnection getPooledConnection() throws SQLException {
        PooledConnection pc = null;
        Object object = this.lock;
        synchronized (object) {
            if (!this.initialized) {
                this.initialize();
                return this.getPooledConnection();
            }
            if (this.available == null) {
                throw new SQLException("DataSource has been closed.");
            }
            while (true) {
                if (this.available.size() > 0) {
                    Set keySet = this.available.keySet();
                    Iterator keyIte = keySet.iterator();
                    Integer key = new Integer(-1);
                    if (keyIte.hasNext()) {
                        key = keyIte.next();
                    }
                    if ((pc = (PooledConnection)this.available.remove(key)).getConnection() == null) {
                        pc = this.source.getPooledConnection();
                    }
                    this.used.put(new Integer(((DispatchPooledConnectionImpl)pc).getSessionID()), pc);
                    break;
                }
                if (this.maxConnections == 0 || this.used.size() < this.maxConnections) {
                    pc = this.source.getPooledConnection();
                    this.used.put(new Integer(((DispatchPooledConnectionImpl)pc).getSessionID()), pc);
                    break;
                }
                try {
                    this.lock.wait(1000L);
                }
                catch (InterruptedException interruptedException) {}
            }
        }
        pc.addConnectionEventListener(this.connectionEventListener);
        return pc;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public PooledConnection getPooledConnection(int sessionID) throws SQLException {
        PooledConnection pc = null;
        Object object = this.lock;
        synchronized (object) {
            if (!this.initialized) {
                this.initialize();
                return this.getPooledConnection();
            }
            if (this.available == null) {
                throw new SQLException("DataSource has been closed.");
            }
            while (true) {
                if (this.available.size() > 0) {
                    pc = (PooledConnection)this.available.remove(new Integer(sessionID));
                    if (pc == null || pc.getConnection() == null) {
                        try {
                            this.lock.wait(1000L);
                        }
                        catch (InterruptedException e) {}
                    } else {
                        this.used.put(new Integer(((DispatchPooledConnectionImpl)pc).getSessionID()), pc);
                        break;
                    }
                }
                if (this.maxConnections == 0 || this.used.size() < this.maxConnections) {
                    pc = this.source.getPooledConnection();
                    this.used.put(new Integer(((DispatchPooledConnectionImpl)pc).getSessionID()), pc);
                    break;
                }
                try {
                    this.lock.wait(1000L);
                }
                catch (InterruptedException e) {}
            }
        }
        pc.addConnectionEventListener(this.connectionEventListener);
        return pc;
    }

    public Reference getReference() throws NamingException {
        Reference ref = super.getReference();
        ref.add(new StringRefAddr("dataSourceName", this.dataSourceName));
        if (this.initialConnections > 0) {
            ref.add(new StringRefAddr("initialConnections", Integer.toString(this.initialConnections)));
        }
        if (this.maxConnections > 0) {
            ref.add(new StringRefAddr("maxConnections", Integer.toString(this.maxConnections)));
        }
        return ref;
    }

    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        if (!this.initialized) {
            this.setUser(user);
            this.setPassword(password);
            this.initialize();
            return this.getPooledConnection();
        }
        if (user == null || user.equals(this.getUser()) && (password == null && this.getPassword() == null || password != null && password.equals(this.getPassword()))) {
            return this.getPooledConnection();
        }
        throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
    }
}

