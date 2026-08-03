/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.core.Encoding;
import com.oscar.protocol.packets.BasePacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class ImportPacket
extends BasePacket {
    private char tag = (char)103;
    private List importRows = null;
    private Encoding encoding = null;

    public void setImportValues(List rows) {
        this.importRows = rows;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        Iterator it = this.importRows.iterator();
        BasePacket.SendChar(stream, 104);
        while (it.hasNext()) {
            BasePacket.SendString(stream, it.next().toString(), this.encoding);
        }
        BasePacket.SendChar(stream, 0);
        BasePacket.SendChar(stream, 104);
        BasePacket.SendChar(stream, 10);
        BasePacket.SendChar(stream, 0);
        stream.flush();
        this.importRows = null;
        this.encoding = null;
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public char getTag() {
        return this.tag;
    }
}

