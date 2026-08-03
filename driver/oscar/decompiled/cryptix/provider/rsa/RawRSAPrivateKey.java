/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.rsa;

import cryptix.provider.rsa.BaseRSAPrivateKey;
import cryptix.util.core.BI;
import cryptix.util.core.Debug;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;

public class RawRSAPrivateKey
extends BaseRSAPrivateKey {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("RSA", "RawRSAPrivateKey");
    private static final PrintWriter err = Debug.getOutput();

    private static void debug(String s) {
        err.println("RawRSAPrivateKey: " + s);
    }

    public RawRSAPrivateKey(BigInteger n, BigInteger d) {
        this.setRsaParams(n, d);
    }

    public RawRSAPrivateKey(BigInteger d, BigInteger p, BigInteger q) {
        this.setRsaParams(d, p, q, null);
    }

    public RawRSAPrivateKey(BigInteger d, BigInteger p, BigInteger q, BigInteger u) {
        this.setRsaParams(d, p, q, u);
    }

    public RawRSAPrivateKey(InputStream is) throws IOException {
        BigInteger d = BI.fromStream(is);
        BigInteger p = BI.fromStream(is);
        BigInteger q = BI.fromStream(is);
        BigInteger u = BI.fromStream(is);
        this.setRsaParams(d, p, q, u);
    }

    public String getFormat() {
        return "RAW";
    }

    public byte[] getEncoded() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        try {
            BI.toStream(this.getExponent(), bos);
            BI.toStream(this.getP(), bos);
            BI.toStream(this.getQ(), bos);
            BI.toStream(this.getInverseOfQModP(), bos);
            bos.flush();
            bos.close();
            return baos.toByteArray();
        }
        catch (IOException e) {
            return null;
        }
    }
}

