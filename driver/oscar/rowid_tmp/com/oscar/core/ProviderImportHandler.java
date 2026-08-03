/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.core.ImportDataProvider;
import java.sql.SQLException;

public interface ProviderImportHandler {
    public long executeImport(ImportDataProvider var1) throws SQLException;

    public void close() throws SQLException;
}

