/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.crypto;

import com.claymoresystems.cert.WrappedObject;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.util.RFC822Hdr;
import com.claymoresystems.util.Util;
import cryptix.provider.key.RawSecretKey;
import cryptix.util.core.Hex;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.security.KeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Random;
import xjava.security.Cipher;
import xjava.security.FeedbackCipher;

class PEMData {
    private static Hashtable eay2jce = null;
    private static Hashtable kSize = null;
    private static String encryptionAlg = "DES-EDE3-CBC";

    PEMData() {
    }

    static byte[] readPEMObject(BufferedReader in, byte[] password) throws IOException {
        byte[] plainText;
        RFC822Hdr proc_type = new RFC822Hdr(in);
        RFC822Hdr dek_info = null;
        if (proc_type.getName() != null) {
            if (!proc_type.getSubfield(0).equals("4")) {
                throw new IOException("Unknown proc type" + proc_type.getSubfield(0));
            }
            dek_info = new RFC822Hdr(in);
            try {
                byte[] cipherText = WrappedObject.readBlock(in);
                String alg = dek_info.getSubfield(0);
                String algorithm = (String)eay2jce.get(alg);
                if (algorithm == null) {
                    throw new InternalError("Algorithm " + alg + " not recognized");
                }
                byte[] key_bytes = new byte[((Integer)kSize.get(alg)).intValue()];
                byte[] iv = Hex.fromString(dek_info.getSubfield(1));
                PEMData.EVP_BytesToKey("MD5", iv, password, (short)1, key_bytes, null);
                RawSecretKey key = new RawSecretKey(algorithm, key_bytes);
                Cipher ciph = Cipher.getInstance(algorithm);
                ((FeedbackCipher)((Object)ciph)).setInitializationVector(iv);
                ciph.initDecrypt(key);
                SSLDebug.debug(8, "PEM Data cipherText", cipherText);
                plainText = ciph.crypt(cipherText);
                SSLDebug.debug(8, "PEM Data plaintext", plainText);
            }
            catch (KeyException e) {
                throw new InternalError(e.toString());
            }
            catch (NoSuchAlgorithmException e) {
                throw new InternalError(e.toString());
            }
        } else {
            plainText = WrappedObject.readBlock(in);
        }
        return plainText;
    }

    static void writePEMObject(byte[] plainText, byte[] password, String type, BufferedWriter out) throws IOException {
        byte[] cipherText;
        byte[] iv = new byte[8];
        Random r = new Random();
        r.nextBytes(iv);
        try {
            String algorithm = (String)eay2jce.get(encryptionAlg);
            byte[] key_bytes = new byte[((Integer)kSize.get(encryptionAlg)).intValue()];
            PEMData.EVP_BytesToKey("MD5", iv, password, (short)1, key_bytes, null);
            Cipher ciph = Cipher.getInstance(algorithm);
            ((FeedbackCipher)((Object)ciph)).setInitializationVector(iv);
            RawSecretKey key = new RawSecretKey(algorithm, key_bytes);
            ciph.initEncrypt(key);
            SSLDebug.debug(8, "Writing PEM Data plaintext", plainText);
            cipherText = ciph.crypt(plainText);
            SSLDebug.debug(8, "Writing PEM Data cipherText", cipherText);
        }
        catch (KeyException e) {
            throw new InternalError(e.toString());
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalError(e.toString());
        }
        String begin = "-----BEGIN " + type + "-----";
        out.write(begin);
        out.newLine();
        out.write("Proc-Type: 4,ENCRYPTED");
        out.newLine();
        String ivl = "DEK-Info: " + encryptionAlg + "," + Hex.toString(iv);
        out.write(ivl);
        out.newLine();
        out.newLine();
        out.flush();
        WrappedObject.writeObject(cipherText, type, out);
    }

    static void EVP_BytesToKey(String digest, byte[] salt, byte[] data, short count, byte[] key, byte[] iv) {
        byte[] md_buf = null;
        int key_offset = 0;
        int key_left = key.length;
        int iv_offset = 0;
        int iv_left = 0;
        if (iv != null) {
            iv_left = iv.length;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(digest);
            do {
                int tocpy;
                if (md_buf != null) {
                    md.update(md_buf);
                }
                md.update(data);
                if (salt != null) {
                    md.update(salt);
                }
                md_buf = md.digest();
                for (short i = 1; i < count; i = (short)(i + 1)) {
                    md.update(md_buf);
                    md_buf = md.digest();
                }
                int left = md_buf.length;
                int offset = 0;
                if (key_left != 0) {
                    tocpy = Util.min(key_left, left);
                    System.arraycopy(md_buf, 0, key, key_offset, tocpy);
                    left -= tocpy;
                    offset = tocpy;
                    key_left -= tocpy;
                    key_offset += tocpy;
                }
                if (left < 0 || iv_left <= 0) continue;
                tocpy = Util.min(iv_left, left);
                System.arraycopy(md_buf, offset, iv, iv_offset, tocpy);
                iv_left -= tocpy;
                iv_offset += tocpy;
            } while (key_left != 0 || iv_left != 0);
        }
        catch (Exception e) {
            throw new InternalError(e.toString());
        }
    }

    static {
        eay2jce = new Hashtable();
        eay2jce.put("DES-EDE3-CBC", "3DES/CBC/PKCS5Padding");
        eay2jce.put("DES-CBC", "DES/CBC/PKCS5Padding");
        kSize = new Hashtable();
        kSize.put("DES-EDE3-CBC", new Integer(24));
        kSize.put("DES-CBC", new Integer(8));
    }
}

