/*
 * Decompiled with CFR 0.152.
 */
package org.xerial.snappy;

import java.io.IOException;
import java.io.OutputStream;
import org.xerial.snappy.Snappy;
import org.xerial.snappy.SnappyCodec;

public class SnappyOutputStream
extends OutputStream {
    static final int DEFAULT_BLOCK_SIZE = 32768;
    protected final OutputStream out;
    private final int blockSize;
    private int cursor = 0;
    protected byte[] uncompressed;
    protected byte[] compressed;

    public SnappyOutputStream(OutputStream out) throws IOException {
        this(out, 32768);
    }

    public SnappyOutputStream(OutputStream out, int blockSize) throws IOException {
        this.out = out;
        this.blockSize = blockSize;
        this.uncompressed = new byte[blockSize];
        this.compressed = new byte[Snappy.maxCompressedLength(blockSize)];
        this.writeHeader();
    }

    protected void writeHeader() throws IOException {
        SnappyCodec.currentHeader().writeHeader(this.out);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.rawWrite(b, off, len);
    }

    public void write(long[] d, int off, int len) throws IOException {
        this.rawWrite(d, off * 8, len * 8);
    }

    public void write(double[] f, int off, int len) throws IOException {
        this.rawWrite(f, off * 8, len * 8);
    }

    public void write(float[] f, int off, int len) throws IOException {
        this.rawWrite(f, off * 4, len * 4);
    }

    public void write(int[] f, int off, int len) throws IOException {
        this.rawWrite(f, off * 4, len * 4);
    }

    public void write(short[] f, int off, int len) throws IOException {
        this.rawWrite(f, off * 2, len * 2);
    }

    public void write(long[] d) throws IOException {
        this.write(d, 0, d.length);
    }

    public void write(double[] f) throws IOException {
        this.write(f, 0, f.length);
    }

    public void write(float[] f) throws IOException {
        this.write(f, 0, f.length);
    }

    public void write(int[] f) throws IOException {
        this.write(f, 0, f.length);
    }

    public void write(short[] f) throws IOException {
        this.write(f, 0, f.length);
    }

    public void rawWrite(Object array, int byteOffset, int byteLength) throws IOException {
        int readBytes = 0;
        while (readBytes < byteLength) {
            int copyLen = Math.min(this.uncompressed.length - this.cursor, byteLength - readBytes);
            Snappy.arrayCopy(array, byteOffset + readBytes, copyLen, this.uncompressed, this.cursor);
            readBytes += copyLen;
            this.cursor += copyLen;
            if (this.cursor < this.uncompressed.length) continue;
            this.dump();
        }
    }

    public void write(int b) throws IOException {
        if (this.cursor >= this.uncompressed.length) {
            this.dump();
        }
        this.uncompressed[this.cursor++] = (byte)b;
    }

    public void flush() throws IOException {
        this.dump();
        this.out.flush();
    }

    static void writeInt(OutputStream out, int value) throws IOException {
        out.write(value >> 24 & 0xFF);
        out.write(value >> 16 & 0xFF);
        out.write(value >> 8 & 0xFF);
        out.write(value >> 0 & 0xFF);
    }

    static int readInt(byte[] buffer, int pos) {
        int b1 = (buffer[pos] & 0xFF) << 24;
        int b2 = (buffer[pos + 1] & 0xFF) << 16;
        int b3 = (buffer[pos + 2] & 0xFF) << 8;
        int b4 = buffer[pos + 3] & 0xFF;
        return b1 | b2 | b3 | b4;
    }

    protected void dump() throws IOException {
        if (this.cursor <= 0) {
            return;
        }
        int compressedSize = Snappy.compress(this.uncompressed, 0, this.cursor, this.compressed, 0);
        SnappyOutputStream.writeInt(this.out, compressedSize);
        this.out.write(this.compressed, 0, compressedSize);
        this.cursor = 0;
    }

    public void close() throws IOException {
        this.flush();
        super.close();
        this.out.close();
    }
}

