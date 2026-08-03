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

public class BackupPhysicalDataPacket
extends BasePacket {
    private char tag = (char)77;
    private int length = 4;
    private byte[] physicalData;

    public BackupPhysicalDataPacket() {
    }

    public BackupPhysicalDataPacket(byte[] physicalData) {
        this.physicalData = physicalData;
    }

    public char getTag() {
        return this.tag;
    }

    public byte[] getPhysicalData() {
        return this.physicalData;
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive BackupPhysicalDataPacket 'M': ").append("\n");
        }
        int dataLength = BasePacket.ReceiveIntegerR(stream, 4);
        this.physicalData = BasePacket.Receive(stream, dataLength);
        if (this.logFlag) {
            this.sb.append("physicalData: ");
            this.append(this.sb, this.physicalData);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send 'M': ").append("\n");
        }
        if (this.physicalData != null) {
            BasePacket.SendChar(stream, this.tag);
            BasePacket.SendInteger(stream, this.physicalData.length, this.length);
            BasePacket.Send(stream, this.physicalData);
            if (this.logFlag) {
                this.sb.append("physicalData Len: ").append(this.physicalData.length);
                this.sb.append(", physicalData: ");
                this.append(this.sb, this.physicalData);
            }
        }
        stream.flush();
        if (this.logFlag) {
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }
}

