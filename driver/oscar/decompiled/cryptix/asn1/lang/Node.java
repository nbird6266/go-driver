/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.lang;

import cryptix.asn1.lang.ParserVisitor;
import java.io.IOException;

public interface Node {
    public void jjtOpen();

    public void jjtClose();

    public void jjtSetParent(Node var1);

    public Node jjtGetParent();

    public void jjtAddChild(Node var1, int var2);

    public Node jjtGetChild(int var1);

    public int jjtGetNumChildren();

    public Object jjtAccept(ParserVisitor var1, Object var2) throws IOException;
}

