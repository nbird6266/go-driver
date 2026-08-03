/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.executor.command;

import com.oscar.Driver;
import com.oscar.dispatcher.executor.command.StatementCreateCommand;
import com.oscar.jdbc.OscarJdbc2Connection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class StatmentCommand
implements StatementCreateCommand<Statement> {
    protected int resultSetType;
    protected int resultSetConcurrency;
    protected int resultSetHoldability;
    protected boolean logFlag = Driver.getLogLevel() >= 2;

    public StatmentCommand(int resultSetType, int resultSetConcurrency) {
        this(resultSetType, resultSetConcurrency, 2);
    }

    public StatmentCommand(int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
    }

    public StatmentCommand() {
        this(1003, 1007, 2);
    }

    @Override
    public Statement getStatement(Connection conn) throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + ((OscarJdbc2Connection)conn).sessionID + ", " + StatmentCommand.class + ", getStatement(Connection conn) ");
        }
        return conn.createStatement(this.resultSetType, this.resultSetConcurrency, this.resultSetHoldability);
    }
}

