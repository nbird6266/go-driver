/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.encoding;

import cryptix.asn1.encoding.CoderOperations;
import cryptix.asn1.encoding.PackageProperties;
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
import cryptix.asn1.lang.Parser;
import cryptix.asn1.lang.SimpleNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class BaseCoder
implements CoderOperations {
    private static final String NAME = "BaseCoder";
    private static final boolean IN = true;
    private static final boolean OUT = false;
    private static final boolean DEBUG = false;
    private static final int debuglevel = 0;
    private static final PrintWriter err = null;
    private static final boolean TRACE = PackageProperties.isTraceable("BaseCoder");
    private static final String ENCODING_HOME = "asn.1.encoding.home";
    protected InputStream in;
    protected OutputStream out;
    private int state = 0;

    private static void debug(String s) {
        err.println(">>> BaseCoder: " + s);
    }

    private static void trace(boolean in, String s) {
        if (TRACE) {
            err.println(String.valueOf(in ? "==> " : "<== ") + NAME + "." + s);
        }
    }

    private static void trace(String s) {
        if (TRACE) {
            err.println("<=> BaseCoder." + s);
        }
    }

    public static CoderOperations getInstance(String anEncoding) {
        int i = anEncoding.lastIndexOf(46);
        if (i == -1) {
            String home = PackageProperties.getProperty(ENCODING_HOME);
            anEncoding = String.valueOf(home) + "." + anEncoding;
        }
        CoderOperations result = null;
        try {
            result = (CoderOperations)Class.forName(anEncoding).newInstance();
        }
        catch (Throwable t) {
            BaseCoder.debug("Unable to instantiate " + anEncoding + " coder");
            BaseCoder.debug(t.toString());
            BaseCoder.debug(t.getMessage());
            t.printStackTrace(err);
        }
        return result;
    }

    public int getState() {
        return this.state;
    }

    public Object visit(SimpleNode x, Object data) throws IOException {
        String msg = "Don't know how to visit " + x;
        throw new RuntimeException(msg);
    }

    public Object visit(ASNSpecification node, Object data) throws IOException {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASNTypeAlias node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNType node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNBoolean node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNInteger node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNBitString node, Object data) throws IOException {
        return node.childrenAccept(this, data);
    }

    public Object visit(ASNOctetString node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNNull node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNObjectIdentifier node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNSequence node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNSequenceOf node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNSet node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNSetOf node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNTaggedType node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNAny node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNPrintableString node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    public Object visit(ASNTime node, Object data) throws IOException {
        return this.visitInternal(node, data);
    }

    protected Object visitInternal(SimpleNode node, Object data) throws IOException {
        block37: {
            block38: {
                block36: {
                    if (this.state != 2) break block36;
                    switch (node.getID()) {
                        case 2: {
                            this.encode((ASNType)node, this.out);
                            break block37;
                        }
                        case 3: {
                            this.encode((ASNTypeAlias)node, this.out);
                            break block37;
                        }
                        case 4: {
                            this.encode((ASNBoolean)node, this.out);
                            break block37;
                        }
                        case 5: {
                            this.encode((ASNInteger)node, this.out);
                            break block37;
                        }
                        case 6: {
                            this.encode((ASNBitString)node, this.out);
                            break block37;
                        }
                        case 7: {
                            this.encode((ASNOctetString)node, this.out);
                            break block37;
                        }
                        case 8: {
                            this.encode((ASNNull)node, this.out);
                            break block37;
                        }
                        case 9: {
                            this.encode((ASNObjectIdentifier)node, this.out);
                            break block37;
                        }
                        case 10: {
                            this.encode((ASNSequence)node, this.out);
                            break block37;
                        }
                        case 11: {
                            this.encode((ASNSequenceOf)node, this.out);
                            break block37;
                        }
                        case 12: {
                            this.encode((ASNSet)node, this.out);
                            break block37;
                        }
                        case 13: {
                            this.encode((ASNSetOf)node, this.out);
                            break block37;
                        }
                        case 14: {
                            this.encode((ASNTaggedType)node, this.out);
                            break block37;
                        }
                        case 15: {
                            this.encode((ASNAny)node, this.out);
                            break block37;
                        }
                        case 16: {
                            this.encode((ASNPrintableString)node, this.out);
                            break block37;
                        }
                        case 17: {
                            this.encode((ASNTime)node, this.out);
                            break block37;
                        }
                        default: {
                            String msg = "Don't know how to encode " + node;
                            throw new RuntimeException(msg);
                        }
                    }
                }
                if (this.state != 1) break block38;
                switch (node.getID()) {
                    case 2: {
                        this.decode((ASNType)node, this.in);
                        break block37;
                    }
                    case 3: {
                        this.decode((ASNTypeAlias)node, this.in);
                        break block37;
                    }
                    case 4: {
                        this.decode((ASNBoolean)node, this.in);
                        break block37;
                    }
                    case 5: {
                        this.decode((ASNInteger)node, this.in);
                        break block37;
                    }
                    case 6: {
                        this.decode((ASNBitString)node, this.in);
                        break block37;
                    }
                    case 7: {
                        this.decode((ASNOctetString)node, this.in);
                        break block37;
                    }
                    case 8: {
                        this.decode((ASNNull)node, this.in);
                        break block37;
                    }
                    case 9: {
                        this.decode((ASNObjectIdentifier)node, this.in);
                        break block37;
                    }
                    case 10: {
                        this.decode((ASNSequence)node, this.in);
                        break block37;
                    }
                    case 11: {
                        this.decode((ASNSequenceOf)node, this.in);
                        break block37;
                    }
                    case 12: {
                        this.decode((ASNSet)node, this.in);
                        break block37;
                    }
                    case 13: {
                        this.decode((ASNSetOf)node, this.in);
                        break block37;
                    }
                    case 14: {
                        this.decode((ASNTaggedType)node, this.in);
                        break block37;
                    }
                    case 15: {
                        this.decode((ASNAny)node, this.in);
                        break block37;
                    }
                    case 16: {
                        this.decode((ASNPrintableString)node, this.in);
                        break block37;
                    }
                    case 17: {
                        this.decode((ASNTime)node, this.in);
                        break block37;
                    }
                    default: {
                        String msg = "Don't know how to decode " + node;
                        throw new RuntimeException(msg);
                    }
                }
            }
            throw new IllegalStateException();
        }
        return node.getValue();
    }

    public void init(OutputStream os) {
        this.out = os;
        this.state = 2;
    }

    public void init(InputStream is) {
        this.in = is;
        this.state = 1;
    }

    public void encode(ASNType obj, OutputStream out) throws IOException {
    }

    public void encode(ASNTypeAlias obj, OutputStream out) throws IOException {
    }

    public void encode(ASNBoolean obj, OutputStream out) throws IOException {
    }

    public void encode(ASNInteger obj, OutputStream out) throws IOException {
    }

    public void encode(ASNBitString obj, OutputStream out) throws IOException {
    }

    public void encode(ASNOctetString obj, OutputStream out) throws IOException {
    }

    public void encode(ASNNull obj, OutputStream out) throws IOException {
    }

    public void encode(ASNObjectIdentifier obj, OutputStream out) throws IOException {
    }

    public void encode(ASNSequence obj, OutputStream out) throws IOException {
    }

    public void encode(ASNSequenceOf obj, OutputStream out) throws IOException {
    }

    public void encode(ASNSet obj, OutputStream out) throws IOException {
    }

    public void encode(ASNSetOf obj, OutputStream out) throws IOException {
    }

    public void encode(ASNTaggedType obj, OutputStream out) throws IOException {
    }

    public void encode(ASNAny obj, OutputStream out) throws IOException {
    }

    public void encode(ASNPrintableString obj, OutputStream out) throws IOException {
    }

    public void encode(ASNTime obj, OutputStream out) throws IOException {
    }

    public void decode(ASNBoolean obj, InputStream in) throws IOException {
    }

    public void decode(ASNInteger obj, InputStream in) throws IOException {
    }

    public void decode(ASNBitString obj, InputStream in) throws IOException {
    }

    public void decode(ASNOctetString obj, InputStream in) throws IOException {
    }

    public void decode(ASNNull obj, InputStream in) throws IOException {
    }

    public void decode(ASNObjectIdentifier obj, InputStream in) throws IOException {
    }

    public void decode(ASNSequence obj, InputStream in) throws IOException {
    }

    public void decode(ASNSequenceOf obj, InputStream in) throws IOException {
    }

    public void decode(ASNSet obj, InputStream in) throws IOException {
    }

    public void decode(ASNSetOf obj, InputStream in) throws IOException {
    }

    public void decode(ASNTaggedType obj, InputStream in) throws IOException {
    }

    public void decode(ASNAny obj, InputStream in) throws IOException {
    }

    public void decode(ASNPrintableString obj, InputStream in) throws IOException {
    }

    public void decode(ASNTime obj, InputStream in) throws IOException {
    }

    public void decode(ASNType obj, InputStream in) throws IOException {
        SimpleNode x;
        boolean optional = obj.isOptional();
        String refType = obj.getName();
        if (refType != null) {
            obj.getParser();
            x = (SimpleNode)Parser.resolve(refType);
        } else {
            x = (SimpleNode)obj.getChild(0);
        }
        x.setOptional(optional);
        Object value = this.visitInternal(x, null);
        obj.setValue(value);
    }

    public void decode(ASNTypeAlias obj, InputStream in) throws IOException {
        obj.getParser();
        SimpleNode x = (SimpleNode)Parser.resolve(obj.getName());
        Object value = this.visitInternal(x, null);
        obj.setValue(value);
    }
}

