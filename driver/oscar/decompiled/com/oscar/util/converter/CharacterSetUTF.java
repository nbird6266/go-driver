/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

import com.oscar.util.converter.CharacterSet;

public class CharacterSetUTF
extends CharacterSet {
    public byte[] encode(String value) {
        if (value == null) {
            return null;
        }
        if ("".equals(value)) {
            return new byte[0];
        }
        byte[] result = CharacterSetUTF.stringToUTF(value);
        return result;
    }

    public String decode(byte[] value) {
        if (value == null) {
            return null;
        }
        return this.decode(value, 0, value.length);
    }

    public String decode(byte[] value, int offset, int count) {
        if (value == null) {
            return null;
        }
        char[] chars = new char[value.length];
        int chars_len = CharacterSetUTF.convertUTFBytesToJavaChars(value, offset, chars, 0, count, false);
        return new String(chars, 0, chars_len);
    }
}

