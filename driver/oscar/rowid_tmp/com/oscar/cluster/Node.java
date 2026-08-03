/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.cluster;

import com.oscar.Driver;
import com.oscar.cluster.Cluster;
import com.oscar.cluster.NodeConnectionManager;
import com.oscar.cluster.Protocol;
import com.oscar.cluster.core.DataImportStream;
import com.oscar.cluster.core.NodeProtocol;
import com.oscar.core.Encoding;
import com.oscar.util.OSQLException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.SQLException;

public class Node
implements DataImportStream {
    private Encoding encoding;
    private int nodeID;
    private int port;
    private String host;
    private Socket connection;
    private NodeProtocol protocol;
    private InputStream in;
    private OutputStream out;
    private static boolean logFlag = Driver.getLogLevel() >= 2;

    public Node(int nodeID, String host, int port, Encoding encoding) {
        this.nodeID = nodeID;
        this.host = host;
        this.port = port;
        this.encoding = encoding;
        this.protocol = new Protocol(encoding);
    }

    public boolean isOpen() {
        return this.connection != null && this.in != null && this.out != null;
    }

    public synchronized void connect(Cluster.ImportCredential importCredential) throws SQLException {
        if (this.isOpen()) {
            try {
                this.connection.sendUrgentData(255);
                if (logFlag) {
                    Driver.writeLog(this.toString() + "::::\u8282\u70b9\u5fc3\u8df3\u6b63\u5e38");
                }
                return;
            }
            catch (IOException e) {
                if (logFlag) {
                    Driver.writeLog(this.toString() + "\u8282\u70b9\u5fc3\u8df3\u5f02\u5e38", e);
                }
                this.disConnect();
            }
        }
        try {
            this.connection = NodeConnectionManager.instance().getConnection(this.host, this.port);
            this.in = this.connection.getInputStream();
            this.out = this.connection.getOutputStream();
            this.sendImportCredential(importCredential);
        }
        catch (IOException e) {
            if (logFlag) {
                Driver.writeLog(this.toString() + "\u521b\u5efatcp\u8fde\u63a5\u5f02\u5e38", e);
            }
            throw new OSQLException("OSCAR-00804", "88888", 804, e);
        }
    }

    public synchronized void sendImportCredential(Cluster.ImportCredential credential) throws SQLException {
        if (!this.isOpen()) {
            throw new SQLException("From JDBC: The connection has been closed");
        }
        this.protocol.sendImportCredential(credential.getGlobalID(), credential.getIdentificationCode(), this.in, this.out);
    }

    public synchronized void importEnd() throws SQLException {
        if (!this.isOpen()) {
            throw new SQLException("From JDBC: The connection has been closed");
        }
        this.protocol.nodeImportEnd(this.in, this.out);
    }

    public synchronized void importData(byte[] data) throws SQLException {
        if (data.length != 0) {
            if (!this.isOpen()) {
                throw new SQLException("From JDBC: The connection has been closed");
            }
            this.protocol.importData2Node(data, this.in, this.out);
        }
    }

    public synchronized void disConnect() {
        try {
            if (this.in != null) {
                this.in.close();
            }
            if (this.out != null) {
                this.out.close();
            }
            if (this.connection != null) {
                NodeConnectionManager.instance().closeConnection(this.connection);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.in = null;
        this.out = null;
        this.connection = null;
    }

    public synchronized void close() {
        this.disConnect();
        this.protocol = null;
        this.encoding = null;
    }

    public int getNodeID() {
        return this.nodeID;
    }

    public int getPort() {
        return this.port;
    }

    public String getHost() {
        return this.host;
    }

    public String toString() {
        return "Node [nodeID=" + this.nodeID + ", port=" + this.port + ", host=" + this.host + "]";
    }
}

