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

public class JDBCVeifyRandomPacket
extends BasePacket {
    private static final char tag = '\u0000';
    private byte[] random = null;

    public byte[] getRandom() {
        return this.random;
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        int size = 0;
        size = BasePacket.ReceiveIntegerR(stream, 4);
        this.random = BasePacket.Receive(stream, size - 4);
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive JDBCVeifyRandomPacket '0': ").append("\n");
            this.sb.append("mode: ");
            this.append(this.sb, this.random);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public static void main(String[] args) {
        JDBCVeifyRandomPacket jdbcveifyrandompacket = new JDBCVeifyRandomPacket();
    }

    public char getTag() {
        return '\u0000';
    }
}

