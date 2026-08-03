/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.rsa;

import cryptix.util.core.BI;
import cryptix.util.core.Debug;
import java.io.PrintWriter;
import java.math.BigInteger;
import xjava.security.interfaces.CryptixRSAPublicKey;

public abstract class BaseRSAPublicKey
implements CryptixRSAPublicKey {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("RSA", "BaseRSAPublicKey");
    private static final PrintWriter err = Debug.getOutput();
    private BigInteger n;
    private BigInteger e;

    private static void debug(String s) {
        err.println("BaseRSAPublicKey: " + s);
    }

    protected BaseRSAPublicKey() {
    }

    public BigInteger getModulus() {
        return this.n;
    }

    public BigInteger getExponent() {
        return this.e;
    }

    public String getAlgorithm() {
        return "RSA";
    }

    protected void setRsaParams(BigInteger n, BigInteger e) {
        if (n == null) {
            throw new NullPointerException("n == null");
        }
        if (e == null) {
            throw new NullPointerException("e == null");
        }
        this.n = n;
        this.e = e;
    }

    public String toString() {
        return "<----- RSAPublicKey:\n         n: " + BI.dumpString(this.n) + "         e: " + BI.dumpString(this.e) + "----->\n";
    }
}

