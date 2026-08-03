/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.sslg;

public interface Extension {
    public byte[] getOID();

    public boolean isCritical();

    public byte[] getValue();
}

