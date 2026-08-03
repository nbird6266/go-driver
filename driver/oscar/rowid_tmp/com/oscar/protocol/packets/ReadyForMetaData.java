/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.protocol.packets.BasePacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class ReadyForMetaData
extends BasePacket {
    private char tag = (char)119;

    public char getTag() {
        return this.tag;
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }
}

