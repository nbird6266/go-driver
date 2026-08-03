/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.cluster.core;

import java.sql.SQLException;

public interface DataImportStream {
    public void importData(byte[] var1) throws SQLException;
}

