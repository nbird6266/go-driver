/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.core.Encoding;
import com.oscar.protocol.ProtocolVersion;
import com.oscar.util.OSQLException;
import com.oscar.util.VersionConfig;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public abstract class BasePacket {
    protected VersionConfig version = null;
    protected BaseConnection conn = null;
    protected ProtocolVersion protocolVersion = null;
    protected StringBuffer sb = new StringBuffer();
    protected boolean logFlag = Driver.getLogLevel() >= 4;

    public abstract void sendTo(BufferedOutputStream var1) throws IOException, SQLException;

    public abstract void receiveFrom(InputStream var1) throws IOException, SQLException;

    public abstract char getTag();

    public static void Receive(InputStream input, byte[] b, int off, int siz) throws SQLException {
        try {
            int w;
            for (int s = 0; s < siz; s += w) {
                w = input.read(b, off + s, siz - s);
                if (w >= 0) continue;
                throw new OSQLException("OSCAR-00104", "88888", 104);
            }
        }
        catch (IOException e) {
            throw new OSQLException("OSCAR-00103", "88888", 103, e);
        }
    }

    public static int ReceiveChar(InputStream input) throws SQLException {
        int c = 0;
        try {
            c = input.read();
            if (c < 0) {
                throw new OSQLException("OSCAR-00104", "88888", 104);
            }
        }
        catch (IOException e) {
            throw new OSQLException("OSCAR-00103", "88888", 103, e);
        }
        return c;
    }

    public static byte[] Receive(InputStream input, int siz) throws SQLException {
        byte[] answer = new byte[siz];
        BasePacket.Receive(input, answer, 0, siz);
        return answer;
    }

    public static int ReceiveIntegerR(InputStream input, int siz) throws SQLException {
        int n = 0;
        try {
            for (int i = 0; i < siz; ++i) {
                int b = input.read();
                if (b < 0) {
                    throw new OSQLException("OSCAR-00104", "88888", 104);
                }
                n = b | n << 8;
            }
        }
        catch (IOException e) {
            throw new OSQLException("OSCAR-00103", "88888", 103, e);
        }
        return n;
    }

    public static byte[] ReceiveStringByLen(InputStream input) throws SQLException {
        int len = BasePacket.ReceiveChar(input);
        return BasePacket.Receive(input, len);
    }

    public static byte[] ReceiveString(InputStream input) throws SQLException {
        int s = 0;
        byte[] rst = new byte[8192];
        try {
            int buflen = rst.length;
            boolean done = false;
            block2: while (!done) {
                while (s < buflen) {
                    int c = input.read();
                    if (c < 0) {
                        throw new OSQLException("OSCAR-00104", "88888", 104);
                    }
                    if (c == 0) {
                        rst[s] = 0;
                        done = true;
                        continue block2;
                    }
                    rst[s++] = (byte)c;
                    if (s < buflen) continue;
                    byte[] newrst = new byte[buflen *= 2];
                    System.arraycopy(rst, 0, newrst, 0, s);
                    rst = newrst;
                }
            }
            byte[] lastrst = new byte[s];
            System.arraycopy(rst, 0, lastrst, 0, s);
            rst = lastrst;
        }
        catch (IOException e) {
            throw new OSQLException("OSCAR-00103", "88888", 103, e);
        }
        return rst;
    }

    public static byte[] ReceiveStringByLen(InputStream input, int siz) throws SQLException {
        int s = 0;
        byte[] rst = new byte[siz];
        try {
            int buflen = rst.length;
            boolean done = false;
            block2: while (!done) {
                while (s < buflen) {
                    int c = input.read();
                    if (c < 0) {
                        throw new OSQLException("OSCAR-00104", "88888", 104);
                    }
                    if (c == 0) {
                        rst[s] = 0;
                        done = true;
                        continue block2;
                    }
                    rst[s++] = (byte)c;
                }
            }
            byte[] lastrst = new byte[s];
            System.arraycopy(rst, 0, lastrst, 0, s);
            rst = lastrst;
        }
        catch (IOException e) {
            throw new OSQLException("OSCAR-00103", "88888", 103, e);
        }
        return rst;
    }

    public static void Send(BufferedOutputStream output, byte[] buf, int off, int siz) throws IOException {
        output.write(buf, off, buf.length - off < siz ? buf.length - off : siz);
        if (buf.length - off < siz) {
            for (int i = buf.length - off; i < siz; ++i) {
                output.write(0);
            }
        }
    }

    public static void Send(BufferedOutputStream output, byte[] buf, int siz) throws IOException {
        BasePacket.Send(output, buf, 0, siz);
    }

    public static void Send(BufferedOutputStream output, byte[] buf) throws IOException {
        output.write(buf);
    }

    public static void SendChar(BufferedOutputStream output, int val) throws IOException {
        output.write((byte)val);
    }

    public static void SendInteger(BufferedOutputStream output, int val, int siz) throws IOException {
        byte[] buf = new byte[siz];
        while (siz-- > 0) {
            buf[siz] = (byte)(val & 0xFF);
            val >>= 8;
        }
        BasePacket.Send(output, buf);
    }

    public static void SendLong(BufferedOutputStream output, long val, int siz) throws IOException {
        byte[] buf = new byte[siz];
        while (siz-- > 0) {
            buf[siz] = (byte)(val & 0xFFL);
            val >>= 8;
        }
        BasePacket.Send(output, buf);
    }

    public static void SendString(BufferedOutputStream output, String val, int size, Encoding encoding) throws IOException, SQLException {
        byte[] strVal = encoding.encode(val);
        if (strVal.length >= size) {
            BasePacket.Send(output, strVal, size);
        } else {
            byte[] retValue = new byte[size];
            int i = 0;
            for (i = 0; i < strVal.length; ++i) {
                retValue[i] = strVal[i];
            }
            while (i < size) {
                retValue[i] = 0;
                ++i;
            }
            BasePacket.Send(output, retValue);
        }
    }

    public static void SendString(BufferedOutputStream output, String val, Encoding encoding) throws IOException, SQLException {
        byte[] outValue = encoding.encode(val);
        BasePacket.Send(output, outValue);
    }

    public static int bytesToIntR(byte[] val, int size) {
        int b = 0;
        int ret = 0;
        for (int i = 0; i < size; ++i) {
            b = val[i];
            if (b < 0) {
                b = 256 + b;
            }
            ret = b | ret << 8;
        }
        return ret;
    }

    public static long bytesToLong(byte[] val, int size) {
        byte b = 0;
        long ret = 0L;
        for (int i = 0; i < size; ++i) {
            b = val[i];
            ret = (long)(b & 0xFF) | ret << 8;
        }
        return ret;
    }

    public VersionConfig getVersion() {
        return this.version;
    }

    public ProtocolVersion getProtocolVersion() {
        return this.protocolVersion;
    }

    public void setConnection(BaseConnection conn) {
        this.conn = conn;
        this.version = conn.getVersion();
        this.protocolVersion = conn.getProtocolVersion();
    }

    protected void append(StringBuffer sb, byte[] value) {
        if (value == null) {
            sb.append("null");
        } else {
            for (int i = 0; i < value.length; ++i) {
                sb.append(value[i]).append(" ");
            }
        }
    }

    protected void append(StringBuffer sb, byte[] value, int off, int len) {
        if (value == null) {
            sb.append("null");
        } else {
            int maxLen = len > value.length ? value.length : len;
            for (int i = off; i < maxLen; ++i) {
                sb.append(value[i]).append(" ");
            }
        }
    }

    protected void append(StringBuffer sb, int[] value) {
        if (value == null) {
            sb.append("null");
        } else {
            for (int i = 0; i < value.length; ++i) {
                sb.append(value[i]).append(" ");
            }
        }
    }
}

