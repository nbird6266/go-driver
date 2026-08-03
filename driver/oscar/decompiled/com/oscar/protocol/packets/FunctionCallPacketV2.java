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

public class FunctionCallPacketV2
extends BasePacket {
    private static final char tag = '\u0002';
    private static int SM_OID = 4;
    private static int SM_PARACOUNT = 4;
    private static int SM_PARALEN = 4;
    private String unused = "";
    private int funcOID;
    private int paraCount;
    private int[] paraLenth;
    private Object[] paraValue;

    public FunctionCallPacketV2(int func_oid, int para_count, int[] para_lenth, Object[] para_value) {
        this.funcOID = func_oid;
        this.paraCount = para_count;
        this.paraLenth = new int[this.paraCount];
        this.paraValue = new Object[this.paraCount];
        for (int i = 0; i < this.paraCount; ++i) {
            this.paraLenth[i] = para_lenth[i];
            this.paraValue[i] = para_value[i];
        }
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send: '0x02' ").append("\n");
            this.sb.append("query num: ").append(0);
            this.sb.append(", funcOID: ").append(this.funcOID);
            this.sb.append(", paraCount: ").append(this.paraCount);
        }
        BasePacket.SendChar(stream, 2);
        BasePacket.SendInteger(stream, 0, 1);
        BasePacket.SendInteger(stream, this.funcOID, SM_OID);
        BasePacket.SendInteger(stream, this.paraCount, SM_PARACOUNT);
        for (int i = 0; i < this.paraCount; ++i) {
            BasePacket.SendInteger(stream, this.paraLenth[i], SM_PARALEN);
            if (this.logFlag) {
                this.sb.append(this.paraLenth[i]).append(" ");
            }
            if (this.paraValue[i] instanceof Integer) {
                BasePacket.SendInteger(stream, (Integer)this.paraValue[i], this.paraLenth[i]);
                if (this.logFlag) {
                    this.sb.append((Integer)this.paraValue[i]);
                }
            } else if (this.paraValue[i] instanceof Long) {
                BasePacket.SendLong(stream, (Long)this.paraValue[i], this.paraLenth[i]);
                if (this.logFlag) {
                    this.sb.append((Long)this.paraValue[i]);
                }
            } else {
                BasePacket.Send(stream, (byte[])this.paraValue[i], this.paraLenth[i]);
                if (this.logFlag) {
                    this.append(this.sb, (byte[])this.paraValue[i]);
                }
            }
            if (!this.logFlag) continue;
            this.sb.append("|");
        }
        stream.flush();
        if (this.logFlag) {
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public char getTag() {
        return '\u0002';
    }
}

