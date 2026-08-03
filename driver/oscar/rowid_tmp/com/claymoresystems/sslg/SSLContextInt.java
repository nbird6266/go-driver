/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.sslg;

import com.claymoresystems.sslg.SSLPolicyInt;
import java.io.IOException;
import java.io.InputStream;

public abstract class SSLContextInt {
    protected SSLPolicyInt policy = new SSLPolicyInt();

    public abstract void loadPKCS12File(String var1, String var2) throws IOException;

    public abstract void loadEAYKeyFile(String var1, String var2) throws IOException;

    public abstract void loadEAYKeyFile(InputStream var1, String var2) throws IOException;

    public abstract void saveEAYKeyFile(String var1, String var2) throws IOException;

    public abstract void useRandomnessFile(String var1, String var2) throws IOException;

    public abstract void loadDHParams(String var1) throws IOException;

    public abstract void loadDHParams(InputStream var1) throws IOException;

    public abstract void saveDHParams(String var1, int var2, boolean var3) throws IOException;

    public abstract void loadRootCertificates(String var1) throws IOException;

    public abstract void loadRootCertificates(InputStream var1) throws IOException;

    public void setPolicy(SSLPolicyInt p) {
        this.policy = p;
    }

    public SSLPolicyInt getPolicy() {
        return this.policy;
    }
}

