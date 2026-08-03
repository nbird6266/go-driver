/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.executor;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.core.BaseResultSet;
import com.oscar.dispatcher.entity.DispatchConnection;
import com.oscar.dispatcher.executor.AbstractExecuteCommand;
import com.oscar.dispatcher.executor.DispatchAbstractStatement;
import com.oscar.dispatcher.executor.command.StatmentCommand;
import com.oscar.util.OSQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class DispatchStatementV2
extends DispatchAbstractStatement
implements Statement {
    public DispatchStatementV2(DispatchConnection con) {
        this.dispatchConnection = con;
        this.createCommand = new StatmentCommand();
    }

    public DispatchStatementV2(DispatchConnection con, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        this.dispatchConnection = con;
        this.createCommand = new StatmentCommand(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public DispatchStatementV2(DispatchConnection con, int resultSetType, int resultSetConcurrency) {
        this.dispatchConnection = con;
        this.createCommand = new StatmentCommand(resultSetType, resultSetConcurrency);
    }

    public ResultSet executeQuery(final String sql) throws SQLException {
        int dispatchType = this.getExecuteType(sql);
        AbstractExecuteCommand<ResultSet> ec = new AbstractExecuteCommand<ResultSet>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.executeQuery(" + sql + ")";
            }

            @Override
            public boolean isExecuteFunction() {
                return true;
            }

            @Override
            public ResultSet execute(Statement t) throws SQLException {
                BaseResultSet brs;
                BaseConnection conn = (BaseConnection)((Object)t.getConnection());
                ResultSet rs = t.executeQuery(sql);
                if (conn.isSlave() && conn.isZeroResend() && (brs = (BaseResultSet)rs).getTupleCount() == 0) {
                    rs.close();
                    throw new OSQLException(888888, "0A502", "resultSet size is 0 ,try to switch to main");
                }
                return rs;
            }
        };
        return this.executeTemplet(ec, dispatchType);
    }

    public int executeUpdate(final String sql) throws SQLException {
        int dispatchType = this.getExecuteType(sql);
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.executeUpdate(" + sql + ")";
            }

            @Override
            public boolean isExecuteFunction() {
                return true;
            }

            @Override
            public Integer execute(Statement t) throws SQLException {
                return t.executeUpdate(sql);
            }
        };
        return this.executeTemplet(ec, dispatchType);
    }

    public void close() throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.close()";
            }

            @Override
            public Object execute(Statement t) throws SQLException {
                t.close();
                return null;
            }
        };
        this.executeTemplet(ec, 3);
    }

    public int getMaxFieldSize() throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.getMaxFieldSize()";
            }

            @Override
            public Integer execute(Statement t) throws SQLException {
                return t.getMaxFieldSize();
            }
        };
        return this.executeTemplet(ec, 0);
    }

    public void setMaxFieldSize(final int max) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.setMaxFieldSize(" + max + ")";
            }

            @Override
            public Object execute(Statement t) throws SQLException {
                t.setMaxFieldSize(max);
                DispatchStatementV2.this.maxFieldSize = max;
                return null;
            }
        };
        this.executeTemplet(ec, 3);
    }

    public int getMaxRows() throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                return t.getMaxRows();
            }
        };
        return this.executeTemplet(ec, 0);
    }

    public void setMaxRows(final int max) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Object execute(Statement t) throws SQLException {
                t.setMaxRows(max);
                DispatchStatementV2.this.maxrows = max;
                return null;
            }
        };
        this.executeTemplet(ec, 3);
    }

    public void setEscapeProcessing(final boolean enable) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Object execute(Statement t) throws SQLException {
                t.setEscapeProcessing(enable);
                DispatchStatementV2.this.replaceProcessingEnabled = enable;
                return null;
            }
        };
        this.executeTemplet(ec, 3);
    }

    public int getQueryTimeout() throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                return t.getQueryTimeout();
            }
        };
        return this.executeTemplet(ec, 0);
    }

    public void setQueryTimeout(final int seconds) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Object execute(Statement t) throws SQLException {
                t.setQueryTimeout(seconds);
                DispatchStatementV2.this.timeout = seconds;
                return null;
            }
        };
        this.executeTemplet(ec, 3);
    }

    public void cancel() throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Object execute(Statement t) throws SQLException {
                t.cancel();
                return null;
            }
        };
        this.executeTemplet(ec, this.getCurrentExecuteType());
    }

    public SQLWarning getWarnings() throws SQLException {
        AbstractExecuteCommand<SQLWarning> ec = new AbstractExecuteCommand<SQLWarning>(){

            @Override
            public SQLWarning execute(Statement t) throws SQLException {
                return t.getWarnings();
            }
        };
        return this.executeTemplet(ec, this.getCurrentExecuteType());
    }

    public void clearWarnings() throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Object execute(Statement t) throws SQLException {
                t.clearWarnings();
                return null;
            }
        };
        this.executeTemplet(ec, 3);
    }

    public void setCursorName(final String name) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Object execute(Statement t) throws SQLException {
                t.setCursorName(name);
                DispatchStatementV2.this.cursor = name;
                return null;
            }
        };
        this.executeTemplet(ec, this.getCurrentExecuteType());
    }

    public boolean execute(final String sql) throws SQLException {
        int dispatchType = this.getExecuteType(sql);
        AbstractExecuteCommand<Boolean> ec = new AbstractExecuteCommand<Boolean>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.execute(" + sql + ")";
            }

            @Override
            public boolean isExecuteFunction() {
                return true;
            }

            @Override
            public Boolean execute(Statement t) throws SQLException {
                return t.execute(sql);
            }
        };
        return this.executeTemplet(ec, dispatchType);
    }

    public ResultSet getResultSet() throws SQLException {
        AbstractExecuteCommand<ResultSet> ec = new AbstractExecuteCommand<ResultSet>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.getResultSet()";
            }

            @Override
            public ResultSet execute(Statement t) throws SQLException {
                return t.getResultSet();
            }
        };
        return this.executeTemplet(ec, this.getCurrentExecuteType());
    }

    public int getUpdateCount() throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.getUpdateCount()";
            }

            @Override
            public Integer execute(Statement t) throws SQLException {
                return t.getUpdateCount();
            }
        };
        return this.executeTemplet(ec, this.getCurrentExecuteType());
    }

    public boolean getMoreResults() throws SQLException {
        AbstractExecuteCommand<Boolean> ec = new AbstractExecuteCommand<Boolean>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.getMoreResults()";
            }

            @Override
            public Boolean execute(Statement t) throws SQLException {
                return t.getMoreResults();
            }
        };
        return this.executeTemplet(ec, this.getCurrentExecuteType());
    }

    public void setFetchDirection(final int direction) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Object execute(Statement t) throws SQLException {
                t.setFetchDirection(direction);
                DispatchStatementV2.this.fetchdirection = direction;
                return null;
            }
        };
        this.executeTemplet(ec, 3);
    }

    public int getFetchDirection() throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                return t.getFetchDirection();
            }
        };
        return this.executeTemplet(ec, 0);
    }

    public void setFetchSize(final int rows) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Object execute(Statement t) throws SQLException {
                t.setFetchSize(rows);
                DispatchStatementV2.this.fetchSize = rows;
                return null;
            }
        };
        this.executeTemplet(ec, 3);
    }

    public int getFetchSize() throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                return t.getFetchSize();
            }
        };
        return this.executeTemplet(ec, 0);
    }

    public int getResultSetConcurrency() throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                return t.getResultSetConcurrency();
            }
        };
        return this.executeTemplet(ec, 0);
    }

    public int getResultSetType() throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                return t.getResultSetType();
            }
        };
        return this.executeTemplet(ec, this.getCurrentExecuteType());
    }

    public void addBatch(final String sql) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.addBatch(" + sql + ")";
            }

            @Override
            public Object execute(Statement t) throws SQLException {
                t.addBatch(sql);
                return null;
            }
        };
        this.executeTemplet(ec, 0);
    }

    public void clearBatch() throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Object execute(Statement t) throws SQLException {
                t.clearBatch();
                return null;
            }
        };
        this.executeTemplet(ec, 0);
    }

    public int[] executeBatch() throws SQLException {
        AbstractExecuteCommand<int[]> ec = new AbstractExecuteCommand<int[]>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.executeBatch()";
            }

            @Override
            public int[] execute(Statement t) throws SQLException {
                return t.executeBatch();
            }
        };
        return this.executeTemplet(ec, 0);
    }

    public Connection getConnection() throws SQLException {
        return this.dispatchConnection;
    }

    public boolean getMoreResults(final int current) throws SQLException {
        AbstractExecuteCommand<Boolean> ec = new AbstractExecuteCommand<Boolean>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.getMoreResults()";
            }

            @Override
            public Boolean execute(Statement t) throws SQLException {
                return t.getMoreResults(current);
            }
        };
        return this.executeTemplet(ec, this.getCurrentExecuteType());
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        AbstractExecuteCommand<ResultSet> ec = new AbstractExecuteCommand<ResultSet>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.getGeneratedKeys()";
            }

            @Override
            public ResultSet execute(Statement t) throws SQLException {
                return t.getGeneratedKeys();
            }
        };
        return this.executeTemplet(ec, this.getCurrentExecuteType());
    }

    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.executeUpdate(" + sql + "," + autoGeneratedKeys + ")";
            }

            @Override
            public boolean isExecuteFunction() {
                return true;
            }

            @Override
            public Integer execute(Statement t) throws SQLException {
                return t.executeUpdate(sql, autoGeneratedKeys);
            }
        };
        return this.executeTemplet(ec, 0);
    }

    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.executeUpdate(" + sql + "," + columnIndexes + ")";
            }

            @Override
            public boolean isExecuteFunction() {
                return true;
            }

            @Override
            public Integer execute(Statement t) throws SQLException {
                return t.executeUpdate(sql, columnIndexes);
            }
        };
        return this.executeTemplet(ec, 0);
    }

    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.executeUpdate(" + sql + "," + columnNames + ")";
            }

            @Override
            public boolean isExecuteFunction() {
                return true;
            }

            @Override
            public Integer execute(Statement t) throws SQLException {
                return t.executeUpdate(sql, columnNames);
            }
        };
        return this.executeTemplet(ec, 0);
    }

    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        int dispatchType = this.getExecuteType(sql);
        AbstractExecuteCommand<Boolean> ec = new AbstractExecuteCommand<Boolean>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.executeUpdate(" + sql + "," + autoGeneratedKeys + ")";
            }

            @Override
            public boolean isExecuteFunction() {
                return true;
            }

            @Override
            public Boolean execute(Statement t) throws SQLException {
                return t.execute(sql, autoGeneratedKeys);
            }
        };
        return this.executeTemplet(ec, dispatchType);
    }

    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        int dispatchType = this.getExecuteType(sql);
        AbstractExecuteCommand<Boolean> ec = new AbstractExecuteCommand<Boolean>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.execute(" + sql + "," + columnIndexes + ")";
            }

            @Override
            public boolean isExecuteFunction() {
                return true;
            }

            @Override
            public Boolean execute(Statement t) throws SQLException {
                return t.execute(sql, columnIndexes);
            }
        };
        return this.executeTemplet(ec, dispatchType);
    }

    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        int dispatchType = this.getExecuteType(sql);
        AbstractExecuteCommand<Boolean> ec = new AbstractExecuteCommand<Boolean>(){

            @Override
            public String getFunctionName() {
                return "DispatchStatementV2.execute(" + sql + "," + columnNames + ")";
            }

            @Override
            public boolean isExecuteFunction() {
                return true;
            }

            @Override
            public Boolean execute(Statement t) throws SQLException {
                return t.execute(sql, columnNames);
            }
        };
        return this.executeTemplet(ec, dispatchType);
    }

    public int getResultSetHoldability() throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                return t.getResultSetHoldability();
            }
        };
        return this.executeTemplet(ec, 0);
    }

    protected void initStatement(Statement statement) throws SQLException {
        if (this.maxFieldSize != null) {
            statement.setMaxFieldSize(this.maxFieldSize);
        }
        if (this.fetchSize != null) {
            statement.setFetchSize(this.fetchSize);
        }
        if (this.maxrows != null) {
            statement.setMaxRows(this.maxrows);
        }
        if (this.replaceProcessingEnabled != null) {
            statement.setEscapeProcessing(this.replaceProcessingEnabled);
        }
        if (this.fetchdirection != null) {
            statement.setFetchDirection(this.fetchdirection);
        }
        if (this.timeout != null) {
            statement.setQueryTimeout(this.timeout);
        }
        if (this.cursor != null) {
            statement.setCursorName(this.cursor);
        }
    }

    protected boolean expectionHandler(SQLException e, Statement slave, Statement master) throws SQLException {
        boolean changeSuccess;
        block3: {
            changeSuccess = this.handleException(e, slave, master);
            if (this.isDisconnected(e)) {
                this.dispatchConnection.removeSlaveConnection(slave.getConnection());
                try {
                    slave.close();
                }
                catch (SQLException e1) {
                    if (!this.logFlag) break block3;
                    Driver.writeLog("warning:" + e1);
                }
            }
        }
        return changeSuccess;
    }

    protected boolean handleException(SQLException e, Statement slave, Statement master) throws SQLException {
        return this.getErrorCode(e);
    }
}

