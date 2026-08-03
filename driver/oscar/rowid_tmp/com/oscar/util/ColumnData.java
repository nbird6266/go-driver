/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.util.ImportStream;
import java.sql.SQLException;

public abstract class ColumnData {
    public static final int dataLength = 2;

    public abstract void preWrite(ImportStream var1) throws SQLException;

    public abstract void endWrite(ImportStream var1) throws SQLException;

    public abstract void clear() throws SQLException;

    public abstract void preWriteBlock(ImportStream var1) throws SQLException;
}

