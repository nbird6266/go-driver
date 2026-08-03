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
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {
        Parser parser;
        args = new String[]{"x509.asn", "1eecert.x509"};
        String definitions = args[0];
        String data = args[1];
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
        ASNObject x509 = x.getComponent("Certificate");
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
            System.out.println();
            System.out.println("8. Can we identify its signature algorithm?");
            System.out.println();
            ASNObject result = null;
            ASNObject tbsCertificate = x509.getComponent("Certificate.tbsCertificate");
            tbsCertificate.dump("8.1. Certificate.tbsCertificate --> ");
            System.out.println();
            ASNObject signature = tbsCertificate.getComponent("TBSCertificate.signature");
            signature.dump("8.2. TBSCertificate.signature --> ");
            System.out.println();
            result = signature.getComponent("AlgorithmIdentifier.algorithm");
            result.dump("8.3. AlgorithmIdentifier.algorithm --> ");
            System.out.println();
            System.out.println("Issuer's signature algorithm OID (#1) is: " + result.getValue());
            System.out.println();
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
        System.out.println();
        System.out.println("Done all tests...");
    }
}

