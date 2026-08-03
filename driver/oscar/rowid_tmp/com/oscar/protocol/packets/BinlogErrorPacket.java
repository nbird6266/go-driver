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

public class BinlogErrorPacket
extends BasePacket {
    private static final char tag = 'e';
    private String errorMessage;
    private int curPos;
    private int errorCode;

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public int getCurpos() {
        return this.curPos;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive BinlogErrorPacket 'e': ").append("\n");
        }
        this.curPos = BasePacket.ReceiveIntegerR(stream, 4);
        int len = BasePacket.ReceiveIntegerR(stream, 4);
        this.errorMessage = new String(BasePacket.ReceiveStringByLen(stream, len), "GBK");
        if (this.logFlag) {
            this.sb.append(", curPos: ").append(this.curPos).append(", errorMessage: ").append(this.errorMessage);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
            this.sb.delete(0, this.sb.length());
        }
    }

    public char getTag() {
        return 'e';
    }
}

