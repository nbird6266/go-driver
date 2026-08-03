/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.core.ImportHandler;
import java.sql.SQLException;

public interface DistributeImportHandler
extends ImportHandler {
    public void setNodeNum(int var1) throws SQLException;
}

