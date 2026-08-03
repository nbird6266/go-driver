/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.cluster;

import com.oscar.cluster.ClusterImportHandler;
import com.oscar.cluster.core.DataImportStream;
import com.oscar.cluster.core.ImportStrategy;
import com.oscar.util.OSQLException;
import com.oscar.util.ShareImportStream1;
import java.sql.SQLException;

public class NodeImportStream
extends ShareImportStream1 {
    private ImportStrategy strategy;
    private ClusterImportHandler handler;

    public NodeImportStream(ClusterImportHandler handler, ImportStrategy strategy) {
        super(handler);
        this.handler = handler;
        this.strategy = strategy;
    }

    public void sendMessage(byte[] data) throws SQLException {
        if (!this.handler.isHasBulk()) {
            try {
                this.handler.getCluster().importBegin(this.handler);
            }
            catch (SQLException e) {
                this.handler.closeImportNodes();
                throw e;
            }
            this.handler.setHasBulk(true);
        }
        DataImportStream node = null;
        node = this.handler.rowCount >= 10000 ? this.strategy.nextStream() : this.strategy.currentStream();
        try {
            node.importData(data);
        }
        catch (SQLException e) {
            this.handler.closeImportNodes();
            throw new OSQLException("OSCAR-01001", e.getSQLState(), 1001);
        }
        this.handler.rowCount = 0;
    }

    public void reStrategy(ImportStrategy strategy) {
        this.strategy = strategy;
    }
}

