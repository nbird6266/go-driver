/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.rsa;

import cryptix.provider.rsa.RSAAlgorithm;
import cryptix.provider.rsa.RawRSAPrivateKey;
import cryptix.provider.rsa.RawRSAPublicKey;
import cryptix.util.core.BI;
import cryptix.util.core.Debug;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import xjava.security.interfaces.RSAKeyPairGenerator;

public class BaseRSAKeyPairGenerator
extends KeyPairGenerator
implements RSAKeyPairGenerator {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("RSA", "BaseRSAKeyPairGenerator");
    private static final PrintWriter err = Debug.getOutput();
    private int strength;
    private BigInteger e;
    private SecureRandom source;
    private static final int CONFIDENCE = 80;
    private static final BigInteger F4 = BigInteger.valueOf(65537L);
    private static final BigInteger ONE = BigInteger.valueOf(1L);
    private static final int DEFAULT_STRENGTH = 1024;

    private static void debug(String s) {
        err.println("BaseRSAKeyPairGenerator: " + s);
    }

    public BaseRSAKeyPairGenerator() {
        super("RSA");
    }

    public void initialize(int strength, BigInteger e, SecureRandom source) {
        this.e = e == null ? F4 : e;
        this.strength = strength < 2 ? 1024 : strength;
        this.source = source == null ? new SecureRandom() : source;
    }

    public void initialize(int strength, SecureRandom source) {
        this.initialize(strength, F4, source);
    }

    public KeyPair generateKeyPair() {
        BigInteger d;
        BigInteger n;
        BigInteger q;
        BigInteger p;
        int k1 = this.strength / 2;
        int k2 = this.strength - k1;
        long t1 = 0L;
        if (debuglevel >= 7) {
            t1 = System.currentTimeMillis();
        }
        while (true) {
            try {
                do {
                    p = new BigInteger(k1, 80, this.source);
                    q = new BigInteger(k2, 80, this.source);
                    n = p.multiply(q);
                } while (p.compareTo(q) == 0 || n.bitLength() != this.strength);
                BigInteger phi = p.subtract(ONE).multiply(q.subtract(ONE));
                d = this.e.modInverse(phi);
            }
            catch (ArithmeticException arithmeticException) {
                continue;
            }
            break;
        }
        if (debuglevel >= 7) {
            t1 = System.currentTimeMillis() - t1;
            BaseRSAKeyPairGenerator.debug(" ...generateKeyPair() completed in " + t1 + " ms.");
        }
        if (debuglevel >= 5) {
            try {
                err.print("RSA parameters self test #1/2... ");
                BigInteger x = new BigInteger(k1, this.source);
                BigInteger y = RSAAlgorithm.rsa(x, n, this.e);
                BigInteger z = RSAAlgorithm.rsa(y, n, d);
                boolean yes = z.compareTo(x) == 0;
                err.println(yes ? "OK" : "Failed");
                if (!yes) {
                    throw new RuntimeException();
                }
                err.print("RSA parameters self test #2/2... ");
                BigInteger u = q.modInverse(p);
                z = RSAAlgorithm.rsa(y, n, d, p, q, u);
                yes = z.compareTo(x) == 0;
                err.println(yes ? "OK" : "Failed");
                if (!yes) {
                    throw new RuntimeException();
                }
                err.println();
            }
            catch (Exception ex) {
                err.println("RSA parameters:");
                err.println("         n: " + BI.dumpString(n));
                err.println("         e: " + BI.dumpString(this.e));
                err.println("         d: " + BI.dumpString(d));
                err.println("         p: " + BI.dumpString(p));
                err.println("         q: " + BI.dumpString(q));
                err.println("q^-1 mod p: " + BI.dumpString(q.modInverse(p)));
                throw new RuntimeException(this.e.toString());
            }
        }
        return this.makeKeyPair(n, this.e, d, p, q);
    }

    protected KeyPair makeKeyPair(BigInteger n, BigInteger e, BigInteger d, BigInteger p, BigInteger q) {
        RawRSAPublicKey pk = new RawRSAPublicKey(n, e);
        RawRSAPrivateKey sk = new RawRSAPrivateKey(d, p, q);
        return new KeyPair(pk, sk);
    }

    public void initialize() {
        this.initialize(1024, F4, new SecureRandom());
    }
}

