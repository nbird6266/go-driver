/*
 * Decompiled with CFR 0.152.
 */
package org.xerial.snappy;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.xerial.snappy.SnappyErrorCode;
import org.xerial.snappy.SnappyNativeAPI;

public class SnappyNative
implements SnappyNativeAPI {
    public native String nativeLibraryVersion();

    public native int rawCompress(ByteBuffer var1, int var2, int var3, ByteBuffer var4, int var5) throws IOException;

    public native int rawCompress(Object var1, int var2, int var3, Object var4, int var5);

    public native int rawUncompress(ByteBuffer var1, int var2, int var3, ByteBuffer var4, int var5) throws IOException;

    public native int rawUncompress(Object var1, int var2, int var3, Object var4, int var5) throws IOException;

    public native int maxCompressedLength(int var1);

    public native int uncompressedLength(ByteBuffer var1, int var2, int var3) throws IOException;

    public native int uncompressedLength(Object var1, int var2, int var3) throws IOException;

    public native boolean isValidCompressedBuffer(ByteBuffer var1, int var2, int var3) throws IOException;

    public native boolean isValidCompressedBuffer(Object var1, int var2, int var3) throws IOException;

    public native void arrayCopy(Object var1, int var2, int var3, Object var4, int var5) throws IOException;

    public void throw_error(int errorCode) throws IOException {
        throw new IOException(String.format("%s(%d)", SnappyErrorCode.getErrorMessage(errorCode), errorCode));
    }
}

