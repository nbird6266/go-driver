/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.encoding;

import cryptix.asn1.encoding.BaseCoder;
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
import cryptix.asn1.lang.ASNTaggedType;
import cryptix.asn1.lang.ASNTime;
import cryptix.asn1.lang.Parser;
import cryptix.asn1.lang.SimpleNode;
import cryptix.asn1.lang.Tag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

public class DER
extends BaseCoder {
    private static final String NAME = "DER";
    private static final boolean IN = true;
    private static final boolean OUT = false;
    private static final boolean DEBUG = false;
    private static final int debuglevel = 0;
    private static final PrintWriter err = null;
    private static final boolean TRACE = PackageProperties.isTraceable("DER");

    private static void debug(String s) {
        err.println(">>> DER: " + s);
    }

    private static void trace(boolean in, String s) {
        if (TRACE) {
            err.println(String.valueOf(in ? "==> " : "<== ") + NAME + "." + s);
        }
    }

    private static void trace(String s) {
        if (TRACE) {
            err.println("<=> DER." + s);
        }
    }

    public void encode(ASNBoolean obj, OutputStream out) throws IOException {
        boolean v = (Boolean)obj.getValue();
        out.write(1);
        DER.encodeLength(1, out);
        out.write(v ? 1 : 0);
    }

    public void encode(ASNOctetString obj, OutputStream out) throws IOException {
        byte[] v = (byte[])obj.getValue();
        out.write(4);
        DER.encodeLength(v.length, out);
        out.write(v);
    }

    public void encode(ASNNull obj, OutputStream out) throws IOException {
        out.write(5);
        DER.encodeLength(0, out);
    }

    public void encode(ASNObjectIdentifier obj, OutputStream out) throws IOException {
        String v = (String)obj.getValue();
        StringTokenizer st = new StringTokenizer(v, ".");
        int[] component = new int[st.countTokens()];
        int i = 0;
        while (i < component.length) {
            component[i] = Integer.parseInt(st.nextToken());
            ++i;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(component[0] * 40 + component[1]);
        int i2 = 2;
        while (i2 < component.length) {
            int c = component[i2];
            byte[] ab = new byte[4];
            int j = 0;
            while (j < 4) {
                ab[j] = (byte)(c & 0x7F);
                if ((c >>>= 7) == 0) break;
                ++j;
            }
            while (j > 0) {
                baos.write(ab[j] | 0x80);
                --j;
            }
            baos.write(ab[0]);
            ++i2;
        }
        out.write(6);
        DER.encodeLength(baos.size(), out);
        baos.writeTo(out);
    }

    public void encode(ASNSequence obj, OutputStream out) throws IOException {
        Object[] v = (Object[])obj.getValue();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        out.write(16);
        DER.encodeLength(baos.size(), out);
        baos.writeTo(out);
    }

    public void encode(ASNTaggedType obj, OutputStream out) throws IOException {
    }

    public void encode(ASNTime obj, OutputStream out) throws IOException {
    }

    public void decode(ASNBoolean obj, InputStream in) throws IOException {
        if (!this.isExpectedTag(1, in)) {
            String msg = "Not a BOOLEAN";
            if (!obj.isOptional()) {
                throw new IOException(msg);
            }
        } else {
            int length = DER.decodeLength(in);
            if (length != 1) {
                throw new IOException("Incorrect BOOLEAN length: " + length);
            }
            int v = in.read();
            if (v == -1) {
                throw new EOFException();
            }
            obj.setValue(new Boolean(v != 0));
        }
    }

    public void decode(ASNInteger obj, InputStream in) throws IOException {
        if (!this.isExpectedTag(2, in)) {
            String msg = "Not an INTEGER";
            if (!obj.isOptional()) {
                throw new IOException(msg);
            }
        } else {
            int length = DER.decodeLength(in);
            byte[] buffer = new byte[length];
            int actualLength = in.read(buffer);
            if (actualLength == -1) {
                throw new EOFException();
            }
            if (actualLength != length) {
                throw new IOException("Length (" + length + ") mismatch: " + actualLength);
            }
            obj.setValue(new BigInteger(1, buffer));
        }
    }

    public void decode(ASNBitString obj, InputStream in) throws IOException {
        if (!this.isExpectedTag(3, in)) {
            String msg = "Not a BIT STRING";
            if (!obj.isOptional()) {
                throw new IOException(msg);
            }
        } else {
            int length = DER.decodeLength(in);
            byte[] buffer = new byte[length];
            int actualLength = in.read(buffer);
            if (actualLength == -1) {
                throw new EOFException();
            }
            if (actualLength != length) {
                throw new IOException("Length (" + length + ") mismatch: " + actualLength);
            }
            obj.setValue(buffer);
        }
    }

    public void decode(ASNOctetString obj, InputStream in) throws IOException {
        if (!this.isExpectedTag(4, in)) {
            String msg = "Not an OCTET STRING";
            if (!obj.isOptional()) {
                throw new IOException(msg);
            }
        } else {
            int length = DER.decodeLength(in);
            byte[] buffer = new byte[length];
            int actualLength = in.read(buffer);
            if (actualLength == -1) {
                throw new EOFException();
            }
            if (actualLength != length) {
                throw new IOException("Length (" + length + ") mismatch: " + actualLength);
            }
            obj.setValue(buffer);
        }
    }

    public void decode(ASNNull obj, InputStream in) throws IOException {
        if (!this.isExpectedTag(5, in)) {
            String msg = "Not a NULL";
            if (!obj.isOptional()) {
                throw new IOException(msg);
            }
        } else {
            int length = DER.decodeLength(in);
            if (length != 0) {
                throw new IOException("Incorrect NULL length: " + length);
            }
        }
    }

    public void decode(ASNObjectIdentifier obj, InputStream in) throws IOException {
        if (!this.isExpectedTag(6, in)) {
            String msg = "Not an OBJECT-IDENTIFIER";
            if (!obj.isOptional()) {
                throw new IOException(msg);
            }
        } else {
            int length = DER.decodeLength(in);
            StringBuffer buffer = new StringBuffer();
            if (--length >= 0) {
                int b = in.read() & 0xFF;
                int first = b < 40 ? 0 : (b < 80 ? 1 : 2);
                int second = b - first * 40;
                buffer.append(first).append(".").append(second);
            }
            while (length > 0) {
                int b;
                buffer.append(".");
                int sid = 0;
                do {
                    b = in.read() & 0xFF;
                    sid = sid << 7 | b & 0x7F;
                } while (--length > 0 && (b & 0x80) == 128);
                buffer.append(sid);
            }
            obj.setValue(new String(buffer));
        }
    }

    public void decode(ASNSequence obj, InputStream in) throws IOException {
        this.decodeSequence(obj, in);
    }

    public void decode(ASNSequenceOf obj, InputStream in) throws IOException {
        this.decodeSequence(obj, in);
    }

    public void decode(ASNSet obj, InputStream in) throws IOException {
        this.decodeSet(obj, in);
    }

    public void decode(ASNSetOf obj, InputStream in) throws IOException {
        this.decodeSet(obj, in);
    }

    public void decode(ASNTaggedType obj, InputStream in) throws IOException {
        Tag tag = obj.getTag();
        in.mark(10);
        if (this.isExpectedTag(tag, in)) {
            if (tag.isExplicit()) {
                this.decodeExplicitTaggedType(obj, in);
            } else {
                in.reset();
                this.decodeImplicitTaggedType(obj, in);
            }
        } else {
            String msg = "Failed to read a non-optional element";
            Object def = obj.getDefaultValue();
            if (def == null && !obj.isOptional()) {
                throw new IOException(msg);
            }
            if (def != null) {
                obj.setValue(def);
            }
            in.reset();
        }
    }

    public void decode(ASNAny obj, InputStream in) throws IOException {
        byte[] buffer = this.getTLV(in);
        if (buffer != null) {
            obj.setValue(buffer);
        }
    }

    public void decode(ASNPrintableString obj, InputStream in) throws IOException {
        if (!this.isExpectedTag(19, in)) {
            String msg = "Not a PrintableString or IA5String";
            if (!obj.isOptional()) {
                throw new IOException(msg);
            }
        } else {
            int length = DER.decodeLength(in);
            byte[] buffer = new byte[length];
            int actualLength = in.read(buffer);
            if (actualLength == -1) {
                throw new EOFException();
            }
            if (actualLength != length) {
                throw new IOException("Length (" + length + ") mismatch: " + actualLength);
            }
            obj.setValue(new String(buffer, "ISO_8859-1"));
        }
    }

    public void decode(ASNTime obj, InputStream in) throws IOException {
        if (!this.isExpectedTag(23, in)) {
            String msg = "Not a UTC_TIME";
            if (!obj.isOptional()) {
                throw new IOException(msg);
            }
        } else {
            int length = DER.decodeLength(in);
            byte[] buffer = new byte[length];
            int actualLength = in.read(buffer);
            if (actualLength == -1) {
                throw new EOFException();
            }
            if (actualLength != length) {
                throw new IOException("Length (" + length + ") mismatch: " + actualLength);
            }
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            int YY = (buffer[0] - 48) * 10 + (buffer[1] - 48);
            int MM = (buffer[2] - 48) * 10 + (buffer[3] - 48) - 1;
            int DD = (buffer[4] - 48) * 10 + (buffer[5] - 48);
            int hh = (buffer[6] - 48) * 10 + (buffer[7] - 48);
            int mm = (buffer[8] - 48) * 10 + (buffer[9] - 48);
            int ss = 0;
            YY += YY <= 50 ? 2000 : 1900;
            if (buffer[10] != 90) {
                ss = (buffer[10] - 48) * 10 + (buffer[11] - 48);
                if (buffer[12] != 90) {
                    throw new IOException("Bad date format");
                }
            }
            cal.set(YY, MM, DD, hh, mm, ss);
            obj.setValue(cal.getTime());
        }
    }

    private boolean isExpectedTag(int expectedTag, InputStream in) throws IOException {
        Tag result = Tag.getExpectedTag(expectedTag, in);
        return result != null;
    }

    private boolean isExpectedTag(Tag tag, InputStream in) throws IOException {
        Tag result = Tag.getExpectedTag(tag, in);
        return result != null;
    }

    private static int decodeLength(InputStream in) throws IOException {
        int result;
        int limit = in.read();
        if ((limit & 0x80) == 0) {
            result = limit;
        } else {
            if ((limit &= 0x7F) > 4) {
                throw new IOException("ASN.1 DER object too large");
            }
            result = 0;
            while (limit-- > 0) {
                result = result << 8 | in.read() & 0xFF;
            }
        }
        return result;
    }

    private void decodeSequence(SimpleNode obj, InputStream in) throws IOException {
        if (!this.isExpectedTag(16, in) && !this.isExpectedTag(48, in)) {
            String msg = "Not a SEQUENCE [OF]";
            if (!obj.isOptional()) {
                throw new IOException(msg);
            }
        } else {
            int length = DER.decodeLength(in);
            byte[] buffer = new byte[length];
            int actualLength = in.read(buffer);
            if (actualLength == -1) {
                throw new EOFException();
            }
            if (actualLength != length) {
                throw new IOException("Length (" + length + ") mismatch: " + actualLength);
            }
            CoderOperations ber = BaseCoder.getInstance(NAME);
            ByteArrayInputStream in2 = new ByteArrayInputStream(buffer);
            ber.init(in2);
            Vector<Object> value = new Vector<Object>();
            while (in2.available() != 0) {
                Object element = obj.childrenAccept(ber, null);
                if (element == null) continue;
                value.addElement(element);
            }
            obj.setValue(value);
        }
    }

    private void decodeSet(SimpleNode obj, InputStream in) throws IOException {
        if (!this.isExpectedTag(17, in) && !this.isExpectedTag(49, in)) {
            String msg = "Not a SET [OF]";
            if (!obj.isOptional()) {
                throw new IOException(msg);
            }
        } else {
            int length = DER.decodeLength(in);
            byte[] buffer = new byte[length];
            int actualLength = in.read(buffer);
            if (actualLength == -1) {
                throw new EOFException();
            }
            if (actualLength != length) {
                throw new IOException("Length (" + length + ") mismatch: " + actualLength);
            }
            CoderOperations ber = BaseCoder.getInstance(NAME);
            ByteArrayInputStream in2 = new ByteArrayInputStream(buffer);
            ber.init(in2);
            Vector<Object> value = new Vector<Object>();
            while (in2.available() != 0) {
                Object element = obj.childrenAccept(ber, null);
                value.addElement(element);
            }
            obj.setValue(value);
        }
    }

    public void decodeExplicitTaggedType(ASNTaggedType obj, InputStream in) throws IOException {
        int length = DER.decodeLength(in);
        byte[] buffer = new byte[length];
        int actualLength = in.read(buffer);
        if (actualLength == -1) {
            throw new EOFException();
        }
        if (actualLength != length) {
            throw new IOException("Length (" + length + ") mismatch: " + actualLength);
        }
        CoderOperations ber = BaseCoder.getInstance(NAME);
        ber.init(new ByteArrayInputStream(buffer));
        obj.getParser();
        SimpleNode x = (SimpleNode)Parser.resolve(obj.getName());
        Object value = x.childrenAccept(ber, null);
        String msg = "********************* New value: XXX";
        obj.setValue(value);
    }

    public boolean decodeImplicitTaggedType(ASNTaggedType obj, InputStream in) throws IOException {
        byte[] origTag = Tag.getTag(in);
        Tag tag = obj.getTag();
        if (tag.isExplicit()) {
            return false;
        }
        int length = DER.decodeLength(in);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (obj.jjtGetNumChildren() != 1) {
            String msg = "Implicitly Tagged types must have exactly one child";
            throw new IOException(msg);
        }
        Tag childTag = obj.getChild(0).getTag();
        if (childTag == null) {
            os.write(origTag);
        } else {
            os.write(childTag.getValue());
        }
        DER.encodeLength(length, os);
        byte[] TL = os.toByteArray();
        byte[] buffer = new byte[length + TL.length];
        System.arraycopy(TL, 0, buffer, 0, TL.length);
        int actualLength = in.read(buffer, TL.length, length);
        if (actualLength == -1) {
            throw new EOFException();
        }
        if (actualLength != length) {
            throw new IOException("Length (" + length + ") mismatch: " + actualLength);
        }
        CoderOperations ber = BaseCoder.getInstance(NAME);
        ber.init(new ByteArrayInputStream(buffer));
        Object value = obj.childrenAccept(ber, null);
        obj.setValue(value);
        return true;
    }

    public void decode(InputStream in, int level) throws IOException {
        Vector<Component> entries = new Vector<Component>();
        while (in.available() != 0) {
            try {
                Tag tag = Tag.decode(in);
                int length = DER.decodeLength(in);
                byte[] buffer = new byte[length];
                in.read(buffer);
                entries.addElement(new Component(tag, length, buffer));
            }
            catch (EOFException x) {
                x.printStackTrace(err);
                break;
            }
        }
    }

    private static void encodeLength(int length, OutputStream out) throws IOException {
        if (length < 128) {
            out.write((byte)length);
            return;
        }
        if (length < 256) {
            out.write(-127);
            out.write((byte)length);
            return;
        }
        if (length < 65536) {
            out.write(-126);
            out.write((byte)(length >> 8));
            out.write((byte)length);
            return;
        }
        if (length < 0x1000000) {
            out.write(-125);
            out.write((byte)(length >> 16));
            out.write((byte)(length >> 8));
            out.write((byte)length);
            return;
        }
        out.write(-124);
        out.write((byte)(length >> 24));
        out.write((byte)(length >> 16));
        out.write((byte)(length >> 8));
        out.write((byte)length);
    }

    private byte[] getTLV(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] tagbytes = Tag.getTag(in);
        if (tagbytes == null) {
            return null;
        }
        int length = DER.decodeLength(in);
        bos.write(tagbytes);
        DER.encodeLength(length, bos);
        byte[] TL = bos.toByteArray();
        byte[] buffer = new byte[TL.length + length];
        System.arraycopy(TL, 0, buffer, 0, TL.length);
        if (length > 0) {
            int actualLength = in.read(buffer, TL.length, length);
            if (actualLength == -1) {
                throw new EOFException();
            }
            if (actualLength != length) {
                throw new IOException("Length (" + length + ") mismatch: " + actualLength);
            }
        }
        return buffer;
    }

    class Component {
        Tag tag;
        int length;
        byte[] data;

        Component(Tag tag, int length, byte[] data) {
            this.tag = tag;
            this.length = length;
            this.data = (byte[])data.clone();
        }

        Tag getTag() {
            return this.tag;
        }

        int getLength() {
            return this.length;
        }

        byte[] getData() {
            return (byte[])this.data.clone();
        }
    }
}

