/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.sslg;

import com.claymoresystems.sslg.DistinguishedName;
import java.math.BigInteger;
import java.util.Date;
import java.util.Vector;

public interface Certificate {
    public byte[] getDER();

    public byte[] getIssuerDER();

    public BigInteger getSerial();

    public byte[] getSubjectDER();

    public DistinguishedName getSubjectName();

    public DistinguishedName getIssuerName();

    public Date getValidityNotBefore();

    public Date getValidityNotAfter();

    public Vector getExtensions();
}

