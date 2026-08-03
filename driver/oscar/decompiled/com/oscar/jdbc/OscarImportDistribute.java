/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseConnection;
import com.oscar.core.Encoding;
import com.oscar.core.ImportHandler;
import com.oscar.jdbc.MetaData;
import com.oscar.jdbc.OscarBlob;
import com.oscar.jdbc.OscarClob;
import com.oscar.jdbc.OscarImportHandler;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.protocol.ProtocolTypeConverter;
import com.oscar.util.Bucket;
import com.oscar.util.ByteData;
import com.oscar.util.ColumnData;
import com.oscar.util.ColumnDataFactory;
import com.oscar.util.HashPartitionMap;
import com.oscar.util.ImportStream;
import com.oscar.util.NullData;
import com.oscar.util.OSQLException;
import com.oscar.util.StreamData;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class OscarImportDistribute
implements ImportHandler {
    private MetaData metadata;
    private HashPartitionMap hashPartitionMap;
    private HashMap distributeHandlers;
    private HashMap realHandlers;
    private HashMap buckets;
    private Connection conn;
    private String tableName;
    public ColumnData[] rowData;
    public boolean isClosed = false;
    private Encoding encoding = null;
    private ImportHandler currentHandler = null;
    private int columnCount = 0;
    private int columnPosition = 0;
    private String userName;
    private String password;
    public ColumnDataFactory factory = new ColumnDataFactory();

    public OscarImportDistribute(Connection conn, String tableName, String userName, String password) throws SQLException {
        this.conn = conn;
        this.tableName = tableName;
        this.encoding = Encoding.getEncoding("UTF8");
        this.userName = userName;
        this.password = password;
        this.initImportHandlers();
        this.initImportBuckets();
        this.rowData = new ColumnData[this.columnCount];
    }

    public void initImportHandlers() throws SQLException {
        this.metadata = new MetaData(this.conn);
        this.hashPartitionMap = new HashPartitionMap(8);
        int tableId = this.hashPartitionMap.getTableId(this.metadata, this.tableName);
        this.hashPartitionMap.initBucketCount(this.metadata, tableId);
        this.columnCount = this.hashPartitionMap.getColumnCount(this.metadata, tableId);
        this.columnPosition = this.hashPartitionMap.getColumnPosition(this.metadata, tableId) - 1;
        this.hashPartitionMap.readMapFromMetadataDB(this.metadata, tableId);
        this.realHandlers = new HashMap();
        this.distributeHandlers = new HashMap();
        ArrayList[] mappingTable = this.hashPartitionMap.getMappingTable();
        int length = mappingTable.length;
        Connection oconn = null;
        ImportHandler handler = null;
        for (int i = 0; i < length; ++i) {
            ArrayList<ImportHandler> handlerList = new ArrayList<ImportHandler>(0);
            Iterator it = mappingTable[i].iterator();
            while (it.hasNext()) {
                String url = this.metadata.getUrl((Integer)it.next());
                if (this.realHandlers.containsKey(url)) {
                    handler = (ImportHandler)this.realHandlers.get(url);
                } else {
                    oconn = DriverManager.getConnection(url, this.userName, this.password);
                    oconn.setAutoCommit(false);
                    handler = ((OscarJdbc2Connection)oconn).createImportHandler(this.tableName);
                    this.realHandlers.put(url, handler);
                }
                handlerList.add(handler);
            }
            this.distributeHandlers.put(new Integer(i), handlerList);
        }
    }

    public void initImportBuckets() {
        this.buckets = new HashMap();
        ArrayList[] mappingTable = this.hashPartitionMap.getMappingTable();
        int length = mappingTable.length;
        Bucket bucket = null;
        for (int i = 0; i < length; ++i) {
            bucket = new Bucket(i, mappingTable[i].size());
            this.buckets.put(new Integer(i), bucket);
        }
    }

    public void addWarning(String msg, String code) {
    }

    public void addWarning(SQLWarning sw) {
    }

    public void cancel() throws SQLException {
        Set keys = this.realHandlers.entrySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            ImportHandler handler = (ImportHandler)it.next().getValue();
            if (!((OscarImportHandler)handler).isBegin()) continue;
            handler.cancel();
        }
    }

    public void setCurrentHandler(int hashValue) {
        Integer key = new Integer(hashValue);
        Bucket bucket = (Bucket)this.buckets.get(key);
        bucket.increase();
        int countSize = bucket.getCurrentSize();
        ArrayList handlerList = (ArrayList)this.distributeHandlers.get(key);
        this.currentHandler = (ImportHandler)handlerList.get(countSize);
    }

    public void clearRow() throws SQLException {
        for (int i = 0; i < this.rowData.length; ++i) {
            if (this.rowData[i] == null) continue;
            this.rowData[i].clear();
        }
        this.rowData = new ColumnData[this.columnCount];
        this.factory.reset();
    }

    public void clearWarnings() throws SQLException {
        Set keys = this.realHandlers.entrySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            ImportHandler handler = (ImportHandler)it.next().getValue();
            if (!((OscarImportHandler)handler).isBegin()) continue;
            handler.clearWarnings();
        }
    }

    public void close() throws SQLException {
        Set keys = this.realHandlers.entrySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            ImportHandler handler = (ImportHandler)it.next().getValue();
            if (!((OscarImportHandler)handler).isBegin()) continue;
            handler.close();
        }
    }

    public void endRow() throws SQLException {
        int i;
        int hashValue = this.hashPartitionMap.hash(((BaseConnection)((Object)this.conn)).getEncoding().decode(((ByteData)this.rowData[this.columnPosition]).getBuffer()));
        this.setCurrentHandler(hashValue);
        if (!((OscarImportHandler)this.currentHandler).isBegin()) {
            ((OscarImportHandler)this.currentHandler).begin();
        }
        ImportStream importStream = ((OscarImportHandler)this.currentHandler).importStream;
        ((OscarImportHandler)this.currentHandler).checkImportStreamException();
        try {
            importStream.writeChar(68);
        }
        catch (Exception e1) {
            throw new OSQLException("OSCAR-00804", "88888", 804, e1);
        }
        for (i = 0; i < this.rowData.length; ++i) {
            if (this.rowData[i] == null) {
                try {
                    NullData.preWirte(importStream, ((OscarImportHandler)this.currentHandler).nullDataProcessMode);
                    continue;
                }
                catch (SQLException e) {
                    throw new OSQLException("OSCAR-00801", "88888", 801, e);
                }
            }
            try {
                this.rowData[i].preWrite(importStream);
                continue;
            }
            catch (Exception e) {
                throw new OSQLException("OSCAR-00804", "88888", 804, e);
            }
        }
        for (i = 0; i < this.rowData.length; ++i) {
            if (this.rowData[i] == null) continue;
            try {
                this.rowData[i].endWrite(importStream);
                continue;
            }
            catch (Exception e) {
                throw new OSQLException("OSCAR-00804", "88888", 804, e);
            }
        }
        importStream.setRowPosition();
        try {
            this.clearRow();
        }
        catch (SQLException e) {
            throw new OSQLException("OSCAR-00804", "88888", 804, e);
        }
    }

    public boolean execute() throws SQLException {
        Set keys = this.realHandlers.entrySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            ImportHandler handler = (ImportHandler)it.next().getValue();
            handler.execute();
            ((Connection)((Object)handler.getConnection())).commit();
        }
        return true;
    }

    public void rollback() throws SQLException {
        Set keys = this.realHandlers.entrySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            ImportHandler handler = (ImportHandler)it.next().getValue();
            ((Connection)((Object)handler.getConnection())).rollback();
        }
    }

    public String getColumnOrder() {
        return null;
    }

    public BaseConnection getConnection() throws SQLException {
        throw new SQLException("not support the method!");
    }

    public ColumnData[] getRowData() {
        return this.rowData;
    }

    public int getUpdateCount() throws SQLException {
        int updateCount = 0;
        Set keys = this.realHandlers.entrySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            ImportHandler handler = (ImportHandler)it.next().getValue();
            int count = handler.getUpdateCount();
            if (count <= 0) continue;
            updateCount += count;
        }
        return updateCount;
    }

    public SQLWarning getWarnings() throws SQLException {
        throw new OSQLException("OSCAR-00810", "88888", 810);
    }

    public void checkClosed() throws SQLException {
        if (this.isClosed) {
            throw new OSQLException("OSCAR-00409", "88888", 409);
        }
    }

    public boolean checkIndexAndSQLType(int i, int type) {
        return true;
    }

    public void setRowDataByIndex(int i, byte[] b) throws SQLException {
        ByteData data = (ByteData)this.factory.getByteData();
        data.setBuffer(b);
        this.rowData[i] = data;
    }

    public void setRowDataByIndex(int i, InputStream in) throws SQLException {
        StreamData data = (StreamData)this.factory.getStreamData();
        data.read(in, 0);
        this.rowData[i] = data;
    }

    public void setRowDataByIndex(int i, InputStream in, long length) throws SQLException {
        StreamData data = (StreamData)this.factory.getStreamData();
        if (length > Integer.MAX_VALUE) {
            data.read(in, 0);
        } else {
            data.read(in, (int)length);
        }
        this.rowData[i] = data;
    }

    public void setArray(int i, Array x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 2003);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setAsciiStream(int i, InputStream x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 1111);
        this.setRowDataByIndex(i - 1, x);
    }

    public void setBigDecimal(int i, BigDecimal x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 2);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setBinaryStream(int i, InputStream x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 1111);
        this.setRowDataByIndex(i - 1, x);
    }

    public void setBinaryStream(int i, InputStream x, long length) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 1111);
        this.setRowDataByIndex(i - 1, x, length);
    }

    public void setBlob(int i, Blob x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 2004);
        this.setRowDataByIndex(i - 1, ((OscarBlob)x).getBinaryStream());
    }

    public void setBoolean(int i, boolean x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, -7);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setBufferSize(int size) {
        Set keys = this.realHandlers.entrySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            ImportHandler handler = (ImportHandler)it.next().getValue();
            handler.setBufferSize(size);
        }
    }

    public void setByte(int i, byte x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, -6);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setBytes(int i, byte[] x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, -3);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setClob(int i, Clob x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 2005);
        this.setRowDataByIndex(i - 1, ((OscarClob)x).getAsciiStream());
    }

    public void setColumnOrder(String columnOrder) throws SQLException {
        Set keys = this.realHandlers.entrySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            ImportHandler handler = (ImportHandler)it.next().getValue();
            handler.setColumnOrder(columnOrder);
        }
    }

    public void setDate(int i, Date x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 91);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setDate(int i, Date x, Calendar cal) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 91);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setDouble(int i, double x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 8);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setFloat(int i, float x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 6);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setHintParam(String hintParam) throws SQLException {
        Set keys = this.realHandlers.entrySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            ImportHandler handler = (ImportHandler)it.next().getValue();
            handler.setHintParam(hintParam);
        }
    }

    public void setInt(int i, int x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 4);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setLong(int i, long x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, -5);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setNull(int i, int sqlType) throws SQLException {
        this.checkClosed();
    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        this.checkClosed();
    }

    public void setNullDataProcessMode(int mode) throws SQLException {
        Set keys = this.realHandlers.entrySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            ImportHandler handler = (ImportHandler)it.next().getValue();
            handler.setNullDataProcessMode(mode);
        }
    }

    public void setObject(int i, Object parameterObj) throws SQLException {
        this.setObject(i, parameterObj);
    }

    public void setObject(int i, Object x, int targetSqlType) throws SQLException {
        this.setObject(i, x);
    }

    public void setShort(int i, short x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 5);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setString(int i, char[] x) throws SQLException {
        this.setString(i, String.valueOf(x));
    }

    public void setString(int i, String x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 12);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setTime(int i, Time x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 92);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setTime(int i, Time x, Calendar cal) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 92);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setTimestamp(int i, Timestamp x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 93);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setTimestamp(int i, Timestamp x, Calendar cal) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 93);
        this.setRowDataByIndex(i - 1, ProtocolTypeConverter.convertToServer(x, this.encoding));
    }

    public void setUnicodeStream(int i, InputStream x) throws SQLException {
        this.checkClosed();
        this.checkIndexAndSQLType(i, 1111);
        this.setRowDataByIndex(i - 1, x);
    }

    public ImportStream getImportStream() {
        try {
            int hashValue = this.hashPartitionMap.hash(((BaseConnection)((Object)this.conn)).getEncoding().decode(((ByteData)this.rowData[this.columnPosition]).getBuffer()));
            this.setCurrentHandler(hashValue);
            return this.currentHandler.getImportStream();
        }
        catch (SQLException e) {
            return null;
        }
    }

    public boolean writeWithRowCache() {
        return true;
    }

    public void setBufferProcessMode(boolean mode) {
    }

    public void beginRow() throws SQLException {
        throw new SQLException("JDBC do not support beginRow().");
    }

    public void setNull(int i) throws SQLException {
    }

    public int getImportBlockParam() {
        return 0;
    }

    public void setImportBlockParam(int multiexectuples) {
    }

    public void setCharacterStream(int i, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setCharacterStream(int i, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();
    }
}

