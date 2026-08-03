/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.lang;

import cryptix.asn1.lang.Parser;
import cryptix.asn1.lang.ParserVisitor;
import cryptix.asn1.lang.SimpleNode;
import java.io.IOException;

public class ASNPrintableString
extends SimpleNode {
    public ASNPrintableString(int id) {
        super(id);
    }

    public ASNPrintableString(Parser p, int id) {
        super(p, id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) throws IOException {
        return visitor.visit(this, data);
    }
}

