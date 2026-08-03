/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.Config;
import com.oscar.Driver;
import com.oscar.util.OSQLException;
import com.oscar.util.converter.CharacterSetByte;
import com.oscar.util.converter.CharacterSetUTF;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Hashtable;

public class Encoding {
    public static final Integer PG_SQL_ASCII = new Integer(0);
    public static final Integer PG_WIN1252 = new Integer(1);
    public static final Integer PG_BIG5 = new Integer(29);
    public static final Integer PG_GBK = new Integer(30);
    public static final Integer PG_UTF8 = new Integer(6);
    public static final Integer PG_UCS2 = new Integer(6);
    public static final Integer PG_GB18030 = new Integer(33);
    public static final Integer PG_UNICODE = new Integer(34);
    public static final Integer PG_ISO88591 = new Integer(8);
    private boolean logFlag = Driver.getLogLevel() >= 3;
    private static final Hashtable encodings = new Hashtable();
    private static final Hashtable encodingCodes;
    private static final Hashtable encodingNameMap;
    private static final Encoding DEFAULT_ENCODING;
    private static final Encoding SYSTEM_ENCODING;
    private String encoding;
    private final boolean fastASCIINumbers;

    public int getEncodingType() {
        if (encodings.get(this.encoding) == null) {
            if (this.logFlag) {
                Driver.writeLog("clientEncoding:" + this.encoding + ",jdbc not support");
            }
            return -1;
        }
        return (Integer)encodings.get(this.encoding);
    }

    protected Encoding() {
        this.encoding = Config.ENCODING;
        if (!Encoding.JVMisAvailable(this.encoding) && this.encoding.equals("GB18030")) {
            this.encoding = "GBK";
        }
        this.fastASCIINumbers = this.testAsciiNumbers();
    }

    protected Encoding(String encoding) {
        this.encoding = encoding;
        this.fastASCIINumbers = this.testAsciiNumbers();
    }

    public static Encoding getEncoding(String OSCARpassedEncoding) {
        if (OSCARpassedEncoding != null) {
            if ("UTF8".equalsIgnoreCase(OSCARpassedEncoding) || "UTF-8".equalsIgnoreCase(OSCARpassedEncoding)) {
                return new CharacterSetUTF();
            }
            if ("ASCII".equalsIgnoreCase(OSCARpassedEncoding)) {
                return new CharacterSetByte();
            }
        }
        if (OSCARpassedEncoding != null && Encoding.OSCARisAvailable(OSCARpassedEncoding)) {
            return new Encoding((String)encodingNameMap.get(OSCARpassedEncoding));
        }
        if (Encoding.SYSTEM_ENCODING.encoding != null) {
            return SYSTEM_ENCODING;
        }
        return DEFAULT_ENCODING;
    }

    public static Encoding getEncoding(Integer encodingCode) throws UnsupportedEncodingException {
        if (encodingCode.intValue() == PG_SQL_ASCII.intValue()) {
            return SYSTEM_ENCODING;
        }
        String dbEncoding = (String)encodingCodes.get(encodingCode);
        if (dbEncoding != null) {
            return new Encoding(dbEncoding);
        }
        if (encodingCode.intValue() == PG_UNICODE.intValue()) {
            return new Encoding((String)encodingCodes.get(PG_UTF8));
        }
        throw new UnsupportedEncodingException("OSCAR-00903");
    }

    public byte[] encode(String s) throws SQLException {
        byte[] l_return = null;
        try {
            if (s != null) {
                l_return = s.getBytes(this.encoding);
            }
            if (l_return == null) {
                l_return = new byte[]{};
            }
            return l_return;
        }
        catch (UnsupportedEncodingException e) {
            throw new OSQLException("OSCAR-00001", "88888", 1);
        }
    }

    public String decode(byte[] encodedString, int offset, int length) throws SQLException {
        try {
            return new String(encodedString, offset, length, this.encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new OSQLException("OSCAR-00001", "88888", 1);
        }
    }

    public String decode(byte[] encodedString) throws SQLException {
        return this.decode(encodedString, 0, encodedString.length);
    }

    public static Encoding defaultEncoding() {
        return DEFAULT_ENCODING;
    }

    private static boolean OSCARisAvailable(String OSCARencodingName) {
        if (encodingNameMap.get(OSCARencodingName) != null) {
            try {
                "DUMMY".getBytes((String)encodingNameMap.get(OSCARencodingName));
                return true;
            }
            catch (UnsupportedEncodingException e) {
                return false;
            }
        }
        return false;
    }

    private static boolean JVMisAvailable(String JVMencodingName) {
        if (encodings.get(JVMencodingName) != null) {
            try {
                "DUMMY".getBytes(JVMencodingName);
                return true;
            }
            catch (UnsupportedEncodingException e) {
                return false;
            }
        }
        return false;
    }

    public String getEncoding() {
        return this.encoding;
    }

    private boolean testAsciiNumbers() {
        try {
            String test = "-0123456789";
            byte[] bytes = this.encode(test);
            String res = new String(bytes, "US-ASCII");
            return test.equals(res);
        }
        catch (UnsupportedEncodingException e) {
            return false;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean hasAsciiNumbers() {
        return this.fastASCIINumbers;
    }

    public boolean equals(Encoding encoding) {
        if (encoding == null) {
            return false;
        }
        return this.encoding.equalsIgnoreCase(encoding.getEncoding());
    }

    static {
        encodings.put("ASCII", PG_SQL_ASCII);
        encodings.put("windows-1252", PG_WIN1252);
        encodings.put("BIG5", PG_BIG5);
        encodings.put("GBK", PG_GBK);
        encodings.put("GB2312", PG_GBK);
        encodings.put("UTF8", PG_UTF8);
        encodings.put("UTF-8", PG_UTF8);
        encodings.put("GB18030", PG_GB18030);
        encodings.put("ISO-8859-1", PG_ISO88591);
        encodingCodes = new Hashtable();
        encodingCodes.put(PG_SQL_ASCII, "ASCII");
        encodingCodes.put(PG_WIN1252, "windows-1252");
        encodingCodes.put(PG_BIG5, "BIG5");
        encodingCodes.put(PG_GBK, "GBK");
        encodingCodes.put(PG_UTF8, "UTF8");
        encodingCodes.put(PG_GB18030, "GB18030");
        encodingNameMap = new Hashtable();
        encodingNameMap.put("ASCII", "ASCII");
        encodingNameMap.put("WIN1252", "windows-1252");
        encodingNameMap.put("BIG5", "BIG5");
        encodingNameMap.put("GBK", "GBK");
        encodingNameMap.put("GB2312", "GBK");
        encodingNameMap.put("UTF8", "UTF8");
        encodingNameMap.put("UTF-8", "UTF8");
        encodingNameMap.put("UCS2", "UTF8");
        encodingNameMap.put("GB18030", "GB18030");
        DEFAULT_ENCODING = new Encoding("UTF8");
        SYSTEM_ENCODING = new Encoding();
    }
}

