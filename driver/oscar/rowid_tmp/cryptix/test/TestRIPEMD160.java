/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import java.security.MessageDigest;

class TestRIPEMD160
extends BaseTest {
    TestRIPEMD160() {
    }

    public static void main(String[] args) {
        new TestRIPEMD160().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(9);
        String[][] data = new String[][]{{"", "9C1185A5C5E9FC54612808977EE8F548B2258D31"}, {"a", "0BDC9D2D256B3EE9DAAE347BE6F4DC835A467FFE"}, {"abc", "8EB208F7E05D987A9B044A8E98C6B087F15A0BFC"}, {"message digest", "5D0689EF49D2FAE572B881B123A85FFA21595F36"}, {"abcdefghijklmnopqrstuvwxyz", "F71C27109C692C1B56BBDCEB5B9D2865B3708DBC"}, {"abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq", "12A053384A9C0C88E405A06C27DCF49ADA62EB2B"}, {"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", "B0E20B6E3116640286ED3A87A5713079B21F5189"}, {"12345678901234567890123456789012345678901234567890123456789012345678901234567890", "9B752E45573D4B39F4DBD3323CAB82BF63326BFB"}, {"aaaaaaaaa...a (1 million times)", "52783243C1697BDBE16D37F97F68F08325DC1528"}};
        MessageDigest alg = MessageDigest.getInstance("RIPEMD160", "Cryptix");
        int i = 0;
        while (i < data.length) {
            byte[] x;
            if (i != 8) {
                x = alg.digest(data[i][0].getBytes());
            } else {
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
            }
            String a = Hex.toString(x);
            this.out.println("Data: '" + data[i][0] + "'");
            this.out.println("  computed md: " + a);
            this.out.println(" certified md: " + data[i][1]);
            this.passIf(a.equals(data[i][1]), " ***** RIPEMD-160");
            ++i;
        }
    }
}

