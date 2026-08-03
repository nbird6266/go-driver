/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import java.security.MessageDigest;

class TestMD2
extends BaseTest {
    TestMD2() {
    }

    public static void main(String[] args) {
        new TestMD2().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(7);
        String[][] data = new String[][]{{"", "8350E5A3E24C153DF2275C9F80692773"}, {"a", "32EC01EC4A6DAC72C0AB96FB34C0B5D1"}, {"abc", "DA853B0D3F88D99B30283A69E6DED6BB"}, {"message digest", "AB4F496BFB2A530B219FF33031FE06B0"}, {"abcdefghijklmnopqrstuvwxyz", "4E8DDFF3650292AB5A4108C3AA47940B"}, {"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", "DA33DEF2A42DF13975352846C30338CD"}, {"12345678901234567890123456789012345678901234567890123456789012345678901234567890", "D5976F79D83D3A0DC9806C3C66F3EFD8"}};
        MessageDigest alg = MessageDigest.getInstance("MD2", "Cryptix");
        int i = 0;
        while (i < data.length) {
            String a = Hex.toString(alg.digest(data[i][0].getBytes()));
            this.out.println("  test vector: " + data[i][0]);
            this.out.println("  computed md: " + a);
            this.out.println(" certified md: " + data[i][1]);
            this.passIf(a.equals(data[i][1]), "MD2 #" + (i + 1));
            ++i;
        }
    }
}

