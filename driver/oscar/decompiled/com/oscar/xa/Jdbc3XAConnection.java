/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.xa;

import com.oscar.jdbcx.Jdbc3PooledConnection;
import com.oscar.util.XAOSQLException;
import com.oscar.xa.XidUtil;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class Jdbc3XAConnection
extends Jdbc3PooledConnection
implements XAConnection,
XAResource {
    private final Connection conn;
    private Xid currentXid;
    private int state;
    private static final int STATE_IDLE = 0;
    private static final int STATE_ACTIVE = 1;
    private static final int STATE_ENDED = 2;

    public Jdbc3XAConnection(Connection con) {
        super(con, true, true);
        this.conn = con;
        this.state = 0;
    }

    public XAResource getXAResource() throws SQLException {
        return this;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
        String sql;
        try {
            sql = "XA COMMIT " + XidUtil.xitToString(xid);
        }
        catch (SQLException e) {
            throw new XAOSQLException("error start xa transaction, xid is invalidated");
        }
        if (onePhase) {
            sql = sql + " ONE PHASE";
        }
        if (!this.currentXid.equals(xid)) {
            throw new XAOSQLException("prepare must be issued using the same connection that started the transaction");
        }
        if (this.state != 2) {
            throw new XAOSQLException("commit called before end");
        }
        try {
            this.executeXA(sql);
        }
        catch (SQLException ex) {
            throw new XAOSQLException(ex.getMessage());
        }
    }

    public void end(Xid xid, int flags) throws XAException {
        String sql;
        try {
            sql = "XA END " + XidUtil.xitToString(xid);
        }
        catch (SQLException e) {
            throw new XAOSQLException("error start xa transaction, xid is invalidated");
        }
        switch (flags) {
            case 0x2000000: {
                sql = sql + " SUSPEND";
                break;
            }
        }
        if (xid == null) {
            throw new XAOSQLException("xid must not be null");
        }
        if (this.state != 1 || !this.currentXid.equals(xid)) {
            throw new XAOSQLException("tried to call end without corresponding start call");
        }
        try {
            this.executeXA(sql);
        }
        catch (SQLException e) {
            throw new XAOSQLException(e.getMessage());
        }
        this.state = 2;
    }

    public void forget(Xid xid) throws XAException {
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        return xares == this;
    }

    public int prepare(Xid xid) throws XAException {
        if (!this.currentXid.equals(xid)) {
            throw new XAOSQLException("prepare must be issued using the same connection that started the transaction");
        }
        if (this.state != 2) {
            throw new XAOSQLException("Prepare called before end");
        }
        try {
            this.executeXA("XA PREPARE " + XidUtil.xitToString(xid));
            return 0;
        }
        catch (SQLException ex) {
            throw new XAOSQLException(ex.getMessage());
        }
    }

    public Xid[] recover(int flag) throws XAException {
        if (flag != 0x1000000 && flag != 0x800000 && flag != 0 && flag != 0x1800000) {
            throw new XAOSQLException("xa transaction recover not supported");
        }
        return new Xid[0];
    }

    public void rollback(Xid xid) throws XAException {
        try {
            this.executeXA("XA ROLLBACK " + XidUtil.xitToString(xid));
        }
        catch (SQLException ex) {
            throw new XAOSQLException(ex.getMessage());
        }
    }

    public boolean setTransactionTimeout(int arg0) throws XAException {
        return false;
    }

    public void start(Xid xid, int flags) throws XAException {
        String sql;
        try {
            sql = "XA START " + XidUtil.xitToString(xid);
        }
        catch (SQLException e) {
            throw new XAOSQLException("error start xa transaction, xid is invalidated");
        }
        switch (flags) {
            case 0x8000000: {
                sql = sql + " RESUME";
                break;
            }
            case 0x200000: {
                sql = sql + " JOIN";
                break;
            }
        }
        if (xid == null) {
            throw new XAOSQLException("xid must not be null");
        }
        if (this.state == 1) {
            throw new XAOSQLException("Connection is busy with another transaction");
        }
        try {
            this.executeXA(sql);
        }
        catch (SQLException ex) {
            throw new XAOSQLException(ex.getMessage());
        }
        this.state = 1;
        this.currentXid = xid;
    }

    public void executeXA(String sql) throws SQLException {
        this.conn.createStatement().execute(sql);
    }
}

