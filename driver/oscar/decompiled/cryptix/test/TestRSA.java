/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.util.core.BI;
import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import xjava.security.Cipher;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.CryptixRSAPublicKey;
import xjava.security.interfaces.RSAFactors;

public class TestRSA
extends BaseTest {
    private static final byte[] message = "Je ne veux que du magnifique, et je ne travaille pas pour le vulgaire des lecteurs --Giambattista BODONI (1740-1813)".getBytes();
    private static final SecureRandom prng = new SecureRandom();

    public static void main(String[] args) {
        new TestRSA().commandline(args);
    }

    protected void engineTest() throws Exception {
        int k = 4;
        this.setExpectedPasses(k * 9);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        Signature[] sigs = new Signature[]{Signature.getInstance("MD2/RSA/PKCS#1"), Signature.getInstance("MD5/RSA/PKCS#1"), Signature.getInstance("SHA-1/RSA/PKCS#1"), Signature.getInstance("RIPEMD160/RSA/PKCS#1")};
        Cipher cipher = Cipher.getInstance("RSA");
        int i = 0;
        while (i < k) {
            int s = 384 + 128 * i;
            this.out.println("\nTest #" + (i + 1) + " (" + s + "-bit modulus)\n");
            this.out.print("  Generating keypair ");
            kpg.initialize(s, prng);
            this.out.print(". ");
            KeyPair pair = kpg.generateKeyPair();
            this.out.print(". ");
            PrivateKey sk = pair.getPrivate();
            this.out.print(". ");
            PublicKey pk = pair.getPublic();
            this.out.println(". Done!\n");
            int j = 0;
            while (j < sigs.length) {
                this.testSignature(s, pk, sk, sigs[j]);
                ++j;
            }
            this.testEncryption(s, pk, sk, cipher);
            ++i;
        }
    }

    private void testSignature(int s, PublicKey pk, PrivateKey sk, Signature alg) {
        try {
            this.out.println("  Signing with a " + s + "-bit key using " + alg.getAlgorithm() + "...");
            alg.initSign(sk);
            alg.update(message);
            byte[] signature = alg.sign();
            this.out.println("  Verifying with same " + s + "-bit key using " + alg.getAlgorithm() + "...");
            alg.initVerify(pk);
            alg.update(message);
            boolean ok = alg.verify(signature);
            this.passIf(ok, "Signature verification");
            if (!ok) {
                this.out.println("---- begin debugging -----\n");
                this.out.println("Computed signature: " + Hex.dumpString(signature));
                this.out.println("RSA parameters:");
                BigInteger m = ((CryptixRSAPublicKey)pk).getModulus();
                BigInteger e = ((CryptixRSAPublicKey)pk).getExponent();
                this.out.println("   Public key material:");
                this.out.println("   n: " + BI.dumpString(m));
                this.out.println("   e: " + BI.dumpString(e));
                BigInteger n = ((CryptixRSAPrivateKey)sk).getModulus();
                BigInteger d = ((CryptixRSAPrivateKey)sk).getExponent();
                BigInteger p = ((RSAFactors)((Object)sk)).getP();
                BigInteger q = ((RSAFactors)((Object)sk)).getQ();
                BigInteger u = ((RSAFactors)((Object)sk)).getInverseOfQModP();
                this.out.println("   Private key material:");
                this.out.println("   n: " + BI.dumpString(n));
                this.out.println("   d: " + BI.dumpString(d));
                this.out.println("   p: " + BI.dumpString(p));
                this.out.println("   q: " + BI.dumpString(q));
                this.out.println("   u: " + BI.dumpString(u));
                BigInteger x = new BigInteger(signature);
                this.out.println("   The signature as a BigInteger:");
                this.out.println("   x: " + BI.dumpString(x));
                this.out.println("RSA correctness tests:");
                try {
                    boolean yes = m.compareTo(n) == 0;
                    this.out.println("\t1. Same modulus? " + yes);
                    if (!yes) {
                        throw new RuntimeException();
                    }
                    yes = p.multiply(q).compareTo(n) == 0;
                    this.out.println("\t2. n = pq? " + yes);
                    if (!yes) {
                        throw new RuntimeException();
                    }
                    BigInteger y = x.modPow(e, n);
                    BigInteger z = y.modPow(d, n);
                    yes = z.compareTo(x) == 0;
                    this.out.println("\t3. x = (x ** ed) mod n? " + yes);
                    if (!yes) {
                        throw new RuntimeException();
                    }
                    BigInteger ONE = BigInteger.valueOf(1L);
                    BigInteger ep = d.mod(p.subtract(ONE));
                    BigInteger eq = d.mod(q.subtract(ONE));
                    BigInteger p2 = y.mod(p).modPow(ep, p);
                    BigInteger q2 = y.mod(q).modPow(eq, q);
                    if ((q2 = q2.subtract(p2)).signum() == -1) {
                        q2 = q2.add(q);
                    }
                    yes = (z = p2.add(p.multiply(q2.multiply(u).mod(q)))).compareTo(x) == 0;
                    this.out.println("\t4. (x ** e) mod n = (y ** d) mod pq? " + yes);
                    if (!yes) {
                        throw new RuntimeException();
                    }
                }
                catch (Throwable ex) {
                    this.error(ex);
                }
                this.out.println("---- end debugging -----");
            }
            signature[0] = (byte)(signature[0] ^ 1);
            alg.initVerify(pk);
            alg.update(message);
            ok = !alg.verify(signature);
            this.passIf(ok, "Incorrect signature should not verify");
        }
        catch (Throwable e) {
            this.error(e);
        }
    }

    private void testEncryption(int s, PublicKey pk, PrivateKey sk, Cipher alg) {
        this.skip("Encryption test not implemented");
    }
}

