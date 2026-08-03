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

public class CursorResponsePacket
extends BasePacket {
    private static final char tag = 'P';
    private byte[] cursorName = null;

    public byte[] getCursorName() {
        return this.cursorName;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        this.cursorName = this.getProtocolVersion() != null && this.getProtocolVersion().getProtocolType() >= 2 ? BasePacket.ReceiveStringByLen(stream) : BasePacket.ReceiveString(stream);
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive CursorResponsePacket 'P': ").append("\n");
            this.sb.append("cursorName: ");
            this.append(this.sb, this.cursorName);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public char getTag() {
        return 'P';
    }
}

