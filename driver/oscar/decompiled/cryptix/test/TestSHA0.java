/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.provider.md.SHA0;
import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import java.security.MessageDigest;

class TestSHA0
extends BaseTest {
    private static String[][] testData1 = new String[][]{{"", "F96CEA198AD1DD5617AC084A3D92C6107708C0EF"}, {"a", "37F297772FAE4CB1BA39B6CF9CF0381180BD62F2"}, {"aa", "5173EC2335C575DEE032B01562A41330EB803503"}, {"aaa", "5DFC8A87381AA03E963AB26A645F0FDD60847DFA"}, {"abc", "0164B8A914CD2A5E74C4F7FF082C4D97F1EDF880"}, {"abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq", "d2516ee1acfa5baf33dfc1c471e438449ef134c8"}, {"message digest", "C1B0F222D150EBB9AA36A40CAFDC8BCBED830B14"}, {"abcdefghijklmnopqrstuvwxyz", "B40CE07A430CFD3C033039B9FE9AFEC95DC1BDCD"}, {"aaaaaaaaa...a (1 million times)", "3232affa48628a26653b5aaa44541fd90d690603"}};

    TestSHA0() {
    }

    public static void main(String[] args) {
        new TestSHA0().commandline(args);
    }

    protected void engineTest() throws Exception {
        int good = 0;
        int fails = 0;
        String[][] data = testData1;
        MessageDigest alg = MessageDigest.getInstance("SHA-0", "Cryptix");
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
        this.out.println("\nSHA-0 succeeded (" + i + " tests)");
        SHA0 cfr_ignored_0 = (SHA0)alg;
        SHA0.self_test();
        this.passIf(true, "Self Test (no diags)");
    }
}

