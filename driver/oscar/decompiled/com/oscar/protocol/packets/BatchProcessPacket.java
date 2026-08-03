/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.core.Encoding;
import com.oscar.jdbc.OscarStatement;
import com.oscar.protocol.packets.BasePacket;
import com.oscar.util.OSQLException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class BatchProcessPacket
extends BasePacket {
    private static final char tag = 'E';
    private static final char end_tag = 'N';
    private static final char batch_tag = 'B';
    private static final int dataLength = 4;
    private int defaultLen = 131072;
    private int bufLength = 0;
    private byte[] buf = null;
    private static int mutiple = 2;
    private byte[] singleBuf = new byte[1];
    private byte[] lengthBuf = new byte[4];
    private StringBuffer strBuf = new StringBuffer(0);
    private int position = 0;
    private List batch;
    private String prepare;
    private Encoding encoding;
    private int preparePosition = 0;
    OscarStatement.BatchRowData rowData = null;
    Object[] m_binds;
    int[] m_bindTypes;
    boolean hasEscapeChar = false;
    String s = "";
    Iterator it = null;

    public BatchProcessPacket(List batch, String prepare, Encoding currentEncoding, int defaultBufferSize) throws SQLException {
        this.init(batch, prepare, currentEncoding, defaultBufferSize);
    }

    public void init(List batch, String prepare, Encoding currentEncoding, int defaultBufferSize) throws SQLException {
        this.batch = batch;
        this.prepare = prepare;
        if (defaultBufferSize > 0) {
            this.defaultLen = defaultBufferSize * 1024;
        }
        this.encoding = currentEncoding;
        if (this.buf == null) {
            this.buf = new byte[this.defaultLen];
        }
        this.it = batch.iterator();
        this.position = 0;
        this.bufLength = this.buf.length;
        this.buf[0] = 69;
        this.position = 5;
        byte[] b = this.encoding.encode(prepare);
        this.writeInteger(b.length, 4);
        this.write(b);
        this.preparePosition = this.position;
    }

    public void reInit() throws SQLException {
        this.position = this.preparePosition;
    }

    public void write(byte[] bytes, int offset, int len) {
        if (this.position + len > this.bufLength) {
            int multiple = (this.position + len) / this.bufLength + 1;
            this.bufLength *= multiple;
            byte[] newBuf = new byte[this.bufLength];
            System.arraycopy(this.buf, 0, newBuf, 0, this.position);
            this.buf = newBuf;
        }
        System.arraycopy(bytes, offset, this.buf, this.position, len);
        this.position += len;
    }

    public void write(byte[] bytes) {
        this.write(bytes, 0, bytes.length);
    }

    public void writeNull() {
        this.writeInteger(0, 4);
    }

    public void write(Object o) throws SQLException {
        String value = o.toString();
        byte[] b = this.encoding.encode(value);
        this.writeInteger(b.length, 4);
        this.write(b);
    }

    public void writeString(String val, boolean hasEscapeChar) throws SQLException {
        int length = (val = val.substring(1, val.length() - 1)).length();
        if (length == 0) {
            this.write("\u0000");
        } else if (hasEscapeChar) {
            this.strBuf.setLength(0);
            char c = '\u0000';
            this.strBuf.ensureCapacity(length);
            for (int i = 0; i < length; ++i) {
                c = val.charAt(i);
                if (c == '\'') {
                    ++i;
                }
                this.strBuf.append(c);
            }
            this.write(this.strBuf.toString());
        } else {
            this.write(val);
        }
    }

    public void writeInteger(int val, int siz) {
        int count = siz;
        while (siz-- > 0) {
            this.lengthBuf[siz] = (byte)(val & 0xFF);
            val >>= 8;
        }
        this.write(this.lengthBuf, 0, count);
    }

    public void writeBatch() {
        this.singleBuf[0] = 66;
        this.write(this.singleBuf);
    }

    public void flush() throws SQLException {
        this.singleBuf[0] = 78;
        this.write(this.singleBuf);
        int tmp = this.position;
        this.position = 1;
        this.writeInteger(tmp - 5, 4);
        this.position = tmp;
    }

    public char getTag() {
        return 'E';
    }

    public int size() {
        if (this.batch == null) {
            return 0;
        }
        return this.batch.size();
    }

    public boolean checkBuffer() {
        return this.position + 1 >= this.defaultLen;
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public boolean writeRow() throws SQLException, IOException {
        if (this.it.hasNext()) {
            this.rowData = (OscarStatement.BatchRowData)this.it.next();
            this.m_binds = this.rowData.getData();
            this.m_bindTypes = this.rowData.getTypes();
            if (this.m_bindTypes.length != this.m_binds.length) {
                throw new OSQLException("OSCAR-00428", "88888", 428);
            }
            this.hasEscapeChar = this.rowData.hasEscapeChar();
            this.writeBatch();
            for (int i = 0; i < this.m_binds.length; ++i) {
                if (this.m_binds[i] == null) {
                    this.writeNull();
                    continue;
                }
                this.s = this.m_binds[i].toString();
                if (this.s.charAt(0) == 'n') {
                    this.writeNull();
                    continue;
                }
                if (this.s.charAt(0) == '\'') {
                    this.writeString(this.s, this.hasEscapeChar);
                    continue;
                }
                if (this.m_bindTypes[i] == -2 || this.m_bindTypes[i] == -1 || this.m_bindTypes[i] == -3) {
                    this.writeInteger(this.m_bindTypes[i], 4);
                }
                this.write(this.m_binds[i]);
            }
        } else {
            return false;
        }
        return true;
    }

    public void sendBatch(BufferedOutputStream stream) throws SQLException, IOException {
        this.flush();
        stream.write(this.buf, 0, this.position);
        stream.flush();
        if (this.buf.length / this.defaultLen > 2) {
            byte[] tmpBytes = new byte[this.preparePosition];
            System.arraycopy(this.buf, 0, tmpBytes, 0, this.preparePosition);
            if (this.preparePosition > this.defaultLen) {
                int multiple = this.preparePosition / this.defaultLen + 1;
                this.bufLength = this.defaultLen * multiple;
                this.buf = new byte[this.bufLength];
            } else {
                this.buf = new byte[this.defaultLen];
                this.bufLength = this.defaultLen;
            }
            System.arraycopy(tmpBytes, 0, this.buf, 0, this.preparePosition);
        }
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        OscarStatement.BatchRowData rowData2 = null;
        boolean hasEscapeChar = false;
        String s = "";
        for (OscarStatement.BatchRowData rowData2 : this.batch) {
            Object[] m_binds = rowData2.getData();
            int[] m_bindTypes = rowData2.getTypes();
            if (m_bindTypes.length != m_binds.length) {
                throw new OSQLException("OSCAR-00428", "88888", 428);
            }
            hasEscapeChar = rowData2.hasEscapeChar();
            this.writeBatch();
            for (int i = 0; i < m_binds.length; ++i) {
                if (m_binds[i] == null) {
                    this.writeNull();
                    continue;
                }
                s = m_binds[i].toString();
                if (s.charAt(0) == 'n') {
                    this.writeNull();
                    continue;
                }
                if (s.charAt(0) == '\'') {
                    this.writeString(s, hasEscapeChar);
                    continue;
                }
                if (m_bindTypes[i] == -2 || m_bindTypes[i] == -1 || m_bindTypes[i] == -3) {
                    this.writeInteger(m_bindTypes[i], 4);
                }
                this.write(m_binds[i]);
            }
        }
        this.flush();
        stream.write(this.buf, 0, this.position);
        stream.flush();
        if (this.buf != null && this.buf.length > this.defaultLen) {
            this.buf = new byte[this.defaultLen];
        }
    }
}

