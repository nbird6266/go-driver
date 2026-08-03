/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.protocol.packets.BasePacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class HashDataPacket
extends BasePacket {
    private static final char tag = 'H';
    private byte[] hashkey;
    private int datalen;
    private byte[] data;

    public byte[] getHashkey() {
        return this.hashkey;
    }

    public void setHashkey(byte[] hashkey) {
        this.hashkey = hashkey;
    }

    public int getDatalen() {
        return this.datalen;
    }

    public void setDatalen(int datalen) {
        this.datalen = datalen;
    }

    public byte[] getDataSize() {
        int siz = 4;
        byte[] buf = new byte[siz];
        while (siz-- > 0) {
            buf[siz] = (byte)(this.datalen & 0xFF);
            this.datalen >>= 8;
        }
        return buf;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        this.hashkey = BasePacket.Receive(stream, 4);
        this.datalen = BasePacket.ReceiveIntegerR(stream, 4);
        this.data = new byte[8 + this.datalen];
        BasePacket.Receive(stream, this.data, 8, this.datalen);
    }

    public char getTag() {
        return 'H';
    }
}

