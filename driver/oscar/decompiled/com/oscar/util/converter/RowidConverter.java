/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

import com.oscar.util.TypeConverter;
import java.util.HashMap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class RowidConverter
extends TypeConverter {
    public static long convertToRowID(byte[] value) {
        if (value == null) {
            return 0L;
        }
        byte bitMap = value[0];
        int srcPos = 1;
        long result = 0L;
        int tmp = 0;
        for (int i = 0; i < 8; ++i) {
            result <<= 8;
            if ((bitMap & 1) == 1) {
                tmp = value[srcPos];
                result |= (long)(tmp < 0 ? 256 + tmp : tmp);
                ++srcPos;
            }
            bitMap = (byte)(bitMap >>> 1);
        }
        return result;
    }

    public static long convertToRowID(byte[] value, int offset) {
        if (value == null || offset >= value.length) {
            return 0L;
        }
        byte bitMap = value[offset];
        ++offset;
        long result = 0L;
        int tmp = 0;
        for (int i = 0; i < 8; ++i) {
            result <<= 8;
            if ((bitMap & 1) == 1) {
                tmp = value[offset];
                result |= (long)(tmp < 0 ? 256 + tmp : tmp);
                ++offset;
            }
            bitMap = (byte)(bitMap >>> 1);
        }
        return result;
    }

    public static HashMap<String, Integer> convertToUpdateCount(byte[] value, int offset) {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        if (value == null || offset >= value.length) {
            result.put("updatecount", 0);
            result.put("tidoffset", offset);
            return result;
        }
        byte bitMap = value[offset];
        ++offset;
        int tmp = 0;
        int count = 0;
        for (int i = 0; i < 8; ++i) {
            count <<= 8;
            if ((bitMap & 1) == 1) {
                tmp = value[offset];
                count |= tmp < 0 ? 256 + tmp : tmp;
                ++offset;
            }
            bitMap = (byte)(bitMap >>> 1);
        }
        result.put("updatecount", count);
        result.put("tidoffset", offset);
        return result;
    }

    public static void main(String[] args) {
        byte[] bb = new byte[]{5, 50, 49, 32};
        long l = RowidConverter.convertToRowID(bb, 0);
        System.out.println(l);
    }
}

