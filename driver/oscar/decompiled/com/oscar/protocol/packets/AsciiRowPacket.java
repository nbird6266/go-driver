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

public class AsciiRowPacket
extends BasePacket {
    private static final char tag = 'D';
    private static final int SM_LEN = 4;
    private int numberOfField;
    private byte[][] tuple;

    public void initTuple(int nf) {
        this.numberOfField = nf;
        this.tuple = new byte[this.numberOfField][0];
    }

    public byte[][] getTuple() {
        return this.tuple;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive AsciiRowPacket 'D': ").append("\n");
        }
        int bim = (this.numberOfField + 7) / 8;
        byte[] bitmask = BasePacket.Receive(stream, bim);
        if (this.logFlag) {
            this.sb.append("bitmask: ");
            this.append(this.sb, bitmask);
        }
        int whichbit = 128;
        int whichbyte = 0;
        for (int i = 0; i < this.numberOfField; ++i) {
            boolean isNull;
            boolean bl = isNull = (bitmask[whichbyte] & whichbit) == 0;
            if ((whichbit >>= 1) == 0) {
                ++whichbyte;
                whichbit = 128;
            }
            if (isNull) {
                this.tuple[i] = null;
            } else {
                int len = BasePacket.ReceiveIntegerR(stream, 4);
                if ((len -= 4) < 0) {
                    len = 0;
                }
                this.tuple[i] = BasePacket.Receive(stream, len);
            }
            if (!this.logFlag) continue;
            this.append(this.sb, this.tuple[i]);
            this.sb.append("|");
        }
        if (this.logFlag) {
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public char getTag() {
        return 'D';
    }
}

