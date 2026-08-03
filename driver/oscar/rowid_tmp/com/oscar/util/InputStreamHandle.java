/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamHandle {
    public static int readInt(InputStream is) throws IOException {
        int ch4;
        int ch3;
        int ch2;
        int ch1 = is.read();
        if ((ch1 | (ch2 = is.read()) | (ch3 = is.read()) | (ch4 = is.read())) < 0) {
            throw new EOFException();
        }
        return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
    }

    public static long readLong(InputStream is) throws IOException {
        return ((long)InputStreamHandle.readInt(is) << 32) + ((long)InputStreamHandle.readInt(is) & 0xFFFFFFFFL);
    }
}

