/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.cluster.Cluster;
import com.oscar.core.BaseResultSet;
import com.oscar.core.BaseStatement;
import com.oscar.core.DistributeImportHandler;
import com.oscar.core.Encoding;
import com.oscar.core.ExportHandler;
import com.oscar.core.ImportHandler;
import com.oscar.core.ProviderImportHandler;
import com.oscar.core.QueryExecutor;
import com.oscar.fastpath.Fastpath;
import com.oscar.jdbc.OSCARTransfer;
import com.oscar.jdbc.OscarBfile;
import com.oscar.jdbc.OscarBlob;
import com.oscar.jdbc.OscarClob;
import com.oscar.protocol.OSCARProtocol;
import com.oscar.protocol.ProtocolVersion;
import com.oscar.util.VersionConfig;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public interface BaseConnection {
    public static final int BIG_ENDIAN = 0;
    public static final int LITTLE_ENDIAN = 1;

    public void addWarning(String var1, String var2);

    public void cancelQuery() throws SQLException;

    public Statement createStatement() throws SQLException;

    public BaseResultSet execSQL(String var1) throws SQLException;

    public BaseResultSet execSQL(String var1, BaseStatement var2) throws SQLException;

    public BaseResultSet execSQL(String var1, BaseStatement var2, BaseResultSet var3) throws SQLException;

    public boolean getAutoCommit() throws SQLException;

    public String getCursorName() throws SQLException;

    public Encoding getClientEncoding();

    public void setClientEncoding(String var1);

    public Encoding getEncoding();

    public DatabaseMetaData getMetaData() throws SQLException;

    public OSCARProtocol getProtocol() throws SQLException;

    public String getDBType(int var1) throws SQLException;

    public int getDBTypeOid(String var1) throws SQLException;

    public int getSQLType(int var1) throws SQLException;

    public int getOscarType(int var1) throws SQLException;

    public int getSQLType(String var1) throws SQLException;

    public void setAutoCommit(boolean var1) throws SQLException;

    public void setCursorName(String var1) throws SQLException;

    public void addCursor(String var1);

    public void removePlanID(int var1);

    public void addPlanID(byte[] var1);

    public boolean hasCursor(String var1);

    public void removeCursor(String var1);

    public OscarBlob getBlobInstance(String var1) throws SQLException;

    public OscarBfile getBfileInstance(String var1) throws SQLException;

    public OscarClob getClobInstance(String var1) throws SQLException;

    public BaseStatement getDefaultStatement() throws SQLException;

    public Object lookupCachedPrepare(String var1);

    public void addNewPrepare(Object var1) throws SQLException;

    public void setInTranscation(boolean var1);

    public boolean isInTransaction();

    public int getPreapredCacheSize();

    public Fastpath getFastpathAPI() throws SQLException;

    public long getAccessHandle();

    public void setSeed(long var1);

    public VersionConfig getVersion();

    public void setVersion(ProtocolVersion var1);

    public ProtocolVersion getProtocolVersion();

    public boolean isUsePrepareCache();

    public boolean isCompatibleOracle();

    public boolean isNotRealPrepare();

    public void closeCursor(String var1, BaseStatement var2) throws SQLException;

    public OSCARTransfer getTransfer();

    public OscarBlob createTempBlob(boolean var1, int var2) throws SQLException;

    public OscarClob createTempClob(boolean var1, int var2) throws SQLException;

    public QueryExecutor getQueryExecutor();

    public boolean isCompressTransfer();

    public void setCompressTransfer(boolean var1);

    public int getBatchBufferSize();

    public void setBatchBufferSize(int var1);

    public ImportHandler createImportHandler() throws SQLException;

    public ImportHandler createImportHandler(String var1) throws SQLException;

    public ImportHandler createImportHandler(String var1, String var2) throws SQLException;

    public ProviderImportHandler createProviderImportHandler(String var1) throws SQLException;

    public ProviderImportHandler createProviderImportHandler(String var1, String var2) throws SQLException;

    public ExportHandler createExportHandler() throws SQLException;

    public int getEndianType();

    public void setEndianType(int var1);

    public int checkPlanID(byte[] var1);

    public Integer getSessionID();

    public int getPlanID() throws SQLException;

    public boolean isNetDataByStr();

    public boolean isNumericKeepPrecision();

    public boolean isPrepareSimpleExecute();

    public boolean isTcpKeepAlive();

    public void setTcpKeepAlive(boolean var1);

    public boolean isValidate();

    public boolean isValidate(int var1);

    public boolean isValid(int var1) throws SQLException;

    public boolean isValid() throws SQLException;

    public boolean isVerifyPoolConnection();

    public int getBatchCount();

    public boolean isUseAsynBatch();

    public boolean isReceiveStringByLen();

    public void setMasterConnection(Connection var1);

    public Connection getMasterConnection();

    public Properties getConnectionProperties();

    public void setTransStatus(int var1);

    public int getTransStatus();

    public boolean isCompatibleOldDateFormat();

    public boolean isSlave();

    public void setIsSlave(boolean var1);

    public boolean isZeroResend();

    public boolean isUseSlaveSynRead();

    public boolean checkDBLinkSql();

    public boolean sendBinaryTypeAsHex();

    public boolean getIntWithPrecision();

    public DistributeImportHandler createDistributeImportHandler(String var1, String var2) throws SQLException;

    public DistributeImportHandler createDistributeImportHandler(String var1) throws SQLException;

    public void setClusterImportNodeRetryTime(int var1);

    public int getClusterImportNodeRetryTime();

    public void setCluster(Cluster var1);

    public Cluster getCluster();

    public boolean isClosed() throws SQLException;

    public void close() throws SQLException;

    public byte[] getHdSymEncryptKey();

    public void setHdSymEncryptKey(byte[] var1);
}

