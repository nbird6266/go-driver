/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.CertificateException;
import com.claymoresystems.cert.Pickledx509;
import com.claymoresystems.cert.X509Cert;
import com.claymoresystems.ptls.SSLDebug;
import cryptix.asn1.lang.ASNSpecification;
import cryptix.asn1.lang.ParseException;
import cryptix.asn1.lang.Parser;
import cryptix.util.core.ArrayUtil;
import java.util.Vector;

public class CertContext {
    Vector root_list = new Vector();
    static Parser parser = new Parser(new Pickledx509());
    static ASNSpecification spec;

    public CertContext() {
    }

    public CertContext(Vector roots) {
        if (roots != null) {
            for (int i = 0; i < roots.size(); ++i) {
                this.addRoot((byte[])roots.elementAt(i));
            }
        }
    }

    public static ASNSpecification getSpec() {
        return spec;
    }

    public void addRoot(byte[] root_ber) {
        X509Cert cert;
        try {
            cert = new X509Cert(root_ber);
        }
        catch (CertificateException e) {
            SSLDebug.debug(32, "Couldn't parse. Skipping cert", root_ber);
            return;
        }
        this.root_list.addElement(cert);
        SSLDebug.debug(32, "Adding root with DN", cert.getSubjectDER());
    }

    public Vector getRootList() {
        return this.root_list;
    }

    public boolean isRoot(byte[] cert) {
        for (int i = 0; i < this.root_list.size(); ++i) {
            byte[] root = ((X509Cert)this.root_list.elementAt(i)).getDER();
            if (!ArrayUtil.areEqual(cert, root)) continue;
            return true;
        }
        return false;
    }

    public X509Cert signedByRoot(byte[] issuer) {
        for (int i = 0; i < this.root_list.size(); ++i) {
            X509Cert root = (X509Cert)this.root_list.elementAt(i);
            byte[] subject = root.getSubjectDER();
            if (!ArrayUtil.areEqual(issuer, subject)) continue;
            return root;
        }
        return null;
    }

    static {
        parser.disable_tracing();
        try {
            spec = parser.Specification(false);
        }
        catch (ParseException e) {
            throw new InternalError(e.toString());
        }
    }
}

