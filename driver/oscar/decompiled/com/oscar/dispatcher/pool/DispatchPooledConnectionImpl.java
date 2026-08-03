/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.pool;

import com.oscar.core.BaseConnection;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;

public class DispatchPooledConnectionImpl
implements PooledConnection {
    private List listeners = new LinkedList();
    private Connection con;
    private ConnectionHandler last;
    private boolean autoCommit;
    private boolean isXA;
    private boolean closed = false;
    private int sessionID;

    protected DispatchPooledConnectionImpl(Connection con, boolean autoCommit) throws SQLException {
        this.con = con;
        this.autoCommit = autoCommit;
        this.sessionID = ((BaseConnection)((Object)con)).getPlanID();
    }

    protected DispatchPooledConnectionImpl(Connection con, boolean autoCommit, boolean isXA) throws SQLException {
        this.con = con;
        this.autoCommit = autoCommit;
        this.isXA = isXA;
        this.sessionID = ((BaseConnection)((Object)con)).getPlanID();
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public int getSessionID() {
        return this.sessionID;
    }

    public void addConnectionEventListener(ConnectionEventListener connectionEventListener) {
        this.listeners.add(connectionEventListener);
    }

    public void removeConnectionEventListener(ConnectionEventListener connectionEventListener) {
        this.listeners.remove(connectionEventListener);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() throws SQLException {
        if (this.last != null) {
            this.last.close();
            if (!this.con.getAutoCommit()) {
                try {
                    this.con.rollback();
                }
                catch (SQLException sQLException) {
                    // empty catch block
                }
            }
        }
        try {
            Object var3_2 = null;
        }
        catch (Throwable throwable) {
            Object var3_3 = null;
            throw throwable;
        }
        this.closed = true;
    }

    public Connection getConnection() throws SQLException {
        ConnectionHandler handler;
        if (this.con == null) {
            SQLException sqlException = new SQLException("This PooledConnection has already been closed!");
            this.fireConnectionFatalError(sqlException);
            throw sqlException;
        }
        if (((BaseConnection)((Object)this.con)).isVerifyPoolConnection() && !((BaseConnection)((Object)this.con)).isValidate(100)) {
            return null;
        }
        try {
            if (this.last != null) {
                if (!this.con.getAutoCommit()) {
                    try {
                        this.con.rollback();
                    }
                    catch (SQLException e) {
                        // empty catch block
                    }
                }
                this.con.clearWarnings();
            }
            if (!this.isXA) {
                this.con.setAutoCommit(this.autoCommit);
            }
        }
        catch (SQLException sqlException) {
            this.fireConnectionFatalError(sqlException);
            throw (SQLException)sqlException.fillInStackTrace();
        }
        this.last = handler = new ConnectionHandler(this.con);
        Connection con = (Connection)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Connection.class, BaseConnection.class}, handler);
        this.last.setProxy(con);
        return con;
    }

    void fireConnectionClosed() {
        ConnectionEvent evt = null;
        ConnectionEventListener[] local = this.listeners.toArray(new ConnectionEventListener[this.listeners.size()]);
        for (int i = 0; i < local.length; ++i) {
            ConnectionEventListener listener = local[i];
            if (evt == null) {
                evt = new ConnectionEvent(this);
            }
            listener.connectionClosed(evt);
        }
    }

    void fireConnectionFatalError(SQLException e) {
        ConnectionEvent evt = null;
        ConnectionEventListener[] local = this.listeners.toArray(new ConnectionEventListener[this.listeners.size()]);
        for (int i = 0; i < local.length; ++i) {
            ConnectionEventListener listener = local[i];
            if (evt == null) {
                evt = new ConnectionEvent(this, e);
            }
            listener.connectionErrorOccurred(evt);
        }
    }

    private static class StatementHandler
    implements InvocationHandler {
        private ConnectionHandler con;
        private Statement st;

        public StatementHandler(ConnectionHandler con, Statement st) {
            this.con = con;
            this.st = st;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass().getName().equals("java.lang.Object")) {
                if (method.getName().equals("toString")) {
                    return "Pooled statement wrapping physical statement " + this.st;
                }
                if (method.getName().equals("hashCode")) {
                    return new Integer(this.st.hashCode());
                }
                if (method.getName().equals("equals")) {
                    if (args[0] == null) {
                        return Boolean.FALSE;
                    }
                    try {
                        return Proxy.isProxyClass(args[0].getClass()) && ((StatementHandler)Proxy.getInvocationHandler((Object)args[0])).st == this.st ? Boolean.TRUE : Boolean.FALSE;
                    }
                    catch (ClassCastException e) {
                        return Boolean.FALSE;
                    }
                }
                return method.invoke(this.st, args);
            }
            if (this.st == null || this.con.isClosed()) {
                throw new SQLException("Statement has been closed");
            }
            if (method.getName().equals("close")) {
                try {
                    this.st.close();
                    Object var6_6 = null;
                    this.con = null;
                    this.st = null;
                    return null;
                }
                catch (Throwable throwable) {
                    Object var6_7 = null;
                    this.con = null;
                    this.st = null;
                    return null;
                }
            }
            if (method.getName().equals("getConnection")) {
                return this.con.getProxy();
            }
            try {
                return method.invoke(this.st, args);
            }
            catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

    private class ConnectionHandler
    implements InvocationHandler {
        private Connection con;
        private Connection proxy;
        private boolean automatic = false;

        public ConnectionHandler(Connection con) {
            this.con = con;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass().getName().equals("java.lang.Object")) {
                if (method.getName().equals("toString")) {
                    return "Pooled connection wrapping physical connection " + this.con;
                }
                if (method.getName().equals("hashCode")) {
                    return new Integer(this.con.hashCode());
                }
                if (method.getName().equals("equals")) {
                    if (args[0] == null) {
                        return Boolean.FALSE;
                    }
                    try {
                        return Proxy.isProxyClass(args[0].getClass()) && ((ConnectionHandler)Proxy.getInvocationHandler((Object)args[0])).con == this.con ? Boolean.TRUE : Boolean.FALSE;
                    }
                    catch (ClassCastException e) {
                        return Boolean.FALSE;
                    }
                }
                try {
                    return method.invoke(this.con, args);
                }
                catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
            if (method.getName().equals("isClosed")) {
                return this.con == null ? Boolean.TRUE : Boolean.FALSE;
            }
            if (this.con == null) {
                throw new SQLException(this.automatic ? "Connection has been closed automatically because a new connection was opened for the same PooledConnection or the PooledConnection has been closed" : "Connection has been closed");
            }
            if (method.getName().equals("close")) {
                SQLException ex = null;
                if (!DispatchPooledConnectionImpl.this.isXA && !this.con.getAutoCommit()) {
                    try {
                        this.con.rollback();
                    }
                    catch (SQLException e) {
                        ex = e;
                    }
                }
                this.con.clearWarnings();
                this.con = null;
                proxy = null;
                DispatchPooledConnectionImpl.this.last = null;
                DispatchPooledConnectionImpl.this.fireConnectionClosed();
                if (ex != null) {
                    throw ex;
                }
                return null;
            }
            if (method.getName().equals("createStatement")) {
                Statement st = (Statement)method.invoke(this.con, args);
                return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Statement.class}, new StatementHandler(this, st));
            }
            if (method.getName().equals("prepareCall")) {
                Statement st = (Statement)method.invoke(this.con, args);
                return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{CallableStatement.class}, new StatementHandler(this, st));
            }
            if (method.getName().equals("prepareStatement")) {
                Statement st = (Statement)method.invoke(this.con, args);
                return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{PreparedStatement.class}, new StatementHandler(this, st));
            }
            try {
                return method.invoke(this.con, args);
            }
            catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        Connection getProxy() {
            return this.proxy;
        }

        void setProxy(Connection proxy) {
            this.proxy = proxy;
        }

        public void close() {
            try {
                SQLException ex = null;
                if (!DispatchPooledConnectionImpl.this.isXA && !this.con.getAutoCommit()) {
                    try {
                        this.con.rollback();
                    }
                    catch (SQLException e) {
                        ex = e;
                    }
                }
                this.con.clearWarnings();
                this.con = null;
                this.proxy = null;
                DispatchPooledConnectionImpl.this.last = null;
                DispatchPooledConnectionImpl.this.fireConnectionClosed();
                if (ex != null) {
                    throw ex;
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public boolean isClosed() {
            return this.con == null;
        }
    }
}

