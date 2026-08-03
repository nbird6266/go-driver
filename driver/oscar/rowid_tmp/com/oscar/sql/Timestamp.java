/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.sql;

import com.oscar.util.TypeConverter;
import java.util.Calendar;

public class Timestamp
extends java.sql.Timestamp {
    private static final long serialVersionUID = 3642001919217596545L;
    private boolean BC = false;
    private int zone;
    private static Calendar cal = Calendar.getInstance();

    public static java.sql.Timestamp valueOf(String s) {
        int index;
        boolean isBC = false;
        if (s != null && s.length() >= 2) {
            if (s.substring(s.length() - 2, s.length()).equalsIgnoreCase("BC")) {
                isBC = true;
                s = s.substring(0, s.length() - 2);
            } else if (s.substring(s.length() - 2, s.length()).equalsIgnoreCase("AD")) {
                isBC = false;
                s = s.substring(0, s.length() - 2);
            }
        }
        if ((index = (s = s.trim()).indexOf(46)) >= 0 && s.substring(index + 1).length() >= 10) {
            s = s.substring(0, index + 1) + s.substring(index + 1, index + 11);
        }
        String tsStr = s;
        String tzStr = "0";
        index = s.indexOf(43);
        if (index >= 0) {
            tsStr = s.substring(0, index);
            tzStr = s.substring(index + 1, index + 3);
        }
        java.sql.Timestamp ts = java.sql.Timestamp.valueOf(tsStr);
        long millis = ts.getTime();
        if (index >= 0) {
            cal.setTimeInMillis(ts.getTime());
            int tz = Integer.parseInt(tzStr);
            cal.add(10, tz);
            millis = cal.getTimeInMillis();
        }
        Timestamp ret = new Timestamp(millis);
        ret.BC = isBC;
        return ret;
    }

    public String toString() {
        String nanosString;
        String yearString;
        int year = super.getYear() + 1900;
        int month = super.getMonth() + 1;
        int day = super.getDate();
        int hour = super.getHours();
        int minute = super.getMinutes();
        int second = super.getSeconds();
        String zeros = "000000000";
        String yearZeros = "0000";
        if (year < 1000) {
            yearString = "" + year;
            yearString = yearZeros.substring(0, 4 - yearString.length()) + yearString;
        } else {
            yearString = "" + year;
        }
        String monthString = month < 10 ? "0" + month : Integer.toString(month);
        String dayString = day < 10 ? "0" + day : Integer.toString(day);
        String hourString = hour < 10 ? "0" + hour : Integer.toString(hour);
        String minuteString = minute < 10 ? "0" + minute : Integer.toString(minute);
        String secondString = second < 10 ? "0" + second : Integer.toString(second);
        int nanos = this.getNanos();
        if (nanos == 0) {
            nanosString = "0";
        } else {
            nanosString = Integer.toString(nanos);
            nanosString = zeros.substring(0, 9 - nanosString.length()) + nanosString;
            char[] nanosChar = new char[nanosString.length()];
            nanosString.getChars(0, nanosString.length(), nanosChar, 0);
            int truncIndex = 8;
            while (nanosChar[truncIndex] == '0') {
                --truncIndex;
            }
            nanosString = new String(nanosChar, 0, truncIndex + 1);
        }
        StringBuffer timestampBuf = new StringBuffer();
        timestampBuf.append(yearString);
        timestampBuf.append("-");
        timestampBuf.append(monthString);
        timestampBuf.append("-");
        timestampBuf.append(dayString);
        timestampBuf.append(" ");
        timestampBuf.append(hourString);
        timestampBuf.append(":");
        timestampBuf.append(minuteString);
        timestampBuf.append(":");
        timestampBuf.append(secondString);
        if (!nanosString.equals("")) {
            timestampBuf.append(".");
            timestampBuf.append(nanosString);
        }
        if (this.zone != 0) {
            timestampBuf.append(" ");
            int zoneHour = this.zone / TypeConverter.HOUR_SECOND;
            if (-10 < zoneHour && zoneHour < 0) {
                timestampBuf.append("-0");
                timestampBuf.append(-zoneHour);
            } else if (zoneHour >= 0 && zoneHour < 10) {
                timestampBuf.append("+0").append(zoneHour);
            } else if (zoneHour >= 10) {
                timestampBuf.append("+").append(zoneHour);
            } else if (zoneHour <= -10) {
                timestampBuf.append(zoneHour);
            }
            timestampBuf.append(":");
            int zoneMin = this.zone % TypeConverter.HOUR_SECOND / TypeConverter.MINUTE_SECOND;
            if (zoneMin < 0) {
                zoneMin = -zoneMin;
            }
            if (0 <= zoneMin && zoneMin < 10) {
                timestampBuf.append("0");
            }
            timestampBuf.append(zoneMin);
        }
        timestampBuf.append(this.BC ? " BC" : "");
        return timestampBuf.toString();
    }

    public String stringValue() {
        String nanosString;
        String yearString;
        int year = super.getYear() + 1900;
        int month = super.getMonth() + 1;
        int day = super.getDate();
        int hour = super.getHours();
        int minute = super.getMinutes();
        int second = super.getSeconds();
        String zeros = "000000000";
        String yearZeros = "0000";
        if (year < 1000) {
            yearString = "" + year;
            yearString = yearZeros.substring(0, 4 - yearString.length()) + yearString;
        } else {
            yearString = "" + year;
        }
        String monthString = month < 10 ? "0" + month : Integer.toString(month);
        String dayString = day < 10 ? "0" + day : Integer.toString(day);
        String hourString = hour < 10 ? "0" + hour : Integer.toString(hour);
        String minuteString = minute < 10 ? "0" + minute : Integer.toString(minute);
        String secondString = second < 10 ? "0" + second : Integer.toString(second);
        int nanos = this.getNanos();
        if (nanos == 0) {
            nanosString = "";
        } else {
            nanosString = Integer.toString(nanos);
            nanosString = zeros.substring(0, 9 - nanosString.length()) + nanosString;
            char[] nanosChar = new char[nanosString.length()];
            nanosString.getChars(0, nanosString.length(), nanosChar, 0);
            int truncIndex = 8;
            while (nanosChar[truncIndex] == '0') {
                --truncIndex;
            }
            nanosString = new String(nanosChar, 0, truncIndex + 1);
        }
        StringBuffer timestampBuf = new StringBuffer();
        timestampBuf.append(yearString);
        timestampBuf.append("-");
        timestampBuf.append(monthString);
        timestampBuf.append("-");
        timestampBuf.append(dayString);
        timestampBuf.append(" ");
        timestampBuf.append(hourString);
        timestampBuf.append(":");
        timestampBuf.append(minuteString);
        timestampBuf.append(":");
        timestampBuf.append(secondString);
        if (!nanosString.equals("")) {
            timestampBuf.append(".");
            timestampBuf.append(nanosString);
        }
        if (this.zone != 0) {
            timestampBuf.append(" ");
            int zoneHour = this.zone / TypeConverter.HOUR_SECOND;
            if (-10 < zoneHour && zoneHour < 0) {
                timestampBuf.append("-0");
                timestampBuf.append(-zoneHour);
            } else if (zoneHour >= 0 && zoneHour < 10) {
                timestampBuf.append("+0").append(zoneHour);
            } else if (zoneHour >= 10) {
                timestampBuf.append("+").append(zoneHour);
            } else if (zoneHour <= -10) {
                timestampBuf.append(zoneHour);
            }
            timestampBuf.append(":");
            int zoneMin = this.zone % TypeConverter.HOUR_SECOND / TypeConverter.MINUTE_SECOND;
            if (zoneMin < 0) {
                zoneMin = -zoneMin;
            }
            if (0 <= zoneMin && zoneMin < 10) {
                timestampBuf.append("0");
            }
            timestampBuf.append(zoneMin);
        }
        timestampBuf.append(this.BC ? " BC" : "");
        return timestampBuf.toString();
    }

    public Timestamp(int year, int month, int date, int hour, int minute, int second, int nano) {
        super(year, month, date, hour, minute, second, nano);
    }

    public Timestamp(long time) {
        super(time);
    }

    public void setBC(boolean isBC) {
        this.BC = isBC;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public int fetchZone() {
        return this.zone;
    }
}

