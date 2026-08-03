/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.sslg.SSLPolicyInt;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.CryptixRSAPublicKey;

class SSLCipherSuite {
    private int cipher_suite;
    private int keyExchangeAlg;
    private int signatureAlg;
    private String digestAlg;
    private int digestBytes;
    private String cipherAlg;
    private int cipherBytes;
    private int effectiveBytes;
    private boolean require_ske;
    private boolean export;
    private boolean block_cipher;
    public static final int SSL_KEX_DH = 1;
    public static final int SSL_KEX_RSA = 2;
    public static final int SSL_SIG_DSS = 1;
    public static final int SSL_SIG_RSA = 2;
    public static final int SSL_SIG_anon = 3;
    private static final SSLCipherSuite[] master_list = new SSLCipherSuite[]{new SSLCipherSuite(1, 2, 2, "MD5", 16, "NULL", 0, 0, false, false, false), new SSLCipherSuite(2, 2, 2, "SHA", 20, "NULL", 0, 0, false, false, false), new SSLCipherSuite(3, 2, 2, "MD5", 16, "RC4", 16, 5, false, true, false), new SSLCipherSuite(4, 2, 2, "MD5", 16, "RC4", 16, 16, false, false, false), new SSLCipherSuite(5, 2, 2, "SHA", 20, "RC4", 16, 16, false, false, false), new SSLCipherSuite(6, 2, 2, "MD5", 16, "RC2", 16, 5, false, true, true), new SSLCipherSuite(7, 2, 2, "SHA", 20, "IDEA", 16, 16, false, false, true), new SSLCipherSuite(8, 2, 2, "SHA", 20, "DES", 8, 5, false, true, true), new SSLCipherSuite(9, 2, 2, "SHA", 20, "DES", 8, 8, false, false, true), new SSLCipherSuite(10, 2, 2, "SHA", 20, "DES-EDE3", 24, 24, false, false, true), new SSLCipherSuite(11, 1, 1, "SHA", 20, "DES", 8, 5, false, true, true), new SSLCipherSuite(12, 1, 1, "SHA", 20, "DES", 8, 8, false, false, true), new SSLCipherSuite(13, 1, 1, "SHA", 20, "DES-EDE3", 24, 24, false, false, true), new SSLCipherSuite(14, 1, 2, "SHA", 20, "DES", 8, 5, false, true, true), new SSLCipherSuite(15, 1, 2, "SHA", 20, "DES", 8, 8, false, false, true), new SSLCipherSuite(16, 1, 2, "SHA", 20, "DES-EDE3", 24, 24, false, false, true), new SSLCipherSuite(17, 1, 1, "SHA", 20, "DES", 8, 5, true, true, true), new SSLCipherSuite(18, 1, 1, "SHA", 20, "DES", 8, 8, true, false, true), new SSLCipherSuite(19, 1, 1, "SHA", 20, "DES-EDE3", 24, 24, true, false, true), new SSLCipherSuite(23, 1, 3, "MD5", 16, "RC4", 16, 5, false, true, false), new SSLCipherSuite(24, 1, 3, "MD5", 16, "RC4", 16, 16, false, false, false), new SSLCipherSuite(25, 1, 3, "SHA", 20, "DES", 8, 8, false, false, true), new SSLCipherSuite(26, 1, 3, "SHA", 20, "DES", 8, 8, false, false, true), new SSLCipherSuite(27, 1, 3, "SHA", 20, "DES-EDE3", 24, 24, false, false, true), new SSLCipherSuite(102, 1, 1, "SHA", 20, "RC4", 16, 16, true, false, false), new SSLCipherSuite(103, 1, 1, "SHA", 20, "NULL", 0, 0, true, false, false)};

    public SSLCipherSuite(int cipher_suite_, int keyExchange, int signature, String digest, int digestBytes_, String cipher, int cipherBytes_, int effectiveBytes_, boolean require_ske_, boolean export_, boolean block_cipher_) {
        this.cipher_suite = cipher_suite_;
        this.keyExchangeAlg = keyExchange;
        this.signatureAlg = signature;
        this.digestAlg = digest;
        this.digestBytes = digestBytes_;
        this.cipherAlg = cipher;
        this.cipherBytes = cipherBytes_;
        this.effectiveBytes = effectiveBytes_;
        this.require_ske = require_ske_;
        this.export = export_;
        this.block_cipher = block_cipher_;
    }

    public static SSLCipherSuite findCipherSuite(int cs) {
        for (int i = 0; i < master_list.length; ++i) {
            if (SSLCipherSuite.master_list[i].cipher_suite != cs) continue;
            return master_list[i];
        }
        throw new Error("Unknown cipher suite  " + cs);
    }

    int getValue() {
        return this.cipher_suite;
    }

    String getName() {
        return SSLPolicyInt.getCipherSuiteName(this.cipher_suite);
    }

    int getKeyExchangeAlg() {
        return this.keyExchangeAlg;
    }

    int getSignatureAlg() {
        return this.signatureAlg;
    }

    String getSignatureAlgNorm() {
        switch (this.signatureAlg) {
            case 2: {
                return this.digestAlg + "/RSA/PKCS#1";
            }
            case 1: {
                return "DSA";
            }
        }
        throw new InternalError("Bogus algorithm");
    }

    String getSignatureAlgBase() {
        switch (this.signatureAlg) {
            case 2: {
                return "RSA";
            }
            case 1: {
                return "DSA";
            }
        }
        throw new InternalError("Bogus algorithm");
    }

    String getSignatureAlgCV() {
        switch (this.signatureAlg) {
            case 2: {
                return "RawRSA";
            }
            case 1: {
                return "RawDSA";
            }
        }
        throw new InternalError("Bogus algorithm");
    }

    String getDigestAlg() {
        return this.digestAlg;
    }

    int getDigestOutputLength() {
        return this.digestBytes;
    }

    String getCipherAlg() {
        String alg = this.cipherAlg;
        if (this.blockCipherP()) {
            alg = alg + "/CBC";
        }
        return alg;
    }

    int getCipherKeyLength() {
        return this.cipherBytes;
    }

    int getCipherEffectiveKeyLength() {
        return this.effectiveBytes;
    }

    boolean requireServerKeyExchangeP(PrivateKey key) {
        BigInteger mod;
        if (this.require_ske) {
            return true;
        }
        if (!this.exportableP()) {
            return false;
        }
        return this.keyExchangeAlg == 2 && (mod = ((CryptixRSAPrivateKey)key).getModulus()).bitLength() > 512;
    }

    boolean allowServerKeyExchangeP(PublicKey key) {
        BigInteger mod;
        if (this.require_ske) {
            return true;
        }
        if (!this.exportableP()) {
            return false;
        }
        return this.keyExchangeAlg == 2 && (mod = ((CryptixRSAPublicKey)key).getModulus()).bitLength() > 512;
    }

    boolean exportableP() {
        return this.export;
    }

    boolean blockCipherP() {
        return this.block_cipher;
    }
}

