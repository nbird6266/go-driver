/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.protocol.packets.BasePacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class BlogDataPacket
extends BasePacket {
    private static final char tag = 'B';
    private static final int SM_LEN = 4;
    private byte[] tuple;
    private int dataLen;
    private int pos;

    public BlogDataPacket() {
    }

    public BlogDataPacket(byte[] tuple, int pos, int dataLen) {
        this.tuple = tuple;
        this.pos = pos;
        this.dataLen = dataLen;
    }

    public void setConnection(BaseConnection conn) {
        super.setConnection(conn);
    }

    public byte[] getTuple() {
        return this.tuple;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        stream.write(66);
        BasePacket.SendInteger(stream, this.dataLen, 4);
        BasePacket.Send(stream, this.tuple, this.pos, this.dataLen);
        stream.flush();
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive BlogDataPacket 'B': ").append("\n");
        }
        int dataLen = BasePacket.ReceiveIntegerR(stream, 4);
        this.tuple = BasePacket.Receive(stream, dataLen);
        if (this.logFlag) {
            this.sb.append("Event len: ").append(dataLen);
            this.sb.append("\n");
            Driver.writeLog(this.sb.toString());
            this.sb.delete(0, this.sb.length());
        }
    }

    public char getTag() {
        return 'B';
    }
}

