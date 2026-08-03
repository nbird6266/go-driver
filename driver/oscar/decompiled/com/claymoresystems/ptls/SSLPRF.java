/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLv3PRF;
import com.claymoresystems.ptls.TLSPRF;

abstract class SSLPRF {
    public static final int SSL_PRF_MASTER_SECRET = 1;
    public static final int SSL_PRF_KEY_BLOCK = 2;
    public static final int SSL_PRF_CLIENT_WRITE_KEY = 3;
    public static final int SSL_PRF_SERVER_WRITE_KEY = 4;
    public static final int SSL_PRF_CLIENT_WRITE_IV = 5;
    public static final int SSL_PRF_SERVER_WRITE_IV = 6;

    SSLPRF() {
    }

    public static SSLPRF getPRFInstance(int version) {
        switch (version) {
            case 768: {
                return new SSLv3PRF();
            }
            case 769: {
                return new TLSPRF();
            }
        }
        throw new Error("Invalid SSL version");
    }

    abstract void PRF(byte[] var1, int var2, byte[] var3, byte[] var4, byte[] var5);
}

