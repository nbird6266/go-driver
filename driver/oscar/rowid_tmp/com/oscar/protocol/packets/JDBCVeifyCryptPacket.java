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

public class JDBCVeifyCryptPacket
extends BasePacket {
    private static final char tag = '\u0000';
    private byte[] cryptNum = null;

    private JDBCVeifyCryptPacket() {
    }

    public JDBCVeifyCryptPacket(byte[] cryptNum) {
        this.cryptNum = cryptNum;
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        BasePacket.SendInteger(stream, this.cryptNum.length, 4);
        BasePacket.Send(stream, this.cryptNum);
        stream.flush();
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send: '0'").append("\n");
            this.sb.append("cryptNum Len: ").append(this.cryptNum.length);
            this.sb.append(", cryptNum: ");
            this.append(this.sb, this.cryptNum);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public static void main(String[] args) {
        JDBCVeifyCryptPacket jdbcveifycryptpacket = new JDBCVeifyCryptPacket();
    }

    public char getTag() {
        return '\u0000';
    }
}

