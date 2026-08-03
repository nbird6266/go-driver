/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.util.ColumnData;
import com.oscar.util.ImportStream;
import java.sql.SQLException;

public class NullData
extends ColumnData {
    public static void preWirte(ImportStream out, int mode) throws SQLException {
        out.writeInteger(mode, 2);
    }

    public void preWrite(ImportStream out) throws SQLException {
    }

    public void endWrite(ImportStream out) throws SQLException {
    }

    public void clear() throws SQLException {
    }

    public void preWriteBlock(ImportStream out) throws SQLException {
    }
}

