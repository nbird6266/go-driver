/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.core.BaseResultSet;
import com.oscar.core.BaseStatement;
import com.oscar.core.Encoding;
import com.oscar.core.Field;
import com.oscar.gis.OscarGisStruct;
import com.oscar.jdbc.Array;
import com.oscar.jdbc.OscarBfile;
import com.oscar.jdbc.OscarBlob;
import com.oscar.jdbc.OscarClob;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.jdbc.OscarResultSetMetaData;
import com.oscar.jdbc.OscarStatement;
import com.oscar.jdbc.OscarStringClob;
import com.oscar.util.OSCARbyte;
import com.oscar.util.OSQLException;
import com.oscar.util.TypeConverter;
import com.oscar.util.converter.BooleanConverter;
import com.oscar.util.converter.DateConverter;
import com.oscar.util.converter.NumberConverter;
import com.oscar.util.converter.TimeConverter;
import com.oscar.util.converter.TimestampConverter;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class OscarResultSet
implements BaseResultSet {
    protected Encoding encoding;
    protected Encoding clientEncoding;
    protected List rows;
    protected BaseStatement statement;
    protected Field[] fields;
    protected String status;
    protected int updateCount;
    protected long insertRowid;
    protected int current_row = -1;
    protected byte[][] this_row;
    protected int lastColumn;
    protected boolean resultTid;
    protected List tidList;
    protected Field tidField;
    protected BaseConnection connection;
    protected SQLWarning warnings;
    protected BaseResultSet next = null;
    protected BaseResultSet previous = null;
    protected BaseResultSet lastResult = null;
    protected StringBuffer sbuf = null;
    public byte[][] rowBuffer = null;
    protected int type = 1003;
    protected int concurrency = 1007;
    protected boolean canUpdateable = true;
    protected int statementType = 0;
    protected int fetchSize = 0;
    protected int fetchDirection = 1000;
    protected int maxRows = 0;
    protected boolean closed = false;
    protected boolean cursorUsed = false;
    protected int moveSize = 0;
    protected String cursorName = null;
    protected int cursorPosition = 0;
    protected Hashtable updateValues = new Hashtable();
    protected boolean nullResult;
    protected boolean doingUpdates = false;
    protected boolean onInsertRow = false;
    protected String tableName = null;
    protected String schemaName = null;
    protected PreparedStatement updateStatement = null;
    protected PreparedStatement insertStatement = null;
    protected Statement deleteStatement = null;
    protected Statement selectStatement = null;
    protected static final BigInteger INTMAX = new BigInteger(Integer.toString(Integer.MAX_VALUE));
    protected static final BigInteger INTMIN = new BigInteger(Integer.toString(Integer.MIN_VALUE));
    protected HashMap columnNameIndexMap;
    protected byte[] planID;
    protected boolean encodingFlag = false;
    protected static boolean logFlag = Driver.getLogLevel() >= 2;
    protected int datakind = 0;
    protected boolean isRowDeleted = false;
    protected boolean isRowinserted = false;
    protected boolean isRowupdated = false;
    protected boolean isOldProtocolResult = false;

    public OscarResultSet() {
    }

    public OscarResultSet(BaseStatement statement, Field[] fields, List tuples, String status, int updateCount, long insertTid) {
        this.connection = statement.getDBConnection();
        this.statement = statement;
        try {
            this.type = statement.getResultSetType();
            this.concurrency = statement.getResultSetConcurrency();
            this.canUpdateable = statement.getResultSetCanUpdateable();
            this.fetchSize = statement.getFetchSize();
            this.fetchDirection = statement.getFetchDirection();
            this.encoding = this.connection.getEncoding();
            this.clientEncoding = this.connection.getClientEncoding();
            if (this.encoding == null) {
                this.encodingFlag = true;
                this.encoding = this.clientEncoding;
            } else {
                this.encodingFlag = this.encoding.equals(this.clientEncoding);
            }
        }
        catch (SQLException se) {
            // empty catch block
        }
        this.fields = fields;
        this.rows = tuples;
        this.status = status;
        this.updateCount = updateCount;
        this.insertRowid = insertTid;
        this.cursorName = statement.getCursorName();
        if (this.fetchSize > this.rows.size()) {
            this.cursorPosition = 1;
        }
        if (this.rows.size() == 0) {
            this.nullResult = true;
        }
    }

    public OscarResultSet(BaseStatement statement, Field[] fields, List tuples, String status, int updateCount, long insertOID, int maxRows) {
        this(statement, fields, tuples, status, updateCount, insertOID);
        this.maxRows = maxRows;
        if (fields != null && fields.length > 0) {
            this.tableName = fields[0].getTableName();
            this.schemaName = fields[0].getSchemaName();
        }
    }

    public OscarResultSet(BaseStatement statement, Field[] fields, List tuples, String status, int updateCount, long insertOID, int fetchSize, int maxRows) {
        this(statement, fields, tuples, status, updateCount, insertOID, maxRows);
    }

    public OscarResultSet(BaseStatement statement, Field[] fields, List tuples, String status, int updateCount, long insertOID, int fetchSize, int maxRows, int dataKind) {
        this(statement, fields, tuples, status, updateCount, insertOID, maxRows);
        this.datakind = dataKind;
        this.fields = new Field[3];
        this.cursorPosition = this.rows != null && this.rows.size() == 0 ? 1 : 0;
    }

    public void reInit(Field[] fields, List tuples, String status, int updateCount, long insertOID) {
        this.fields = fields;
        this.rows = tuples;
        this.status = status;
        this.updateCount = updateCount;
        this.insertRowid = insertOID;
        this.this_row = null;
        this.current_row = -1;
        this.columnNameIndexMap = null;
    }

    public Field[] getFields() {
        return this.fields;
    }

    public List getTuples() {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", getTuples(), rows: " + this.rows.size());
        }
        return this.rows;
    }

    public byte[][] getCurrentRow() {
        return this.this_row;
    }

    public synchronized boolean next() throws SQLException {
        if (logFlag && this.connection != null) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", next()");
        }
        if (!this.checkResultClosed()) {
            return false;
        }
        this.updateValues.clear();
        if (!this.cursorUsed && this.current_row == this.rows.size()) {
            this.this_row = null;
            return false;
        }
        if (this.cursorPosition == 1 && this.current_row == this.rows.size()) {
            this.this_row = null;
            return false;
        }
        if (++this.current_row >= this.rows.size()) {
            if (!this.cursorUsed) {
                this.this_row = null;
                return false;
            }
            if (this.cursorPosition == 1) {
                this.this_row = null;
                return false;
            }
            String sql = "FETCH FORWARD " + this.fetchSize + " IN " + this.cursorName;
            this.connection.execSQL(sql, this.statement, this);
            this.cursorPosition = this.moveSize < this.fetchSize ? 1 : 0;
            this.current_row = 0;
            if (this.rows.size() == 0) {
                this.this_row = null;
                this.rowBuffer = null;
                return false;
            }
        }
        this.this_row = (byte[][])this.rows.get(this.current_row);
        this.rowBuffer = new byte[this.this_row.length][];
        System.arraycopy(this.this_row, 0, this.rowBuffer, 0, this.this_row.length);
        return true;
    }

    public void close() throws SQLException {
        if (this.closed) {
            return;
        }
        if (this.cursorUsed) {
            try {
                this.connection.closeCursor(this.cursorName, this.statement);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.rows = null;
        this.encoding = null;
        this.statement = null;
        this.fields = null;
        this.status = null;
        this.this_row = null;
        this.tidList = null;
        this.tidField = null;
        this.warnings = null;
        this.next = null;
        this.sbuf = null;
        this.rowBuffer = null;
        this.updateValues = null;
        this.connection = null;
        this.cursorName = null;
        this.closed = true;
        this.deleteStatement = null;
        this.selectStatement = null;
        this.columnNameIndexMap = null;
    }

    public void setTidValues(Field field, List tids) {
        this.resultTid = true;
        this.tidField = field;
        this.tidList = tids;
        if (field != null && this.statement instanceof OscarStatement) {
            ((OscarStatement)this.statement).firstField = field;
        }
    }

    public boolean wasNull() throws SQLException {
        this.checkNull();
        if (this.lastColumn == 0) {
            throw new OSQLException("OSCAR-00301", "88888", 301);
        }
        return this.this_row[this.lastColumn - 1] == null;
    }

    public String getString(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        switch (this.getFieldType(columnIndex)) {
            case -4: 
            case -3: 
            case -2: {
                return OSCARbyte.toOSCARString(this.getBytes(columnIndex));
            }
            case 2005: {
                OscarClob clob = this.connection.getClobInstance(new String(this.this_row[columnIndex - 1]));
                long length = clob.length();
                if (length > Integer.MAX_VALUE) {
                    throw new OSQLException("OSCAR-00316", "22000", 316);
                }
                return clob.getSubString(1L, (int)length);
            }
            case 16: {
                return String.valueOf(this.getBoolean(columnIndex));
            }
        }
        return this.encoding.decode(this.this_row[columnIndex - 1]);
    }

    protected String getFixedString(int columnIndex) throws SQLException {
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        return this.encoding.decode(this.this_row[columnIndex - 1]);
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toBoolean(this.getFixedString(columnIndex), this.getFieldType(columnIndex));
    }

    public byte getByte(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toByte(this.getFixedString(columnIndex), this.getFieldType(columnIndex));
    }

    public short getShort(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toShort(this.getFixedString(columnIndex), this.getFieldType(columnIndex));
    }

    public int getInt(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.encoding.hasAsciiNumbers()) {
            try {
                return this.getFastInt(columnIndex);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return OscarResultSet.toInt(this.getFixedString(columnIndex));
    }

    public static int toInt(String s) throws SQLException {
        if (s != null) {
            try {
                s = s.trim();
                return Integer.parseInt(s);
            }
            catch (NumberFormatException e) {
                try {
                    BigDecimal n = new BigDecimal(s);
                    BigInteger i = n.toBigInteger();
                    int gt = i.compareTo(INTMAX);
                    int lt = i.compareTo(INTMIN);
                    if (gt > 0 || lt < 0) {
                        throw new OSQLException("OSCAR-00703", "88888", 703, e);
                    }
                    return i.intValue();
                }
                catch (NumberFormatException ne) {
                    throw new OSQLException("OSCAR-00703", "88888", 703, ne);
                }
            }
        }
        return 0;
    }

    private int getFastInt(int columnIndex) throws SQLException, NumberFormatException {
        int start;
        boolean neg;
        byte[] bytes = this.this_row[columnIndex - 1];
        if (bytes == null) {
            return 0;
        }
        if (bytes.length == 0) {
            throw new NumberFormatException();
        }
        int val = 0;
        if (bytes[0] == 45) {
            neg = true;
            start = 1;
            if (bytes.length == 1 || bytes.length > 10) {
                throw new NumberFormatException();
            }
        } else {
            start = 0;
            neg = false;
            if (bytes.length > 9) {
                throw new NumberFormatException();
            }
        }
        while (start < bytes.length) {
            byte b;
            if ((b = bytes[start++]) < 48 || b > 57) {
                throw new NumberFormatException();
            }
            val *= 10;
            val += b - 48;
        }
        if (neg) {
            val = -val;
        }
        return val;
    }

    public long getLong(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toLong(this.getFixedString(columnIndex), this.getFieldType(columnIndex));
    }

    public float getFloat(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toFloat(this.getFixedString(columnIndex), this.getFieldType(columnIndex));
    }

    public double getDouble(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toDouble(this.getFixedString(columnIndex), this.getFieldType(columnIndex));
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toBigDecimal(this.getFixedString(columnIndex), this.getFieldType(columnIndex), scale);
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        if (this.getFieldType(columnIndex) == 2004) {
            OscarBlob blob = this.connection.getBlobInstance(new String(this.this_row[columnIndex - 1]));
            long length = blob.length();
            if (length > Integer.MAX_VALUE) {
                throw new OSQLException("OSCAR-00316", "22000", 316);
            }
            if (this.encodingFlag) {
                return blob.getBytes(1L, (int)length);
            }
            return this.clientEncoding.encode(this.encoding.decode(blob.getBytes(1L, (int)length)));
        }
        if (this.encodingFlag) {
            return TypeConverter.toBytes(this.connection, this.this_row[columnIndex - 1], this.getFieldType(columnIndex));
        }
        return TypeConverter.toBytes(this.connection, this.clientEncoding.encode(this.encoding.decode(this.this_row[columnIndex - 1])), this.getFieldType(columnIndex));
    }

    public Date getDate(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toDate(this.getFixedString(columnIndex), this.getFieldType(columnIndex));
    }

    public Time getTime(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toTime(this.getFixedString(columnIndex), this.getFieldType(columnIndex));
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toTimestamp(this.getFixedString(columnIndex), this.getFieldType(columnIndex));
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        try {
            return new ByteArrayInputStream(this.getFixedString(columnIndex).getBytes("UTF-16BE"));
        }
        catch (UnsupportedEncodingException l_uee) {
            throw new OSQLException("OSCAR-00303", "88888", 303, l_uee.getMessage(), l_uee);
        }
    }

    public String getString(String columnName) throws SQLException {
        return this.getString(this.findColumn(columnName));
    }

    public boolean getBoolean(String columnName) throws SQLException {
        return this.getBoolean(this.findColumn(columnName));
    }

    public byte getByte(String columnName) throws SQLException {
        return this.getByte(this.findColumn(columnName));
    }

    public short getShort(String columnName) throws SQLException {
        return this.getShort(this.findColumn(columnName));
    }

    public int getInt(String columnName) throws SQLException {
        return this.getInt(this.findColumn(columnName));
    }

    public long getLong(String columnName) throws SQLException {
        return this.getLong(this.findColumn(columnName));
    }

    public float getFloat(String columnName) throws SQLException {
        return this.getFloat(this.findColumn(columnName));
    }

    public double getDouble(String columnName) throws SQLException {
        return this.getDouble(this.findColumn(columnName));
    }

    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return this.getBigDecimal(this.findColumn(columnName), scale);
    }

    public byte[] getBytes(String columnName) throws SQLException {
        return this.getBytes(this.findColumn(columnName));
    }

    public Date getDate(String columnName) throws SQLException {
        return this.getDate(this.findColumn(columnName));
    }

    public Time getTime(String columnName) throws SQLException {
        return this.getTime(this.findColumn(columnName));
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        return this.getTimestamp(this.findColumn(columnName));
    }

    public InputStream getAsciiStream(String columnName) throws SQLException {
        return this.getAsciiStream(this.findColumn(columnName));
    }

    public InputStream getUnicodeStream(String columnName) throws SQLException {
        return this.getUnicodeStream(this.findColumn(columnName));
    }

    public InputStream getBinaryStream(String columnName) throws SQLException {
        return this.getBinaryStream(this.findColumn(columnName));
    }

    public SQLWarning getWarnings() throws SQLException {
        return this.warnings;
    }

    public void clearWarnings() throws SQLException {
        this.warnings = null;
    }

    public void addWarnings(SQLWarning warnings) {
        if (this.warnings != null) {
            this.warnings.setNextWarning(warnings);
        } else {
            this.warnings = warnings;
        }
    }

    public String getCursorName() throws SQLException {
        return this.cursorName;
    }

    public Object getObject(String columnName) throws SQLException {
        return this.getObject(this.findColumn(columnName));
    }

    public int findColumn(String columnName) throws SQLException {
        Integer index;
        if (this.columnNameIndexMap == null) {
            this.initalizeColNames();
        }
        if ((index = (Integer)this.columnNameIndexMap.get(columnName.toUpperCase())) == null) {
            throw new OSQLException("OSCAR-00304", "88888", 304);
        }
        return index;
    }

    private void initalizeColNames() {
        int flen = this.fields.length;
        this.columnNameIndexMap = new HashMap(flen * 2);
        for (int i = 0; i < flen; ++i) {
            this.columnNameIndexMap.put(this.fields[i].getAliasName().toUpperCase(), new Integer(i + 1));
        }
    }

    public boolean reallyResultSet() {
        boolean isNotNull = false;
        if (this.fields == null) {
            return isNotNull;
        }
        for (int i = 0; i < this.fields.length; ++i) {
            if (this.fields[i] == null) continue;
            isNotNull = true;
            break;
        }
        return isNotNull;
    }

    public ResultSet getNext() {
        return this.next;
    }

    public void append(BaseResultSet r) {
        if (this.next == null) {
            this.next = r;
            r.setPrevious(this);
            this.lastResult = r;
        } else {
            this.lastResult.append(r);
        }
        this.lastResult = r;
    }

    public void setPrevious(BaseResultSet rs) {
        this.previous = rs;
    }

    public ResultSet getPrevious() {
        return this.previous;
    }

    public int getResultCount() {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", getResultCount()" + this.updateCount);
        }
        return this.updateCount;
    }

    public int getTupleCount() {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", getTupleCount()" + this.rows.size());
        }
        return this.rows.size();
    }

    public int getColumnCount() {
        if (this.fields == null) {
            return 0;
        }
        return this.fields.length;
    }

    public String getStatusString() {
        return this.status;
    }

    public long getInsertRowid() {
        return this.insertRowid;
    }

    protected void checkIndex(int column) throws SQLException {
        if (column < 1 || column > this.fields.length) {
            throw new OSQLException("OSCAR-00305", "88888", 305);
        }
        this.lastColumn = column;
    }

    protected void checkNull() throws SQLException {
        if (this.this_row == null) {
            throw new OSQLException("OSCAR-00301", "88888", 301);
        }
    }

    protected final int getFieldType(int columnIndex) throws SQLException {
        return this.fields[columnIndex - 1].getSQLType();
    }

    public final boolean isCursorUsed() {
        return this.cursorUsed;
    }

    public void setCursorUsed(boolean flag) {
        this.cursorUsed = flag;
    }

    public void setCursorMoveSize(int size) {
        this.moveSize = size;
    }

    protected void checkClosed() throws SQLException {
        if (this.closed) {
            throw new OSQLException("OSCAR-00306", "88888", 306);
        }
    }

    protected boolean checkResultClosed() throws SQLException {
        return !this.closed;
    }

    public List getTidValues() {
        return this.tidList;
    }

    public Field getTidField() {
        return this.tidField;
    }

    public Object getObject(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        Field field = this.fields[columnIndex - 1];
        switch (field.getSQLType()) {
            case -6: 
            case 4: 
            case 5: {
                return new Integer(this.getInt(columnIndex));
            }
            case -5: {
                return new Long(this.getLong(columnIndex));
            }
            case 7: {
                return new Float(this.getFloat(columnIndex));
            }
            case 6: 
            case 8: {
                return new Double(this.getDouble(columnIndex));
            }
            case 2: 
            case 3: {
                return this.getBigDecimal(columnIndex);
            }
            case 16: {
                return new Boolean(this.getBoolean(columnIndex));
            }
            case -7: 
            case -1: 
            case 1: 
            case 12: {
                return this.getString(columnIndex);
            }
            case -4: 
            case -3: 
            case -2: {
                return this.getBytes(columnIndex);
            }
            case 91: {
                return this.getDate(columnIndex);
            }
            case 92: {
                return this.getTime(columnIndex);
            }
            case 93: {
                if (Boolean.valueOf(this.connection.getConnectionProperties().getProperty("OBJECTTOSTRING", "false")).booleanValue()) {
                    return this.getString(columnIndex);
                }
                return this.getTimestamp(columnIndex);
            }
            case 2003: {
                return this.getArray(columnIndex);
            }
            case 2004: {
                if (Boolean.valueOf(this.connection.getConnectionProperties().getProperty("OBJECTTOSTRING", "false")).booleanValue()) {
                    return ((OscarBlob)this.getBlob(columnIndex)).getSubString(1L, 8000);
                }
                return this.getBlob(columnIndex);
            }
            case 2005: {
                return this.getClob(columnIndex);
            }
            case -11: {
                return this.getBfile(columnIndex);
            }
            case 2002: {
                return new OscarGisStruct(this.getString(columnIndex));
            }
        }
        return this.getString(columnIndex);
    }

    public synchronized boolean absolute(int index) throws SQLException {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", absolute(int index), params: " + index);
        }
        this.checkClosed();
        this.checkMovable();
        this.updateValues.clear();
        String sql = null;
        if (!this.cursorUsed) {
            if (index == 0) {
                this.current_row = -1;
                this.this_row = null;
                return false;
            }
            if (index > 0) {
                if (index > this.rows.size()) {
                    this.current_row = this.rows.size();
                    this.this_row = null;
                    return false;
                }
                this.current_row = index - 1;
            } else {
                if (-index > this.rows.size()) {
                    this.current_row = -1;
                    this.this_row = null;
                    return false;
                }
                this.current_row = index + this.rows.size();
            }
        } else if (index == 0 || index == 1) {
            sql = "MOVE BACKWARD ALL IN " + this.cursorName;
            this.connection.execSQL(sql, this.statement);
            sql = "FETCH FORWARD " + this.fetchSize + " IN " + this.cursorName;
            this.connection.execSQL(sql, this.statement);
            this.cursorPosition = this.moveSize < this.fetchSize ? 1 : 0;
            if (index == 0) {
                this.current_row = -1;
                this.this_row = null;
                this.rowBuffer = null;
                return false;
            }
            this.current_row = 0;
        } else if (index > 1) {
            sql = "MOVE BACKWARD ALL IN " + this.cursorName;
            this.connection.execSQL(sql, this.statement);
            sql = "MOVE FORWARD " + (index - 1) + " IN " + this.cursorName;
            this.connection.execSQL(sql, this.statement);
            boolean size = false;
            if (this.moveSize < index - 1) {
                this.cursorPosition = 1;
                this.rows.clear();
                this.current_row = 0;
                this.rowBuffer = null;
                this.this_row = null;
                return false;
            }
            if (this.moveSize == index - 1) {
                sql = "FETCH FORWARD " + this.fetchSize + " IN " + this.cursorName;
                this.connection.execSQL(sql, this.statement);
                this.current_row = 0;
                this.cursorPosition = this.moveSize < this.fetchSize ? 1 : 0;
            }
        } else {
            int absIndex = -index;
            sql = "MOVE FORWARD ALL IN " + this.cursorName;
            this.connection.execSQL(sql, this.statement);
            sql = "MOVE BACKWARD " + (absIndex + 1) + " IN " + this.cursorName;
            this.connection.execSQL(sql, this.statement);
            if (this.moveSize < absIndex) {
                sql = "FETCH FORWARD " + this.fetchSize + " IN " + this.cursorName;
                this.connection.execSQL(sql, this.statement);
                this.cursorPosition = this.moveSize < this.fetchSize ? 1 : 0;
                this.this_row = null;
                this.rowBuffer = null;
                this.current_row = -1;
                return false;
            }
            if (this.moveSize == absIndex + 1 || this.moveSize == absIndex) {
                sql = "FETCH FORWARD " + this.fetchSize + " IN " + this.cursorName;
                this.connection.execSQL(sql, this.statement);
                this.current_row = 0;
                this.cursorPosition = this.moveSize < this.fetchSize ? 1 : 0;
            }
        }
        if (this.rows.size() == 0) {
            this.current_row = -1;
            this.this_row = null;
            this.rowBuffer = null;
            return false;
        }
        this.this_row = (byte[][])this.rows.get(this.current_row);
        this.rowBuffer = new byte[this.this_row.length][];
        System.arraycopy(this.this_row, 0, this.rowBuffer, 0, this.this_row.length);
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", absolute(int index), rows: " + this.rows.size());
        }
        return true;
    }

    public synchronized void afterLast() throws SQLException {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", afterLast ");
        }
        this.checkClosed();
        this.checkMovable();
        this.updateValues.clear();
        this.this_row = null;
        if (this.isAfterLast()) {
            return;
        }
        if (!this.cursorUsed) {
            this.current_row = this.rows.size();
            return;
        }
        String sql = "MOVE FORWARD ALL IN " + this.cursorName;
        this.connection.execSQL(sql, this.statement);
        this.current_row = 0;
        this.rows.clear();
        this.this_row = null;
        this.rowBuffer = null;
        this.cursorPosition = 1;
    }

    public synchronized void beforeFirst() throws SQLException {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", beforeFirst ");
        }
        this.checkClosed();
        this.checkMovable();
        this.updateValues.clear();
        this.this_row = null;
        if (this.isBeforeFirst()) {
            return;
        }
        if (!this.cursorUsed) {
            this.current_row = -1;
            return;
        }
        String sql = "MOVE BACKWARD ALL IN " + this.cursorName;
        this.connection.execSQL(sql, this.statement);
        sql = "FETCH FORWARD " + this.fetchSize + " IN " + this.cursorName;
        this.connection.execSQL(sql, this.statement);
        this.cursorPosition = this.moveSize < this.fetchSize ? 1 : 0;
        this.this_row = null;
        this.rowBuffer = null;
        this.current_row = -1;
    }

    public boolean first() throws SQLException {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", first ");
        }
        return this.absolute(1);
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        try {
            int type = this.getFieldType(columnIndex);
            if (type == 2004) {
                throw new SQLException("OSCAR-00302", "88888", 302);
            }
            if (type == 2005) {
                return this.getClob(columnIndex).getAsciiStream();
            }
            return new ByteArrayInputStream(this.getFixedString(columnIndex).getBytes("US-ASCII"));
        }
        catch (UnsupportedEncodingException l_uee) {
            throw new OSQLException("OSCAR-00302", "88888", 302, l_uee.getMessage(), l_uee);
        }
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        int type = this.getFieldType(columnIndex);
        if (type == 2004) {
            return this.getBlob(columnIndex).getBinaryStream();
        }
        byte[] b = this.getBytes(columnIndex);
        return new ByteArrayInputStream(b);
    }

    public java.sql.Array getArray(String columnName) throws SQLException {
        return this.getArray(this.findColumn(columnName));
    }

    public java.sql.Array getArray(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        return new Array(this.connection, columnIndex, this.fields[columnIndex - 1], this);
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toBigDecimal(this.getFixedString(columnIndex), this.getFieldType(columnIndex));
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        int t = this.findColumn(columnName);
        return this.getBigDecimal(t);
    }

    public Blob getBlob(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.getFieldType(columnIndex) != 2004) {
            throw new OSQLException("OSCAR-00714", "88888", 714);
        }
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        return this.connection.getBlobInstance(new String(this.this_row[columnIndex - 1]));
    }

    public OscarBfile getBfile(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.getFieldType(columnIndex) != -11) {
            throw new OSQLException("OSCAR-00714", "88888", 714);
        }
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        return this.connection.getBfileInstance(new String(this.this_row[columnIndex - 1]));
    }

    public Blob getBlob(String columnName) throws SQLException {
        return this.getBlob(this.findColumn(columnName));
    }

    public Clob getClob(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        if (this.getFieldType(columnIndex) == 2005) {
            return this.connection.getClobInstance(new String(this.this_row[columnIndex - 1]));
        }
        return new OscarStringClob(this.getString(columnIndex));
    }

    public Clob getClob(String columnName) throws SQLException {
        return this.getClob(this.findColumn(columnName));
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        int type = this.getFieldType(columnIndex);
        if (type == 2004) {
            throw new SQLException("OSCAR-00302", "88888", 302);
        }
        if (type == 2005) {
            return this.getClob(columnIndex).getCharacterStream();
        }
        return new CharArrayReader(this.getFixedString(columnIndex).toCharArray());
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        return this.getCharacterStream(this.findColumn(columnName));
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toDate(this.getFixedString(columnIndex), this.getFieldType(columnIndex), cal, Calendar.getInstance());
    }

    public Date getDate(String columnName, Calendar cal) throws SQLException {
        return this.getDate(this.findColumn(columnName), cal);
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toTime(this.getFixedString(columnIndex), this.getFieldType(columnIndex), cal, Calendar.getInstance());
    }

    public Time getTime(String columnName, Calendar cal) throws SQLException {
        return this.getTime(this.findColumn(columnName), cal);
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        return TypeConverter.toTimestamp(this.getFixedString(columnIndex), this.getFieldType(columnIndex), cal, Calendar.getInstance());
    }

    public Timestamp getTimestamp(String c, Calendar cal) throws SQLException {
        return this.getTimestamp(this.findColumn(c), cal);
    }

    public int getConcurrency() throws SQLException {
        if (this.statement == null) {
            return 1007;
        }
        return this.statement.getResultSetConcurrency();
    }

    public int getFetchDirection() throws SQLException {
        return this.fetchDirection;
    }

    public int getFetchSize() throws SQLException {
        return this.fetchSize;
    }

    public Object getObject(String columnName, Map map) throws SQLException {
        return this.getObject(this.findColumn(columnName), map);
    }

    public Object getObject(int columnIndex, Map map) throws SQLException {
        throw Driver.notImplemented();
    }

    public Ref getRef(String columnName) throws SQLException {
        return this.getRef(this.findColumn(columnName));
    }

    public Ref getRef(int columnIndex) throws SQLException {
        throw new SQLException("oscar.osqlnotimp", "HY000", -1);
    }

    public int getRow() throws SQLException {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", getRow()");
        }
        int rows_size = this.rows.size();
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", rows_size: " + rows_size);
        }
        if (this.current_row < 0 || this.current_row >= rows_size) {
            return 0;
        }
        if (!this.cursorUsed || this.cursorName == null) {
            return this.current_row + 1;
        }
        String sql = "MOVE BACKWARD ALL IN " + this.cursorName;
        this.connection.execSQL(sql, this.statement);
        int size = this.moveSize;
        int retSize = 0;
        if (this.cursorPosition != 1) {
            sql = "MOVE FORWARD " + (this.moveSize + 1) + " IN " + this.cursorName;
            retSize = this.current_row + 2 - this.rows.size();
        } else {
            sql = "MOVE FORWARD ALL IN " + this.cursorName;
            retSize = this.current_row + 1 - this.rows.size();
        }
        this.connection.execSQL(sql, this.statement);
        return size + retSize;
    }

    public Statement getStatement() throws SQLException {
        return (Statement)((Object)this.statement);
    }

    public int getType() throws SQLException {
        return this.type;
    }

    public synchronized boolean isAfterLast() throws SQLException {
        if (!this.cursorUsed) {
            if (this.rows.size() == 0) {
                return false;
            }
            return this.current_row == this.rows.size();
        }
        if (this.nullResult && this.rows.size() == 0) {
            return false;
        }
        return this.current_row == this.rows.size() && this.cursorPosition == 1;
    }

    public boolean isBeforeFirst() throws SQLException {
        if (this.nullResult && this.rows.size() == 0) {
            return false;
        }
        return this.current_row == -1;
    }

    public synchronized boolean isFirst() throws SQLException {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", isFirst() ");
        }
        if (!this.cursorUsed) {
            return this.current_row == 0 && this.rows.size() > 0;
        }
        if (this.current_row == -1) {
            return false;
        }
        if (this.current_row == 0 && this.rows.size() > 0) {
            if (this.cursorPosition == 1) {
                String sql = "MOVE BACKWARD " + (this.rows.size() + 1) + " IN " + this.cursorName;
                this.connection.execSQL(sql, this.statement);
                int size = this.moveSize;
                sql = "MOVE FORWARD ALL IN " + this.cursorName;
                this.connection.execSQL(sql, this.statement);
                return size < this.rows.size() + 1;
            }
            String sql = "MOVE BACKWARD " + this.rows.size() + " IN " + this.cursorName;
            this.connection.execSQL(sql, this.statement);
            int size = this.moveSize;
            if (size < this.rows.size()) {
                sql = "MOVE FORWARD " + (size + 1) + " IN " + this.cursorName;
                this.connection.execSQL(sql, this.statement);
                return true;
            }
            sql = "MOVE FORWARD " + size + " IN " + this.cursorName;
            this.connection.execSQL(sql, this.statement);
            return false;
        }
        return false;
    }

    public synchronized boolean isLast() throws SQLException {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", isLast() ");
        }
        if (!this.cursorUsed) {
            return this.current_row == this.rows.size() - 1 && this.rows.size() != 0;
        }
        if (this.current_row == this.rows.size() - 1) {
            if (this.cursorPosition == 1) {
                return true;
            }
            String sql = "MOVE FORWARD 1 IN " + this.cursorName;
            this.connection.execSQL(sql, this.statement);
            int size = this.moveSize;
            sql = "MOVE BACKWARD 1 IN " + this.cursorName;
            this.connection.execSQL(sql, this.statement);
            return size < 1;
        }
        return false;
    }

    public synchronized boolean last() throws SQLException {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", last()");
        }
        return this.absolute(-1);
    }

    public synchronized boolean previous() throws SQLException {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", previous()");
        }
        this.checkClosed();
        this.checkMovable();
        this.updateValues.clear();
        if (this.current_row == -1) {
            this.this_row = null;
            return false;
        }
        if (--this.current_row < 0) {
            if (!this.cursorUsed) {
                this.this_row = null;
                return false;
            }
            int previousSize = this.rows.size();
            String sql = null;
            if (this.cursorPosition == 0) {
                sql = "MOVE BACKWARD " + (this.fetchSize + previousSize) + " IN " + this.cursorName;
                this.connection.execSQL(sql, this.statement);
                if (this.moveSize < previousSize) {
                    this.current_row = -1;
                    this.rows.clear();
                    this.cursorPosition = -1;
                    this.rowBuffer = null;
                    this.this_row = null;
                    return false;
                }
                int newFetchsize = this.moveSize - previousSize;
                if (this.moveSize < this.fetchSize + previousSize) {
                    ++newFetchsize;
                }
                sql = "FETCH FORWARD " + newFetchsize + " IN " + this.cursorName;
                this.connection.execSQL(sql, this.statement);
                this.current_row = this.rows.size() - 1;
                this.cursorPosition = 0;
            } else if (this.cursorPosition == 1) {
                sql = "MOVE BACKWARD " + (this.fetchSize + (previousSize + 1)) + " IN " + this.cursorName;
                this.connection.execSQL(sql, this.statement);
                if (this.moveSize <= previousSize) {
                    this.current_row = -1;
                    this.cursorPosition = -1;
                    this.rows.clear();
                    this.rowBuffer = null;
                    this.this_row = null;
                    return false;
                }
                int newFetchsize = this.moveSize - (previousSize + 1);
                if (this.moveSize < this.fetchSize + (previousSize + 1)) {
                    ++newFetchsize;
                }
                sql = "FETCH FORWARD " + newFetchsize + " IN " + this.cursorName;
                this.connection.execSQL(sql, this.statement);
                this.current_row = this.rows.size() - 1;
                this.cursorPosition = 0;
            }
        }
        this.this_row = (byte[][])this.rows.get(this.current_row);
        this.rowBuffer = new byte[this.this_row.length][];
        System.arraycopy(this.this_row, 0, this.rowBuffer, 0, this.this_row.length);
        return true;
    }

    public synchronized boolean relative(int relativeSize) throws SQLException {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSet.class + ", relative(int relativeSize)(), params: " + relativeSize);
        }
        this.checkClosed();
        this.checkMovable();
        if (relativeSize == 0) {
            if (this.isAfterLast() || this.isBeforeFirst()) {
                this.this_row = null;
                return false;
            }
            return true;
        }
        this.updateValues.clear();
        int index = relativeSize + this.current_row;
        if (!this.cursorUsed) {
            if (index < 0) {
                this.current_row = -1;
                this.this_row = null;
                return false;
            }
            if (index >= this.rows.size()) {
                this.current_row = this.rows.size();
                this.this_row = null;
                return false;
            }
            this.current_row = index;
        } else if (index < 0) {
            if (this.isBeforeFirst()) {
                this.this_row = null;
                return false;
            }
            int mSize = this.cursorPosition == 1 ? this.rows.size() - index + 1 : this.rows.size() - index;
            String sql = "MOVE BACKWARD " + mSize + " IN " + this.cursorName;
            this.connection.execSQL(sql, this.statement);
            if (this.moveSize < mSize - 1) {
                this.current_row = -1;
                this.rows.clear();
                this.rowBuffer = null;
                this.this_row = null;
                this.cursorPosition = -1;
                return false;
            }
            sql = "FETCH FORWARD " + this.fetchSize + " IN " + this.cursorName;
            this.connection.execSQL(sql, this.statement);
            this.cursorPosition = this.moveSize < this.fetchSize ? 1 : 0;
            this.current_row = 0;
        } else if (index >= this.rows.size()) {
            if (this.cursorPosition == 1) {
                this.current_row = this.rows.size();
                this.this_row = null;
                this.rowBuffer = null;
                return false;
            }
            int mSize = index - this.rows.size();
            if (mSize > 0) {
                String sql = "MOVE FORWARD " + mSize + " IN " + this.cursorName;
                this.connection.execSQL(sql, this.statement);
                if (this.moveSize < mSize) {
                    this.rows.clear();
                    this.current_row = 0;
                    this.this_row = null;
                    this.rowBuffer = null;
                    this.cursorPosition = 1;
                    return false;
                }
                if (this.moveSize == mSize) {
                    sql = "FETCH FORWARD " + this.fetchSize + " IN " + this.cursorName;
                    this.connection.execSQL(sql, this.statement);
                    this.current_row = 0;
                    if (this.moveSize < this.fetchSize) {
                        this.cursorPosition = 1;
                        if (this.rows.size() == 0) {
                            this.this_row = null;
                            this.rowBuffer = null;
                            return false;
                        }
                    } else {
                        this.cursorPosition = 0;
                    }
                }
            } else {
                String sql = "FETCH FORWARD " + this.fetchSize + " IN " + this.cursorName;
                this.connection.execSQL(sql, this.statement);
                this.current_row = 0;
                if (this.moveSize < this.fetchSize) {
                    this.cursorPosition = 1;
                    if (this.rows.size() == 0) {
                        this.this_row = null;
                        this.rowBuffer = null;
                        return false;
                    }
                } else {
                    this.cursorPosition = 0;
                }
            }
        } else {
            this.current_row = index;
        }
        this.this_row = (byte[][])this.rows.get(this.current_row);
        this.rowBuffer = new byte[this.this_row.length][];
        System.arraycopy(this.this_row, 0, this.rowBuffer, 0, this.this_row.length);
        return true;
    }

    public void setFetchDirection(int direction) throws SQLException {
        if (direction != 1000 && direction != 1001 && direction != 1002) {
            throw new OSQLException("OSCAR-00418", "88888", 408);
        }
        if (this.type == 1003 && direction != 1000) {
            throw new OSQLException("OSCAR-00418", "88888", 408);
        }
        this.fetchDirection = direction;
    }

    public void setFetchSize(int rows) throws SQLException {
        if (rows < 0) {
            throw new OSQLException("OSCAR-00307", "88888", 307);
        }
        if (this.maxRows != 0 && rows > this.maxRows) {
            throw new OSQLException("OSCAR-00307", "88888", 307);
        }
        if (rows > 0) {
            this.fetchSize = rows;
        }
    }

    public synchronized void cancelRowUpdates() throws SQLException {
        if (this.doingUpdates) {
            this.doingUpdates = false;
            this.clearRowBuffer();
        }
    }

    public synchronized void deleteRow() throws SQLException {
        this.checkUpdatable();
        if (this.onInsertRow) {
            throw new OSQLException("OSCAR-00308", "88888", 308);
        }
        if (this.rows.size() == 0 || this.isBeforeFirst() || this.isAfterLast()) {
            throw new OSQLException("OSCAR-00301", "88888", 301);
        }
        if (this.deleteStatement == null) {
            this.deleteStatement = this.connection.getMasterConnection().createStatement();
        }
        StringBuffer deleteSQL = new StringBuffer("DELETE FROM ").append(this.schemaName).append(".").append(this.tableName).append(" where rowid = ");
        byte[] rowidArray = (byte[])this.tidList.get(this.current_row);
        if (this.connection.getProtocolVersion().getProtocolType() >= 2) {
            long rowid = NumberConverter.convertBytesToLong(rowidArray);
            deleteSQL.append(rowid);
        } else {
            deleteSQL.append(this.encoding.decode(rowidArray));
        }
        int deleteCount = this.deleteStatement.executeUpdate(deleteSQL.toString());
        this.isRowDeleted = deleteCount > 0;
    }

    public synchronized void insertRow() throws SQLException {
        this.checkUpdatable();
        if (!this.onInsertRow) {
            throw new OSQLException("OSCAR-00309", "88888", 309);
        }
        StringBuffer insertSQL = new StringBuffer("INSERT INTO ").append(this.schemaName).append(".").append(this.tableName).append(" (");
        StringBuffer paramSQL = new StringBuffer(") values (");
        Enumeration columnNames = this.updateValues.keys();
        int numColumns = this.updateValues.size();
        int i = 0;
        while (columnNames.hasMoreElements()) {
            String columnName = (String)columnNames.nextElement();
            insertSQL.append(columnName);
            if (i < numColumns - 1) {
                insertSQL.append(", ");
                paramSQL.append("?,");
            } else {
                paramSQL.append("?)");
            }
            ++i;
        }
        insertSQL.append(paramSQL.toString());
        this.insertStatement = this.connection.getMasterConnection().prepareStatement(insertSQL.toString());
        Enumeration keys = this.updateValues.keys();
        int i2 = 1;
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            Object o = this.updateValues.get(key);
            if (o instanceof NullObject) {
                this.insertStatement.setNull(i2, 0);
            } else {
                this.insertStatement.setObject(i2, o);
            }
            ++i2;
        }
        int insertCount = this.insertStatement.executeUpdate();
        this.isRowinserted = insertCount > 0;
        this.updateRowBuffer();
        if (!this.cursorUsed) {
            int insertRowid = ((OscarStatement)((Object)this.insertStatement)).getInsertRowid();
            this.tidList.add(this.encoding.encode(String.valueOf(insertRowid)));
            this.rows.add(this.rowBuffer);
        }
        this.insertStatement.close();
        this.this_row = this.rowBuffer;
        this.clearRowBuffer();
        this.onInsertRow = false;
    }

    public synchronized void moveToCurrentRow() throws SQLException {
        this.checkUpdatable();
        if (this.current_row < 0 || this.current_row >= this.rows.size()) {
            this.this_row = null;
            this.rowBuffer = null;
        } else {
            this.this_row = (byte[][])this.rows.get(this.current_row);
            this.rowBuffer = new byte[this.this_row.length][];
            System.arraycopy(this.this_row, 0, this.rowBuffer, 0, this.this_row.length);
        }
        this.onInsertRow = false;
    }

    public synchronized void moveToInsertRow() throws SQLException {
        this.checkUpdatable();
        if (this.insertStatement != null) {
            this.insertStatement = null;
        }
        this.clearRowBuffer();
        this.onInsertRow = true;
    }

    private synchronized void clearRowBuffer() throws SQLException {
        this.rowBuffer = new byte[this.fields.length][];
        this.updateValues.clear();
    }

    public boolean rowDeleted() throws SQLException {
        return this.isRowDeleted;
    }

    public boolean rowInserted() throws SQLException {
        return this.isRowinserted;
    }

    public boolean rowUpdated() throws SQLException {
        return this.isRowupdated;
    }

    public synchronized void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        if (x == null) {
            this.updateNull(columnIndex);
            return;
        }
        byte[] theData = new byte[length];
        try {
            x.read(theData, 0, length);
            this.updateValue(columnIndex, new String(theData, "US-ASCII"));
        }
        catch (IOException ie) {
            throw new OSQLException("OSCAR-00310", "88888", 310, ie.getMessage(), ie);
        }
        catch (Exception e) {
            throw new OSQLException("OSCAR-00310", "88888", 310, e);
        }
    }

    public synchronized void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        this.checkUpdatable();
        this.updateValue(columnIndex, x);
    }

    public synchronized void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        if (x == null) {
            this.updateNull(columnIndex);
            return;
        }
        byte[] data = new byte[length];
        try {
            x.read(data, 0, length);
        }
        catch (IOException ie) {
            throw new OSQLException("OSCAR-00103", "88888", 103, ie.getMessage(), ie);
        }
        this.updateBytes(columnIndex, data);
    }

    public synchronized void updateBoolean(int columnIndex, boolean x) throws SQLException {
        this.updateValue(columnIndex, new Boolean(x));
    }

    public synchronized void updateByte(int columnIndex, byte x) throws SQLException {
        this.updateValue(columnIndex, new Byte(x));
    }

    public synchronized void updateBytes(int columnIndex, byte[] x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    public synchronized void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        if (x == null) {
            this.updateNull(columnIndex);
            return;
        }
        char[] data = new char[length];
        String value = null;
        try {
            int rLength = x.read(data, 0, length);
            value = new String(data, 0, rLength);
        }
        catch (IOException ie) {
            throw new OSQLException("OSCAR-00311", "88888", 311, ie.getMessage(), ie);
        }
        this.updateValue(columnIndex, value);
    }

    public synchronized void updateDate(int columnIndex, Date x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    public synchronized void updateDouble(int columnIndex, double x) throws SQLException {
        this.updateValue(columnIndex, new Double(x));
    }

    public synchronized void updateFloat(int columnIndex, float x) throws SQLException {
        this.updateValue(columnIndex, new Float(x));
    }

    public synchronized void updateInt(int columnIndex, int x) throws SQLException {
        this.updateValue(columnIndex, new Integer(x));
    }

    public synchronized void updateLong(int columnIndex, long x) throws SQLException {
        this.updateValue(columnIndex, new Long(x));
    }

    public synchronized void updateNull(int columnIndex) throws SQLException {
        this.updateValue(columnIndex, new NullObject());
    }

    public synchronized void updateObject(int columnIndex, Object x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    public synchronized void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        this.checkIndex(columnIndex);
        if (this.fields[columnIndex - 1].getSQLType() == 2 || this.fields[columnIndex - 1].getSQLType() == 3) {
            try {
                if (scale < -1) {
                    throw new OSQLException("OSCAR-00312", "88888", 312);
                }
                BigDecimal dec = new BigDecimal(x.toString()).setScale(scale);
                this.updateObject(columnIndex, (Object)dec);
            }
            catch (Exception e) {
                throw new OSQLException("OSCAR-00707", "88888", 707, e);
            }
        } else {
            this.updateObject(columnIndex, x);
        }
    }

    public synchronized void refreshRow() throws SQLException {
        this.checkUpdatable();
        if (this.onInsertRow) {
            throw new OSQLException("OSCAR-00308", "88888", 308);
        }
        if (this.rows.size() == 0 || this.isBeforeFirst() || this.isAfterLast()) {
            throw new OSQLException("OSCAR-00301", "88888", 301);
        }
        try {
            this.updateRowBuffer();
            System.arraycopy(this.rowBuffer, 0, this.this_row, 0, this.rowBuffer.length);
            this.rows.set(this.current_row, this.rowBuffer);
            this.this_row = this.rowBuffer;
        }
        catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    public synchronized void updateRow() throws SQLException {
        this.checkUpdatable();
        if (this.onInsertRow) {
            throw new OSQLException("OSCAR-00308", "88888", 308);
        }
        if (this.updateValues.isEmpty()) {
            throw new OSQLException("OSCAR-00315", "88888", 315);
        }
        try {
            StringBuffer updateSQL = new StringBuffer("UPDATE ").append(this.schemaName).append(".").append(this.tableName).append(" SET ");
            int numColumns = this.updateValues.size();
            Enumeration columns = this.updateValues.keys();
            ArrayList<Integer> nullColumns = new ArrayList<Integer>();
            int i = 0;
            while (columns.hasMoreElements()) {
                String column = (String)columns.nextElement();
                if (this.updateValues.get(column) instanceof NullObject) {
                    int columnIndex = this.findColumn(column) - 1;
                    nullColumns.add(columnIndex);
                }
                updateSQL.append(column);
                updateSQL.append(" = ?");
                if (i < numColumns - 1) {
                    updateSQL.append(", ");
                }
                ++i;
            }
            if (this.tidList == null) {
                throw new OSQLException("OSCAR-00317", "88888", 317);
            }
            byte[] rowidArray = (byte[])this.tidList.get(this.current_row);
            if (this.connection.getProtocolVersion().getProtocolType() >= 2) {
                long rowid = NumberConverter.convertBytesToLong(rowidArray);
                updateSQL.append(" WHERE rowid = " + rowid);
            } else {
                updateSQL.append(" WHERE rowid = " + this.encoding.decode(rowidArray));
            }
            this.updateStatement = this.connection.getMasterConnection().prepareStatement(updateSQL.toString());
            int i2 = 0;
            for (Object o : this.updateValues.values()) {
                if (o instanceof NullObject) {
                    this.updateStatement.setNull(i2 + 1, 0);
                } else {
                    this.updateStatement.setObject(i2 + 1, o);
                }
                ++i2;
            }
            int updateCount = this.updateStatement.executeUpdate();
            this.isRowupdated = updateCount > 0;
            this.updateStatement.close();
            this.updateStatement = null;
            this.updateRowBuffer();
            for (int col = 0; col < this.rowBuffer.length; ++col) {
                if (this.rowBuffer[col] != null || nullColumns.size() > 0 && nullColumns.contains(col)) continue;
                this.rowBuffer[col] = this.this_row[col];
            }
            System.arraycopy(this.rowBuffer, 0, this.this_row, 0, this.rowBuffer.length);
            this.rows.set(this.current_row, this.rowBuffer);
            this.updateValues.clear();
            this.doingUpdates = false;
        }
        catch (SQLException se) {
            throw se;
        }
        catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    public synchronized void updateShort(int columnIndex, short x) throws SQLException {
        this.updateValue(columnIndex, new Short(x));
    }

    public synchronized void updateString(int columnIndex, String x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    public synchronized void updateTime(int columnIndex, Time x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    public synchronized void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        this.updateValue(columnIndex, x);
    }

    public synchronized void updateNull(String columnName) throws SQLException {
        this.updateNull(this.findColumn(columnName));
    }

    public synchronized void updateBoolean(String columnName, boolean x) throws SQLException {
        this.updateBoolean(this.findColumn(columnName), x);
    }

    public synchronized void updateByte(String columnName, byte x) throws SQLException {
        this.updateByte(this.findColumn(columnName), x);
    }

    public synchronized void updateShort(String columnName, short x) throws SQLException {
        this.updateShort(this.findColumn(columnName), x);
    }

    public synchronized void updateInt(String columnName, int x) throws SQLException {
        this.updateInt(this.findColumn(columnName), x);
    }

    public synchronized void updateLong(String columnName, long x) throws SQLException {
        this.updateLong(this.findColumn(columnName), x);
    }

    public synchronized void updateFloat(String columnName, float x) throws SQLException {
        this.updateFloat(this.findColumn(columnName), x);
    }

    public synchronized void updateDouble(String columnName, double x) throws SQLException {
        this.updateDouble(this.findColumn(columnName), x);
    }

    public synchronized void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        this.updateBigDecimal(this.findColumn(columnName), x);
    }

    public synchronized void updateString(String columnName, String x) throws SQLException {
        this.updateString(this.findColumn(columnName), x);
    }

    public synchronized void updateBytes(String columnName, byte[] x) throws SQLException {
        this.updateBytes(this.findColumn(columnName), x);
    }

    public synchronized void updateDate(String columnName, Date x) throws SQLException {
        this.updateDate(this.findColumn(columnName), x);
    }

    public synchronized void updateTime(String columnName, Time x) throws SQLException {
        this.updateTime(this.findColumn(columnName), x);
    }

    public synchronized void updateTimestamp(String columnName, Timestamp x) throws SQLException {
        this.updateTimestamp(this.findColumn(columnName), x);
    }

    public synchronized void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnName), x, length);
    }

    public synchronized void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnName), x, length);
    }

    public synchronized void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnName), reader, length);
    }

    public synchronized void updateObject(String columnName, Object x, int scale) throws SQLException {
        this.updateObject(this.findColumn(columnName), x, scale);
    }

    public synchronized void updateObject(String columnName, Object x) throws SQLException {
        this.updateObject(this.findColumn(columnName), x);
    }

    private void updateRowBuffer() throws SQLException {
        Enumeration columns = this.updateValues.keys();
        block21: while (columns.hasMoreElements()) {
            String columnName = (String)columns.nextElement();
            int columnIndex = this.findColumn(columnName) - 1;
            Object valueObject = this.updateValues.get(columnName);
            if (valueObject instanceof NullObject) {
                this.rowBuffer[columnIndex] = null;
                continue;
            }
            if (this.connection.getProtocolVersion().getProtocolType() >= 2) {
                byte[] tmpBytes = null;
                boolean netDataByStr = this.connection.isNetDataByStr();
                switch (this.connection.getSQLType(this.fields[columnIndex].getDBType())) {
                    case -6: 
                    case 5: {
                        this.rowBuffer[columnIndex] = NumberConverter.convertIntToBytes(((Short)valueObject).intValue());
                        break;
                    }
                    case 4: {
                        this.rowBuffer[columnIndex] = NumberConverter.convertIntToBytes((Integer)valueObject);
                        break;
                    }
                    case -5: {
                        this.rowBuffer[columnIndex] = NumberConverter.convertLongToBytes(((Long)valueObject).intValue());
                        break;
                    }
                    case -7: 
                    case 16: {
                        this.rowBuffer[columnIndex] = BooleanConverter.convertBooleanToBytes(Boolean.parseBoolean(String.valueOf(valueObject)));
                        break;
                    }
                    case -1: 
                    case 1: 
                    case 12: {
                        tmpBytes = this.encoding.encode(String.valueOf(valueObject));
                        this.rowBuffer[columnIndex] = this.convertByteArr(tmpBytes);
                        break;
                    }
                    case 91: {
                        if (netDataByStr) {
                            this.rowBuffer[columnIndex] = this.encoding.encode(String.valueOf(((Date)valueObject).getTime()));
                            break;
                        }
                        this.rowBuffer[columnIndex] = DateConverter.convertDateToBytes((Date)valueObject);
                        break;
                    }
                    case 92: {
                        this.rowBuffer[columnIndex] = TimeConverter.convertTimeToBytes((Time)valueObject);
                        break;
                    }
                    case 93: {
                        if (netDataByStr) {
                            if (valueObject instanceof Timestamp) {
                                this.rowBuffer[columnIndex] = this.encoding.encode(String.valueOf(((Timestamp)valueObject).getTime()));
                                break;
                            }
                            this.rowBuffer[columnIndex] = this.encoding.encode(String.valueOf(((Date)valueObject).getTime()));
                            break;
                        }
                        if (valueObject instanceof Timestamp) {
                            this.rowBuffer[columnIndex] = TimestampConverter.convertTimestampToBytes((Timestamp)valueObject);
                            break;
                        }
                        this.rowBuffer[columnIndex] = TimestampConverter.convertTimestampToBytes(new Timestamp(((Date)valueObject).getTime()));
                        break;
                    }
                    case 2004: 
                    case 2005: {
                        tmpBytes = this.encoding.encode(String.valueOf(valueObject));
                        break;
                    }
                    case 0: {
                        break;
                    }
                    case -4: 
                    case -3: 
                    case -2: 
                    case 2: 
                    case 3: 
                    case 6: 
                    case 7: 
                    case 8: {
                        if (valueObject instanceof byte[]) {
                            this.rowBuffer[columnIndex] = this.connection.getEncoding().encode(OSCARbyte.toOSCARString((byte[])valueObject));
                            break;
                        }
                        this.rowBuffer[columnIndex] = this.connection.getEncoding().encode(String.valueOf(valueObject));
                        break;
                    }
                    default: {
                        throw new OSQLException("OSCAR-00411", "88888", 411);
                    }
                }
                continue;
            }
            switch (this.connection.getSQLType(this.fields[columnIndex].getDBType())) {
                case -7: 
                case -6: 
                case -5: 
                case -1: 
                case 1: 
                case 2: 
                case 3: 
                case 4: 
                case 5: 
                case 6: 
                case 7: 
                case 8: 
                case 12: 
                case 16: 
                case 92: {
                    this.rowBuffer[columnIndex] = this.connection.getEncoding().encode(String.valueOf(valueObject));
                    continue block21;
                }
                case 2004: 
                case 2005: {
                    continue block21;
                }
                case 91: {
                    this.rowBuffer[columnIndex] = this.connection.getEncoding().encode(String.valueOf(((Date)valueObject).getTime()));
                    continue block21;
                }
                case 93: {
                    if (valueObject instanceof Timestamp) {
                        this.rowBuffer[columnIndex] = this.connection.getEncoding().encode(String.valueOf(((Timestamp)valueObject).getTime()));
                        continue block21;
                    }
                    this.rowBuffer[columnIndex] = this.connection.getEncoding().encode(String.valueOf(((Date)valueObject).getTime()));
                    continue block21;
                }
                case 0: {
                    continue block21;
                }
                case -4: 
                case -3: 
                case -2: {
                    if (valueObject instanceof byte[]) {
                        this.rowBuffer[columnIndex] = this.connection.getEncoding().encode(OSCARbyte.toOSCARString((byte[])valueObject));
                        continue block21;
                    }
                    this.rowBuffer[columnIndex] = this.connection.getEncoding().encode(String.valueOf(valueObject));
                    continue block21;
                }
            }
            throw new OSQLException("OSCAR-00411", "88888", 411);
        }
    }

    protected byte[] convertByteArr(byte[] src) {
        if (src != null && src.length > 240) {
            byte[] s = this.convertVarcharData(src);
            return s;
        }
        return src;
    }

    public byte[] convertVarcharData(byte[] data) {
        int len = data.length + 1 + (data.length + 240 - 1) / 240 + 1;
        byte[] result = new byte[len];
        result[0] = -2;
        int srcPosition = 0;
        int destPosition = 1;
        int tmp = data.length - srcPosition;
        while (true) {
            if (tmp == 240) {
                result[destPosition] = -16;
                System.arraycopy(data, srcPosition, result, ++destPosition, 240);
                srcPosition += 240;
                destPosition += 240;
                break;
            }
            if (tmp > 240) {
                result[destPosition] = -16;
                System.arraycopy(data, srcPosition, result, ++destPosition, 240);
                destPosition += 240;
            } else {
                result[destPosition] = (byte)tmp;
                System.arraycopy(data, srcPosition, result, ++destPosition, tmp);
                srcPosition += tmp;
                destPosition += tmp;
                break;
            }
            tmp = data.length - (srcPosition += 240);
        }
        result[len - 1] = 0;
        return result;
    }

    protected void updateValue(int columnIndex, Object value) throws SQLException {
        this.checkUpdatable();
        this.checkIndex(columnIndex);
        if (!this.onInsertRow) {
            this.checkNull();
        }
        boolean bl = this.doingUpdates = !this.onInsertRow;
        if (value == null) {
            this.updateNull(columnIndex);
        } else {
            this.updateValues.put(this.fields[columnIndex - 1].getName(), value);
        }
    }

    protected void checkMovable() throws SQLException {
        if (this.type == 1003) {
            throw new OSQLException("OSCAR-00313", "88888", 313);
        }
    }

    protected void checkUpdatable() throws SQLException {
        if (this.concurrency == 1007 || !this.canUpdateable) {
            throw new OSQLException("OSCAR-00314", "88888", 314);
        }
    }

    public URL getURL(int columnIndex) throws SQLException {
        throw Driver.notImplemented();
    }

    public URL getURL(String columnName) throws SQLException {
        throw Driver.notImplemented();
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw Driver.notImplemented();
    }

    public void updateRef(String columnName, Ref x) throws SQLException {
        throw Driver.notImplemented();
    }

    public synchronized void updateBlob(int columnIndex, Blob x) throws SQLException {
        this.updateObject(columnIndex, (Object)x);
    }

    public synchronized void updateBlob(String columnName, Blob x) throws SQLException {
        this.updateBlob(this.findColumn(columnName), x);
    }

    public synchronized void updateClob(int columnIndex, Clob x) throws SQLException {
        this.updateObject(columnIndex, (Object)x);
    }

    public synchronized void updateClob(String columnName, Clob x) throws SQLException {
        this.updateClob(this.findColumn(columnName), x);
    }

    public synchronized void updateArray(int columnIndex, java.sql.Array x) throws SQLException {
        throw Driver.notImplemented();
    }

    public synchronized void updateArray(String columnName, java.sql.Array x) throws SQLException {
        throw Driver.notImplemented();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return new OscarResultSetMetaData(this.fields, (OscarJdbc2Connection)this.connection);
    }

    public void setPlanID(byte[] planID) {
        this.planID = planID;
    }

    public byte[] getPlanID() {
        return this.planID;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public byte[] getCurrentBlock() {
        System.arraycopy(this.this_row[0], 0, this.this_row[2], 0, 4);
        System.arraycopy(this.this_row[1], 0, this.this_row[2], 4, 4);
        return this.this_row[2];
    }

    public void setResultType(boolean flag) {
        this.isOldProtocolResult = flag;
    }

    class NullObject {
        NullObject() {
        }
    }
}

