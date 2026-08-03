/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.entity;

import java.sql.SQLException;
import java.sql.Statement;

public interface LsnVo {
    public long sendLsn(Statement var1) throws SQLException;

    public long getMasterLsn();

    public void setMasterLsn(long var1);
}

