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

public final class KAT {
    static final String VERSION = "$Revision: 1.6 $";
    static final String SUBMITTER = "<as stated on the submission cover sheet>";
    boolean varKey = false;
    boolean varText = false;
    String dirName = null;
    String keylengths = null;
    String provider = null;
    String cipherName = null;
    File destination = null;
    int[] keys = new int[]{128, 192, 256};
    final String vkFileName = "ecb_vk.txt";
    final String vtFileName = "ecb_vt.txt";
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
        System.out.println("NIST Known Answer Tests data generator/exerciser\n\n$Revision: 1.6 $\nCopyright (c) 1998 Systemics Ltd. on behalf of\nthe Cryptix Development Team.  All rights reserved.\n\n");
        KAT cmd = new KAT();
        cmd.processOptions(args);
        cmd.run();
    }

    private void processOptions(String[] args) {
        int argc = args.length;
        if (argc == 0) {
            this.printUsage();
        }
        System.out.println("(type \"java cryptix.tools.KAT\" with no arguments for help)\n\n");
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
            if (cmd.startsWith("-k")) {
                this.varKey = true;
                next = cmd.length() == 2;
                continue;
            }
            if (cmd.startsWith("-t")) {
                this.varText = true;
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
            KAT.halt("Missing cipher algorithm name");
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
                    KAT.halt("Negative key length not allowed: " + k);
                }
                if (count == 3) {
                    KAT.halt("Only three key-length values are allowed.");
                }
                keystemp[count++] = k;
            }
            if (count != 0) {
                this.keys = new int[count];
                System.arraycopy(keystemp, 0, this.keys, 0, count);
            }
        }
        if (!this.varKey && !this.varText) {
            this.varText = true;
            this.varKey = true;
        }
        if (this.dirName == null) {
            this.dirName = System.getProperty("user.dir");
        }
        this.destination = new File(this.dirName);
        if (!this.destination.isDirectory()) {
            KAT.halt("Destination <" + this.destination.getName() + "> is not a directory");
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
            KAT.notify("Unable to find a " + this.cipherName + "_Algorithm class");
            this.algorithm = null;
        }
        catch (NoSuchMethodException ex2) {
            KAT.notify("Unable to find method " + ex2.getMessage() + " in " + this.cipherName + "_Algorithm class");
            this.algorithm = null;
        }
        try {
            this.cipher = Cipher.getInstance(String.valueOf(this.cipherName) + "/ECB", this.provider);
        }
        catch (NoSuchProviderException ex3) {
            KAT.halt("Unable to locate Security Provider: " + this.provider);
        }
        catch (NoSuchAlgorithmException ex4) {
            KAT.halt("Unable to locate an implementation for Cipher: " + this.cipherName + "/ECB");
        }
        this.useReflection = this.algorithm != null;
    }

    static void halt(String s) {
        System.err.println("\n*** " + s + "...");
        System.exit(-1);
    }

    static void notify(String s) {
        System.out.println("KAT: " + s + "...");
    }

    void printUsage() {
        System.out.println("NAME\n  KAT: A Known Answer Tests data generator/exerciser for any block\n  cipher algorithm.\n\nSYNTAX\n  java cryptix.tools.KAT\n    [ -k | -t ]\n    [ -l <comma-separated-key-lengths>]\n    [ -d <output-directory>]\n    [ -p <provider>]\n    <cipher>\n\nDESCRIPTION\n  For a designated symmetric block cipher algorithm, KAT generates\n  and exercises Known Answer Tests data for both Variable Key and\n  Variable Text suites.\n  KAT's output file format conforms to the layout described in\n  Section 3 of NIST's document \"Description of Known Answer Tests\n  and Monte Carlo Tests for Advanced Encryption Standard (AES)\n  Candidate Algorithm Submissions\" dated January 7, 1998.\n\nOPTIONS\n  -k   Generate data for variable-key tests only.  By default KAT\n       generates both variable-key and variable-text test uites.\n\n  -t   Generate data for variable-text tests only. By default KAT\n       generates both variable-key and variable-text test suites.\n\n  -l <comma-separated-key-lengths>\n       Comma separated list (maximum of three) of key lengths to use\n       for the tests.  If omitted, the following three values are\n       assumed: 128, 192 and 256.\n\n  -d <output-directory>\n       Pathname of the directory where output files: \"ecb_vk.txt\"\n       and \"ecb_vt.txt\" will be generated.  If this destination\n       directory is not specified, those files will be placed in\n       the current user directory.\n\n  -p <provider>\n       Name of the Security Provider for the designated algorithm.\n       If omitted, then assumes provider has the same name as the\n       algorithm itself.\n\n  <cipher>\n       Cipher algorithm name.\n\nCOPYRIGHT\n  Copyright (c) 1998 Systemics Ltd. on behalf of\n  the Cryptix Development Team.  All rights reserved.\n");
        System.exit(0);
    }

    void run() {
        long time = System.currentTimeMillis();
        try {
            if (this.varKey) {
                this.vkKAT("ecb_vk.txt");
            }
            if (this.varText) {
                this.vtKAT("ecb_vt.txt");
            }
        }
        catch (KeyException ex1) {
            ex1.printStackTrace();
            KAT.halt("Key Exception encountered:\n" + ex1.getMessage());
        }
        KAT.notify("Java interpreter used: Version " + System.getProperty("java.version"));
        KAT.notify("Java Just-In-Time (JIT) compiler: " + System.getProperty("java.compiler"));
        KAT.notify("Total execution time (ms): " + (System.currentTimeMillis() - time));
        KAT.notify("During this time, " + this.cipherName + ":");
        KAT.notify("  Encrypted " + this.encBlocks + " blocks");
        KAT.notify("  Decrypted " + this.decBlocks + " blocks");
        KAT.notify("  Created " + this.keyCount + " session keys");
    }

    void vkKAT(String fileName) throws KeyException {
        File f = new File(this.destination, fileName);
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(f), true);
        }
        catch (IOException ex1) {
            KAT.halt("Unable to initialize <" + fileName + "> as a Writer:\n" + ex1.getMessage());
        }
        out.println();
        out.println("=========================");
        out.println();
        out.println("FILENAME:  \"" + fileName + "\"");
        out.println();
        out.println("Electronic Codebook (ECB) Mode");
        out.println("Variable Key Known Answer Tests");
        out.println();
        out.println("Algorithm Name: " + this.cipherName);
        out.println("Principal Submitter: <as stated on the submission cover sheet>");
        out.println();
        boolean useIJCE = true;
        if (this.useReflection) {
            try {
                int k = 0;
                while (k < this.keys.length) {
                    this.vkForKeyReflect(this.keys[k], out);
                    ++k;
                }
                useIJCE = false;
            }
            catch (IllegalAccessException ex1) {
                KAT.notify("Exception while invoking a method in " + this.cipherName + "_Algorithm class");
            }
            catch (InvocationTargetException ex3) {
                KAT.halt("Exception encountered in a " + this.cipherName + "_Algorithm method:\n" + ex3.getMessage());
                useIJCE = false;
            }
        }
        if (useIJCE) {
            int k = 0;
            while (k < this.keys.length) {
                this.vkForKeyIjce(this.keys[k], out);
                ++k;
            }
        }
        out.println("==========");
        out.close();
    }

    void vkForKeyReflect(int keysize, PrintWriter out) throws IllegalAccessException, InvocationTargetException {
        KAT.notify("Generating and testing Variable Key KAT (short); key size: " + keysize);
        KAT.notify("Using Reflection API methods");
        Object[] args = new Object[]{};
        int count = keysize / 8;
        int size = (Integer)this.blockSize.invoke(null, args);
        byte[] keyMaterial = new byte[count];
        byte[] pt = new byte[size];
        int round = 0;
        out.println("==========");
        out.println();
        out.println("KEYSIZE=" + keysize);
        out.println();
        out.println("PT=" + Hex.toString(pt));
        out.println();
        int i = 0;
        while (i < count) {
            int j = 0;
            while (j < 8) {
                out.println("I=" + ++round);
                keyMaterial[i] = (byte)(1 << 7 - j);
                out.println("KEY=" + Hex.toString(keyMaterial));
                args = new Object[]{keyMaterial};
                Object skeys = this.makeKey.invoke(null, args);
                ++this.keyCount;
                args = new Object[]{pt, new Integer(0), skeys};
                byte[] ct = (byte[])this.encrypt.invoke(null, args);
                ++this.encBlocks;
                out.print("CT=" + Hex.toString(ct));
                args[0] = ct;
                byte[] cpt = (byte[])this.decrypt.invoke(null, args);
                ++this.decBlocks;
                if (!ArrayUtil.areEqual(pt, cpt)) {
                    out.print(" *** ERROR ***");
                }
                out.println();
                out.println();
                ++j;
            }
            keyMaterial[i] = 0;
            ++i;
        }
    }

    void vkForKeyIjce(int keysize, PrintWriter out) throws KeyException {
        KAT.notify("Generating and testing Variable Key KAT (short); key size: " + keysize);
        KAT.notify("Using IJCE API methods");
        int count = keysize / 8;
        int size = this.cipher.blockSize();
        byte[] keyMaterial = new byte[count];
        byte[] pt = new byte[size];
        int round = 0;
        out.println("==========");
        out.println();
        out.println("KEYSIZE=" + keysize);
        out.println();
        out.println("PT=" + Hex.toString(pt));
        out.println();
        int i = 0;
        while (i < count) {
            int j = 0;
            while (j < 8) {
                out.println("I=" + ++round);
                keyMaterial[i] = (byte)(1 << 7 - j);
                out.println("KEY=" + Hex.toString(keyMaterial));
                KAT_Key key = new KAT_Key(keyMaterial);
                this.cipher.initEncrypt(key);
                ++this.keyCount;
                byte[] ct = this.cipher.crypt(pt);
                ++this.encBlocks;
                out.print("CT=" + Hex.toString(ct));
                this.cipher.initDecrypt(key);
                ++this.keyCount;
                byte[] cpt = this.cipher.crypt(ct);
                ++this.decBlocks;
                if (!ArrayUtil.areEqual(pt, cpt)) {
                    out.print(" *** ERROR ***");
                }
                out.println();
                out.println();
                ++j;
            }
            keyMaterial[i] = 0;
            ++i;
        }
    }

    void vtKAT(String fileName) throws KeyException {
        File f = new File(this.destination, fileName);
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(f), true);
        }
        catch (IOException ex1) {
            KAT.halt("Unable to initialize <" + fileName + "> as a Writer:\n" + ex1.getMessage());
        }
        out.println();
        out.println("=========================");
        out.println();
        out.println("FILENAME:  \"" + fileName + "\"");
        out.println();
        out.println("Electronic Codebook (ECB) Mode");
        out.println("Variable Text Known Answer Tests");
        out.println();
        out.println("Algorithm Name: " + this.cipherName);
        out.println("Principal Submitter: <as stated on the submission cover sheet>");
        out.println();
        boolean useIJCE = true;
        if (this.useReflection) {
            try {
                int k = 0;
                while (k < this.keys.length) {
                    this.vtForKeyReflect(this.keys[k], out);
                    ++k;
                }
                useIJCE = false;
            }
            catch (IllegalAccessException ex1) {
                KAT.notify("Exception while invoking a method in " + this.cipherName + "_Algorithm class");
            }
            catch (InvocationTargetException ex3) {
                KAT.halt("Exception encountered in a " + this.cipherName + "_Algorithm method:\n" + ex3.getMessage());
                useIJCE = false;
            }
        }
        if (useIJCE) {
            int k = 0;
            while (k < this.keys.length) {
                this.vtForKeyIjce(this.keys[k], out);
                ++k;
            }
        }
        out.println("==========");
        out.close();
    }

    void vtForKeyReflect(int keysize, PrintWriter out) throws IllegalAccessException, InvocationTargetException {
        KAT.notify("Generating and testing Variable Text KAT (short); key size: " + keysize);
        KAT.notify("Using Reflection API methods");
        Object[] args = new Object[]{};
        byte[] keyMaterial = new byte[keysize / 8];
        int count = (Integer)this.blockSize.invoke(null, args);
        byte[] pt = new byte[count];
        int round = 0;
        args = new Object[]{keyMaterial};
        Object skeys = this.makeKey.invoke(null, args);
        ++this.keyCount;
        out.println("==========");
        out.println();
        out.println("KEYSIZE=" + keysize);
        out.println();
        out.println("KEY=" + Hex.toString(keyMaterial));
        out.println();
        args = new Object[3];
        args[1] = new Integer(0);
        args[2] = skeys;
        int i = 0;
        while (i < count) {
            int j = 0;
            while (j < 8) {
                out.println("I=" + ++round);
                pt[i] = (byte)(1 << 7 - j);
                out.println("PT=" + Hex.toString(pt));
                args[0] = pt;
                byte[] ct = (byte[])this.encrypt.invoke(null, args);
                ++this.encBlocks;
                out.print("CT=" + Hex.toString(ct));
                args[0] = ct;
                byte[] cpt = (byte[])this.decrypt.invoke(null, args);
                ++this.decBlocks;
                if (!ArrayUtil.areEqual(pt, cpt)) {
                    out.print(" *** ERROR ***");
                }
                out.println();
                out.println();
                ++j;
            }
            pt[i] = 0;
            ++i;
        }
    }

    void vtForKeyIjce(int keysize, PrintWriter out) throws KeyException {
        KAT.notify("Generating and testing Variable Text KAT (short); key size: " + keysize);
        KAT.notify("Using IJCE API methods");
        byte[] keyMaterial = new byte[keysize / 8];
        int count = this.cipher.blockSize();
        byte[] pt = new byte[count];
        int round = 0;
        KAT_Key key = new KAT_Key(keyMaterial);
        out.println("==========");
        out.println();
        out.println("KEYSIZE=" + keysize);
        out.println();
        out.println("KEY=" + Hex.toString(keyMaterial));
        out.println();
        int i = 0;
        while (i < count) {
            int j = 0;
            while (j < 8) {
                out.println("I=" + ++round);
                pt[i] = (byte)(1 << 7 - j);
                out.println("PT=" + Hex.toString(pt));
                this.cipher.initEncrypt(key);
                ++this.keyCount;
                byte[] ct = this.cipher.crypt(pt);
                ++this.encBlocks;
                out.print("CT=" + Hex.toString(ct));
                this.cipher.initDecrypt(key);
                ++this.keyCount;
                byte[] cpt = this.cipher.crypt(ct);
                ++this.decBlocks;
                if (!ArrayUtil.areEqual(pt, cpt)) {
                    out.print(" *** ERROR ***");
                }
                out.println();
                out.println();
                ++j;
            }
            pt[i] = 0;
            ++i;
        }
    }

    final class KAT_Key
    implements SecretKey {
        byte[] key;

        public KAT_Key(byte[] data) {
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

