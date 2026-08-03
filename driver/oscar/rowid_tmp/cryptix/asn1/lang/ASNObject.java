/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.lang;

import cryptix.asn1.lang.Node;
import cryptix.asn1.lang.ParserVisitor;
import cryptix.asn1.lang.Tag;
import java.io.IOException;

public interface ASNObject
extends Node {
    public ASNObject getParent();

    public ASNObject[] getChildren();

    public int getID();

    public String getName();

    public ASNObject getComponent(String var1);

    public void setTag(Tag var1);

    public Tag getTag();

    public boolean isOptional();

    public void setOptional(boolean var1);

    public void setValue(Object var1);

    public Object getValue();

    public void setDefaultValue(Object var1);

    public Object getDefaultValue();

    public void dump();

    public void dump(String var1);

    public Object accept(ParserVisitor var1, Object var2) throws IOException;
}

