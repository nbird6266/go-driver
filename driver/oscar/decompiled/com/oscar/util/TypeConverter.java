/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.core.BaseConnection;
import com.oscar.jdbc.OscarBlob;
import com.oscar.util.FDBigInt;
import com.oscar.util.OSCARbyte;
import com.oscar.util.OSQLException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

public class TypeConverter {
    public static final int SPLIT_LEN = 240;
    protected static TimeZone localTimeZone = TimeZone.getDefault();
    private static Calendar localCalendar = Calendar.getInstance(localTimeZone);
    private static Calendar localCalendarStr = Calendar.getInstance(localTimeZone);
    protected static int HOUR_MILLISECOND = 3600000;
    protected static int MINUTE_MILLISECOND = 60000;
    public static int HOUR_SECOND = 3600;
    public static int MINUTE_SECOND = 60;
    protected static int POSITIVE_INT_MASK = 192;
    protected static final boolean DEBUG = false;
    protected static final boolean SLOW_CONVERSIONS = true;
    protected static final int LNXSGNBT = 128;
    protected static final byte LNXDIGS = 20;
    protected static final byte LNXEXPBS = 64;
    protected static final int LNXEXPMX = 127;
    protected static final double[] factorTable = new double[]{1.0E254, 1.0E252, 1.0E250, 1.0E248, 1.0E246, 1.0E244, 1.0E242, 1.0E240, 1.0E238, 1.0E236, 1.0E234, 1.0E232, 1.0E230, 1.0E228, 1.0E226, 1.0E224, 1.0E222, 1.0E220, 1.0E218, 1.0E216, 1.0E214, 1.0E212, 1.0E210, 1.0E208, 1.0E206, 1.0E204, 1.0E202, 1.0E200, 1.0E198, 1.0E196, 1.0E194, 1.0E192, 1.0E190, 1.0E188, 1.0E186, 1.0E184, 1.0E182, 1.0E180, 1.0E178, 1.0E176, 1.0E174, 1.0E172, 1.0E170, 1.0E168, 1.0E166, 1.0E164, 1.0E162, 1.0E160, 1.0E158, 1.0E156, 1.0E154, 1.0E152, 1.0E150, 1.0E148, 1.0E146, 1.0E144, 1.0E142, 1.0E140, 1.0E138, 1.0E136, 1.0E134, 1.0E132, 1.0E130, 1.0E128, 1.0E126, 1.0E124, 1.0E122, 1.0E120, 1.0E118, 1.0E116, 1.0E114, 1.0E112, 1.0E110, 1.0E108, 1.0E106, 1.0E104, 1.0E102, 1.0E100, 1.0E98, 1.0E96, 1.0E94, 1.0E92, 1.0E90, 1.0E88, 1.0E86, 1.0E84, 1.0E82, 1.0E80, 1.0E78, 1.0E76, 1.0E74, 1.0E72, 1.0E70, 1.0E68, 1.0E66, 1.0E64, 1.0E62, 1.0E60, 1.0E58, 1.0E56, 1.0E54, 1.0E52, 1.0E50, 1.0E48, 1.0E46, 1.0E44, 1.0E42, 1.0E40, 1.0E38, 1.0E36, 1.0E34, 1.0E32, 1.0E30, 1.0E28, 1.0E26, 1.0E24, 1.0E22, 1.0E20, 1.0E18, 1.0E16, 1.0E14, 1.0E12, 1.0E10, 1.0E8, 1000000.0, 10000.0, 100.0, 1.0, 0.01, 1.0E-4, 1.0E-6, 1.0E-8, 1.0E-10, 1.0E-12, 1.0E-14, 1.0E-16, 1.0E-18, 1.0E-20, 1.0E-22, 1.0E-24, 1.0E-26, 1.0E-28, 1.0E-30, 1.0E-32, 1.0E-34, 1.0E-36, 1.0E-38, 1.0E-40, 1.0E-42, 1.0E-44, 1.0E-46, 1.0E-48, 1.0E-50, 1.0E-52, 1.0E-54, 1.0E-56, 1.0E-58, 1.0E-60, 1.0E-62, 1.0E-64, 1.0E-66, 1.0E-68, 1.0E-70, 1.0E-72, 1.0E-74, 1.0E-76, 1.0E-78, 1.0E-80, 1.0E-82, 1.0E-84, 1.0E-86, 1.0E-88, 1.0E-90, 1.0E-92, 1.0E-94, 1.0E-96, 1.0E-98, 1.0E-100, 1.0E-102, 1.0E-104, 1.0E-106, 1.0E-108, 1.0E-110, 1.0E-112, 1.0E-114, 1.0E-116, 1.0E-118, 1.0E-120, 1.0E-122, 1.0E-124, 1.0E-126, 1.0E-128, 1.0E-130, 1.0E-132, 1.0E-134, 1.0E-136, 1.0E-138, 1.0E-140, 1.0E-142, 1.0E-144, 1.0E-146, 1.0E-148, 1.0E-150, 1.0E-152, 1.0E-154, 1.0E-156, 1.0E-158, 1.0E-160, 1.0E-162, 1.0E-164, 1.0E-166, 1.0E-168, 1.0E-170, 1.0E-172, 1.0E-174, 1.0E-176, 1.0E-178, 1.0E-180, 1.0E-182, 1.0E-184, 1.0E-186, 1.0E-188, 1.0E-190, 1.0E-192, 1.0E-194, 1.0E-196, 1.0E-198, 1.0E-200, 1.0E-202, 1.0E-204, 1.0E-206, 1.0E-208, 1.0E-210, 1.0E-212, 1.0E-214, 1.0E-216, 1.0E-218, 1.0E-220, 1.0E-222, 1.0E-224, 1.0E-226, 1.0E-228, 1.0E-230, 1.0E-232, 1.0E-234, 1.0E-236, 1.0E-238, 1.0E-240, 1.0E-242, 1.0E-244, 1.0E-246, 1.0E-248, 1.0E-250, 1.0E-252, 1.0E-254};
    protected static final int MANTISSA_SIZE = 53;
    protected static final int expShift = 52;
    protected static final long fractHOB = 0x10000000000000L;
    protected static final long fractMask = 0xFFFFFFFFFFFFFL;
    protected static final int expBias = 1023;
    protected static final int maxSmallBinExp = 62;
    protected static final int minSmallBinExp = -21;
    protected static final long expOne = 0x3FF0000000000000L;
    protected static final long highbyte = -72057594037927936L;
    protected static final long highbit = Long.MIN_VALUE;
    protected static final long lowbytes = 0xFFFFFFFFFFFFFFL;
    protected static final int[] small5pow = new int[]{1, 5, 25, 125, 625, 3125, 15625, 78125, 390625, 1953125, 9765625, 48828125, 244140625, 1220703125};
    protected static final long[] long5pow = new long[]{1L, 5L, 25L, 125L, 625L, 3125L, 15625L, 78125L, 390625L, 1953125L, 9765625L, 48828125L, 244140625L, 1220703125L, 6103515625L, 30517578125L, 152587890625L, 762939453125L, 3814697265625L, 19073486328125L, 95367431640625L, 476837158203125L, 2384185791015625L, 11920928955078125L, 59604644775390625L, 298023223876953125L, 1490116119384765625L};
    protected static final int[] n5bits = new int[]{0, 3, 5, 7, 10, 12, 14, 17, 19, 21, 24, 26, 28, 31, 33, 35, 38, 40, 42, 45, 47, 49, 52, 54, 56, 59, 61};
    protected static FDBigInt[] b5p;
    public static final long DATE_POSITIVE_INFINITY = Long.MAX_VALUE;
    public static final long DATE_NEGATIVE_INFINITY = Long.MIN_VALUE;
    public static final String DATE_POSITIVE_INFINITY_TAG = "infinity";
    public static final String DATE_NEGATIVE_INFINITY_TAG = "-infinity";

    public static byte toByte(String value, int fromType) throws SQLException {
        if (value == null) {
            return 0;
        }
        switch (fromType) {
            case -6: {
                return TypeConverter.convertToByte(value);
            }
            case 5: {
                short shortResult = TypeConverter.convertToShort(value);
                if (shortResult > 127 || shortResult < -128) {
                    throw new OSQLException("OSCAR-00701", "88888", 701);
                }
                return (byte)shortResult;
            }
            case 4: {
                int intResult = TypeConverter.convertToInt(value);
                if (intResult > 127 || intResult < -128) {
                    throw new OSQLException("OSCAR-00701", "88888", 701);
                }
                return (byte)intResult;
            }
            case -5: {
                long longResult = TypeConverter.convertToLong(value);
                if (longResult > 127L || longResult < -128L) {
                    throw new OSQLException("OSCAR-00701", "88888", 701);
                }
                return (byte)longResult;
            }
            case 7: {
                float floatResult = TypeConverter.convertToFloat(value);
                if (floatResult > 127.0f || floatResult < -128.0f) {
                    throw new OSQLException("OSCAR-00701", "88888", 701);
                }
                return (byte)floatResult;
            }
            case 6: 
            case 8: {
                double doubleResult = TypeConverter.convertToDouble(value);
                if (doubleResult > 127.0 || doubleResult < -128.0) {
                    throw new OSQLException("OSCAR-00701", "88888", 701);
                }
                return (byte)doubleResult;
            }
            case 2: 
            case 3: {
                BigDecimal decimalResult = TypeConverter.convertToDecimal(value);
                double tdouble = decimalResult.doubleValue();
                if (tdouble > 127.0 || tdouble < -128.0) {
                    throw new OSQLException("OSCAR-00701", "88888", 701);
                }
                return (byte)tdouble;
            }
            case 16: {
                boolean booleanResult = TypeConverter.convertToBoolean(value);
                if (booleanResult) {
                    return 1;
                }
                return 0;
            }
            case 1: {
                for (int i = value.length() - 1; i >= 0; --i) {
                    if (value.charAt(i) == ' ') continue;
                    value = value.substring(0, i + 1);
                    break;
                }
            }
            case -7: 
            case -1: 
            case 12: {
                return TypeConverter.convertToByte(value);
            }
        }
        throw new OSQLException("OSCAR-00701", "88888", 701);
    }

    public static short toShort(String value, int fromType) throws SQLException {
        if (value == null) {
            return 0;
        }
        switch (fromType) {
            case -6: {
                return TypeConverter.convertToByte(value);
            }
            case 5: {
                return TypeConverter.convertToShort(value);
            }
            case 4: {
                int intResult = TypeConverter.convertToInt(value);
                if (intResult > Short.MAX_VALUE || intResult < Short.MIN_VALUE) {
                    throw new OSQLException("OSCAR-00702", "88888", 702);
                }
                return (short)intResult;
            }
            case -5: {
                long longResult = TypeConverter.convertToLong(value);
                if (longResult > 32767L || longResult < -32768L) {
                    throw new OSQLException("OSCAR-00702", "88888", 702);
                }
                return (short)longResult;
            }
            case 7: {
                float floatResult = TypeConverter.convertToFloat(value);
                if (floatResult > 32767.0f || floatResult < -32768.0f) {
                    throw new OSQLException("OSCAR-00702", "88888", 702);
                }
                return (short)floatResult;
            }
            case 6: 
            case 8: {
                double doubleResult = TypeConverter.convertToDouble(value);
                if (doubleResult > 32767.0 || doubleResult < -32768.0) {
                    throw new OSQLException("OSCAR-00702", "88888", 702);
                }
                return (short)doubleResult;
            }
            case 2: 
            case 3: {
                BigDecimal decimalResult = TypeConverter.convertToDecimal(value);
                double tdouble = decimalResult.doubleValue();
                if (tdouble > 32767.0 || tdouble < -32768.0) {
                    throw new OSQLException("OSCAR-00702", "88888", 702);
                }
                return (short)tdouble;
            }
            case -7: 
            case 16: {
                boolean booleanResult = TypeConverter.convertToBoolean(value);
                if (booleanResult) {
                    return 1;
                }
                return 0;
            }
            case 1: {
                for (int i = value.length() - 1; i >= 0; --i) {
                    if (value.charAt(i) == ' ') continue;
                    value = value.substring(0, i + 1);
                    break;
                }
            }
            case -1: 
            case 12: {
                return TypeConverter.convertToShort(value);
            }
        }
        throw new OSQLException("OSCAR-00702", "88888", 702);
    }

    public static int toInt(String value, int fromType) throws SQLException {
        if (value == null) {
            return 0;
        }
        switch (fromType) {
            case -6: {
                return TypeConverter.convertToByte(value);
            }
            case 5: {
                return TypeConverter.convertToShort(value);
            }
            case 4: {
                return TypeConverter.convertToInt(value);
            }
            case -5: {
                long longResult = TypeConverter.convertToLong(value);
                if (longResult > Integer.MAX_VALUE || longResult < Integer.MIN_VALUE) {
                    throw new OSQLException("OSCAR-00703", "88888", 703);
                }
                return (int)longResult;
            }
            case 7: {
                float floatResult = TypeConverter.convertToFloat(value);
                if (floatResult > 2.14748365E9f || floatResult < -2.14748365E9f) {
                    throw new OSQLException("OSCAR-00703", "88888", 703);
                }
                return (int)floatResult;
            }
            case 6: 
            case 8: {
                double doubleResult = TypeConverter.convertToDouble(value);
                if (doubleResult > 2.147483647E9 || doubleResult < -2.147483648E9) {
                    throw new OSQLException("OSCAR-00703", "88888", 703);
                }
                return (int)doubleResult;
            }
            case 2: 
            case 3: {
                BigDecimal decimalResult = TypeConverter.convertToDecimal(value);
                double tdouble = decimalResult.doubleValue();
                if (tdouble > 2.147483647E9 || tdouble < -2.147483648E9) {
                    throw new OSQLException("OSCAR-00703", "88888", 703);
                }
                return (int)tdouble;
            }
            case -7: 
            case 16: {
                boolean booleanResult = TypeConverter.convertToBoolean(value);
                if (booleanResult) {
                    return 1;
                }
                return 0;
            }
            case 1: {
                for (int i = value.length() - 1; i >= 0; --i) {
                    if (value.charAt(i) == ' ') continue;
                    value = value.substring(0, i + 1);
                    break;
                }
            }
            case -1: 
            case 12: {
                return TypeConverter.convertToInt(value);
            }
        }
        throw new OSQLException("OSCAR-00703", "88888", 703);
    }

    public static long toLong(String value, int fromType) throws SQLException {
        if (value == null) {
            return 0L;
        }
        switch (fromType) {
            case -6: {
                return TypeConverter.convertToByte(value);
            }
            case 5: {
                return TypeConverter.convertToShort(value);
            }
            case 4: {
                return TypeConverter.convertToInt(value);
            }
            case -5: {
                return TypeConverter.convertToLong(value);
            }
            case 7: {
                float floatResult = TypeConverter.convertToFloat(value);
                if (floatResult > 9.223372E18f || floatResult < -9.223372E18f) {
                    throw new OSQLException("OSCAR-00704", "88888", 704);
                }
                return (long)floatResult;
            }
            case 6: 
            case 8: {
                double doubleResult = TypeConverter.convertToDouble(value);
                if (doubleResult > 9.223372036854776E18 || doubleResult < -9.223372036854776E18) {
                    throw new OSQLException("OSCAR-00704", "88888", 704);
                }
                return (long)doubleResult;
            }
            case 2: 
            case 3: {
                BigDecimal decimalResult = TypeConverter.convertToDecimal(value);
                if (decimalResult.compareTo(new BigDecimal(Long.toString(Long.MAX_VALUE))) > 0 || decimalResult.compareTo(new BigDecimal(Long.toString(Long.MIN_VALUE))) < 0) {
                    throw new OSQLException("OSCAR-00704", "88888", 704);
                }
                return decimalResult.longValue();
            }
            case -7: 
            case 16: {
                boolean booleanResult = TypeConverter.convertToBoolean(value);
                if (booleanResult) {
                    return 1L;
                }
                return 0L;
            }
            case 1: {
                for (int i = value.length() - 1; i >= 0; --i) {
                    if (value.charAt(i) == ' ') continue;
                    value = value.substring(0, i + 1);
                    break;
                }
            }
            case -1: 
            case 12: {
                return TypeConverter.convertToLong(value);
            }
        }
        throw new OSQLException("OSCAR-00704", "88888", 704);
    }

    public static float toFloat(String value, int fromType) throws SQLException {
        if (value == null) {
            return 0.0f;
        }
        switch (fromType) {
            case -6: {
                return TypeConverter.convertToByte(value);
            }
            case 5: {
                return TypeConverter.convertToShort(value);
            }
            case 4: {
                return TypeConverter.convertToInt(value);
            }
            case -5: {
                return TypeConverter.convertToLong(value);
            }
            case 7: {
                return TypeConverter.convertToFloat(value);
            }
            case 6: 
            case 8: {
                double doubleResult = TypeConverter.convertToDouble(value);
                if (doubleResult > 3.4028234663852886E38 || doubleResult < -3.4028234663852886E38) {
                    throw new OSQLException("OSCAR-00705", "88888", 705);
                }
                return (float)doubleResult;
            }
            case 2: 
            case 3: {
                BigDecimal decimalResult = TypeConverter.convertToDecimal(value);
                double tdouble = decimalResult.doubleValue();
                if (tdouble > 3.4028234663852886E38 || tdouble < -3.4028234663852886E38) {
                    throw new OSQLException("OSCAR-00705", "88888", 705);
                }
                return (float)tdouble;
            }
            case -7: 
            case 16: {
                boolean booleanResult = TypeConverter.convertToBoolean(value);
                if (booleanResult) {
                    return 1.0f;
                }
                return 0.0f;
            }
            case 1: {
                for (int i = value.length() - 1; i >= 0; --i) {
                    if (value.charAt(i) == ' ') continue;
                    value = value.substring(0, i + 1);
                    break;
                }
                return TypeConverter.convertToFloat(value);
            }
            case -1: 
            case 12: {
                return TypeConverter.convertToFloat(value);
            }
        }
        throw new OSQLException("OSCAR-00705", "88888", 705);
    }

    public static double toDouble(String value, int fromType) throws SQLException {
        if (value == null) {
            return 0.0;
        }
        switch (fromType) {
            case -6: {
                return TypeConverter.convertToByte(value);
            }
            case 5: {
                return TypeConverter.convertToShort(value);
            }
            case 4: {
                return TypeConverter.convertToInt(value);
            }
            case -5: {
                return TypeConverter.convertToLong(value);
            }
            case 7: {
                return TypeConverter.convertToFloat(value);
            }
            case 6: 
            case 8: {
                return TypeConverter.convertToDouble(value);
            }
            case 2: 
            case 3: {
                double tdouble;
                if (value.equals("NaN")) {
                    tdouble = new Double(value);
                } else {
                    BigDecimal decimalResult = TypeConverter.convertToDecimal(value);
                    tdouble = decimalResult.doubleValue();
                    if (tdouble > Double.MAX_VALUE || tdouble < -1.7976931348623157E308) {
                        throw new OSQLException("OSCAR-00706", "88888", 706);
                    }
                }
                return tdouble;
            }
            case -7: 
            case 16: {
                boolean booleanResult = TypeConverter.convertToBoolean(value);
                if (booleanResult) {
                    return 1.0;
                }
                return 0.0;
            }
            case 1: {
                for (int i = value.length() - 1; i >= 0; --i) {
                    if (value.charAt(i) == ' ') continue;
                    value = value.substring(0, i + 1);
                    break;
                }
            }
            case -1: 
            case 12: {
                return TypeConverter.convertToDouble(value);
            }
        }
        throw new OSQLException("OSCAR-00706", "88888", 706);
    }

    public static BigDecimal toBigDecimal(String value, int fromType) throws SQLException {
        if (value == null) {
            return null;
        }
        switch (fromType) {
            case -6: {
                return BigDecimal.valueOf(TypeConverter.convertToByte(value));
            }
            case 5: {
                return BigDecimal.valueOf(TypeConverter.convertToShort(value));
            }
            case 4: {
                return BigDecimal.valueOf(TypeConverter.convertToInt(value));
            }
            case -5: {
                return BigDecimal.valueOf(TypeConverter.convertToLong(value));
            }
            case 7: {
                return new BigDecimal(TypeConverter.convertToFloat(value));
            }
            case 6: 
            case 8: {
                return TypeConverter.convertToDecimal(value);
            }
            case 2: 
            case 3: {
                return TypeConverter.convertToDecimal(value);
            }
            case -7: 
            case 16: {
                boolean booleanResult = TypeConverter.convertToBoolean(value);
                if (booleanResult) {
                    return BigDecimal.valueOf(1L);
                }
                return BigDecimal.valueOf(0L);
            }
            case 1: {
                int index = value.indexOf(32);
                if (index != -1) {
                    for (int i = index + 1; i < value.length(); ++i) {
                        if (value.charAt(i) == ' ') continue;
                        throw new OSQLException("OSCAR-00707", "88888", 707);
                    }
                    value = value.substring(0, index);
                }
                return TypeConverter.convertToDecimal(value);
            }
            case -1: 
            case 12: {
                return TypeConverter.convertToDecimal(value);
            }
        }
        throw new OSQLException("OSCAR-00707", "88888", 707);
    }

    public static BigDecimal toBigDecimal(String value, int fromType, int scale) throws SQLException {
        if (value == null) {
            return null;
        }
        switch (fromType) {
            case -6: {
                return BigDecimal.valueOf(TypeConverter.convertToByte(value)).setScale(scale);
            }
            case 5: {
                return BigDecimal.valueOf(TypeConverter.convertToShort(value)).setScale(scale);
            }
            case 4: {
                return BigDecimal.valueOf(TypeConverter.convertToInt(value)).setScale(scale);
            }
            case -5: {
                return BigDecimal.valueOf(TypeConverter.convertToLong(value)).setScale(scale);
            }
            case 7: {
                return new BigDecimal(TypeConverter.convertToFloat(value)).setScale(scale);
            }
            case 6: 
            case 8: {
                return new BigDecimal(TypeConverter.convertToDouble(value)).setScale(scale);
            }
            case 2: 
            case 3: {
                return TypeConverter.convertToDecimal(value, scale);
            }
            case -7: 
            case 16: {
                boolean booleanResult = TypeConverter.convertToBoolean(value);
                if (booleanResult) {
                    return BigDecimal.valueOf(1L).setScale(scale);
                }
                return BigDecimal.valueOf(0L).setScale(scale);
            }
            case 1: {
                int index = value.indexOf(32);
                if (index != -1) {
                    for (int i = index + 1; i < value.length(); ++i) {
                        if (value.charAt(i) == ' ') continue;
                        throw new OSQLException("OSCAR-00707", "88888", 707);
                    }
                    value = value.substring(0, index);
                }
                return TypeConverter.convertToDecimal(value, scale);
            }
            case -1: 
            case 12: {
                return TypeConverter.convertToDecimal(value, scale);
            }
        }
        throw new OSQLException("OSCAR-00707", "88888", 707);
    }

    public static boolean toBoolean(String value, int fromType) throws SQLException {
        if (value == null) {
            return false;
        }
        switch (fromType) {
            case -6: {
                return TypeConverter.convertToByte(value) != 0;
            }
            case 5: {
                return TypeConverter.convertToShort(value) != 0;
            }
            case 4: {
                return TypeConverter.convertToInt(value) != 0;
            }
            case -5: {
                return TypeConverter.convertToLong(value) != 0L;
            }
            case 7: {
                float tfloat = TypeConverter.convertToFloat(value);
                return tfloat >= 1.0E-10f || tfloat <= -1.0E-10f;
            }
            case 6: 
            case 8: {
                double tdouble = TypeConverter.convertToDouble(value);
                return tdouble >= 1.0E-10 || tdouble <= -1.0E-10;
            }
            case 2: 
            case 3: {
                BigDecimal decimalResult = TypeConverter.convertToDecimal(value, 15);
                double tdouble2 = decimalResult.doubleValue();
                return tdouble2 >= 1.0E-10 || tdouble2 <= -1.0E-10;
            }
            case -7: 
            case 16: {
                return TypeConverter.convertToBoolean(value);
            }
            case 1: {
                int index = value.indexOf(32);
                if (index != -1) {
                    for (int i = index + 1; i < value.length(); ++i) {
                        if (value.charAt(i) == ' ') continue;
                        throw new OSQLException("OSCAR-00708", "88888", 708);
                    }
                    value = value.substring(0, index);
                }
                return TypeConverter.convertToBoolean(value);
            }
            case -1: 
            case 12: {
                return TypeConverter.convertToBoolean(value);
            }
        }
        throw new OSQLException("OSCAR-00708", "88888", 708);
    }

    public static byte[] toBytes(BaseConnection connection, byte[] value, int fromType) throws SQLException {
        if (value == null) {
            return null;
        }
        switch (fromType) {
            case -4: 
            case -3: 
            case -2: {
                return OSCARbyte.toBytes(value);
            }
            case -7: 
            case -6: 
            case -5: 
            case -1: 
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 8: 
            case 12: 
            case 92: 
            case 93: {
                return value;
            }
            case 2004: {
                OscarBlob blob = connection.getBlobInstance(new String(value));
                return blob.getBytes(1L, (int)blob.length());
            }
        }
        throw new OSQLException("OSCAR-00709", "88888", 709);
    }

    public static Date toDate(String value, int fromType) throws SQLException {
        if (value == null) {
            return null;
        }
        Date infinityDate = TypeConverter.toInfinityDate(value);
        if (infinityDate != null) {
            return infinityDate;
        }
        switch (fromType) {
            case 1: {
                for (int i = value.length() - 1; i >= 0; --i) {
                    if (value.charAt(i) == ' ') continue;
                    value = value.substring(0, i + 1);
                    break;
                }
            }
            case -1: 
            case 12: {
                if (value.indexOf("-") != -1) {
                    if (value.indexOf(":") != -1) {
                        return new Date(TypeConverter.convertToTimestamp(value).getTime());
                    }
                    return TypeConverter.convertToDate(value);
                }
                throw new OSQLException("OSCAR-00710", "88888", 710);
            }
            case 91: {
                return TypeConverter.convertToDate(value);
            }
            case 93: {
                return new Date(TypeConverter.convertToTimestamp(value).getTime());
            }
        }
        throw new OSQLException("OSCAR-00710", "88888", 710);
    }

    private static Date toInfinityDate(String value) {
        if (DATE_POSITIVE_INFINITY_TAG.equalsIgnoreCase(value)) {
            return new com.oscar.sql.Date(Long.MAX_VALUE);
        }
        if (DATE_NEGATIVE_INFINITY_TAG.equalsIgnoreCase(value)) {
            return new com.oscar.sql.Date(Long.MIN_VALUE);
        }
        return null;
    }

    public static Date toDate(String value, int fromType, Calendar userCal, Calendar systemCal) throws SQLException {
        if (value == null) {
            return null;
        }
        Date infinityDate = TypeConverter.toInfinityDate(value);
        if (infinityDate != null) {
            return infinityDate;
        }
        switch (fromType) {
            case 1: {
                for (int i = value.length() - 1; i >= 0; --i) {
                    if (value.charAt(i) == ' ') continue;
                    value = value.substring(0, i + 1);
                    break;
                }
            }
            case -1: 
            case 12: {
                if (value.indexOf("-") != -1) {
                    if (value.indexOf(":") != -1) {
                        return new Date(TypeConverter.convertToTimestamp(value, userCal, systemCal).getTime());
                    }
                    return TypeConverter.convertToDate(value);
                }
                throw new OSQLException("OSCAR-00710", "88888", 710);
            }
            case 91: {
                return TypeConverter.convertToDate(value);
            }
            case 93: {
                return new Date(TypeConverter.convertToTimestamp(value, userCal, systemCal).getTime());
            }
        }
        throw new OSQLException("OSCAR-00710", "88888", 710);
    }

    public static Time toTime(String value, int fromType) throws SQLException {
        if (value == null) {
            return null;
        }
        switch (fromType) {
            case 1: {
                for (int i = value.length() - 1; i >= 0; --i) {
                    if (value.charAt(i) == ' ') continue;
                    value = value.substring(0, i + 1);
                    break;
                }
            }
            case -1: 
            case 12: {
                if (value.indexOf(":") != -1) {
                    if (value.indexOf("-") != -1) {
                        return new Time(TypeConverter.convertToTimestamp(value).getTime());
                    }
                    return TypeConverter.convertToTime(value);
                }
                throw new OSQLException("OSCAR-00711", "88888", 711);
            }
            case 92: {
                return TypeConverter.convertToTime(value);
            }
            case 93: {
                return new Time(TypeConverter.convertToTimestamp(value).getTime());
            }
        }
        throw new OSQLException("OSCAR-00711", "88888", 711);
    }

    public static Time toTime(String value, int fromType, Calendar userCal, Calendar systemCal) throws SQLException {
        if (value == null) {
            return null;
        }
        switch (fromType) {
            case 1: {
                for (int i = value.length() - 1; i >= 0; --i) {
                    if (value.charAt(i) == ' ') continue;
                    value = value.substring(0, i + 1);
                    break;
                }
            }
            case -1: 
            case 12: {
                if (value.indexOf(":") != -1) {
                    if (value.indexOf("-") != -1) {
                        return new Time(TypeConverter.convertToTimestamp(value, userCal, systemCal).getTime());
                    }
                    return TypeConverter.convertToTime(value, userCal, systemCal);
                }
                throw new OSQLException("OSCAR-00711", "88888", 711);
            }
            case 92: {
                return TypeConverter.convertToTime(value, userCal, systemCal);
            }
            case 93: {
                return new Time(TypeConverter.convertToTimestamp(value, userCal, systemCal).getTime());
            }
        }
        throw new OSQLException("OSCAR-00711", "88888", 711);
    }

    public static Timestamp toTimestamp(String value, int fromType) throws SQLException {
        if (value == null) {
            return null;
        }
        Timestamp infinityTimestamp = TypeConverter.toInfinityTimestamp(value);
        if (infinityTimestamp != null) {
            return infinityTimestamp;
        }
        switch (fromType) {
            case 1: {
                for (int i = value.length() - 1; i >= 0; --i) {
                    if (value.charAt(i) == ' ') continue;
                    value = value.substring(0, i + 1);
                    break;
                }
            }
            case -1: 
            case 12: {
                if (value.indexOf(":") != -1) {
                    if (value.indexOf("-") != -1) {
                        return TypeConverter.convertToTimestamp(value);
                    }
                    return new Timestamp(TypeConverter.convertToTime(value).getTime());
                }
                if (value.indexOf("-") != -1) {
                    return new Timestamp(TypeConverter.convertToDate(value).getTime());
                }
                throw new OSQLException("OSCAR-00712", "88888", 712);
            }
            case 91: {
                return new Timestamp(TypeConverter.convertToDate(value).getTime());
            }
            case 92: {
                return new Timestamp(TypeConverter.convertToTime(value).getTime());
            }
            case 93: {
                return new Timestamp(TypeConverter.convertToTimestamp(value).getTime());
            }
        }
        throw new OSQLException("OSCAR-00712", "88888", 712);
    }

    private static Timestamp toInfinityTimestamp(String value) {
        if (DATE_POSITIVE_INFINITY_TAG.equalsIgnoreCase(value)) {
            return new com.oscar.sql.Timestamp(Long.MAX_VALUE);
        }
        if (DATE_NEGATIVE_INFINITY_TAG.equalsIgnoreCase(value)) {
            return new com.oscar.sql.Timestamp(Long.MIN_VALUE);
        }
        return null;
    }

    public static Timestamp toTimestamp(String value, int fromType, Calendar userCal, Calendar systemCal) throws SQLException {
        if (value == null) {
            return null;
        }
        Timestamp infinityTimestamp = TypeConverter.toInfinityTimestamp(value);
        if (infinityTimestamp != null) {
            return infinityTimestamp;
        }
        switch (fromType) {
            case 1: {
                for (int i = value.length() - 1; i >= 0; --i) {
                    if (value.charAt(i) == ' ') continue;
                    value = value.substring(0, i + 1);
                    break;
                }
            }
            case -1: 
            case 12: {
                if (value.indexOf(":") != -1) {
                    if (value.indexOf("-") != -1) {
                        return TypeConverter.convertToTimestamp(value, userCal, systemCal);
                    }
                    return new Timestamp(TypeConverter.convertToTime(value, userCal, systemCal).getTime());
                }
                if (value.indexOf("-") != -1) {
                    return new Timestamp(TypeConverter.convertToDate(value).getTime());
                }
                throw new OSQLException("OSCAR-00712", "88888", 712);
            }
            case 91: {
                return new Timestamp(TypeConverter.convertToDate(value).getTime());
            }
            case 92: {
                return new Timestamp(TypeConverter.convertToTime(value, userCal, systemCal).getTime());
            }
            case 93: {
                return TypeConverter.convertToTimestamp(value, userCal, systemCal);
            }
        }
        throw new OSQLException("OSCAR-00712", "88888", 712);
    }

    public static boolean convertToBoolean(String s) throws SQLException {
        if (s == null) {
            return false;
        }
        if ((s = s.trim()).length() == 1) {
            return s.charAt(0) == '1' || s.charAt(0) == 't' || s.charAt(0) == 'y' || s.charAt(0) == 'T' || s.charAt(0) == 'Y';
        }
        if (s.length() > 1) {
            if (s.equalsIgnoreCase("true")) {
                return true;
            }
            if (s.equalsIgnoreCase("false")) {
                return false;
            }
            for (int i = 0; i < s.length(); ++i) {
                if (!(i == 0 ? s.charAt(0) != '-' && !Character.isDigit(s.charAt(i)) : !Character.isDigit(s.charAt(i)))) continue;
                return false;
            }
            return Double.parseDouble(s) >= 1.0E-10 || Double.parseDouble(s) <= -1.0E-10;
        }
        return false;
    }

    public static byte convertToByte(String s) throws SQLException {
        if (s == null) {
            return 0;
        }
        try {
            return Byte.parseByte(s);
        }
        catch (NumberFormatException nfe) {
            throw new OSQLException("OSCAR-00701", "88888", 701);
        }
    }

    public static short convertToShort(String s) throws SQLException {
        if (s == null) {
            return 0;
        }
        try {
            return Short.parseShort(s);
        }
        catch (NumberFormatException nfe) {
            throw new OSQLException("OSCAR-00702", "88888", 702);
        }
    }

    public static int convertToInt(String s) throws SQLException {
        if (s == null) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException nfe) {
            throw new OSQLException("OSCAR-00703", "88888", 703);
        }
    }

    public static long convertToLong(String s) throws SQLException {
        if (s == null) {
            return 0L;
        }
        try {
            return Long.parseLong(s);
        }
        catch (NumberFormatException nfe) {
            throw new OSQLException("OSCAR-00704", "88888", 704);
        }
    }

    public static float convertToFloat(String s) throws SQLException {
        if (s == null) {
            return 0.0f;
        }
        try {
            return Float.parseFloat(s);
        }
        catch (NumberFormatException nfe) {
            throw new OSQLException("OSCAR-00705", "88888", 705);
        }
    }

    public static double convertToDouble(String s) throws SQLException {
        if (s == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(s);
        }
        catch (NumberFormatException nfe) {
            throw new OSQLException("OSCAR-00706", "88888", 706);
        }
    }

    public static BigDecimal convertToDecimal(String s) throws SQLException {
        if (s == null) {
            return null;
        }
        try {
            return new BigDecimal(s);
        }
        catch (NumberFormatException e) {
            throw new OSQLException("OSCAR-00707", "88888", 707);
        }
    }

    public static BigDecimal convertToDecimal(String s, int scale) throws SQLException {
        if (s == null) {
            return null;
        }
        BigDecimal value = null;
        try {
            value = new BigDecimal(s);
        }
        catch (NumberFormatException e) {
            throw new OSQLException("OSCAR-00707", "88888", 707);
        }
        if (scale <= -1) {
            return value;
        }
        try {
            return value.setScale(scale, 4);
        }
        catch (ArithmeticException e) {
            throw new OSQLException("OSCAR-00707", "88888", 707);
        }
    }

    public static Date convertToDate(String s) throws SQLException {
        if (s == null) {
            return null;
        }
        try {
            return com.oscar.sql.Date.valueOf(s);
        }
        catch (Exception e) {
            try {
                return new Date(Long.valueOf(s));
            }
            catch (Exception e2) {
                throw new OSQLException("OSCAR-00710", "88888", 710, e2);
            }
        }
    }

    public static synchronized Time convertToTime(String s) throws SQLException {
        if (s == null) {
            return null;
        }
        try {
            s = s.trim();
            if (s.length() == 8) {
                return Time.valueOf(s);
            }
            long millis = 0L;
            String tzStr = "0";
            int tz = 0;
            int plusIndex = s.indexOf(43);
            int minusIndex = s.indexOf(45);
            if (plusIndex >= 0) {
                tzStr = s.substring(plusIndex + 1, plusIndex + 3);
                s = s.substring(0, plusIndex);
            }
            if (minusIndex >= 0) {
                tzStr = s.substring(minusIndex + 1, minusIndex + 3);
                s = s.substring(0, minusIndex);
            }
            Time l_time = Time.valueOf(s.substring(0, 8));
            int l_millis = 0;
            int index = s.indexOf(46);
            if (index > 0) {
                String l_strMillis = s.substring(index + 1);
                if (l_strMillis.length() > 3) {
                    l_strMillis = l_strMillis.substring(0, 3);
                }
                if ((l_millis = Integer.parseInt(l_strMillis)) < 10) {
                    l_millis *= 100;
                } else if (l_millis < 100) {
                    l_millis *= 10;
                }
            }
            millis = l_time.getTime() + (long)l_millis;
            if (plusIndex >= 0) {
                localCalendar.setTimeInMillis(millis);
                tz = Integer.parseInt("-" + tzStr);
                localCalendar.add(10, tz);
                millis = localCalendar.getTimeInMillis();
            }
            if (minusIndex >= 0) {
                localCalendar.setTimeInMillis(millis);
                tz = Integer.parseInt(tzStr);
                localCalendar.add(10, tz);
                millis = localCalendar.getTimeInMillis();
            }
            return new Time(millis);
        }
        catch (Exception e) {
            throw new OSQLException("OSCAR-00711", "88888", 711, e);
        }
    }

    public static Time convertToTime(String s, Calendar userCal, Calendar systemCal) throws SQLException {
        if (s == null) {
            return null;
        }
        int userOffset = userCal.get(15) + userCal.get(16);
        int systemOffset = systemCal.get(15) + systemCal.get(16);
        int offset = userOffset - systemOffset;
        try {
            int l_millis;
            s = s.trim();
            if (s.length() == 8) {
                Time old = Time.valueOf(s);
                return new Time(old.getTime() + (long)offset);
            }
            Time l_time = Time.valueOf(s.substring(0, 8));
            String l_strMillis = s.substring(9);
            if (l_strMillis.length() > 3) {
                l_strMillis = l_strMillis.substring(0, 3);
            }
            if ((l_millis = Integer.parseInt(l_strMillis)) < 10) {
                l_millis *= 100;
            } else if (l_millis < 100) {
                l_millis *= 10;
            }
            return new Time(l_time.getTime() + (long)offset + (long)l_millis);
        }
        catch (Exception e) {
            throw new OSQLException("OSCAR-00711", "88888", 711, e);
        }
    }

    public static Timestamp convertToTimestamp(String s) throws SQLException {
        if (s == null) {
            return null;
        }
        try {
            return com.oscar.sql.Timestamp.valueOf(s);
        }
        catch (Exception e) {
            try {
                return new Timestamp(Long.valueOf(s));
            }
            catch (Exception e2) {
                throw new OSQLException("OSCAR-00712", "88888", 712, e2);
            }
        }
    }

    public static Timestamp convertToTimestamp(String s, Calendar userCal, Calendar systemCal) throws SQLException {
        if (s == null) {
            return null;
        }
        int userOffset = userCal.get(15) + userCal.get(16);
        int systemOffset = systemCal.get(15) + systemCal.get(16);
        int offset = systemOffset - userOffset;
        int index = s.indexOf(46);
        if (index != -1 && s.substring(index + 1).length() >= 9) {
            String temp;
            s = temp = s.substring(0, index + 1) + s.substring(index + 1, index + 10);
        }
        try {
            Timestamp oldts = Timestamp.valueOf(s);
            Timestamp newts = new Timestamp(oldts.getTime() + (long)offset);
            newts.setNanos(oldts.getNanos());
            return newts;
        }
        catch (Exception e) {
            throw new OSQLException("OSCAR-00712", "88888", 712, e);
        }
    }

    public static synchronized String toString(Timestamp paramTimestamp) {
        if (paramTimestamp == null) {
            return null;
        }
        localCalendarStr.setTime(paramTimestamp);
        return localCalendarStr.get(1) + "-" + (localCalendarStr.get(2) + 1) + "-" + localCalendarStr.get(5) + " " + localCalendarStr.get(11) + ":" + localCalendarStr.get(12) + ":" + localCalendarStr.get(13);
    }

    protected static int calculateRemainderLength(double d, int remainderPartLen) {
        double tmp = d % 100.0;
        double tmp1 = d / 100.0;
        if (tmp > -1.0 && tmp < 1.0) {
            remainderPartLen -= 2;
            tmp = tmp1 % 100.0;
            tmp1 /= 100.0;
            if (tmp > -1.0 && tmp < 1.0) {
                remainderPartLen -= 2;
                tmp = tmp1 % 100.0;
                tmp1 /= 100.0;
                if (tmp > -1.0 && tmp < 1.0) {
                    remainderPartLen -= 2;
                }
            }
        }
        return remainderPartLen;
    }

    public static int getByteLength(long paramLong) {
        int i = 10;
        if (paramLong / 1000000000000000000L == 0L) {
            --i;
            if (paramLong / 10000000000000000L == 0L) {
                --i;
                if (paramLong / 100000000000000L == 0L) {
                    --i;
                    if (paramLong / 1000000000000L == 0L) {
                        --i;
                        if (paramLong / 10000000000L == 0L) {
                            --i;
                            if (paramLong / 100000000L == 0L) {
                                --i;
                                if (paramLong / 1000000L == 0L) {
                                    --i;
                                    if (paramLong / 10000L == 0L) {
                                        --i;
                                        if (paramLong / 100L == 0L) {
                                            --i;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return i;
    }

    public static int getByteLength(long paramLong, int len) {
        int i = 0;
        long tmp = 0L;
        while ((tmp = new BigDecimal(paramLong).divide(new BigDecimal(Math.pow(100.0, i))).longValue() % 100L) == 0L) {
            --len;
            ++i;
        }
        return len;
    }

    public static long getLongInternal(byte[] paramLong) {
        if (paramLong.length == 1) {
            return 0L;
        }
        byte mode = 0;
        int length = paramLong.length;
        int j = 1;
        long result = 0L;
        mode = paramLong[0];
        do {
            result = result * 100L + (long)(paramLong[j] - 1);
        } while (++j != length);
        if (mode == 1) {
            return -result;
        }
        return result;
    }

    protected static int getMonth(byte[] val) {
        return val[2] - 1;
    }

    protected static int getDay(byte[] val) {
        return val[3];
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

    protected static int getNanos(byte[] val) {
        int i = (val[7] & 0xFF) << 24;
        i |= (val[8] & 0xFF) << 16;
        i |= (val[9] & 0xFF) << 8;
        if ((i |= val[10] & 0xFF) / 100000000 <= 0) {
            i *= 1000;
        }
        return i;
    }

    protected static int getTimeNanos(byte[] val) {
        int i = (val[3] & 0xFF) << 24;
        i |= (val[4] & 0xFF) << 16;
        i |= (val[5] & 0xFF) << 8;
        if ((i |= val[6] & 0xFF) / 100000000 <= 0) {
            i *= 1000;
        }
        return i;
    }

    protected static int getTimeHour(byte[] val) {
        return val[0] - 1;
    }

    protected static int getTimeMin(byte[] val) {
        return val[1] - 1;
    }

    protected static int getTimeSec(byte[] val) {
        return val[2] - 1;
    }

    static int countBits(long paramLong) {
        if (paramLong == 0L) {
            return 0;
        }
        while ((paramLong & 0xFFFFFFFL) == 0L) {
            paramLong <<= 8;
        }
        while (paramLong > 0L) {
            paramLong <<= 1;
        }
        int i = 0;
        long tmp = 0L;
        while ((paramLong & 0xFFFFFFFFF0000000L) != 0L) {
            tmp = paramLong;
            paramLong <<= 8;
            i += 8;
        }
        if (i >= 8 && paramLong == 0L) {
            i -= 8;
        }
        while (tmp != 0L) {
            tmp <<= 1;
            ++i;
        }
        return i;
    }

    public static int dtoa(byte[] paramArrayOfByte, int paramInt1, double paramDouble, boolean paramBoolean1, boolean paramBoolean2, char[] paramArrayOfChar, int paramInt2, long paramLong, int paramInt3) {
        int i3;
        boolean i2;
        int i10;
        int i9;
        int i8;
        int i11;
        int i = Integer.MAX_VALUE;
        int j = -1;
        int k = TypeConverter.countBits(paramLong);
        int m = k - paramInt2 - 1;
        boolean i1 = false;
        if (m < 0) {
            m = 0;
        }
        if (paramInt2 <= 62 && paramInt2 >= -21 && m < long5pow.length && k + n5bits[m] < 64 && m == 0) {
            long l1 = paramInt2 > paramInt3 ? 1L << paramInt2 - paramInt3 - 1 : 0L;
            paramLong = paramInt2 >= 52 ? (paramLong <<= paramInt2 - 52) : (paramLong >>>= 52 - paramInt2);
            i = 0;
            long l2 = paramLong;
            long l3 = l1;
            i11 = 0;
            while (l3 >= 10L) {
                l3 /= 10L;
                ++i11;
            }
            if (i11 != 0) {
                long l4 = long5pow[i11] << i11;
                long l5 = l2 % l4;
                l2 /= l4;
                i += i11;
                if (l5 >= l4 >> 1) {
                    ++l2;
                }
            }
            if (l2 <= Integer.MAX_VALUE) {
                int i12 = (int)l2;
                i8 = 10;
                i9 = i8 - 1;
                i10 = i12 % 10;
                i12 /= 10;
                while (i10 == 0) {
                    ++i;
                    i10 = i12 % 10;
                    i12 /= 10;
                }
                while (i12 != 0) {
                    paramArrayOfChar[i9--] = (char)(i10 + 48);
                    ++i;
                    i10 = i12 % 10;
                    i12 /= 10;
                }
                paramArrayOfChar[i9] = (char)(i10 + 48);
            } else {
                i8 = 20;
                i9 = i8 - 1;
                i10 = (int)(l2 % 10L);
                l2 /= 10L;
                while (i10 == 0) {
                    ++i;
                    i10 = (int)(l2 % 10L);
                    l2 /= 10L;
                }
                while (l2 != 0L) {
                    paramArrayOfChar[i9--] = (char)(i10 + 48);
                    ++i;
                    i10 = (int)(l2 % 10L);
                    l2 /= 10L;
                }
                paramArrayOfChar[i9] = (char)(i10 + 48);
            }
            i8 -= i9;
            if (i9 != 0) {
                System.arraycopy(paramArrayOfChar, i9, paramArrayOfChar, 0, i8);
            }
            ++i;
            j = i8;
            i1 = true;
        }
        if (!i1) {
            long l6;
            boolean i16;
            boolean i15;
            int i17;
            int i19;
            double d = Double.longBitsToDouble(0L | paramLong & 0xFFFFFFFFFFFFFFFFL);
            int n = (int)Math.floor((d - 1.5) * 0.289529654 + 0.176091259 + (double)paramInt2 * 0.301029995663981);
            int i5 = Math.max(0, -n);
            int i4 = i5 + m + paramInt2;
            int i7 = Math.max(0, n);
            int i6 = i7 + m;
            i9 = i5;
            i8 = i4 - paramInt3;
            paramLong >>>= 53 - k;
            int i13 = Math.min(i4 -= k - 1, i6);
            i4 -= i13;
            i6 -= i13;
            i8 -= i13;
            if (k == 1) {
                --i8;
            }
            if (i8 < 0) {
                i4 -= i8;
                i6 -= i8;
                i8 = 0;
            }
            int i14 = 0;
            i10 = k + i4 + (i5 < n5bits.length ? n5bits[i5] : i5 * 3);
            i11 = i6 + 1 + (i7 + 1 < n5bits.length ? n5bits[i7 + 1] : (i7 + 1) * 3);
            if (i10 < 64 && i11 < 64) {
                if (i10 < 32 && i11 < 32) {
                    int i18 = (int)paramLong * small5pow[i5] << i4;
                    i19 = small5pow[i7] << i6;
                    int i20 = small5pow[i9] << i8;
                    int i21 = i19 * 10;
                    i14 = 0;
                    i17 = i18 / i19;
                    i15 = (i18 = 10 * (i18 % i19)) < (i20 *= 10);
                    boolean bl = i16 = i18 + i20 > i21;
                    if (i17 == 0 && !i16) {
                        --n;
                    } else {
                        paramArrayOfChar[i14++] = (char)(48 + i17);
                    }
                    if (n <= -3 || n >= 8) {
                        i15 = false;
                        i16 = false;
                    }
                    while (!i15 && !i16) {
                        i17 = i18 / i19;
                        i18 = 10 * (i18 % i19);
                        if ((long)(i20 *= 10) > 0L) {
                            i15 = i18 < i20;
                            i16 = i18 + i20 > i21;
                        } else {
                            i15 = true;
                            i16 = true;
                        }
                        paramArrayOfChar[i14++] = (char)(48 + i17);
                    }
                    l6 = (i18 << 1) - i21;
                } else {
                    long l7 = paramLong * long5pow[i5] << i4;
                    long l8 = long5pow[i7] << i6;
                    long l9 = long5pow[i9] << i8;
                    long l10 = l8 * 10L;
                    i14 = 0;
                    i17 = (int)(l7 / l8);
                    i15 = (l7 = 10L * (l7 % l8)) < (l9 *= 10L);
                    boolean bl = i16 = l7 + l9 > l10;
                    if (i17 == 0 && !i16) {
                        --n;
                    } else {
                        paramArrayOfChar[i14++] = (char)(48 + i17);
                    }
                    if (n <= -3 || n >= 8) {
                        i15 = false;
                        i16 = false;
                    }
                    while (!i15 && !i16) {
                        i17 = (int)(l7 / l8);
                        l7 = 10L * (l7 % l8);
                        if ((l9 *= 10L) > 0L) {
                            i15 = l7 < l9;
                            i16 = l7 + l9 > l10;
                        } else {
                            i15 = true;
                            i16 = true;
                        }
                        paramArrayOfChar[i14++] = (char)(48 + i17);
                    }
                    l6 = (l7 << 1) - l10;
                }
            } else {
                FDBigInt localFDBigInt2 = TypeConverter.multPow52(new FDBigInt(paramLong), i5, i4);
                FDBigInt localFDBigInt1 = TypeConverter.constructPow52(i7, i6);
                FDBigInt localFDBigInt3 = TypeConverter.constructPow52(i9, i8);
                i19 = localFDBigInt1.normalizeMe();
                localFDBigInt2.lshiftMe(i19);
                localFDBigInt3.lshiftMe(i19);
                FDBigInt localFDBigInt4 = localFDBigInt1.mult(10);
                i14 = 0;
                i17 = localFDBigInt2.quoRemIteration(localFDBigInt1);
                localFDBigInt3 = localFDBigInt3.mult(10);
                i15 = localFDBigInt2.cmp(localFDBigInt3) < 0;
                boolean bl = i16 = localFDBigInt2.add(localFDBigInt3).cmp(localFDBigInt4) > 0;
                if (i17 == 0 && !i16) {
                    --n;
                } else {
                    paramArrayOfChar[i14++] = (char)(48 + i17);
                }
                if (n <= -3 || n >= 8) {
                    i15 = false;
                    i16 = false;
                }
                while (!i15 && !i16) {
                    i17 = localFDBigInt2.quoRemIteration(localFDBigInt1);
                    i15 = localFDBigInt2.cmp(localFDBigInt3 = localFDBigInt3.mult(10)) < 0;
                    i16 = localFDBigInt2.add(localFDBigInt3).cmp(localFDBigInt4) > 0;
                    paramArrayOfChar[i14++] = (char)(48 + i17);
                }
                if (i16 && i15) {
                    localFDBigInt2.lshiftMe(1);
                    l6 = localFDBigInt2.cmp(localFDBigInt4);
                } else {
                    l6 = 0L;
                }
            }
            i = n + 1;
            j = i14;
            if (i16) {
                if (i15) {
                    if (l6 == 0L) {
                        if ((paramArrayOfChar[j - 1] & '\u0001') != 0 && TypeConverter.roundup(paramArrayOfChar, j)) {
                            ++i;
                        }
                    } else if (l6 > 0L && TypeConverter.roundup(paramArrayOfChar, j)) {
                        ++i;
                    }
                } else if (TypeConverter.roundup(paramArrayOfChar, j)) {
                    ++i;
                }
            }
        }
        while (j - i > 0 && paramArrayOfChar[j - 1] == '0') {
            --j;
        }
        boolean bl = i2 = i % 2 != 0;
        if (i2) {
            if (j % 2 == 0) {
                paramArrayOfChar[j++] = 48;
            }
            i3 = (i - 1) / 2;
        } else {
            if (j % 2 == 1) {
                paramArrayOfChar[j++] = 48;
            }
            i3 = (i - 2) / 2;
        }
        int i5 = 0;
        int i6 = 1;
        if (paramBoolean1) {
            paramArrayOfByte[paramInt1] = (byte)(62 - i3);
            if (i2) {
                paramArrayOfByte[paramInt1 + i6] = (byte)(101 - (paramArrayOfChar[i5++] - 48));
                ++i6;
            }
            while (i5 < j) {
                paramArrayOfByte[paramInt1 + i6] = (byte)(101 - ((paramArrayOfChar[i5] - 48) * 10 + (paramArrayOfChar[i5 + 1] - 48)));
                i5 += 2;
                ++i6;
            }
            paramArrayOfByte[paramInt1 + i6++] = 102;
        } else {
            paramArrayOfByte[paramInt1] = (byte)(192 + (i3 + 1));
            if (i2) {
                paramArrayOfByte[paramInt1 + i6] = (byte)(paramArrayOfChar[i5++] - 48 + 1);
                ++i6;
            }
            while (i5 < j) {
                paramArrayOfByte[paramInt1 + i6] = (byte)((paramArrayOfChar[i5] - 48) * 10 + (paramArrayOfChar[i5 + 1] - 48) + 1);
                i5 += 2;
                ++i6;
            }
        }
        return i6;
    }

    public static boolean roundup(char[] paramArrayOfChar, int paramInt) {
        int i = paramInt - 1;
        char j = paramArrayOfChar[i];
        if (j == '9') {
            while (j == '9' && i > 0) {
                paramArrayOfChar[i] = 48;
                j = paramArrayOfChar[--i];
            }
            if (j == '9') {
                paramArrayOfChar[0] = 49;
                return true;
            }
        }
        paramArrayOfChar[i] = (char)(j + '\u0001');
        return false;
    }

    public static FDBigInt multPow52(FDBigInt paramFDBigInt, int paramInt1, int paramInt2) {
        if (paramInt1 != 0) {
            paramFDBigInt = paramInt1 < small5pow.length ? paramFDBigInt.mult(small5pow[paramInt1]) : paramFDBigInt.mult(TypeConverter.big5pow(paramInt1));
        }
        if (paramInt2 != 0) {
            paramFDBigInt.lshiftMe(paramInt2);
        }
        return paramFDBigInt;
    }

    public static synchronized FDBigInt big5pow(int paramInt) {
        if (paramInt < 0) {
            throw new RuntimeException("Assertion botch: negative power of 5");
        }
        if (b5p == null) {
            b5p = new FDBigInt[paramInt + 1];
        } else if (b5p.length <= paramInt) {
            FDBigInt[] arrayOfFDBigInt = new FDBigInt[paramInt + 1];
            System.arraycopy(b5p, 0, arrayOfFDBigInt, 0, b5p.length);
            b5p = arrayOfFDBigInt;
        }
        if (b5p[paramInt] != null) {
            return b5p[paramInt];
        }
        if (paramInt < small5pow.length) {
            TypeConverter.b5p[paramInt] = new FDBigInt(small5pow[paramInt]);
            return TypeConverter.b5p[paramInt];
        }
        if (paramInt < long5pow.length) {
            TypeConverter.b5p[paramInt] = new FDBigInt(long5pow[paramInt]);
            return TypeConverter.b5p[paramInt];
        }
        int i = paramInt >> 1;
        int j = paramInt - i;
        FDBigInt localFDBigInt1 = b5p[i];
        if (localFDBigInt1 == null) {
            localFDBigInt1 = TypeConverter.big5pow(i);
        }
        if (j < small5pow.length) {
            TypeConverter.b5p[paramInt] = localFDBigInt1.mult(small5pow[j]);
            return TypeConverter.b5p[paramInt];
        }
        FDBigInt localFDBigInt2 = b5p[j];
        if (localFDBigInt2 == null) {
            localFDBigInt2 = TypeConverter.big5pow(j);
        }
        TypeConverter.b5p[paramInt] = localFDBigInt1.mult(localFDBigInt2);
        return TypeConverter.b5p[paramInt];
    }

    public static FDBigInt constructPow52(int paramInt1, int paramInt2) {
        FDBigInt localFDBigInt = new FDBigInt(TypeConverter.big5pow(paramInt1));
        if (paramInt2 != 0) {
            localFDBigInt.lshiftMe(paramInt2);
        }
        return localFDBigInt;
    }

    public static int getZone(byte[] value, int off) {
        int hour = value[off] - 20;
        int min = value[++off] - 60;
        int zone = hour * HOUR_SECOND + min * MINUTE_SECOND;
        return zone;
    }

    public static void main(String[] args) {
    }
}

