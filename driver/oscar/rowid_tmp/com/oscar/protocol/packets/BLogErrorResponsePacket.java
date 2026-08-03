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

public class BLogErrorResponsePacket
extends BasePacket {
    private static final char tag = 'k';
    private int errorCode;
    private byte[] errorMessage;
    private byte[] sqlState;

    public byte[] getErrorMessage() {
        return this.errorMessage;
    }

    public byte[] getSQLState() {
        return this.sqlState;
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
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive AsciiRowPacketV2 'D': ").append("\n");
        }
        if (this.version.isReceiveErrorCode()) {
            this.errorCode = BasePacket.ReceiveIntegerR(stream, 4);
            if (this.logFlag) {
                this.sb.append("errorCode: ").append(this.errorCode);
            }
        }
        this.sqlState = BasePacket.ReceiveString(stream);
        this.errorMessage = BasePacket.ReceiveString(stream);
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
                this.sb.append(this.conn.getClientEncoding().decode(this.errorMessage));
            } else {
                this.append(this.sb, this.errorMessage);
            }
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public char getTag() {
        return 'k';
    }
}

