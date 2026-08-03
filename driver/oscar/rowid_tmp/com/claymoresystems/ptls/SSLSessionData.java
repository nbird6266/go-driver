/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLCipherSuite;
import com.claymoresystems.ptls.SSLHandshake;
import java.util.Vector;

class SSLSessionData {
    byte[] session_id;
    int ssl_version;
    byte[] master_secret;
    Vector peerCertificateChain;
    SSLCipherSuite cipher_suite;
    long expiry;
    String lookupKey;

    SSLSessionData(SSLHandshake hs, String key) {
        this.session_id = hs.session_id;
        this.ssl_version = hs._conn.ssl_version;
        this.master_secret = hs.master_secret;
        this.peerCertificateChain = hs._conn.peerCertificateChain;
        this.cipher_suite = hs.cipher_suite;
        this.expiry = System.currentTimeMillis() + (long)(hs._conn.getPolicy().getSessionLifetime() * 1000);
        this.lookupKey = key;
        hs._conn.sessionLookupKey = key;
    }

    void restoreSession(SSLHandshake hs) {
        hs.session_id = this.session_id;
        hs._conn.ssl_version = this.ssl_version;
        hs.master_secret = this.master_secret;
        hs._conn.peerCertificateChain = this.peerCertificateChain;
        hs.cipher_suite = this.cipher_suite;
        hs._conn.sessionLookupKey = this.lookupKey;
    }

    byte[] getSessionID() {
        return this.session_id;
    }

    SSLCipherSuite getCipherSuite() {
        return this.cipher_suite;
    }

    int getSSLVersion() {
        return this.ssl_version;
    }

    Vector getPeerCertificateChain() {
        return this.peerCertificateChain;
    }

    long getExpiryTime() {
        return this.expiry;
    }

    String getLookupKey() {
        return this.lookupKey;
    }
}

