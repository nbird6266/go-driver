/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.CertContext;
import com.claymoresystems.cert.WrappedObject;
import com.claymoresystems.cert.X509Cert;
import com.claymoresystems.ptls.LoadProviders;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.sslg.CertVerifyPolicyInt;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;

public class CertVerify {
    public static CertContext cctx = new CertContext();

    public static void loadRoots(String file) throws Exception {
        byte[] root;
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        while ((root = WrappedObject.loadObject(br, "CERTIFICATE", null)) != null) {
            cctx.addRoot(root);
        }
    }

    public static byte[] loadCert(String file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        byte[] cert = WrappedObject.loadObject(br, "CERTIFICATE", null);
        return cert;
    }

    public static void main(String[] args) throws Exception {
        int argIndex;
        LoadProviders.init();
        boolean damage = false;
        CertVerifyPolicyInt policy = new CertVerifyPolicyInt();
        for (argIndex = 0; argIndex < args.length && args[argIndex].startsWith("-"); ++argIndex) {
            if (args[argIndex].equals("-debug")) {
                SSLDebug.setDebug(40);
                continue;
            }
            if (args[argIndex].equals("-damage")) {
                damage = true;
                continue;
            }
            if (args[argIndex].equals("-checkbc")) {
                policy.requireBasicConstraints(true);
                continue;
            }
            if (args[argIndex].equals("-bccritical")) {
                policy.requireBasicConstraintsCritical(true);
                continue;
            }
            if (args[argIndex].equals("-checkdates")) {
                policy.checkDates(true);
                continue;
            }
            if (args[argIndex].equals("-checkkeyusage")) {
                policy.requireKeyUsage(true);
                continue;
            }
            throw new InternalError("Bogus argument " + args[argIndex]);
        }
        CertVerify.loadRoots(args[argIndex++]);
        Vector<X509Cert> vec = new Vector<X509Cert>();
        while (argIndex < args.length) {
            byte[] cert_ber = CertVerify.loadCert(args[argIndex]);
            if (damage && argIndex == args.length - 1) {
                int n = cert_ber.length - 1;
                cert_ber[n] = (byte)(cert_ber[n] + 1);
            }
            X509Cert cert = new X509Cert(cert_ber);
            vec.addElement(cert);
            ++argIndex;
        }
        Vector vchain = X509Cert.verifyCertChain(cctx, vec, policy);
        if (vchain == null) {
            System.out.println("Couldn't verify chain");
        } else if (vchain.size() == 1) {
            System.out.println("IS ROOT! Didn't actually verify signature");
        } else {
            System.out.println("Verified successfully");
            for (int i = 0; i < vchain.size(); ++i) {
                X509Cert cert = (X509Cert)vchain.elementAt(i);
                System.out.println("Issuer " + cert.getIssuerName().getNameString());
                System.out.println("Subject " + cert.getSubjectName().getNameString());
                System.out.println("Serial " + cert.getSerial());
                System.out.println("Validity " + cert.getValidityNotBefore() + "-" + cert.getValidityNotAfter());
            }
        }
    }
}

