/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.protocol.OSCARProtocol;
import com.oscar.protocol.OSCARProtocolV2;
import com.oscar.protocol.Packet;
import com.oscar.protocol.ProtocolVersion;
import com.oscar.protocol.VeifyJDBC;
import com.oscar.protocol.packets.AuthenticationPacket;
import com.oscar.protocol.packets.AuthenticationPacketV2;
import com.oscar.protocol.packets.AuthenticationPacketV3;
import com.oscar.protocol.packets.BackendKeyPacket;
import com.oscar.protocol.packets.BasePacket;
import com.oscar.protocol.packets.EndianTypePacket;
import com.oscar.protocol.packets.ErrorResponsePacket;
import com.oscar.protocol.packets.HdEncryptRequestPacket;
import com.oscar.protocol.packets.ListenerResponsePacket;
import com.oscar.protocol.packets.MessagePacket;
import com.oscar.protocol.packets.NoticeResponsePacket;
import com.oscar.protocol.packets.ReadyForQueryPacket;
import com.oscar.protocol.packets.SSLRequestPacket;
import com.oscar.protocol.packets.StartupPacket;
import com.oscar.protocol.packets.TerminatePacket;
import com.oscar.protocol.packets.UnencryptedPasswordPacket;
import com.oscar.protocol.stream.OSocket;
import com.oscar.protocol.stream.OStream;
import com.oscar.util.EncodeUtil;
import com.oscar.util.MD5Digest;
import com.oscar.util.OSQLException;
import com.oscar.util.VersionConfig;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

public class ProtocolManager {
    private BaseConnection connection;
    private String host;
    private int port;
    private String database;
    private String db_user;
    private String db_passwd;
    private int authPolicy;
    private Properties info;
    private static final int ProtocolMajorVersion = 2;
    private static final int ProtocolMinorVersion = 0;
    public static final int PROTOCOL_OK = 1;
    public static final int PROTOCOL_WAIT = 0;
    public static final int PROTOCOL_FAIL = -1;
    private int status = 0;
    private static final int AUTH_UNKNOW = -1;
    private static final int AUTH_REQ_OK = 0;
    private static final int AUTH_REQ_PASSWORD = 3;
    private static final int AUTH_REQ_MD5 = 5;
    private static final int AUTH_REQ_SCM = 6;
    private boolean isSSL = false;
    private int listenerVersion = 0;
    private int pid;
    private int ckey;
    private BasePacket bk = null;
    private Packet pk = new Packet();
    public OStream oStream;
    private boolean logFlag = Driver.getLogLevel() >= 3;
    private int requestTimeOut = 0;

    public ProtocolManager(BaseConnection con, String _host, int _port, String _database, String user, Properties _info) throws ConnectException, IOException {
        this(con, _host, _port, _database, user, _info, 0);
    }

    public ProtocolManager(BaseConnection con, String _host, int _port, String _database, String user, Properties _info, int requestTimeOut) throws ConnectException, IOException {
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
        this.requestTimeOut = requestTimeOut;
        this.oStream = new OSocket(_host, _port, requestTimeOut, con, _info);
        this.oStream.open();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void startup(boolean useSSL, boolean compatibleOldProtocal) throws SQLException {
        OStream oStream = this.oStream;
        synchronized (oStream) {
            this.status = 0;
            try {
                ProtocolVersion config = new ProtocolVersion();
                config.setVersion(VersionConfig.getInstance());
                this.connection.setVersion(config);
                StartupPacket start = new StartupPacket(2, 0, this.database, this.db_user, this.info, this.connection.getClientEncoding(), this.connection.getVersion());
                start.setConnection(this.connection);
                start.setCompatibleOldProtocal(compatibleOldProtocal);
                if (this.connection.getVersion().isWuziVersion()) {
                    this.oStream.readJDCBVerifyKey(this.info.getProperty("keyfileForVerifyJDBC", "jdbc.key"), this.info.getProperty("keypassForVerifyJDBC", "szoscar55"));
                }
                if (this.info.getProperty("NOSSLVERSION", "t").equals("f")) {
                    BasePacket srp = null;
                    byte[] respondType = new byte[1];
                    boolean hdEncrypt = this.info.getProperty("HDENCRYPT", "f").equals("t");
                    srp = hdEncrypt ? new HdEncryptRequestPacket(this.connection, useSSL) : new SSLRequestPacket(useSSL);
                    srp.setConnection(this.connection);
                    this.sendMessage(this.oStream.getBufferedOutputStream(), srp);
                    this.oStream.getInputStream().read(respondType);
                    switch (respondType[0]) {
                        case 2: {
                            this.oStream.wrapEncryptStream();
                            break;
                        }
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
                    UnencryptedPasswordPacket uppmd5;
                    byte[] digest;
                    UnencryptedPasswordPacket upp;
                    byte[] byteUser;
                    this.bk = this.getMessage(this.oStream.getInputStream());
                    byte[] md5Salt = null;
                    if (this.bk instanceof EndianTypePacket) {
                        EndianTypePacket etPacket = (EndianTypePacket)this.bk;
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.connection.setEndianType(etPacket.getEndianType());
                        continue;
                    }
                    if (this.bk instanceof ListenerResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.listenerVersion = ((ListenerResponsePacket)this.bk).getListenerVersion();
                        int dbPort = ((ListenerResponsePacket)this.bk).getDbPort();
                        TerminatePacket tp = new TerminatePacket();
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
                        this.startup(useSSL, compatibleOldProtocal);
                        continue;
                    }
                    if (this.bk instanceof AuthenticationPacket) {
                        config.setProtocolType(1);
                        byteUser = this.connection.getClientEncoding().encode(this.db_user);
                        byte[] bytePassword = this.connection.getClientEncoding().encode(this.db_passwd);
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
                                upp = new UnencryptedPasswordPacket(bytePassword);
                                upp.setConnection(this.connection);
                                this.sendMessage(this.oStream.getBufferedOutputStream(), upp);
                                break;
                            }
                            case 5: {
                                digest = MD5Digest.encode(byteUser, bytePassword, md5Salt);
                                uppmd5 = new UnencryptedPasswordPacket(digest);
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
                    if (this.bk instanceof AuthenticationPacketV2) {
                        config.setProtocolType(2);
                        byteUser = this.connection.getClientEncoding().encode(this.db_user);
                        byte[] bytePassword = this.connection.getClientEncoding().encode(this.db_passwd);
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.authPolicy = ((AuthenticationPacketV2)this.bk).getAuthenPolicy();
                        if (this.authPolicy == 5) {
                            md5Salt = (byte[])((AuthenticationPacketV2)this.bk).getSalt();
                        }
                        switch (this.authPolicy) {
                            case 0: {
                                break;
                            }
                            case 3: {
                                upp = new UnencryptedPasswordPacket(bytePassword);
                                upp.setConnection(this.connection);
                                this.sendMessage(this.oStream.getBufferedOutputStream(), upp);
                                break;
                            }
                            case 5: {
                                digest = MD5Digest.encode(byteUser, bytePassword, md5Salt);
                                uppmd5 = new UnencryptedPasswordPacket(digest);
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
                        throw new OSQLException(errorPacket.getErrorCode(), EncodeUtil.tryDecode(errorPacket.getSQLState()), EncodeUtil.tryDecode(errorPacket.getErrorMessage()));
                    }
                    if (this.bk instanceof NoticeResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.connection.addWarning(new String(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                        System.out.println("Connect Warning: " + new String(((NoticeResponsePacket)this.bk).getNoticeMessage()));
                        continue;
                    }
                    if (this.bk instanceof AuthenticationPacketV3) {
                        byteUser = this.connection.getClientEncoding().encode(this.db_user);
                        byte[] bytePassword = this.connection.getClientEncoding().encode(this.db_passwd);
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.authPolicy = ((AuthenticationPacketV3)this.bk).getAuthenPolicy();
                        if (this.authPolicy == 5) {
                            md5Salt = (byte[])((AuthenticationPacketV3)this.bk).getSalt();
                        }
                        switch (this.authPolicy) {
                            case 0: {
                                break;
                            }
                            case 3: {
                                upp = new UnencryptedPasswordPacket(bytePassword);
                                upp.setConnection(this.connection);
                                this.sendMessage(this.oStream.getBufferedOutputStream(), upp);
                                break;
                            }
                            case 5: {
                                digest = MD5Digest.encode(byteUser, bytePassword, md5Salt);
                                uppmd5 = new UnencryptedPasswordPacket(digest);
                                uppmd5.setConnection(this.connection);
                                this.sendMessage(this.oStream.getBufferedOutputStream(), uppmd5);
                                break;
                            }
                            default: {
                                throw new OSQLException("OSCAR-00106", "08004", 106);
                            }
                        }
                        config.setProtocolType(((AuthenticationPacketV3)this.bk).getVersionNum());
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
                        throw new OSQLException(((ErrorResponsePacket)this.bk).getErrorCode(), EncodeUtil.tryDecode(((ErrorResponsePacket)this.bk).getSQLState()), EncodeUtil.tryDecode(((ErrorResponsePacket)this.bk).getErrorMessage()));
                    }
                    if (this.bk instanceof NoticeResponsePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        this.connection.addWarning(new String(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                        continue;
                    }
                    if (this.bk instanceof MessagePacket) {
                        this.getMessage(this.oStream.getInputStream(), this.bk);
                        HashMap<String, String> maps = ((MessagePacket)this.bk).getMessageMap();
                        if (maps == null || maps.get(MessagePacket.SERVER_VERSION) == null || !maps.get(MessagePacket.SERVER_VERSION).equalsIgnoreCase("stmpp")) continue;
                        config.setMpp5(true);
                        continue;
                    }
                    this.status = -1;
                    throw new OSQLException("OSCAR-00107", "08001", 107);
                } while (!(this.bk instanceof ReadyForQueryPacket));
                this.oStream.setSocketTimeOut(this.requestTimeOut);
            }
            catch (SocketTimeoutException e) {
                this.status = -1;
                if (this.oStream != null) {
                    try {
                        this.oStream.close();
                    }
                    catch (IOException e1) {
                        // empty catch block
                    }
                }
                throw new OSQLException("OSCAR-00107", "08001", 107, e.getMessage(), e);
            }
            catch (IOException e) {
                this.status = -1;
                throw new OSQLException("OSCAR-00107", "08001", 107, e.getMessage(), e);
            }
        }
    }

    private void sendMessage(BufferedOutputStream stream, BasePacket packet) throws IOException, SQLException {
        packet.sendTo(stream);
    }

    private BasePacket getMessage(InputStream stream) throws IOException, SQLException {
        byte[] tagTemp = new byte[1];
        stream.read(tagTemp, 0, 1);
        char tag = (char)tagTemp[0];
        if (this.logFlag) {
            if ('\uffa1' == tag) {
                Driver.writeLog("session: " + this.connection.getSessionID() + ", " + ProtocolManager.class + ", getMessage(InputStream stream)), return tag: 0xA1");
            } else {
                Driver.writeLog("session: " + this.connection.getSessionID() + ", " + ProtocolManager.class + ", getMessage(InputStream stream)), return tag: " + tag);
            }
        }
        return this.pk.getInstance(tag, this.connection);
    }

    private void getMessage(InputStream stream, BasePacket packet) throws IOException, SQLException {
        packet.receiveFrom(stream);
    }

    public OSCARProtocol createProtocol(boolean compatibleOldProtocal) throws IOException {
        OSCARProtocol protocol = null;
        if (compatibleOldProtocal) {
            this.connection.getProtocolVersion().setProtocolType(1);
            protocol = new OSCARProtocol(this.connection, this.host, this.port, this.database, this.db_user, this.db_passwd, this.info, this.oStream);
        }
        if (this.connection.getProtocolVersion().getProtocolType() == 1) {
            protocol = new OSCARProtocol(this.connection, this.host, this.port, this.database, this.db_user, this.db_passwd, this.info, this.oStream);
        } else {
            this.oStream.reInitStream(true);
            protocol = new OSCARProtocolV2(this.connection, this.host, this.port, this.database, this.db_user, this.db_passwd, this.info, this.oStream);
        }
        if (protocol != null) {
            protocol.setPid(this.pid);
            protocol.setCkey(this.ckey);
        }
        return protocol;
    }
}

