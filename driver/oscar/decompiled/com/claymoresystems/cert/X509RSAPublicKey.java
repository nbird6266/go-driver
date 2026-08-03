/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.CertContext;
import com.claymoresystems.cert.DERUtils;
import com.claymoresystems.cert.X509SubjectPublicKeyInfo;
import com.claymoresystems.ptls.SSLDebug;
import cryptix.asn1.encoding.BaseCoder;
import cryptix.asn1.encoding.CoderOperations;
import cryptix.asn1.lang.ASNObject;
import cryptix.asn1.lang.ASNSpecification;
import cryptix.provider.rsa.BaseRSAPublicKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import xjava.security.interfaces.CryptixRSAPublicKey;

class X509RSAPublicKey
extends BaseRSAPublicKey {
    private byte[] encoding;
    private byte[] oid = new byte[]{42, -122, 72, -122, -9, 13, 1, 1, 1};

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public X509RSAPublicKey(String oid, byte[] parameters, byte[] key) throws IOException {
        this.encoding = key;
        ASNSpecification aSNSpecification = CertContext.getSpec();
        synchronized (aSNSpecification) {
            SSLDebug.debug(8, "RSA Public key encoding", key);
            if (key[0] != 0) {
                throw new IOException("Bad encoded key");
            }
            byte[] enc_key = new byte[key.length - 1];
            System.arraycopy(key, 1, enc_key, 0, enc_key.length);
            ByteArrayInputStream is = new ByteArrayInputStream(enc_key);
            CoderOperations der_coder = BaseCoder.getInstance("DER");
            der_coder.init(is);
            ASNObject rsaKey = CertContext.getSpec().getComponent("RSAPublicKey");
            rsaKey.accept(der_coder, null);
            BigInteger n = (BigInteger)rsaKey.getComponent("RSAPublicKey.modulus").getValue();
            BigInteger e = (BigInteger)rsaKey.getComponent("RSAPublicKey.publicExponent").getValue();
            SSLDebug.debug(32, "Modulus ", n.toByteArray());
            SSLDebug.debug(32, "Public ", e.toByteArray());
            this.setRsaParams(n, e);
        }
    }

    public X509RSAPublicKey(CryptixRSAPublicKey k) {
        BaseRSAPublicKey key = (BaseRSAPublicKey)k;
        this.setRsaParams(key.getModulus(), key.getExponent());
    }

    public String getFormat() {
        return "PKCS-1";
    }

    public byte[] getEncoded() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (this.encoding != null) {
            return this.encoding;
        }
        try {
            DERUtils.encodeInteger(this.getModulus(), os);
            DERUtils.encodeInteger(this.getExponent(), os);
            byte[] tmp = os.toByteArray();
            os.reset();
            DERUtils.encodeSequence(tmp, (OutputStream)os);
            byte[] key = os.toByteArray();
            byte[] p = new byte[]{};
            this.encoding = X509SubjectPublicKeyInfo.encodePublicKey(this.oid, p, key);
            return this.encoding;
        }
        catch (IOException e) {
            throw new InternalError(e.toString());
        }
    }
}

