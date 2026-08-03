/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

import com.oscar.core.Encoding;

public abstract class CharacterSet
extends Encoding {
    public static final byte[] stringToUTF(String value) {
        try {
            char[] chararr = value.toCharArray();
            int maxNbBytes = chararr.length * 3;
            byte[] bytearr = null;
            byte[] rbytearr = null;
            bytearr = new byte[maxNbBytes];
            int byte_len = CharacterSet.convertJavaCharsToUTFBytes(chararr, 0, bytearr, 0, chararr.length);
            rbytearr = new byte[byte_len];
            System.arraycopy(bytearr, 0, rbytearr, 0, byte_len);
            bytearr = null;
            return rbytearr;
        }
        catch (Exception e) {
            System.out.println("stringToUTF fail");
            return null;
        }
    }

    public static final int convertJavaCharsToUTFBytes(char[] chars, int chars_offset, byte[] bytes, int bytes_begin, int chars_count) {
        int chars_begin = chars_offset;
        int chars_end = chars_offset + chars_count;
        int byte_index = bytes_begin;
        for (int i = chars_begin; i < chars_end; ++i) {
            char c = chars[i];
            if (c >= '\u0000' && c <= '\u007f') {
                bytes[byte_index++] = (byte)c;
                continue;
            }
            if (c > '\u07ff') {
                bytes[byte_index++] = (byte)(0xE0 | c >>> 12 & 0xF);
                bytes[byte_index++] = (byte)(0x80 | c >>> 6 & 0x3F);
                bytes[byte_index++] = (byte)(0x80 | c >>> 0 & 0x3F);
                continue;
            }
            bytes[byte_index++] = (byte)(0xC0 | c >>> 6 & 0x1F);
            bytes[byte_index++] = (byte)(0x80 | c >>> 0 & 0x3F);
        }
        return byte_index - bytes_begin;
    }

    public static final int convertUTFBytesToJavaChars(byte[] bytes, int offset, char[] chars, int chars_offset, int count, boolean convertWithReplacement) {
        return CharacterSet.convertUTFBytesToJavaChars(bytes, offset, chars, chars_offset, count, convertWithReplacement, chars.length - chars_offset);
    }

    public static final int convertUTFBytesToJavaChars(byte[] bytes, int offset, char[] chars, int chars_offset, int count, boolean convertWithReplacement, int charSize) {
        int bytes_index = offset;
        int bytes_end = offset + count;
        int chars_index = chars_offset;
        int charsLength = chars_offset + charSize;
        block5: while (bytes_index < bytes_end) {
            byte c = bytes[bytes_index++];
            int b = c & 0xF0;
            switch (b / 16) {
                case 0: 
                case 1: 
                case 2: 
                case 3: 
                case 4: 
                case 5: 
                case 6: 
                case 7: {
                    if (chars_index < charsLength) {
                        chars[chars_index++] = (char)(c & 0xFFFFFFFF);
                        continue block5;
                    }
                    count = bytes_end - bytes_index + 2;
                    break;
                }
                case 12: 
                case 13: {
                    if (bytes_index >= bytes_end) break;
                    char c2 = CharacterSet.conv2ByteUTFtoUTF16(c, bytes[bytes_index++]);
                    if (chars_index < charsLength) {
                        chars[chars_index++] = c2;
                        break;
                    }
                    count = bytes_end - bytes_index + 3;
                    break;
                }
                case 14: {
                    int c1;
                    char c2;
                    if (bytes_index + 1 >= bytes_end) break;
                    if (CharacterSet.isHiSurrogate((char)(c1 = CharacterSet.conv3ByteUTFtoUTF16(c, bytes[bytes_index++], bytes[bytes_index++])))) {
                        if (chars_index > charsLength - 2) {
                            count = bytes_end - bytes_index + 4;
                            break;
                        }
                        if (bytes_index >= bytes_end) continue block5;
                        c = bytes[bytes_index];
                        if ((byte)(c & 0xF0) != -32) {
                            chars[chars_index++] = 65533;
                            continue block5;
                        }
                        if (++bytes_index + 1 >= bytes_end) {
                            count = bytes_end - bytes_index + 1;
                            break;
                        }
                        chars[chars_index++] = CharacterSet.isLoSurrogate(c2 = CharacterSet.conv3ByteUTFtoUTF16(c, bytes[bytes_index++], bytes[bytes_index++])) ? c1 : 65533;
                        chars[chars_index++] = c2;
                        continue block5;
                    }
                    if (chars_index < charsLength) {
                        chars[chars_index++] = c1;
                        continue block5;
                    }
                    count = bytes_end - bytes_index + 4;
                    break;
                }
                default: {
                    if (chars_index < charsLength) {
                        chars[chars_index++] = 65533;
                        break;
                    }
                    count = bytes_end - bytes_index + 2;
                }
            }
        }
        return chars_index - chars_offset;
    }

    public static final char conv2ByteUTFtoUTF16(byte c, byte c2) {
        if (c < -62 || c > -33 || !CharacterSet.check80toBF(c2)) {
            return '\ufffd';
        }
        return (char)((c & 0x1F) << 6 | c2 & 0x3F);
    }

    public static final char conv3ByteUTFtoUTF16(byte c, byte c2, byte c3) {
        if (!(c == -32 && CharacterSet.checkA0toBF(c2) && CharacterSet.check80toBF(c3) || c >= -31 && c <= -17 && CharacterSet.check80toBF(c2) && CharacterSet.check80toBF(c3))) {
            return '\ufffd';
        }
        return (char)((c & 0xF) << 12 | (c2 & 0x3F) << 6 | c3 & 0x3F);
    }

    public static final boolean check80toBF(byte b) {
        return (b & 0xFFFFFFC0) == -128;
    }

    public static final boolean checkA0toBF(byte b) {
        return (b & 0xFFFFFFE0) == -96;
    }

    public static final boolean isLoSurrogate(char c) {
        return (char)(c & 0xFC00) == '\udc00';
    }

    public static final boolean isHiSurrogate(char c) {
        return (char)(c & 0xFC00) == '\ud800';
    }
}

