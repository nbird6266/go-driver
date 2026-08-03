/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

import com.oscar.util.converter.CharacterSet;

public class CharacterSetByte
extends CharacterSet {
    public byte[] encode(String value) {
        if (value == null) {
            return null;
        }
        if ("".equals(value)) {
            return new byte[0];
        }
        int strLength = value.length();
        char[] chars = new char[strLength];
        value.getChars(0, strLength, chars, 0);
        return CharacterSetByte.charsToBytes(chars, (byte)0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] charsToBytes(char[] chars, byte replacement) {
        try {
            byte[] bytes = new byte[chars.length];
            for (int x = 0; x < chars.length; ++x) {
                if (chars[x] > '\u6bcf') {
                    bytes[x] = replacement;
                    if (replacement == 0) continue;
                    continue;
                }
                bytes[x] = (byte)chars[x];
            }
            byte[] byArray = bytes;
            Object var5_5 = null;
            return byArray;
        }
        catch (Throwable throwable) {
            Object var5_6 = null;
            throw throwable;
        }
    }

    public String decode(byte[] value) {
        if (value == null || value.length == 0) {
            return null;
        }
        return this.decode(value, 0, value.length, '\u0000');
    }

    public String decode(byte[] value, int offset, int count, char replacement) {
        if (value == null || value.length == 0) {
            return null;
        }
        try {
            return new String(value, offset, count, "ASCII");
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

