/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.core.BaseStatement;
import com.oscar.core.Encoding;
import com.oscar.core.ImportHandler;
import com.oscar.jdbc.OscarResultSetMetaData;
import com.oscar.protocol.ProtocolTypeConverter;
import com.oscar.util.ByteData;
import com.oscar.util.ColumnData;
import com.oscar.util.ColumnDataFactory;
import com.oscar.util.EscapeTools;
import com.oscar.util.ImportStream;
import com.oscar.util.ImportStream1;
import com.oscar.util.ImportStream2;
import com.oscar.util.NullData;
import com.oscar.util.OSQLException;
import com.oscar.util.ShareImportStream;
import com.oscar.util.StreamData;
import com.oscar.util.TableNameParser;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class OscarImportHandler
implements ImportHandler {
    protected volatile BaseConnection connection;
    protected SQLWarning warnings = null;
    protected OscarResultSetMetaData dbTableInfo;
    private int updateCount = -2;
    protected String tableName;
    protected String schemName;
    private String hintParam;
    public int nullDataProcessMode = -1;
    private ColumnData[] rowData;
    private Map<Integer, String> columnIndexTypesMap = new LinkedHashMap<Integer, String>(16);
    private Map<String, String> columnNameTypeMap = new LinkedHashMap<String, String>(16);
    public ImportStream importStream;
    private StringBuffer insertBulkStr;
    private String columnOrder;
    private boolean isClosed = false;
    private ColumnDataFactory factory = new ColumnDataFactory();
    protected Encoding encoding;
    protected boolean isBegin = false;
    private boolean hasStreamData = false;
    private ByteData byteData = null;
    private StreamData streamData = null;
    private boolean writeWithoutRowCache = false;
    public static final int BUFFER_SIZE_DEFAULT = 0x500000;
    private int defaultBufferSize = 0x500000;
    private int multiexectuples = 0;
    private static final int MAX_BYTE_SIZE = 32765;
    protected static boolean logFlag = Driver.getLogLevel() >= 1;
    public static int BULK_FLOW = 0;
    public static int BULK_BATCH = 1;
    private int bulkKind = BULK_BATCH;
    private static final String PUBLIC_SCHEMA = "PUBLIC";
    private boolean hasNotSubmittedNullValueOfRow = false;

    public OscarImportHandler(BaseConnection conn, String tableName) throws SQLException {
        this(conn, null, tableName);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public OscarImportHandler(BaseConnection conn, String schemName, String tableName) throws SQLException {
        BaseConnection baseConnection = this.connection = conn;
        synchronized (baseConnection) {
            this.encoding = this.connection.getEncoding();
            this.initImportTable(schemName, tableName);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public OscarImportHandler(BaseConnection conn) throws SQLException {
        BaseConnection baseConnection = this.connection = conn;
        synchronized (baseConnection) {
            this.encoding = this.connection.getEncoding();
        }
    }

    public void initImportTable(String schemName, String tableName) throws SQLException {
        this.initImportTable(schemName, tableName, null);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void initImportTable(String innerSchemaName, String innerTableName, List<String[]> columnStrs) throws SQLException {
        if (innerTableName == null || innerTableName.length() == 0) {
            throw new SQLException("\u8868\u540d\u4e0d\u80fd\u662fnull");
        }
        this.tableName = innerTableName = TableNameParser.dbStringToOrgString(innerTableName);
        if (innerSchemaName == null) {
            innerSchemaName = this.fetchSchema(innerTableName);
            if (innerSchemaName == null || innerSchemaName.length() <= 0) throw new SQLException("\u83b7\u53d6\u5230\u6a21\u5f0f\u540d\u662fnull\u6216\u8005\u4e0d\u7b26\u5408\u89c4\u5219");
            this.schemName = innerSchemaName;
        } else {
            this.schemName = innerSchemaName = TableNameParser.dbStringToOrgString(innerSchemaName);
        }
        this.connection.getProtocol().setImportHandler(this);
        this.insertBulkStr = innerSchemaName == null ? new StringBuffer("INSERT BULK " + EscapeTools.quotationWrapper(innerTableName) + " (") : new StringBuffer("INSERT BULK " + EscapeTools.quotationWrapper(innerSchemaName) + "." + EscapeTools.quotationWrapper(innerTableName) + " (");
        int columnCount = 0;
        if (columnStrs == null || columnStrs.size() == 0) {
            this.initColumns(innerSchemaName, innerTableName);
            for (String column : this.columnNameTypeMap.keySet()) {
                this.insertBulkStr.append(" " + EscapeTools.quotationWrapper(column) + " " + this.columnNameTypeMap.get(column));
                this.insertBulkStr.append(",");
            }
            this.insertBulkStr.deleteCharAt(this.insertBulkStr.length() - 1);
            columnCount = this.columnNameTypeMap.size();
        } else {
            String columnName = null;
            String typeName = null;
            HashSet<String> columnSet = new HashSet<String>();
            for (String[] column : columnStrs) {
                if (column == null || column.length != 2) continue;
                columnName = column[0];
                typeName = column[1];
                if (columnSet.contains(columnName)) continue;
                columnSet.add(columnName);
                this.columnIndexTypesMap.put(columnCount, typeName);
                this.columnNameTypeMap.put(TableNameParser.dbStringToOrgString(columnName), typeName);
                ++columnCount;
                this.insertBulkStr.append(" " + EscapeTools.quotationWrapper(columnName) + " " + typeName);
                this.insertBulkStr.append(",");
            }
            columnSet = null;
            this.insertBulkStr.deleteCharAt(this.insertBulkStr.length() - 1);
        }
        this.insertBulkStr.append(")");
        if (columnCount == 0) {
            throw new SQLException("Relation " + this.tableName + " does not exist.");
        }
        this.rowData = new ColumnData[columnCount];
        if (!logFlag) return;
        StringBuffer log = new StringBuffer();
        log.append("session: " + this.connection.getSessionID()).append(", " + OscarImportHandler.class).append(", initImportTable(String schemName, String tableName)").append("\n").append("bulkSql: " + this.insertBulkStr.toString());
        Driver.writeLog(log.toString());
    }

    private void initColumns(String innerSchemaName, String innerTableName) throws SQLException {
        StringBuffer sqlBuf = new StringBuffer();
        sqlBuf.append("select A.ATTNAME AS NAME , T.TYPNAME AS TYPE_NAME, CASE WHEN (A.ATTTYPID = 16) THEN 1 WHEN (A.ATTTYPID = 17) THEN 8000 ").append(" WHEN (A.ATTTYPID = 18) THEN 1 WHEN (A.ATTTYPID = 19) THEN 128 WHEN (A.ATTTYPID = 20) THEN 19 WHEN (A.ATTTYPID = 21) ").append(" THEN 5 WHEN (A.ATTTYPID = 23) THEN 10 WHEN (A.ATTTYPID = 25) THEN 64000 WHEN (A.ATTTYPID = 26) THEN 10 ").append(" WHEN (A.ATTTYPID = 700) THEN 24 WHEN (A.ATTTYPID = 701) THEN 53 WHEN (A.ATTTYPID = 702) THEN 22 ").append(" WHEN (A.ATTTYPID = 972) THEN 3 WHEN (A.ATTTYPID = 1042) THEN (A.ATTTYPMOD - 4) WHEN (A.ATTTYPID = 1043) THEN (A.ATTTYPMOD - 4) ").append(" WHEN (A.ATTTYPID = 1082) THEN 10 WHEN (A.ATTTYPID = 1083) THEN 8 WHEN (A.ATTTYPID = 1114) THEN 26 WHEN (A.ATTTYPID = (1184)) THEN 22 ").append(" WHEN (A.ATTTYPID = 1186) THEN 33 WHEN (A.ATTTYPID = 1188) THEN 21 WHEN (A.ATTTYPID = 1365) THEN (A.ATTTYPMOD - 4) WHEN (A.ATTTYPID = 1560) ").append(" THEN A.ATTTYPMOD WHEN (A.ATTTYPID = 1700) THEN (A.ATTTYPMOD >> 16) WHEN (A.ATTTYPID = 2174) THEN 24 WHEN (A.ATTTYPID = 2175) ").append(" THEN 53 WHEN (A.ATTTYPID = 2315) THEN (A.ATTTYPMOD >> 16) WHEN (A.ATTTYPID = 3000) THEN 2147483647 WHEN (A.ATTTYPID = 3001) ").append(" THEN 2147483647 WHEN (A.ATTTYPID = 3002) THEN 2147483647 WHEN (A.ATTTYPID = 3100) THEN (A.ATTTYPMOD - 4) ELSE NULL END AS COLUMN_SIZE ").append(" from INFO_SCHEM.V_SYS_ATTRIBUTE A  left join INFO_SCHEM.V_SYS_TYPE T on(A.ATTTYPID = T.OID) ").append(" where A.ATTNUM >0 ");
        String schemaStr = null;
        schemaStr = innerSchemaName == null || innerSchemaName.length() == 0 ? "CURRENT_SCHEMA()" : EscapeTools.toSingleQuotationMarks(innerSchemaName);
        sqlBuf.append("and  A.ATTRELID =  INFO_SCHEM.sys_get_relid(" + schemaStr + "," + EscapeTools.toSingleQuotationMarks(innerTableName) + ") ");
        sqlBuf.append("order by A.ATTNUM;");
        String sql = sqlBuf.toString();
        Statement stmt = this.connection.createStatement();
        ((BaseStatement)((Object)stmt)).setPrint(true);
        ResultSet rs = null;
        try {
            stmt.setFetchSize(0);
            rs = stmt.executeQuery(sql);
            String columnName = null;
            String typeName = null;
            int precision = 0;
            int columnCount = 0;
            HashSet<String> columnSet = new HashSet<String>();
            while (rs.next()) {
                columnName = rs.getString(1);
                typeName = rs.getString(2);
                if (columnSet.contains(columnName)) continue;
                columnSet.add(columnName);
                if (typeName.equalsIgnoreCase("interval") || typeName.equalsIgnoreCase("INTERVALYTM") || typeName.equalsIgnoreCase("numeric") || typeName.equalsIgnoreCase("decimal") || typeName.equalsIgnoreCase("TIMESTAMP") || typeName.equalsIgnoreCase("INTERVALDTS")) {
                    typeName = "TEXT";
                }
                precision = rs.getInt(3);
                if (typeName.equalsIgnoreCase("char") || typeName.equalsIgnoreCase("varchar") || typeName.equalsIgnoreCase("bit") || typeName.equalsIgnoreCase("binary") || typeName.equalsIgnoreCase("varbinary") || typeName.equalsIgnoreCase("bpchar")) {
                    typeName = typeName + "(" + precision + ")";
                }
                this.columnNameTypeMap.put(columnName, typeName);
                this.columnIndexTypesMap.put(columnCount, typeName);
                ++columnCount;
            }
        }
        catch (SQLException ex) {
            throw ex;
        }
        finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    public String fetchSchema(String tableName) throws SQLException {
        String sql = "SELECT TABLE_SCHEM FROM V_SYS_TABLES WHERE TABLE_SCHEM IN('PUBLIC',CURRENT_SCHEMA()) AND TABLE_TYPE NOT IN('VIEW','SYSTEM TABLE') AND TABLE_NAME='" + tableName + "' ";
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = this.connection.createStatement();
            stmt.setFetchSize(0);
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                String schema = rs.getString(1);
                if (PUBLIC_SCHEMA.equals(schema)) {
                    String string = rs.next() ? rs.getString(1) : schema;
                    return string;
                }
                String string = schema;
                return string;
            }
            String schema = null;
            return schema;
        }
        catch (SQLException ex) {
            throw ex;
        }
        finally {
            if (stmt != null) {
                stmt.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
    }

    @Override
    public void setHintParam(String hintParam) throws SQLException {
        this.hintParam = hintParam;
    }

    @Override
    public void setNullDataProcessMode(int mode) throws SQLException {
        this.nullDataProcessMode = mode;
    }

    public boolean isBegin() {
        return this.isBegin;
    }

    @Override
    public void setColumnOrder(String columnOrder) throws SQLException {
        this.checkColumn(columnOrder);
        this.columnOrder = columnOrder;
    }

    public String getColumnOrder() {
        return this.columnOrder;
    }

    public String getDBColumnType(String columnName) throws SQLException {
        String type = null;
        type = this.columnNameTypeMap.get(columnName);
        if (type == null) {
            throw new OSQLException("OSCAR-00807", "88888", 107);
        }
        return type;
    }

    public void checkColumn(String columnOrder) throws SQLException {
        this.insertBulkStr = new StringBuffer("INSERT BULK ");
        if (this.schemName != null) {
            this.insertBulkStr.append(EscapeTools.quotationWrapper(this.schemName)).append(".");
        }
        this.insertBulkStr.append(EscapeTools.quotationWrapper(this.tableName)).append(" (");
        StringTokenizer columns = new StringTokenizer(columnOrder, ",");
        int count = columns.countTokens();
        this.rowData = new ColumnData[count];
        String columnName = null;
        String typeName = null;
        int i = 0;
        this.columnIndexTypesMap.clear();
        int columnCount = 0;
        while (columns.hasMoreTokens()) {
            columnName = columns.nextToken();
            this.insertBulkStr.append(EscapeTools.quotationWrapper(columnName));
            typeName = this.getDBColumnType(columnName);
            this.columnIndexTypesMap.put(columnCount, typeName);
            ++columnCount;
            this.insertBulkStr.append(" " + typeName);
            if (++i == count) continue;
            this.insertBulkStr.append(",");
        }
        this.insertBulkStr.append(")");
    }

    public int getBufferSize() {
        return this.defaultBufferSize;
    }

    @Override
    public void setBufferSize(int size) {
        this.defaultBufferSize = size * 1024 * 1024;
        if (this.defaultBufferSize <= 0) {
            this.defaultBufferSize = 0x500000;
        }
    }

    @Override
    public void setArray(int i, Array x) throws SQLException {
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.connection.getEncoding()));
    }

    @Override
    public void setAsciiStream(int i, InputStream x) throws SQLException {
        this.setRowDataByIndex(i - 1, x);
    }

    @Override
    public void setBigDecimal(int i, BigDecimal x) throws SQLException {
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.connection.getEncoding()));
    }

    @Override
    public void setBinaryStream(int i, InputStream x) throws SQLException {
        this.setRowDataByIndex(i - 1, x);
    }

    @Override
    public void setBinaryStream(int i, InputStream x, long length) throws SQLException {
        this.setRowDataByIndex(i - 1, x, length);
    }

    @Override
    public void setCharacterStream(int i, Reader reader, long length) throws SQLException {
        this.setRowDataByIndex(i - 1, reader, length);
    }

    @Override
    public void setCharacterStream(int i, Reader reader) throws SQLException {
        this.setRowDataByIndex(i - 1, reader);
    }

    @Override
    public void setBlob(int i, Blob x) throws SQLException {
        this.setRowDataByIndex(i - 1, x.getBinaryStream());
    }

    @Override
    public void setBoolean(int i, boolean x) throws SQLException {
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    @Override
    public void setByte(int i, byte x) throws SQLException {
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    @Override
    public void setBytes(int i, byte[] x) throws SQLException {
        if (this.multiexectuples == 1) {
            if (x == null) {
                NullData.preWirte(this.importStream, this.nullDataProcessMode);
            } else {
                this.importStream.write(x, 8, x.length - 8);
            }
        } else if (this.writeWithRowCache()) {
            this.setRowDataByIndex(i - 1, x);
        } else if (x == null) {
            NullData.preWirte(this.importStream, this.nullDataProcessMode);
        } else {
            this.importStream.writeInteger(x.length + 2, 2);
            this.importStream.write(x);
        }
    }

    @Override
    public void setNull(int i) throws SQLException {
        this.hasNotSubmittedNullValueOfRow = true;
    }

    @Override
    public void setClob(int i, Clob x) throws SQLException {
        this.setRowDataByIndex(i - 1, x.getCharacterStream());
    }

    @Override
    public void setDate(int i, Date x) throws SQLException {
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    @Override
    public void setDate(int i, Date x, Calendar cal) throws SQLException {
        throw new SQLException("oscarJDBC does not support this method of setDate(int, Date, Calendar).");
    }

    @Override
    public void setDouble(int i, double x) throws SQLException {
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    @Override
    public void setFloat(int i, float x) throws SQLException {
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    @Override
    public void setInt(int i, int x) throws SQLException {
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    @Override
    public void setLong(int i, long x) throws SQLException {
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    @Override
    public void setNull(int i, int sqlType) throws SQLException {
        this.hasNotSubmittedNullValueOfRow = true;
    }

    @Override
    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        this.hasNotSubmittedNullValueOfRow = true;
    }

    @Override
    public void setObject(int i, Object parameterObj) throws SQLException {
        if (parameterObj == null) {
            this.setNull(i, 1111);
        } else if (parameterObj instanceof Byte) {
            this.setByte(i, (Byte)parameterObj);
        } else if (parameterObj instanceof String) {
            this.setString(i, (String)parameterObj);
        } else if (parameterObj instanceof BigDecimal) {
            this.setBigDecimal(i, (BigDecimal)parameterObj);
        } else if (parameterObj instanceof Short) {
            this.setShort(i, (Short)parameterObj);
        } else if (parameterObj instanceof Integer) {
            this.setInt(i, (Integer)parameterObj);
        } else if (parameterObj instanceof Long) {
            this.setLong(i, (Long)parameterObj);
        } else if (parameterObj instanceof Float) {
            this.setFloat(i, ((Float)parameterObj).floatValue());
        } else if (parameterObj instanceof Double) {
            this.setDouble(i, (Double)parameterObj);
        } else if (parameterObj instanceof byte[]) {
            this.setBytes(i, (byte[])parameterObj);
        } else if (parameterObj instanceof Date) {
            this.setDate(i, (Date)parameterObj);
        } else if (parameterObj instanceof Time) {
            this.setTime(i, (Time)parameterObj);
        } else if (parameterObj instanceof Timestamp) {
            this.setTimestamp(i, (Timestamp)parameterObj);
        } else if (parameterObj instanceof Boolean) {
            this.setBoolean(i, (Boolean)parameterObj);
        } else if (parameterObj instanceof InputStream) {
            this.setBinaryStream(i, (InputStream)parameterObj);
        } else if (parameterObj instanceof Blob) {
            this.setBlob(i, (Blob)parameterObj);
        } else if (parameterObj instanceof Clob) {
            this.setClob(i, (Clob)parameterObj);
        } else {
            this.setString(i, parameterObj.toString());
        }
    }

    @Override
    public void setObject(int i, Object x, int targetSqlType) throws SQLException {
    }

    @Override
    public void setShort(int i, short x) throws SQLException {
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    @Override
    public void setString(int i, char[] x) throws SQLException {
        this.setString(i, String.valueOf(x));
    }

    @Override
    public void setString(int i, String x) throws SQLException {
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    @Override
    public void setTime(int i, Time x) throws SQLException {
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    @Override
    public void setTime(int i, Time x, Calendar cal) throws SQLException {
        this.setTime(i, x);
    }

    @Override
    public void setTimestamp(int i, Timestamp x) throws SQLException {
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    @Override
    public void setTimestamp(int i, Timestamp x, Calendar cal) throws SQLException {
        this.setTimestamp(i, x);
    }

    @Override
    public void setUnicodeStream(int i, InputStream x) throws SQLException {
        throw new SQLException("oscarJDBC does not support this method of setUnicodeStream(int, InputStream).");
    }

    public boolean checkIndexAndSQLType(int i, int type) {
        return true;
    }

    @Override
    public void cancel() throws SQLException {
        this.connection.cancelQuery();
    }

    @Override
    public void clearWarnings() throws SQLException {
        this.warnings = null;
    }

    @Override
    public void close() throws SQLException {
        if (this.isClosed) {
            return;
        }
        this.connection = null;
        this.dbTableInfo = null;
        this.hintParam = null;
        this.nullDataProcessMode = -1;
        this.rowData = null;
        this.tableName = null;
        this.updateCount = -2;
        this.warnings = null;
        this.isClosed = true;
        if (this.importStream != null) {
            if (!this.importStream.isFinished()) {
                this.importStream.finished();
            }
            this.importStream.close();
        }
        this.importStream = null;
    }

    @Override
    public BaseConnection getConnection() throws SQLException {
        return this.connection;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return this.updateCount;
    }

    public void setUpdateCount(int count) {
        this.updateCount = count;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.warnings;
    }

    public void begin() throws SQLException {
        this.isBegin = true;
        if (this.importStream == null) {
            if (this.writeWithRowCache()) {
                try {
                    this.importStream = new ShareImportStream(this);
                }
                catch (Exception e) {
                    throw new SQLException(e.getMessage());
                }
            } else {
                this.importStream = new ImportStream2(this);
            }
        } else {
            this.importStream.reInit();
        }
    }

    public void checkImportStreamException() throws SQLException {
        Exception exception;
        if (this.importStream == null) {
            return;
        }
        Throwable exp = this.importStream.getThreadException();
        if (exp != null) {
            if (exp instanceof SQLException) {
                throw (SQLException)exp;
            }
            throw new OSQLException("OSCAR-00804", "88888", 804, exp);
        }
        if (this.importStream instanceof ImportStream1 && (exception = ((ImportStream1)this.importStream).getException()) != null) {
            throw new OSQLException("OSCAR-00804", "88888", 804, exception);
        }
    }

    @Override
    public void beginRow() throws SQLException {
        throw new SQLException("JDBC do not support beginRow().");
    }

    @Override
    public void endRow() throws SQLException {
        try {
            int i;
            if (!this.isBegin) {
                this.begin();
            }
            this.checkImportStreamException();
            if (this.multiexectuples != 1) {
                this.importStream.writeChar(68);
                for (i = 0; i < this.rowData.length; ++i) {
                    if (this.rowData[i] == null) {
                        NullData.preWirte(this.importStream, this.nullDataProcessMode);
                        continue;
                    }
                    this.rowData[i].preWrite(this.importStream);
                }
            }
            if (this.hasStreamData) {
                for (i = 0; i < this.rowData.length; ++i) {
                    if (this.rowData[i] != null) {
                        this.rowData[i].endWrite(this.importStream);
                    }
                    this.hasStreamData = false;
                }
            }
            this.importStream.setRowPosition();
        }
        catch (SQLException ose) {
            throw ose;
        }
        catch (Exception e1) {
            Driver.writeLog(e1);
            if (e1.getMessage().contains("OutOfMemoryError")) {
                throw new OSQLException("OSCAR-00800", "88888", 800, e1.getMessage(), e1);
            }
            if (e1.getCause() != null && e1.getCause() instanceof SQLException) {
                throw (SQLException)e1.getCause();
            }
            throw new OSQLException("OSCAR-00804", "88888", 804, e1.getMessage(), e1);
        }
        finally {
            try {
                this.clearRow();
            }
            catch (SQLException e) {
                throw new OSQLException("OSCAR-00804", "88888", 804, e);
            }
        }
    }

    @Override
    public boolean execute() throws SQLException {
        if (this.hasRowNotSubmitted()) {
            this.endRow();
        }
        return this.endExecute();
    }

    protected boolean endExecute() throws SQLException {
        if (this.importStream != null) {
            this.importStream.flush();
            this.importStream.finished();
        }
        this.checkImportStreamException();
        this.isBegin = false;
        return true;
    }

    public void setRowDataByIndex(int i, ColumnData data) {
        this.rowData[i] = data;
    }

    public void setRowDataByIndex(int i, byte[] b) throws SQLException {
        if (b != null && b.length > 32765) {
            BufferedInputStream byteStream = new BufferedInputStream(new ByteArrayInputStream(b));
            this.setRowDataByIndex(i, byteStream, (long)b.length);
            return;
        }
        this.byteData = (ByteData)this.factory.getByteData();
        if (this.isBinaryColumn(i)) {
            this.byteData.setBuffer(b, true);
        } else {
            this.byteData.setBuffer(b);
        }
        this.rowData[i] = this.byteData;
    }

    public void setRowDataByIndex(int i, InputStream in, long length) throws SQLException {
        this.declareHasStreamData();
        this.streamData = (StreamData)this.factory.getStreamData();
        if (length > Integer.MAX_VALUE) {
            if (this.isBinaryColumn(i)) {
                this.streamData.read(in, 0, true);
            } else {
                this.streamData.read(in, 0);
            }
        } else if (this.isBinaryColumn(i)) {
            this.streamData.read(in, (int)length, true);
        } else {
            this.streamData.read(in, (int)length);
        }
        this.rowData[i] = this.streamData;
    }

    public void setRowDataByIndex(int i, InputStream in) throws SQLException {
        this.declareHasStreamData();
        this.streamData = (StreamData)this.factory.getStreamData();
        if (this.isBinaryColumn(i)) {
            this.streamData.read(in, 0, true);
        } else {
            this.streamData.read(in, 0);
        }
        this.rowData[i] = this.streamData;
    }

    private void declareHasStreamData() {
        if (!this.hasStreamData) {
            this.hasStreamData = true;
        }
    }

    public void setRowDataByIndex(int i, Reader reader) throws SQLException {
        this.setRowDataByIndex(i, reader, 0L);
    }

    public void setRowDataByIndex(int i, Reader reader, long length) throws SQLException {
        this.declareHasStreamData();
        this.streamData = (StreamData)this.factory.getStreamData();
        if (length > Integer.MAX_VALUE) {
            this.streamData.read(reader, 0, this.connection.getEncoding().getEncoding());
        } else {
            this.streamData.read(reader, (int)length, this.connection.getEncoding().getEncoding());
        }
        this.rowData[i] = this.streamData;
    }

    public ColumnData getRowDataByIndex(int index) {
        return this.rowData[index];
    }

    public void checkClosed() throws SQLException {
        if (this.isClosed) {
            throw new OSQLException("OSCAR-00409", "88888", 409);
        }
    }

    @Override
    public void clearRow() throws SQLException {
        for (int i = 0; i < this.rowData.length; ++i) {
            if (this.rowData[i] == null) continue;
            this.rowData[i].clear();
            this.rowData[i] = null;
        }
        this.factory.reset();
        this.hasNotSubmittedNullValueOfRow = false;
    }

    @Override
    public void addWarning(String msg, String code) {
        if (this.warnings != null) {
            this.warnings.setNextWarning(new SQLWarning(msg, code));
        } else {
            this.warnings = new SQLWarning(msg);
        }
    }

    @Override
    public void addWarning(SQLWarning sw) {
        if (this.warnings != null) {
            this.warnings.setNextWarning(sw);
        } else {
            this.warnings = sw;
        }
    }

    public StringBuffer getInsertBulkStr() {
        return this.insertBulkStr;
    }

    public void setInsertBulkInfo(StringBuffer insertBulkStr, int columnCount) {
        this.insertBulkStr = insertBulkStr;
        this.rowData = new ColumnData[columnCount];
    }

    public String getHintParam() {
        return this.hintParam;
    }

    @Override
    public ImportStream getImportStream() {
        return this.importStream;
    }

    @Override
    public void setBufferProcessMode(boolean mode) {
        this.writeWithoutRowCache = mode;
    }

    @Override
    public boolean writeWithRowCache() {
        return !this.writeWithoutRowCache;
    }

    public int getBatchRowsOffset() {
        return this.importStream.getBatchRowsOffset();
    }

    public int getBatchRowsEnd() {
        return this.importStream.getBatchRowsEnd();
    }

    public String getCurrentSchema() throws SQLException {
        String sql = "select current_schema();";
        ResultSet rs = null;
        String currentSchema = null;
        Statement stmt = null;
        try {
            stmt = this.connection.createStatement();
            stmt.setFetchSize(0);
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                currentSchema = rs.getString(1);
            }
        }
        catch (SQLException ex) {
            throw ex;
        }
        finally {
            if (stmt != null) {
                stmt.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
        return currentSchema;
    }

    @Override
    public int getImportBlockParam() {
        return this.multiexectuples;
    }

    @Override
    public void setImportBlockParam(int multiexectuples) {
        this.multiexectuples = multiexectuples;
    }

    private boolean isBinaryColumn(int columnIndex) throws SQLException {
        if (!this.connection.sendBinaryTypeAsHex()) {
            return false;
        }
        String columnType = this.columnIndexTypesMap.get(columnIndex);
        if (columnType == null) {
            throw new SQLException("\u627e\u4e0d\u5230\u5bf9\u5e94\u5217\uff0c\u5217\u7d22\u5f15\uff1a" + columnIndex);
        }
        return (columnType = columnType.toLowerCase()).startsWith("varbinary") || columnType.startsWith("binary");
    }

    public int getBulkKind() {
        return this.bulkKind;
    }

    public void setBulkKind(int bulkKind) {
        this.bulkKind = bulkKind;
    }

    public void setBlob(int i, InputStream x, long length) throws SQLException {
        this.setRowDataByIndex(i - 1, x, length);
    }

    protected boolean hasRowNotSubmitted() {
        if (this.hasNotSubmittedNullValueOfRow) {
            return true;
        }
        if (this.rowData != null) {
            for (ColumnData cd : this.rowData) {
                if (cd == null) continue;
                return true;
            }
        }
        return false;
    }
}

