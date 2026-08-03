/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.checksum;

import java.util.zip.Checksum;

public class PRZ24
implements Checksum {
    private static final int[] table = new int[256];
    private static final int CRC_BITS = 24;
    private static final int CRC_HIGH_BIT = 0x1000000;
    private static final int CRC_MASK = 0xFFFFFF;
    private static final int PRZ_CRC = 8801531;
    private static final int START_VALUE = 11994318;
    private int prz24;

    static {
        PRZ24.table[0] = 0;
        PRZ24.table[1] = 8801531;
        int i = 1;
        int j = 2;
        while (i < 128) {
            int t = table[i] << 1;
            if ((t & 0x1000000) != 0) {
                PRZ24.table[j++] = t ^ 0x864CFB;
                PRZ24.table[j++] = t;
            } else {
                PRZ24.table[j++] = t;
                PRZ24.table[j++] = t ^ 0x864CFB;
            }
            ++i;
        }
    }

    public PRZ24() {
        this.reset();
    }

    public void reset() {
        this.prz24 = 11994318;
    }

    public void update(byte[] buffer, int offset, int length) {
        if (length + offset > buffer.length) {
            length = buffer.length - offset;
        }
        while (length-- > 0) {
            this.update(buffer[offset++]);
        }
    }

    public void update(int n) {
        this.prz24 = (this.prz24 << 8 ^ table[this.prz24 >>> 16 ^ n & 0xFF]) & 0xFFFFFF;
    }

    public long getValue() {
        return (long)this.prz24 & 0xFFFFFFFFL;
    }
}

