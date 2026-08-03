/*
 * Decompiled with CFR 0.152.
 */
package org.xerial.snappy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xerial.snappy.Snappy;
import org.xerial.snappy.SnappyCodec;
import org.xerial.snappy.SnappyOutputStream;

public class SnappyInputStream
extends InputStream {
    private boolean finishedReading = false;
    protected final InputStream in;
    private byte[] compressed;
    private byte[] uncompressed;
    private int uncompressedCursor = 0;
    private int uncompressedLimit = 0;
    private byte[] chunkSizeBuf = new byte[4];

    public SnappyInputStream(InputStream input) throws IOException {
        this.in = input;
        this.readHeader();
    }

    public void close() throws IOException {
        this.compressed = null;
        this.uncompressed = null;
        if (this.in != null) {
            this.in.close();
        }
    }

    protected void readHeader() throws IOException {
        int readBytes;
        int ret;
        byte[] header = new byte[SnappyCodec.headerSize()];
        for (readBytes = 0; readBytes < header.length && (ret = this.in.read(header, readBytes, header.length - readBytes)) != -1; readBytes += ret) {
        }
        if (readBytes < header.length || header[0] != SnappyCodec.MAGIC_HEADER[0]) {
            this.readFully(header, readBytes);
            return;
        }
        SnappyCodec codec = SnappyCodec.readHeader(new ByteArrayInputStream(header));
        if (codec.isValidMagicHeader()) {
            if (codec.version < 1) {
                throw new IOException(String.format("compressed with imcompatible codec version %d. At least version %d is required", codec.version, 1));
            }
        } else {
            this.readFully(header, readBytes);
            return;
        }
    }

    protected void readFully(byte[] fragment, int fragmentLength) throws IOException {
        this.compressed = new byte[Math.max(8192, fragmentLength)];
        System.arraycopy(fragment, 0, this.compressed, 0, fragmentLength);
        int cursor = fragmentLength;
        int readBytes = 0;
        while ((readBytes = this.in.read(this.compressed, cursor, this.compressed.length - cursor)) != -1) {
            if ((cursor += readBytes) < this.compressed.length) continue;
            byte[] newBuf = new byte[this.compressed.length * 2];
            System.arraycopy(this.compressed, 0, newBuf, 0, this.compressed.length);
            this.compressed = newBuf;
        }
        this.finishedReading = true;
        int uncompressedLength = Snappy.uncompressedLength(this.compressed, 0, cursor);
        this.uncompressed = new byte[uncompressedLength];
        Snappy.uncompress(this.compressed, 0, cursor, this.uncompressed, 0);
        this.uncompressedCursor = 0;
        this.uncompressedLimit = uncompressedLength;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return this.rawRead(b, off, len);
    }

    public int rawRead(Object array, int byteOffset, int byteLength) throws IOException {
        int writtenBytes = 0;
        while (writtenBytes < byteLength) {
            if (this.uncompressedCursor >= this.uncompressedLimit) {
                if (this.hasNextChunk()) continue;
                return writtenBytes == 0 ? -1 : writtenBytes;
            }
            int bytesToWrite = Math.min(this.uncompressedLimit - this.uncompressedCursor, byteLength - writtenBytes);
            Snappy.arrayCopy(this.uncompressed, this.uncompressedCursor, bytesToWrite, array, byteOffset + writtenBytes);
            writtenBytes += bytesToWrite;
            this.uncompressedCursor += bytesToWrite;
        }
        return writtenBytes;
    }

    public int read(long[] d, int off, int len) throws IOException {
        return this.rawRead(d, off * 8, len * 8);
    }

    public int read(long[] d) throws IOException {
        return this.read(d, 0, d.length);
    }

    public int read(double[] d, int off, int len) throws IOException {
        return this.rawRead(d, off * 8, len * 8);
    }

    public int read(double[] d) throws IOException {
        return this.read(d, 0, d.length);
    }

    public int read(int[] d) throws IOException {
        return this.read(d, 0, d.length);
    }

    public int read(int[] d, int off, int len) throws IOException {
        return this.rawRead(d, off * 4, len * 4);
    }

    public int read(float[] d, int off, int len) throws IOException {
        return this.rawRead(d, off * 4, len * 4);
    }

    public int read(float[] d) throws IOException {
        return this.read(d, 0, d.length);
    }

    public int read(short[] d, int off, int len) throws IOException {
        return this.rawRead(d, off * 2, len * 2);
    }

    public int read(short[] d) throws IOException {
        return this.read(d, 0, d.length);
    }

    protected boolean hasNextChunk() throws IOException {
        int ret;
        int readBytes;
        int ret2;
        if (this.finishedReading) {
            return false;
        }
        this.uncompressedCursor = 0;
        this.uncompressedLimit = 0;
        for (readBytes = 0; readBytes < 4; readBytes += ret2) {
            ret2 = this.in.read(this.chunkSizeBuf, readBytes, 4 - readBytes);
            if (ret2 != -1) continue;
            this.finishedReading = true;
            return false;
        }
        int chunkSize = SnappyOutputStream.readInt(this.chunkSizeBuf, 0);
        if (this.compressed == null || chunkSize > this.compressed.length) {
            this.compressed = new byte[chunkSize];
        }
        for (readBytes = 0; readBytes < chunkSize && (ret = this.in.read(this.compressed, readBytes, chunkSize - readBytes)) != -1; readBytes += ret) {
        }
        if (readBytes < chunkSize) {
            throw new IOException("failed to read chunk");
        }
        try {
            int actualUncompressedLength;
            int uncompressedLength = Snappy.uncompressedLength(this.compressed, 0, chunkSize);
            if (this.uncompressed == null || uncompressedLength > this.uncompressed.length) {
                this.uncompressed = new byte[uncompressedLength];
            }
            if (uncompressedLength != (actualUncompressedLength = Snappy.uncompress(this.compressed, 0, chunkSize, this.uncompressed, 0))) {
                throw new IOException("invalid uncompressed byte size");
            }
            this.uncompressedLimit = actualUncompressedLength;
        }
        catch (IOException e) {
            throw new IOException("failed to uncompress the chunk: " + e.getMessage());
        }
        return true;
    }

    public int read() throws IOException {
        if (this.uncompressedCursor < this.uncompressedLimit) {
            return this.uncompressed[this.uncompressedCursor++] & 0xFF;
        }
        if (this.hasNextChunk()) {
            return this.read();
        }
        return -1;
    }

    public int available() throws IOException {
        if (this.uncompressedCursor < this.uncompressedLimit) {
            return this.uncompressedLimit - this.uncompressedCursor;
        }
        if (this.hasNextChunk()) {
            return this.uncompressedLimit - this.uncompressedCursor;
        }
        return 0;
    }
}

