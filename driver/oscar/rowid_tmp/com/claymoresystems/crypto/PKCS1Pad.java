/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.crypto;

import com.claymoresystems.ptls.SSLDebug;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import xjava.security.IllegalBlockSizeException;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.CryptixRSAPublicKey;

public class PKCS1Pad {
    public static final int ENCRYPT = 1;
    public static final int SIGN = 2;
    public static final int DECRYPT = 1;
    public static final int VERIFY = 2;

    public static byte[] pkcs1PadBuf(SecureRandom rnd, byte[] input, PublicKey key) {
        BigInteger modulus = ((CryptixRSAPublicKey)key).getModulus();
        return PKCS1Pad.pkcs1PadBuf(rnd, input, modulus, 1);
    }

    public static byte[] pkcs1PadBuf(byte[] input, PrivateKey key) {
        BigInteger modulus = ((CryptixRSAPrivateKey)key).getModulus();
        return PKCS1Pad.pkcs1PadBuf(input, modulus, 2);
    }

    public static byte[] pkcs1PadBuf(byte[] input, BigInteger modulus, int how) {
        SecureRandom rnd = null;
        if (how == 1) {
            rnd = new SecureRandom();
        }
        return PKCS1Pad.pkcs1PadBuf(rnd, input, modulus, how);
    }

    public static byte[] pkcs1PadBuf(SecureRandom rnd, byte[] input, BigInteger modulus, int how) {
        SSLDebug.debug(8, "PKCS1 pad input", input);
        int length = modulus.bitLength() / 8;
        length += modulus.bitLength() % 8 > 0 ? 1 : 0;
        int num_pad = how == 1 ? --length - (2 + input.length) : length - (3 + input.length);
        byte[] output = new byte[length];
        if (num_pad < 8) {
            throw new InternalError("Input too long");
        }
        int offset = 0;
        if (how == 1) {
            output[offset++] = 2;
            byte[] tmp = new byte[1];
            for (int ct = 0; ct < num_pad; ++ct) {
                do {
                    rnd.nextBytes(tmp);
                } while (tmp[0] == 0);
                output[offset++] = tmp[0];
            }
        } else {
            output[offset++] = 0;
            output[offset++] = 1;
            for (int i = 0; i < num_pad; ++i) {
                output[offset++] = -1;
            }
        }
        output[offset++] = 0;
        System.arraycopy(input, 0, output, offset, input.length);
        SSLDebug.debug(8, "PKCS1 padded output", output);
        return output;
    }

    public static byte[] pkcs1UnpadBuf(byte[] input, int how, CryptixRSAPrivateKey key) {
        return PKCS1Pad.pkcs1UnpadBuf(input, how, key.getModulus());
    }

    public static byte[] pkcs1UnpadBuf(byte[] input, int how, BigInteger modulus) {
        int pad_bytes = 0;
        SSLDebug.debug(8, "PKCS1 padded input", input);
        int length = modulus.bitLength() / 8;
        if ((length += modulus.bitLength() % 8 > 0 ? 1 : 0) - 1 != input.length) {
            SSLDebug.debug(8, "Encryption block wrong length");
            throw new IllegalBlockSizeException("Bad RSA padding: wrong length" + input.length);
        }
        if (how == 1 ? input[0] != 2 : input[0] != 1) {
            throw new IllegalBlockSizeException("Bad RSA padding");
        }
        for (int i = 1; i < input.length; ++i) {
            if (input[i] == 0) {
                if (pad_bytes < 8) {
                    SSLDebug.debug(8, "Bad RSA padding" + pad_bytes + "bytes");
                    throw new IllegalBlockSizeException("Bad RSA padding");
                }
                byte[] result = new byte[input.length - (i + 1)];
                System.arraycopy(input, i + 1, result, 0, result.length);
                SSLDebug.debug(8, "PKCS1 unpadded output", result);
                return result;
            }
            ++pad_bytes;
            if (how == 1 || input[i] == -1) continue;
            throw new IllegalBlockSizeException("Bad RSA padding");
        }
        throw new IllegalBlockSizeException("Bad RSA padding");
    }
}

