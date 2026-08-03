/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.crypto;

import com.claymoresystems.crypto.DHPublicKey;
import com.claymoresystems.ptls.LoadProviders;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.SecureRandom;

public abstract class DHPrivateKey
implements PrivateKey {
    protected BigInteger X;
    protected BigInteger Y;
    protected BigInteger g;
    protected BigInteger p;

    public abstract void initPrivateKey(BigInteger var1, BigInteger var2, SecureRandom var3);

    public abstract void initPrivateKey(SecureRandom var1, int var2, boolean var3);

    public abstract byte[] keyAgree(DHPublicKey var1, boolean var2);

    public static DHPrivateKey getInstance() {
        String clazz = LoadProviders.haveGoNativeProvider() ? "com.claymoresystems.gnp.OSDHPrivateKey" : "com.claymoresystems.ptls.SSLDHPrivateKey";
        try {
            Class<?> cl = Class.forName(clazz);
            return (DHPrivateKey)cl.newInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new InternalError("Couldn't find DH class" + clazz);
        }
    }

    protected byte[] toBytes(BigInteger num) {
        byte[] tmp = num.toByteArray();
        int i = 0;
        for (i = 0; i < tmp.length && tmp[i] == 0; ++i) {
        }
        int totrim = i;
        if (totrim > 0) {
            byte[] trim = new byte[tmp.length - totrim];
            if (totrim == tmp.length) {
                throw new InternalError("Can't trim whole array");
            }
            System.arraycopy(tmp, totrim, trim, 0, tmp.length - totrim);
            return trim;
        }
        return tmp;
    }

    public BigInteger getX() {
        return this.X;
    }

    public BigInteger getY() {
        return this.Y;
    }

    public byte[] getYBytes() {
        return this.toBytes(this.Y);
    }

    public BigInteger getg() {
        return this.g;
    }

    public byte[] getgBytes() {
        return this.toBytes(this.g);
    }

    public BigInteger getp() {
        return this.p;
    }

    public byte[] getpBytes() {
        return this.toBytes(this.p);
    }

    public String getFormat() {
        return "RAW";
    }

    public byte[] getEncoded() {
        return null;
    }

    public String getAlgorithm() {
        return "DH";
    }
}

