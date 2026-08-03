/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.Driver;
import com.oscar.protocol.packets.BasePacket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class FetchPacket
extends BasePacket {
    private char prepareFetchTag = (char)7;
    private char executeFetchTag = (char)6;
    private char selectFetchTag = (char)5;
    private byte[] query;
    private byte[] prepareName;
    private int fetchSize;
    private boolean isPrepare = false;
    private int[] bindTypes;
    private Object[] bindDatas;
    private int marked = 0;
    private int totalLen = 0;
    private boolean bindTypeChanged;
    private int packetLenSize;
    private int paramCountSize;

    public FetchPacket(byte[] query, byte[] prepareName, int[] bindTypes, Object[] bindDatas, int fetchSize, boolean isPrepare, boolean bindTypeChanged, int protocolType) {
        int i;
        this.query = query;
        this.fetchSize = fetchSize;
        this.bindTypes = bindTypes;
        this.prepareName = prepareName;
        this.isPrepare = isPrepare;
        this.bindDatas = bindDatas;
        this.bindTypeChanged = bindTypeChanged;
        this.totalLen += 3;
        if (query != null) {
            this.totalLen += query.length + 1;
        }
        this.totalLen = bindTypes != null && (isPrepare && query != null || bindTypeChanged) ? (this.totalLen += bindTypes.length + 1) : ++this.totalLen;
        if (prepareName != null) {
            this.totalLen += prepareName.length + 1;
        }
        for (i = 0; i < bindDatas.length; ++i) {
            this.totalLen += ((byte[])bindDatas[i]).length + 1;
        }
        if (bindTypes != null) {
            for (i = 0; i < bindTypes.length; ++i) {
                if (bindTypes[i] != 51 && bindTypes[i] != 50 && bindTypes[i] != 52) continue;
                this.totalLen += 4;
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

    public FetchPacket(byte[] query, byte[] prepareName, int[] bindTypes, Object[] bindDatas, int fetchSize, boolean isPrepare, int marked, boolean bindTypeChanged, int protocolType) {
        this.query = query;
        this.fetchSize = fetchSize;
        this.bindTypes = bindTypes;
        this.prepareName = prepareName;
        this.isPrepare = isPrepare;
        this.bindDatas = bindDatas;
        this.marked = marked;
        this.bindTypeChanged = bindTypeChanged;
        this.totalLen += 3;
        if (query != null) {
            this.totalLen += query.length + 1;
        }
        this.totalLen = bindTypes != null && (isPrepare && query != null || bindTypeChanged) ? (this.totalLen += bindTypes.length + 1) : ++this.totalLen;
        if (prepareName != null) {
            this.totalLen += prepareName.length + 1;
        }
        if (bindDatas != null) {
            byte[] tmp = null;
            for (int i = 0; i < bindDatas.length; ++i) {
                tmp = (byte[])bindDatas[i];
                if (tmp == null || tmp.length == 0) {
                    ++this.totalLen;
                    continue;
                }
                if (bindTypes[i] == 24 && tmp.length > 240) {
                    this.totalLen += tmp.length;
                    continue;
                }
                if (bindTypes[i] != 24 && tmp[0] == -3) {
                    this.totalLen += tmp.length;
                    continue;
                }
                this.totalLen += tmp.length + 1;
            }
        }
        if (bindTypes != null) {
            for (int i = 0; i < bindTypes.length; ++i) {
                if (bindTypes[i] != 51 && bindTypes[i] != 50 && bindTypes[i] != 52) continue;
                this.totalLen += 4;
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
        return this.prepareFetchTag;
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send: ").append("\n");
        }
        if (this.isPrepare) {
            int i;
            if (this.query != null) {
                BasePacket.SendChar(stream, this.prepareFetchTag);
                BasePacket.SendInteger(stream, this.marked, 2);
                BasePacket.SendInteger(stream, this.totalLen, this.packetLenSize);
                BasePacket.SendInteger(stream, this.fetchSize, 2);
                BasePacket.SendInteger(stream, 0, 1);
                BasePacket.Send(stream, this.query);
                BasePacket.SendChar(stream, 0);
                if (this.logFlag) {
                    this.sb.append("prepareFetch: 0x07, querynum: " + this.marked + ", totalLen: " + this.totalLen + ", fetchSize: " + this.fetchSize + ", cursor type: 0, sql: ");
                    for (i = 0; i < this.query.length; ++i) {
                        this.sb.append(this.query[i]).append(" ");
                    }
                    this.sb.append(0);
                }
            } else {
                BasePacket.SendChar(stream, this.executeFetchTag);
                BasePacket.SendInteger(stream, this.totalLen, this.packetLenSize);
                BasePacket.SendInteger(stream, this.fetchSize, 2);
                BasePacket.SendInteger(stream, 0, 1);
                if (this.logFlag) {
                    this.sb.append("executeFetch: 0x06, totalLen: " + this.totalLen + ", fetchSize: " + this.fetchSize + ", cursor type: 0");
                }
            }
            if (this.bindTypes != null && (this.query != null || this.bindTypeChanged)) {
                BasePacket.SendInteger(stream, this.bindTypes.length, this.paramCountSize);
                if (this.logFlag) {
                    this.sb.append(", bindType Len: ").append(this.bindTypes.length).append(", bindType: ");
                }
                for (i = 0; i < this.bindTypes.length; ++i) {
                    if (this.logFlag) {
                        this.sb.append(this.bindTypes[i]).append(" ");
                    }
                    BasePacket.SendInteger(stream, this.bindTypes[i], 1);
                }
            } else {
                BasePacket.SendInteger(stream, 0, this.paramCountSize);
                if (this.logFlag) {
                    this.sb.append(", bindType Len: 0");
                }
            }
            BasePacket.SendInteger(stream, this.prepareName.length, 1);
            BasePacket.Send(stream, this.prepareName);
            if (this.logFlag) {
                this.sb.append(", prepareName Len: ").append(this.prepareName.length).append(", prepareName: ");
                for (i = 0; i < this.prepareName.length; ++i) {
                    this.sb.append(this.prepareName[i]).append(" ");
                }
                this.sb.append(", bindDatas: ");
            }
            byte[] tmp = null;
            for (int i2 = 0; i2 < this.bindDatas.length; ++i2) {
                int j;
                if (this.bindDatas[i2] == null) {
                    BasePacket.SendInteger(stream, 0, 1);
                    if (!this.logFlag) continue;
                    this.sb.append(0).append(" | ");
                    continue;
                }
                tmp = (byte[])this.bindDatas[i2];
                if (tmp.length == 0) {
                    BasePacket.SendInteger(stream, 0, 1);
                    if (!this.logFlag) continue;
                    this.sb.append(0).append(" | ");
                    continue;
                }
                if (this.bindTypes[i2] == 24 && tmp.length > 240) {
                    BasePacket.Send(stream, tmp);
                    if (!this.logFlag) continue;
                    for (j = 0; j < tmp.length; ++j) {
                        this.sb.append(tmp[j]).append(" ");
                    }
                    this.sb.append("| ");
                    continue;
                }
                if (this.bindTypes[i2] != 24 && tmp[0] == -3) {
                    BasePacket.Send(stream, tmp);
                    if (!this.logFlag) continue;
                    for (j = 0; j < tmp.length; ++j) {
                        this.sb.append(tmp[j]).append(" ");
                    }
                    this.sb.append("| ");
                    continue;
                }
                if (this.bindTypes[i2] == 50) {
                    BasePacket.SendInteger(stream, 251, 1);
                    BasePacket.SendInteger(stream, tmp.length, 4);
                    BasePacket.Send(stream, tmp);
                    if (!this.logFlag) continue;
                    for (j = 0; j < tmp.length; ++j) {
                        this.sb.append(tmp[j]).append(" ");
                    }
                    this.sb.append("| ");
                    continue;
                }
                if (this.bindTypes[i2] == 51) {
                    BasePacket.SendInteger(stream, 250, 1);
                    BasePacket.SendInteger(stream, tmp.length, 4);
                    BasePacket.Send(stream, tmp);
                    if (!this.logFlag) continue;
                    for (j = 0; j < tmp.length; ++j) {
                        this.sb.append(tmp[j]).append(" ");
                    }
                    this.sb.append("| ");
                    continue;
                }
                if (this.bindTypes[i2] == 52) {
                    BasePacket.SendInteger(stream, 252, 1);
                    BasePacket.SendInteger(stream, tmp.length, 4);
                    BasePacket.Send(stream, tmp);
                    if (!this.logFlag) continue;
                    for (j = 0; j < tmp.length; ++j) {
                        this.sb.append(tmp[j]).append(" ");
                    }
                    this.sb.append("| ");
                    continue;
                }
                BasePacket.SendInteger(stream, tmp.length, 1);
                BasePacket.Send(stream, tmp);
                if (!this.logFlag) continue;
                this.sb.append(tmp.length).append(" ");
                for (j = 0; j < tmp.length; ++j) {
                    this.sb.append(tmp[j]).append(" ");
                }
                this.sb.append("| ");
            }
        } else {
            BasePacket.SendChar(stream, this.selectFetchTag);
            BasePacket.SendInteger(stream, this.marked, 2);
            BasePacket.SendInteger(stream, this.fetchSize, 2);
            BasePacket.SendInteger(stream, 0, 1);
            BasePacket.Send(stream, this.query);
            BasePacket.SendChar(stream, 0);
            if (this.logFlag) {
                this.sb.append("selectFetch: 0x05, querynum: " + this.marked + ", fetchSize: " + this.fetchSize + ", cursor type: 0, sql: ");
                for (int i = 0; i < this.query.length; ++i) {
                    this.sb.append(this.query[i]).append(" ");
                }
                this.sb.append(0);
            }
        }
        stream.flush();
        if (this.logFlag) {
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }
}

