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

public class CompleteResponsePacket
extends BasePacket {
    private static final char tag = 'C';
    private byte[] command;

    public byte[] getCommand() {
        return this.command;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        this.command = this.conn.isReceiveStringByLen() ? BasePacket.ReceiveStringByLen(stream) : BasePacket.ReceiveString(stream);
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive CompleteResponsePacket 'C': ").append("\n");
            this.sb.append("command: ");
            this.append(this.sb, this.command);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public char getTag() {
        return 'C';
    }
}

