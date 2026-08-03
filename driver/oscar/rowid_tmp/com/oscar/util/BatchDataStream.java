/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.jdbc.OscarStatement;
import java.util.ArrayList;
import java.util.List;

public class BatchDataStream {
    BatchDataList currentBatch = new BatchDataList();
    BatchDataList backBatch = new BatchDataList();
    BatchDataList tmpBatch;
    int batchCount = 0;
    OscarStatement osrStmt;
    Object lock = new Object();
    Object endLock = new Object();
    boolean ended = false;
    boolean finshed = false;
    int rowPosition = 0;
    int currentPosition = 0;
    BatchDataThread thread;

    public List getBatch() {
        return this.currentBatch.getBatch();
    }

    public BatchDataStream(OscarStatement osrStmt) {
        this.osrStmt = osrStmt;
        this.batchCount = osrStmt.getBatchCount();
        this.reInit();
    }

    public void reInit() {
        this.ended = false;
        this.finshed = false;
        this.rowPosition = 0;
        this.currentPosition = 0;
        this.currentBatch.init();
        this.backBatch.init();
        this.thread = new BatchDataThread();
        this.thread.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addBatchData(OscarStatement.BatchRowData batchRowData) {
        if (this.rowPosition + 1 < this.batchCount) {
            this.addData(batchRowData);
        } else {
            if (!this.backBatch.isPrepareToFill()) {
                Object object = this.lock;
                synchronized (object) {
                    try {
                        this.lock.wait();
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            this.addData(batchRowData);
            this.currentBatch.reachFull();
            this.rowPosition -= this.batchCount;
            this.currentPosition -= this.batchCount;
        }
    }

    private void addData(OscarStatement.BatchRowData batchRowData) {
        if (this.currentPosition > this.rowPosition) {
            this.currentBatch.getBatch().set(this.currentPosition, batchRowData);
        } else {
            this.currentBatch.getBatch().add(this.currentPosition, batchRowData);
        }
        ++this.rowPosition;
        ++this.currentPosition;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void switchBatchList() {
        Object object = this.lock;
        synchronized (object) {
            this.tmpBatch = this.currentBatch;
            this.currentBatch = this.backBatch;
            this.backBatch = this.tmpBatch;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void finshed() {
        Object object = this.lock;
        synchronized (object) {
            this.finshed = true;
            this.lock.notify();
        }
        object = this.endLock;
        synchronized (object) {
            try {
                if (!this.ended) {
                    this.endLock.wait();
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            if (this.backBatch.getBatch().size() > 0) {
                this.osrStmt.executeBatchAsyn(this.backBatch.getBatch());
            }
            if (this.currentBatch.getBatch().size() > 0) {
                this.osrStmt.executeBatchAsyn(this.currentBatch.getBatch());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    class BatchDataList {
        private List batch = new ArrayList();
        private boolean isFull = false;
        private boolean isPrepareToFill = true;

        BatchDataList() {
        }

        public void init() {
            if (this.batch != null) {
                this.batch.clear();
            }
            this.setFull(false);
            this.setPrepareToFill(true);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void reachFull() {
            this.setFull(true);
            this.setPrepareToFill(false);
            Object object = BatchDataStream.this.lock;
            synchronized (object) {
                BatchDataStream.this.switchBatchList();
                BatchDataStream.this.currentBatch.init();
                BatchDataStream.this.lock.notify();
            }
        }

        public List getBatch() {
            return this.batch;
        }

        public void setBatch(List batch) {
            this.batch = batch;
        }

        public boolean isFull() {
            return this.isFull;
        }

        public void setFull(boolean isFull) {
            this.isFull = isFull;
        }

        public boolean isPrepareToFill() {
            return this.isPrepareToFill;
        }

        public void setPrepareToFill(boolean isPrepareToFill) {
            this.isPrepareToFill = isPrepareToFill;
        }
    }

    class BatchDataThread
    extends Thread {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            Object object = BatchDataStream.this.endLock;
            synchronized (object) {
                while (!BatchDataStream.this.finshed) {
                    Object object2;
                    if (BatchDataStream.this.backBatch.isFull()) {
                        try {
                            BatchDataStream.this.osrStmt.executeBatchAsyn(BatchDataStream.this.backBatch.getBatch());
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        object2 = BatchDataStream.this.lock;
                        synchronized (object2) {
                            BatchDataStream.this.backBatch.init();
                            BatchDataStream.this.lock.notify();
                            continue;
                        }
                    }
                    object2 = BatchDataStream.this.lock;
                    synchronized (object2) {
                        try {
                            if (!BatchDataStream.this.backBatch.isFull()) {
                                BatchDataStream.this.lock.wait();
                            }
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                BatchDataStream.this.ended = true;
                BatchDataStream.this.endLock.notify();
            }
        }
    }
}

