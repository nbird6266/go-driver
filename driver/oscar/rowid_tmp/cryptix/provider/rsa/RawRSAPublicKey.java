/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.rsa;

import cryptix.provider.rsa.BaseRSAPublicKey;
import cryptix.util.core.BI;
import cryptix.util.core.Debug;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;

public class RawRSAPublicKey
extends BaseRSAPublicKey {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("RSA", "RawRSAPublicKey");
    private static final PrintWriter err = Debug.getOutput();
    private static final BigInteger F4 = BigInteger.valueOf(65537L);

    private static void debug(String s) {
        err.println("RawRSAPublicKey: " + s);
    }

    public RawRSAPublicKey(BigInteger n, BigInteger e) {
        this.setRsaParams(n, e);
    }

    public RawRSAPublicKey(BigInteger n) {
        this(n, F4);
    }

    public RawRSAPublicKey(InputStream is) throws IOException {
        BigInteger e = BI.fromStream(is);
        BigInteger n = BI.fromStream(is);
        this.setRsaParams(n, e);
    }

    public String getFormat() {
        return "RAW";
    }

    public byte[] getEncoded() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        try {
            BI.toStream(this.getExponent(), bos);
            BI.toStream(this.getModulus(), bos);
            bos.flush();
            bos.close();
            return baos.toByteArray();
        }
        catch (IOException e) {
            return null;
        }
    }
}

