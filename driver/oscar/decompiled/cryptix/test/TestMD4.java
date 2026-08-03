/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import java.security.MessageDigest;

class TestMD4
extends BaseTest {
    TestMD4() {
    }

    public static void main(String[] args) {
        new TestMD4().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(7);
        String[][] data = new String[][]{{"", "31D6CFE0D16AE931B73C59D7E0C089C0"}, {"a", "BDE52CB31DE33E46245E05FBDBD6FB24"}, {"abc", "A448017AAF21D8525FC10AE87AA6729D"}, {"message digest", "D9130A8164549FE818874806E1C7014B"}, {"abcdefghijklmnopqrstuvwxyz", "D79E1C308AA5BBCDEEA8ED63DF412DA9"}, {"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", "043F8582F241DB351CE627E153E7F0E4"}, {"12345678901234567890123456789012345678901234567890123456789012345678901234567890", "E33B4DDC9C38F2199C3E7B164FCC0536"}};
        MessageDigest alg = MessageDigest.getInstance("MD4", "Cryptix");
        int i = 0;
        while (i < data.length) {
            String a = Hex.toString(alg.digest(data[i][0].getBytes()));
            this.out.println("  test vector: " + data[i][0]);
            this.out.println("  computed md: " + a);
            this.out.println(" certified md: " + data[i][1]);
            this.passIf(a.equals(data[i][1]), "MD4 #" + (i + 1));
            ++i;
        }
    }
}

