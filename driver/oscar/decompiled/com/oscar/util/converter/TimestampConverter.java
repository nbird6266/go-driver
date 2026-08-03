/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

import com.oscar.util.TypeConverter;
import java.sql.Timestamp;
import java.util.Calendar;

public class TimestampConverter
extends TypeConverter {
    public static final byte[] POSITIVE_INFINITY = new byte[]{-3, 2};
    public static final byte[] NEGATIVE_INFINITY = new byte[]{-3, 3};
    private static Calendar localCalendar = Calendar.getInstance(localTimeZone);

    public static synchronized byte[] convertTimestampToBytes(Timestamp paramTimestamp) {
        if (paramTimestamp == null) {
            return null;
        }
        byte[] infinityTimestamp = TimestampConverter.convertInfinityTimestampToBytes(paramTimestamp);
        if (infinityTimestamp != null) {
            return infinityTimestamp;
        }
        byte[] arrayOfByte = new byte[11];
        localCalendar.setTime(paramTimestamp);
        int i = localCalendar.get(1);
        long ns = paramTimestamp.getNanos();
        ns /= 1000L;
        if (localCalendar.get(0) == 0) {
            i = -i;
        }
        arrayOfByte[0] = (byte)(i / 100 + 100);
        arrayOfByte[1] = (byte)(i % 100 + 100);
        arrayOfByte[2] = (byte)(localCalendar.get(2) + 1);
        arrayOfByte[3] = (byte)localCalendar.get(5);
        arrayOfByte[4] = (byte)(localCalendar.get(11) + 1);
        arrayOfByte[5] = (byte)(localCalendar.get(12) + 1);
        arrayOfByte[6] = (byte)(localCalendar.get(13) + 1);
        arrayOfByte[10] = (byte)(ns & 0xFFL);
        arrayOfByte[9] = (byte)(ns >> 8 & 0xFFL);
        arrayOfByte[8] = (byte)(ns >> 16 & 0xFFL);
        arrayOfByte[7] = (byte)(ns >> 24 & 0xFFL);
        return arrayOfByte;
    }

    public static com.oscar.sql.Timestamp convertBytesToTimeStamp(byte[] val) {
        com.oscar.sql.Timestamp infinityTimestamp = TimestampConverter.convertBytesToInfinityTimeStamp(val);
        if (infinityTimestamp != null) {
            return infinityTimestamp;
        }
        com.oscar.sql.Timestamp returnVal = null;
        int year = ((val[0] & 0xFF) - 100) * 100 + (val[1] & 0xFF) - 100;
        boolean isBC = false;
        if (year < 0) {
            year = -year;
            isBC = true;
        }
        int month = TimestampConverter.getMonth(val);
        int day = TimestampConverter.getDay(val);
        int hour = TimestampConverter.getHour(val);
        int min = TimestampConverter.getMin(val);
        int sec = TimestampConverter.getSec(val);
        int nano = 0;
        if (val.length >= 11) {
            nano = TimestampConverter.getNanos(val);
        }
        int zone = 0;
        if (val.length >= 13) {
            zone = TimestampConverter.getZone(val, 11);
        }
        returnVal = new com.oscar.sql.Timestamp(year - 1900, month, day, hour, min, sec, nano);
        returnVal.setZone(zone);
        returnVal.setBC(isBC);
        return returnVal;
    }

    private static com.oscar.sql.Timestamp convertBytesToInfinityTimeStamp(byte[] val) {
        if (val.length == 2 && val[0] == -3) {
            if (val[1] == 2) {
                return new com.oscar.sql.Timestamp(Long.MAX_VALUE);
            }
            if (val[1] == 3) {
                return new com.oscar.sql.Timestamp(Long.MIN_VALUE);
            }
        }
        return null;
    }

    private static byte[] convertInfinityTimestampToBytes(Timestamp paramTimestamp) {
        if (paramTimestamp.getTime() == Long.MAX_VALUE) {
            return POSITIVE_INFINITY;
        }
        if (paramTimestamp.getTime() == Long.MIN_VALUE) {
            return NEGATIVE_INFINITY;
        }
        return null;
    }
}

