/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

import com.oscar.util.TypeConverter;
import java.sql.Time;
import java.util.Calendar;

public class TimeConverter
extends TypeConverter {
    private static Calendar localCalendar = Calendar.getInstance(localTimeZone);
    private static Calendar localCalendarOut = Calendar.getInstance(localTimeZone);

    public static synchronized byte[] convertTimeToBytes(Time time) {
        if (time == null) {
            return null;
        }
        byte[] arrayOfByte = new byte[3];
        localCalendar.setTime(time);
        arrayOfByte[0] = (byte)(localCalendar.get(11) + 1);
        arrayOfByte[1] = (byte)(localCalendar.get(12) + 1);
        arrayOfByte[2] = (byte)(localCalendar.get(13) + 1);
        return arrayOfByte;
    }

    public static synchronized com.oscar.sql.Time convertBytesToTime(byte[] val) {
        com.oscar.sql.Time returnVal = null;
        if (val != null && val.length > 0) {
            localCalendarOut.set(1970, 0, 1, TimeConverter.getTimeHour(val), TimeConverter.getTimeMin(val), TimeConverter.getTimeSec(val));
            localCalendarOut.set(14, 0);
            if (localCalendarOut.isSet(0)) {
                localCalendarOut.set(0, 1);
            }
            returnVal = new com.oscar.sql.Time(localCalendarOut.getTimeInMillis());
            if (val.length >= 7) {
                returnVal.setNanos(TimeConverter.getTimeNanos(val));
            }
        }
        return returnVal;
    }
}

