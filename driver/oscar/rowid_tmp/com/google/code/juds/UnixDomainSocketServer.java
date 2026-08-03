/*
 * Decompiled with CFR 0.152.
 */
package com.google.code.juds;

import com.google.code.juds.UnixDomainSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UnixDomainSocketServer
extends UnixDomainSocket {
    public UnixDomainSocketServer(String socketFile, int socketType) throws IOException {
        this.socketFile = socketFile;
        this.socketType = socketType;
        this.nativeSocketFileHandle = UnixDomainSocketServer.nativeCreate(socketFile, socketType);
        if (this.nativeSocketFileHandle == -1) {
            throw new IOException("Unable to open Unix domain socket");
        }
        this.in = new UnixDomainSocket.UnixDomainSocketInputStream();
        if (socketType == 1) {
            this.out = new UnixDomainSocket.UnixDomainSocketOutputStream();
        }
    }

    public UnixDomainSocketServer(String pSocketFile, int pSocketType, int backlog) throws IOException {
        this.socketFile = pSocketFile;
        this.socketType = pSocketType;
        this.nativeSocketFileHandle = UnixDomainSocketServer.nativeListen(this.socketFile, this.socketType, backlog);
        if (this.nativeSocketFileHandle == -1) {
            throw new IOException("Unable to open and listen on Unix domain socket");
        }
    }

    public UnixDomainSocket accept() throws IOException {
        int newSocketFileHandle = -1;
        newSocketFileHandle = UnixDomainSocketServer.nativeAccept(this.nativeSocketFileHandle, this.socketType);
        if (newSocketFileHandle == -1) {
            throw new IOException("Unable to accept on Unix domain socket");
        }
        return new UnixDomainSocket(newSocketFileHandle, this.socketType){};
    }

    public OutputStream getOutputStream() {
        if (this.out == null) {
            throw new UnsupportedOperationException();
        }
        return this.out;
    }

    public InputStream getInputStream() {
        if (this.in == null) {
            throw new UnsupportedOperationException();
        }
        return this.in;
    }
}

