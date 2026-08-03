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
import com.claymoresystems.ptls.SSLHelloRequest;
import com.claymoresystems.ptls.SSLServerHello;
import com.claymoresystems.ptls.SSLServerHelloDone;
import com.claymoresystems.ptls.SSLServerKeyExchange;
import com.claymoresystems.ptls.SSLSessionData;
import com.claymoresystems.ptls.SSLuint16;
import com.claymoresystems.ptls.SSLuint24;
import com.claymoresystems.ptls.SSLuintX;
import com.claymoresystems.ptls.SSLv2ClientHello;
import com.claymoresystems.sslg.SSLPolicyInt;
import com.claymoresystems.util.Util;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.util.Hashtable;
import java.util.Vector;

class SSLHandshakeServer
extends SSLHandshake {
    public final int SSL_HS_WAIT_FOR_CLIENT_HELLO = 1;
    public final int SSL_HS_WAIT_FOR_CERTIFICATE = 2;
    public final int SSL_HS_WAIT_FOR_CLIENT_KEY_EXCHANGE = 3;
    public final int SSL_HS_WAIT_FOR_CERTIFICATE_VERIFY = 4;
    public final int SSL_HS_SEND_HELLO_REQUEST = 5;
    private static final int SSL2_CK_RC4_128_WITH_MD5 = 65664;
    private static final int SSL2_CK_RC4_128_EXPORT40_WITH_MD5 = 131200;
    private static final int SSL2_CK_RC2_128_CBC_WITH_MD5 = 65664;
    private static final int SSL2_CK_RC2_128_CBC_EXPORT40_WITH_MD5 = 131200;
    private static final int SSL2_CK_IDEA_128_CBC_WITH_MD5 = 327808;
    private static final int SSL2_CK_DES_64_CBC_WITH_MD5 = 393344;
    private static final int SSL2_CK_DES_192_EDE3_CBC_WITH_MD5 = 393344;
    boolean resume = false;
    boolean clientAuth = false;
    SSLSessionData possibleResume = null;
    Vector offered_cipher_suites;
    Vector offered_compression_methods;
    private static Hashtable _v2_v3_cipher_suite_map = new Hashtable();

    public SSLHandshakeServer(SSLConn c) {
        super(c);
        this.stateChange(this._conn.write_cipher_state == null ? 1 : 5);
        SSLDebug.debug(64, "Starting server handshake (connection " + c + ") in state " + this.state + " write cipher state " + this._conn.write_cipher_state);
        this.client = false;
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

    public void handshakeContinue() throws IOException {
        int x;
        InputStream is = null;
        int type = -1;
        boolean v2_hello = false;
        SSLHandshakeHdr hdr = new SSLHandshakeHdr();
        if (this.state == 5) {
            this.sendHelloRequest();
            this.stateChange(1);
        }
        if (this._conn.read_cipher_state == null && this.state == 1) {
            int b1 = this._conn.sock_in.read();
            SSLDebug.debug(64, "Testing for SSLv2 handshake. First byte off wire is: " + b1);
            this._conn.sock_in.unread(b1);
            if (b1 != 22) {
                if (this._conn.read_cipher_state != null) {
                    this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
                }
                v2_hello = true;
                type = 255;
            }
        }
        if (!v2_hello) {
            is = this.recvHandshakeMsg(this._conn, hdr);
            type = hdr.ct.value;
            SSLConn.debug(4, "Processing handshake message of type " + type);
        }
        switch (type) {
            case 255: {
                this.stateAssert(1);
                this.recvSSLv2ClientHello(this._conn.sock_in);
                this.resume = false;
                this.sendServerPhase1();
                this._conn.sock_out.flush();
                break;
            }
            case 1: {
                this.stateAssert(1);
                this.recvSSLv3ClientHello(is);
                this.resume = false;
                if (this.possibleResume != null) {
                    if (this._conn.getPolicy().requireClientAuthP() && this.possibleResume.getPeerCertificateChain() == null) {
                        this.resume = false;
                    } else {
                        this.restoreSession(this.possibleResume);
                        this.resume = true;
                    }
                }
                this.sendServerPhase1();
                this._conn.sock_out.flush();
                break;
            }
            case 11: {
                this.stateAssert(2);
                this.recvCertificate(is);
                this.stateChange(3);
                break;
            }
            case 16: {
                this.stateAssert(3);
                this.recvClientKeyExchange(is);
                this.stateChange(this.clientAuth ? 4 : 20);
                break;
            }
            case 15: {
                this.stateAssert(4);
                this.recvCertificateVerify(is);
                this.stateChange(20);
                break;
            }
            case 20: {
                this.stateAssert(21);
                this.recvFinished(is);
                if (!this.resume) {
                    this.sendChangeCipherSpec();
                    this.sendFinished();
                }
                if (this.session_id.length != 0) {
                    this.storeSession(Util.toHex(this.session_id));
                }
                this.stateChange(255);
                break;
            }
            default: {
                this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
            }
        }
        if (type != 1 && type != 255 && (x = is.read()) != -1) {
            this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
        }
    }

    public void sendCertificate() throws IOException {
        Vector certs = this._conn.ctx.getCertificateChain();
        if (certs == null) {
            this._conn.sendAlertNoException(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE, true);
            throw new IOException("Certificate needed but no certificate available");
        }
        this.sendCertificate(certs);
    }

    public void sendServerPhase1() throws IOException {
        if (!this.resume) {
            this.session_id = new byte[32];
            this.rng.nextBytes(this.session_id);
            SSLDebug.debug(64, "Session ID", this.session_id);
            this.selectCipherSuite();
        }
        this.sendServerHello();
        if (this.resume) {
            this.computeNextCipherStates();
            this.sendChangeCipherSpec();
            this.sendFinished();
            this.stateChange(20);
        } else {
            this.sendCertificate();
            this.sendServerKeyExchange();
            if (this._conn.getPolicy().requireClientAuthP()) {
                this.clientAuth = true;
                this.sendCertificateRequest();
                this.stateChange(2);
            } else {
                this.stateChange(3);
            }
            this.sendServerHelloDone();
        }
    }

    public void recvSSLv2ClientHello(InputStream is) throws IOException {
        SSLv2ClientHello ch = new SSLv2ClientHello();
        ch.decode(this._conn, is);
        if (ch.client_version.value < 768) {
            this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
        }
        this._conn.ssl_version = Util.min(ch.client_version.value, this._conn.ssl_version);
        this.client_offered_version = ch.client_version.value;
        if (ch.session_id.length != 0) {
            this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
        }
        Vector<SSLuint16> v = new Vector<SSLuint16>();
        for (int i = 0; i < ch.cipher_specs.value.size(); ++i) {
            SSLuint24 cs = (SSLuint24)ch.cipher_specs.value.elementAt(i);
            if ((cs.value & 0xFF0000) == 0) {
                v.addElement(new SSLuint16(cs.value));
                continue;
            }
            Integer x = new Integer(cs.value);
            Integer y = (Integer)_v2_v3_cipher_suite_map.get(x);
            if (y == null) continue;
            v.addElement(new SSLuint16(y));
        }
        this.offered_cipher_suites = v;
        this.offered_compression_methods = new Vector();
        this.offered_compression_methods.addElement(new SSLuint16(0));
        this.client_random = new byte[32];
        if (ch.challenge.length > 32) {
            this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
        }
        if (ch.challenge.length < 16) {
            this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
        }
        int offset = 32 - ch.challenge.length;
        System.arraycopy(ch.challenge.value, 0, this.client_random, offset, ch.challenge.length);
        this.hashes.update(ch.message_value);
    }

    public void recvSSLv3ClientHello(InputStream is) throws IOException {
        SSLClientHello ch = new SSLClientHello();
        ch.decode(this._conn, is);
        if (ch.client_version.value < 768) {
            this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
        }
        this._conn.ssl_version = Util.min(ch.client_version.value, this._conn.ssl_version);
        this.client_offered_version = ch.client_version.value;
        this.client_random = ch.random.value;
        if (ch.session_id.value.length != 0) {
            this.possibleResume = this.findSession(Util.toHex(ch.session_id.value));
        }
        this.offered_cipher_suites = ch.cipher_suites.value;
        this.offered_compression_methods = ch.compression_methods.value;
    }

    public void selectCipherSuite() throws IOException {
        SSLDebug.debug(64, "Client offered " + this.offered_cipher_suites.size() + "ciphersuites");
        for (int i = 0; i < this.cipher_suites.size(); ++i) {
            SSLCipherSuite oursuite = (SSLCipherSuite)this.cipher_suites.elementAt(i);
            SSLDebug.debug(64, "Seeing if client supports our ciphersuite " + oursuite.getValue());
            for (int j = 0; j < this.offered_cipher_suites.size(); ++j) {
                if (oursuite.getValue() != ((SSLuintX)this.offered_cipher_suites.elementAt((int)j)).value) continue;
                this.cipher_suite = oursuite;
                SSLDebug.debug(64, "Choosing cipher" + this.cipher_suite.getName());
                return;
            }
        }
        this._conn.alert(SSLAlertX.TLS_ALERT_HANDSHAKE_FAILURE);
    }

    public void sendServerHello() throws IOException {
        SSLServerHello sh = new SSLServerHello();
        sh.server_version.value = this._conn.ssl_version;
        this.makeRandomValue(this.server_random);
        sh.random.value = this.server_random;
        sh.session_id.value = this.session_id;
        sh.cipher_suite.value = this.cipher_suite.getValue();
        sh.compression_method.value = 0;
        this.sendHandshakeMsg(this._conn, 2, sh);
    }

    public void recvClientKeyExchange(InputStream is) throws IOException {
        SSLClientKeyExchange ck = new SSLClientKeyExchange();
        ck.decode(this._conn, is);
        this.computeMasterSecret();
        this.computeNextCipherStates();
    }

    public void recvCertificateVerify(InputStream is) throws IOException {
        SSLCertificateVerify cv = new SSLCertificateVerify(this._conn, this, false);
        cv.decode(this._conn, is);
    }

    public void sendServerKeyExchange() throws IOException {
        if (this.cipher_suite.requireServerKeyExchangeP(this._conn.ctx.getPrivateKey())) {
            SSLServerKeyExchange sk = new SSLServerKeyExchange();
            this.sendHandshakeMsg(this._conn, 12, sk);
        }
    }

    public void sendCertificateRequest() throws IOException {
        SSLCertificateRequest cr = new SSLCertificateRequest();
        this.sendHandshakeMsg(this._conn, 13, cr);
    }

    public void sendServerHelloDone() throws IOException {
        this.sendHandshakeMsg(this._conn, 14, new SSLServerHelloDone());
    }

    public void sendHelloRequest() throws IOException {
        this.sendHandshakeMsg(this._conn, 0, new SSLHelloRequest(), false);
        this._conn.sock_out.flush();
    }

    static {
        _v2_v3_cipher_suite_map.put(new Integer(65664), new Integer(4));
        _v2_v3_cipher_suite_map.put(new Integer(131200), new Integer(3));
        _v2_v3_cipher_suite_map.put(new Integer(131200), new Integer(6));
    }
}

