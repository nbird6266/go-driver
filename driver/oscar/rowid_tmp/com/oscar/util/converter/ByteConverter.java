/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

public class ByteConverter {
    public static byte[] convertByteArr(byte[] src) {
        if (src != null && src.length > 240) {
            byte[] s = ByteConverter.convertVarcharData(src);
            return s;
        }
        return src;
    }

    public static byte[] convertVarcharData(byte[] data) {
        int len = data.length + 1 + (data.length + 240 - 1) / 240 + 1;
        byte[] result = new byte[len];
        result[0] = -2;
        int srcPosition = 0;
        int destPosition = 1;
        int tmp = data.length - srcPosition;
        while (true) {
            if (tmp == 240) {
                result[destPosition] = -16;
                System.arraycopy(data, srcPosition, result, ++destPosition, 240);
                srcPosition += 240;
                destPosition += 240;
                break;
            }
            if (tmp > 240) {
                result[destPosition] = -16;
                System.arraycopy(data, srcPosition, result, ++destPosition, 240);
                destPosition += 240;
            } else {
                result[destPosition] = (byte)tmp;
                System.arraycopy(data, srcPosition, result, ++destPosition, tmp);
                srcPosition += tmp;
                destPosition += tmp;
                break;
            }
            tmp = data.length - (srcPosition += 240);
        }
        result[len - 1] = 0;
        return result;
    }
}

