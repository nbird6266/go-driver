/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLPRF;
import com.claymoresystems.util.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import xjava.security.InvalidParameterTypeException;
import xjava.security.NoSuchParameterException;
import xjava.security.Parameterized;

class TLSPRF
extends SSLPRF {
    MessageDigest md5;
    MessageDigest sha;
    private static byte[][] labels = new byte[][]{new byte[0], {109, 97, 115, 116, 101, 114, 32, 115, 101, 99, 114, 101, 116}, {107, 101, 121, 32, 101, 120, 112, 97, 110, 115, 105, 111, 110}, {99, 108, 105, 101, 110, 116, 32, 119, 114, 105, 116, 101, 32, 107, 101, 121}, {115, 101, 114, 118, 101, 114, 32, 119, 114, 105, 116, 101, 32, 107, 101, 121}, {73, 86, 32, 98, 108, 111, 99, 107}, {73, 86, 32, 98, 108, 111, 99, 107}, {99, 108, 105, 101, 110, 116, 32, 102, 105, 110, 105, 115, 104, 101, 100}, {115, 101, 114, 118, 101, 114, 32, 102, 105, 110, 105, 115, 104, 101, 100}};
    public static final int TLS_PRF_CLIENT_FINISHED = 7;
    public static final int TLS_PRF_SERVER_FINISHED = 8;

    public TLSPRF() {
        try {
            this.md5 = MessageDigest.getInstance("HMAC-MD5");
            this.sha = MessageDigest.getInstance("HMAC-SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            throw new Error("Internal inconsistency");
        }
    }

    void PRF(byte[] secret, int usage, byte[] client_random, byte[] server_random, byte[] out) {
        int length = out.length;
        byte[] md5_out = null;
        byte[] sha_out = null;
        SSLDebug.debug(8, "PRF in: secret", secret);
        SSLDebug.debug(8, "PRF in: client_random", client_random);
        SSLDebug.debug(8, "PRF in: server_random", server_random);
        int slen = secret.length / 2 + secret.length % 2;
        byte[] S1 = new byte[slen];
        byte[] S2 = new byte[slen];
        System.arraycopy(secret, 0, S1, 0, slen);
        System.arraycopy(secret, secret.length - slen, S2, 0, slen);
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            os.write(labels[usage]);
            switch (usage) {
                case 1: 
                case 3: 
                case 4: 
                case 7: 
                case 8: {
                    os.write(client_random);
                    os.write(server_random);
                    break;
                }
                case 2: {
                    os.write(server_random);
                    os.write(client_random);
                    break;
                }
                case 6: {
                    length *= 2;
                }
                case 5: {
                    os.write(client_random);
                    os.write(server_random);
                    break;
                }
                default: {
                    throw new Error("Unknown PRF usage");
                }
            }
            byte[] seed = os.toByteArray();
            md5_out = this.P_hash(S1, seed, this.md5, length);
            sha_out = this.P_hash(S2, seed, this.sha, length);
        }
        catch (IOException e) {
            throw new Error("Internal Error");
        }
        if (usage == 6) {
            System.arraycopy(md5_out, out.length, md5_out, 0, out.length);
            System.arraycopy(sha_out, out.length, sha_out, 0, out.length);
        }
        for (int i = 0; i < out.length; ++i) {
            out[i] = (byte)(md5_out[i] ^ sha_out[i]);
        }
        SSLDebug.debug(8, "PRF out", out);
    }

    private byte[] P_hash(byte[] secret, byte[] seed, MessageDigest md, int required) {
        byte[] out = new byte[required];
        int offset = 0;
        byte[] A = seed;
        while (required > 0) {
            this.setKey(secret, md);
            md.update(A);
            A = md.digest();
            this.setKey(secret, md);
            md.update(A);
            md.update(seed);
            byte[] tmp = md.digest();
            int tocpy = Util.min(required, tmp.length);
            System.arraycopy(tmp, 0, out, offset, tocpy);
            offset += tocpy;
            required -= tocpy;
        }
        return out;
    }

    private void setKey(byte[] secret, MessageDigest md) {
        try {
            ((Parameterized)((Object)md)).setParameter("key", secret);
        }
        catch (InvalidParameterTypeException e) {
            throw new InternalError(e.toString());
        }
        catch (NoSuchParameterException e) {
            throw new InternalError(e.toString());
        }
    }
}

