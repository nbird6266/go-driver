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
import xjava.security.interfaces.ElGamalPrivateKey;
import xjava.security.interfaces.ElGamalPublicKey;

public class TestElGamal
extends BaseTest {
    private static final byte[] message = "Je ne veux que du magnifique, et je ne travaille pas pour le vulgaire des lecteurs --Giambattista BODONI (1740-1813)".getBytes();
    private static final SecureRandom prng = new SecureRandom();

    public static void main(String[] args) {
        new TestElGamal().commandline(args);
    }

    protected void engineTest() throws Exception {
        int k = 2;
        this.setExpectedPasses(k * 9);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ElGamal");
        Signature[] sigs = new Signature[]{Signature.getInstance("MD2/ElGamal/PKCS#1"), Signature.getInstance("MD5/ElGamal/PKCS#1"), Signature.getInstance("SHA-1/ElGamal/PKCS#1"), Signature.getInstance("RIPEMD160/ElGamal/PKCS#1")};
        Cipher cipher = null;
        int i = 0;
        while (i < k) {
            int s = 384 + 128 * i;
            this.out.println("\nTest #" + (i + 1) + " (" + s + "-bit modulus)\n");
            this.out.println("  Generating keypair...\n");
            kpg.initialize(s, prng);
            KeyPair pair = kpg.generateKeyPair();
            PrivateKey sk = pair.getPrivate();
            PublicKey pk = pair.getPublic();
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
            this.out.println("\n  Signing with a " + s + "-bit key using " + alg.getAlgorithm() + "...");
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
                this.out.println("ElGamal parameters:");
                BigInteger p = ((ElGamalPublicKey)pk).getP();
                BigInteger g = ((ElGamalPublicKey)pk).getG();
                BigInteger y = ((ElGamalPublicKey)pk).getY();
                this.out.println("  Public key material:");
                this.out.println("    p: " + BI.dumpString(p));
                this.out.println("    g: " + BI.dumpString(g));
                this.out.println("    y: " + BI.dumpString(y));
                BigInteger x = ((ElGamalPrivateKey)sk).getX();
                this.out.println("  Private key material:");
                this.out.println("    x: " + BI.dumpString(x));
                BigInteger sig = new BigInteger(signature);
                this.out.println("  The signature as a BigInteger:");
                this.out.println(" sig: " + BI.dumpString(sig));
                this.out.println("---- end debugging -----");
            }
            signature[0] = (byte)(signature[0] ^ 1);
            alg.initVerify(pk);
            alg.update(message);
            ok = !alg.verify(signature);
            this.passIf(ok, "Incorrect signature should not verify");
        }
        catch (Exception e) {
            this.error(e);
        }
    }

    private void testEncryption(int s, PublicKey pk, PrivateKey sk, Cipher alg) {
        this.skip("Encryption test not implemented");
    }
}

