/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.protocol.packets.BasePacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class ImportExportResponsePacket
extends BasePacket {
    private static final char tag = 'c';
    private int amount;

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        this.amount = BasePacket.ReceiveIntegerR(stream, 4);
    }

    public int getAmount() {
        return this.amount;
    }

    public char getTag() {
        return 'c';
    }
}

