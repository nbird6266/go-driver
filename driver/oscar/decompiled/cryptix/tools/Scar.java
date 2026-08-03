/*
 * Decompiled with CFR 0.152.
 */
package cryptix.tools;

import cryptix.CryptixException;
import cryptix.provider.key.RawKeyGenerator;
import cryptix.util.checksum.PRZ24;
import cryptix.util.io.DosFilter;
import java.io.CharConversionException;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import xjava.security.Cipher;
import xjava.security.CipherInputStream;
import xjava.security.CipherOutputStream;
import xjava.security.FeedbackCipher;
import xjava.security.KeyGenerator;
import xjava.security.SecretKey;
import xjava.security.WeakKeyException;

public class Scar
extends Thread {
    public static boolean DEBUG = true;
    static int debuglevel;
    static final PrintWriter err;
    static final boolean TRACE = false;
    static final boolean IN = true;
    static final boolean OUT = false;
    private boolean asciify = false;
    private boolean decrypting = false;
    private boolean encrypting = false;
    private boolean recursion = false;
    private boolean useDirInfo = false;
    private boolean verbose = false;
    private boolean wipeSource = false;
    private String cipherAlgorithm = null;
    private String passPhrase = null;
    private String mdAlgorithm = null;
    private String salt = null;
    private int iterations;
    private String input = null;
    private String output = null;
    private File inFile = null;
    private File outFile = null;
    private File temp = null;
    private File temp2 = null;
    private byte[] buffer = new byte[512];
    private DosFilter filter = new DosFilter();
    private int count = 0;
    private static final String MAGIC_STRING = "Que du magnifique...";
    private static final byte[] MAGIC;
    PropertyResourceBundle properties;
    static String fs;
    String header;
    String footer;
    String comment;
    static final SecureRandom random;
    static final String DEFAULT_HEADER = "BEGIN SCAR ARCHIVE";
    static final String DEFAULT_FOOTER = "END SCAR ARCHIVE";
    static final String DEFAULT_COMMENT = "scar by Cryptix...";
    static final String DEFAULT_CIPHER = "Square";
    static final String DEFAULT_PASS_PHRASE = "sub rosa";
    static final String DEFAULT_MD = "RIPEMD-160";
    static final String DEFAULT_SALT = "BEGIN SCAR ARCHIVE";
    static final int DEFAULT_ITERATIONS = 7;
    static final int CONV_WHITE = -1;
    static final int CONV_PAD = -2;
    static final int CONV_OTHER = -3;
    static final String VERSION = "Version: Alpha.1 --December 97";
    static final int MAX_LINE_LENGTH = 64;
    static final char[] BASE64;
    static final char PADDING = '=';

    static {
        err = new PrintWriter(System.out, true);
        MAGIC = MAGIC_STRING.getBytes();
        random = new SecureRandom();
        BASE64 = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    }

    static void debug(String s) {
        if (DEBUG) {
            err.println(">>> scar: " + s);
        }
    }

    static void trace(boolean in, String s) {
    }

    static void trace(String s) {
    }

    public static void main(String[] args) {
        System.out.println("scar (Strong Cryptographic ARchiver)\nVersion: Alpha.1 --December 97\nCopyright (c) 1997, 1998 Systemics Ltd. on behalf of\nthe Cryptix Development Team.  All rights reserved.\n\n");
        Scar jc = new Scar();
        jc.processOptions(args);
        jc.run();
    }

    public Scar() {
        Scar.trace(true, "Scar()");
        fs = System.getProperty("file.separator");
        String home = System.getProperty("user.home");
        try {
            String resource = String.valueOf(home) + "scar.properties";
            this.properties = new PropertyResourceBundle(new FileInputStream(resource));
        }
        catch (FileNotFoundException ex1) {
            Scar.debug("File \"scar.properties\" was not found in " + home + ". Using default properties");
            this.initDefaults();
            Scar.trace(false, "Scar()");
            return;
        }
        catch (IOException ex2) {
            Scar.debug("I/O exception occured while loading \"scar.properties\" file. Using default properties");
            this.initDefaults();
            Scar.trace(false, "Scar()");
            return;
        }
        try {
            this.header = this.properties.getString("scar.header");
        }
        catch (MissingResourceException ex3) {
            this.header = "BEGIN SCAR ARCHIVE";
        }
        try {
            this.footer = this.properties.getString("scar.footer");
        }
        catch (MissingResourceException ex4) {
            this.footer = DEFAULT_FOOTER;
        }
        try {
            this.comment = this.properties.getString("scar.comment");
        }
        catch (MissingResourceException ex5) {
            this.comment = DEFAULT_COMMENT;
        }
        try {
            this.cipherAlgorithm = this.properties.getString("scar.cipher.algorithm");
        }
        catch (MissingResourceException ex6) {
            this.cipherAlgorithm = DEFAULT_CIPHER;
        }
        try {
            this.passPhrase = this.properties.getString("scar.passphrase");
        }
        catch (MissingResourceException ex7) {
            this.passPhrase = DEFAULT_PASS_PHRASE;
        }
        try {
            this.mdAlgorithm = this.properties.getString("scar.md.algorithm");
        }
        catch (MissingResourceException ex8) {
            this.mdAlgorithm = DEFAULT_MD;
        }
        try {
            this.salt = this.properties.getString("scar.md.salt");
        }
        catch (MissingResourceException ex9) {
            this.salt = "BEGIN SCAR ARCHIVE";
        }
        try {
            this.iterations = Integer.parseInt(this.properties.getString("scar.md.iterations"));
        }
        catch (MissingResourceException ex10) {
            this.iterations = 7;
        }
        Scar.debug("Default properties [...");
        Scar.debug("      header line: \"-----" + this.header + "-----\"");
        Scar.debug("     comment line: \"Comment: " + this.comment + "\"");
        Scar.debug("      footer line: \"-----" + this.footer + "-----\"");
        Scar.debug(" cipher algorithm: \"" + this.cipherAlgorithm + "\"");
        Scar.debug("      pass-phrase: \"" + this.passPhrase + "\"");
        Scar.debug("   message digest: \"" + this.mdAlgorithm + "\"");
        Scar.debug("          md salt: \"" + this.salt + "\"");
        Scar.debug("    md iterations: " + this.iterations);
        Scar.debug("...]");
        Scar.trace(false, "Scar()");
    }

    void initDefaults() {
        Scar.trace(true, "initDefaults()");
        this.header = "BEGIN SCAR ARCHIVE";
        this.footer = DEFAULT_FOOTER;
        this.comment = DEFAULT_COMMENT;
        this.cipherAlgorithm = DEFAULT_CIPHER;
        this.passPhrase = DEFAULT_PASS_PHRASE;
        this.mdAlgorithm = DEFAULT_MD;
        this.salt = "BEGIN SCAR ARCHIVE";
        this.iterations = 7;
        Scar.trace(false, "initDefaults()");
    }

    public void processOptions(String[] args) {
        Scar.trace(true, "processOptions()");
        int argc = args.length;
        Scar.debug("Command line arguments [...");
        int i = 0;
        while (i < argc) {
            Scar.debug(" args[" + (i + 1) + "]: " + args[i]);
            ++i;
        }
        Scar.debug("...]");
        if (argc == 0) {
            this.printUsage();
        }
        Vector<String> files = new Vector<String>();
        int i2 = -1;
        String cmd = "";
        boolean next = true;
        this.filter.reset();
        int cargs = 0;
        while (true) {
            if (next) {
                if (++i2 >= argc) break;
                cmd = args[i2];
            } else {
                cmd = "-" + cmd.substring(2);
            }
            if (cmd.startsWith("-a")) {
                this.asciify = true;
                next = cmd.length() == 2;
                continue;
            }
            if (cmd.startsWith("-d")) {
                this.decrypting = true;
                next = cmd.length() == 2;
                continue;
            }
            if (cmd.startsWith("-e")) {
                this.encrypting = true;
                next = cmd.length() == 2;
                continue;
            }
            if (cmd.startsWith("-r")) {
                this.recursion = true;
                next = cmd.length() == 2;
                continue;
            }
            if (cmd.startsWith("-u")) {
                this.useDirInfo = true;
                next = cmd.length() == 2;
                continue;
            }
            if (cmd.startsWith("-v")) {
                this.verbose = true;
                next = cmd.length() == 2;
                continue;
            }
            if (cmd.startsWith("-w")) {
                this.wipeSource = true;
                next = cmd.length() == 2;
                continue;
            }
            if (cmd.startsWith("-c")) {
                this.cipherAlgorithm = args[i2 + 1];
                ++i2;
                next = true;
                ++cargs;
                continue;
            }
            if (cmd.startsWith("-p")) {
                this.passPhrase = args[i2 + 1];
                ++i2;
                next = true;
                ++cargs;
                continue;
            }
            if (cmd.startsWith("-m")) {
                this.mdAlgorithm = args[i2 + 1];
                ++i2;
                next = true;
                ++cargs;
                continue;
            }
            if (cmd.startsWith("-s")) {
                this.salt = args[i2 + 1];
                ++i2;
                next = true;
                ++cargs;
                continue;
            }
            if (cmd.startsWith("-i")) {
                this.iterations = Integer.parseInt(args[i2 + 1]);
                ++i2;
                next = true;
                ++cargs;
                continue;
            }
            if (files.contains(cmd)) continue;
            files.addElement(cmd);
        }
        if (this.decrypting) {
            if (this.encrypting || this.recursion || this.asciify) {
                this.halt("Found at least one conflicting option to Decryption");
            }
        } else {
            this.encrypting = true;
        }
        if (this.cipherAlgorithm.length() > 1 && (this.cipherAlgorithm.startsWith("\"") || this.cipherAlgorithm.startsWith("'"))) {
            this.cipherAlgorithm = this.cipherAlgorithm.substring(2, this.cipherAlgorithm.length() - 2);
        }
        if (this.passPhrase.length() > 1 && (this.passPhrase.startsWith("\"") || this.passPhrase.startsWith("'"))) {
            this.passPhrase = this.passPhrase.substring(2, this.passPhrase.length() - 2);
        }
        if (this.mdAlgorithm.length() > 1 && (this.mdAlgorithm.startsWith("\"") || this.mdAlgorithm.startsWith("'"))) {
            this.mdAlgorithm = this.mdAlgorithm.substring(2, this.mdAlgorithm.length() - 2);
        }
        if (this.salt.length() > 1 && (this.salt.startsWith("\"") || this.salt.startsWith("'"))) {
            this.salt = this.salt.substring(2, this.salt.length() - 2);
        }
        files.trimToSize();
        if (files.size() == 0) {
            this.halt("Missing <input> path-name");
        } else if (files.size() == 1) {
            this.input = (String)files.elementAt(0);
            this.output = System.getProperty("user.dir", new File("." + fs).getAbsolutePath());
        } else if (files.size() == 2) {
            this.input = (String)files.elementAt(0);
            this.output = (String)files.elementAt(1);
        } else {
            this.halt("Too many files");
        }
        if (this.input.startsWith("\"") || this.input.startsWith("'")) {
            this.input = this.input.substring(2, this.input.length() - 2);
        }
        if (this.input.indexOf("*") != -1 || this.input.indexOf("?") != -1) {
            this.filter.setMask(new File(this.input).getName());
            this.input = new File(new File(this.input).getAbsolutePath()).getParent();
        }
        if (this.output.startsWith("\"") || this.output.startsWith("'")) {
            this.output = this.output.substring(2, this.output.length() - 2);
        }
        if (this.encrypting && (this.decrypting || this.useDirInfo || this.output == null)) {
            this.halt("Found at least one conflicting option to Encryption");
        }
        this.inFile = new File(this.input);
        if (!this.inFile.exists()) {
            this.halt("Input <" + this.input + "> not found");
        }
        if (!this.inFile.canRead()) {
            this.halt("Input <" + this.input + "> is unreadable");
        }
        if (this.decrypting && this.inFile.isDirectory()) {
            this.halt("Decryption required but input <" + this.input + "> is a directory");
        }
        this.outFile = new File(this.output);
        if (this.encrypting && this.outFile.isDirectory()) {
            this.halt("Encryption required but output <" + this.output + "> is a directory");
        } else if (this.decrypting && !this.outFile.isDirectory()) {
            this.halt("Decryption required but output <" + this.output + "> is not a directory");
        }
        if (cargs == 0) {
            System.out.println("WARNING:\n  You did not specify at least one of: cipher algorithm, pass-phrase,\n  message digest algorithm, message digest salt value or message\n  digest iteration count; instead you are relying on default values\n  for these arguments. Please note that it is bad practice not to\n  vary at least one of those parameters. Failing to do so reduces\n  the efforts of an attacker trying to decrypt your scar.");
        }
        this.temp = this.getTempFile();
        Scar.trace(false, "processOptions()");
    }

    private void halt(String s) {
        Scar.trace("halt()");
        Scar.debug("halt() --> " + s);
        System.err.println("\n*** " + s + "...");
        System.exit(-1);
    }

    private void printUsage() {
        Scar.trace(true, "printUsage()");
        System.out.println("NAME\n  Scar: Strong Cryptographic ARchiver using Cryptix IJCE\n  (International Java Cryptography Extensions).\n\nSYNTAX\n  java cryptix.tools.Scar\n    [ -e ]\n    [ -a ]\n    [ -r ]\n    [ -v ]\n    [ -w ]\n    [ -c cipher]\n    [ -p passphrase]\n    [ -m s2k_message_digest]\n    [ -s s2k_salt]\n    [ -i s2k_iterations]\n    input\n    output\n\n  java cryptix.tools.Scar\n    -d\n    [ -u ]\n    [ -v ]\n    [ -w ]\n    [ -c cipher]\n    [ -p passphrase]\n    [ -m s2k_message_digest]\n    [ -s s2k_salt]\n    [ -i s2k_iterations]\n    input\n    [output]\n\nDESCRIPTION\n  Scar  reads  and  compresses input and writes the encrypted\n  result to output. It also does the inverse operation: reads\n  and decrypts input and decompresses the resulting data into\n  output.\n\n  By default both encryption and decryption are done using the\n  'Square' cipher algorithm (designed by Joan Daemen & Vincent\n  Rijmen) in Cipher Electronic Codebook (CBC) mode padded with\n  the method described in PKCS#7.\n\n  The cipher's secret session key is derived from a passphrase\n  supplied by the user through an S2K algorithm. The types and\n  differences of such S2K algorithms are described in Open-PGP\n  I.E.T.F document (draft-ietf-openpgp-formats.txt) dated 9/97.\n  This scar uses Simple, Salted and Salted-Iterated S2K variants.\n  The default message digest used with all S2K variants is\n  RIPEMD-160.\n\n  As mentioned earlier, the encryption and decryption are not\n  done on the data itself but on a ZIP-ped image.  ZIPping is\n  accomplished using the DEFLATE  method at its maximum level\n  (best size).\n\n  When a command line is entered, scar tries to load a properties\n  file named \"scar.properties\" from the user home directory,\n  the value of which is returned by Java's property \"user.home\"\n\nOPTIONS\n  -a   Asciify (Encryption).  Encode the output in Base-64 format\n       (RFC-1521) making it suitable for Internet transmission.\n\n  -d   Decryption.\n\n  -e   Encryption (default).\n\n  -r   Recurse (Encryption).  Apply the process repetitively to\n       sub-directories found in input.\n\n  -u   Use directory information (Decryption).  Recreate original\n       directory tree structure.\n\n  -v   Verbose.  Print notification messages to System.out.\n\n  -w   Wipe the source input after processing.\n\n  -c <cipher>\n       Cipher algorithm name ('Square' by default).  Other values\n       can be any block cipher algorithm installed and accessible\n       on user platform  that conforms to Sun(R)'s JCE or Cryptix\n       IJCE. With Cryptix security provider installed choices are\n       Blowfish, CAST5, RC4, IDEA, SAFER and LOKI91 in addition to\n       Square.\n\n  -p <passphrase>\n       An alphanumeric string with no spaces.  If contains spaces\n       then include within double quotes.  If not supplied use \"\".\n\n  -m <s2k_message_digest>\n       Message digest algorithm name ('RIPEMD-160' by default).\n       Other values can be any message digest algorithm installed\n       and accessible on user platform that conforms to Sun (R)'s\n       JCE or Cryptix's IJCE. With Cryptix security provider this\n       can be, in addition to 'RIPEMD-160', HAVAL, MD2, MD4, MD5,\n       SHA-1 and RIPEMD-128.\n\n  -s <s2k_salt>\n       S2K salt value.  If not supplied a Simple or Iterated S2K\n       algorithm will be used, depending on whether an iteration\n       count was supplied or not.\n\n  -i <s2k_iterations>\n       S2K iteration count. If a positive value is not provided,\n       a Simple or Salted S2K algorithm will be used, depending\n       on whether a salt value is given or not.\n\n  <input>\n       Input file or directory pathname. If the pathname includes\n       spaces,  then it should be enclosed within  double quotes.\n       Wild characters such as '*' (any number of characters) and\n       '?' (any one character) are allowed, in such case <input>\n       acts as a filter for actual selection of input file(s).\n       When a filter is used, it should be enclosed within \"\".\n\n  <output>\n       Output file or directory.  When decrypting this should be\n       a directory pathname. If absent, in the case of decryption,\n       use the current directory.\n\nCOPYRIGHT\n  Copyright (c) 1997, 1998 Systemics Ltd. on behalf of\n  the Cryptix Development Team.  All rights reserved.\n");
        Scar.trace(false, "printUsage()");
        System.exit(0);
    }

    private File getTempFile() {
        int x;
        File f;
        Scar.trace(true, "getTempFile()");
        do {
            x = (int)(Math.abs(random.nextDouble()) * 1000000.0);
        } while ((f = new File("." + fs, String.valueOf('F') + String.valueOf(x))).exists());
        Scar.debug("getTempFile() --> " + f.getName());
        Scar.trace(false, "getTempFile()");
        return f;
    }

    /*
     * Unable to fully structure code
     */
    public void run() {
        block42: {
            block43: {
                Scar.trace(true, "run()");
                this.notify("\nActual parameters");
                this.notify("\tcipher algorithm: \"" + this.cipherAlgorithm + "\"");
                this.notify("\t     pass-phrase: \"" + this.passPhrase + "\"");
                this.notify("\t  message digest: \"" + this.mdAlgorithm + "\"");
                this.notify("\t         md salt: \"" + this.salt + "\"");
                this.notify("\t   md iterations: " + this.iterations);
                this.notify("\t   input file(s): <" + this.input + ">");
                this.notify("\t output file/dir: <" + this.output + ">");
                this.notify("\tselection filter: <" + this.filter + ">");
                fis = null;
                fos = null;
                try {
                    block45: {
                        block44: {
                            cipher = null;
                            try {
                                cipher = Cipher.getInstance(String.valueOf(this.cipherAlgorithm) + "/CBC/PKCS#7");
                            }
                            catch (NoSuchAlgorithmException ex1) {
                                throw new CryptixException("Unable to instantiate a " + this.cipherAlgorithm + " cipher object in CBC mode with PKCS#7 padding.");
                            }
                            ivLen = ((FeedbackCipher)cipher).getInitializationVectorLength();
                            iv = new byte[ivLen];
                            ((FeedbackCipher)cipher).setInitializationVector(iv);
                            key = this.s2k();
                            if (!this.encrypting) break block44;
                            this.notify("\nZipping");
                            zipo = new ZipOutputStream(new FileOutputStream(this.temp));
                            zipo.setComment("Made with scar");
                            zipo.setMethod(8);
                            zipo.setLevel(9);
                            this.zip(this.inFile, zipo, 0);
                            if (this.count == 0) {
                                this.halt("No files were found that satisfy the selection criteria");
                            }
                            zipo.flush();
                            zipo.finish();
                            zipo.close();
                            this.notify("\nEncrypting");
                            this.temp2 = this.getTempFile();
                            fos = new FileOutputStream(this.temp2);
                            cipher.initEncrypt(key);
                            cis = new CipherInputStream(new FileInputStream(this.temp), cipher);
                            Scar.random.nextBytes(iv);
                            fos.write(iv);
                            fos.write(Scar.MAGIC);
                            while ((n = cis.read(this.buffer)) != -1) {
                                fos.write(this.buffer, 0, n);
                            }
                            cis.close();
                            fos.close();
                            if (this.asciify) {
                                this.notify("\nAsciifying");
                                fis = new FileInputStream(this.temp2);
                                cros = new ScarOutputStream(new FileOutputStream(this.outFile));
                                while ((n = fis.read(this.buffer)) != -1) {
                                    cros.write(this.buffer, 0, n);
                                }
                                fis.close();
                                cros.flush();
                                cros.close();
                            } else {
                                this.outFile.delete();
                                this.temp2.renameTo(this.outFile);
                            }
                            this.temp.delete();
                            this.temp2.delete();
                            break block45;
                        }
                        this.notify("\nDe-asciifying");
                        fos = new FileOutputStream(this.temp);
                        cris = null;
                        try {
                            cris = new ScarInputStream(new FileInputStream(this.inFile));
                            while ((n = cris.read(this.buffer)) != -1) {
                                fos.write(this.buffer, 0, n);
                            }
                            cris.close();
                            fos.flush();
                            fos.close();
                            fis = new FileInputStream(this.temp);
                        }
                        catch (IOException e1) {
                            Scar.debug("Warning: " + e1.getMessage());
                            this.notify("Warning: " + e1.getMessage());
                            fis = new FileInputStream(this.inFile);
                        }
                        this.notify("\nDecrypting");
                        this.temp2 = this.getTempFile();
                        fos = new FileOutputStream(this.temp2);
                        cipher.initDecrypt(key);
                        cos = new CipherOutputStream(fos, cipher);
                        n = fis.read(this.buffer, 0, iv.length);
                        Scar.debug("length of alleged iv: " + n);
                        if (n != iv.length) {
                            throw new CryptixException("File too short to be a scar (1).");
                        }
                        n = fis.read(this.buffer, 0, Scar.MAGIC.length);
                        if (n == -1 || n != Scar.MAGIC.length) {
                            throw new CryptixException("File too short to be a scar (2).");
                        }
                        Scar.debug("Magic word: " + new String(this.buffer, 0, Scar.MAGIC.length));
                        if (new String(this.buffer, 0, Scar.MAGIC.length).equals("Que du magnifique...")) ** GOTO lbl104
                        throw new CryptixException("File doesn't look to be a scar. If it is, it was produced using a different set of properties.");
lbl-1000:
                        // 1 sources

                        {
                            cos.write(this.buffer, 0, n);
lbl104:
                            // 2 sources

                            ** while ((n = fis.read((byte[])this.buffer)) != -1)
                        }
lbl105:
                        // 1 sources

                        cos.flush();
                        cos.close();
                        fis.close();
                        fos.close();
                        this.notify("\nUnzipping");
                        zipi = new ZipInputStream(new FileInputStream(this.temp2));
                        this.unzip(zipi, this.outFile);
                        zipi.close();
                        this.temp.delete();
                        this.temp2.delete();
                    }
                    if (this.wipeSource) {
                        this.notify("\nDeleting input");
                        this.wipe(this.inFile, 0);
                    }
                }
                catch (CryptixException ex1) {
                    Scar.debug(ex1.getMessage());
                    this.notify("\n--- Exception: " + ex1.getMessage());
                    if (this.temp != null) {
                        try {
                            this.temp.delete();
                        }
                        catch (Exception var12_19) {
                            // empty catch block
                        }
                    }
                    if (this.temp2 != null) {
                        try {
                            this.temp2.delete();
                        }
                        catch (Exception var12_20) {}
                    }
                    break block42;
                }
                catch (Exception ex2) {
                    try {
                        ex2.printStackTrace();
                        break block42;
                    }
                    catch (Throwable var11_27) {
                        throw var11_27;
                    }
                    finally {
                        if (this.temp != null) {
                            try {
                                this.temp.delete();
                            }
                            catch (Exception var12_21) {}
                        }
                        if (this.temp2 != null) {
                            try {
                                this.temp2.delete();
                            }
                            catch (Exception var12_22) {}
                        }
                    }
                }
                if (this.temp == null) break block43;
                try {
                    this.temp.delete();
                }
                catch (Exception var12_25) {
                    // empty catch block
                }
            }
            if (this.temp2 != null) {
                try {
                    this.temp2.delete();
                }
                catch (Exception var12_26) {
                    // empty catch block
                }
            }
        }
        Scar.trace(false, "run()");
    }

    private SecretKey s2k() throws CloneNotSupportedException, InvalidKeyException {
        Scar.trace(true, "s2k()");
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(this.mdAlgorithm);
        }
        catch (NoSuchAlgorithmException ex1) {
            throw new CryptixException("Unable to instantiate a " + this.mdAlgorithm + " message digest object");
        }
        int mdLength = md.digest().length;
        RawKeyGenerator rkg = null;
        try {
            rkg = (RawKeyGenerator)KeyGenerator.getInstance(this.cipherAlgorithm);
        }
        catch (NoSuchAlgorithmException ex1) {
            throw new CryptixException("Unable to instantiate a " + this.cipherAlgorithm + " key-generator object");
        }
        int keyLength = rkg.getDefaultKeyLength();
        Vector<Object> mds = new Vector<Object>();
        int lensofar = 0;
        do {
            mds.addElement(md.clone());
            md.update((byte)0);
        } while ((lensofar += mdLength) < keyLength);
        int s = this.salt.length();
        int p = this.passPhrase.length();
        byte[] salted = new byte[s + p];
        if (s != 0) {
            System.arraycopy(this.salt.getBytes(), 0, salted, 0, s);
        }
        System.arraycopy(this.passPhrase.getBytes(), 0, salted, s, p);
        int countsofar = 0;
        int mdCount = mds.size();
        do {
            int i = 0;
            while (i < mdCount) {
                ((MessageDigest)mds.elementAt(i)).update(salted);
                ++i;
            }
        } while (++countsofar < this.iterations);
        byte[] keyData = new byte[keyLength];
        lensofar = 0;
        int i = 0;
        while (i < mdCount) {
            byte[] mdBytes = ((MessageDigest)mds.elementAt(i)).digest();
            int length = lensofar + mdLength > keyLength ? keyLength - lensofar : mdLength;
            System.arraycopy(mdBytes, 0, keyData, lensofar, length);
            lensofar += length;
            ++i;
        }
        rkg.setWeakAllowed(true);
        SecretKey key = null;
        try {
            key = rkg.generateKey(keyData);
        }
        catch (WeakKeyException weakKeyException) {
            // empty catch block
        }
        Scar.trace(false, "s2k()");
        return key;
    }

    private void notify(String s) {
        Scar.trace(true, "notify()");
        if (this.verbose) {
            System.out.println(String.valueOf(s) + "...");
        }
        Scar.trace(false, "notify()");
    }

    public void zip(File source, ZipOutputStream zip, int level) throws FileNotFoundException, IOException {
        Scar.trace(true, "zip(" + level + ")");
        FileInputStream fis = null;
        if (source.isFile()) {
            try {
                ++this.count;
                fis = new FileInputStream(source);
                String sf = source.getCanonicalPath();
                int n = sf.indexOf(fs);
                if (n != -1) {
                    sf = sf.substring(n + 1);
                }
                sf = sf.replace(fs.charAt(0), '/');
                this.notify("\t " + sf);
                zip.putNextEntry(new ZipEntry(sf));
                while ((n = fis.read(this.buffer)) != -1) {
                    zip.write(this.buffer, 0, n);
                }
            }
            finally {
                if (fis != null) {
                    try {
                        fis.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (source.isDirectory() && (level == 0 || level != 0 && this.recursion)) {
            String[] entries = source.list(this.filter);
            int i = 0;
            while (i < entries.length) {
                this.zip(new File(source, entries[i]), zip, level + 1);
                ++i;
            }
        }
        Scar.trace(false, "zip(" + level + ")");
    }

    public void unzip(ZipInputStream zip, File dest) throws FileNotFoundException, IOException {
        ZipEntry ze;
        Scar.trace(true, "unzip()");
        OutputStream fos = null;
        while ((ze = zip.getNextEntry()) != null) {
            if (ze.isDirectory()) continue;
            try {
                String destPath;
                int n;
                String sf = ze.getName();
                if (!this.useDirInfo) {
                    n = sf.lastIndexOf("/");
                    if (n != -1) {
                        sf = sf.substring(n + 1);
                    }
                } else {
                    sf = sf.replace('/', fs.charAt(0));
                }
                sf = (destPath = dest.getPath()).endsWith(fs) ? String.valueOf(destPath) + sf : String.valueOf(destPath) + fs + sf;
                this.notify("\t " + sf);
                File f = new File(sf);
                if (this.useDirInfo) {
                    new File(f.getParent()).mkdirs();
                }
                fos = new FileOutputStream(f);
                while ((n = zip.read(this.buffer)) != -1) {
                    ((FileOutputStream)fos).write(this.buffer, 0, n);
                }
            }
            finally {
                if (fos != null) {
                    try {
                        fos.flush();
                        ((FileOutputStream)fos).close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Scar.trace(false, "unzip()");
    }

    private void wipe(File source, int level) {
        Scar.trace(true, "wipe(" + level + ")");
        if (source.isDirectory() && (level == 0 || level != 0 && this.recursion)) {
            String[] entries = source.list();
            int i = 0;
            while (i < entries.length) {
                this.wipe(new File(source, entries[i]), level + 1);
                ++i;
            }
        }
        try {
            source.delete();
        }
        catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
        Scar.trace(false, "wipe(" + level + ")");
    }

    final class ScarInputStream
    extends FilterInputStream {
        byte[] lineBuffer;
        boolean finished;
        PRZ24 crc;
        byte[] inBuf = new byte[4];
        byte[] outBuf = new byte[3];
        int inOff;
        int outOff;
        int outBufMax;

        public ScarInputStream(InputStream is) throws IOException {
            super(is);
            String line;
            Scar.trace(true, "ScarInputStream()");
            this.outBufMax = 3;
            this.outOff = 0;
            this.inOff = 0;
            this.finished = false;
            this.crc = new PRZ24();
            do {
                if ((line = this.readLine()) != null) continue;
                throw new EOFException("Missing scar header");
            } while (!line.startsWith("-----" + Scar.this.header + "-----"));
            do {
                if ((line = this.readLine()) != null) continue;
                throw new EOFException("Missing scar data");
            } while (line.length() != 0);
            Scar.trace(false, "ScarInputStream()");
        }

        public synchronized int read() throws IOException {
            Scar.trace(true, "ScarInputStream.read()");
            if (this.outOff == 0) {
                if (this.finished) {
                    return -1;
                }
                int inByte = 0;
                int n = -1;
                while (n == -1) {
                    inByte = this.in.read();
                    if (inByte < 0) {
                        return -1;
                    }
                    n = this.toNumber(inByte);
                }
                if (n < 0) {
                    if (n == -3) {
                        throw new CharConversionException();
                    }
                    long computed = this.crc.getValue();
                    long actual = 0L;
                    this.crc = null;
                    int i = 0;
                    while (i < 3) {
                        inByte = this.read();
                        if (inByte < 0) {
                            throw new EOFException();
                        }
                        actual = actual << 8 | (long)inByte;
                        ++i;
                    }
                    this.finished = true;
                    this.outOff = 0;
                    if (actual != computed) {
                        throw new IOException("PRZ24 crc mismatch");
                    }
                    return -1;
                }
                int i = 0;
                while (i < 4) {
                    if (n == -2) {
                        if (i < 2) {
                            throw new CharConversionException();
                        }
                    } else {
                        if (n < 0) {
                            throw new CharConversionException();
                        }
                        this.inBuf[this.inOff++] = (byte)n;
                    }
                    if (i != 3) {
                        inByte = this.in.read();
                        if (inByte < 0) {
                            throw new EOFException();
                        }
                        n = this.toNumber(inByte);
                    }
                    ++i;
                }
                this.writeTriplet();
            }
            int b = this.outBuf[this.outOff++] & 0xFF;
            if (this.outOff == this.outBufMax) {
                this.outOff = 0;
            }
            if (this.crc != null) {
                this.crc.update(b);
            }
            Scar.trace(false, "ScarInputStream.read()");
            return b;
        }

        public synchronized int read(byte[] buffer, int offset, int length) throws IOException {
            Scar.trace(true, "ScarInputStream.read(3)");
            Scar.debug("ScarInputStream.read(" + buffer + ", " + offset + ", " + length + ")");
            int i = 0;
            while (i < length) {
                int c = this.read();
                if (c < 0) {
                    return i == 0 ? -1 : i;
                }
                buffer[offset++] = (byte)c;
                ++i;
            }
            Scar.trace(false, "ScarInputStream.read(3)");
            return length;
        }

        public synchronized void close() throws IOException {
            String line;
            Scar.trace(true, "ScarInputStream.close()");
            do {
                if ((line = this.readLine()) != null) continue;
                throw new EOFException("Missing scar footer");
            } while (!line.startsWith("-----" + Scar.this.footer + "-----"));
            this.finished = true;
            this.outOff = 0;
            super.close();
            Scar.trace(false, "ScarInputStream.close()");
        }

        private String readLine() throws IOException {
            int c;
            InputStream in = this.in;
            byte[] buf = this.lineBuffer;
            if (buf == null) {
                buf = this.lineBuffer = new byte[128];
            }
            int room = buf.length;
            int offset = 0;
            block4: while (true) {
                c = in.read();
                switch (c) {
                    case -1: 
                    case 10: {
                        break block4;
                    }
                    case 13: {
                        int c2 = in.read();
                        if (c2 == 10) break block4;
                        if (!(in instanceof PushbackInputStream)) {
                            in = this.in = new PushbackInputStream(in);
                        }
                        ((PushbackInputStream)in).unread(c2);
                        break block4;
                    }
                    default: {
                        if (--room < 0) {
                            buf = new byte[offset + 128];
                            room = buf.length - offset - 1;
                            System.arraycopy(this.lineBuffer, 0, buf, 0, offset);
                            this.lineBuffer = buf;
                        }
                        buf[offset++] = (byte)c;
                        continue block4;
                    }
                }
                break;
            }
            if (c == -1 && offset == 0) {
                return null;
            }
            byte[] lineBytes = new byte[offset];
            System.arraycopy(buf, 0, lineBytes, 0, offset);
            return new String(lineBytes);
        }

        private void writeTriplet() {
            this.outBufMax = 0;
            this.outBuf[this.outBufMax++] = (byte)(this.inBuf[0] << 2 | this.inBuf[1] >>> 4);
            if (this.inOff > 2) {
                this.outBuf[this.outBufMax++] = (byte)(this.inBuf[1] << 4 | this.inBuf[2] >>> 2);
            }
            if (this.inOff > 3) {
                this.outBuf[this.outBufMax++] = (byte)(this.inBuf[2] << 6 | this.inBuf[3]);
            }
            this.inOff = 0;
        }

        private int toNumber(int b) {
            if (b >= 97 & b <= 122) {
                return b - 97 + 26;
            }
            if (b >= 65 & b <= 90) {
                return b - 65;
            }
            if (b >= 48 & b <= 57) {
                return b - 48 + 52;
            }
            if (b == 43) {
                return 62;
            }
            if (b == 47) {
                return 63;
            }
            if (b == 61) {
                return -2;
            }
            if (b == 10 || b == 13 || b == 32 || b == 9) {
                return -1;
            }
            return -3;
        }
    }

    final class ScarOutputStream
    extends FilterOutputStream {
        PRZ24 crc;
        byte[] inBuf;
        int inOff;
        int lineLength;

        public ScarOutputStream(OutputStream os) throws IOException {
            super(os);
            Scar.trace(true, "ScarOutputStream()");
            this.inBuf = new byte[3];
            this.crc = new PRZ24();
            this.lineLength = 0;
            this.inOff = 0;
            this.out.write(new String("-----" + Scar.this.header + "-----").getBytes());
            this.writeln();
            this.out.write(Scar.VERSION.getBytes());
            this.writeln();
            if (Scar.this.comment.length() != 0) {
                this.out.write(new String("Comment: " + Scar.this.comment).getBytes());
                this.writeln();
            }
            this.writeln();
            Scar.trace(false, "ScarOutputStream()");
        }

        public void write(int b) throws IOException {
            this.inBuf[this.inOff++] = (byte)b;
            this.crc.update(b);
            if (this.inOff == 3) {
                this.writeQuadruplet();
            }
        }

        public void write(byte[] b, int offset, int length) throws IOException {
            int i = 0;
            while (i < length) {
                this.write(b[i + offset]);
                ++i;
            }
        }

        public void close() throws IOException {
            Scar.trace(true, "ScarOutputStream.close()");
            if (this.inOff != 0) {
                int i = this.inOff;
                while (i < 3) {
                    this.inBuf[i] = 0;
                    ++i;
                }
                this.writeQuadruplet();
            }
            if (this.lineLength != 0) {
                this.writeln();
            }
            this.out.write(61);
            int cks = (int)this.crc.getValue();
            this.inBuf[0] = (byte)(cks >> 16);
            this.inBuf[1] = (byte)(cks >> 8);
            this.inBuf[2] = (byte)cks;
            this.inOff = 3;
            this.writeQuadruplet();
            this.writeln();
            this.out.write(new String("-----" + Scar.this.footer + "-----").getBytes());
            this.writeln();
            super.flush();
            super.close();
            Scar.trace(false, "ScarOutputStream.close()");
        }

        private synchronized void writeQuadruplet() throws IOException {
            int c = BASE64[(this.inBuf[0] & 0xFF) >> 2];
            this.out.write(c);
            c = BASE64[(this.inBuf[0] & 3) << 4 | (this.inBuf[1] & 0xFF) >> 4];
            this.out.write(c);
            c = this.inOff > 1 ? BASE64[(this.inBuf[1] & 0xF) << 2 | (this.inBuf[2] & 0xCF) >> 6] : 61;
            this.out.write(c);
            c = this.inOff > 2 ? BASE64[this.inBuf[2] & 0x3F] : 61;
            this.out.write(c);
            this.inOff = 0;
            this.lineLength += 4;
            if (this.lineLength >= 64) {
                this.writeln();
            }
        }

        private void writeln() throws IOException {
            this.out.write(13);
            this.out.write(10);
            this.lineLength = 0;
        }
    }
}

