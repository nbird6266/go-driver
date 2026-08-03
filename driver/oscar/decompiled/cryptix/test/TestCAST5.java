/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.provider.key.RawSecretKey;
import cryptix.util.core.ArrayUtil;
import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import java.util.Date;
import xjava.security.Cipher;

class TestCAST5
extends BaseTest {
    private Cipher alg;
    private static final byte[] input = Hex.fromString("0123456789ABCDEF");

    TestCAST5() {
    }

    public static void main(String[] args) {
        new TestCAST5().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(7);
        this.alg = Cipher.getInstance("CAST5", "Cryptix");
        this.test1();
        this.test2();
    }

    private void test1() throws Exception {
        String[][] data = new String[][]{{"0123456712345678234567893456789A", "238B4FE5847E44B2"}, {"01234567123456782345", "EB6A711A2C02271B"}, {"0123456712", "7AC816D16E9B302E"}};
        int i = 0;
        while (i < data.length) {
            RawSecretKey aKey = new RawSecretKey("CAST5", Hex.fromString(data[i][0]));
            byte[] output = Hex.fromString(data[i][1]);
            this.out.println("\nTest vector (" + 4 * data[i][0].length() + "-bit key):");
            this.out.println("\nEncrypting:");
            this.alg.initEncrypt(aKey);
            this.compareIt(this.alg.crypt(input), output);
            this.out.println("\nDecrypting:");
            this.alg.initDecrypt(aKey);
            this.compareIt(this.alg.crypt(output), input);
            ++i;
        }
    }

    private void test2() throws Exception {
        int j;
        RawSecretKey aKey = new RawSecretKey("CAST5", Hex.fromString("0123456712"));
        byte[] a = Hex.fromString("0123456789ABCDEF");
        this.alg.initEncrypt(aKey);
        this.out.println("\nSpeed test (10,000 x 8-byte w/40-bit key):\n");
        this.out.println("...Encryption\n");
        this.out.println("      start date/time: " + new Date());
        int i = 0;
        while (i < 10) {
            j = 0;
            while (j < 1000) {
                this.alg.crypt(a, 0, 8, a, 0);
                ++j;
            }
            ++i;
        }
        this.out.println("     finish date/time: " + new Date());
        this.alg.initDecrypt(aKey);
        this.out.println("\n...Decryption\n");
        this.out.println("      start date/time: " + new Date());
        i = 0;
        while (i < 10) {
            j = 0;
            while (j < 1000) {
                this.alg.crypt(a, 0, 8, a, 0);
                ++j;
            }
            ++i;
        }
        this.out.println("     finish date/time: " + new Date());
        this.out.println("\n result:");
        this.compareIt(a, input);
    }

    private void test3() throws Exception {
        byte[] a = Hex.fromString("0123456712345678234567893456789A");
        byte[] b = Hex.fromString("0123456712345678234567893456789A");
        byte[] aOut = Hex.fromString("EEA9D0A249FD3BA6B3436FB89D6DCA92");
        byte[] bOut = Hex.fromString("B2C95EB00C31AD7180AC05B8E83D696E");
        this.out.println("\nTest 1,000,000 encryptions with 128-bit key:\n");
        this.out.println("  start date/time: " + new Date());
        int i = 0;
        while (i < 1000) {
            int j = 0;
            while (j < 1000) {
                this.alg.initEncrypt(new RawSecretKey("CAST5", b));
                a = this.alg.crypt(a);
                this.alg.initEncrypt(new RawSecretKey("CAST5", a));
                b = this.alg.crypt(b);
                ++j;
            }
            ++i;
        }
        this.out.println(" finish date/time: " + new Date());
        this.out.println("\n result for 'a'\n");
        this.compareIt(a, aOut);
        this.out.println("\n result for 'b'\n");
        this.compareIt(b, bOut);
    }

    private void compareIt(byte[] o1, byte[] o2) {
        this.out.println("  computed: " + Hex.dumpString(o1));
        this.out.println(" certified: " + Hex.dumpString(o2));
        this.passIf(ArrayUtil.areEqual(o1, o2), " *** CAST5 OUTPUT");
    }
}

