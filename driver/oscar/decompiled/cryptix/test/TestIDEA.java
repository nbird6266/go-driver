/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.provider.key.RawSecretKey;
import cryptix.util.core.ArrayUtil;
import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import xjava.security.Cipher;
import xjava.security.FeedbackCipher;

class TestIDEA
extends BaseTest {
    private static final String[][] testData1 = new String[][]{{"00010002000300040005000600070008", "0000000100020003", "11FBED2B01986DE5"}, {"00010002000300040005000600070008", "0102030405060708", "540E5FEA18C2F8B1"}, {"00010002000300040005000600070008", "0019324B647D96AF", "9F0A0AB6E10CED78"}, {"00010002000300040005000600070008", "F5202D5B9C671B08", "CF18FD7355E2C5C5"}, {"00010002000300040005000600070008", "FAE6D2BEAA96826E", "85DF52005608193D"}, {"00010002000300040005000600070008", "0A141E28323C4650", "2F7DE750212FB734"}, {"00010002000300040005000600070008", "050A0F14191E2328", "7B7314925DE59C09"}, {"0005000A000F00140019001E00230028", "0102030405060708", "3EC04780BEFF6E20"}, {"3A984E2000195DB32EE501C8C47CEA60", "0102030405060708", "97BCD8200780DA86"}, {"006400C8012C019001F4025802BC0320", "05320A6414C819FA", "65BE87E7A2538AED"}, {"9D4075C103BC322AFB03E7BE6AB30006", "0808080808080808", "F5DB1AC45E5EF9F9"}};
    private static final String[][] testData2 = new String[][]{{"00010002000300040005000600070008", "0000000100020003"}, {"00010002000300040005000600070008", "01020304050607084E"}, {"00010002000300040005000600070008", "0019324B647D96AF4E2019"}, {"00010002000300040005000600070008", "F5202D5B9C671B084E2009"}, {"00010002000300040005000600070008", "FAE6D2BEAA96826E4E200019"}, {"00010002000300040005000600070008", "0A141E28323C46504E200019"}, {"00010002000300040005000600070008", "050A0F14191E23284E2019"}, {"0005000A000F00140019001E00230028", "01020304050607080A000F"}, {"3A984E2000195DB32EE501C8C47CEA60", "0102030405060708EA60"}, {"006400C8012C019001F4025802BC0320", "05320A6414C819FA025802BC0320"}, {"9D4075C103BC322AFB03E7BE6AB30006", "08080808080808086AB30006"}};

    TestIDEA() {
    }

    public static void main(String[] args) {
        new TestIDEA().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(66);
        this.out.println("*** IDEA in ECB mode:\n");
        Cipher alg = Cipher.getInstance("IDEA", "Cryptix");
        this.test1(alg, testData1);
        this.out.println("\n*** IDEA in CFB mode:\n");
        alg = Cipher.getInstance("IDEA/CFB", "Cryptix");
        this.test2(alg, testData2);
        this.out.println("\n*** IDEA in OFB mode:\n");
        alg = Cipher.getInstance("IDEA/OFB", "Cryptix");
        this.test2(alg, testData2);
        this.out.println("\n*** IDEA in CFB-PGP mode:\n");
        alg = Cipher.getInstance("IDEA/CFB-PGP", "Cryptix");
        this.test2(alg, testData2);
        this.out.println("\n*** IDEA in CBC mode with PKCS#5 padding:\n");
        alg = Cipher.getInstance("IDEA/CBC/PKCS#5", "Cryptix");
        this.test2(alg, testData2);
    }

    private void test1(Cipher alg, String[][] data) throws Exception {
        int i = 0;
        while (i < data.length) {
            RawSecretKey key = new RawSecretKey("IDEA", Hex.fromString(data[i][0]));
            alg.initEncrypt(key);
            byte[] ect = alg.crypt(Hex.fromString(data[i][1]));
            String a = Hex.toString(ect);
            alg.initDecrypt(key);
            byte[] dct = alg.crypt(ect);
            String b = Hex.toString(dct);
            this.out.println("\nplain:" + data[i][1] + " enc:" + a + " calc:" + data[i][2]);
            this.passIf(a.equals(data[i][2]), "IDEA encrypt");
            this.out.println("  enc:" + Hex.toString(ect) + " dec:" + b + " calc:" + data[i][1]);
            this.passIf(b.equals(data[i][1]), "IDEA decrypt");
            ++i;
        }
    }

    private void test2(Cipher alg, String[][] data) throws Exception {
        byte[] iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        ((FeedbackCipher)((Object)alg)).setInitializationVector(iv);
        int i = 0;
        while (i < data.length) {
            RawSecretKey key = new RawSecretKey("IDEA", Hex.fromString(data[i][0]));
            alg.initEncrypt(key);
            byte[] pt = Hex.fromString(data[i][1]);
            byte[] ect = alg.crypt(pt);
            alg.initDecrypt(key);
            byte[] dct = alg.crypt(ect);
            this.out.println("\nplain:" + Hex.toString(pt) + " enc:" + Hex.toString(ect) + " dec:" + Hex.toString(dct));
            this.passIf(ArrayUtil.areEqual(pt, dct), "IDEA feedback");
            ++i;
        }
    }
}

