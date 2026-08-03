/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.DERUtils;
import com.claymoresystems.cert.X509Ext;
import com.claymoresystems.ptls.SSLDebug;
import java.io.ByteArrayInputStream;
import java.io.IOException;

class X509BasicConstraints {
    private boolean critical = false;
    private boolean cA = false;
    private int pathLen = 255;
    static byte[] oid = new byte[]{85, 29, 19};

    X509BasicConstraints(X509Ext ext) throws IOException {
        this.critical = ext.isCritical();
        SSLDebug.debug(32, "Contents of basic constraints", ext.getValue());
        ByteArrayInputStream bis = new ByteArrayInputStream(ext.getValue());
        byte[] encoding = DERUtils.decodeOctetString(bis);
        if (bis.available() != 0) {
            throw new IOException("Overlong Basic Constraints encoding, bytes left=" + bis.available());
        }
        SSLDebug.debug(32, "Sequence encoding", encoding);
        bis = new ByteArrayInputStream(encoding);
        byte[] tmp = DERUtils.decodeSequence(bis);
        if (bis.available() != 0) {
            throw new IOException("Overlong Basic Constraints encoding, bytes left=" + bis.available());
        }
        SSLDebug.debug(32, "Sequence internal data", tmp);
        bis = new ByteArrayInputStream(tmp);
        if (bis.available() > 0) {
            if (DERUtils.isTag(1, bis)) {
                this.cA = DERUtils.decodeBoolean(bis);
            }
            if (bis.available() > 0 && DERUtils.isTag(2, bis)) {
                this.pathLen = DERUtils.decodeIntegerX(bis);
            }
        }
        if (bis.available() != 0) {
            throw new IOException("Bad encoding for Basic Constraints");
        }
    }

    boolean isCritical() {
        return this.critical;
    }

    boolean isCA() {
        return this.cA;
    }

    int getPathLen() {
        return this.pathLen;
    }
}

