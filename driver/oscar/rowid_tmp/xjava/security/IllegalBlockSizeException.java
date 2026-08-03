/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

public class IllegalBlockSizeException
extends RuntimeException {
    public int blockSize;
    public int dataSize;

    public int getBlockSize() {
        return this.blockSize;
    }

    public int getDataSize() {
        return this.dataSize;
    }

    public IllegalBlockSizeException(String message) {
        super(message);
    }

    public IllegalBlockSizeException(int blockSize, int dataSize) {
        super("blockSize = " + blockSize + ", dataSize = " + dataSize);
        this.blockSize = blockSize;
        this.dataSize = dataSize;
    }

    public IllegalBlockSizeException(int blockSize, int dataSize, String message) {
        super(message);
        this.blockSize = blockSize;
        this.dataSize = dataSize;
    }
}

