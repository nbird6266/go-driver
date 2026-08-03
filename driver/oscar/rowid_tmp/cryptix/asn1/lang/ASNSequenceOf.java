/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.lang;

import cryptix.asn1.lang.Parser;
import cryptix.asn1.lang.ParserVisitor;
import cryptix.asn1.lang.SimpleNode;
import java.io.IOException;

public class ASNSequenceOf
extends SimpleNode {
    public ASNSequenceOf(int id) {
        super(id);
    }

    public ASNSequenceOf(Parser p, int id) {
        super(p, id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) throws IOException {
        return visitor.visit(this, data);
    }
}

