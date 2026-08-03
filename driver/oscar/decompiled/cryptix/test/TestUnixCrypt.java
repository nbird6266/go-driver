/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.tools.UnixCrypt;
import cryptix.util.test.BaseTest;

public class TestUnixCrypt
extends BaseTest {
    public static void main(String[] args) {
        new TestUnixCrypt().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(1);
        String original = "CryptixRulez";
        String salt = "OK";
        String solution = "OKDvOv8WCyJBI";
        UnixCrypt jc = new UnixCrypt(salt);
        String crypted = jc.crypt(original);
        this.out.println("original = \"" + original + "\", salt = \"" + salt + "\", solution = \"" + solution + "\",\n crypted = \"" + crypted + "\"");
        this.passIf(solution.equals(crypted), "UnixCrypt");
    }
}

