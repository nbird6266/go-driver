/*
 * Decompiled with CFR 0.152.
 */
package com.oscar;

import com.oscar.Config;
import com.oscar.Driver;
import com.oscar.jdbc.OscarJdbc2BulkConnection;
import com.oscar.util.OSQLException;
import com.oscar.util.TrackLog;
import com.oscar.util.VersionConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.StringTokenizer;

public class BulkDriver
extends Driver
implements java.sql.Driver {
    private VersionConfig versionConfig = VersionConfig.getInstance();
    private byte[] lock = new byte[0];
    private static Config config;
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
                if (loginTimeoutValue != null) {
                    loginTimeout = (long)Float.parseFloat(loginTimeoutValue);
                }
                if (loginTimeout <= 0L) {
                    return this.makeConnection(url, props);
                }
                ConnectThread ct = new ConnectThread(url, props);
                new Thread((Runnable)ct, "OSCAR JDBC driver connection thread").start();
                return ct.getResult(loginTimeout);
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

    private synchronized Connection makeConnection(String url, Properties props) throws SQLException {
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
        OscarJdbc2BulkConnection con = new OscarJdbc2BulkConnection();
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
        DriverPropertyInfo[] props = new DriverPropertyInfo[Driver.knownProperties.length];
        for (int i = 0; i < Driver.knownProperties.length; ++i) {
            String name = (String)Driver.knownProperties[i][0];
            String value = newPro.getProperty(name);
            props[i] = new DriverPropertyInfo(name, value);
            props[i].required = (Boolean)Driver.knownProperties[i][1];
            props[i].description = (String)Driver.knownProperties[i][2];
            if (value == null && Driver.knownProperties[i].length > 3) {
                props[i].value = (String)Driver.knownProperties[i][3];
            }
            if (Driver.knownProperties[i].length <= 4) continue;
            props[i].choices = (String[])Driver.knownProperties[i][4];
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
        count = 0;
        while (st.hasMoreTokens()) {
            token = st.nextToken();
            if (count == 0) {
                tokener = new StringTokenizer(token, ":", true);
                tcount = 0;
                while (tokener.hasMoreTokens()) {
                    if (tcount > 3 || !tokener.nextToken().equals(BulkDriver.protocols[tcount])) {
                        return null;
                    }
                    ++tcount;
                }
            } else if (count == 1 || count == 2) {
                state = 1;
            } else if (count == 3 && state == 1) {
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
                if (!f.exists()) ** GOTO lbl100
                is = new FileInputStream(f);
                tmpProp = new Properties();
                tmpProp.load(is);
                if (tmpProp == null) ** GOTO lbl100
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
lbl100:
        // 5 sources

        if (configProps != null) {
            e = configProps.keys();
            while (e.hasMoreElements()) {
                obj = e.nextElement();
                if (urlProps.get(obj) != null) continue;
                urlProps.put(obj, configProps.get(obj));
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
        if (trackLog != null) {
            trackLog.writeLog(msg);
        }
    }

    static {
        try {
            config = new Config();
            config.init();
            DriverManager.registerDriver(new BulkDriver());
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        protocols = new String[]{"jdbc", ":", "oscarbulk", ":"};
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
            Throwable error;
            Connection conn;
            try {
                conn = BulkDriver.this.makeConnection(this.url, this.props);
                error = null;
            }
            catch (Throwable t) {
                conn = null;
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

