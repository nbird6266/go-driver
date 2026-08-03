/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.cluster;

import com.oscar.cluster.Node;
import com.oscar.cluster.Protocol;
import com.oscar.cluster.core.ClusterProtocol;
import com.oscar.core.BaseConnection;
import com.oscar.core.BaseResultSet;
import com.oscar.jdbc.OscarImportHandler;
import com.oscar.protocol.stream.OStream;
import com.oscar.util.EscapeTools;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Cluster {
    private BaseConnection clusterConn;
    private Map<Integer, Node> nodeMap;
    private ClusterProtocol protocol;
    private ImportCredential importCredential = null;

    public Cluster(BaseConnection clusterConn) throws SQLException {
        this.clusterConn = clusterConn;
        this.protocol = new Protocol(clusterConn.getEncoding(), clusterConn);
        this.initNodes();
    }

    public void initNodes() throws SQLException {
        if (this.nodeMap == null) {
            this.nodeMap = new ConcurrentHashMap<Integer, Node>();
        }
        this.initNodesInfo(this.nodeMap);
        this.importCredential = this.getImportCredential();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized void reInit() throws SQLException {
        if (this.nodeMap == null || this.nodeMap.size() == 0) {
            this.initNodes();
            return;
        }
        Cluster cluster = this;
        synchronized (cluster) {
            HashMap<Integer, Node> newNodeMap = new HashMap<Integer, Node>();
            this.initNodesInfo(newNodeMap);
            for (Node oldNode : this.nodeMap.values()) {
                Node newNode = Cluster.getNewNodeInNewMap(newNodeMap, oldNode);
                if (newNode == null) {
                    if (!oldNode.isOpen()) continue;
                    oldNode.disConnect();
                    continue;
                }
                if (!oldNode.isOpen()) continue;
                newNodeMap.put(newNode.getNodeID(), oldNode);
            }
            this.nodeMap.clear();
            this.nodeMap.putAll(newNodeMap);
            this.importCredential = this.getImportCredential();
        }
    }

    private static Node getNewNodeInNewMap(Map<Integer, Node> newNodes, Node oldNode) {
        for (Map.Entry<Integer, Node> kv : newNodes.entrySet()) {
            Node newNode = kv.getValue();
            if (newNode.getNodeID() != oldNode.getNodeID() || !newNode.getHost().equals(oldNode.getHost()) || newNode.getPort() != oldNode.getPort()) continue;
            return kv.getValue();
        }
        return null;
    }

    protected void initNodesInfo(Map<Integer, Node> nodeMap) throws SQLException {
        String queryNodesSql = "select * from get_import_info() as (\"NODEID\" INT2, \"PORT\" INT4, \"ADDRESS\" TEXT)";
        BaseResultSet res = this.clusterConn.execSQL(queryNodesSql);
        int nodeID = 0;
        int port = 0;
        String host = null;
        while (res.next()) {
            nodeID = res.getInt("NODEID");
            port = res.getInt("PORT");
            host = res.getString("ADDRESS");
            Node node = new Node(nodeID, host, port, this.clusterConn.getEncoding());
            nodeMap.put(nodeID, node);
        }
    }

    public ImportCredential getImportCredential() throws SQLException {
        String queryCredentialSql = "select get_import_identification_code()";
        int[] importIdentification = null;
        BaseResultSet res = this.clusterConn.execSQL(queryCredentialSql);
        res.next();
        Array nodes = res.getArray(1);
        importIdentification = (int[])nodes.getArray();
        ImportCredential credential = new ImportCredential(importIdentification[0], importIdentification[1]);
        return credential;
    }

    public List<Node> getImportNodes(String schemaName, String tableName, int nodenum) throws SQLException {
        StringBuilder queryImportNodesSql = new StringBuilder("select get_current_import_nodes('");
        if (schemaName != null && schemaName.length() != 0) {
            schemaName = EscapeTools.quotationWrapper(schemaName);
            queryImportNodesSql.append(schemaName).append(".");
        }
        queryImportNodesSql.append(tableName);
        queryImportNodesSql.append("', ").append(nodenum).append(")");
        ArrayList<Integer> importNodes = new ArrayList<Integer>();
        BaseResultSet res = this.clusterConn.execSQL(queryImportNodesSql.toString());
        while (res.next()) {
            int[] nodeArr;
            Array nodes = res.getArray(1);
            for (int node : nodeArr = (int[])nodes.getArray()) {
                importNodes.add(node);
            }
        }
        boolean allActiveNode = false;
        ArrayList<Node> nodes = new ArrayList<Node>();
        do {
            allActiveNode = false;
            nodes.clear();
            Iterator i$ = importNodes.iterator();
            while (i$.hasNext()) {
                int nodeID = (Integer)i$.next();
                Node node = this.nodeMap.get(nodeID);
                if (node != null) {
                    nodes.add(node);
                    continue;
                }
                this.reInit();
                allActiveNode = true;
            }
        } while (allActiveNode);
        return nodes;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Cluster [");
        sb.append("\n");
        sb.append("    nodeMap=[").append("\n");
        for (Node value : this.nodeMap.values()) {
            sb.append("    ");
            sb.append(value.toString()).append("\n");
        }
        sb.append("    ]");
        sb.append("\n");
        sb.append("]");
        return sb.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void importBegin(OscarImportHandler handler) throws SQLException {
        OStream stream;
        String insertBulkSql = handler.getInsertBulkStr().toString();
        String hintParam = handler.getHintParam();
        if (hintParam != null) {
            insertBulkSql = insertBulkSql + " WITH " + hintParam;
        }
        OStream oStream = stream = this.clusterConn.getProtocol().oStream;
        synchronized (oStream) {
            this.protocol.importBegin(insertBulkSql, stream.getInputStream(), stream.getBufferedOutputStream());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int importEnd() throws SQLException {
        OStream stream;
        OStream oStream = stream = this.clusterConn.getProtocol().oStream;
        synchronized (oStream) {
            return this.protocol.importEnd(stream.getInputStream(), stream.getBufferedOutputStream());
        }
    }

    public Map<Integer, Node> getNodeMap() {
        return this.nodeMap;
    }

    public void close() {
        if (this.nodeMap != null) {
            for (Node node : this.nodeMap.values()) {
                node.close();
            }
            this.nodeMap.clear();
        }
        this.nodeMap = null;
        this.clusterConn = null;
        this.protocol = null;
    }

    public ImportCredential getClusterImportCredential() throws SQLException {
        if (this.importCredential == null) {
            this.importCredential = this.getImportCredential();
        }
        return this.importCredential;
    }

    public void setClusterImportCredential(ImportCredential importCredential) {
        this.importCredential = importCredential;
    }

    static class ImportCredential {
        private int globalID;
        private int identificationCode;

        public ImportCredential(int globalID, int identificationCode) {
            this.globalID = globalID;
            this.identificationCode = identificationCode;
        }

        public int getGlobalID() {
            return this.globalID;
        }

        public int getIdentificationCode() {
            return this.identificationCode;
        }
    }
}

