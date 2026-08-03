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

public class FetchMorePacket
extends BasePacket {
    private char fetchMoretag = (char)8;
    private char fetchCloseTag = (char)9;
    private int fetchSize;
    private byte[] planID;
    private boolean finished;

    public FetchMorePacket(int fetchSize, byte[] planID, boolean finished) {
        this.fetchSize = fetchSize;
        this.planID = planID;
        this.finished = finished;
    }

    public char getTag() {
        return this.fetchMoretag;
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send: ").append("\n");
        }
        if (!this.finished) {
            BasePacket.SendChar(stream, this.fetchMoretag);
            BasePacket.Send(stream, this.planID);
            BasePacket.SendInteger(stream, this.fetchSize, 2);
            if (this.logFlag) {
                this.sb.append("fetchMore: 0x08, planID: ");
                for (int i = 0; i < this.planID.length; ++i) {
                    this.sb.append(this.planID[i]).append(" ");
                }
                this.sb.append(", fetchSize: " + this.fetchSize);
            }
        } else {
            BasePacket.SendChar(stream, this.fetchCloseTag);
            BasePacket.Send(stream, this.planID);
            if (this.logFlag) {
                this.sb.append("fetchClose: 0x09, planID: ");
                for (int i = 0; i < this.planID.length; ++i) {
                    this.sb.append(this.planID[i]).append(" ");
                }
            }
        }
        stream.flush();
        if (this.logFlag) {
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }
}

