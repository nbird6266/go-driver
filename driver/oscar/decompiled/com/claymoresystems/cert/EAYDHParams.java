/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.DERUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

public class EAYDHParams {
    private BigInteger g;
    private BigInteger p;

    public EAYDHParams(BigInteger g, BigInteger p) {
        this.p = p;
        this.g = g;
    }

    public EAYDHParams(byte[] encoding) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(encoding);
        byte[] seq = DERUtils.decodeSequence(is);
        is = new ByteArrayInputStream(seq);
        this.p = DERUtils.decodeInteger(is);
        this.g = DERUtils.decodeInteger(is);
    }

    public BigInteger getG() {
        return this.g;
    }

    public BigInteger getP() {
        return this.p;
    }

    public byte[] getEncoded() {
        ByteArrayOutputStream os;
        try {
            os = new ByteArrayOutputStream();
            DERUtils.encodeInteger(this.p, os);
            DERUtils.encodeInteger(this.g, os);
            byte[] tmp = os.toByteArray();
            os.reset();
            DERUtils.encodeSequence(tmp, (OutputStream)os);
        }
        catch (IOException e) {
            throw new InternalError(e.toString());
        }
        return os.toByteArray();
    }
}

