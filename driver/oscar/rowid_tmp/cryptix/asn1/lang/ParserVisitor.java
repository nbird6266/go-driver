/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.lang;

import cryptix.asn1.lang.ASNAny;
import cryptix.asn1.lang.ASNBitString;
import cryptix.asn1.lang.ASNBoolean;
import cryptix.asn1.lang.ASNInteger;
import cryptix.asn1.lang.ASNNull;
import cryptix.asn1.lang.ASNObjectIdentifier;
import cryptix.asn1.lang.ASNOctetString;
import cryptix.asn1.lang.ASNPrintableString;
import cryptix.asn1.lang.ASNSequence;
import cryptix.asn1.lang.ASNSequenceOf;
import cryptix.asn1.lang.ASNSet;
import cryptix.asn1.lang.ASNSetOf;
import cryptix.asn1.lang.ASNSpecification;
import cryptix.asn1.lang.ASNTaggedType;
import cryptix.asn1.lang.ASNTime;
import cryptix.asn1.lang.ASNType;
import cryptix.asn1.lang.ASNTypeAlias;
import cryptix.asn1.lang.SimpleNode;
import java.io.IOException;

public interface ParserVisitor {
    public Object visit(SimpleNode var1, Object var2) throws IOException;

    public Object visit(ASNSpecification var1, Object var2) throws IOException;

    public Object visit(ASNType var1, Object var2) throws IOException;

    public Object visit(ASNTypeAlias var1, Object var2) throws IOException;

    public Object visit(ASNBoolean var1, Object var2) throws IOException;

    public Object visit(ASNInteger var1, Object var2) throws IOException;

    public Object visit(ASNBitString var1, Object var2) throws IOException;

    public Object visit(ASNOctetString var1, Object var2) throws IOException;

    public Object visit(ASNNull var1, Object var2) throws IOException;

    public Object visit(ASNObjectIdentifier var1, Object var2) throws IOException;

    public Object visit(ASNSequence var1, Object var2) throws IOException;

    public Object visit(ASNSequenceOf var1, Object var2) throws IOException;

    public Object visit(ASNSet var1, Object var2) throws IOException;

    public Object visit(ASNSetOf var1, Object var2) throws IOException;

    public Object visit(ASNTaggedType var1, Object var2) throws IOException;

    public Object visit(ASNAny var1, Object var2) throws IOException;

    public Object visit(ASNPrintableString var1, Object var2) throws IOException;

    public Object visit(ASNTime var1, Object var2) throws IOException;
}

