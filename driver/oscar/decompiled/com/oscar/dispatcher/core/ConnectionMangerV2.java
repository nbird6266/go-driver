/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.core;

import com.oscar.Driver;
import com.oscar.dispatcher.entity.DispatchConnection;
import com.oscar.jdbc.OscarJdbc2Connection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ConnectionMangerV2 {
    public static ConcurrentHashMap<String, ConcurrentHashMap> slaves = new ConcurrentHashMap();
    public static Connection master;
    public static String passward;
    public static String dbName;
    public static ConcurrentHashMap<String, String> rates;
    public static ConcurrentHashMap<String, AtomicInteger> hostAcount;
    public static ConcurrentHashMap<String, AtomicInteger> totalAcount;
    public static ConcurrentHashMap<String, AtomicInteger> lastSlaveID;
    public static Properties props;
    protected static boolean logFlag;

    public static DispatchConnection createConnection(OscarJdbc2Connection con, String passward, String dbName, Properties props, Map<String, Map<String, String>> slaveConnInfo) {
        if (logFlag) {
            Driver.writeLog("session: " + con.sessionID + ", " + ConnectionMangerV2.class + ", createConnection()");
        }
        props.setProperty("HOSTLOADRATE", props.getProperty("HOSTLOADRATE", "0"));
        master = con;
        ConnectionMangerV2.passward = passward;
        ConnectionMangerV2.dbName = dbName;
        ConnectionMangerV2.props = props;
        DispatchConnection conn = new DispatchConnection(con, passward, dbName, props, slaveConnInfo);
        ConnectionMangerV2.props.setProperty("USEDISPATCH", "FALSE");
        if (rates.get(conn.url) == null) {
            rates.put(conn.url, props.getProperty("HOSTLOADRATE"));
            hostAcount.put(conn.url, new AtomicInteger(0));
            totalAcount.put(conn.url, new AtomicInteger(0));
            lastSlaveID.put(conn.url, new AtomicInteger(0));
        }
        return conn;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    public static void getSlaves() {
        block17: {
            Statement st = null;
            ResultSet rs = null;
            st = master.createStatement();
            rs = st.executeQuery("select * from v_sys_ha_slave_info");
            int index = 0;
            ConcurrentHashMap<String, ArrayList<String>> slaveValue = null;
            while (rs.next()) {
                String key;
                ArrayList<String> info;
                if (!rs.getBoolean("READABLE")) continue;
                if (slaveValue == null) {
                    slaveValue = new ConcurrentHashMap<String, ArrayList<String>>();
                    slaves.put(((OscarJdbc2Connection)master).getURL(), slaveValue);
                }
                if ((info = (ArrayList<String>)slaveValue.get(key = "slave" + index)) == null) {
                    info = new ArrayList<String>();
                    slaveValue.put(key, info);
                }
                info.add(rs.getString("ADDRESS"));
                info.add(rs.getString("PORT"));
                ++index;
            }
            Object var7_7 = null;
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                break block17;
            }
            catch (SQLException e2) {
                e2.printStackTrace();
            }
            break block17;
            {
                catch (SQLException e) {
                    e.printStackTrace();
                    Object var7_8 = null;
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                        if (st != null) {
                            st.close();
                        }
                        break block17;
                    }
                    catch (SQLException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            catch (Throwable throwable) {
                Object var7_9 = null;
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

    static {
        rates = new ConcurrentHashMap();
        hostAcount = new ConcurrentHashMap();
        totalAcount = new ConcurrentHashMap();
        lastSlaveID = new ConcurrentHashMap();
        props = new Properties();
        logFlag = Driver.getLogLevel() >= 2;
    }
}

