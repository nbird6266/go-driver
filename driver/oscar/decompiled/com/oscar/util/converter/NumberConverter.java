/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

import com.oscar.util.TypeConverter;

public class NumberConverter
extends TypeConverter {
    public static byte[] convertIntToBytes(int value) {
        byte[] arrayOfByte = null;
        if (value == 0) {
            arrayOfByte = new byte[]{-128};
        } else if (value < 0) {
            int m;
            arrayOfByte = value == Integer.MIN_VALUE ? new byte[]{58, 80, 54, 53, 65, 53, 102} : (-value < 100 ? new byte[]{62, (byte)(101 + value), 102} : (-value < 10000 ? ((m = -value % 100) != 0 ? new byte[]{61, (byte)(101 - -value / 100), (byte)(101 - m), 102} : new byte[]{61, (byte)(101 - -value / 100), 102}) : (-value < 1000000 ? ((m = -value % 100) != 0 ? new byte[]{60, (byte)(101 - -value / 10000), (byte)(101 - -value % 10000 / 100), (byte)(101 - m), 102} : ((m = -value % 10000 / 100) != 0 ? new byte[]{60, (byte)(101 - -value / 10000), (byte)(101 - m), 102} : new byte[]{60, (byte)(101 - -value / 10000), 102})) : (-value < 100000000 ? ((m = -value % 100) != 0 ? new byte[]{59, (byte)(101 - -value / 1000000), (byte)(101 - -value % 1000000 / 10000), (byte)(101 - -value % 10000 / 100), (byte)(101 - m), 102} : ((m = -value % 10000 / 100) != 0 ? new byte[]{59, (byte)(101 - -value / 1000000), (byte)(101 - -value % 1000000 / 10000), (byte)(101 - m), 102} : ((m = -value % 1000000 / 10000) != 0 ? new byte[]{59, (byte)(101 - -value / 1000000), (byte)(101 - m), 102} : new byte[]{59, (byte)(101 - -value / 1000000), 102}))) : ((m = -value % 100) != 0 ? new byte[]{58, (byte)(101 - -value / 100000000), (byte)(101 - -value % 100000000 / 1000000), (byte)(101 - -value % 1000000 / 10000), (byte)(101 - -value % 10000 / 100), (byte)(101 - m), 102} : ((m = -value % 10000 / 100) != 0 ? new byte[]{58, (byte)(101 - -value / 100000000), (byte)(101 - -value % 100000000 / 1000000), (byte)(101 - -value % 1000000 / 10000), (byte)(101 - m), 102} : ((m = -value % 1000000 / 10000) != 0 ? new byte[]{58, (byte)(101 - -value / 100000000), (byte)(101 - -value % 100000000 / 1000000), (byte)(101 - m), 102} : ((m = -value % 100000000 / 1000000) != 0 ? new byte[]{58, (byte)(101 - -value / 100000000), (byte)(101 - m), 102} : new byte[]{58, (byte)(101 - -value / 100000000), 102}))))))));
        } else if (value < 100) {
            int realLen = 1;
            arrayOfByte = new byte[1 + realLen];
            arrayOfByte[0] = (byte)(192 + realLen);
            arrayOfByte[1] = (byte)(value + 1);
        } else if (value < 10000) {
            int realLen = 2;
            int m = value % 100;
            if (m != 0) {
                arrayOfByte = new byte[1 + realLen];
                arrayOfByte[0] = (byte)(192 + realLen);
                arrayOfByte[1] = (byte)(value / 100 + 1);
                arrayOfByte[2] = (byte)(m + 1);
            } else {
                int sendLen = 1;
                arrayOfByte = new byte[1 + sendLen];
                arrayOfByte[0] = (byte)(192 + realLen);
                arrayOfByte[1] = (byte)(value / 100 + 1);
            }
        } else if (value < 1000000) {
            int realLen = 3;
            int m = value % 100;
            if (m != 0) {
                arrayOfByte = new byte[1 + realLen];
                arrayOfByte[0] = (byte)(192 + realLen);
                arrayOfByte[1] = (byte)(value / 10000 + 1);
                arrayOfByte[2] = (byte)(value % 10000 / 100 + 1);
                arrayOfByte[3] = (byte)(m + 1);
            } else {
                m = value % 10000 / 100;
                if (m != 0) {
                    int sendLen = 2;
                    arrayOfByte = new byte[1 + sendLen];
                    arrayOfByte[0] = (byte)(192 + realLen);
                    arrayOfByte[1] = (byte)(value / 10000 + 1);
                    arrayOfByte[2] = (byte)(m + 1);
                } else {
                    int sendLen = 1;
                    arrayOfByte = new byte[1 + sendLen];
                    arrayOfByte[0] = (byte)(192 + realLen);
                    arrayOfByte[1] = (byte)(value / 10000 + 1);
                }
            }
        } else if (value < 100000000) {
            int realLen = 4;
            int m = value % 100;
            if (m != 0) {
                arrayOfByte = new byte[1 + realLen];
                arrayOfByte[0] = (byte)(192 + realLen);
                arrayOfByte[1] = (byte)(value / 1000000 + 1);
                arrayOfByte[2] = (byte)(value % 1000000 / 10000 + 1);
                arrayOfByte[3] = (byte)(value % 10000 / 100 + 1);
                arrayOfByte[4] = (byte)(m + 1);
            } else {
                m = value % 10000 / 100;
                if (m != 0) {
                    int sendLen = 3;
                    arrayOfByte = new byte[1 + sendLen];
                    arrayOfByte[0] = (byte)(192 + realLen);
                    arrayOfByte[1] = (byte)(value / 1000000 + 1);
                    arrayOfByte[2] = (byte)(value % 1000000 / 10000 + 1);
                    arrayOfByte[3] = (byte)(m + 1);
                } else {
                    m = value % 1000000 / 10000;
                    if (m != 0) {
                        int sendLen = 2;
                        arrayOfByte = new byte[1 + sendLen];
                        arrayOfByte[0] = (byte)(192 + realLen);
                        arrayOfByte[1] = (byte)(value / 1000000 + 1);
                        arrayOfByte[2] = (byte)(m + 1);
                    } else {
                        int sendLen = 1;
                        arrayOfByte = new byte[1 + sendLen];
                        arrayOfByte[0] = (byte)(192 + realLen);
                        arrayOfByte[1] = (byte)(value / 1000000 + 1);
                    }
                }
            }
        } else {
            int m = value % 100;
            int realLen = 5;
            if (m != 0) {
                int sendLen = 5;
                arrayOfByte = new byte[1 + sendLen];
                arrayOfByte[0] = (byte)(192 + realLen);
                arrayOfByte[1] = (byte)(value / 100000000 + 1);
                arrayOfByte[2] = (byte)(value % 100000000 / 1000000 + 1);
                arrayOfByte[3] = (byte)(value % 1000000 / 10000 + 1);
                arrayOfByte[4] = (byte)(value % 10000 / 100 + 1);
                arrayOfByte[5] = (byte)(m + 1);
            } else {
                m = value % 10000 / 100;
                if (m != 0) {
                    int sendLen = 4;
                    arrayOfByte = new byte[1 + sendLen];
                    arrayOfByte[0] = (byte)(192 + realLen);
                    arrayOfByte[1] = (byte)(value / 100000000 + 1);
                    arrayOfByte[2] = (byte)(value % 100000000 / 1000000 + 1);
                    arrayOfByte[3] = (byte)(value % 1000000 / 10000 + 1);
                    arrayOfByte[4] = (byte)(m + 1);
                } else {
                    m = value % 1000000 / 10000;
                    if (m != 0) {
                        int sendLen = 3;
                        arrayOfByte = new byte[1 + sendLen];
                        arrayOfByte[0] = (byte)(192 + realLen);
                        arrayOfByte[1] = (byte)(value / 100000000 + 1);
                        arrayOfByte[2] = (byte)(value % 100000000 / 1000000 + 1);
                        arrayOfByte[3] = (byte)(m + 1);
                    } else {
                        m = value % 100000000 / 1000000;
                        if (m != 0) {
                            int sendLen = 2;
                            arrayOfByte = new byte[1 + sendLen];
                            arrayOfByte[0] = (byte)(192 + realLen);
                            arrayOfByte[1] = (byte)(value / 100000000 + 1);
                            arrayOfByte[2] = (byte)(m + 1);
                        } else {
                            int sendLen = 1;
                            arrayOfByte = new byte[1 + sendLen];
                            arrayOfByte[0] = (byte)(192 + realLen);
                            arrayOfByte[1] = (byte)(value / 100000000 + 1);
                        }
                    }
                }
            }
        }
        return arrayOfByte;
    }

    public static byte[] convertLongToBytes(long paramLong) {
        byte[] paramArrayOfByte = null;
        if (paramLong == 0L) {
            paramArrayOfByte = new byte[]{-128};
            return paramArrayOfByte;
        }
        if (paramLong == Long.MIN_VALUE) {
            paramArrayOfByte = new byte[]{53, 92, 79, 68, 29, 98, 33, 47, 24, 43, 93, 102};
            return paramArrayOfByte;
        }
        int i = NumberConverter.getByteLength(paramLong);
        int realLen = NumberConverter.getByteLength(paramLong, i);
        int j = 0;
        paramArrayOfByte = paramLong < 0L ? new byte[realLen + 2] : new byte[realLen + 1];
        j = i;
        int k = 0;
        if (paramLong < 0L) {
            paramLong = -paramLong;
            paramArrayOfByte[0] = (byte)(63 - i);
            while (true) {
                int m = (int)(paramLong % 100L);
                if (k == 0) {
                    if (m != 0) {
                        paramArrayOfByte[j + 1] = 102;
                        k = j + 2;
                        paramArrayOfByte[j] = (byte)(101 - m);
                    }
                } else {
                    paramArrayOfByte[j] = (byte)(101 - m);
                }
                if (--j != 0) {
                    paramLong /= 100L;
                    continue;
                }
                break;
            }
        } else {
            paramArrayOfByte[0] = (byte)(192 + i);
            while (true) {
                int m = (int)(paramLong % 100L);
                if (k == 0) {
                    if (m != 0) {
                        paramArrayOfByte[j] = (byte)(m + 1);
                        k = j + 1;
                    }
                } else {
                    paramArrayOfByte[j] = (byte)(m + 1);
                }
                if (--j == 0) break;
                paramLong /= 100L;
            }
        }
        return paramArrayOfByte;
    }

    public static byte[] convertDoubleToBytes(double paramDouble) {
        int m;
        if (paramDouble < 1.0E-9 && paramDouble > -1.0E-9) {
            byte[] paramArrayOfByte = new byte[]{-128};
            return paramArrayOfByte;
        }
        if (paramDouble == 0.0) {
            byte[] paramArrayOfByte = new byte[]{-128};
            return paramArrayOfByte;
        }
        if (paramDouble == Double.POSITIVE_INFINITY) {
            byte[] paramArrayOfByte = new byte[]{-3, 2};
            return paramArrayOfByte;
        }
        if (paramDouble == Double.NEGATIVE_INFINITY) {
            byte[] paramArrayOfByte = new byte[]{-3, 3};
            return paramArrayOfByte;
        }
        if (Double.isNaN(paramDouble)) {
            byte[] paramArrayOfByte = new byte[]{-3, 1};
            return paramArrayOfByte;
        }
        boolean flag = false;
        if (paramDouble < 0.0) {
            flag = true;
            paramDouble = -paramDouble;
        }
        long integerPart = (long)Math.floor(paramDouble);
        double tmp = paramDouble % 1000000.0 * 1000000.0;
        int remainderPartLen = 6;
        if ((remainderPartLen = NumberConverter.calculateRemainderLength(tmp, remainderPartLen)) == 0) {
            if (flag) {
                return NumberConverter.convertLongToBytes(-integerPart);
            }
            return NumberConverter.convertLongToBytes(integerPart);
        }
        if (remainderPartLen % 2 != 0) {
            ++remainderPartLen;
        }
        double power = Math.pow(10.0, remainderPartLen);
        double remainderPart = paramDouble % 1000000.0 * power % power;
        int integerPartLen = NumberConverter.getByteLength(integerPart);
        byte[] paramArrayOfByte = flag ? new byte[integerPartLen + remainderPartLen + 2] : new byte[integerPartLen + (remainderPartLen /= 2) + 1];
        int j = integerPartLen;
        if (flag) {
            paramArrayOfByte[0] = (byte)(63 - integerPartLen);
            while (true) {
                m = (int)(integerPart % 100L);
                paramArrayOfByte[j] = (byte)(101 - m);
                if (--j != 0) {
                    integerPart /= 100L;
                    continue;
                }
                break;
            }
        } else {
            paramArrayOfByte[0] = (byte)(192 + integerPartLen);
            while (true) {
                m = (int)(integerPart % 100L);
                paramArrayOfByte[j] = (byte)(m + 1);
                if (--j == 0) break;
                integerPart /= 100L;
            }
        }
        if (flag) {
            if (remainderPartLen == 0) {
                paramArrayOfByte[integerPartLen + 1] = 102;
            } else if (remainderPartLen == 1) {
                paramArrayOfByte[integerPartLen + 1] = (byte)(101 - (int)remainderPart);
                paramArrayOfByte[integerPartLen + 2] = 102;
            } else if (remainderPartLen == 2) {
                paramArrayOfByte[integerPartLen + 1] = (byte)(101 - (int)remainderPart / 100);
                paramArrayOfByte[integerPartLen + 2] = (byte)(101 - (int)remainderPart % 100);
                paramArrayOfByte[integerPartLen + 3] = 102;
            } else if (remainderPartLen == 3) {
                m = (int)(remainderPart / 10000.0);
                paramArrayOfByte[integerPartLen + 1] = (byte)(101 - m);
                paramArrayOfByte[integerPartLen + 2] = (byte)(101 - (int)((remainderPart %= 10000.0) / 100.0));
                paramArrayOfByte[integerPartLen + 3] = (byte)(101 - (int)(remainderPart % 100.0));
                paramArrayOfByte[integerPartLen + 4] = 102;
            }
        } else if (remainderPartLen == 1) {
            paramArrayOfByte[integerPartLen + 1] = (byte)(remainderPart + 1.0);
        } else if (remainderPartLen == 2) {
            paramArrayOfByte[integerPartLen + 1] = (byte)(remainderPart / 100.0 + 1.0);
            paramArrayOfByte[integerPartLen + 2] = (byte)(remainderPart % 100.0 + 1.0);
        } else if (remainderPartLen == 3) {
            m = (int)(remainderPart / 10000.0);
            paramArrayOfByte[integerPartLen + 1] = (byte)(m + 1);
            paramArrayOfByte[integerPartLen + 2] = (byte)((int)((remainderPart %= 10000.0) / 100.0) + 1);
            paramArrayOfByte[integerPartLen + 3] = (byte)((int)(remainderPart % 100.0) + 1);
        }
        return paramArrayOfByte;
    }

    public static byte[] convertDoubleToBytes1(double d) {
        int i1;
        long l1;
        int k;
        int m;
        boolean bool;
        byte[] arrayOfByte = null;
        int i = 0;
        int j = 0;
        if (d == 0.0) {
            arrayOfByte = new byte[]{-128};
            return arrayOfByte;
        }
        if (d == Double.POSITIVE_INFINITY) {
            arrayOfByte = new byte[]{-3, 2};
            return arrayOfByte;
        }
        if (d == Double.NEGATIVE_INFINITY) {
            arrayOfByte = new byte[]{-3, 3};
            return arrayOfByte;
        }
        if (Double.isNaN(d)) {
            arrayOfByte = new byte[]{-3, 1};
            return arrayOfByte;
        }
        boolean bl = bool = d < 0.0;
        if (bool) {
            d = -d;
        }
        if ((m = ((k = (int)((l1 = Double.doubleToLongBits(d)) >> 52 & 0x7FFL)) > 1023 ? 126 : 127) - (int)((double)(k - 1023) / 6.643856189774725)) < 0) {
            return null;
        }
        if (m > 192) {
            return null;
        }
        if (d > factorTable[m]) {
            while (m > 0 && d >= factorTable[m]) {
                --m;
            }
        }
        while (m < 193 && d <= factorTable[m + 1]) {
            ++m;
        }
        if (d == factorTable[m]) {
            if (m < 65) {
                return null;
            }
            if (m > 192) {
                return null;
            }
            if (bool) {
                arrayOfByte = new byte[3];
                arrayOfByte[i] = (byte)(62 - (127 - m));
                arrayOfByte[i + 1] = 100;
                arrayOfByte[i + 2] = 102;
                return arrayOfByte;
            }
            arrayOfByte = new byte[2];
            arrayOfByte[i] = (byte)(192 + (128 - m));
            arrayOfByte[i + 1] = 2;
            return arrayOfByte;
        }
        if (m < 64) {
            return null;
        }
        if (m > 191) {
            return null;
        }
        long l2 = bool ? l1 & 0x1FFFFFFFFFFFFFL : l1;
        long l3 = l2 & 0x1FFFFFFFFFFFFFL;
        int n = k;
        char[] arrayOfChar = null;
        if (n == 0) {
            while ((l3 & 0L) == 0L) {
                l3 <<= 1;
                --n;
            }
            i1 = 53 + n;
            ++n;
        } else {
            l3 |= 0x10000000000000L;
            i1 = 53;
        }
        arrayOfChar = new char[25];
        arrayOfByte = new byte[22];
        j = NumberConverter.dtoa(arrayOfByte, i, d, bool, false, arrayOfChar, n -= 1023, l3, i1);
        if (j > 0 && j < arrayOfByte.length) {
            byte[] retbyte = new byte[j];
            System.arraycopy(arrayOfByte, 0, retbyte, 0, j);
            return retbyte;
        }
        return arrayOfByte;
    }

    public static double convertBytesToDouble(byte[] val) {
        int i;
        if (val == null) {
            return 0.0;
        }
        if (val.length == 0) {
            return 0.0;
        }
        if (val.length == 1 && val[0] == -128) {
            return 0.0;
        }
        long longVal = 0L;
        int realLen = 0;
        double doubleVal = 0.0;
        if ((val[0] & POSITIVE_INT_MASK) == POSITIVE_INT_MASK) {
            realLen = val[0] - -64 + 1;
            if (realLen >= val.length) {
                for (i = 1; i < val.length; ++i) {
                    longVal *= 100L;
                    longVal += (long)(val[i] - 1);
                }
                while (i < realLen) {
                    longVal *= 100L;
                    ++i;
                }
            } else {
                while (i < realLen) {
                    longVal *= 100L;
                    longVal += (long)(val[i] - 1);
                    ++i;
                }
            }
            if (i < val.length) {
                for (int j = val.length - 1; j >= i; --j) {
                    doubleVal += (double)(val[j] - 1);
                    doubleVal /= 100.0;
                }
            }
            doubleVal += (double)longVal;
        } else {
            realLen = 63 - val[0] + 2;
            if (realLen >= val.length) {
                while (i < val.length - 1) {
                    longVal *= 100L;
                    longVal += (long)(101 - val[i]);
                    ++i;
                }
                while (i < realLen - 1) {
                    longVal *= 100L;
                    ++i;
                }
            } else {
                while (i < realLen - 1) {
                    longVal *= 100L;
                    longVal += (long)(101 - val[i]);
                    ++i;
                }
            }
            longVal = -longVal;
            if (++i < val.length) {
                for (int j = val.length - 1; j >= i; --j) {
                    doubleVal += (double)(101 - val[j]);
                    doubleVal /= 100.0;
                }
            }
            doubleVal = -doubleVal;
            doubleVal += (double)longVal;
        }
        return doubleVal;
    }

    public static long convertBytesToLong(byte[] val) {
        int i;
        if (val == null) {
            return 0L;
        }
        if (val.length == 0) {
            return 0L;
        }
        if (val.length == 1 && val[0] == -128) {
            return 0L;
        }
        long longVal = 0L;
        int realLen = 0;
        if ((val[0] & POSITIVE_INT_MASK) == POSITIVE_INT_MASK) {
            realLen = val[0] - -64 + 1;
            if (realLen >= val.length) {
                for (i = 1; i < val.length; ++i) {
                    longVal *= 100L;
                    longVal += (long)(val[i] - 1);
                }
                while (i < realLen) {
                    longVal *= 100L;
                    ++i;
                }
            } else {
                while (i < realLen) {
                    longVal *= 100L;
                    longVal += (long)(val[i] - 1);
                    ++i;
                }
            }
        } else {
            realLen = 63 - val[0] + 2;
            if (realLen >= val.length) {
                while (i < val.length - 1) {
                    longVal *= 100L;
                    longVal += (long)(101 - val[i]);
                    ++i;
                }
                while (i < realLen - 1) {
                    longVal *= 100L;
                    ++i;
                }
            } else {
                while (i < realLen - 1) {
                    longVal *= 100L;
                    longVal += (long)(101 - val[i]);
                    ++i;
                }
            }
            longVal = -longVal;
        }
        return longVal;
    }

    public static int convertBytesToInt(byte[] val) {
        int i;
        if (val == null) {
            return 0;
        }
        if (val.length == 0) {
            return 0;
        }
        if (val.length == 1 && val[0] == -128) {
            return 0;
        }
        int intVal = 0;
        int realLen = 0;
        if ((val[0] & POSITIVE_INT_MASK) == POSITIVE_INT_MASK) {
            realLen = val[0] - -64 + 1;
            if (realLen >= val.length) {
                for (i = 1; i < val.length; ++i) {
                    intVal *= 100;
                    intVal += val[i] - 1;
                }
                while (i < realLen) {
                    intVal *= 100;
                    ++i;
                }
            } else {
                while (i < realLen) {
                    intVal *= 100;
                    intVal += val[i] - 1;
                    ++i;
                }
            }
        } else {
            realLen = 63 - val[0] + 2;
            if (realLen >= val.length) {
                while (i < val.length - 1) {
                    intVal *= 100;
                    intVal += 101 - val[i];
                    ++i;
                }
                while (i < realLen - 1) {
                    intVal *= 100;
                    ++i;
                }
            } else {
                while (i < realLen - 1) {
                    intVal *= 100;
                    intVal += 101 - val[i];
                    ++i;
                }
            }
            intVal = -intVal;
        }
        return intVal;
    }
}

