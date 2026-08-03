/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.core.BaseResultSet;
import com.oscar.core.BaseStatement;
import com.oscar.core.ExportBinlogHandler;
import com.oscar.core.Field;
import com.oscar.core.ImportBinlogHandler;
import com.oscar.jdbc.BlogResultSet;
import com.oscar.jdbc.OscarImportHandler;
import com.oscar.protocol.Packet;
import com.oscar.protocol.VeifyJDBC;
import com.oscar.protocol.packets.AsciiRowPacket;
import com.oscar.protocol.packets.AuthenticationPacket;
import com.oscar.protocol.packets.BLogErrorResponsePacket;
import com.oscar.protocol.packets.BackendKeyPacket;
import com.oscar.protocol.packets.BackupMetaDataPacket;
import com.oscar.protocol.packets.BackupPhysicalDataPacket;
import com.oscar.protocol.packets.BackupPhysicalRowEndPacket;
import com.oscar.protocol.packets.BasePacket;
import com.oscar.protocol.packets.BatchProcessPacket;
import com.oscar.protocol.packets.BinlogErrorPacket;
import com.oscar.protocol.packets.BlogDataPacket;
import com.oscar.protocol.packets.CancelRequestPacket;
import com.oscar.protocol.packets.CompleteResponsePacket;
import com.oscar.protocol.packets.CursorResponsePacket;
import com.oscar.protocol.packets.EmptyQueryResponsePacket;
import com.oscar.protocol.packets.ErrorResponsePacket;
import com.oscar.protocol.packets.ExportBinlogSuccessPacket;
import com.oscar.protocol.packets.FunctionCallPacket;
import com.oscar.protocol.packets.FunctionResponsePacket;
import com.oscar.protocol.packets.HashDataPacket;
import com.oscar.protocol.packets.ImportExportResponsePacket;
import com.oscar.protocol.packets.ImportPacket;
import com.oscar.protocol.packets.ListenerResponsePacket;
import com.oscar.protocol.packets.NewImportPacket;
import com.oscar.protocol.packets.NoticeResponsePacket;
import com.oscar.protocol.packets.ParamInforPacket;
import com.oscar.protocol.packets.QueryPacket;
import com.oscar.protocol.packets.QueryPacketHash;
import com.oscar.protocol.packets.ReadyForMetaData;
import com.oscar.protocol.packets.ReadyForPhysicalDataPacket;
import com.oscar.protocol.packets.ReadyForQueryPacket;
import com.oscar.protocol.packets.RowDescriptionPacket;
import com.oscar.protocol.packets.SSLRequestPacket;
import com.oscar.protocol.packets.SetQueryLsnPacket;
import com.oscar.protocol.packets.StartupPacket;
import com.oscar.protocol.packets.TerminatePacket;
import com.oscar.protocol.packets.UnencryptedPasswordPacket;
import com.oscar.protocol.stream.OSocket;
import com.oscar.protocol.stream.OStream;
import com.oscar.util.MD5Digest;
import com.oscar.util.OSQLException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class OSCARProtocol {
    public OStream oStream;
    protected BaseConnection connection;
    protected String host;
    protected int port;
    protected String database;
    protected String db_user;
    protected String db_passwd;
    protected BaseStatement statement;
    protected volatile OscarImportHandler handler;
    protected ImportBinlogHandler importBinlogHandler;
    protected ExportBinlogHandler exportBinlogHandler;
    protected BaseResultSet resultset;
    protected int update_count = -2;
    protected long insert_tid = 0L;
    protected Object callResult;
    protected int authPolicy;
    protected int pid;
    protected int ckey;
    protected Field[] fields = null;
    protected Field tidField = null;
    protected List tuples = new ArrayList();
    protected int columnCount;
    protected boolean isSSL = false;
    protected Properties info;
    protected boolean resultTid = false;
    protected List tidList = null;
    protected static int ProtocolMajorVersion = 2;
    protected static int ProtocolMinorVersion = 0;
    protected int listenerVersion = 0;
    public static final int PROTOCOL_OK = 1;
    public static final int PROTOCOL_WAIT = 0;
    public static final int PROTOCOL_FAIL = -1;
    protected int status = 0;
    protected static final int AUTH_UNKNOW = -1;
    protected static final int AUTH_REQ_OK = 0;
    protected static final int AUTH_REQ_PASSWORD = 3;
    protected static final int AUTH_REQ_MD5 = 5;
    protected static final int AUTH_REQ_SCM = 6;
    protected byte[] cmd = null;
    protected BasePacket bk = null;
    protected Packet pk = new Packet();
    protected int errorRetryTimes;
    protected boolean logFlag = Driver.getLogLevel() >= 3;

    public OSCARProtocol(BaseConnection con, String _host, int _port, String _database, String user, Properties _info) throws ConnectException, IOException {
        this(con, _host, _port, _database, user, _info, 0);
    }

    public OSCARProtocol(BaseConnection con, String _host, int _port, String _database, String user, Properties _info, int requestTimeOut) throws ConnectException, IOException {
        this.connection = con;
        this.host = _host;
        this.port = _port;
        if (Boolean.valueOf(_info.getProperty("NAMESENSITIVE")).booleanValue()) {
            this.database = _database;
            this.db_user = user;
        } else {
            this.database = OSCARProtocol.convertString(_database);
            this.db_user = OSCARProtocol.convertString(user);
        }
        this.db_passwd = _info.getProperty("PASSWORD") == null ? "" : _info.getProperty("PASSWORD");
        this.info = _info;
        this.errorRetryTimes = Integer.parseInt(_info.getProperty("ERRORRETRYTIMES", "10"));
        this.oStream = new OSocket(_host, _port, requestTimeOut, _info);
        this.oStream.open();
    }

    public OSCARProtocol(BaseConnection con, String _host, int _port, String _database, String user, String _password, Properties _info, OStream oStream) {
        this.connection = con;
        this.host = _host;
        this.port = _port;
        this.database = _database;
        this.db_user = user;
        this.db_passwd = _password;
        this.info = _info;
        this.errorRetryTimes = Integer.parseInt(_info.getProperty("ERRORRETRYTIMES", "10"));
        this.oStream = oStream;
    }

    public int getProtocolMajorVersion() {
        return ProtocolMajorVersion;
    }

    public int getProtocolMinorVersion() {
        return ProtocolMinorVersion;
    }

    public int getProtocolStatus() {
        return this.status;
    }

    public int getPID() {
        return this.pid;
    }

    public int getCKEY() {
        return this.ckey;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean startup(boolean useSSL) throws SQLException {
        OStream oStream = this.oStream;
        synchronized (oStream) {
            this.status = 0;
            boolean encodingFlag = this.connection.getEncoding() == null;
            try {
                StartupPacket start = new StartupPacket(ProtocolMajorVersion, ProtocolMinorVersion, this.database, this.db_user, this.info, this.connection.getEncoding(), this.connection.getVersion());
                start.setConnection(this.connection);
                if (this.connection.getVersion().isWuziVersion()) {
                    this.oStream.readJDCBVerifyKey(this.info.getProperty("keyfileForVerifyJDBC", "jdbc.key"), this.info.getProperty("keypassForVerifyJDBC", "szoscar55"));
                }
                if (this.info.getProperty("NOSSLVERSION", "t").equals("f")) {
                    byte[] respondType = new byte[1];
                    SSLRequestPacket srp = new SSLRequestPacket(useSSL);
                    srp.setConnection(this.connection);
                    this.sendMessage(this.oStream.getBufferedOutputStream(), srp);
                    this.oStream.getInputStream().read(respondType);
                    switch (respondType[0]) {
                        case 1: {
                            try {
                                if (this.info.getProperty("ssl_load_style", "SSL_LOAD_STYLE_FILE").equals("SSL_LOAD_STYLE_FILE")) {
                                    this.oStream.openWithSSL(this.info.getProperty("KEYFILE", "client.key"), this.info.getProperty("CERTFILE", "client.pem"), this.info.getProperty("KEYFILEPWD", "password"), this.info.getProperty("ROOTFILE", "root.pem"), this.info.getProperty("RANDOMFILE", "random.pem"), this.database);
                                } else if (this.info.getProperty("ssl_load_style", "SSL_LOAD_STYLE_FILE").equals("SSL_LOAD_STYLE_OSCARKEYSTORE")) {
                                    this.oStream.openWithSSLUseWallet(this.info.getProperty("OscarKeyStore", "sysdba.p12"), this.info.getProperty("OscarKeyStorePass", "szoscar55"), this.info.getProperty("RANDOMFILE", "random.pem"), this.database);
                                }
                            }
                            catch (InternalError er) {
                                throw new SQLException(er.getMessage());
                            }
                            catch (Exception ex) {
                                throw new SQLException(ex.getMessage());
                            }
                            this.isSSL = true;
                            break;
                        }
                        case 0: {
                            break;
                        }
                        case -1: {
                            this.status = -1;
                            throw new OSQLException("OSCAR-00111", "88888", 111);
                        }
                        case -2: {
                            this.status = -1;
                            throw new OSQLException("OSCAR-00112", "88888", 112);
                        }
                        case -3: {
                            this.status = -1;
                            throw new OSQLException("OSCAR-00117", "88888", 117);
                        }
                    }
                }
                this.sendMessage(this.oStream.getBufferedOutputStream(), start);
                if (this.connection.getVersion().isWuziVersion()) {
                    VeifyJDBC veifyJDBC = new VeifyJDBC(this.oStream);
                    veifyJDBC.veify();
                    this.connection.setSeed(veifyJDBC.getRandNum());
                }
                this.authPolicy = -1;
                do {
                    this.bk = this.getMessage(this.oStream.getInputStream());
                    byte[] md5Salt = null;
                    if (this.bk instanceof ListenerResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.listenerVersion = ((ListenerResponsePacket)this.bk).getListenerVersion();
                        int dbPort = ((ListenerResponsePacket)this.bk).getDbPort();
                        TerminatePacket tp = new TerminatePacket();
                        tp.setConnection(this.connection);
                        this.sendMessage(this.oStream.getBufferedOutputStream(), tp);
                        try {
                            Thread.sleep(100L);
                        }
                        catch (InterruptedException exp) {
                            // empty catch block
                        }
                        this.oStream.close();
                        if (dbPort == 0) {
                            throw new OSQLException("OSCAR-00212", "88888", 106);
                        }
                        this.port = dbPort;
                        this.oStream = new OSocket(this.host, this.port, this.info);
                        this.oStream.open();
                        return this.startup(useSSL);
                    }
                    if (this.bk instanceof AuthenticationPacket) {
                        byte[] byteUser = this.connection.getEncoding().encode(this.db_user);
                        byte[] bytePassword = this.connection.getEncoding().encode(this.db_passwd);
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.authPolicy = ((AuthenticationPacket)this.bk).getAuthenPolicy();
                        if (this.authPolicy == 5) {
                            md5Salt = (byte[])((AuthenticationPacket)this.bk).getSalt();
                        }
                        switch (this.authPolicy) {
                            case 0: {
                                break;
                            }
                            case 3: {
                                UnencryptedPasswordPacket upp = new UnencryptedPasswordPacket(bytePassword);
                                upp.setConnection(this.connection);
                                this.sendMessage(this.oStream.getBufferedOutputStream(), upp);
                                break;
                            }
                            case 5: {
                                byte[] digest = MD5Digest.encode(byteUser, bytePassword, md5Salt);
                                UnencryptedPasswordPacket uppmd5 = new UnencryptedPasswordPacket(digest);
                                uppmd5.setConnection(this.connection);
                                this.sendMessage(this.oStream.getBufferedOutputStream(), uppmd5);
                                break;
                            }
                            default: {
                                throw new OSQLException("OSCAR-00106", "08004", 106);
                            }
                        }
                        continue;
                    }
                    if (this.bk instanceof ErrorResponsePacket) {
                        this.status = -1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                        throw new OSQLException(errorPacket.getErrorCode(), new String(errorPacket.getSQLState()), new String(errorPacket.getErrorMessage()));
                    }
                    if (this.bk instanceof NoticeResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.connection.addWarning(new String(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                        System.out.println("Connect Warning: " + new String(((NoticeResponsePacket)this.bk).getNoticeMessage()));
                        continue;
                    }
                    this.status = -1;
                    throw new OSQLException("OSCAR-00106", "08004", 106);
                } while (this.authPolicy != 0);
                do {
                    this.bk = this.getMessage(this.oStream.getInputStream());
                    if (this.bk instanceof ReadyForQueryPacket) {
                        this.status = 1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    if (this.bk instanceof BackendKeyPacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.pid = ((BackendKeyPacket)this.bk).getPID();
                        this.ckey = ((BackendKeyPacket)this.bk).getCKey();
                        continue;
                    }
                    if (this.bk instanceof ErrorResponsePacket) {
                        this.status = -1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        throw new OSQLException(((ErrorResponsePacket)this.bk).getErrorCode(), new String(((ErrorResponsePacket)this.bk).getSQLState()), new String(((ErrorResponsePacket)this.bk).getErrorMessage()));
                    }
                    if (this.bk instanceof NoticeResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.connection.addWarning(new String(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                        continue;
                    }
                    this.status = -1;
                    throw new OSQLException("OSCAR-00107", "08001", 107);
                } while (!(this.bk instanceof ReadyForQueryPacket));
            }
            catch (IOException e) {
                this.status = -1;
                e.printStackTrace();
                throw new OSQLException("OSCAR-00107", "08001", 107, e.getMessage(), e);
            }
            return this.status == 1;
            {
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cancelRequest(int pid, int ckey) throws IOException, SQLException {
        if (this.logFlag) {
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID() + ", " + OSCARProtocol.class).append(", cancelRequest, paras: ").append("\n");
            sb.append(" pid: ").append(pid).append(", ");
            sb.append(" ckey: ").append(ckey);
            Driver.writeLog(sb.toString());
        }
        OStream cancelStream = null;
        try {
            cancelStream = new OSocket(this.host, this.port, this.info);
            cancelStream.open();
            CancelRequestPacket crp = new CancelRequestPacket(pid, ckey);
            crp.setConnection(this.connection);
            this.sendMessage(cancelStream.getBufferedOutputStream(), crp);
        }
        finally {
            cancelStream.close();
        }
    }

    public BaseResultSet query(String queryStr, int maxRows, BaseStatement stmt) throws SQLException {
        return this.query(queryStr, maxRows, stmt, (BaseResultSet)stmt.getResultSet());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public BaseResultSet query(String queryStr, int maxRows, BaseStatement stmt, BaseResultSet res) throws SQLException {
        print = false;
        if (stmt.isPrint()) {
            print = true;
        }
        if (this.logFlag) {
            sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID() + ", " + OSCARProtocol.class).append(", query, paras: ");
            sb.append(" sql: ").append(queryStr).append(", ");
            sb.append(" maxRows: ").append(maxRows);
            Driver.writeLog(sb.toString());
        }
        if (stmt.getHashColIds() != null && stmt.getHashColIds().length > 0 && queryStr.trim().toUpperCase().startsWith("FETCH ")) {
            return this.queryHashResultSet(queryStr, maxRows, stmt, stmt.getHashColIds(), stmt.getBuckets());
        }
        var6_6 = this.oStream;
        synchronized (var6_6) {
            exception = null;
            this.statement = stmt;
            this.status = 0;
            encodingFlag = this.connection.getEncoding() == null;
            this.resultset = res;
            if (this.resultset != null && !this.resultset.isCursorUsed()) {
                this.resultset = null;
            }
            try {
                marked = 0;
                if (this.statement.isDDLSql()) {
                    marked = 3;
                    this.statement.resetDDLSql(false);
                } else if (this.statement.isPrepareAndNotRealPrepare()) {
                    marked = 4;
                } else if (this.statement.getAutoGeneratedInfo() != -1) {
                    marked = 2;
                    if (queryStr != null && !(sql = this.statement.generatedKeySqlTransform(queryStr)).equals(queryStr)) {
                        queryStr = sql;
                        marked = 0;
                    }
                } else if (this.statement.useTid()) {
                    marked = 1;
                }
                qp = null;
                qp = encodingFlag != false ? new QueryPacket(this.connection.getClientEncoding().encode(queryStr), marked) : new QueryPacket(this.connection.getEncoding().encode(queryStr), marked);
                qp.setConnection(this.connection);
                this.tuples = new ArrayList<E>();
                this.resultTid = false;
                this.tidList = null;
                this.fields = null;
                this.update_count = -2;
                this.insert_tid = 0L;
                this.sendMessage(this.oStream.getBufferedOutputStream(), qp);
                do {
                    timeout = true;
                    getMessageTimes = 0;
                    do {
                        try {
                            this.bk = this.getMessage(this.oStream.getInputStream());
                            timeout = false;
                        }
                        catch (Throwable e) {
                            if (this.isSocketConnectionError(e)) {
                                timeout = false;
                                this.status = -1;
                                throw new OSQLException("OSCAR-00901", "08003", 901, e);
                            }
                            if (this.ping(this.oStream)) {
                                if (e.getMessage().equals("Read timed out") || !this.needRetry(++getMessageTimes)) {
                                    timeout = false;
                                    this.status = -1;
                                    throw new OSQLException("OSCAR-00109", "08003", 109, e);
                                }
                                timeout = true;
                                continue;
                            }
                            timeout = false;
                            this.status = -1;
                            throw new OSQLException("OSCAR-00901", "08003", 901, e);
                        }
                    } while (timeout);
                    if (!(this.bk instanceof RowDescriptionPacket)) ** GOTO lbl95
                    this.getMessage(this.oStream.getInputStream(), this.bk);
                    tempFields = ((RowDescriptionPacket)this.bk).getFields();
                    this.columnCount = tempFields.length;
                    if (this.columnCount <= 0) ** GOTO lbl93
                    if ((this.statement.useTid() || this.statement.getAutoGeneratedInfo() != -1) && tempFields[0].getAliasName().equalsIgnoreCase("ROWID")) {
                        this.resultTid = true;
                        this.tidField = tempFields[0];
                        this.tidList = new ArrayList<E>();
                        if (this.columnCount == 1) continue;
                        this.fields = new Field[this.columnCount - 1];
                        for (i = 0; i < this.columnCount - 1; ++i) {
                            this.fields[i] = tempFields[i + 1];
                        }
                    } else {
                        this.fields = tempFields;
                        continue;
lbl93:
                        // 1 sources

                        this.fields = new Field[0];
                        continue;
lbl95:
                        // 1 sources

                        if (this.bk instanceof AsciiRowPacket) {
                            if (print) {
                                Driver.writeLog("session" + this.pid + "-------D-------" + stmt.getSQL());
                            }
                            ((AsciiRowPacket)this.bk).initTuple(this.columnCount);
                            this.getMessage(this.oStream.getInputStream(), this.bk);
                            tempTuple = ((AsciiRowPacket)this.bk).getTuple();
                            tuple /* !! */  = null;
                            if (this.columnCount <= 0 || maxRows != 0 && this.tuples.size() >= maxRows) continue;
                            if (this.resultTid && tempTuple.length > 0) {
                                this.tidList.add(tempTuple[0]);
                                tuple /* !! */  = new byte[tempTuple.length - 1][];
                                for (i = 0; i < tempTuple.length - 1; ++i) {
                                    tuple /* !! */ [i] = tempTuple[i + 1];
                                }
                            } else {
                                tuple /* !! */  = tempTuple;
                            }
                            this.tuples.add(tuple /* !! */ );
                            continue;
                        }
                        if (this.bk instanceof CompleteResponsePacket) {
                            this.getMessage(this.oStream.getInputStream(), this.bk);
                            command = null;
                            command = encodingFlag != false ? this.connection.getClientEncoding().decode(((CompleteResponsePacket)this.bk).getCommand()) : this.connection.getEncoding().decode(((CompleteResponsePacket)this.bk).getCommand());
                            tag1 = command.charAt(0);
                            tag2 = command.charAt(1);
                            if (tag1 == '5' && tag2 == '0') {
                                tag3 = command.charAt(3);
                                if (tag3 == '0') {
                                    this.statement.setResultSetCanUpdateable(false);
                                } else if (tag3 == '1') {
                                    this.statement.setResultSetCanUpdateable(true);
                                }
                            } else if (tag1 == '3') {
                                if (tag2 == '0') {
                                    this.connection.setInTranscation(true);
                                } else if (tag2 == '1' || tag2 == '2' || tag2 == '3') {
                                    this.connection.setInTranscation(false);
                                }
                            } else if (tag1 == '0' && tag2 != '6' && tag2 <= 'S' || tag1 == '1' && (tag2 == '0' || tag2 == '1') || tag1 == '2' && tag2 != '2' || tag1 == '5' && tag2 == '5' || tag1 == '4' && tag2 == '0' || tag1 == '4' && tag2 == '5' || tag1 == '4' && tag2 == '8' || tag1 == '5' && tag2 == '6' || tag1 == '5' && tag2 == 'D') {
                                if (tag1 == '0' && tag2 != '6' && tag2 <= 'S') {
                                    this.update_count = 0;
                                }
                                if (tag1 == '4' && tag2 == '0') {
                                    this.update_count = 0;
                                }
                                if (tag1 == '2' && (tag2 == '0' || tag2 == '5')) {
                                    this.update_count = Integer.parseInt(command.substring(1 + command.indexOf(32)));
                                }
                                if (tag1 == '2' && tag2 == '1') {
                                    this.update_count = Integer.parseInt(command.substring(command.indexOf(32) + 1, command.lastIndexOf(32)));
                                    this.insert_tid = Long.parseLong(command.substring(1 + command.lastIndexOf(32)));
                                }
                                if (this.resultset == null) {
                                    this.resultset = this.statement.createResultSet(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                    if (this.resultTid) {
                                        this.resultset.setTidValues(this.tidField, this.tidList);
                                    }
                                    if (tag1 == '1' && tag2 == '0' && this.statement.isCursorUsed()) {
                                        this.resultset.setCursorUsed(true);
                                    }
                                    if (this.statement.getResultSetHoldability() == 2) {
                                        this.connection.addCursor(this.resultset.getCursorName());
                                    }
                                } else if (this.resultset.isCursorUsed()) {
                                    moveSize = 0;
                                    moveSize = Integer.parseInt(command.substring(1 + command.indexOf(32)));
                                    if (tag1 == '1' && tag2 == '0') {
                                        this.resultset.reInit(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                        if (this.resultTid) {
                                            this.resultset.setTidValues(this.tidField, this.tidList);
                                        }
                                        this.resultset.setCursorMoveSize(moveSize);
                                    } else if (tag1 == '1' && tag2 == '1') {
                                        this.resultset.setCursorMoveSize(moveSize);
                                    }
                                } else {
                                    rs = this.statement.createResultSet(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                    if (this.resultTid) {
                                        this.resultset.setTidValues(this.tidField, this.tidList);
                                    }
                                    this.resultset.append(rs);
                                    if (this.statement.getResultSetHoldability() == 2) {
                                        this.connection.addCursor(this.resultset.getCursorName());
                                    }
                                }
                            }
                            this.tuples = new ArrayList<E>();
                            this.resultTid = false;
                            this.tidList = null;
                            this.fields = null;
                            this.update_count = -2;
                            this.insert_tid = 0L;
                            continue;
                        }
                        if (this.bk instanceof ParamInforPacket) {
                            this.getMessage(this.oStream.getInputStream(), this.bk);
                            this.statement.setParamInfor(((ParamInforPacket)this.bk).getParamInfo());
                            continue;
                        }
                        if (this.bk instanceof EmptyQueryResponsePacket) {
                            this.getMessage(this.oStream.getInputStream(), this.bk);
                            continue;
                        }
                        if (this.bk instanceof CursorResponsePacket) {
                            this.getMessage(this.oStream.getInputStream(), this.bk);
                            continue;
                        }
                        if (this.bk instanceof ErrorResponsePacket) {
                            this.status = -1;
                            this.getMessage(this.oStream.getInputStream(), this.bk);
                            errorPacket = (ErrorResponsePacket)this.bk;
                            if (encodingFlag) {
                                if (exception == null) {
                                    exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                                    continue;
                                }
                                exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                                continue;
                            }
                            if (exception == null) {
                                exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                                continue;
                            }
                            exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                            continue;
                        }
                        if (this.bk instanceof NoticeResponsePacket) {
                            this.getMessage(this.oStream.getInputStream(), this.bk);
                            if (encodingFlag) {
                                this.statement.addWarning(this.connection.getClientEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                                continue;
                            }
                            this.statement.addWarning(this.connection.getEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                            continue;
                        }
                        if (this.bk instanceof ImportPacket) {
                            if (encodingFlag) {
                                ((ImportPacket)this.bk).setEncoding(this.statement.getDBConnection().getClientEncoding());
                            } else {
                                ((ImportPacket)this.bk).setEncoding(this.statement.getDBConnection().getEncoding());
                            }
                            ((ImportPacket)this.bk).setImportValues(this.statement.getImportValues());
                            this.sendMessage(this.oStream.getBufferedOutputStream(), this.bk);
                            this.statement.importValues(null);
                            continue;
                        }
                        if (this.bk instanceof ImportExportResponsePacket) {
                            this.getMessage(this.oStream.getInputStream(), this.bk);
                            this.statement.setTransferRowCount(((ImportExportResponsePacket)this.bk).getAmount());
                            continue;
                        }
                        if (this.bk instanceof ReadyForQueryPacket) {
                            this.status = 1;
                            this.getMessage(this.oStream.getInputStream(), this.bk);
                            continue;
                        }
                        this.status = -1;
                        throw new OSQLException("OSCAR-00109", "08003", 109);
                    }
                } while (!(this.bk instanceof ReadyForQueryPacket));
                if (exception != null) {
                    throw exception;
                }
            }
            catch (SocketTimeoutException e) {
                this.status = -1;
                throw new OSQLException("OSCAR-00901", "08003", 901, e);
            }
            catch (IOException e) {
                this.status = -1;
                throw new OSQLException("OSCAR-00109", "08003", 109, e);
            }
            return this.resultset;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object[] functionCall(int funcOID, int paraCount, int[] paraLenth, Object[] paraValue) throws SQLException {
        if (this.logFlag) {
            int i;
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID() + ", " + OSCARProtocol.class).append(", functionCall, paras: ");
            sb.append(" funcOID: ").append(funcOID).append(", ");
            sb.append(" paraCount: ").append(paraCount);
            sb.append(" funcOID: ").append(funcOID);
            if (paraLenth != null) {
                sb.append("paraLenth: ");
                for (i = 0; i < paraLenth.length; ++i) {
                    sb.append(paraLenth[i]).append(" ");
                }
            }
            if (paraLenth != null) {
                sb.append("paraValue: ");
                for (i = 0; i < paraValue.length; ++i) {
                    if (paraValue[i] instanceof byte[]) {
                        sb.append(Arrays.toString((byte[])paraValue[i]));
                        continue;
                    }
                    sb.append(paraValue[i]).append(" ");
                }
            }
            Driver.writeLog(sb.toString());
        }
        FunctionCallPacket fcp = new FunctionCallPacket(funcOID, paraCount, paraLenth, paraValue);
        fcp.setConnection(this.connection);
        Object[] result = new Object[3];
        boolean encodingFlag = this.connection.getEncoding() == null;
        OStream oStream = this.oStream;
        synchronized (oStream) {
            OSQLException exception = null;
            this.status = 0;
            try {
                this.sendMessage(this.oStream.getBufferedOutputStream(), fcp);
            }
            catch (IOException e) {
                throw new OSQLException("OSCAR-00108", "88888", 108, e.getMessage(), e);
            }
            try {
                do {
                    this.bk = this.getMessage(this.oStream.getInputStream());
                    if (this.bk instanceof ErrorResponsePacket) {
                        this.status = -1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                        if (encodingFlag) {
                            if (exception == null) {
                                exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                                continue;
                            }
                            exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                            continue;
                        }
                        if (exception == null) {
                            exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                            continue;
                        }
                        exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                        continue;
                    }
                    if (this.bk instanceof NoticeResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        if (encodingFlag) {
                            this.connection.addWarning(this.connection.getClientEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), this.connection.getClientEncoding().decode(((NoticeResponsePacket)this.bk).getSQLState()));
                            continue;
                        }
                        this.connection.addWarning(this.connection.getEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), this.connection.getEncoding().decode(((NoticeResponsePacket)this.bk).getSQLState()));
                        continue;
                    }
                    if (this.bk instanceof FunctionResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        result[0] = new Boolean(((FunctionResponsePacket)this.bk).isNull());
                        result[1] = new Integer(((FunctionResponsePacket)this.bk).getResultSize());
                        result[2] = ((FunctionResponsePacket)this.bk).getResult();
                        continue;
                    }
                    if (this.bk instanceof ReadyForQueryPacket) {
                        this.status = 1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    this.status = -1;
                    throw new OSQLException("OSCAR-00109", "08003", 109);
                } while (!(this.bk instanceof ReadyForQueryPacket));
            }
            catch (SocketTimeoutException e) {
                this.status = -1;
                throw new OSQLException("OSCAR-00901", "08003", 901, e);
            }
            catch (IOException e) {
                this.status = -1;
                throw new OSQLException("OSCAR-00109", "08003", 109, e);
            }
            if (exception != null) {
                throw exception;
            }
            return result;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void batchProcess(BatchProcessPacket batchPacket, BaseStatement stmt, int[] updateCounts) throws SQLException {
        if (this.logFlag) {
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID() + ", " + OSCARProtocol.class).append(", batchProcess");
            Driver.writeLog(sb.toString());
        }
        OStream oStream = this.oStream;
        synchronized (oStream) {
            this.statement = stmt;
            int size = batchPacket.size();
            int position = 0;
            try {
                int j = 0;
                for (j = 0; j < size; ++j) {
                    batchPacket.writeRow();
                    if (!batchPacket.checkBuffer()) continue;
                    batchPacket.sendBatch(this.oStream.getBufferedOutputStream());
                    this.receiveBatchResult(position, updateCounts);
                    batchPacket.reInit();
                    position = j + 1;
                    this.statement.setUpdateBatchSize(position);
                }
                if (position != j) {
                    batchPacket.sendBatch(this.oStream.getBufferedOutputStream());
                    this.receiveBatchResult(position, updateCounts);
                    this.statement.setUpdateBatchSize(j);
                }
            }
            catch (IOException ex) {
                throw new OSQLException("OSCAR-00109", "08003", 109, ex);
            }
        }
    }

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public void receiveBatchResult(int position, int[] updateCounts) throws IOException, SQLException {
        p = position;
        exception = null;
        encodingFlag = this.connection.getEncoding() == null;
        do {
            this.bk = this.getMessage(this.oStream.getInputStream());
            if (!(this.bk instanceof RowDescriptionPacket)) ** GOTO lbl25
            this.getMessage(this.oStream.getInputStream(), this.bk);
            tempFields = ((RowDescriptionPacket)this.bk).getFields();
            this.columnCount = tempFields.length;
            if (this.columnCount <= 0) ** GOTO lbl23
            if ((this.statement.useTid() || this.statement.getAutoGeneratedInfo() != -1) && tempFields[0].getAliasName().equalsIgnoreCase("ROWID")) {
                this.resultTid = true;
                this.tidField = tempFields[0];
                this.tidList = new ArrayList<E>();
                if (this.columnCount == 1) continue;
                this.fields = new Field[this.columnCount - 1];
                for (i = 0; i < this.columnCount - 1; ++i) {
                    this.fields[i] = tempFields[i + 1];
                }
            } else {
                this.fields = tempFields;
                continue;
lbl23:
                // 1 sources

                this.fields = new Field[0];
                continue;
lbl25:
                // 1 sources

                if (this.bk instanceof AsciiRowPacket) {
                    ((AsciiRowPacket)this.bk).initTuple(this.columnCount);
                    this.getMessage(this.oStream.getInputStream(), this.bk);
                    tempTuple = ((AsciiRowPacket)this.bk).getTuple();
                    tuple /* !! */  = null;
                    if (this.columnCount <= 0) continue;
                    if (this.resultTid && tempTuple.length > 0) {
                        this.tidList.add(tempTuple[0]);
                        tuple /* !! */  = new byte[tempTuple.length - 1][];
                        for (i = 0; i < tempTuple.length - 1; ++i) {
                            tuple /* !! */ [i] = tempTuple[i + 1];
                        }
                    } else {
                        tuple /* !! */  = tempTuple;
                    }
                    this.tuples.add(tuple /* !! */ );
                    continue;
                }
                if (this.bk instanceof CompleteResponsePacket) {
                    this.getMessage(this.oStream.getInputStream(), this.bk);
                    command = this.connection.getEncoding().decode(((CompleteResponsePacket)this.bk).getCommand());
                    tag1 = command.charAt(0);
                    tag2 = command.charAt(1);
                    if (tag1 == '5' && tag2 == '0') {
                        tag3 = command.charAt(3);
                        if (tag3 == '0') {
                            this.statement.setResultSetCanUpdateable(false);
                        } else if (tag3 == '1') {
                            this.statement.setResultSetCanUpdateable(true);
                        }
                    } else if (tag1 == '3') {
                        if (tag2 == '0') {
                            this.connection.setInTranscation(true);
                        } else if (tag2 == '1' || tag2 == '2' || tag2 == '3') {
                            this.connection.setInTranscation(false);
                        }
                    } else if (tag1 == '0' && tag2 != '6' && tag2 <= 'S' || tag1 == '1' && (tag2 == '0' || tag2 == '1') || tag1 == '2' && tag2 != '2' || tag1 == '5' && tag2 == '5' || tag1 == '4' && tag2 == '0' || tag1 == '4' && tag2 == '5' || tag1 == '4' && tag2 == '8' || tag1 == '5' && tag2 == '6' || tag1 == '5' && tag2 == 'D') {
                        if (tag1 == '0' && tag2 != '6' && tag2 <= 'S') {
                            this.update_count = 0;
                        }
                        if (tag1 == '4' && tag2 == '0') {
                            this.update_count = 0;
                        }
                        if (tag1 == '2' && (tag2 == '0' || tag2 == '5')) {
                            this.update_count = Integer.parseInt(command.substring(1 + command.indexOf(32)));
                        }
                        if (tag1 == '2' && tag2 == '1') {
                            this.update_count = Integer.parseInt(command.substring(command.indexOf(32) + 1, command.lastIndexOf(32)));
                            this.insert_tid = Long.parseLong(command.substring(1 + command.lastIndexOf(32)));
                        }
                        if (this.resultset == null) {
                            this.resultset = this.statement.createResultSet(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                            updateCounts[p++] = this.update_count;
                            if (this.resultTid) {
                                this.resultset.setTidValues(this.tidField, this.tidList);
                            }
                            if (tag1 == '1' && tag2 == '0' && this.statement.isCursorUsed()) {
                                this.resultset.setCursorUsed(true);
                            }
                            if (this.statement.getResultSetHoldability() == 2) {
                                this.connection.addCursor(this.resultset.getCursorName());
                            }
                            this.statement.setResultSet(this.resultset);
                        } else if (this.resultset.isCursorUsed()) {
                            moveSize = 0;
                            commands = command.split(" ");
                            moveSize = Integer.parseInt(commands[1]);
                            if (tag1 == '1' && tag2 == '0') {
                                this.resultset.reInit(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                if (this.resultTid) {
                                    this.resultset.setTidValues(this.tidField, this.tidList);
                                }
                                this.resultset.setCursorMoveSize(moveSize);
                            } else if (tag1 == '1' && tag2 == '1') {
                                this.resultset.setCursorMoveSize(moveSize);
                            }
                        } else {
                            updateCounts[p++] = this.update_count;
                        }
                    }
                    this.tuples = new ArrayList<E>();
                    this.resultTid = false;
                    this.tidList = null;
                    this.fields = null;
                    this.update_count = -2;
                    this.insert_tid = 0L;
                    continue;
                }
                if (this.bk instanceof ParamInforPacket) {
                    this.getMessage(this.oStream.getInputStream(), this.bk);
                    this.statement.setParamInfor(((ParamInforPacket)this.bk).getParamInfo());
                    continue;
                }
                if (this.bk instanceof EmptyQueryResponsePacket) {
                    this.getMessage(this.oStream.getInputStream(), this.bk);
                    continue;
                }
                if (this.bk instanceof CursorResponsePacket) {
                    this.getMessage(this.oStream.getInputStream(), this.bk);
                    pname = this.connection.getEncoding().decode(((CursorResponsePacket)this.bk).getCursorName());
                    continue;
                }
                if (this.bk instanceof ErrorResponsePacket) {
                    this.status = -1;
                    this.getMessage(this.oStream.getInputStream(), this.bk);
                    errorPacket = (ErrorResponsePacket)this.bk;
                    if (encodingFlag) {
                        if (exception == null) {
                            exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                            continue;
                        }
                        exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                        continue;
                    }
                    if (exception == null) {
                        exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                        continue;
                    }
                    exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                    continue;
                }
                if (this.bk instanceof NoticeResponsePacket) {
                    this.getMessage(this.oStream.getInputStream(), this.bk);
                    this.statement.addWarning(new String(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                    continue;
                }
                if (this.bk instanceof ImportPacket) {
                    ((ImportPacket)this.bk).setEncoding(this.statement.getDBConnection().getEncoding());
                    ((ImportPacket)this.bk).setImportValues(this.statement.getImportValues());
                    this.sendMessage(this.oStream.getBufferedOutputStream(), this.bk);
                    this.statement.importValues(null);
                    continue;
                }
                if (this.bk instanceof ImportExportResponsePacket) {
                    this.getMessage(this.oStream.getInputStream(), this.bk);
                    this.statement.setTransferRowCount(((ImportExportResponsePacket)this.bk).getAmount());
                    continue;
                }
                if (this.bk instanceof ReadyForQueryPacket) {
                    this.status = 1;
                    this.getMessage(this.oStream.getInputStream(), this.bk);
                    continue;
                }
                this.status = -1;
                throw new OSQLException("OSCAR-00109", "08003", 109);
            }
        } while (!(this.bk instanceof ReadyForQueryPacket));
        if (exception != null) {
            throw exception;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() throws IOException, SQLException {
        TerminatePacket tp = new TerminatePacket();
        tp.setConnection(this.connection);
        if (this.oStream != null) {
            OStream oStream = this.oStream;
            synchronized (oStream) {
                try {
                    this.sendMessage(this.oStream.getBufferedOutputStream(), tp);
                    int flag = 1;
                    this.oStream.setSocketTimeOut(1000);
                    while (flag != -1) {
                        try {
                            flag = this.oStream.getInputStream().read();
                        }
                        catch (Exception e) {
                            // empty catch block
                            break;
                        }
                    }
                }
                catch (IOException ioex) {
                    throw ioex;
                }
                catch (SQLException sqlEx) {
                    throw sqlEx;
                }
                finally {
                    this.oStream.close();
                }
            }
        }
        tp = null;
        this.oStream = null;
        this.connection = null;
        this.host = "ErrorIP";
        this.database = null;
        this.db_user = null;
        this.db_passwd = null;
        this.statement = null;
        this.resultset = null;
        this.callResult = null;
        this.fields = null;
        this.tidField = null;
        this.tuples = null;
        this.bk = null;
        this.pk = new Packet();
    }

    protected void sendMessage(BufferedOutputStream stream, BasePacket packet) throws IOException, SQLException {
        packet.sendTo(stream);
    }

    protected BasePacket getMessage(InputStream stream) throws IOException, SQLException {
        byte[] tagTemp = new byte[1];
        stream.read(tagTemp, 0, 1);
        char tag = (char)tagTemp[0];
        if (this.logFlag) {
            if ('\uffa1' == tag) {
                Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OSCARProtocol.class + ", getMessage(InputStream stream)), return tag: 0xA1");
            } else {
                Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OSCARProtocol.class + ", getMessage(InputStream stream)), return tag: " + tag);
            }
        }
        return this.pk.getInstance(tag, this.connection);
    }

    protected void getMessage(InputStream stream, BasePacket packet) throws IOException, SQLException {
        packet.receiveFrom(stream);
    }

    protected byte[] getMessage(InputStream stream, int len) throws IOException, SQLException {
        byte[] tagTemp = new byte[len];
        stream.read(tagTemp, 0, len);
        return tagTemp;
    }

    public static String convertString(String commonString) {
        if (commonString.length() > 0 && commonString.charAt(0) == '\"' && commonString.charAt(commonString.length() - 1) == '\"') {
            return commonString.substring(1, commonString.length() - 1);
        }
        return commonString.toUpperCase();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void importBegin(QueryPacket qp) throws SQLException {
        if (this.logFlag) {
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID()).append(", " + OSCARProtocol.class).append(", importBegin(QueryPacket qp)");
            Driver.writeLog(sb.toString());
        }
        OStream oStream = this.oStream;
        synchronized (oStream) {
            try {
                this.sendMessage(this.oStream.getBufferedOutputStream(), qp);
                BasePacket readyBk = null;
                boolean encodingFlag = this.connection.getEncoding() == null;
                do {
                    this.bk = this.getMessage(this.oStream.getInputStream());
                    if (this.bk instanceof CompleteResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    if (this.bk instanceof NewImportPacket || this.bk instanceof ReadyForQueryPacket) {
                        this.status = 1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    if (this.bk instanceof ErrorResponsePacket) {
                        this.status = -1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        readyBk = this.getMessage(this.oStream.getInputStream());
                        while (!(readyBk instanceof ReadyForQueryPacket) && !(this.bk instanceof NewImportPacket)) {
                            readyBk = this.getMessage(this.oStream.getInputStream());
                        }
                        ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                        if (encodingFlag) {
                            throw new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                        }
                        throw new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                    }
                    this.status = -1;
                    throw new OSQLException("OSCAR-00109", "08003", 109);
                } while (!(this.bk instanceof ReadyForQueryPacket) && !(this.bk instanceof NewImportPacket));
            }
            catch (IOException e) {
                throw new OSQLException("OSCAR-00804", "88888", 804, e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void importData(byte[] b) throws SQLException {
        if (this.logFlag) {
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID()).append(", " + OSCARProtocol.class).append(", importData(byte[] b), data:");
            sb.append("[");
            for (byte c : b) {
                sb.append(c).append(" ");
            }
            sb.append("]");
            Driver.writeLog(sb.toString());
        }
        OStream oStream = this.oStream;
        synchronized (oStream) {
            try {
                if (b.length != 0) {
                    this.oStream.getBufferedOutputStream().write(b);
                }
            }
            catch (IOException e) {
                throw new OSQLException("OSCAR-00804", "88888", 804, e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void importData(byte[] b, int off, int len) throws SQLException {
        if (this.logFlag) {
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID()).append(", " + OSCARProtocol.class).append(", importData(byte[] b, int off, int len) off= " + off + " len= " + len).append(", data:");
            sb.append("[");
            for (byte c : b) {
                sb.append(c).append(" ");
            }
            sb.append("]");
            Driver.writeLog(sb.toString());
        }
        OStream oStream = this.oStream;
        synchronized (oStream) {
            try {
                if (b.length != 0) {
                    this.oStream.getBufferedOutputStream().write(b, off, len);
                }
            }
            catch (IOException e) {
                throw new OSQLException("OSCAR-00804", "88888", 804, e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void importEnd() throws SQLException {
        if (this.logFlag) {
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID()).append(", " + OSCARProtocol.class).append(", importEnd()");
            Driver.writeLog(sb.toString());
        }
        OStream oStream = this.oStream;
        synchronized (oStream) {
            try {
                BasePacket.SendChar(this.oStream.getBufferedOutputStream(), 67);
                this.oStream.getBufferedOutputStream().flush();
                OSQLException exception = null;
                boolean encodingFlag = this.connection.getEncoding() == null;
                do {
                    this.bk = this.getMessage(this.oStream.getInputStream());
                    if (this.bk instanceof NoticeResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        if (encodingFlag) {
                            this.connection.addWarning(this.connection.getClientEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), this.connection.getClientEncoding().decode(((NoticeResponsePacket)this.bk).getSQLState()));
                            continue;
                        }
                        this.connection.addWarning(this.connection.getEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), this.connection.getEncoding().decode(((NoticeResponsePacket)this.bk).getSQLState()));
                        continue;
                    }
                    if (this.bk instanceof CompleteResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.cmd = ((CompleteResponsePacket)this.bk).getCommand();
                        this.update_count = Integer.parseInt(this.connection.getEncoding().decode(this.cmd, 3, this.cmd.length - 3));
                        continue;
                    }
                    if (this.bk instanceof ErrorResponsePacket) {
                        this.status = -1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                        if (encodingFlag) {
                            if (exception == null) {
                                exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                                continue;
                            }
                            exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                            continue;
                        }
                        if (exception == null) {
                            exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                            continue;
                        }
                        exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                        continue;
                    }
                    if (this.bk instanceof ReadyForQueryPacket) {
                        this.status = 1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    this.status = -1;
                    throw new OSQLException("OSCAR-00109", "08003", 109);
                } while (!(this.bk instanceof ReadyForQueryPacket));
                this.handler.setUpdateCount(this.update_count);
                if (exception != null) {
                    throw exception;
                }
            }
            catch (IOException e) {
                throw new OSQLException("OSCAR-00804", "88888", 804, e);
            }
        }
    }

    public void setImportHandler(OscarImportHandler handler) {
        this.handler = handler;
    }

    public void setImportBlogHandler(ImportBinlogHandler handler) {
        this.importBinlogHandler = handler;
    }

    public void setExportBlogHandler(ExportBinlogHandler handler) {
        this.exportBinlogHandler = handler;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean ping(OStream ostream) {
        boolean flag = true;
        Socket socket = null;
        try {
            socket = ostream.getSocket();
            socket.setOOBInline(false);
            socket.sendUrgentData(255);
        }
        catch (Throwable e) {
            flag = false;
        }
        finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public byte[] backUpKstore(String queryStr) throws SQLException {
        QueryPacket qp = new QueryPacket(this.connection.getEncoding().encode(queryStr), 0);
        qp.setConnection(this.connection);
        byte[] result = null;
        boolean encodingFlag = this.connection.getEncoding() == null;
        OStream oStream = this.oStream;
        synchronized (oStream) {
            OSQLException exception = null;
            this.status = 0;
            try {
                this.sendMessage(this.oStream.getBufferedOutputStream(), qp);
            }
            catch (IOException e) {
                throw new OSQLException("OSCAR-00108", "88888", 108, e.getMessage(), e);
            }
            try {
                do {
                    this.bk = this.getMessage(this.oStream.getInputStream());
                    if (this.bk instanceof ErrorResponsePacket) {
                        this.status = -1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                        if (encodingFlag) {
                            if (exception == null) {
                                exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                                continue;
                            }
                            exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                            continue;
                        }
                        if (exception == null) {
                            exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                            continue;
                        }
                        exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                        continue;
                    }
                    if (this.bk instanceof CompleteResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    if (this.bk instanceof BackupMetaDataPacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        result = ((BackupMetaDataPacket)this.bk).getMetaData();
                        break;
                    }
                    if (this.bk instanceof BackupPhysicalDataPacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    if (this.bk instanceof BackupPhysicalRowEndPacket) continue;
                    if (this.bk instanceof ReadyForQueryPacket) {
                        this.status = 1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    this.status = -1;
                    throw new OSQLException("OSCAR-00109", "08003", 109);
                } while (!(this.bk instanceof ReadyForQueryPacket));
            }
            catch (SocketTimeoutException e) {
                this.status = -1;
                throw new OSQLException("OSCAR-00901", "08003", 901, e);
            }
            catch (IOException e) {
                this.status = -1;
                throw new OSQLException("OSCAR-00109", "08003", 109, e);
            }
            if (exception != null) {
                throw exception;
            }
            return result;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public byte[] getNextPhysicalDataRow() throws SQLException {
        boolean encodingFlag;
        byte[] result = null;
        boolean bl = encodingFlag = this.connection.getEncoding() == null;
        if (this.oStream != null) {
            OStream oStream = this.oStream;
            synchronized (oStream) {
                OSQLException exception = null;
                try {
                    do {
                        this.bk = this.getMessage(this.oStream.getInputStream());
                        if (this.bk instanceof ErrorResponsePacket) {
                            this.status = -1;
                            this.getMessage(this.oStream.getInputStream(), this.bk);
                            ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                            if (encodingFlag) {
                                if (exception == null) {
                                    exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                                    continue;
                                }
                                exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                                continue;
                            }
                            if (exception == null) {
                                exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                                continue;
                            }
                            exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                            continue;
                        }
                        if (this.bk instanceof CompleteResponsePacket) {
                            this.getMessage(this.oStream.getInputStream(), this.bk);
                            continue;
                        }
                        if (this.bk instanceof BackupMetaDataPacket) continue;
                        if (this.bk instanceof BackupPhysicalDataPacket) {
                            this.getMessage(this.oStream.getInputStream(), this.bk);
                            result = ((BackupPhysicalDataPacket)this.bk).getPhysicalData();
                            continue;
                        }
                        if (this.bk instanceof BackupPhysicalRowEndPacket) continue;
                        if (this.bk instanceof ReadyForQueryPacket) {
                            this.status = 1;
                            this.getMessage(this.oStream.getInputStream(), this.bk);
                            break;
                        }
                        this.status = -1;
                        throw new OSQLException("OSCAR-00109", "08003", 109);
                    } while (!(this.bk instanceof BackupPhysicalDataPacket));
                }
                catch (SocketTimeoutException e) {
                    this.status = -1;
                    throw new OSQLException("OSCAR-00901", "08003", 901, e);
                }
                catch (IOException e) {
                    this.status = -1;
                    throw new OSQLException("OSCAR-00109", "08003", 109, e);
                }
                if (exception != null) {
                    throw exception;
                }
            }
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void restoreKstore(String queryStr, byte[] metaData, byte[] physicalRowData) throws SQLException {
        OStream oStream = this.oStream;
        synchronized (oStream) {
            QueryPacket qp = new QueryPacket(this.connection.getEncoding().encode(queryStr), 0);
            qp.setConnection(this.connection);
            OSQLException exception = null;
            boolean encodingFlag = this.connection.getEncoding() == null;
            this.status = 0;
            try {
                this.sendMessage(this.oStream.getBufferedOutputStream(), qp);
            }
            catch (IOException e) {
                throw new OSQLException("OSCAR-00108", "88888", 108, e.getMessage());
            }
            try {
                do {
                    this.bk = this.getMessage(this.oStream.getInputStream());
                    if (this.bk instanceof ErrorResponsePacket) {
                        this.status = -1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                        if (encodingFlag) {
                            if (exception == null) {
                                exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                                continue;
                            }
                            exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                            continue;
                        }
                        if (exception == null) {
                            exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                            continue;
                        }
                        exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                        continue;
                    }
                    if (this.bk instanceof CompleteResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    if (this.bk instanceof ReadyForMetaData) {
                        BackupMetaDataPacket metaDataPacket = new BackupMetaDataPacket(metaData);
                        metaDataPacket.setConnection(this.connection);
                        metaDataPacket.sendTo(this.oStream.getBufferedOutputStream());
                        continue;
                    }
                    if (this.bk instanceof ReadyForPhysicalDataPacket) {
                        BackupPhysicalDataPacket physicalDataPacket = new BackupPhysicalDataPacket(physicalRowData);
                        physicalDataPacket.setConnection(this.connection);
                        physicalDataPacket.sendTo(this.oStream.getBufferedOutputStream());
                        this.oStream.getBufferedOutputStream().flush();
                        BasePacket.SendChar(this.oStream.getBufferedOutputStream(), 111);
                        this.oStream.getBufferedOutputStream().flush();
                        continue;
                    }
                    if (this.bk instanceof ReadyForQueryPacket) {
                        this.status = 1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    this.status = -1;
                    throw new OSQLException("OSCAR-00109", "08003", 109);
                } while (!(this.bk instanceof ReadyForQueryPacket));
            }
            catch (SocketTimeoutException e) {
                this.status = -1;
                throw new OSQLException("OSCAR-00901", "08003", 901, e);
            }
            catch (IOException e) {
                this.status = -1;
                throw new OSQLException("OSCAR-00109", "08003", 109, e);
            }
            if (exception != null) {
                throw exception;
            }
        }
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public void setCkey(int ckey) {
        this.ckey = ckey;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public BaseResultSet queryHashResultSet(String queryStr, int maxRows, BaseStatement stmt, int[] bindIDs, int buckets) throws SQLException {
        if (this.logFlag) {
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID() + ", " + OSCARProtocol.class).append(", query, paras: ");
            sb.append(" sql: ").append(queryStr).append(", ");
            sb.append(" maxRows: ").append(maxRows);
            Driver.writeLog(sb.toString());
        }
        OStream oStream = this.oStream;
        synchronized (oStream) {
            OSQLException exception = null;
            this.statement = stmt;
            this.status = 0;
            boolean encodingFlag = this.connection.getEncoding() == null;
            this.resultset = (BaseResultSet)stmt.getResultSet();
            if (this.resultset != null && !this.resultset.isCursorUsed()) {
                this.resultset = null;
            }
            try {
                int marked = -1;
                QueryPacketHash qp = null;
                qp = encodingFlag ? new QueryPacketHash(this.connection.getClientEncoding().encode(queryStr), marked, bindIDs, buckets) : new QueryPacketHash(this.connection.getEncoding().encode(queryStr), marked, bindIDs, buckets);
                qp.setConnection(this.connection);
                this.tuples = new ArrayList();
                this.resultTid = false;
                this.tidList = null;
                this.fields = null;
                this.update_count = -2;
                this.insert_tid = 0L;
                this.sendMessage(this.oStream.getBufferedOutputStream(), qp);
                do {
                    block54: {
                        block56: {
                            String command;
                            block59: {
                                char tag2;
                                char tag1;
                                block58: {
                                    block57: {
                                        block55: {
                                            boolean timeout = true;
                                            int getMessageTimes = 0;
                                            do {
                                                try {
                                                    this.bk = this.getMessage(this.oStream.getInputStream());
                                                    timeout = false;
                                                }
                                                catch (Throwable e) {
                                                    if (this.isSocketConnectionError(e)) {
                                                        timeout = false;
                                                        this.status = -1;
                                                        throw new OSQLException("OSCAR-00901", "08003", 901, e);
                                                    }
                                                    if (!this.ping(this.oStream)) {
                                                        timeout = false;
                                                        this.status = -1;
                                                        throw new OSQLException("OSCAR-00901", "08003", 901, e);
                                                    }
                                                    if (e.getMessage().equals("Read timed out") || !this.needRetry(++getMessageTimes)) {
                                                        timeout = false;
                                                        this.status = -1;
                                                        throw new OSQLException("OSCAR-00109", "08003", 109, e);
                                                    }
                                                    timeout = true;
                                                }
                                            } while (timeout);
                                            if (this.bk instanceof RowDescriptionPacket) {
                                                this.getMessage(this.oStream.getInputStream(), this.bk);
                                                Field[] tempFields = ((RowDescriptionPacket)this.bk).getFields();
                                                this.columnCount = tempFields.length;
                                                if (this.columnCount > 0) {
                                                    if ((this.statement.useTid() || this.statement.getAutoGeneratedInfo() != -1) && tempFields[0].getAliasName().equalsIgnoreCase("ROWID")) {
                                                        this.resultTid = true;
                                                        this.tidField = tempFields[0];
                                                        this.tidList = new ArrayList();
                                                        if (this.columnCount == 1) continue;
                                                        this.fields = new Field[this.columnCount - 1];
                                                        for (int i = 0; i < this.columnCount - 1; ++i) {
                                                            this.fields[i] = tempFields[i + 1];
                                                        }
                                                        continue;
                                                    } else {
                                                        this.fields = tempFields;
                                                        continue;
                                                    }
                                                }
                                                this.fields = new Field[0];
                                                continue;
                                            }
                                            if (!(this.bk instanceof CompleteResponsePacket)) break block54;
                                            this.getMessage(this.oStream.getInputStream(), this.bk);
                                            command = null;
                                            command = encodingFlag ? this.connection.getClientEncoding().decode(((CompleteResponsePacket)this.bk).getCommand()) : this.connection.getEncoding().decode(((CompleteResponsePacket)this.bk).getCommand());
                                            tag1 = command.charAt(0);
                                            tag2 = command.charAt(1);
                                            if (tag1 != '5' || tag2 != '0') break block55;
                                            char tag3 = command.charAt(3);
                                            if (tag3 == '0') {
                                                this.statement.setResultSetCanUpdateable(false);
                                                break block56;
                                            } else if (tag3 == '1') {
                                                this.statement.setResultSetCanUpdateable(true);
                                            }
                                            break block56;
                                        }
                                        if (tag1 != '3') break block57;
                                        if (tag2 == '0') {
                                            this.connection.setInTranscation(true);
                                            break block56;
                                        } else if (tag2 == '1' || tag2 == '2' || tag2 == '3') {
                                            this.connection.setInTranscation(false);
                                        }
                                        break block56;
                                    }
                                    if (!(tag1 == '0' && tag2 != '6' && tag2 <= 'S' || tag1 == '1' && (tag2 == '0' || tag2 == '1') || tag1 == '2' && tag2 != '2' || tag1 == '5' && tag2 == '5' || tag1 == '4' && tag2 == '0' || tag1 == '4' && tag2 == '5' || tag1 == '4' && tag2 == '8' || tag1 == '5' && tag2 == '6') && (tag1 != '5' || tag2 != 'D')) break block56;
                                    if (tag1 == '0' && tag2 != '6' && tag2 <= 'S') {
                                        this.update_count = 0;
                                    }
                                    if (tag1 == '4' && tag2 == '0') {
                                        this.update_count = 0;
                                    }
                                    if (tag1 == '2' && (tag2 == '0' || tag2 == '5')) {
                                        this.update_count = Integer.parseInt(command.substring(1 + command.indexOf(32)));
                                    }
                                    if (tag1 == '2' && tag2 == '1') {
                                        this.update_count = Integer.parseInt(command.substring(command.indexOf(32) + 1, command.lastIndexOf(32)));
                                        this.insert_tid = Long.parseLong(command.substring(1 + command.lastIndexOf(32)));
                                    }
                                    if (this.resultset != null) break block58;
                                    this.resultset = this.statement.createResultSet(this.fields, this.tuples, command, this.update_count, this.insert_tid, 1);
                                    if (this.resultTid) {
                                        this.resultset.setTidValues(this.tidField, this.tidList);
                                    }
                                    if (tag1 == '1' && tag2 == '0' && this.statement.isCursorUsed()) {
                                        this.resultset.setCursorUsed(true);
                                    }
                                    if (this.statement.getResultSetHoldability() == 2) {
                                        this.connection.addCursor(this.resultset.getCursorName());
                                    }
                                    break block56;
                                }
                                if (!this.resultset.isCursorUsed()) break block59;
                                int moveSize = 0;
                                moveSize = Integer.parseInt(command.substring(1 + command.indexOf(32)));
                                this.fields = new Field[3];
                                if (tag1 == '1' && tag2 == '0') {
                                    this.resultset.reInit(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                    if (this.resultTid) {
                                        this.resultset.setTidValues(this.tidField, this.tidList);
                                    }
                                    this.resultset.setCursorMoveSize(moveSize);
                                    break block56;
                                } else if (tag1 == '1' && tag2 == '1') {
                                    this.resultset.setCursorMoveSize(moveSize);
                                }
                                break block56;
                            }
                            BaseResultSet rs = this.statement.createResultSet(this.fields, this.tuples, command, this.update_count, this.insert_tid, 1);
                            if (this.resultTid) {
                                this.resultset.setTidValues(this.tidField, this.tidList);
                            }
                            this.resultset.append(rs);
                            if (this.statement.getResultSetHoldability() == 2) {
                                this.connection.addCursor(this.resultset.getCursorName());
                            }
                        }
                        this.tuples = new ArrayList();
                        this.resultTid = false;
                        this.tidList = null;
                        this.fields = null;
                        this.update_count = -2;
                        this.insert_tid = 0L;
                        continue;
                    }
                    if (this.bk instanceof HashDataPacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        HashDataPacket hashDataPacket = (HashDataPacket)this.bk;
                        byte[][] tuple = new byte[][]{hashDataPacket.getHashkey(), hashDataPacket.getDataSize(), hashDataPacket.getData()};
                        this.tuples.add(tuple);
                        continue;
                    }
                    if (this.bk instanceof ParamInforPacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.statement.setParamInfor(((ParamInforPacket)this.bk).getParamInfo());
                        continue;
                    }
                    if (this.bk instanceof EmptyQueryResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    if (this.bk instanceof CursorResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    if (this.bk instanceof ErrorResponsePacket) {
                        this.status = -1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                        if (encodingFlag) {
                            if (exception == null) {
                                exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                                continue;
                            }
                            exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                            continue;
                        }
                        if (exception == null) {
                            exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                            continue;
                        }
                        exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                        continue;
                    }
                    if (this.bk instanceof NoticeResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        if (encodingFlag) {
                            this.statement.addWarning(this.connection.getClientEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                            continue;
                        }
                        this.statement.addWarning(this.connection.getEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                        continue;
                    }
                    if (this.bk instanceof ImportPacket) {
                        if (encodingFlag) {
                            ((ImportPacket)this.bk).setEncoding(this.statement.getDBConnection().getClientEncoding());
                        } else {
                            ((ImportPacket)this.bk).setEncoding(this.statement.getDBConnection().getEncoding());
                        }
                        ((ImportPacket)this.bk).setImportValues(this.statement.getImportValues());
                        this.sendMessage(this.oStream.getBufferedOutputStream(), this.bk);
                        this.statement.importValues(null);
                        continue;
                    }
                    if (this.bk instanceof ImportExportResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.statement.setTransferRowCount(((ImportExportResponsePacket)this.bk).getAmount());
                        continue;
                    }
                    if (!(this.bk instanceof ReadyForQueryPacket)) {
                        this.status = -1;
                        throw new OSQLException("OSCAR-00109", "08003", 109);
                    }
                    this.status = 1;
                    this.getMessage(this.oStream.getInputStream(), this.bk);
                } while (!(this.bk instanceof ReadyForQueryPacket));
                if (exception != null) {
                    throw exception;
                }
            }
            catch (SocketTimeoutException e) {
                this.status = -1;
                throw new OSQLException("OSCAR-00901", "08003", 901, e);
            }
            catch (IOException e) {
                this.status = -1;
                throw new OSQLException("OSCAR-00109", "08003", 109, e);
            }
            return this.resultset;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void importBlogBegin(QueryPacket qp) throws SQLException {
        OStream oStream = this.oStream;
        synchronized (oStream) {
            try {
                OSQLException exception = null;
                this.sendMessage(this.oStream.getBufferedOutputStream(), qp);
                Object readyBk = null;
                boolean encodingFlag = this.connection.getEncoding() == null;
                do {
                    this.bk = this.getMessage(this.oStream.getInputStream());
                    if (this.bk == null) {
                        throw new OSQLException("The received information is abnormal, the network may be disconnected", "08003");
                    }
                    if (this.bk instanceof CompleteResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    if (this.bk instanceof NewImportPacket || this.bk instanceof ReadyForQueryPacket) {
                        this.status = 1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    if (!(this.bk instanceof ErrorResponsePacket)) continue;
                    this.status = -1;
                    this.getMessage(this.oStream.getInputStream(), this.bk);
                    ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                    exception = encodingFlag ? new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())) : new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                } while (!(this.bk instanceof ReadyForQueryPacket) && !(this.bk instanceof NewImportPacket));
                if (exception != null) {
                    throw exception;
                }
            }
            catch (IOException e) {
                if (this.isSocketConnectionError(e)) {
                    this.status = -1;
                    throw new OSQLException("OSCAR-00901", "08003", 901, e);
                }
                throw new OSQLException("OSCAR-00120", "88888", 120, e);
            }
        }
    }

    public int importBinlogData(byte[] b, int pos, int len) throws SQLException {
        OStream oStream = this.oStream;
        synchronized (oStream) {
            int retVal = -1;
            try {
                BlogDataPacket bp = new BlogDataPacket(b, pos, len);
                this.sendMessage(this.oStream.getBufferedOutputStream(), bp);
                int n = retVal;
                return n;
            }
            catch (IOException e) {
                if (this.isSocketConnectionError(e)) {
                    this.status = -1;
                    throw new OSQLException("OSCAR-00901", "08003", 901, e);
                }
                throw new OSQLException("OSCAR-00120", "88888", 120, e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void importBinlogEnd() throws SQLException {
        OStream oStream = this.oStream;
        synchronized (oStream) {
            try {
                BasePacket.SendChar(this.oStream.getBufferedOutputStream(), 67);
                this.oStream.getBufferedOutputStream().flush();
                OSQLException exception = null;
                boolean encodingFlag = this.connection.getEncoding() == null;
                do {
                    this.bk = this.getMessage(this.oStream.getInputStream());
                    if (this.bk == null) {
                        throw new OSQLException("The received information is abnormal, the network may be disconnected", "08003");
                    }
                    if (this.bk instanceof NoticeResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        if (encodingFlag) {
                            this.connection.addWarning(this.connection.getClientEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), this.connection.getClientEncoding().decode(((NoticeResponsePacket)this.bk).getSQLState()));
                            continue;
                        }
                        this.connection.addWarning(this.connection.getEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), this.connection.getEncoding().decode(((NoticeResponsePacket)this.bk).getSQLState()));
                        continue;
                    }
                    if (this.bk instanceof CompleteResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.cmd = ((CompleteResponsePacket)this.bk).getCommand();
                        continue;
                    }
                    if (this.bk instanceof ErrorResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    if (this.bk instanceof BLogErrorResponsePacket) {
                        this.status = -1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        BLogErrorResponsePacket errorPacket = (BLogErrorResponsePacket)this.bk;
                        if (exception == null) {
                            exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                            continue;
                        }
                        exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                        continue;
                    }
                    if (!(this.bk instanceof ReadyForQueryPacket)) continue;
                    this.status = 1;
                    this.getMessage(this.oStream.getInputStream(), this.bk);
                } while (!(this.bk instanceof ReadyForQueryPacket));
                if (exception != null) {
                    throw exception;
                }
            }
            catch (IOException e) {
                if (this.isSocketConnectionError(e)) {
                    this.status = -1;
                    throw new OSQLException("OSCAR-00901", "08003", 901, e);
                }
                throw new OSQLException("OSCAR-00120", "88888", 120, e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public BlogResultSet queryBlogData(QueryPacket qp) throws SQLException {
        OStream oStream = this.oStream;
        synchronized (oStream) {
            BlogResultSet resultSet = null;
            OSQLException exception = null;
            boolean encodingFlag = this.connection.getEncoding() == null;
            try {
                this.sendMessage(this.oStream.getBufferedOutputStream(), qp);
                do {
                    this.bk = this.getMessage(this.oStream.getInputStream());
                    if (this.bk instanceof CompleteResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    if (this.bk instanceof BlogDataPacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        if (resultSet == null) {
                            resultSet = new BlogResultSet(this.exportBinlogHandler.getExportFile());
                            System.out.print("export file\uff1a" + this.exportBinlogHandler.getExportFile());
                        }
                        resultSet.setCurData(((BlogDataPacket)this.bk).getTuple());
                        continue;
                    }
                    if (this.bk instanceof BinlogErrorPacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        if (resultSet == null) {
                            resultSet = new BlogResultSet(this.exportBinlogHandler.getExportFile());
                        }
                        System.out.println("e\u9519\u8bef\u8bbe\u7f6e\u4e00\u4e2a\u4f4d\u7f6e\uff1a" + ((BinlogErrorPacket)this.bk).getErrorMessage() + ":::" + ((BinlogErrorPacket)this.bk).getCurpos());
                        resultSet.setCurPos(((BinlogErrorPacket)this.bk).getCurpos());
                        resultSet.setOutputFileSeek((long)((BinlogErrorPacket)this.bk).getCurpos() - this.exportBinlogHandler.getStartPos());
                        resultSet.setCurFile(((BinlogErrorPacket)this.bk).getErrorMessage());
                        continue;
                    }
                    if (this.bk instanceof ExportBinlogSuccessPacket) {
                        this.status = 1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        if (resultSet == null) {
                            resultSet = new BlogResultSet(this.exportBinlogHandler.getExportFile());
                        }
                        resultSet.setCurFile(((ExportBinlogSuccessPacket)this.bk).getCurfile());
                        resultSet.setCurPos(((ExportBinlogSuccessPacket)this.bk).getCurpos());
                        resultSet.setIsExportEnd(((ExportBinlogSuccessPacket)this.bk).getFlag() != 0);
                        continue;
                    }
                    if (this.bk instanceof ErrorResponsePacket) {
                        this.status = -1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                        if (encodingFlag) {
                            if (exception == null) {
                                exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                                continue;
                            }
                            exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                            continue;
                        }
                        if (exception == null) {
                            exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                            continue;
                        }
                        exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                        continue;
                    }
                    if (this.bk instanceof ReadyForQueryPacket) {
                        this.status = 1;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        continue;
                    }
                    this.status = -1;
                    OSQLException e = new OSQLException("OSCAR-00109", "08003", 109);
                    if (this.logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                    }
                    throw e;
                } while (!(this.bk instanceof ReadyForQueryPacket));
                if (exception != null) {
                    throw exception;
                }
            }
            catch (IOException e) {
                if (this.isSocketConnectionError(e)) {
                    this.status = -1;
                    throw new OSQLException("OSCAR-00901", "08003", 901, e);
                }
                throw new OSQLException("OSCAR-00906", "88888", 906, e);
            }
            if (resultSet != null) {
                resultSet.close();
            }
            return resultSet;
        }
    }

    protected boolean isSocketConnectionError(Throwable e) {
        SocketException se = null;
        if (e instanceof SocketException) {
            se = (SocketException)e;
        } else if (e.getCause() instanceof SocketException) {
            se = (SocketException)e.getCause();
        }
        if (se != null) {
            if (se.getMessage().startsWith("Software caused connection abort: ")) {
                return true;
            }
            if (se.getMessage().startsWith("Connection reset")) {
                return true;
            }
            if (e.getMessage().contains("Broken pipe") || e.getMessage().contains("\u65ad\u5f00\u7684\u7ba1\u9053")) {
                return true;
            }
        } else if (e.getMessage().contains("system closing")) {
            return true;
        }
        return false;
    }

    protected boolean needRetry(int getMessageTimes) {
        return getMessageTimes <= this.errorRetryTimes || this.errorRetryTimes < 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sendLsn(SetQueryLsnPacket sqlp, BaseConnection conn) throws SQLException {
        OStream oStream = this.oStream;
        synchronized (oStream) {
            try {
                sqlp.setConnection(conn);
                this.sendMessage(this.oStream.getBufferedOutputStream(), sqlp);
            }
            catch (IOException e) {
                throw new OSQLException("OSCAR-00906", "88888", 109, e);
            }
        }
    }
}

