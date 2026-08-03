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

public class JDBCVeifyModePacket
extends BasePacket {
    private static final char tag = '\u0000';
    private int mode = 0;

    public int getMode() {
        return this.mode;
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        this.mode = BasePacket.ReceiveIntegerR(stream, 1);
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive JDBCVeifyModePacket '0': ").append("\n");
            this.sb.append("mode: ").append(this.mode);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public static void main(String[] args) {
        JDBCVeifyModePacket jdbcveifymodepacket = new JDBCVeifyModePacket();
    }

    public char getTag() {
        return '\u0000';
    }
}

