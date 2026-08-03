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
public class PrepareStIndexCommand
implements StatementCreateCommand<PreparedStatement> {
    private String sql;
    private int[] columnIndexes;
    protected boolean logFlag = Driver.getLogLevel() >= 2;

    public PrepareStIndexCommand(String sql, int[] columnIndexes) {
        this.sql = sql;
        this.columnIndexes = columnIndexes;
    }

    @Override
    public PreparedStatement getStatement(Connection conn) throws SQLException {
        if (this.logFlag) {
            Driver.writeLog("session: " + ((OscarJdbc2Connection)conn).sessionID + ", " + PrepareStIndexCommand.class + ", getStatement(Connection conn) ");
        }
        return conn.prepareStatement(this.sql, this.columnIndexes);
    }
}

