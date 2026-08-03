/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.sslg;

import java.util.Vector;

public interface DistinguishedName {
    public byte[] getNameDER();

    public Vector getName();

    public String getNameString();
}

