/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.lang;

import cryptix.asn1.lang.ASCII_CharStream;
import cryptix.asn1.lang.ASNAny;
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
import cryptix.asn1.lang.ASNSpecification;
import cryptix.asn1.lang.ASNTaggedType;
import cryptix.asn1.lang.ASNTime;
import cryptix.asn1.lang.ASNType;
import cryptix.asn1.lang.ASNTypeAlias;
import cryptix.asn1.lang.JJTParserState;
import cryptix.asn1.lang.Node;
import cryptix.asn1.lang.PackageProperties;
import cryptix.asn1.lang.ParseException;
import cryptix.asn1.lang.ParserConstants;
import cryptix.asn1.lang.ParserTokenManager;
import cryptix.asn1.lang.ParserTreeConstants;
import cryptix.asn1.lang.SimpleNode;
import cryptix.asn1.lang.Tag;
import cryptix.asn1.lang.Token;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Parser
implements ParserTreeConstants,
ParserConstants {
    protected JJTParserState jjtree = new JJTParserState();
    private static final String NAME = "Parser";
    private static final boolean IN = true;
    private static final boolean OUT = false;
    private static final boolean DEBUG = false;
    private static final int debuglevel = 0;
    private static final PrintWriter err = null;
    private static final boolean TRACE = PackageProperties.isTraceable("Parser");
    static Hashtable sTable;
    static final boolean DEFAULT_TAG_TYPE = false;
    boolean isTagExplicit = false;
    static Object[] stack;
    static int top;
    public ParserTokenManager token_source;
    ASCII_CharStream jj_input_stream;
    public Token token;
    public Token jj_nt;
    private int jj_ntk;
    private Token jj_scanpos;
    private Token jj_lastpos;
    private int jj_la;
    public boolean lookingAhead = false;
    private boolean jj_semLA;
    private int jj_gen;
    private final int[] jj_la1 = new int[39];
    private final int[] jj_la1_0;
    private final int[] jj_la1_1;
    private final JJCalls[] jj_2_rtns;
    private boolean jj_rescan;
    private int jj_gc;
    private Vector jj_expentries;
    private int[] jj_expentry;
    private int jj_kind;
    private int[] jj_lasttokens;
    private int jj_endpos;
    private int trace_indent;
    private boolean trace_enabled;

    static {
        stack = new Object[64];
        top = -1;
    }

    private static void debug(String s) {
        err.println(">>> Parser: " + s);
    }

    private static void trace(boolean in, String s) {
        if (TRACE) {
            err.println(String.valueOf(in ? "==> " : "<== ") + NAME + "." + s);
        }
    }

    private static void trace(String s) {
        if (TRACE) {
            err.println("<=> Parser." + s);
        }
    }

    public static void dumpSymbolTable() {
        Enumeration symbols = sTable.keys();
        int i = 1;
        while (symbols.hasMoreElements()) {
            String id = (String)symbols.nextElement();
            Object obj = sTable.get(id);
            System.out.println("\t" + i++ + ". " + id + " = " + obj);
        }
        System.out.println();
    }

    /*
     * WARNING - void declaration
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void define(String name, Object type) {
        void var3_6;
        Object v = sTable.get(name);
        if (v == null) {
            Object object = type;
        } else if (v instanceof String) {
            if (!(type instanceof SimpleNode)) throw new RuntimeException("Unhandled 1");
            SimpleNode n = (SimpleNode)type;
            if (n.getName() != null) {
                throw new RuntimeException("unable to re-assign names...");
            }
            n.name = (String)v;
            Object object = type;
        } else if (v instanceof ASNAny || v instanceof ASNType) {
            if (!(type instanceof SimpleNode)) throw new RuntimeException("Unhandled 2");
            SimpleNode n = (SimpleNode)type;
            ((SimpleNode)v).setOptional(n.optional);
            ((SimpleNode)v).setDefaultValue(n.defaultValue);
        } else {
            if (!(v instanceof ASNObject) || !(type instanceof ASNType)) throw new RuntimeException("Unhandled 3: " + v + ", type: " + type);
            ((ASNObject)v).setDefaultValue(((ASNObject)type).getDefaultValue());
        }
        sTable.put(name, var3_6);
    }

    public static Object resolve(String aName) {
        Object result = sTable.get(aName);
        while (result instanceof String) {
            result = sTable.get((String)result);
        }
        return result;
    }

    public static int countUnresolvedReferences() {
        int it = 0;
        Enumeration symbols = sTable.keys();
        while (symbols.hasMoreElements()) {
            ASNObject[] theChildren;
            Object result = sTable.get((String)symbols.nextElement());
            if (!(result instanceof SimpleNode) || (theChildren = ((SimpleNode)result).children) == null) continue;
            int i = 0;
            while (i < theChildren.length) {
                String aName;
                result = theChildren[i];
                if (result instanceof ASNTypeAlias && Parser.resolve(aName = ((ASNObject)result).getName()) == null) {
                    ++it;
                }
                ++i;
            }
        }
        return it;
    }

    /*
     * Loose catch block
     */
    public final ASNSpecification Specification(boolean tracing) throws ParseException {
        this.trace_call("Specification");
        try {
            ASNSpecification jjtn000 = new ASNSpecification(0);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            sTable = new Hashtable();
            try {
                if (tracing) {
                    this.enable_tracing();
                } else {
                    this.disable_tracing();
                }
                block12: while (true) {
                    this.Assignment();
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 46: 
                        case 47: {
                            continue block12;
                        }
                    }
                    break;
                }
                this.jj_la1[0] = this.jj_gen;
                this.jj_consume_token(0);
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                ASNSpecification aSNSpecification = jjtn000;
                return aSNSpecification;
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                throw (Error)jjte000;
            }
            finally {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
            }
            {
                catch (Throwable throwable) {
                    throw throwable;
                }
            }
        }
        finally {
            this.trace_return("Specification");
        }
    }

    public final void Assignment() throws ParseException {
        this.trace_call("Assignment");
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 46: {
                    this.TypeAssignment();
                    break;
                }
                case 47: {
                    this.OIDAssignment();
                    break;
                }
                default: {
                    this.jj_la1[1] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
            }
        }
        finally {
            this.trace_return("Assignment");
        }
    }

    public final void TypeAssignment() throws ParseException {
        this.trace_call("TypeAssignment");
        try {
            String id = this.TypeReference();
            Object obj = sTable.get(id);
            if (obj != null) {
                // empty if block
            }
            this.jj_consume_token(50);
            this.Type(id, true);
        }
        finally {
            this.trace_return("TypeAssignment");
        }
    }

    public final void OIDAssignment() throws ParseException {
        this.trace_call("OIDAssignment");
        try {
            String id = this.ValueReference();
            this.jj_consume_token(17);
            this.jj_consume_token(31);
            this.jj_consume_token(50);
            String oid = this.ObjectIdentifier();
            sTable.put(id, oid);
        }
        finally {
            this.trace_return("OIDAssignment");
        }
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public final ASNType Type(String name, boolean topLevel) throws ParseException {
        this.trace_call("Type");
        try {
            jjtn000 = new ASNType(2);
            jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            try {
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 46: {
                        jjtn001 = new ASNTypeAlias(3);
                        jjtc001 = true;
                        this.jjtree.openNodeScope(jjtn001);
                        try {
                            id = this.TypeReference();
                            this.jjtree.closeNodeScope((Node)jjtn001, true);
                            jjtc001 = false;
                            jjtn001.name = name;
                            if (topLevel) {
                                this.define(name, id);
                            }
                            ** GOTO lbl36
                        }
                        catch (Throwable jjte001) {
                            if (jjtc001) {
                                this.jjtree.clearNodeScope(jjtn001);
                                jjtc001 = false;
                            } else {
                                this.jjtree.popNode();
                            }
                            if (jjte001 instanceof ParseException) {
                                throw (ParseException)jjte001;
                            }
                            if (jjte001 instanceof RuntimeException == false) throw (Error)jjte001;
                            throw (RuntimeException)jjte001;
                        }
                    }
                    finally {
                        if (jjtc001) {
                            this.jjtree.closeNodeScope((Node)jjtn001, true);
                        }
                    }
lbl36:
                    // 1 sources

                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                    jjtc000 = false;
                    var11_11 = jjtn000;
                    return var11_11;
                    case 9: 
                    case 10: 
                    case 11: 
                    case 13: 
                    case 14: 
                    case 17: 
                    case 18: 
                    case 21: 
                    case 22: 
                    case 27: 
                    case 28: 
                    case 29: 
                    case 33: 
                    case 34: 
                    case 35: 
                    case 36: 
                    case 37: 
                    case 38: 
                    case 39: 
                    case 40: 
                    case 41: 
                    case 57: {
                        this.BuiltInType(name, topLevel);
                        this.jjtree.closeNodeScope((Node)jjtn000, true);
                        jjtc000 = false;
                        var11_12 = jjtn000;
                        return var11_12;
                    }
                }
                this.jj_la1[2] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                if (jjte000 instanceof RuntimeException == false) throw (Error)jjte000;
                throw (RuntimeException)jjte000;
            }
            finally {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
            }
        }
        finally {
            this.trace_return("Type");
        }
    }

    public final void BuiltInType(String name, boolean topLevel) throws ParseException {
        block21: {
            this.trace_call("BuiltInType");
            try {
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 18: {
                        this.BooleanType(name, topLevel);
                        break;
                    }
                    case 21: {
                        this.IntegerType(name, topLevel);
                        break;
                    }
                    case 10: {
                        this.BitStringType(name, topLevel);
                        break;
                    }
                    case 14: {
                        this.OctetStringType(name, topLevel);
                        break;
                    }
                    case 13: {
                        this.NullType(name, topLevel);
                        break;
                    }
                    case 17: {
                        this.ObjectIdentifierType(name, topLevel);
                        break;
                    }
                    default: {
                        this.jj_la1[3] = this.jj_gen;
                        if (this.jj_2_1(2)) {
                            this.SequenceType(name, topLevel);
                            break;
                        }
                        if (this.jj_2_2(2)) {
                            this.SequenceOfType(name, topLevel);
                            break;
                        }
                        if (this.jj_2_3(2)) {
                            this.SetType(name, topLevel);
                            break;
                        }
                        switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                            case 11: {
                                this.SetOfType(name, topLevel);
                                break block21;
                            }
                            case 57: {
                                this.TaggedType(name, topLevel);
                                break block21;
                            }
                            case 9: {
                                this.AnyType(name, topLevel);
                                break block21;
                            }
                            case 28: 
                            case 29: 
                            case 33: 
                            case 34: 
                            case 35: 
                            case 36: 
                            case 37: 
                            case 38: 
                            case 39: 
                            case 41: {
                                this.CharacterStringType(name, topLevel);
                                break block21;
                            }
                            case 22: 
                            case 40: {
                                this.UsefulType(name, topLevel);
                                break block21;
                            }
                            default: {
                                this.jj_la1[4] = this.jj_gen;
                                this.jj_consume_token(-1);
                                throw new ParseException();
                            }
                        }
                    }
                }
            }
            finally {
                this.trace_return("BuiltInType");
            }
        }
    }

    public final ASNBoolean BooleanType(String name, boolean topLevel) throws ParseException {
        this.trace_call("BooleanType");
        try {
            ASNBoolean jjtn000 = new ASNBoolean(4);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            try {
                this.jj_consume_token(18);
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                jjtn000.name = name;
                jjtn000.tag = new Tag(1, this.isTagExplicit);
                if (topLevel) {
                    this.define(name, jjtn000);
                }
                ASNBoolean aSNBoolean = jjtn000;
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
                return aSNBoolean;
            }
            catch (Throwable throwable) {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
                throw throwable;
            }
        }
        finally {
            this.trace_return("BooleanType");
        }
    }

    /*
     * Loose catch block
     */
    public final ASNInteger IntegerType(String name, boolean topLevel) throws ParseException {
        this.trace_call("IntegerType");
        try {
            ASNInteger jjtn000 = new ASNInteger(5);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            try {
                this.jj_consume_token(21);
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 51: {
                        this.jj_consume_token(51);
                        this.NamedNumberList();
                        this.jj_consume_token(52);
                        break;
                    }
                    default: {
                        this.jj_la1[5] = this.jj_gen;
                    }
                }
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                jjtn000.name = name;
                jjtn000.tag = new Tag(2, this.isTagExplicit);
                if (topLevel) {
                    this.define(name, jjtn000);
                }
                ASNInteger aSNInteger = jjtn000;
                return aSNInteger;
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                throw (Error)jjte000;
            }
            finally {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
            }
            {
                catch (Throwable throwable) {
                    throw throwable;
                }
            }
        }
        finally {
            this.trace_return("IntegerType");
        }
    }

    public final void NamedNumberList() throws ParseException {
        block7: {
            this.trace_call("NamedNumberList");
            try {
                this.NamedNumber();
                while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 53: {
                            break;
                        }
                        default: {
                            this.jj_la1[6] = this.jj_gen;
                            break block7;
                        }
                    }
                    this.jj_consume_token(53);
                    this.NamedNumber();
                }
            }
            finally {
                this.trace_return("NamedNumberList");
            }
        }
    }

    public final void NamedNumber() throws ParseException {
        this.trace_call("NamedNumber");
        try {
            BigInteger val;
            String id = this.Identifier();
            this.jj_consume_token(54);
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 42: 
                case 56: {
                    val = this.SignedNumber();
                    break;
                }
                case 47: {
                    this.DefinedValue();
                    val = (BigInteger)stack[top--];
                    break;
                }
                default: {
                    this.jj_la1[7] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
            }
            sTable.put(id, val);
            this.jj_consume_token(55);
        }
        finally {
            this.trace_return("NamedNumber");
        }
    }

    public final BigInteger SignedNumber() throws ParseException {
        this.trace_call("SignedNumber");
        try {
            String image = "";
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 56: {
                    this.jj_consume_token(56);
                    image = "-";
                    break;
                }
                default: {
                    this.jj_la1[8] = this.jj_gen;
                }
            }
            Token t = this.jj_consume_token(42);
            image = String.valueOf(image) + t.image;
            BigInteger bigInteger = new BigInteger(image);
            return bigInteger;
        }
        finally {
            this.trace_return("SignedNumber");
        }
    }

    public final ASNBitString BitStringType(String name, boolean topLevel) throws ParseException {
        this.trace_call("BitStringType");
        try {
            ASNBitString jjtn000 = new ASNBitString(6);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            try {
                this.jj_consume_token(10);
                this.jj_consume_token(16);
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                jjtn000.name = name;
                jjtn000.tag = new Tag(3, this.isTagExplicit);
                if (topLevel) {
                    this.define(name, jjtn000);
                }
                ASNBitString aSNBitString = jjtn000;
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
                return aSNBitString;
            }
            catch (Throwable throwable) {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
                throw throwable;
            }
        }
        finally {
            this.trace_return("BitStringType");
        }
    }

    public final ASNOctetString OctetStringType(String name, boolean topLevel) throws ParseException {
        this.trace_call("OctetStringType");
        try {
            ASNOctetString jjtn000 = new ASNOctetString(7);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            try {
                this.jj_consume_token(14);
                this.jj_consume_token(16);
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                jjtn000.name = name;
                jjtn000.tag = new Tag(4, this.isTagExplicit);
                if (topLevel) {
                    this.define(name, jjtn000);
                }
                ASNOctetString aSNOctetString = jjtn000;
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
                return aSNOctetString;
            }
            catch (Throwable throwable) {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
                throw throwable;
            }
        }
        finally {
            this.trace_return("OctetStringType");
        }
    }

    public final ASNNull NullType(String name, boolean topLevel) throws ParseException {
        this.trace_call("NullType");
        try {
            ASNNull jjtn000 = new ASNNull(8);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            try {
                this.jj_consume_token(13);
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                jjtn000.name = name;
                jjtn000.tag = new Tag(5, this.isTagExplicit);
                if (topLevel) {
                    this.define(name, jjtn000);
                }
                ASNNull aSNNull = jjtn000;
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
                return aSNNull;
            }
            catch (Throwable throwable) {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
                throw throwable;
            }
        }
        finally {
            this.trace_return("NullType");
        }
    }

    public final ASNObjectIdentifier ObjectIdentifierType(String name, boolean topLevel) throws ParseException {
        this.trace_call("ObjectIdentifierType");
        try {
            ASNObjectIdentifier jjtn000 = new ASNObjectIdentifier(9);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            try {
                this.jj_consume_token(17);
                this.jj_consume_token(31);
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                jjtn000.name = name;
                jjtn000.tag = new Tag(6, this.isTagExplicit);
                if (topLevel) {
                    this.define(name, jjtn000);
                }
                ASNObjectIdentifier aSNObjectIdentifier = jjtn000;
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
                return aSNObjectIdentifier;
            }
            catch (Throwable throwable) {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
                throw throwable;
            }
        }
        finally {
            this.trace_return("ObjectIdentifierType");
        }
    }

    /*
     * Loose catch block
     */
    public final ASNSequence SequenceType(String name, boolean topLevel) throws ParseException {
        this.trace_call("SequenceType");
        try {
            ASNSequence jjtn000 = new ASNSequence(10);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            try {
                this.jj_consume_token(27);
                this.jj_consume_token(51);
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 9: 
                    case 10: 
                    case 11: 
                    case 13: 
                    case 14: 
                    case 17: 
                    case 18: 
                    case 21: 
                    case 22: 
                    case 27: 
                    case 28: 
                    case 29: 
                    case 33: 
                    case 34: 
                    case 35: 
                    case 36: 
                    case 37: 
                    case 38: 
                    case 39: 
                    case 40: 
                    case 41: 
                    case 46: 
                    case 47: 
                    case 57: {
                        this.ElementTypeList(name);
                        break;
                    }
                    default: {
                        this.jj_la1[9] = this.jj_gen;
                    }
                }
                this.jj_consume_token(52);
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                jjtn000.name = name;
                jjtn000.tag = new Tag(16, this.isTagExplicit);
                if (topLevel) {
                    this.define(name, jjtn000);
                }
                ASNSequence aSNSequence = jjtn000;
                return aSNSequence;
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                throw (Error)jjte000;
            }
            finally {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
            }
            {
                catch (Throwable throwable) {
                    throw throwable;
                }
            }
        }
        finally {
            this.trace_return("SequenceType");
        }
    }

    public final void ElementTypeList(String structure) throws ParseException {
        block7: {
            this.trace_call("ElementTypeList");
            try {
                this.ElementType(structure);
                while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 53: {
                            break;
                        }
                        default: {
                            this.jj_la1[10] = this.jj_gen;
                            break block7;
                        }
                    }
                    this.jj_consume_token(53);
                    this.ElementType(structure);
                }
            }
            finally {
                this.trace_return("ElementTypeList");
            }
        }
    }

    public final void ElementType(String structure) throws ParseException {
        this.trace_call("ElementType");
        try {
            String id = "__LOCAL__";
            ASNType type = null;
            Object defaultValue = null;
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 47: {
                    id = this.Identifier();
                    break;
                }
                default: {
                    this.jj_la1[11] = this.jj_gen;
                }
            }
            type = this.Type(String.valueOf(structure) + "." + id, true);
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 19: 
                case 26: {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 26: {
                            this.jj_consume_token(26);
                            type.setOptional(true);
                            break;
                        }
                        case 19: {
                            this.jj_consume_token(19);
                            defaultValue = this.Value();
                            type.setDefaultValue(defaultValue);
                            break;
                        }
                        default: {
                            this.jj_la1[12] = this.jj_gen;
                            this.jj_consume_token(-1);
                            throw new ParseException();
                        }
                    }
                    this.define(String.valueOf(structure) + "." + id, type);
                    break;
                }
                default: {
                    this.jj_la1[13] = this.jj_gen;
                    break;
                }
            }
        }
        finally {
            this.trace_return("ElementType");
        }
    }

    /*
     * Loose catch block
     */
    public final ASNSequenceOf SequenceOfType(String name, boolean topLevel) throws ParseException {
        this.trace_call("SequenceOfType");
        try {
            ASNSequenceOf jjtn000 = new ASNSequenceOf(11);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            try {
                this.jj_consume_token(27);
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 8: {
                        this.jj_consume_token(8);
                        this.Type(String.valueOf(name) + ".element.<o>", topLevel);
                        break;
                    }
                    default: {
                        this.jj_la1[14] = this.jj_gen;
                    }
                }
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                jjtn000.name = name;
                jjtn000.tag = new Tag(48, this.isTagExplicit);
                if (topLevel) {
                    this.define(name, jjtn000);
                }
                ASNSequenceOf aSNSequenceOf = jjtn000;
                return aSNSequenceOf;
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                throw (Error)jjte000;
            }
            finally {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
            }
            {
                catch (Throwable throwable) {
                    throw throwable;
                }
            }
        }
        finally {
            this.trace_return("SequenceOfType");
        }
    }

    /*
     * Loose catch block
     */
    public final ASNSet SetType(String name, boolean topLevel) throws ParseException {
        this.trace_call("SetType");
        try {
            ASNSet jjtn000 = new ASNSet(12);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            try {
                this.jj_consume_token(11);
                this.jj_consume_token(51);
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 9: 
                    case 10: 
                    case 11: 
                    case 13: 
                    case 14: 
                    case 17: 
                    case 18: 
                    case 21: 
                    case 22: 
                    case 27: 
                    case 28: 
                    case 29: 
                    case 33: 
                    case 34: 
                    case 35: 
                    case 36: 
                    case 37: 
                    case 38: 
                    case 39: 
                    case 40: 
                    case 41: 
                    case 46: 
                    case 47: 
                    case 57: {
                        this.ElementTypeList(name);
                        break;
                    }
                    default: {
                        this.jj_la1[15] = this.jj_gen;
                    }
                }
                this.jj_consume_token(52);
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                jjtn000.name = name;
                jjtn000.tag = new Tag(17, this.isTagExplicit);
                if (topLevel) {
                    this.define(name, jjtn000);
                }
                ASNSet aSNSet = jjtn000;
                return aSNSet;
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                throw (Error)jjte000;
            }
            finally {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
            }
            {
                catch (Throwable throwable) {
                    throw throwable;
                }
            }
        }
        finally {
            this.trace_return("SetType");
        }
    }

    /*
     * Loose catch block
     */
    public final ASNSetOf SetOfType(String name, boolean topLevel) throws ParseException {
        this.trace_call("SetOfType");
        try {
            ASNSetOf jjtn000 = new ASNSetOf(13);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            try {
                this.jj_consume_token(11);
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 8: {
                        this.jj_consume_token(8);
                        this.Type(String.valueOf(name) + ".element.<u>", topLevel);
                        break;
                    }
                    default: {
                        this.jj_la1[16] = this.jj_gen;
                    }
                }
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                jjtn000.name = name;
                jjtn000.tag = new Tag(49, this.isTagExplicit);
                if (topLevel) {
                    this.define(name, jjtn000);
                }
                ASNSetOf aSNSetOf = jjtn000;
                return aSNSetOf;
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                throw (Error)jjte000;
            }
            finally {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
            }
            {
                catch (Throwable throwable) {
                    throw throwable;
                }
            }
        }
        finally {
            this.trace_return("SetOfType");
        }
    }

    /*
     * Loose catch block
     */
    public final ASNTaggedType TaggedType(String name, boolean topLevel) throws ParseException {
        this.trace_call("TaggedType");
        try {
            ASNTaggedType jjtn000 = new ASNTaggedType(14);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            int clazz = 128;
            boolean explicit = false;
            try {
                this.jj_consume_token(57);
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 23: 
                    case 30: 
                    case 32: {
                        clazz = this.Clazz();
                        break;
                    }
                    default: {
                        this.jj_la1[17] = this.jj_gen;
                    }
                }
                int n = this.ClassNumber();
                this.jj_consume_token(58);
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 24: 
                    case 25: {
                        explicit = this.Tagging();
                        break;
                    }
                    default: {
                        this.jj_la1[18] = this.jj_gen;
                    }
                }
                ASNType type = this.Type(name, topLevel);
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                jjtn000.name = name;
                jjtn000.tag = new Tag(clazz, n, explicit);
                if (topLevel) {
                    this.define(name, type);
                }
                ASNTaggedType aSNTaggedType = jjtn000;
                return aSNTaggedType;
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                throw (Error)jjte000;
            }
            finally {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
            }
            {
                catch (Throwable throwable) {
                    throw throwable;
                }
            }
        }
        finally {
            this.trace_return("TaggedType");
        }
    }

    public final int Clazz() throws ParseException {
        this.trace_call("Clazz");
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 30: {
                    this.jj_consume_token(30);
                    return 0;
                }
                case 32: {
                    this.jj_consume_token(32);
                    return 64;
                }
                case 23: {
                    this.jj_consume_token(23);
                    return 192;
                }
            }
            this.jj_la1[19] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
        finally {
            this.trace_return("Clazz");
        }
    }

    public final int ClassNumber() throws ParseException {
        this.trace_call("ClassNumber");
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 42: {
                    Token t = this.jj_consume_token(42);
                    int n = Integer.parseInt(t.image);
                    return n;
                }
                case 47: {
                    this.DefinedValue();
                    int n = Integer.parseInt((String)stack[top--]);
                    return n;
                }
            }
            this.jj_la1[20] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
        finally {
            this.trace_return("ClassNumber");
        }
    }

    public final boolean Tagging() throws ParseException {
        this.trace_call("Tagging");
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 24: {
                    this.jj_consume_token(24);
                    return true;
                }
                case 25: {
                    this.jj_consume_token(25);
                    return false;
                }
            }
            this.jj_la1[21] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
        finally {
            this.trace_return("Tagging");
        }
    }

    /*
     * Loose catch block
     */
    public final ASNAny AnyType(String name, boolean topLevel) throws ParseException {
        this.trace_call("AnyType");
        try {
            ASNAny jjtn000 = new ASNAny(15);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            try {
                this.jj_consume_token(9);
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 20: {
                        this.jj_consume_token(20);
                        this.jj_consume_token(7);
                        this.Identifier();
                        break;
                    }
                    default: {
                        this.jj_la1[22] = this.jj_gen;
                    }
                }
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                jjtn000.name = name;
                jjtn000.tag = new Tag(5, this.isTagExplicit);
                if (topLevel) {
                    this.define(name, jjtn000);
                }
                ASNAny aSNAny = jjtn000;
                return aSNAny;
            }
            catch (Throwable jjte000) {
                if (jjtc000) {
                    this.jjtree.clearNodeScope(jjtn000);
                    jjtc000 = false;
                } else {
                    this.jjtree.popNode();
                }
                if (jjte000 instanceof ParseException) {
                    throw (ParseException)jjte000;
                }
                if (jjte000 instanceof RuntimeException) {
                    throw (RuntimeException)jjte000;
                }
                throw (Error)jjte000;
            }
            finally {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
            }
            {
                catch (Throwable throwable) {
                    throw throwable;
                }
            }
        }
        finally {
            this.trace_return("AnyType");
        }
    }

    public final void CharacterStringType(String name, boolean topLevel) throws ParseException {
        this.trace_call("CharacterStringType");
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 34: {
                    this.jj_consume_token(34);
                    break;
                }
                case 41: {
                    ASNPrintableString jjtn001 = new ASNPrintableString(16);
                    boolean jjtc001 = true;
                    this.jjtree.openNodeScope(jjtn001);
                    try {
                        this.jj_consume_token(41);
                        this.jjtree.closeNodeScope((Node)jjtn001, true);
                        jjtc001 = false;
                        jjtn001.name = name;
                        jjtn001.tag = new Tag(19, this.isTagExplicit);
                        if (topLevel) {
                            this.define(name, jjtn001);
                        }
                        break;
                    }
                    finally {
                        if (jjtc001) {
                            this.jjtree.closeNodeScope((Node)jjtn001, true);
                        }
                    }
                }
                case 35: {
                    this.jj_consume_token(35);
                    break;
                }
                case 28: {
                    this.jj_consume_token(28);
                    break;
                }
                case 39: {
                    this.jj_consume_token(39);
                    break;
                }
                case 36: {
                    this.jj_consume_token(36);
                    break;
                }
                case 33: {
                    this.jj_consume_token(33);
                    break;
                }
                case 29: {
                    this.jj_consume_token(29);
                    break;
                }
                case 37: {
                    this.jj_consume_token(37);
                    break;
                }
                case 38: {
                    this.jj_consume_token(38);
                    break;
                }
                default: {
                    this.jj_la1[23] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
            }
        }
        finally {
            this.trace_return("CharacterStringType");
        }
    }

    public final void UsefulType(String name, boolean topLevel) throws ParseException {
        this.trace_call("UsefulType");
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 22: {
                    this.UTCTime(name, topLevel);
                    break;
                }
                case 40: {
                    this.jj_consume_token(40);
                    break;
                }
                default: {
                    this.jj_la1[24] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
            }
        }
        finally {
            this.trace_return("UsefulType");
        }
    }

    public final ASNTime UTCTime(String name, boolean topLevel) throws ParseException {
        this.trace_call("UTCTime");
        try {
            ASNTime jjtn000 = new ASNTime(17);
            boolean jjtc000 = true;
            this.jjtree.openNodeScope(jjtn000);
            try {
                this.jj_consume_token(22);
                this.jjtree.closeNodeScope((Node)jjtn000, true);
                jjtc000 = false;
                jjtn000.name = name;
                jjtn000.tag = new Tag(23, this.isTagExplicit);
                if (topLevel) {
                    this.define(name, jjtn000);
                }
                ASNTime aSNTime = jjtn000;
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
                return aSNTime;
            }
            catch (Throwable throwable) {
                if (jjtc000) {
                    this.jjtree.closeNodeScope((Node)jjtn000, true);
                }
                throw throwable;
            }
        }
        finally {
            this.trace_return("UTCTime");
        }
    }

    public final Object Value() throws ParseException {
        this.trace_call("Value");
        try {
            Object result = null;
            if (this.jj_2_4(2)) {
                Object object = result = this.BuiltInValue();
                return object;
            }
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 47: {
                    Object object = result = this.DefinedValue();
                    return object;
                }
            }
            this.jj_la1[25] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
        finally {
            this.trace_return("Value");
        }
    }

    public final Object BuiltInValue() throws ParseException {
        this.trace_call("BuiltInValue");
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 12: 
                case 15: {
                    boolean b = this.Boolean();
                    Boolean bl = new Boolean(b);
                    return bl;
                }
            }
            this.jj_la1[26] = this.jj_gen;
            if (this.jj_2_5(2)) {
                BigInteger bi;
                BigInteger bigInteger = bi = this.Integer();
                return bigInteger;
            }
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 43: 
                case 44: {
                    byte[] ba;
                    byte[] byArray = ba = this.OctetString();
                    return byArray;
                }
                case 13: {
                    this.Null();
                    return null;
                }
            }
            this.jj_la1[27] = this.jj_gen;
            if (this.jj_2_6(2)) {
                String s;
                String string = s = this.ObjectIdentifier();
                return string;
            }
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 51: {
                    Vector v;
                    Vector vector = v = this.Sequence();
                    return vector;
                }
                case 45: {
                    String s;
                    String string = s = this.CharacterString();
                    return string;
                }
            }
            this.jj_la1[28] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
        finally {
            this.trace_return("BuiltInValue");
        }
    }

    public final Object DefinedValue() throws ParseException {
        this.trace_call("DefinedValue");
        try {
            String id = this.ValueReference();
            if (!sTable.containsKey(id)) {
                throw new ParseException("Undefined Value: " + id);
            }
            Object result = sTable.get(id);
            Parser.stack[++Parser.top] = result;
            Object v = result;
            return v;
        }
        finally {
            this.trace_return("DefinedValue");
        }
    }

    public final boolean Boolean() throws ParseException {
        this.trace_call("Boolean");
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 12: {
                    this.jj_consume_token(12);
                    return true;
                }
                case 15: {
                    this.jj_consume_token(15);
                    return false;
                }
            }
            this.jj_la1[29] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
        finally {
            this.trace_return("Boolean");
        }
    }

    public final BigInteger Integer() throws ParseException {
        this.trace_call("Integer");
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 42: 
                case 56: {
                    BigInteger result;
                    BigInteger bigInteger = result = this.SignedNumber();
                    return bigInteger;
                }
                case 47: {
                    String id = this.Identifier();
                    if (!sTable.containsKey(id)) {
                        throw new ParseException("Undefined Identifer: " + id);
                    }
                    BigInteger bigInteger = (BigInteger)sTable.get(id);
                    return bigInteger;
                }
            }
            this.jj_la1[30] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
        finally {
            this.trace_return("Integer");
        }
    }

    public final byte[] OctetString() throws ParseException {
        this.trace_call("OctetString");
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 44: {
                    Token t = this.jj_consume_token(44);
                    String image = t.image.substring(1, t.image.length() - 2);
                    BigInteger result = new BigInteger(image, 2);
                    byte[] byArray = result.toByteArray();
                    return byArray;
                }
                case 43: {
                    Token t = this.jj_consume_token(43);
                    String image = t.image.substring(1, t.image.length() - 2);
                    BigInteger result = new BigInteger(image, 16);
                    byte[] byArray = result.toByteArray();
                    return byArray;
                }
            }
            this.jj_la1[31] = this.jj_gen;
            this.jj_consume_token(-1);
            throw new ParseException();
        }
        finally {
            this.trace_return("OctetString");
        }
    }

    public final void Null() throws ParseException {
        this.trace_call("Null");
        try {
            this.jj_consume_token(13);
        }
        finally {
            this.trace_return("Null");
        }
    }

    public final String ObjectIdentifier() throws ParseException {
        this.trace_call("ObjectIdentifier");
        try {
            int m = top;
            this.jj_consume_token(51);
            if (this.jj_2_7(2)) {
                this.DefinedValue();
                this.ObjIdComponent();
            } else {
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 42: 
                    case 47: {
                        this.ObjIdComponent();
                        break;
                    }
                    default: {
                        this.jj_la1[32] = this.jj_gen;
                        this.jj_consume_token(-1);
                        throw new ParseException();
                    }
                }
            }
            block9: while (true) {
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 42: 
                    case 47: {
                        break;
                    }
                    default: {
                        this.jj_la1[33] = this.jj_gen;
                        break block9;
                    }
                }
                this.ObjIdComponent();
            }
            this.jj_consume_token(52);
            int limit = top;
            String it = "";
            boolean virgin = true;
            while (m < limit) {
                it = String.valueOf(it) + (virgin ? "" : ".") + (String)stack[++m];
                virgin = false;
            }
            top = m;
            String string = it;
            return string;
        }
        finally {
            this.trace_return("ObjectIdentifier");
        }
    }

    public final void ObjIdComponent() throws ParseException {
        block9: {
            this.trace_call("ObjIdComponent");
            try {
                if (this.jj_2_8(2)) {
                    String id = this.Identifier();
                    this.jj_consume_token(54);
                    this.NumberForm();
                    this.jj_consume_token(55);
                    sTable.put(id, (String)stack[top]);
                    break block9;
                }
                if (this.jj_2_9(2)) {
                    String id = this.Identifier();
                    if (!sTable.containsKey(id)) {
                        throw new ParseException("Undefined Identifer: " + id);
                    }
                    Parser.stack[++Parser.top] = (String)sTable.get(id);
                    break block9;
                }
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 42: 
                    case 47: {
                        this.NumberForm();
                        break;
                    }
                    default: {
                        this.jj_la1[34] = this.jj_gen;
                        this.jj_consume_token(-1);
                        throw new ParseException();
                    }
                }
            }
            finally {
                this.trace_return("ObjIdComponent");
            }
        }
    }

    public final void NumberForm() throws ParseException {
        this.trace_call("NumberForm");
        try {
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 42: {
                    Token t = this.jj_consume_token(42);
                    Parser.stack[++Parser.top] = t.image;
                    break;
                }
                case 47: {
                    this.DefinedValue();
                    break;
                }
                default: {
                    this.jj_la1[35] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
                }
            }
        }
        finally {
            this.trace_return("NumberForm");
        }
    }

    public final Vector Sequence() throws ParseException {
        this.trace_call("Sequence");
        try {
            Vector result = new Vector();
            this.jj_consume_token(51);
            switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                case 12: 
                case 13: 
                case 15: 
                case 42: 
                case 43: 
                case 44: 
                case 45: 
                case 47: 
                case 51: 
                case 56: {
                    this.ElementValueList(result);
                    break;
                }
                default: {
                    this.jj_la1[36] = this.jj_gen;
                }
            }
            this.jj_consume_token(52);
            Vector vector = result;
            return vector;
        }
        finally {
            this.trace_return("Sequence");
        }
    }

    public final void ElementValueList(Vector sequence) throws ParseException {
        block7: {
            this.trace_call("ElementValueList");
            try {
                this.NamedValue(sequence);
                while (true) {
                    switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                        case 53: {
                            break;
                        }
                        default: {
                            this.jj_la1[37] = this.jj_gen;
                            break block7;
                        }
                    }
                    this.jj_consume_token(53);
                    this.NamedValue(sequence);
                }
            }
            finally {
                this.trace_return("ElementValueList");
            }
        }
    }

    public final void NamedValue(Vector container) throws ParseException {
        block7: {
            this.trace_call("NamedValue");
            try {
                if (this.jj_2_10(2)) {
                    this.Identifier();
                    Object val = this.Value();
                    container.addElement(val);
                    break block7;
                }
                switch (this.jj_ntk == -1 ? this.jj_ntk() : this.jj_ntk) {
                    case 12: 
                    case 13: 
                    case 15: 
                    case 42: 
                    case 43: 
                    case 44: 
                    case 45: 
                    case 47: 
                    case 51: 
                    case 56: {
                        Object val = this.Value();
                        container.addElement(val);
                        break;
                    }
                    default: {
                        this.jj_la1[38] = this.jj_gen;
                        this.jj_consume_token(-1);
                        throw new ParseException();
                    }
                }
            }
            finally {
                this.trace_return("NamedValue");
            }
        }
    }

    public final String CharacterString() throws ParseException {
        this.trace_call("CharacterString");
        try {
            Token t = this.jj_consume_token(45);
            String string = t.image.substring(1, t.image.length() - 1);
            return string;
        }
        finally {
            this.trace_return("CharacterString");
        }
    }

    public final String TypeReference() throws ParseException {
        this.trace_call("TypeReference");
        try {
            Token t = this.jj_consume_token(46);
            String string = t.image;
            return string;
        }
        finally {
            this.trace_return("TypeReference");
        }
    }

    public final String Identifier() throws ParseException {
        this.trace_call("Identifier");
        try {
            Token t = this.jj_consume_token(47);
            String string = t.image;
            return string;
        }
        finally {
            this.trace_return("Identifier");
        }
    }

    public final String ValueReference() throws ParseException {
        this.trace_call("ValueReference");
        try {
            Token t = this.jj_consume_token(47);
            String string = t.image;
            return string;
        }
        finally {
            this.trace_return("ValueReference");
        }
    }

    private final boolean jj_2_1(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        boolean retval = !this.jj_3_1();
        this.jj_save(0, xla);
        return retval;
    }

    private final boolean jj_2_2(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        boolean retval = !this.jj_3_2();
        this.jj_save(1, xla);
        return retval;
    }

    private final boolean jj_2_3(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        boolean retval = !this.jj_3_3();
        this.jj_save(2, xla);
        return retval;
    }

    private final boolean jj_2_4(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        boolean retval = !this.jj_3_4();
        this.jj_save(3, xla);
        return retval;
    }

    private final boolean jj_2_5(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        boolean retval = !this.jj_3_5();
        this.jj_save(4, xla);
        return retval;
    }

    private final boolean jj_2_6(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        boolean retval = !this.jj_3_6();
        this.jj_save(5, xla);
        return retval;
    }

    private final boolean jj_2_7(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        boolean retval = !this.jj_3_7();
        this.jj_save(6, xla);
        return retval;
    }

    private final boolean jj_2_8(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        boolean retval = !this.jj_3_8();
        this.jj_save(7, xla);
        return retval;
    }

    private final boolean jj_2_9(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        boolean retval = !this.jj_3_9();
        this.jj_save(8, xla);
        return retval;
    }

    private final boolean jj_2_10(int xla) {
        this.jj_la = xla;
        this.jj_lastpos = this.jj_scanpos = this.token;
        boolean retval = !this.jj_3_10();
        this.jj_save(9, xla);
        return retval;
    }

    private final boolean jj_3R_44() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3_10()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_45()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3_10() {
        if (this.jj_3R_14()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_3R_15()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_15() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3_4()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_27()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3_4() {
        if (this.jj_3R_9()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_37() {
        if (this.jj_scan_token(44)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_29() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_37()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_38()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_43() {
        if (this.jj_3R_44()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_39() {
        if (this.jj_3R_43()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_6() {
        if (this.jj_scan_token(27)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_scan_token(51)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_31() {
        if (this.jj_scan_token(51)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_39()) {
            this.jj_scanpos = xsp;
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_scan_token(52)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_23() {
        if (this.jj_3R_14()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_22() {
        if (this.jj_3R_33()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_10() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_22()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_23()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_42() {
        if (this.jj_3R_12()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_41() {
        if (this.jj_scan_token(42)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_34() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_41()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_42()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_36() {
        if (this.jj_scan_token(15)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_26() {
        if (this.jj_3R_34()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_35() {
        if (this.jj_scan_token(12)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_28() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_35()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_36()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3_9() {
        if (this.jj_3R_14()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3_3() {
        if (this.jj_3R_8()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3_2() {
        if (this.jj_3R_7()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3_1() {
        if (this.jj_3R_6()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_13() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3_8()) {
            this.jj_scanpos = xsp;
            if (this.jj_3_9()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_26()) {
                    return true;
                }
                if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                    return false;
                }
            } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3_8() {
        if (this.jj_3R_14()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_scan_token(54)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_12() {
        if (this.jj_3R_25()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_25() {
        if (this.jj_scan_token(47)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_8() {
        if (this.jj_scan_token(11)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_scan_token(51)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_16() {
        if (this.jj_scan_token(8)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_21() {
        if (this.jj_3R_32()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_20() {
        if (this.jj_3R_31()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_14() {
        if (this.jj_scan_token(47)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_24() {
        if (this.jj_3R_13()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3_6() {
        if (this.jj_3R_11()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_7() {
        if (this.jj_scan_token(27)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_16()) {
            this.jj_scanpos = xsp;
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3_7() {
        if (this.jj_3R_12()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_3R_13()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_19() {
        if (this.jj_3R_30()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_11() {
        if (this.jj_scan_token(51)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        Token xsp = this.jj_scanpos;
        if (this.jj_3_7()) {
            this.jj_scanpos = xsp;
            if (this.jj_3R_24()) {
                return true;
            }
            if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_18() {
        if (this.jj_3R_29()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_40() {
        if (this.jj_scan_token(56)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3_5() {
        if (this.jj_3R_10()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_33() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_40()) {
            this.jj_scanpos = xsp;
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        if (this.jj_scan_token(42)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_30() {
        if (this.jj_scan_token(13)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_17() {
        if (this.jj_3R_28()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_9() {
        Token xsp = this.jj_scanpos;
        if (this.jj_3R_17()) {
            this.jj_scanpos = xsp;
            if (this.jj_3_5()) {
                this.jj_scanpos = xsp;
                if (this.jj_3R_18()) {
                    this.jj_scanpos = xsp;
                    if (this.jj_3R_19()) {
                        this.jj_scanpos = xsp;
                        if (this.jj_3_6()) {
                            this.jj_scanpos = xsp;
                            if (this.jj_3R_20()) {
                                this.jj_scanpos = xsp;
                                if (this.jj_3R_21()) {
                                    return true;
                                }
                                if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                                    return false;
                                }
                            } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                                return false;
                            }
                        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                            return false;
                        }
                    } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                        return false;
                    }
                } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                    return false;
                }
            } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
                return false;
            }
        } else if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_32() {
        if (this.jj_scan_token(45)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_45() {
        if (this.jj_3R_15()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_38() {
        if (this.jj_scan_token(43)) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    private final boolean jj_3R_27() {
        if (this.jj_3R_12()) {
            return true;
        }
        if (this.jj_la == 0 && this.jj_scanpos == this.jj_lastpos) {
            return false;
        }
        return false;
    }

    public Parser(InputStream stream) {
        int[] nArray = new int[39];
        nArray[2] = 946236928;
        nArray[3] = 2515968;
        nArray[4] = 809503232;
        nArray[9] = 946236928;
        nArray[12] = 0x4080000;
        nArray[13] = 0x4080000;
        nArray[14] = 256;
        nArray[15] = 946236928;
        nArray[16] = 256;
        nArray[17] = 0x40800000;
        nArray[18] = 0x3000000;
        nArray[19] = 0x40800000;
        nArray[21] = 0x3000000;
        nArray[22] = 0x100000;
        nArray[23] = 0x30000000;
        nArray[24] = 0x400000;
        nArray[26] = 36864;
        nArray[27] = 8192;
        nArray[29] = 36864;
        nArray[36] = 45056;
        nArray[38] = 45056;
        this.jj_la1_0 = nArray;
        int[] nArray2 = new int[39];
        nArray2[0] = 49152;
        nArray2[1] = 49152;
        nArray2[2] = 33571838;
        nArray2[4] = 33555454;
        nArray2[5] = 524288;
        nArray2[6] = 0x200000;
        nArray2[7] = 16811008;
        nArray2[8] = 0x1000000;
        nArray2[9] = 33604606;
        nArray2[10] = 0x200000;
        nArray2[11] = 32768;
        nArray2[15] = 33604606;
        nArray2[17] = 1;
        nArray2[19] = 1;
        nArray2[20] = 33792;
        nArray2[23] = 766;
        nArray2[24] = 256;
        nArray2[25] = 32768;
        nArray2[27] = 6144;
        nArray2[28] = 532480;
        nArray2[30] = 16811008;
        nArray2[31] = 6144;
        nArray2[32] = 33792;
        nArray2[33] = 33792;
        nArray2[34] = 33792;
        nArray2[35] = 33792;
        nArray2[36] = 17349632;
        nArray2[37] = 0x200000;
        nArray2[38] = 17349632;
        this.jj_la1_1 = nArray2;
        this.jj_2_rtns = new JJCalls[10];
        this.jj_rescan = false;
        this.jj_gc = 0;
        this.jj_expentries = new Vector();
        this.jj_kind = -1;
        this.jj_lasttokens = new int[100];
        this.trace_indent = 0;
        this.trace_enabled = true;
        this.jj_input_stream = new ASCII_CharStream(stream, 1, 1);
        this.token_source = new ParserTokenManager(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jj_gen = 0;
        int i = 0;
        while (i < 39) {
            this.jj_la1[i] = -1;
            ++i;
        }
        i = 0;
        while (i < this.jj_2_rtns.length) {
            this.jj_2_rtns[i] = new JJCalls();
            ++i;
        }
    }

    public void ReInit(InputStream stream) {
        this.jj_input_stream.ReInit(stream, 1, 1);
        this.token_source.ReInit(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jjtree.reset();
        this.jj_gen = 0;
        int i = 0;
        while (i < 39) {
            this.jj_la1[i] = -1;
            ++i;
        }
        i = 0;
        while (i < this.jj_2_rtns.length) {
            this.jj_2_rtns[i] = new JJCalls();
            ++i;
        }
    }

    public Parser(Reader stream) {
        int[] nArray = new int[39];
        nArray[2] = 946236928;
        nArray[3] = 2515968;
        nArray[4] = 809503232;
        nArray[9] = 946236928;
        nArray[12] = 0x4080000;
        nArray[13] = 0x4080000;
        nArray[14] = 256;
        nArray[15] = 946236928;
        nArray[16] = 256;
        nArray[17] = 0x40800000;
        nArray[18] = 0x3000000;
        nArray[19] = 0x40800000;
        nArray[21] = 0x3000000;
        nArray[22] = 0x100000;
        nArray[23] = 0x30000000;
        nArray[24] = 0x400000;
        nArray[26] = 36864;
        nArray[27] = 8192;
        nArray[29] = 36864;
        nArray[36] = 45056;
        nArray[38] = 45056;
        this.jj_la1_0 = nArray;
        int[] nArray2 = new int[39];
        nArray2[0] = 49152;
        nArray2[1] = 49152;
        nArray2[2] = 33571838;
        nArray2[4] = 33555454;
        nArray2[5] = 524288;
        nArray2[6] = 0x200000;
        nArray2[7] = 16811008;
        nArray2[8] = 0x1000000;
        nArray2[9] = 33604606;
        nArray2[10] = 0x200000;
        nArray2[11] = 32768;
        nArray2[15] = 33604606;
        nArray2[17] = 1;
        nArray2[19] = 1;
        nArray2[20] = 33792;
        nArray2[23] = 766;
        nArray2[24] = 256;
        nArray2[25] = 32768;
        nArray2[27] = 6144;
        nArray2[28] = 532480;
        nArray2[30] = 16811008;
        nArray2[31] = 6144;
        nArray2[32] = 33792;
        nArray2[33] = 33792;
        nArray2[34] = 33792;
        nArray2[35] = 33792;
        nArray2[36] = 17349632;
        nArray2[37] = 0x200000;
        nArray2[38] = 17349632;
        this.jj_la1_1 = nArray2;
        this.jj_2_rtns = new JJCalls[10];
        this.jj_rescan = false;
        this.jj_gc = 0;
        this.jj_expentries = new Vector();
        this.jj_kind = -1;
        this.jj_lasttokens = new int[100];
        this.trace_indent = 0;
        this.trace_enabled = true;
        this.jj_input_stream = new ASCII_CharStream(stream, 1, 1);
        this.token_source = new ParserTokenManager(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jj_gen = 0;
        int i = 0;
        while (i < 39) {
            this.jj_la1[i] = -1;
            ++i;
        }
        i = 0;
        while (i < this.jj_2_rtns.length) {
            this.jj_2_rtns[i] = new JJCalls();
            ++i;
        }
    }

    public void ReInit(Reader stream) {
        this.jj_input_stream.ReInit(stream, 1, 1);
        this.token_source.ReInit(this.jj_input_stream);
        this.token = new Token();
        this.jj_ntk = -1;
        this.jjtree.reset();
        this.jj_gen = 0;
        int i = 0;
        while (i < 39) {
            this.jj_la1[i] = -1;
            ++i;
        }
        i = 0;
        while (i < this.jj_2_rtns.length) {
            this.jj_2_rtns[i] = new JJCalls();
            ++i;
        }
    }

    public Parser(ParserTokenManager tm) {
        int[] nArray = new int[39];
        nArray[2] = 946236928;
        nArray[3] = 2515968;
        nArray[4] = 809503232;
        nArray[9] = 946236928;
        nArray[12] = 0x4080000;
        nArray[13] = 0x4080000;
        nArray[14] = 256;
        nArray[15] = 946236928;
        nArray[16] = 256;
        nArray[17] = 0x40800000;
        nArray[18] = 0x3000000;
        nArray[19] = 0x40800000;
        nArray[21] = 0x3000000;
        nArray[22] = 0x100000;
        nArray[23] = 0x30000000;
        nArray[24] = 0x400000;
        nArray[26] = 36864;
        nArray[27] = 8192;
        nArray[29] = 36864;
        nArray[36] = 45056;
        nArray[38] = 45056;
        this.jj_la1_0 = nArray;
        int[] nArray2 = new int[39];
        nArray2[0] = 49152;
        nArray2[1] = 49152;
        nArray2[2] = 33571838;
        nArray2[4] = 33555454;
        nArray2[5] = 524288;
        nArray2[6] = 0x200000;
        nArray2[7] = 16811008;
        nArray2[8] = 0x1000000;
        nArray2[9] = 33604606;
        nArray2[10] = 0x200000;
        nArray2[11] = 32768;
        nArray2[15] = 33604606;
        nArray2[17] = 1;
        nArray2[19] = 1;
        nArray2[20] = 33792;
        nArray2[23] = 766;
        nArray2[24] = 256;
        nArray2[25] = 32768;
        nArray2[27] = 6144;
        nArray2[28] = 532480;
        nArray2[30] = 16811008;
        nArray2[31] = 6144;
        nArray2[32] = 33792;
        nArray2[33] = 33792;
        nArray2[34] = 33792;
        nArray2[35] = 33792;
        nArray2[36] = 17349632;
        nArray2[37] = 0x200000;
        nArray2[38] = 17349632;
        this.jj_la1_1 = nArray2;
        this.jj_2_rtns = new JJCalls[10];
        this.jj_rescan = false;
        this.jj_gc = 0;
        this.jj_expentries = new Vector();
        this.jj_kind = -1;
        this.jj_lasttokens = new int[100];
        this.trace_indent = 0;
        this.trace_enabled = true;
        this.token_source = tm;
        this.token = new Token();
        this.jj_ntk = -1;
        this.jj_gen = 0;
        int i = 0;
        while (i < 39) {
            this.jj_la1[i] = -1;
            ++i;
        }
        i = 0;
        while (i < this.jj_2_rtns.length) {
            this.jj_2_rtns[i] = new JJCalls();
            ++i;
        }
    }

    public void ReInit(ParserTokenManager tm) {
        this.token_source = tm;
        this.token = new Token();
        this.jj_ntk = -1;
        this.jjtree.reset();
        this.jj_gen = 0;
        int i = 0;
        while (i < 39) {
            this.jj_la1[i] = -1;
            ++i;
        }
        i = 0;
        while (i < this.jj_2_rtns.length) {
            this.jj_2_rtns[i] = new JJCalls();
            ++i;
        }
    }

    private final Token jj_consume_token(int kind) throws ParseException {
        Token oldToken = this.token;
        this.token = oldToken.next != null ? this.token.next : (this.token.next = this.token_source.getNextToken());
        this.jj_ntk = -1;
        if (this.token.kind == kind) {
            ++this.jj_gen;
            if (++this.jj_gc > 100) {
                this.jj_gc = 0;
                int i = 0;
                while (i < this.jj_2_rtns.length) {
                    JJCalls c = this.jj_2_rtns[i];
                    while (c != null) {
                        if (c.gen < this.jj_gen) {
                            c.first = null;
                        }
                        c = c.next;
                    }
                    ++i;
                }
            }
            this.trace_token(this.token, "");
            return this.token;
        }
        this.token = oldToken;
        this.jj_kind = kind;
        throw this.generateParseException();
    }

    private final boolean jj_scan_token(int kind) {
        if (this.jj_scanpos == this.jj_lastpos) {
            --this.jj_la;
            if (this.jj_scanpos.next == null) {
                this.jj_scanpos = this.jj_scanpos.next = this.token_source.getNextToken();
                this.jj_lastpos = this.jj_scanpos.next;
            } else {
                this.jj_lastpos = this.jj_scanpos = this.jj_scanpos.next;
            }
        } else {
            this.jj_scanpos = this.jj_scanpos.next;
        }
        if (this.jj_rescan) {
            int i = 0;
            Token tok = this.token;
            while (tok != null && tok != this.jj_scanpos) {
                ++i;
                tok = tok.next;
            }
            if (tok != null) {
                this.jj_add_error_token(kind, i);
            }
        }
        return this.jj_scanpos.kind != kind;
    }

    public final Token getNextToken() {
        this.token = this.token.next != null ? this.token.next : (this.token.next = this.token_source.getNextToken());
        this.jj_ntk = -1;
        ++this.jj_gen;
        this.trace_token(this.token, " (in getNextToken)");
        return this.token;
    }

    public final Token getToken(int index) {
        Token t = this.lookingAhead ? this.jj_scanpos : this.token;
        int i = 0;
        while (i < index) {
            t = t.next != null ? t.next : (t.next = this.token_source.getNextToken());
            ++i;
        }
        return t;
    }

    private final int jj_ntk() {
        this.jj_nt = this.token.next;
        if (this.jj_nt == null) {
            this.token.next = this.token_source.getNextToken();
            this.jj_ntk = this.token.next.kind;
            return this.jj_ntk;
        }
        this.jj_ntk = this.jj_nt.kind;
        return this.jj_ntk;
    }

    private void jj_add_error_token(int kind, int pos) {
        if (pos >= 100) {
            return;
        }
        if (pos == this.jj_endpos + 1) {
            this.jj_lasttokens[this.jj_endpos++] = kind;
        } else if (this.jj_endpos != 0) {
            this.jj_expentry = new int[this.jj_endpos];
            int i = 0;
            while (i < this.jj_endpos) {
                this.jj_expentry[i] = this.jj_lasttokens[i];
                ++i;
            }
            boolean exists = false;
            Enumeration propenum = this.jj_expentries.elements();
            while (propenum.hasMoreElements()) {
                int[] oldentry = (int[])propenum.nextElement();
                if (oldentry.length != this.jj_expentry.length) continue;
                exists = true;
                int i2 = 0;
                while (i2 < this.jj_expentry.length) {
                    if (oldentry[i2] != this.jj_expentry[i2]) {
                        exists = false;
                        break;
                    }
                    ++i2;
                }
                if (exists) break;
            }
            if (!exists) {
                this.jj_expentries.addElement(this.jj_expentry);
            }
            if (pos != 0) {
                this.jj_endpos = pos;
                this.jj_lasttokens[this.jj_endpos - 1] = kind;
            }
        }
    }

    public final ParseException generateParseException() {
        this.jj_expentries.removeAllElements();
        boolean[] la1tokens = new boolean[59];
        int i = 0;
        while (i < 59) {
            la1tokens[i] = false;
            ++i;
        }
        if (this.jj_kind >= 0) {
            la1tokens[this.jj_kind] = true;
            this.jj_kind = -1;
        }
        i = 0;
        while (i < 39) {
            if (this.jj_la1[i] == this.jj_gen) {
                int j = 0;
                while (j < 32) {
                    if ((this.jj_la1_0[i] & 1 << j) != 0) {
                        la1tokens[j] = true;
                    }
                    if ((this.jj_la1_1[i] & 1 << j) != 0) {
                        la1tokens[32 + j] = true;
                    }
                    ++j;
                }
            }
            ++i;
        }
        i = 0;
        while (i < 59) {
            if (la1tokens[i]) {
                this.jj_expentry = new int[1];
                this.jj_expentry[0] = i;
                this.jj_expentries.addElement(this.jj_expentry);
            }
            ++i;
        }
        this.jj_endpos = 0;
        this.jj_rescan_token();
        this.jj_add_error_token(0, 0);
        int[][] exptokseq = new int[this.jj_expentries.size()][];
        int i2 = 0;
        while (i2 < this.jj_expentries.size()) {
            exptokseq[i2] = (int[])this.jj_expentries.elementAt(i2);
            ++i2;
        }
        return new ParseException(this.token, exptokseq, tokenImage);
    }

    public final void enable_tracing() {
        this.trace_enabled = true;
    }

    public final void disable_tracing() {
        this.trace_enabled = false;
    }

    private final void trace_call(String s) {
        if (this.trace_enabled) {
            int i = 0;
            while (i < this.trace_indent) {
                System.out.print(" ");
                ++i;
            }
            System.out.println("Call:   " + s);
        }
        this.trace_indent += 2;
    }

    private final void trace_return(String s) {
        this.trace_indent -= 2;
        if (this.trace_enabled) {
            int i = 0;
            while (i < this.trace_indent) {
                System.out.print(" ");
                ++i;
            }
            System.out.println("Return: " + s);
        }
    }

    private final void trace_token(Token t, String where) {
        if (this.trace_enabled) {
            int i = 0;
            while (i < this.trace_indent) {
                System.out.print(" ");
                ++i;
            }
            System.out.print("Consumed token: <" + tokenImage[t.kind]);
            if (t.kind != 0 && !tokenImage[t.kind].equals("\"" + t.image + "\"")) {
                System.out.print(": \"" + t.image + "\"");
            }
            System.out.println(">" + where);
        }
    }

    private final void trace_scan(Token t1, int t2) {
        if (this.trace_enabled) {
            int i = 0;
            while (i < this.trace_indent) {
                System.out.print(" ");
                ++i;
            }
            System.out.print("Visited token: <" + tokenImage[t1.kind]);
            if (t1.kind != 0 && !tokenImage[t1.kind].equals("\"" + t1.image + "\"")) {
                System.out.print(": \"" + t1.image + "\"");
            }
            System.out.println(">; Expected token: <" + tokenImage[t2] + ">");
        }
    }

    private final void jj_rescan_token() {
        this.jj_rescan = true;
        int i = 0;
        while (i < 10) {
            JJCalls p = this.jj_2_rtns[i];
            do {
                if (p.gen <= this.jj_gen) continue;
                this.jj_la = p.arg;
                this.jj_lastpos = this.jj_scanpos = p.first;
                switch (i) {
                    case 0: {
                        this.jj_3_1();
                        break;
                    }
                    case 1: {
                        this.jj_3_2();
                        break;
                    }
                    case 2: {
                        this.jj_3_3();
                        break;
                    }
                    case 3: {
                        this.jj_3_4();
                        break;
                    }
                    case 4: {
                        this.jj_3_5();
                        break;
                    }
                    case 5: {
                        this.jj_3_6();
                        break;
                    }
                    case 6: {
                        this.jj_3_7();
                        break;
                    }
                    case 7: {
                        this.jj_3_8();
                        break;
                    }
                    case 8: {
                        this.jj_3_9();
                        break;
                    }
                    case 9: {
                        this.jj_3_10();
                    }
                }
            } while ((p = p.next) != null);
            ++i;
        }
        this.jj_rescan = false;
    }

    private final void jj_save(int index, int xla) {
        JJCalls p = this.jj_2_rtns[index];
        while (p.gen > this.jj_gen) {
            if (p.next == null) {
                p = p.next = new JJCalls();
                break;
            }
            p = p.next;
        }
        p.gen = this.jj_gen + xla - this.jj_la;
        p.first = this.token;
        p.arg = xla;
    }

    static final class JJCalls {
        int gen;
        Token first;
        int arg;
        JJCalls next;

        JJCalls() {
        }
    }
}

