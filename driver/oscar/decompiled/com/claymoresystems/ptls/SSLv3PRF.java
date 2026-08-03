/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLPRF;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class SSLv3PRF
extends SSLPRF {
    MessageDigest md5;
    MessageDigest sha;

    public SSLv3PRF() {
        try {
            this.md5 = MessageDigest.getInstance("MD5");
            this.sha = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            throw new Error("Internal inconsistency");
        }
    }

    public void PRF(byte[] secret, int usage, byte[] client_random, byte[] server_random, byte[] out) {
        switch (usage) {
            case 3: 
            case 4: 
            case 5: 
            case 6: {
                this.PRFHash(secret, usage, client_random, server_random, out);
                break;
            }
            default: {
                this.PRFPRF(secret, usage, client_random, server_random, out);
            }
        }
    }

    public void PRFPRF(byte[] secret, int usage, byte[] client_random, byte[] server_random, byte[] out) {
        int off = 0;
        byte[] buf = new byte[20];
        int i = 0;
        SSLDebug.debug(8, "Secret", secret);
        SSLDebug.debug(8, "Client random", client_random);
        SSLDebug.debug(8, "Server random", server_random);
        for (off = 0; off < out.length; off += 16) {
            ++i;
            for (int j = 0; j < i; ++j) {
                buf[j] = (byte)(64 + i);
            }
            SSLDebug.debug(8, "BUF", buf);
            this.sha.update(buf, 0, i);
            this.sha.update(secret);
            switch (usage) {
                case 1: {
                    this.sha.update(client_random);
                    this.sha.update(server_random);
                    break;
                }
                case 2: {
                    this.sha.update(server_random);
                    this.sha.update(client_random);
                    break;
                }
                default: {
                    throw new InternalError("Bad usage");
                }
            }
            byte[] sha_out = this.sha.digest();
            SSLDebug.debug(8, "SHA out", sha_out);
            this.md5.update(secret);
            this.md5.update(sha_out);
            byte[] md5_out = this.md5.digest();
            SSLDebug.debug(8, "MD5 out", md5_out);
            System.arraycopy(md5_out, 0, out, off, 16 > out.length - off ? out.length - off : 16);
        }
    }

    public void PRFHash(byte[] secret, int usage, byte[] client_random, byte[] server_random, byte[] out) {
        this.md5.update(secret);
        switch (usage) {
            case 3: 
            case 5: {
                this.md5.update(client_random);
                this.md5.update(server_random);
                break;
            }
            default: {
                this.md5.update(server_random);
                this.md5.update(client_random);
            }
        }
        byte[] md5out = this.md5.digest();
        SSLDebug.debug(8, "PRFHash out", md5out);
        System.arraycopy(md5out, 0, out, 0, out.length);
    }
}

