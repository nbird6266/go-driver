/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.Driver;
import com.oscar.dispatcher.entity.DispatchConnection;
import com.oscar.dispatcher.entity.LsnVo;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.protocol.packets.BasePacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class ReadyForQueryPacket
extends BasePacket {
    private static final char tag = 'Z';
    private byte[] lsn;

    public byte[] getLsn() {
        return this.lsn;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.conn.getProtocolVersion().getProtocolType() >= 3) {
            if (this.logFlag) {
                this.sb.delete(0, this.sb.length());
                this.sb.append("***********************************************************").append("\n");
                this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive ReadyForQueryPacket 'Z': ").append("\n");
            }
            int transStatus = BasePacket.ReceiveChar(stream);
            this.conn.setTransStatus(transStatus);
            this.lsn = BasePacket.Receive(stream, 8);
            if (this.conn.isUseSlaveSynRead()) {
                LsnVo lv;
                long value = ReadyForQueryPacket.getLong(this.lsn);
                DispatchConnection dispatchConn = ((OscarJdbc2Connection)this.conn).getDispatchConn();
                if (dispatchConn != null && value > 0L && value > (lv = dispatchConn.getLsnVo()).getMasterLsn()) {
                    lv.setMasterLsn(value);
                    if (this.logFlag) {
                        this.sb.append("MasterLsn :").append(value);
                        this.sb.append("\n").append("***********************************************************");
                        Driver.writeLog(this.sb.toString());
                    }
                }
            }
        }
    }

    public char getTag() {
        return 'Z';
    }

    public static long getLong(byte[] bb) {
        return ((long)bb[0] & 0xFFL) << 56 | ((long)bb[1] & 0xFFL) << 48 | ((long)bb[2] & 0xFFL) << 40 | ((long)bb[3] & 0xFFL) << 32 | ((long)bb[4] & 0xFFL) << 24 | ((long)bb[5] & 0xFFL) << 16 | ((long)bb[6] & 0xFFL) << 8 | ((long)bb[7] & 0xFFL) << 0;
    }
}

