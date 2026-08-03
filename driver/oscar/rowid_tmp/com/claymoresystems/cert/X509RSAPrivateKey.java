/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.CertContext;
import com.claymoresystems.cert.DERUtils;
import com.claymoresystems.ptls.SSLDebug;
import cryptix.asn1.encoding.BaseCoder;
import cryptix.asn1.encoding.CoderOperations;
import cryptix.asn1.lang.ASNObject;
import cryptix.asn1.lang.ASNSpecification;
import cryptix.provider.rsa.BaseRSAPrivateKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.RSAFactors;

class X509RSAPrivateKey
extends BaseRSAPrivateKey {
    private byte[] encoding;

    X509RSAPrivateKey(CryptixRSAPrivateKey key) throws ClassCastException {
        RSAFactors rsa = (RSAFactors)((Object)key);
        this.setRsaParams(key.getExponent(), rsa.getP(), rsa.getQ(), rsa.getInverseOfQModP());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public X509RSAPrivateKey(String oid, byte[] parameters, byte[] key) throws IOException {
        ASNSpecification aSNSpecification = CertContext.getSpec();
        synchronized (aSNSpecification) {
            SSLDebug.debug(8, "RSA Private key encoding", key);
            CoderOperations der_coder = BaseCoder.getInstance("DER");
            ByteArrayInputStream is = new ByteArrayInputStream(key);
            der_coder.init(is);
            ASNObject rsaKey = CertContext.getSpec().getComponent("RSAPrivateKey");
            rsaKey.accept(der_coder, null);
            BigInteger d = (BigInteger)rsaKey.getComponent("RSAPrivateKey.privateExponent").getValue();
            BigInteger p = (BigInteger)rsaKey.getComponent("RSAPrivateKey.prime1").getValue();
            BigInteger q = (BigInteger)rsaKey.getComponent("RSAPrivateKey.prime2").getValue();
            BigInteger u = (BigInteger)rsaKey.getComponent("RSAPrivateKey.coefficient").getValue();
            SSLDebug.debug(8, "RSA Private decoded");
            this.setRsaParams(d, p, q, u);
        }
    }

    public byte[] getEncoded() {
        SSLDebug.debug(8, "Decoding private key");
        if (this.encoding != null) {
            return this.encoding;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BigInteger p1 = this.getP().subtract(new BigInteger("1"));
        BigInteger q1 = this.getQ().subtract(new BigInteger("1"));
        BigInteger pub = this.getExponent().modInverse(p1.multiply(q1));
        BigInteger dmp1 = this.getExponent().mod(p1);
        BigInteger dmq1 = this.getExponent().mod(q1);
        try {
            DERUtils.encodeInteger(new BigInteger("0"), os);
            DERUtils.encodeInteger(this.getModulus(), os);
            DERUtils.encodeInteger(pub, os);
            DERUtils.encodeInteger(this.getExponent(), os);
            DERUtils.encodeInteger(this.getP(), os);
            DERUtils.encodeInteger(this.getQ(), os);
            DERUtils.encodeInteger(dmp1, os);
            DERUtils.encodeInteger(dmq1, os);
            DERUtils.encodeInteger(this.getInverseOfQModP(), os);
            byte[] tmp = os.toByteArray();
            os.reset();
            DERUtils.encodeSequence(tmp, (OutputStream)os);
            this.encoding = os.toByteArray();
            return this.encoding;
        }
        catch (IOException e) {
            throw new InternalError(e.toString());
        }
    }

    public String getFormat() {
        return "X509";
    }
}

