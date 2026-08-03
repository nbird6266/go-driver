/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.core.Encoding;
import com.oscar.jdbc.OscarStatement;
import com.oscar.protocol.packets.BasePacket;
import com.oscar.util.OSQLException;
import com.oscar.util.converter.BindConverter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class BatchProcessPacketV2
extends BasePacket {
    private static final char prepareBatchTag = '\n';
    private static final char batchTag = '\f';
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
    private int marked = 0;
    OscarStatement.BatchRowData rowData = null;
    Object[] m_binds;
    int[] m_bindTypes;
    boolean hasEscapeChar = false;
    String s = "";
    Iterator it = null;
    private byte[] prepareSql;
    private int beginPosition;
    private boolean bindTypeChanged = false;
    private int protocolType;

    public BatchProcessPacketV2(List batch, String prepare, Encoding currentEncoding, int defaultBufferSize, int[] m_bindTypes, String m_prepareSqlStatement, int marked, boolean bindTypeChanged) throws SQLException {
        this.init(batch, prepare, currentEncoding, defaultBufferSize, m_bindTypes, m_prepareSqlStatement, marked, bindTypeChanged);
    }

    public BatchProcessPacketV2(List batch, String prepare, Encoding currentEncoding, int defaultBufferSize, int[] m_bindTypes, String m_prepareSqlStatement, int marked, boolean bindTypeChanged, BaseConnection conn) throws SQLException {
        this.conn = conn;
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(conn.getSessionID()).append(", BatchProcessPacketV2, params: ").append("\n");
            this.sb.append(" prepareSQL: ").append(prepare).append(", defaultBufferSize: ").append(defaultBufferSize).append("M, bindTypes: ");
            this.append(this.sb, m_bindTypes);
            this.sb.append(", prepareName: ").append(m_prepareSqlStatement).append(", queryNum: ").append(marked).append(", bindTypeChanged: ").append(bindTypeChanged);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
        this.init(batch, prepare, currentEncoding, defaultBufferSize, m_bindTypes, m_prepareSqlStatement, marked, bindTypeChanged);
    }

    public void init(List batch, String prepare, Encoding currentEncoding, int defaultBufferSize, int[] m_bindTypes, String m_prepareSqlStatement, int marked, boolean bindTypeChanged) throws SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", batch init: ").append("\n");
        }
        this.batch = batch;
        this.marked = marked;
        this.prepare = prepare;
        this.bindTypeChanged = bindTypeChanged;
        if (defaultBufferSize > 0) {
            this.defaultLen = defaultBufferSize * 1024;
        }
        this.encoding = currentEncoding;
        this.m_bindTypes = m_bindTypes;
        this.prepareSql = (byte[])(m_prepareSqlStatement != null ? this.encoding.encode(m_prepareSqlStatement) : null);
        if (this.buf == null) {
            this.buf = new byte[this.defaultLen];
        }
        this.bufLength = this.buf.length;
        this.it = batch.iterator();
        this.position = 0;
        if (this.prepareSql != null) {
            if (this.logFlag) {
                this.sb.append("prepareBatch: 0x0A");
                this.sb.append(", queryNum: ").append(marked);
                this.sb.append(", prepareSql(").append(this.prepareSql.length + 1).append(" bytes): ");
                this.append(this.sb, this.prepareSql);
                this.sb.append(0);
            }
            this.buf[0] = 10;
            ++this.position;
            this.writeInteger(marked, 2);
            this.beginPosition = this.position;
            this.position += 4;
            this.write(this.prepareSql);
            this.writeInteger(0, 1);
        } else {
            if (this.logFlag) {
                this.sb.append("batch: 0x0C");
            }
            this.buf[this.position] = 12;
            this.beginPosition = this.position + 1;
            this.position += 5;
        }
        this.protocolType = this.conn.getProtocolVersion().getProtocolType();
        if (m_bindTypes != null && (this.prepareSql != null || bindTypeChanged)) {
            if (this.protocolType >= 4) {
                this.writeInteger(m_bindTypes.length, 2);
            } else {
                this.writeInteger(m_bindTypes.length, 1);
            }
            if (this.logFlag) {
                this.sb.append(", bindType Len: ").append(m_bindTypes.length).append(", bindType: ");
            }
            for (int i = 0; i < m_bindTypes.length; ++i) {
                this.writeInteger(m_bindTypes[i], 1);
                if (!this.logFlag) continue;
                this.sb.append(m_bindTypes[i]).append(" ");
            }
        } else {
            if (this.protocolType >= 4) {
                this.writeInteger(0, 2);
            } else {
                this.writeInteger(0, 1);
            }
            if (this.logFlag) {
                this.sb.append(", bindType Len: 0");
            }
        }
        byte[] b = this.encoding.encode(prepare);
        this.writeInteger(b.length, 1);
        this.write(b);
        this.preparePosition = this.position;
        if (this.logFlag) {
            this.sb.append(", prepareName Len: ").append(b.length).append(", prepareName: ");
            this.append(this.sb, b);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public void reInit() throws SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", batch reinit: ").append("\n");
        }
        if (this.hasPrepareSQL()) {
            this.setPrepareSQL(null);
            this.bindTypeChanged = false;
            this.bufLength = this.buf.length;
            this.position = 0;
            if (this.prepareSql != null) {
                if (this.logFlag) {
                    this.sb.append("prepareBatch: 0x0A");
                    this.sb.append(", queryNum: ").append(this.marked);
                    this.sb.append(", prepareSql(").append(this.prepareSql.length + 1).append(" bytes): ");
                    this.append(this.sb, this.prepareSql);
                    this.sb.append(0);
                }
                this.buf[0] = 10;
                ++this.position;
                this.writeInteger(this.marked, 2);
                this.beginPosition = this.position;
                this.position += 4;
                this.write(this.prepareSql);
                this.writeInteger(0, 1);
            } else {
                if (this.logFlag) {
                    this.sb.append("batch: 0x0C");
                }
                this.buf[this.position] = 12;
                this.beginPosition = this.position + 1;
                this.position += 5;
            }
            if (this.m_bindTypes != null && (this.prepareSql != null || this.bindTypeChanged)) {
                if (this.protocolType >= 4) {
                    this.writeInteger(this.m_bindTypes.length, 2);
                } else {
                    this.writeInteger(this.m_bindTypes.length, 1);
                }
                if (this.logFlag) {
                    this.sb.append(", bindType Len: ").append(this.m_bindTypes.length).append(", bindType: ");
                }
                for (int i = 0; i < this.m_bindTypes.length; ++i) {
                    this.writeInteger(this.m_bindTypes[i], 1);
                    if (!this.logFlag) continue;
                    this.sb.append(this.m_bindTypes[i]).append(" ");
                }
            } else {
                if (this.protocolType >= 4) {
                    this.writeInteger(0, 2);
                } else {
                    this.writeInteger(0, 1);
                }
                if (this.logFlag) {
                    this.sb.append(", bindType Len: 0");
                }
            }
            byte[] b = this.encoding.encode(this.prepare);
            this.writeInteger(b.length, 1);
            this.write(b);
            this.preparePosition = this.position;
            if (this.logFlag) {
                this.sb.append(", prepareName Len: ").append(b.length).append(", prepareName: ");
                this.append(this.sb, b);
                this.sb.append("\n").append("***********************************************************");
                Driver.writeLog(this.sb.toString());
            }
        } else {
            this.position = this.preparePosition;
        }
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

    public void write(byte value) {
        if (this.position + 1 > this.bufLength) {
            int multiple = (this.position + 1) / this.bufLength + 1;
            this.bufLength *= multiple;
            byte[] newBuf = new byte[this.bufLength];
            System.arraycopy(this.buf, 0, newBuf, 0, this.position);
            this.buf = newBuf;
        }
        this.buf[this.position] = value;
        ++this.position;
    }

    public void write(byte[] bytes) {
        this.write(bytes, 0, bytes.length);
    }

    public void writeNull() {
        this.writeInteger(0, 1);
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
        this.position = this.beginPosition;
        this.writeInteger(tmp - this.beginPosition - 4, 4);
        this.position = tmp;
    }

    public char getTag() {
        return '\n';
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
            BindConverter.convertBindDatas(this.m_binds, this.m_bindTypes, this.encoding);
            if (this.m_bindTypes.length != this.m_binds.length) {
                throw new OSQLException("OSCAR-00428", "88888", 428);
            }
            this.hasEscapeChar = this.rowData.hasEscapeChar();
            this.writeBatch();
            int len = 0;
            byte[] tmp = null;
            for (int i = 0; i < this.m_binds.length; ++i) {
                if (this.m_binds[i] == null) {
                    this.writeNull();
                    continue;
                }
                tmp = (byte[])this.m_binds[i];
                len = tmp.length;
                if (len == 0) {
                    tmp = "\u0000".getBytes();
                    len = tmp.length;
                }
                if (this.m_bindTypes[i] == 24 && len > 240) {
                    this.write(tmp);
                    continue;
                }
                if (this.m_bindTypes[i] != 24 && tmp[0] == -3) {
                    this.write(tmp);
                    continue;
                }
                if (this.m_bindTypes[i] == 50) {
                    this.writeInteger(251, 1);
                    this.writeInteger(tmp.length, 4);
                    this.write(tmp);
                    continue;
                }
                if (this.m_bindTypes[i] == 51) {
                    this.writeInteger(250, 1);
                    this.writeInteger(tmp.length, 4);
                    this.write(tmp);
                    continue;
                }
                if (this.m_bindTypes[i] == 52) {
                    this.writeInteger(252, 1);
                    this.writeInteger(tmp.length, 4);
                    this.write(tmp);
                    continue;
                }
                this.writeInteger(len, 1);
                this.write(tmp);
            }
        } else {
            return false;
        }
        return true;
    }

    public void writeRow(int row) throws SQLException, IOException {
        this.rowData = (OscarStatement.BatchRowData)this.batch.get(row);
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
            if (this.m_bindTypes[i] == 251 || this.m_bindTypes[i] == 250 || this.m_bindTypes[i] == 252) {
                this.writeInteger(this.m_bindTypes[i], 4);
            }
            this.write(this.m_binds[i]);
        }
    }

    public void sendBatch(BufferedOutputStream stream) throws SQLException, IOException {
        this.flush();
        stream.write(this.buf, 0, this.position);
        stream.flush();
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", batch send(totalLen: ").append(this.position).append("): \n");
            this.append(this.sb, this.buf, 0, this.position);
            Driver.writeLog(this.sb.toString());
        }
        if (this.buf.length / this.defaultLen > 2) {
            byte[] tmpBytes = new byte[this.preparePosition];
            System.arraycopy(this.buf, 0, tmpBytes, 0, this.preparePosition);
            this.buf = new byte[this.defaultLen];
            if (this.preparePosition > this.defaultLen) {
                int multiple = this.preparePosition / this.defaultLen + 1;
                this.bufLength = this.defaultLen * multiple;
                this.buf = new byte[this.bufLength];
            } else {
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
            BindConverter.convertBindDatas(m_binds, m_bindTypes, this.encoding);
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
                if (m_bindTypes[i] == 251 || m_bindTypes[i] == 250 || m_bindTypes[i] == 252) {
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

    public boolean hasPrepareSQL() {
        return this.prepareSql != null;
    }

    public void setPrepareSQL(byte[] prepareSql) {
        this.prepareSql = prepareSql;
    }
}

