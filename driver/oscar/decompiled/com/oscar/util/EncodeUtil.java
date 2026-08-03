/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class EncodeUtil {
    private static final String[] TRY_DECODERS = new String[]{"utf8", "gb18030", "big5"};

    public static String tryDecode(byte[] before) {
        if (before == null || before.length == 0) {
            return null;
        }
        try {
            for (String tryDecode : TRY_DECODERS) {
                String str = new String(before, tryDecode);
                byte[] after = str.getBytes(tryDecode);
                if (!Arrays.equals(before, after)) continue;
                return str;
            }
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            // empty catch block
        }
        return new String(before);
    }

    public static String tryDecode(byte[] before, String preference) {
        if (before == null || before.length == 0) {
            return null;
        }
        try {
            return EncodeUtil.tryDecodeWithCharset(before, preference);
        }
        catch (IOException e1) {
            for (String tryDecode : TRY_DECODERS) {
                if (tryDecode.equalsIgnoreCase(preference)) continue;
                try {
                    String str = new String(before, tryDecode);
                    byte[] after = str.getBytes(tryDecode);
                    if (!Arrays.equals(before, after)) continue;
                    return str;
                }
                catch (UnsupportedEncodingException e) {
                    // empty catch block
                }
            }
            return new String(before);
        }
    }

    public static String tryDecodeWithCharset(byte[] before, String charSet) throws UndecodedEncodingException, UnsupportedEncodingException {
        if (before == null || before.length == 0) {
            return null;
        }
        String str = new String(before, charSet);
        byte[] after = str.getBytes(charSet);
        if (Arrays.equals(before, after)) {
            return str;
        }
        throw new UndecodedEncodingException("\u7f16\u7801\u4e0d\u5bf9");
    }

    private static class UndecodedEncodingException
    extends IOException {
        public UndecodedEncodingException(String s) {
            super(s);
        }

        public UndecodedEncodingException() {
        }
    }
}

