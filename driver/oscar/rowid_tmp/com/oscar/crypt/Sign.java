/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.crypt;

import com.claymoresystems.ptls.LoadProviders;
import com.oscar.crypt.PrivateKeyReader;
import com.oscar.crypt.X509CertificateReader;
import com.oscar.util.Hex;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;

public class Sign {
    public static final String Claymore_RSA_Name = "RawRSA";
    public static final String Cryptix_SHA1withRSA_Name = "SHA-1/RSA/PKCS#1";
    public static final String SunRsaSign_SHA1withRSA_Name = "SHA1withRSA";
    public static final String SunJSSE_SHA1withRSA_Name = "SHA1withRSA";
    public static final String Cryptix_MD5withRSA_Name = "MD5/RSA";
    public static final String SunRsaSign_MD5withRSA_Name = "MD5withRSA";
    public static final String SunJSSE_MD5withRSA_Name = "MD5withRSA";
    private static final String DefaultAlgName = "SHA1withRSA";
    public static final String CryptixProvider = "Cryptix";
    public static final String SunRsaSignProvider = "SunRsaSign";
    public static final String ClaymoreProvider = "ClaymoreProvider";
    public static final String SunJSSEProvider = "SunJSSE";

    private Sign() {
    }

    public static byte[] sign(byte[] in, PrivateKey privatekey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        return Sign.sign(in, privatekey, null, null);
    }

    public static byte[] sign(byte[] in, PrivateKey privatekey, String algName) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        return Sign.sign(in, privatekey, algName, null);
    }

    public static byte[] sign(byte[] in, PrivateKey privatekey, String algName, String provider) throws SignatureException, InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException {
        Signature signingEngine = null;
        if (algName == null || algName.length() == 0) {
            algName = "SHA1withRSA";
        }
        signingEngine = provider == null || provider.length() == 0 ? Signature.getInstance(algName) : Signature.getInstance(algName, provider);
        signingEngine.initSign(privatekey);
        signingEngine.update(in);
        return signingEngine.sign();
    }

    public static void veify(byte[] to_be_signed, byte[] have_signed, PublicKey publickey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Sign.veify(to_be_signed, have_signed, publickey, null, null);
    }

    public static void veify(byte[] to_be_signed, byte[] have_signed, PublicKey publickey, String algName) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Sign.veify(to_be_signed, have_signed, publickey, algName, null);
    }

    public static void veify(byte[] to_be_signed, byte[] have_signed, PublicKey publickey, String algName, String provider) throws SignatureException, InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException {
        Signature signature1 = null;
        if (algName == null || algName.length() == 0) {
            algName = "SHA1withRSA";
        }
        signature1 = provider == null || provider.length() == 0 ? Signature.getInstance(algName) : Signature.getInstance(algName, provider);
        signature1.initVerify(publickey);
        signature1.update(to_be_signed, 0, to_be_signed.length);
        if (!signature1.verify(have_signed)) {
            throw new SignatureException("\u7b7e\u540d\u4e0d\u5339\u914d!");
        }
    }

    public static void main(String[] args) {
        PrivateKey pk = null;
        X509Certificate cert = null;
        PublicKey puk = null;
        byte[] origin = "jxaiyy1314".getBytes();
        byte[] result = null;
        try {
            pk = PrivateKeyReader.readFromFile("D:\\temp\\sslTest\\unTrustCa\\sysdba.key", "0000");
            result = Sign.sign(origin, pk, Cryptix_SHA1withRSA_Name, CryptixProvider);
            Hex.hexprint_series(origin);
            Hex.hexprint(result);
            Hex.hexprint_series(result);
            X509CertificateReader x509certread = X509CertificateReader.getInstance();
            cert = x509certread.read("D:\\temp\\sslTest\\unTrustCa\\sysdba.crt");
            puk = cert.getPublicKey();
            Sign.veify(origin, result, puk, "SHA1withRSA", SunRsaSignProvider);
        }
        catch (Throwable ex2) {
            ex2.printStackTrace();
            return;
        }
        System.out.println("\u7b7e\u540d\u6b63\u786e");
    }

    static {
        LoadProviders.init();
    }
}

