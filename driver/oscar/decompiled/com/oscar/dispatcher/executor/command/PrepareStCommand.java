/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.executor.command;

import com.oscar.Driver;
import com.oscar.dispatcher.executor.command.StatementCreateCommand;
import com.oscar.jdbc.OscarJdbc2Connection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class PrepareStCommand
implements StatementCreateCommand<PreparedStatement> {
    protected String sql;
    protected int resultSetType;
    protected int resultSetConcurrency;
    protected int resultSetHoldability;
    protected boolean logFlag = Driver.getLogLevel() >= 2;

    public PrepareStCommand(String sql, int resultSetType, int resultSetConcurrency) {
        this(sql, resultSetType, resultSetConcurrency, 2);
    }

    public PrepareStCommand(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        this.sql = sql;
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
    }

    public PrepareStCommand(String sql) {
        this(sql, 1003, 1007, 2);
    }

    @Override
    public PreparedStatement getStatement(Connection conn) throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + ((OscarJdbc2Connection)conn).sessionID + ", " + PrepareStCommand.class + ", getStatement(Connection conn) ");
        }
        return conn.prepareStatement(this.sql, this.resultSetType, this.resultSetConcurrency, this.resultSetHoldability);
    }
}

