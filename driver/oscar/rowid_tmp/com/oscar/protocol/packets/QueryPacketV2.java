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

public class QueryPacketV2
extends BasePacket {
    private static final char tag = '\u0001';
    private byte[] query;
    private int marked = 0;
    private StringBuffer sb = new StringBuffer();

    public QueryPacketV2(byte[] request, int marked) {
        this.query = request;
        this.marked = marked;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send: ").append("\n");
        }
        BasePacket.SendChar(stream, 1);
        BasePacket.SendInteger(stream, this.marked, 2);
        BasePacket.Send(stream, this.query);
        BasePacket.SendChar(stream, 0);
        stream.flush();
        if (this.logFlag) {
            this.sb.append("query: ").append(1).append(", query num: ").append(this.marked).append(", sql :");
            for (int i = 0; i < this.query.length; ++i) {
                this.sb.append(this.query[i]).append(" ");
            }
            this.sb.append("0");
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public char getTag() {
        return '\u0001';
    }
}

