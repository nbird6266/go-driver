/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.oscarParser.sql;

import com.oscar.dispatcher.entity.DispatchConnection;
import com.oscar.dispatcher.parser.ParseException;
import com.oscar.dispatcher.parser.statement.AbstractStatement;
import com.oscar.dispatcher.parser.statement.BeginStatement;
import com.oscar.dispatcher.parser.statement.EndStatement;
import com.oscar.dispatcher.parser.statement.OtherStatement;
import com.oscar.dispatcher.parser.statement.SelectStatement;
import com.oscar.dispatcher.parser.statement.SetStatement;
import com.oscar.dispatcher.parser.statement.Statement;
import com.oscar.dispatcher.parser.statement.TransactionStatement;
import com.oscar.util.OscarSqlProcessor;
import java.sql.SQLException;

public class OscarParser {
    private Statement statement;
    private String commandText = "";
    private String strategyValue;

    public String getStrategyValue() {
        return this.strategyValue;
    }

    public OscarParser(String sql, String strategyValue) {
        this.commandText = sql.trim().endsWith(";") ? sql.trim().substring(0, sql.trim().length() - 1) : sql.trim();
        this.strategyValue = strategyValue;
    }

    public Statement doParse(DispatchConnection dispatchConnection) throws ParseException {
        try {
            this.statement = this.parse(dispatchConnection);
            this.statement.setCommandText(this.commandText);
            return this.statement;
        }
        catch (Exception e) {
            throw new ParseException(e);
        }
    }

    public Statement getParsedStatement() {
        return this.statement;
    }

    public final Statement parse(DispatchConnection dispatchConnection) throws SQLException {
        AbstractStatement statement = null;
        if (this.commandText.toLowerCase().startsWith("begin")) {
            return new BeginStatement();
        }
        if (this.commandText.toLowerCase().equalsIgnoreCase("commit") || this.commandText.toLowerCase().equalsIgnoreCase("rollback")) {
            return new EndStatement();
        }
        if (this.commandText.toLowerCase().startsWith("set") || this.commandText.toLowerCase().startsWith("reset")) {
            return new SetStatement();
        }
        if (this.commandText.toLowerCase().contains("nextval") || this.commandText.toLowerCase().contains("currval")) {
            return new OtherStatement();
        }
        if (!dispatchConnection.getAutoCommit() && dispatchConnection.isHasUpdate()) {
            statement = new TransactionStatement();
        } else if ("2".equals(this.strategyValue)) {
            if (!dispatchConnection.getAutoCommit()) {
                if (OscarSqlProcessor.whetherToUseCursor(this.commandText)) {
                    statement = new SelectStatement();
                } else {
                    statement = new TransactionStatement();
                    dispatchConnection.setHasUpdate(true);
                }
            }
        } else if (!dispatchConnection.getAutoCommit()) {
            statement = new TransactionStatement();
        }
        if (statement != null) {
            return statement;
        }
        statement = OscarSqlProcessor.whetherToUseCursor(this.commandText) ? new SelectStatement() : new OtherStatement();
        return statement;
    }
}

