/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.jdbc.OscarPreparedStatement;
import com.oscar.jdbc.OscarPreparedStatementV2;
import com.oscar.jdbc.OscarStatement;
import com.oscar.util.OSQLException;
import com.oscar.util.OscarStatementCacheEntry;
import java.sql.SQLException;

public class LRUStatementCache {
    protected int cacheSize;
    protected int numElements;
    protected boolean implicitCacheEnabled;
    protected OscarStatementCacheEntry implicitCacheStart;
    protected OscarStatementCacheEntry implicitCacheEnd;

    public LRUStatementCache(int size) throws SQLException {
        if (size < 0) {
            throw new OSQLException("The initial cache size should not be less than 0.", "");
        }
        this.cacheSize = size;
        this.numElements = 0;
        this.implicitCacheStart = null;
        this.implicitCacheEnd = null;
        this.implicitCacheEnabled = false;
    }

    public void resize(int newSize) throws SQLException {
        if (newSize < 0) {
            throw new OSQLException("The reset cache size value should not be less than 0.", "");
        }
        if (newSize >= this.cacheSize || newSize >= this.numElements) {
            this.cacheSize = newSize;
        } else {
            OscarStatementCacheEntry e = this.implicitCacheEnd;
            while (this.numElements > newSize) {
                this.purgeCacheEntry(e);
                e = e.implicitPrev;
            }
            this.cacheSize = newSize;
        }
    }

    public void setImplicitCachingEnabled(boolean cacheEnabled) throws SQLException {
        if (!cacheEnabled) {
            this.purgeImplicitCache();
        }
        this.implicitCacheEnabled = cacheEnabled;
    }

    public boolean getImplicitCachingEnabled() throws SQLException {
        boolean retValue = this.cacheSize == 0 ? false : this.implicitCacheEnabled;
        return retValue;
    }

    public void addToImplicitCache(OscarPreparedStatement stmt, String sql, int statementType, int scrollType) throws SQLException {
        if (!this.implicitCacheEnabled || this.cacheSize == 0 || stmt.cacheState == 2 || stmt.cacheState == 4) {
            return;
        }
        if (this.numElements == this.cacheSize) {
            this.purgeCacheEntry(this.implicitCacheEnd);
        }
        OscarStatementCacheEntry entry = new OscarStatementCacheEntry();
        entry.statement = stmt;
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
            entry.statement.exitImplicitCacheToActive();
            return entry.statement;
        }
        return null;
    }

    public void purgeImplicitCache() throws SQLException {
        OscarStatementCacheEntry entry = this.implicitCacheStart;
        while (entry != null) {
            this.purgeCacheEntry(entry);
            entry = entry.implicitNext;
        }
        this.implicitCacheStart = null;
    }

    private void purgeCacheEntry(OscarStatementCacheEntry entry) throws SQLException {
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
            entry.statement.exitImplicitCacheToClose();
        }
    }

    public int getCacheSize() {
        return this.cacheSize;
    }

    public void printCache(String msg) throws SQLException {
        System.out.println("*** Start of Statement Cache Dump (" + msg + ") ***");
        System.out.println("cache size: " + this.cacheSize + " num elements: " + this.numElements + " implicit enabled: " + this.implicitCacheEnabled);
        System.out.println("implicitStart: " + this.implicitCacheStart);
        OscarStatementCacheEntry e = this.implicitCacheStart;
        while (e != null) {
            e.print();
            e = e.implicitNext;
        }
        System.out.println("*** End of Statement Cache Dump (" + msg + ") ***");
    }

    public void close() throws SQLException {
        OscarStatementCacheEntry entry = this.implicitCacheStart;
        while (entry != null) {
            if (entry.onImplicit) {
                entry.statement.exitImplicitCacheToClose();
            }
            entry = entry.implicitNext;
        }
        this.implicitCacheStart = null;
        this.implicitCacheEnd = null;
        this.numElements = 0;
    }

    public void addToImplicitCache(OscarPreparedStatementV2 stmt, String sql, int statementType, int scrollType) throws SQLException {
    }
}

