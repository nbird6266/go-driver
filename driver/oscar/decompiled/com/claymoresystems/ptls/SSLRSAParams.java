/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLopaque;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import xjava.security.interfaces.CryptixRSAPublicKey;

class SSLRSAParams
extends SSLPDU {
    SSLopaque RSA_modulus = new SSLopaque(-65535);
    SSLopaque RSA_exponent = new SSLopaque(-65535);

    public SSLRSAParams() {
    }

    public SSLRSAParams(CryptixRSAPublicKey key) {
        this.RSA_modulus.value = this.toBytes(key.getModulus());
        this.RSA_exponent.value = this.toBytes(key.getExponent());
    }

    public int decode(SSLConn conn, InputStream s) throws Error, IOException {
        int r = 0;
        r = this.RSA_modulus.decode(conn, s);
        return r += this.RSA_exponent.decode(conn, s);
    }

    public int encode(SSLConn conn, OutputStream s) throws Error, IOException {
        int r = 0;
        r = this.RSA_modulus.encode(conn, s);
        return r += this.RSA_exponent.encode(conn, s);
    }

    private byte[] toBytes(BigInteger num) {
        byte[] tmp = num.toByteArray();
        if (tmp[0] == 0) {
            byte[] trim = new byte[tmp.length - 1];
            System.arraycopy(tmp, 1, trim, 0, tmp.length - 1);
            return trim;
        }
        return tmp;
    }
}

