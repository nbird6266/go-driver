/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.lang;

import cryptix.asn1.lang.PackageProperties;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;

public class Tag {
    private static final String NAME = "Tag";
    private static final boolean IN = true;
    private static final boolean OUT = false;
    private static final boolean DEBUG = false;
    private static final int debuglevel = 0;
    private static final PrintWriter err = null;
    private static final boolean TRACE = PackageProperties.isTraceable("Tag");
    public static final int UNIVERSAL = 0;
    public static final int APPLICATION = 64;
    public static final int CONTEXT = 128;
    public static final int PRIVATE = 192;
    public static final int BOOLEAN = 1;
    public static final int INTEGER = 2;
    public static final int BIT_STRING = 3;
    public static final int OCTET_STRING = 4;
    public static final int NULL = 5;
    public static final int OBJECT_IDENTIFIER = 6;
    public static final int SEQUENCE = 16;
    public static final int SEQUENCE_OF = 48;
    public static final int SET = 17;
    public static final int SET_OF = 49;
    public static final int PRINT_STRING = 19;
    public static final int T61_STRING = 20;
    public static final int IA5_STRING = 22;
    public static final int UTC_TIME = 23;
    int clazz;
    int value;
    boolean explicit;
    boolean constructed;

    private static void debug(String s) {
        err.println(">>> Tag: " + s);
    }

    private static void trace(boolean in, String s) {
        if (TRACE) {
            err.println(String.valueOf(in ? "==> " : "<== ") + NAME + "." + s);
        }
    }

    private static void trace(String s) {
        if (TRACE) {
            err.println("<=> Tag." + s);
        }
    }

    Tag(int clazz, int value, boolean explicit, boolean constructed) {
        this.clazz = clazz;
        this.value = value;
        this.explicit = explicit;
        this.constructed = constructed;
    }

    Tag(int clazz, int value, boolean explicit) {
        this(clazz, value, explicit, false);
    }

    Tag(int value, boolean explicit) {
        this(0, value, explicit);
    }

    public int getClazz() {
        return this.clazz;
    }

    public int getValue() {
        return this.value;
    }

    public boolean isExplicit() {
        return this.explicit;
    }

    public boolean isConstructed() {
        return this.constructed;
    }

    /*
     * Unable to fully structure code
     */
    public static Tag getExpectedTag(int expectedClass, int expectedValue, InputStream in) throws IOException {
        block8: {
            result = null;
            recovery = new ByteArrayOutputStream();
            tagClass = -1;
            tagValue = -1;
            c = in.read();
            if (c >= 0) break block8;
lbl8:
            // 3 sources

            while (true) {
                if (!Tag.eval(tagClass, expectedClass, tagValue, expectedValue)) {
                    result = null;
                    recovered = recovery.toByteArray();
                    pbis = in instanceof PushbackInputStream != false ? (PushbackInputStream)in : new PushbackInputStream(in, 2048);
                    pbis.unread(recovered);
                    in = pbis;
                }
                recovery = null;
                return null;
            }
        }
        try {
            block9: {
                recovery.write(c &= 255);
                tagClass = c & 192;
                tagConstructed = (c & 32) != 0;
                tagValue = c & 31;
                if (tagValue != 31) break block9;
                tagValue = 0;
                c = in.read();
                if (c < 0) ** GOTO lbl8
                c &= 255;
                do {
                    tagValue += c & 128;
                    if ((c = in.read()) < 0) ** continue;
                    recovery.write(c &= 255);
                } while ((c & 128) != 0);
            }
            result = new Tag(tagClass, tagValue, true, tagConstructed);
        }
        catch (Throwable var9_15) {
            if (!Tag.eval(tagClass, expectedClass, tagValue, expectedValue)) {
                result = null;
                recovered = recovery.toByteArray();
                pbis = in instanceof PushbackInputStream != false ? (PushbackInputStream)in : new PushbackInputStream(in, 2048);
                pbis.unread(recovered);
                in = pbis;
            }
            recovery = null;
            throw var9_15;
        }
        if (!Tag.eval(tagClass, expectedClass, tagValue, expectedValue)) {
            result = null;
            recovered = recovery.toByteArray();
            pbis = in instanceof PushbackInputStream != false ? (PushbackInputStream)in : new PushbackInputStream(in, 2048);
            pbis.unread(recovered);
            in = pbis;
        }
        recovery = null;
        return result;
    }

    public static Tag getExpectedTag(int expectedValue, InputStream in) throws IOException {
        return Tag.getExpectedTag(0, expectedValue, in);
    }

    public static Tag getExpectedTag(Tag tag, InputStream in) throws IOException {
        return Tag.getExpectedTag(tag.getClazz(), tag.getValue(), in);
    }

    public static byte[] getTag(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int c = in.read();
        if (c == -1) {
            return null;
        }
        bos.write(c &= 0xFF);
        if ((c & 0x1F) == 31) {
            do {
                c = in.read() & 0xFF;
                bos.write(c);
            } while ((c & 0x80) != 0);
        }
        return bos.toByteArray();
    }

    public static Tag decode(InputStream in) throws IOException {
        int c = in.read() & 0xFF;
        int tagClass = c & 0xC0;
        boolean tagConstructed = (c & 0x20) != 0;
        int tagNumber = c & 0x1F;
        if (tagNumber == 31) {
            tagNumber = 0;
            c = in.read() & 0xFF;
            do {
                tagNumber += c & 0x80;
            } while (((c = in.read() & 0xFF) & 0x80) != 0);
        }
        Tag result = new Tag(tagClass, tagNumber, true, tagConstructed);
        return result;
    }

    public static Tag peek(InputStream in) throws IOException {
        in.mark(Integer.MAX_VALUE);
        Tag result = null;
        try {
            result = Tag.decode(in);
        }
        finally {
            in.reset();
        }
        return result;
    }

    private static boolean eval(int tagClass, int expectedClass, int tagValue, int expectedValue) {
        if (tagClass != expectedClass) {
            return false;
        }
        if (tagClass != 0) {
            return tagValue == expectedValue;
        }
        if (expectedValue == 19 || expectedValue == 22 || tagValue == 20) {
            return tagValue == 19 || tagValue == 22 || tagValue == 20;
        }
        return tagValue == expectedValue;
    }

    public String toString() {
        String result = "<Tag class=\"";
        switch (this.clazz) {
            case 0: {
                result = String.valueOf(result) + "UNIVERSAL (0)";
                break;
            }
            case 64: {
                result = String.valueOf(result) + "APPLICATION (64)";
                break;
            }
            case 128: {
                result = String.valueOf(result) + "CONTEXT (128)";
                break;
            }
            case 192: {
                result = String.valueOf(result) + "PRIVATE (192)";
                break;
            }
            default: {
                result = String.valueOf(result) + this.clazz;
            }
        }
        result = String.valueOf(result) + "\" value=\"";
        if (this.clazz == 128) {
            result = String.valueOf(result) + this.value;
        } else {
            switch (this.value) {
                case 1: {
                    result = String.valueOf(result) + "BOOLEAN (1)";
                    break;
                }
                case 2: {
                    result = String.valueOf(result) + "INTEGER (2)";
                    break;
                }
                case 3: {
                    result = String.valueOf(result) + "BIT STRING (3)";
                    break;
                }
                case 4: {
                    result = String.valueOf(result) + "OCTET STRING (4)";
                    break;
                }
                case 5: {
                    result = String.valueOf(result) + "NULL (5)";
                    break;
                }
                case 6: {
                    result = String.valueOf(result) + "OBJECT_IDENTIFIER (6)";
                    break;
                }
                case 16: {
                    result = String.valueOf(result) + "SEQUENCE (16)";
                    break;
                }
                case 17: {
                    result = String.valueOf(result) + "SET (17)";
                    break;
                }
                case 19: {
                    result = String.valueOf(result) + "PrintableString (19)";
                    break;
                }
                case 20: {
                    result = String.valueOf(result) + "T61String (20)";
                    break;
                }
                case 22: {
                    result = String.valueOf(result) + "IA5String (22)";
                    break;
                }
                case 23: {
                    result = String.valueOf(result) + "UTCTime (23)";
                    break;
                }
                default: {
                    result = String.valueOf(result) + this.value;
                }
            }
        }
        result = String.valueOf(result) + "\" explicit=\"" + (this.explicit ? "yes" : "no");
        result = String.valueOf(result) + "\" constructed=\"" + (this.constructed ? "yes" : "no") + "\" />";
        return result;
    }
}

