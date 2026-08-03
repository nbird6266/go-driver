/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.CryptixProperties;
import cryptix.provider.Cryptix;
import cryptix.util.test.BaseTest;
import java.security.Provider;
import java.security.Security;

public class TestInstall
extends BaseTest {
    public static void main(String[] args) {
        new TestInstall().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(1);
        this.out.println(CryptixProperties.getVersionString());
        Provider provider = Security.getProvider("Cryptix");
        boolean ok = provider != null && provider instanceof Cryptix;
        this.passIf(ok, "Cryptix installed?");
        if (!ok) {
            this.out.println("Cryptix is not installed as a provider in the java.security file.");
            this.out.println("Enter \"java cryptix.provider.Install\" to correct this.");
        }
    }
}

