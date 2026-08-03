/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.provider.md.SHA1;
import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import java.security.MessageDigest;

class TestSHA1
extends BaseTest {
    private static String[][] testData1 = new String[][]{{"", "da39a3ee5e6b4b0d3255bfef95601890afd80709"}, {"1", "356a192b7913b04c54574d18c28d46e6395428ab"}, {"abc", "A9993E364706816ABA3E25717850C26C9CD0D89D"}, {"abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq", "84983E441C3BD26EBAAE4AA1F95129E5E54670F1"}, {"Anyone got any SHA-1 test data?", "09b9e9c04a84ce274942048acf3a6f2ff4a8a39c"}, {"Of cabbages and kings", "5f093d74a9cb1f2f14537bcf3a8a1ffd59b038a2"}, {"aaaaaaaaa...a (1 million times)", "34AA973CD4C4DAA4F61EEB2BDBAD27316534016F"}};

    TestSHA1() {
    }

    public static void main(String[] args) {
        new TestSHA1().commandline(args);
    }

    protected void engineTest() throws Exception {
        int good = 0;
        int fails = 0;
        String[][] data = testData1;
        MessageDigest alg = MessageDigest.getInstance("SHA-1", "Cryptix");
        this.setExpectedPasses(data.length + 1);
        int i = 0;
        while (i < data.length) {
            byte[] x;
            alg.reset();
            if (i == data.length - 1) {
                int j = 0;
                while (j < 1000) {
                    int k = 0;
                    while (k < 1000) {
                        alg.update((byte)97);
                        ++k;
                    }
                    ++j;
                }
                x = alg.digest();
            } else {
                x = alg.digest(data[i][0].getBytes());
            }
            String a = Hex.toString(x);
            this.out.println("         data: '" + data[i][0] + "'");
            this.out.println("  computed md: " + a);
            this.out.println(" certified md: " + data[i][1]);
            boolean ok = a.equalsIgnoreCase(data[i][1]);
            this.passIf(ok, "Data Set #" + (i + 1));
            if (ok) {
                this.out.println("   * Hash (#" + ++good + "/" + (i + 1) + ") good");
            } else {
                this.out.println("===> Hash (#" + ++fails + "/" + (i + 1) + ") FAILED  <===");
                this.out.println("     (no debugging available)");
            }
            this.out.println();
            ++i;
        }
        this.out.println("\nSHA-1 succeeded (" + i + " tests)");
        SHA1 cfr_ignored_0 = (SHA1)alg;
        SHA1.self_test();
        this.passIf(true, "Self Test (no diags)");
    }
}

