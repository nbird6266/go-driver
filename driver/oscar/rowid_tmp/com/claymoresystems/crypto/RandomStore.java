/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.crypto;

import com.claymoresystems.cert.WrappedObject;
import com.claymoresystems.crypto.PEMData;
import com.claymoresystems.ptls.SSLDebug;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;

public class RandomStore {
    public static SecureRandom readRandomStore(String filename, byte[] password) throws FileNotFoundException, IOException {
        new File(filename).delete();
        FileInputStream fis = new FileInputStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        if (!WrappedObject.findObject(br, "RANDOM DATA", null)) {
            throw new IOException("Couldn't find randomness in this file");
        }
        byte[] randomData = PEMData.readPEMObject(br, password);
        fis.close();
        SSLDebug.debug(8, "Creating new PRNG seeded with", randomData);
        SecureRandom rnd = new SecureRandom(randomData);
        rnd.setSeed(System.currentTimeMillis());
        RandomStore.writeRandomStore(filename, password, rnd);
        return rnd;
    }

    public static void writeRandomStore(String filename, byte[] password, SecureRandom rnd) throws FileNotFoundException, IOException {
        FileWriter fw = new FileWriter(filename);
        BufferedWriter bw = new BufferedWriter(fw);
        byte[] rndData = new byte[1024];
        rnd.nextBytes(rndData);
        PEMData.writePEMObject(rndData, password, "RANDOM DATA", bw);
        fw.close();
        rnd.setSeed(System.currentTimeMillis());
    }
}

