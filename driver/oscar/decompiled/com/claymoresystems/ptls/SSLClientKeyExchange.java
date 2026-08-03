/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.crypto.Blindable;
import com.claymoresystems.crypto.DHPrivateKey;
import com.claymoresystems.crypto.DHPublicKey;
import com.claymoresystems.crypto.PKCS1Pad;
import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLException;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLopaque;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import xjava.security.Cipher;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.CryptixRSAPublicKey;

class SSLClientKeyExchange
extends SSLPDU {
    SSLopaque client_data = new SSLopaque(-65535);

    SSLClientKeyExchange() {
    }

    public int encode(SSLConn conn, OutputStream s) throws IOException {
        switch (conn.hs.cipher_suite.getKeyExchangeAlg()) {
            case 1: {
                DHPublicKey dhpub = (DHPublicKey)conn.hs.peerEncryptionKey;
                DHPrivateKey dhpriv = DHPrivateKey.getInstance();
                dhpriv.initPrivateKey(dhpub.getg(), dhpub.getp(), conn.hs.rng);
                this.client_data.value = dhpriv.getYBytes();
                conn.hs.pre_master_secret = dhpriv.keyAgree(dhpub, true);
                return this.client_data.encode(conn, s);
            }
            case 2: {
                try {
                    conn.hs.pre_master_secret = new byte[48];
                    conn.hs.rng.nextBytes(conn.hs.pre_master_secret);
                    conn.hs.pre_master_secret[0] = 3;
                    conn.hs.pre_master_secret[1] = (byte)(conn.max_ssl_version & 0xFF);
                    Cipher ciph = Cipher.getInstance("RSA", "Cryptix");
                    if (conn.hs.peerEncryptionKey == null) {
                        conn.hs.peerEncryptionKey = conn.hs.peerSignatureKey;
                    }
                    ciph.initEncrypt(conn.hs.peerEncryptionKey);
                    byte[] pkcs1_padded_data = PKCS1Pad.pkcs1PadBuf(conn.hs.rng, conn.hs.pre_master_secret, conn.hs.peerEncryptionKey);
                    SSLDebug.debug(8, "RSA input", pkcs1_padded_data);
                    byte[] enc_data = ciph.crypt(pkcs1_padded_data);
                    this.client_data.value = enc_data;
                    SSLDebug.debug(8, "PreMasterSecret", conn.hs.pre_master_secret);
                    SSLDebug.debug(8, "EncryptedPreMasterSecret", enc_data);
                    if (conn.ssl_version >= 769) {
                        return this.client_data.encode(conn, s);
                    }
                    s.write(enc_data);
                    return enc_data.length;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    throw new InternalError(e.toString());
                }
            }
        }
        throw new InternalError("Inconsistent algorithm");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public int decode(SSLConn conn, InputStream s) throws IOException {
        switch (conn.hs.cipher_suite.getKeyExchangeAlg()) {
            case 1: {
                int rb = this.client_data.decode(conn, s);
                conn.hs.peerEncryptionKey = new DHPublicKey(new BigInteger(1, this.client_data.value));
                conn.hs.pre_master_secret = conn.hs.dhEphemeral.keyAgree((DHPublicKey)conn.hs.peerEncryptionKey, false);
                return rb;
            }
            case 2: {
                byte[] val;
                int rb;
                if (conn.ssl_version >= 769) {
                    rb = this.client_data.decode(conn, s);
                    val = this.client_data.value;
                } else {
                    byte[] in = new byte[512];
                    rb = s.read(in);
                    if (rb < 0) {
                        throw new SSLException("Short RSA key");
                    }
                    val = new byte[rb];
                    System.arraycopy(in, 0, val, 0, rb);
                }
                try {
                    Cipher ciph = Cipher.getInstance("RSABlind");
                    PrivateKey privk = null;
                    PublicKey pubk = null;
                    if (conn.hs.rsaEphemeral == null) {
                        privk = conn.ctx.getPrivateKey();
                        pubk = conn.ctx.getPublicKey();
                    } else {
                        privk = conn.hs.rsaEphemeral;
                        pubk = conn.hs.rsaEphemeralPublic;
                    }
                    ciph.initDecrypt(privk);
                    ((Blindable)((Object)ciph)).setBlindingInfo(conn.hs.rng, (CryptixRSAPublicKey)pubk);
                    byte[] decrypted = ciph.crypt(val);
                    conn.hs.pre_master_secret = PKCS1Pad.pkcs1UnpadBuf(decrypted, 1, (CryptixRSAPrivateKey)privk);
                    if (conn.hs.pre_master_secret.length != 48) {
                        throw new Exception("Bad PMS length");
                    }
                    SSLDebug.debug(8, "Checking client offered version against RSA block for rollback " + conn.hs.client_offered_version);
                    if (conn.hs.pre_master_secret[0] == (conn.hs.client_offered_version >> 8 & 0xFF) && conn.hs.pre_master_secret[1] == (conn.hs.client_offered_version & 0xFF)) return rb;
                    if (conn.hs.pre_master_secret[0] != 3 || conn.hs.pre_master_secret[1] != 0 || conn.hs.client_offered_version != 769 || conn.ssl_version != 768) throw new Exception("Bad PMS version number");
                    SSLDebug.debug(8, "Accepting rollback to SSLv3 from TLS since this is a common SSLv3/TLS bug");
                    return rb;
                }
                catch (Exception e) {
                    conn.hs.pre_master_secret = new byte[48];
                    SSLDebug.debug(8, "Bad padding. Randomizing PMS");
                    conn.ctx.rng.nextBytes(conn.hs.pre_master_secret);
                }
                return rb;
            }
            default: {
                throw new InternalError("Inconsistent algorithm");
            }
        }
    }
}

