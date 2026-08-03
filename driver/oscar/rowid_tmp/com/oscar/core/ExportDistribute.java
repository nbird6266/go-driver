/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import java.sql.SQLException;

public interface ExportDistribute {
    public void setFetchSize(int var1);

    public void executeExport(String var1, String var2, String var3, Character var4, boolean var5) throws SQLException;

    public void executeExport(String var1, String var2, String var3, boolean var4) throws SQLException;

    public long getExportCount();

    public void close() throws SQLException;
}

