/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.jdbc.OscarPreparedStatementV2;
import com.oscar.jdbc.OscarStatement;
import com.oscar.util.LRUStatementCache;
import com.oscar.util.OscarStatementCacheEntry;
import java.sql.SQLException;

public class LRUStatementCacheV2
extends LRUStatementCache {
    public LRUStatementCacheV2(int size) throws SQLException {
        super(size);
    }

    public void addToImplicitCache(OscarPreparedStatementV2 stmt, String sql, int statementType, int scrollType) throws SQLException {
        if (!this.implicitCacheEnabled || this.cacheSize == 0 || stmt.cacheState == 2 || stmt.cacheState == 4) {
            return;
        }
        if (this.numElements == this.cacheSize) {
            this.purgeCacheEntry(this.implicitCacheEnd);
        }
        OscarStatementCacheEntry entry = new OscarStatementCacheEntry();
        entry.statementV2 = stmt;
        entry.onImplicit = true;
        entry.sql = sql;
        entry.statementType = statementType;
        entry.scrollType = scrollType;
        entry.implicitNext = this.implicitCacheStart;
        entry.implicitPrev = null;
        if (this.implicitCacheStart != null) {
            this.implicitCacheStart.implicitPrev = entry;
        }
        this.implicitCacheStart = entry;
        if (this.implicitCacheEnd == null) {
            this.implicitCacheEnd = entry;
        }
        ++this.numElements;
    }

    public void close() throws SQLException {
        OscarStatementCacheEntry entry = this.implicitCacheStart;
        while (entry != null) {
            if (entry.onImplicit) {
                entry.statementV2.exitImplicitCacheToClose();
            }
            entry = entry.implicitNext;
        }
        this.implicitCacheStart = null;
        this.implicitCacheEnd = null;
        this.numElements = 0;
    }

    public OscarStatement searchImplicitCache(String sql, int statementType, int scrollType) throws SQLException {
        if (!this.implicitCacheEnabled) {
            return null;
        }
        OscarStatementCacheEntry entry = null;
        entry = this.implicitCacheStart;
        while (!(entry == null || entry.statementType == statementType && entry.scrollType == scrollType && entry.sql.equals(sql))) {
            entry = entry.implicitNext;
        }
        if (entry != null) {
            if (entry.implicitPrev != null) {
                entry.implicitPrev.implicitNext = entry.implicitNext;
            }
            if (entry.implicitNext != null) {
                entry.implicitNext.implicitPrev = entry.implicitPrev;
            }
            if (this.implicitCacheStart == entry) {
                this.implicitCacheStart = entry.implicitNext;
            }
            if (this.implicitCacheEnd == entry) {
                this.implicitCacheEnd = entry.implicitPrev;
            }
            --this.numElements;
            entry.statementV2.exitImplicitCacheToActive();
            return entry.statementV2;
        }
        return null;
    }

    protected void purgeCacheEntry(OscarStatementCacheEntry entry) throws SQLException {
        if (entry.onImplicit) {
            if (entry.implicitNext != null) {
                entry.implicitNext.implicitPrev = entry.implicitPrev;
            }
            if (entry.implicitPrev != null) {
                entry.implicitPrev.implicitNext = entry.implicitNext;
            }
            if (this.implicitCacheStart == entry) {
                this.implicitCacheStart = entry.implicitNext;
            }
            if (this.implicitCacheEnd == entry) {
                this.implicitCacheEnd = entry.implicitPrev;
            }
        }
        --this.numElements;
        if (entry.onImplicit) {
            entry.statementV2.exitImplicitCacheToClose();
        }
    }
}

