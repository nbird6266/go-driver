/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.CertContext;
import com.claymoresystems.cert.CertificateDecodeException;
import com.claymoresystems.cert.CertificateException;
import com.claymoresystems.cert.CertificateVerifyException;
import com.claymoresystems.cert.X509BasicConstraints;
import com.claymoresystems.cert.X509Ext;
import com.claymoresystems.cert.X509KeyUsage;
import com.claymoresystems.cert.X509Name;
import com.claymoresystems.cert.X509SubjectPublicKeyInfo;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.sslg.CertVerifyPolicyInt;
import com.claymoresystems.sslg.Certificate;
import com.claymoresystems.sslg.DistinguishedName;
import cryptix.asn1.encoding.BaseCoder;
import cryptix.asn1.encoding.CoderOperations;
import cryptix.asn1.lang.ASNObject;
import cryptix.asn1.lang.ASNSpecification;
import cryptix.util.core.ArrayUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.DSAPublicKey;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import xjava.security.interfaces.CryptixRSAPublicKey;

public class X509Cert
implements Certificate {
    ASNObject signedCert;
    ASNObject unsignedCert;
    ASNObject issuer;
    ASNObject subject;
    ASNObject sigAlg;
    ASNObject sig;
    byte[] DER;
    byte[] unsignedCertDER;
    byte[] subjectDER;
    byte[] issuerDER;
    byte[] signature;
    String signatureAlgorithm;
    PublicKey pubKey;
    X509Name subjectName;
    X509Name issuerName;
    BigInteger serialNumber;
    Date notBefore;
    Date notAfter;
    Vector extensions = null;
    private static Hashtable oid2NameMap = new Hashtable();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public X509Cert(byte[] ber_) throws CertificateException {
        this.DER = ber_;
        ASNSpecification aSNSpecification = CertContext.getSpec();
        synchronized (aSNSpecification) {
            ASNObject x509 = CertContext.getSpec().getComponent("UsefulCertificate");
            CoderOperations der_coder = BaseCoder.getInstance("DER");
            ByteArrayInputStream is = new ByteArrayInputStream(ber_);
            der_coder.init(is);
            try {
                x509.accept(der_coder, null);
                this.signedCert = x509;
                ASNObject tmp = x509.getComponent("UsefulCertificate.tbsCertificate");
                this.unsignedCertDER = (byte[])tmp.getValue();
                this.sigAlg = x509.getComponent("UsefulCertificate.signatureAlgorithm");
                Vector v = (Vector)this.sigAlg.getValue();
                Vector v2 = (Vector)v.elementAt(0);
                this.signatureAlgorithm = (String)v2.elementAt(0);
                SSLDebug.debug(32, "Signed by " + this.signatureAlgorithm);
                this.sig = x509.getComponent("UsefulCertificate.signature");
                byte[] sig_bs = (byte[])this.sig.getValue();
                if (sig_bs[0] != 0) {
                    throw new IOException();
                }
                this.signature = new byte[sig_bs.length - 1];
                System.arraycopy(sig_bs, 1, this.signature, 0, this.signature.length);
                SSLDebug.debug(32, "Signature ", this.signature);
                this.unsignedCert = CertContext.getSpec().getComponent("UsefulTBSCertificate");
                ASNObject exts = this.unsignedCert.getComponent("UsefulTBSCertificate.extensions");
                exts.setValue(exts.getDefaultValue());
                ASNObject xxx = this.unsignedCert.getComponent("UsefulTBSCertificate.version");
                xxx.setValue(xxx.getDefaultValue());
                xxx = this.unsignedCert.getComponent("UsefulTBSCertificate.issuerUniqueID");
                xxx.setValue(xxx.getDefaultValue());
                xxx = this.unsignedCert.getComponent("UsefulTBSCertificate.subjectUniqueID");
                xxx.setValue(xxx.getDefaultValue());
                SSLDebug.debug(32, "Unsigned cert DER", this.unsignedCertDER);
                is = new ByteArrayInputStream(this.unsignedCertDER);
                der_coder.init(is);
                this.unsignedCert.accept(der_coder, null);
                this.issuer = this.unsignedCert.getComponent("UsefulTBSCertificate.issuer");
                this.issuerDER = (byte[])this.issuer.getValue();
                this.issuerName = new X509Name(this.issuerDER);
                SSLDebug.debug(32, "Issuer DER", this.issuerDER);
                this.subject = this.unsignedCert.getComponent("UsefulTBSCertificate.subject");
                this.subjectDER = (byte[])this.subject.getValue();
                this.subjectName = new X509Name(this.subjectDER);
                SSLDebug.debug(32, "Subject DER", this.subjectDER);
                ASNObject spki = this.unsignedCert.getComponent("UsefulTBSCertificate.subjectPublicKeyInfo");
                byte[] spkiDER = (byte[])spki.getValue();
                this.pubKey = X509SubjectPublicKeyInfo.createPublicKey(spkiDER);
                tmp = this.unsignedCert.getComponent("UsefulTBSCertificate.serialNumber");
                this.serialNumber = (BigInteger)tmp.getValue();
                ASNObject validity = this.unsignedCert.getComponent("UsefulTBSCertificate.validity");
                tmp = validity.getComponent("Validity.notBefore");
                this.notBefore = (Date)tmp.getValue();
                tmp = validity.getComponent("Validity.notAfter");
                this.notAfter = (Date)tmp.getValue();
                ASNObject ext = this.unsignedCert.getComponent("UsefulTBSCertificate.extensions");
                v = (Vector)ext.getValue();
                if (v != null) {
                    for (int i = 0; i < v.size(); ++i) {
                        if (i == 0) {
                            this.extensions = new Vector();
                        }
                        v2 = (Vector)v.elementAt(i);
                        byte[] extnval = (byte[])v2.elementAt(0);
                        this.extensions.addElement(new X509Ext(extnval));
                    }
                }
            }
            catch (IOException e) {
                throw new CertificateDecodeException(e.toString());
            }
        }
    }

    public PublicKey getPublicKey() {
        return this.pubKey;
    }

    public byte[] getDER() {
        return this.DER;
    }

    public byte[] getIssuerDER() {
        return this.issuerDER;
    }

    public byte[] getSubjectDER() {
        return this.subjectDER;
    }

    public DistinguishedName getSubjectName() {
        return this.subjectName;
    }

    public DistinguishedName getIssuerName() {
        return this.issuerName;
    }

    public Date getValidityNotBefore() {
        return this.notBefore;
    }

    public Date getValidityNotAfter() {
        return this.notAfter;
    }

    public Vector getExtensions() {
        return this.extensions;
    }

    public BigInteger getSerial() {
        return this.serialNumber;
    }

    void checkSignatureKey(PublicKey key, String alg) throws CertificateVerifyException {
        if (alg.equals("MD2/RSA") || alg.equals("MD4/RSA") || alg.equals("MD5/RSA") || alg.equals("SHA-1/RSA/PKCS#1")) {
            if (!(key instanceof CryptixRSAPublicKey)) {
                throw new CertificateVerifyException("Public key doesn't match algorithm " + alg);
            }
        } else if (alg.equals("DSA")) {
            if (!(key instanceof DSAPublicKey)) {
                throw new CertificateVerifyException("Public key doesn't match algorithm " + alg);
            }
        } else {
            throw new CertificateVerifyException("Unknown algorithm " + alg);
        }
    }

    public boolean verify(PublicKey key) throws CertificateException {
        try {
            String alg = (String)oid2NameMap.get(this.signatureAlgorithm);
            if (alg != null) {
                SSLDebug.debug(32, "OID " + this.signatureAlgorithm + "mapped to " + alg);
            }
            this.checkSignatureKey(key, alg);
            Signature sig = Signature.getInstance(alg != null ? alg : this.signatureAlgorithm);
            sig.initVerify(key);
            sig.update(this.unsignedCertDER);
            return sig.verify(this.signature);
        }
        catch (NoSuchAlgorithmException e) {
            if (SSLDebug.getDebug(32)) {
                e.printStackTrace();
            }
            throw new CertificateVerifyException(e.toString());
        }
        catch (SignatureException e) {
            if (SSLDebug.getDebug(32)) {
                e.printStackTrace();
            }
            throw new CertificateVerifyException(e.toString());
        }
        catch (InvalidKeyException e) {
            if (SSLDebug.getDebug(32)) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static Vector verifyCertChain(CertContext ctx, Vector certs, CertVerifyPolicyInt policy) throws CertificateException {
        int num_certs = certs.size();
        Vector<X509Cert> chain = new Vector<X509Cert>();
        X509Cert last = null;
        boolean found_root = false;
        int pathLen = 255;
        for (int i = 0; i < num_certs; ++i) {
            int tmpPathLen;
            X509Cert cert = (X509Cert)certs.elementAt(i);
            SSLDebug.debug(32, "Trying to verify", cert.getDER());
            if (!found_root) {
                if (ctx.isRoot(cert.getDER())) {
                    SSLDebug.debug(32, "Is root");
                    last = cert;
                    chain.addElement(last);
                    found_root = true;
                    continue;
                }
                SSLDebug.debug(32, "Trying to find root with DN", cert.getIssuerDER());
                last = ctx.signedByRoot(cert.getIssuerDER());
                if (last == null) {
                    SSLDebug.debug(32, "Nope");
                    continue;
                }
                SSLDebug.debug(32, "Found one");
                chain.addElement(last);
                found_root = true;
            }
            if (!ArrayUtil.areEqual(last.getSubjectDER(), cert.getIssuerDER())) {
                throw new CertificateVerifyException("Subject and issuer name don't match");
            }
            if (!cert.verify(last.getPublicKey())) {
                throw new CertificateVerifyException("Certificate signature doesn't match");
            }
            if (policy.checkDatesP()) {
                X509Cert.checkExpiry(cert, new Date());
            }
            if (chain.size() == 1) {
                tmpPathLen = last.checkBasicConstraintExtension(false, policy.requireBasicConstraintsCriticalP());
                if (++tmpPathLen != -1) {
                    pathLen = tmpPathLen;
                }
                last.checkKeyUsage(false);
            } else {
                tmpPathLen = last.checkBasicConstraintExtension(policy.requireBasicConstraintsP(), policy.requireBasicConstraintsCriticalP());
                if (++tmpPathLen < pathLen) {
                    pathLen = tmpPathLen;
                }
                last.checkKeyUsage(policy.requireKeyUsageP());
            }
            if (pathLen < 1) {
                throw new CertificateVerifyException("No more certificates allowed. Ran out of pathLen");
            }
            --pathLen;
            last = cert;
            chain.addElement(cert);
        }
        if (last != null) {
            return chain;
        }
        return null;
    }

    static void checkExpiry(Certificate cert, Date d) throws CertificateVerifyException {
        Date nb = cert.getValidityNotBefore();
        Date na = cert.getValidityNotAfter();
        if (d.before(nb)) {
            throw new CertificateVerifyException("Certificate not yet valid. Not before date " + nb);
        }
        if (d.after(na)) {
            throw new CertificateVerifyException("Certificate expired. Not after date " + na);
        }
    }

    private int checkBasicConstraintExtension(boolean require, boolean checkCritical) throws CertificateVerifyException {
        X509BasicConstraints bcExt = null;
        try {
            X509Ext ext = X509Ext.getExtensionFromCert(this, X509BasicConstraints.oid);
            if (ext != null) {
                bcExt = new X509BasicConstraints(ext);
            }
        }
        catch (IOException e) {
            throw new CertificateVerifyException("Problem parsing Basic Constraints" + e.toString());
        }
        if (bcExt != null) {
            if (bcExt.isCA()) {
                if (checkCritical && !bcExt.isCritical()) {
                    throw new CertificateVerifyException("Basic constraints for a CA must be critical");
                }
                return bcExt.getPathLen();
            }
            throw new CertificateVerifyException("Basic Constraints present in signing cert but not a CA");
        }
        if (require) {
            throw new CertificateVerifyException("Basic Constraints not present");
        }
        return 255;
    }

    private void checkKeyUsage(boolean require) throws CertificateVerifyException {
        X509KeyUsage kuExt = null;
        try {
            X509Ext ext = X509Ext.getExtensionFromCert(this, X509KeyUsage.oid);
            if (ext != null) {
                kuExt = new X509KeyUsage(ext);
            }
        }
        catch (IOException e) {
            throw new CertificateVerifyException("Problem parsing Key Usage" + e.toString());
        }
        if (kuExt == null) {
            if (require) {
                throw new CertificateVerifyException("Key Usage required for CAs");
            }
        } else if (!kuExt.isAsserted(X509KeyUsage.BIT_keyCertSign)) {
            throw new CertificateVerifyException("Key Usage present but keyCertSign not asserted");
        }
    }

    static {
        oid2NameMap.put("1.2.840.10040.4.3", "DSA");
        oid2NameMap.put("1.2.840.113549.1.1.2", "MD2/RSA");
        oid2NameMap.put("1.2.840.113549.1.1.3", "MD4/RSA");
        oid2NameMap.put("1.2.840.113549.1.1.4", "MD5/RSA");
        oid2NameMap.put("1.2.840.113549.1.1.5", "SHA-1/RSA/PKCS#1");
    }
}

