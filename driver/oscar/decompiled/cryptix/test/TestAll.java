/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.util.test.BaseTest;
import cryptix.util.test.TestException;

class TestAll
extends BaseTest {
    private boolean allVerbose;
    private static String[] tests = new String[]{"Install", "3LFSR", "Base64Stream", "BR", "IJCE", "Blowfish", "CAST5", "DES", "DES_EDE3", "IDEA", "LOKI91", "RC2", "RC4", "Rijndael", "SAFER", "SPEED", "Square", "HAVAL", "MD2", "MD4", "MD5", "RIPEMD128", "RIPEMD160", "SHA0", "SHA1", "HMAC", "Scar", "UnixCrypt", "RSA", "ElGamal"};

    TestAll() {
    }

    protected void parseOption(String option) throws TestException {
        if (option.equalsIgnoreCase("-allVerbose")) {
            this.allVerbose = true;
        } else {
            super.parseOption(option);
        }
    }

    public String describeOptions() {
        return String.valueOf(super.describeOptions()) + "    -allVerbose: print full output for each test class.\n";
    }

    public static void main(String[] args) {
        new TestAll().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(tests.length);
        if (this.allVerbose) {
            this.setVerbose(true);
        }
        int i = 0;
        while (i < tests.length) {
            this.out.println("---------------------------------------------------------------------------");
            String classname = "cryptix.test.Test" + tests[i];
            this.status.print("\n>>> " + classname);
            this.status.flush();
            try {
                Object obj = Class.forName(classname).newInstance();
                if (obj instanceof BaseTest) {
                    BaseTest test = (BaseTest)obj;
                    test.setOutput(this.out);
                    test.setVerbose(this.allVerbose);
                    test.test();
                    this.passIf(test.isOverallPass(), classname);
                } else {
                    this.error("Test class does not extend cryptix.util.test.BaseTest");
                }
            }
            catch (ClassNotFoundException e) {
                this.skip("Class not found");
            }
            catch (TestException e) {
                this.fail(e.getMessage());
            }
            catch (Exception e) {
                this.error(e);
            }
            ++i;
        }
    }
}

