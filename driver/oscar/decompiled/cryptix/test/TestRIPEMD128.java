/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import java.security.MessageDigest;

class TestRIPEMD128
extends BaseTest {
    TestRIPEMD128() {
    }

    public static void main(String[] args) {
        new TestRIPEMD128().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(9);
        String[][] data = new String[][]{{"", "CDF26213A150DC3ECB610F18F6B38B46"}, {"a", "86BE7AFA339D0FC7CFC785E72F578D33"}, {"abc", "C14A12199C66E4BA84636B0F69144C77"}, {"message digest", "9E327B3D6E523062AFC1132D7DF9D1B8"}, {"abcdefghijklmnopqrstuvwxyz", "FD2AA607F71DC8F510714922B371834E"}, {"abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq", "A1AA0689D0FAFA2DDC22E88B49133A06"}, {"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", "D1E959EB179C911FAEA4624C60C5C702"}, {"12345678901234567890123456789012345678901234567890123456789012345678901234567890", "3F45EF194732C2DBB2C4A2C769795FA3"}, {"aaaaaaaaa...a (1 million times)", "4A7F5723F954EBA1216C9D8F6320431F"}};
        MessageDigest alg = MessageDigest.getInstance("RIPEMD128", "Cryptix");
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
            this.passIf(a.equals(data[i][1]), " ***** RIPEMD-128");
            ++i;
        }
    }
}

