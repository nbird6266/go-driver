/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.core.Encoding;
import com.oscar.protocol.packets.BasePacket;
import com.oscar.util.HdEncrypt;
import com.oscar.util.OSQLException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class HdEncryptRequestPacket
extends BasePacket {
    private static final String SECURE = "SECUREHDREQUEST";
    private static final String UNSECURE = "SECUREUNREQUEST";
    private static final int sizeOfHeaderLen = 4;
    private String requestSSL;
    private int headerLen = 0;
    private BaseConnection conn;
    private boolean secure = false;

    public HdEncryptRequestPacket(BaseConnection conn, boolean secure) {
        this.conn = conn;
        this.secure = secure;
        this.requestSSL = secure ? SECURE : UNSECURE;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        if (this.secure) {
            try {
                HdEncrypt.instance().open();
            }
            catch (Exception e) {
                throw new OSQLException("OSCAR-00125", "88888", 125, e);
            }
        }
        this.headerLen = this.requestSSL.length() + 4 + 1;
        BasePacket.SendInteger(stream, this.headerLen, 4);
        BasePacket.SendString(stream, this.requestSSL, this.headerLen - 4, Encoding.getEncoding("ASCII"));
        if (this.secure) {
            byte[] key = HdEncrypt.instance().hdGenerateKey();
            byte[] encryptedKey = HdEncrypt.instance().hdAsymEncrypt(key);
            BasePacket.SendInteger(stream, encryptedKey.length, 2);
            BasePacket.Send(stream, encryptedKey);
            this.conn.setHdSymEncryptKey(key);
        }
        stream.flush();
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send : ").append("\n");
            this.sb.append("packetLen: ").append(this.headerLen).append(", requestSSL").append(this.requestSSL);
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

