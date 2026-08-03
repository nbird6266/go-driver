/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

public interface Padding {
    public int pad(byte[] var1, int var2, int var3);

    public int unpad(byte[] var1, int var2, int var3);

    public int padLength(int var1);

    public String paddingScheme();
}

