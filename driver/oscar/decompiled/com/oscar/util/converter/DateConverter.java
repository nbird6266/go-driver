/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

import com.oscar.util.OSQLException;
import com.oscar.util.TypeConverter;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;

public class DateConverter
extends TypeConverter {
    private static ThreadLocal<Calendar> localCalendarHolder = new ThreadLocal<Calendar>(){

        @Override
        public Calendar initialValue() {
            return Calendar.getInstance(localTimeZone);
        }
    };
    public static final byte[] POSITIVE_INFINITY = new byte[]{-3, 2};
    public static final byte[] NEGATIVE_INFINITY = new byte[]{-3, 3};

    public static byte[] convertDateToBytes(Date date) {
        Calendar calendar = localCalendarHolder.get();
        if (date == null) {
            return null;
        }
        if (date.getTime() == Long.MAX_VALUE) {
            return POSITIVE_INFINITY;
        }
        if (date.getTime() == Long.MIN_VALUE) {
            return NEGATIVE_INFINITY;
        }
        byte[] arrayOfByte = new byte[7];
        calendar.setTime(date);
        int i = calendar.get(1);
        if (calendar.get(0) == 0) {
            i = -i;
        }
        arrayOfByte[0] = (byte)(i / 100 + 100);
        arrayOfByte[1] = (byte)(i % 100 + 100);
        arrayOfByte[2] = (byte)(calendar.get(2) + 1);
        arrayOfByte[3] = (byte)calendar.get(5);
        arrayOfByte[4] = (byte)(calendar.get(11) + 1);
        arrayOfByte[5] = (byte)(calendar.get(12) + 1);
        arrayOfByte[6] = (byte)(calendar.get(13) + 1);
        return arrayOfByte;
    }

    public static Date convertBytesToDate(byte[] val) throws SQLException {
        int year;
        com.oscar.sql.Date returnVal = null;
        Calendar calendar = localCalendarHolder.get();
        if (val == null) {
            return null;
        }
        if (val.length == 2 && val[0] == -3) {
            if (val[1] == 2) {
                return new com.oscar.sql.Date(Long.MAX_VALUE);
            }
            if (val[1] == 3) {
                return new com.oscar.sql.Date(Long.MIN_VALUE);
            }
        }
        boolean isBC = false;
        if (val.length == 7) {
            year = ((val[0] & 0xFF) - 100) * 100 + (val[1] & 0xFF) - 100;
            if (year < 0) {
                year = -year;
                isBC = true;
            }
            calendar.set(year, DateConverter.getMonth(val), DateConverter.getDay(val), DateConverter.getHour(val), DateConverter.getMin(val), DateConverter.getSec(val));
        } else if (val.length == 4) {
            year = ((val[0] & 0xFF) - 100) * 100 + (val[1] & 0xFF) - 100;
            if (year < 0) {
                year = -year;
                isBC = true;
            }
            calendar.set(year, DateConverter.getMonth(val), DateConverter.getDay(val));
        } else {
            throw new OSQLException("OSCAR-00710", "88888", 710);
        }
        calendar.set(14, 0);
        if (year > 0 && calendar.isSet(0)) {
            calendar.set(0, 1);
        }
        returnVal = new com.oscar.sql.Date(calendar.getTimeInMillis());
        returnVal.setBC(isBC);
        return returnVal;
    }
}

