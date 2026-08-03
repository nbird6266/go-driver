/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.core.BaseConnection;
import com.oscar.jdbc.BlogResultSet;
import java.sql.SQLException;

public interface ExportBinlogHandler {
    public BaseConnection getConnection() throws SQLException;

    public BlogResultSet exportBinlogData() throws SQLException, Exception;

    public void setStartFile(String var1);

    public void setStartPos(int var1);

    public void setStopFile(String var1);

    public void setStopPos(int var1);

    public void close();

    public String getExportFile();

    public long getStartPos();
}

