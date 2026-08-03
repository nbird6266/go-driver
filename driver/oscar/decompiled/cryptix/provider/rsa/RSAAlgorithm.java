/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.rsa;

import cryptix.util.core.Debug;
import java.io.PrintWriter;
import java.math.BigInteger;

public final class RSAAlgorithm {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("RSA", "RSAAlgorithm");
    private static final PrintWriter err = Debug.getOutput();
    private static final BigInteger ONE = BigInteger.valueOf(1L);

    private RSAAlgorithm() {
    }

    private static void debug(String s) {
        err.println("RSAAlgorithm: " + s);
    }

    public static BigInteger rsa(BigInteger X, BigInteger n, BigInteger exp, BigInteger p, BigInteger q, BigInteger u) {
        if (p != null) {
            BigInteger q2;
            BigInteger p2;
            if (!u.equals(q.modInverse(p))) {
                BigInteger t = q;
                q = p;
                p = t;
            }
            if ((p2 = X.mod(p).modPow(exp.mod(p.subtract(ONE)), p)).equals(q2 = X.mod(q).modPow(exp.mod(q.subtract(ONE)), q))) {
                return q2;
            }
            BigInteger k = p2.subtract(q2).mod(p).multiply(u).mod(p);
            return q.multiply(k).add(q2);
        }
        return X.modPow(exp, n);
    }

    public static BigInteger rsa(BigInteger X, BigInteger n, BigInteger exp) {
        return X.modPow(exp, n);
    }
}

