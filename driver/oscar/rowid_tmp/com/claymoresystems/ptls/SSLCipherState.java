/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLCipherSuite;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLHandshake;
import com.claymoresystems.ptls.SSLPRF;
import cryptix.provider.key.RawSecretKey;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import xjava.security.Cipher;
import xjava.security.FeedbackCipher;

class SSLCipherState {
    SSLCipherSuite cipher_suite;
    Cipher cipher;
    byte[] cipher_key;
    byte[] mac_key;
    byte[] iv;

    SSLCipherState() {
    }

    public static void computeSSLCipherState(SSLHandshake hs, SSLCipherState write, SSLCipherState read) throws NoSuchAlgorithmException, KeyException {
        int kb_size = 0;
        int digest_size = hs.cipher_suite.getDigestOutputLength();
        int key_size = hs.cipher_suite.getCipherEffectiveKeyLength();
        int block = hs.cipher_suite.blockCipherP() ? 8 : 0;
        int off = 0;
        write.iv = new byte[8];
        read.iv = new byte[8];
        write.cipher_suite = hs.cipher_suite;
        read.cipher_suite = hs.cipher_suite;
        SSLPRF prf = SSLPRF.getPRFInstance(hs._conn.ssl_version);
        kb_size += 2 * digest_size;
        kb_size += 2 * key_size;
        byte[] key_block = new byte[kb_size += 2 * block];
        prf.PRF(hs.master_secret, 2, hs.client_random, hs.server_random, key_block);
        SSLDebug.debug(8, "Key block", key_block);
        byte[] client_write_key = null;
        byte[] server_write_key = null;
        if (key_size > 0) {
            client_write_key = new byte[key_size];
            server_write_key = new byte[key_size];
        }
        write.mac_key = new byte[digest_size];
        read.mac_key = new byte[digest_size];
        if (hs.client) {
            System.arraycopy(key_block, off, write.mac_key, 0, digest_size);
            System.arraycopy(key_block, off += digest_size, read.mac_key, 0, digest_size);
            off += digest_size;
        } else {
            System.arraycopy(key_block, off, read.mac_key, 0, digest_size);
            System.arraycopy(key_block, off += digest_size, write.mac_key, 0, digest_size);
            off += digest_size;
        }
        if (key_size > 0) {
            System.arraycopy(key_block, off, client_write_key, 0, key_size);
            System.arraycopy(key_block, off += key_size, server_write_key, 0, key_size);
            off += key_size;
            if (block > 0 && !hs.cipher_suite.exportableP()) {
                if (hs.client) {
                    System.arraycopy(key_block, off, write.iv, 0, block);
                    System.arraycopy(key_block, off += block, read.iv, 0, block);
                } else {
                    System.arraycopy(key_block, off, read.iv, 0, block);
                    System.arraycopy(key_block, off += block, write.iv, 0, block);
                }
            }
            if (hs.cipher_suite.exportableP()) {
                byte[] old_client_write_key = client_write_key;
                byte[] old_server_write_key = server_write_key;
                client_write_key = new byte[hs.cipher_suite.getCipherKeyLength()];
                server_write_key = new byte[hs.cipher_suite.getCipherKeyLength()];
                prf.PRF(old_client_write_key, 3, hs.client_random, hs.server_random, client_write_key);
                prf.PRF(old_server_write_key, 4, hs.client_random, hs.server_random, server_write_key);
                if (block > 0) {
                    byte[] iv_block = new byte[2 * block];
                    if (hs.client) {
                        prf.PRF(new byte[0], 5, hs.client_random, hs.server_random, write.iv);
                        prf.PRF(new byte[0], 6, hs.client_random, hs.server_random, read.iv);
                    } else {
                        prf.PRF(new byte[0], 6, hs.client_random, hs.server_random, write.iv);
                        prf.PRF(new byte[0], 5, hs.client_random, hs.server_random, read.iv);
                    }
                }
            }
            if (hs.client) {
                write.cipher_key = client_write_key;
                read.cipher_key = server_write_key;
            } else {
                write.cipher_key = server_write_key;
                read.cipher_key = client_write_key;
            }
            SSLDebug.debug(8, "Write key", write.cipher_key);
            SSLDebug.debug(8, "Read key", read.cipher_key);
        }
        SSLDebug.debug(8, "Write IV", write.iv);
        SSLDebug.debug(8, "Read IV", read.iv);
        SSLDebug.debug(8, "Write MAC key", write.mac_key);
        SSLDebug.debug(8, "Read MAC key", read.mac_key);
        if (key_size > 0) {
            read.cipher = Cipher.getInstance(hs.cipher_suite.getCipherAlg());
            write.cipher = Cipher.getInstance(hs.cipher_suite.getCipherAlg());
            RawSecretKey rwrite_key = new RawSecretKey(hs.cipher_suite.getCipherAlg(), write.cipher_key);
            RawSecretKey rread_key = new RawSecretKey(hs.cipher_suite.getCipherAlg(), read.cipher_key);
            if (block > 0) {
                ((FeedbackCipher)((Object)write.cipher)).setInitializationVector(write.iv);
                ((FeedbackCipher)((Object)read.cipher)).setInitializationVector(read.iv);
            }
            write.cipher.initEncrypt(rwrite_key);
            read.cipher.initDecrypt(rread_key);
        }
    }
}

