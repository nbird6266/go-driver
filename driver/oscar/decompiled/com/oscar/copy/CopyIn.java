/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.copy;

import java.sql.SQLException;

public interface CopyIn {
    public void cancelCopy() throws SQLException;

    public long endCopy() throws SQLException;

    public void writeToCopy(byte[] var1, int var2, int var3) throws SQLException;

    public boolean isBackup();

    public void setMetaData(byte[] var1);
}

