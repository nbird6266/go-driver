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

public class FunctionResponsePacket
extends BasePacket {
    private static final char tag = 'V';
    private static int SM_ISNULL = 1;
    private static int SM_LEN = 4;
    private static int SM_UNUSED = 1;
    private int isNull = 48;
    private int resultSize = 0;
    private byte[] result = null;
    private byte unused = (byte)48;

    public boolean isNull() {
        return this.isNull != 71;
    }

    public int getResultSize() {
        return this.resultSize;
    }

    public byte[] getResult() {
        return this.result;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive FunctionResponsePacket 'V': ").append("\n");
        }
        this.isNull = BasePacket.ReceiveChar(stream);
        if (this.logFlag) {
            this.sb.append("isNull :").append(this.isNull);
        }
        if (this.isNull == 71) {
            this.resultSize = BasePacket.ReceiveIntegerR(stream, SM_LEN);
            this.result = BasePacket.Receive(stream, this.resultSize);
            this.unused = BasePacket.Receive(stream, SM_UNUSED)[SM_UNUSED - 1];
            if (this.logFlag) {
                this.sb.append(", resultSize: ").append(this.resultSize);
                this.sb.append(", result: ");
                this.append(this.sb, this.result);
                this.sb.append(", unused: ").append(this.unused);
            }
        }
        if (this.logFlag) {
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public char getTag() {
        return 'V';
    }
}

