/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.test;

import cryptix.asn1.encoding.BaseCoder;
import cryptix.asn1.encoding.CoderOperations;
import cryptix.asn1.lang.ASNObject;
import cryptix.asn1.lang.ASNSpecification;
import cryptix.asn1.lang.ParseException;
import cryptix.asn1.lang.Parser;
import cryptix.util.core.Hex;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Vector;

public class Main2 {
    private static final boolean CRYPTIX_ASN = false;

    public static void main(String[] args) {
        Parser parser;
        String definitions = "cryptix.asn";
        String data = "Duke.x509";
        String CERTIFICATE = "Certificate";
        String CERTIFICATE_INFO = "certificateInfo";
        if (args.length != 2) {
            definitions = "x509useful.asn";
            data = "1eecert.x509";
            CERTIFICATE = "UsefulCertificate";
            CERTIFICATE_INFO = "tbsCertificate";
        } else {
            definitions = args[0];
            data = args[1];
        }
        System.out.println();
        System.out.println("1. Parsing \"" + definitions + "\"...");
        try {
            parser = new Parser(new FileInputStream(definitions));
        }
        catch (FileNotFoundException e) {
            System.out.println("*** \"" + definitions + "\" not found in work directory...");
            return;
        }
        ASNSpecification x = null;
        try {
            x = parser.Specification(false);
            System.out.println();
            System.out.println("2. Result of compilation: ");
            x.dump();
            System.out.println();
            System.out.println("3. Current symbol table:");
            Parser.dumpSymbolTable();
            System.out.println();
            int it = Parser.countUnresolvedReferences();
            System.out.println("4. Number of unresolved references: " + it);
        }
        catch (ParseException e) {
            System.out.println("*** Failed parsing...");
            System.out.println("*** " + e.getMessage() + "...");
            e.printStackTrace(System.out);
        }
        System.out.println();
        System.out.println("5. Can we access a \"Certificate\" from an x509 struct?");
        ASNObject x509 = x.getComponent(CERTIFICATE);
        x509.dump("Certificate --> ");
        System.out.println();
        System.out.println("6. Can we access \"" + data + "\" ?");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(data);
        }
        catch (FileNotFoundException e) {
            System.out.println("*** \"" + data + "\" not found in work directory...");
            return;
        }
        System.out.println("Yes...");
        System.out.println();
        System.out.println("7. Can we access the x509 instance present inside?");
        CoderOperations der = BaseCoder.getInstance("DER");
        if (der == null) {
            return;
        }
        der.init(fis);
        try {
            x509.accept(der, null);
            x509.dump();
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
        ASNObject tbsCert = x509.getComponent(String.valueOf(CERTIFICATE) + "." + CERTIFICATE_INFO);
        byte[] ob = null;
        ob = (byte[])tbsCert.getValue();
        System.out.println(String.valueOf(CERTIFICATE) + " encoding: \"" + Hex.toString(ob) + "\"");
        try {
            tbsCert = x.getComponent("UsefulTBSCertificate");
            ByteArrayInputStream is = new ByteArrayInputStream(ob);
            der.init(is);
            tbsCert.dump("UsefulTBSCertificate--> ");
            tbsCert.accept(der, null);
            tbsCert.dump();
            ASNObject issuer = x.getComponent("UsefulTBSCertificate.issuer");
            byte[] issuer_bytes = (byte[])issuer.getValue();
            System.out.println("Issuer encoding: " + Hex.toString(issuer_bytes));
            ASNObject subject = x.getComponent("UsefulTBSCertificate.subject");
            byte[] subject_bytes = (byte[])subject.getValue();
            System.out.println("Subject encoding: " + Hex.toString(subject_bytes));
            ASNObject a = x.getComponent("UsefulTBSCertificate.extensions");
            Vector v = (Vector)a.getValue();
            int i = 0;
            while (i < v.size()) {
                Vector v2 = (Vector)v.elementAt(i);
                byte[] extnval = (byte[])v2.elementAt(0);
                System.out.println("Extn encoding: " + Hex.toString(extnval));
                ++i;
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
        ASNObject sig = x509.getComponent(String.valueOf(CERTIFICATE) + ".signature");
        ob = (byte[])sig.getValue();
        System.out.println("Signature encoding: \"" + Hex.toString(ob) + "\"");
        System.out.println();
        System.out.println("Done all tests...");
    }
}

