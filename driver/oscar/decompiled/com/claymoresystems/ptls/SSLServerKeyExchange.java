/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.crypto.Blindable;
import com.claymoresystems.crypto.DHPublicKey;
import com.claymoresystems.ptls.LoadProviders;
import com.claymoresystems.ptls.SSLAlertX;
import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLDHParams;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLRSAParams;
import com.claymoresystems.ptls.SSLopaque;
import cryptix.provider.rsa.RawRSAPublicKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.DSAPublicKey;
import xjava.security.interfaces.CryptixRSAPublicKey;

class SSLServerKeyExchange
extends SSLPDU {
    SSLDHParams dh_params;
    SSLRSAParams rsa_params;
    SSLopaque signature = new SSLopaque(-65535);
    SSLPDU par;
    int wb = 0;
    String algorithm;

    SSLServerKeyExchange() {
    }

    public int encode(SSLConn conn, OutputStream s) throws IOException {
        ByteArrayOutputStream kex_os = new ByteArrayOutputStream();
        switch (conn.hs.cipher_suite.getKeyExchangeAlg()) {
            case 1: {
                conn.hs.dhEphemeral = conn.ctx.getEphemeralDHPrivateKey(conn.policy.dhAlwaysEphemeralP());
                this.dh_params = new SSLDHParams(conn.hs.dhEphemeral);
                this.par = this.dh_params;
                break;
            }
            case 2: {
                conn.hs.rsaEphemeral = conn.ctx.getEphemeralRSAPrivateKey();
                conn.hs.rsaEphemeralPublic = conn.ctx.getEphemeralRSAPublicKey();
                this.rsa_params = new SSLRSAParams(conn.ctx.getEphemeralRSAPublicKey());
                this.par = this.rsa_params;
                break;
            }
            default: {
                throw new Error("Unknown key exchange algorithm");
            }
        }
        this.par.encode(conn, kex_os);
        byte[] kex_enc = kex_os.toByteArray();
        try {
            Signature sigChecker;
            PrivateKey pk = conn.ctx.getPrivateKey();
            String alg = conn.hs.cipher_suite.getSignatureAlgCV();
            if (alg.equals("RawDSA")) {
                sigChecker = Signature.getInstance(alg, LoadProviders.getDSAProvider());
                sigChecker.setParameter("SecureRandom", conn.hs.rng);
            } else if (alg.equals("RawRSA")) {
                sigChecker = Signature.getInstance(alg);
                ((Blindable)((Object)sigChecker)).setBlindingInfo(conn.hs.rng, (CryptixRSAPublicKey)conn.ctx.getPublicKey());
            } else {
                throw new Exception("Unknown key type");
            }
            sigChecker.initSign(pk);
            byte[] toBeSigned = this.getToBeSigned(conn, alg, kex_enc);
            sigChecker.update(toBeSigned);
            byte[] sig = sigChecker.sign();
            SSLDebug.debug(8, "Signed Data", kex_enc);
            SSLDebug.debug(8, "Signature Data", sig);
            this.signature.value = sig;
        }
        catch (Exception e) {
            throw new InternalError(e.toString());
        }
        this.wb = this.par.encode(conn, s);
        this.wb += this.signature.encode(conn, s);
        return this.wb;
    }

    public int decode(SSLConn conn, InputStream s) throws Error, IOException {
        int rb;
        PublicKey tmp_pk = null;
        PublicKey pk = conn.hs.peerSignatureKey;
        ByteArrayOutputStream kex_os = new ByteArrayOutputStream();
        if (!conn.hs.cipher_suite.allowServerKeyExchangeP(pk)) {
            conn.alert(SSLAlertX.TLS_ALERT_ILLEGAL_PARAMETER);
        }
        switch (conn.hs.cipher_suite.getKeyExchangeAlg()) {
            case 1: {
                this.dh_params = new SSLDHParams();
                rb = this.dh_params.decode(conn, s);
                this.dh_params.encode(conn, kex_os);
                tmp_pk = new DHPublicKey(new BigInteger(1, this.dh_params.DH_g.value), new BigInteger(1, this.dh_params.DH_p.value), new BigInteger(1, this.dh_params.DH_Ys.value));
                break;
            }
            case 2: {
                this.rsa_params = new SSLRSAParams();
                rb = this.rsa_params.decode(conn, s);
                this.rsa_params.encode(conn, kex_os);
                BigInteger mod = new BigInteger(1, this.rsa_params.RSA_modulus.value);
                BigInteger exp = new BigInteger(1, this.rsa_params.RSA_exponent.value);
                if (mod.bitLength() > 512) {
                    conn.alert(SSLAlertX.TLS_ALERT_ILLEGAL_PARAMETER);
                }
                tmp_pk = new RawRSAPublicKey(mod, exp);
                break;
            }
            default: {
                throw new Error("Unknown key exchange algorithm");
            }
        }
        int params_size = rb;
        rb += this.signature.decode(conn, s);
        byte[] kex_enc = kex_os.toByteArray();
        if (kex_enc.length != params_size) {
            throw new InternalError("Inconsistency in param size");
        }
        try {
            String alg = conn.hs.cipher_suite.getSignatureAlgCV();
            Signature sigChecker = Signature.getInstance(alg);
            this.checkSignatureKey(conn, pk, alg);
            sigChecker.initVerify(pk);
            byte[] toBeSigned = this.getToBeSigned(conn, alg, kex_enc);
            sigChecker.update(toBeSigned);
            SSLDebug.debug(8, "Signed Data", kex_enc);
            SSLDebug.debug(8, "Signature Data", this.signature.value);
            if (!sigChecker.verify(this.signature.value)) {
                conn.alert(SSLAlertX.TLS_ALERT_DECRYPT_ERROR);
            }
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalError(e.toString());
        }
        catch (InvalidKeyException e) {
            conn.alert(SSLAlertX.TLS_ALERT_DECRYPT_ERROR);
        }
        catch (SignatureException e) {
            conn.alert(SSLAlertX.TLS_ALERT_DECRYPT_ERROR);
        }
        conn.hs.peerEncryptionKey = tmp_pk;
        return rb;
    }

    private byte[] getToBeSigned(SSLConn conn, String alg, byte[] kex_enc) throws NoSuchAlgorithmException {
        byte[] toBeSigned;
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        sha.update(conn.hs.client_random);
        sha.update(conn.hs.server_random);
        sha.update(kex_enc);
        if (alg.equals("RawRSA")) {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(conn.hs.client_random);
            md5.update(conn.hs.server_random);
            md5.update(kex_enc);
            byte[] md5dig = md5.digest();
            byte[] shadig = sha.digest();
            toBeSigned = new byte[36];
            System.arraycopy(md5dig, 0, toBeSigned, 0, md5dig.length);
            System.arraycopy(shadig, 0, toBeSigned, 16, shadig.length);
        } else {
            toBeSigned = sha.digest();
        }
        return toBeSigned;
    }

    private void checkSignatureKey(SSLConn conn, PublicKey key, String alg) throws IOException {
        if (alg.equals("RawRSA")) {
            if (!(key instanceof CryptixRSAPublicKey)) {
                conn.alert(SSLAlertX.TLS_ALERT_ILLEGAL_PARAMETER);
            }
        } else if (alg.equals("RawDSA")) {
            if (!(key instanceof DSAPublicKey)) {
                conn.alert(SSLAlertX.TLS_ALERT_ILLEGAL_PARAMETER);
            }
        } else {
            throw new InternalError("Unknown Algorithm");
        }
    }
}

