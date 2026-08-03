/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.provider.key.RawSecretKey;
import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import xjava.security.Cipher;

class TestSPEED
extends BaseTest {
    private static final String[][] testData1 = new String[][]{{"64", "0000000000000000", "0000000000000000", "6D8526BC1980002E"}, {"128", "00000000000000000000000000000000", "00000000000000000000000000000000", "09B96371D5DFA2D7F8CBF6ED29BF4FA4"}, {"128", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "E8C45B9116D854AB711517C3B9E4136C"}, {"48", "4142434445464748494A4B4C4D4E4F50", "000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F", "92363E88EDF53BA775E86E0AB7CD512E9BF36BADCDCA78C11BD2A3F61E98C590"}, {"256", "0000000000000000000000000000000000000000000000000000000000000000", "0000000000000000000000000000000000000000000000000000000000000000", "F59BB02EA027DF8C29568D300117FA0A2C3A71AFC4D15FE9A76ABC492B4DD46C"}, {"256", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "C5D093BAA39B1BCA81FB89CE0E9A4ACF22C0EB7B22382E2224AF3F2664E8F3C8"}, {"256", "4142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F60", "000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F", "681031BACC31B108D7616B298A55AA3F1BEC3F6974154E434768629AFA6CE13D"}};

    TestSPEED() {
    }

    public static void main(String[] args) {
        new TestSPEED().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(14);
        this.test1();
    }

    private void test1() throws Exception {
        int i = 0;
        while (i < testData1.length) {
            Cipher alg = Cipher.getInstance("SPEED", "Cryptix");
            alg.setParameter("rounds", new Integer(testData1[i][0]));
            alg.setParameter("blockSize", new Integer(testData1[i][2].length() / 2));
            RawSecretKey key = new RawSecretKey("SPEED", Hex.fromString(testData1[i][1]));
            alg.initEncrypt(key);
            byte[] ect = alg.crypt(Hex.fromString(testData1[i][2]));
            String a = Hex.toString(ect);
            alg.initDecrypt(key);
            byte[] dct = alg.crypt(ect);
            String b = Hex.toString(dct);
            this.out.println("     plain:  " + testData1[i][2]);
            this.out.println("     cipher: " + a);
            this.out.println("     cert:   " + testData1[i][3]);
            this.passIf(a.equals(testData1[i][3]), " *** SPEED encrypt");
            this.out.println("     cipher: " + Hex.toString(ect));
            this.out.println("     plain:  " + b);
            this.out.println("     cert:   " + testData1[i][2]);
            this.passIf(b.equals(testData1[i][2]), " *** SPEED decrypt");
            ++i;
        }
    }
}

