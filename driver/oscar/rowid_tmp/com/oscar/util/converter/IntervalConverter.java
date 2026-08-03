/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

import com.oscar.util.TypeConverter;

public class IntervalConverter
extends TypeConverter {
    private static long YEAR_DAY_NS_MASK = Integer.MIN_VALUE;
    private static int MONTH_HOUR_MIN_SEC_MASK = 60;
    private static long MASK = 0x100000000L;

    public static String convertToIntervalDTS(byte[] value) {
        if (value == null) {
            return null;
        }
        if (value.length == 11) {
            long ns;
            int sec;
            int minut;
            int hour;
            StringBuffer sb = new StringBuffer();
            boolean flag = false;
            long day = IntervalConverter.converToYearOrDay(value, 0);
            if (day < 0L) {
                flag = true;
                day = -day;
            }
            if ((hour = IntervalConverter.converToMhms(value, 4)) < 0) {
                flag = true;
                hour = -hour;
            }
            if ((minut = IntervalConverter.converToMhms(value, 5)) < 0) {
                flag = true;
                minut = -minut;
            }
            if ((sec = IntervalConverter.converToMhms(value, 6)) < 0) {
                flag = true;
                sec = -sec;
            }
            if ((ns = IntervalConverter.converToYearOrDay(value, 7)) < 0L) {
                flag = true;
                ns = -ns;
            }
            if (flag) {
                sb.append("-");
            } else {
                sb.append("+");
            }
            for (int dayLen = IntervalConverter.getLenth(day); dayLen < 6; ++dayLen) {
                sb.append("0");
            }
            sb.append(day).append(" ");
            for (int hourLen = IntervalConverter.getLenth(hour); hourLen < 2; ++hourLen) {
                sb.append("0");
            }
            sb.append(hour).append(":");
            for (int minLen = IntervalConverter.getLenth(minut); minLen < 2; ++minLen) {
                sb.append("0");
            }
            sb.append(minut).append(":");
            for (int secLen = IntervalConverter.getLenth(sec); secLen < 2; ++secLen) {
                sb.append("0");
            }
            sb.append(sec);
            if (ns != 0L) {
                int i = 0;
                while (ns % 10L == 0L) {
                    ns /= 10L;
                    ++i;
                }
                int nsLen = IntervalConverter.getLenth(ns);
                i = 6 - nsLen - i;
                sb.append(".");
                for (int j = 0; j < i; ++j) {
                    sb.append("0");
                }
                sb.append(ns);
            }
            return sb.toString();
        }
        return null;
    }

    public static String convertToIntervalYTM(byte[] value) {
        if (value == null) {
            return null;
        }
        if (value.length == 5) {
            int month;
            StringBuffer sb = new StringBuffer();
            boolean flag = false;
            long year = IntervalConverter.converToYearOrDay(value, 0);
            if (year < 0L) {
                flag = true;
                year = -year;
            }
            if ((month = IntervalConverter.converToMhms(value, 4)) < 0) {
                flag = true;
                month = -month;
            }
            if (flag) {
                sb.append("-");
            } else {
                sb.append("+");
            }
            for (int yearLen = IntervalConverter.getLenth(year); yearLen < 9; ++yearLen) {
                sb.append("0");
            }
            sb.append(year).append("-");
            for (int monthLen = IntervalConverter.getLenth(month); monthLen < 2; ++monthLen) {
                sb.append("0");
            }
            sb.append(month);
            return sb.toString();
        }
        return null;
    }

    protected static int getLenth(long val) {
        if (val == 0L) {
            return 1;
        }
        int i = 0;
        while (val >= 1L) {
            val /= 10L;
            ++i;
        }
        return i;
    }

    protected static long converToYearOrDay(byte[] val, int off) {
        if (val == null || val.length < off + 4) {
            return 0L;
        }
        long i = IntervalConverter.getNumber(val[off]) << 24;
        i |= (long)(IntervalConverter.getNumber(val[off + 1]) << 16);
        i |= (long)(IntervalConverter.getNumber(val[off + 2]) << 8);
        if ((i |= (long)IntervalConverter.getNumber(val[off + 3])) < 0L) {
            i = MASK + i;
        }
        return i + YEAR_DAY_NS_MASK;
    }

    private static int getNumber(byte b) {
        return (b < 0 ? 256 + b : b) & 0xFF;
    }

    protected static int converToMhms(byte[] val, int off) {
        if (val == null || val.length < off + 1) {
            return 0;
        }
        int i = val[off] - MONTH_HOUR_MIN_SEC_MASK;
        return i;
    }

    public static void main(String[] args) {
        int ns = 123000;
        while (ns % 10 == 0) {
            ns /= 10;
        }
        System.out.println(ns);
    }
}

