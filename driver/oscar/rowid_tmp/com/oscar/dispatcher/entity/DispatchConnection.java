/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.entity;

import com.oscar.Driver;
import com.oscar.cluster.Cluster;
import com.oscar.core.BaseConnection;
import com.oscar.core.BaseResultSet;
import com.oscar.core.BaseStatement;
import com.oscar.core.DistributeImportHandler;
import com.oscar.core.Encoding;
import com.oscar.core.ExportHandler;
import com.oscar.core.ImportHandler;
import com.oscar.core.ProviderImportHandler;
import com.oscar.core.QueryExecutor;
import com.oscar.dispatcher.core.ConnectionMangerV2;
import com.oscar.dispatcher.entity.FakeConnection;
import com.oscar.dispatcher.entity.GlobalLsnVo;
import com.oscar.dispatcher.entity.LsnVo;
import com.oscar.dispatcher.entity.ThreadLocalLsnVo;
import com.oscar.dispatcher.executor.DispatchCallableStatementV2;
import com.oscar.dispatcher.executor.DispatchPreparedStatementV2;
import com.oscar.dispatcher.executor.DispatchStatementV2;
import com.oscar.fastpath.Fastpath;
import com.oscar.jdbc.OSCARTransfer;
import com.oscar.jdbc.OscarBfile;
import com.oscar.jdbc.OscarBlob;
import com.oscar.jdbc.OscarClob;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.protocol.OSCARProtocol;
import com.oscar.protocol.ProtocolVersion;
import com.oscar.util.VersionConfig;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class DispatchConnection
implements Connection,
BaseConnection {
    private int transStatus;
    private String dispatcherSessionId;
    private final OscarJdbc2Connection mainConn;
    public Integer sessionID = new Integer(-1);
    public String passward;
    public String dbName;
    public Properties props = new Properties();
    public String url;
    private final Map<String, Connection> slaveConnMap = new ConcurrentHashMap<String, Connection>();
    private final Map<String, Map<String, String>> slaveConnInfo = new HashMap<String, Map<String, String>>();
    public static final String SLAVE = "slave";
    public final int slaveCount;
    protected boolean logFlag = Driver.getLogLevel() >= 2;
    protected volatile boolean autoCommit = true;
    protected volatile boolean hasUpdate = false;
    private Object lockOnCreate = new Object();
    private final Map<String, Integer> slaveTryCountMap = new ConcurrentHashMap<String, Integer>();
    private static final int MAX_SLAVE_TRY_CREATE_COUNT = 3;
    public static final ThreadLocal<ThreadLocalLsnVo> threadLocalLsn = new ThreadLocal<ThreadLocalLsnVo>(){

        @Override
        public ThreadLocalLsnVo initialValue() {
            return new ThreadLocalLsnVo();
        }
    };
    public static final GlobalLsnVo globalLsnVo = new GlobalLsnVo();

    public OscarJdbc2Connection getMainConn() {
        return this.mainConn;
    }

    public boolean isHasUpdate() {
        return this.hasUpdate;
    }

    public void setHasUpdate(boolean hasUpdate) {
        this.hasUpdate = hasUpdate;
    }

    public DispatchConnection(OscarJdbc2Connection mainConn, String passward, String dbName, Properties props) {
        this.mainConn = mainConn;
        this.passward = passward;
        this.dbName = dbName;
        this.props = props;
        this.sessionID = mainConn.sessionID;
        this.initSlavesInfo();
        this.url = this.getUrl();
        mainConn.setDispatchConn(this);
        this.slaveCount = this.slaveConnInfo.size();
    }

    public DispatchConnection(OscarJdbc2Connection mainConn, String passward, String dbName, Properties props, Map<String, Map<String, String>> slaveConnInfo) {
        this.mainConn = mainConn;
        this.passward = passward;
        this.dbName = dbName;
        this.props = props;
        this.sessionID = mainConn.sessionID;
        this.url = this.getUrl();
        mainConn.setDispatchConn(this);
        this.slaveConnInfo.putAll(slaveConnInfo);
        this.slaveCount = this.slaveConnInfo.size();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    public void initSlavesInfo() {
        block18: {
            ResultSet rs;
            Statement st;
            block17: {
                st = null;
                rs = null;
                StringBuilder sb = new StringBuilder("session: " + this.sessionID + ", " + DispatchConnection.class + ", initSlavesInfo(), ");
                st = this.mainConn.createStatement();
                boolean useAsynchronous = Boolean.valueOf(this.props.getProperty("USEASYNCHRONOUS", "true"));
                StringBuilder sql = new StringBuilder("select address, port from v_sys_ha_slave_info where readable = TRUE ");
                if (!useAsynchronous) {
                    sql.append(" and SYNCHRONIZED = true");
                    sb.append("useAsynchronous=false, nodes:");
                } else {
                    sb.append("useAsynchronous=true, slave nodes: ");
                }
                rs = st.executeQuery(sql.toString());
                int index = 0;
                while (rs.next()) {
                    String key = SLAVE + index;
                    HashMap<String, String> info = new HashMap<String, String>();
                    this.slaveConnInfo.put(key, info);
                    info.put("ADDRESS", rs.getString("ADDRESS"));
                    info.put("PORT", rs.getString("PORT"));
                    sb.append(key + ":" + ((Object)info).toString() + "  ");
                    ++index;
                }
                if (!this.logFlag) break block17;
                Driver.writeLog(sb.toString());
            }
            Object var10_10 = null;
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                break block18;
            }
            catch (SQLException e2) {
                e2.printStackTrace();
            }
            break block18;
            {
                catch (SQLException e) {
                    e.printStackTrace();
                    Object var10_11 = null;
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                        if (st != null) {
                            st.close();
                        }
                        break block18;
                    }
                    catch (SQLException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            catch (Throwable throwable) {
                Object var10_12 = null;
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (st != null) {
                        st.close();
                    }
                }
                catch (SQLException e2) {
                    e2.printStackTrace();
                }
                throw throwable;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Connection getSlaveConnection(String key) {
        if (key == null) {
            return this.mainConn;
        }
        Connection conn = this.slaveConnMap.get(key);
        if (conn == null) {
            Object object = this.lockOnCreate;
            synchronized (object) {
                conn = this.slaveConnMap.get(key);
                if (conn != null) {
                    return conn;
                }
                Integer tryCnt = this.slaveTryCountMap.get(key);
                if (tryCnt != null && tryCnt > 3) {
                    return this.mainConn;
                }
                Map<String, String> infoMap = this.slaveConnInfo.get(key);
                if (infoMap == null) {
                    return this.mainConn;
                }
                String url = "jdbc:oscar://" + infoMap.get("ADDRESS") + ":" + infoMap.get("PORT") + "/" + this.dbName;
                try {
                    int n;
                    this.props.setProperty("USEDISPATCH", "FALSE");
                    conn = DriverManager.getConnection(url, this.props);
                    ((OscarJdbc2Connection)conn).setDispatchConn(this);
                    ((BaseConnection)((Object)conn)).setMasterConnection(this.mainConn);
                    ((BaseConnection)((Object)conn)).setIsSlave(true);
                    this.slaveConnMap.put(key, conn);
                    if (tryCnt == null) {
                        n = 1;
                    } else {
                        Integer n2 = tryCnt;
                        Integer n3 = tryCnt = Integer.valueOf(tryCnt + 1);
                        n = n2;
                    }
                    this.slaveTryCountMap.put(key, n);
                }
                catch (SQLException e) {
                    if (this.logFlag) {
                        StringBuilder sb = new StringBuilder("session: " + this.sessionID + ", " + DispatchConnection.class + ", getSlaveConnection(String key), ");
                        sb.append("connect slave: " + url + "failed:");
                        sb.append(e.getMessage());
                        Driver.writeLog(sb.toString());
                    }
                    FakeConnection tagConn = FakeConnection.getInstance();
                    this.slaveConnMap.put(key, tagConn);
                    return tagConn;
                }
            }
        }
        return conn;
    }

    public void removeSlaveConnection(Connection conn) {
        for (String key : this.slaveConnMap.keySet()) {
            if (!conn.equals(this.slaveConnMap.get(key))) continue;
            this.slaveConnMap.remove(key);
            break;
        }
    }

    public Connection getSlaveConn() throws SQLException {
        Connection conn = this.getSlaveConnection(SLAVE + ConnectionMangerV2.lastSlaveID.get(this.url).get() % this.slaveConnInfo.size());
        ConnectionMangerV2.lastSlaveID.get(this.url).incrementAndGet();
        return conn;
    }

    public List<Connection> getAllSlaveConn() throws SQLException {
        ArrayList<Connection> rs = new ArrayList<Connection>();
        for (int i = 0; i < this.slaveCount; ++i) {
            rs.add(this.getSlaveConnection(SLAVE + i));
        }
        return rs;
    }

    protected String getUrl() {
        String url = "jdbc:oscar://" + this.props.get("DBHOST") + ":" + this.props.get("DBPORT") + "/" + this.dbName + "?hostLoadRate=" + this.props.getProperty("HOSTLOADRATE");
        return url;
    }

    public String getDispatcherSessionId() {
        return this.dispatcherSessionId;
    }

    public void setDispatcherSessionId(String dispatcherSessionId) {
        this.dispatcherSessionId = dispatcherSessionId;
    }

    @Override
    public void close() {
        try {
            this.mainConn.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        Iterator<Connection> it = this.slaveConnMap.values().iterator();
        while (it.hasNext()) {
            try {
                it.next().close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            it.remove();
        }
    }

    @Override
    public void clearWarnings() throws SQLException {
    }

    @Override
    public void commit() throws SQLException {
        this.mainConn.commit();
        this.hasUpdate = false;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return new DispatchStatementV2(this);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return new DispatchStatementV2(this, resultSetType, resultSetConcurrency);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return new DispatchStatementV2(this, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return this.mainConn.getAutoCommit();
    }

    @Override
    public String getCatalog() throws SQLException {
        return this.mainConn.getCatalog();
    }

    @Override
    public int getHoldability() throws SQLException {
        return this.mainConn.getHoldability();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return this.mainConn.getMetaData();
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return this.mainConn.getTransactionIsolation();
    }

    public Map getTypeMap() throws SQLException {
        return this.mainConn.getTypeMap();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.mainConn.getWarnings();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.mainConn.isClosed();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return this.mainConn.isReadOnly();
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return this.mainConn.nativeSQL(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return new DispatchCallableStatementV2(this, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return new DispatchCallableStatementV2(this, sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return new DispatchCallableStatementV2(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new DispatchPreparedStatementV2(this, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return new DispatchPreparedStatementV2(this, sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return new DispatchPreparedStatementV2(this, sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return new DispatchPreparedStatementV2(this, sql, columnNames);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        this.mainConn.releaseSavepoint(savepoint);
    }

    @Override
    public void rollback() throws SQLException {
        this.mainConn.rollback();
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        this.mainConn.rollback(savepoint);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.mainConn.setAutoCommit(autoCommit);
        if (this.autoCommit != autoCommit) {
            this.autoCommit = autoCommit;
            this.hasUpdate = false;
        }
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        this.mainConn.setCatalog(catalog);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        this.mainConn.setHoldability(holdability);
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        this.mainConn.setReadOnly(readOnly);
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return this.mainConn.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return this.mainConn.setSavepoint(name);
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        this.mainConn.setTransactionIsolation(level);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return new DispatchPreparedStatementV2(this, sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        this.mainConn.setTypeMap((Map)map);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return new DispatchPreparedStatementV2(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public void addWarning(String msg, String code) {
        this.mainConn.addWarning(msg, code);
    }

    @Override
    public void cancelQuery() throws SQLException {
        this.mainConn.cancelQuery();
    }

    @Override
    public BaseResultSet execSQL(String s) throws SQLException {
        return this.mainConn.execSQL(s);
    }

    @Override
    public BaseResultSet execSQL(String s, BaseStatement stmt) throws SQLException {
        return this.mainConn.execSQL(s, stmt);
    }

    @Override
    public BaseResultSet execSQL(String s, BaseStatement stmt, BaseResultSet res) throws SQLException {
        return this.mainConn.execSQL(s, stmt, res);
    }

    @Override
    public String getCursorName() throws SQLException {
        return this.mainConn.getCursorName();
    }

    @Override
    public Encoding getClientEncoding() {
        return this.mainConn.getClientEncoding();
    }

    @Override
    public void setClientEncoding(String encode) {
        this.mainConn.setClientEncoding(encode);
    }

    @Override
    public Encoding getEncoding() {
        return this.mainConn.getEncoding();
    }

    @Override
    public OSCARProtocol getProtocol() throws SQLException {
        return this.mainConn.getProtocol();
    }

    @Override
    public String getDBType(int oid) throws SQLException {
        return this.mainConn.getDBType(oid);
    }

    @Override
    public int getDBTypeOid(String DBTypeName) throws SQLException {
        return this.mainConn.getDBTypeOid(DBTypeName);
    }

    @Override
    public int getSQLType(int oid) throws SQLException {
        return this.mainConn.getSQLType(oid);
    }

    @Override
    public int getOscarType(int oid) throws SQLException {
        return this.mainConn.getOscarType(oid);
    }

    @Override
    public int getSQLType(String DBTypeName) throws SQLException {
        return this.mainConn.getSQLType(DBTypeName);
    }

    @Override
    public void setCursorName(String cursor) throws SQLException {
        this.mainConn.setCursorName(cursor);
    }

    @Override
    public void addCursor(String cursorName) {
        this.mainConn.addCursor(cursorName);
    }

    @Override
    public void removePlanID(int index) {
        this.mainConn.removePlanID(index);
    }

    @Override
    public void addPlanID(byte[] planID) {
        this.mainConn.addPlanID(planID);
    }

    @Override
    public boolean hasCursor(String cursorName) {
        return this.mainConn.hasCursor(cursorName);
    }

    @Override
    public void removeCursor(String cursorName) {
        this.mainConn.removeCursor(cursorName);
    }

    @Override
    public OscarBlob getBlobInstance(String locatorStr) throws SQLException {
        return this.mainConn.getBlobInstance(locatorStr);
    }

    @Override
    public OscarBfile getBfileInstance(String locatorStr) throws SQLException {
        return this.mainConn.getBfileInstance(locatorStr);
    }

    @Override
    public OscarClob getClobInstance(String locatorStr) throws SQLException {
        return this.mainConn.getClobInstance(locatorStr);
    }

    @Override
    public BaseStatement getDefaultStatement() throws SQLException {
        return this.mainConn.getDefaultStatement();
    }

    @Override
    public Object lookupCachedPrepare(String sql) {
        return this.mainConn.lookupCachedPrepare(sql);
    }

    @Override
    public void addNewPrepare(Object p) throws SQLException {
        this.mainConn.addNewPrepare(p);
    }

    @Override
    public void setInTranscation(boolean tran) {
        this.mainConn.setInTranscation(tran);
    }

    @Override
    public boolean isInTransaction() {
        return this.mainConn.isInTransaction();
    }

    @Override
    public int getPreapredCacheSize() {
        return this.mainConn.getPreapredCacheSize();
    }

    @Override
    public Fastpath getFastpathAPI() throws SQLException {
        return this.mainConn.getFastpathAPI();
    }

    @Override
    public long getAccessHandle() {
        return this.mainConn.getAccessHandle();
    }

    @Override
    public void setSeed(long seed) {
        this.mainConn.setSeed(seed);
    }

    @Override
    public VersionConfig getVersion() {
        return this.mainConn.getVersion();
    }

    @Override
    public void setVersion(ProtocolVersion version) {
        this.mainConn.setVersion(version);
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.mainConn.getProtocolVersion();
    }

    @Override
    public boolean isUsePrepareCache() {
        return this.mainConn.isUsePrepareCache();
    }

    @Override
    public boolean isCompatibleOracle() {
        return this.mainConn.isCompatibleOracle();
    }

    @Override
    public boolean isNotRealPrepare() {
        return this.mainConn.isNotRealPrepare();
    }

    @Override
    public void closeCursor(String cursorName, BaseStatement statement) throws SQLException {
        this.mainConn.closeCursor(cursorName, statement);
    }

    @Override
    public OSCARTransfer getTransfer() {
        return this.mainConn.getTransfer();
    }

    @Override
    public OscarBlob createTempBlob(boolean cache, int duration) throws SQLException {
        return this.mainConn.createTempBlob(cache, duration);
    }

    @Override
    public OscarClob createTempClob(boolean cache, int duration) throws SQLException {
        return this.mainConn.createTempClob(cache, duration);
    }

    @Override
    public QueryExecutor getQueryExecutor() {
        return this.mainConn.getQueryExecutor();
    }

    @Override
    public boolean isCompressTransfer() {
        return this.mainConn.isCompressTransfer();
    }

    @Override
    public void setCompressTransfer(boolean compressTransfer) {
        this.mainConn.setCompressTransfer(compressTransfer);
    }

    @Override
    public int getBatchBufferSize() {
        return this.mainConn.getBatchBufferSize();
    }

    @Override
    public void setBatchBufferSize(int defaultBatchBufferSize) {
        this.mainConn.setBatchBufferSize(defaultBatchBufferSize);
    }

    @Override
    public ImportHandler createImportHandler() throws SQLException {
        return this.mainConn.createImportHandler();
    }

    @Override
    public ImportHandler createImportHandler(String tableName) throws SQLException {
        return this.mainConn.createImportHandler(tableName);
    }

    @Override
    public ImportHandler createImportHandler(String schemName, String tableName) throws SQLException {
        return this.mainConn.createImportHandler(schemName, tableName);
    }

    @Override
    public ProviderImportHandler createProviderImportHandler(String tableName) throws SQLException {
        return this.mainConn.createProviderImportHandler(tableName);
    }

    @Override
    public ProviderImportHandler createProviderImportHandler(String schemName, String tableName) throws SQLException {
        return this.mainConn.createProviderImportHandler(schemName, tableName);
    }

    @Override
    public ExportHandler createExportHandler() throws SQLException {
        return this.mainConn.createExportHandler();
    }

    @Override
    public int getEndianType() {
        return this.mainConn.getEndianType();
    }

    @Override
    public void setEndianType(int type) {
        this.mainConn.setEndianType(type);
    }

    @Override
    public int checkPlanID(byte[] planID) {
        return this.mainConn.checkPlanID(planID);
    }

    @Override
    public Integer getSessionID() {
        return this.mainConn.getSessionID();
    }

    @Override
    public int getPlanID() throws SQLException {
        return this.mainConn.getPlanID();
    }

    @Override
    public boolean isNetDataByStr() {
        return this.mainConn.isNetDataByStr();
    }

    @Override
    public boolean isNumericKeepPrecision() {
        return this.mainConn.isNumericKeepPrecision();
    }

    @Override
    public boolean isPrepareSimpleExecute() {
        return this.mainConn.isPrepareSimpleExecute();
    }

    @Override
    public boolean isTcpKeepAlive() {
        return this.mainConn.isTcpKeepAlive();
    }

    @Override
    public void setTcpKeepAlive(boolean tcpKeepAlive) {
        this.mainConn.setTcpKeepAlive(tcpKeepAlive);
    }

    @Override
    public boolean isValidate() {
        return this.mainConn.isValidate();
    }

    @Override
    public boolean isValidate(int timeout) {
        return this.mainConn.isValidate(timeout);
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return this.mainConn.isValid(timeout);
    }

    @Override
    public boolean isValid() throws SQLException {
        return this.mainConn.isValid();
    }

    @Override
    public boolean isVerifyPoolConnection() {
        return this.mainConn.isVerifyPoolConnection();
    }

    @Override
    public int getBatchCount() {
        return this.mainConn.getBatchCount();
    }

    @Override
    public boolean isUseAsynBatch() {
        return this.mainConn.isUseAsynBatch();
    }

    @Override
    public boolean isReceiveStringByLen() {
        return this.mainConn.isReceiveStringByLen();
    }

    @Override
    public void setMasterConnection(Connection conn) {
    }

    @Override
    public Connection getMasterConnection() {
        return this.mainConn;
    }

    @Override
    public Properties getConnectionProperties() {
        return this.props;
    }

    @Override
    public void setTransStatus(int transStatus) {
        this.transStatus = transStatus;
    }

    @Override
    public int getTransStatus() {
        return this.transStatus;
    }

    public LsnVo getLsnVo() {
        if (this.getMainConn().isUseSlaveSyncReadGlobal()) {
            return globalLsnVo;
        }
        return threadLocalLsn.get();
    }

    @Override
    public boolean isCompatibleOldDateFormat() {
        return false;
    }

    @Override
    public boolean isSlave() {
        return false;
    }

    @Override
    public void setIsSlave(boolean isSlave) {
    }

    @Override
    public boolean isZeroResend() {
        return false;
    }

    @Override
    public boolean isUseSlaveSynRead() {
        return false;
    }

    @Override
    public boolean checkDBLinkSql() {
        return false;
    }

    @Override
    public boolean sendBinaryTypeAsHex() {
        return this.mainConn.sendBinaryTypeAsHex();
    }

    @Override
    public boolean getIntWithPrecision() {
        return this.mainConn.getIntWithPrecision();
    }

    @Override
    public DistributeImportHandler createDistributeImportHandler(String schemName, String tableName) throws SQLException {
        return this.mainConn.createDistributeImportHandler(schemName, tableName);
    }

    @Override
    public DistributeImportHandler createDistributeImportHandler(String tableName) throws SQLException {
        return this.mainConn.createDistributeImportHandler(tableName);
    }

    @Override
    public void setClusterImportNodeRetryTime(int time) {
        this.mainConn.setClusterImportNodeRetryTime(time);
    }

    @Override
    public int getClusterImportNodeRetryTime() {
        return this.mainConn.getClusterImportNodeRetryTime();
    }

    @Override
    public void setCluster(Cluster cluster) {
    }

    @Override
    public Cluster getCluster() {
        return null;
    }

    @Override
    public byte[] getHdSymEncryptKey() {
        return this.mainConn.getHdSymEncryptKey();
    }

    @Override
    public void setHdSymEncryptKey(byte[] key) {
        this.mainConn.setHdSymEncryptKey(key);
    }
}

