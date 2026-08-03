/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.protocol.packets.BasePacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class QueryPacket
extends BasePacket {
    private static final char tag = 'Q';
    private byte[] query;
    private int marked = 0;

    public QueryPacket(byte[] request, int marked) {
        this.query = request;
        this.marked = marked;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        BasePacket.SendChar(stream, 81);
        BasePacket.SendInteger(stream, this.marked, 2);
        BasePacket.Send(stream, this.query);
        BasePacket.SendChar(stream, 0);
        stream.flush();
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public char getTag() {
        return 'Q';
    }
}

