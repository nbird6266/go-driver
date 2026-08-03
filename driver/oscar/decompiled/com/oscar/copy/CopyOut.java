/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.copy;

import java.sql.SQLException;

public interface CopyOut {
    public static final int NORMAL = 0;
    public static final int BLOCK = 1;
    public static final int BACKUP = 2;

    public void cancelCopy() throws SQLException;

    public int getFieldCount() throws SQLException;

    public byte[] readFromCopy() throws SQLException;

    public boolean isBackup();

    public byte[] getBackUpMetadata();

    public int getKind();

    public int getHashIntValue() throws SQLException;

    public byte[] getHash() throws SQLException;

    public byte[] getCurrentBlockData() throws SQLException;
}

