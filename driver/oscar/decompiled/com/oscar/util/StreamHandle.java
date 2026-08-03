/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class StreamHandle {
    public static int MAX_INIT_LENGTH = 0x7D0000;

    private static long min(long a, long b) {
        return a < b ? a : b;
    }

    public static long write(Writer writer, Reader reader, long length, int bufferLength) throws IOException {
        char[] buffer = new char[length == -1L ? bufferLength : (int)Math.min((long)bufferLength, length)];
        int readLen = 0;
        int writeLen = 0;
        if (length == -1L) {
            while ((readLen = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, readLen);
                writeLen += readLen;
            }
        } else {
            long remainLen = length;
            readLen = (int)StreamHandle.min(remainLen, buffer.length);
            while ((readLen = reader.read(buffer, 0, readLen)) != -1) {
                writer.write(buffer, 0, readLen);
                writeLen += readLen;
                if ((remainLen -= (long)readLen) != 0L) {
                    readLen = (int)StreamHandle.min(remainLen, buffer.length);
                    continue;
                }
                break;
            }
        }
        writer.flush();
        return writeLen;
    }

    public static int write(OutputStream os, InputStream is, long length, int bufferLength) throws IOException {
        byte[] buffer = new byte[length == -1L ? bufferLength : (int)Math.min((long)bufferLength, length)];
        int readLen = 0;
        int writeLen = 0;
        if (length == -1L) {
            while ((readLen = is.read(buffer)) != -1) {
                os.write(buffer, 0, readLen);
                writeLen += readLen;
            }
        } else {
            long remainLen = length;
            readLen = (int)StreamHandle.min(remainLen, buffer.length);
            while ((readLen = is.read(buffer, 0, readLen)) != -1) {
                os.write(buffer, 0, readLen);
                writeLen += readLen;
                if ((remainLen -= (long)readLen) != 0L) {
                    readLen = (int)StreamHandle.min(remainLen, buffer.length);
                    continue;
                }
                break;
            }
        }
        os.flush();
        return writeLen;
    }

    public static int write(OutputStream os, byte[] data, int offset, int length, int bufferLength) throws IOException {
        int writeLen = 0;
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        bis.skip(offset);
        writeLen = StreamHandle.write(os, bis, (long)length, bufferLength);
        bis.close();
        return writeLen;
    }

    public static int write(Writer writer, String data, int offset, int length, int bufferLength) throws IOException {
        int writeLen = 0;
        StringReader reader = new StringReader(data);
        reader.skip(offset);
        writeLen = (int)StreamHandle.write(writer, reader, (long)length, bufferLength);
        reader.close();
        return writeLen;
    }

    public static byte[] read(InputStream is, int length, int bufferLength) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(Math.min(length, MAX_INIT_LENGTH));
        StreamHandle.write(bos, is, (long)length, bufferLength);
        bos.close();
        return bos.toByteArray();
    }

    public static String read(Reader reader, int length, int bufferLength) throws IOException {
        StringWriter writer = null;
        String ret = null;
        writer = length <= -1 ? new StringWriter(MAX_INIT_LENGTH) : new StringWriter(Math.min(length, MAX_INIT_LENGTH));
        StreamHandle.write(writer, reader, (long)length, bufferLength);
        ret = writer.toString();
        writer.close();
        return ret;
    }

    public static int read(InputStream is, byte[] buffer, int offset, int length) throws IOException {
        if (length == -1) {
            length = buffer.length - offset;
        }
        return is.read(buffer, offset, length);
    }

    public static int read(Reader is, char[] buffer, int offset, int length) throws IOException {
        if (length == -1) {
            length = buffer.length - offset;
        }
        return is.read(buffer, offset, length);
    }
}

