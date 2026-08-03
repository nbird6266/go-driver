/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

import com.oscar.util.TypeConverter;

public class BooleanConverter
extends TypeConverter {
    public static byte[] convertBooleanToBytes(boolean value) {
        if (value) {
            return new byte[]{1};
        }
        return new byte[]{0};
    }

    public static boolean convertToBoolean(byte value) {
        return value == 1;
    }

    public static boolean convertToBoolean(byte[] value) {
        if (value == null) {
            return false;
        }
        if (value.length >= 1) {
            char c_tmp = (char)value[0];
            if (c_tmp == 't' || c_tmp == '1') {
                return true;
            }
            if (c_tmp == 'f' || c_tmp == '0') {
                return false;
            }
            byte i_tmp = value[0];
            return i_tmp > 0;
        }
        return false;
    }

    public static boolean convertToBoolean(byte[] value, int oscarType) {
        if (value == null) {
            return false;
        }
        return value.length >= 1 && value[0] == 1;
    }
}

