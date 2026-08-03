/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.core;

import cryptix.util.core.ArrayUtil;
import java.io.PrintWriter;

public class Hex {
    private static final char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private Hex() {
    }

    public static String toString(byte[] ba, int offset, int length) {
        char[] buf = new char[length * 2];
        int j = 0;
        int i = offset;
        while (i < offset + length) {
            byte k = ba[i];
            buf[j++] = hexDigits[k >>> 4 & 0xF];
            buf[j++] = hexDigits[k & 0xF];
            ++i;
        }
        return new String(buf);
    }

    public static String toString(byte[] ba) {
        return Hex.toString(ba, 0, ba.length);
    }

    public static String toString(int[] ia, int offset, int length) {
        char[] buf = new char[length * 8];
        int j = 0;
        int i = offset;
        while (i < offset + length) {
            int k = ia[i];
            buf[j++] = hexDigits[k >>> 28 & 0xF];
            buf[j++] = hexDigits[k >>> 24 & 0xF];
            buf[j++] = hexDigits[k >>> 20 & 0xF];
            buf[j++] = hexDigits[k >>> 16 & 0xF];
            buf[j++] = hexDigits[k >>> 12 & 0xF];
            buf[j++] = hexDigits[k >>> 8 & 0xF];
            buf[j++] = hexDigits[k >>> 4 & 0xF];
            buf[j++] = hexDigits[k & 0xF];
            ++i;
        }
        return new String(buf);
    }

    public static String toString(int[] ia) {
        return Hex.toString(ia, 0, ia.length);
    }

    public static String toReversedString(byte[] b, int offset, int length) {
        char[] buf = new char[length * 2];
        int j = 0;
        int i = offset + length - 1;
        while (i >= offset) {
            buf[j++] = hexDigits[b[i] >>> 4 & 0xF];
            buf[j++] = hexDigits[b[i] & 0xF];
            --i;
        }
        return new String(buf);
    }

    public static String toReversedString(byte[] b) {
        return Hex.toReversedString(b, 0, b.length);
    }

    public static byte[] fromString(String hex) {
        int len = hex.length();
        byte[] buf = new byte[(len + 1) / 2];
        int i = 0;
        int j = 0;
        if (len % 2 == 1) {
            buf[j++] = (byte)Hex.fromDigit(hex.charAt(i++));
        }
        while (i < len) {
            buf[j++] = (byte)(Hex.fromDigit(hex.charAt(i++)) << 4 | Hex.fromDigit(hex.charAt(i++)));
        }
        return buf;
    }

    /*
     * Unable to fully structure code
     */
    public static byte[] fromReversedString(String hex) {
        len = hex.length();
        buf = new byte[(len + 1) / 2];
        j = 0;
        if (len % 2 != 1) ** GOTO lbl7
        throw new IllegalArgumentException("string must have an even number of digits");
lbl-1000:
        // 1 sources

        {
            buf[j++] = (byte)(Hex.fromDigit(hex.charAt(--len)) | Hex.fromDigit(hex.charAt(--len)) << 4);
lbl7:
            // 2 sources

            ** while (len > 0)
        }
lbl8:
        // 1 sources

        return buf;
    }

    public static char toDigit(int n) {
        try {
            return hexDigits[n];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(String.valueOf(n) + " is out of range for a hex digit");
        }
    }

    public static int fromDigit(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - 48;
        }
        if (ch >= 'A' && ch <= 'F') {
            return ch - 65 + 10;
        }
        if (ch >= 'a' && ch <= 'f') {
            return ch - 97 + 10;
        }
        throw new IllegalArgumentException("invalid hex digit '" + ch + "'");
    }

    public static String byteToString(int n) {
        char[] buf = new char[]{hexDigits[n >>> 4 & 0xF], hexDigits[n & 0xF]};
        return new String(buf);
    }

    public static String shortToString(int n) {
        char[] buf = new char[]{hexDigits[n >>> 12 & 0xF], hexDigits[n >>> 8 & 0xF], hexDigits[n >>> 4 & 0xF], hexDigits[n & 0xF]};
        return new String(buf);
    }

    public static String intToString(int n) {
        char[] buf = new char[8];
        int i = 7;
        while (i >= 0) {
            buf[i] = hexDigits[n & 0xF];
            n >>>= 4;
            --i;
        }
        return new String(buf);
    }

    public static String longToString(long n) {
        char[] buf = new char[16];
        int i = 15;
        while (i >= 0) {
            buf[i] = hexDigits[(int)n & 0xF];
            n >>>= 4;
            --i;
        }
        return new String(buf);
    }

    public static String dumpString(byte[] data, int offset, int length, String m) {
        if (data == null) {
            return String.valueOf(m) + "null\n";
        }
        StringBuffer sb = new StringBuffer(length * 3);
        if (length > 32) {
            sb.append(m).append("Hexadecimal dump of ").append(length).append(" bytes...\n");
        }
        int end = offset + length;
        int l = Integer.toString(length).length();
        if (l < 4) {
            l = 4;
        }
        while (offset < end) {
            if (length > 32) {
                String s = "         " + offset;
                sb.append(m).append(s.substring(s.length() - l)).append(": ");
            }
            int i = 0;
            while (i < 32 && offset + i + 7 < end) {
                sb.append(Hex.toString(data, offset + i, 8)).append(' ');
                i += 8;
            }
            if (i < 32) {
                while (i < 32 && offset + i < end) {
                    sb.append(Hex.byteToString(data[offset + i]));
                    ++i;
                }
            }
            sb.append('\n');
            offset += 32;
        }
        return sb.toString();
    }

    public static String dumpString(byte[] data) {
        return data == null ? "null\n" : Hex.dumpString(data, 0, data.length, "");
    }

    public static String dumpString(byte[] data, String m) {
        return data == null ? "null\n" : Hex.dumpString(data, 0, data.length, m);
    }

    public static String dumpString(byte[] data, int offset, int length) {
        return Hex.dumpString(data, offset, length, "");
    }

    public static String dumpString(int[] data, int offset, int length, String m) {
        if (data == null) {
            return String.valueOf(m) + "null\n";
        }
        StringBuffer sb = new StringBuffer(length * 3);
        if (length > 8) {
            sb.append(m).append("Hexadecimal dump of ").append(length).append(" integers...\n");
        }
        int end = offset + length;
        int x = Integer.toString(length).length();
        if (x < 8) {
            x = 8;
        }
        while (offset < end) {
            if (length > 8) {
                String s = "         " + offset;
                sb.append(m).append(s.substring(s.length() - x)).append(": ");
            }
            int i = 0;
            while (i < 8 && offset < end) {
                sb.append(Hex.intToString(data[offset++])).append(' ');
                ++i;
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public static String dumpString(int[] data) {
        return Hex.dumpString(data, 0, data.length, "");
    }

    public static String dumpString(int[] data, String m) {
        return Hex.dumpString(data, 0, data.length, m);
    }

    public static String dumpString(int[] data, int offset, int length) {
        return Hex.dumpString(data, offset, length, "");
    }

    public static void main(String[] args) {
        Hex.self_test(new PrintWriter(System.out, true));
    }

    public static void self_test(PrintWriter out) {
        String test = "Hello. This is a test string with more than 32 characters.";
        byte[] buf = new byte[test.length()];
        int i = 0;
        while (i < test.length()) {
            buf[i] = (byte)test.charAt(i);
            ++i;
        }
        String s = Hex.toString(buf);
        out.println("Hex.toString(buf) = " + s);
        byte[] buf2 = Hex.fromString(s);
        if (!ArrayUtil.areEqual(buf, buf2)) {
            System.out.println("buf != buf2");
        }
        s = Hex.toReversedString(buf);
        out.println("Hex.toReversedString(buf) = " + s);
        buf2 = Hex.fromReversedString(s);
        if (!ArrayUtil.areEqual(buf, buf2)) {
            out.println("buf != buf2");
        }
        out.print("Hex.dumpString(buf, 0, 28) =\n" + Hex.dumpString(buf, 0, 28));
        out.print("Hex.dumpString(null) =\n" + Hex.dumpString(null));
        out.print(Hex.dumpString(buf, "+++"));
        out.flush();
    }
}

