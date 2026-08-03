/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

public class Node {
    private int nodeId;
    private String host;
    private int port;
    private String driverName;
    private String url;

    public Node(int nodeId, String host, int port, String driverName, String url) {
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
        this.driverName = driverName;
        this.url = url;
    }

    public int getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDriverName() {
        return this.driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

