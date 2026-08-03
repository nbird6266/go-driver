/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.Encoding;
import com.oscar.core.ExportDistribute;
import com.oscar.core.ExportHandler;
import com.oscar.jdbc.CSVExportStringCallback;
import com.oscar.jdbc.MetaData;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.util.Bucket;
import com.oscar.util.HashPartitionMap;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class OscarExportDistribute
implements ExportDistribute {
    private MetaData metadata;
    private HashPartitionMap hashPartitionMap;
    private HashMap distributeHandlers;
    private HashMap realHandlers;
    private HashMap buckets;
    private Connection conn;
    private String tableName;
    public boolean isClosed = false;
    private Encoding encoding = null;
    private int columnCount = 0;
    private int columnPosition = 0;
    private String userName;
    private String password;
    private int exportSize = 1000;

    public OscarExportDistribute(Connection conn, String tableName, String userName, String password) throws SQLException {
        this.conn = conn;
        this.tableName = tableName;
        this.encoding = Encoding.getEncoding("UTF8");
        this.userName = userName;
        this.password = password;
        this.initExportHandlers();
        this.initExportBuckets();
    }

    public void initExportHandlers() throws SQLException {
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
        ExportHandler handler = null;
        for (int i = 0; i < length; ++i) {
            ArrayList<ExportHandler> handlerList = new ArrayList<ExportHandler>(0);
            Iterator it = mappingTable[i].iterator();
            while (it.hasNext()) {
                String url = this.metadata.getUrl((Integer)it.next());
                if (this.realHandlers.containsKey(url)) {
                    handler = (ExportHandler)this.realHandlers.get(url);
                } else {
                    oconn = DriverManager.getConnection(url, this.userName, this.password);
                    oconn.setAutoCommit(false);
                    handler = ((OscarJdbc2Connection)oconn).createExportHandler();
                    handler.setFetchSize(this.exportSize);
                    this.realHandlers.put(url, handler);
                }
                handlerList.add(handler);
            }
            this.distributeHandlers.put(new Integer(i), handlerList);
        }
    }

    public void initExportBuckets() {
        this.buckets = new HashMap();
        ArrayList[] mappingTable = this.hashPartitionMap.getMappingTable();
        int length = mappingTable.length;
        Bucket bucket = null;
        for (int i = 0; i < length; ++i) {
            bucket = new Bucket(i, mappingTable[i].size());
            this.buckets.put(new Integer(i), bucket);
        }
    }

    public void close() throws SQLException {
        Set keys = this.realHandlers.entrySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            ExportHandler handler = (ExportHandler)it.next().getValue();
            handler.close();
        }
    }

    public long getExportCount() {
        long exportCount = 0L;
        Set keys = this.realHandlers.entrySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            ExportHandler handler = (ExportHandler)it.next().getValue();
            long count = handler.getExportCount();
            if (count <= 0L) continue;
            exportCount += count;
        }
        return exportCount;
    }

    public void setFetchSize(int fetchSize) {
        this.exportSize = fetchSize;
    }

    public void executeExport(String fileName, String colSep, String rowSep, Character escapeChar, boolean append) throws SQLException {
        try {
            boolean isFirst = true;
            Set keys = this.realHandlers.entrySet();
            Iterator it = keys.iterator();
            while (it.hasNext()) {
                ExportHandler handler = (ExportHandler)it.next().getValue();
                handler.prepareExport("select * from " + this.tableName);
                if (isFirst) {
                    isFirst = false;
                    handler.executeExport(new CSVExportStringCallback(fileName, colSep, rowSep, escapeChar, append));
                    continue;
                }
                handler.executeExport(new CSVExportStringCallback(fileName, colSep, rowSep, escapeChar, true));
            }
        }
        catch (IOException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public void executeExport(String fileName, String colSep, String rowSep, boolean append) throws SQLException {
        try {
            boolean isFirst = true;
            Set keys = this.realHandlers.entrySet();
            Iterator it = keys.iterator();
            while (it.hasNext()) {
                ExportHandler handler = (ExportHandler)it.next().getValue();
                handler.prepareExport("select * from " + this.tableName);
                if (isFirst) {
                    isFirst = false;
                    handler.executeExport(new CSVExportStringCallback(fileName, colSep, rowSep, append));
                    continue;
                }
                handler.executeExport(new CSVExportStringCallback(fileName, colSep, rowSep, true));
            }
        }
        catch (IOException e) {
            throw new SQLException(e.getMessage());
        }
    }
}

