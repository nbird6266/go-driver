/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Hex {
    public static void hexprint(byte[] aa) {
        for (int i = 0; i < aa.length; ++i) {
            char high = (char)(aa[i] >> 4 & 0xF);
            high = high <= '\t' ? (char)(high + 48) : (char)(high - 10 + 97);
            char low = (char)(aa[i] & 0xF);
            low = low <= '\t' ? (char)(low + 48) : (char)(low - 10 + 97);
            if (i % 2 == 0) {
                System.out.print("\\u");
            }
            System.out.print("" + high + low);
            if (i == 0 || i % 2 != 1) continue;
            System.out.print(" ");
        }
        System.out.println();
    }

    public static void hexprint_series(byte[] aa) {
        for (int i = 0; i < aa.length; ++i) {
            char high = (char)(aa[i] >> 4 & 0xF);
            high = high <= '\t' ? (char)(high + 48) : (char)(high - 10 + 97);
            char low = (char)(aa[i] & 0xF);
            low = low <= '\t' ? (char)(low + 48) : (char)(low - 10 + 97);
            System.out.print("" + high + low);
        }
        System.out.println();
    }

    public static String hexPrintToString(byte[] aa) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < aa.length; ++i) {
            char high = (char)(aa[i] >> 4 & 0xF);
            high = high <= '\t' ? (char)(high + 48) : (char)(high - 10 + 97);
            char low = (char)(aa[i] & 0xF);
            low = low <= '\t' ? (char)(low + 48) : (char)(low - 10 + 97);
            sb.append(high).append(low);
        }
        return sb.toString();
    }

    public static byte[] parserStringToByte(String aa) {
        byte[] ret = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        char[] temp = aa.toCharArray();
        int tempByte = 0;
        for (int i = 0; i < temp.length; ++i) {
            int halfByte = 0;
            if (temp[i] <= '9' && temp[i] >= '0') {
                halfByte = temp[i] - 48;
            } else if (temp[i] <= 'F' && temp[i] >= 'A') {
                halfByte = temp[i] - 65 + 10;
            } else if (temp[i] <= 'f' && temp[i] >= 'a') {
                halfByte = temp[i] - 97 + 10;
            } else {
                throw new IllegalArgumentException("\u975e\u6cd5\u7684\u5b57\u7b26\u4e32\u7c7b\u578b");
            }
            if (i % 2 != 0) {
                tempByte = (tempByte << 4 | halfByte & 0xF) & 0xFF;
                bos.write(tempByte);
                tempByte &= 0;
                continue;
            }
            tempByte = halfByte & 0xF;
        }
        try {
            bos.flush();
            ret = bos.toByteArray();
            bos.close();
        }
        catch (IOException ex) {
            throw new Error("Byte\u6570\u636e\u6d41\u5bf9\u8c61\u8f93\u51fa\u5931\u8d25");
        }
        return ret;
    }
}

