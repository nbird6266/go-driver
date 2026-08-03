/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLAlertX;
import com.claymoresystems.ptls.SSLCertificateRequest;
import com.claymoresystems.ptls.SSLCertificateVerify;
import com.claymoresystems.ptls.SSLCipherSuite;
import com.claymoresystems.ptls.SSLClientHello;
import com.claymoresystems.ptls.SSLClientKeyExchange;
import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLHandshake;
import com.claymoresystems.ptls.SSLHandshakeHdr;
import com.claymoresystems.ptls.SSLServerHello;
import com.claymoresystems.ptls.SSLServerKeyExchange;
import com.claymoresystems.ptls.SSLSessionData;
import com.claymoresystems.ptls.SSLuint16;
import com.claymoresystems.ptls.SSLuint8;
import com.claymoresystems.ptls.SSLvector;
import com.claymoresystems.sslg.SSLPolicyInt;
import cryptix.util.core.ArrayUtil;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.util.Vector;

class SSLHandshakeClient
extends SSLHandshake {
    public final int SSL_HS_HANDSHAKE_START = 0;
    public final int SSL_HS_SENT_CLIENT_HELLO = 1;
    public final int SSL_HS_RECEIVED_SERVER_HELLO = 2;
    public final int SSL_HS_RECEIVED_CERTIFICATE = 3;
    public final int SSL_HS_RECEIVED_SERVER_KEY_EXCHANGE = 4;
    public final int SSL_HS_RECEIVED_CERTIFICATE_REQUEST = 5;
    public final int SSL_HS_RECEIVED_SERVER_HELLO_DONE = 6;
    boolean resume = false;
    SSLSessionData possibleResume;
    boolean clientAuth = false;

    public SSLHandshakeClient(SSLConn c) {
        super(c);
        this.client = true;
    }

    protected void filterCipherSuites(PrivateKey key, SSLPolicyInt policy) {
        this.cipher_suites = new Vector();
        short[] policySuites = this._conn.getPolicy().getCipherSuites();
        for (int i = 0; i < policySuites.length; ++i) {
            SSLCipherSuite cs = SSLCipherSuite.findCipherSuite(policySuites[i]);
            if (cs == null) {
                SSLDebug.debug(16, "Rejecting unrecognized cipher suite" + policySuites[i]);
                continue;
            }
            SSLDebug.debug(16, "Accepting cipher suite: " + cs.getName());
            this.cipher_suites.addElement(cs);
        }
    }

    public void handshakeContinue() throws IOException {
        InputStream is = null;
        int type = -1;
        if (this.state == 0) {
            this.sendClientHello();
            this.state = 1;
        }
        SSLHandshakeHdr hdr = new SSLHandshakeHdr();
        is = this.recvHandshakeMsg(this._conn, hdr);
        type = hdr.ct.value;
        SSLConn.debug(4, "Processing handshake message of type " + type);
        switch (type) {
            case 2: {
                this.stateAssert(1);
                this.recvServerHello(is);
                if (!this.resume) {
                    this.stateChange(2);
                    break;
                }
                this.stateChange(20);
                break;
            }
            case 11: {
                this.stateAssert(2);
                this.recvCertificate(is);
                this.stateChange(3);
                break;
            }
            case 12: {
                this.stateAssert(3);
                this.recvServerKeyExchange(is);
                this.stateChange(4);
                break;
            }
            case 13: {
                this.stateAssert(4, 3);
                this.recvCertificateRequest(is);
                this.stateChange(5);
                break;
            }
            case 14: {
                this.stateAssert(3, 4, 5);
                if (this.clientAuth) {
                    this.sendCertificate();
                }
                this.sendClientKeyExchange();
                if (this.clientAuth) {
                    this.sendCertificateVerify();
                }
                this.sendChangeCipherSpec();
                this.sendFinished();
                this.stateChange(20);
                break;
            }
            case 20: {
                this.stateAssert(21);
                this.recvFinished(is);
                if (this.resume) {
                    this.sendChangeCipherSpec();
                    this.sendFinished();
                }
                if (this.session_id.length != 0) {
                    this.storeSession(this.sessionLookupKey());
                }
                this.stateChange(255);
                break;
            }
            default: {
                this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
            }
        }
        int x = is.read();
        if (x != -1) {
            this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
        }
    }

    private void sendClientHello() throws IOException {
        SSLClientHello ch = new SSLClientHello();
        byte[] sid = new byte[]{};
        this.possibleResume = this.findSession(this.sessionLookupKey());
        ch.client_version.value = this._conn.ssl_version;
        this.makeRandomValue(this.client_random);
        ch.random.value = this.client_random;
        ch.session_id.value = this.possibleResume == null ? sid : this.possibleResume.getSessionID();
        Vector<SSLuint16> cs = new Vector<SSLuint16>();
        for (int i = 0; i < this.cipher_suites.size(); ++i) {
            SSLCipherSuite suite = (SSLCipherSuite)this.cipher_suites.elementAt(i);
            cs.addElement(new SSLuint16(suite.getValue()));
        }
        ch.cipher_suites = new SSLvector(-65535, cs);
        Vector<SSLuint8> cm = new Vector<SSLuint8>();
        cm.addElement(new SSLuint8(0));
        ch.compression_methods = new SSLvector(-255, cm);
        this.sendHandshakeMsg(this._conn, 1, ch);
        this._conn.sock_out.flush();
    }

    private void recvServerHello(InputStream is) throws IOException {
        SSLServerHello sh = new SSLServerHello();
        sh.decode(this._conn, is);
        if (sh.server_version.value < 768 || sh.server_version.value > this._conn.ssl_version) {
            this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
        }
        this._conn.ssl_version = sh.server_version.value;
        System.arraycopy(sh.random.value, 0, this.server_random, 0, 32);
        this.session_id = sh.session_id.value;
        SSLDebug.debug(2, "Received Session ID", this.session_id);
        if (this.session_id.length != 0 && this.possibleResume != null && ArrayUtil.areEqual(this.session_id, this.possibleResume.getSessionID())) {
            this.restoreSession(this.possibleResume);
            if (sh.cipher_suite.value != this.cipher_suite.getValue()) {
                this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
            }
            this.resume = true;
            this.computeNextCipherStates();
            SSLDebug.debug(4, "Resuming...");
            return;
        }
        this.cipher_suite = null;
        for (int i = 0; i < this.cipher_suites.size(); ++i) {
            SSLCipherSuite suite = (SSLCipherSuite)this.cipher_suites.elementAt(i);
            if (suite.getValue() != sh.cipher_suite.value) continue;
            this.cipher_suite = suite;
            break;
        }
        if (this.cipher_suite == null) {
            this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
        }
        SSLDebug.debug(64, "Server chose cipher" + this.cipher_suite.getName());
        if (sh.compression_method.value != 0) {
            this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
        }
    }

    private void recvServerKeyExchange(InputStream is) throws IOException {
        SSLServerKeyExchange sk = new SSLServerKeyExchange();
        sk.decode(this._conn, is);
    }

    private void recvCertificateRequest(InputStream is) throws IOException {
        SSLCertificateRequest cr = new SSLCertificateRequest();
        cr.decode(this._conn, is);
        this.clientAuth = true;
    }

    public void sendCertificate() throws IOException {
        Vector certs = this._conn.ctx.getCertificateChain();
        if (certs == null) {
            this.clientAuth = false;
            switch (this._conn.ssl_version) {
                case 768: {
                    this._conn.sendAlertNoException(SSLAlertX.SSL_ALERT_NO_CERTIFICATE, false);
                    return;
                }
                case 769: {
                    certs = new Vector();
                    break;
                }
                default: {
                    throw new InternalError("Inconsistent version");
                }
            }
        }
        this.sendCertificate(certs);
    }

    private void sendCertificateVerify() throws IOException {
        SSLCertificateVerify cv = new SSLCertificateVerify(this._conn, this, true);
        this.sendHandshakeMsg(this._conn, 15, cv);
    }

    private void sendClientKeyExchange() throws IOException {
        SSLClientKeyExchange ck = new SSLClientKeyExchange();
        this.sendHandshakeMsg(this._conn, 16, ck);
        this.computeMasterSecret();
        this.computeNextCipherStates();
    }

    private String sessionLookupKey() {
        String key = this._conn.s.remote_host + ":" + this._conn.s.remote_port;
        return key;
    }
}

