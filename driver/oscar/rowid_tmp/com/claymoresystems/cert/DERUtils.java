/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class DERUtils {
    public static final byte BOOLEAN = 1;
    public static final byte INTEGER = 2;
    public static final byte BIT_STRING = 3;
    public static final byte OCTET_STRING = 4;
    public static final byte OID = 6;
    public static final byte SET = 49;
    public static final byte SEQUENCE = 48;
    public static final byte IA5STRING = 22;
    public static final byte PRINTABLE_STRING = 19;
    public static final byte T61STRING = 20;
    public static final byte UTCTIME = 23;
    private static final int[] legals = new int[]{32, 39, 40, 41, 43, 44, 45, 46, 47, 58, 61, 63};

    private static void writeLength(int length, OutputStream os) throws IOException {
        if (length < 128) {
            os.write((byte)length);
        } else {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            int i = 1;
            while (length > 0) {
                bs.write((byte)(length & 0xFF));
                length >>= 8;
                ++i;
            }
            byte[] b = bs.toByteArray();
            os.write((byte)(0x80 | b.length));
            for (i = 0; i < b.length; ++i) {
                os.write(b[b.length - (i + 1)]);
            }
        }
    }

    private static void encodeBytes(byte type, byte[] in, OutputStream os) throws IOException {
        os.write(type);
        DERUtils.writeLength(in.length, os);
        os.write(in);
    }

    public static void encodeInteger(BigInteger i, OutputStream os) throws IOException {
        byte[] i_bytes = i.toByteArray();
        if (i_bytes[0] == 0 && i_bytes.length > 1 && (i_bytes[1] & 0x80) == 0) {
            byte[] n_bytes = new byte[i_bytes.length - 1];
            System.arraycopy(i_bytes, 1, n_bytes, 0, n_bytes.length);
            i_bytes = n_bytes;
        }
        DERUtils.encodeBytes((byte)2, i_bytes, os);
    }

    public static void encodeSequence(byte[] in, OutputStream os) throws IOException {
        DERUtils.encodeBytes((byte)48, in, os);
    }

    public static void encodeSequence(ByteArrayOutputStream is, OutputStream os) throws IOException {
        byte[] in = is.toByteArray();
        DERUtils.encodeSequence(in, os);
    }

    public static void encodeSet(byte[] in, OutputStream os) throws IOException {
        DERUtils.encodeBytes((byte)49, in, os);
    }

    public static void encodeSet(ByteArrayOutputStream is, OutputStream os) throws IOException {
        byte[] in = is.toByteArray();
        DERUtils.encodeSet(in, os);
    }

    public static void encodeOID(byte[] in, OutputStream os) throws IOException {
        DERUtils.encodeBytes((byte)6, in, os);
    }

    private static void encodeBase128(int num, OutputStream os) throws IOException {
        byte[] tmp = new byte[6];
        for (int i = 0; i < 6; ++i) {
            tmp[i] = (byte)(num & 0x7F);
            if ((num >>= 7) == 0) break;
        }
        for (int j = i; j >= 0; --j) {
            if (j == 0) {
                os.write(tmp[j]);
                continue;
            }
            os.write(tmp[j] | 0x80);
        }
    }

    public static void encodeOID(String oid, OutputStream os) throws IOException {
        int i;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StringTokenizer tok = new StringTokenizer(oid, ".");
        int[] components = new int[tok.countTokens()];
        for (i = 0; i < components.length; ++i) {
            components[i] = Integer.parseInt(tok.nextToken());
        }
        bos.write(components[0] * 40 + components[1]);
        for (i = 2; i < components.length; ++i) {
            DERUtils.encodeBase128(components[i], bos);
        }
        byte[] tmp = bos.toByteArray();
        DERUtils.encodeOID(tmp, os);
    }

    public static void encodeIA5String(String in, OutputStream os) throws IOException {
        DERUtils.encodeBytes((byte)22, in.getBytes(), os);
    }

    public static void encodePrintableString(String in, OutputStream os) throws IOException {
        DERUtils.encodeBytes((byte)19, in.getBytes(), os);
    }

    private static boolean isPrintableString(String str) {
        byte[] in = str.getBytes();
        for (int i = 0; i < in.length; ++i) {
            if (in[i] >= 48 && in[i] <= 57 || in[i] >= 65 && in[i] <= 90 || in[i] >= 97 && in[i] <= 122) continue;
            for (int j = 0; j < legals.length; ++j) {
                if (in[i] != legals[j]) continue;
            }
            return false;
        }
        return true;
    }

    public static void encodeUnknownString(String in, OutputStream os) throws IOException {
        if (DERUtils.isPrintableString(in)) {
            DERUtils.encodePrintableString(in, os);
        } else {
            DERUtils.encodeIA5String(in, os);
        }
    }

    public static void encodeBitString(byte[] in, OutputStream os) throws IOException {
        os.write(3);
        DERUtils.writeLength(in.length + 1, os);
        os.write(0);
        os.write(in);
    }

    private static void put2digits(byte[] buf, int val, int offset) {
        if (val > 99) {
            throw new InternalError("Illegal value");
        }
        if (val < 0) {
            throw new InternalError("Illegal value");
        }
        buf[offset++] = (byte)(val / 10 + 48 & 0xFF);
        buf[offset++] = (byte)(val % 10 + 48 & 0xFF);
    }

    public static void encodeUTCTime(long time, OutputStream os) throws IOException {
        byte[] tbuf = new byte[13];
        int offset = 0;
        Date d = new Date(time);
        TimeZone tz = TimeZone.getTimeZone("GMT");
        GregorianCalendar cal = new GregorianCalendar(tz);
        cal.setTime(d);
        int year = cal.get(1);
        int month = cal.get(2);
        ++month;
        int day = cal.get(5);
        int hr = cal.get(11);
        int min = cal.get(12);
        int sec = cal.get(13);
        if (year < 1950) {
            throw new InternalError("Bad time to encode " + cal.toString());
        }
        if (year > 2049) {
            throw new InternalError("Bad time to encode " + cal.toString());
        }
        year = year < 2000 ? (year -= 1900) : (year -= 2000);
        DERUtils.put2digits(tbuf, year, offset);
        DERUtils.put2digits(tbuf, month, offset += 2);
        DERUtils.put2digits(tbuf, day, offset += 2);
        DERUtils.put2digits(tbuf, hr, offset += 2);
        DERUtils.put2digits(tbuf, min, offset += 2);
        DERUtils.put2digits(tbuf, sec, offset += 2);
        tbuf[offset += 2] = 90;
        DERUtils.encodeBytes((byte)23, tbuf, os);
    }

    public static byte[] decodeSequence(InputStream is) throws IOException {
        return DERUtils.readTLV(48, is);
    }

    public static byte[] decodeOID(InputStream is) throws IOException {
        return DERUtils.readTLV(6, is);
    }

    public static byte[] decodeOctetString(InputStream is) throws IOException {
        return DERUtils.readTLV(4, is);
    }

    public static BigInteger decodeInteger(InputStream is) throws IOException {
        byte[] intb = DERUtils.readTLV(2, is);
        return new BigInteger(1, intb);
    }

    public static int decodeIntegerX(InputStream is) throws IOException {
        BigInteger max = BigInteger.valueOf(0xFFFFFFFL);
        BigInteger b = DERUtils.decodeInteger(is);
        if (b.compareTo(max) > 0) {
            throw new IOException("Overlarge big integer");
        }
        if (b.compareTo(BigInteger.ZERO) < 0) {
            throw new IOException("Tried to decode negative number");
        }
        return b.intValue();
    }

    public static BitSet decodeBitStringX(InputStream is) throws IOException {
        byte[] b = DERUtils.decodeBitString(is);
        byte shrt = b[0];
        BitSet ret = new BitSet();
        for (int i = 1; i < b.length; ++i) {
            int max = i != b.length - 1 ? 8 : 8 - shrt;
            int msk = 128;
            for (int j = 0; j < max; ++j) {
                int tmp = b[i] & 0xFF;
                if ((msk & tmp) == msk) {
                    ret.set((i - 1) * 8 + j);
                }
                msk >>= 1;
            }
        }
        return ret;
    }

    public static byte[] decodeBitString(InputStream is) throws IOException {
        return DERUtils.readTLV(3, is);
    }

    public static boolean decodeBoolean(InputStream is) throws IOException {
        byte[] v = DERUtils.readTLV(1, is);
        if (v.length != 1) {
            throw new IOException("Bad encoding for boolean");
        }
        switch (v[0]) {
            case -1: {
                return true;
            }
            case 0: {
                return false;
            }
        }
        throw new IOException("Boolean must be either 0xff or 0");
    }

    public static byte[] decodeAny(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int t = DERUtils.decodeTag(is);
        byte[] b = DERUtils.readLV(is);
        DERUtils.encodeBytes((byte)t, b, os);
        return os.toByteArray();
    }

    private static byte[] readTLV(int type, InputStream is) throws IOException {
        DERUtils.decodeTagOrDie(type, is);
        byte[] ret = DERUtils.readLV(is);
        return ret;
    }

    private static byte[] readLV(InputStream is) throws IOException {
        int actualLength;
        int length = DERUtils.decodeLength(is);
        byte[] ret = new byte[length];
        if (length != 0 && (actualLength = is.read(ret)) != length) {
            throw new IOException("Bad encoding: short read");
        }
        return ret;
    }

    private static void decodeTagOrDie(int tag, InputStream is) throws IOException {
        if (!DERUtils.decodeTag(tag, is)) {
            throw new IOException("Bad encoding: wrong tag");
        }
    }

    private static int decodeTag(InputStream is) throws IOException {
        int t = is.read();
        return t;
    }

    private static boolean decodeTag(int tag, InputStream is) throws IOException {
        int t = DERUtils.decodeTag(is);
        if (t < 0) {
            return false;
        }
        return t == tag;
    }

    public static boolean isTag(int tag, InputStream is) throws IOException {
        is.mark(1);
        boolean result = DERUtils.decodeTag(tag, is);
        is.reset();
        return result;
    }

    private static int decodeLength(InputStream is) throws IOException {
        int length = 0;
        int l = is.read();
        if (l < 0) {
            throw new IOException("Bad encoding: short read");
        }
        if ((l & 0x80) == 0) {
            return l;
        }
        int ct = l & 0x7F;
        while (ct-- > 0) {
            l = is.read();
            if (l < 0) {
                throw new IOException("Bad encoding: short read");
            }
            length *= 256;
            length += l;
        }
        return length;
    }
}

