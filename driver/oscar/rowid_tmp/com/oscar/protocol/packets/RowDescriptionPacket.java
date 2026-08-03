/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.Driver;
import com.oscar.core.Field;
import com.oscar.protocol.packets.BasePacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class RowDescriptionPacket
extends BasePacket {
    private static int SM_COLCOUNT = 2;
    private static int SM_COLOID = 4;
    private static int SM_TYPESIZE = 2;
    private static int SM_TYPEDES = 4;
    private static int SM_TYPEOID = 4;
    private static final char tag = 'T';
    private short columnCount;
    private Field[] fields = null;
    private StringBuffer sb = new StringBuffer();

    public Field[] getFields() {
        return this.fields;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive RowDescriptionPacket 'T': ").append("\n");
        }
        this.columnCount = (short)BasePacket.ReceiveIntegerR(stream, SM_COLCOUNT);
        if (this.logFlag) {
            this.sb.append("columnCount: ").append(this.columnCount).append(", columns: ").append("\n");
        }
        byte attr = 0;
        this.fields = new Field[this.columnCount];
        byte[] name = null;
        byte[] aliasName = null;
        byte[] tableName = null;
        byte[] schemaName = null;
        int typeOid = 0;
        int typeLength = 0;
        int typeModifier = 0;
        for (int i = 0; i < this.columnCount; ++i) {
            attr = (byte)BasePacket.ReceiveChar(stream);
            if (this.conn.isReceiveStringByLen()) {
                name = BasePacket.ReceiveStringByLen(stream);
                aliasName = BasePacket.ReceiveStringByLen(stream);
                tableName = BasePacket.ReceiveStringByLen(stream);
                schemaName = BasePacket.ReceiveStringByLen(stream);
            } else {
                name = BasePacket.ReceiveString(stream);
                aliasName = BasePacket.ReceiveString(stream);
                tableName = BasePacket.ReceiveString(stream);
                schemaName = BasePacket.ReceiveString(stream);
            }
            typeOid = BasePacket.ReceiveIntegerR(stream, SM_TYPEOID);
            typeLength = BasePacket.ReceiveIntegerR(stream, SM_TYPESIZE);
            typeModifier = BasePacket.ReceiveIntegerR(stream, SM_TYPEDES);
            if (this.logFlag) {
                this.sb.append("nullAndUpdateAbleFlag: ").append(attr).append(", name: ");
                this.append(this.sb, name);
                this.sb.append(", aliasName");
                this.append(this.sb, aliasName);
                this.sb.append(", tableName");
                this.append(this.sb, tableName);
                this.sb.append(", schemaName");
                this.append(this.sb, schemaName);
                this.sb.append(", typeOid").append(typeOid);
                this.sb.append(", typeLength: ").append(typeLength);
                this.sb.append(", typeModifier: ").append(typeModifier).append("\n");
            }
            this.fields[i] = new Field(this.conn, name, typeOid, typeLength, typeModifier, aliasName, tableName, schemaName, attr);
        }
        if (this.logFlag) {
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public char getTag() {
        return 'T';
    }
}

