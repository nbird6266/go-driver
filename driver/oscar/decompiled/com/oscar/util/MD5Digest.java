/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import java.security.MessageDigest;

public class MD5Digest {
    private MD5Digest() {
    }

    public static byte[] encode(byte[] user, byte[] password, byte[] salt) {
        byte[] hex_digest = new byte[35];
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password);
            md.update(user);
            byte[] temp_digest = md.digest();
            MD5Digest.bytesToHex(temp_digest, hex_digest, 0);
            md.update(hex_digest, 0, 32);
            md.update(salt);
            byte[] pass_digest = md.digest();
            MD5Digest.bytesToHex(pass_digest, hex_digest, 3);
            hex_digest[0] = 109;
            hex_digest[1] = 100;
            hex_digest[2] = 53;
        }
        catch (Exception e) {
            // empty catch block
        }
        return hex_digest;
    }

    private static void bytesToHex(byte[] bytes, byte[] hex, int offset) {
        char[] lookup = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int pos = offset;
        for (int i = 0; i < 16; ++i) {
            int c = bytes[i] & 0xFF;
            int j = c >> 4;
            hex[pos++] = (byte)lookup[j];
            j = c & 0xF;
            hex[pos++] = (byte)lookup[j];
        }
    }
}

