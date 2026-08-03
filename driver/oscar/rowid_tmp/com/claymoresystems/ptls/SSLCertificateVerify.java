/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.crypto.Blindable;
import com.claymoresystems.ptls.SSLAlertX;
import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLHandshake;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLopaque;
import com.claymoresystems.ptls.SSLv3CertificateVerify;
import com.claymoresystems.ptls.TLSCertificateVerify;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import xjava.security.interfaces.CryptixRSAPublicKey;

class SSLCertificateVerify
extends SSLPDU {
    SSLopaque signature = new SSLopaque(-65535);
    byte[] toBeSigned;

    private String getCVAlg(String alg) {
        if (alg.equals("DSA")) {
            return "RawDSA";
        }
        if (alg.equals("RSA")) {
            return "RawRSA";
        }
        throw new InternalError("Bogus algorithm");
    }

    public SSLCertificateVerify(SSLConn conn, SSLHandshake hs, boolean mine) {
        switch (conn.ssl_version) {
            case 768: {
                this.toBeSigned = SSLv3CertificateVerify.computeToBeSigned(hs, mine);
                break;
            }
            case 769: {
                this.toBeSigned = TLSCertificateVerify.computeToBeSigned(hs, mine);
                break;
            }
            default: {
                throw new InternalError("Bogus version number");
            }
        }
    }

    public int encode(SSLConn conn, OutputStream s) throws IOException {
        try {
            PrivateKey pk = conn.ctx.getPrivateKey();
            String alg = this.getCVAlg(pk.getAlgorithm());
            Signature sig = Signature.getInstance(alg);
            sig.initSign(pk);
            if (alg.equals("RawRSA")) {
                ((Blindable)((Object)sig)).setBlindingInfo(conn.hs.rng, (CryptixRSAPublicKey)conn.ctx.getPublicKey());
            }
            SSLDebug.debug(8, "Certificate verify toBeSigned", this.toBeSigned);
            if (alg.equals("RawDSA")) {
                sig.setParameter("SecureRandom", conn.hs.rng);
                sig.update(this.toBeSigned, 16, 20);
            } else {
                sig.update(this.toBeSigned, 0, this.toBeSigned.length);
            }
            byte[] sig_bytes = sig.sign();
            SSLDebug.debug(8, "Certificate verify signature", sig_bytes);
            this.signature.value = sig_bytes;
            return this.signature.encode(conn, s);
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalError(e.toString());
        }
        catch (SignatureException e) {
            throw new InternalError(e.toString());
        }
        catch (InvalidKeyException e) {
            throw new InternalError(e.toString());
        }
    }

    public int decode(SSLConn conn, InputStream s) throws IOException {
        int rb = 0;
        try {
            PublicKey pk = conn.hs.peerSignatureKey;
            String alg = this.getCVAlg(pk.getAlgorithm());
            Signature sig = Signature.getInstance(alg);
            sig.initVerify(pk);
            rb = this.signature.decode(conn, s);
            SSLDebug.debug(8, "Certificate verify toBeSigned", this.toBeSigned);
            if (alg.equals("RawDSA")) {
                sig.update(this.toBeSigned, 16, 20);
            } else {
                sig.update(this.toBeSigned, 0, this.toBeSigned.length);
            }
            SSLDebug.debug(8, "Certificate verify signature", this.signature.value);
            if (!sig.verify(this.signature.value)) {
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
        return rb;
    }
}

