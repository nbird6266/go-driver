/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.util.OSQLException;
import java.sql.SQLException;

public class OSCARbyte {
    private OSCARbyte() {
    }

    public static byte[] toBytes(byte[] fromBytes) throws SQLException {
        if (fromBytes == null) {
            return null;
        }
        if (fromBytes.length <= 1) {
            throw new OSQLException("OSCAR-00104", "88888", 104);
        }
        if (fromBytes[0] != 48 || fromBytes[1] != 120) {
            throw new OSQLException("OSCAR-00104", "88888", 104);
        }
        int length = (fromBytes.length - 2) / 2;
        byte[] toBytes = new byte[length];
        for (int i = 0; i < length; ++i) {
            byte first = fromBytes[i * 2 + 2];
            first = first >= 48 && first <= 57 ? (byte)(first - 48) : (first >= 97 ? (byte)(first - 87) : (byte)(first - 55));
            byte second = fromBytes[i * 2 + 3];
            second = second >= 48 && second <= 57 ? (byte)(second - 48) : (second >= 97 ? (byte)(second - 87) : (byte)(second - 55));
            toBytes[i] = (byte)(first << 4 | second);
        }
        return toBytes;
    }

    public static String toOSCARString(byte[] bytes) throws SQLException {
        if (bytes == null) {
            return null;
        }
        int length = bytes.length;
        StringBuffer buffer = new StringBuffer(bytes.length * 2 + 2);
        buffer.append('0');
        buffer.append('x');
        for (int i = 0; i < length; ++i) {
            byte first = (byte)(bytes[i] >>> 4 & 0xF);
            byte second = (byte)(bytes[i] & 0xF);
            if (first <= 9) {
                buffer.append(first);
            } else {
                buffer.append((char)(65 + first - 10));
            }
            if (second <= 9) {
                buffer.append(second);
                continue;
            }
            buffer.append((char)(65 + second - 10));
        }
        return buffer.toString();
    }
}

