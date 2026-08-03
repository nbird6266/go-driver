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

public class ImportStream2
extends ImportStream {
    private DataBuffer tmpBuffer;
    private DataBuffer currentBuffer = null;
    private DataBuffer backBuffer = null;
    private ImportDataThread thread;
    private boolean ended = false;
    private volatile boolean flushed = false;
    QueryPacket qp = null;
    Object lock = null;
    Object endLock = null;

    public ImportStream2(OscarImportHandler handler) {
        this.handler = handler;
        this.defaultBufferSize = handler.getBufferSize();
        this.currentBuffer = new DataBuffer(this.defaultBufferSize);
        this.backBuffer = new DataBuffer(this.defaultBufferSize);
        this.lock = new Object();
        this.endLock = new Object();
        this.reInit();
    }

    public void reInit() {
        this.cacheSize = 0;
        this.position = 0;
        this.rowPosition = 0;
        this.ended = false;
        this.flushed = false;
        this.exFinished = false;
        this.thread = new ImportDataThread();
        this.thread.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void write(byte[] b, int off, int len) {
        if (off < 0 || len > b.length) {
            throw new NullPointerException();
        }
        if (len + off > b.length) {
            throw new IndexOutOfBoundsException();
        }
        if (this.position + len < this.defaultBufferSize) {
            System.arraycopy(b, off, this.currentBuffer.getBuffer(), this.position, len);
            this.position += len;
            this.currentBuffer.setLength(this.position);
        } else {
            if (!this.backBuffer.isPreparedForFill()) {
                Object object = this.lock;
                synchronized (object) {
                    try {
                        this.lock.wait();
                    }
                    catch (InterruptedException e) {
                        // empty catch block
                    }
                }
            }
            if (this.position >= this.rowPosition) {
                System.arraycopy(this.currentBuffer.getBuffer(), this.rowPosition, this.backBuffer.getBuffer(), 0, this.position - this.rowPosition);
            }
            this.currentBuffer.reachFull();
            System.arraycopy(b, off, this.currentBuffer.getBuffer(), this.position, len);
            this.position += len;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void switchBuffer() {
        Object object = this.lock;
        synchronized (object) {
            this.tmpBuffer = this.currentBuffer;
            this.currentBuffer = this.backBuffer;
            this.backBuffer = this.tmpBuffer;
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
            this.handler.getConnection().getProtocol().importBegin(this.qp);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        OStream oStream = this.handler.getConnection().getProtocol().oStream;
        synchronized (oStream) {
            this.handler.getConnection().getProtocol().setImportHandler(this.handler);
            this.handler.getConnection().getProtocol().importBegin(this.qp);
            this.handler.getConnection().getProtocol().importData(data);
            this.handler.getConnection().getProtocol().importEnd();
        }
        this.updateCount += this.handler.getUpdateCount();
        this.handler.setUpdateCount(this.updateCount);
    }

    public void sendMessage(DataBuffer dataBuffer) throws SQLException {
        this.handler.getConnection().getProtocol().importData(dataBuffer.getBuffer(), 0, dataBuffer.getLength());
        dataBuffer.setLength(0);
    }

    public boolean isFinished() {
        return this.flushed;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void flush() {
        Object object = this.lock;
        synchronized (object) {
            this.flushed = true;
            this.lock.notify();
        }
        object = this.endLock;
        synchronized (object) {
            try {
                if (!this.ended && !this.exFinished) {
                    this.endLock.wait();
                }
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
    }

    public void finished() {
        try {
            if (this.currentBuffer.getLength() != 0) {
                this.sendMessage(this.currentBuffer);
                this.handler.getConnection().getProtocol().importEnd();
                this.updateCount += this.handler.getUpdateCount();
                this.handler.setUpdateCount(this.updateCount);
                this.currentBuffer.setBatchRowsOffset(this.currentBuffer.getBatchRowsEnd() + 1);
            }
        }
        catch (SQLException e) {
            this.threadException = e;
            e.printStackTrace();
        }
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

    public void close() {
        this.currentBuffer = null;
        this.backBuffer = null;
        this.thread = null;
    }

    public void batchRowsIncrease() {
        this.currentBuffer.batchRowsIncrease();
    }

    public int getBatchRowsOffset() {
        if (this.ended) {
            return this.currentBuffer.getBatchRowsOffset();
        }
        return this.backBuffer.getBatchRowsOffset();
    }

    public int getBatchRowsEnd() {
        if (this.ended) {
            return this.currentBuffer.getBatchRowsEnd();
        }
        return this.backBuffer.getBatchRowsEnd();
    }

    class DataBuffer {
        boolean filled = false;
        boolean preparedForFill = true;
        int length = 0;
        int bufferOffset = 1;
        int bufferEnd = 0;
        byte[] buffer;

        public void init() {
            this.setFilled(false);
            this.setPreparedForFill(true);
            this.setLength(0);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void reachFull() {
            ImportStream2.this.currentBuffer.setLength(ImportStream2.this.rowPosition);
            this.setPreparedForFill(false);
            this.setFilled(true);
            ImportStream2.this.position -= ImportStream2.this.rowPosition;
            ImportStream2.this.rowPosition = 0;
            Object object = ImportStream2.this.lock;
            synchronized (object) {
                ImportStream2.this.switchBuffer();
                ImportStream2.this.currentBuffer.setBatchRowsEnd(ImportStream2.this.backBuffer.getBatchRowsEnd());
                ImportStream2.this.lock.notify();
            }
        }

        public int getLength() {
            return this.length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public boolean isPreparedForFill() {
            return this.preparedForFill;
        }

        public void setPreparedForFill(boolean preparedForFill) {
            this.preparedForFill = preparedForFill;
        }

        public boolean isFilled() {
            return this.filled;
        }

        public void setFilled(boolean filled) {
            this.filled = filled;
        }

        public DataBuffer() {
        }

        public DataBuffer(int size) {
            this.buffer = new byte[size];
        }

        public byte[] getBuffer() {
            return this.buffer;
        }

        public int getBatchRowsOffset() {
            return this.bufferOffset;
        }

        public void setBatchRowsOffset(int bufferOffset) {
            this.bufferOffset = bufferOffset;
        }

        public int getBatchRowsEnd() {
            return this.bufferEnd;
        }

        public void setBatchRowsEnd(int bufferEnd) {
            this.bufferEnd = bufferEnd;
        }

        public void batchRowsIncrease() {
            ++this.bufferEnd;
        }
    }

    class ImportDataThread
    extends Thread {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            Object object;
            while (!ImportStream2.this.flushed) {
                if (ImportStream2.this.backBuffer.isFilled()) {
                    try {
                        ImportStream2.this.sendMessage(ImportStream2.this.backBuffer);
                    }
                    catch (SQLException e) {
                        ImportStream2.this.exFinished = true;
                    }
                    object = ImportStream2.this.lock;
                    synchronized (object) {
                        ImportStream2.this.backBuffer.init();
                        ImportStream2.this.lock.notify();
                        continue;
                    }
                }
                object = ImportStream2.this.lock;
                synchronized (object) {
                    try {
                        if (!ImportStream2.this.backBuffer.isFilled()) {
                            ImportStream2.this.lock.wait();
                        }
                    }
                    catch (InterruptedException e) {
                        ImportStream2.this.exFinished = true;
                    }
                }
            }
            object = ImportStream2.this.endLock;
            synchronized (object) {
                ImportStream2.this.ended = true;
                ImportStream2.this.endLock.notify();
            }
        }
    }
}

