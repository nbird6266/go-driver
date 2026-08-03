/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.md;

import cryptix.CryptixException;
import java.security.MessageDigest;

abstract class BlockMessageDigest
extends MessageDigest {
    private byte[] buffer;
    private int buffered;
    private long count;
    private static final long MAX_COUNT = 0x1FFFFFFFFFFFFFFFL;
    private int data_length = this.engineGetDataLength();

    protected BlockMessageDigest(String algorithm) {
        super(algorithm);
        this.buffer = new byte[this.data_length];
    }

    protected long bitcount() {
        return this.count * 8L;
    }

    protected void engineReset() {
        this.buffered = 0;
        this.count = 0L;
    }

    protected void engineUpdate(byte b) {
        byte[] data = new byte[]{b};
        this.engineUpdate(data, 0, 1);
    }

    protected void engineUpdate(byte[] data, int offset, int length) {
        int remainder;
        this.count += (long)length;
        if (this.count > 0x1FFFFFFFFFFFFFFFL) {
            throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Maximum input length exceeded");
        }
        int datalen = this.data_length;
        while (length >= (remainder = datalen - this.buffered)) {
            System.arraycopy(data, offset, this.buffer, this.buffered, remainder);
            this.engineTransform(this.buffer);
            length -= remainder;
            offset += remainder;
            this.buffered = 0;
        }
        if (length > 0) {
            System.arraycopy(data, offset, this.buffer, this.buffered, length);
            this.buffered += length;
        }
    }

    protected byte[] engineDigest() {
        return this.engineDigest(this.buffer, this.buffered);
    }

    protected abstract byte[] engineDigest(byte[] var1, int var2);

    protected abstract void engineTransform(byte[] var1);

    protected abstract int engineGetDataLength();
}

