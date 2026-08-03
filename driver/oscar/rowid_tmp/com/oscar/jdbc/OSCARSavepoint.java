/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseConnection;
import com.oscar.util.OSQLException;
import java.sql.SQLException;
import java.sql.Savepoint;

public class OSCARSavepoint
implements Savepoint {
    private int savepointId = 0;
    private String savepointName = null;
    private boolean released = false;
    private static int savepointIndex = 0;
    private static final String savepointPrefix = "JDBC_SP_";
    private BaseConnection connection = null;

    public OSCARSavepoint(BaseConnection conn) {
        this.savepointId = OSCARSavepoint.nextSavepointId();
        this.connection = conn;
    }

    public OSCARSavepoint(String name, BaseConnection conn) {
        this.savepointName = name;
        this.connection = conn;
    }

    public int getSavepointId() throws SQLException {
        if (this.savepointId == 0) {
            throw new OSQLException("OSCAR-00601", "88888", 601);
        }
        return this.savepointId;
    }

    public String getSavepointName() throws SQLException {
        if (this.savepointName == null) {
            throw new OSQLException("OSCAR-00602", "88888", 602);
        }
        return this.savepointName;
    }

    static final synchronized int nextSavepointId() {
        return ++savepointIndex;
    }

    final String getInternalSavepointName() {
        if (this.savepointName == null) {
            return savepointPrefix + this.savepointId;
        }
        return this.savepointName;
    }

    private void releaseSavepoint() throws SQLException {
        this.connection.execSQL("DROP SAVEPOINT " + this.getInternalSavepointName());
        this.released = true;
    }

    private boolean isReleased() {
        return this.released;
    }

    private void setSavepoint() throws SQLException {
        this.connection.execSQL("SAVEPOINT " + this.getInternalSavepointName());
    }

    private void rollbackSavepoint() throws SQLException {
        this.connection.execSQL("ROLLBACK TO SAVEPOINT " + this.getInternalSavepointName());
    }

    public static Savepoint setSavepoint(BaseConnection conn) throws SQLException {
        OSCARSavepoint sp = new OSCARSavepoint(conn);
        sp.setSavepoint();
        return sp;
    }

    public static Savepoint setSavepoint(String name, BaseConnection conn) throws SQLException {
        OSCARSavepoint sp = new OSCARSavepoint(name, conn);
        sp.setSavepoint();
        return sp;
    }

    public static void rollbackSavepoint(Savepoint savepoint) throws SQLException {
        OSCARSavepoint sp = (OSCARSavepoint)savepoint;
        if (sp.isReleased()) {
            throw new OSQLException("OSCAR-00604", "25000", 604);
        }
        sp.rollbackSavepoint();
    }

    public static void releaseSavepoint(Savepoint savepoint) throws SQLException {
        OSCARSavepoint sp = (OSCARSavepoint)savepoint;
        if (sp.isReleased()) {
            throw new OSQLException("OSCAR-00603", "25000", 603);
        }
        sp.releaseSavepoint();
    }
}

