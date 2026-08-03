/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.cluster;

import com.oscar.cluster.Cluster;
import com.oscar.cluster.core.ClusterProtocol;
import com.oscar.cluster.core.NodeProtocol;
import com.oscar.core.BaseConnection;
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
import com.oscar.protocol.Packet;
import com.oscar.protocol.ProtocolVersion;
import com.oscar.protocol.packets.BasePacket;
import com.oscar.protocol.packets.CompleteResponsePacket;
import com.oscar.protocol.packets.ErrorResponsePacket;
import com.oscar.protocol.packets.NewImportPacket;
import com.oscar.protocol.packets.NoticeResponsePacket;
import com.oscar.protocol.packets.QueryPacket;
import com.oscar.protocol.packets.ReadyForQueryPacket;
import com.oscar.util.OSQLException;
import com.oscar.util.VersionConfig;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Protocol
implements ClusterProtocol,
NodeProtocol {
    protected Packet pk = new Packet();
    private Encoding encoding;
    private BaseConnection connection;

    public Protocol(Encoding encoding, BaseConnection connection) {
        this.encoding = encoding;
        this.connection = connection;
    }

    public Protocol(Encoding encoding) {
        this.encoding = encoding;
        this.connection = new FakeConnection();
    }

    public void nodeImportEnd(InputStream in, OutputStream out) throws SQLException {
        BufferedOutputStream bfout = this.decorateStream(out);
        try {
            BasePacket.SendChar(bfout, 67);
            bfout.flush();
        }
        catch (Exception e) {
            throw new OSQLException("OSCAR-00804", "88888", 804, e);
        }
    }

    public void sendImportCredential(int globalID, int identity, InputStream in, OutputStream out) throws SQLException {
        BufferedOutputStream bfout = this.decorateStream(out);
        try {
            BasePacket.SendChar(bfout, 82);
            BasePacket.SendInteger(bfout, globalID, 4);
            BasePacket.SendInteger(bfout, identity, 4);
            bfout.flush();
            BasePacket bk = this.getRespond(in);
            if (!(bk instanceof NewImportPacket)) {
                throw new OSQLException("OSCAR-00804", "88888", 804, "\u8282\u70b9\u8fd8\u672a\u51c6\u5907\u597d\uff0c\u53ef\u80fd\u7e41\u5fd9");
            }
        }
        catch (IOException e) {
            throw new OSQLException("OSCAR-00804", "88888", 804, e);
        }
    }

    public void importData2Node(byte[] data, InputStream in, OutputStream out) throws SQLException {
        try {
            out.write(data);
            out.flush();
        }
        catch (IOException e) {
            throw new OSQLException("OSCAR-00804", "88888", 804, e);
        }
    }

    public void importBegin(String insertBulk, InputStream in, OutputStream out) throws SQLException {
        try {
            BasePacket bk;
            byte[] data = this.encoding.encode(insertBulk);
            QueryPacket qp = new QueryPacket(data, 0);
            this.sendMessage(this.decorateStream(out), qp);
            BasePacket readyBk = null;
            do {
                if ((bk = this.getRespond(in)) instanceof NoticeResponsePacket) {
                    this.getMessage(in, bk);
                    continue;
                }
                if (bk instanceof NewImportPacket) {
                    this.getMessage(in, bk);
                    continue;
                }
                if (bk instanceof ErrorResponsePacket) {
                    this.getMessage(in, bk);
                    readyBk = this.getRespond(in);
                    while (!(readyBk instanceof ReadyForQueryPacket)) {
                        readyBk = this.getRespond(in);
                    }
                    ErrorResponsePacket errorPacket = (ErrorResponsePacket)bk;
                    throw new OSQLException(errorPacket.getErrorCode(), this.encoding.decode(errorPacket.getSQLState()), this.encoding.decode(errorPacket.getErrorMessage()));
                }
                throw new OSQLException("OSCAR-00109", "08003", 109);
            } while (!(bk instanceof NewImportPacket));
        }
        catch (IOException e) {
            throw new OSQLException("OSCAR-00804", "88888", 804, e);
        }
    }

    private BufferedOutputStream decorateStream(OutputStream out) {
        BufferedOutputStream bf = null;
        bf = out instanceof BufferedOutputStream ? (BufferedOutputStream)out : new BufferedOutputStream(out, 8192);
        return bf;
    }

    protected void sendMessage(BufferedOutputStream stream, BasePacket packet) throws IOException, SQLException {
        packet.sendTo(stream);
    }

    protected BasePacket getRespond(InputStream stream) throws IOException, SQLException {
        byte[] tagTemp = new byte[1];
        stream.read(tagTemp, 0, 1);
        char tag = (char)tagTemp[0];
        return this.pk.getInstance(tag, this.connection);
    }

    protected void getMessage(InputStream stream, BasePacket packet) throws IOException, SQLException {
        packet.receiveFrom(stream);
    }

    public int importEnd(InputStream in, OutputStream out) throws SQLException {
        try {
            BasePacket bk;
            BasePacket readyBk = null;
            int updateCount = 0;
            do {
                if ((bk = this.getRespond(in)) instanceof NoticeResponsePacket || bk instanceof NewImportPacket) {
                    this.getMessage(in, bk);
                    continue;
                }
                if (bk instanceof CompleteResponsePacket) {
                    this.getMessage(in, bk);
                    String command = this.connection.getEncoding().decode(((CompleteResponsePacket)bk).getCommand());
                    char tag1 = command.charAt(0);
                    char tag2 = command.charAt(1);
                    if (tag1 != '2' || tag2 != '1') continue;
                    updateCount = Integer.parseInt(command.substring(command.indexOf(32) + 1, command.lastIndexOf(32)));
                    continue;
                }
                if (bk instanceof ReadyForQueryPacket) {
                    this.getMessage(in, bk);
                    continue;
                }
                if (bk instanceof ErrorResponsePacket) {
                    this.getMessage(in, bk);
                    readyBk = this.getRespond(in);
                    while (!(readyBk instanceof ReadyForQueryPacket)) {
                        readyBk = this.getRespond(in);
                    }
                    ErrorResponsePacket errorPacket = (ErrorResponsePacket)bk;
                    throw new OSQLException(errorPacket.getErrorCode(), this.encoding.decode(errorPacket.getSQLState()), this.encoding.decode(errorPacket.getErrorMessage()));
                }
                throw new OSQLException("OSCAR-00109", "08003", 109);
            } while (!(bk instanceof ReadyForQueryPacket));
            return updateCount;
        }
        catch (IOException e) {
            throw new OSQLException("OSCAR-00804", "88888", 804, e);
        }
    }

    static class FakeConnection
    implements BaseConnection {
        FakeConnection() {
        }

        public void addWarning(String msg, String code) {
        }

        public void cancelQuery() throws SQLException {
        }

        public Statement createStatement() throws SQLException {
            return null;
        }

        public BaseResultSet execSQL(String s) throws SQLException {
            return null;
        }

        public BaseResultSet execSQL(String s, BaseStatement stmt) throws SQLException {
            return null;
        }

        public BaseResultSet execSQL(String s, BaseStatement stmt, BaseResultSet res) throws SQLException {
            return null;
        }

        public boolean getAutoCommit() throws SQLException {
            return false;
        }

        public String getCursorName() throws SQLException {
            return null;
        }

        public Encoding getClientEncoding() {
            return null;
        }

        public void setClientEncoding(String encode) {
        }

        public Encoding getEncoding() {
            return null;
        }

        public DatabaseMetaData getMetaData() throws SQLException {
            return null;
        }

        public OSCARProtocol getProtocol() {
            return null;
        }

        public String getDBType(int oid) throws SQLException {
            return null;
        }

        public int getDBTypeOid(String DBTypeName) throws SQLException {
            return 0;
        }

        public int getSQLType(int oid) throws SQLException {
            return 0;
        }

        public int getOscarType(int oid) throws SQLException {
            return 0;
        }

        public int getSQLType(String DBTypeName) throws SQLException {
            return 0;
        }

        public void setAutoCommit(boolean autoCommit) throws SQLException {
        }

        public void setCursorName(String cursor) throws SQLException {
        }

        public void addCursor(String cursorName) {
        }

        public void removePlanID(int index) {
        }

        public void addPlanID(byte[] planID) {
        }

        public boolean hasCursor(String cursorName) {
            return false;
        }

        public void removeCursor(String cursorName) {
        }

        public OscarBlob getBlobInstance(String locatorStr) throws SQLException {
            return null;
        }

        public OscarBfile getBfileInstance(String locatorStr) throws SQLException {
            return null;
        }

        public OscarClob getClobInstance(String locatorStr) throws SQLException {
            return null;
        }

        public BaseStatement getDefaultStatement() throws SQLException {
            return null;
        }

        public Object lookupCachedPrepare(String sql) {
            return null;
        }

        public void addNewPrepare(Object p) throws SQLException {
        }

        public void setInTranscation(boolean tran) {
        }

        public boolean isInTransaction() {
            return false;
        }

        public int getPreapredCacheSize() {
            return 0;
        }

        public Fastpath getFastpathAPI() throws SQLException {
            return null;
        }

        public long getAccessHandle() {
            return 0L;
        }

        public void setSeed(long seed) {
        }

        public VersionConfig getVersion() {
            return null;
        }

        public void setVersion(ProtocolVersion version) {
        }

        public ProtocolVersion getProtocolVersion() {
            return null;
        }

        public boolean isUsePrepareCache() {
            return false;
        }

        public boolean isCompatibleOracle() {
            return false;
        }

        public boolean isNotRealPrepare() {
            return false;
        }

        public void closeCursor(String cursorName, BaseStatement statement) throws SQLException {
        }

        public OSCARTransfer getTransfer() {
            return null;
        }

        public OscarBlob createTempBlob(boolean cache, int duration) throws SQLException {
            return null;
        }

        public OscarClob createTempClob(boolean cache, int duration) throws SQLException {
            return null;
        }

        public QueryExecutor getQueryExecutor() {
            return null;
        }

        public boolean isCompressTransfer() {
            return false;
        }

        public void setCompressTransfer(boolean compressTransfer) {
        }

        public int getBatchBufferSize() {
            return 0;
        }

        public void setBatchBufferSize(int defaultBatchBufferSize) {
        }

        public ImportHandler createImportHandler() throws SQLException {
            return null;
        }

        public ImportHandler createImportHandler(String tableName) throws SQLException {
            return null;
        }

        public ImportHandler createImportHandler(String schemName, String tableName) throws SQLException {
            return null;
        }

        public ProviderImportHandler createProviderImportHandler(String tableName) throws SQLException {
            return null;
        }

        public ProviderImportHandler createProviderImportHandler(String schemName, String tableName) throws SQLException {
            return null;
        }

        public ExportHandler createExportHandler() throws SQLException {
            return null;
        }

        public int getEndianType() {
            return 0;
        }

        public void setEndianType(int type) {
        }

        public int checkPlanID(byte[] planID) {
            return 0;
        }

        public Integer getSessionID() {
            return null;
        }

        public int getPlanID() {
            return 0;
        }

        public boolean isNetDataByStr() {
            return false;
        }

        public boolean isNumericKeepPrecision() {
            return false;
        }

        public boolean isPrepareSimpleExecute() {
            return false;
        }

        public boolean isTcpKeepAlive() {
            return false;
        }

        public void setTcpKeepAlive(boolean tcpKeepAlive) {
        }

        public boolean isValidate() {
            return false;
        }

        public boolean isValidate(int timeout) {
            return false;
        }

        public boolean isValid(int timeout) throws SQLException {
            return false;
        }

        public boolean isValid() throws SQLException {
            return false;
        }

        public boolean isVerifyPoolConnection() {
            return false;
        }

        public int getBatchCount() {
            return 0;
        }

        public boolean isUseAsynBatch() {
            return false;
        }

        public boolean isReceiveStringByLen() {
            return false;
        }

        public void setMasterConnection(Connection conn) {
        }

        public Connection getMasterConnection() {
            return null;
        }

        public Properties getConnectionProperties() {
            return null;
        }

        public void setTransStatus(int transStatus) {
        }

        public int getTransStatus() {
            return 0;
        }

        public boolean isCompatibleOldDateFormat() {
            return false;
        }

        public boolean isSlave() {
            return false;
        }

        public void setIsSlave(boolean isSlave) {
        }

        public boolean isZeroResend() {
            return false;
        }

        public boolean isUseSlaveSynRead() {
            return false;
        }

        public boolean checkDBLinkSql() {
            return false;
        }

        public boolean sendBinaryTypeAsHex() {
            return false;
        }

        public boolean getIntWithPrecision() {
            return false;
        }

        public DistributeImportHandler createDistributeImportHandler(String schemName, String tableName) throws SQLException {
            return null;
        }

        public DistributeImportHandler createDistributeImportHandler(String tableName) throws SQLException {
            return null;
        }

        public void setClusterImportNodeRetryTime(int time) {
        }

        public int getClusterImportNodeRetryTime() {
            return 0;
        }

        public void setCluster(Cluster cluster) {
        }

        public Cluster getCluster() {
            return null;
        }

        public boolean isClosed() throws SQLException {
            return false;
        }

        public void close() throws SQLException {
        }

        public byte[] getHdSymEncryptKey() {
            return null;
        }

        public void setHdSymEncryptKey(byte[] key) {
        }
    }
}

