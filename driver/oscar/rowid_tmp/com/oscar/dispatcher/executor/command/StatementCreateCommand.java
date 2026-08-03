/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.executor.command;

import java.sql.Connection;
import java.sql.SQLException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface StatementCreateCommand<T> {
    public T getStatement(Connection var1) throws SQLException;
}

