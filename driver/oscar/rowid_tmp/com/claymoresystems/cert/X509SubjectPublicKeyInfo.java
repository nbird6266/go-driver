/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.CertContext;
import com.claymoresystems.cert.DERUtils;
import com.claymoresystems.cert.X509DSAPublicKey;
import com.claymoresystems.cert.X509RSAPublicKey;
import com.claymoresystems.ptls.SSLDebug;
import cryptix.asn1.encoding.BaseCoder;
import cryptix.asn1.encoding.CoderOperations;
import cryptix.asn1.lang.ASNObject;
import cryptix.asn1.lang.ASNSpecification;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.Hashtable;

public class X509SubjectPublicKeyInfo {
    static Hashtable algorithmMap = new Hashtable();

    public static PublicKey createPublicKey(byte[] spkiDER) throws IOException {
        ASNSpecification aSNSpecification = CertContext.getSpec();
        synchronized (aSNSpecification) {
            SSLDebug.debug(32, "SPKI encoding", spkiDER);
            CoderOperations der_coder = BaseCoder.getInstance("DER");
            ByteArrayInputStream is = new ByteArrayInputStream(spkiDER);
            der_coder.init(is);
            ASNObject spki = CertContext.getSpec().getComponent("SubjectPublicKeyInfo");
            ASNObject tmp_alg = spki.getComponent("SubjectPublicKeyInfo.algorithm");
            ASNObject par_zero = tmp_alg.getComponent("AlgorithmIdentifier.parameters");
            par_zero.setValue(par_zero.getDefaultValue());
            spki.accept(der_coder, null);
            ASNObject tmp = spki.getComponent("SubjectPublicKeyInfo.algorithm");
            ASNObject params = tmp.getComponent("AlgorithmIdentifier.parameters");
            tmp = tmp.getComponent("AlgorithmIdentifier.algorithm");
            String algorithm = (String)tmp.getValue();
            byte[] param = (byte[])params.getValue();
            SSLDebug.debug(32, "SPKI params", param);
            tmp = spki.getComponent("SubjectPublicKeyInfo.subjectPublicKey");
            byte[] pk = (byte[])tmp.getValue();
            if (algorithm.equals("1.2.840.113549.1.1.1")) {
                return new X509RSAPublicKey(algorithm, param, pk);
            }
            if (algorithm.equals("1.2.840.10040.4.1")) {
                return new X509DSAPublicKey(algorithm, param, pk);
            }
            throw new IOException("Unrecognized OID for key" + algorithm);
        }
    }

    public static byte[] encodePublicKey(byte[] OID, byte[] params, byte[] key) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DERUtils.encodeOID(OID, (OutputStream)os);
        os.write(params);
        byte[] algId_c = os.toByteArray();
        os.reset();
        DERUtils.encodeSequence(algId_c, (OutputStream)os);
        DERUtils.encodeBitString(key, os);
        byte[] tmp = os.toByteArray();
        os.reset();
        DERUtils.encodeSequence(tmp, (OutputStream)os);
        return os.toByteArray();
    }
}

