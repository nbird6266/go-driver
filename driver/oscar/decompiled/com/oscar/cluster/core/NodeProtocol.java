/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.cluster.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

public interface NodeProtocol {
    public void nodeImportEnd(InputStream var1, OutputStream var2) throws SQLException;

    public void sendImportCredential(int var1, int var2, InputStream var3, OutputStream var4) throws SQLException;

    public void importData2Node(byte[] var1, InputStream var2, OutputStream var3) throws SQLException;
}

