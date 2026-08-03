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

public class BackendKeyPacket
extends BasePacket {
    private static final char tag = 'K';
    private static final int SM_PID = 4;
    private static final int SM_CKEY = 4;
    private int pid;
    private int ckey;

    public int getPID() {
        return this.pid;
    }

    public int getCKey() {
        return this.ckey;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive BackendKeyPacket 'K': ").append("\n");
        }
        this.pid = BasePacket.ReceiveIntegerR(stream, 4);
        this.ckey = BasePacket.ReceiveIntegerR(stream, 4);
        if (this.logFlag) {
            this.sb.append("pid: ").append(this.pid).append(", ckey").append(this.ckey);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public char getTag() {
        return 'K';
    }
}

