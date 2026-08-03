/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.cluster;

import java.io.IOException;
import java.net.Socket;

public class NodeConnectionManager {
    public static volatile NodeConnectionManager instance;

    private NodeConnectionManager() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static NodeConnectionManager instance() {
        if (instance != null) return instance;
        Class<NodeConnectionManager> clazz = NodeConnectionManager.class;
        synchronized (NodeConnectionManager.class) {
            if (instance != null) return instance;
            instance = new NodeConnectionManager();
            // ** MonitorExit[var0] (shouldn't be in output)
            return instance;
        }
    }

    public Socket getConnection(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);
        return socket;
    }

    public void closeConnection(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }
}

