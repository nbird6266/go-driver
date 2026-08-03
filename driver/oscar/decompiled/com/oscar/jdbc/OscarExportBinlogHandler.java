/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseConnection;
import com.oscar.core.ExportBinlogHandler;
import com.oscar.jdbc.BlogResultSet;
import com.oscar.jdbc.ExceptionUtil;
import com.oscar.protocol.packets.QueryPacket;
import com.oscar.util.OSQLException;
import java.sql.SQLException;

public class OscarExportBinlogHandler
implements ExportBinlogHandler {
    BaseConnection conn = null;
    String startFile = null;
    long startPos;
    String stopFile = null;
    long stopPos;
    int exportFileSize = 10;
    String exportSQL = "BINLOG EXPORT";
    String exportFile = "tmp.txt";

    public OscarExportBinlogHandler(BaseConnection conn) {
        this.conn = conn;
    }

    public OscarExportBinlogHandler(BaseConnection conn, String exportFile, int exportFileSize) {
        this.conn = conn;
        this.exportFile = exportFile;
        this.exportFileSize = exportFileSize;
    }

    public OscarExportBinlogHandler(BaseConnection conn, String startFile, long startPos, String stopFile, long stopPos, int exportFileSize) {
        this.conn = conn;
        this.startFile = startFile;
        this.startPos = startPos;
        this.stopFile = stopFile;
        this.stopPos = stopPos;
        this.exportFileSize = exportFileSize;
    }

    public OscarExportBinlogHandler(BaseConnection conn, String startFile, long startPos, String stopFile, long stopPos, String exportFile, int exportFileSize) {
        this.conn = conn;
        this.startFile = startFile;
        this.startPos = startPos;
        this.stopFile = stopFile;
        this.stopPos = stopPos;
        this.exportFile = exportFile;
        this.exportFileSize = exportFileSize;
    }

    public BlogResultSet exportBinlogData() throws OSQLException, Exception {
        BlogResultSet brs = null;
        this.initParams();
        try {
            this.conn.getProtocol().setExportBlogHandler(this);
            QueryPacket qp = new QueryPacket(this.exportSQL.getBytes(), 0);
            brs = this.conn.getProtocol().queryBlogData(qp);
        }
        catch (OSQLException e) {
            this.checkConnectionClosed(e);
            throw e;
        }
        catch (Exception e) {
            this.checkConnectionClosed(e);
            throw e;
        }
        return brs;
    }

    private void initParams() {
        if (this.startFile != null) {
            this.exportSQL = this.exportSQL + " STARTFILE '" + this.startFile + "'";
        }
        if (this.startPos != 0L) {
            this.exportSQL = this.exportSQL + " STARTPOS " + this.startPos;
        }
        if (this.stopFile != null) {
            this.exportSQL = this.exportSQL + " STOPFILE '" + this.stopFile + "'";
        }
        if (this.stopPos != 0L) {
            this.exportSQL = this.exportSQL + " STOPPOS " + this.stopPos;
        }
        this.exportSQL = this.exportSQL + " FILESIZE " + this.exportFileSize + "M";
    }

    public BaseConnection getConnection() throws SQLException {
        return this.conn;
    }

    public void setStartFile(String startFile) {
        this.startFile = startFile;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public void setStopFile(String stopFile) {
        this.stopFile = stopFile;
    }

    public void setStopPos(int stopPos) {
        this.stopPos = stopPos;
    }

    public String getExportFile() {
        return this.exportFile;
    }

    public long getStartPos() {
        return this.startPos;
    }

    public void close() {
        this.exportFile = null;
        this.exportSQL = null;
        this.startFile = null;
        this.stopFile = null;
        this.startPos = 0L;
        this.stopPos = 0L;
    }

    void checkConnectionClosed(Exception e) {
        switch (ExceptionUtil.isConnectionClosed(e)) {
            case 1: {
                this.close();
                try {
                    this.conn.close();
                }
                catch (SQLException e1) {
                    e1.printStackTrace();
                }
                break;
            }
            case 2: {
                try {
                    if (this.conn.isClosed()) {
                        return;
                    }
                }
                catch (SQLException e2) {
                    e2.printStackTrace();
                }
                this.close();
                try {
                    this.conn.close();
                }
                catch (SQLException e1) {
                    e1.printStackTrace();
                }
                break;
            }
        }
    }
}

