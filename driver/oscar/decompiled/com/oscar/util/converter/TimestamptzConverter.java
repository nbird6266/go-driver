/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

import com.oscar.util.TypeConverter;
import java.sql.Timestamp;
import java.util.Calendar;

public class TimestamptzConverter
extends TypeConverter {
    private static Calendar localCalendar = Calendar.getInstance(localTimeZone);
    private static Calendar localCalendarCal = Calendar.getInstance(localTimeZone);
    public static final byte[] POSITIVE_INFINITY = new byte[]{-3, 2};
    public static final byte[] NEGATIVE_INFINITY = new byte[]{-3, 3};

    public static synchronized byte[] convertTimestamptzToBytes(Timestamp paramTimestamp) {
        if (paramTimestamp == null) {
            return null;
        }
        byte[] infinityTimestamp = TimestamptzConverter.convertInfinityTimestampToBytes(paramTimestamp);
        if (infinityTimestamp != null) {
            return infinityTimestamp;
        }
        byte[] arrayOfByte = new byte[13];
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
        arrayOfByte[11] = (byte)(localCalendar.get(15) / HOUR_MILLISECOND + 20);
        arrayOfByte[12] = (byte)(localCalendar.get(16) % HOUR_MILLISECOND + 60);
        return arrayOfByte;
    }

    public static synchronized byte[] convertTimestampToBytes(Timestamp paramTimestamp, Calendar cal) {
        if (paramTimestamp == null) {
            return null;
        }
        byte[] infinityTimestamp = TimestamptzConverter.convertInfinityTimestampToBytes(paramTimestamp);
        if (infinityTimestamp != null) {
            return infinityTimestamp;
        }
        byte[] arrayOfByte = new byte[13];
        if (cal != null) {
            arrayOfByte[11] = (byte)(cal.get(15) / HOUR_MILLISECOND);
            arrayOfByte[12] = (byte)(cal.get(16) % HOUR_MILLISECOND / MINUTE_MILLISECOND);
        } else {
            arrayOfByte[11] = (byte)(localCalendarCal.get(15) / HOUR_MILLISECOND);
            arrayOfByte[12] = (byte)(localCalendarCal.get(16) % HOUR_MILLISECOND / MINUTE_MILLISECOND);
        }
        localCalendarCal.setTime(paramTimestamp);
        int i = localCalendarCal.get(1);
        long ns = paramTimestamp.getNanos();
        ns /= 1000L;
        if (localCalendarCal.get(0) == 0) {
            i = -i;
        }
        arrayOfByte[0] = (byte)(i / 100 + 100);
        arrayOfByte[1] = (byte)(i % 100 + 100);
        arrayOfByte[2] = (byte)(localCalendarCal.get(2) + 1);
        arrayOfByte[3] = (byte)localCalendarCal.get(5);
        arrayOfByte[4] = (byte)(localCalendarCal.get(11) + 1);
        arrayOfByte[5] = (byte)(localCalendarCal.get(12) + 1);
        arrayOfByte[6] = (byte)(localCalendarCal.get(13) + 1);
        arrayOfByte[10] = (byte)(ns & 0xFFL);
        arrayOfByte[9] = (byte)(ns >> 8 & 0xFFL);
        arrayOfByte[8] = (byte)(ns >> 16 & 0xFFL);
        arrayOfByte[7] = (byte)(ns >> 24 & 0xFFL);
        return arrayOfByte;
    }

    public static com.oscar.sql.Timestamp convertBytesToTimeStamp(byte[] val) {
        com.oscar.sql.Timestamp infinityTimestamp = TimestamptzConverter.convertBytesToInfinityTimeStamp(val);
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
        int month = TimestamptzConverter.getMonth(val);
        int day = TimestamptzConverter.getDay(val);
        int hour = TimestamptzConverter.getHour(val);
        int min = TimestamptzConverter.getMin(val);
        int sec = TimestamptzConverter.getSec(val);
        int nano = 0;
        if (val.length >= 11) {
            nano = TimestamptzConverter.getNanos(val);
        }
        int zone = 0;
        if (val.length >= 13) {
            zone = TimestamptzConverter.getZone(val, 11);
        }
        returnVal = new com.oscar.sql.Timestamp(year - 1900, month, day, hour, min, sec, nano);
        returnVal.setZone(zone);
        returnVal.setBC(isBC);
        return returnVal;
    }

    public static Timestamp convertBytesToTimeStamp(byte[] val, Calendar userCal) {
        com.oscar.sql.Timestamp infinityTimestamp = TimestamptzConverter.convertBytesToInfinityTimeStamp(val);
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
        int month = TimestamptzConverter.getMonth(val);
        int day = TimestamptzConverter.getDay(val);
        int hour = TimestamptzConverter.getHour(val);
        int min = TimestamptzConverter.getMin(val);
        int sec = TimestamptzConverter.getSec(val);
        int nano = 0;
        if (val.length >= 11) {
            nano = TimestamptzConverter.getNanos(val);
        }
        int zone = 0;
        if (val.length >= 13) {
            zone = TimestamptzConverter.getZone(val, 11);
            int userOffset = userCal.get(15) + userCal.get(16);
            int offset = userOffset - zone;
            com.oscar.sql.Timestamp oldts = new com.oscar.sql.Timestamp(year - 1900, month, day, hour, min, sec, nano);
            returnVal = new com.oscar.sql.Timestamp(oldts.getTime() + (long)offset);
        } else {
            returnVal = new com.oscar.sql.Timestamp(year - 1900, month, day, hour, min, sec, nano);
            Calendar systemCal = Calendar.getInstance();
            if (userCal != null) {
                long user = userCal.get(15) + userCal.get(16);
                long system = systemCal.get(15) + systemCal.get(16);
                returnVal = new com.oscar.sql.Timestamp(returnVal.getTime() + (system - user));
            }
        }
        returnVal.setZone(zone);
        returnVal.setBC(isBC);
        return returnVal;
    }

    protected static int getHour(byte[] val) {
        return val[4] - 1;
    }

    protected static int getMin(byte[] val) {
        return val[5] - 1;
    }

    protected static int getSec(byte[] val) {
        return val[6] - 1;
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

