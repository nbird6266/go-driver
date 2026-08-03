/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.cert.EAYDHParams;
import com.claymoresystems.cert.WrappedObject;
import com.claymoresystems.cert.X509Cert;
import com.claymoresystems.crypto.DHPrivateKey;
import com.claymoresystems.crypto.EAYEncryptedPrivateKey;
import com.claymoresystems.crypto.RandomStore;
import com.claymoresystems.ptls.LoadProviders;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLSessionData;
import com.claymoresystems.sslg.SSLContextInt;
import com.oscar.wallet.ParsePkcs12;
import cryptix.provider.rsa.BaseRSAPrivateKey;
import cryptix.provider.rsa.BaseRSAPublicKey;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Vector;
import xjava.security.interfaces.CryptixRSAPrivateKey;
import xjava.security.interfaces.CryptixRSAPublicKey;

public class SSLContext
extends SSLContextInt {
    Vector root_list = new Vector();
    Vector certificates = null;
    PrivateKey privateKey = null;
    PublicKey publicKey = null;
    private static final int SEED_BYTES = 128;
    private Hashtable session_cache = new Hashtable();
    private int ephemeralDHKeyLength = 1024;
    DHPrivateKey dhEphemeral = null;
    EAYDHParams dhParams = null;
    KeyPair rsaEphemeral = null;
    SecureRandom rng = null;
    private boolean sophieGermain = false;

    public void seedRNG(byte[] seed) {
        if (seed == null) {
            seed = new byte[]{};
        }
        if (this.rng == null) {
            this.rng = new SecureRandom(seed);
        } else {
            this.rng.setSeed(seed);
        }
        this.rng.setSeed(System.currentTimeMillis());
    }

    public void useRandomnessFile(String file, String passphrase) throws IOException, FileNotFoundException {
        this.rng = null;
        try {
            new File(file).delete();
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (this.rng == null) {
            this.rng = new SecureRandom();
            RandomStore.writeRandomStore(file, passphrase.getBytes(), this.rng);
        }
    }

    public void loadPKCS12File(String path, String passphrase) throws IOException {
        ParsePkcs12 pkcs12 = null;
        try {
            pkcs12 = new ParsePkcs12(path, passphrase);
        }
        catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
        catch (Error er) {
            throw new IOException(er.getMessage());
        }
        ByteArrayInputStream casbi = new ByteArrayInputStream(pkcs12.calist);
        this.loadRootCertificates(casbi);
        casbi.close();
        ByteArrayInputStream keybi = new ByteArrayInputStream(pkcs12.privatekey);
        this.LoadKeyFile(keybi, "");
        ByteArrayInputStream certsbi = new ByteArrayInputStream(pkcs12.usercerts);
        this.LoadCertFile(certsbi, "");
    }

    public void loadEAYKeyFile(String path, String passphrase) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(path);
        this.loadEAYKeyFile(fis, passphrase);
    }

    public void loadEAYKeyFile(String path, String cert, String passphrase) throws FileNotFoundException, IOException {
        FileInputStream keyfile = new FileInputStream(path);
        FileInputStream certfile = new FileInputStream(cert);
        this.LoadKeyFile(keyfile, passphrase);
        this.LoadCertFile(certfile, passphrase);
    }

    public void LoadKeyFile(InputStream is, String passphrase) throws IOException {
        PrivateKey tmpPrivateKey;
        int r;
        byte[] blk = new byte[1024];
        ByteArrayOutputStream tos = new ByteArrayOutputStream();
        while ((r = is.read(blk)) > 0) {
            tos.write(blk, 0, r);
        }
        byte[] tmp = tos.toByteArray();
        ByteArrayInputStream tis = new ByteArrayInputStream(tmp);
        BufferedReader br = new BufferedReader(new InputStreamReader(tis));
        StringBuffer keyType = new StringBuffer();
        SSLDebug.debug(16, "Loading key file");
        if (!WrappedObject.findObject(br, "PRIVATE KEY", keyType)) {
            throw new IOException("Couldn't find private key in this file");
        }
        try {
            tmpPrivateKey = EAYEncryptedPrivateKey.createPrivateKey(br, keyType.toString(), passphrase.getBytes());
        }
        catch (IllegalArgumentException e) {
            throw new IOException(e.toString());
        }
        this.privateKey = tmpPrivateKey;
    }

    public void LoadCertFile(InputStream is, String passphras) throws IOException {
        Object cert;
        int r;
        byte[] blk = new byte[1024];
        ByteArrayOutputStream tos = new ByteArrayOutputStream();
        while ((r = is.read(blk)) > 0) {
            tos.write(blk, 0, r);
        }
        byte[] tmp = tos.toByteArray();
        ByteArrayInputStream tis = new ByteArrayInputStream(tmp);
        BufferedReader br = new BufferedReader(new InputStreamReader(tis));
        Vector<byte[]> certs = new Vector<byte[]>();
        while ((cert = WrappedObject.loadObject(br, "CERTIFICATE", null)) != null) {
            SSLDebug.debug(16, "Loading certificate", (byte[])cert);
            certs.add((byte[])cert);
        }
        if (certs.size() == 0) {
            throw new IOException("Need at least one certificate");
        }
        cert = new X509Cert((byte[])certs.elementAt(0));
        this.publicKey = ((X509Cert)cert).getPublicKey();
        this.certificates = certs;
    }

    public void loadEAYKeyFile(InputStream is, String passphrase) throws IOException {
        Object cert;
        PrivateKey tmpPrivateKey;
        int r;
        byte[] blk = new byte[1024];
        ByteArrayOutputStream tos = new ByteArrayOutputStream();
        while ((r = is.read(blk)) > 0) {
            tos.write(blk, 0, r);
        }
        byte[] tmp = tos.toByteArray();
        ByteArrayInputStream tis = new ByteArrayInputStream(tmp);
        BufferedReader br = new BufferedReader(new InputStreamReader(tis));
        StringBuffer keyType = new StringBuffer();
        SSLDebug.debug(16, "Loading key file");
        if (!WrappedObject.findObject(br, "PRIVATE KEY", keyType)) {
            throw new IOException("Couldn't find private key in this file");
        }
        try {
            tmpPrivateKey = EAYEncryptedPrivateKey.createPrivateKey(br, keyType.toString(), passphrase.getBytes());
        }
        catch (IllegalArgumentException e) {
            throw new IOException(e.toString());
        }
        tis = new ByteArrayInputStream(tmp);
        br = new BufferedReader(new InputStreamReader(tis));
        Vector<byte[]> certs = new Vector<byte[]>();
        while ((cert = WrappedObject.loadObject(br, "CERTIFICATE", null)) != null) {
            SSLDebug.debug(16, "Loading certificate", (byte[])cert);
            certs.insertElementAt((byte[])cert, 0);
        }
        if (certs.size() == 0) {
            throw new IOException("Need at least one certificate");
        }
        cert = new X509Cert((byte[])certs.elementAt(0));
        this.publicKey = ((X509Cert)cert).getPublicKey();
        this.privateKey = tmpPrivateKey;
        this.certificates = certs;
    }

    public void saveEAYKeyFile(String path, String passphrase) throws IOException, FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(path);
        OutputStreamWriter fw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(fw);
        EAYEncryptedPrivateKey.writePrivateKey(this.privateKey, passphrase.getBytes(), bw);
        for (int i = 1; i <= this.certificates.size(); ++i) {
            byte[] cert = (byte[])this.certificates.elementAt(this.certificates.size() - i);
            WrappedObject.writeHeader("CERTIFICATE", bw);
            WrappedObject.writeObject(cert, "CERTIFICATE", bw);
        }
        bw.flush();
        fos.close();
    }

    public void loadRootCertificates(String path) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(path);
        this.loadRootCertificates(fis);
        fis.close();
    }

    public void loadRootCertificates(InputStream is) throws IOException {
        byte[] root;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((root = WrappedObject.loadObject(br, "CERTIFICATE", null)) != null) {
            SSLDebug.debug(16, "Loading root", root);
            this.root_list.addElement(root);
        }
    }

    public void loadDHParams(String path) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(path);
        this.loadDHParams(fis);
        fis.close();
    }

    public void loadDHParams(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        byte[] dh = WrappedObject.loadObject(br, "DH PARAMETERS", null);
        if (dh == null) {
            return;
        }
        SSLDebug.debug(16, "Loading DH params", dh);
        this.dhParams = new EAYDHParams(dh);
    }

    public void saveDHParams(String path, int size, boolean sophieGermainPrimes) throws IOException, FileNotFoundException {
        DHPrivateKey eph = this.getEphemeralDHPrivateKey(size, sophieGermainPrimes, true);
        EAYDHParams par = new EAYDHParams(eph.getg(), eph.getp());
        byte[] dhEnc = par.getEncoded();
        FileOutputStream fos = new FileOutputStream(path);
        OutputStreamWriter fw = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(fw);
        WrappedObject.writeHeader("DH PARAMETERS", bw);
        WrappedObject.writeObject(dhEnc, "DH PARAMETERS", bw);
        bw.flush();
        fw.flush();
        fos.close();
    }

    Vector getRootList() {
        return this.root_list;
    }

    Vector getCertificateChain() {
        return this.certificates;
    }

    PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    PublicKey getPublicKey() {
        return this.publicKey;
    }

    DHPrivateKey getEphemeralDHPrivateKey(boolean alwaysgenerate) throws IOException {
        if (this.dhParams == null) {
            throw new IOException("Must install DH parameters");
        }
        return this.getEphemeralDHPrivateKey(this.ephemeralDHKeyLength, this.sophieGermain, alwaysgenerate);
    }

    private synchronized DHPrivateKey getEphemeralDHPrivateKey(int size, boolean strongp, boolean alwaysgenerate) {
        this.seedRNG();
        if (this.dhEphemeral == null || alwaysgenerate) {
            this.dhEphemeral = DHPrivateKey.getInstance();
            if (this.dhParams == null) {
                throw new InternalError("Can't generate ephemeral key without setting DH params");
            }
            this.dhEphemeral.initPrivateKey(this.dhParams.getG(), this.dhParams.getP(), this.rng);
        }
        return this.dhEphemeral;
    }

    private synchronized KeyPair getEphemeralRSAPair() {
        this.seedRNG();
        if (this.rsaEphemeral == null) {
            try {
                KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
                kg.initialize(512, this.rng);
                this.rsaEphemeral = kg.generateKeyPair();
            }
            catch (Exception e) {
                throw new InternalError(e.toString());
            }
        }
        return this.rsaEphemeral;
    }

    synchronized CryptixRSAPrivateKey getEphemeralRSAPrivateKey() {
        return (CryptixRSAPrivateKey)this.getEphemeralRSAPair().getPrivate();
    }

    synchronized CryptixRSAPublicKey getEphemeralRSAPublicKey() {
        return (CryptixRSAPublicKey)this.getEphemeralRSAPair().getPublic();
    }

    synchronized byte[] getSeedBytes() {
        byte[] buf = new byte[128];
        this.seedRNG();
        this.rng.nextBytes(buf);
        return buf;
    }

    protected synchronized void storeSession(String key, SSLSessionData sd) {
        SSLDebug.debug(4, "Storing session under key" + key);
        this.session_cache.put(key, sd);
    }

    protected synchronized SSLSessionData findSession(String key) {
        SSLDebug.debug(4, "Trying to recover session using key" + key);
        Object obj = this.session_cache.get(key);
        if (obj == null) {
            return null;
        }
        return (SSLSessionData)obj;
    }

    protected synchronized void destroySession(String sessionLookupKey) {
        SSLDebug.debug(4, "Destroying session" + sessionLookupKey);
        this.session_cache.remove(sessionLookupKey);
    }

    private void seedRNG() {
        if (this.rng != null) {
            return;
        }
        this.rng = new SecureRandom();
    }

    public int checkKeyPair() {
        int result = 1;
        if (!this.privateKey.getAlgorithm().equalsIgnoreCase("rsa")) {
            return 0;
        }
        BaseRSAPrivateKey rsaPrivateKey = (BaseRSAPrivateKey)this.privateKey;
        BaseRSAPublicKey rsaPublicKey = (BaseRSAPublicKey)this.publicKey;
        BigInteger nPrivate = rsaPrivateKey.getModulus();
        BigInteger dPrivate = rsaPrivateKey.getExponent();
        BigInteger PPrivate = rsaPrivateKey.getP();
        BigInteger QPrivate = rsaPrivateKey.getQ();
        BigInteger nPublic = rsaPublicKey.getModulus();
        BigInteger ePublic = rsaPublicKey.getExponent();
        BigInteger Psub1 = PPrivate.subtract(BigInteger.ONE);
        BigInteger Qsub1 = QPrivate.subtract(BigInteger.ONE);
        BigInteger ed = ePublic.multiply(dPrivate);
        BigInteger phi = Psub1.multiply(Qsub1);
        BigInteger edModphi = ed.mod(phi);
        if (!rsaPrivateKey.getAlgorithm().equalsIgnoreCase(rsaPublicKey.getAlgorithm())) {
            result = 2;
            return result;
        }
        if (nPrivate.compareTo(nPublic) != 0 || edModphi.compareTo(BigInteger.ONE) != 0) {
            result = 1;
            return result;
        }
        result = 0;
        return result;
    }

    static {
        LoadProviders.init();
    }
}

