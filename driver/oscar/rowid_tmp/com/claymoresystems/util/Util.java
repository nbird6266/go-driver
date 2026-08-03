/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.util;

public class Util {
    static String[] hex = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public static void xdump(String label, byte[] arr) {
        System.out.println(label + "[" + arr.length + "]");
        Util.xdump(arr);
    }

    public static void xdump(byte[] arr) {
        for (int i = 0; i < arr.length; ++i) {
            if (i > 0 && i % 12 == 0) {
                System.out.println("");
            }
            System.out.print(hex[arr[i] >> 4 & 0xF] + hex[arr[i] & 0xF] + " ");
        }
        System.out.println("");
    }

    public static String toHex(byte[] arr) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < arr.length; ++i) {
            str.append(hex[arr[i] >> 4 & 0xF]);
            str.append(hex[arr[i] & 0xF]);
        }
        return str.toString();
    }

    public static boolean areEqual(byte[] arr1, byte[] arr2) {
        if (arr1.length != arr2.length) {
            return false;
        }
        int al = arr1.length;
        for (int i = 0; i < al; ++i) {
            if (arr1[i] == arr2[i]) continue;
            return false;
        }
        return true;
    }

    public static byte[] toBytes(long val) {
        return Util.toBytes(val, 8);
    }

    public static byte[] toBytes(short val) {
        return Util.toBytes(val, 2);
    }

    public static byte[] toBytes(long val, int bytes) {
        byte[] retval = new byte[bytes];
        while (bytes-- > 0) {
            retval[bytes] = (byte)(val & 0xFFL);
            val >>= 8;
        }
        return retval;
    }

    public static int min(int a, int b) {
        return a > b ? b : a;
    }

    public static int max(int a, int b) {
        return a > b ? a : b;
    }
}

