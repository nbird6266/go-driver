/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.DERUtils;
import com.claymoresystems.cert.X509Cert;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.sslg.Extension;
import cryptix.util.core.ArrayUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Vector;

public class X509Ext
implements Extension {
    private byte[] oid;
    private boolean critical;
    private byte[] value;

    X509Ext(byte[] encoding) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(encoding);
        byte[] inner = DERUtils.decodeSequence(is);
        is = new ByteArrayInputStream(inner);
        this.oid = DERUtils.decodeOID(is);
        if (DERUtils.isTag(1, is)) {
            this.critical = DERUtils.decodeBoolean(is);
        }
        this.value = DERUtils.decodeAny(is);
    }

    public byte[] getOID() {
        return this.oid;
    }

    public boolean isCritical() {
        return this.critical;
    }

    public byte[] getValue() {
        return this.value;
    }

    static X509Ext getExtensionFromCert(X509Cert cert, byte[] oid) throws IOException {
        X509Ext fExt = null;
        Vector extensions = cert.getExtensions();
        SSLDebug.debug(32, "Looking for extension", oid);
        if (extensions == null) {
            return null;
        }
        for (int i = 0; i < extensions.size(); ++i) {
            X509Ext ext = (X509Ext)extensions.elementAt(i);
            SSLDebug.debug(32, "Found extension", ext.getOID());
            if (!ArrayUtil.areEqual(ext.getOID(), oid)) continue;
            if (fExt != null) {
                throw new IOException("Can't have two extensions of this type");
            }
            fExt = ext;
        }
        return fExt;
    }
}

