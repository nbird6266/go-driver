/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;

class SocketBasedSocketImpl
extends SocketImpl {
    private Socket socket;

    SocketBasedSocketImpl(Socket socket) {
        this.socket = socket;
        this.address = socket.getInetAddress();
        this.port = socket.getPort();
        this.localport = socket.getLocalPort();
    }

    protected void create(boolean stream) {
    }

    protected void connect(String host, int port) {
    }

    protected void connect(InetAddress address, int port) {
    }

    protected void bind(InetAddress host, int port) {
    }

    protected void listen(int backlog) {
    }

    protected void accept(SocketImpl s) {
    }

    protected int available() {
        return 0;
    }

    protected InputStream getInputStream() throws IOException {
        return this.socket.getInputStream();
    }

    protected OutputStream getOutputStream() throws IOException {
        return this.socket.getOutputStream();
    }

    protected void close() throws IOException {
        this.socket.close();
    }

    protected void sendUrgentData(int data) throws IOException {
    }

    protected void connect(SocketAddress address, int timeout) throws IOException {
    }

    public void setOption(int optID, Object value) throws SocketException {
        switch (optID) {
            case 1: {
                this.socket.setTcpNoDelay((Boolean)value);
                break;
            }
            case 128: {
                if (value instanceof Integer) {
                    this.socket.setSoLinger(true, (Integer)value);
                    break;
                }
                this.socket.setSoLinger(false, -1);
                break;
            }
            case 4102: {
                this.socket.setSoTimeout((Integer)value);
                break;
            }
            default: {
                throw new SocketException("Unexpected option " + optID);
            }
        }
    }

    public Object getOption(int optID) throws SocketException {
        switch (optID) {
            case 1: {
                return new Boolean(this.socket.getTcpNoDelay());
            }
            case 15: {
                return this.socket.getLocalAddress();
            }
            case 128: {
                int linger = this.socket.getSoLinger();
                return linger < 0 ? (Comparable<Boolean>)Boolean.FALSE : (Comparable<Boolean>)new Integer(linger);
            }
            case 4102: {
                return new Integer(this.socket.getSoTimeout());
            }
        }
        throw new SocketException("Unexpected option " + optID);
    }
}

