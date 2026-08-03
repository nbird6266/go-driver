/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.provider;

import cryptix.provider.rsa.RSAAlgorithm;
import java.math.BigInteger;
import java.security.SecureRandom;

class RSAAlgorithmBlind {
    RSAAlgorithmBlind() {
    }

    static BigInteger rsa(BigInteger X, BigInteger n, BigInteger exp, BigInteger otherExp, BigInteger p, BigInteger q, BigInteger u, SecureRandom blindingRNG) {
        BigInteger r_inv = null;
        if (blindingRNG != null) {
            BigInteger r = new BigInteger(n.bitLength() - 1, blindingRNG);
            BigInteger r_to_e = r.modPow(otherExp, n);
            X = X.multiply(r_to_e).mod(n);
            r_inv = r.modInverse(n);
        }
        BigInteger result = RSAAlgorithm.rsa(X, n, exp, p, q, u);
        if (blindingRNG == null) {
            return result;
        }
        return result.multiply(r_inv).mod(n);
    }
}

