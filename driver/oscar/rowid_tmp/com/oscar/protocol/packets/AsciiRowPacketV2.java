/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.core.Field;
import com.oscar.protocol.packets.BasePacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class AsciiRowPacketV2
extends BasePacket {
    private static final char tag = 'D';
    private static final int SM_LEN = 4;
    private int numberOfField;
    private byte[][] tuple;
    private Field[] fields;
    private boolean numericKeepPrecision = true;
    private boolean netDataByStr = false;

    public void setConnection(BaseConnection conn) {
        super.setConnection(conn);
        this.numericKeepPrecision = conn.isNumericKeepPrecision();
        this.netDataByStr = conn.isNetDataByStr();
    }

    public void initTuple(int nf) {
        this.numberOfField = nf;
        this.tuple = new byte[this.numberOfField][0];
    }

    public void initTuple(int nf, Field[] fields) {
        if (nf != fields.length) {
            nf = fields.length;
        }
        this.numberOfField = nf;
        this.tuple = new byte[this.numberOfField][0];
        this.fields = fields;
    }

    public byte[][] getTuple() {
        return this.tuple;
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", receive AsciiRowPacketV2 'D': ").append("\n");
        }
        int bim = (this.numberOfField + 7) / 8;
        byte[] bitmask = BasePacket.Receive(stream, bim);
        if (this.logFlag) {
            this.sb.append("bitmask: ");
            this.append(this.sb, bitmask);
            this.sb.append(", tuples: ");
        }
        int whichbit = 128;
        int whichbyte = 0;
        int dataLen = -1;
        if (this.netDataByStr) {
            block11: for (int i = 0; i < this.numberOfField; ++i) {
                boolean isNull;
                boolean bl = isNull = (bitmask[whichbyte] & whichbit) == 0;
                if ((whichbit >>= 1) == 0) {
                    ++whichbyte;
                    whichbit = 128;
                }
                if (isNull) {
                    this.tuple[i] = null;
                } else {
                    switch (this.fields[i].getOscarType()) {
                        case 50: 
                        case 51: 
                        case 52: {
                            dataLen = BasePacket.ReceiveIntegerR(stream, 4);
                            this.tuple[i] = BasePacket.Receive(stream, dataLen -= 4);
                            break;
                        }
                        default: {
                            dataLen = BasePacket.ReceiveIntegerR(stream, 1);
                            if (dataLen == 253) {
                                this.tuple[i] = new byte[2];
                                this.tuple[i][0] = -3;
                                BasePacket.Receive(stream, this.tuple[i], 1, 1);
                                continue block11;
                            }
                            if (dataLen < 0) {
                                dataLen += 256;
                            }
                            if (dataLen > 240) {
                                int position = 0;
                                byte[] result = new byte[8192];
                                dataLen = BasePacket.ReceiveIntegerR(stream, 1);
                                byte[] tmpBytes = null;
                                while (dataLen != 0) {
                                    if (position + dataLen > result.length) {
                                        tmpBytes = result;
                                        result = new byte[result.length + 8192];
                                        System.arraycopy(tmpBytes, 0, result, 0, position);
                                    }
                                    BasePacket.Receive(stream, result, position, dataLen);
                                    position += dataLen;
                                    dataLen = BasePacket.ReceiveIntegerR(stream, 1);
                                }
                                this.tuple[i] = new byte[position];
                                System.arraycopy(result, 0, this.tuple[i], 0, position);
                                break;
                            }
                            this.tuple[i] = BasePacket.Receive(stream, dataLen);
                        }
                    }
                }
                if (!this.logFlag) continue;
                this.append(this.sb, this.tuple[i]);
                this.sb.append("|");
            }
        } else if (this.numericKeepPrecision) {
            block13: for (int i = 0; i < this.numberOfField; ++i) {
                boolean isNull;
                boolean bl = isNull = (bitmask[whichbyte] & whichbit) == 0;
                if ((whichbit >>= 1) == 0) {
                    ++whichbyte;
                    whichbit = 128;
                }
                if (isNull) {
                    this.tuple[i] = null;
                } else {
                    switch (this.fields[i].getOscarType()) {
                        case 23: 
                        case 25: 
                        case 26: 
                        case 27: 
                        case 28: 
                        case 29: 
                        case 30: 
                        case 31: 
                        case 32: 
                        case 33: {
                            dataLen = BasePacket.ReceiveIntegerR(stream, 1);
                            if (dataLen == 253) {
                                this.tuple[i] = new byte[2];
                                this.tuple[i][0] = -3;
                                BasePacket.Receive(stream, this.tuple[i], 1, 1);
                                continue block13;
                            }
                            this.tuple[i] = BasePacket.Receive(stream, dataLen);
                            break;
                        }
                        case 50: 
                        case 51: 
                        case 52: {
                            dataLen = BasePacket.ReceiveIntegerR(stream, 4);
                            this.tuple[i] = BasePacket.Receive(stream, dataLen -= 4);
                            break;
                        }
                        default: {
                            dataLen = BasePacket.ReceiveIntegerR(stream, 1);
                            if (dataLen == 253) {
                                this.tuple[i] = new byte[2];
                                this.tuple[i][0] = -3;
                                BasePacket.Receive(stream, this.tuple[i], 1, 1);
                                continue block13;
                            }
                            if (dataLen < 0) {
                                dataLen += 256;
                            }
                            if (dataLen > 240) {
                                int position = 0;
                                byte[] result = new byte[8192];
                                dataLen = BasePacket.ReceiveIntegerR(stream, 1);
                                byte[] tmpBytes = null;
                                while (dataLen != 0) {
                                    if (position + dataLen > result.length) {
                                        tmpBytes = result;
                                        result = new byte[result.length + 8192];
                                        System.arraycopy(tmpBytes, 0, result, 0, position);
                                    }
                                    BasePacket.Receive(stream, result, position, dataLen);
                                    position += dataLen;
                                    dataLen = BasePacket.ReceiveIntegerR(stream, 1);
                                }
                                this.tuple[i] = new byte[position];
                                System.arraycopy(result, 0, this.tuple[i], 0, position);
                                break;
                            }
                            this.tuple[i] = BasePacket.Receive(stream, dataLen);
                        }
                    }
                }
                if (!this.logFlag) continue;
                this.append(this.sb, this.tuple[i]);
                this.sb.append("|");
            }
        } else {
            block15: for (int i = 0; i < this.numberOfField; ++i) {
                boolean isNull;
                boolean bl = isNull = (bitmask[whichbyte] & whichbit) == 0;
                if ((whichbit >>= 1) == 0) {
                    ++whichbyte;
                    whichbit = 128;
                }
                if (isNull) {
                    this.tuple[i] = null;
                } else {
                    switch (this.fields[i].getOscarType()) {
                        case 23: 
                        case 25: 
                        case 26: 
                        case 27: 
                        case 28: 
                        case 29: 
                        case 30: 
                        case 31: 
                        case 32: 
                        case 33: 
                        case 34: {
                            dataLen = BasePacket.ReceiveIntegerR(stream, 1);
                            if (dataLen == 253) {
                                this.tuple[i] = new byte[2];
                                this.tuple[i][0] = -3;
                                BasePacket.Receive(stream, this.tuple[i], 1, 1);
                                continue block15;
                            }
                            this.tuple[i] = BasePacket.Receive(stream, dataLen);
                            break;
                        }
                        case 50: 
                        case 51: 
                        case 52: {
                            dataLen = BasePacket.ReceiveIntegerR(stream, 4);
                            this.tuple[i] = BasePacket.Receive(stream, dataLen -= 4);
                            break;
                        }
                        default: {
                            dataLen = BasePacket.ReceiveIntegerR(stream, 1);
                            if (dataLen == 253) {
                                this.tuple[i] = new byte[2];
                                this.tuple[i][0] = -3;
                                BasePacket.Receive(stream, this.tuple[i], 1, 1);
                                continue block15;
                            }
                            if (dataLen < 0) {
                                dataLen += 256;
                            }
                            if (dataLen > 240) {
                                int position = 0;
                                byte[] result = new byte[8192];
                                dataLen = BasePacket.ReceiveIntegerR(stream, 1);
                                byte[] tmpBytes = null;
                                while (dataLen != 0) {
                                    if (position + dataLen > result.length) {
                                        tmpBytes = result;
                                        result = new byte[result.length + 8192];
                                        System.arraycopy(tmpBytes, 0, result, 0, position);
                                    }
                                    BasePacket.Receive(stream, result, position, dataLen);
                                    position += dataLen;
                                    dataLen = BasePacket.ReceiveIntegerR(stream, 1);
                                }
                                this.tuple[i] = new byte[position];
                                System.arraycopy(result, 0, this.tuple[i], 0, position);
                                break;
                            }
                            this.tuple[i] = BasePacket.Receive(stream, dataLen);
                        }
                    }
                }
                if (!this.logFlag) continue;
                this.append(this.sb, this.tuple[i]);
                this.sb.append("|");
            }
        }
        if (this.logFlag) {
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    public char getTag() {
        return 'D';
    }
}

