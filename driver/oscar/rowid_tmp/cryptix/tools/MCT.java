/*
 * Decompiled with CFR 0.152.
 */
package cryptix.tools;

import cryptix.util.core.ArrayUtil;
import cryptix.util.core.Hex;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.StringTokenizer;
import xjava.security.Cipher;
import xjava.security.SecretKey;

public final class MCT {
    static final String VERSION = "$Revision: 1.5 $";
    static final String SUBMITTER = "<as stated on the submission cover sheet>";
    boolean ecb = false;
    boolean cbc = false;
    String dirName = null;
    String keylengths = null;
    String provider = null;
    String cipherName = null;
    File destination = null;
    int[] keys = new int[]{128, 192, 256};
    final String eeFileName = "ecb_e_m.txt";
    final String edFileName = "ecb_d_m.txt";
    final String ceFileName = "cbc_e_m.txt";
    final String cdFileName = "cbc_d_m.txt";
    long encBlocks;
    long decBlocks;
    long keyCount;
    Class algorithm = null;
    Method blockSize = null;
    Method makeKey = null;
    Method encrypt = null;
    Method decrypt = null;
    Cipher cipher = null;
    boolean useReflection = true;

    public static void main(String[] args) {
        System.out.println("NIST Monte-Carlo Tests data generator/exerciser\n\n$Revision: 1.5 $\nCopyright (c) 1998 Systemics Ltd. on behalf of\nthe Cryptix Development Team.  All rights reserved.\n\n");
        MCT cmd = new MCT();
        cmd.processOptions(args);
        cmd.run();
    }

    void processOptions(String[] args) {
        int argc = args.length;
        if (argc == 0) {
            this.printUsage();
        }
        System.out.println("(type \"java cryptix.tools.MCT\" with no arguments for help)\n\n");
        int i = -1;
        String cmd = "";
        boolean next = true;
        while (true) {
            if (next) {
                if (++i >= argc) break;
                cmd = args[i];
            } else {
                cmd = "-" + cmd.substring(2);
            }
            if (cmd.startsWith("-e")) {
                this.ecb = true;
                next = cmd.length() == 2;
                continue;
            }
            if (cmd.startsWith("-c")) {
                this.cbc = true;
                next = cmd.length() == 2;
                continue;
            }
            if (cmd.startsWith("-l")) {
                this.keylengths = args[i + 1];
                ++i;
                next = true;
                continue;
            }
            if (cmd.startsWith("-d")) {
                this.dirName = args[i + 1];
                ++i;
                next = true;
                continue;
            }
            if (cmd.startsWith("-p")) {
                this.provider = args[i + 1];
                ++i;
                next = true;
                continue;
            }
            this.cipherName = cmd;
        }
        if (this.cipherName == null) {
            MCT.halt("Missing cipher algorithm name");
        }
        if (this.cipherName.length() > 1 && (this.cipherName.startsWith("\"") || this.cipherName.startsWith("'"))) {
            this.cipherName = this.cipherName.substring(2, this.cipherName.length() - 2);
        }
        if (this.provider == null) {
            this.provider = this.cipherName;
        }
        if (this.keylengths != null) {
            int count = 0;
            int[] keystemp = new int[3];
            StringTokenizer st = new StringTokenizer(this.keylengths, ", \t\"");
            while (st.hasMoreTokens()) {
                int k = Integer.parseInt(st.nextToken());
                if (k <= 0) {
                    MCT.halt("Negative key length not allowed: " + k);
                }
                if (count == 3) {
                    MCT.halt("Only three key-length values are allowed.");
                }
                keystemp[count++] = k;
            }
            if (count != 0) {
                this.keys = new int[count];
                System.arraycopy(keystemp, 0, this.keys, 0, count);
            }
        }
        if (!this.ecb && !this.cbc) {
            this.cbc = true;
            this.ecb = true;
        }
        if (this.dirName == null) {
            this.dirName = System.getProperty("user.dir");
        }
        this.destination = new File(this.dirName);
        if (!this.destination.isDirectory()) {
            MCT.halt("Destination <" + this.destination.getName() + "> is not a directory");
        }
        try {
            this.algorithm = Class.forName(String.valueOf(this.provider) + "." + this.cipherName + "_Algorithm");
            Method[] methods = this.algorithm.getDeclaredMethods();
            i = 0;
            while (i < methods.length) {
                String name = methods[i].getName();
                int params = methods[i].getParameterTypes().length;
                if (name.equals("blockSize")) {
                    this.blockSize = methods[i];
                } else if (name.equals("makeKey") && params == 1) {
                    this.makeKey = methods[i];
                } else if (name.equals("blockEncrypt") && params == 3) {
                    this.encrypt = methods[i];
                } else if (name.equals("blockDecrypt") && params == 3) {
                    this.decrypt = methods[i];
                }
                ++i;
            }
            if (this.blockSize == null) {
                throw new NoSuchMethodException("blockSize()");
            }
            if (this.makeKey == null) {
                throw new NoSuchMethodException("makeKey()");
            }
            if (this.encrypt == null) {
                throw new NoSuchMethodException("blockEncrypt()");
            }
            if (this.decrypt == null) {
                throw new NoSuchMethodException("blockDecrypt()");
            }
        }
        catch (ClassNotFoundException ex1) {
            MCT.notify("Unable to find a " + this.cipherName + "_Algorithm class");
            this.algorithm = null;
        }
        catch (NoSuchMethodException ex2) {
            MCT.notify("Unable to find method " + ex2.getMessage() + " in " + this.cipherName + "_Algorithm class");
            this.algorithm = null;
        }
        try {
            this.cipher = Cipher.getInstance(String.valueOf(this.cipherName) + "/ECB", this.provider);
        }
        catch (NoSuchProviderException ex3) {
            MCT.halt("Unable to locate Security Provider: " + this.provider);
        }
        catch (NoSuchAlgorithmException ex4) {
            MCT.halt("Unable to locate an implementation for Cipher: " + this.cipherName + "/ECB");
        }
        this.useReflection = this.algorithm != null;
    }

    static void halt(String s) {
        System.err.println("\n*** " + s + "...");
        System.exit(-1);
    }

    static void notify(String s) {
        System.out.println("MCT: " + s + "...");
    }

    void printUsage() {
        System.out.println("NAME\n  MCT: A Monte Carlo Tests data generator/exerciser for any block\n  cipher algorithm.\n\nSYNTAX\n  java cryptix.tools.MCT\n    [ -e | -c ]\n    [ -l <comma-separated-key-lengths>]\n    [ -d <output-directory>]\n    [ -p <provider>]\n    <cipher>\n\nDESCRIPTION\n  For a designated symmetric block cipher algorithm, this command\n  generates and exercises Monte Carlo Tests data for both Encryption\n  and Decryption in Electronic Codebook (ECB) and Cipher Block Chaining\n  (CBC) modes.\n  MCT's output file format is in conformance with the layout described\n  in Section 4 of NIST's document \"Description of Known Answer Tests\n  and Monte Carlo Tests for Advanced Encryption Standard (AES) Candidate\n  Algorithm Submissions\" dated January 7, 1998.\n\nOPTIONS\n  -e   Generate both Encryption and Decryption data for the cipher in\n       ECB mode only.  By default MCT generates both ECB and CBC test\n       suites.\n\n  -c   Generate both Encryption and Decryption data for the cipher in\n       CBC mode only.  By default MCT generates both ECB and CBC test\n       suites.\n\n  -l <comma-separated-key-lengths>\n       Comma separated list (maximum of three) of key lengths to use\n       for the tests.  If omitted, the following three values are\n       assumed: 128, 192 and 256.\n\n  -d <output-directory>\n       Pathname of the directory where the output files: \"ecb_e_m.txt\",\n       \"ecb_d_m.txt\", \"cbc_e_m.txt\" and \"cbc_d_m.txt\" will be generated.\n       If this destination directory is not specified, those files will\n       be placed in the current user directory.\n\n  -p <provider>\n       Name of the Security Provider for the designated algorithm.\n       If omitted, then assumes provider has the same name as the\n       algorithm itself.\n\n  <cipher>\n       Cipher algorithm name.\n\nCOPYRIGHT\n  Copyright (c) 1998 Systemics Ltd. on behalf of\n  the Cryptix Development Team.  All rights reserved.\n");
        System.exit(0);
    }

    void run() {
        long time = System.currentTimeMillis();
        try {
            if (this.ecb) {
                this.ecbMCT("ecb_e_m.txt", "ecb_d_m.txt");
            }
            if (this.cbc) {
                this.cbcMCT("cbc_e_m.txt", "cbc_d_m.txt");
            }
        }
        catch (KeyException ex1) {
            ex1.printStackTrace();
            MCT.halt("Key Exception encountered\n" + ex1.getMessage());
        }
        MCT.notify("Java interpreter used: Version " + System.getProperty("java.version"));
        MCT.notify("Java Just-In-Time (JIT) compiler: " + System.getProperty("java.compiler"));
        MCT.notify("Total execution time (ms): " + (System.currentTimeMillis() - time));
        MCT.notify("During this time, " + this.cipherName + ":");
        MCT.notify("  Encrypted " + this.encBlocks + " blocks");
        MCT.notify("  Decrypted " + this.decBlocks + " blocks");
        MCT.notify("  Created " + this.keyCount + " session keys");
    }

    void ecbMCT(String encName, String decName) throws KeyException {
        PrintWriter enc = null;
        File f1 = new File(this.destination, encName);
        try {
            enc = new PrintWriter(new FileWriter(f1), true);
        }
        catch (IOException ex3) {
            MCT.halt("Unable to initialize <" + encName + "> as a Writer:\n" + ex3.getMessage());
        }
        PrintWriter dec = null;
        File f2 = new File(this.destination, decName);
        try {
            dec = new PrintWriter(new FileWriter(f2), true);
        }
        catch (IOException ex4) {
            MCT.halt("Unable to initialize <" + decName + "> as a Writer:\n" + ex4.getMessage());
        }
        enc.println();
        enc.println("=========================");
        enc.println();
        enc.println("FILENAME:  \"" + encName + "\"");
        enc.println();
        enc.println("Electronic Codebook (ECB) Mode - ENCRYPTION");
        enc.println("Monte Carlo Test");
        enc.println();
        enc.println("Algorithm Name: " + this.cipherName);
        enc.println("Principal Submitter: <as stated on the submission cover sheet>");
        enc.println();
        dec.println();
        dec.println("=========================");
        dec.println();
        dec.println("FILENAME:  \"" + decName + "\"");
        dec.println();
        dec.println("Electronic Codebook (ECB) Mode - DECRYPTION");
        dec.println("Monte Carlo Test");
        dec.println();
        dec.println("Algorithm Name: " + this.cipherName);
        dec.println("Principal Submitter: <as stated on the submission cover sheet>");
        dec.println();
        boolean useIJCE = true;
        if (this.useReflection) {
            try {
                int k = 0;
                while (k < this.keys.length) {
                    this.ecbForKeyReflect(this.keys[k], enc, dec);
                    ++k;
                }
                useIJCE = false;
            }
            catch (IllegalAccessException ex1) {
                MCT.notify("Exception while invoking a method in " + this.cipherName + "_Algorithm class");
            }
            catch (InvocationTargetException ex2) {
                MCT.halt("Exception encountered in a " + this.cipherName + "_Algorithm method:\n" + ex2.getMessage());
                useIJCE = false;
            }
        }
        if (useIJCE) {
            int k = 0;
            while (k < this.keys.length) {
                this.ecbForKeyIjce(this.keys[k], enc, dec);
                ++k;
            }
        }
        enc.println("==========");
        dec.println("==========");
        enc.close();
        dec.close();
    }

    void ecbForKeyReflect(int keysize, PrintWriter enc, PrintWriter dec) throws IllegalAccessException, InvocationTargetException {
        MCT.notify("Processing MCT in ECB mode (long); key size: " + keysize);
        MCT.notify("Using Reflection API methods");
        enc.println("==========");
        enc.println();
        enc.println("KEYSIZE=" + keysize);
        enc.println();
        dec.println("==========");
        dec.println();
        dec.println("KEYSIZE=" + keysize);
        dec.println();
        Object[] args = new Object[]{};
        int keylen = keysize / 8;
        byte[] keyMaterial = new byte[keylen];
        int size = (Integer)this.blockSize.invoke(null, args);
        byte[] pt = new byte[size];
        int i = 0;
        while (i < 400) {
            int k;
            String ks = Hex.toString(keyMaterial);
            args = new Object[]{keyMaterial};
            Object skeys = this.makeKey.invoke(null, args);
            ++this.keyCount;
            enc.println("I=" + i);
            enc.println("KEY=" + ks);
            enc.println("PT=" + Hex.toString(pt));
            args = new Object[]{pt, new Integer(0), skeys};
            byte[] ct_1 = (byte[])this.encrypt.invoke(null, args);
            int j = 1;
            while (j < 9999) {
                args[0] = ct_1;
                ct_1 = (byte[])this.encrypt.invoke(null, args);
                ++this.encBlocks;
                ++j;
            }
            args[0] = ct_1;
            byte[] ct = (byte[])this.encrypt.invoke(null, args);
            ++this.encBlocks;
            String cts = Hex.toString(ct);
            enc.println("CT=" + cts);
            dec.println("I=" + i);
            dec.println("KEY=" + ks);
            dec.println("CT=" + cts);
            args[0] = ct;
            byte[] cpt = (byte[])this.decrypt.invoke(null, args);
            ++this.decBlocks;
            j = 1;
            while (j < 10000) {
                args[0] = cpt;
                cpt = (byte[])this.decrypt.invoke(null, args);
                ++this.decBlocks;
                ++j;
            }
            dec.println("PT=" + Hex.toString(cpt));
            if (!ArrayUtil.areEqual(pt, cpt)) {
                enc.println(" *** ERROR ***");
                dec.println(" *** ERROR ***");
                MCT.halt("ECB Encryption/Decryption mismatch");
            }
            enc.println();
            dec.println();
            j = 0;
            if (keylen > size) {
                int count = keylen - size;
                k = size - count;
                while (j < count) {
                    int n = j++;
                    keyMaterial[n] = (byte)(keyMaterial[n] ^ ct_1[k++]);
                }
            }
            k = 0;
            while (j < keylen) {
                int n = j++;
                keyMaterial[n] = (byte)(keyMaterial[n] ^ ct[k++]);
            }
            System.arraycopy(ct, 0, pt, 0, size);
            ++i;
        }
    }

    void ecbForKeyIjce(int keysize, PrintWriter enc, PrintWriter dec) throws KeyException {
        MCT.notify("Processing MCT in ECB mode (long); key size: " + keysize);
        MCT.notify("Using IJCE API methods");
        enc.println("==========");
        enc.println();
        enc.println("KEYSIZE=" + keysize);
        enc.println();
        dec.println("==========");
        dec.println();
        dec.println("KEYSIZE=" + keysize);
        dec.println();
        int keylen = keysize / 8;
        byte[] keyMaterial = new byte[keylen];
        int size = this.cipher.blockSize();
        byte[] pt = new byte[size];
        int i = 0;
        while (i < 400) {
            int k;
            String ks = Hex.toString(keyMaterial);
            MCT_Key key = new MCT_Key(keyMaterial);
            enc.println("I=" + i);
            enc.println("KEY=" + ks);
            enc.println("PT=" + Hex.toString(pt));
            this.cipher.initEncrypt(key);
            ++this.keyCount;
            byte[] ct_1 = this.cipher.crypt(pt);
            ++this.encBlocks;
            int j = 1;
            while (j < 9999) {
                ct_1 = this.cipher.crypt(ct_1);
                ++this.encBlocks;
                ++j;
            }
            byte[] ct = this.cipher.crypt(ct_1);
            ++this.encBlocks;
            String cts = Hex.toString(ct);
            enc.println("CT=" + cts);
            dec.println("I=" + i);
            dec.println("KEY=" + ks);
            dec.println("CT=" + cts);
            this.cipher.initDecrypt(key);
            ++this.keyCount;
            byte[] cpt = this.cipher.crypt(ct);
            ++this.decBlocks;
            j = 1;
            while (j < 10000) {
                cpt = this.cipher.crypt(cpt);
                ++this.decBlocks;
                ++j;
            }
            dec.println("PT=" + Hex.toString(cpt));
            if (!ArrayUtil.areEqual(pt, cpt)) {
                enc.println(" *** ERROR ***");
                dec.println(" *** ERROR ***");
                MCT.halt("ECB Encryption/Decryption mismatch");
            }
            enc.println();
            dec.println();
            j = 0;
            if (keylen > size) {
                int count = keylen - size;
                k = size - count;
                while (j < count) {
                    int n = j++;
                    keyMaterial[n] = (byte)(keyMaterial[n] ^ ct_1[k++]);
                }
            }
            k = 0;
            while (j < keylen) {
                int n = j++;
                keyMaterial[n] = (byte)(keyMaterial[n] ^ ct[k++]);
            }
            System.arraycopy(ct, 0, pt, 0, size);
            ++i;
        }
    }

    void cbcMCT(String encName, String decName) throws KeyException {
        this.cbcEncrypt(encName);
        this.cbcDecrypt(decName);
    }

    void cbcEncrypt(String encName) throws KeyException {
        PrintWriter pw = null;
        File f = new File(this.destination, encName);
        try {
            pw = new PrintWriter(new FileWriter(f), true);
        }
        catch (IOException ex1) {
            MCT.halt("Unable to initialize <" + encName + "> as a Writer:\n" + ex1.getMessage());
        }
        pw.println();
        pw.println("=========================");
        pw.println();
        pw.println("FILENAME:  \"" + encName + "\"");
        pw.println();
        pw.println("Cipher Block Chaining (CBC) Mode - ENCRYPTION");
        pw.println("Monte Carlo Test");
        pw.println();
        pw.println("Algorithm Name: " + this.cipherName);
        pw.println("Principal Submitter: <as stated on the submission cover sheet>");
        pw.println();
        boolean useIJCE = true;
        if (this.useReflection) {
            try {
                int k = 0;
                while (k < this.keys.length) {
                    this.cbcEncForKeyReflect(this.keys[k], pw);
                    ++k;
                }
                useIJCE = false;
            }
            catch (IllegalAccessException ex1) {
                MCT.notify("Exception while invoking a method in " + this.cipherName + "_Algorithm class");
            }
            catch (InvocationTargetException ex2) {
                MCT.halt("Exception encountered in a " + this.cipherName + "_Algorithm method:\n" + ex2.getMessage());
                useIJCE = false;
            }
        }
        if (useIJCE) {
            int k = 0;
            while (k < this.keys.length) {
                this.cbcEncForKeyIjce(this.keys[k], pw);
                ++k;
            }
        }
        pw.println("==========");
        pw.close();
    }

    void cbcEncForKeyReflect(int keysize, PrintWriter pw) throws IllegalAccessException, InvocationTargetException {
        MCT.notify("Processing MCT in CBC-Encrypt mode (long); key size: " + keysize);
        MCT.notify("Using Reflection API methods");
        pw.println("==========");
        pw.println();
        pw.println("KEYSIZE=" + keysize);
        pw.println();
        Object[] args = new Object[]{};
        int keylen = keysize / 8;
        byte[] keyMaterial = new byte[keylen];
        int size = (Integer)this.blockSize.invoke(null, args);
        byte[] pt = new byte[size];
        byte[] ct = new byte[size];
        byte[] iv = new byte[size];
        System.arraycopy(iv, 0, ct, 0, size);
        int i = 0;
        while (i < 400) {
            int k;
            pw.println("I=" + i);
            pw.println("KEY=" + Hex.toString(keyMaterial));
            pw.println("IV=" + Hex.toString(iv));
            pw.println("PT=" + Hex.toString(pt));
            args = new Object[]{keyMaterial};
            Object skeys = this.makeKey.invoke(null, args);
            ++this.keyCount;
            args = new Object[3];
            args[1] = new Integer(0);
            args[2] = skeys;
            int j = 0;
            while (j < 10000) {
                k = 0;
                while (k < size) {
                    int n = k;
                    iv[n] = (byte)(iv[n] ^ pt[k]);
                    ++k;
                }
                System.arraycopy(ct, 0, pt, 0, size);
                args[0] = iv;
                ct = (byte[])this.encrypt.invoke(null, args);
                ++this.encBlocks;
                System.arraycopy(ct, 0, iv, 0, size);
                ++j;
            }
            pw.println("CT=" + Hex.toString(ct));
            pw.println();
            j = 0;
            if (keylen > size) {
                int count = keylen - size;
                k = size - count;
                while (j < count) {
                    int n = j++;
                    keyMaterial[n] = (byte)(keyMaterial[n] ^ pt[k++]);
                }
            }
            k = 0;
            while (j < keylen) {
                int n = j++;
                keyMaterial[n] = (byte)(keyMaterial[n] ^ ct[k++]);
            }
            ++i;
        }
    }

    void cbcEncForKeyIjce(int keysize, PrintWriter pw) throws KeyException {
        MCT.notify("Processing MCT in CBC-Encrypt mode (long); key size: " + keysize);
        MCT.notify("Using IJCE API methods");
        pw.println("==========");
        pw.println();
        pw.println("KEYSIZE=" + keysize);
        pw.println();
        int keylen = keysize / 8;
        byte[] keyMaterial = new byte[keylen];
        int size = this.cipher.blockSize();
        byte[] pt = new byte[size];
        byte[] ct = new byte[size];
        byte[] iv = new byte[size];
        System.arraycopy(iv, 0, ct, 0, size);
        int i = 0;
        while (i < 400) {
            int k;
            pw.println("I=" + i);
            pw.println("KEY=" + Hex.toString(keyMaterial));
            pw.println("IV=" + Hex.toString(iv));
            pw.println("PT=" + Hex.toString(pt));
            MCT_Key key = new MCT_Key(keyMaterial);
            this.cipher.initEncrypt(key);
            ++this.keyCount;
            int j = 0;
            while (j < 10000) {
                k = 0;
                while (k < size) {
                    int n = k;
                    iv[n] = (byte)(iv[n] ^ pt[k]);
                    ++k;
                }
                System.arraycopy(ct, 0, pt, 0, size);
                ct = this.cipher.crypt(iv);
                ++this.encBlocks;
                System.arraycopy(ct, 0, iv, 0, size);
                ++j;
            }
            pw.println("CT=" + Hex.toString(ct));
            pw.println();
            j = 0;
            if (keylen > size) {
                int count = keylen - size;
                k = size - count;
                while (j < count) {
                    int n = j++;
                    keyMaterial[n] = (byte)(keyMaterial[n] ^ pt[k++]);
                }
            }
            k = 0;
            while (j < keylen) {
                int n = j++;
                keyMaterial[n] = (byte)(keyMaterial[n] ^ ct[k++]);
            }
            ++i;
        }
    }

    void cbcDecrypt(String decName) throws KeyException {
        PrintWriter pw = null;
        File f = new File(this.destination, decName);
        try {
            pw = new PrintWriter(new FileWriter(f), true);
        }
        catch (IOException ex1) {
            MCT.halt("Unable to initialize <" + decName + "> as a Writer:\n" + ex1.getMessage());
        }
        pw.println();
        pw.println("=========================");
        pw.println();
        pw.println("FILENAME:  \"" + decName + "\"");
        pw.println();
        pw.println("Cipher Block Chaining (CBC) Mode - DECRYPTION");
        pw.println("Monte Carlo Test");
        pw.println();
        pw.println("Algorithm Name: " + this.cipherName);
        pw.println("Principal Submitter: <as stated on the submission cover sheet>");
        pw.println();
        boolean useIJCE = false;
        if (this.useReflection) {
            try {
                int k = 128;
                while (k < 257) {
                    this.cbcDecForKeyReflect(k, pw);
                    k += 64;
                }
            }
            catch (IllegalAccessException ex1) {
                MCT.notify("Exception while invoking a method in " + this.cipherName + "_Algorithm class");
                useIJCE = true;
            }
            catch (InvocationTargetException ex2) {
                MCT.halt("Exception encountered in a " + this.cipherName + "_Algorithm method:\n" + ex2.getMessage());
            }
        }
        if (useIJCE) {
            int k = 128;
            while (k < 257) {
                this.cbcDecForKeyIjce(k, pw);
                k += 64;
            }
        }
        pw.println("==========");
        pw.close();
    }

    void cbcDecForKeyReflect(int keysize, PrintWriter pw) throws IllegalAccessException, InvocationTargetException {
        MCT.notify("Processing MCT in CBC-Decrypt mode (long); key size: " + keysize);
        MCT.notify("Using Reflection API methods");
        pw.println("==========");
        pw.println();
        pw.println("KEYSIZE=" + keysize);
        pw.println();
        Object[] args = new Object[]{};
        int keylen = keysize / 8;
        byte[] keyMaterial = new byte[keylen];
        int size = (Integer)this.blockSize.invoke(null, args);
        byte[] pt = new byte[size];
        byte[] ct = new byte[size];
        byte[] iv = new byte[size];
        int i = 0;
        while (i < 400) {
            int k;
            pw.println("I=" + i);
            pw.println("KEY=" + Hex.toString(keyMaterial));
            pw.println("IV=" + Hex.toString(iv));
            pw.println("CT=" + Hex.toString(ct));
            args = new Object[]{keyMaterial};
            Object skeys = this.makeKey.invoke(null, args);
            ++this.keyCount;
            args = new Object[3];
            args[1] = new Integer(0);
            args[2] = skeys;
            int j = 0;
            while (j < 10000) {
                args[0] = ct;
                pt = (byte[])this.decrypt.invoke(null, args);
                ++this.decBlocks;
                k = 0;
                while (k < size) {
                    int n = k;
                    pt[n] = (byte)(pt[n] ^ iv[k]);
                    ++k;
                }
                System.arraycopy(ct, 0, iv, 0, size);
                System.arraycopy(pt, 0, ct, 0, size);
                ++j;
            }
            pw.println("PT=" + Hex.toString(pt));
            pw.println();
            j = 0;
            if (keylen > size) {
                int count = keylen - size;
                k = size - count;
                while (j < count) {
                    int n = j++;
                    keyMaterial[n] = (byte)(keyMaterial[n] ^ iv[k++]);
                }
            }
            k = 0;
            while (j < keylen) {
                int n = j++;
                keyMaterial[n] = (byte)(keyMaterial[n] ^ pt[k++]);
            }
            ++i;
        }
    }

    void cbcDecForKeyIjce(int keysize, PrintWriter pw) throws KeyException {
        MCT.notify("Processing MCT in CBC-Decrypt mode (long); key size: " + keysize);
        MCT.notify("Using IJCE API methods");
        pw.println("==========");
        pw.println();
        pw.println("KEYSIZE=" + keysize);
        pw.println();
        int keylen = keysize / 8;
        byte[] keyMaterial = new byte[keylen];
        int size = this.cipher.blockSize();
        byte[] pt = new byte[size];
        byte[] ct = new byte[size];
        byte[] iv = new byte[size];
        int i = 0;
        while (i < 400) {
            int k;
            pw.println("I=" + i);
            pw.println("KEY=" + Hex.toString(keyMaterial));
            pw.println("IV=" + Hex.toString(iv));
            pw.println("CT=" + Hex.toString(ct));
            MCT_Key key = new MCT_Key(keyMaterial);
            this.cipher.initDecrypt(key);
            ++this.keyCount;
            int j = 0;
            while (j < 10000) {
                pt = this.cipher.crypt(ct);
                ++this.decBlocks;
                k = 0;
                while (k < size) {
                    int n = k;
                    pt[n] = (byte)(pt[n] ^ iv[k]);
                    ++k;
                }
                System.arraycopy(ct, 0, iv, 0, size);
                System.arraycopy(pt, 0, ct, 0, size);
                ++j;
            }
            pw.println("PT=" + Hex.toString(pt));
            pw.println();
            j = 0;
            if (keylen > size) {
                int count = keylen - size;
                k = size - count;
                while (j < count) {
                    int n = j++;
                    keyMaterial[n] = (byte)(keyMaterial[n] ^ iv[k++]);
                }
            }
            k = 0;
            while (j < keylen) {
                int n = j++;
                keyMaterial[n] = (byte)(keyMaterial[n] ^ pt[k++]);
            }
            ++i;
        }
    }

    final class MCT_Key
    implements SecretKey {
        byte[] key;

        public MCT_Key(byte[] data) {
            this.key = (byte[])data.clone();
        }

        public String getAlgorithm() {
            return "<ANY>";
        }

        public String getFormat() {
            return "RAW";
        }

        public byte[] getEncoded() {
            return (byte[])this.key.clone();
        }
    }
}

