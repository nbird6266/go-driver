/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.Driver;
import com.oscar.core.Encoding;
import com.oscar.protocol.packets.BasePacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class SSLRequestPacket
extends BasePacket {
    private static final char tag = '\u0000';
    private String requestSSL;
    private int packetLen = 0;

    public SSLRequestPacket(boolean useSSL) {
        if (useSSL) {
            this.requestSSL = "SECUREREQUEST";
            this.packetLen = 18;
        } else {
            this.requestSSL = "SECUREUNREQUEST";
            this.packetLen = 20;
        }
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        BasePacket.SendInteger(stream, this.packetLen, 4);
        BasePacket.SendString(stream, this.requestSSL, this.packetLen - 4, Encoding.getEncoding("ASCII"));
        stream.flush();
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send : ").append("\n");
            this.sb.append("packetLen: ").append(this.packetLen).append(", requestSSL").append(this.requestSSL);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public char getTag() {
        return '\u0000';
    }
}

