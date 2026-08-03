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

public class ParamInforPacket
extends BasePacket {
    private static int SM_PARAMCOUNT = 2;
    private static int SM_TYPEOID = 4;
    private static int SM_TYPESIZE = 4;
    private static int SM_TYPEDES = 4;
    private static int SM_ISNULL = 1;
    private static int SM_MODE = 1;
    private static int SM_TABLEOID = 4;
    private static int SM_COLUMNINDEX = 2;
    private static final char tag = 'p';
    private short paramCount;
    private Object[][] paramInfo = null;

    public Object[][] getParamInfo() {
        return this.paramInfo;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive ParamInforPacket 'p': ").append("\n");
        }
        this.paramCount = (short)BasePacket.ReceiveIntegerR(stream, SM_PARAMCOUNT);
        if (this.logFlag) {
            this.sb.append("paramCount: ").append(this.paramCount).append("\n");
        }
        this.paramInfo = this.version.isNewParamInfoPacket() ? new Object[this.paramCount][8] : new Object[this.paramCount][6];
        for (int i = 0; i < this.paramCount; ++i) {
            if (this.logFlag) {
                this.sb.append("param(").append(i).append("): ");
            }
            this.paramInfo[i][0] = BasePacket.ReceiveString(stream);
            this.paramInfo[i][1] = new Integer(BasePacket.ReceiveIntegerR(stream, SM_TYPEOID));
            this.paramInfo[i][2] = new Integer(BasePacket.ReceiveIntegerR(stream, SM_TYPESIZE));
            this.paramInfo[i][3] = new Integer(BasePacket.ReceiveIntegerR(stream, SM_TYPEDES));
            this.paramInfo[i][4] = new Byte(BasePacket.Receive(stream, 1)[0]);
            this.paramInfo[i][5] = new Byte(BasePacket.Receive(stream, 1)[0]);
            byte[] colName = (byte[])this.paramInfo[i][0];
            if (this.logFlag) {
                this.sb.append("colName: ");
                if (colName == null) {
                    this.sb.append("null");
                } else {
                    this.append(this.sb, colName);
                }
                this.sb.append(" typeoid: ").append(this.paramInfo[i][1]).append(" typesize: ").append(this.paramInfo[i][2]).append(" typedes: ");
                this.sb.append(this.paramInfo[i][3]).append(" nullable: ").append(this.paramInfo[i][4]).append(" mode: ").append(this.paramInfo[i][5]);
            }
            if (this.version.isNewParamInfoPacket()) {
                this.paramInfo[i][6] = new Integer(BasePacket.ReceiveIntegerR(stream, SM_TABLEOID));
                this.paramInfo[i][7] = new Integer(BasePacket.ReceiveIntegerR(stream, SM_COLUMNINDEX));
                if (this.logFlag) {
                    this.sb.append(" tableOId: ").append(this.paramInfo[i][6]).append(" columnIndex: ").append(this.paramInfo[i][7]);
                }
            }
            if (!this.logFlag) continue;
            this.sb.append("\n");
        }
        if (this.logFlag) {
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public char getTag() {
        return 'p';
    }
}

