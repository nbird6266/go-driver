/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.core.ImportHandler;
import com.oscar.jdbc.OscarImportHandler;
import com.oscar.protocol.OSCARProtocol;
import com.oscar.protocol.packets.QueryPacket;
import com.oscar.protocol.stream.OStream;
import com.oscar.util.ImportBufferManager;
import com.oscar.util.ImportStream;
import java.sql.SQLException;

public class ShareImportStream1
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
    private boolean bulkflow = false;
    private boolean inited = false;

    public ShareImportStream1(OscarImportHandler handler) {
        this.handler = handler;
        this.defaultBufferSize = handler.getBufferSize();
        if (handler.getBulkKind() == OscarImportHandler.BULK_FLOW) {
            this.bulkflow = true;
        }
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
        this.inited = false;
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
            System.arraycopy(b, off, this.getBuffer(), this.position, len);
            this.position += len;
        } else {
            this.flush();
            System.arraycopy(b, off, this.getBuffer(), this.position, len);
            this.position += len;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sendMessage(byte[] data) throws SQLException {
        if (!this.bulkflow || !this.inited) {
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
        }
        OStream oStream = this.handler.getConnection().getProtocol().oStream;
        synchronized (oStream) {
            OSCARProtocol protocol = this.handler.getConnection().getProtocol();
            protocol.setImportHandler(this.handler);
            if (!this.bulkflow || !this.inited) {
                protocol.importBegin(this.qp);
            }
            if (this.handler.getImportBlockParam() == 1) {
                byte[] dataLen = new byte[]{(byte)(data.length >> 24 & 0xFF), (byte)(data.length >> 16 & 0xFF), (byte)(data.length >> 8 & 0xFF), (byte)(data.length & 0xFF)};
                protocol.importData(dataLen);
            }
            protocol.importData(data);
            if (!this.bulkflow) {
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

    public synchronized byte[] getMessage() {
        byte[] data = null;
        while (true) {
            if (this.cacheDataFilled) {
                data = new byte[this.cacheSize];
                System.arraycopy(this.getCache(), 0, data, 0, this.cacheSize);
                this.releaseCache();
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
                System.arraycopy(this.getBuffer(), 0, this.getCache(), 0, this.rowPosition);
                this.cacheSize = this.rowPosition;
                this.moveData(this.getBuffer(), 0, this.rowPosition);
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
        this.releaseBuffer();
        while (!this.ended && !this.exFinished) {
            ShareImportStream1 shareImportStream1 = this;
            synchronized (shareImportStream1) {
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
        System.arraycopy(this.getBuffer(), length, this.getBuffer(), 0, this.position - length);
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
        this.releaseBuffer();
        this.releaseCache();
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

    public byte[] getCache() {
        if (this.cache == null) {
            this.cache = ImportBufferManager.getBufferAllways(this.defaultBufferSize);
        }
        return this.cache;
    }

    public void releaseBuffer() {
        ImportBufferManager.releaseBuffer(this.buffer);
        this.buffer = null;
    }

    public void releaseCache() {
        ImportBufferManager.releaseCatch(this.cache);
        this.cache = null;
    }

    protected void finalize() {
        if (this.buffer != null) {
            this.releaseBuffer();
        }
        if (this.cache != null) {
            this.releaseCache();
        }
    }

    static /* synthetic */ boolean access$000(ShareImportStream1 x0) {
        return x0.cacheDataFilled;
    }

    static /* synthetic */ boolean access$102(ShareImportStream1 x0, boolean x1) {
        x0.ended = x1;
        return x0.ended;
    }

    class ImportDataThread
    extends Thread {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Unable to fully structure code
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        public void run() {
            block21: {
                try {
                    while (true) {
                        data = null;
                        if (ShareImportStream1.this.finished && !ShareImportStream1.access$000(ShareImportStream1.this)) break;
                        if (ShareImportStream1.this.exFinished) {
                            var2_2 = ShareImportStream1.this;
                            synchronized (var2_2) {
                                ShareImportStream1.this.notify();
                                break;
                            }
                        }
                        try {
                            data = ShareImportStream1.this.getMessage();
                            if (data == null) break;
                            ShareImportStream1.this.sendMessage(data);
                            continue;
                        }
                        catch (SQLException ex) {
                            ShareImportStream1.this.threadException = ex;
                            ShareImportStream1.this.exFinished = true;
                            continue;
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                            ShareImportStream1.this.threadException = ex;
                            ShareImportStream1.this.exFinished = true;
                            continue;
                        }
                        catch (Throwable ex) {
                            ShareImportStream1.this.threadException = ex;
                            ShareImportStream1.this.exFinished = true;
                            continue;
                        }
                        break;
                    }
                    var5_6 = null;
                    if (ShareImportStream1.this.exFinished) break block21;
                }
                catch (Throwable var4_10) {
                    var5_7 = null;
                    if (!ShareImportStream1.this.exFinished) {
                        try {
                            ShareImportStream1.this.ImportEnd();
                            ShareImportStream1.access$102(ShareImportStream1.this, true);
                        }
                        catch (SQLException ex) {
                            ShareImportStream1.this.threadException = ex;
                            ShareImportStream1.this.exFinished = true;
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                            ShareImportStream1.this.threadException = ex;
                            ShareImportStream1.this.exFinished = true;
                        }
                        catch (Throwable ex) {
                            ShareImportStream1.this.threadException = ex;
                            ShareImportStream1.this.exFinished = true;
                        }
                    }
                    if (ShareImportStream1.this.exFinished == false) throw var4_10;
                    ShareImportStream1.access$102(ShareImportStream1.this, true);
                    var6_9 = ShareImportStream1.this;
                    synchronized (var6_9) {
                        ShareImportStream1.this.notify();
                        throw var4_10;
                    }
                }
                ** try [egrp 3[TRYBLOCK] [10, 11, 12 : 178->197)] { 
lbl61:
                // 1 sources

                ShareImportStream1.this.ImportEnd();
                ShareImportStream1.access$102(ShareImportStream1.this, true);
                break block21;
lbl65:
                // 1 sources

                catch (SQLException ex) {
                    ShareImportStream1.this.threadException = ex;
                    ShareImportStream1.this.exFinished = true;
                }
lbl69:
                // 1 sources

                catch (Exception ex) {
                    ex.printStackTrace();
                    ShareImportStream1.this.threadException = ex;
                    ShareImportStream1.this.exFinished = true;
                }
lbl74:
                // 1 sources

                catch (Throwable ex) {
                    ShareImportStream1.this.threadException = ex;
                    ShareImportStream1.this.exFinished = true;
                }
            }
            if (ShareImportStream1.this.exFinished == false) return;
            ShareImportStream1.access$102(ShareImportStream1.this, true);
            var6_8 = ShareImportStream1.this;
            synchronized (var6_8) {
                ShareImportStream1.this.notify();
                return;
            }
        }
    }
}

