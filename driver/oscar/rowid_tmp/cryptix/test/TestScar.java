/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.tools.Scar;
import cryptix.util.test.BaseTest;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TestScar
extends BaseTest {
    public static boolean DEBUG = false;
    public static final String tmpArch = "tmpARCH.scar";
    public static final String tmpDirS_in = "scarIN.tmp";
    public static final String tmpDirS_out = "scarOUT.tmp";
    public static final String tFile_1 = "file_1";
    public static final String tFile_2 = "file_2";
    public static final String tFile_3 = "file_3";
    public static final String testData_1 = "Marry had a little lamb";
    public static final String testData_2 = "Burp!, pardon me...";
    public static final char[] testData_3 = new char[]{'\u0001', '\u0002', '\u00f3', '\u00f4'};
    public static final String[] inParams = new String[]{"-er", "-p", "blabla", "scarIN.tmp", "tmpARCH.scar"};
    public static final String[] outParams = new String[]{"-d", "-p", "blabla", "tmpARCH.scar", "scarOUT.tmp"};

    public void localWriter(File toWrite, Object contents) throws IOException {
        String tmpString;
        FileWriter tmpWriter = new FileWriter(toWrite);
        Class<?> tmpClass = contents.getClass();
        String string = tmpString = tmpClass.getComponentType() == null ? null : tmpClass.getComponentType().toString();
        if (DEBUG) {
            this.out.print("TestScar.localWriter(file, ");
            if (tmpString == null) {
                this.out.println(String.valueOf(tmpClass.getName()) + ")");
            } else {
                this.out.println(String.valueOf(tmpString) + "[])");
            }
        }
        if (contents instanceof String) {
            tmpWriter.write((String)contents);
        } else if (tmpString.equals("char")) {
            tmpWriter.write((char[])contents);
        } else {
            tmpWriter.write(">> Unknown data of " + tmpString + "[] type given as input <<");
        }
        tmpWriter.flush();
        tmpWriter.close();
    }

    public String localReader(File toRead) throws IOException {
        int tempChar;
        FileReader tmpReader = new FileReader(toRead);
        StringBuffer tempSB = new StringBuffer();
        while ((tempChar = tmpReader.read()) > -1) {
            tempSB.append((char)tempChar);
        }
        return tempSB.length() > 0 ? tempSB.toString() : null;
    }

    protected void engineTest() throws Exception {
        File tempDir = new File(tmpDirS_in);
        File filek_1 = new File(tmpDirS_in, tFile_1);
        File filek_2 = new File(tmpDirS_in, tFile_2);
        File filek_3 = new File(tmpDirS_in, tFile_3);
        this.setExpectedPasses(3);
        try {
            if (tempDir.mkdirs() && DEBUG) {
                this.out.println("\nDirectory scarIN.tmp created.");
            }
            this.localWriter(filek_1, testData_1);
            this.localWriter(filek_2, testData_2);
            this.localWriter(filek_3, testData_3);
        }
        catch (Exception e) {
            this.error("Can't write test files: " + e.getMessage());
            System.exit(1);
        }
        if (DEBUG) {
            this.out.println("TestScar> Starting Scar()");
        }
        Scar.DEBUG = false;
        Scar jc = new Scar();
        jc.processOptions(inParams);
        jc.run();
        if (DEBUG) {
            this.out.println("Encrypted. Now decrypting...");
        }
        if (filek_1.delete() && filek_2.delete() && filek_3.delete() && tempDir.delete()) {
            if (DEBUG) {
                this.out.println("TestScar> Test files deleted.");
            }
        } else {
            System.err.println("TestScar> Warning: Unable to delete all test files!");
        }
        tempDir = new File(tmpDirS_out);
        filek_1 = new File(tmpDirS_out, tFile_1);
        filek_2 = new File(tmpDirS_out, tFile_2);
        filek_3 = new File(tmpDirS_out, tFile_3);
        if (tempDir.mkdirs() && DEBUG) {
            this.out.println("Directory scarOUT.tmp created.");
        }
        jc = new Scar();
        jc.processOptions(outParams);
        jc.run();
        this.passIf(this.localReader(filek_1).equals(testData_1), "Scar file 1 OK");
        this.passIf(this.localReader(filek_2).equals(testData_2), "Scar file 2 OK");
        this.passIf(this.localReader(filek_3).equals(new String(testData_3)), "Scar file 3 OK");
        if (filek_1.delete() && filek_2.delete() && filek_3.delete() && tempDir.delete() && new File(tmpArch).delete()) {
            if (DEBUG) {
                this.out.println("TestScar> Test files deleted.");
            }
        } else {
            System.err.println("TestScar> Warning: Unable to delete all test files!");
        }
    }

    public static void main(String[] argv) {
        new TestScar().commandline(argv);
    }
}

