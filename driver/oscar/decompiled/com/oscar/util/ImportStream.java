/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.Driver;
import com.oscar.core.ImportHandler;
import com.oscar.jdbc.OscarImportHandler;
import java.sql.SQLException;

public class ImportStream {
    public int cacheSize = 0;
    public volatile int position = 0;
    public volatile int rowPosition = 0;
    public int updateCount = 0;
    public byte[] integerBuf = new byte[4];
    public byte[] singleBuf = new byte[1];
    public volatile boolean exFinished = false;
    public Throwable threadException = null;
    public int defaultBufferSize = 0xA00000;
    public volatile OscarImportHandler handler;
    public String currentSql = null;
    public volatile boolean finished = false;

    public void reInit() {
    }

    public boolean isSufficient(int len) {
        if (len + this.position - this.rowPosition > this.defaultBufferSize) {
            Driver.writeLog("-----len + position - rowPosition = len:::" + len + "+" + this.position + "-" + this.rowPosition + "=" + (len + this.position - this.rowPosition) + "/" + this.defaultBufferSize);
            return false;
        }
        return true;
    }

    public void write(byte[] b, int off, int len) throws SQLException {
    }

    public void sendMessage(byte[] data) throws SQLException {
    }

    public boolean isFinished() {
        return this.finished;
    }

    public int write() throws SQLException {
        return 0;
    }

    public void write(int b) throws SQLException {
    }

    public void setRowPosition() {
        this.rowPosition = this.position;
    }

    public ImportHandler getHandler() {
        return this.handler;
    }

    public void writeChar(int c) throws SQLException {
        this.singleBuf[0] = (byte)c;
        this.write(this.singleBuf);
    }

    public void writeInteger(int val, int size) throws SQLException {
        int count = size;
        while (size-- > 0) {
            this.integerBuf[size] = (byte)(val & 0xFF);
            val >>= 8;
        }
        this.write(this.integerBuf, 0, count);
    }

    public void write(byte[] b) throws SQLException {
        this.write(b, 0, b.length);
    }

    public Throwable getThreadException() {
        return this.threadException;
    }

    public int getDefaultBufferSize() {
        return this.defaultBufferSize;
    }

    public void setDefaultBufferSize(int size) {
        this.defaultBufferSize = size;
    }

    public void finished() {
    }

    public void close() throws SQLException {
    }

    public void flush() throws SQLException {
    }

    public void batchRowsIncrease() {
    }

    public int getBatchRowsOffset() {
        return 0;
    }

    public int getBatchRowsEnd() {
        return 0;
    }
}

