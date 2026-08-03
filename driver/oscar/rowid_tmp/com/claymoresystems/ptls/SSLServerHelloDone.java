/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLPDU;
import java.io.IOException;
import java.io.OutputStream;

class SSLServerHelloDone
extends SSLPDU {
    SSLServerHelloDone() {
    }

    public int encode(SSLConn conn, OutputStream s) throws IOException {
        return 0;
    }
}

