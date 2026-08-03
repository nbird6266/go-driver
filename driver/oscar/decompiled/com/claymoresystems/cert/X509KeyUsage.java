/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.DERUtils;
import com.claymoresystems.cert.X509Ext;
import com.claymoresystems.ptls.SSLDebug;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.BitSet;

class X509KeyUsage {
    static byte[] oid = new byte[]{85, 29, 15};
    static int BIT_digitalSignature = 0;
    static int BIT_nonRepudiation = 1;
    static int BIT_keyEncipherment = 2;
    static int BIT_dataEncipherment = 3;
    static int BIT_keyAgreement = 4;
    static int BIT_keyCertSign = 5;
    static int BIT_cRLSign = 6;
    static int BIT_encipherOnly = 7;
    static int BIT_decipherOnly = 8;
    private boolean critical;
    BitSet bitsAsserted = null;

    X509KeyUsage(X509Ext ext) throws IOException {
        this.critical = ext.isCritical();
        SSLDebug.debug(32, "Contents of keyUsage", ext.getValue());
        ByteArrayInputStream bis = new ByteArrayInputStream(ext.getValue());
        byte[] encoding = DERUtils.decodeOctetString(bis);
        if (bis.available() != 0) {
            throw new IOException("Overlong keyUsage encoding, bytes left=" + bis.available());
        }
        SSLDebug.debug(32, "Sequence encoding", encoding);
        bis = new ByteArrayInputStream(encoding);
        this.bitsAsserted = DERUtils.decodeBitStringX(bis);
        if (bis.available() != 0) {
            throw new IOException("Overlong keyUsage encoding, bytes left=" + bis.available());
        }
    }

    boolean isAsserted(int bit) {
        return this.bitsAsserted.get(bit);
    }
}

