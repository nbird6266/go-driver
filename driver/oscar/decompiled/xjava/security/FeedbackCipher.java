/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

public interface FeedbackCipher {
    public void setInitializationVector(byte[] var1);

    public byte[] getInitializationVector();

    public int getInitializationVectorLength();
}

