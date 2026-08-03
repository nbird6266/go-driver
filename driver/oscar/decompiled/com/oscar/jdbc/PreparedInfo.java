/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.jdbc.OscarParaMetaData;

public class PreparedInfo {
    private String o_sql;
    private String[] m_sqlFragments;
    private int binds_length;
    private String m_statementName;
    private OscarParaMetaData pMetaData;
    private boolean selectSql;
    private int reference;
    private String cursorName;

    public PreparedInfo(String sql, String[] sqlFragments, String statementName, OscarParaMetaData metadata, boolean select, int length) {
        this.o_sql = sql;
        this.m_statementName = statementName;
        if (sqlFragments != null) {
            this.m_sqlFragments = new String[sqlFragments.length];
            for (int i = 0; i < sqlFragments.length; ++i) {
                this.m_sqlFragments[i] = sqlFragments[i];
            }
        }
        this.pMetaData = metadata;
        this.selectSql = select;
        this.reference = 1;
        this.binds_length = length;
    }

    public String[] getSQLFragments() {
        return this.m_sqlFragments;
    }

    public String getSql() {
        return this.o_sql;
    }

    public boolean isSelectSql() {
        return this.selectSql;
    }

    public OscarParaMetaData getMetaData() {
        return this.pMetaData;
    }

    public String getStatementName() {
        return this.m_statementName;
    }

    public int getBindsLength() {
        return this.binds_length;
    }

    public synchronized void increaseReference() {
        ++this.reference;
    }

    public synchronized void decreaseReference() {
        --this.reference;
    }

    public synchronized int getReference() {
        return this.reference;
    }

    public void setCursorName(String cursorName) {
        this.cursorName = cursorName;
    }

    public String getCursorName() {
        return this.cursorName;
    }
}

