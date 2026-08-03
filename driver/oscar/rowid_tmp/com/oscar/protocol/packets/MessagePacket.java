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
import java.util.HashMap;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MessagePacket
extends BasePacket {
    private static final char tag = 'S';
    protected StringBuffer sb = new StringBuffer();
    protected boolean logFlag = Driver.getLogLevel() >= 4;
    public static String SERVER_VERSION = "server_version";
    private HashMap<String, String> messages = new HashMap();

    @Override
    public char getTag() {
        return 'S';
    }

    @Override
    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        int length = BasePacket.ReceiveIntegerR(stream, 4);
        String name = new String(BasePacket.ReceiveString(stream));
        String value = new String(BasePacket.ReceiveString(stream));
        if (name != null && value != null) {
            this.messages.put(name, value);
        }
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive : ");
            this.sb.append('S').append(": ").append("\n").append(name).append(":").append(value);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    @Override
    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public HashMap<String, String> getMessageMap() {
        return this.messages;
    }
}

