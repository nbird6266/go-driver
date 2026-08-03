/*
 * Decompiled with CFR 0.152.
 */
package com.google.code.juds;

import com.google.code.juds.UnixDomainSocket;
import java.io.IOException;
import java.io.InputStream;

public class UnixDomainSocketClient
extends UnixDomainSocket {
    public UnixDomainSocketClient(String socketFile, int socketType) throws IOException {
        this.socketFile = socketFile;
        this.socketType = socketType;
        this.nativeSocketFileHandle = UnixDomainSocketClient.nativeOpen(socketFile, socketType);
        if (this.nativeSocketFileHandle == -1) {
            throw new IOException("Unable to open Unix domain socket");
        }
        if (socketType == 1) {
            this.in = new UnixDomainSocket.UnixDomainSocketInputStream();
        }
        this.out = new UnixDomainSocket.UnixDomainSocketOutputStream();
    }

    public InputStream getInputStream() {
        if (this.socketType == 1) {
            return this.in;
        }
        throw new UnsupportedOperationException();
    }
}

