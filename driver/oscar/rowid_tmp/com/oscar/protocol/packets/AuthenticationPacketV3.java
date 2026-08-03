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

public class AuthenticationPacketV3
extends BasePacket {
    public static final char tag = '\uffb1';
    private static final int SM_LEN = 4;
    private static final int SM_AUTHPOLICY = 4;
    private static final int SM_MD5SALT = 4;
    private static final int AUTH_REQ_MD5 = 5;
    private int authenPolicy;
    private int versionNum;
    private Object salt;

    public Object getSalt() {
        return this.salt;
    }

    public int getAuthenPolicy() {
        return this.authenPolicy;
    }

    public int getVersionNum() {
        return this.versionNum;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive AuthenticationPacketV3 '0xB1': ").append("\n");
        }
        this.versionNum = BasePacket.ReceiveIntegerR(stream, 2);
        this.authenPolicy = BasePacket.ReceiveIntegerR(stream, 4);
        if (this.logFlag) {
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive : ");
            this.sb.append("authenPolicy: ").append(this.authenPolicy).append(", send 'R': ").append("\n");
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
        return '\uffb1';
    }
}

