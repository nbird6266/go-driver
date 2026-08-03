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

public class CancelRequestPacket
extends BasePacket {
    private static final char tag = 'Y';
    private static final int SM_LEN = 4;
    private static final int SM_CODE = 4;
    private static final int SM_PID = 4;
    private static final int SM_SKEY = 4;
    private int pid;
    private int skey;
    private static int CANCLE_REQUEST = 80877102;

    public CancelRequestPacket(int p_id, int s_key) {
        this.pid = p_id;
        this.skey = s_key;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send '").append(CANCLE_REQUEST).append("': ");
            this.sb.append("lenth(4 bytes): ").append(16);
            this.sb.append(", cancle_request: ").append(CANCLE_REQUEST);
            this.sb.append(", pid").append(this.pid).append(", skey").append(this.skey);
        }
        BasePacket.SendInteger(stream, 16, 4);
        BasePacket.SendInteger(stream, CANCLE_REQUEST, 4);
        BasePacket.SendInteger(stream, this.pid, 4);
        BasePacket.SendInteger(stream, this.skey, 4);
        stream.flush();
        if (this.logFlag) {
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public char getTag() {
        return 'Y';
    }
}

