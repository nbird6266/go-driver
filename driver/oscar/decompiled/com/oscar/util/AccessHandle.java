/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.core.BaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class AccessHandle {
    private AccessHandle() {
    }

    public static long get(Connection connection) throws SQLException {
        if (connection instanceof BaseConnection) {
            if (connection != null) {
                return ((BaseConnection)((Object)connection)).getAccessHandle();
            }
            throw new SQLException("\u8be5\u8fde\u63a5\u5c1a\u672a\u5efa\u7acb");
        }
        throw new SQLException("\u8be5\u8fde\u63a5\u4e0d\u662fOSCAR\u7684JDBC\u8fde\u63a5");
    }

    public static void main(String[] args) {
        AccessHandle accesshandle = new AccessHandle();
    }
}

