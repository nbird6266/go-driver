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

public class BackupMetaDataPacket
extends BasePacket {
    private char tag = (char)109;
    private int length = 4;
    private byte[] metaData;

    public BackupMetaDataPacket() {
    }

    public BackupMetaDataPacket(byte[] metaData) {
        this.metaData = metaData;
    }

    public char getTag() {
        return this.tag;
    }

    public byte[] getMetaData() {
        return this.metaData;
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive BackupMetaDataPacket 'm': ").append("\n");
        }
        int dataLength = BasePacket.ReceiveIntegerR(stream, 4);
        this.metaData = BasePacket.Receive(stream, dataLength);
        if (this.logFlag) {
            this.sb.append("metaData: ");
            this.append(this.sb, this.metaData);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send 'm': ").append("\n");
        }
        if (this.metaData != null) {
            BasePacket.SendChar(stream, this.tag);
            BasePacket.SendInteger(stream, this.metaData.length, this.length);
            BasePacket.Send(stream, this.metaData);
            if (this.logFlag) {
                this.sb.append("metaData Len: ").append(this.metaData.length);
                this.sb.append(", metaData: ");
                this.append(this.sb, this.metaData);
            }
        }
        stream.flush();
        if (this.logFlag) {
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }
}

