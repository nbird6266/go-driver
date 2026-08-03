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

public class AuthenticationPacketV2
extends BasePacket {
    public static final char tag = '\uffa1';
    private static final int SM_AUTHPOLICY = 4;
    private static final int SM_SALT = 2;
    private static final int SM_MD5SALT = 4;
    private static final int AUTH_REQ_OK = 0;
    private static final int AUTH_REQ_PASSWORD = 3;
    private static final int AUTH_REQ_MD5 = 5;
    private static final int AUTH_REQ_SCM = 6;
    private int authenPolicy;
    private Object salt = null;

    public int getAuthenPolicy() {
        return this.authenPolicy;
    }

    public Object getSalt() {
        return this.salt;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive AuthenticationPacketV2 '0xA1': ").append("\n");
        }
        this.authenPolicy = BasePacket.ReceiveIntegerR(stream, 4);
        if (this.logFlag) {
            this.sb.append("authenPolicy: ").append(this.authenPolicy);
        }
        if (this.authenPolicy == 5) {
            byte[] md5Salt = new byte[]{(byte)BasePacket.ReceiveChar(stream), (byte)BasePacket.ReceiveChar(stream), (byte)BasePacket.ReceiveChar(stream), (byte)BasePacket.ReceiveChar(stream)};
            this.salt = md5Salt;
            if (this.logFlag) {
                for (int i = 0; i < md5Salt.length; ++i) {
                    this.sb.append(md5Salt[i]).append(" ");
                }
            }
        }
        if (this.logFlag) {
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public char getTag() {
        return '\uffa1';
    }
}

