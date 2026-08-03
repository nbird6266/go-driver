/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.Driver;
import com.oscar.cluster.Cluster;
import com.oscar.cluster.ClusterImportHandler;
import com.oscar.core.BaseConnection;
import com.oscar.core.BaseResultSet;
import com.oscar.core.BaseStatement;
import com.oscar.core.DistributeImportHandler;
import com.oscar.core.Encoding;
import com.oscar.core.ExportHandler;
import com.oscar.core.ImportHandler;
import com.oscar.core.ProviderImportHandler;
import com.oscar.core.QueryExecutor;
import com.oscar.dispatcher.entity.DispatchConnection;
import com.oscar.fastpath.Fastpath;
import com.oscar.jdbc.EscapeProcessor;
import com.oscar.jdbc.EscapeProcessorResult;
import com.oscar.jdbc.ExceptionUtil;
import com.oscar.jdbc.OSCARSavepoint;
import com.oscar.jdbc.OSCARTransfer;
import com.oscar.jdbc.OscarBfile;
import com.oscar.jdbc.OscarBlob;
import com.oscar.jdbc.OscarCallableStatement;
import com.oscar.jdbc.OscarCallableStatementV2;
import com.oscar.jdbc.OscarClob;
import com.oscar.jdbc.OscarDatabaseMetaData;
import com.oscar.jdbc.OscarExportHandler;
import com.oscar.jdbc.OscarImportHandler;
import com.oscar.jdbc.OscarPreparedStatement;
import com.oscar.jdbc.OscarPreparedStatementV2;
import com.oscar.jdbc.OscarProviderImportHandle;
import com.oscar.jdbc.OscarStatement;
import com.oscar.jdbc.OscarStatementV2;
import com.oscar.jdbc.PreparedInfo;
import com.oscar.protocol.OSCARProtocol;
import com.oscar.protocol.OSCARProtocolV2;
import com.oscar.protocol.ProtocolManager;
import com.oscar.protocol.ProtocolVersion;
import com.oscar.protocol.packets.BasePacket;
import com.oscar.util.ImportBufferManager;
import com.oscar.util.LRUStatementCache;
import com.oscar.util.LRUStatementCacheV2;
import com.oscar.util.OSQLException;
import com.oscar.util.VersionConfig;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class OscarJdbc2Connection
implements BaseConnection,
Connection {
    public Integer sessionID = new Integer(-1);
    protected BaseStatement stmt;
    protected OSCARProtocol protocol;
    protected String DB_HOST;
    protected int DB_PORT;
    protected String DB_USER;
    protected String DB_DATABASE;
    protected boolean DB_STATUS;
    protected boolean useSSL;
    protected boolean compressTransfer = false;
    private Connection masterConnection = null;
    protected long randNum;
    protected static final int KEY1 = 1103515245;
    protected static final int KEY2 = 12345;
    protected static final int KEY_MAX = 0x40000000;
    protected ProtocolVersion version = null;
    protected int pid;
    protected int ckey;
    private Encoding clientEncoding = null;
    private Encoding dbEncoding = null;
    private final boolean CONNECTION_OK = true;
    private final boolean CONNECTION_BAD = false;
    protected boolean autoCommit = true;
    private boolean readOnly = false;
    private boolean inTransaction = false;
    private Driver this_driver;
    private String this_url;
    private String cursor = null;
    private Fastpath fastpath = null;
    protected int holdability = 2;
    private Vector planIDVec;
    private Vector cursorVec;
    protected List preparedList;
    protected DatabaseMetaData metadata;
    private volatile boolean closed;
    public SQLWarning warnings = null;
    private int isolationLevel = 2;
    protected boolean usePrepareCache = false;
    protected int preparedCacheSize = 0;
    protected boolean compatibleOracle = false;
    protected boolean notRealPrepare = false;
    protected boolean verifyPoolConnection = false;
    protected boolean compatibleOldDateFormat = false;
    private int statementFetchSize = 16;
    public Properties connectionProperty = null;
    private static ConcurrentMap sqlTypeCache = new ConcurrentHashMap();
    private static ConcurrentMap DBTypeCache = new ConcurrentHashMap();
    private static ConcurrentMap typeOidCache = new ConcurrentHashMap();
    private static ConcurrentMap jdbcTypes = new ConcurrentHashMap();
    private static ConcurrentMap oscarTypeCache = new ConcurrentHashMap();
    public static Integer recycleSocketNum = new Integer(0);
    private byte[] hdSymEncryptKey;
    protected int timeOut_MilliSecond = 0;
    private final QueryExecutor executor;
    private int batchBufferSize = 0;
    ProtocolManager protocolManager;
    private int endianType = 1;
    protected boolean netDataByStr = false;
    protected boolean numericKeepPrecision = true;
    protected boolean compatibleOldProtocol = true;
    protected boolean receiveStringByLen = true;
    protected boolean prepareSimpleExecute = true;
    protected LRUStatementCache statementCache = null;
    protected int lobDisplayMaxSize = -1;
    protected boolean logFlag = Driver.getLogLevel() >= 2;
    protected static boolean stLogFlag = Driver.getLogLevel() >= 1;
    protected int batchCount = 0;
    protected boolean useAsynBatch = false;
    private List stList;
    private boolean isClearSt = false;
    private long lsnValue = 0L;
    private int transStatus;
    private DispatchConnection dispatchConn = null;
    private String validTestString;
    protected boolean isSlave = false;
    protected boolean isResultSetZeroResend = false;
    protected boolean useSlaveSynRead = false;
    protected String[] directToMainArray = null;
    protected String[] directToSlaveArray = null;
    private boolean checkDBLinkSql = false;
    private boolean sendBinaryTypeAsHex = false;
    private String schema = null;
    private boolean getIntWithPrecision = false;
    private int clusterImportNodeRetryTime = 0;
    private Cluster cluster = null;
    private boolean useSlaveSyncReadGlobal = false;
    private long slaveDelayTime = 0L;
    private long sleepEndTime = 0L;
    private String tempFileDir;
    private static final int[] jdbcTypei;
    protected Map typemap;
    private static final int AUTO_GENERATED_OID = 1;
    private static final int AUTO_GENERATED_COLINDEX = 2;
    private static final int AUTO_GENERATED_COLNAME = 3;
    protected int autoGeneratedInfo = -1;
    protected int[] columnIndex = null;
    protected String[] columnName = null;

    public String[] getDirectToSlaveArray() {
        return this.directToSlaveArray;
    }

    @Override
    public OSCARProtocol getProtocol() throws SQLException {
        if (this.protocol == null) {
            throw new OSQLException("OSCAR-00211", "08003", 211);
        }
        return this.protocol;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void openConnection(String host, int port, Properties info, String database, String url, Driver driver) throws SQLException {
        String directToSlave;
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", openConnection()");
        }
        this.connectionProperty = info;
        int timeout = 0;
        ImportBufferManager.setMaxUsingBufferSize(Integer.valueOf(info.getProperty("MAXUSINGBUFFERSIZE", "5120")));
        if (info.getProperty("REQUESTTIMEOUT") != null) {
            timeout = Integer.parseInt(info.getProperty("REQUESTTIMEOUT"));
        }
        if (timeout > 0 && timeout != this.timeOut_MilliSecond) {
            this.timeOut_MilliSecond = timeout;
        }
        int batchcount = 0;
        if (info.getProperty("BATCHCOUNT") != null) {
            batchcount = Integer.parseInt(info.getProperty("BATCHCOUNT"));
        }
        if (batchcount > 0) {
            this.batchCount = batchcount;
        }
        this.useAsynBatch = Boolean.valueOf(info.getProperty("USEASYNBATCH", "false"));
        String authType = info.getProperty("AUTH_TYPE", "PW");
        if (info.getProperty("OSAUTHEN", "f").equalsIgnoreCase("t")) {
            info.setProperty("AUTH_TYPE", "OS");
            authType = "OS";
        }
        if (!(authType.equalsIgnoreCase("PW") || authType.equalsIgnoreCase("OS") || authType.equalsIgnoreCase("RA") || authType.equalsIgnoreCase("FP"))) {
            throw new OSQLException("OSCAR-00118", "08001", 118);
        }
        if (authType.equalsIgnoreCase("FP") && (info.get("FINGERPRINT") == null || !(info.get("FINGERPRINT") instanceof byte[]))) {
            throw new OSQLException("OSCAR-00110", "08001", 110);
        }
        this.DB_USER = "";
        if (!authType.equalsIgnoreCase("OS") && (this.DB_USER = info.getProperty("USER")) == null) {
            throw new OSQLException("OSCAR-00119", "08001", 119);
        }
        this.this_driver = driver;
        this.this_url = url;
        this.DB_DATABASE = database;
        this.DB_PORT = port;
        this.DB_HOST = host;
        this.DB_STATUS = false;
        this.useSSL = info.getProperty("SSL", "f").equals("t") && Driver.sslEnabled();
        this.validTestString = info.getProperty("VALIDTESTSTRING", "select 1");
        this.checkDBLinkSql = Boolean.valueOf(info.getProperty("CHECKDBLINKSQL", "false"));
        this.sendBinaryTypeAsHex = Boolean.valueOf(info.getProperty("SENDBINARYTYPEASHEX", "false"));
        this.getIntWithPrecision = Boolean.valueOf(info.getProperty("GETINTWITHPRECISION", "false"));
        this.isResultSetZeroResend = Boolean.valueOf(info.getProperty("RESULTSETZERORESEND", "false"));
        this.useSlaveSynRead = Boolean.valueOf(info.getProperty("USESLAVESYNCREAD", "false"));
        String directToMain = info.getProperty("DIRECTTOMAINSQL", "");
        if (!"".equals(directToMain)) {
            this.directToMainArray = directToMain.split(";");
        }
        if (!"".equals(directToSlave = info.getProperty("DIRECTTOSLAVESQL", ""))) {
            this.directToSlaveArray = directToSlave.split(";");
        }
        this.clientEncoding = Encoding.getEncoding(System.getProperty("file.encoding"));
        this.compatibleOldProtocol = Boolean.valueOf(info.getProperty("COMPATIBLEOLDPROTOCOL", "false"));
        this.receiveStringByLen = Boolean.valueOf(info.getProperty("RECEIVESTRINGBYLEN", "true"));
        this.verifyPoolConnection = Boolean.valueOf(info.getProperty("VERIFYPOOLCONNECTION", "false"));
        this.useSlaveSyncReadGlobal = Boolean.valueOf(info.getProperty("USESLAVESYNCREADGLOBAL", "false"));
        this.slaveDelayTime = Long.valueOf(info.getProperty("SLAVEDELAYTIME", "0"));
        this.tempFileDir = info.getProperty("TEMPFILEDIR");
        try {
            this.protocolManager = this.timeOut_MilliSecond > 0 ? new ProtocolManager(this, this.DB_HOST, this.DB_PORT, this.DB_DATABASE, this.DB_USER, info, this.timeOut_MilliSecond) : new ProtocolManager(this, this.DB_HOST, this.DB_PORT, this.DB_DATABASE, this.DB_USER, info);
            this.protocolManager.startup(this.useSSL, this.compatibleOldProtocol);
            this.protocol = this.protocolManager.createProtocol(this.compatibleOldProtocol);
        }
        catch (IOException e) {
            throw new OSQLException("OSCAR-00201", "08001", 201, e.getMessage(), e);
        }
        catch (SQLException e) {
            if (this.protocolManager.oStream != null) {
                try {
                    this.protocolManager.oStream.close();
                }
                catch (IOException e1) {
                    // empty catch block
                }
            }
            throw e;
        }
        try {
            this.openConnectionV2(host, port, info, database, url, driver);
        }
        catch (SQLException ex) {
            block24: {
                try {
                    try {
                        this.protocol.close();
                    }
                    catch (SQLException ex1) {
                        Object var15_19 = null;
                        this.protocol = null;
                        break block24;
                    }
                    catch (IOException ex1) {
                        Object var15_20 = null;
                        this.protocol = null;
                    }
                    Object var15_18 = null;
                    this.protocol = null;
                }
                catch (Throwable throwable) {
                    Object var15_21 = null;
                    this.protocol = null;
                    throw throwable;
                }
            }
            throw ex;
        }
    }

    public String[] getDirectToMainArray() {
        return this.directToMainArray;
    }

    private void openConnectionV2(String host, int port, Properties info, String database, String url, Driver d) throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", openConnectionV2()");
        }
        if (this.protocol != null) {
            this.pid = this.protocol.getPID();
            this.ckey = this.protocol.getCKEY();
            this.setDBEncoding();
            this.prepareSimpleExecute = Boolean.valueOf(info.getProperty("PREPARESIMPLEEXECUTE", "true"));
            if (this.getProtocolVersion().getProtocolType() >= 2) {
                this.netDataByStr = Boolean.valueOf(info.getProperty("NETDATABYSTR", "false"));
                if (this.netDataByStr) {
                    this.execSQL("SET NET_DATA_BY_STR=TRUE");
                } else {
                    this.execSQL("SET NET_DATA_BY_STR=FALSE");
                }
                if (!this.netDataByStr) {
                    this.numericKeepPrecision = Boolean.valueOf(info.getProperty("NUMERICKEEPPRECISION", "true"));
                    if (this.numericKeepPrecision) {
                        this.execSQL("SET SEND_FLOATINGNUMBER_KEEP_PRECISION=TRUE");
                    } else {
                        this.execSQL("SET SEND_FLOATINGNUMBER_KEEP_PRECISION=FALSE");
                    }
                }
            }
            this.setTransactionIsolation(2);
            this.execSQL("SET AUTOCOMMIT TO TRUE");
            this.execSQL("SET DISPLAY_LOBLOCATOR=ON");
            this.batchBufferSize = Integer.parseInt(info.getProperty("BATCHBUFFERSIZE", "128"));
            if (this.batchBufferSize > 0x100000) {
                this.batchBufferSize = 0x100000;
            } else if (this.batchBufferSize < 1) {
                this.batchBufferSize = 1;
            }
            this.preparedCacheSize = Integer.parseInt(info.getProperty("PREPARECACHESIZE", "0"));
            if (this.preparedCacheSize == 0) {
                this.usePrepareCache = false;
            } else {
                this.setStatementCacheSize(this.preparedCacheSize);
                this.setImplicitCachingEnabled(true);
            }
            this.compatibleOracle = Boolean.valueOf(info.getProperty("COMPATIBLEORACLE", "false"));
            this.notRealPrepare = Boolean.valueOf(info.getProperty("NOTREALPREPARE", "false"));
            this.compatibleOldDateFormat = Boolean.valueOf(info.getProperty("COMPATIBLEOLDDATEFORMAT", "false"));
            boolean stmtRollback = Boolean.valueOf(info.getProperty("STMTROLLBACK", "true"));
            try {
                if (stmtRollback) {
                    this.execSQL("SET STMT_ROLLBACK=1");
                } else {
                    this.execSQL("SET STMT_ROLLBACK=0");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            String searchPath = info.getProperty("SEARCHPATH");
            if (null != searchPath) {
                this.execSQL("SET SEARCH_PATH='" + searchPath.toUpperCase() + "'");
            }
            this.statementFetchSize = Integer.parseInt(info.getProperty("FETCHSIZE", "16"));
            if (this.statementFetchSize > 5000) {
                this.statementFetchSize = 5000;
            }
            if (this.statementFetchSize < 0) {
                this.statementFetchSize = 0;
            }
            this.DB_STATUS = true;
            this.lobDisplayMaxSize = this.getLobDisplayMaxSize();
            this.isClearSt = Boolean.valueOf(info.getProperty("CLEANSTATEMENT", "false"));
            this.closed = false;
        }
    }

    @Override
    public boolean isPrepareSimpleExecute() {
        return this.prepareSimpleExecute;
    }

    public OscarJdbc2Connection() {
        this.executor = new QueryExecutor();
    }

    public Driver getDriver() {
        return this.this_driver;
    }

    @Override
    public void addWarning(String msg, String code) {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", addWarning()");
        }
        if (this.warnings != null) {
            this.warnings.setNextWarning(new SQLWarning(msg, code));
        } else {
            this.warnings = new SQLWarning(msg);
        }
    }

    @Override
    public BaseResultSet execSQL(String s) throws SQLException {
        if (stLogFlag || this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", execSQL(String s), paras: " + s);
        }
        return this.execSQL(s, this.getDefaultStatement());
    }

    @Override
    public BaseResultSet execSQL(String s, BaseStatement stmt) throws SQLException {
        return this.execSQL(s, stmt, (BaseResultSet)stmt.getResultSet());
    }

    @Override
    public BaseResultSet execSQL(String s, BaseStatement stmt, BaseResultSet res) throws SQLException {
        try {
            if (stLogFlag || this.logFlag) {
                Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", execSQL(String s, BaseStatement stmt)" + s);
            }
            if (this.protocol == null) {
                throw new OSQLException("OSCAR-00211", "08003", 211);
            }
            int maxRows = stmt.getMaxRows();
            return this.protocol.query(s, maxRows, stmt, res);
        }
        catch (SQLException e) {
            this.checkConnectionClosed(e);
            throw e;
        }
    }

    @Override
    public void setCursorName(String cursor) throws SQLException {
        this.cursor = cursor;
    }

    @Override
    public String getCursorName() throws SQLException {
        return this.cursor;
    }

    public String getURL() {
        return this.this_url;
    }

    public String getUserName() throws SQLException {
        if (this.DB_USER.length() > 1 && this.DB_USER.charAt(0) == '\"' && this.DB_USER.charAt(this.DB_USER.length() - 1) == '\"') {
            return this.DB_USER.substring(1, this.DB_USER.length() - 1);
        }
        return this.DB_USER.toUpperCase();
    }

    @Override
    public Encoding getClientEncoding() {
        return this.clientEncoding;
    }

    @Override
    public void setClientEncoding(String encode) {
        if (encode == null || "".equals(encode)) {
            return;
        }
        this.clientEncoding = Encoding.getEncoding(encode);
    }

    @Override
    public Encoding getEncoding() {
        return this.dbEncoding;
    }

    @Override
    public Fastpath getFastpathAPI() throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", getFastpathAPI()");
        }
        if (this.fastpath == null) {
            this.fastpath = new Fastpath(this);
        }
        return this.fastpath;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public synchronized void close() throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", close()");
        }
        if (this.closed) {
            return;
        }
        if (this.preparedList != null) {
            Iterator i = this.preparedList.iterator();
            while (i.hasNext()) {
                PreparedInfo pinfo = (PreparedInfo)i.next();
                this.execSQL("DEALLOCATE PREPARE " + pinfo.getStatementName());
                i.remove();
            }
        }
        if (this.getImplicitCachingEnabled() && this.getStatementCacheSize() > 0) {
            this.statementCache.close();
        }
        if (this.stList != null && this.stList.size() > 0) {
            for (int i = 0; i < this.stList.size(); ++i) {
                if (this.stList.get(i) == null) continue;
                ((Statement)this.stList.get(i)).close();
            }
        }
        this.stmt = null;
        this.clientEncoding = null;
        this.dbEncoding = null;
        this.planIDVec = null;
        this.this_driver = null;
        this.this_url = null;
        this.cursor = null;
        this.fastpath = null;
        this.metadata = null;
        this.preparedList = null;
        this.stList = null;
        if (this.protocol != null) {
            try {
                try {
                    this.protocol.close();
                }
                catch (IOException iOException) {
                    Object var4_6 = null;
                    this.protocol = null;
                }
                Object var4_5 = null;
                this.protocol = null;
            }
            catch (Throwable throwable) {
                Object var4_7 = null;
                this.protocol = null;
                throw throwable;
            }
        }
        if (this.cluster != null) {
            this.cluster.close();
        }
        this.cluster = null;
        this.closed = true;
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        if (sql == null) {
            return null;
        }
        Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, true);
        if (escapedSqlResult instanceof String) {
            return (String)escapedSqlResult;
        }
        return ((EscapeProcessorResult)escapedSqlResult).escapedSql;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.warnings;
    }

    @Override
    public void clearWarnings() throws SQLException {
        this.warnings = null;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return this.readOnly;
    }

    @Override
    public void setInTranscation(boolean tran) {
        this.inTransaction = tran;
    }

    @Override
    public boolean isInTransaction() {
        return this.inTransaction;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", setAutoCommit(boolean autoCommit), parameters: " + autoCommit);
        }
        if (this.autoCommit == autoCommit) {
            return;
        }
        if (autoCommit) {
            this.execSQL("commit");
        } else {
            this.execSQL("begin");
        }
        this.autoCommit = autoCommit;
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return this.autoCommit;
    }

    @Override
    public void commit() throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", commit() autoCommit=" + this.autoCommit);
        }
        if (!this.autoCommit) {
            this.closeCursor();
            if (this.holdability == 2) {
                this.releasePlanID();
            }
            this.execSQL("commit");
            try {
                this.execSQL("begin");
            }
            catch (SQLException e) {
                if (e instanceof OSQLException) {
                    ((OSQLException)e).setExtraState(1);
                }
                throw e;
            }
        }
    }

    @Override
    public synchronized void rollback() throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", rollback()");
        }
        if (!this.autoCommit) {
            this.closeCursor();
            if (this.holdability == 2) {
                this.releasePlanID();
            }
            this.execSQL("rollback");
            this.execSQL("begin");
        }
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return this.isolationLevel;
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", setTransactionIsolation(int level), paras: " + level);
        }
        String isolationLevelSQL = "SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL ";
        switch (level) {
            case 1: {
                isolationLevelSQL = isolationLevelSQL + "READ UNCOMMITTED";
                this.isolationLevel = 1;
                break;
            }
            case 2: {
                isolationLevelSQL = isolationLevelSQL + "READ COMMITTED";
                this.isolationLevel = 2;
                break;
            }
            case 4: {
                isolationLevelSQL = isolationLevelSQL + "REPEATABLE READ";
                this.isolationLevel = 4;
                break;
            }
            case 8: {
                isolationLevelSQL = isolationLevelSQL + "SERIALIZABLE";
                this.isolationLevel = 8;
                break;
            }
            default: {
                throw new OSQLException("OSCAR-00204", "88888", 204);
            }
        }
        this.execSQL(isolationLevelSQL);
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
    }

    @Override
    public String getCatalog() throws SQLException {
        return null;
    }

    @Override
    public synchronized int getSQLType(int oid) throws SQLException {
        Integer sqlType = (Integer)sqlTypeCache.get(new Integer(oid));
        if (sqlType == null) {
            String sql = "SELECT typname FROM INFO_SCHEM.sys_type WHERE oid = " + oid;
            BaseResultSet result = this.execSQL(sql);
            if (result.getColumnCount() != 1 || result.getTupleCount() != 1) {
                throw new OSQLException("OSCAR-00205", "88888", 205);
            }
            result.next();
            String DBType = result.getString(1);
            result.close();
            Integer iOid = new Integer(oid);
            sqlType = new Integer(this.getSQLType(DBType));
            sqlTypeCache.put(iOid, sqlType);
            DBTypeCache.put(iOid, DBType);
        }
        return sqlType;
    }

    @Override
    public int getOscarType(int oid) throws SQLException {
        Integer sqlType = (Integer)oscarTypeCache.get(oid);
        if (sqlType == null) {
            return 0;
        }
        return sqlType;
    }

    @Override
    public int getDBTypeOid(String typeName) throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", getDBTypeOid(String typeName), paras: " + typeName);
        }
        int oid = -1;
        if (typeName != null) {
            Integer oidValue = (Integer)typeOidCache.get(typeName);
            if (oidValue != null) {
                oid = oidValue;
            } else {
                String sql = "SELECT oid FROM INFO_SCHEM.sys_type WHERE typname='" + typeName + "'";
                BaseResultSet result = this.execSQL(sql);
                if (result.getColumnCount() != 1 || result.getTupleCount() != 1) {
                    throw new OSQLException("OSCAR-00205", "88888", 205);
                }
                result.next();
                oid = Integer.parseInt(result.getString(1));
                typeOidCache.put(typeName, new Integer(oid));
                result.close();
            }
        }
        return oid;
    }

    @Override
    public synchronized String getDBType(int oid) throws SQLException {
        String DBType;
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", getDBType(int oid), paras: " + oid);
        }
        if ((DBType = (String)DBTypeCache.get(new Integer(oid))) == null) {
            String sql = "SELECT typname FROM INFO_SCHEM.sys_type WHERE oid = " + oid;
            BaseResultSet result = this.execSQL(sql);
            if (result.getColumnCount() != 1 || result.getTupleCount() != 1) {
                throw new OSQLException("OSCAR-00205", "88888", 205);
            }
            result.next();
            DBType = result.getString(1);
            result.close();
            DBTypeCache.put(new Integer(oid), DBType);
            sqlTypeCache.put(new Integer(oid), new Integer(12));
        }
        return DBType;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.closed;
    }

    @Override
    public synchronized int getSQLType(String DBTypeName) {
        int defaultType = 12;
        if (jdbcTypes.get(DBTypeName) != null) {
            defaultType = (Integer)jdbcTypes.get(DBTypeName);
        }
        return defaultType;
    }

    @Override
    public void cancelQuery() throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", cancelQuery()");
        }
        if (null == this.protocol) {
            return;
        }
        try {
            this.protocol.cancelRequest(this.pid, this.ckey);
        }
        catch (IOException e) {
            throw new OSQLException("OSCAR-00206", "08001", 206, e.getMessage(), e);
        }
    }

    @Override
    public void addCursor(String cursorName) {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", addCursor(String cursorName), paras: " + cursorName);
        }
        if (this.cursorVec == null) {
            this.cursorVec = new Vector();
        }
        if (cursorName != null && !"".equals(cursorName)) {
            this.cursorVec.add(cursorName);
        }
    }

    @Override
    public void addPlanID(byte[] planID) {
        if (this.logFlag && planID != null) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", addCursor(String cursorName), paras: " + planID[0] + planID[1]);
        }
        if (this.planIDVec == null) {
            this.planIDVec = new Vector();
        }
        int index = -1;
        if (planID != null && (index = this.checkPlanID(planID)) == -1) {
            this.planIDVec.add(planID);
        }
    }

    @Override
    public void removePlanID(int index) {
        if (this.planIDVec != null && 0 <= index && index < this.planIDVec.size()) {
            this.planIDVec.remove(index);
        }
    }

    private synchronized void closeCursor() throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", closeCursor()");
        }
        if (this.cursorVec != null) {
            StringBuffer sql = new StringBuffer();
            int size = this.cursorVec.size();
            String cursorName = null;
            for (int i = size - 1; i >= 0; --i) {
                cursorName = (String)this.cursorVec.remove(i);
                sql.delete(0, sql.length());
                sql.append("CLOSE ").append(cursorName).append(";DEALLOCATE ").append(cursorName);
                this.execSQL(sql.toString(), this.stmt);
            }
        }
    }

    @Override
    public synchronized void closeCursor(String cursorName, BaseStatement statement) throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ",modify closeCursor(cursorName,statement)");
        }
        if (this.cursorVec != null && this.cursorVec.contains(cursorName)) {
            this.cursorVec.remove(cursorName);
            if (!this.isClosed()) {
                StringBuffer sql = new StringBuffer();
                sql.delete(0, sql.length());
                sql.append("CLOSE ").append(cursorName).append(";DEALLOCATE ").append(cursorName);
                this.execSQL(sql.toString(), this.stmt);
            }
        }
    }

    private void releasePlanID() throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", releasePlanID()");
        }
        if (this.planIDVec != null) {
            int size = this.planIDVec.size();
            byte[] planID = null;
            for (int i = size - 1; i >= 0; --i) {
                planID = (byte[])this.planIDVec.remove(i);
                ((OSCARProtocolV2)this.getProtocol()).fetchMore(null, null, null, null, planID, 0, 0, true, false, this.getDefaultStatement(), false);
            }
        }
    }

    @Override
    public void removeCursor(String cursorName) {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", removeCursor(String cursorName), paras: " + cursorName);
        }
        if (this.cursorVec != null) {
            this.cursorVec.remove(cursorName);
        }
    }

    @Override
    public boolean hasCursor(String cursorName) {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ", hasCursor(String cursorName), paras: " + cursorName);
        }
        boolean hasCur = false;
        if (this.cursorVec == null) {
            return hasCur;
        }
        if (cursorName == null) {
            return hasCur;
        }
        hasCur = this.cursorVec.contains(cursorName);
        return hasCur;
    }

    @Override
    public BaseStatement getDefaultStatement() throws SQLException {
        if (this.stmt == null) {
            this.stmt = (BaseStatement)((Object)this.createStatement(1003, 1007, this.holdability));
        }
        return this.stmt;
    }

    @Override
    public int getPreapredCacheSize() {
        return this.preparedCacheSize;
    }

    @Override
    public Object lookupCachedPrepare(String sql) {
        if (this.preparedList == null || sql == null) {
            return null;
        }
        Iterator i = this.preparedList.iterator();
        while (i.hasNext()) {
            PreparedInfo pinfo = (PreparedInfo)i.next();
            if (!pinfo.getSql().equals(sql)) continue;
            i.remove();
            this.preparedList.add(pinfo);
            return pinfo;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addNewPrepare(Object p) throws SQLException {
        List list = this.preparedList;
        synchronized (list) {
            PreparedInfo pinfo;
            if (this.preparedList.size() >= this.preparedCacheSize && (pinfo = (PreparedInfo)this.preparedList.remove(0)).getReference() <= 0) {
                this.execSQL("DEALLOCATE PREPARE " + pinfo.getStatementName());
            }
            this.preparedList.add(p);
        }
    }

    @Override
    public Statement createStatement() throws SQLException {
        return this.createStatement(1003, 1007);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return this.prepareStatement(sql, 1003, 1007);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return this.prepareCall(sql, 1003, 1007);
    }

    public Map getTypeMap() throws SQLException {
        return this.typemap;
    }

    public void setTypeMap(Map map) throws SQLException {
        this.typemap = map;
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        if (holdability != 2 && holdability != 1) {
            throw new OSQLException("OSCAR-00207", "88888", 207);
        }
        this.holdability = holdability;
    }

    @Override
    public int getHoldability() throws SQLException {
        return this.holdability;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        if (this.autoCommit) {
            throw new OSQLException("OSCAR-00208", "25000", 208);
        }
        return OSCARSavepoint.setSavepoint(this);
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        if (this.autoCommit) {
            throw new OSQLException("OSCAR-00208", "25000", 208);
        }
        return OSCARSavepoint.setSavepoint(name, this);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        if (this.autoCommit) {
            throw new OSQLException("OSCAR-00209", "25000", 209);
        }
        OSCARSavepoint.rollbackSavepoint(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        if (this.autoCommit) {
            throw new OSQLException("OSCAR-00209", "25000", 209);
        }
        OSCARSavepoint.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return this.createStatement(resultSetType, resultSetConcurrency, this.getHoldability());
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return this.prepareStatement(sql, resultSetType, resultSetConcurrency, this.getHoldability());
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return this.prepareCall(sql, resultSetType, resultSetConcurrency, this.getHoldability());
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        if (autoGeneratedKeys == 2) {
            this.autoGeneratedInfo = -1;
        } else if (autoGeneratedKeys == 1) {
            this.autoGeneratedInfo = 1;
        } else {
            throw new OSQLException("OSCAR-00210", "88888", 210);
        }
        return this.prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        this.autoGeneratedInfo = 2;
        if (columnIndexes != null) {
            this.columnIndex = new int[columnIndexes.length];
            for (int i = 0; i < columnIndexes.length; ++i) {
                this.columnIndex[i] = columnIndexes[i];
            }
        }
        return this.prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        this.autoGeneratedInfo = 3;
        if (columnNames != null) {
            this.columnName = new String[columnNames.length];
            for (int i = 0; i < columnNames.length; ++i) {
                this.columnName[i] = columnNames[i];
            }
        }
        return this.prepareStatement(sql);
    }

    @Override
    public OscarBfile getBfileInstance(String locatorStr) throws SQLException {
        return OscarBfile.createByLocator(this, locatorStr);
    }

    @Override
    public OscarBlob getBlobInstance(String locatorStr) throws SQLException {
        return OscarBlob.createByLocator(this, locatorStr);
    }

    @Override
    public OscarClob getClobInstance(String locatorStr) throws SQLException {
        return OscarClob.createByLocator(this, locatorStr);
    }

    @Override
    public OscarBlob createBlob() throws SQLException {
        return this.createTempBlob(true, 0);
    }

    @Override
    public OscarBlob createTempBlob(boolean cache, int duration) throws SQLException {
        return OscarBlob.createTemporary(this, cache, duration);
    }

    @Override
    public OscarClob createClob() throws SQLException {
        return this.createTempClob(true, 0);
    }

    @Override
    public OscarClob createTempClob(boolean cache, int duration) throws SQLException {
        return OscarClob.createTemporary(this, cache, duration);
    }

    @Override
    public OSCARTransfer getTransfer() {
        return new OSCARTransfer(this);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ",createStatement, paras: resultSetType: " + resultSetType + ", concurrency: " + resultSetConcurrency + ", holdability: " + resultSetHoldability);
        }
        if (resultSetType != 1003 && resultSetType != 1004 && resultSetType != 1005) {
            throw new OSQLException("OSCAR-00422", "88888", 422);
        }
        if (resultSetConcurrency != 1007 && resultSetConcurrency != 1008) {
            throw new OSQLException("OSCAR-00423", "88888", 423);
        }
        if (resultSetHoldability != 2 && resultSetHoldability != 1) {
            throw new OSQLException("OSCAR-00207", "88888", 207);
        }
        if (this.stList == null) {
            this.stList = new LinkedList();
        }
        if (this.version.getProtocolType() == 1) {
            OscarStatement s = new OscarStatement(this);
            s.setResultSetType(resultSetType);
            s.setResultSetConcurrency(resultSetConcurrency);
            s.setResultSetHoldability(resultSetHoldability);
            s.setFetchSize(this.statementFetchSize);
            this.setHoldability(resultSetHoldability);
            if (this.isClearSt) {
                this.stList.add(s);
            }
            return s;
        }
        OscarStatementV2 s = new OscarStatementV2(this);
        s.setResultSetType(resultSetType);
        s.setResultSetConcurrency(resultSetConcurrency);
        s.setResultSetHoldability(resultSetHoldability);
        s.setFetchSize(this.statementFetchSize);
        this.setHoldability(resultSetHoldability);
        if (this.isClearSt) {
            this.stList.add(s);
        }
        return s;
    }

    boolean isValidPrepare(String prepareName) throws SQLException {
        if (this.closed) {
            return false;
        }
        int paraCount = 1;
        int[] paraLength = new int[paraCount];
        Object[] paraValue = new Object[paraCount];
        paraValue[0] = prepareName.getBytes();
        paraLength[0] = ((byte[])paraValue[0]).length;
        Object[] result = this.getProtocol().functionCall(4638, paraCount, paraLength, paraValue);
        if (!((Boolean)result[0]).booleanValue()) {
            int sz = (Integer)result[1];
            Integer isValid = new Integer(BasePacket.bytesToIntR((byte[])result[2], sz));
            return isValid.equals(new Integer(1));
        }
        return false;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        sql = sql.trim();
        if (stLogFlag || this.logFlag) {
            Driver.writeLog("session: " + this.sessionID + ", " + OscarJdbc2Connection.class + ",prepareStatement, paras: sql: " + sql + ", resultSetType: " + resultSetType + ", concurrency: " + resultSetConcurrency + ", holdability: " + resultSetHoldability);
        }
        if (resultSetType != 1003 && resultSetType != 1004 && resultSetType != 1005) {
            throw new OSQLException("OSCAR-00422", "88888", 422);
        }
        if (resultSetConcurrency != 1007 && resultSetConcurrency != 1008) {
            throw new OSQLException("OSCAR-00423", "88888", 423);
        }
        if (resultSetHoldability != 2 && resultSetHoldability != 1) {
            throw new OSQLException("OSCAR-00207", "88888", 207);
        }
        if (this.stList == null) {
            this.stList = new LinkedList();
        }
        if (this.version.getProtocolType() == 1) {
            OscarStatement s = null;
            if (this.statementCache != null) {
                s = (OscarPreparedStatement)this.statementCache.searchImplicitCache(sql, 1, resultSetType);
            }
            if (s == null) {
                s = new OscarPreparedStatement(this, sql);
                if (this.autoGeneratedInfo != -1) {
                    s.setAutoGeneratedInfo(this.autoGeneratedInfo);
                    s.setAutoGeneratedIndexes(this.columnIndex);
                    s.setAutoGeneratedNames(this.columnName);
                }
                if (resultSetConcurrency == 1008) {
                    s.setUseTid(true);
                }
                if (s.isAnonymous()) {
                    if (this.isClearSt) {
                        this.stList.add(s);
                    }
                    return s;
                }
                s.prepare();
            }
            s.setFetchSize(this.statementFetchSize);
            s.setResultSetType(resultSetType);
            s.setResultSetConcurrency(resultSetConcurrency);
            s.setResultSetHoldability(resultSetHoldability);
            if (this.isClearSt) {
                this.stList.add(s);
            }
            return s;
        }
        OscarStatement s = null;
        if (this.statementCache != null) {
            s = (OscarPreparedStatementV2)this.statementCache.searchImplicitCache(sql, 1, resultSetType);
        }
        if (s == null) {
            s = new OscarPreparedStatementV2(this, sql);
            if (this.autoGeneratedInfo != -1) {
                s.setAutoGeneratedInfo(this.autoGeneratedInfo);
                s.setAutoGeneratedIndexes(this.columnIndex);
                s.setAutoGeneratedNames(this.columnName);
            }
            if (resultSetConcurrency == 1008) {
                s.setUseTid(true);
            }
            if (s.isAnonymous()) {
                if (this.isClearSt) {
                    this.stList.add(s);
                }
                return s;
            }
            ((OscarStatementV2)s).prepare();
        }
        s.setFetchSize(this.statementFetchSize);
        s.setResultSetType(resultSetType);
        s.setResultSetConcurrency(resultSetConcurrency);
        s.setResultSetHoldability(resultSetHoldability);
        ((OscarStatementV2)s).setLobDisplayMaxSize(this.lobDisplayMaxSize);
        if (this.isClearSt) {
            this.stList.add(s);
        }
        return s;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (resultSetType != 1003 && resultSetType != 1004 && resultSetType != 1005) {
            throw new OSQLException("OSCAR-00422", "88888", 422);
        }
        if (resultSetConcurrency != 1007 && resultSetConcurrency != 1008) {
            throw new OSQLException("OSCAR-00423", "88888", 423);
        }
        if (resultSetHoldability != 2 && resultSetHoldability != 1) {
            throw new OSQLException("OSCAR-00207", "88888", 207);
        }
        if (this.stList == null) {
            this.stList = new LinkedList();
        }
        if (this.version.getProtocolType() == 1) {
            OscarCallableStatement s = new OscarCallableStatement(this, sql);
            s.setResultSetType(resultSetType);
            s.setResultSetConcurrency(resultSetConcurrency);
            s.setResultSetHoldability(resultSetHoldability);
            s.setFetchSize(this.statementFetchSize);
            this.setHoldability(resultSetHoldability);
            s.prepare();
            if (this.isClearSt) {
                this.stList.add(s);
            }
            return s;
        }
        OscarCallableStatementV2 s = new OscarCallableStatementV2(this, sql);
        s.setResultSetType(resultSetType);
        s.setResultSetConcurrency(resultSetConcurrency);
        s.setResultSetHoldability(resultSetHoldability);
        s.setFetchSize(this.statementFetchSize);
        this.setHoldability(resultSetHoldability);
        s.prepare();
        s.setLobDisplayMaxSize(this.lobDisplayMaxSize);
        if (this.isClearSt) {
            this.stList.add(s);
        }
        return s;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        if (this.metadata == null) {
            this.metadata = new OscarDatabaseMetaData(this);
        }
        return this.metadata;
    }

    @Override
    public long getAccessHandle() {
        this.randNum = (this.randNum * 1103515245L + 12345L) % 0x40000000L;
        return this.randNum;
    }

    @Override
    public void setSeed(long seed) {
        this.randNum = seed % 0x40000000L;
    }

    @Override
    public VersionConfig getVersion() {
        return this.version.getVersion();
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.version;
    }

    @Override
    public void setVersion(ProtocolVersion version) {
        this.version = version;
    }

    @Override
    public boolean isUsePrepareCache() {
        return this.usePrepareCache;
    }

    @Override
    public boolean isCompatibleOracle() {
        return this.compatibleOracle;
    }

    public void setCompatibleOracle(boolean compatibleOracle) {
        this.compatibleOracle = compatibleOracle;
    }

    @Override
    public boolean isNotRealPrepare() {
        return this.notRealPrepare;
    }

    public List getPreparedList() {
        return this.preparedList;
    }

    @Override
    public QueryExecutor getQueryExecutor() {
        return this.executor;
    }

    public int getStatementFetchSize() {
        return this.statementFetchSize;
    }

    @Override
    public boolean isCompressTransfer() {
        return this.compressTransfer;
    }

    @Override
    public void setCompressTransfer(boolean compressTransfer) {
    }

    @Override
    public ImportHandler createImportHandler(String tableName) throws SQLException {
        if (this.getProtocolVersion().isMpp5()) {
            return this.createDistributeImportHandler(tableName);
        }
        return new OscarImportHandler(this, null, tableName);
    }

    @Override
    public ImportHandler createImportHandler() throws SQLException {
        return new OscarImportHandler(this);
    }

    @Override
    public ImportHandler createImportHandler(String schemName, String tableName) throws SQLException {
        if (this.getProtocolVersion().isMpp5()) {
            return this.createDistributeImportHandler(schemName, tableName);
        }
        return new OscarImportHandler(this, schemName, tableName);
    }

    @Override
    public ProviderImportHandler createProviderImportHandler(String tableName) throws SQLException {
        return new OscarProviderImportHandle(this, tableName);
    }

    @Override
    public ProviderImportHandler createProviderImportHandler(String schemName, String tableName) throws SQLException {
        return new OscarProviderImportHandle(this, schemName, tableName);
    }

    @Override
    public ExportHandler createExportHandler() throws SQLException {
        return new OscarExportHandler(this);
    }

    @Override
    public int getBatchBufferSize() {
        return this.batchBufferSize;
    }

    @Override
    public void setBatchBufferSize(int defaultBatchBufferSize) {
        if (defaultBatchBufferSize > 0x100000) {
            defaultBatchBufferSize = 0x100000;
        } else if (defaultBatchBufferSize < 1) {
            defaultBatchBufferSize = 1;
        }
        this.batchBufferSize = defaultBatchBufferSize;
    }

    @Override
    public int getEndianType() {
        return this.endianType;
    }

    @Override
    public void setEndianType(int endianType) {
        this.endianType = endianType;
    }

    @Override
    public int checkPlanID(byte[] planID) {
        if (this.planIDVec != null) {
            byte[] tmp = null;
            for (int i = 0; i < this.planIDVec.size(); ++i) {
                tmp = (byte[])this.planIDVec.get(i);
                if (!Arrays.equals(planID, tmp)) continue;
                return i;
            }
        }
        return -1;
    }

    @Override
    public Integer getSessionID() {
        return this.sessionID;
    }

    @Override
    public int getPlanID() throws SQLException {
        return this.getProtocol().getPID();
    }

    @Override
    public boolean isNetDataByStr() {
        return this.netDataByStr;
    }

    @Override
    public boolean isNumericKeepPrecision() {
        return this.numericKeepPrecision;
    }

    public boolean isDB_STATUS() {
        return this.DB_STATUS;
    }

    public int getStatementCacheSize() throws SQLException {
        return this.statementCache.getCacheSize();
    }

    public void setStatementCacheSize(int size) throws SQLException {
        if (this.statementCache == null) {
            this.statementCache = this.version.getProtocolType() == 1 ? new LRUStatementCache(size) : new LRUStatementCacheV2(size);
        } else {
            this.statementCache.resize(size);
        }
    }

    public boolean getImplicitCachingEnabled() throws SQLException {
        if (this.statementCache == null) {
            return false;
        }
        return this.statementCache.getImplicitCachingEnabled();
    }

    public void setImplicitCachingEnabled(boolean cacheEnabled) throws SQLException {
        if (this.statementCache == null) {
            this.statementCache = this.version.getProtocolType() == 1 ? new LRUStatementCache(0) : new LRUStatementCacheV2(0);
        }
        this.statementCache.setImplicitCachingEnabled(cacheEnabled);
    }

    public boolean isStatementCacheInitialized() {
        if (this.statementCache == null) {
            return false;
        }
        return this.statementCache.getCacheSize() != 0;
    }

    public final void purgeStatementCache() throws SQLException {
        if (this.isStatementCacheInitialized()) {
            this.statementCache.purgeImplicitCache();
        }
    }

    public final void closeStatementCache() throws SQLException {
        if (this.isStatementCacheInitialized()) {
            this.statementCache.close();
            this.statementCache = null;
        }
    }

    public synchronized void cacheImplicitStatement(OscarPreparedStatement paramOraclePreparedStatement, String sql, int statementType, int resultSetType) throws SQLException {
        if (this.statementCache == null) {
            throw new SQLException("The statement cache is not enabled.", "");
        }
        this.statementCache.addToImplicitCache(paramOraclePreparedStatement, sql, statementType, resultSetType);
    }

    public synchronized void cacheImplicitStatement(OscarPreparedStatementV2 paramOraclePreparedStatementV2, String sql, int statementType, int resultSetType) throws SQLException {
        if (this.statementCache == null) {
            throw new SQLException("The statement cache is not enabled.", "");
        }
        this.statementCache.addToImplicitCache(paramOraclePreparedStatementV2, sql, statementType, resultSetType);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    protected int getLobDisplayMaxSize() {
        block27: {
            if (this.lobDisplayMaxSize == -1) {
                Exception e2222;
                BaseStatement temp;
                BaseResultSet rs;
                block25: {
                    block24: {
                        rs = null;
                        temp = null;
                        temp = (BaseStatement)((Object)this.createStatement());
                        rs = this.execSQL("show LOB_DISPLAY_MAX_SIZE", temp);
                        if (rs.next()) {
                            this.lobDisplayMaxSize = rs.getInt(2);
                            break block24;
                        }
                        this.lobDisplayMaxSize = 4000;
                    }
                    Object var5_3 = null;
                    if (rs == null) break block25;
                    try {
                        rs.close();
                    }
                    catch (Exception e2222) {
                        // empty catch block
                    }
                }
                if (temp != null) {
                    try {
                        ((Statement)((Object)temp)).close();
                    }
                    catch (Exception e2222) {}
                }
                break block27;
                {
                    catch (SQLException sqle) {
                        Exception e2222;
                        this.lobDisplayMaxSize = 4000;
                        Object var5_4 = null;
                        if (rs != null) {
                            try {
                                rs.close();
                            }
                            catch (Exception e2222) {
                                // empty catch block
                            }
                        }
                        if (temp != null) {
                            try {
                                ((Statement)((Object)temp)).close();
                            }
                            catch (Exception e2222) {}
                        }
                        break block27;
                    }
                    catch (Exception e3) {
                        Exception e2222;
                        this.lobDisplayMaxSize = 4000;
                        Object var5_5 = null;
                        if (rs != null) {
                            try {
                                rs.close();
                            }
                            catch (Exception e2222) {
                                // empty catch block
                            }
                        }
                        if (temp != null) {
                            try {
                                ((Statement)((Object)temp)).close();
                            }
                            catch (Exception e2222) {}
                        }
                    }
                }
                catch (Throwable throwable) {
                    Exception e2222;
                    Object var5_6 = null;
                    if (rs != null) {
                        try {
                            rs.close();
                        }
                        catch (Exception e2222) {
                            // empty catch block
                        }
                    }
                    if (temp != null) {
                        try {
                            ((Statement)((Object)temp)).close();
                        }
                        catch (Exception e2222) {
                            // empty catch block
                        }
                    }
                    throw throwable;
                }
            }
        }
        return this.lobDisplayMaxSize;
    }

    @Override
    public boolean isTcpKeepAlive() {
        boolean tcpKeepAlive = false;
        Socket currentSocket = this.protocol.oStream.getCurrentSocket();
        if (currentSocket != null) {
            try {
                tcpKeepAlive = currentSocket.getKeepAlive();
            }
            catch (SocketException e) {
                Driver.writeLog(e.getMessage());
            }
        }
        return tcpKeepAlive;
    }

    @Override
    public void setTcpKeepAlive(boolean tcpKeepAlive) {
        Socket currentSocket = this.protocol.oStream.getCurrentSocket();
        if (currentSocket != null) {
            try {
                currentSocket.setKeepAlive(tcpKeepAlive);
            }
            catch (SocketException e) {
                Driver.writeLog(e.getMessage());
            }
        }
    }

    @Override
    public boolean isValidate() {
        return this.isValidate(1000);
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        if (timeout < 0) {
            throw new OSQLException("OSCAR-00124", "88888", 124);
        }
        if (this.logFlag) {
            Driver.writeLog("isValid(int timeout) start, timeout=" + timeout);
        }
        return this.isValidate(timeout * 1000);
    }

    @Override
    public boolean isValid() throws SQLException {
        return this.isValid(10);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    @Override
    public boolean isValidate(int timeout) {
        boolean isValidate;
        block22: {
            Statement stmt;
            block21: {
                if (this.closed) {
                    return false;
                }
                stmt = null;
                isValidate = false;
                if (this.logFlag) {
                    Driver.writeLog("isValidate(int timeout) start, timeout(ms)=" + timeout);
                }
                this.protocol.oStream.setSocketTimeOut(timeout);
                stmt = this.createStatement();
                stmt.executeQuery(this.validTestString);
                this.protocol.oStream.setSocketTimeOut(this.timeOut_MilliSecond);
                isValidate = true;
                if (!this.logFlag) break block21;
                Driver.writeLog("isValidate(int timeout): connection is valid");
            }
            Object var6_4 = null;
            try {
                if (stmt != null) {
                    stmt.close();
                }
                break block22;
            }
            catch (SQLException e2) {
                if (this.logFlag) {
                    Driver.writeLog("isValidate(int timeout):close statement SQLException" + e2.getMessage());
                }
                break block22;
            }
            {
                catch (SocketException e) {
                    if (this.logFlag) {
                        Driver.writeLog("isValidate(int timeout): SocketException " + e.getMessage());
                    }
                    Object var6_5 = null;
                    try {
                        if (stmt != null) {
                            stmt.close();
                        }
                        break block22;
                    }
                    catch (SQLException e2) {
                        if (this.logFlag) {
                            Driver.writeLog("isValidate(int timeout):close statement SQLException" + e2.getMessage());
                        }
                        break block22;
                    }
                }
                catch (SQLException e) {
                    if (this.logFlag) {
                        Driver.writeLog("isValidate(int timeout): SQLException" + e.getMessage());
                    }
                    Object var6_6 = null;
                    try {
                        if (stmt != null) {
                            stmt.close();
                        }
                        break block22;
                    }
                    catch (SQLException e2) {
                        if (this.logFlag) {
                            Driver.writeLog("isValidate(int timeout):close statement SQLException" + e2.getMessage());
                        }
                    }
                }
            }
            catch (Throwable throwable) {
                block23: {
                    Object var6_7 = null;
                    try {
                        if (stmt != null) {
                            stmt.close();
                        }
                    }
                    catch (SQLException e2) {
                        if (!this.logFlag) break block23;
                        Driver.writeLog("isValidate(int timeout):close statement SQLException" + e2.getMessage());
                    }
                }
                throw throwable;
            }
        }
        return isValidate;
    }

    @Override
    public boolean isVerifyPoolConnection() {
        return this.verifyPoolConnection;
    }

    /*
     * Loose catch block
     */
    protected void setDBEncoding() throws SQLException {
        block18: {
            Exception e22;
            BaseStatement temp;
            BaseResultSet rs;
            block17: {
                block16: {
                    rs = null;
                    temp = null;
                    temp = (BaseStatement)((Object)this.createStatement());
                    rs = this.execSQL("select ENCODING from v_sys_database ", temp);
                    if (rs != null && rs.next()) {
                        this.dbEncoding = Encoding.getEncoding(new Integer(rs.getInt(1)));
                        break block16;
                    }
                    this.dbEncoding = this.clientEncoding;
                }
                Object var5_3 = null;
                if (rs == null) break block17;
                try {
                    rs.close();
                }
                catch (Exception e22) {
                    // empty catch block
                }
            }
            if (temp != null) {
                try {
                    ((Statement)((Object)temp)).close();
                }
                catch (Exception e22) {}
            }
            break block18;
            {
                catch (UnsupportedEncodingException e3) {
                    throw new OSQLException("OSCAR-00903", "88888", 903);
                }
                catch (SQLException e4) {
                    throw e4;
                }
                catch (Exception e5) {
                    throw new SQLException(e5.getMessage());
                }
            }
            catch (Throwable throwable) {
                Exception e22;
                Object var5_4 = null;
                if (rs != null) {
                    try {
                        rs.close();
                    }
                    catch (Exception e22) {
                        // empty catch block
                    }
                }
                if (temp != null) {
                    try {
                        ((Statement)((Object)temp)).close();
                    }
                    catch (Exception e22) {
                        // empty catch block
                    }
                }
                throw throwable;
            }
        }
    }

    @Override
    public int getBatchCount() {
        return this.batchCount;
    }

    @Override
    public boolean isUseAsynBatch() {
        return this.useAsynBatch;
    }

    @Override
    public boolean isReceiveStringByLen() {
        return this.receiveStringByLen;
    }

    @Override
    public void setMasterConnection(Connection conn) {
        this.masterConnection = conn;
    }

    @Override
    public Connection getMasterConnection() {
        if (this.masterConnection == null) {
            return this;
        }
        return this.masterConnection;
    }

    @Override
    public Properties getConnectionProperties() {
        return this.connectionProperty;
    }

    public long getLsnValue() {
        return this.lsnValue;
    }

    public void setLsnValue(long lsnValue) {
        this.lsnValue = lsnValue;
    }

    @Override
    public void setTransStatus(int transStatus) {
        this.transStatus = transStatus;
    }

    @Override
    public int getTransStatus() {
        return this.transStatus;
    }

    public DispatchConnection getDispatchConn() {
        return this.dispatchConn;
    }

    public void setDispatchConn(DispatchConnection dispatchConn) {
        this.dispatchConn = dispatchConn;
    }

    @Override
    public boolean isCompatibleOldDateFormat() {
        return this.compatibleOldDateFormat;
    }

    @Override
    public boolean isSlave() {
        return this.isSlave;
    }

    @Override
    public void setIsSlave(boolean isSlave) {
        this.isSlave = isSlave;
    }

    @Override
    public boolean isZeroResend() {
        return this.isResultSetZeroResend;
    }

    @Override
    public boolean isUseSlaveSynRead() {
        return this.useSlaveSynRead;
    }

    @Override
    public boolean checkDBLinkSql() {
        return this.checkDBLinkSql;
    }

    @Override
    public boolean sendBinaryTypeAsHex() {
        return this.sendBinaryTypeAsHex;
    }

    @Override
    public boolean getIntWithPrecision() {
        return this.getIntWithPrecision;
    }

    void checkConnectionClosed(Exception e) {
        switch (ExceptionUtil.isConnectionClosed(e)) {
            case 1: {
                try {
                    this.close();
                }
                catch (SQLException e1) {
                    if (stLogFlag) {
                        Driver.writeLog("OscarJdbc2Connection checkConnectionClosed() ::" + e1.getMessage());
                    }
                    this.closed = true;
                }
                break;
            }
            case 2: {
                this.closed = true;
            }
        }
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        if (schema == null) {
            throw new SQLException("Schema name can not be null!");
        }
        if (this.isClosed()) {
            throw new SQLException("This connection has been closed.");
        }
        this.schema = schema;
    }

    @Override
    public byte[] getHdSymEncryptKey() {
        return this.hdSymEncryptKey;
    }

    @Override
    public void setHdSymEncryptKey(byte[] key) {
        this.hdSymEncryptKey = key;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public String getSchema() throws SQLException {
        if (this.schema != null) {
            return this.schema;
        }
        String sql = "select current_schema();";
        BaseResultSet rs = null;
        try {
            block5: {
                rs = this.execSQL(sql);
                if (!rs.next()) break block5;
                this.schema = rs.getString(1);
            }
            Object var5_3 = null;
            if (rs == null) return this.schema;
            rs.close();
            return this.schema;
        }
        catch (Throwable throwable) {
            Object var5_4 = null;
            if (rs == null) throw throwable;
            rs.close();
            throw throwable;
        }
    }

    @Override
    public DistributeImportHandler createDistributeImportHandler(String schemName, String tableName) throws SQLException {
        return new ClusterImportHandler(this, schemName, tableName);
    }

    @Override
    public DistributeImportHandler createDistributeImportHandler(String tableName) throws SQLException {
        return new ClusterImportHandler(this, null, tableName);
    }

    @Override
    public void setClusterImportNodeRetryTime(int time) {
        this.clusterImportNodeRetryTime = time;
    }

    @Override
    public int getClusterImportNodeRetryTime() {
        return this.clusterImportNodeRetryTime;
    }

    @Override
    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public Cluster getCluster() {
        return this.cluster;
    }

    public boolean isUseSlaveSyncReadGlobal() {
        return this.useSlaveSyncReadGlobal;
    }

    public long getSleepEndTime() {
        return this.sleepEndTime;
    }

    public void setSleepEndTime(long sleepEndTime) {
        this.sleepEndTime = sleepEndTime;
    }

    public long getSlaveDelayTime() {
        return this.slaveDelayTime;
    }

    public List getStList() {
        return this.stList;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    public boolean isMaster() {
        boolean isMaster;
        block12: {
            BaseResultSet rs;
            block11: {
                isMaster = false;
                rs = null;
                rs = this.execSQL("select * from info_schem.v_sys_ha_slave_info");
                if (!rs.next()) break block11;
                isMaster = true;
            }
            Object var5_3 = null;
            try {
                if (rs != null) {
                    rs.close();
                }
                break block12;
            }
            catch (SQLException e2) {
                e2.printStackTrace();
            }
            break block12;
            {
                catch (SQLException e) {
                    e.printStackTrace();
                    Object var5_4 = null;
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                        break block12;
                    }
                    catch (SQLException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            catch (Throwable throwable) {
                Object var5_5 = null;
                try {
                    if (rs != null) {
                        rs.close();
                    }
                }
                catch (SQLException e2) {
                    e2.printStackTrace();
                }
                throw throwable;
            }
        }
        return isMaster;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    public Map<String, Map<String, String>> initSlavesInfo(Properties props) {
        HashMap<String, Map<String, String>> slaveConnInfo;
        block15: {
            ResultSet rs;
            block14: {
                slaveConnInfo = new HashMap<String, Map<String, String>>();
                rs = null;
                StringBuilder sb = new StringBuilder("session: " + this.sessionID + ", " + DispatchConnection.class + ", initSlavesInfo(), ");
                boolean useAsynchronous = Boolean.valueOf(props.getProperty("USEASYNCHRONOUS", "true"));
                StringBuilder sql = new StringBuilder("select address, port from info_schem.v_sys_ha_slave_info where readable = TRUE ");
                if (!useAsynchronous) {
                    sql.append(" and SYNCHRONIZED = true");
                    sb.append("useAsynchronous=false, nodes:");
                } else {
                    sb.append("useAsynchronous=true, slave nodes: ");
                }
                rs = this.execSQL(sql.toString());
                int index = 0;
                while (rs.next()) {
                    String key = "slave" + index;
                    HashMap<String, String> info = new HashMap<String, String>();
                    slaveConnInfo.put(key, info);
                    info.put("ADDRESS", rs.getString("ADDRESS"));
                    info.put("PORT", rs.getString("PORT"));
                    sb.append(key + ":" + ((Object)info).toString() + "  ");
                    ++index;
                }
                if (!this.logFlag) break block14;
                Driver.writeLog(sb.toString());
            }
            Object var11_11 = null;
            try {
                if (rs != null) {
                    rs.close();
                }
                break block15;
            }
            catch (SQLException e2) {
                e2.printStackTrace();
            }
            break block15;
            {
                catch (SQLException e) {
                    e.printStackTrace();
                    Object var11_12 = null;
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                        break block15;
                    }
                    catch (SQLException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            catch (Throwable throwable) {
                Object var11_13 = null;
                try {
                    if (rs != null) {
                        rs.close();
                    }
                }
                catch (SQLException e2) {
                    e2.printStackTrace();
                }
                throw throwable;
            }
        }
        return slaveConnInfo;
    }

    static {
        Integer tinyintoid = new Integer(972);
        Integer smallintoid = new Integer(21);
        Integer intoid = new Integer(23);
        Integer oidoid = new Integer(26);
        Integer bigintoid = new Integer(20);
        Integer bitoid = new Integer(1560);
        Integer booleanoid = new Integer(16);
        Integer numericoid = new Integer(1700);
        Integer decimaloid = new Integer(2315);
        Integer realoid = new Integer(700);
        Integer lpfloatoid = new Integer(2174);
        Integer hpfloatoid = new Integer(2175);
        Integer _lpfloatoid = new Integer(2176);
        Integer _hpfloatoid = new Integer(2177);
        Integer doubleoid = new Integer(701);
        Integer charoid = new Integer(1042);
        Integer varcharoid = new Integer(1043);
        Integer textoid = new Integer(25);
        Integer nameoid = new Integer(19);
        Integer binaryoid = new Integer(1365);
        Integer varbinaryoid = new Integer(3100);
        Integer dateoid = new Integer(1082);
        Integer timeoid = new Integer(1083);
        Integer timestampoid = new Integer(1114);
        Integer timetzoid = new Integer(1266);
        Integer timestamptzoid = new Integer(1184);
        Integer bloboid = new Integer(3000);
        Integer cloboid = new Integer(3001);
        Integer bfileoid = new Integer(3002);
        Integer _aclitemoid = new Integer(1034);
        Integer refcursoroid = new Integer(1790);
        Integer byteaoid = new Integer(17);
        Integer voidoid = new Integer(2278);
        Integer varchararray = new Integer(1009);
        Integer intervaldts = new Integer(1188);
        Integer intervalytm = new Integer(1186);
        Integer bpcharoid = new Integer(18);
        Integer int2oid = new Integer(21);
        Integer int2vectoroid = new Integer(22);
        Integer int4oid = new Integer(23);
        Integer regprocoid = new Integer(24);
        Integer oidvectoroid = new Integer(30);
        Integer setoid = new Integer(32);
        Integer sys_typeoid = new Integer(71);
        Integer sys_attribute = new Integer(75);
        Integer sys_procoid = new Integer(81);
        Integer sys_classoid = new Integer(83);
        Integer abstimeoid = new Integer(702);
        Integer unknownoid = new Integer(705);
        Integer int1oid = new Integer(972);
        Integer _booloid = new Integer(1000);
        Integer _byteaoid = new Integer(1001);
        Integer _charoid = new Integer(1002);
        Integer _nameoid = new Integer(1003);
        Integer _int2oid = new Integer(1005);
        Integer _int2vectoroid = new Integer(1006);
        Integer _int4oid = new Integer(1007);
        Integer _regprocoid = new Integer(1008);
        Integer _textoid = new Integer(1009);
        Integer _oidoid = new Integer(1028);
        Integer _oidvectoroid = new Integer(1013);
        Integer _bpcharoid = new Integer(1014);
        Integer _varcharoid = new Integer(1015);
        Integer _int8oid = new Integer(1016);
        Integer _float4oid = new Integer(1021);
        Integer _float8oid = new Integer(1022);
        Integer aclitemoid = new Integer(1033);
        Integer _timestampoid = new Integer(1115);
        Integer _dateoid = new Integer(1182);
        Integer _timeoid = new Integer(1183);
        Integer _timestamptzoid = new Integer(1185);
        Integer _intervalytmoid = new Integer(1187);
        Integer _intervaldtsoid = new Integer(1189);
        Integer _int1oid = new Integer(1200);
        Integer _numericoid = new Integer(1231);
        Integer _timetzoid = new Integer(1270);
        Integer _bitoid = new Integer(1561);
        Integer varbitoid = new Integer(1562);
        Integer _varbitoid = new Integer(1563);
        Integer _binaryoid = new Integer(2097);
        Integer _refcursoroid = new Integer(2201);
        Integer regprocedureoid = new Integer(2202);
        Integer regoperoid = new Integer(2203);
        Integer regoperatoroid = new Integer(2204);
        Integer regclassoid = new Integer(2205);
        Integer regtypeoid = new Integer(2206);
        Integer _regprocedureoid = new Integer(2207);
        Integer _regoperoid = new Integer(2208);
        Integer _regoperatoroid = new Integer(2209);
        Integer _regclassoid = new Integer(2210);
        Integer _regtypeoid = new Integer(2211);
        Integer recordoid = new Integer(2249);
        Integer cstringoid = new Integer(2275);
        Integer anyoid = new Integer(2276);
        Integer anyarrayoid = new Integer(2277);
        Integer triggeroid = new Integer(2279);
        Integer language_handleroid = new Integer(2280);
        Integer internaloid = new Integer(2281);
        Integer _varbinaryoid = new Integer(3101);
        Integer labeloid = new Integer(3200);
        Integer xmloid = new Integer(3300);
        Integer _xmloid = new Integer(3301);
        Integer collectionoid = new Integer(3302);
        Integer plsql_recordoid = new Integer(3303);
        Integer jsonoid = new Integer(3304);
        Integer _jsonoid = new Integer(3305);
        Integer geometry = new Integer(86);
        DBTypeCache.put(tinyintoid, "tinyint");
        DBTypeCache.put(smallintoid, "smallint");
        DBTypeCache.put(intoid, "int");
        DBTypeCache.put(oidoid, "OID");
        DBTypeCache.put(bigintoid, "bigint");
        DBTypeCache.put(bitoid, "bit");
        DBTypeCache.put(booleanoid, "boolean");
        DBTypeCache.put(numericoid, "numeric");
        DBTypeCache.put(decimaloid, "decimal");
        DBTypeCache.put(realoid, "real");
        DBTypeCache.put(lpfloatoid, "float");
        DBTypeCache.put(hpfloatoid, "float");
        DBTypeCache.put(doubleoid, "double precision");
        DBTypeCache.put(charoid, "char");
        DBTypeCache.put(varcharoid, "varchar");
        DBTypeCache.put(textoid, "text");
        DBTypeCache.put(nameoid, "name");
        DBTypeCache.put(binaryoid, "binary");
        DBTypeCache.put(varbinaryoid, "varbinary");
        DBTypeCache.put(dateoid, "date");
        DBTypeCache.put(timeoid, "time");
        DBTypeCache.put(timestampoid, "timestamp");
        DBTypeCache.put(timetzoid, "timetz");
        DBTypeCache.put(timestamptzoid, "timestamptz");
        DBTypeCache.put(bloboid, "blob");
        DBTypeCache.put(cloboid, "clob");
        DBTypeCache.put(bfileoid, "bfile");
        DBTypeCache.put(_aclitemoid, "_ACLITEM");
        DBTypeCache.put(refcursoroid, "REFCURSOR");
        DBTypeCache.put(byteaoid, "BYTEA");
        DBTypeCache.put(voidoid, "VOID");
        DBTypeCache.put(intervaldts, "INTERVALDTS");
        DBTypeCache.put(intervalytm, "INTERVALYTM");
        DBTypeCache.put(bpcharoid, "bpchar");
        DBTypeCache.put(int2oid, "int2");
        DBTypeCache.put(int2vectoroid, "int2vector");
        DBTypeCache.put(int4oid, "int4");
        DBTypeCache.put(regprocoid, "regproc");
        DBTypeCache.put(oidvectoroid, "oidvector");
        DBTypeCache.put(setoid, "set");
        DBTypeCache.put(sys_typeoid, "sys_type");
        DBTypeCache.put(sys_attribute, "sys_attribute");
        DBTypeCache.put(sys_procoid, "sys_proc");
        DBTypeCache.put(sys_classoid, "sys_class");
        DBTypeCache.put(abstimeoid, "abstime");
        DBTypeCache.put(unknownoid, "unknown");
        DBTypeCache.put(int1oid, "int1");
        DBTypeCache.put(_booloid, "_bool");
        DBTypeCache.put(_byteaoid, "_bytea");
        DBTypeCache.put(_charoid, "_char");
        DBTypeCache.put(_nameoid, "_name");
        DBTypeCache.put(_int2oid, "_int2");
        DBTypeCache.put(_int2vectoroid, "_int2vector");
        DBTypeCache.put(_int4oid, "_int4");
        DBTypeCache.put(_regprocoid, "_regproc");
        DBTypeCache.put(_textoid, "_text");
        DBTypeCache.put(_oidoid, "_oid");
        DBTypeCache.put(_oidvectoroid, "_oidvector");
        DBTypeCache.put(_bpcharoid, "_bpchar");
        DBTypeCache.put(_varcharoid, "_varchar");
        DBTypeCache.put(_int8oid, "_int8");
        DBTypeCache.put(_float4oid, "_float4");
        DBTypeCache.put(_float8oid, "_float8");
        DBTypeCache.put(aclitemoid, "aclitem");
        DBTypeCache.put(_timestampoid, "_timestamp");
        DBTypeCache.put(_dateoid, "_date");
        DBTypeCache.put(_timeoid, "_time");
        DBTypeCache.put(_timestamptzoid, "_timestamptz");
        DBTypeCache.put(_intervalytmoid, "_intervalytm");
        DBTypeCache.put(_intervaldtsoid, "_intervaldts");
        DBTypeCache.put(_int1oid, "_int1");
        DBTypeCache.put(_numericoid, "_numeric");
        DBTypeCache.put(_timetzoid, "_timetz");
        DBTypeCache.put(_bitoid, "_bit");
        DBTypeCache.put(varbitoid, "varbit");
        DBTypeCache.put(_varbitoid, "_varbit");
        DBTypeCache.put(_binaryoid, "_binary");
        DBTypeCache.put(_lpfloatoid, "_lpfloat");
        DBTypeCache.put(_hpfloatoid, "_hpfloat");
        DBTypeCache.put(_refcursoroid, "_refcursor");
        DBTypeCache.put(regprocedureoid, "regprocedure");
        DBTypeCache.put(regoperoid, "regoper");
        DBTypeCache.put(regoperatoroid, "regoperator");
        DBTypeCache.put(regclassoid, "regclass");
        DBTypeCache.put(regtypeoid, "regtype");
        DBTypeCache.put(_regprocedureoid, "_regprocedure");
        DBTypeCache.put(_regoperoid, "_regoper");
        DBTypeCache.put(_regoperatoroid, "_regoperator");
        DBTypeCache.put(_regclassoid, "_regclass");
        DBTypeCache.put(_regtypeoid, "_regtype");
        DBTypeCache.put(recordoid, "record");
        DBTypeCache.put(cstringoid, "cstring");
        DBTypeCache.put(anyoid, "any");
        DBTypeCache.put(anyarrayoid, "anyarray");
        DBTypeCache.put(triggeroid, "trigger");
        DBTypeCache.put(language_handleroid, "language_handler");
        DBTypeCache.put(internaloid, "internal");
        DBTypeCache.put(_varbinaryoid, "_varbinary");
        DBTypeCache.put(labeloid, "label");
        DBTypeCache.put(xmloid, "xml");
        DBTypeCache.put(_xmloid, "_xml");
        DBTypeCache.put(collectionoid, "collection");
        DBTypeCache.put(plsql_recordoid, "plsql_record");
        DBTypeCache.put(jsonoid, "json");
        DBTypeCache.put(_jsonoid, "_json");
        oscarTypeCache.put(tinyintoid, new Integer(23));
        oscarTypeCache.put(smallintoid, new Integer(23));
        oscarTypeCache.put(intoid, new Integer(23));
        oscarTypeCache.put(bigintoid, new Integer(23));
        oscarTypeCache.put(booleanoid, new Integer(33));
        oscarTypeCache.put(numericoid, new Integer(34));
        oscarTypeCache.put(decimaloid, new Integer(34));
        oscarTypeCache.put(realoid, new Integer(34));
        oscarTypeCache.put(hpfloatoid, new Integer(34));
        oscarTypeCache.put(lpfloatoid, new Integer(34));
        oscarTypeCache.put(doubleoid, new Integer(34));
        oscarTypeCache.put(charoid, new Integer(24));
        oscarTypeCache.put(varcharoid, new Integer(24));
        oscarTypeCache.put(textoid, new Integer(24));
        oscarTypeCache.put(nameoid, new Integer(24));
        oscarTypeCache.put(oidoid, new Integer(24));
        oscarTypeCache.put(bitoid, new Integer(24));
        oscarTypeCache.put(binaryoid, new Integer(35));
        oscarTypeCache.put(varbinaryoid, new Integer(35));
        oscarTypeCache.put(dateoid, new Integer(25));
        oscarTypeCache.put(timeoid, new Integer(26));
        oscarTypeCache.put(timestampoid, new Integer(28));
        oscarTypeCache.put(timetzoid, new Integer(27));
        oscarTypeCache.put(timestamptzoid, new Integer(29));
        oscarTypeCache.put(bloboid, new Integer(50));
        oscarTypeCache.put(cloboid, new Integer(51));
        oscarTypeCache.put(bfileoid, new Integer(52));
        oscarTypeCache.put(intervaldts, new Integer(31));
        oscarTypeCache.put(intervalytm, new Integer(30));
        oscarTypeCache.put(_aclitemoid, new Integer(2003));
        oscarTypeCache.put(refcursoroid, new Integer(24));
        oscarTypeCache.put(byteaoid, new Integer(24));
        oscarTypeCache.put(voidoid, new Integer(24));
        oscarTypeCache.put(varchararray, new Integer(2003));
        oscarTypeCache.put(jsonoid, new Integer(24));
        sqlTypeCache.put(tinyintoid, new Integer(-6));
        sqlTypeCache.put(smallintoid, new Integer(5));
        sqlTypeCache.put(intoid, new Integer(4));
        sqlTypeCache.put(oidoid, new Integer(4));
        sqlTypeCache.put(bigintoid, new Integer(-5));
        sqlTypeCache.put(bitoid, new Integer(-7));
        sqlTypeCache.put(booleanoid, new Integer(16));
        sqlTypeCache.put(numericoid, new Integer(2));
        sqlTypeCache.put(decimaloid, new Integer(3));
        sqlTypeCache.put(realoid, new Integer(7));
        sqlTypeCache.put(hpfloatoid, new Integer(6));
        sqlTypeCache.put(lpfloatoid, new Integer(6));
        sqlTypeCache.put(doubleoid, new Integer(8));
        sqlTypeCache.put(charoid, new Integer(1));
        sqlTypeCache.put(varcharoid, new Integer(12));
        sqlTypeCache.put(textoid, new Integer(12));
        sqlTypeCache.put(nameoid, new Integer(12));
        sqlTypeCache.put(binaryoid, new Integer(-2));
        sqlTypeCache.put(varbinaryoid, new Integer(-3));
        sqlTypeCache.put(dateoid, new Integer(91));
        sqlTypeCache.put(timeoid, new Integer(92));
        sqlTypeCache.put(timestampoid, new Integer(93));
        sqlTypeCache.put(timetzoid, new Integer(92));
        sqlTypeCache.put(timestamptzoid, new Integer(93));
        sqlTypeCache.put(bloboid, new Integer(2004));
        sqlTypeCache.put(cloboid, new Integer(2005));
        sqlTypeCache.put(bfileoid, new Integer(-11));
        sqlTypeCache.put(_aclitemoid, new Integer(2003));
        sqlTypeCache.put(_lpfloatoid, new Integer(2003));
        sqlTypeCache.put(refcursoroid, new Integer(12));
        sqlTypeCache.put(byteaoid, new Integer(12));
        sqlTypeCache.put(voidoid, new Integer(0));
        sqlTypeCache.put(jsonoid, new Integer(-1));
        sqlTypeCache.put(geometry, new Integer(2002));
        jdbcTypes.put("TINYINT", new Integer(-6));
        jdbcTypes.put("SMALLINT", new Integer(5));
        jdbcTypes.put("INTEGER", new Integer(4));
        jdbcTypes.put("OID", new Integer(4));
        jdbcTypes.put("BIGINT", new Integer(-5));
        jdbcTypes.put("BIT", new Integer(-7));
        jdbcTypes.put("NUMERIC", new Integer(2));
        jdbcTypes.put("DECIMAL", new Integer(3));
        jdbcTypes.put("REAL", new Integer(7));
        jdbcTypes.put("MONEY", new Integer(7));
        jdbcTypes.put("FLOAT", new Integer(6));
        jdbcTypes.put("DOUBLE", new Integer(8));
        jdbcTypes.put("CHAR", new Integer(1));
        jdbcTypes.put("VARCHAR", new Integer(12));
        jdbcTypes.put("TEXT", new Integer(-1));
        jdbcTypes.put("NAME", new Integer(12));
        jdbcTypes.put("BINARY", new Integer(-2));
        jdbcTypes.put("VARBINARY", new Integer(-3));
        jdbcTypes.put("BOOLEAN", new Integer(16));
        jdbcTypes.put("DATE", new Integer(91));
        jdbcTypes.put("TIME", new Integer(92));
        jdbcTypes.put("TIMESTAMP", new Integer(93));
        jdbcTypes.put("TIMESTAMPTZ", new Integer(93));
        jdbcTypes.put("BLOB", new Integer(2004));
        jdbcTypes.put("CLOB", new Integer(2005));
        jdbcTypes.put("BYTEA", new Integer(-3));
        jdbcTypes.put("LABEL", new Integer(1111));
        jdbcTypes.put("INT1", new Integer(-6));
        jdbcTypes.put("_INT1", new Integer(2003));
        jdbcTypes.put("INT2", new Integer(5));
        jdbcTypes.put("_INT2", new Integer(2003));
        jdbcTypes.put("INT4", new Integer(4));
        jdbcTypes.put("_INT4", new Integer(2003));
        jdbcTypes.put("INT8", new Integer(-5));
        jdbcTypes.put("_INT8", new Integer(2003));
        jdbcTypes.put("_VARCHAR", new Integer(2003));
        jdbcTypes.put("_varchar", new Integer(2003));
        jdbcTypes.put("_BOOL", new Integer(2003));
        jdbcTypes.put("_bool", new Integer(2003));
        jdbcTypes.put("_HPFLOAT", new Integer(2003));
        jdbcTypes.put("_hpfloat", new Integer(2003));
        jdbcTypes.put("_FLOAT4", new Integer(2003));
        jdbcTypes.put("_float4", new Integer(2003));
        jdbcTypes.put("_FLOAT8", new Integer(2003));
        jdbcTypes.put("_float8", new Integer(2003));
        jdbcTypes.put("_TEXT", new Integer(2003));
        jdbcTypes.put("_text", new Integer(2003));
        jdbcTypes.put("_BPCHAR", new Integer(2003));
        jdbcTypes.put("_bpchar", new Integer(2003));
        jdbcTypes.put("_BIT", new Integer(2003));
        jdbcTypes.put("_bit", new Integer(2003));
        jdbcTypes.put("_NUMERIC", new Integer(2003));
        jdbcTypes.put("_numeric", new Integer(2003));
        jdbcTypes.put("_BINARY", new Integer(2003));
        jdbcTypes.put("_binary", new Integer(2003));
        jdbcTypes.put("_VARBINARY", new Integer(2003));
        jdbcTypes.put("_varbinary", new Integer(2003));
        jdbcTypes.put("_DATE", new Integer(2003));
        jdbcTypes.put("_date", new Integer(2003));
        jdbcTypes.put("_INTERVALYTM", new Integer(2003));
        jdbcTypes.put("_intervalytm", new Integer(2003));
        jdbcTypes.put("_INTERVALDTS", new Integer(2003));
        jdbcTypes.put("_intervaldts", new Integer(2003));
        jdbcTypes.put("int1", new Integer(-6));
        jdbcTypes.put("_int1", new Integer(2003));
        jdbcTypes.put("int2", new Integer(5));
        jdbcTypes.put("_int2", new Integer(2003));
        jdbcTypes.put("int4", new Integer(4));
        jdbcTypes.put("_int4", new Integer(2003));
        jdbcTypes.put("int8", new Integer(-5));
        jdbcTypes.put("_int8", new Integer(2003));
        jdbcTypes.put("tinyint", new Integer(-6));
        jdbcTypes.put("smallint", new Integer(5));
        jdbcTypes.put("integer", new Integer(4));
        jdbcTypes.put("oid", new Integer(4));
        jdbcTypes.put("bigint", new Integer(-5));
        jdbcTypes.put("bit", new Integer(-7));
        jdbcTypes.put("numeric", new Integer(2));
        jdbcTypes.put("decimal", new Integer(3));
        jdbcTypes.put("real", new Integer(7));
        jdbcTypes.put("money", new Integer(7));
        jdbcTypes.put("float", new Integer(6));
        jdbcTypes.put("double", new Integer(8));
        jdbcTypes.put("char", new Integer(1));
        jdbcTypes.put("varchar", new Integer(12));
        jdbcTypes.put("text", new Integer(-1));
        jdbcTypes.put("name", new Integer(12));
        jdbcTypes.put("binary", new Integer(-2));
        jdbcTypes.put("varbinary", new Integer(-3));
        jdbcTypes.put("boolean", new Integer(16));
        jdbcTypes.put("date", new Integer(91));
        jdbcTypes.put("time", new Integer(92));
        jdbcTypes.put("timestamp", new Integer(93));
        jdbcTypes.put("timestamptz", new Integer(93));
        jdbcTypes.put("blob", new Integer(2004));
        jdbcTypes.put("clob", new Integer(2005));
        jdbcTypes.put("bytea", new Integer(-3));
        jdbcTypes.put("label", new Integer(1111));
        jdbcTypes.put("json", new Integer(-1));
        jdbcTypes.put("_json", new Integer(2003));
        jdbcTypes.put("JSON", new Integer(-1));
        jdbcTypes.put("_JSON", new Integer(2003));
        jdbcTypei = new int[]{-6, 5, 4, 4, -5, -7, 2, 3, 7, 7, 6, 8, 1, 12, -1, 12, -2, -3, 16, 91, 92, 93, 2004, 2005, -3};
    }
}

