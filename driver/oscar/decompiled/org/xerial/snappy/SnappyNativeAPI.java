/*
 * Decompiled with CFR 0.152.
 */
package org.xerial.snappy;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface SnappyNativeAPI {
    public String nativeLibraryVersion();

    public int rawCompress(ByteBuffer var1, int var2, int var3, ByteBuffer var4, int var5) throws IOException;

    public int rawCompress(Object var1, int var2, int var3, Object var4, int var5);

    public int rawUncompress(ByteBuffer var1, int var2, int var3, ByteBuffer var4, int var5) throws IOException;

    public int rawUncompress(Object var1, int var2, int var3, Object var4, int var5) throws IOException;

    public int maxCompressedLength(int var1);

    public int uncompressedLength(ByteBuffer var1, int var2, int var3) throws IOException;

    public int uncompressedLength(Object var1, int var2, int var3) throws IOException;

    public boolean isValidCompressedBuffer(ByteBuffer var1, int var2, int var3) throws IOException;

    public boolean isValidCompressedBuffer(Object var1, int var2, int var3) throws IOException;

    public void arrayCopy(Object var1, int var2, int var3, Object var4, int var5) throws IOException;

    public void throw_error(int var1) throws IOException;
}

