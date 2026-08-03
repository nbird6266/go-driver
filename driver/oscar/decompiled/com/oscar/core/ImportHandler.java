/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.core.BaseConnection;
import com.oscar.util.ImportStream;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public interface ImportHandler {
    public static final int NULL_DATA_PROCESS_MODE_ERROR = 0;
    public static final int NULL_DATA_PROCESS_MODE_DB_DEFAULT = -3;
    public static final int NULL_DATA_PROCESS_MODE_NULL = -1;
    public static final int END_IMPORT_MARK = -2;

    public BaseConnection getConnection() throws SQLException;

    public void setHintParam(String var1) throws SQLException;

    public void setNullDataProcessMode(int var1) throws SQLException;

    public void setBufferProcessMode(boolean var1);

    public boolean writeWithRowCache();

    public void setColumnOrder(String var1) throws SQLException;

    public void setBufferSize(int var1);

    public void setArray(int var1, Array var2) throws SQLException;

    public void setAsciiStream(int var1, InputStream var2) throws SQLException;

    public void setBigDecimal(int var1, BigDecimal var2) throws SQLException;

    public void setBinaryStream(int var1, InputStream var2) throws SQLException;

    public void setCharacterStream(int var1, Reader var2, long var3) throws SQLException;

    public void setCharacterStream(int var1, Reader var2) throws SQLException;

    public void setBinaryStream(int var1, InputStream var2, long var3) throws SQLException;

    public void setBlob(int var1, Blob var2) throws SQLException;

    public void setBoolean(int var1, boolean var2) throws SQLException;

    public void setByte(int var1, byte var2) throws SQLException;

    public void setBytes(int var1, byte[] var2) throws SQLException;

    public void setClob(int var1, Clob var2) throws SQLException;

    public void setDate(int var1, Date var2) throws SQLException;

    public void setDate(int var1, Date var2, Calendar var3) throws SQLException;

    public void setDouble(int var1, double var2) throws SQLException;

    public void setFloat(int var1, float var2) throws SQLException;

    public void setInt(int var1, int var2) throws SQLException;

    public void setLong(int var1, long var2) throws SQLException;

    public void setNull(int var1, int var2) throws SQLException;

    public void setNull(int var1, int var2, String var3) throws SQLException;

    public void setObject(int var1, Object var2) throws SQLException;

    public void setObject(int var1, Object var2, int var3) throws SQLException;

    public void setShort(int var1, short var2) throws SQLException;

    public void setString(int var1, char[] var2) throws SQLException;

    public void setString(int var1, String var2) throws SQLException;

    public void setTime(int var1, Time var2) throws SQLException;

    public void setTime(int var1, Time var2, Calendar var3) throws SQLException;

    public void setTimestamp(int var1, Timestamp var2) throws SQLException;

    public void setTimestamp(int var1, Timestamp var2, Calendar var3) throws SQLException;

    public void setUnicodeStream(int var1, InputStream var2) throws SQLException;

    public void setNull(int var1) throws SQLException;

    public void cancel() throws SQLException;

    public void clearRow() throws SQLException;

    public void beginRow() throws SQLException;

    public void endRow() throws SQLException;

    public boolean execute() throws SQLException;

    public int getUpdateCount() throws SQLException;

    public ImportStream getImportStream();

    public SQLWarning getWarnings() throws SQLException;

    public void addWarning(String var1, String var2);

    public void addWarning(SQLWarning var1);

    public void clearWarnings() throws SQLException;

    public void close() throws SQLException;

    public int getImportBlockParam();

    public void setImportBlockParam(int var1);
}

