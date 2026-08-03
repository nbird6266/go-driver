/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.provider.md.MD5;
import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import java.security.MessageDigest;

class TestMD5
extends BaseTest {
    private static String[][] testData1 = new String[][]{{"", "D41D8CD98F00B204E9800998ECF8427E"}, {"a", "0CC175B9C0F1B6A831C399E269772661"}, {"aa", "4124BC0A9335C27F086F24BA207A4912"}, {"abc", "900150983CD24FB0D6963F7D28E17F72"}, {"aaa", "47BCE5C74F589F4867DBD57E9CA9F808"}, {"bbb", "08F8E0260C64418510CEFB2B06EEE5CD"}, {"ccc", "9DF62E693988EB4E1E1444ECE0578579"}, {"message digest", "F96B697D7CB7938D525A2F31AAF161D0"}, {"abcdefg", "7AC66C0F148DE9519B8BD264312C4D64"}, {"abcdefghijk", "92B9CCCC0B98C3A0B8D0DF25A421C0E3"}, {"abcdefghijklmnopqrstuvwxyz", "C3FCD3D76192E4007DFB496CCA67E13B"}, {"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", "D174AB98D277D9F5A5611C2C9F419D9F"}, {"12345678901234567890123456789012345678901234567890123456789012345678901234567890", "57EDF4A22BE3C955AC49DA2E2107B67A"}};

    TestMD5() {
    }

    public static void main(String[] args) {
        new TestMD5().commandline(args);
    }

    protected void engineTest() throws Exception {
        int good = 0;
        int fails = 0;
        String[][] data = testData1;
        MessageDigest alg = MessageDigest.getInstance("MD5", "Cryptix");
        this.setExpectedPasses(data.length + 1);
        int i = 0;
        while (i < data.length) {
            byte[] x;
            alg.reset();
            if (i != 6) {
                x = alg.digest(data[i][0].getBytes());
            } else {
                int j = 0;
                while (j < data[i][0].length()) {
                    alg.update((byte)data[i][0].charAt(j));
                    ++j;
                }
                x = alg.digest();
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
        this.out.println("\nMD5 succeeded (" + i + " tests)");
        MD5 cfr_ignored_0 = (MD5)alg;
        MD5.self_test();
        this.passIf(true, "Self Test (no diags)");
    }
}

