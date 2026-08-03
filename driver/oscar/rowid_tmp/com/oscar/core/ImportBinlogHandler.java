/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.core.BaseConnection;
import java.sql.SQLException;

public interface ImportBinlogHandler {
    public BaseConnection getConnection() throws SQLException;

    public void importBinlogBegin() throws SQLException;

    public int importBinlogData(byte[] var1, int var2, int var3) throws SQLException;

    public void importBinlogEnd() throws SQLException;

    public void setRepeatCount(int var1);

    public void execute() throws Exception;

    public void close();
}

