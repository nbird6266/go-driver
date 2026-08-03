/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.crypt;

import com.claymoresystems.cert.WrappedObject;
import com.claymoresystems.cert.X509Cert;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class X509CertReader {
    private X509CertReader() {
    }

    public static X509Cert read(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("\u6587\u4ef6\u8def\u5f84\u4e0d\u6b63\u786e");
        }
        FileInputStream fis = new FileInputStream(file);
        return X509CertReader.read(fis);
    }

    public static X509Cert read(InputStream is) throws IOException {
        int r;
        X509Cert cert = null;
        byte[] blk = new byte[1024];
        ByteArrayOutputStream tos = new ByteArrayOutputStream();
        while ((r = is.read(blk)) > 0) {
            tos.write(blk, 0, r);
        }
        byte[] tmp = tos.toByteArray();
        ByteArrayInputStream tis = new ByteArrayInputStream(tmp);
        BufferedReader br = new BufferedReader(new InputStreamReader(tis));
        byte[] cert_byte = WrappedObject.loadObject(br, "CERTIFICATE", null);
        if (cert_byte == null) {
            throw new IOException("\u8bfb\u53d6\u8bc1\u4e66\u5931\u8d25");
        }
        cert = new X509Cert(cert_byte);
        if (cert == null) {
            throw new IOException("\u8bfb\u53d6\u8bc1\u4e66\u5931\u8d25");
        }
        return cert;
    }

    public static void main(String[] args) {
        X509Cert cert = null;
        try {
            cert = X509CertReader.read("D:\\oscar\\admin\\ca512.crt");
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        System.out.println(cert.getSubjectName().getNameString());
    }
}

