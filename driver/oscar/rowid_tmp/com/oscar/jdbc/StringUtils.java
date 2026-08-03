/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.jdbc.SingleByteCharsetConverter;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtils {
    private static final int BYTE_RANGE = 256;
    private static byte[] allBytes = new byte[256];
    private static char[] byteToChars = new char[256];
    private static Method toPlainStringMethod;
    static final int WILD_COMPARE_MATCH_NO_WILD = 0;
    static final int WILD_COMPARE_MATCH_WITH_WILD = 1;
    static final int WILD_COMPARE_NO_MATCH = -1;

    public static String consistentToString(BigDecimal decimal) {
        if (decimal == null) {
            return null;
        }
        if (toPlainStringMethod != null) {
            try {
                return (String)toPlainStringMethod.invoke(decimal, new Object[]{null});
            }
            catch (InvocationTargetException invokeEx) {
            }
            catch (IllegalAccessException illegalAccessException) {
                // empty catch block
            }
        }
        return decimal.toString();
    }

    public static final String dumpAsHex(byte[] byteBuffer, int length) {
        int i;
        StringBuffer outputBuf = new StringBuffer(length * 4);
        int p = 0;
        int rows = length / 8;
        for (int i2 = 0; i2 < rows && p < length; ++i2) {
            int j;
            int ptemp = p;
            for (j = 0; j < 8; ++j) {
                String hexVal = Integer.toHexString(byteBuffer[ptemp] & 0xFF);
                if (hexVal.length() == 1) {
                    hexVal = "0" + hexVal;
                }
                outputBuf.append(hexVal + " ");
                ++ptemp;
            }
            outputBuf.append("    ");
            for (j = 0; j < 8; ++j) {
                int b = 0xFF & byteBuffer[p];
                if (b > 32 && b < 127) {
                    outputBuf.append((char)b + " ");
                } else {
                    outputBuf.append(". ");
                }
                ++p;
            }
            outputBuf.append("\n");
        }
        int n = 0;
        for (i = p; i < length; ++i) {
            String hexVal = Integer.toHexString(byteBuffer[i] & 0xFF);
            if (hexVal.length() == 1) {
                hexVal = "0" + hexVal;
            }
            outputBuf.append(hexVal + " ");
            ++n;
        }
        for (i = n; i < 8; ++i) {
            outputBuf.append("   ");
        }
        outputBuf.append("    ");
        for (i = p; i < length; ++i) {
            int b = 0xFF & byteBuffer[i];
            if (b > 32 && b < 127) {
                outputBuf.append((char)b + " ");
                continue;
            }
            outputBuf.append(". ");
        }
        outputBuf.append("\n");
        return outputBuf.toString();
    }

    private static boolean endsWith(byte[] dataFrom, String suffix) {
        for (int i = 1; i <= suffix.length(); ++i) {
            int dfOffset = dataFrom.length - i;
            int suffixOffset = suffix.length() - i;
            if (dataFrom[dfOffset] == suffix.charAt(suffixOffset)) continue;
            return false;
        }
        return true;
    }

    public static byte[] escapeEasternUnicodeByteStream(byte[] origBytes, String origString, int offset, int length) {
        if (origBytes == null || origBytes.length == 0) {
            return origBytes;
        }
        int bytesLen = origBytes.length;
        int bufIndex = 0;
        int strIndex = 0;
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(bytesLen);
        while (true) {
            if (origString.charAt(strIndex) == '\\') {
                bytesOut.write(origBytes[bufIndex++]);
            } else {
                int hiByte;
                int loByte = origBytes[bufIndex];
                if (loByte < 0) {
                    loByte += 256;
                }
                bytesOut.write(loByte);
                if (loByte >= 128) {
                    if (bufIndex < bytesLen - 1) {
                        hiByte = origBytes[bufIndex + 1];
                        if (hiByte < 0) {
                            hiByte += 256;
                        }
                        bytesOut.write(hiByte);
                        ++bufIndex;
                        if (hiByte == 92) {
                            bytesOut.write(hiByte);
                        }
                    }
                } else if (loByte == 92 && bufIndex < bytesLen - 1) {
                    hiByte = origBytes[bufIndex + 1];
                    if (hiByte < 0) {
                        hiByte += 256;
                    }
                    if (hiByte == 98) {
                        bytesOut.write(92);
                        bytesOut.write(98);
                        ++bufIndex;
                    }
                }
                ++bufIndex;
            }
            if (bufIndex >= bytesLen) break;
            ++strIndex;
        }
        return bytesOut.toByteArray();
    }

    public static char firstNonWsCharUc(String searchIn) {
        return StringUtils.firstNonWsCharUc(searchIn, 0);
    }

    public static char firstNonWsCharUc(String searchIn, int startAt) {
        if (searchIn == null) {
            return '\u0000';
        }
        int length = searchIn.length();
        for (int i = startAt; i < length; ++i) {
            char c = searchIn.charAt(i);
            if (Character.isWhitespace(c)) continue;
            return Character.toUpperCase(c);
        }
        return '\u0000';
    }

    public static char firstAlphaCharUc(String searchIn, int startAt) {
        if (searchIn == null) {
            return '\u0000';
        }
        int length = searchIn.length();
        for (int i = startAt; i < length; ++i) {
            char c = searchIn.charAt(i);
            if (!Character.isLetter(c)) continue;
            return Character.toUpperCase(c);
        }
        return '\u0000';
    }

    public static final String fixDecimalExponent(String dString) {
        char maybeMinusChar;
        int ePos = dString.indexOf("E");
        if (ePos == -1) {
            ePos = dString.indexOf("e");
        }
        if (ePos != -1 && dString.length() > ePos + 1 && (maybeMinusChar = dString.charAt(ePos + 1)) != '-' && maybeMinusChar != '+') {
            StringBuffer buf = new StringBuffer(dString.length() + 1);
            buf.append(dString.substring(0, ePos + 1));
            buf.append('+');
            buf.append(dString.substring(ePos + 1, dString.length()));
            dString = buf.toString();
        }
        return dString;
    }

    public static final byte[] getBytes(char[] c, SingleByteCharsetConverter converter, String encoding, String serverEncoding, boolean parserKnowsUnicode) throws SQLException {
        byte[] b = null;
        try {
            if (converter != null) {
                b = converter.toBytes(c);
            } else if (encoding == null) {
                b = new String(c).getBytes();
            } else {
                String s = new String(c);
                b = s.getBytes(encoding);
                if (!parserKnowsUnicode && (encoding.equalsIgnoreCase("SJIS") || encoding.equalsIgnoreCase("BIG5") || encoding.equalsIgnoreCase("GBK")) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = StringUtils.escapeEasternUnicodeByteStream(b, s, 0, s.length());
                }
            }
        }
        catch (UnsupportedEncodingException uee) {
            // empty catch block
        }
        return b;
    }

    public static final byte[] getBytes(char[] c, SingleByteCharsetConverter converter, String encoding, String serverEncoding, int offset, int length, boolean parserKnowsUnicode) throws SQLException {
        byte[] b = null;
        try {
            if (converter != null) {
                b = converter.toBytes(c, offset, length);
            } else if (encoding == null) {
                byte[] temp = new String(c, offset, length).getBytes();
                length = temp.length;
                b = new byte[length];
                System.arraycopy(temp, 0, b, 0, length);
            } else {
                String s = new String(c, offset, length);
                byte[] temp = s.getBytes(encoding);
                length = temp.length;
                b = new byte[length];
                System.arraycopy(temp, 0, b, 0, length);
                if (!parserKnowsUnicode && (encoding.equalsIgnoreCase("SJIS") || encoding.equalsIgnoreCase("BIG5") || encoding.equalsIgnoreCase("GBK")) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = StringUtils.escapeEasternUnicodeByteStream(b, s, offset, length);
                }
            }
        }
        catch (UnsupportedEncodingException uee) {
            // empty catch block
        }
        return b;
    }

    public static final byte[] getBytes(char[] c, String encoding, String serverEncoding, boolean parserKnowsUnicode) throws SQLException {
        byte[] temp = null;
        try {
            SingleByteCharsetConverter converter = null;
            converter = SingleByteCharsetConverter.getInstance(encoding);
            temp = StringUtils.getBytes(c, converter, encoding, serverEncoding, parserKnowsUnicode);
        }
        catch (UnsupportedEncodingException uee) {
            // empty catch block
        }
        return temp;
    }

    public static final byte[] getBytes(String s, SingleByteCharsetConverter converter, String encoding, String serverEncoding, boolean parserKnowsUnicode) throws SQLException {
        byte[] b = null;
        try {
            if (converter != null) {
                b = converter.toBytes(s);
            } else if (encoding == null) {
                b = s.getBytes();
            } else {
                b = s.getBytes(encoding);
                if (!parserKnowsUnicode && (encoding.equalsIgnoreCase("SJIS") || encoding.equalsIgnoreCase("BIG5") || encoding.equalsIgnoreCase("GBK")) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = StringUtils.escapeEasternUnicodeByteStream(b, s, 0, s.length());
                }
            }
        }
        catch (UnsupportedEncodingException uee) {
            // empty catch block
        }
        return b;
    }

    public static final byte[] getBytesWrapped(String s, char beginWrap, char endWrap, SingleByteCharsetConverter converter, String encoding, String serverEncoding, boolean parserKnowsUnicode) throws SQLException {
        byte[] b = null;
        try {
            if (converter != null) {
                b = converter.toBytesWrapped(s, beginWrap, endWrap);
            } else if (encoding == null) {
                StringBuffer buf = new StringBuffer(s.length() + 2);
                buf.append(beginWrap);
                buf.append(s);
                buf.append(endWrap);
                b = buf.toString().getBytes();
            } else {
                StringBuffer buf = new StringBuffer(s.length() + 2);
                buf.append(beginWrap);
                buf.append(s);
                buf.append(endWrap);
                b = buf.toString().getBytes(encoding);
                if (!parserKnowsUnicode && (encoding.equalsIgnoreCase("SJIS") || encoding.equalsIgnoreCase("BIG5") || encoding.equalsIgnoreCase("GBK")) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = StringUtils.escapeEasternUnicodeByteStream(b, s, 0, s.length());
                }
            }
        }
        catch (UnsupportedEncodingException uee) {
            // empty catch block
        }
        return b;
    }

    public static final byte[] getBytes(String s, SingleByteCharsetConverter converter, String encoding, String serverEncoding, int offset, int length, boolean parserKnowsUnicode) throws SQLException {
        byte[] b = null;
        try {
            if (converter != null) {
                b = converter.toBytes(s, offset, length);
            } else if (encoding == null) {
                byte[] temp = s.substring(offset, offset + length).getBytes();
                length = temp.length;
                b = new byte[length];
                System.arraycopy(temp, 0, b, 0, length);
            } else {
                byte[] temp = s.substring(offset, offset + length).getBytes(encoding);
                length = temp.length;
                b = new byte[length];
                System.arraycopy(temp, 0, b, 0, length);
                if (!parserKnowsUnicode && (encoding.equalsIgnoreCase("SJIS") || encoding.equalsIgnoreCase("BIG5") || encoding.equalsIgnoreCase("GBK")) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = StringUtils.escapeEasternUnicodeByteStream(b, s, offset, length);
                }
            }
        }
        catch (UnsupportedEncodingException uee) {
            // empty catch block
        }
        return b;
    }

    public static final byte[] getBytes(String s, String encoding, String serverEncoding, boolean parserKnowsUnicode) throws SQLException {
        byte[] temp = null;
        try {
            SingleByteCharsetConverter converter = null;
            converter = SingleByteCharsetConverter.getInstance(encoding);
            temp = StringUtils.getBytes(s, converter, encoding, serverEncoding, parserKnowsUnicode);
        }
        catch (UnsupportedEncodingException uee) {
            // empty catch block
        }
        return temp;
    }

    public static int getInt(byte[] buf, int offset, int endPos) throws NumberFormatException {
        int s;
        char base = '\n';
        for (s = offset; Character.isWhitespace((char)buf[s]) && s < endPos; ++s) {
        }
        if (s == endPos) {
            throw new NumberFormatException(new String(buf));
        }
        boolean negative = false;
        if ((char)buf[s] == '-') {
            negative = true;
            ++s;
        } else if ((char)buf[s] == '+') {
            ++s;
        }
        int save = s;
        int cutoff = Integer.MAX_VALUE / base;
        int cutlim = Integer.MAX_VALUE % base;
        if (negative) {
            ++cutlim;
        }
        boolean overflow = false;
        int i = 0;
        while (s < endPos) {
            char c = (char)buf[s];
            if (Character.isDigit(c)) {
                c = (char)(c - 48);
            } else {
                if (!Character.isLetter(c)) break;
                c = (char)(Character.toUpperCase(c) - 65 + 10);
            }
            if (c >= base) break;
            if (i > cutoff || i == cutoff && c > cutlim) {
                overflow = true;
            } else {
                i *= base;
                i += c;
            }
            ++s;
        }
        if (s == save) {
            throw new NumberFormatException(new String(buf));
        }
        if (overflow) {
            throw new NumberFormatException(new String(buf));
        }
        return negative ? -i : i;
    }

    public static int getInt(byte[] buf) throws NumberFormatException {
        return StringUtils.getInt(buf, 0, buf.length);
    }

    public static long getLong(byte[] buf) throws NumberFormatException {
        return StringUtils.getLong(buf, 0, buf.length);
    }

    public static long getLong(byte[] buf, int offset, int endpos) throws NumberFormatException {
        int s;
        char base = '\n';
        for (s = offset; Character.isWhitespace((char)buf[s]) && s < endpos; ++s) {
        }
        if (s == endpos) {
            throw new NumberFormatException(new String(buf));
        }
        boolean negative = false;
        if ((char)buf[s] == '-') {
            negative = true;
            ++s;
        } else if ((char)buf[s] == '+') {
            ++s;
        }
        int save = s;
        long cutoff = Long.MAX_VALUE / (long)base;
        long cutlim = (int)(Long.MAX_VALUE % (long)base);
        if (negative) {
            ++cutlim;
        }
        boolean overflow = false;
        long i = 0L;
        while (s < endpos) {
            char c = (char)buf[s];
            if (Character.isDigit(c)) {
                c = (char)(c - 48);
            } else {
                if (!Character.isLetter(c)) break;
                c = (char)(Character.toUpperCase(c) - 65 + 10);
            }
            if (c >= base) break;
            if (i > cutoff || i == cutoff && (long)c > cutlim) {
                overflow = true;
            } else {
                i *= (long)base;
                i += (long)c;
            }
            ++s;
        }
        if (s == save) {
            throw new NumberFormatException(new String(buf));
        }
        if (overflow) {
            throw new NumberFormatException(new String(buf));
        }
        return negative ? -i : i;
    }

    public static short getShort(byte[] buf) throws NumberFormatException {
        int s;
        char base = '\n';
        for (s = 0; Character.isWhitespace((char)buf[s]) && s < buf.length; ++s) {
        }
        if (s == buf.length) {
            throw new NumberFormatException(new String(buf));
        }
        boolean negative = false;
        if ((char)buf[s] == '-') {
            negative = true;
            ++s;
        } else if ((char)buf[s] == '+') {
            ++s;
        }
        int save = s;
        short cutoff = (short)(Short.MAX_VALUE / base);
        short cutlim = (short)(Short.MAX_VALUE % base);
        if (negative) {
            cutlim = (short)(cutlim + 1);
        }
        boolean overflow = false;
        short i = 0;
        while (s < buf.length) {
            char c = (char)buf[s];
            if (Character.isDigit(c)) {
                c = (char)(c - 48);
            } else {
                if (!Character.isLetter(c)) break;
                c = (char)(Character.toUpperCase(c) - 65 + 10);
            }
            if (c >= base) break;
            if (i > cutoff || i == cutoff && c > cutlim) {
                overflow = true;
            } else {
                i = (short)(i * base);
                i = (short)(i + c);
            }
            ++s;
        }
        if (s == save) {
            throw new NumberFormatException(new String(buf));
        }
        if (overflow) {
            throw new NumberFormatException(new String(buf));
        }
        return negative ? (short)(-i) : i;
    }

    public static final int indexOfIgnoreCase(int startingPosition, String searchIn, String searchFor) {
        int i;
        if (searchIn == null || searchFor == null || startingPosition > searchIn.length()) {
            return -1;
        }
        int patternLength = searchFor.length();
        int stringLength = searchIn.length();
        int stopSearchingAt = stringLength - patternLength;
        if (patternLength == 0) {
            return -1;
        }
        char firstCharOfPatternUc = Character.toUpperCase(searchFor.charAt(0));
        char firstCharOfPatternLc = Character.toLowerCase(searchFor.charAt(0));
        for (i = startingPosition; i < stopSearchingAt && Character.toUpperCase(searchIn.charAt(i)) != firstCharOfPatternUc && Character.toLowerCase(searchIn.charAt(i)) != firstCharOfPatternLc; ++i) {
        }
        if (i > stopSearchingAt) {
            return -1;
        }
        int j = i + 1;
        int end = j + patternLength - 1;
        int k = 1;
        while (j < end) {
            int searchInPos = j++;
            int searchForPos = k++;
            if (Character.toUpperCase(searchIn.charAt(searchInPos)) != Character.toUpperCase(searchFor.charAt(searchForPos))) {
                ++i;
                break;
            }
            if (Character.toLowerCase(searchIn.charAt(searchInPos)) == Character.toLowerCase(searchFor.charAt(searchForPos))) continue;
            ++i;
            break;
        }
        return i;
    }

    public static final int indexOfIgnoreCase(String searchIn, String searchFor) {
        return StringUtils.indexOfIgnoreCase(0, searchIn, searchFor);
    }

    public static int indexOfIgnoreCaseRespectMarker(int startAt, String src, String target, String marker, String markerCloses, boolean allowBackslashEscapes) {
        char contextMarker = '\u0000';
        boolean escaped = false;
        int markerTypeFound = 0;
        int srcLength = src.length();
        int ind = 0;
        for (int i = startAt; i < srcLength; ++i) {
            char c = src.charAt(i);
            if (allowBackslashEscapes && c == '\\') {
                escaped = !escaped;
                continue;
            }
            if (c == markerCloses.charAt(markerTypeFound) && !escaped) {
                contextMarker = '\u0000';
                continue;
            }
            ind = marker.indexOf(c);
            if (ind != -1 && !escaped && contextMarker == '\u0000') {
                markerTypeFound = ind;
                contextMarker = c;
                continue;
            }
            if (c != target.charAt(0) || escaped || contextMarker != '\u0000' || StringUtils.indexOfIgnoreCase(i, src, target) == -1) continue;
            return i;
        }
        return -1;
    }

    public static int indexOfIgnoreCaseRespectQuotes(int startAt, String src, String target, char quoteChar, boolean allowBackslashEscapes) {
        char contextMarker = '\u0000';
        boolean escaped = false;
        int srcLength = src.length();
        for (int i = startAt; i < srcLength; ++i) {
            char c = src.charAt(i);
            if (allowBackslashEscapes && c == '\\') {
                escaped = !escaped;
                continue;
            }
            if (c == contextMarker && !escaped) {
                contextMarker = '\u0000';
                continue;
            }
            if (c == quoteChar && !escaped && contextMarker == '\u0000') {
                contextMarker = c;
                continue;
            }
            if (Character.toUpperCase(c) != Character.toUpperCase(target.charAt(0)) && Character.toLowerCase(c) != Character.toLowerCase(target.charAt(0)) || escaped || contextMarker != '\u0000' || !StringUtils.startsWithIgnoreCase(src, i, target)) continue;
            return i;
        }
        return -1;
    }

    public static final List split(String stringToSplit, String delimitter, boolean trim) {
        if (stringToSplit == null) {
            return new ArrayList();
        }
        if (delimitter == null) {
            throw new IllegalArgumentException();
        }
        StringTokenizer tokenizer = new StringTokenizer(stringToSplit, delimitter, false);
        ArrayList<String> splitTokens = new ArrayList<String>(tokenizer.countTokens());
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (trim) {
                token = token.trim();
            }
            splitTokens.add(token);
        }
        return splitTokens;
    }

    public static final List split(String stringToSplit, String delimiter, String markers, String markerCloses, boolean trim) {
        String token;
        if (stringToSplit == null) {
            return new ArrayList();
        }
        if (delimiter == null) {
            throw new IllegalArgumentException();
        }
        int delimPos = 0;
        int currentPos = 0;
        ArrayList<String> splitTokens = new ArrayList<String>();
        while ((delimPos = StringUtils.indexOfIgnoreCaseRespectMarker(currentPos, stringToSplit, delimiter, markers, markerCloses, false)) != -1) {
            token = stringToSplit.substring(currentPos, delimPos);
            if (trim) {
                token = token.trim();
            }
            splitTokens.add(token);
            currentPos = delimPos + 1;
        }
        if (currentPos < stringToSplit.length()) {
            token = stringToSplit.substring(currentPos);
            if (trim) {
                token = token.trim();
            }
            splitTokens.add(token);
        }
        return splitTokens;
    }

    private static boolean startsWith(byte[] dataFrom, String chars) {
        for (int i = 0; i < chars.length(); ++i) {
            if (dataFrom[i] == chars.charAt(i)) continue;
            return false;
        }
        return true;
    }

    public static boolean startsWithIgnoreCase(String searchIn, int startAt, String searchFor) {
        return searchIn.regionMatches(true, startAt, searchFor, 0, searchFor.length());
    }

    public static boolean startsWithIgnoreCase(String searchIn, String searchFor) {
        return StringUtils.startsWithIgnoreCase(searchIn, 0, searchFor);
    }

    public static boolean startsWithIgnoreCaseAndNonAlphaNumeric(String searchIn, String searchFor) {
        char c;
        if (searchIn == null) {
            return searchFor == null;
        }
        int beginPos = 0;
        int inLength = searchIn.length();
        for (beginPos = 0; beginPos < inLength && !Character.isLetterOrDigit(c = searchIn.charAt(beginPos)); ++beginPos) {
        }
        return StringUtils.startsWithIgnoreCase(searchIn, beginPos, searchFor);
    }

    public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor) {
        return StringUtils.startsWithIgnoreCaseAndWs(searchIn, searchFor, 0);
    }

    public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor, int beginPos) {
        if (searchIn == null) {
            return searchFor == null;
        }
        int inLength = searchIn.length();
        while (beginPos < inLength && Character.isWhitespace(searchIn.charAt(beginPos))) {
            ++beginPos;
        }
        return StringUtils.startsWithIgnoreCase(searchIn, beginPos, searchFor);
    }

    public static byte[] stripEnclosure(byte[] source, String prefix, String suffix) {
        if (source.length >= prefix.length() + suffix.length() && StringUtils.startsWith(source, prefix) && StringUtils.endsWith(source, suffix)) {
            int totalToStrip = prefix.length() + suffix.length();
            int enclosedLength = source.length - totalToStrip;
            byte[] enclosed = new byte[enclosedLength];
            int startPos = prefix.length();
            int numToCopy = enclosed.length;
            System.arraycopy(source, startPos, enclosed, 0, numToCopy);
            return enclosed;
        }
        return source;
    }

    public static final String toAsciiString(byte[] buffer) {
        return StringUtils.toAsciiString(buffer, 0, buffer.length);
    }

    public static final String toAsciiString(byte[] buffer, int startPos, int length) {
        char[] charArray = new char[length];
        int readpoint = startPos;
        for (int i = 0; i < length; ++i) {
            charArray[i] = (char)buffer[readpoint];
            ++readpoint;
        }
        return new String(charArray);
    }

    public static int wildCompare(String searchIn, String searchForWildcard) {
        if (searchIn == null || searchForWildcard == null) {
            return -1;
        }
        if (searchForWildcard.equals("%")) {
            return 1;
        }
        int result = -1;
        char wildcardMany = '%';
        char wildcardOne = '_';
        char wildcardEscape = '\\';
        int searchForPos = 0;
        int searchForEnd = searchForWildcard.length();
        int searchInPos = 0;
        int searchInEnd = searchIn.length();
        while (searchForPos != searchForEnd) {
            char wildstrChar = searchForWildcard.charAt(searchForPos);
            while (searchForWildcard.charAt(searchForPos) != wildcardMany && wildstrChar != wildcardOne) {
                if (searchForWildcard.charAt(searchForPos) == wildcardEscape && searchForPos + 1 != searchForEnd) {
                    ++searchForPos;
                }
                if (searchInPos == searchInEnd || Character.toUpperCase(searchForWildcard.charAt(searchForPos++)) != Character.toUpperCase(searchIn.charAt(searchInPos++))) {
                    return 1;
                }
                if (searchForPos == searchForEnd) {
                    return searchInPos != searchInEnd ? 1 : 0;
                }
                result = 1;
            }
            if (searchForWildcard.charAt(searchForPos) == wildcardOne) {
                do {
                    if (searchInPos == searchInEnd) {
                        return result;
                    }
                    ++searchInPos;
                } while (++searchForPos < searchForEnd && searchForWildcard.charAt(searchForPos) == wildcardOne);
                if (searchForPos == searchForEnd) break;
            }
            if (searchForWildcard.charAt(searchForPos) != wildcardMany) continue;
            ++searchForPos;
            while (searchForPos != searchForEnd) {
                if (searchForWildcard.charAt(searchForPos) != wildcardMany) {
                    if (searchForWildcard.charAt(searchForPos) != wildcardOne) break;
                    if (searchInPos == searchInEnd) {
                        return -1;
                    }
                    ++searchInPos;
                }
                ++searchForPos;
            }
            if (searchForPos == searchForEnd) {
                return 0;
            }
            if (searchInPos == searchInEnd) {
                return -1;
            }
            char cmp = searchForWildcard.charAt(searchForPos);
            if (cmp == wildcardEscape && searchForPos + 1 != searchForEnd) {
                cmp = searchForWildcard.charAt(++searchForPos);
            }
            ++searchForPos;
            while (true) {
                if (searchInPos != searchInEnd && Character.toUpperCase(searchIn.charAt(searchInPos)) != Character.toUpperCase(cmp)) {
                    ++searchInPos;
                    continue;
                }
                if (searchInPos++ == searchInEnd) {
                    return -1;
                }
                int tmp = StringUtils.wildCompare(searchIn, searchForWildcard);
                if (tmp <= 0) {
                    return tmp;
                }
                if (searchInPos == searchInEnd || searchForWildcard.charAt(0) == wildcardMany) break;
            }
            return -1;
        }
        return searchInPos != searchInEnd ? 1 : 0;
    }

    public static int lastIndexOf(byte[] s, char c) {
        if (s == null) {
            return -1;
        }
        for (int i = s.length - 1; i >= 0; --i) {
            if (s[i] != c) continue;
            return i;
        }
        return -1;
    }

    public static int indexOf(byte[] s, char c) {
        if (s == null) {
            return -1;
        }
        int length = s.length;
        for (int i = 0; i < length; ++i) {
            if (s[i] != c) continue;
            return i;
        }
        return -1;
    }

    public static boolean isNullOrEmpty(String toTest) {
        return toTest == null || toTest.length() == 0;
    }

    public static final boolean isEmptyOrWhitespaceOnly(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        int length = str.length();
        for (int i = 0; i < length; ++i) {
            if (Character.isWhitespace(str.charAt(i))) continue;
            return false;
        }
        return true;
    }

    static {
        for (int i = -128; i <= 127; ++i) {
            StringUtils.allBytes[i - -128] = (byte)i;
        }
        String allBytesString = new String(allBytes, 0, 255);
        int allBytesStringLen = allBytesString.length();
        for (int i = 0; i < 255 && i < allBytesStringLen; ++i) {
            StringUtils.byteToChars[i] = allBytesString.charAt(i);
        }
        try {
            toPlainStringMethod = BigDecimal.class.getMethod("toPlainString", new Class[0]);
        }
        catch (NoSuchMethodException noSuchMethodException) {
            // empty catch block
        }
    }
}

