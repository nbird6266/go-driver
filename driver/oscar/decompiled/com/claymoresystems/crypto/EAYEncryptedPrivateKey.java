/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.crypto;

import com.claymoresystems.cert.EAYDSAPrivateKey;
import com.claymoresystems.cert.EAYRSAPrivateKey;
import com.claymoresystems.crypto.PEMData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.security.PrivateKey;

public class EAYEncryptedPrivateKey {
    private static final String DSA_STRING = "DSA";
    private static final String RSA_STRING = "RSA";

    public static PrivateKey createPrivateKey(BufferedReader in, String type, byte[] password) throws IOException, IllegalArgumentException {
        byte[] plainKey = PEMData.readPEMObject(in, password);
        if (type.equals(DSA_STRING)) {
            return new EAYDSAPrivateKey(plainKey);
        }
        if (type.equals(RSA_STRING)) {
            return new EAYRSAPrivateKey(plainKey);
        }
        throw new InternalError("Couldn't find key type" + type);
    }

    public static void writePrivateKey(PrivateKey priv, byte[] password, BufferedWriter out) throws IOException {
        String keyType = priv.getAlgorithm();
        byte[] rawPrivate = priv.getEncoded();
        String type = keyType + " PRIVATE KEY";
        PEMData.writePEMObject(rawPrivate, password, type, out);
    }
}

