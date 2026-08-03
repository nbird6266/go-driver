/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.fastpath;

public class FastpathArg {
    public static final int INT_TYPE = 1;
    public static final int LONG_TYPE = 2;
    public static final int BYTEARRAY_TYPE = 3;
    public int type;
    public int intValue;
    public long longValue;
    public byte[] bytes;

    public FastpathArg(int value) {
        this.type = 1;
        this.intValue = value;
    }

    public FastpathArg(long value) {
        this.type = 2;
        this.longValue = value;
    }

    public FastpathArg(byte[] bytes) {
        this.type = 3;
        this.bytes = bytes;
    }

    protected int sendSize() {
        int length = 0;
        switch (this.type) {
            case 1: {
                length = 4;
                break;
            }
            case 2: {
                length = 8;
                break;
            }
            case 3: {
                length = this.bytes.length;
            }
        }
        return length;
    }
}

