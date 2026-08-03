/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.cluster.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

public interface ClusterProtocol {
    public void importBegin(String var1, InputStream var2, OutputStream var3) throws SQLException;

    public int importEnd(InputStream var1, OutputStream var2) throws SQLException;
}

