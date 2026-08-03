/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseConnection;
import com.oscar.core.ImportDataProvider;
import com.oscar.core.ImportHandler;
import com.oscar.core.ProviderImportHandler;
import com.oscar.util.ImportDataContainer;
import com.oscar.util.OSQLException;
import java.io.IOException;
import java.sql.SQLException;

public class OscarProviderImportHandle
implements ProviderImportHandler {
    protected BaseConnection conn;
    protected ImportHandler handler;
    private String schemName;
    private String tableName;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public OscarProviderImportHandle(BaseConnection conn, String schemName, String tableName) throws SQLException {
        BaseConnection baseConnection = this.conn = conn;
        synchronized (baseConnection) {
            this.schemName = schemName;
            this.tableName = tableName;
            this.handler = schemName == null ? conn.createImportHandler(tableName) : conn.createImportHandler(schemName, tableName);
        }
    }

    public void setColumnOrder(String columnOrder) throws SQLException {
        this.handler.setColumnOrder(columnOrder);
    }

    public void setNullDataProcessMode(int mode) throws SQLException {
        this.handler.setNullDataProcessMode(mode);
    }

    public OscarProviderImportHandle(BaseConnection conn, String tableName) throws SQLException {
        this(conn, null, tableName);
    }

    public long executeImport(ImportDataProvider dataProvider) throws SQLException {
        int index = 0;
        ImportDataContainer container = new ImportDataContainer();
        try {
            int status;
            if (dataProvider.hasHeadColumn() && dataProvider.nextRow()) {
                status = 0;
                StringBuffer columnStr = null;
                block10: do {
                    status = dataProvider.setNextColumnData(container);
                    switch (status) {
                        case 1: {
                            if (container.getData() == null) continue block10;
                            if (columnStr == null) {
                                columnStr = new StringBuffer();
                            } else {
                                columnStr.append(",");
                            }
                            if (container.getDataType() == 2) {
                                columnStr.append(new String((byte[])container.getData(), dataProvider.getFileEncoding()));
                                break;
                            }
                            if (container.getDataType() != 1) continue block10;
                            columnStr.append((char[])container.getData());
                            break;
                        }
                        case 4: {
                            break;
                        }
                        default: {
                            throw new OSQLException("OSCAR-00809", "88888", 107);
                        }
                    }
                } while (status != 4);
                if (columnStr != null) {
                    this.handler.setColumnOrder(columnStr.toString());
                }
            }
            while (dataProvider.nextRow()) {
                status = 0;
                block12: do {
                    status = dataProvider.setNextColumnData(container);
                    switch (status) {
                        case 1: {
                            if (container.getData() == null) {
                                ++index;
                                break;
                            }
                            if (container.getDataType() == 2) {
                                this.handler.setBytes(++index, this.conn.getEncoding().encode(new String((byte[])container.getData(), dataProvider.getFileEncoding())));
                                break;
                            }
                            if (container.getDataType() != 1) continue block12;
                            this.handler.setString(++index, (char[])container.getData());
                            break;
                        }
                        case 4: {
                            this.handler.endRow();
                            index = 0;
                            break;
                        }
                        default: {
                            throw new OSQLException("OSCAR-00809", "88888", 107);
                        }
                    }
                } while (status != 4);
            }
            dataProvider.close();
        }
        catch (IOException e) {
            throw new SQLException("dataProvider occur error " + e.getMessage());
        }
        boolean success = this.handler.execute();
        if (success) {
            return this.handler.getUpdateCount();
        }
        return 0L;
    }

    public void close() throws SQLException {
        this.handler.close();
        this.conn = null;
    }
}

