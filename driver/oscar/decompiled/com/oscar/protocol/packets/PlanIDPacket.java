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

public class PlanIDPacket
extends BasePacket {
    public static final char tag = '\uffa4';
    private byte[] planID;

    public char getTag() {
        return '\uffa4';
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        this.planID = new byte[2];
        stream.read(this.planID, 0, 2);
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive(0xA4): planID: ");
            for (int i = 0; i < this.planID.length; ++i) {
                this.sb.append(this.planID[i]).append(" ");
            }
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public byte[] getPlanID() {
        return this.planID;
    }
}

