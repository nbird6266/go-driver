/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.crypt;

import com.claymoresystems.cert.WrappedObject;
import com.claymoresystems.crypto.EAYEncryptedPrivateKey;
import com.claymoresystems.ptls.LoadProviders;
import com.claymoresystems.ptls.SSLDebug;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PrivateKey;

public class PrivateKeyReader {
    private PrivateKeyReader() {
    }

    public static PrivateKey readFromStream(InputStream is, String passphrase) throws IOException {
        PrivateKey tmpPrivateKey;
        int r;
        byte[] blk = new byte[1024];
        ByteArrayOutputStream tos = new ByteArrayOutputStream();
        while ((r = is.read(blk)) > 0) {
            tos.write(blk, 0, r);
        }
        byte[] tmp = tos.toByteArray();
        ByteArrayInputStream tis = new ByteArrayInputStream(tmp);
        BufferedReader br = new BufferedReader(new InputStreamReader(tis));
        StringBuffer keyType = new StringBuffer();
        SSLDebug.debug(16, "Loading key file");
        if (!WrappedObject.findObject(br, "PRIVATE KEY", keyType)) {
            throw new IOException("Couldn't find private key in this file");
        }
        try {
            try {
                tmpPrivateKey = EAYEncryptedPrivateKey.createPrivateKey(br, keyType.toString(), passphrase.getBytes());
            }
            catch (IllegalArgumentException e) {
                throw new IOException(e.toString());
            }
            catch (Exception ex) {
                if (ex.getMessage() == null || ex.getMessage().length() == 0) {
                    throw new IOException("\u89e3\u6790\u79c1\u94a5\u5931\u8d25");
                }
                throw new IOException("\u89e3\u6790\u79c1\u94a5\u5931\u8d25: " + ex.getMessage());
            }
            Object var12_10 = null;
        }
        catch (Throwable throwable) {
            Object var12_11 = null;
            is.close();
            throw throwable;
        }
        is.close();
        if (tmpPrivateKey == null) {
            throw new IOException("\u8bfb\u53d6\u79c1\u94a5\u5931\u8d25");
        }
        return tmpPrivateKey;
    }

    public static PrivateKey readFromFile(String filename, String pass) throws IOException {
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("\u8bfb\u53d6\u6587\u4ef6: " + filename + "\u5931\u8d25");
        }
        FileInputStream is = new FileInputStream(file);
        return PrivateKeyReader.readFromStream(is, pass);
    }

    public static void main(String[] args) {
        try {
            PrivateKeyReader.readFromFile("D:\\oscar\\admin\\ca512.key", "jxaiyy");
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    static {
        LoadProviders.init();
    }
}

