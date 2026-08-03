/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol;

import com.oscar.core.Encoding;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

public class ProtocolTypeConverter {
    public static byte[] convertToServer(byte val, Encoding encoding) {
        return new byte[]{val};
    }

    public static byte[] convertToServer(short val, Encoding encoding) throws SQLException {
        return encoding.encode(String.valueOf(val));
    }

    public static byte[] convertToServer(int val, Encoding encoding) throws SQLException {
        return encoding.encode(String.valueOf(val));
    }

    public static byte[] convertToServer(long val, Encoding encoding) throws SQLException {
        return encoding.encode(String.valueOf(val));
    }

    public static byte[] convertToServer(float val, Encoding encoding) throws SQLException {
        return encoding.encode(String.valueOf(val));
    }

    public static byte[] convertToServer(double val, Encoding encoding) throws SQLException {
        return encoding.encode(String.valueOf(val));
    }

    public static byte[] convertToServer(String val, Encoding encoding) throws SQLException {
        return encoding.encode(val);
    }

    public static byte[] convertToServer(boolean val, Encoding encoding) throws SQLException {
        return encoding.encode(String.valueOf(val));
    }

    public static byte[] convertToServer(Array val, Encoding encoding) throws SQLException {
        return encoding.encode(val.toString());
    }

    public static byte[] convertToServer(byte[] val, Encoding encoding) {
        return val;
    }

    public static byte[] convertToServer(BigDecimal val, Encoding encoding) throws SQLException {
        return encoding.encode(val.toString());
    }

    public static byte[] convertToServer(Date val, Encoding encoding) throws SQLException {
        return encoding.encode(val.toString());
    }

    public static byte[] convertToServer(Time val, Encoding encoding) throws SQLException {
        return encoding.encode(val.toString());
    }

    public static byte[] convertToServer(Timestamp val, Encoding encoding) throws SQLException {
        return encoding.encode(val.toString());
    }
}

