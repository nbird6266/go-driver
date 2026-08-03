/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.sslg;

import com.claymoresystems.sslg.CertVerifyPolicyInt;
import java.util.Vector;

public class SSLPolicyInt {
    private static Vector _name_table = new Vector(256);
    public static final short TLS_RSA_WITH_NULL_MD5 = 1;
    public static final short TLS_RSA_WITH_NULL_SHA = 2;
    public static final short TLS_RSA_EXPORT_WITH_RC4_40_MD5 = 3;
    public static final short TLS_RSA_WITH_RC4_128_MD5 = 4;
    public static final short TLS_RSA_WITH_RC4_128_SHA = 5;
    public static final short TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5 = 6;
    public static final short TLS_RSA_WITH_IDEA_CBC_SHA = 7;
    public static final short TLS_RSA_EXPORT_WITH_DES40_CBC_SHA = 8;
    public static final short TLS_RSA_WITH_DES_CBC_SHA = 9;
    public static final short TLS_RSA_WITH_3DES_EDE_CBC_SHA = 10;
    public static final short TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA = 11;
    public static final short TLS_DH_DSS_WITH_DES_CBC_SHA = 12;
    public static final short TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA = 13;
    public static final short TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA = 14;
    public static final short TLS_DH_RSA_WITH_DES_CBC_SHA = 15;
    public static final short TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA = 16;
    public static final short TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA = 17;
    public static final short TLS_DHE_DSS_WITH_DES_CBC_SHA = 18;
    public static final short TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA = 19;
    public static final short TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA = 20;
    public static final short TLS_DHE_RSA_WITH_DES_CBC_SHA = 21;
    public static final short TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA = 22;
    public static final short TLS_DH_anon_EXPORT_WITH_RC4_40_MD5 = 23;
    public static final short TLS_DH_anon_WITH_RC4_128_MD5 = 24;
    public static final short TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA = 25;
    public static final short TLS_DH_anon_WITH_DES_CBC_SHA = 26;
    public static final short TLS_DH_anon_WITH_3DES_EDE_CBC_SHA = 27;
    public static final short TLS_DHE_DSS_WITH_RC4_128_SHA = 102;
    public static final short TLS_DHE_DSS_WITH_NULL_SHA = 103;
    private boolean requireClientAuth = false;
    private short[] cipherSuites = new short[]{10, 5, 4, 19, 102, 9, 18};
    private boolean negotiateTLS = true;
    private byte[][] rootCertificates = null;
    private int sessionLife = 86400;
    private boolean acceptUnverifiable = false;
    private boolean dhAlwaysEphemeral = true;
    private boolean handshakeOnConnect = true;
    private boolean waitOnClose = true;
    private CertVerifyPolicyInt certVerifyPolicy = new CertVerifyPolicyInt();

    public void requireClientAuth(boolean val) {
        this.requireClientAuth = val;
    }

    public boolean requireClientAuthP() {
        return this.requireClientAuth;
    }

    public void setCipherSuites(short[] cS) {
        this.cipherSuites = cS;
    }

    public short[] getCipherSuites() {
        return this.cipherSuites;
    }

    public void negotiateTLS(boolean tls) {
        this.negotiateTLS = tls;
    }

    public boolean negotiateTLSP() {
        return this.negotiateTLS;
    }

    public void setSessonLifetime(int lifetime) {
        this.sessionLife = lifetime;
    }

    public int getSessionLifetime() {
        return this.sessionLife;
    }

    public static String getCipherSuiteName(int num) {
        return (String)_name_table.elementAt(num);
    }

    public static int getCipherSuiteNumber(String name) {
        return _name_table.indexOf(name);
    }

    public void acceptUnverifiableCertificates(boolean accept) {
        this.acceptUnverifiable = accept;
    }

    public boolean acceptUnverifiableCertificatesP() {
        return this.acceptUnverifiable;
    }

    public void setDHAlwaysEphemeral(boolean dhephemeral) {
        this.dhAlwaysEphemeral = dhephemeral;
    }

    public boolean dhAlwaysEphemeralP() {
        return this.dhAlwaysEphemeral;
    }

    public void handshakeOnConnect(boolean value) {
        this.handshakeOnConnect = value;
    }

    public boolean handshakeOnConnectP() {
        return this.handshakeOnConnect;
    }

    public void waitOnClose(boolean v) {
        this.waitOnClose = v;
    }

    public boolean waitOnCloseP() {
        return this.waitOnClose;
    }

    public void setCertVerifyPolicy(CertVerifyPolicyInt p) {
        this.certVerifyPolicy = p;
    }

    public CertVerifyPolicyInt getCertVerifyPolicy() {
        return this.certVerifyPolicy;
    }

    static {
        _name_table.setSize(100);
        _name_table.insertElementAt("TLS_RSA_WITH_NULL_MD5", 1);
        _name_table.insertElementAt("TLS_RSA_WITH_NULL_SHA", 2);
        _name_table.insertElementAt("TLS_RSA_EXPORT_WITH_RC4_40_MD5", 3);
        _name_table.insertElementAt("TLS_RSA_WITH_RC4_128_MD5", 4);
        _name_table.insertElementAt("TLS_RSA_WITH_RC4_128_SHA", 5);
        _name_table.insertElementAt("TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5", 6);
        _name_table.insertElementAt("TLS_RSA_WITH_IDEA_CBC_SHA", 7);
        _name_table.insertElementAt("TLS_RSA_EXPORT_WITH_DES40_CBC_SHA", 8);
        _name_table.insertElementAt("TLS_RSA_WITH_DES_CBC_SHA", 9);
        _name_table.insertElementAt("TLS_RSA_WITH_3DES_EDE_CBC_SHA", 10);
        _name_table.insertElementAt("TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA", 11);
        _name_table.insertElementAt("TLS_DH_DSS_WITH_DES_CBC_SHA", 12);
        _name_table.insertElementAt("TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA", 13);
        _name_table.insertElementAt("TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA", 14);
        _name_table.insertElementAt("TLS_DH_RSA_WITH_DES_CBC_SHA", 15);
        _name_table.insertElementAt("TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA", 16);
        _name_table.insertElementAt("TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", 17);
        _name_table.insertElementAt("TLS_DHE_DSS_WITH_DES_CBC_SHA", 18);
        _name_table.insertElementAt("TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA", 19);
        _name_table.insertElementAt("TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", 20);
        _name_table.insertElementAt("TLS_DHE_RSA_WITH_DES_CBC_SHA", 21);
        _name_table.insertElementAt("TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA", 22);
        _name_table.insertElementAt("TLS_DH_anon_EXPORT_WITH_RC4_40_MD5", 23);
        _name_table.insertElementAt("TLS_DH_anon_WITH_RC4_128_MD5", 24);
        _name_table.insertElementAt("TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA", 25);
        _name_table.insertElementAt("TLS_DH_anon_WITH_DES_CBC_SHA", 26);
        _name_table.insertElementAt("TLS_DH_anon_WITH_3DES_EDE_CBC_SHA", 27);
        _name_table.insertElementAt("TLS_DHE_DSS_WITH_RC4_128_SHA", 102);
        _name_table.insertElementAt("TLS_DHE_DSS_WITH_NULL_SHA", 103);
    }
}

