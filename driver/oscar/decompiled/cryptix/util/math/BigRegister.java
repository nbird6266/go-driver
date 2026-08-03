/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.math;

import cryptix.util.core.ArrayUtil;
import java.io.Serializable;
import java.security.SecureRandom;

public class BigRegister
implements Cloneable,
Serializable {
    public static final int MAXIMUM_SIZE = 4096;
    private static final byte[] log2x;
    private static final byte[] high;
    private static final byte[] low;
    private static final String[] binaryDigits;
    private static final String m_1 = "size < 2";
    private static final String m_2 = "size > MAXIMUM_SIZE";
    private static final SecureRandom prng;
    private byte[] bits;
    private int size;
    private static final long serialVersionUID = 2535877383275048954L;

    static {
        byte[] byArray = new byte[128];
        byArray[1] = 1;
        byArray[2] = 2;
        byArray[3] = 2;
        byArray[4] = 3;
        byArray[5] = 3;
        byArray[6] = 3;
        byArray[7] = 3;
        byArray[8] = 4;
        byArray[9] = 4;
        byArray[10] = 4;
        byArray[11] = 4;
        byArray[12] = 4;
        byArray[13] = 4;
        byArray[14] = 4;
        byArray[15] = 4;
        byArray[16] = 5;
        byArray[17] = 5;
        byArray[18] = 5;
        byArray[19] = 5;
        byArray[20] = 5;
        byArray[21] = 5;
        byArray[22] = 5;
        byArray[23] = 5;
        byArray[24] = 5;
        byArray[25] = 5;
        byArray[26] = 5;
        byArray[27] = 5;
        byArray[28] = 5;
        byArray[29] = 5;
        byArray[30] = 5;
        byArray[31] = 5;
        byArray[32] = 6;
        byArray[33] = 6;
        byArray[34] = 6;
        byArray[35] = 6;
        byArray[36] = 6;
        byArray[37] = 6;
        byArray[38] = 6;
        byArray[39] = 6;
        byArray[40] = 6;
        byArray[41] = 6;
        byArray[42] = 6;
        byArray[43] = 6;
        byArray[44] = 6;
        byArray[45] = 6;
        byArray[46] = 6;
        byArray[47] = 6;
        byArray[48] = 6;
        byArray[49] = 6;
        byArray[50] = 6;
        byArray[51] = 6;
        byArray[52] = 6;
        byArray[53] = 6;
        byArray[54] = 6;
        byArray[55] = 6;
        byArray[56] = 6;
        byArray[57] = 6;
        byArray[58] = 6;
        byArray[59] = 6;
        byArray[60] = 6;
        byArray[61] = 6;
        byArray[62] = 6;
        byArray[63] = 6;
        byArray[64] = 7;
        byArray[65] = 7;
        byArray[66] = 7;
        byArray[67] = 7;
        byArray[68] = 7;
        byArray[69] = 7;
        byArray[70] = 7;
        byArray[71] = 7;
        byArray[72] = 7;
        byArray[73] = 7;
        byArray[74] = 7;
        byArray[75] = 7;
        byArray[76] = 7;
        byArray[77] = 7;
        byArray[78] = 7;
        byArray[79] = 7;
        byArray[80] = 7;
        byArray[81] = 7;
        byArray[82] = 7;
        byArray[83] = 7;
        byArray[84] = 7;
        byArray[85] = 7;
        byArray[86] = 7;
        byArray[87] = 7;
        byArray[88] = 7;
        byArray[89] = 7;
        byArray[90] = 7;
        byArray[91] = 7;
        byArray[92] = 7;
        byArray[93] = 7;
        byArray[94] = 7;
        byArray[95] = 7;
        byArray[96] = 7;
        byArray[97] = 7;
        byArray[98] = 7;
        byArray[99] = 7;
        byArray[100] = 7;
        byArray[101] = 7;
        byArray[102] = 7;
        byArray[103] = 7;
        byArray[104] = 7;
        byArray[105] = 7;
        byArray[106] = 7;
        byArray[107] = 7;
        byArray[108] = 7;
        byArray[109] = 7;
        byArray[110] = 7;
        byArray[111] = 7;
        byArray[112] = 7;
        byArray[113] = 7;
        byArray[114] = 7;
        byArray[115] = 7;
        byArray[116] = 7;
        byArray[117] = 7;
        byArray[118] = 7;
        byArray[119] = 7;
        byArray[120] = 7;
        byArray[121] = 7;
        byArray[122] = 7;
        byArray[123] = 7;
        byArray[124] = 7;
        byArray[125] = 7;
        byArray[126] = 7;
        byArray[127] = 7;
        log2x = byArray;
        byte[] byArray2 = new byte[16];
        byArray2[2] = 1;
        byArray2[3] = 1;
        byArray2[4] = 2;
        byArray2[5] = 2;
        byArray2[6] = 2;
        byArray2[7] = 2;
        byArray2[8] = 3;
        byArray2[9] = 3;
        byArray2[10] = 3;
        byArray2[11] = 3;
        byArray2[12] = 3;
        byArray2[13] = 3;
        byArray2[14] = 3;
        byArray2[15] = 3;
        high = byArray2;
        byte[] byArray3 = new byte[16];
        byArray3[2] = 1;
        byArray3[4] = 2;
        byArray3[6] = 1;
        byArray3[8] = 3;
        byArray3[10] = 1;
        byArray3[12] = 2;
        byArray3[14] = 1;
        low = byArray3;
        binaryDigits = new String[]{"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};
        prng = new SecureRandom();
    }

    public BigRegister(int size) {
        if (size < 2) {
            throw new IllegalArgumentException(m_1);
        }
        if (size > 4096) {
            throw new IllegalArgumentException(m_2);
        }
        this.size = size;
        this.bits = new byte[(size + 7) / 8];
    }

    private BigRegister(BigRegister r) {
        this.size = r.size;
        this.bits = (byte[])r.bits.clone();
    }

    public synchronized Object clone() {
        return new BigRegister(this);
    }

    public synchronized void and(BigRegister source) {
        if (this.size != source.size) {
            throw new IllegalArgumentException();
        }
        int i = 0;
        while (i < this.bits.length) {
            int n = i;
            this.bits[n] = (byte)(this.bits[n] & source.bits[i]);
            ++i;
        }
    }

    public synchronized void andNot(BigRegister source) {
        if (this.size != source.size) {
            throw new IllegalArgumentException();
        }
        int i = 0;
        while (i < this.bits.length) {
            int n = i;
            this.bits[n] = (byte)(this.bits[n] & ~source.bits[i]);
            ++i;
        }
    }

    public synchronized void or(BigRegister source) {
        if (this.size != source.size) {
            throw new IllegalArgumentException();
        }
        int i = 0;
        while (i < this.bits.length) {
            int n = i;
            this.bits[n] = (byte)(this.bits[n] | source.bits[i]);
            ++i;
        }
        this.pad();
    }

    public synchronized void not() {
        int i = 0;
        while (i < this.bits.length) {
            this.bits[i] = ~this.bits[i];
            ++i;
        }
        this.pad();
    }

    public synchronized void xor(BigRegister source) {
        if (this.size != source.size) {
            throw new IllegalArgumentException();
        }
        int i = 0;
        while (i < this.bits.length) {
            int n = i;
            this.bits[n] = (byte)(this.bits[n] ^ source.bits[i]);
            ++i;
        }
        this.pad();
    }

    public synchronized void shiftLeft(int n) {
        if (n == 0) {
            return;
        }
        if (n < 0) {
            this.shiftRight(-n);
            return;
        }
        if (n >= this.size) {
            this.reset();
            return;
        }
        int start = this.lowestSetBit();
        if (start == -1) {
            return;
        }
        if (start >= this.size - n) {
            this.reset();
            return;
        }
        start = n / 8;
        int offset = n % 8;
        int length = this.bits.length;
        byte[] result = new byte[length];
        if (offset == 0) {
            System.arraycopy(this.bits, 0, result, start, length - start);
        } else {
            int offsetBar = 8 - offset;
            int i = start;
            int j = 0;
            while (i < length) {
                result[i] = (byte)(this.bits[j] << offset | (j == 0 ? 0 : (this.bits[j - 1] & 0xFF) >>> offsetBar));
                ++i;
                ++j;
            }
        }
        this.bits = result;
        this.pad();
    }

    public synchronized void shiftRight(int n) {
        if (n == 0) {
            return;
        }
        if (n < 0) {
            this.shiftLeft(-n);
            return;
        }
        if (n >= this.size) {
            this.reset();
            return;
        }
        int start = this.highestSetBit();
        if (start < 0) {
            return;
        }
        if (start < n) {
            this.reset();
            return;
        }
        start = n / 8;
        int offset = n % 8;
        int length = this.bits.length;
        byte[] result = new byte[length];
        if (offset == 0) {
            System.arraycopy(this.bits, start, result, 0, length - start);
        } else {
            int i = 0;
            int j = start;
            while (i < length && j < length) {
                result[i] = (byte)(((j == length - 1 ? 0 : this.bits[j + 1] << 8) | this.bits[j] & 0xFF) >>> offset);
                ++i;
                ++j;
            }
        }
        this.bits = result;
        this.pad();
    }

    public synchronized void rotateLeft(int n) {
        if ((n %= this.size) == 0) {
            return;
        }
        if (n < 0) {
            this.rotateRight(-n);
        } else {
            BigRegister same = (BigRegister)this.clone();
            same.shiftRight(this.size - n);
            this.shiftLeft(n);
            this.or(same);
        }
    }

    public synchronized void rotateRight(int n) {
        if ((n %= this.size) == 0) {
            return;
        }
        if (n < 0) {
            this.rotateLeft(-n);
        } else {
            BigRegister same = (BigRegister)this.clone();
            same.shiftLeft(this.size - n);
            this.shiftRight(n);
            this.or(same);
        }
    }

    public synchronized void invertOrder() {
        byte[] result = new byte[this.bits.length];
        int i = 0;
        int j = this.size - 1;
        while (i < this.size) {
            if (this.testBit(i)) {
                int n = j / 8;
                result[n] = (byte)(result[n] | 1 << j % 8);
            }
            ++i;
            --j;
        }
        this.bits = result;
    }

    public synchronized boolean testBit(int n) {
        if (n < 0 || n > this.size) {
            throw new IllegalArgumentException();
        }
        return (this.bits[n / 8] & 1 << n % 8) != 0;
    }

    public synchronized boolean isSameValue(BigRegister x) {
        if (x.size != this.size) {
            return false;
        }
        return ArrayUtil.areEqual(this.bits, x.bits);
    }

    public synchronized int compareTo(BigRegister x) {
        if (this.size > x.size) {
            return 1;
        }
        if (this.size < x.size) {
            return -1;
        }
        return ArrayUtil.compared(this.bits, x.bits, true);
    }

    public synchronized void setBit(int n) {
        if (n < 0 || n > this.size) {
            throw new IllegalArgumentException();
        }
        int n2 = n / 8;
        this.bits[n2] = (byte)(this.bits[n2] | 1 << n % 8);
    }

    public synchronized void setBits(int n, int count, long value) {
        if (n < 0 || n > this.size || count < 1 || count > 64 || n + count > this.size) {
            throw new IllegalArgumentException();
        }
        int i = 0;
        int j = n;
        while (i < count) {
            if ((value & 1L) == 1L) {
                int n2 = j / 8;
                this.bits[n2] = (byte)(this.bits[n2] | 1 << j % 8);
            }
            value >>>= 1;
            ++i;
            ++j;
        }
    }

    public synchronized void clearBit(int n) {
        if (n < 0 || n > this.size) {
            throw new IllegalArgumentException();
        }
        int n2 = n / 8;
        this.bits[n2] = (byte)(this.bits[n2] & ~(1 << n % 8));
    }

    public synchronized void flipBit(int n) {
        if (n < 0 || n > this.size) {
            throw new IllegalArgumentException();
        }
        int n2 = n / 8;
        this.bits[n2] = (byte)(this.bits[n2] ^ 1 << n % 8);
    }

    public synchronized int getBit(int n) {
        if (n < 0 || n > this.size) {
            throw new IllegalArgumentException();
        }
        return (this.bits[n / 8] & 0xFF) >> n % 8 & 1;
    }

    public synchronized long getBits(int n, int count) {
        if (n < 0 || n > this.size || count < 1 || count > 64 || n + count > this.size) {
            throw new IllegalArgumentException();
        }
        long result = 0L;
        int i = 0;
        int j = n + count - 1;
        while (i < count) {
            result = result << 1 | (long)((this.bits[j / 8] & 0xFF) >> j % 8 & 1);
            ++i;
            --j;
        }
        return result;
    }

    public synchronized int byteValue() {
        return this.bits[0] & 0xFF;
    }

    public synchronized int intValue() {
        int offset = 0;
        int result = this.bits[offset++] & 0xFF;
        try {
            result |= (this.bits[offset++] & 0xFF) << 8 | (this.bits[offset++] & 0xFF) << 16 | (this.bits[offset] & 0xFF) << 24;
        }
        catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
            // empty catch block
        }
        return result;
    }

    public synchronized long longValue() {
        int offset = 0;
        long result = (long)this.bits[offset++] & 0xFFL;
        try {
            result |= ((long)this.bits[offset++] & 0xFFL) << 8 | ((long)this.bits[offset++] & 0xFFL) << 16 | ((long)this.bits[offset++] & 0xFFL) << 24 | ((long)this.bits[offset++] & 0xFFL) << 32 | ((long)this.bits[offset++] & 0xFFL) << 40 | ((long)this.bits[offset++] & 0xFFL) << 48 | ((long)this.bits[offset] & 0xFFL) << 56;
        }
        catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
            // empty catch block
        }
        return result;
    }

    public synchronized BigRegister valueOf(long n) {
        BigRegister result = new BigRegister(this.size);
        int limit = Math.min(8, this.bits.length);
        int i = 0;
        while (i < limit) {
            result.bits[i] = (byte)(n >>> 8 * i);
            ++i;
        }
        result.pad();
        return result;
    }

    public synchronized void reset() {
        ArrayUtil.clear(this.bits);
    }

    public synchronized void atRandom() {
        this.atRandom(prng);
    }

    public synchronized void atRandom(SecureRandom source) {
        source.nextBytes(this.bits);
        this.pad();
    }

    public synchronized void load(BigRegister source) {
        if (this.size != source.size) {
            throw new IllegalArgumentException();
        }
        System.arraycopy(source.bits, 0, this.bits, 0, this.bits.length);
    }

    public synchronized void load(byte[] source) {
        int length = source.length;
        int limit = this.bits.length;
        if (length > limit) {
            throw new IllegalArgumentException();
        }
        System.arraycopy(source, 0, this.bits, 0, length);
        if (length < limit) {
            ArrayUtil.clear(this.bits, length, limit - length);
        }
        this.pad();
    }

    public synchronized byte[] toByteArray() {
        return (byte[])this.bits.clone();
    }

    public synchronized int getSize() {
        return this.size;
    }

    public synchronized int countSetBits() {
        int count = 0;
        int limit = this.bits.length;
        int i = 0;
        while (i < limit) {
            byte j = this.bits[i];
            count += j < 0 ? 8 : log2x[j & 0xFF];
            ++i;
        }
        return count;
    }

    public synchronized int highestSetBit() {
        int i = this.bits.length - 1;
        while (i > 0 && this.bits[i] == 0) {
            --i;
        }
        if (this.bits[i] == 0) {
            return -1;
        }
        int b = this.bits[i] >>> 4 & 0xF;
        int j = 4;
        if (b == 0) {
            b = this.bits[i] & 0xF;
            j -= 4;
        }
        return i * 8 + (j += high[b]);
    }

    public synchronized int lowestSetBit() {
        int i = 0;
        int limit = this.bits.length;
        while (i < limit && this.bits[i] == 0) {
            ++i;
        }
        if (i == limit) {
            return -1;
        }
        int b = this.bits[i] & 0xF;
        int j = 0;
        if (b == 0) {
            b = this.bits[i] >>> 4 & 0xF;
            j += 4;
        }
        return i * 8 + (j += low[b]);
    }

    public synchronized String toString() {
        int b;
        int i;
        String s;
        StringBuffer sb = new StringBuffer(8 * this.bits.length + 64);
        sb.append("Binary dump of a BigRegister [").append(this.size).append("-bit]...\n");
        sb.append("Byte #:|........|........|........|........|........|........|........|........|\n");
        int k = this.bits.length;
        int first = k-- % 8;
        if (first != 0) {
            s = "      " + String.valueOf(this.bits.length);
            sb.append(s.substring(s.length() - 6)).append(':');
            i = 0;
            while (i < 8 - first) {
                sb.append("         ");
                ++i;
            }
            i = 0;
            while (i < first) {
                b = this.bits[k--] & 0xFF;
                sb.append(' ').append(binaryDigits[b >>> 4 & 0xF]).append(binaryDigits[b & 0xF]);
                ++i;
            }
            sb.append('\n');
        }
        int lines = (k + 1) / 8;
        i = 0;
        while (i < lines) {
            s = "      " + String.valueOf(8 * (lines - i));
            sb.append(s.substring(s.length() - 6)).append(':');
            int j = 0;
            while (j < 8) {
                b = this.bits[k--] & 0xFF;
                sb.append(' ').append(binaryDigits[b >>> 4 & 0xF]).append(binaryDigits[b & 0xF]);
                ++j;
            }
            sb.append('\n');
            ++i;
        }
        sb.append('\n');
        return sb.toString();
    }

    private synchronized void pad() {
        int n = 8 - this.size % 8;
        if (n != 8) {
            int n2 = this.bits.length - 1;
            this.bits[n2] = (byte)(this.bits[n2] & 255 >>> n);
        }
    }
}

