/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.CertContext;
import com.claymoresystems.cert.DERUtils;
import com.claymoresystems.sslg.DistinguishedName;
import cryptix.asn1.encoding.BaseCoder;
import cryptix.asn1.encoding.CoderOperations;
import cryptix.asn1.lang.ASNObject;
import cryptix.asn1.lang.ASNSpecification;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public class X509Name
implements DistinguishedName {
    private static String[][] OIDMAP = new String[][]{{"2.5.4.6", "C"}, {"2.5.4.8", "ST"}, {"2.5.4.7", "L"}, {"2.5.4.9", "STREET"}, {"2.5.4.10", "O"}, {"2.5.4.11", "OU"}, {"2.5.4.3", "CN"}, {"1.2.840.113549.1.9.1", "EmailAddress"}, {"2.5.4.5", "SN"}};
    private byte[] nameDER = null;
    private Vector name = null;
    private String nameString = null;

    public X509Name(byte[] nameDER) {
        this.nameDER = nameDER;
        this.name = X509Name.rawNameToName(nameDER);
    }

    public X509Name(Vector dn) {
        Vector ndn = new Vector();
        for (int i = 0; i < dn.size(); ++i) {
            Vector<String[]> nrdn = new Vector<String[]>();
            Vector rdn = (Vector)dn.elementAt(i);
            for (int j = 0; j < rdn.size(); ++j) {
                String[] nava = new String[2];
                String[] ava = (String[])rdn.elementAt(j);
                nava[0] = new String(ava[0]);
                nava[1] = new String(ava[1]);
                nrdn.addElement(nava);
            }
            ndn.addElement(nrdn);
        }
        this.name = ndn;
    }

    public byte[] getNameDER() {
        try {
            if (this.nameDER == null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                for (int i = 0; i < this.name.size(); ++i) {
                    Vector rdn = (Vector)this.name.elementAt(i);
                    this.encodeRDN(rdn, bos);
                }
                byte[] tmp = bos.toByteArray();
                bos = new ByteArrayOutputStream();
                DERUtils.encodeSequence(tmp, (OutputStream)bos);
                this.nameDER = bos.toByteArray();
            }
            return this.nameDER;
        }
        catch (IOException e) {
            throw new InternalError("Problem encoding: " + e.toString());
        }
    }

    private void encodeRDN(Vector rdn, OutputStream out) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < rdn.size(); ++i) {
            ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
            String[] ava = (String[])rdn.elementAt(i);
            DERUtils.encodeOID(X509Name.lookupComponent(ava[0]), (OutputStream)bos2);
            DERUtils.encodeUnknownString(ava[1], bos2);
            DERUtils.encodeSequence(bos2, (OutputStream)bos);
        }
        DERUtils.encodeSet(bos, out);
    }

    public Vector getName() {
        return this.name;
    }

    public String getNameString() {
        StringBuffer str = new StringBuffer();
        if (this.nameString == null) {
            for (int i = 0; i < this.name.size(); ++i) {
                Vector rdn = (Vector)this.name.elementAt(i);
                if (i > 0) {
                    str.append(",");
                }
                for (int j = 0; j < rdn.size(); ++j) {
                    String[] ava = (String[])rdn.elementAt(j);
                    if (j > 0) {
                        str.append("+");
                    }
                    str.append(ava[0] + "=" + ava[1]);
                }
            }
            this.nameString = str.toString();
        }
        return this.nameString;
    }

    private static String lookupOID(String oid) {
        for (int i = 0; i < OIDMAP.length; ++i) {
            if (!oid.equals(OIDMAP[i][0])) continue;
            return OIDMAP[i][1];
        }
        return "Unknown Attribute(" + oid + ")";
    }

    private static String lookupComponent(String component) {
        for (int i = 0; i < OIDMAP.length; ++i) {
            if (!component.equals(OIDMAP[i][1])) continue;
            return OIDMAP[i][0];
        }
        throw new InternalError("Unknown component type " + component);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Vector rawNameToName(byte[] rawName) {
        Vector nRDNSequence = new Vector();
        ASNSpecification aSNSpecification = CertContext.getSpec();
        synchronized (aSNSpecification) {
            ASNObject name = CertContext.getSpec().getComponent("Name");
            CoderOperations der_coder = BaseCoder.getInstance("DER");
            ByteArrayInputStream is = new ByteArrayInputStream(rawName);
            der_coder.init(is);
            try {
                name.accept(der_coder, null);
                Vector rdnSequence = (Vector)name.getValue();
                X509Name.vecDebug(rdnSequence);
                for (int i = 0; i < rdnSequence.size(); ++i) {
                    Vector xxx = (Vector)rdnSequence.elementAt(i);
                    X509Name.vecDebug(xxx);
                    Vector rDN = (Vector)xxx.elementAt(0);
                    X509Name.vecDebug(rDN);
                    Vector<String[]> nRDN = new Vector<String[]>();
                    for (int j = 0; j < rDN.size(); ++j) {
                        Vector x = (Vector)rDN.elementAt(j);
                        X509Name.vecDebug(x);
                        Vector y = (Vector)x.elementAt(0);
                        X509Name.vecDebug(y);
                        Vector w = (Vector)y.elementAt(0);
                        X509Name.vecDebug(w);
                        String type = (String)w.elementAt(0);
                        String value = (String)w.elementAt(1);
                        String[] aVA = new String[]{X509Name.lookupOID(type), value};
                        nRDN.addElement(aVA);
                    }
                    nRDNSequence.addElement(nRDN);
                }
            }
            catch (IOException e) {
                e.printStackTrace(System.out);
                throw new InternalError(e.toString());
            }
        }
        return nRDNSequence;
    }

    private static void vecDebug(Vector x) {
    }
}

