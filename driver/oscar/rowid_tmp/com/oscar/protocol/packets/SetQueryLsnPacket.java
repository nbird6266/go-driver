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

public class SetQueryLsnPacket
extends BasePacket {
    private char tag = (char)14;
    private long lsn;

    public SetQueryLsnPacket(long masterLsn) {
        this.lsn = masterLsn;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send: ").append("\n");
        }
        BasePacket.SendChar(stream, this.tag);
        BasePacket.SendLong(stream, this.lsn, 8);
        if (this.logFlag) {
            this.sb.append("set lsn= ").append(this.lsn);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public char getTag() {
        return this.tag;
    }
}

