/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseConnection;
import com.oscar.core.ImportBinlogHandler;
import com.oscar.jdbc.ExceptionUtil;
import com.oscar.protocol.packets.QueryPacket;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.SQLException;

public class OscarImportBinlogHandler
implements ImportBinlogHandler {
    BaseConnection conn = null;
    int curBinlogPos = 0;
    int failPos = -1;
    String curBinlogFile = null;
    boolean isBegin = false;
    byte[] dataBuffer;
    int defaultBufferSize = 51200;
    String importSQL = "BINLOG IMPORT";
    QueryPacket qp = null;
    int repeatCount = 0;

    public OscarImportBinlogHandler(BaseConnection connection) {
        this.conn = connection;
    }

    public OscarImportBinlogHandler(BaseConnection connection, String importBinlogFile, int curPos) {
        this.conn = connection;
        this.curBinlogFile = importBinlogFile;
        this.curBinlogPos = curPos;
    }

    public BaseConnection getConnection() throws SQLException {
        return this.conn;
    }

    public void importBinlogBegin() throws SQLException {
        if (!this.isBegin) {
            this.qp = new QueryPacket(this.importSQL.getBytes(), 0);
            this.isBegin = true;
        }
        this.conn.getProtocol().setImportBlogHandler(this);
        this.conn.getProtocol().importBlogBegin(this.qp);
    }

    public void execute() throws Exception {
        try {
            this.importBinlogBegin();
            this.importBinlog();
            this.importBinlogEnd();
        }
        catch (Exception e) {
            this.checkConnectionClosed(e);
            throw e;
        }
    }

    /*
     * Loose catch block
     */
    public void importBinlog() throws Exception {
        block22: {
            if (this.curBinlogFile == null) {
                throw new Exception("import binlog file is null.");
            }
            RandomAccessFile fis = null;
            fis = new RandomAccessFile(this.curBinlogFile, "r");
            fis.seek(this.curBinlogPos);
            this.dataBuffer = new byte[this.defaultBufferSize];
            int s = 0;
            long currentSendLen = 0L;
            float per = 0.0f;
            long printDataLen = 0xA00000L;
            int retVal = -1;
            int count = 10;
            while ((s = fis.read(this.dataBuffer, 0, this.defaultBufferSize)) > 0) {
                try {
                    retVal = this.importBinlogData(this.dataBuffer, 0, s);
                    if (fis.length() <= 0xA00000L || (currentSendLen += (long)s) <= printDataLen) continue;
                    per = (float)((double)currentSendLen * 1.0 * 100.0 / (double)fis.length());
                    System.out.println("\t\tImport binlog size " + count + "M" + " ,current progerss of " + this.curBinlogFile + " is: " + per + "%");
                    printDataLen += 0xA00000L;
                    count += 10;
                }
                catch (Exception e) {
                    if (ExceptionUtil.isConnectionClosed(e) != 0) {
                        throw e;
                    }
                    if (retVal != -1) {
                        if (this.repeatCount > 0) {
                            int tmp = this.repeatCount;
                            block12: while (tmp-- > 0) {
                                fis.seek(retVal + this.curBinlogPos);
                                while ((s = fis.read(this.dataBuffer, 0, this.defaultBufferSize)) > 0) {
                                    try {
                                        retVal = this.importBinlogData(this.dataBuffer, 0, s);
                                    }
                                    catch (Exception e2) {
                                        // empty catch block
                                    }
                                    if (retVal == -1) continue;
                                    continue block12;
                                }
                            }
                            if (tmp <= 0) {
                                throw e;
                            }
                        } else {
                            throw e;
                        }
                    }
                    throw e;
                }
            }
            Object var14_12 = null;
            try {
                if (fis != null) {
                    fis.close();
                }
                break block22;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            break block22;
            {
                catch (Exception e) {
                    throw e;
                }
            }
            catch (Throwable throwable) {
                Object var14_13 = null;
                try {
                    if (fis != null) {
                        fis.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                throw throwable;
            }
        }
    }

    public int importBinlogData(byte[] data, int pos, int len) throws SQLException {
        return this.conn.getProtocol().importBinlogData(data, pos, len);
    }

    public void importBinlogEnd() throws SQLException {
        this.conn.getProtocol().importBinlogEnd();
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void close() {
        this.isBegin = false;
        this.curBinlogFile = null;
        this.importSQL = null;
        this.curBinlogPos = 0;
        this.dataBuffer = null;
        this.repeatCount = 0;
        this.qp = null;
        this.failPos = -1;
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

