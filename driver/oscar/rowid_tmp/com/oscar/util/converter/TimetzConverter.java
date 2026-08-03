/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

import com.oscar.util.TypeConverter;
import java.sql.Time;
import java.util.Calendar;

public class TimetzConverter
extends TypeConverter {
    private static Calendar localCalendar = Calendar.getInstance(localTimeZone);
    private static Calendar localCalendarCal = Calendar.getInstance(localTimeZone);

    public static synchronized byte[] convertTimetzToBytes(Time paramTime) {
        if (paramTime == null) {
            return null;
        }
        byte[] arrayOfByte = new byte[5];
        localCalendar.setTime(paramTime);
        arrayOfByte[0] = (byte)(localCalendar.get(11) + 1);
        arrayOfByte[1] = (byte)(localCalendar.get(12) + 1);
        arrayOfByte[2] = (byte)(localCalendar.get(13) + 1);
        arrayOfByte[3] = (byte)(localCalendar.get(15) / HOUR_MILLISECOND);
        arrayOfByte[4] = (byte)(localCalendar.get(16) % HOUR_MILLISECOND);
        return arrayOfByte;
    }

    public static synchronized byte[] convertTimeToBytes(Time paramTime, Calendar cal) {
        if (paramTime == null) {
            return null;
        }
        byte[] arrayOfByte = new byte[9];
        if (cal != null) {
            arrayOfByte[3] = (byte)(cal.get(15) / HOUR_MILLISECOND);
            arrayOfByte[4] = (byte)(cal.get(16) % HOUR_MILLISECOND);
        } else {
            arrayOfByte[3] = (byte)(localCalendarCal.get(15) / HOUR_MILLISECOND);
            arrayOfByte[4] = (byte)(localCalendarCal.get(16) % HOUR_MILLISECOND);
        }
        localCalendarCal.setTime(paramTime);
        arrayOfByte[0] = (byte)(localCalendarCal.get(11) + 1);
        arrayOfByte[1] = (byte)(localCalendarCal.get(12) + 1);
        arrayOfByte[2] = (byte)(localCalendarCal.get(13) + 1);
        return arrayOfByte;
    }

    public static com.oscar.sql.Time convertBytesToTime(byte[] val) {
        com.oscar.sql.Time returnVal = null;
        int hour = TimetzConverter.getTimeHour(val);
        int min = TimetzConverter.getTimeMin(val);
        int sec = TimetzConverter.getTimeSec(val);
        int nano = 0;
        if (val.length >= 7) {
            nano = TimetzConverter.getTimeNanos(val);
        }
        int zone = 15;
        if (val.length >= 9) {
            zone = TimetzConverter.getZone(val, 7);
        }
        returnVal = new com.oscar.sql.Time(hour, min, sec);
        returnVal.setNanos(nano);
        returnVal.setZone(zone);
        return returnVal;
    }

    public static com.oscar.sql.Time convertBytesToTime(byte[] val, Calendar cal) {
        com.oscar.sql.Time returnVal = null;
        if (cal == null) {
            return TimetzConverter.convertBytesToTime(val);
        }
        if (val != null && val.length > 0) {
            cal.set(1970, 0, 1, TimetzConverter.getTimeHour(val), TimetzConverter.getTimeMin(val), TimetzConverter.getTimeSec(val));
            cal.set(14, 0);
            if (cal.isSet(0)) {
                cal.set(0, 1);
            }
            if (val.length >= 9) {
                int zone = TimetzConverter.getZone(val, 7);
                int userOffset = cal.get(15) + cal.get(16);
                int offset = userOffset - zone;
                returnVal = new com.oscar.sql.Time(cal.getTimeInMillis() + (long)offset);
            } else {
                returnVal = new com.oscar.sql.Time(cal.getTimeInMillis());
            }
            if (val.length >= 7) {
                returnVal.setNanos(TimetzConverter.getTimeNanos(val));
            }
        }
        return returnVal;
    }
}

