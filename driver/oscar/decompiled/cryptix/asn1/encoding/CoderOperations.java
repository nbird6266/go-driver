/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.encoding;

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
import cryptix.asn1.lang.ASNTaggedType;
import cryptix.asn1.lang.ASNTime;
import cryptix.asn1.lang.ParserVisitor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface CoderOperations
extends ParserVisitor {
    public static final int UNINITIALIZED = 0;
    public static final int DECODING = 1;
    public static final int ENCODING = 2;

    public void init(OutputStream var1);

    public void encode(ASNBoolean var1, OutputStream var2) throws IOException;

    public void encode(ASNInteger var1, OutputStream var2) throws IOException;

    public void encode(ASNBitString var1, OutputStream var2) throws IOException;

    public void encode(ASNOctetString var1, OutputStream var2) throws IOException;

    public void encode(ASNNull var1, OutputStream var2) throws IOException;

    public void encode(ASNObjectIdentifier var1, OutputStream var2) throws IOException;

    public void encode(ASNSequence var1, OutputStream var2) throws IOException;

    public void encode(ASNSequenceOf var1, OutputStream var2) throws IOException;

    public void encode(ASNSet var1, OutputStream var2) throws IOException;

    public void encode(ASNSetOf var1, OutputStream var2) throws IOException;

    public void encode(ASNTaggedType var1, OutputStream var2) throws IOException;

    public void encode(ASNAny var1, OutputStream var2) throws IOException;

    public void encode(ASNPrintableString var1, OutputStream var2) throws IOException;

    public void encode(ASNTime var1, OutputStream var2) throws IOException;

    public void init(InputStream var1);

    public void decode(ASNBoolean var1, InputStream var2) throws IOException;

    public void decode(ASNInteger var1, InputStream var2) throws IOException;

    public void decode(ASNBitString var1, InputStream var2) throws IOException;

    public void decode(ASNOctetString var1, InputStream var2) throws IOException;

    public void decode(ASNNull var1, InputStream var2) throws IOException;

    public void decode(ASNObjectIdentifier var1, InputStream var2) throws IOException;

    public void decode(ASNSequence var1, InputStream var2) throws IOException;

    public void decode(ASNSequenceOf var1, InputStream var2) throws IOException;

    public void decode(ASNSet var1, InputStream var2) throws IOException;

    public void decode(ASNSetOf var1, InputStream var2) throws IOException;

    public void decode(ASNTaggedType var1, InputStream var2) throws IOException;

    public void decode(ASNAny var1, InputStream var2) throws IOException;

    public void decode(ASNPrintableString var1, InputStream var2) throws IOException;

    public void decode(ASNTime var1, InputStream var2) throws IOException;
}

