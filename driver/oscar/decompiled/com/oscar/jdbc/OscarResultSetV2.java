/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.core.BaseStatement;
import com.oscar.core.Encoding;
import com.oscar.core.Field;
import com.oscar.jdbc.OscarBlob;
import com.oscar.jdbc.OscarClob;
import com.oscar.jdbc.OscarResultSet;
import com.oscar.protocol.OSCARProtocolV2;
import com.oscar.sql.Date;
import com.oscar.util.OSCARbyte;
import com.oscar.util.OSQLException;
import com.oscar.util.converter.BooleanConverter;
import com.oscar.util.converter.DateConverter;
import com.oscar.util.converter.IntervalConverter;
import com.oscar.util.converter.NumberConverter;
import com.oscar.util.converter.RowidConverter;
import com.oscar.util.converter.TimestampConverter;
import com.oscar.util.converter.TimestamptzConverter;
import com.oscar.util.converter.TimetzConverter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class OscarResultSetV2
extends OscarResultSet {
    private boolean netDataByStr = false;
    private boolean numericKeepPrecision = true;
    private static ThreadLocal<SimpleDateFormat> formaterHolder = new ThreadLocal<SimpleDateFormat>(){

        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };
    private static ThreadLocal<SimpleDateFormat> oldFormaterHolder = new ThreadLocal<SimpleDateFormat>(){

        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    public OscarResultSetV2(BaseStatement statement, BaseConnection connection, boolean netDataByStr, boolean numericKeepPrecision, int resultSetType, int resultSetConcurrency, boolean canUpdateable, int fetchDirection, Encoding encoding, String curSorName, Field[] fields, List tuples, String status, int updateCount, long insertOID, int fetchSize, int maxRows) {
        this.connection = connection;
        this.netDataByStr = netDataByStr;
        this.numericKeepPrecision = numericKeepPrecision;
        this.statement = statement;
        this.type = resultSetType;
        this.concurrency = resultSetConcurrency;
        this.canUpdateable = canUpdateable;
        this.fetchSize = fetchSize;
        this.fetchDirection = fetchDirection;
        this.encoding = encoding;
        this.clientEncoding = connection.getClientEncoding();
        if (encoding == null) {
            this.encodingFlag = true;
            encoding = this.clientEncoding;
        } else {
            this.encodingFlag = encoding.equals(this.clientEncoding);
        }
        this.fields = fields;
        this.rows = tuples;
        this.status = status;
        this.updateCount = updateCount;
        this.insertRowid = insertOID;
        this.cursorName = curSorName;
        if (fetchSize > this.rows.size()) {
            this.cursorPosition = 1;
        }
        if (this.rows.size() == 0) {
            this.nullResult = true;
        }
        this.maxRows = maxRows;
        if (fields != null && fields.length > 0 && fields[0] != null) {
            this.tableName = fields[0].getTableName();
            this.schemaName = fields[0].getSchemaName();
        }
    }

    public OscarResultSetV2(BaseStatement statement, Field[] fields, List tuples, String status, int updateCount, long insertTid) {
        this.connection = statement.getDBConnection();
        this.netDataByStr = this.connection.isNetDataByStr();
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

    public OscarResultSetV2(BaseStatement statement, Field[] fields, List tuples, String status, int updateCount, long insertOID, int maxRows) {
        this(statement, fields, tuples, status, updateCount, insertOID);
        this.maxRows = maxRows;
        if (fields != null && fields.length > 0 && fields[0] != null) {
            this.tableName = fields[0].getTableName();
            this.schemaName = fields[0].getSchemaName();
        }
    }

    public OscarResultSetV2(BaseStatement statement, Field[] fields, List tuples, String status, int updateCount, long insertOID, int fetchSize, int maxRows) {
        this(statement, fields, tuples, status, updateCount, insertOID, maxRows);
    }

    public void close() throws SQLException {
        if (this.type != 1003 || this.concurrency != 1007) {
            super.close();
            return;
        }
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.cursorUsed && this.connection != null && !((Connection)((Object)this.connection)).isClosed()) {
            int index = this.connection.checkPlanID(this.planID);
            if (index > -1) {
                try {
                    ((OSCARProtocolV2)this.connection.getProtocol()).fetchMore(null, null, null, null, this.planID, this.fetchSize, this.maxRows, true, false, this.statement, false);
                    this.connection.removePlanID(index);
                }
                catch (Exception e) {
                    // empty catch block
                }
            }
            try {
                if (this.connection.hasCursor(this.cursorName)) {
                    String sql = "CLOSE " + this.cursorName + ";DEALLOCATE " + this.cursorName;
                    this.connection.execSQL(sql, this.statement);
                    this.connection.removeCursor(this.cursorName);
                }
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

    public synchronized boolean next() throws SQLException {
        if (this.isOldProtocolResult || this.type != 1003 || this.concurrency != 1007) {
            return super.next();
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
            if (this.planID != null) {
                ((OSCARProtocolV2)this.connection.getProtocol()).fetchMore(null, null, null, null, this.planID, this.fetchSize, this.maxRows, false, false, this.statement, this, false);
                this.cursorPosition = this.moveSize < this.fetchSize ? 1 : 0;
            } else {
                this.cursorPosition = 1;
            }
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

    public String getString(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        if (this.netDataByStr) {
            return super.getString(columnIndex);
        }
        return this.getStringValue(this.this_row[columnIndex - 1], this.getOscarType(columnIndex));
    }

    protected String getFixedString(int columnIndex) throws SQLException {
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        if (this.netDataByStr) {
            return super.getFixedString(columnIndex);
        }
        return this.getStringValue(this.this_row[columnIndex - 1], this.getOscarType(columnIndex));
    }

    private String getStringValue(byte[] value, int oscarType) throws SQLException {
        if (value == null) {
            return null;
        }
        if (value.length == 2 && value[0] == -3) {
            if (value[1] == 1) {
                return "NaN";
            }
            if (value[1] == 2) {
                return "infinity";
            }
            if (value[1] == 3) {
                return "-infinity";
            }
        }
        switch (oscarType) {
            case 35: {
                return this.encoding.decode(value);
            }
            case 51: {
                OscarClob clob = this.connection.getClobInstance(new String(value));
                long length = clob.length();
                if (length > Integer.MAX_VALUE) {
                    throw new OSQLException("OSCAR-00316", "22000", 316);
                }
                return clob.getSubString(1L, (int)length);
            }
            case 33: {
                return String.valueOf(BooleanConverter.convertToBoolean(value));
            }
            case 25: {
                Date date = (Date)DateConverter.convertBytesToDate(value);
                String res = null;
                res = this.connection.isCompatibleOldDateFormat() || value.length == 4 ? oldFormaterHolder.get().format(date) : formaterHolder.get().format(date);
                return date.isBC() ? res + " BC" : res;
            }
            case 34: {
                if (this.numericKeepPrecision) {
                    return this.encoding.decode(value);
                }
                String retVal = String.valueOf(NumberConverter.convertBytesToDouble(value));
                if (retVal.endsWith(".0") && retVal.length() > 2) {
                    return retVal.substring(0, retVal.length() - 2);
                }
                return retVal;
            }
            case 31: {
                return IntervalConverter.convertToIntervalDTS(value);
            }
            case 30: {
                return IntervalConverter.convertToIntervalYTM(value);
            }
            case 23: {
                return String.valueOf(NumberConverter.convertBytesToLong(value));
            }
            case 32: {
                return String.valueOf(RowidConverter.convertToRowID(value));
            }
            case 26: {
                return String.valueOf(TimetzConverter.convertBytesToTime(value));
            }
            case 28: {
                return TimestampConverter.convertBytesToTimeStamp(value).stringValue();
            }
            case 29: {
                return TimestamptzConverter.convertBytesToTimeStamp(value).stringValue();
            }
            case 27: {
                return String.valueOf(TimetzConverter.convertBytesToTime(value));
            }
            case 24: 
            case 2003: {
                return this.encoding.decode(value);
            }
        }
        return this.encoding.decode(value);
    }

    protected int getOscarType(int columnIndex) throws SQLException {
        return this.fields[columnIndex - 1].getOscarType();
    }

    public int getInt(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return 0;
        }
        if (this.netDataByStr) {
            return super.getInt(columnIndex);
        }
        switch (this.getOscarType(columnIndex)) {
            case 34: {
                if (this.numericKeepPrecision) {
                    if (this.connection.getIntWithPrecision()) {
                        return (int)Double.parseDouble(this.encoding.decode(this.this_row[columnIndex - 1]));
                    }
                    return Integer.parseInt(this.encoding.decode(this.this_row[columnIndex - 1]));
                }
                return (int)NumberConverter.convertBytesToDouble(this.this_row[columnIndex - 1]);
            }
            case 23: {
                return (int)NumberConverter.convertBytesToLong(this.this_row[columnIndex - 1]);
            }
        }
        return super.getInt(columnIndex);
    }

    public long getLong(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return 0L;
        }
        if (this.netDataByStr) {
            return super.getLong(columnIndex);
        }
        switch (this.getOscarType(columnIndex)) {
            case 34: {
                if (this.numericKeepPrecision) {
                    if (this.connection.getIntWithPrecision()) {
                        return (long)Double.parseDouble(this.encoding.decode(this.this_row[columnIndex - 1]));
                    }
                    return Long.parseLong(this.encoding.decode(this.this_row[columnIndex - 1]));
                }
                return (long)NumberConverter.convertBytesToDouble(this.this_row[columnIndex - 1]);
            }
            case 23: {
                return NumberConverter.convertBytesToLong(this.this_row[columnIndex - 1]);
            }
        }
        return super.getLong(columnIndex);
    }

    public float getFloat(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return 0.0f;
        }
        if (this.netDataByStr) {
            return super.getFloat(columnIndex);
        }
        switch (this.getOscarType(columnIndex)) {
            case 34: {
                if (this.numericKeepPrecision) {
                    return Float.parseFloat(this.encoding.decode(this.this_row[columnIndex - 1]));
                }
                return (float)NumberConverter.convertBytesToDouble(this.this_row[columnIndex - 1]);
            }
            case 23: {
                return NumberConverter.convertBytesToLong(this.this_row[columnIndex - 1]);
            }
        }
        return super.getFloat(columnIndex);
    }

    public double getDouble(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return 0.0;
        }
        if (this.netDataByStr) {
            return super.getDouble(columnIndex);
        }
        if (this.this_row[columnIndex - 1].length == 2 && this.this_row[columnIndex - 1][0] == -3) {
            if (this.this_row[columnIndex - 1][1] == 1) {
                return Double.NaN;
            }
            if (this.this_row[columnIndex - 1][1] == 2) {
                return Double.POSITIVE_INFINITY;
            }
            if (this.this_row[columnIndex - 1][1] == 3) {
                return Double.NEGATIVE_INFINITY;
            }
        }
        switch (this.getOscarType(columnIndex)) {
            case 34: {
                if (this.numericKeepPrecision) {
                    return Double.parseDouble(this.encoding.decode(this.this_row[columnIndex - 1]));
                }
                return NumberConverter.convertBytesToDouble(this.this_row[columnIndex - 1]);
            }
            case 23: {
                return NumberConverter.convertBytesToLong(this.this_row[columnIndex - 1]);
            }
        }
        return super.getDouble(columnIndex);
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        if (this.netDataByStr) {
            return super.getBytes(columnIndex);
        }
        switch (this.getOscarType(columnIndex)) {
            case 24: {
                if (this.encodingFlag) {
                    return this.this_row[columnIndex - 1];
                }
                return this.clientEncoding.encode(this.encoding.decode(this.this_row[columnIndex - 1]));
            }
            case 2003: {
                if (this.encodingFlag) {
                    return this.this_row[columnIndex - 1];
                }
                return this.clientEncoding.encode(this.encoding.decode(this.this_row[columnIndex - 1]));
            }
            case 50: {
                OscarBlob blob = this.connection.getBlobInstance(new String(this.this_row[columnIndex - 1]));
                long length = blob.length();
                if (length > Integer.MAX_VALUE) {
                    throw new OSQLException("OSCAR-00316", "22000", 316);
                }
                return blob.getBytes(1L, (int)length);
            }
            case 35: {
                if (this.encodingFlag) {
                    return OSCARbyte.toBytes(this.this_row[columnIndex - 1]);
                }
                return OSCARbyte.toBytes(this.clientEncoding.encode(this.encoding.decode(this.this_row[columnIndex - 1])));
            }
        }
        return this.clientEncoding.encode(this.getString(columnIndex));
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        if (this.netDataByStr) {
            return super.getTimestamp(columnIndex);
        }
        switch (this.getOscarType(columnIndex)) {
            case 26: {
                return new Timestamp(TimetzConverter.convertBytesToTime(this.this_row[columnIndex - 1]).getTime());
            }
            case 28: {
                return TimestampConverter.convertBytesToTimeStamp(this.this_row[columnIndex - 1]);
            }
            case 29: {
                return TimestamptzConverter.convertBytesToTimeStamp(this.this_row[columnIndex - 1]);
            }
            case 27: {
                return new Timestamp(TimetzConverter.convertBytesToTime(this.this_row[columnIndex - 1]).getTime());
            }
        }
        return super.getTimestamp(columnIndex);
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        if (this.netDataByStr) {
            return super.getTimestamp(columnIndex, cal);
        }
        switch (this.getOscarType(columnIndex)) {
            case 26: {
                return new Timestamp(TimetzConverter.convertBytesToTime(this.this_row[columnIndex - 1], cal).getTime());
            }
            case 28: {
                return TimestamptzConverter.convertBytesToTimeStamp(this.this_row[columnIndex - 1], cal);
            }
            case 29: {
                return TimestamptzConverter.convertBytesToTimeStamp(this.this_row[columnIndex - 1], cal);
            }
            case 27: {
                return new Timestamp(TimetzConverter.convertBytesToTime(this.this_row[columnIndex - 1], cal).getTime());
            }
        }
        return super.getTimestamp(columnIndex, cal);
    }

    public Time getTime(int columnIndex) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        if (this.netDataByStr) {
            return super.getTime(columnIndex);
        }
        switch (this.getOscarType(columnIndex)) {
            case 26: {
                return TimetzConverter.convertBytesToTime(this.this_row[columnIndex - 1]);
            }
            case 28: {
                return new Time(TimestampConverter.convertBytesToTimeStamp(this.this_row[columnIndex - 1]).getTime());
            }
            case 29: {
                return new Time(TimestamptzConverter.convertBytesToTimeStamp(this.this_row[columnIndex - 1]).getTime());
            }
            case 27: {
                return TimetzConverter.convertBytesToTime(this.this_row[columnIndex - 1]);
            }
        }
        return super.getTime(columnIndex);
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        this.checkClosed();
        this.checkIndex(columnIndex);
        this.checkNull();
        if (this.this_row[columnIndex - 1] == null) {
            return null;
        }
        if (this.netDataByStr) {
            return super.getTime(columnIndex);
        }
        switch (this.getOscarType(columnIndex)) {
            case 26: {
                return TimetzConverter.convertBytesToTime(this.this_row[columnIndex - 1], cal);
            }
            case 28: {
                return new Time(TimestamptzConverter.convertBytesToTimeStamp(this.this_row[columnIndex - 1], cal).getTime());
            }
            case 29: {
                return new Time(TimestamptzConverter.convertBytesToTimeStamp(this.this_row[columnIndex - 1], cal).getTime());
            }
            case 27: {
                return TimetzConverter.convertBytesToTime(this.this_row[columnIndex - 1], cal);
            }
        }
        return super.getTime(columnIndex);
    }

    public synchronized boolean isLast() throws SQLException {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarResultSetV2.class + ", isLast() ");
        }
        if (this.nullResult && this.rows.size() == 0) {
            return false;
        }
        if (!this.cursorUsed) {
            return this.current_row == this.rows.size() - 1 && this.rows.size() != 0;
        }
        if (this.current_row == this.rows.size() - 1) {
            if (this.cursorPosition == 1) {
                return true;
            }
            if (this.planID != null) {
                ((OSCARProtocolV2)this.connection.getProtocol()).fetchMore(null, null, null, null, this.planID, this.fetchSize, this.maxRows, false, false, this.statement, this, false);
                if (this.moveSize == 0) {
                    return true;
                }
                this.cursorPosition = this.moveSize < this.fetchSize ? 1 : 0;
                return false;
            }
            return super.isLast();
        }
        return false;
    }
}

