/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.util.core.ArrayUtil;
import java.security.Key;

public class RawKey
implements Key {
    private static final String FORMAT = "RAW";
    private String algorithm;
    private byte[] data;

    public RawKey(String algorithm, byte[] data) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        this.algorithm = algorithm;
        this.data = (byte[])data.clone();
    }

    public RawKey(String algorithm, byte[] data, int offset, int length) {
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        this.algorithm = algorithm;
        this.data = new byte[length];
        System.arraycopy(data, offset, this.data, 0, length);
    }

    public int hashCode() {
        int h = 0;
        int i = 0;
        while (i < this.data.length) {
            h ^= this.data[i];
            i += 4;
        }
        h <<= 8;
        i = 1;
        while (i < this.data.length) {
            h ^= this.data[i];
            i += 4;
        }
        h <<= 8;
        i = 2;
        while (i < this.data.length) {
            h ^= this.data[i];
            i += 4;
        }
        h <<= 8;
        i = 3;
        while (i < this.data.length) {
            h ^= this.data[i];
            i += 4;
        }
        return h;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof RawKey) {
            return ArrayUtil.areEqual(this.data, ((RawKey)obj).data);
        }
        return false;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public String getFormat() {
        return FORMAT;
    }

    public byte[] getEncoded() {
        return (byte[])this.data.clone();
    }
}

