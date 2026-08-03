/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.sslg;

import com.claymoresystems.sslg.SSLPolicyInt;
import java.io.IOException;
import java.util.Vector;

public interface SSLSocketXInt {
    public static final int CLIENT = 1;
    public static final int SERVER = 2;

    public int getCipherSuite() throws IOException;

    public Vector getCertificateChain() throws IOException;

    public SSLPolicyInt getPolicy();

    public byte[] getSessionID() throws IOException;

    public int getVersion() throws IOException;

    public void renegotiate(SSLPolicyInt var1) throws IOException;

    public void sendClose() throws IOException;

    public void waitForClose(boolean var1) throws IOException;
}

