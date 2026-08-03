/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.crypt;

import com.claymoresystems.cert.EAYDSAPrivateKey;
import com.claymoresystems.cert.EAYRSAPrivateKey;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;

public class PrivateKeyConversion {
    public static String CryptixProvider = "Cryptix";
    public static String SunRsaSignProvider = "SunRsaSign";

    private PrivateKeyConversion() {
    }

    public static PrivateKey conver(PrivateKey origin, String from, String to) throws Exception {
        throw new Exception("\u5c1a\u672a\u5b9e\u73b0");
    }

    public static PrivateKey converEAYEncryptedKey(PrivateKey originPrivateKey) throws Exception {
        return PrivateKeyConversion.converEAYEncryptedKey(originPrivateKey, null);
    }

    public static PrivateKey converEAYEncryptedKey(PrivateKey originPrivateKey, String to) throws Exception {
        PrivateKey PrivateKey2 = null;
        if (originPrivateKey instanceof EAYRSAPrivateKey) {
            EAYRSAPrivateKey tmpRSAPrivateKey = (EAYRSAPrivateKey)originPrivateKey;
            RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(tmpRSAPrivateKey.getModulus(), tmpRSAPrivateKey.getExponent());
            try {
                KeyFactory keyFactory = null;
                keyFactory = to == null || to.length() == 0 ? KeyFactory.getInstance("RSA") : KeyFactory.getInstance("RSA", to);
                PrivateKey2 = keyFactory.generatePrivate(rsaPrivateKeySpec);
            }
            catch (NoSuchAlgorithmException ex) {
                throw new IOException("\u8bfb\u53d6\u79c1\u94a5\u5931\u8d25:\u6ca1\u6709\u627e\u5230\u6253\u5f00\u8be5\u79c1\u94a5\u7684\u7b97\u6cd5\u5b9e\u73b0");
            }
            catch (InvalidKeySpecException ex) {
                throw new IOException("\u8bfb\u53d6\u79c1\u94a5\u5931\u8d25:\u79c1\u94a5\u683c\u5f0f\u65e0\u6548");
            }
        } else if (originPrivateKey instanceof EAYDSAPrivateKey) {
            throw new Exception("\u5c1a\u672a\u5b9e\u73b0");
        }
        return PrivateKey2;
    }

    public static void main(String[] args) {
        PrivateKeyConversion eayencryptedprivatekey_to_othertype = new PrivateKeyConversion();
    }
}

