/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.wallet;

import com.claymoresystems.cert.WrappedObject;
import com.claymoresystems.cert.X509Cert;
import com.claymoresystems.crypto.EAYEncryptedPrivateKey;
import com.claymoresystems.ptls.SSLDebug;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.util.Vector;

public class ParsePkcs12 {
    public byte[] calist;
    public byte[] usercerts;
    public byte[] privatekey;

    private native int parse(byte[] var1, byte[] var2) throws Exception;

    private static native void initlib();

    public ParsePkcs12(String path, String pass) throws Exception {
        if (this.parse(path.getBytes(), pass.getBytes()) == 0) {
            throw new Exception("\u52a0\u8f7dOscarKeyStore\u5931\u8d25\uff01");
        }
    }

    public static void main(String[] args) {
        ParsePkcs12 parsepkcs12 = null;
        try {
            parsepkcs12 = new ParsePkcs12("e:\\doc\\cert\\localhost\\rs\\sysdba.p12", "jxaiyy1314");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        catch (Error er) {
            er.printStackTrace();
            return;
        }
        try {
            byte[] root;
            ByteArrayInputStream casbi = new ByteArrayInputStream(parsepkcs12.usercerts);
            ByteArrayInputStream keybi = new ByteArrayInputStream(parsepkcs12.privatekey);
            System.err.println("calist pem encode---------------------------");
            System.out.println(new String(parsepkcs12.calist));
            System.err.println("usercerts pem encode---------------------------");
            System.out.print(new String(parsepkcs12.usercerts));
            System.err.println("privatekey pem encode---------------------------");
            System.out.println(new String(parsepkcs12.privatekey));
            System.err.println("-------------------------------------");
            BufferedReader br = new BufferedReader(new InputStreamReader(casbi));
            BufferedReader keybr = new BufferedReader(new InputStreamReader(keybi));
            Vector<byte[]> root_list = new Vector<byte[]>();
            int num = 0;
            while ((root = WrappedObject.loadObject(br, "CERTIFICATE", null)) != null) {
                ++num;
                SSLDebug.debug(16, "Loading root", root);
                root_list.addElement(root);
            }
            System.err.println(num + "----------------------------------");
            for (int i = 0; i < root_list.size(); ++i) {
                X509Cert cert = new X509Cert((byte[])root_list.elementAt(i));
                System.err.println(cert.getSubjectName().getNameString());
            }
            System.err.println(num + "----------------------------------");
            StringBuffer keyType = new StringBuffer();
            SSLDebug.debug(16, "Loading key file");
            if (!WrappedObject.findObject(keybr, "PRIVATE KEY", keyType)) {
                throw new IOException("Couldn't find private key in this file");
            }
            try {
                PrivateKey tmpPrivateKey = EAYEncryptedPrivateKey.createPrivateKey(keybr, keyType.toString(), "".getBytes());
                System.err.println("key-----------------------");
                System.err.println(new String(tmpPrivateKey.getAlgorithm()));
                System.err.println("--------------------------");
            }
            catch (IllegalArgumentException e) {
                throw new IOException(e.toString());
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    static {
        System.loadLibrary("jdbcp12psr");
        ParsePkcs12.initlib();
    }
}

