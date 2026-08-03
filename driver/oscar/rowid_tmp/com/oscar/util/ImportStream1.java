/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.core.ImportHandler;
import com.oscar.jdbc.OscarImportHandler;
import com.oscar.protocol.packets.QueryPacket;
import com.oscar.protocol.stream.OStream;
import com.oscar.util.ImportStream;
import java.sql.SQLException;

public class ImportStream1
extends ImportStream {
    private byte[] buffer;
    public byte[] cache;
    public int bufferOffset = 1;
    public int batchRowCounts = 0;
    public int batchRowsOffset = 1;
    public int batchRowsEnd = 0;
    private ImportDataThread thread;
    private boolean cacheDataFilled = false;
    private boolean ended = false;
    QueryPacket qp = null;
    private Exception exception = null;

    public ImportStream1(OscarImportHandler handler) {
        this.handler = handler;
        this.defaultBufferSize = handler.getBufferSize();
        this.buffer = new byte[this.defaultBufferSize];
        this.cache = new byte[this.defaultBufferSize];
        this.reInit();
    }

    public void reInit() {
        this.cacheSize = 0;
        this.position = 0;
        this.rowPosition = 0;
        this.cacheDataFilled = false;
        this.ended = false;
        this.finished = false;
        this.exFinished = false;
        this.thread = new ImportDataThread();
        this.thread.start();
    }

    public void write(byte[] b, int off, int len) {
        if (off < 0 || len > b.length) {
            throw new NullPointerException();
        }
        if (len + off > b.length) {
            throw new IndexOutOfBoundsException();
        }
        if (this.position + len < this.defaultBufferSize) {
            System.arraycopy(b, off, this.buffer, this.position, len);
            this.position += len;
        } else {
            this.flush();
            System.arraycopy(b, off, this.buffer, this.position, len);
            this.position += len;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sendMessage(byte[] data) throws SQLException {
        this.currentSql = this.handler.getInsertBulkStr().toString();
        if (this.handler.getHintParam() != null) {
            this.currentSql = this.currentSql + " WITH " + this.handler.getHintParam();
        }
        try {
            this.qp = new QueryPacket(this.handler.getConnection().getEncoding().encode(this.currentSql), 0);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        OStream oStream = this.handler.getConnection().getProtocol().oStream;
        synchronized (oStream) {
            this.handler.getConnection().getProtocol().setImportHandler(this.handler);
            this.handler.getConnection().getProtocol().importBegin(this.qp);
            if (this.handler.getImportBlockParam() == 1) {
                byte[] dataLen = new byte[]{(byte)(data.length >> 24 & 0xFF), (byte)(data.length >> 16 & 0xFF), (byte)(data.length >> 8 & 0xFF), (byte)(data.length & 0xFF)};
                this.handler.getConnection().getProtocol().importData(dataLen);
            }
            this.handler.getConnection().getProtocol().importData(data);
            this.handler.getConnection().getProtocol().importEnd();
        }
        this.updateCount += this.handler.getUpdateCount();
        this.handler.setUpdateCount(this.updateCount);
    }

    public synchronized byte[] getMessage() {
        byte[] data = null;
        while (true) {
            if (this.cacheDataFilled) {
                data = new byte[this.cacheSize];
                System.arraycopy(this.cache, 0, data, 0, this.cacheSize);
                this.cacheDataFilled = false;
                this.notify();
                break;
            }
            if (this.finished) break;
            try {
                this.wait();
            }
            catch (InterruptedException interruptedException) {}
        }
        return data;
    }

    public synchronized void setMessage() {
        while (!this.ended) {
            if (!this.cacheDataFilled) {
                System.arraycopy(this.buffer, 0, this.cache, 0, this.rowPosition);
                this.cacheSize = this.rowPosition;
                this.moveData(this.buffer, 0, this.rowPosition);
                this.cacheDataFilled = true;
                this.batchRowsOffset = this.bufferOffset;
                this.batchRowsEnd = this.batchRowCounts;
                this.bufferOffset = this.batchRowCounts + 1;
                this.notify();
                break;
            }
            try {
                this.wait();
            }
            catch (InterruptedException interruptedException) {}
        }
    }

    public boolean isFinished() {
        return this.finished;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void finished() {
        this.finished = true;
        while (!this.ended && !this.exFinished) {
            ImportStream1 importStream1 = this;
            synchronized (importStream1) {
                this.notify();
            }
            try {
                Thread.sleep(1L);
            }
            catch (InterruptedException interruptedException) {}
        }
    }

    public void flush() {
        this.setMessage();
    }

    public void moveData(byte[] bytes, int offset, int length) {
        System.arraycopy(this.buffer, length, this.buffer, 0, this.position - length);
        this.position -= length;
        this.rowPosition = 0;
    }

    public void moveData(byte[] bytes) throws SQLException {
        this.moveData(bytes, 0, bytes.length);
    }

    public void setRowPosition() {
        this.rowPosition = this.position;
    }

    public ImportHandler getHandler() {
        return this.handler;
    }

    public void setDefaultBufferSize(int size) {
        this.defaultBufferSize = size;
    }

    public void close() {
        this.buffer = null;
        this.cache = null;
        this.thread = null;
    }

    public void batchRowsIncrease() {
        ++this.batchRowCounts;
    }

    public int getBatchRowsOffset() {
        return this.batchRowsOffset;
    }

    public int getBatchRowsEnd() {
        return this.batchRowsEnd;
    }

    public Exception getException() {
        return this.exception;
    }

    class ImportDataThread
    extends Thread {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            while (true) {
                byte[] data = null;
                if (ImportStream1.this.finished && !ImportStream1.this.cacheDataFilled) {
                    ImportStream1.this.ended = true;
                    break;
                }
                if (ImportStream1.this.exFinished) {
                    ImportStream1.this.ended = true;
                    ImportStream1 importStream1 = ImportStream1.this;
                    synchronized (importStream1) {
                        ImportStream1.this.notify();
                        break;
                    }
                }
                try {
                    data = ImportStream1.this.getMessage();
                    if (data == null) {
                        ImportStream1.this.ended = true;
                        break;
                    }
                    ImportStream1.this.sendMessage(data);
                }
                catch (SQLException ex) {
                    ImportStream1.this.threadException = ex;
                    ImportStream1.this.exFinished = true;
                }
                catch (Exception ex) {
                    ImportStream1.this.exception = ex;
                    ImportStream1.this.exFinished = true;
                }
                catch (Throwable ex) {
                    ImportStream1.this.exception = new Exception(ex.getMessage());
                    ImportStream1.this.exFinished = true;
                }
            }
        }
    }
}

