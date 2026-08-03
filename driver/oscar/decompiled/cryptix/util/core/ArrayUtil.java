/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.core;

public class ArrayUtil {
    private static final int ZEROES_LEN = 500;
    private static byte[] zeroes = new byte[500];

    private ArrayUtil() {
    }

    public static void clear(byte[] buf) {
        ArrayUtil.clear(buf, 0, buf.length);
    }

    public static void clear(byte[] buf, int offset, int length) {
        if (length <= 500) {
            System.arraycopy(zeroes, 0, buf, offset, length);
        } else {
            System.arraycopy(zeroes, 0, buf, offset, 500);
            int halflength = length / 2;
            int i = 500;
            while (i < length) {
                System.arraycopy(buf, offset, buf, offset + i, i <= halflength ? i : length - i);
                i += i;
            }
        }
    }

    public static int toInt(short s0, short s1) {
        return s0 & 0xFFFF | s1 << 16;
    }

    public static short toShort(byte b0, byte b1) {
        return (short)(b0 & 0xFF | b1 << 8);
    }

    public static byte[] toBytes(int n) {
        byte[] buf = new byte[4];
        int i = 3;
        while (i >= 0) {
            buf[i] = (byte)(n & 0xFF);
            n >>>= 8;
            --i;
        }
        return buf;
    }

    public static byte[] toBytes(short[] array, int offset, int length) {
        byte[] buf = new byte[2 * length];
        int j = 0;
        int i = offset;
        while (i < offset + length) {
            buf[j++] = (byte)(array[i] >>> 8 & 0xFF);
            buf[j++] = (byte)(array[i] & 0xFF);
            ++i;
        }
        return buf;
    }

    public static byte[] toBytes(short[] array) {
        return ArrayUtil.toBytes(array, 0, array.length);
    }

    public static short[] toShorts(byte[] array, int offset, int length) {
        short[] buf = new short[length / 2];
        int j = 0;
        int i = offset;
        while (i < offset + length - 1) {
            buf[j++] = (short)((array[i] & 0xFF) << 8 | array[i + 1] & 0xFF);
            i += 2;
        }
        return buf;
    }

    public static short[] toShorts(byte[] array) {
        return ArrayUtil.toShorts(array, 0, array.length);
    }

    public static boolean areEqual(byte[] a, byte[] b) {
        int aLength = a.length;
        if (aLength != b.length) {
            return false;
        }
        int i = 0;
        while (i < aLength) {
            if (a[i] != b[i]) {
                return false;
            }
            ++i;
        }
        return true;
    }

    public static boolean areEqual(int[] a, int[] b) {
        int aLength = a.length;
        if (aLength != b.length) {
            return false;
        }
        int i = 0;
        while (i < aLength) {
            if (a[i] != b[i]) {
                return false;
            }
            ++i;
        }
        return true;
    }

    public static int compared(byte[] a, byte[] b, boolean msbFirst) {
        int aLength = a.length;
        if (aLength < b.length) {
            return -1;
        }
        if (aLength > b.length) {
            return 1;
        }
        if (msbFirst) {
            int i = aLength - 1;
            while (i >= 0) {
                int b1 = a[i] & 0xFF;
                int b2 = b[i] & 0xFF;
                if (b1 < b2) {
                    return -1;
                }
                if (b1 > b2) {
                    return 1;
                }
                --i;
            }
        } else {
            int i = 0;
            while (i < aLength) {
                int b1 = a[i] & 0xFF;
                int b2 = b[i] & 0xFF;
                if (b1 < b2) {
                    return -1;
                }
                if (b1 > b2) {
                    return 1;
                }
                ++i;
            }
        }
        return 0;
    }

    public static boolean isText(byte[] buffer) {
        int len = buffer.length;
        if (len == 0) {
            return false;
        }
        int i = 0;
        while (i < len) {
            int c = buffer[i] & 0xFF;
            if (c < 32 || c > 127) {
                switch (c) {
                    case 7: 
                    case 8: 
                    case 9: 
                    case 10: 
                    case 11: 
                    case 12: 
                    case 13: 
                    case 26: 
                    case 27: 
                    case 155: {
                        break;
                    }
                    default: {
                        return false;
                    }
                }
            }
            ++i;
        }
        return true;
    }
}

