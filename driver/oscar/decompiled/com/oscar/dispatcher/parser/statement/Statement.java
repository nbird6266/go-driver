/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.parser.statement;

public interface Statement {
    public boolean isPrepared();

    public void setParameterCount(int var1);

    public int getParameterCount();

    public String getSql();

    public boolean isHaveTempTable();

    public boolean isInTransaction();

    public boolean isSelectStatement();

    public boolean isHaveSysOrLobTable();

    public void setHaveTempTable(boolean var1);

    public void setHaveSysOrLobTable(boolean var1);

    public void setInTracsaction(boolean var1);

    public void setSelectStatement(boolean var1);

    public int getSQLType();

    public String getCommandText();

    public void setCommandText(String var1);
}

