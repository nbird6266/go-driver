/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.util.ColumnData;
import com.oscar.util.ImportStream;
import com.oscar.util.OSCARbyte;
import java.sql.SQLException;

public class ByteData
extends ColumnData {
    private byte[] buffer;

    public byte[] getBuffer() {
        return this.buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.setBuffer(buffer, false);
    }

    public void setBuffer(byte[] buffer, boolean toOscarString) {
        byte[] tempBuffer = buffer;
        if (toOscarString) {
            String hexStr = null;
            try {
                hexStr = OSCARbyte.toOSCARString(buffer);
            }
            catch (SQLException e) {
                // empty catch block
            }
            tempBuffer = hexStr.getBytes();
        }
        this.buffer = tempBuffer;
    }

    public void endWrite(ImportStream out) throws SQLException {
    }

    public void preWrite(ImportStream out) throws SQLException {
        out.writeInteger(this.buffer.length + 2, 2);
        out.write(this.buffer);
    }

    public void clear() throws SQLException {
        this.buffer = null;
    }

    public void preWriteBlock(ImportStream out) throws SQLException {
        out.write(this.buffer, 4, this.buffer.length - 4);
    }
}

