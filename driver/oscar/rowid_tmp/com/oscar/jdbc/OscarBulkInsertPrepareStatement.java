/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.Driver;
import com.oscar.core.ImportHandler;
import com.oscar.jdbc.OscarJdbc2BulkConnection;
import com.oscar.util.OSQLException;
import com.oscar.util.TableNameParser;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OscarBulkInsertPrepareStatement
implements PreparedStatement {
    private OscarJdbc2BulkConnection connection = null;
    private ImportHandler bulkHandler = null;
    private String tableName = null;
    private String[] fieldName = null;
    private String fieldNameStr = null;
    protected boolean isClosed = false;
    protected int maxfieldSize = 0;
    private HashMap constantMap = new HashMap();
    private HashMap variableMap = new HashMap();
    protected int maxrows = 0;
    protected int timeout = 0;
    protected boolean replaceProcessingEnabled = true;
    protected SQLWarning warnings = null;
    private String schemaName = null;

    OscarBulkInsertPrepareStatement(OscarJdbc2BulkConnection connection, String sql, int rsType, int rsConcurrency, int rsHoldability) throws SQLException {
        this.connection = connection;
        this.parseSQL(sql);
        this.bulkHandler = this.schemaName != null ? connection.createImportHandler(this.schemaName, this.tableName) : connection.createImportHandler(this.tableName);
        this.bulkHandler.setBufferSize(connection.getBufferSize());
        if (this.fieldNameStr != null) {
            this.bulkHandler.setColumnOrder(this.fieldNameStr);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void parseSQL(String sql) throws SQLException {
        String tmpSql = sql.trim();
        tmpSql = tmpSql.replaceAll("\r\n", " ").replaceAll("\n", " ");
        Pattern p = Pattern.compile("INSERT\\s+INTO\\s+((\".*?(|\"\").*?\")|(\\w|[\u4e00-\u9fa5])+)\\s*((\\.)\\s*(((\".*\")|(\"\"))|(\\w|[\u4e00-\u9fa5])+))?(\\s+|\\(+)", 2);
        Pattern pI = Pattern.compile("INSERT\\s+INTO", 2);
        Matcher matcher = p.matcher(tmpSql);
        String tempSplit = null;
        if (!matcher.find()) throw new SQLException("Parsing grammarerror, your sql is: " + sql);
        String temp = matcher.group();
        Matcher mI = pI.matcher(temp);
        if (mI.find()) {
            this.tableName = temp.endsWith("(") ? temp.substring(mI.group().length(), temp.length() - 1).trim() : temp.substring(mI.group().length(), temp.length()).trim();
            String[] names = TableNameParser.parserToDbNames(this.tableName);
            this.schemaName = names[0];
            this.tableName = names[1];
        }
        tempSplit = temp.endsWith("(") ? tmpSql.substring(temp.length() - 1, tmpSql.length()) : tmpSql.substring(temp.length(), tmpSql.length());
        String firstPartStr = null;
        String secondPartStr = null;
        Pattern pAfter = Pattern.compile("\\((\\s*(('.*?[^\\\\]')|(\\w|[\u4e00-\u9fa5])+|\\?)\\s*)(,\\s*(('.*?[^\\\\]')|(\\w|[\u4e00-\u9fa5])+|\\?)\\s*)*\\)", 2);
        Matcher matcherAfter = pAfter.matcher(tempSplit);
        if (tempSplit.trim().toUpperCase().startsWith("VALUES")) {
            if (!matcherAfter.find()) throw new SQLException("Parsing grammarerror, your sql is: " + sql);
            secondPartStr = matcherAfter.group();
        } else {
            Pattern pBefore = Pattern.compile("\\((\\s*((\".*?[^\\\\]\")|(\\w|[\u4e00-\u9fa5])+|\\?)\\s*)(,\\s*((\".*?[^\\\\]\")|(\\w|[\u4e00-\u9fa5])+|\\?)\\s*)*\\)", 2);
            Matcher matcherBefore = pBefore.matcher(tempSplit);
            if (!matcherBefore.find()) {
                throw new SQLException("Parsing grammarerror, your sql is: " + sql);
            }
            firstPartStr = matcherBefore.group();
            if (!matcherAfter.find(firstPartStr.length())) throw new SQLException("Parsing grammarerror, your sql is: " + sql);
            secondPartStr = matcherAfter.group();
        }
        Pattern pColumn = Pattern.compile("\\s*((\".*?[^\\\\]\")|(\\w|[\u4e00-\u9fa5])+|\\?)\\s*", 2);
        if (null != firstPartStr) {
            Matcher matcherFirst = pColumn.matcher(firstPartStr);
            ArrayList<String> splitFirst = new ArrayList<String>();
            while (matcherFirst.find()) {
                splitFirst.add(matcherFirst.group());
            }
            String[] splitFieldNameArray = new String[splitFirst.size()];
            for (int j = 0; j < splitFirst.size(); ++j) {
                splitFieldNameArray[j] = (String)splitFirst.get(j);
            }
            if (splitFieldNameArray != null && splitFieldNameArray.length > 0) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < splitFieldNameArray.length; ++i) {
                    String temp2 = splitFieldNameArray[i].trim();
                    splitFieldNameArray[i] = temp2.length() > 2 && temp2.startsWith("\"") && temp2.endsWith("\"") ? temp2.substring(1, temp2.length() - 1) : temp2.toUpperCase();
                    sb.append(splitFieldNameArray[i]).append(",");
                }
                this.fieldNameStr = sb.substring(0, sb.length() - 1);
                this.fieldName = splitFieldNameArray;
            }
        }
        Pattern pParam = Pattern.compile("\\s*(('.*?[^\\\\]')|\\w+|\\?)\\s*", 2);
        Matcher matcherSecond = pParam.matcher(secondPartStr);
        ArrayList<String> splitSecond = new ArrayList<String>();
        while (matcherSecond.find()) {
            splitSecond.add(matcherSecond.group());
        }
        String[] splitQuestionMarkArray = new String[splitSecond.size()];
        for (int j = 0; j < splitSecond.size(); ++j) {
            splitQuestionMarkArray[j] = (String)splitSecond.get(j);
        }
        if (splitQuestionMarkArray == null || splitQuestionMarkArray.length <= 0 || this.fieldName != null && splitQuestionMarkArray.length != this.fieldName.length) {
            throw new SQLException("The total number of question marks must be the same as the total number of column names, your sql is: " + sql);
        }
        int index = 0;
        for (int i = 0; i < splitQuestionMarkArray.length; ++i) {
            splitQuestionMarkArray[i] = splitQuestionMarkArray[i].trim();
            if ("?".equals(splitQuestionMarkArray[i])) {
                this.variableMap.put(new Integer(++index), new Integer(i + 1));
                continue;
            }
            if ("null".equalsIgnoreCase(splitQuestionMarkArray[i])) {
                this.constantMap.put(new Integer(i + 1), null);
                continue;
            }
            if (splitQuestionMarkArray[i].matches("^'.*'$")) {
                splitQuestionMarkArray[i] = splitQuestionMarkArray[i].substring(1, splitQuestionMarkArray[i].length() - 1);
            }
            this.constantMap.put(new Integer(i + 1), splitQuestionMarkArray[i]);
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        throw Driver.notImplemented();
    }

    public int executeUpdate(String sql) throws SQLException {
        throw Driver.notImplemented();
    }

    public void close() throws SQLException {
        if (this.isClosed) {
            return;
        }
        if (this.bulkHandler != null) {
            this.bulkHandler.close();
            this.bulkHandler = null;
        }
        this.isClosed = true;
    }

    public int getMaxFieldSize() throws SQLException {
        return this.maxfieldSize;
    }

    public void setMaxFieldSize(int max) throws SQLException {
        throw Driver.notImplemented();
    }

    public int getMaxRows() throws SQLException {
        throw Driver.notImplemented();
    }

    public void setMaxRows(int max) throws SQLException {
        throw Driver.notImplemented();
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw Driver.notImplemented();
    }

    public int getQueryTimeout() throws SQLException {
        return this.timeout;
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        this.timeout = seconds;
    }

    public void cancel() throws SQLException {
        this.connection.cancelQuery();
    }

    public void addWarning(SQLWarning warn) {
        if (this.warnings != null) {
            this.warnings.setNextWarning(warn);
        } else {
            this.warnings = warn;
        }
    }

    public SQLWarning getWarnings() throws SQLException {
        this.checkClosed();
        return this.warnings;
    }

    public void clearWarnings() throws SQLException {
        this.warnings = null;
    }

    public void setCursorName(String name) throws SQLException {
        this.checkClosed();
    }

    public boolean execute(String sql) throws SQLException {
        this.checkClosed();
        this.clearWarnings();
        if (this.bulkHandler != null) {
            return this.bulkHandler.execute();
        }
        return false;
    }

    public ResultSet getResultSet() throws SQLException {
        return null;
    }

    public int getUpdateCount() throws SQLException {
        this.checkClosed();
        return this.bulkHandler.getUpdateCount();
    }

    public boolean getMoreResults() throws SQLException {
        return false;
    }

    public void setFetchDirection(int direction) throws SQLException {
    }

    public int getFetchDirection() throws SQLException {
        return 0;
    }

    public void setFetchSize(int rows) throws SQLException {
    }

    public int getFetchSize() throws SQLException {
        return 0;
    }

    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    public int getResultSetType() throws SQLException {
        return 0;
    }

    public void addBatch(String sql) throws SQLException {
        throw Driver.notImplemented();
    }

    public void clearBatch() throws SQLException {
    }

    public int[] executeBatch() throws SQLException {
        int[] resultSize = new int[1];
        this.execute();
        resultSize[0] = this.bulkHandler.getUpdateCount();
        return resultSize;
    }

    public Connection getConnection() throws SQLException {
        return this.connection;
    }

    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw Driver.notImplemented();
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw Driver.notImplemented();
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw Driver.notImplemented();
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw Driver.notImplemented();
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw Driver.notImplemented();
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw Driver.notImplemented();
    }

    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    public ResultSet executeQuery() throws SQLException {
        throw Driver.notImplemented();
    }

    public int executeUpdate() throws SQLException {
        throw Driver.notImplemented();
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setNull(parameterIndex, sqlType);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setBoolean(parameterIndex, x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setByte(parameterIndex, x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setShort(parameterIndex, x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setInt(parameterIndex, x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setLong(parameterIndex, x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setFloat(parameterIndex, x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setDouble(parameterIndex, x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setBigDecimal(parameterIndex, x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setString(parameterIndex, x);
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setBytes(parameterIndex, x);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setDate(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setTime(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setTimestamp(parameterIndex, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setAsciiStream(parameterIndex, x);
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setUnicodeStream(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setBinaryStream(parameterIndex, x, length);
    }

    public void clearParameters() throws SQLException {
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        throw Driver.notImplemented();
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setObject(parameterIndex, x, targetSqlType);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setObject(parameterIndex, x);
    }

    public boolean execute() throws SQLException {
        return this.bulkHandler.execute();
    }

    public void addBatch() throws SQLException {
        for (Integer index : this.constantMap.keySet()) {
            this.bulkHandler.setString((int)index, (String)this.constantMap.get(index));
        }
        this.bulkHandler.endRow();
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        throw Driver.notImplemented();
    }

    public void setRef(int i, Ref x) throws SQLException {
        throw Driver.notImplemented();
    }

    public void setBlob(int i, Blob x) throws SQLException {
        i = this.reIndex(i);
        this.bulkHandler.setBlob(i, x);
    }

    public void setClob(int i, Clob x) throws SQLException {
        i = this.reIndex(i);
        this.bulkHandler.setClob(i, x);
    }

    public void setArray(int i, Array x) throws SQLException {
        i = this.reIndex(i);
        this.bulkHandler.setArray(i, x);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setDate(parameterIndex, x, cal);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setTime(parameterIndex, x, cal);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        parameterIndex = this.reIndex(parameterIndex);
        this.bulkHandler.setTimestamp(parameterIndex, x, cal);
    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        paramIndex = this.reIndex(paramIndex);
        this.bulkHandler.setNull(paramIndex, sqlType, typeName);
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        throw Driver.notImplemented();
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return null;
    }

    protected void checkClosed() throws SQLException {
        if (this.isClosed) {
            throw new OSQLException("OSCAR-00409", "00409", 118);
        }
    }

    private int reIndex(int x) {
        return (Integer)this.variableMap.get(x);
    }
}

