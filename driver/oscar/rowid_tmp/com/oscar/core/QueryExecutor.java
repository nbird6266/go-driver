/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.core.BaseResultSet;
import com.oscar.core.BaseStatement;
import com.oscar.protocol.OSCARProtocol;
import com.oscar.protocol.OSCARProtocolV2;
import com.oscar.protocol.packets.BatchProcessPacket;
import com.oscar.protocol.packets.BatchProcessPacketV2;
import com.oscar.util.OSQLException;
import java.sql.SQLException;

public class QueryExecutor {
    public BaseResultSet execute(String[] o_sqlFrags, Object[] p_binds, BaseStatement statement) throws SQLException {
        int maxRows = statement.getMaxRows();
        OSCARProtocol protocol = statement.getDBConnection().getProtocol();
        if (protocol == null) {
            throw new OSQLException("OSCAR-00211", "08003", 211);
        }
        String queryString = "";
        if (statement.getStatementType() == 1 && statement.isCursorUsed()) {
            String cursOpen = "OPEN ";
            String cursorName = statement.getCursorName();
            if (statement.isNotRealPrepare()) {
                cursOpen = cursOpen + this.getQueryString(o_sqlFrags, p_binds, statement);
            } else {
                cursOpen = cursOpen + cursorName;
                int bindLen = p_binds.length;
                if (bindLen > 0) {
                    cursOpen = cursOpen + " USING ";
                    for (int i = 0; i < bindLen; ++i) {
                        if (p_binds[i] != null) {
                            cursOpen = cursOpen + p_binds[i].toString();
                            if (i == bindLen - 1) continue;
                            cursOpen = cursOpen + ", ";
                            continue;
                        }
                        throw new OSQLException("OSCAR-00405", "88888", 405);
                    }
                }
            }
            cursOpen = cursOpen + ";";
            BaseStatement temp = (BaseStatement)((Object)statement.getDBConnection().createStatement());
            statement.getDBConnection().execSQL(cursOpen, temp);
            queryString = " FETCH FORWARD " + statement.getFetchSize() + " FROM " + cursorName + ";";
        } else {
            queryString = this.getQueryString(o_sqlFrags, p_binds, statement);
        }
        return protocol.query(queryString, maxRows, statement);
    }

    private String getQueryString(String[] sqlFrags, Object[] binds, BaseStatement statement) {
        StringBuffer sbuf = new StringBuffer();
        if (statement.isResultNeeded()) {
            sbuf.append(sqlFrags[0]);
            for (int i = 1; i < binds.length; ++i) {
                sbuf.append(binds[i].toString());
                sbuf.append(sqlFrags[i]);
            }
        } else {
            for (int i = 0; i < binds.length; ++i) {
                sbuf.append(sqlFrags[i]);
                sbuf.append(binds[i].toString());
            }
            sbuf.append(sqlFrags[binds.length]);
        }
        return sbuf.toString();
    }

    public void executeBatch(BatchProcessPacket packet, BaseStatement stmt, int[] updateCounts) throws SQLException {
        OSCARProtocol protocol = stmt.getDBConnection().getProtocol();
        if (protocol == null) {
            throw new OSQLException("OSCAR-00211", "08003", 211);
        }
        protocol.batchProcess(packet, stmt, updateCounts);
    }

    public void executeBatch(BatchProcessPacketV2 packet, BaseStatement stmt, int[] updateCounts) throws SQLException {
        OSCARProtocolV2 protocol = (OSCARProtocolV2)stmt.getDBConnection().getProtocol();
        if (protocol == null) {
            throw new OSQLException("OSCAR-00211", "08003", 211);
        }
        protocol.batchProcess(packet, stmt, updateCounts);
    }

    public byte[] backupKstore(String queryStr, BaseStatement stmt) throws SQLException {
        OSCARProtocol protocol = stmt.getDBConnection().getProtocol();
        if (protocol == null) {
            throw new OSQLException("OSCAR-00211", "08003", 211);
        }
        return protocol.backUpKstore(queryStr);
    }

    public byte[] getNextPhysicalDataRow(BaseStatement stmt) throws SQLException {
        OSCARProtocol protocol = stmt.getDBConnection().getProtocol();
        if (protocol == null) {
            throw new OSQLException("OSCAR-00211", "08003", 211);
        }
        return protocol.getNextPhysicalDataRow();
    }

    public void restoreKstore(String queryStr, byte[] metaData, byte[] physicalData, BaseStatement stmt) throws SQLException {
        OSCARProtocol protocol = stmt.getDBConnection().getProtocol();
        if (protocol == null) {
            throw new OSQLException("OSCAR-00211", "08003", 211);
        }
        protocol.restoreKstore(queryStr, metaData, physicalData);
    }
}

