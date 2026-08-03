/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.provider.key.RawSecretKey;
import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import xjava.security.Cipher;

class TestDES_EDE3
extends BaseTest {
    private static final String[][] testData1 = new String[][]{{"010101010101010101010101010101010101010101010101", "95F8A5E5DD31D900", "8000000000000000"}, {"010101010101010101010101010101010101010101010101", "9D64555A9A10B852", "0000001000000000"}, {"3849674C2602319E3849674C2602319E3849674C2602319E", "51454B582DDF440A", "7178876E01F19B2A"}, {"04B915BA43FEB5B604B915BA43FEB5B604B915BA43FEB5B6", "42FD443059577FA2", "AF37FB421F8C4095"}, {"0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF", "736F6D6564617461", "3D124FE2198BA318"}, {"0123456789ABCDEF55555555555555550123456789ABCDEF", "736F6D6564617461", "FBABA1FF9D05E9B1"}, {"0123456789ABCDEF5555555555555555FEDCBA9876543210", "736F6D6564617461", "18d748e563620572"}, {"0352020767208217860287665908219864056ABDFEA93457", "7371756967676C65", "c07d2a0fa566fa30"}, {"010101010101010180010101010101010101010101010102", "0000000000000000", "e6e6dd5b7e722974"}, {"10461034899880209107D0158919010119079210981A0101", "0000000000000000", "e1ef62c332fe825b"}};

    TestDES_EDE3() {
    }

    public static void main(String[] args) {
        new TestDES_EDE3().commandline(args);
    }

    protected void engineTest() throws Exception {
        boolean good = false;
        int fails = 0;
        String[][] data = testData1;
        Cipher alg = Cipher.getInstance("DES-EDE3", "Cryptix");
        this.setExpectedPasses(2 * data.length);
        int i = 0;
        while (i < data.length) {
            this.out.println("     key:" + data[i][0]);
            RawSecretKey key = new RawSecretKey("DES_EDE3", Hex.fromString(data[i][0]));
            alg.initEncrypt(key);
            byte[] ect = alg.crypt(Hex.fromString(data[i][1]));
            String a = Hex.toString(ect);
            alg.initDecrypt(key);
            byte[] dct = alg.crypt(ect);
            String b = Hex.toString(dct);
            this.out.println("     p:" + data[i][1] + " enc:" + a + " calc:" + data[i][2]);
            boolean ok = a.equalsIgnoreCase(data[i][2]);
            this.passIf(ok, "Data Set #" + (i + 1));
            if (ok) {
                this.out.println("   * Encrypt good");
            } else {
                this.out.println("===> Encrypt (#" + ++fails + ") FAILED <===");
            }
            this.out.println("   enc:" + Hex.toString(ect) + " dec:" + b + " calc:" + data[i][1]);
            ok = b.equalsIgnoreCase(data[i][1]);
            this.passIf(ok, "Data Set #" + (i + 1));
            if (ok) {
                this.out.println("   * Decrypt good");
            } else {
                this.out.println("===> Decrypt (#" + ++fails + ") FAILED  <===");
            }
            this.out.println("");
            ++i;
        }
        this.out.println("\nDES_EDE3 succeeded (" + i + " data tests)");
    }
}

