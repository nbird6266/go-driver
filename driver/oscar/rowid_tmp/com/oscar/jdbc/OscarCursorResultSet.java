/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseConnection;
import com.oscar.core.BaseStatement;
import com.oscar.util.OSQLException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public class OscarCursorResultSet
implements ResultSet {
    private String cursorName = null;
    private int fetchSize = 1000;
    private ResultSet resultset = null;
    private BaseConnection conn;
    private Statement stmt = null;
    private String fetchSql = null;
    private int current_row = 0;

    public OscarCursorResultSet(String cursorName, int fetchSize, BaseConnection conn) throws SQLException {
        this.cursorName = this.formatCursor(cursorName);
        this.conn = conn;
        this.fetchSize = fetchSize == 0 ? 1 : fetchSize;
        this.fetchSql = "fetch forward " + fetchSize + " in " + this.cursorName;
        this.stmt = conn.createStatement();
        this.stmt.execute(this.fetchSql);
        this.resultset = this.stmt.getResultSet();
        conn.addCursor(this.cursorName);
    }

    public String formatCursor(String cursorName) {
        if (!cursorName.startsWith("\"") || !cursorName.endsWith("\"")) {
            cursorName = "\"" + cursorName + "\"";
        }
        return cursorName;
    }

    public boolean absolute(int row) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void afterLast() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void beforeFirst() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void cancelRowUpdates() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void clearWarnings() throws SQLException {
        this.resultset.clearWarnings();
    }

    public void close() throws SQLException {
        this.resultset.close();
        this.conn.closeCursor(this.cursorName, (BaseStatement)((Object)this.stmt));
    }

    public void deleteRow() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public int findColumn(String columnName) throws SQLException {
        return this.resultset.findColumn(columnName);
    }

    public boolean first() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public Array getArray(int i) throws SQLException {
        this.checkResultSet();
        return this.resultset.getArray(i);
    }

    public Array getArray(String colName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getArray(colName);
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getAsciiStream(columnIndex);
    }

    public InputStream getAsciiStream(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getAsciiStream(columnName);
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getBigDecimal(columnIndex);
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getBigDecimal(columnName);
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        this.checkResultSet();
        return this.resultset.getBigDecimal(columnIndex, scale);
    }

    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        this.checkResultSet();
        return this.resultset.getBigDecimal(columnName, scale);
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getBinaryStream(columnIndex);
    }

    public InputStream getBinaryStream(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getBinaryStream(columnName);
    }

    public Blob getBlob(int i) throws SQLException {
        this.checkResultSet();
        return this.resultset.getBlob(i);
    }

    public Blob getBlob(String colName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getBlob(colName);
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getBoolean(columnIndex);
    }

    public boolean getBoolean(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getBoolean(columnName);
    }

    public byte getByte(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getByte(columnIndex);
    }

    public byte getByte(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getByte(columnName);
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getBytes(columnIndex);
    }

    public byte[] getBytes(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getBytes(columnName);
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getCharacterStream(columnIndex);
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getCharacterStream(columnName);
    }

    public Clob getClob(int i) throws SQLException {
        this.checkResultSet();
        return this.resultset.getClob(i);
    }

    public Clob getClob(String colName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getClob(colName);
    }

    public int getConcurrency() throws SQLException {
        return 1007;
    }

    public String getCursorName() throws SQLException {
        return this.cursorName;
    }

    public Date getDate(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getDate(columnIndex);
    }

    public Date getDate(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getDate(columnName);
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        this.checkResultSet();
        return this.resultset.getDate(columnIndex, cal);
    }

    public Date getDate(String columnName, Calendar cal) throws SQLException {
        this.checkResultSet();
        return this.resultset.getDate(columnName, cal);
    }

    public double getDouble(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getDouble(columnIndex);
    }

    public double getDouble(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getDouble(columnName);
    }

    public int getFetchDirection() throws SQLException {
        return 1000;
    }

    public int getFetchSize() throws SQLException {
        return this.fetchSize;
    }

    public float getFloat(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getFloat(columnIndex);
    }

    public float getFloat(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getFloat(columnName);
    }

    public int getInt(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getInt(columnIndex);
    }

    public int getInt(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getInt(columnName);
    }

    public long getLong(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getLong(columnIndex);
    }

    public long getLong(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getLong(columnName);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        this.checkResultSet();
        return this.resultset.getMetaData();
    }

    public Object getObject(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getObject(columnIndex);
    }

    public Object getObject(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getObject(columnName);
    }

    public Object getObject(int arg0, Map arg1) throws SQLException {
        this.checkResultSet();
        return this.resultset.getObject(arg0, arg1);
    }

    public Object getObject(String arg0, Map arg1) throws SQLException {
        this.checkResultSet();
        return this.resultset.getObject(arg0, arg1);
    }

    public Ref getRef(int i) throws SQLException {
        this.checkResultSet();
        return this.resultset.getRef(i);
    }

    public Ref getRef(String colName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getRef(colName);
    }

    public int getRow() throws SQLException {
        return this.current_row;
    }

    public short getShort(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getShort(columnIndex);
    }

    public short getShort(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getShort(columnName);
    }

    public Statement getStatement() throws SQLException {
        return this.stmt;
    }

    public String getString(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getString(columnIndex);
    }

    public String getString(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getString(columnName);
    }

    public Time getTime(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getTime(columnIndex);
    }

    public Time getTime(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getTime(columnName);
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        this.checkResultSet();
        return this.resultset.getTime(columnIndex, cal);
    }

    public Time getTime(String columnName, Calendar cal) throws SQLException {
        this.checkResultSet();
        return this.resultset.getTime(columnName, cal);
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getTimestamp(columnIndex);
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getTimestamp(columnName);
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        this.checkResultSet();
        return this.resultset.getTimestamp(columnIndex, cal);
    }

    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        this.checkResultSet();
        return this.resultset.getTimestamp(columnName, cal);
    }

    public int getType() throws SQLException {
        return 1003;
    }

    public URL getURL(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getURL(columnIndex);
    }

    public URL getURL(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getURL(columnName);
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        this.checkResultSet();
        return this.resultset.getUnicodeStream(columnIndex);
    }

    public InputStream getUnicodeStream(String columnName) throws SQLException {
        this.checkResultSet();
        return this.resultset.getUnicodeStream(columnName);
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    public void insertRow() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public boolean isAfterLast() throws SQLException {
        return this.current_row == -1;
    }

    public boolean isBeforeFirst() throws SQLException {
        return this.current_row == 0;
    }

    public boolean isFirst() throws SQLException {
        return this.current_row == 1;
    }

    public boolean isLast() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public boolean last() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void moveToCurrentRow() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void moveToInsertRow() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public boolean next() throws SQLException {
        if (this.resultset.next()) {
            ++this.current_row;
            return true;
        }
        this.stmt.execute(this.fetchSql);
        this.resultset = this.stmt.getResultSet();
        if (this.resultset.next()) {
            ++this.current_row;
            return true;
        }
        this.current_row = -1;
        return false;
    }

    public boolean previous() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void refreshRow() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public boolean relative(int rows) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public boolean rowDeleted() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public boolean rowInserted() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public boolean rowUpdated() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void setFetchDirection(int direction) throws SQLException {
        if (direction != 1000) {
            throw new OSQLException("OSCAR-00426", "88888", 426);
        }
    }

    public void setFetchSize(int rows) throws SQLException {
        if (rows < 0) {
            throw new OSQLException("OSCAR-00307", "88888", 307);
        }
        if (rows > 0) {
            this.fetchSize = rows;
        }
    }

    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateArray(String columnName, Array x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateBlob(String columnName, Blob x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateBoolean(String columnName, boolean x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateByte(String columnName, byte x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateBytes(String columnName, byte[] x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateClob(String columnName, Clob x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateDate(String columnName, Date x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateDouble(String columnName, double x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateFloat(String columnName, float x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateInt(String columnName, int x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateLong(String columnName, long x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateNull(int columnIndex) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateNull(String columnName) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateObject(String columnName, Object x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateRef(String columnName, Ref x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateRow() throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateShort(String columnName, short x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateString(int columnIndex, String x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateString(String columnName, String x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateTime(String columnName, Time x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
        throw new OSQLException("OSCAR-00426", "88888", 426);
    }

    public boolean wasNull() throws SQLException {
        return this.resultset.wasNull();
    }

    public void checkResultSet() throws SQLException {
        if (this.resultset == null) {
            throw new OSQLException("OSCAR-00427", "88888", 427);
        }
    }
}

