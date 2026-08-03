/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.sql;

import com.oscar.util.TypeConverter;

public class Time
extends java.sql.Time {
    private static final long serialVersionUID = 2761880807538277795L;
    private int nanos;
    private int zone = 15;

    public Time(long time) {
        super(time);
    }

    public Time(int hour, int minute, int second) {
        super(hour, minute, second);
    }

    public void setNanos(int n) {
        if (n > 999999999 || n < 0) {
            throw new IllegalArgumentException("nanos > 999999999 or < 0");
        }
        this.nanos = n;
    }

    public String toString() {
        StringBuffer ret = new StringBuffer(super.toString());
        String nanosString = "";
        if (this.nanos != 0) {
            String zeros = "000000000";
            nanosString = Integer.toString(this.nanos);
            nanosString = zeros.substring(0, 9 - nanosString.length()) + nanosString;
            char[] nanosChar = new char[nanosString.length()];
            nanosString.getChars(0, nanosString.length(), nanosChar, 0);
            int truncIndex = 8;
            while (nanosChar[truncIndex] == '0') {
                --truncIndex;
            }
            nanosString = new String(nanosChar, 0, truncIndex + 1);
        }
        if (!"".equals(nanosString)) {
            ret.append(".").append(nanosString);
        }
        if (this.zone != 15 && this.zone != 0) {
            ret.append(" ");
            int zoneHour = this.zone / TypeConverter.HOUR_SECOND;
            if (-10 < zoneHour && zoneHour < 0) {
                ret.append("-0");
                ret.append(-zoneHour);
            } else if (zoneHour >= 0 && zoneHour < 10) {
                ret.append("+0").append(zoneHour);
            } else if (zoneHour >= 10) {
                ret.append("+").append(zoneHour);
            } else if (zoneHour <= -10) {
                ret.append(zoneHour);
            }
            ret.append(":");
            int zoneMin = this.zone % TypeConverter.HOUR_SECOND / TypeConverter.MINUTE_SECOND;
            if (zoneMin < 0) {
                zoneMin = -zoneMin;
            }
            if (0 <= zoneMin && zoneMin < 10) {
                ret.append("0");
            }
            ret.append(zoneMin);
        } else if (this.zone == 0) {
            ret.append(" +00:00");
        }
        return ret.toString();
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public int getZone() {
        return this.zone;
    }
}

