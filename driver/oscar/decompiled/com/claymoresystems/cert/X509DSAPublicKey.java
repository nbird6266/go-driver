/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.CertContext;
import com.claymoresystems.cert.DERUtils;
import com.claymoresystems.cert.X509SubjectPublicKeyInfo;
import com.claymoresystems.crypto.BaseDSAPublicKey;
import com.claymoresystems.crypto.RawDSAParams;
import cryptix.asn1.encoding.BaseCoder;
import cryptix.asn1.encoding.CoderOperations;
import cryptix.asn1.lang.ASNObject;
import cryptix.asn1.lang.ASNSpecification;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.interfaces.DSAPublicKey;

public class X509DSAPublicKey
extends BaseDSAPublicKey {
    private byte[] oid = new byte[]{42, -122, 72, -50, 56, 4, 1};

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public X509DSAPublicKey(String oid, byte[] parameters, byte[] key) throws IOException {
        ASNSpecification aSNSpecification = CertContext.getSpec();
        synchronized (aSNSpecification) {
            CoderOperations der_coder = BaseCoder.getInstance("DER");
            ByteArrayInputStream is = new ByteArrayInputStream(parameters);
            der_coder.init(is);
            ASNObject dsaParams = CertContext.getSpec().getComponent("DSAParameters");
            dsaParams.accept(der_coder, null);
            BigInteger p = (BigInteger)dsaParams.getComponent("DSAParameters.p").getValue();
            BigInteger q = (BigInteger)dsaParams.getComponent("DSAParameters.q").getValue();
            BigInteger g = (BigInteger)dsaParams.getComponent("DSAParameters.g").getValue();
            this.params = new RawDSAParams(p, q, g);
            if (key[0] != 0) {
                throw new IOException("Bad encoded key");
            }
            byte[] enc_key = new byte[key.length - 1];
            System.arraycopy(key, 1, enc_key, 0, enc_key.length);
            is = new ByteArrayInputStream(enc_key);
            ASNObject dsaKey = CertContext.getSpec().getComponent("DSAKey");
            der_coder.init(is);
            dsaKey.accept(der_coder, null);
            this.Y = (BigInteger)dsaKey.getValue();
        }
    }

    public X509DSAPublicKey(DSAPublicKey key) {
        super(key);
    }

    public String getFormat() {
        return "DER";
    }

    public byte[] getEncoded() {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            DERUtils.encodeInteger(this.params.getP(), os);
            DERUtils.encodeInteger(this.params.getQ(), os);
            DERUtils.encodeInteger(this.params.getG(), os);
            byte[] tmp = os.toByteArray();
            os.reset();
            DERUtils.encodeSequence(tmp, (OutputStream)os);
            byte[] p = os.toByteArray();
            os.reset();
            DERUtils.encodeInteger(this.Y, os);
            byte[] key = os.toByteArray();
            return X509SubjectPublicKeyInfo.encodePublicKey(this.oid, p, key);
        }
        catch (IOException e) {
            throw new InternalError(e.toString());
        }
    }
}

