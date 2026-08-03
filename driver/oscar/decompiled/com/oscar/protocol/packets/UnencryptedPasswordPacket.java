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

public class UnencryptedPasswordPacket
extends BasePacket {
    private static int SM_LEN = 4;
    private static final char tag = 'U';
    private byte[] password;

    public UnencryptedPasswordPacket(byte[] passwd) {
        this.password = passwd;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        BasePacket.SendInteger(stream, 5 + this.password.length, SM_LEN);
        BasePacket.Send(stream, this.password);
        BasePacket.SendInteger(stream, 0, 1);
        stream.flush();
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send: UnencryptedPasswordPacket ").append("\n");
            this.sb.append("len: ").append(5 + this.password.length).append(", password: ");
            this.append(this.sb, this.password);
            this.sb.append(0);
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public char getTag() {
        return 'U';
    }
}

