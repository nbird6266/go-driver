/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class X509CertificateReader {
    private static CertificateFactory cf = null;
    public static String DEFAULT_ALG_NAME = "X.509";

    private X509CertificateReader() throws NoSuchProviderException, CertificateException {
        this(DEFAULT_ALG_NAME, null);
    }

    private X509CertificateReader(String algName, String provider) throws CertificateException, NoSuchProviderException {
        if (algName == null || algName.length() == 0) {
            algName = DEFAULT_ALG_NAME;
        }
        cf = provider == null || provider.length() == 0 ? CertificateFactory.getInstance(algName) : CertificateFactory.getInstance(algName, provider);
    }

    public static X509CertificateReader getInstance() throws NoSuchProviderException, CertificateException {
        return new X509CertificateReader();
    }

    public static X509CertificateReader getInstance(String algName) throws NoSuchProviderException, CertificateException {
        return new X509CertificateReader(algName, null);
    }

    public static X509CertificateReader getInstance(String algName, String provider) throws NoSuchProviderException, CertificateException {
        return new X509CertificateReader(algName, provider);
    }

    public X509Certificate read(String filepath) throws IOException {
        File file = new File(filepath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("\u6587\u4ef6\u8def\u5f84\u4e0d\u6b63\u786e");
        }
        FileInputStream fis = new FileInputStream(file);
        return this.read(fis);
    }

    public X509Certificate read(InputStream is) throws IOException {
        X509Certificate ret = null;
        if (is == null) {
            throw new IOException("\u8bfb\u53d6X509\u683c\u5f0f\u8bc1\u4e66\u5931\u8d25");
        }
        try {
            ret = (X509Certificate)cf.generateCertificate(is);
        }
        catch (CertificateException ex) {
            throw new IOException("\u8bfb\u53d6X509\u683c\u5f0f\u8bc1\u4e66\u5931\u8d25");
        }
        return ret;
    }

    public static void main(String[] args) {
        try {
            X509CertificateReader x509certread = X509CertificateReader.getInstance();
            x509certread.read("");
        }
        catch (NoSuchProviderException ex) {
        }
        catch (CertificateException ex) {
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

