/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import com.claymoresystems.cert.DERUtils;
import com.claymoresystems.cert.EAYDSAPrivateKey;
import com.claymoresystems.cert.X509DSAPublicKey;
import com.claymoresystems.cert.X509Name;
import com.claymoresystems.cert.X509RSAPrivateKey;
import com.claymoresystems.cert.X509RSAPublicKey;
import com.claymoresystems.crypto.EAYEncryptedPrivateKey;
import com.claymoresystems.ptls.LoadProviders;
import cryptix.util.mime.Base64OutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.DSAKeyPairGenerator;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.util.Vector;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.CryptixRSAPublicKey;

public class CertRequest {
    public static KeyPair generateKey(String type, int size, String password, BufferedWriter keyfile, boolean newParams) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        KeyPairGenerator kg;
        if (type.equals("DSA")) {
            kg = KeyPairGenerator.getInstance(type);
            DSAKeyPairGenerator dsaGen = (DSAKeyPairGenerator)((Object)kg);
            dsaGen.initialize(size, newParams, new SecureRandom());
        } else {
            kg = KeyPairGenerator.getInstance(type, "Cryptix");
            kg.initialize(size);
        }
        KeyPair pair = kg.generateKeyPair();
        PrivateKey priv = type.equals("DSA") ? new EAYDSAPrivateKey((DSAPrivateKey)pair.getPrivate()) : new X509RSAPrivateKey((CryptixRSAPrivateKey)pair.getPrivate());
        EAYEncryptedPrivateKey.writePrivateKey(priv, password.getBytes(), keyfile);
        return pair;
    }

    public static byte[] makeSPKACRequest(KeyPair p) throws IOException {
        byte[] signature;
        String algorithm;
        byte[] algId;
        PrivateKey priv = p.getPrivate();
        PublicKey pub = p.getPublic();
        String alg = priv.getAlgorithm();
        byte[] DSA_algid = new byte[]{48, 11, 6, 7, 42, -122, 72, -50, 56, 4, 3, 5, 0};
        byte[] RSA_algid = new byte[]{48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 5, 5, 0};
        if (alg.equals("DSA")) {
            algId = DSA_algid;
            algorithm = "DSA";
            pub = new X509DSAPublicKey((DSAPublicKey)pub);
        } else if (alg.equals("RSA")) {
            algId = RSA_algid;
            algorithm = "SHA-1/RSA/PKCS#1";
            pub = new X509RSAPublicKey((CryptixRSAPublicKey)pub);
        } else {
            throw new InternalError("Unknown algorithm " + alg);
        }
        byte[] key_enc = pub.getEncoded();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(key_enc);
        DERUtils.encodeIA5String("Challenge", os);
        byte[] tmp = os.toByteArray();
        os.reset();
        DERUtils.encodeSequence(tmp, (OutputStream)os);
        byte[] toBeSigned = os.toByteArray();
        try {
            Signature sig = Signature.getInstance(algorithm);
            sig.initSign(priv);
            sig.update(toBeSigned);
            signature = sig.sign();
        }
        catch (Exception e) {
            throw new InternalError(e.toString());
        }
        os.reset();
        os.write(toBeSigned);
        os.write(algId);
        if (alg.equals("RSA")) {
            signature = CertRequest.fitSignature(signature, pub);
        }
        DERUtils.encodeBitString(signature, os);
        byte[] spkac_c = os.toByteArray();
        os.reset();
        DERUtils.encodeSequence(spkac_c, (OutputStream)os);
        byte[] spkac = os.toByteArray();
        return spkac;
    }

    public static byte[] makePKCS10Request(KeyPair p, X509Name name) throws IOException {
        byte[] signature;
        String algorithm;
        byte[] algId;
        PrivateKey priv = p.getPrivate();
        PublicKey pub = p.getPublic();
        String alg = priv.getAlgorithm();
        byte[] DSA_algid = new byte[]{48, 11, 6, 7, 42, -122, 72, -50, 56, 4, 3, 5, 0};
        byte[] RSA_algid = new byte[]{48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 5, 5, 0};
        if (alg.equals("DSA")) {
            algId = DSA_algid;
            algorithm = "DSA";
            pub = new X509DSAPublicKey((DSAPublicKey)pub);
        } else if (alg.equals("RSA")) {
            algId = RSA_algid;
            algorithm = "SHA-1/RSA/PKCS#1";
            pub = new X509RSAPublicKey((CryptixRSAPublicKey)pub);
        } else {
            throw new InternalError("Unknown algorithm " + alg);
        }
        byte[] key_enc = pub.getEncoded();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DERUtils.encodeInteger(new BigInteger("0"), os);
        os.write(name.getNameDER());
        os.write(key_enc);
        byte[] tmp = os.toByteArray();
        os.reset();
        DERUtils.encodeSequence(tmp, (OutputStream)os);
        byte[] toBeSigned = os.toByteArray();
        try {
            Signature sig = Signature.getInstance(algorithm);
            sig.initSign(priv);
            sig.update(toBeSigned);
            signature = sig.sign();
        }
        catch (Exception e) {
            throw new InternalError(e.toString());
        }
        os.reset();
        os.write(toBeSigned);
        os.write(algId);
        if (alg.equals("RSA")) {
            signature = CertRequest.fitSignature(signature, pub);
        }
        DERUtils.encodeBitString(signature, os);
        byte[] pkcs10_c = os.toByteArray();
        os.reset();
        DERUtils.encodeSequence(pkcs10_c, (OutputStream)os);
        byte[] pkcs10 = os.toByteArray();
        return pkcs10;
    }

    public static byte[] makeSelfSignedCert(KeyPair p, X509Name name, int lifetime) throws IOException {
        byte[] signature;
        String algorithm;
        byte[] algId;
        PrivateKey priv = p.getPrivate();
        PublicKey pub = p.getPublic();
        String alg = priv.getAlgorithm();
        byte[] DSA_algid = new byte[]{48, 11, 6, 7, 42, -122, 72, -50, 56, 4, 3, 5, 0};
        byte[] RSA_algid = new byte[]{48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 5, 5, 0};
        if (alg.equals("DSA")) {
            algId = DSA_algid;
            algorithm = "DSA";
            pub = new X509DSAPublicKey((DSAPublicKey)pub);
        } else if (alg.equals("RSA")) {
            algId = RSA_algid;
            algorithm = "SHA-1/RSA/PKCS#1";
            pub = new X509RSAPublicKey((CryptixRSAPublicKey)pub);
        } else {
            throw new InternalError("Unknown algorithm " + alg);
        }
        byte[] key_enc = pub.getEncoded();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DERUtils.encodeInteger(new BigInteger("0"), os);
        os.write(algId);
        os.write(name.getNameDER());
        ByteArrayOutputStream os2 = new ByteArrayOutputStream();
        long notBefore = System.currentTimeMillis();
        long increment = lifetime;
        long notAfter = notBefore + increment * 1000L;
        DERUtils.encodeUTCTime(notBefore, os2);
        DERUtils.encodeUTCTime(notAfter, os2);
        DERUtils.encodeSequence(os2, (OutputStream)os);
        os.write(name.getNameDER());
        os.write(key_enc);
        byte[] tmp = os.toByteArray();
        os.reset();
        DERUtils.encodeSequence(tmp, (OutputStream)os);
        byte[] toBeSigned = os.toByteArray();
        try {
            Signature sig = Signature.getInstance(algorithm);
            sig.initSign(priv);
            sig.update(toBeSigned);
            signature = sig.sign();
        }
        catch (Exception e) {
            throw new InternalError(e.toString());
        }
        os.reset();
        os.write(toBeSigned);
        os.write(algId);
        if (alg.equals("RSA")) {
            signature = CertRequest.fitSignature(signature, pub);
        }
        DERUtils.encodeBitString(signature, os);
        byte[] cert_c = os.toByteArray();
        os.reset();
        DERUtils.encodeSequence(cert_c, (OutputStream)os);
        byte[] cert = os.toByteArray();
        return cert;
    }

    public static X509Name makeSimpleDN(Vector rdns) {
        Vector dn = new Vector();
        for (int i = 0; i < rdns.size(); ++i) {
            String[] ava = (String[])rdns.elementAt(i);
            Vector<String[]> nrdn = new Vector<String[]>();
            String[] nava = new String[]{new String(ava[0]), new String(ava[1])};
            nrdn.addElement(nava);
            dn.addElement(nrdn);
        }
        return new X509Name(dn);
    }

    protected static byte[] fitSignature(byte[] tmp, PublicKey pub) {
        CryptixRSAPublicKey rsa = (CryptixRSAPublicKey)pub;
        int bitLength = rsa.getModulus().bitLength();
        int length = bitLength / 8;
        if (tmp.length == (length += bitLength % 8 > 0 ? 1 : 0)) {
            return tmp;
        }
        byte[] ntmp = new byte[length];
        if (tmp.length < length) {
            int i;
            for (i = 0; i < length - tmp.length; ++i) {
                ntmp[i] = 0;
            }
            System.arraycopy(tmp, 0, ntmp, i, tmp.length);
        } else {
            int i;
            for (i = 0; i < tmp.length - length; ++i) {
                if (tmp[i] == 0) continue;
                throw new InternalError("RSA signature error");
            }
            System.arraycopy(tmp, i, ntmp, 0, length);
        }
        return ntmp;
    }

    private static X509Name makeSuperSimpleDN(String CN) {
        Vector<String[]> tdn = new Vector<String[]>();
        String[] c = new String[2];
        String[] o = new String[2];
        String[] ou = new String[2];
        String[] cn = new String[2];
        c[0] = "C";
        c[1] = "US";
        o[0] = "O";
        o[1] = "Snake Oil, Inc.";
        ou[0] = "OU";
        ou[1] = "Test";
        cn[0] = "CN";
        cn[1] = CN;
        tdn.addElement(c);
        tdn.addElement(o);
        tdn.addElement(ou);
        tdn.addElement(cn);
        return CertRequest.makeSimpleDN(tdn);
    }

    public static void main(String[] args) throws IOException, Exception {
        String line;
        byte[] req;
        String keyFileName = args[0];
        String cn = null;
        int type = 0;
        String keyType = args.length >= 2 ? args[1] : "DSA";
        if (args.length >= 3) {
            if (args[2].equals("SPKAC")) {
                type = 0;
            } else if (args[2].equals("PKCS10")) {
                type = 1;
            } else if (args[2].equals("X509")) {
                type = 2;
            } else {
                throw new InternalError("Unknown type " + args[2]);
            }
        }
        if (type == 1 || type == 2) {
            if (args.length != 4) {
                throw new InternalError("Must supply common name for type" + type);
            }
            cn = args[3];
        }
        int size = 1024;
        InputStreamReader ir = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(ir);
        String password = br.readLine();
        FileWriter fw = new FileWriter(keyFileName);
        BufferedWriter bw = new BufferedWriter(fw);
        KeyPair kp = CertRequest.generateKey(keyType, size, password, bw, true);
        switch (type) {
            case 0: {
                req = CertRequest.makeSPKACRequest(kp);
                break;
            }
            case 1: {
                req = CertRequest.makePKCS10Request(kp, CertRequest.makeSuperSimpleDN(cn));
                break;
            }
            case 2: {
                req = CertRequest.makeSelfSignedCert(kp, CertRequest.makeSuperSimpleDN(cn), 30000000);
                break;
            }
            default: {
                throw new InternalError("Bad type");
            }
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Base64OutputStream b64os = new Base64OutputStream(bos);
        b64os.write(req);
        b64os.flush();
        b64os.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        InputStreamReader irr = new InputStreamReader(bis);
        BufferedReader r = new BufferedReader(irr);
        System.out.println(password);
        switch (type) {
            case 1: {
                System.out.println("-----BEGIN CERTIFICATE REQUEST-----");
                break;
            }
            case 2: {
                System.out.println("-----BEGIN CERTIFICATE-----");
            }
        }
        while ((line = r.readLine()) != null) {
            System.out.println(line);
        }
        switch (type) {
            case 1: {
                System.out.println("-----END CERTIFICATE REQUEST-----");
                break;
            }
            case 2: {
                System.out.println("-----END CERTIFICATE-----");
            }
        }
    }

    static {
        LoadProviders.init();
    }
}

