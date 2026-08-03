/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.crypt;

import com.claymoresystems.cert.X509Cert;
import com.claymoresystems.ptls.LoadProviders;
import com.oscar.crypt.PrivateKeyReader;
import com.oscar.crypt.X509CertReader;
import com.oscar.util.Hex;
import java.security.Key;
import java.security.PrivateKey;
import xjava.security.Cipher;
import xjava.security.IJCE;
import xjava.security.PaddingScheme;
import xjava.security.interfaces.RSAKey;

public class RSAEncrypt {
    public static final String Cryptix_RSA_Name = "RSA";
    public static final String Cryptix_PaddingScheme_NONE_Name = "NONE";
    public static final String Cryptix_PaddingScheme_PKCS7_Name = "PKCS#7";
    public static final String Cryptix_PaddingScheme_OneAndZeroes_Name = "OneAndZeroes";
    public static final String Cryptix_Provider = "Cryptix";
    public static final String Default_RSA_AlgName = "RSA";
    public static final String Default_PaddingScheme_AlgName = "OneAndZeroes";
    public static final String Default_mod_Name = null;
    public static final int isprovider = 1;
    public static final int ispaddion = 2;
    private Cipher rsacipher = null;

    private RSAEncrypt() throws Exception {
        this(null, null, null, null);
    }

    private RSAEncrypt(String algName, String mode, String paddingName, String provider) throws Exception {
        PaddingScheme paddingscheme = null;
        if (algName == null || algName.length() == 0) {
            algName = "RSA";
        }
        if (paddingName == null || paddingName.length() == 0) {
            paddingName = "OneAndZeroes";
        }
        this.rsacipher = provider == null || provider.length() == 0 ? Cipher.getInstance(algName) : Cipher.getInstance(algName, provider);
        if (!paddingName.equals(Cryptix_PaddingScheme_NONE_Name)) {
            paddingscheme = (PaddingScheme)IJCE.getImplementation(paddingName, this.rsacipher.getProvider(), "PaddingScheme");
        }
        if (mode == null || mode.length() == 0) {
            if (paddingscheme != null) {
                this.rsacipher = Cipher.getInstance(this.rsacipher, null, paddingscheme);
            }
        } else {
            throw new Exception("\u5c1a\u672a\u5b9e\u73b0");
        }
    }

    public static RSAEncrypt getInstance() throws Exception {
        return new RSAEncrypt();
    }

    public static RSAEncrypt getInstance(String algName) throws Exception {
        return new RSAEncrypt(algName, null, null, null);
    }

    public static RSAEncrypt getInstance(String algName, String name, int providerOrPadding) throws Exception {
        switch (providerOrPadding) {
            case 1: {
                return new RSAEncrypt(algName, Default_mod_Name, null, name);
            }
            case 2: {
                return new RSAEncrypt(algName, Default_mod_Name, name, null);
            }
        }
        throw new Exception("\u65e0\u6548\u9009\u9879");
    }

    public static RSAEncrypt getInstance(String algName, String paddingName, String provider) throws Exception {
        return new RSAEncrypt(algName, Default_mod_Name, paddingName, provider);
    }

    public byte[] encrypt(Key key, byte[] plainText) throws Exception {
        byte[] ret = null;
        if (!(key instanceof RSAKey)) {
            throw new Exception("\u8be5\u79c1\u94a5\u4e0d\u662fRSAKey\u7684\u5b50\u7c7b");
        }
        this.rsacipher.initEncrypt(key);
        ret = this.rsacipher.crypt(plainText);
        return ret;
    }

    public byte[] decrypt(Key key, byte[] encryptText) throws Exception {
        byte[] ret = null;
        if (!(key instanceof RSAKey)) {
            throw new Exception("\u8be5\u79c1\u94a5\u4e0d\u662fRSAKey\u7684\u5b50\u7c7b");
        }
        this.rsacipher.initDecrypt(key);
        ret = this.rsacipher.crypt(encryptText);
        return ret;
    }

    public static void main(String[] args) {
        X509Cert cert = null;
        byte[] plainText = "123456789012345678901234567890123456789012345678901234567890123".getBytes();
        byte[] encryptText = null;
        byte[] decryptText = null;
        try {
            RSAEncrypt rsaEncrypt = RSAEncrypt.getInstance(null, Cryptix_PaddingScheme_NONE_Name, null);
            RSAEncrypt rsaEncrypt1 = RSAEncrypt.getInstance(null, Cryptix_PaddingScheme_NONE_Name, null);
            cert = X509CertReader.read("D:\\oscar\\admin\\ca512.crt");
            encryptText = rsaEncrypt.encrypt(cert.getPublicKey(), plainText);
            Hex.hexprint_series(plainText);
            Hex.hexprint_series(encryptText);
            PrivateKey key = PrivateKeyReader.readFromFile("D:\\oscar\\admin\\ca512.key", "jxaiyy");
            decryptText = rsaEncrypt1.decrypt(key, encryptText);
            Hex.hexprint_series(decryptText);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static {
        LoadProviders.init();
    }
}

