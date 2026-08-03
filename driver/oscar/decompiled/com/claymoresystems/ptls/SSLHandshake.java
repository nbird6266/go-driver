/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.cert.CertContext;
import com.claymoresystems.cert.CertificateDecodeException;
import com.claymoresystems.cert.CertificateVerifyException;
import com.claymoresystems.cert.X509Cert;
import com.claymoresystems.crypto.DHPrivateKey;
import com.claymoresystems.ptls.SSLAlertX;
import com.claymoresystems.ptls.SSLCertificate;
import com.claymoresystems.ptls.SSLCipherState;
import com.claymoresystems.ptls.SSLCipherSuite;
import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLFinished;
import com.claymoresystems.ptls.SSLHandshakeHashes;
import com.claymoresystems.ptls.SSLHandshakeHdr;
import com.claymoresystems.ptls.SSLOutputStream;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLPRF;
import com.claymoresystems.ptls.SSLRecord;
import com.claymoresystems.ptls.SSLSessionData;
import com.claymoresystems.ptls.SSLopaque;
import com.claymoresystems.sslg.SSLPolicyInt;
import com.claymoresystems.util.Util;
import cryptix.util.core.ArrayUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Vector;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.CryptixRSAPublicKey;

abstract class SSLHandshake {
    public static final int SSL_HT_HELLO_REQUEST = 0;
    public static final int SSL_HT_CLIENT_HELLO = 1;
    public static final int SSL_HT_SERVER_HELLO = 2;
    public static final int SSL_HT_CERTIFICATE = 11;
    public static final int SSL_HT_SERVER_KEY_EXCHANGE = 12;
    public static final int SSL_HT_CERTIFICATE_REQUEST = 13;
    public static final int SSL_HT_SERVER_HELLO_DONE = 14;
    public static final int SSL_HT_CERTIFICATE_VERIFY = 15;
    public static final int SSL_HT_CLIENT_KEY_EXCHANGE = 16;
    public static final int SSL_HT_FINISHED = 20;
    public static final int SSL_HT_V2_CLIENT_HELLO = 255;
    public final int SSL_HS_WAIT_FOR_CHANGE_CIPHER_SPECS = 20;
    public final int SSL_HS_WAIT_FOR_FINISHED = 21;
    public static final int SSL_HANDSHAKE_FINISHED = 255;
    public static final int SSL_V3_VERSION = 768;
    public static final int TLS_V1_VERSION = 769;
    public static final int MASTER_SECRET_SIZE = 48;
    public static byte[] pad_1 = new byte[]{54};
    public static byte[] pad_2 = new byte[]{92};
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    int state;
    SSLConn _conn;
    byte[] session_id;
    boolean client;
    CertContext cert_ctx;
    Vector cipher_suites;
    SecureRandom rng;
    byte[] client_random = new byte[32];
    byte[] server_random = new byte[32];
    SSLHandshakeHashes hashes;
    SSLHandshakeHashes save_hashes;
    SSLCipherSuite cipher_suite;
    byte[] pre_master_secret;
    byte[] master_secret;
    PublicKey peerSignatureKey;
    PublicKey peerEncryptionKey;
    DHPrivateKey dhEphemeral;
    CryptixRSAPrivateKey rsaEphemeral = null;
    CryptixRSAPublicKey rsaEphemeralPublic = null;
    int client_offered_version = 0;

    public SSLHandshake(SSLConn conn) {
        this._conn = conn;
        this.hashes = new SSLHandshakeHashes();
        this.cert_ctx = new CertContext(conn.ctx.getRootList());
        this.rng = new SecureRandom(conn.ctx.getSeedBytes());
        this.filterCipherSuites(conn.ctx.getPrivateKey(), conn.getPolicy());
    }

    public void handshake() throws IOException {
        while (this.state != 255) {
            this.handshakeContinue();
        }
        this._conn.session_id = this.session_id.length != 0 ? this.session_id : null;
        this._conn.sock_out_external = new SSLOutputStream(this._conn);
        SSLDebug.debug(4, "Handshake completed");
    }

    public abstract void handshakeContinue() throws IOException;

    public void sendHandshakeMsg(SSLConn conn, int msgtype, SSLPDU pdu) throws IOException {
        this.sendHandshakeMsg(conn, msgtype, pdu, true);
    }

    public void sendHandshakeMsg(SSLConn conn, int msgtype, SSLPDU pdu, boolean digest) throws IOException, Error {
        pdu.encode(conn, this.os);
        ByteArrayOutputStream nos = new ByteArrayOutputStream(this.os.size() + 10);
        SSLHandshakeHdr hdr = new SSLHandshakeHdr(msgtype, this.os.size());
        hdr.encode(conn, nos);
        this.os.writeTo(nos);
        this.os.reset();
        byte[] buf = nos.toByteArray();
        if (digest) {
            this.hashes.update(buf);
        }
        SSLRecord rec = new SSLRecord(conn, 22, buf);
        rec.send(conn);
    }

    public InputStream recvHandshakeMsg(SSLConn conn, SSLHandshakeHdr hdr) throws IOException {
        hdr.decode(conn, conn.sock_in_hp);
        switch (hdr.ct.value) {
            case 15: 
            case 20: {
                try {
                    this.save_hashes = (SSLHandshakeHashes)this.hashes.clone();
                    break;
                }
                catch (CloneNotSupportedException e) {
                    throw new Error("Internal error");
                }
            }
        }
        ByteArrayOutputStream tmp_os = new ByteArrayOutputStream();
        hdr.encode(conn, tmp_os);
        byte[] tmp = tmp_os.toByteArray();
        this.hashes.update(tmp);
        byte[] buf = new byte[hdr.length.value];
        int br = 0;
        while (br < buf.length) {
            br = conn.sock_in_hp.read(buf, br, buf.length - br);
        }
        this.hashes.update(buf);
        return new ByteArrayInputStream(buf);
    }

    public boolean finishedP() {
        return this.state == 255;
    }

    public void stateChange(int st) {
        this.state = st;
        SSLDebug.debug(4, "New handshake state " + st);
    }

    public void stateAssert(int st) throws IOException {
        if (this.state == st) {
            return;
        }
        this._conn.alert(SSLAlertX.TLS_ALERT_UNEXPECTED_MESSAGE);
    }

    public void stateAssert(int st1, int st2) throws IOException {
        if (this.state == st1) {
            return;
        }
        if (this.state == st2) {
            return;
        }
        this._conn.alert(SSLAlertX.TLS_ALERT_UNEXPECTED_MESSAGE);
    }

    public void stateAssert(int st1, int st2, int st3) throws IOException {
        if (this.state == st1) {
            return;
        }
        if (this.state == st2) {
            return;
        }
        if (this.state == st3) {
            return;
        }
        this._conn.alert(SSLAlertX.TLS_ALERT_UNEXPECTED_MESSAGE);
    }

    public void sendCertificate(Vector certs) throws IOException {
        SSLCertificate cert_list = new SSLCertificate();
        for (int i = 1; i <= certs.size(); ++i) {
            SSLopaque cert = new SSLopaque(-16777215);
            cert.value = (byte[])certs.elementAt(i - 1);
            cert_list.certificate_list.value.addElement(cert);
        }
        this.sendHandshakeMsg(this._conn, 11, cert_list);
    }

    public void recvCertificate(InputStream is) throws IOException {
        PublicKey pk;
        SSLCertificate cert_list = new SSLCertificate();
        Vector<X509Cert> certs = new Vector<X509Cert>();
        Vector verified_certs = null;
        cert_list.decode(this._conn, is);
        if (cert_list.certificate_list.value.size() == 0) {
            this._conn.alert(SSLAlertX.TLS_ALERT_ILLEGAL_PARAMETER);
        }
        for (int i = 1; i <= cert_list.certificate_list.value.size(); ++i) {
            SSLopaque op = (SSLopaque)cert_list.certificate_list.value.elementAt(cert_list.certificate_list.value.size() - i);
            byte[] cert = op.value;
            certs.addElement(new X509Cert(cert));
        }
        try {
            verified_certs = X509Cert.verifyCertChain(this.cert_ctx, certs, this._conn.getPolicy().getCertVerifyPolicy());
        }
        catch (CertificateDecodeException e) {
            this._conn.alert(SSLAlertX.TLS_ALERT_BAD_CERTIFICATE);
        }
        catch (CertificateVerifyException e) {
            if (SSLDebug.getDebug(32)) {
                e.printStackTrace();
            }
            this._conn.alert(SSLAlertX.TLS_ALERT_BAD_CERTIFICATE);
        }
        if (verified_certs == null && !this._conn.getPolicy().acceptUnverifiableCertificatesP()) {
            this._conn.alert(SSLAlertX.TLS_ALERT_UNKNOWN_CA);
        }
        X509Cert peerCert = (X509Cert)certs.elementAt(certs.size() - 1);
        this.peerSignatureKey = pk = peerCert.getPublicKey();
        this._conn.peerCertificateChain = verified_certs;
    }

    public void computeMasterSecret() {
        SSLPRF prf = SSLPRF.getPRFInstance(this._conn.ssl_version);
        SSLDebug.debug(8, "Pre master secret", this.pre_master_secret);
        this.master_secret = new byte[48];
        prf.PRF(this.pre_master_secret, 1, this.client_random, this.server_random, this.master_secret);
        SSLDebug.debug(8, "Master secret", this.master_secret);
    }

    public void computeNextCipherStates() {
        this._conn.next_write_cipher_state = new SSLCipherState();
        this._conn.next_read_cipher_state = new SSLCipherState();
        try {
            SSLCipherState.computeSSLCipherState(this, this._conn.next_write_cipher_state, this._conn.next_read_cipher_state);
        }
        catch (NoSuchAlgorithmException e) {
            throw new Error(e.toString());
        }
        catch (KeyException e) {
            e.printStackTrace();
            throw new Error(e.toString());
        }
    }

    public void sendChangeCipherSpec() throws IOException {
        byte[] css = new byte[]{1};
        SSLRecord rec = new SSLRecord(this._conn, 20, css);
        rec.send(this._conn);
        this._conn.write_cipher_state = this._conn.next_write_cipher_state;
        this._conn.write_sequence_num = 0L;
    }

    public void recvFinished(InputStream is) throws IOException {
        SSLFinished finished = new SSLFinished(this._conn, this, false);
        finished.decode(this._conn, is);
    }

    public void sendFinished() throws IOException {
        SSLFinished finished = new SSLFinished(this._conn, this, true);
        this.sendHandshakeMsg(this._conn, 20, finished);
        this._conn.sock_out.flush();
    }

    public void recvChangeCipherSpecs(byte[] data) throws IOException {
        byte[] ccsTmpl = new byte[]{1};
        this.stateAssert(20);
        if (!ArrayUtil.areEqual(ccsTmpl, data)) {
            this._conn.alert(SSLAlertX.TLS_ALERT_ILLEGAL_PARAMETER);
        }
        this._conn.read_cipher_state = this._conn.next_read_cipher_state;
        this._conn.read_sequence_num = 0L;
        this.stateChange(21);
    }

    protected void storeSession(String key) {
        SSLDebug.debug(64, "Storing session ", this.session_id);
        SSLSessionData sd = new SSLSessionData(this, key);
        this._conn.ctx.storeSession(key, sd);
    }

    protected SSLSessionData findSession(String key) {
        SSLDebug.debug(4, "Trying to recover session using key" + key);
        SSLSessionData d = this._conn.ctx.findSession(key);
        if (d != null && d.getExpiryTime() < System.currentTimeMillis()) {
            this._conn.ctx.destroySession(d.getLookupKey());
            return null;
        }
        return d;
    }

    protected void restoreSession(SSLSessionData sess) {
        sess.restoreSession(this);
    }

    protected void filterCipherSuites(PrivateKey key, SSLPolicyInt policy) {
        String alg = key.getAlgorithm();
        this.cipher_suites = new Vector();
        short[] policySuites = this._conn.getPolicy().getCipherSuites();
        for (int i = 0; i < policySuites.length; ++i) {
            SSLCipherSuite cs = SSLCipherSuite.findCipherSuite(policySuites[i]);
            if (cs == null) {
                SSLDebug.debug(16, "Rejecting unrecognized cipher suite" + policySuites[i]);
                continue;
            }
            if (!cs.getSignatureAlgBase().equals(alg)) {
                SSLDebug.debug(16, "Rejecting cipher suite: " + cs.getName() + " -- incompatible with signature algorithm " + alg);
                continue;
            }
            SSLDebug.debug(16, "Accepting cipher suite: " + cs.getName());
            this.cipher_suites.addElement(cs);
        }
    }

    protected void makeRandomValue(byte[] val) {
        if (val.length != 32) {
            throw new InternalError("Incorrect random value length");
        }
        this.rng.nextBytes(val);
        long t = System.currentTimeMillis();
        byte[] tb = Util.toBytes(t /= 1000L, 4);
        System.arraycopy(tb, 0, val, 0, 4);
    }
}

