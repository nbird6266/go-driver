/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseConnection;
import com.oscar.core.ExportBinaryCallback;
import com.oscar.core.ExportHandler;
import com.oscar.core.ExportObjectCallback;
import com.oscar.core.ExportStringCallback;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.jdbc.OscarResultSet;
import com.oscar.util.OSQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class OscarExportHandler
implements ExportHandler {
    protected BaseConnection conn;
    protected PreparedStatement pstmt;
    private int fetchSize = 0;
    private int exportCount = 0;

    public OscarExportHandler(BaseConnection conn) {
        this.conn = conn;
    }

    public void prepareExport(String sql) throws SQLException {
        this.pstmt = ((OscarJdbc2Connection)this.conn).prepareStatement(sql);
        this.pstmt.setFetchSize(this.fetchSize);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void executeExport(ExportBinaryCallback binaryCallback) throws SQLException {
        this.exportCount = 0;
        ResultSet rs = null;
        int colIndex = 0;
        int rowNum = 0;
        byte[][] row = null;
        int length = 0;
        try {
            try {
                this.checkExport();
                rs = this.pstmt.executeQuery();
                if (binaryCallback.hasHeadColumn()) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
                        String columnName = rsmd.getColumnName(i);
                        binaryCallback.processColumn(++colIndex, 1, columnName.getBytes());
                        binaryCallback.processEndColumn();
                    }
                    binaryCallback.processEndRow(++rowNum);
                }
                int i = 0;
                while (rs.next()) {
                    row = ((OscarResultSet)rs).getCurrentRow();
                    length = row.length;
                    while (i < length - 1) {
                        if (row[i] != null) {
                            binaryCallback.processColumn(++colIndex, 1, row[i]);
                        }
                        binaryCallback.processEndColumn();
                        ++i;
                    }
                    if (row[i] != null) {
                        binaryCallback.processColumn(++colIndex, 1, row[i]);
                    }
                    binaryCallback.processEndRow(++rowNum);
                    colIndex = 0;
                    ++this.exportCount;
                    i = 0;
                }
                binaryCallback.processEnd();
            }
            catch (Exception e) {
                throw new OSQLException("OSCAR-00806", "88888", 107, e);
            }
            Object var11_12 = null;
        }
        catch (Throwable throwable) {
            Object var11_13 = null;
            try {
                if (rs != null) {
                    rs.close();
                }
                if (this.pstmt == null) throw throwable;
                this.pstmt.close();
                throw throwable;
            }
            catch (SQLException e) {
                // empty catch block
            }
            throw throwable;
        }
        try {}
        catch (SQLException e) {}
        if (rs != null) {
            rs.close();
        }
        if (this.pstmt == null) return;
        this.pstmt.close();
        return;
    }

    public void checkExport() throws SQLException {
        if (this.pstmt == null) {
            throw new OSQLException("OSCAR-00805", "88888", 107);
        }
        this.exportCount = 0;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void executeExport(ExportStringCallback stringCallback) throws SQLException {
        this.exportCount = 0;
        ResultSet rs = null;
        int colIndex = 0;
        int rowNum = 0;
        byte[][] row = null;
        int length = 0;
        try {
            try {
                this.checkExport();
                rs = this.pstmt.executeQuery();
                if (stringCallback.hasHeadColumn()) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
                        String columnName = rsmd.getColumnName(i);
                        stringCallback.processColumn(++colIndex, 2, columnName);
                        stringCallback.processEndColumn();
                    }
                    stringCallback.processEndRow(++rowNum);
                }
                int i = 0;
                while (rs.next()) {
                    row = ((OscarResultSet)rs).getCurrentRow();
                    length = row.length;
                    while (i < length - 1) {
                        if (row[i] != null) {
                            if (this.conn.getProtocolVersion().getProtocolType() >= 2) {
                                stringCallback.processColumn(++colIndex, 2, rs.getObject(i + 1).toString());
                            } else {
                                stringCallback.processColumn(++colIndex, 2, this.conn.getEncoding().decode(row[i]));
                            }
                        }
                        stringCallback.processEndColumn();
                        ++i;
                    }
                    if (row[i] != null) {
                        if (this.conn.getProtocolVersion().getProtocolType() >= 2) {
                            stringCallback.processColumn(++colIndex, 2, rs.getObject(i + 1).toString());
                        } else {
                            stringCallback.processColumn(++colIndex, 2, this.conn.getEncoding().decode(row[i]));
                        }
                    }
                    stringCallback.processEndRow(++rowNum);
                    colIndex = 0;
                    ++this.exportCount;
                    i = 0;
                }
                stringCallback.processEnd();
            }
            catch (Exception e) {
                throw new OSQLException("OSCAR-00806", "88888", 107, e);
            }
            Object var11_12 = null;
        }
        catch (Throwable throwable) {
            Object var11_13 = null;
            try {
                if (rs != null) {
                    rs.close();
                }
                if (this.pstmt == null) throw throwable;
                this.pstmt.close();
                throw throwable;
            }
            catch (SQLException e) {
                // empty catch block
            }
            throw throwable;
        }
        try {}
        catch (SQLException e) {}
        if (rs != null) {
            rs.close();
        }
        if (this.pstmt == null) return;
        this.pstmt.close();
        return;
    }

    public void executeExport(ExportObjectCallback objectCallback) {
    }

    public void close() throws SQLException {
        this.conn = null;
        this.pstmt.close();
    }

    public int getFetchSize() {
        return this.fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public long getExportCount() {
        return this.exportCount;
    }
}

