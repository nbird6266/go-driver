/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.parser.statement;

import com.oscar.dispatcher.parser.statement.Statement;

public abstract class AbstractStatement
implements Statement {
    private int parameterCount;
    protected boolean isHaveTempTable = false;
    protected boolean isHaveSysOrLobTable = false;
    protected boolean isInTransaction = false;
    protected boolean isSelectStatement = false;
    protected String commandText = "";
    private boolean isPrepared;

    public int getParameterCount() {
        return this.parameterCount;
    }

    public String getSql() {
        return null;
    }

    public void setParameterCount(int count) {
        this.parameterCount = count;
    }

    public boolean isPrepared() {
        return this.isPrepared;
    }

    public void setPrepared(boolean isPrepared) {
        this.isPrepared = isPrepared;
    }

    public boolean isHaveTempTable() {
        return this.isHaveTempTable;
    }

    public void setHaveTempTable(boolean value) {
        this.isHaveTempTable = value;
    }

    public boolean isHaveSysOrLobTable() {
        return this.isHaveSysOrLobTable;
    }

    public void setHaveSysOrLobTable(boolean isHaveSysOrLobTable) {
        this.isHaveSysOrLobTable = isHaveSysOrLobTable;
    }

    public boolean isInTransaction() {
        return this.isInTransaction;
    }

    public void setInTracsaction(boolean value) {
        this.isInTransaction = value;
    }

    public boolean isSelectStatement() {
        return this.isSelectStatement;
    }

    public void setSelectStatement(boolean value) {
        this.isSelectStatement = value;
    }

    public int getSQLType() {
        return 4;
    }

    public String getCommandText() {
        return this.commandText;
    }

    public void setCommandText(String value) {
        this.commandText = value;
    }
}

