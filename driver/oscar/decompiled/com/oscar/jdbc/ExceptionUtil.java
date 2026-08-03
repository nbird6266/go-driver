/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import java.net.SocketException;
import java.sql.SQLException;

public class ExceptionUtil {
    public static final int STATE_CLOSED_NO = 0;
    public static final int STATE_CLOSED_YES = 1;
    public static final int STATE_DISCONNECTIONED = 2;

    public static int isConnectionClosed(Exception e) {
        if (!(e instanceof SQLException)) {
            return 0;
        }
        SQLException tmp = (SQLException)e;
        if ("08003".equals(tmp.getSQLState())) {
            return tmp.getErrorCode() == 211 ? 2 : 1;
        }
        return 0;
    }

    public static int isConnectionClosed(Throwable e) {
        if (!(e instanceof SQLException)) {
            return 0;
        }
        SQLException tmp = (SQLException)e;
        if ("08003".equals(tmp.getSQLState())) {
            return tmp.getErrorCode() == 211 ? 2 : 1;
        }
        return 0;
    }

    public static boolean isSocketConnectionError(Throwable e) {
        SocketException se = null;
        if (e instanceof SocketException) {
            se = (SocketException)e;
        } else if (e.getCause() instanceof SocketException) {
            se = (SocketException)e.getCause();
        }
        if (se != null) {
            if (se.getMessage().startsWith("Software caused connection abort: ")) {
                return true;
            }
            if (se.getMessage().startsWith("Connection reset")) {
                return true;
            }
            if (e.getMessage().contains("Broken pipe") || e.getMessage().contains("\u65ad\u5f00\u7684\u7ba1\u9053")) {
                return true;
            }
        } else if (e.getMessage().contains("system closing")) {
            return true;
        }
        return false;
    }
}

