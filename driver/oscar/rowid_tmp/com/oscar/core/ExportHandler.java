/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.core.ExportBinaryCallback;
import com.oscar.core.ExportObjectCallback;
import com.oscar.core.ExportStringCallback;
import java.sql.SQLException;

public interface ExportHandler {
    public void prepareExport(String var1) throws SQLException;

    public void executeExport(ExportBinaryCallback var1) throws SQLException;

    public void executeExport(ExportStringCallback var1) throws SQLException;

    public void executeExport(ExportObjectCallback var1) throws SQLException;

    public void setFetchSize(int var1);

    public long getExportCount();

    public void close() throws SQLException;
}

