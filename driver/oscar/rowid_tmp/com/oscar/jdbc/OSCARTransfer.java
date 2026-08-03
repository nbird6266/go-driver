/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseConnection;
import com.oscar.jdbc.OscarBlob;
import com.oscar.jdbc.OscarClob;
import com.oscar.jdbc.OscarLob;
import com.oscar.jdbc.OscarResultSet;
import com.oscar.jdbc.OscarStatement;
import com.oscar.util.InputStreamHandle;
import com.oscar.util.StreamHandle;
import com.oscar.util.TableNameParser;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OSCARTransfer {
    private String[] columnTypes = null;
    private List rows = new LinkedList();
    private String table;
    private long tableOid;
    private Connection conn;
    private boolean closed;
    private StringBuffer buf = new StringBuffer(128);
    private Statement st;
    private static final int DEFAULT_LOB_BUF_SIZE = 633600;
    private int colIndex = -1;
    private ArrayList tempLobList = new ArrayList();

    public OSCARTransfer(Connection connection) {
        this.conn = connection;
    }

    public void setColumnType(String table, String[] types) throws SQLException {
        if (types == null) {
            throw new SQLException("oscar.tran.notype", "HY000", 1);
        }
        this.setTable(table);
        this.columnTypes = types;
    }

    public void setTable(String table) throws SQLException {
        this.table = table;
        String[] tableName = TableNameParser.parserToQueryString(table);
        String selectOidSql = "SELECT C.OID FROM V_SYS_CLASS C, V_SYS_NAMESPACE N  WHERE C.RELNAME=" + tableName[1] + " AND C.RELKIND='r' AND N.NSPNAME=" + tableName[0] + " AND C.RELNAMESPACE=N.OID";
        OscarResultSet rs = (OscarResultSet)((BaseConnection)((Object)this.conn)).execSQL(selectOidSql);
        if (!rs.next()) {
            throw new SQLException("\u8868" + table + "\u5728\u6570\u636e\u5e93\u4e2d\u4e0d\u5b58\u5728");
        }
        this.tableOid = rs.getLong(1);
    }

    public void addRow(Object[] values) throws SQLException {
        if (values == null) {
            throw new SQLException("oscar.tran.notype", "HY000", 1);
        }
        if (values.length != this.columnTypes.length) {
            throw new SQLException("oscar.tran.lengtherror", "HY000", 1);
        }
        for (int i = 0; i < values.length; ++i) {
            this.colIndex = i + 1;
            if (values[i] != null) {
                if (values[i] instanceof String) {
                    String v = ((String)values[i]).trim();
                    if (!v.equalsIgnoreCase("null")) {
                        String locator;
                        if (this.columnTypes[i].equalsIgnoreCase("blob")) {
                            String fileName = v.substring(1, v.length() - 1);
                            locator = this.createBlob(fileName);
                            values[i] = locator;
                        } else if (this.columnTypes[i].equalsIgnoreCase("clob")) {
                            String fileName = v.substring(1, v.length() - 1);
                            locator = this.createClob(fileName);
                            values[i] = locator;
                        }
                    } else {
                        values[i] = "";
                    }
                } else if (values[i] instanceof InputStream) {
                    block31: {
                        try {
                            String locator;
                            long length;
                            InputStream is = (InputStream)values[i];
                            if (this.columnTypes[i].equalsIgnoreCase("blob")) {
                                length = InputStreamHandle.readLong(is);
                                locator = this.createBlob(is, length);
                                values[i] = locator;
                                break block31;
                            }
                            if (this.columnTypes[i].equalsIgnoreCase("clob")) {
                                length = InputStreamHandle.readLong(is);
                                locator = this.createClob(is, length);
                                values[i] = locator;
                                break block31;
                            }
                            throw new SQLException("oscar.tran.notype", "HY000", 1);
                        }
                        catch (IOException ex) {
                            throw new SQLException("oscar.trans:" + ex.toString());
                        }
                    }
                    if (this.columnTypes[i].equalsIgnoreCase("clob")) {
                        // empty if block
                    }
                } else if (values[i] instanceof Reader) {
                    Reader reader = (Reader)values[i];
                    if (this.columnTypes[i].equalsIgnoreCase("clob")) {
                        String locator = this.createClob(reader, -1L);
                        values[i] = locator;
                    }
                } else if (values[i] instanceof StringBuffer) {
                    if (this.columnTypes[i].equalsIgnoreCase("clob")) {
                        String clobStr = ((StringBuffer)values[i]).toString();
                        values[i] = this.createClob(new StringReader(clobStr), (long)clobStr.length());
                    }
                } else if (values[i] instanceof byte[]) {
                    if (this.columnTypes[i].equalsIgnoreCase("blob")) {
                        values[i] = this.createBlob(new ByteArrayInputStream((byte[])values[i]), ((byte[])values[i]).length);
                    }
                } else if (values[i] instanceof Blob) {
                    Blob blob = (Blob)values[i];
                    values[i] = this.createBlob(blob.getBinaryStream(), blob.length());
                } else if (values[i] instanceof Clob) {
                    Clob clob = (Clob)values[i];
                    values[i] = this.createClob(clob.getCharacterStream(), clob.length());
                }
            } else {
                values[i] = "";
            }
            this.buf.append(values[i]);
            if (i >= values.length - 1) continue;
            this.buf.append(',');
        }
        this.buf.append('\n');
        this.rows.add(this.buf.toString());
        this.buf.setLength(0);
    }

    public void setRow(String[] values) throws SQLException {
        this.addRow(values);
    }

    private String createBlob(String fileName) throws SQLException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
        }
        catch (IOException e) {
            throw new SQLException("oscar.trans" + e.toString());
        }
        return this.createBlob(fis, -1L);
    }

    private String createBlob(InputStream is, long length) throws SQLException {
        BaseConnection oscarConn = (BaseConnection)((Object)this.conn);
        OscarBlob blob = null;
        blob = oscarConn.getVersion().isHaveEmptyXlobWithOid() ? OscarBlob.createForTable(this.conn, this.tableOid, this.colIndex) : oscarConn.createTempBlob(true, 0);
        this.tempLobList.add(blob);
        OutputStream os = blob.setBinaryStream(1L);
        try {
            StreamHandle.write(os, is, length, 633600);
        }
        catch (IOException e) {
            throw new SQLException("oscar.trans:" + e.toString());
        }
        return blob.getLocatorStr();
    }

    private String createClob(String fileName) throws SQLException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
        }
        catch (IOException e) {
            throw new SQLException("oscar.trans" + e.toString());
        }
        return this.createClob(fis, -1L);
    }

    private String createClob(InputStream is, long length) throws SQLException {
        BaseConnection oscarConn = (BaseConnection)((Object)this.conn);
        OscarClob clob = null;
        clob = oscarConn.getVersion().isHaveEmptyXlobWithOid() ? OscarClob.createForTable(this.conn, this.tableOid, this.colIndex) : oscarConn.createTempClob(true, 0);
        this.tempLobList.add(clob);
        OutputStream os = clob.setBinaryStream(1L);
        try {
            StreamHandle.write(os, is, length, 633600);
        }
        catch (IOException e) {
            throw new SQLException("oscar.trans:" + e.toString());
        }
        return clob.getLocatorStr();
    }

    private String createClob(Reader reader, long length) throws SQLException {
        BaseConnection oscarConn = (BaseConnection)((Object)this.conn);
        OscarClob clob = null;
        clob = oscarConn.getVersion().isHaveEmptyXlobWithOid() ? OscarClob.createForTable(this.conn, this.tableOid, this.colIndex) : oscarConn.createTempClob(true, 0);
        this.tempLobList.add(clob);
        try {
            StreamHandle.write(clob.setCharacterStream(1L), reader, length, 633600);
        }
        catch (IOException e) {
            throw new SQLException("oscar.trans" + e.toString());
        }
        return clob.getLocatorStr();
    }

    public int executeTransfer() throws SQLException {
        if (this.closed) {
            throw new SQLException("oscar.trans.closed");
        }
        if (this.st == null) {
            this.st = this.conn.createStatement();
        }
        ((OscarStatement)this.st).importValues(this.rows);
        this.st.execute("IMPORT " + this.table);
        this.rows = new LinkedList();
        for (int i = 0; i < this.tempLobList.size(); ++i) {
            OscarLob lob = (OscarLob)this.tempLobList.get(i);
            lob.close();
        }
        return ((OscarStatement)this.st).getTransferRowCount();
    }

    public void close() throws SQLException {
        if (this.closed) {
            return;
        }
        this.buf = null;
        this.conn = null;
        this.columnTypes = null;
        this.st = null;
        this.closed = true;
    }
}

