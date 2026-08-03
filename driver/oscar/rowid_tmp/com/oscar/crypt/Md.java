/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.crypt;

import com.claymoresystems.ptls.LoadProviders;
import com.oscar.util.Hex;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class Md {
    public static final String SUN_MD5_Name = "MD5";
    public static final String SUN_SHA1_Name = "SHA";
    public static final String SUN_SHA256_Name = "SHA-256";
    public static final String SUN_SHA384_Name = "SHA-384";
    public static final String SUN_SHA512_Name = "SHA-512";
    public static final String Cryptix_MD2_Name = "MD2";
    public static final String Cryptix_MD4_Name = "MD4";
    public static final String Cryptix_MD5_Name = "MD5";
    public static final String Cryptix_SHA_Name = "SHA-0";
    public static final String Cryptix_SHA1_Name = "SHA-1";
    public static final String Cryptix_SHA256_Name = "SHA-256";
    public static final String Cryptix_SHA384_Name = "SHA-384";
    public static final String Cryptix_SHA512_Name = "SHA-512";
    public static final String DefaultAlgName = "SHA";
    public static final String CryptixProvider = "Cryptix";
    public static final String SunProvider = "Sun";

    private Md() {
    }

    public static byte[] md(byte[] in) throws NoSuchAlgorithmException, NoSuchProviderException, Exception {
        return Md.md(in, null);
    }

    public static byte[] md(byte[] in, String algName) throws NoSuchProviderException, NoSuchAlgorithmException, Exception {
        return Md.md(in, algName, null);
    }

    public static byte[] md(byte[] in, String algName, String provider) throws NoSuchAlgorithmException, NoSuchProviderException, Exception {
        MessageDigest md = null;
        byte[] ret = null;
        if (algName == null || algName.length() == 0) {
            algName = "SHA";
        }
        md = provider == null || provider.length() == 0 ? MessageDigest.getInstance(algName) : MessageDigest.getInstance(algName, provider);
        md.update(in);
        ret = md.digest();
        if (ret == null || ret.length == 0) {
            throw new Exception("\u8ba1\u7b97\u6458\u8981\u503c\u5931\u8d25");
        }
        return ret;
    }

    public static boolean veify(byte[] origin, byte[] mded) throws NoSuchAlgorithmException, NoSuchProviderException, Exception {
        return Md.veify(origin, mded, null);
    }

    public static boolean veify(byte[] origin, byte[] mded, String algName) throws NoSuchAlgorithmException, NoSuchProviderException, Exception {
        return Md.veify(origin, mded, algName, null);
    }

    public static boolean veify(byte[] origin, byte[] mded, String algName, String provider) throws NoSuchProviderException, NoSuchAlgorithmException, Exception {
        byte[] temp = null;
        temp = Md.md(origin, algName, provider);
        if (mded == null || temp.length != mded.length) {
            return false;
        }
        for (int i = 0; i < temp.length; ++i) {
            if (temp[i] == mded[i]) continue;
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        byte[] origin = "jxaiyy1314".getBytes();
        byte[] mded = null;
        try {
            mded = Md.md(origin, Cryptix_MD2_Name, CryptixProvider);
            Hex.hexprint_series(origin);
            Hex.hexprint_series(mded);
            if (Md.veify(origin, mded)) {
                System.out.println("\u6563\u5217\u5408\u6cd5");
            } else {
                System.out.println("\u6563\u5217\u4e0d\u5408\u6cd5");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    static {
        LoadProviders.init();
    }
}

