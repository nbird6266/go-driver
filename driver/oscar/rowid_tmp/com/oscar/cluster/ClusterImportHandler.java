/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.cluster;

import com.oscar.cluster.Cluster;
import com.oscar.cluster.Node;
import com.oscar.cluster.NodeImportStream;
import com.oscar.cluster.RobinImportStrategy;
import com.oscar.core.BaseConnection;
import com.oscar.core.DistributeImportHandler;
import com.oscar.jdbc.OscarImportHandler;
import java.sql.SQLException;
import java.util.List;

public class ClusterImportHandler
extends OscarImportHandler
implements DistributeImportHandler {
    private Cluster cluster;
    private Cluster.ImportCredential importCredential;
    private List<Node> importNodes;
    private int importNodeConnectRetryTime;
    int nodenum = 0;
    public volatile int rowCount = 0;
    private boolean hasBulk = false;

    public ClusterImportHandler(BaseConnection conn, String schemName, String tableName) throws SQLException {
        super(conn, schemName, tableName);
        this.importNodeConnectRetryTime = conn.getClusterImportNodeRetryTime();
        if (conn.getCluster() == null) {
            conn.setCluster(new Cluster(conn));
        }
        this.cluster = conn.getCluster();
        this.importCredential = this.cluster.getClusterImportCredential();
    }

    public ClusterImportHandler(BaseConnection conn, String tableName) throws SQLException {
        this(conn, null, tableName);
    }

    public ClusterImportHandler(BaseConnection conn, String schemName, String tableName, String tempFileDir) throws SQLException {
        super(conn, schemName, tableName);
        this.importNodeConnectRetryTime = conn.getClusterImportNodeRetryTime();
        if (conn.getCluster() == null) {
            conn.setCluster(new Cluster(conn));
        }
        this.cluster = conn.getCluster();
        this.importCredential = this.cluster.getClusterImportCredential();
    }

    public void reInit() throws SQLException {
        this.cluster.reInit();
        this.importCredential = this.cluster.getImportCredential();
        this.nodenum = 0;
    }

    public void begin() throws SQLException {
        this.rowCount = 0;
        this.initImportNodes();
        this.isBegin = true;
        RobinImportStrategy strategy = new RobinImportStrategy(this.importNodes);
        if (this.importStream == null) {
            this.importStream = new NodeImportStream(this, strategy);
        } else {
            ((NodeImportStream)this.importStream).reStrategy(strategy);
            this.importStream.reInit();
        }
    }

    public void endRow() throws SQLException {
        try {
            super.endRow();
            ++this.rowCount;
        }
        catch (SQLException e) {
            this.closeImportNodes();
            if (this.importStream != null && this.importStream.getThreadException() != null) {
                this.cluster.importEnd();
            }
            throw e;
        }
    }

    public void closeImportNodes() {
        if (this.importNodes != null) {
            for (Node node : this.importNodes) {
                node.disConnect();
            }
            this.importNodes.clear();
            this.importNodes = null;
        }
    }

    public boolean execute() throws SQLException {
        if (this.hasRowNotSubmitted()) {
            this.endRow();
        }
        boolean result = true;
        if (this.isBegin()) {
            try {
                boolean canComplete = true;
                try {
                    super.endExecute();
                }
                catch (SQLException e) {
                    if (e.getErrorCode() == 1001) {
                        canComplete = false;
                    }
                    throw e;
                }
                if (canComplete) {
                    for (Node node : this.importNodes) {
                        node.importEnd();
                    }
                }
                int updateCount = this.cluster.importEnd();
                this.hasBulk = false;
                int currentUpdateCount = this.getUpdateCount();
                if (currentUpdateCount > 0) {
                    this.setUpdateCount(updateCount + currentUpdateCount);
                } else {
                    this.setUpdateCount(updateCount);
                }
            }
            catch (SQLException e) {
                this.closeImportNodes();
                this.importCredential = null;
                this.cluster.setClusterImportCredential(null);
                throw e;
            }
        }
        return result;
    }

    public void close() throws SQLException {
        super.close();
        if (this.importNodes != null) {
            this.importNodes.clear();
        }
        this.importNodes = null;
        this.importCredential = null;
    }

    public void setNodeNum(int num) throws SQLException {
        if (num > this.cluster.getNodeMap().size()) {
            throw new SQLException("\u8282\u70b9\u6570\u91cf\u8fc7\u5927, \u5f53\u524d\u8282\u70b9\u6570" + this.cluster.getNodeMap().size());
        }
        this.nodenum = num;
    }

    private void initImportNodes() throws SQLException {
        this.importNodes = this.cluster.getImportNodes(this.schemName, this.tableName, this.nodenum);
        boolean initNodeSuccessful = false;
        int retryTime = 0;
        do {
            try {
                for (Node node : this.importNodes) {
                    node.connect(this.importCredential);
                }
                initNodeSuccessful = true;
            }
            catch (Exception e) {
                this.closeImportNodes();
                initNodeSuccessful = false;
                if (retryTime >= this.importNodeConnectRetryTime) continue;
                this.reInit();
            }
        } while (!initNodeSuccessful && retryTime++ < this.importNodeConnectRetryTime);
        if (!initNodeSuccessful) {
            throw new SQLException("\u8282\u70b9\u8fde\u63a5\u521b\u5efa\u5931\u8d25\uff0c\u5c1d\u8bd5\u6b21\u6570" + (this.importNodeConnectRetryTime + 1));
        }
    }

    public Cluster getCluster() {
        return this.cluster;
    }

    public boolean isHasBulk() {
        return this.hasBulk;
    }

    public void setHasBulk(boolean hasBulk) {
        this.hasBulk = hasBulk;
    }
}

