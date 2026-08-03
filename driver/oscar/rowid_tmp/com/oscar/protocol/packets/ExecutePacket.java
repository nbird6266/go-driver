/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.Driver;
import com.oscar.core.Encoding;
import com.oscar.protocol.packets.BasePacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class ExecutePacket
extends BasePacket {
    private char prepareExecuteTag = (char)11;
    private char executeTag = (char)13;
    private int statementNameLen = 0;
    private byte[] statementName;
    private Object[] bindDatas;
    private int[] bindTypes;
    private byte[] prepareSql;
    private Encoding encoding;
    private int totalLen = 0;
    private int marked = 0;
    private boolean bindTypeChanged;
    private boolean isHaveFuncReturn = false;
    private int packetLenSize;
    private int paramCountSize;

    public ExecutePacket(Encoding encoding, String request, String statementName, int[] bindTypes, Object[] m_binds, int marked, boolean bindTypeChanged, boolean isHaveFuncReturn, int protocolType) {
        this.encoding = encoding;
        this.bindTypeChanged = bindTypeChanged;
        this.isHaveFuncReturn = isHaveFuncReturn;
        try {
            if (request != null) {
                this.prepareSql = encoding.encode(request);
                this.totalLen += this.prepareSql.length + 1;
            }
            this.statementName = encoding.encode(statementName);
            this.statementNameLen = statementName.length();
            this.totalLen += this.statementNameLen + 1;
        }
        catch (SQLException e) {
            // empty catch block
        }
        this.bindTypes = bindTypes;
        this.totalLen = bindTypes != null && (this.prepareSql != null || bindTypeChanged) ? (this.isHaveFuncReturn ? (this.totalLen += bindTypes.length) : (this.totalLen += bindTypes.length + 1)) : ++this.totalLen;
        this.bindDatas = m_binds;
        this.marked = marked;
        if (this.bindDatas != null) {
            byte[] tmp = null;
            int i = 0;
            if (this.isHaveFuncReturn) {
                i = 1;
            }
            while (i < this.bindDatas.length) {
                tmp = (byte[])this.bindDatas[i];
                this.totalLen = tmp == null ? ++this.totalLen : (tmp.length == 0 ? (this.totalLen += 2) : (bindTypes[i] == 24 && tmp.length > 240 ? (this.totalLen += tmp.length) : (bindTypes[i] != 24 && tmp[0] == -3 ? (this.totalLen += tmp.length) : (bindTypes[i] == 50 || bindTypes[i] == 51 || bindTypes[i] == 52 ? (this.totalLen += tmp.length + 5) : (this.totalLen += tmp.length + 1)))));
                ++i;
            }
        }
        if (protocolType >= 3) {
            this.packetLenSize = 4;
            this.paramCountSize = 2;
            ++this.totalLen;
        } else {
            this.packetLenSize = 2;
            this.paramCountSize = 1;
        }
    }

    public char getTag() {
        return this.prepareExecuteTag;
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        int i;
        int i2;
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send: ").append("\n");
        }
        if (this.prepareSql != null) {
            BasePacket.SendChar(stream, this.prepareExecuteTag);
            BasePacket.SendInteger(stream, this.marked, 2);
            BasePacket.SendInteger(stream, this.totalLen, this.packetLenSize);
            BasePacket.Send(stream, this.prepareSql);
            BasePacket.SendChar(stream, 0);
            if (this.logFlag) {
                this.sb.append("prepareExecute: 0x0B, querynum: " + this.marked + ", totalLen: " + this.totalLen + ", sql: ");
                for (i2 = 0; i2 < this.prepareSql.length; ++i2) {
                    this.sb.append(this.prepareSql[i2]).append(" ");
                }
                this.sb.append(0);
            }
        } else {
            BasePacket.SendChar(stream, this.executeTag);
            BasePacket.SendInteger(stream, this.totalLen, this.packetLenSize);
            if (this.logFlag) {
                this.sb.append("execute: 0x0D, totalLen: " + this.totalLen);
            }
        }
        if (this.bindTypes != null && (this.prepareSql != null || this.bindTypeChanged)) {
            i2 = 0;
            if (this.isHaveFuncReturn) {
                BasePacket.SendInteger(stream, this.bindTypes.length - 1, this.paramCountSize);
                i2 = 1;
            } else {
                BasePacket.SendInteger(stream, this.bindTypes.length, this.paramCountSize);
            }
            while (i2 < this.bindTypes.length) {
                BasePacket.SendInteger(stream, this.bindTypes[i2], 1);
                ++i2;
            }
            if (this.logFlag) {
                this.sb.append(", bindType Len: ").append(this.bindTypes.length).append(", bindType: ");
                for (i2 = 0; i2 < this.bindTypes.length; ++i2) {
                    this.sb.append(this.bindTypes[i2]).append(" ");
                }
            }
        } else {
            BasePacket.SendInteger(stream, 0, this.paramCountSize);
            if (this.logFlag) {
                this.sb.append(", bindType Len: 0");
            }
        }
        BasePacket.SendInteger(stream, this.statementNameLen, 1);
        BasePacket.Send(stream, this.statementName);
        if (this.logFlag) {
            this.sb.append(", prepareName Len: ").append(this.statementNameLen).append(", prepareName: ");
            for (i2 = 0; i2 < this.statementName.length; ++i2) {
                this.sb.append(this.statementName[i2]).append(" ");
            }
            this.sb.append(", bindDatas: ");
        }
        if (this.bindDatas != null) {
            byte[] tmp = null;
            i = 0;
            if (this.isHaveFuncReturn) {
                i = 1;
            }
            while (i < this.bindDatas.length) {
                if (this.bindDatas[i] == null) {
                    BasePacket.SendInteger(stream, 0, 1);
                } else {
                    tmp = (byte[])this.bindDatas[i];
                    if (tmp.length == 0) {
                        tmp = "\u0000".getBytes();
                    }
                    if (this.bindTypes[i] == 24 && tmp.length > 240) {
                        BasePacket.Send(stream, tmp);
                    } else if (this.bindTypes[i] != 24 && tmp[0] == -3) {
                        BasePacket.Send(stream, tmp);
                    } else if (this.bindTypes[i] == 50) {
                        BasePacket.SendInteger(stream, 251, 1);
                        BasePacket.SendInteger(stream, tmp.length, 4);
                        BasePacket.Send(stream, tmp);
                    } else if (this.bindTypes[i] == 51) {
                        BasePacket.SendInteger(stream, 250, 1);
                        BasePacket.SendInteger(stream, tmp.length, 4);
                        BasePacket.Send(stream, tmp);
                    } else if (this.bindTypes[i] == 52) {
                        BasePacket.SendInteger(stream, 252, 1);
                        BasePacket.SendInteger(stream, tmp.length, 4);
                        BasePacket.Send(stream, tmp);
                    } else {
                        BasePacket.SendInteger(stream, tmp.length, 1);
                        BasePacket.Send(stream, tmp);
                    }
                }
                ++i;
            }
        }
        stream.flush();
        if (this.logFlag) {
            if (this.bindDatas != null) {
                byte[] tmp = null;
                for (i = 0; i < this.bindDatas.length; ++i) {
                    int j;
                    if (this.bindDatas[i] == null) {
                        this.sb.append(0).append(" | ");
                        continue;
                    }
                    tmp = (byte[])this.bindDatas[i];
                    if (tmp.length == 0) {
                        this.sb.append(0).append(" | ");
                        continue;
                    }
                    if (this.bindTypes[i] == 24 && tmp.length > 240) {
                        if (!this.logFlag) continue;
                        for (j = 0; j < tmp.length; ++j) {
                            this.sb.append(tmp[j]).append(" ");
                        }
                        this.sb.append("| ");
                        continue;
                    }
                    if (this.bindTypes[i] != 24 && tmp[0] == -3) {
                        for (j = 0; j < tmp.length; ++j) {
                            this.sb.append(tmp[j]).append(" ");
                        }
                        this.sb.append("| ");
                        continue;
                    }
                    if (this.bindTypes[i] == 50) {
                        this.sb.append(251).append(" ").append(tmp.length).append("(4 bytes)");
                        for (j = 0; j < tmp.length; ++j) {
                            this.sb.append(tmp[j]).append(" ");
                        }
                        this.sb.append("| ");
                        continue;
                    }
                    if (this.bindTypes[i] == 51) {
                        this.sb.append(250).append(" ").append(tmp.length).append("(4 bytes)");
                        for (j = 0; j < tmp.length; ++j) {
                            this.sb.append(tmp[j]).append(" ");
                        }
                        this.sb.append("| ");
                        continue;
                    }
                    if (this.bindTypes[i] == 52) {
                        this.sb.append(252).append(" ").append(tmp.length).append("(4 bytes)");
                        for (j = 0; j < tmp.length; ++j) {
                            this.sb.append(tmp[j]).append(" ");
                        }
                        this.sb.append("| ");
                        continue;
                    }
                    this.sb.append(tmp.length).append(" ");
                    for (j = 0; j < tmp.length; ++j) {
                        this.sb.append(tmp[j]).append(" ");
                    }
                    this.sb.append("| ");
                }
            }
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }
}

