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
import xjava.security.FeedbackCipher;

class TestBlowfish
extends BaseTest {
    private Cipher alg;
    private static final String[][] testData1 = new String[][]{{"0000000000000000", "0000000000000000", "4EF997456198DD78"}, {"FFFFFFFFFFFFFFFF", "FFFFFFFFFFFFFFFF", "51866FD5B85ECB8A"}, {"3000000000000000", "1000000000000001", "7D856F9A613063F2"}, {"1111111111111111", "1111111111111111", "2466DD878B963C9D"}, {"0123456789ABCDEF", "1111111111111111", "61F9C3802281B096"}, {"1111111111111111", "0123456789ABCDEF", "7D0CC630AFDA1EC7"}, {"FEDCBA9876543210", "0123456789ABCDEF", "0ACEAB0FC6A0A28D"}, {"7CA110454A1A6E57", "01A1D6D039776742", "59C68245EB05282B"}, {"0131D9619DC1376E", "5CD54CA83DEF57DA", "B1B8CC0B250F09A0"}, {"07A1133E4A0B2686", "0248D43806F67172", "1730E5778BEA1DA4"}, {"3849674C2602319E", "51454B582DDF440A", "A25E7856CF2651EB"}, {"04B915BA43FEB5B6", "42FD443059577FA2", "353882B109CE8F1A"}, {"0113B970FD34F2CE", "059B5E0851CF143A", "48F4D0884C379918"}, {"0170F175468FB5E6", "0756D8E0774761D2", "432193B78951FC98"}, {"43297FAD38E373FE", "762514B829BF486A", "13F04154D69D1AE5"}, {"07A7137045DA2A16", "3BDD119049372802", "2EEDDA93FFD39C79"}, {"04689104C2FD3B2F", "26955F6835AF609A", "D887E0393C2DA6E3"}, {"37D06BB516CB7546", "164D5E404F275232", "5F99D04F5B163969"}, {"1F08260D1AC2465E", "6B056E18759F5CCA", "4A057A3B24D3977B"}, {"584023641ABA6176", "004BD6EF09176062", "452031C1E4FADA8E"}, {"025816164629B007", "480D39006EE762F2", "7555AE39F59B87BD"}, {"49793EBC79B3258F", "437540C8698F3CFA", "53C55F9CB49FC019"}, {"4FB05E1515AB73A7", "072D43A077075292", "7A8E7BFA937E89A3"}, {"49E95D6D4CA229BF", "02FE55778117F12A", "CF9C5D7A4986ADB5"}, {"018310DC409B26D6", "1D9D5C5018F728C2", "D1ABB290658BC778"}, {"1C587F1C13924FEF", "305532286D6F295A", "55CB3774D13EF201"}, {"0101010101010101", "0123456789ABCDEF", "FA34EC4847B268B2"}, {"1F1F1F1F0E0E0E0E", "0123456789ABCDEF", "A790795108EA3CAE"}, {"E0FEE0FEF1FEF1FE", "0123456789ABCDEF", "C39E072D9FAC631D"}, {"0000000000000000", "FFFFFFFFFFFFFFFF", "014933E0CDAFF6E4"}, {"FFFFFFFFFFFFFFFF", "0000000000000000", "F21E9A77B71C49BC"}, {"0123456789ABCDEF", "0000000000000000", "245946885754369A"}, {"FEDCBA9876543210", "FFFFFFFFFFFFFFFF", "6B5C5A9C5D9E0A5A"}};
    private static final String[][] testData2 = new String[][]{{"B39E44481BDB1E6E", "F0E1D2C3B4"}, {"9457AA83B1928C0D", "F0E1D2C3B4A5"}, {"8BB77032F960629D", "F0E1D2C3B4A596"}, {"E87A244E2CC85E82", "F0E1D2C3B4A59687"}, {"15750E7A4F4EC577", "F0E1D2C3B4A5968778"}, {"122BA70B3AB64AE0", "F0E1D2C3B4A596877869"}, {"3A833C9AFFC537F6", "F0E1D2C3B4A5968778695A"}, {"9409DA87A90F6BF2", "F0E1D2C3B4A5968778695A4B"}, {"884F80625060B8B4", "F0E1D2C3B4A5968778695A4B3C"}, {"1F85031C19E11968", "F0E1D2C3B4A5968778695A4B3C2D"}, {"79D9373A714CA34F", "F0E1D2C3B4A5968778695A4B3C2D1E"}, {"93142887EE3BE15C", "F0E1D2C3B4A5968778695A4B3C2D1E0F"}, {"03429E838CE2D14B", "F0E1D2C3B4A5968778695A4B3C2D1E0F00"}, {"A4299E27469FF67B", "F0E1D2C3B4A5968778695A4B3C2D1E0F0011"}, {"AFD5AED1C1BC96A8", "F0E1D2C3B4A5968778695A4B3C2D1E0F001122"}, {"10851C0E3858DA9F", "F0E1D2C3B4A5968778695A4B3C2D1E0F00112233"}, {"E6F51ED79B9DB21F", "F0E1D2C3B4A5968778695A4B3C2D1E0F0011223344"}, {"64A6E14AFD36B46F", "F0E1D2C3B4A5968778695A4B3C2D1E0F001122334455"}, {"80C7D7D45A5479AD", "F0E1D2C3B4A5968778695A4B3C2D1E0F00112233445566"}, {"05044B62FA52D080", "F0E1D2C3B4A5968778695A4B3C2D1E0F0011223344556677"}};

    TestBlowfish() {
    }

    public static void main(String[] args) {
        new TestBlowfish().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(90);
        this.alg = Cipher.getInstance("Blowfish", "Cryptix");
        this.test1();
        this.test2();
        this.test3();
        this.test4();
    }

    private void test1() throws Exception {
        this.out.println("*** Blowfish (16-round) in ECB mode 1/2:\n");
        int i = 0;
        while (i < testData1.length) {
            RawSecretKey key = new RawSecretKey("Blowfish", Hex.fromString(testData1[i][0]));
            this.alg.initEncrypt(key);
            byte[] ect = this.alg.crypt(Hex.fromString(testData1[i][1]));
            String a = Hex.toString(ect);
            this.alg.initDecrypt(key);
            byte[] dct = this.alg.crypt(ect);
            String b = Hex.toString(dct);
            this.out.print("  plain: " + testData1[i][1] + ", cipher: " + a + ", cert: " + testData1[i][2]);
            this.passIf(a.equals(testData1[i][2]), "Test #1/Enc " + (i + 1));
            this.out.print(" cipher: " + Hex.toString(ect) + ",  plain: " + b + ", cert: " + testData1[i][1]);
            this.passIf(b.equals(testData1[i][1]), "Test #1/Dec " + (i + 1));
            ++i;
        }
    }

    private void test2() throws Exception {
        this.out.println("\n*** Blowfish (16-round) in ECB mode 2/2:\n");
        byte[] input = Hex.fromString("FEDCBA9876543210");
        int i = 0;
        while (i < testData2.length) {
            byte[] k = Hex.fromString(testData2[i][1]);
            RawSecretKey key = new RawSecretKey("Blowfish", k);
            this.alg.initEncrypt(key);
            byte[] ect = this.alg.crypt(input);
            String a = Hex.toString(ect);
            this.out.print("  plain: FEDCBA9876543210, cipher: " + a + " cert: " + testData2[i][0]);
            this.passIf(a.equals(testData2[i][0]), "Test #2/" + (i + 1));
            ++i;
        }
    }

    private void test3() throws Exception {
        String in = "37363534333231204E6F77206973207468652074696D6520666F722000";
        String o1 = "6B77B4D63006DEE605B156E27403979358DEB9E7154616D959F1652BD5FF92CC";
        String o2 = "E73214A2822139CAF26ECF6D2EB9E76E3DA3DE04D1517200519D57A6C3";
        String o3 = "E73214A2822139CA62B343CC5B65587310DD908D0C241B2263C2CF80DA";
        byte[] input = Hex.fromString(in);
        byte[] iv = Hex.fromString("FEDCBA9876543210");
        byte[] key = Hex.fromString("0123456789ABCDEFF0E1D2C3B4A59687");
        RawSecretKey k = new RawSecretKey("Blowfish", key);
        this.out.println("\n*** Blowfish (16-round) in CBC mode:\n");
        this.alg = Cipher.getInstance("Blowfish/CBC", "Cryptix");
        ((FeedbackCipher)((Object)this.alg)).setInitializationVector(iv);
        this.alg.initEncrypt(k);
        byte[] newIn = new byte[32];
        System.arraycopy(input, 0, newIn, 0, input.length);
        byte[] ect = this.alg.crypt(newIn);
        String a = Hex.toString(ect);
        this.out.println("  plain: " + in);
        this.out.println(" cipher: " + a);
        this.out.println("   cert: " + o1);
        this.passIf(a.equals(o1), "Test #3/1");
        this.out.println("\n*** Blowfish (16-round) in CFB mode:\n");
        this.alg = Cipher.getInstance("Blowfish/CFB", "Cryptix");
        ((FeedbackCipher)((Object)this.alg)).setInitializationVector(iv);
        this.alg.initEncrypt(k);
        ect = this.alg.crypt(input);
        a = Hex.toString(ect);
        this.out.println("  plain: " + in);
        this.out.println(" cipher: " + a);
        this.out.println("   cert: " + o2);
        this.passIf(a.equals(o2), "Test #3/2");
        this.out.println("\n*** Blowfish (16-round) in OFB mode:\n");
        this.alg = Cipher.getInstance("Blowfish/OFB", "Cryptix");
        ((FeedbackCipher)((Object)this.alg)).setInitializationVector(iv);
        this.alg.initEncrypt(k);
        ect = this.alg.crypt(input);
        a = Hex.toString(ect);
        this.out.println("  plain: " + in);
        this.out.println(" cipher: " + a);
        this.out.println("   cert: " + o3);
        this.passIf(a.equals(o3), "Test #3/3");
    }

    private void test4() throws Exception {
        int j;
        byte[] key = Hex.fromString("FEDCBA9876543210");
        byte[] a = Hex.fromString("FFFFFFFFFFFFFFFF");
        RawSecretKey k = new RawSecretKey("Blowfish", key);
        this.alg.initEncrypt(k);
        this.out.println("\nSpeed test 100,000 x 8-byte:\n");
        this.out.println("...Encryption\n");
        this.out.println("      start date/time: " + new Date().toString());
        int i = 0;
        while (i < 100) {
            j = 0;
            while (j < 1000) {
                this.alg.update(a, 0, 8, a, 0);
                ++j;
            }
            ++i;
        }
        this.out.println("     finish date/time: " + new Date().toString());
        this.alg.initDecrypt(k);
        this.out.println("\n...Decryption\n");
        this.out.println("      start date/time: " + new Date().toString());
        i = 0;
        while (i < 100) {
            j = 0;
            while (j < 1000) {
                this.alg.update(a, 0, 8, a, 0);
                ++j;
            }
            ++i;
        }
        this.out.println("     finish date/time: " + new Date().toString());
        this.out.println("\n result:");
        byte[] x = Hex.fromString("FFFFFFFFFFFFFFFF");
        this.out.println("  computed: " + Hex.dumpString(a));
        this.out.println(" certified: " + Hex.dumpString(x));
        this.passIf(ArrayUtil.areEqual(a, x), "Test #4");
    }
}

