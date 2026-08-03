/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.provider.key.RawKeyGenerator;
import cryptix.util.core.ArrayUtil;
import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import xjava.security.Cipher;
import xjava.security.FeedbackCipher;
import xjava.security.KeyGenerator;
import xjava.security.SecretKey;

class TestSquare
extends BaseTest {
    private static final byte[] key = new byte[16];
    private SecretKey aKey;

    static {
        int i = 0;
        while (i < 16) {
            TestSquare.key[i] = (byte)i;
            ++i;
        }
    }

    TestSquare() {
    }

    public static void main(String[] args) {
        new TestSquare().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(10);
        RawKeyGenerator rkg = (RawKeyGenerator)KeyGenerator.getInstance("Square", "Cryptix");
        this.aKey = rkg.generateKey(key);
        this.test1();
        this.test2();
        this.test3();
        this.test4();
        this.test5();
    }

    private void test1() throws Exception {
        byte[] input = new byte[16];
        int i = 0;
        while (i < 16) {
            input[i] = (byte)i;
            ++i;
        }
        byte[] output = new byte[]{124, 52, -111, -39, 73, -108, -25, 15, 14, -62, -25, -91, -52, -75, -95, 79};
        this.out.println("\nTest vector (raw/ECB):\nEncrypting:");
        Cipher alg = Cipher.getInstance("Square", "Cryptix");
        alg.initEncrypt(this.aKey);
        this.compareIt(alg.crypt(input), output);
        this.out.println("\nDecrypting:");
        alg.initDecrypt(this.aKey);
        this.compareIt(alg.crypt(output), input);
    }

    private void test2() throws Exception {
        byte[] input = new byte[32];
        int i = 0;
        while (i < 16) {
            byte by = (byte)i;
            input[i + 16] = by;
            input[i] = by;
            ++i;
        }
        byte[] output = new byte[]{124, 52, -111, -39, 73, -108, -25, 15, 14, -62, -25, -91, -52, -75, -95, 79, 124, 52, -111, -39, 73, -108, -25, 15, 14, -62, -25, -91, -52, -75, -95, 79};
        this.out.println("\nTest vector (ECB):\nEncrypting:");
        Cipher alg = Cipher.getInstance("Square/ECB", "Cryptix");
        alg.initEncrypt(this.aKey);
        this.compareIt(alg.crypt(input), output);
        this.out.println("\nDecrypting:");
        alg.initDecrypt(this.aKey);
        this.compareIt(alg.crypt(output), input);
    }

    private void test3() throws Exception {
        byte[] input = new byte[32];
        int i = 0;
        while (i < 16) {
            byte by = (byte)i;
            input[i + 16] = by;
            input[i] = by;
            ++i;
        }
        byte[] output = new byte[]{124, 52, -111, -39, 73, -108, -25, 15, 14, -62, -25, -91, -52, -75, -95, 79, 65, -46, -15, -99, 126, -121, -115, -75, 108, 116, 70, -44, 36, -61, -83, -4};
        this.out.println("\nTest vector (CBC):\nEncrypting:");
        Cipher alg = Cipher.getInstance("Square/CBC", "Cryptix");
        FeedbackCipher fbc = (FeedbackCipher)((Object)alg);
        fbc.setInitializationVector(new byte[fbc.getInitializationVectorLength()]);
        alg.initEncrypt(this.aKey);
        this.compareIt(alg.crypt(input), output);
        this.out.println("\nDecrypting:");
        alg.initDecrypt(this.aKey);
        this.compareIt(alg.crypt(output), input);
    }

    private void test4() throws Exception {
        byte[] input = new byte[32];
        int i = 0;
        while (i < 16) {
            byte by = (byte)i;
            input[i + 16] = by;
            input[i] = by;
            ++i;
        }
        byte[] output = new byte[]{-1, 88, 109, -91, 108, -70, -59, 6, 74, 9, -92, 10, -18, -74, -82, -81, -43, -53, 83, -114, -22, 40, -105, 79, 124, 117, -25, -101, -53, 13, 77, 14};
        this.out.println("\nTest vector (CFB):\nEncrypting:");
        Cipher alg = Cipher.getInstance("Square/CFB", "Cryptix");
        ((FeedbackCipher)((Object)alg)).setInitializationVector(new byte[16]);
        alg.initEncrypt(this.aKey);
        this.compareIt(alg.crypt(input), output);
        this.out.println("\nDecrypting:");
        alg.initDecrypt(this.aKey);
        this.compareIt(alg.crypt(output), input);
    }

    private void test5() throws Exception {
        byte[] input = new byte[32];
        int i = 0;
        while (i < 16) {
            byte by = (byte)i;
            input[i + 16] = by;
            input[i] = by;
            ++i;
        }
        byte[] output = new byte[]{-1, 88, 109, -91, 108, -70, -59, 6, 74, 9, -92, 10, -18, -74, -82, -81, 53, -56, 51, -45, 92, 41, 68, 55, 53, -46, 37, -68, -107, 40, -61, -56};
        this.out.println("\nTest vector (OFB):\nEncrypting:");
        Cipher alg = Cipher.getInstance("Square/OFB", "Cryptix");
        FeedbackCipher fbc = (FeedbackCipher)((Object)alg);
        fbc.setInitializationVector(new byte[fbc.getInitializationVectorLength()]);
        alg.initEncrypt(this.aKey);
        this.compareIt(alg.crypt(input), output);
        this.out.println("\nDecrypting:");
        alg.initDecrypt(this.aKey);
        this.compareIt(alg.crypt(output), input);
    }

    private void compareIt(byte[] o1, byte[] o2) {
        this.out.print("  computed output:" + Hex.dumpString(o1));
        boolean ok = ArrayUtil.areEqual(o1, o2);
        if (!ok) {
            this.out.print("\n certified output:" + Hex.dumpString(o2));
        }
        this.passIf(ok, " *** Square OUTPUT");
    }
}

