/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.crypto.DHPrivateKey;
import com.claymoresystems.crypto.DHPublicKey;
import com.claymoresystems.ptls.SSLDebug;
import java.math.BigInteger;
import java.security.SecureRandom;

public class SSLDHPrivateKey
extends DHPrivateKey {
    private SecureRandom rand;
    private static int PRIME_CERTAINTY = 80;

    public void initPrivateKey(BigInteger g_, BigInteger p_, SecureRandom rand_) {
        this.g = g_;
        this.p = p_;
        this.rand = rand_;
        this.generatePrivate();
    }

    public void initPrivateKey(SecureRandom rand_, int keylength, boolean sg) {
        BigInteger p_tmp = null;
        BigInteger padd = null;
        BigInteger twofer = new BigInteger("24");
        BigInteger eleven = new BigInteger("11");
        BigInteger zero = new BigInteger("0");
        if (keylength % 8 != 0) {
            throw new InternalError("keylength must be a multiple of 8");
        }
        if (keylength < 768) {
            throw new InternalError("Keylength must be minimum 768");
        }
        int l = keylength / 8;
        byte[] p_bytes = new byte[l];
        if (sg) {
            rand_.nextBytes(p_bytes);
            for (int i = 0; i < 8; ++i) {
                p_bytes[i] = -1;
                p_bytes[l - (i + 1)] = -1;
            }
            BigInteger padd_tmp = new BigInteger("0");
            padd = padd_tmp.setBit(64);
            p_tmp = new BigInteger(1, p_bytes);
        }
        this.g = new BigInteger("2");
        while (true) {
            if (sg) {
                BigInteger p_hold = p_tmp.add(padd);
                if (!(p_tmp = p_hold).isProbablePrime(2)) continue;
                SSLDebug.debug(8, "p passes quick check");
                BigInteger q = p_tmp.shiftRight(1);
                if (!q.isProbablePrime(PRIME_CERTAINTY)) continue;
                SSLDebug.debug(8, "q is prime");
            } else {
                rand_.nextBytes(p_bytes);
                p_bytes[0] = (byte)(p_bytes[0] | 0xFFFFFF80);
                int n = l - 1;
                p_bytes[n] = (byte)(p_bytes[n] | 1);
                p_tmp = new BigInteger(1, p_bytes);
                BigInteger md = p_tmp.mod(twofer);
                BigInteger tmp = p_tmp.subtract(md);
                p_tmp = eleven.add(tmp);
            }
            SSLDebug.debug(8, "p candidate", p_tmp.toByteArray());
            if (p_tmp.isProbablePrime(PRIME_CERTAINTY)) break;
            SSLDebug.debug(8, "p not prime");
        }
        this.rand = rand_;
        this.p = p_tmp;
        BigInteger mod24 = this.p.mod(twofer);
        SSLDebug.debug(8, "P is prime" + this.p.isProbablePrime(PRIME_CERTAINTY) + "mod 24=" + mod24);
        SSLDebug.debug(8, "p", this.p.toByteArray());
        this.generatePrivate();
    }

    public byte[] keyAgree(DHPublicKey pub, boolean check) {
        if (check) {
            BigInteger pp = pub.getp();
            BigInteger pg = pub.getg();
            if (pg != null || pp != null) {
                if (this.g.compareTo(pg) != 0) {
                    throw new Error("DH parameters don't match (g)");
                }
                if (this.p.compareTo(pp) != 0) {
                    throw new Error("DH parameters don't match (p)");
                }
            }
        }
        BigInteger pY = pub.getY();
        return this.toBytes(pY.modPow(this.X, this.p));
    }

    private void generatePrivate() {
        int bits = this.p.bitLength();
        int bytelen = bits / 8;
        int bitr = bits % 8;
        if (bitr > 0) {
            ++bytelen;
        }
        byte[] buf = new byte[bytelen];
        this.rand.nextBytes(buf);
        int shift = bitr > 0 ? 8 - bitr : 1;
        buf[0] = (byte)(buf[0] & (byte)(255 >> shift));
        this.X = new BigInteger(1, buf);
        SSLDebug.debug(8, "DH private", buf);
        this.Y = this.g.modPow(this.X, this.p);
    }
}

