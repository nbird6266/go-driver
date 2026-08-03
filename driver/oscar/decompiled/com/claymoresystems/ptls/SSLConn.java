/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLAlert;
import com.claymoresystems.ptls.SSLAlertException;
import com.claymoresystems.ptls.SSLAlertX;
import com.claymoresystems.ptls.SSLCipherState;
import com.claymoresystems.ptls.SSLContext;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLException;
import com.claymoresystems.ptls.SSLHandshake;
import com.claymoresystems.ptls.SSLHandshakeClient;
import com.claymoresystems.ptls.SSLHandshakeFailedException;
import com.claymoresystems.ptls.SSLHandshakeServer;
import com.claymoresystems.ptls.SSLReHandshakeException;
import com.claymoresystems.ptls.SSLRecord;
import com.claymoresystems.ptls.SSLRecordReader;
import com.claymoresystems.ptls.SSLSocket;
import com.claymoresystems.ptls.SSLThrewAlertException;
import com.claymoresystems.sslg.SSLPolicyInt;
import com.claymoresystems.util.Util;
import cryptix.util.core.ArrayUtil;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Vector;

class SSLConn {
    static final int SSL_CLIENT = 1;
    static final int SSL_SERVER = 2;
    static int debugVal = 0;
    int ssl_version = 0;
    int max_ssl_version = 0;
    SSLContext ctx;
    SSLSocket s = null;
    SSLPolicyInt policy;
    PushbackInputStream sock_in;
    InputStream sock_in_hp;
    InputStream sock_in_data;
    OutputStream _sock_out;
    BufferedOutputStream sock_out;
    OutputStream sock_out_external;
    boolean sentClose = false;
    boolean recvdClose = false;
    Vector peerCertificateChain = null;
    String sessionLookupKey = null;
    int how;
    byte[] session_id;
    SSLCipherState write_cipher_state = null;
    SSLCipherState read_cipher_state = null;
    SSLCipherState next_write_cipher_state;
    SSLCipherState next_read_cipher_state;
    long write_sequence_num;
    long read_sequence_num;
    boolean secureMode = false;
    boolean invalid = false;
    SSLHandshake hs;
    SSLRecordReader reader;

    SSLConn(SSLSocket sock, InputStream in, OutputStream out, SSLContext c, int how) throws IOException {
        this.s = sock;
        this.how = how;
        this.ctx = c;
        this.policy = c.getPolicy();
        this.sock_in = new PushbackInputStream(in);
        this._sock_out = out;
        this.sock_out = new BufferedOutputStream(this._sock_out);
        this.reader = new SSLRecordReader(this);
    }

    void renegotiate(SSLPolicyInt p) throws IOException {
        this.policy = p;
        this.handshake();
    }

    void handshake() throws IOException {
        if (this.read_cipher_state == null) {
            this.max_ssl_version = this.policy.negotiateTLSP() ? 769 : 768;
            this.ssl_version = this.policy.negotiateTLSP() ? 769 : 768;
        }
        this.hs = this.how == 1 ? new SSLHandshakeClient(this) : new SSLHandshakeServer(this);
        try {
            this.hs.handshake();
            if (this.sock_in_hp.available() != 0) {
                this.alert(SSLAlertX.TLS_ALERT_UNEXPECTED_MESSAGE);
            }
            this.secureMode = true;
        }
        catch (IOException e) {
            if ((SSLDebug.debugVal & 0x40) > 0) {
                e.printStackTrace();
            }
            if (!(e instanceof SSLAlertException)) {
                throw new SSLHandshakeFailedException(e.toString());
            }
            throw e;
        }
    }

    int getCipherSuite() throws IOException {
        if (!this.hs.finishedP()) {
            throw new SSLException("Handshake not finished");
        }
        return this.write_cipher_state.cipher_suite.getValue();
    }

    SSLPolicyInt getPolicy() {
        return this.policy;
    }

    byte[] getSessionID() throws IOException {
        if (!this.hs.finishedP()) {
            throw new SSLException("Handshake not finished");
        }
        return this.session_id;
    }

    int getVersion() throws IOException {
        if (!this.hs.finishedP()) {
            throw new SSLException("Handshake not finished");
        }
        return this.ssl_version;
    }

    Vector getCertificateChain() throws IOException {
        if (!this.hs.finishedP()) {
            throw new SSLException("Handshake not finished");
        }
        return this.peerCertificateChain;
    }

    void alert(int a) throws IOException {
        this.sendAlertNoException(a, true);
        throw new SSLThrewAlertException(new SSLAlertX(this.ssl_version, a, true));
    }

    void sendAlertNoException(int a, boolean fatal) throws IOException {
        SSLAlertX alertx = new SSLAlertX(this.ssl_version, a, fatal);
        if (fatal) {
            SSLDebug.debug(4, "Throwing a fatal alert, lookup key " + this.sessionLookupKey);
            this.makeUnresumable();
            this.invalid = true;
        }
        SSLAlert alert = new SSLAlert(alertx);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        alert.encode(this, bos);
        SSLRecord r = new SSLRecord(this, 21, bos.toByteArray());
        r.send(this);
        this.sock_out.flush();
    }

    boolean processIncomingHandshakeRecord(byte[] data) throws IOException {
        byte[] helloRequest = new byte[]{0, 0, 0, 0};
        if (this.hs.finishedP()) {
            switch (data[0]) {
                case 0: {
                    if (this.how != 1) {
                        this.alert(SSLAlertX.TLS_ALERT_UNEXPECTED_MESSAGE);
                    }
                    if (!ArrayUtil.areEqual(data, helloRequest)) {
                        this.alert(SSLAlertX.TLS_ALERT_ILLEGAL_PARAMETER);
                    }
                    throw new SSLReHandshakeException();
                }
                case 1: {
                    if (this.how != 2) {
                        this.alert(SSLAlertX.TLS_ALERT_UNEXPECTED_MESSAGE);
                    }
                    return true;
                }
            }
            this.alert(SSLAlertX.TLS_ALERT_UNEXPECTED_MESSAGE);
        } else if (data[0] == 0) {
            this.alert(SSLAlertX.TLS_ALERT_UNEXPECTED_MESSAGE);
        }
        return false;
    }

    static void debug(int type, String val) {
        if ((debugVal & type) > 0) {
            System.out.println(val);
        }
    }

    static void debug(int type, String label, byte[] hd) {
        if ((debugVal & type) > 0) {
            Util.xdump(label, hd);
        }
    }

    InputStream getInStream() {
        if (!this.hs.finishedP()) {
            return null;
        }
        if (this.read_cipher_state == null) {
            return null;
        }
        return this.sock_in_data;
    }

    OutputStream getOutStream() {
        if (!this.hs.finishedP()) {
            return null;
        }
        if (this.write_cipher_state == null) {
            return null;
        }
        return this.sock_out_external;
    }

    void makeUnresumable() {
        if (this.sessionLookupKey != null) {
            SSLDebug.debug(4, "Making session " + this.sessionLookupKey + "Unresumable");
            this.ctx.destroySession(this.sessionLookupKey);
        }
    }

    void sendClose() throws IOException {
        if (!this.sentClose) {
            this.sendAlertNoException(SSLAlertX.TLS_ALERT_CLOSE_NOTIFY, false);
            this.sentClose = true;
        }
    }

    void recvClose(boolean enforceFinished) throws IOException {
        InputStream in = this.getInStream();
        byte[] buf = new byte[1024];
        while (in.read(buf) >= 0) {
            if (!enforceFinished) continue;
            throw new SSLException("Excess data in pipe when closed");
        }
    }

    void close() throws IOException {
        this.sendClose();
        if (this.policy.waitOnCloseP()) {
            this.recvClose(false);
        }
        if (this.s != null) {
            this.s.hardClose();
        }
    }

    public static void setDebug(int flag) {
        debugVal = flag;
    }
}

