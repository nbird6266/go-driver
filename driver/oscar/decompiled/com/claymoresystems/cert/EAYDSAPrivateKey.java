/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.CertContext;
import com.claymoresystems.cert.DERUtils;
import com.claymoresystems.crypto.BaseDSAPrivateKey;
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
import java.security.interfaces.DSAPrivateKey;

public class EAYDSAPrivateKey
extends BaseDSAPrivateKey {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public EAYDSAPrivateKey(byte[] encoding) throws IOException {
        ASNSpecification aSNSpecification = CertContext.getSpec();
        synchronized (aSNSpecification) {
            CoderOperations der_coder = BaseCoder.getInstance("DER");
            ByteArrayInputStream is = new ByteArrayInputStream(encoding);
            der_coder.init(is);
            ASNObject dsaKey = CertContext.getSpec().getComponent("EAYDSAPrivateKey");
            dsaKey.accept(der_coder, null);
            BigInteger p = (BigInteger)dsaKey.getComponent("EAYDSAPrivateKey.p").getValue();
            BigInteger q = (BigInteger)dsaKey.getComponent("EAYDSAPrivateKey.q").getValue();
            BigInteger g = (BigInteger)dsaKey.getComponent("EAYDSAPrivateKey.g").getValue();
            this.params = new RawDSAParams(p, q, g);
            this.X = (BigInteger)dsaKey.getComponent("EAYDSAPrivateKey.x").getValue();
        }
    }

    public EAYDSAPrivateKey(DSAPrivateKey key) {
        super(key);
    }

    public String getFormat() {
        return "DER";
    }

    public byte[] getEncoded() {
        ByteArrayOutputStream os;
        try {
            os = new ByteArrayOutputStream();
            DERUtils.encodeInteger(BigInteger.valueOf(0L), os);
            DERUtils.encodeInteger(this.params.getP(), os);
            DERUtils.encodeInteger(this.params.getQ(), os);
            DERUtils.encodeInteger(this.params.getG(), os);
            DERUtils.encodeInteger(this.getY(), os);
            DERUtils.encodeInteger(this.X, os);
            byte[] tmp = os.toByteArray();
            os.reset();
            DERUtils.encodeSequence(tmp, (OutputStream)os);
        }
        catch (IOException e) {
            throw new InternalError(e.toString());
        }
        return os.toByteArray();
    }
}

