/*
 * Decompiled with CFR 0.152.
 */
package com.oscar;

import com.oscar.Config;
import com.oscar.dispatcher.core.ConnectionMangerV2;
import com.oscar.dispatcher.entity.DispatchConnection;
import com.oscar.jdbc.OscarJdbc2BulkConnection;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.util.MessageTranslator;
import com.oscar.util.OSQLException;
import com.oscar.util.TrackLog;
import com.oscar.util.VersionConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

public class Driver
implements java.sql.Driver {
    public static TrackLog trackLog;
    public static final AtomicInteger sessionID;
    public static final boolean debug = false;
    public static final boolean err = true;
    public static final boolean out = false;
    public static final String AUTH_TYPE_PW = "PW";
    public static final String AUTH_TYPE_OS = "OS";
    public static final String AUTH_TYPE_FP = "FP";
    public static final String AUTH_TYPE_RA = "RA";
    public static final boolean JDBC3;
    private VersionConfig versionConfig = VersionConfig.getInstance();
    private byte[] lock = new byte[0];
    private static Config config;
    public static final Object[][] knownProperties;
    public static final String[] chinaTimeZone;
    private static String[] protocols;

    public Connection connect(String url, Properties info) throws SQLException {
        try {
            if (JDBC3) {
                Properties props = this.parseURL(url, info);
                if (props == null) {
                    return null;
                }
                long loginTimeout = 0L;
                String loginTimeoutValue = props.getProperty("LOGINTIMEOUT");
                loginTimeout = loginTimeoutValue != null ? (long)Float.parseFloat(loginTimeoutValue) : (long)(DriverManager.getLoginTimeout() * 1000);
                if (Boolean.valueOf(props.getProperty("USEDISPATCH", "FALSE")).booleanValue()) {
                    return this.dispatchConnection(loginTimeout, url, props);
                }
                return this.commonConnection(loginTimeout, url, props);
            }
            throw new SQLException("not support jdbc2.0");
        }
        catch (SQLException e) {
            throw e;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new SQLException("oscar.unusual");
        }
    }

    private Connection commonConnection(long loginTimeout, String url, Properties props) throws SQLException {
        if (loginTimeout <= 0L) {
            return this.makeConnection(url, props);
        }
        ConnectThread ct = new ConnectThread(url, props);
        new Thread((Runnable)ct, "OSCAR JDBC driver connection thread").start();
        return ct.getResult(loginTimeout);
    }

    private Connection dispatchConnection(long loginTimeout, String url, Properties props) throws SQLException {
        boolean hasNext = true;
        Object hostsObject = props.get("DBHOSTS");
        String[] hosts = hostsObject != null ? (String[])hostsObject : null;
        String hostsIndex = props.getProperty("DBHOSTS_INDEX");
        int dbHosts_index = hostsIndex == null ? -1 : Integer.valueOf(hostsIndex);
        Connection firstConnection = null;
        do {
            try {
                Connection conn = this.commonConnection(loginTimeout, url, props);
                if (hosts == null || ((OscarJdbc2Connection)conn).isMaster()) {
                    String password = props.getProperty("PASSWORD") == null ? "" : props.getProperty("PASSWORD");
                    String database = props.getProperty("DBNAME", "");
                    conn = ConnectionMangerV2.createConnection((OscarJdbc2Connection)conn, password, database, props, ((OscarJdbc2Connection)conn).initSlavesInfo(props));
                }
                if (hosts != null) {
                    if (conn instanceof DispatchConnection) {
                        hasNext = false;
                        if (firstConnection != null) {
                            try {
                                firstConnection.close();
                            }
                            catch (SQLException e) {
                                // empty catch block
                            }
                        }
                        return conn;
                    }
                    if (firstConnection == null) {
                        firstConnection = conn;
                    } else {
                        try {
                            conn.close();
                        }
                        catch (SQLException e) {
                            // empty catch block
                        }
                    }
                    if (++dbHosts_index >= hosts.length) {
                        return firstConnection;
                    }
                } else {
                    return conn;
                }
                this.setNextHost(hosts, dbHosts_index, props);
            }
            catch (SQLException e) {
                if (hostsIndex != null) {
                    if (++dbHosts_index >= hosts.length) {
                        if (firstConnection != null) {
                            return firstConnection;
                        }
                        throw e;
                    }
                    this.setNextHost(hosts, dbHosts_index, props);
                    continue;
                }
                throw e;
            }
        } while (hasNext);
        return null;
    }

    public void setNextHost(String[] hosts, int dbHosts_index, Properties props) {
        String hostAndPort = hosts[dbHosts_index];
        StringTokenizer tokener = new StringTokenizer(hostAndPort, ":", true);
        if (tokener.countTokens() == 1) {
            props.put("DBHOST", hostAndPort);
        } else {
            int index = hostAndPort.lastIndexOf(":");
            String host = hostAndPort.substring(0, index);
            String port = hostAndPort.substring(index + 1, hostAndPort.length());
            props.put("DBHOST", host);
            props.put("DBPORT", Integer.decode(port).toString());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Connection makeConnection(String url, Properties props) throws SQLException {
        Driver driver = this;
        synchronized (driver) {
            Object logLevel = props.get("LOGLEVEL");
            if (trackLog == null && logLevel != null) {
                int iLogLevel = Integer.parseInt(logLevel.toString());
                trackLog = TrackLog.getInstance();
                trackLog.setLogLevel(iLogLevel);
                Object logFilePath = props.get("LOGFILEPATH");
                if (logFilePath != null && !"".equals(logFilePath)) {
                    trackLog.initLogPath(logFilePath.toString());
                } else {
                    trackLog.initLogPath(Config.defultLogPath);
                }
                Object maxFileSize = props.get("MAXFILESIZE");
                if (maxFileSize != null) {
                    trackLog.setMaxFileSize(Integer.parseInt(maxFileSize.toString()));
                }
            }
        }
        boolean useBulkInsertBatch = Boolean.valueOf(props.getProperty("USEBULKINSERTBATCH", "FALSE"));
        OscarJdbc2Connection con = null;
        con = useBulkInsertBatch ? new OscarJdbc2BulkConnection() : new OscarJdbc2Connection();
        con.sessionID = sessionID.getAndIncrement();
        String host = props.getProperty("DBHOST", "localhost");
        int port = Integer.parseInt(props.getProperty("DBPORT", "2003"));
        String database = props.getProperty("DBNAME", "");
        con.openConnection(host, port, props, database, url, this);
        if (trackLog != null && trackLog.getLogLevel() > 0) {
            trackLog.writeLog("session " + con.getSessionID() + " JDBC \u8fde\u63a5\u4fe1\u606f\uff1a" + props.toString());
        }
        return con;
    }

    public boolean acceptsURL(String url) throws SQLException {
        return this.parseURL(url, null) != null;
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        Properties newPro = this.parseURL(url, info);
        DriverPropertyInfo[] props = new DriverPropertyInfo[knownProperties.length];
        for (int i = 0; i < knownProperties.length; ++i) {
            String name = (String)knownProperties[i][0];
            String value = newPro.getProperty(name);
            props[i] = new DriverPropertyInfo(name, value);
            props[i].required = (Boolean)knownProperties[i][1];
            props[i].description = (String)knownProperties[i][2];
            if (value == null && knownProperties[i].length > 3) {
                props[i].value = (String)knownProperties[i][3];
            }
            if (knownProperties[i].length <= 4) continue;
            props[i].choices = (String[])knownProperties[i][4];
        }
        return props;
    }

    public int getMajorVersion() {
        return this.versionConfig.getDriverMajorVersion();
    }

    public int getMinorVersion() {
        return this.versionConfig.getDriverMinorVersion();
    }

    public static String getVersion() {
        VersionConfig vc = VersionConfig.getInstance();
        return vc.getDriverMajorVersion() + "." + vc.getDriverMinorVersion();
    }

    public boolean jdbcCompliant() {
        return true;
    }

    /*
     * Unable to fully structure code
     */
    Properties parseURL(String url, Properties defaults) throws SQLException {
        urlProps = new Properties();
        state = -1;
        if (defaults != null) {
            o = null;
            destKey = null;
            e = defaults.keys();
            while (e.hasMoreElements()) {
                o = e.nextElement();
                destKey = ((String)o).toUpperCase();
                urlProps.put(destKey, defaults.get(o));
            }
        }
        l_urlServer = url;
        l_urlArgs = "";
        l_qPos = url.indexOf(63);
        if (l_qPos == -1) {
            l_qPos = url.indexOf(59);
        }
        if (l_qPos != -1) {
            l_urlServer = url.substring(0, l_qPos);
            l_urlArgs = url.substring(l_qPos + 1);
        }
        st = new StringTokenizer(l_urlServer, "/", true);
        tokener = null;
        token = null;
        hosts = null;
        count = 0;
        while (st.hasMoreTokens()) {
            token = st.nextToken();
            if (count == 0) {
                tokener = new StringTokenizer(token, ":", true);
                tcount = 0;
                while (tokener.hasMoreTokens()) {
                    if (tcount > 3 || !tokener.nextToken().equals(Driver.protocols[tcount])) {
                        return null;
                    }
                    ++tcount;
                }
            } else if (count == 1 || count == 2) {
                state = 1;
            } else if (count == 3 && state == 1) {
                if (token.indexOf(",") != -1) {
                    hosts = token.split(",");
                } else {
                    tokener = new StringTokenizer(token, ":", true);
                    if (tokener.countTokens() == 1) {
                        urlProps.put("DBHOST", token);
                    } else {
                        index = token.lastIndexOf(":");
                        host = token.substring(0, index);
                        port = token.substring(index + 1, token.length());
                        state = tokener.countTokens() > 1 ? 2 : 3;
                        urlProps.put("DBHOST", host);
                        urlProps.put("DBPORT", Integer.decode(port).toString());
                    }
                }
            } else if (count == 4) {
                state = 4;
            } else if (count == 5 && state == 4) {
                urlProps.put("DBNAME", token);
                state = -2;
            }
            ++count;
        }
        qst = new StringTokenizer(l_urlArgs, "&");
        count = 0;
        while (qst.hasMoreTokens()) {
            gtoken = qst.nextToken().trim();
            l_pos = gtoken.indexOf(61);
            if (l_pos == -1) {
                urlProps.put(gtoken.toUpperCase(), "");
            } else {
                urlProps.put(gtoken.substring(0, l_pos).toUpperCase(), gtoken.substring(l_pos + 1));
            }
            ++count;
        }
        configPath = urlProps.get("CONFIGFILE");
        configProps = null;
        if (configPath != null) {
            configProps = new Properties();
            try {
                f = new File(configPath.toString());
                if (!f.exists()) ** GOTO lbl104
                is = new FileInputStream(f);
                tmpProp = new Properties();
                tmpProp.load(is);
                if (tmpProp == null) ** GOTO lbl104
                o = null;
                destKey = null;
                e = tmpProp.keys();
                while (e.hasMoreElements()) {
                    o = e.nextElement();
                    destKey = ((String)o).toUpperCase();
                    configProps.put(destKey, tmpProp.get(o));
                }
            }
            catch (IOException ex) {}
        } else {
            configProps = Config.configProp;
        }
lbl104:
        // 5 sources

        if (configProps != null) {
            e = configProps.keys();
            while (e.hasMoreElements()) {
                obj = e.nextElement();
                if (urlProps.get(obj) != null) continue;
                urlProps.put(obj, configProps.get(obj));
            }
        }
        if (hosts != null) {
            urlProps.put("DBHOSTS", hosts);
            hostAndPort = hosts[0];
            urlProps.setProperty("DBHOSTS_INDEX", "0");
            tokener = new StringTokenizer(hostAndPort, ":", true);
            if (tokener.countTokens() == 1) {
                urlProps.put("DBHOST", hostAndPort);
            } else {
                index = hostAndPort.lastIndexOf(":");
                host = hostAndPort.substring(0, index);
                port = hostAndPort.substring(index + 1, hostAndPort.length());
                urlProps.put("DBHOST", host);
                urlProps.put("DBPORT", Integer.decode(port).toString());
            }
        }
        return urlProps;
    }

    public static SQLException notImplemented() throws SQLException {
        return new SQLException("oscar.unimplemented");
    }

    public static boolean sslEnabled() {
        return true;
    }

    public static int getLogLevel() {
        if (trackLog == null) {
            return 0;
        }
        return trackLog.getLogLevel();
    }

    public static void writeLog(String msg) {
        if (trackLog != null && (trackLog.getLogLevel() > 0 || trackLog.getLogLevel() == -1)) {
            trackLog.writeLog(msg);
        }
    }

    public static void writeLog(String prefix, Throwable throwable) {
        if (trackLog != null && (trackLog.getLogLevel() > 0 || trackLog.getLogLevel() == -1)) {
            String msg = Driver.getStackTrace(throwable);
            msg = prefix == null ? msg : prefix + " " + msg;
            trackLog.writeLog(msg);
        }
    }

    public static void writeLog(Throwable throwable) {
        Driver.writeLog(null, throwable);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(sw, true);
            throwable.printStackTrace(pw);
            pw.flush();
        }
        finally {
            pw.close();
        }
        return sw.getBuffer().toString();
    }

    static {
        sessionID = new AtomicInteger(0);
        JDBC3 = "1.4".compareTo(System.getProperty("java.specification.version")) <= 0;
        knownProperties = new Object[][]{{"DBNAME", Boolean.TRUE, MessageTranslator.translate("DRIVER_DBNAME")}, {"DBHOST", Boolean.TRUE, MessageTranslator.translate("DRIVER_DBHOST")}, {"DBPORT", Boolean.TRUE, "DRIVER_DBPORT"}, {"LOGLEVEL", Boolean.FALSE, MessageTranslator.translate("DRIVER_LOGLEVEL"), "0", new String[]{"0", "1", "2", "3", "4"}}, {"LOGFILEPATH", Boolean.FALSE, MessageTranslator.translate("DRIVER_LOGFILEPATH")}, {"LOGINTIMEOUT", Boolean.FALSE, MessageTranslator.translate("DRIVER_LOGINTIMEOUT"), "0"}, {"REQUESTTIMEOUT", Boolean.FALSE, MessageTranslator.translate("DRIVER_REQUESTTIMEOUT"), "0"}, {"NOTREALPREPARE", Boolean.FALSE, MessageTranslator.translate("DRIVER_NOTREALPREPARE"), "false", new String[]{"true", "false"}}, {"COMPATIBLEOLDPROTOCOL", Boolean.FALSE, MessageTranslator.translate("DRIVER_COMPATIBLEOLDPROTOCOL"), "false", new String[]{"true", "false"}}, {"FETCHSIZE", Boolean.FALSE, MessageTranslator.translate("DRIVER_FETCHSIZE"), "16"}, {"PREPARESIMPLEEXECUTE", Boolean.FALSE, MessageTranslator.translate("DRIVER_PREPARESIMPLEEXECUTE"), "true", new String[]{"true", "false"}}, {"VERIFYPOOLCONNECTIN", Boolean.FALSE, MessageTranslator.translate("DRIVER_VERIFYPOOLCONNECTIN"), "false", new String[]{"true", "false"}}, {"USEASYNBATCH", Boolean.FALSE, MessageTranslator.translate("DRIVER_USEASYNBATCH"), "false", new String[]{"true", "false"}}, {"BATCHCOUNT", Boolean.FALSE, MessageTranslator.translate("DRIVER_BATCHCOUNT"), "0"}, {"TCPKEEPALIVE", Boolean.FALSE, MessageTranslator.translate("DRIVER_TCPKEEPALIVE"), "false", new String[]{"true", "false"}}, {"USEDISPATCH", Boolean.FALSE, MessageTranslator.translate("DRIVER_USEDISPATCH"), "false", new String[]{"true", "false"}}, {"USEASYNCHRONOUS", Boolean.FALSE, MessageTranslator.translate("DRIVER_USEASYNCHRONOUS"), "true", new String[]{"true", "false"}}, {"USESLAVESYNCREAD", Boolean.FALSE, MessageTranslator.translate("DRIVER_USESLAVESYNCREAD"), "false", new String[]{"true", "false"}}, {"TRANSACTIONDISPATCHSTRATEGY", Boolean.FALSE, MessageTranslator.translate("DRIVER_TRANSACTIONDISPATCHSTRATEGY"), "1", new String[]{"1", "2", "3"}}, {"VALIDTESTSTRING", Boolean.FALSE, MessageTranslator.translate("DRIVER_VALIDTESTSTRING"), "select 1", new String[]{"select 'mpp nodeagent probeing'", "select 'user direct testing'", "select 'mpp master probeing'"}}, {"CHECKDBLINKSQL", Boolean.FALSE, MessageTranslator.translate("DRIVER_CHECKDBLINKSQL"), "false", new String[]{"true", "false"}}, {"SENDBINARYTYPEASHEX", Boolean.FALSE, MessageTranslator.translate("DRIVER_SENDBINARYTYPEASHEX"), "false", new String[]{"true", "false"}}};
        chinaTimeZone = new String[]{"Asia/Chongqing", "Asia/Chungking", "Asia/Harbin", "Asia/Shanghai", "CTT", "PRC"};
        try {
            if (Arrays.asList(chinaTimeZone).contains(TimeZone.getDefault().getID())) {
                TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
            }
            config = new Config();
            config.init();
            DriverManager.registerDriver(new Driver());
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        protocols = new String[]{"jdbc", ":", "oscar", ":"};
    }

    private class ConnectThread
    implements Runnable {
        private String url;
        private Properties props;
        private Connection result;
        private Throwable resultException;
        private boolean abandoned = false;

        ConnectThread(String url, Properties props) {
            this.url = url;
            this.props = props;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            Connection conn = null;
            Throwable error = null;
            try {
                conn = Driver.this.makeConnection(this.url, this.props);
                error = null;
            }
            catch (Throwable t) {
                error = t;
            }
            ConnectThread connectThread = this;
            synchronized (connectThread) {
                if (this.abandoned) {
                    if (conn != null) {
                        try {
                            conn.close();
                        }
                        catch (SQLException e) {}
                    }
                } else {
                    this.result = conn;
                    this.resultException = error;
                    this.notify();
                }
            }
        }

        /*
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public Connection getResult(long timeout) throws SQLException {
            long expiry = System.currentTimeMillis() + timeout;
            ConnectThread connectThread = this;
            synchronized (connectThread) {
                while (this.result == null) {
                    if (this.resultException != null) {
                        if (this.resultException instanceof SQLException) {
                            this.resultException.fillInStackTrace();
                            throw (SQLException)this.resultException;
                        }
                        throw new OSQLException("OSCAR-00121", "08001", 121);
                    }
                    long delay = expiry - System.currentTimeMillis();
                    if (delay <= 0L) {
                        this.abandoned = true;
                        throw new OSQLException("OSCAR-00122", "08001", 122);
                    }
                    try {
                        this.wait(delay);
                    }
                    catch (InterruptedException ie) {
                        this.abandoned = true;
                        throw new OSQLException("OSCAR-00123", "08001", 123);
                    }
                }
                return this.result;
            }
        }
    }
}

