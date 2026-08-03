/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbcx.optional;

import com.oscar.Config;
import com.oscar.Driver;
import com.oscar.jdbcx.optional.BaseDataSource;
import com.oscar.jdbcx.optional.ConnectionPool;
import com.oscar.jdbcx.optional.PooledConnectionImpl;
import com.oscar.util.OSQLException;
import com.oscar.util.OscarSqlProcessor;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public class PoolingDataSource
extends BaseDataSource
implements ConnectionPoolDataSource {
    private static Map dataSources = new HashMap();
    protected String dataSourceName;
    private int initialConnections = 0;
    private int maxConnections = 0;
    private boolean initialized = false;
    private Stack available = new Stack();
    private Stack used = new Stack();
    private Object lock = new Object();
    private ConnectionPool source;
    private int maxWait = -1;
    private int maxIdle = 0;
    private int minIdle = 0;
    private String validationQuery = "";
    private boolean removeAbandoned = false;
    private int removeAbandonedTimeout;
    private ConcurrentMap connTimeMap = null;
    private int timeBetweenEvictionRunsMillis = -1;
    private int minEvictableIdleTimeMillis;
    private volatile PoolCleaner poolCleaner;
    private int abandonWhenPercentageFull = 0;
    private boolean defaultAutoCommit = true;
    private boolean logFlag = Driver.getLogLevel() >= 1;
    public static final String LOGFLAG = "PoolingDataSource------------";
    private static volatile Timer poolCleanTimer = null;
    private static HashSet<PoolCleaner> cleaners = new HashSet();
    private ConnectionEventListener connectionEventListener = new ConnectionEventListener(){

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void connectionClosed(ConnectionEvent event) {
            ((PooledConnection)event.getSource()).removeConnectionEventListener(this);
            Object object = PoolingDataSource.this.lock;
            synchronized (object) {
                if (PoolingDataSource.this.available == null) {
                    return;
                }
                PooledConnectionImpl pci = (PooledConnectionImpl)event.getSource();
                boolean removed = PoolingDataSource.this.used.remove(pci);
                if (removed) {
                    if (pci.isClosed()) {
                        PoolingDataSource.this.available.remove(pci);
                    } else if (PoolingDataSource.this.maxIdle > 0 && (PoolingDataSource.this.getMaxConnections() == 0 || PoolingDataSource.this.maxIdle < PoolingDataSource.this.getMaxConnections())) {
                        if (PoolingDataSource.this.available.size() >= PoolingDataSource.this.maxIdle) {
                            try {
                                pci.close();
                            }
                            catch (SQLException e) {}
                        } else {
                            PoolingDataSource.this.available.push(event.getSource());
                        }
                    } else {
                        PoolingDataSource.this.available.push(event.getSource());
                    }
                    PoolingDataSource.this.lock.notify();
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void connectionErrorOccurred(ConnectionEvent event) {
            ((PooledConnection)event.getSource()).removeConnectionEventListener(this);
            Object object = PoolingDataSource.this.lock;
            synchronized (object) {
                if (PoolingDataSource.this.available == null) {
                    return;
                }
                PoolingDataSource.this.used.remove(event.getSource());
                PoolingDataSource.this.lock.notify();
            }
        }
    };

    static PoolingDataSource getDataSource(String name) {
        return (PoolingDataSource)dataSources.get(name);
    }

    public PoolingDataSource() {
        this.initProperties();
    }

    public String getDescription() {
        return "Pooling DataSource " + this.dataSourceName + " from " + Driver.getVersion();
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

    public void setSSL(boolean useSSL) {
        if (this.initialized) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        super.setSSL(useSSL);
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
            if (PoolingDataSource.getDataSource(dataSourceName) != null) {
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
            this.source.setPropertity(this.getPropertity());
            if (this.getPropertity() != null) {
                this.source.setSSL("t".equals(this.getPropertity().getProperty("ssl", "f")));
            }
            this.source.setDefaultAutoCommit(this.defaultAutoCommit);
            while (this.available.size() < this.initialConnections) {
                this.available.push(this.source.getPooledConnection());
            }
            this.initialized = true;
        }
        this.initializePoolCleaner();
    }

    protected boolean isInitialized() {
        return this.initialized;
    }

    protected ConnectionPool createConnectionPool() {
        return new ConnectionPool();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Connection getConnection(String user, String password) throws SQLException {
        if (user == null || user.equals(this.getUser()) && (password == null && this.getPassword() == null || password != null && password.equals(this.getPassword()))) {
            return this.getConnection();
        }
        Object object = this.lock;
        synchronized (object) {
            if (!this.initialized) {
                this.initialize();
            }
        }
        return super.getConnection(user, password);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Connection getConnection() throws SQLException {
        Object object = this.lock;
        synchronized (object) {
            if (!this.initialized) {
                this.initialize();
            }
        }
        return this.getPooledConnection().getConnection();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() {
        Object object = this.lock;
        synchronized (object) {
            PooledConnectionImpl pci;
            if (this.poolCleaner != null) {
                this.poolCleaner.stopRunning();
            }
            while (this.available.size() > 0) {
                pci = (PooledConnectionImpl)this.available.pop();
                try {
                    pci.close();
                }
                catch (SQLException e) {}
            }
            this.available = null;
            while (this.used.size() > 0) {
                pci = (PooledConnectionImpl)this.used.pop();
                pci.removeConnectionEventListener(this.connectionEventListener);
                try {
                    pci.close();
                }
                catch (SQLException sQLException) {}
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
            long currentTimeBef = System.currentTimeMillis();
            while (true) {
                block27: {
                    block25: {
                        PooledConnectionImpl pci;
                        block26: {
                            Long currentTime;
                            if (this.maxWait > 0 && (currentTime = Long.valueOf(System.currentTimeMillis())) - currentTimeBef > (long)this.maxWait) {
                                throw new OSQLException("OSCAR-00122", "08001", 122);
                            }
                            if (this.available.size() <= 0) break block25;
                            if (this.minIdle <= 0 || this.maxIdle != 0 && this.minIdle >= this.maxIdle || this.getMaxConnections() != 0 && this.minIdle >= this.getMaxConnections()) break block26;
                            if (this.available.size() <= this.minIdle) break block25;
                            pci = (PooledConnectionImpl)this.available.pop();
                            this.poollog("---getPooledConnection---pc::" + pci.hashCode() + "::" + pci.isClosed());
                            if (pci.isClosed()) {
                                pc = this.source.getPooledConnection();
                            } else if (pci.getConnection() == null) {
                                pci.close();
                                pc = this.source.getPooledConnection();
                            } else {
                                pc = pci;
                            }
                            this.used.push(pc);
                            break block27;
                        }
                        pci = (PooledConnectionImpl)this.available.pop();
                        this.poollog("getPooledConnection---pc::" + pci.hashCode() + "::" + pci.isClosed());
                        if (pci.isClosed()) {
                            pc = this.source.getPooledConnection();
                        } else if (pci.getConnection() == null) {
                            pci.close();
                            pc = this.source.getPooledConnection();
                        } else {
                            pc = pci;
                        }
                        this.used.push(pc);
                        break block27;
                    }
                    if (this.maxConnections == 0 || this.used.size() < this.maxConnections) {
                        pc = this.source.getPooledConnection();
                        this.used.push(pc);
                    } else {
                        try {
                            this.lock.wait(1000L);
                        }
                        catch (InterruptedException e) {}
                        continue;
                    }
                }
                if (this.validationQuery == null || "".equals(this.validationQuery)) break;
                OscarSqlProcessor.ParseResult pr = OscarSqlProcessor.parsing(this.validationQuery.trim());
                if (!pr.isSelectSql()) {
                    throw new SQLException("set fail, must be select sql");
                }
                Statement st = pc.getConnection().createStatement();
                try {
                    st.executeQuery(this.validationQuery);
                    st.close();
                }
                catch (SQLException e) {
                    if (st == null) continue;
                    try {
                        st.close();
                    }
                    catch (Exception ignore2) {}
                    continue;
                }
                break;
            }
        }
        pc.addConnectionEventListener(this.connectionEventListener);
        if (this.connTimeMap == null) {
            this.connTimeMap = new ConcurrentHashMap();
        }
        this.connTimeMap.put(pc, System.currentTimeMillis());
        return pc;
    }

    public void poollog(String msg) {
        if (this.logFlag) {
            Driver.writeLog(LOGFLAG + msg);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static synchronized void registerCleaner(PoolCleaner cleaner) {
        PoolingDataSource.unregisterCleaner(cleaner);
        cleaners.add(cleaner);
        if (poolCleanTimer == null) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(PoolingDataSource.class.getClassLoader());
                poolCleanTimer = new Timer("PoolCleaner[" + System.identityHashCode(PoolingDataSource.class.getClassLoader()) + ":" + System.currentTimeMillis() + "]", true);
                Object var3_2 = null;
                Thread.currentThread().setContextClassLoader(loader);
            }
            catch (Throwable throwable) {
                Object var3_3 = null;
                Thread.currentThread().setContextClassLoader(loader);
                throw throwable;
            }
        }
        poolCleanTimer.scheduleAtFixedRate((TimerTask)cleaner, cleaner.sleepTime, cleaner.sleepTime);
    }

    private static synchronized void unregisterCleaner(PoolCleaner cleaner) {
        boolean removed = cleaners.remove(cleaner);
        if (removed) {
            cleaner.cancel();
            if (poolCleanTimer != null) {
                poolCleanTimer.purge();
                if (cleaners.size() == 0) {
                    poolCleanTimer.cancel();
                    poolCleanTimer = null;
                }
            }
        }
    }

    public void initializePoolCleaner() {
        this.poollog("initializePoolCleaner");
        if (this.isPoolSweeperEnabled()) {
            this.poollog("isPoolSweeperEnabled() : true");
            this.poolCleaner = new PoolCleaner(this, this.timeBetweenEvictionRunsMillis);
            this.poolCleaner.start();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void checkAbandoned() {
        if (this.used.size() == 0) {
            return;
        }
        Object object = this.lock;
        synchronized (object) {
            for (int i = 0; i < this.used.size(); ++i) {
                PooledConnection pc = (PooledConnection)this.used.get(i);
                if (this.available.contains(pc)) continue;
                long now = System.currentTimeMillis();
                if (!this.shouldAbandon() || this.connTimeMap.get(pc) == null || now - Long.valueOf(this.connTimeMap.get(pc).toString()) <= (long)(this.removeAbandonedTimeout * 1000)) continue;
                this.used.remove(i);
                try {
                    pc.close();
                    continue;
                }
                catch (SQLException e) {
                    // empty catch block
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void checkIdle() {
        if (this.available.size() == 0) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator unlocked = this.available.iterator();
        while (this.available.size() >= this.minIdle && unlocked.hasNext()) {
            PooledConnection pc = (PooledConnection)unlocked.next();
            Object object = this.lock;
            synchronized (object) {
                if (this.used.contains(pc)) {
                    continue;
                }
                long time = Long.valueOf(this.connTimeMap.get(pc).toString());
                if (this.shouldReleaseIdle(now, time)) {
                    this.available.remove(pc);
                    try {
                        pc.close();
                    }
                    catch (SQLException e) {
                        // empty catch block
                    }
                }
            }
        }
    }

    protected boolean shouldReleaseIdle(long now, long time) {
        return this.minEvictableIdleTimeMillis > 0 && now - time > (long)this.minEvictableIdleTimeMillis;
    }

    protected boolean shouldAbandon() {
        if (this.abandonWhenPercentageFull == 0) {
            return true;
        }
        return (float)(this.used.size() / this.maxConnections) * 100.0f >= (float)this.abandonWhenPercentageFull;
    }

    public boolean isPoolSweeperEnabled() {
        boolean timer = this.timeBetweenEvictionRunsMillis > 0;
        boolean result = timer && this.removeAbandoned && this.removeAbandonedTimeout > 0;
        result = result || timer && this.minEvictableIdleTimeMillis > 0;
        return result;
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
        Object object = this.lock;
        synchronized (object) {
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

    public int getMaxWait() {
        return this.maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public void setMaxIdle(int maxidle) {
        this.maxIdle = maxidle;
    }

    public void setMinIdle(int minidle) {
        this.minIdle = minidle;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public void setRemoveabandoned(boolean removeabandoned) {
        this.removeAbandoned = removeabandoned;
    }

    public void setRemoveabandonedtimeout(int removeabandonedtimeout) {
        this.removeAbandonedTimeout = removeabandonedtimeout;
    }

    public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public void setAbandonWhenPercentageFull(int abandonWhenPercentageFull) {
        this.abandonWhenPercentageFull = abandonWhenPercentageFull;
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }

    protected void initProperties() {
        Properties pros = Config.configProp;
        if (pros != null) {
            this.setMaxConnections(Integer.valueOf(pros.getProperty("MAXACTIVE", "0")));
            this.setInitialConnections(Integer.valueOf(pros.getProperty("INITIALSIZE", "0")));
            if (this.maxConnections < this.initialConnections) {
                this.setInitialConnections(this.maxConnections);
            }
            this.setMinIdle(Integer.valueOf(pros.getProperty("MINIDLE", "0")));
            if (this.minIdle > this.maxConnections) {
                this.setMinIdle(this.maxConnections);
            }
            this.setMaxIdle(Integer.valueOf(pros.getProperty("MAXIDLE", "0")));
            if (this.maxIdle > this.maxConnections) {
                this.setMaxIdle(this.maxConnections);
            }
            if (this.maxIdle < this.minIdle) {
                this.setMaxIdle(this.minIdle);
            }
            this.setDefaultAutoCommit(Boolean.valueOf(pros.getProperty("DEFAULTAUTOCOMMIT", "true")));
            this.setMaxWait(Integer.valueOf(pros.getProperty("MAXWAIT", "-1")));
            this.setValidationQuery(pros.getProperty("VALIDATIONQUERY", ""));
            this.setRemoveabandoned(Boolean.valueOf(pros.getProperty("REMOVEABANDONED", "false")));
            this.setRemoveabandonedtimeout(Integer.valueOf(pros.getProperty("REMOVEABANDONEDTIMEOUT", "0")));
            this.setTimeBetweenEvictionRunsMillis(Integer.valueOf(pros.getProperty("TIMEBETWEENEVICTIONRUNSMILLIS", "0")));
            this.setMinEvictableIdleTimeMillis(Integer.valueOf(pros.getProperty("MINEVICTABLEIDLETIMEMILLIS", "0")));
        }
    }

    protected static class PoolCleaner
    extends TimerTask {
        protected WeakReference<PoolingDataSource> pool;
        protected volatile long lastRun = 0L;
        protected long sleepTime;

        PoolCleaner(PoolingDataSource pool, long sleepTime) {
            this.pool = new WeakReference<PoolingDataSource>(pool);
            this.sleepTime = sleepTime;
            if (sleepTime <= 0L) {
                this.sleepTime = 30000L;
            }
        }

        public void run() {
            PoolingDataSource pool = (PoolingDataSource)this.pool.get();
            if (pool == null) {
                this.stopRunning();
            }
            if (System.currentTimeMillis() - this.lastRun > this.sleepTime) {
                this.lastRun = System.currentTimeMillis();
                if (pool.removeAbandoned) {
                    pool.checkAbandoned();
                }
                if (pool.minIdle < pool.available.size()) {
                    pool.checkIdle();
                }
            }
        }

        public void start() {
            PoolingDataSource.registerCleaner(this);
        }

        public void stopRunning() {
            PoolingDataSource.unregisterCleaner(this);
        }
    }
}

