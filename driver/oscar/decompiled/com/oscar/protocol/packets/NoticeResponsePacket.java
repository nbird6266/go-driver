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

public class NoticeResponsePacket
extends BasePacket {
    private static final char tag = 'N';
    private byte[] noticeMessage;
    private byte[] sqlState;
    private int errorCode;

    public byte[] getSQLState() {
        return this.sqlState;
    }

    public byte[] getNoticeMessage() {
        return this.noticeMessage;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive NoticeResponsePacket 'N': ").append("\n");
        }
        if (this.version.isReceiveErrorCode()) {
            this.errorCode = BasePacket.ReceiveIntegerR(stream, 4);
            if (this.logFlag) {
                this.sb.append("errorCode: ").append(this.errorCode);
            }
        }
        this.sqlState = BasePacket.ReceiveString(stream);
        this.noticeMessage = BasePacket.ReceiveString(stream);
        if (this.logFlag) {
            boolean encoding = false;
            if (this.conn != null && this.conn.getClientEncoding() != null) {
                encoding = true;
            }
            this.sb.append(", sqlState: ");
            if (encoding) {
                this.sb.append(this.conn.getClientEncoding().decode(this.sqlState));
            } else {
                this.append(this.sb, this.sqlState);
            }
            this.sb.append(", errorMessage: ");
            if (encoding) {
                this.sb.append(this.conn.getClientEncoding().decode(this.noticeMessage));
            } else {
                this.append(this.sb, this.noticeMessage);
            }
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public char getTag() {
        return 'N';
    }
}

