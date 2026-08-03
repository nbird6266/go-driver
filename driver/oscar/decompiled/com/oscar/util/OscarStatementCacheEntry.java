/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.jdbc.OscarPreparedStatement;
import com.oscar.jdbc.OscarPreparedStatementV2;
import java.sql.SQLException;

public class OscarStatementCacheEntry {
    protected OscarStatementCacheEntry implicitNext = null;
    protected OscarStatementCacheEntry implicitPrev = null;
    boolean onImplicit;
    String sql;
    int statementType;
    int scrollType;
    OscarPreparedStatement statement;
    OscarPreparedStatementV2 statementV2;

    public void print() throws SQLException {
        System.out.println("Cache entry " + this);
        System.out.println("  Key: " + this.sql + "$$" + this.statementType + "$$" + this.scrollType);
        System.out.println("  Statement: " + this.statement);
        System.out.println("  StatementV2: " + this.statementV2);
        System.out.println("  onImplicit: " + this.onImplicit);
        System.out.println("  implicitNext: " + this.implicitNext + "  implicitPrev: " + this.implicitPrev);
    }
}

