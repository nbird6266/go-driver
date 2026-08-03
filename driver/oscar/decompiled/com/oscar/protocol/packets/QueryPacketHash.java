/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.protocol.packets.BasePacket;
import com.oscar.protocol.packets.QueryPacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class QueryPacketHash
extends QueryPacket {
    private static final char tag = 'Q';
    private byte[] query;
    private int marked = -1;
    private int[] bindIDs;
    private int bucket;

    public QueryPacketHash(byte[] request, int marked, int[] bindIDs, int bucket) {
        super(request, marked);
        this.query = request;
        this.marked = marked;
        this.bindIDs = bindIDs;
        this.bucket = bucket;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        BasePacket.SendChar(stream, 81);
        BasePacket.SendInteger(stream, this.marked, 2);
        BasePacket.SendInteger(stream, this.bindIDs.length, 2);
        for (int i = 0; i < this.bindIDs.length; ++i) {
            BasePacket.SendInteger(stream, this.bindIDs[i], 2);
        }
        BasePacket.SendInteger(stream, this.bucket, 4);
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

