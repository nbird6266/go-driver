/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.util.Node;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class MetaData {
    private Connection conn;
    private HashMap nodeList;

    public MetaData() {
    }

    public MetaData(Connection conn) throws SQLException {
        this.conn = conn;
        this.initClusterNodeList();
    }

    public void initClusterNodeList() throws SQLException {
        ResultSet rs = this.executeQuery("SELECT * from CSYS_NODE");
        Node node = null;
        int nodeId = 0;
        String host = "";
        int port = 0;
        String driver = "";
        String url = "";
        this.nodeList = new HashMap();
        while (rs.next()) {
            nodeId = rs.getInt("nodeid");
            host = rs.getString("host");
            port = rs.getInt("port");
            driver = rs.getString("driver");
            url = rs.getString("url");
            node = new Node(nodeId, host, port, driver, url);
            this.nodeList.put(new Integer(nodeId), node);
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        PreparedStatement pstmt = this.conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        return rs;
    }

    public int executeUpdate(String sql) throws SQLException {
        PreparedStatement pstmt = this.conn.prepareStatement(sql);
        int count = pstmt.executeUpdate();
        return count;
    }

    public String getUrl(Integer nodeId) {
        return ((Node)this.nodeList.get(nodeId)).getUrl();
    }

    public HashMap getNodeList() {
        return this.nodeList;
    }
}

