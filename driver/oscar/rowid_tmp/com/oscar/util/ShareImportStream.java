/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.Driver;
import com.oscar.core.ImportHandler;
import com.oscar.jdbc.OscarImportHandler;
import com.oscar.protocol.OSCARProtocol;
import com.oscar.protocol.packets.QueryPacket;
import com.oscar.protocol.stream.OStream;
import com.oscar.util.ImportBufferManager;
import com.oscar.util.ImportStream;
import com.oscar.util.OSQLException;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ShareImportStream
extends ImportStream {
    private static final int MAX_QUEUE_SIZE = 2;
    private BlockingQueue<Cache> queue;
    private byte[] buffer;
    private volatile boolean ended = false;
    private ImportDataThread thread;
    private boolean bulkflow = false;
    private boolean inited = false;
    private boolean lastCacheHalfRow = false;
    private int bufferOffset = 1;
    private int batchRowCounts = 0;
    private int batchRowsOffset = 1;
    private int batchRowsEnd = 0;
    private static final int OFFER_WAIT_SECOND = 120;
    private static final int OFFER_WAIT_INTERVAL_SECOND = 10;
    private static final int COUNT = 12;
    protected static boolean logFlag = Driver.getLogLevel() >= 2;

    public ShareImportStream(OscarImportHandler handler) {
        this.handler = handler;
        this.defaultBufferSize = handler.getBufferSize();
        if (handler.getBulkKind() == OscarImportHandler.BULK_FLOW) {
            this.bulkflow = true;
        }
        int queueSize = ShareImportStream.getQueueSize(this.defaultBufferSize);
        this.queue = new ArrayBlockingQueue<Cache>(queueSize);
        this.reInit();
    }

    private static int getQueueSize(int defaultBufferSize) {
        long maxOn = Runtime.getRuntime().maxMemory() * 2L / 3L;
        if (maxOn < (long)defaultBufferSize) {
            throw new RuntimeException("JVM's max memory is not enough, set -Xmx or reduce buffer");
        }
        int queueSize = (int)(maxOn / (long)defaultBufferSize);
        if (queueSize == 0) {
            return 1;
        }
        queueSize = queueSize > 2 ? 2 : queueSize;
        return queueSize;
    }

    public void reInit() {
        this.cacheSize = 0;
        this.position = 0;
        this.rowPosition = 0;
        this.ended = false;
        this.finished = false;
        this.exFinished = false;
        this.inited = false;
        this.thread = new ImportDataThread();
        this.thread.start();
    }

    public void write(byte[] b, int off, int len) throws SQLException {
        this.checkException();
        if (off < 0 || len > b.length) {
            throw new SQLException("\u504f\u79fb\u5c0f\u4e8e0\u6216\u8005\u6570\u636e\u957f\u5ea6\u5927\u4e8ebuffer");
        }
        if (len + off > b.length) {
            throw new SQLException("\u504f\u79fb\u91cf\u518d\u52a0\u4e0a\u6570\u636e\u957f\u5ea6\u5927\u4e8ebuffer");
        }
        int offset = off;
        int length = len;
        do {
            if (this.isBufferNotFull(length)) {
                this.copyToBuffer(b, offset, length);
                return;
            }
            if (!this.bulkflow && this.rowPosition > 0) {
                this.flushToQueue(this.rowPosition, false);
                continue;
            }
            int remain = this.defaultBufferSize - this.position;
            this.copyToBuffer(b, offset, remain);
            this.flushToQueue(true);
            offset += remain;
            length -= remain;
        } while (!this.ended);
    }

    private boolean isBufferNotFull(int len) {
        return this.position + len <= this.defaultBufferSize;
    }

    private void copyToBuffer(byte[] data, int off, int len) {
        System.arraycopy(data, off, this.getBuffer(), this.position, len);
        this.position += len;
    }

    private void flushToQueue(int endPosition, boolean halfRowEnd) throws SQLException {
        if (this.buffer == null) {
            return;
        }
        if (this.position < endPosition || endPosition <= 0) {
            return;
        }
        byte[] data = null;
        if (endPosition == this.defaultBufferSize) {
            data = this.buffer;
            this.buffer = null;
            this.position = 0;
            this.rowPosition = 0;
        } else {
            try {
                data = ImportBufferManager.getBuffer(endPosition);
            }
            catch (InterruptedException e) {
                data = new byte[endPosition];
            }
            System.arraycopy(this.buffer, 0, data, 0, endPosition);
            if (endPosition < this.position) {
                System.arraycopy(this.buffer, endPosition, this.buffer, 0, this.position - endPosition);
            }
            this.position -= endPosition;
            this.rowPosition = 0;
        }
        try {
            boolean suc = false;
            int count = 0;
            while (!suc && count < 12) {
                this.checkException();
                suc = this.queue.offer(new Cache(data, this.lastCacheHalfRow, halfRowEnd), 10L, TimeUnit.SECONDS);
                if (!logFlag || ++count * 10 < 30) continue;
                Driver.writeLog("WARNING: \u6570\u636e\u5e93\u6267\u884c\u65f6\u95f4\u4e3a(s): " + count * 10);
            }
            if (!suc) {
                String message = "importer is " + (this.thread.isAlive() ? "alive" : "not alive");
                if (this.threadException != null) {
                    Driver.writeLog(this.threadException);
                    throw new OSQLException("OSCAR-00804", "88888", 804, this.threadException.getMessage() + " " + message, this.threadException);
                }
                throw new OSQLException("OSCAR-00804", "88888", 804, this.threadException.getMessage() + " " + message, this.threadException);
            }
        }
        catch (InterruptedException e) {
            throw new SQLException("Interrupted when offer buffer to queue, " + e.getMessage());
        }
        this.lastCacheHalfRow = halfRowEnd;
        this.batchRowsOffset = this.bufferOffset;
        this.batchRowsEnd = this.batchRowCounts;
        this.bufferOffset = this.batchRowCounts + 1;
    }

    private void flushToQueue(boolean halfRowEnd) throws SQLException {
        this.flushToQueue(this.position, halfRowEnd);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sendMessage(Cache cache) throws SQLException {
        if (cache == null || cache.cacheData == null) {
            throw new OSQLException("cache is null when send data to db", "88888");
        }
        byte[] data = cache.cacheData;
        QueryPacket qp = null;
        if (!this.inited || !cache.lastHalfRow && !this.bulkflow) {
            this.currentSql = this.handler.getInsertBulkStr().toString();
            if (this.handler.getHintParam() != null) {
                this.currentSql = this.currentSql + " WITH " + this.handler.getHintParam();
            }
            try {
                qp = new QueryPacket(this.handler.getConnection().getEncoding().encode(this.currentSql), 0);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        OStream oStream = this.handler.getConnection().getProtocol().oStream;
        synchronized (oStream) {
            OSCARProtocol protocol = this.handler.getConnection().getProtocol();
            protocol.setImportHandler(this.handler);
            if (!this.inited || !cache.lastHalfRow && !this.bulkflow) {
                protocol.importBegin(qp);
            }
            if (this.handler.getImportBlockParam() == 1) {
                byte[] dataLen = new byte[]{(byte)(data.length >> 24 & 0xFF), (byte)(data.length >> 16 & 0xFF), (byte)(data.length >> 8 & 0xFF), (byte)(data.length & 0xFF)};
                protocol.importData(dataLen);
            }
            protocol.importData(data);
            if (!this.bulkflow && !cache.thisHalfRow) {
                protocol.importEnd();
                this.updateCount += this.handler.getUpdateCount();
                this.handler.setUpdateCount(this.updateCount);
            }
        }
        this.inited = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void ImportEnd() throws SQLException {
        if (!this.bulkflow) {
            return;
        }
        OStream oStream = this.handler.getConnection().getProtocol().oStream;
        synchronized (oStream) {
            OSCARProtocol protocol = this.handler.getConnection().getProtocol();
            protocol.setImportHandler(this.handler);
            protocol.importEnd();
            this.updateCount += this.handler.getUpdateCount();
            this.handler.setUpdateCount(this.updateCount);
        }
        this.inited = false;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void finished() {
        this.finished = true;
        this.releaseBuffer();
        while (!this.ended && !this.exFinished) {
            if (this.thread == null || !this.thread.isAlive()) {
                this.ended = true;
                break;
            }
            try {
                this.thread.join(120000L);
                if (!this.thread.isAlive() && this.ended || !logFlag) continue;
                Driver.writeLog("\u7ebf\u7a0b\u6267\u884c\u8d85\u8fc7120\u79d2\u8fd8\u672a\u9000\u51fa");
            }
            catch (InterruptedException interruptedException) {}
        }
    }

    public void flush() throws SQLException {
        this.flushToQueue(this.position, false);
    }

    public void checkException() throws SQLException {
        if (this.threadException != null) {
            if (this.threadException instanceof SQLException) {
                throw (SQLException)this.threadException;
            }
            if (this.threadException.getCause() != null) {
                throw new SQLException(this.threadException.getMessage() + " Cause: " + this.threadException.getCause().getMessage());
            }
            throw new SQLException(this.threadException.getMessage());
        }
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
        this.releaseBuffer();
        this.releaseQueue();
        if (!this.ended) {
            this.ended = true;
            this.finished();
        }
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

    public byte[] getBuffer() {
        if (this.buffer == null) {
            try {
                this.buffer = ImportBufferManager.getBuffer(this.defaultBufferSize);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.buffer;
    }

    public void releaseBuffer() {
        if (this.buffer != null) {
            ImportBufferManager.releaseBuffer(this.buffer);
            this.buffer = null;
        }
    }

    public void releaseQueue() {
        this.queue.clear();
        this.queue = null;
    }

    public void releaseCache(Cache cache) {
        if (cache != null) {
            cache.release();
            cache = null;
        }
    }

    protected void finalize() {
        this.releaseBuffer();
        this.releaseQueue();
    }

    private class Cache {
        final boolean lastHalfRow;
        final boolean thisHalfRow;
        byte[] cacheData;

        public Cache(byte[] cacheData, boolean lastHalfRow, boolean thisHalfRow) {
            this.cacheData = cacheData;
            this.lastHalfRow = lastHalfRow;
            this.thisHalfRow = thisHalfRow;
        }

        void release() {
            ImportBufferManager.releaseBuffer(this.cacheData);
            this.cacheData = null;
        }
    }

    class ImportDataThread
    extends Thread {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Loose catch block
         */
        public void run() {
            block22: {
                try {
                    while (!(ShareImportStream.this.ended || ShareImportStream.this.finished && ShareImportStream.this.queue.size() <= 0)) {
                        Cache cache = null;
                        try {
                            cache = (Cache)ShareImportStream.this.queue.poll(10L, TimeUnit.SECONDS);
                        }
                        catch (InterruptedException e1) {
                            ShareImportStream.this.threadException = e1;
                            ShareImportStream.this.exFinished = true;
                            break;
                        }
                        if (cache != null && cache.cacheData != null) {
                            ShareImportStream.this.sendMessage(cache);
                            ShareImportStream.this.releaseCache(cache);
                            continue;
                        }
                        if (!ShareImportStream.this.finished) continue;
                        ShareImportStream.this.ended = true;
                        break;
                    }
                    Object var4_5 = null;
                }
                catch (Throwable throwable) {
                    Object var4_8 = null;
                    ShareImportStream.this.ended = true;
                    if (!ShareImportStream.this.exFinished) {
                        try {
                            ShareImportStream.this.ImportEnd();
                        }
                        catch (Throwable ex) {
                            if (ShareImportStream.this.threadException == null) {
                                ShareImportStream.this.threadException = ex;
                            }
                            ShareImportStream.this.exFinished = true;
                        }
                    }
                    throw throwable;
                }
                ShareImportStream.this.ended = true;
                if (!ShareImportStream.this.exFinished) {
                    try {
                        ShareImportStream.this.ImportEnd();
                    }
                    catch (Throwable ex) {
                        if (ShareImportStream.this.threadException == null) {
                            ShareImportStream.this.threadException = ex;
                        }
                        ShareImportStream.this.exFinished = true;
                    }
                }
                break block22;
                {
                    catch (SQLException e) {
                        ShareImportStream.this.threadException = e;
                        ShareImportStream.this.exFinished = true;
                        Object var4_6 = null;
                        ShareImportStream.this.ended = true;
                        if (!ShareImportStream.this.exFinished) {
                            try {
                                ShareImportStream.this.ImportEnd();
                            }
                            catch (Throwable ex) {
                                if (ShareImportStream.this.threadException == null) {
                                    ShareImportStream.this.threadException = ex;
                                }
                                ShareImportStream.this.exFinished = true;
                            }
                        }
                        break block22;
                    }
                    catch (Exception e) {
                        ShareImportStream.this.threadException = e;
                        ShareImportStream.this.exFinished = true;
                        Object var4_7 = null;
                        ShareImportStream.this.ended = true;
                        if (!ShareImportStream.this.exFinished) {
                            try {
                                ShareImportStream.this.ImportEnd();
                            }
                            catch (Throwable ex) {
                                if (ShareImportStream.this.threadException == null) {
                                    ShareImportStream.this.threadException = ex;
                                }
                                ShareImportStream.this.exFinished = true;
                            }
                        }
                    }
                }
            }
        }
    }
}

