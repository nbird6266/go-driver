/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.rsa;

import cryptix.util.core.BI;
import cryptix.util.core.Debug;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.RSAFactors;

public abstract class BaseRSAPrivateKey
implements CryptixRSAPrivateKey,
RSAFactors {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("RSA", "BaseRSAPrivateKey");
    private static final PrintWriter err = Debug.getOutput();
    private static final BigInteger ZERO = BigInteger.valueOf(0L);
    private static final BigInteger ONE = BigInteger.valueOf(1L);
    private BigInteger n;
    private BigInteger d;
    private BigInteger p;
    private BigInteger q;
    private BigInteger u;

    private static void debug(String s) {
        err.println("BaseRSAPrivateKey: " + s);
    }

    protected BaseRSAPrivateKey() {
    }

    public BigInteger getModulus() {
        return this.n;
    }

    public BigInteger getExponent() {
        return this.d;
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getQ() {
        return this.q;
    }

    public BigInteger getInverseOfQModP() {
        return this.u;
    }

    public String getAlgorithm() {
        return "RSA";
    }

    protected void setRsaParams(BigInteger n, BigInteger d) {
        if (n == null) {
            throw new NullPointerException("n == null");
        }
        if (d == null) {
            throw new NullPointerException("d == null");
        }
        this.n = n;
        this.d = d;
    }

    protected void setRsaParams(BigInteger d, BigInteger p, BigInteger q, BigInteger u) {
        if (d == null) {
            throw new NullPointerException("d == null");
        }
        this.n = p.multiply(q);
        this.d = d;
        this.p = p;
        this.q = q;
        if (u != null && !u.multiply(q).mod(p).equals(ONE)) {
            if (debuglevel >= 1) {
                BaseRSAPrivateKey.debug("uq != 1 (mod p)");
            }
            u = null;
        }
        if (u == null) {
            try {
                u = q.modInverse(p);
            }
            catch (ArithmeticException ae) {
                if (debuglevel >= 1) {
                    if (p.compareTo(ZERO) <= 0) {
                        BaseRSAPrivateKey.debug("p <= 0");
                    }
                    if (p.equals(q)) {
                        BaseRSAPrivateKey.debug("p == q");
                    }
                    if (!p.isProbablePrime(80)) {
                        BaseRSAPrivateKey.debug("p is composite");
                    }
                    if (!q.isProbablePrime(80)) {
                        BaseRSAPrivateKey.debug("q is composite");
                    }
                }
                throw new InvalidParameterException("gcd(q, p) != 1");
            }
        }
        this.u = u;
    }

    public String toString() {
        if (debuglevel >= 5) {
            return "<----- RSAPrivateKey:\n         d: " + BI.dumpString(this.d) + "         p: " + BI.dumpString(this.p) + "         q: " + BI.dumpString(this.q) + "q^-1 mod p: " + BI.dumpString(this.u) + "----->\n";
        }
        return "<BaseRSAPrivateKey>";
    }
}

