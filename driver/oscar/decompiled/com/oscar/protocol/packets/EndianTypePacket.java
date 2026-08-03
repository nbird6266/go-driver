/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.protocol.packets.BasePacket;
import com.oscar.util.OSQLException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class EndianTypePacket
extends BasePacket {
    private int endianType;

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        byte[] receiveBytes = BasePacket.Receive(stream, 1);
        byte orderType = receiveBytes[0];
        if (orderType == 48) {
            this.endianType = 0;
        } else if (orderType == 49) {
            this.endianType = 1;
        } else {
            throw new OSQLException("OSCAR-00107", "08001", 107);
        }
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public int getEndianType() {
        return this.endianType;
    }

    public char getTag() {
        return '\u0000';
    }
}

