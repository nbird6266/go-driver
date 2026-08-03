/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Properties;

final class Install {
    private PrintWriter out;
    private String product_name;
    private String provider_class;
    private String version_string;
    private int errorcode;
    private static final int INSTALLED = 10;
    private static final int ALREADY_INSTALLED = 10;
    private static final int NOT_INSTALLED = 1;

    public static void main(String[] args) {
        Install x = new Install(new PrintWriter(System.out, true), "Cryptix", "cryptix.provider.Cryptix", "Cryptix V3");
        try {
            x.run();
        }
        catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.err.println();
            System.err.println("Cryptix has not been installed.");
        }
        System.exit(x.getErrorCode());
    }

    Install(PrintWriter out, String product_name, String provider_class, String version_string) {
        this.out = out;
        this.product_name = product_name;
        this.provider_class = provider_class;
        this.version_string = version_string;
        this.errorcode = 1;
    }

    int getErrorCode() {
        return this.errorcode;
    }

    void run() {
        String value;
        String javaHome = System.getProperty("java.home");
        this.out.println("Examining the Java installation at " + javaHome);
        this.out.println();
        String nl = System.getProperty("line.separator");
        Properties properties = new Properties();
        File securityDir = new File(javaHome, "lib" + File.separator + "security" + File.separator);
        File securityPropsFile = new File(securityDir, "java.security");
        try {
            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(securityPropsFile));
                properties.load(bis);
                bis.close();
            }
            catch (FileNotFoundException e) {
                try {
                    securityPropsFile = new File(securityDir, "JAVA.SEC");
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(securityPropsFile));
                    properties.load(bis);
                    bis.close();
                }
                catch (FileNotFoundException e2) {
                    securityPropsFile = new File(securityDir, "java.security");
                    throw e;
                }
            }
        }
        catch (FileNotFoundException e) {
            try {
                if (securityDir.exists()) {
                    if (!securityDir.isDirectory()) {
                        this.out.println("The installation program needs to create the directory");
                        this.out.println("  " + securityDir.getPath() + File.separator);
                        this.out.println("but a file already exists with that name.");
                        throw new RuntimeException("Could not create directory.");
                    }
                } else {
                    securityDir.mkdirs();
                }
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(securityPropsFile)));
                dos.writeBytes("# " + this.version_string + " (do not edit or delete this line)" + nl + "# This is the \"master security properties file\"." + nl + "#" + nl + "# The " + this.product_name + " installation program was unable to find an existing" + nl + "# java.security file, so it created this one." + nl + nl + "security.provider.1=" + this.provider_class + nl);
                dos.close();
                this.errorcode = 10;
                this.out.println("The file " + securityPropsFile);
                this.out.println("has been created.");
                return;
            }
            catch (IOException e2) {
                this.errorcode = 1;
                this.out.println("The file " + securityPropsFile);
                this.out.println("could not be created because of an I/O exception.");
                throw new RuntimeException(e2.toString());
            }
        }
        catch (IOException e) {
            this.errorcode = 1;
            this.out.println("Failed to load the java.security file.");
            throw new RuntimeException(e.toString());
        }
        catch (SecurityException e) {
            this.errorcode = 1;
            this.out.println("Not allowed to load the java.security file.");
            throw new RuntimeException(e.toString());
        }
        int nextProvider = 1;
        while ((value = properties.getProperty("security.provider." + nextProvider)) != null) {
            if (value.equals(this.provider_class)) {
                this.errorcode = 10;
                this.out.println(String.valueOf(this.product_name) + " is already installed.");
                return;
            }
            ++nextProvider;
        }
        if (properties.getProperty("security.provider." + (nextProvider + 1)) != null) {
            this.out.println("Warning: additional providers may have been added that were not previously");
            this.out.println("recognized, because a gap in the sequence of provider numbers has been filled.");
            this.out.println("You should edit the java.security file and check that it is correct.");
            this.out.println();
        }
        String linesToAdd = String.valueOf(nl) + "# Added by " + this.version_string + " installation program:" + nl + "security.provider." + nextProvider + "=" + this.provider_class + nl;
        try {
            RandomAccessFile file = new RandomAccessFile(securityPropsFile, "rw");
            long fileLen = securityPropsFile.length();
            file.seek(fileLen);
            file.writeBytes(linesToAdd);
            file.close();
            this.errorcode = 10;
            this.out.println("The following lines were added to");
            this.out.println("  " + securityPropsFile + ":");
            this.out.println(linesToAdd);
            this.out.println("To uninstall " + this.product_name + ", remove these lines manually.");
        }
        catch (IOException e) {
            this.errorcode = 1;
            this.out.println("The file " + securityPropsFile);
            this.out.println("could not be written to because of an I/O exception.");
            throw new RuntimeException(e.toString());
        }
        catch (SecurityException e) {
            this.errorcode = 1;
            this.out.println("The file " + securityPropsFile);
            this.out.println("could not be written to because of a security exception.");
            throw new RuntimeException(e.toString());
        }
    }
}

