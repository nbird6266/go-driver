/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.elgamal;

import cryptix.CryptixException;
import cryptix.util.core.Debug;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Random;

public final class ElGamalAlgorithm {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("ElGamal", "ElGamalAlgorithm");
    private static final PrintWriter err = Debug.getOutput();
    private static final BigInteger ZERO = BigInteger.valueOf(0L);
    private static final BigInteger ONE = BigInteger.valueOf(1L);

    private ElGamalAlgorithm() {
    }

    private static void debug(String s) {
        err.println("ElGamalAlgorithm: " + s);
    }

    public static void encrypt(BigInteger M, BigInteger[] ab, BigInteger p, BigInteger g, BigInteger y, Random rng) {
        BigInteger k;
        BigInteger p_minus_1 = p.subtract(ONE);
        do {
            if ((k = new BigInteger(p.bitLength() - 1, rng)).testBit(0)) continue;
            k = k.setBit(0);
        } while (!k.gcd(p_minus_1).equals(ONE));
        ab[0] = g.modPow(k, p);
        ab[1] = y.modPow(k, p).multiply(M).mod(p);
    }

    public static BigInteger decrypt(BigInteger a, BigInteger b, BigInteger p, BigInteger g, BigInteger x) {
        try {
            return b.multiply(a.modPow(x, p).modInverse(p)).mod(p);
        }
        catch (ArithmeticException e) {
            throw new CryptixException("ElGamal: " + e.getClass().getName() + " while calculating a.modPow(x, p).modInverse(p) - maybe key was" + " not generated properly?");
        }
    }

    public static void sign(BigInteger M, BigInteger[] ab, BigInteger p, BigInteger g, BigInteger x, Random rng) {
        BigInteger a;
        BigInteger k;
        BigInteger p_minus_1 = p.subtract(ONE);
        do {
            if ((k = new BigInteger(p.bitLength() - 1, rng)).testBit(0)) continue;
            k = k.setBit(0);
        } while (!k.gcd(p_minus_1).equals(ONE));
        ab[0] = a = g.modPow(k, p);
        try {
            ab[1] = k.modInverse(p_minus_1).multiply(M.subtract(x.multiply(a)).mod(p_minus_1)).mod(p_minus_1);
        }
        catch (ArithmeticException e) {
            throw new CryptixException("ElGamal: ArithmeticException while calculating k.modInverse(p-1)");
        }
    }

    public static boolean verify(BigInteger M, BigInteger a, BigInteger b, BigInteger p, BigInteger g, BigInteger y) {
        BigInteger p_minus_1 = p.subtract(ONE);
        if (M.compareTo(ZERO) < 0 || M.compareTo(p_minus_1) >= 0 || a.compareTo(ZERO) < 0 || a.compareTo(p_minus_1) >= 0 || b.compareTo(ZERO) < 0 || b.compareTo(p_minus_1) >= 0) {
            return false;
        }
        return y.modPow(a, p).multiply(a.modPow(b, p)).mod(p).equals(g.modPow(M, p));
    }
}

