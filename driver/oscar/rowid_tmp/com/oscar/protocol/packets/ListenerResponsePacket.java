/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.Driver;
import com.oscar.protocol.packets.BasePacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class ListenerResponsePacket
extends BasePacket {
    private static final char tag = 'L';
    private int version = 0;
    private int port = 0;

    public int getListenerVersion() {
        return this.version;
    }

    public int getDbPort() {
        return this.port;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        this.version = BasePacket.ReceiveIntegerR(stream, 4);
        this.port = BasePacket.ReceiveIntegerR(stream, 4);
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive ListenerResponsePacket 'L': ").append("\n");
            this.sb.append("version: ").append(this.version).append(", port: ").append(this.port);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public char getTag() {
        return 'L';
    }
}

