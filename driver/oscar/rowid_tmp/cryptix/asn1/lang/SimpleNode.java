/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.lang;

import cryptix.asn1.lang.ASNBitString;
import cryptix.asn1.lang.ASNBoolean;
import cryptix.asn1.lang.ASNInteger;
import cryptix.asn1.lang.ASNNull;
import cryptix.asn1.lang.ASNObject;
import cryptix.asn1.lang.ASNObjectIdentifier;
import cryptix.asn1.lang.ASNOctetString;
import cryptix.asn1.lang.ASNPrintableString;
import cryptix.asn1.lang.ASNSequence;
import cryptix.asn1.lang.ASNSequenceOf;
import cryptix.asn1.lang.ASNSet;
import cryptix.asn1.lang.ASNSetOf;
import cryptix.asn1.lang.ASNTaggedType;
import cryptix.asn1.lang.ASNTime;
import cryptix.asn1.lang.ASNType;
import cryptix.asn1.lang.ASNTypeAlias;
import cryptix.asn1.lang.Node;
import cryptix.asn1.lang.PackageProperties;
import cryptix.asn1.lang.Parser;
import cryptix.asn1.lang.ParserTreeConstants;
import cryptix.asn1.lang.ParserVisitor;
import cryptix.asn1.lang.Tag;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

public class SimpleNode
implements ASNObject {
    private static final String NAME = "SimpleNode";
    private static final boolean IN = true;
    private static final boolean OUT = false;
    private static final boolean DEBUG = false;
    private static final int debuglevel = 0;
    private static final PrintWriter err = null;
    private static final boolean TRACE = PackageProperties.isTraceable("SimpleNode");
    protected ASNObject parent;
    protected ASNObject[] children;
    protected int id;
    protected Parser parser;
    protected String name;
    protected Tag tag;
    protected boolean optional;
    protected Object value;
    protected Object defaultValue;

    private static void debug(String s) {
        err.println(">>> SimpleNode: " + s);
    }

    private static void trace(boolean in, String s) {
        if (TRACE) {
            err.println(String.valueOf(in ? "==> " : "<== ") + NAME + "." + s);
        }
    }

    private static void trace(String s) {
        if (TRACE) {
            err.println("<=> SimpleNode." + s);
        }
    }

    public SimpleNode(int i) {
        this.id = i;
    }

    public SimpleNode(Parser p, int i) {
        this(i);
        this.parser = p;
    }

    public static final SimpleNode getInstance(Parser p, Tag tag) {
        int clazz = tag.getClazz();
        if (clazz != 0) {
            return null;
        }
        int value = tag.getValue();
        SimpleNode result = null;
        switch (value) {
            case 1: {
                result = new ASNBoolean(p, 4);
                break;
            }
            case 2: {
                result = new ASNInteger(p, 5);
                break;
            }
            case 3: {
                result = new ASNBitString(p, 6);
                break;
            }
            case 4: {
                result = new ASNOctetString(p, 7);
                break;
            }
            case 5: {
                result = new ASNNull(p, 8);
                break;
            }
            case 6: {
                result = new ASNObjectIdentifier(p, 9);
                break;
            }
            case 16: {
                result = new ASNSequence(p, 10);
                break;
            }
            case 48: {
                result = new ASNSequenceOf(p, 11);
                break;
            }
            case 17: {
                result = new ASNSet(p, 12);
                break;
            }
            case 49: {
                result = new ASNSetOf(p, 13);
                break;
            }
            case 19: 
            case 20: 
            case 22: {
                result = new ASNPrintableString(p, 16);
                break;
            }
            case 23: {
                result = new ASNTime(p, 17);
            }
        }
        return result;
    }

    public void jjtOpen() {
    }

    public void jjtClose() {
    }

    public void jjtSetParent(Node n) {
        this.parent = (ASNObject)n;
    }

    public Node jjtGetParent() {
        return this.parent;
    }

    public ASNObject getParent() {
        SimpleNode result = this;
        while ((result = (SimpleNode)result.jjtGetParent()) != null && result.id == 2) {
        }
        return result;
    }

    public void jjtAddChild(Node n, int i) {
        if (this.children == null) {
            this.children = new ASNObject[i + 1];
        } else if (i >= this.children.length) {
            ASNObject[] c = new ASNObject[i + 1];
            System.arraycopy(this.children, 0, c, 0, this.children.length);
            this.children = c;
        }
        this.children[i] = (ASNObject)n;
    }

    public Node jjtGetChild(int i) {
        return this.children[i];
    }

    public ASNObject getChild(int i) {
        return (ASNObject)this.jjtGetChild(i);
    }

    public ASNObject[] getChildren() {
        return this.children;
    }

    public int jjtGetNumChildren() {
        return this.children == null ? 0 : this.children.length;
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) throws IOException {
        return visitor.visit(this, data);
    }

    public Object childrenAccept(ParserVisitor visitor, Object data) throws IOException {
        Vector<Object> result = new Vector<Object>();
        if (this.children != null) {
            int i = 0;
            while (i < this.children.length) {
                this.children[i].jjtAccept(visitor, data);
                result.addElement(this.children[i].getValue());
                ++i;
            }
        }
        return result;
    }

    public String toString() {
        String result = "<ASNObject name=\"" + this.name + "\" type=\"";
        result = String.valueOf(result) + ParserTreeConstants.jjtNodeName[this.id] + "\"";
        result = String.valueOf(result) + " default=\"" + this.defaultValue + "\"";
        result = String.valueOf(result) + " optional=\"" + (this.optional ? "yes" : "no") + "\" />";
        return result;
    }

    public String toString(String prefix) {
        String result = String.valueOf(prefix) + this.toString();
        return result;
    }

    public Parser getParser() {
        return this.parser;
    }

    public int getID() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public ASNObject getComponent(String aName) {
        ASNObject result = (ASNObject)Parser.resolve(aName);
        return result;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public Tag getTag() {
        return this.tag;
    }

    public boolean isOptional() {
        return this.optional;
    }

    public void setOptional(boolean flag) {
        this.optional = flag;
    }

    public void setValue(Object value) {
        if (this.value != null) {
            // empty if block
        }
        this.value = value;
    }

    public Object getValue() {
        Object result = null;
        if (this.id == 3) {
            SimpleNode n = (SimpleNode)Parser.resolve(this.name);
            if (n != null) {
                result = n.getValue();
            }
        } else {
            result = this.value;
        }
        return result;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
        if (this instanceof ASNType) {
            SimpleNode x = (SimpleNode)this.children[0];
            return x.defaultValue;
        }
        if (this instanceof ASNTaggedType) {
            SimpleNode x = (SimpleNode)Parser.resolve(this.name);
            return x.defaultValue;
        }
        if (this instanceof ASNTypeAlias) {
            SimpleNode x = (SimpleNode)Parser.resolve(this.name);
            return x.defaultValue;
        }
        return this.defaultValue;
    }

    public void dump() {
        System.out.println(this.toString());
    }

    public void dump(String prefix) {
        if (this instanceof ASNType) {
            this.dumpType(this, prefix);
        } else if (this instanceof ASNTaggedType) {
            this.dumpTaggedType(this, prefix);
        } else if (this instanceof ASNTypeAlias) {
            this.dumpTypeAlias(this, prefix);
        } else {
            this.dumpSimpleNode(this, prefix);
        }
    }

    public Object accept(ParserVisitor visitor, Object data) throws IOException {
        return this.jjtAccept(visitor, data);
    }

    private void dumpType(SimpleNode n, String prefix) {
        SimpleNode x = (SimpleNode)n.children[0];
        x.dump(prefix);
    }

    private void dumpTaggedType(SimpleNode n, String prefix) {
        SimpleNode x = (SimpleNode)Parser.resolve(n.name);
        System.out.println(x.toString(String.valueOf(prefix) + n.getTag()));
    }

    private void dumpTypeAlias(SimpleNode n, String prefix) {
        SimpleNode x = (SimpleNode)Parser.resolve(n.name);
        x.dump(prefix);
    }

    private void dumpSimpleNode(SimpleNode n, String prefix) {
        System.out.println(n.toString(prefix));
        this.dumpChildren(this.children, prefix);
    }

    private void dumpChildren(ASNObject[] children, String prefix) {
        if (children == null) {
            return;
        }
        prefix = String.valueOf(prefix) + "+ ";
        int limit = children.length;
        int i = 0;
        while (i < limit) {
            SimpleNode x = (SimpleNode)children[i];
            if (x != null) {
                x.dump(prefix);
            }
            ++i;
        }
    }
}

