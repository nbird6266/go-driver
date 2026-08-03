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

public class ExportBinlogSuccessPacket
extends BasePacket {
    private static final char tag = 'f';
    protected String curFile;
    protected long curPos;
    protected int flag;

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        int dataLen = BasePacket.ReceiveIntegerR(stream, 2);
        this.curFile = new String(BasePacket.ReceiveStringByLen(stream, dataLen), "GBK");
        this.curPos = BasePacket.ReceiveIntegerR(stream, 4);
        this.flag = BasePacket.ReceiveIntegerR(stream, 1);
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive BlogDataPacket 'f': ").append("\n");
            this.sb.append("endfile: ").append(this.curFile).append("\n");
            this.sb.append("endpos: ").append(this.curPos).append("\n");
            Driver.writeLog(this.sb.toString());
            this.sb.delete(0, this.sb.length());
        }
    }

    public String getCurfile() {
        return this.curFile;
    }

    public long getCurpos() {
        return this.curPos;
    }

    public int getFlag() {
        return this.flag;
    }

    public char getTag() {
        return 'f';
    }
}

