/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.core.BaseResultSet;
import com.oscar.core.BaseStatement;
import com.oscar.core.Field;
import com.oscar.protocol.OSCARProtocol;
import com.oscar.protocol.Packet;
import com.oscar.protocol.packets.AsciiRowPacketV2;
import com.oscar.protocol.packets.BasePacket;
import com.oscar.protocol.packets.BatchProcessPacketV2;
import com.oscar.protocol.packets.CompleteResponsePacket;
import com.oscar.protocol.packets.CursorResponsePacket;
import com.oscar.protocol.packets.EmptyQueryResponsePacket;
import com.oscar.protocol.packets.ErrorResponsePacket;
import com.oscar.protocol.packets.ExecutePacket;
import com.oscar.protocol.packets.FetchMorePacket;
import com.oscar.protocol.packets.FetchPacket;
import com.oscar.protocol.packets.FunctionCallPacketV2;
import com.oscar.protocol.packets.FunctionResponsePacket;
import com.oscar.protocol.packets.ImportExportResponsePacket;
import com.oscar.protocol.packets.ImportPacket;
import com.oscar.protocol.packets.NoticeResponsePacket;
import com.oscar.protocol.packets.ParamInforPacket;
import com.oscar.protocol.packets.PlanIDPacket;
import com.oscar.protocol.packets.QueryPacketV2;
import com.oscar.protocol.packets.ReadyForQueryPacket;
import com.oscar.protocol.packets.RowDescriptionPacket;
import com.oscar.protocol.packets.TerminatePacketV2;
import com.oscar.protocol.stream.OStream;
import com.oscar.util.OSQLException;
import com.oscar.util.converter.RowidConverter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

public class OSCARProtocolV2
extends OSCARProtocol {
    protected InputStream osr_input;
    protected BufferedOutputStream osr_output;

    public OSCARProtocolV2(BaseConnection con, String _host, int _port, String _database, String user, String _password, Properties _info, OStream oStream) {
        super(con, _host, _port, _database, user, _password, _info, oStream);
        this.osr_input = oStream.getInputStream();
        this.osr_output = oStream.getBufferedOutputStream();
    }

    public BaseResultSet fetchMore(String query, String prepareName, int[] m_bindTypes, Object[] m_bindDatas, byte[] planID, int fetchSize, int maxRows, boolean finished, boolean isPrepare, BaseStatement stmt, boolean bindTypeChanged) throws SQLException {
        return this.fetchMore(query, prepareName, m_bindTypes, m_bindDatas, planID, fetchSize, maxRows, finished, isPrepare, stmt, (BaseResultSet)stmt.getResultSet(), bindTypeChanged);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public BaseResultSet fetchMore(String query, String prepareName, int[] m_bindTypes, Object[] m_bindDatas, byte[] planID, int fetchSize, int maxRows, boolean finished, boolean isPrepare, BaseStatement stmt, BaseResultSet res, boolean bindTypeChanged) throws SQLException {
        if (this.logFlag) {
            int i;
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID() + ", " + OSCARProtocolV2.class).append(", fetchMore, paras: ").append("\n");
            sb.append(" sql: ").append(query).append(", ");
            sb.append(" prepareName:").append(prepareName).append(", ");
            sb.append(" bindTypes: ");
            if (m_bindTypes == null) {
                sb.append("null");
            } else {
                for (i = 0; i < m_bindTypes.length; ++i) {
                    sb.append(m_bindTypes[i]).append(" ");
                }
            }
            if (m_bindDatas != null) {
                sb.append(" bindDatas: ");
                for (int i2 = 0; i2 < m_bindDatas.length; ++i2) {
                    byte[] tmp = (byte[])m_bindDatas[i2];
                    if (tmp == null) {
                        sb.append("null ");
                        continue;
                    }
                    for (int j = 0; j < tmp.length; ++j) {
                        sb.append(tmp[j]).append(" ");
                    }
                }
            }
            sb.append(" planID: ");
            if (planID != null) {
                for (i = 0; i < planID.length; ++i) {
                    sb.append(planID[i]).append(" ");
                }
            } else {
                sb.append("null");
            }
            sb.append(" fetchSize: ").append(fetchSize);
            sb.append(" maxRows: ").append(maxRows);
            sb.append(" fetch close: ").append(finished);
            sb.append(" isprepare: ").append(isPrepare);
            sb.append(" bindTypeChanged: ").append(bindTypeChanged);
            Driver.writeLog(sb.toString());
        }
        OStream oStream = this.oStream;
        synchronized (oStream) {
            OSQLException exception = null;
            this.statement = stmt;
            this.status = 0;
            this.resultset = res;
            if (this.resultset != null && !this.resultset.isCursorUsed()) {
                this.resultset = null;
            }
            try {
                boolean encodingFlag;
                int marked = 0;
                boolean bl = encodingFlag = this.connection.getEncoding() == null;
                if (this.statement.isDDLSql()) {
                    marked = 3;
                    this.statement.resetDDLSql(false);
                } else if (this.statement.isPrepareAndNotRealPrepare()) {
                    marked = 4;
                } else if (this.statement.getAutoGeneratedInfo() != -1) {
                    String sql;
                    marked = 2;
                    if (query != null && !(sql = this.statement.generatedKeySqlTransform(query)).equals(query)) {
                        query = sql;
                        marked = 0;
                    }
                } else if (this.statement.useTid()) {
                    marked = 1;
                }
                BasePacket qp = null;
                qp = planID == null ? new FetchPacket(query == null ? null : this.connection.getEncoding().encode(query), prepareName == null ? null : this.connection.getEncoding().encode(prepareName), m_bindTypes, m_bindDatas, fetchSize, isPrepare, marked, bindTypeChanged, this.connection.getProtocolVersion().getProtocolType()) : new FetchMorePacket(fetchSize, planID, finished);
                qp.setConnection(this.connection);
                this.tuples = new ArrayList();
                this.resultTid = false;
                this.tidList = null;
                this.fields = this.statement.getFields();
                this.update_count = -2;
                this.insert_tid = 0L;
                this.sendMessage(this.osr_output, qp);
                do {
                    boolean timeout = true;
                    int getMessageTimes = 0;
                    do {
                        try {
                            this.bk = this.getMessage(this.osr_input);
                            timeout = false;
                        }
                        catch (Throwable e) {
                            if (this.isSocketConnectionError(e)) {
                                timeout = false;
                                this.status = -1;
                                throw new OSQLException("OSCAR-00901", "08003", 901, e);
                            }
                            if (this.ping(this.oStream)) {
                                if (e.getMessage().equals("Read timed out") || !this.needRetry(++getMessageTimes)) {
                                    timeout = false;
                                    this.status = -1;
                                    throw new OSQLException("OSCAR-00109", "08003", 109, e);
                                }
                                timeout = true;
                                continue;
                            }
                            timeout = false;
                            this.status = -1;
                            throw new OSQLException("OSCAR-00901", "08003", 901, e);
                        }
                    } while (timeout);
                    if (this.bk instanceof PlanIDPacket) {
                        this.getMessage(this.osr_input, this.bk);
                        planID = ((PlanIDPacket)this.bk).getPlanID();
                        continue;
                    }
                    if (this.bk instanceof RowDescriptionPacket) {
                        this.getMessage(this.osr_input, this.bk);
                        Field[] tempFields = ((RowDescriptionPacket)this.bk).getFields();
                        this.columnCount = tempFields.length;
                        if (this.columnCount > 0) {
                            if ((this.statement.useTid() || this.statement.getAutoGeneratedInfo() != -1) && tempFields[0].getAliasName().equalsIgnoreCase("ROWID")) {
                                this.resultTid = true;
                                this.tidField = tempFields[0];
                                this.tidList = new ArrayList();
                                if (this.columnCount != 1) {
                                    this.fields = new Field[this.columnCount - 1];
                                    for (int i = 0; i < this.columnCount - 1; ++i) {
                                        this.fields[i] = tempFields[i + 1];
                                    }
                                }
                            } else {
                                this.fields = tempFields;
                            }
                        } else {
                            this.fields = new Field[0];
                        }
                        this.statement.setFields(this.fields);
                        continue;
                    }
                    if (this.bk instanceof AsciiRowPacketV2) {
                        ((AsciiRowPacketV2)this.bk).initTuple(this.columnCount, this.fields);
                        this.getMessage(this.osr_input, this.bk);
                        byte[][] tempTuple = ((AsciiRowPacketV2)this.bk).getTuple();
                        Object tuple = null;
                        if (this.columnCount <= 0 || maxRows != 0 && this.tuples.size() >= maxRows) continue;
                        if (this.resultTid && tempTuple.length > 0) {
                            this.tidList.add(tempTuple[0]);
                            tuple = new byte[tempTuple.length - 1][];
                            for (int i = 0; i < tempTuple.length - 1; ++i) {
                                tuple[i] = tempTuple[i + 1];
                            }
                        } else {
                            tuple = tempTuple;
                        }
                        this.tuples.add(tuple);
                        continue;
                    }
                    if (this.bk instanceof CompleteResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        String command = this.connection.getEncoding().decode(((CompleteResponsePacket)this.bk).getCommand());
                        char tag1 = command.charAt(0);
                        char tag2 = command.charAt(1);
                        if (tag1 == '5' && tag2 == '0') {
                            char tag3 = command.charAt(3);
                            if (tag3 == '0') {
                                this.statement.setResultSetCanUpdateable(false);
                            } else if (tag3 == '1') {
                                this.statement.setResultSetCanUpdateable(true);
                            }
                        } else if (tag1 == '3') {
                            if (tag2 == '0') {
                                this.connection.setInTranscation(true);
                            } else if (tag2 == '1' || tag2 == '2' || tag2 == '3') {
                                this.connection.setInTranscation(false);
                            }
                        } else if (tag1 == '0' && tag2 != '6' && tag2 <= 'S' || tag1 == '1' && (tag2 == '0' || tag2 == '1') || tag1 == '2' && tag2 != '2' || tag1 == '5' && tag2 == '5' || tag1 == '4' && tag2 == '0' || tag1 == '4' && tag2 == '5' || tag1 == '4' && tag2 == '8' || tag1 == '5' && tag2 == '6' || tag1 == '5' && tag2 == 'D') {
                            int updateCountOffset;
                            if (tag1 == '0' && tag2 != '6' && tag2 <= 'S') {
                                this.update_count = 0;
                            }
                            if (tag1 == '4' && tag2 == '0') {
                                this.update_count = 0;
                            }
                            byte[] cmd = ((CompleteResponsePacket)this.bk).getCommand();
                            if (tag1 == '2' && (tag2 == '0' || tag2 == '5')) {
                                updateCountOffset = this.getFirstBlankPosition(cmd, 0);
                                this.update_count = (int)RowidConverter.convertToRowID(cmd, updateCountOffset + 1);
                            }
                            if (tag1 == '2' && tag2 == '1') {
                                updateCountOffset = this.getFirstBlankPosition(cmd, 0);
                                HashMap<String, Integer> map = RowidConverter.convertToUpdateCount(cmd, updateCountOffset + 1);
                                this.update_count = map.get("updatecount");
                                int insertTidOffset = map.get("tidoffset");
                                this.insert_tid = insertTidOffset <= updateCountOffset ? 0L : RowidConverter.convertToRowID(cmd, insertTidOffset + 1);
                            }
                            if (this.resultset == null) {
                                this.resultset = this.statement.createResultSet(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                int index = this.connection.checkPlanID(planID);
                                if (planID != null && this.tuples != null && this.tuples.size() >= fetchSize) {
                                    if (index == -1) {
                                        this.resultset.setPlanID(planID);
                                        this.connection.addPlanID(planID);
                                    }
                                } else {
                                    if (index > -1) {
                                        this.connection.removePlanID(index);
                                    }
                                    this.resultset.setPlanID(null);
                                }
                                if (this.resultTid) {
                                    this.resultset.setTidValues(this.tidField, this.tidList);
                                }
                                if (tag1 == '1' && tag2 == '0' && this.statement.isCursorUsed()) {
                                    this.resultset.setCursorUsed(true);
                                }
                            } else {
                                if (tag1 == '1' && tag2 == '0' && this.statement.isCursorUsed()) {
                                    this.resultset.setCursorUsed(true);
                                } else if (tag1 == '1' && tag2 == '1' && this.statement.isCursorUsed()) {
                                    this.resultset.setCursorUsed(true);
                                } else {
                                    this.resultset.setCursorUsed(false);
                                }
                                if (this.resultset.isCursorUsed()) {
                                    int moveSize = 0;
                                    moveSize = Integer.parseInt(command.substring(1 + command.indexOf(32)));
                                    if (tag1 == '1' && tag2 == '0') {
                                        this.resultset.reInit(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                        if (this.resultTid) {
                                            this.resultset.setTidValues(this.tidField, this.tidList);
                                        }
                                        this.resultset.setCursorMoveSize(moveSize);
                                        int index = this.connection.checkPlanID(planID);
                                        if (planID != null && this.tuples != null && this.tuples.size() >= fetchSize) {
                                            if (index == -1) {
                                                this.resultset.setPlanID(planID);
                                                this.connection.addPlanID(planID);
                                            }
                                        } else {
                                            if (index > -1) {
                                                this.connection.removePlanID(index);
                                            }
                                            this.resultset.setPlanID(null);
                                        }
                                    } else if (tag1 == '1' && tag2 == '1') {
                                        this.resultset.setCursorMoveSize(moveSize);
                                    }
                                } else if (this.resultset.isClosed()) {
                                    this.resultset.reInit(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                    if (this.resultTid) {
                                        this.resultset.setTidValues(this.tidField, this.tidList);
                                    }
                                } else {
                                    BaseResultSet rs = this.statement.createResultSet(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                    if (this.resultTid) {
                                        this.resultset.setTidValues(this.tidField, this.tidList);
                                    }
                                    this.resultset.append(rs);
                                }
                            }
                        }
                        this.tuples = new ArrayList();
                        this.resultTid = false;
                        this.tidList = null;
                        this.fields = null;
                        this.update_count = -2;
                        this.insert_tid = 0L;
                        continue;
                    }
                    if (this.bk instanceof ParamInforPacket) {
                        this.getMessage(this.osr_input, this.bk);
                        this.statement.setParamInfor(((ParamInforPacket)this.bk).getParamInfo());
                        continue;
                    }
                    if (this.bk instanceof EmptyQueryResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        continue;
                    }
                    if (this.bk instanceof CursorResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        continue;
                    }
                    if (this.bk instanceof ErrorResponsePacket) {
                        this.status = -1;
                        this.getMessage(this.osr_input, this.bk);
                        ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                        if (encodingFlag) {
                            if (exception == null) {
                                exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                                continue;
                            }
                            exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                            continue;
                        }
                        if (exception == null) {
                            exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                            continue;
                        }
                        exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                        continue;
                    }
                    if (this.bk instanceof NoticeResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        if (encodingFlag) {
                            this.statement.addWarning(this.connection.getClientEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                            continue;
                        }
                        this.statement.addWarning(this.connection.getEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                        continue;
                    }
                    if (this.bk instanceof ImportPacket) {
                        ((ImportPacket)this.bk).setEncoding(this.statement.getDBConnection().getEncoding());
                        ((ImportPacket)this.bk).setImportValues(this.statement.getImportValues());
                        this.sendMessage(this.osr_output, this.bk);
                        this.statement.importValues(null);
                        continue;
                    }
                    if (this.bk instanceof ImportExportResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        this.statement.setTransferRowCount(((ImportExportResponsePacket)this.bk).getAmount());
                        continue;
                    }
                    if (this.bk instanceof ReadyForQueryPacket) {
                        this.status = 1;
                        this.getMessage(this.osr_input, this.bk);
                        continue;
                    }
                    this.status = -1;
                    OSQLException e = new OSQLException("OSCAR-00109", "08003", 109);
                    if (this.logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                    }
                    throw e;
                } while (!(this.bk instanceof ReadyForQueryPacket));
                if (exception != null) {
                    if (this.logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + exception.getMessage());
                    }
                    throw exception;
                }
            }
            catch (SocketTimeoutException e) {
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                }
                this.closePlanID(planID);
                this.status = -1;
                throw new OSQLException("OSCAR-00901", "08003", 901, e);
            }
            catch (IOException e) {
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                }
                this.closePlanID(planID);
                this.status = -1;
                throw new OSQLException("OSCAR-00109", "08003", 109, e);
            }
            catch (OSQLException e) {
                this.closePlanID(planID);
                throw e;
            }
            catch (Exception e) {
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                }
                this.closePlanID(planID);
                this.status = -1;
                throw new OSQLException("OSCAR-00318", "08003", 318, e);
            }
            return this.resultset;
        }
    }

    private void closePlanID(byte[] planID) {
        if (this.resultset == null && planID != null) {
            int index = this.connection.checkPlanID(planID);
            if (index > -1) {
                try {
                    this.fetchMore(null, null, null, null, planID, 0, 0, true, false, this.statement, false);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (index > -1) {
                this.connection.removePlanID(index);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public BaseResultSet execute(String prepareSQL, String statementName, int[] bindTypes, Object[] m_binds, int maxRows, BaseStatement stmt, boolean bindTypeChanged) throws SQLException {
        if (this.logFlag) {
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID() + ", " + OSCARProtocolV2.class).append(", execute, paras: ").append("\n");
            sb.append(" sql: ").append(prepareSQL).append(", ");
            sb.append(" prepareName:").append(statementName).append(", ");
            sb.append(" bindTypes: ");
            if (bindTypes == null) {
                sb.append("null");
            } else {
                for (int i = 0; i < bindTypes.length; ++i) {
                    sb.append(bindTypes[i]).append(" ");
                }
            }
            if (m_binds != null) {
                sb.append(" bindDatas: ");
                for (int i = 0; i < m_binds.length; ++i) {
                    byte[] tmp = (byte[])m_binds[i];
                    if (tmp == null) {
                        sb.append("null").append(" ");
                        continue;
                    }
                    for (int j = 0; j < tmp.length; ++j) {
                        sb.append(tmp[j]).append(" ");
                    }
                }
                sb.append("\n");
            }
            sb.append(" maxRows: ").append(maxRows);
            sb.append(" bindTypeChanged: ").append(bindTypeChanged);
            Driver.writeLog(sb.toString());
        }
        OStream oStream = this.oStream;
        synchronized (oStream) {
            OSQLException exception = null;
            this.statement = stmt;
            this.status = 0;
            this.resultset = (BaseResultSet)stmt.getResultSet();
            if (this.resultset != null && !this.resultset.isCursorUsed()) {
                this.resultset = null;
            }
            try {
                boolean encodingFlag;
                int marked = 0;
                boolean bl = encodingFlag = this.connection.getEncoding() == null;
                if (this.statement.isDDLSql()) {
                    marked = 3;
                    this.statement.resetDDLSql(false);
                } else if (this.statement.isPrepareAndNotRealPrepare()) {
                    marked = 4;
                } else if (this.statement.getAutoGeneratedInfo() != -1) {
                    String sql;
                    marked = 2;
                    if (prepareSQL != null && !(sql = this.statement.generatedKeySqlTransform(prepareSQL)).equals(prepareSQL)) {
                        prepareSQL = sql;
                        marked = 0;
                    }
                } else if (this.statement.useTid()) {
                    marked = 1;
                }
                ExecutePacket qp = new ExecutePacket(this.connection.getEncoding(), prepareSQL, statementName, bindTypes, m_binds, marked, bindTypeChanged, this.statement.isHaveFuncRetrun(), this.connection.getProtocolVersion().getProtocolType());
                qp.setConnection(this.connection);
                this.tuples = new ArrayList();
                this.resultTid = false;
                this.tidList = null;
                this.fields = this.statement.getFields();
                this.update_count = -2;
                this.insert_tid = 0L;
                Field[] tmpFields = this.statement.getFields();
                this.sendMessage(this.osr_output, qp);
                do {
                    int i;
                    boolean timeout = true;
                    int getMessageTimes = 0;
                    do {
                        try {
                            this.bk = this.getMessage(this.osr_input);
                            timeout = false;
                        }
                        catch (Throwable e) {
                            if (this.isSocketConnectionError(e)) {
                                timeout = false;
                                this.status = -1;
                                throw new OSQLException("OSCAR-00901", "08003", 901, e);
                            }
                            if (this.ping(this.oStream)) {
                                if (e.getMessage().equals("Read timed out") || !this.needRetry(++getMessageTimes)) {
                                    timeout = false;
                                    this.status = -1;
                                    throw new OSQLException("OSCAR-00109", "08003", 109, e);
                                }
                                timeout = true;
                                continue;
                            }
                            timeout = false;
                            this.status = -1;
                            throw new OSQLException("OSCAR-00901", "08003", 901, e);
                        }
                    } while (timeout);
                    if (this.bk instanceof RowDescriptionPacket) {
                        this.getMessage(this.osr_input, this.bk);
                        Field[] tempFields = ((RowDescriptionPacket)this.bk).getFields();
                        this.columnCount = tempFields.length;
                        if (this.columnCount > 0) {
                            if ((this.statement.useTid() || this.statement.getAutoGeneratedInfo() != -1) && tempFields[0].getAliasName().equalsIgnoreCase("ROWID")) {
                                this.resultTid = true;
                                this.tidField = tempFields[0];
                                this.tidList = new ArrayList();
                                if (this.columnCount != 1) {
                                    this.fields = new Field[this.columnCount - 1];
                                    for (int i2 = 0; i2 < this.columnCount - 1; ++i2) {
                                        this.fields[i2] = tempFields[i2 + 1];
                                    }
                                }
                            } else {
                                this.fields = tempFields;
                            }
                        } else {
                            this.fields = new Field[0];
                        }
                        if (tmpFields != null && this.fields.length != tmpFields.length && tmpFields.length > this.fields.length) {
                            for (int j = 0; j < this.fields.length; ++j) {
                                for (i = 0; i < tmpFields.length; ++i) {
                                    if (tmpFields[i] == null) continue;
                                    tmpFields[i] = this.fields[j];
                                }
                            }
                            this.statement.setFields(tempFields);
                            continue;
                        }
                        this.statement.setFields(this.fields);
                        continue;
                    }
                    if (this.bk instanceof AsciiRowPacketV2) {
                        this.columnCount = this.fields.length;
                        ((AsciiRowPacketV2)this.bk).initTuple(this.columnCount, this.fields);
                        this.getMessage(this.osr_input, this.bk);
                        byte[][] tempTuple = ((AsciiRowPacketV2)this.bk).getTuple();
                        Object tuple = null;
                        if (this.columnCount <= 0 || maxRows != 0 && this.tuples.size() >= maxRows) continue;
                        if (this.resultTid && tempTuple.length > 0) {
                            this.tidList.add(tempTuple[0]);
                            tuple = new byte[tempTuple.length - 1][];
                            for (i = 0; i < tempTuple.length - 1; ++i) {
                                tuple[i] = tempTuple[i + 1];
                            }
                        } else {
                            tuple = tempTuple;
                        }
                        this.tuples.add(tuple);
                        continue;
                    }
                    if (this.bk instanceof CompleteResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        String command = this.connection.getEncoding().decode(((CompleteResponsePacket)this.bk).getCommand());
                        char tag1 = command.charAt(0);
                        char tag2 = command.charAt(1);
                        if (tag1 == '5' && tag2 == '0') {
                            char tag3 = command.charAt(3);
                            if (tag3 == '0') {
                                this.statement.setResultSetCanUpdateable(false);
                            } else if (tag3 == '1') {
                                this.statement.setResultSetCanUpdateable(true);
                            }
                        } else if (tag1 == '3') {
                            if (tag2 == '0') {
                                this.connection.setInTranscation(true);
                            } else if (tag2 == '1' || tag2 == '2' || tag2 == '3') {
                                this.connection.setInTranscation(false);
                            }
                        } else if (tag1 == '0' && tag2 != '6' && tag2 <= 'S' || tag1 == '1' && (tag2 == '0' || tag2 == '1') || tag1 == '2' && tag2 != '2' || tag1 == '5' && tag2 == '5' || tag1 == '4' && tag2 == '0' || tag1 == '4' && tag2 == '5' || tag1 == '4' && tag2 == '8' || tag1 == '5' && tag2 == '6' || tag1 == '5' && tag2 == 'D') {
                            int updateCountOffset;
                            if (tag1 == '0' && tag2 != '6' && tag2 <= 'S') {
                                this.update_count = 0;
                            }
                            if (tag1 == '4' && tag2 == '0') {
                                this.update_count = 0;
                            }
                            byte[] cmd = ((CompleteResponsePacket)this.bk).getCommand();
                            if (tag1 == '2' && (tag2 == '0' || tag2 == '5')) {
                                updateCountOffset = this.getFirstBlankPosition(cmd, 0);
                                this.update_count = (int)RowidConverter.convertToRowID(cmd, updateCountOffset + 1);
                            }
                            if (tag1 == '2' && tag2 == '1') {
                                updateCountOffset = this.getFirstBlankPosition(cmd, 0);
                                HashMap<String, Integer> map = RowidConverter.convertToUpdateCount(cmd, updateCountOffset + 1);
                                this.update_count = map.get("updatecount");
                                int insertTidOffset = map.get("tidoffset");
                                this.insert_tid = insertTidOffset <= updateCountOffset ? 0L : RowidConverter.convertToRowID(cmd, insertTidOffset + 1);
                            }
                            if (this.resultset == null) {
                                this.resultset = this.statement.createResultSet(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                if (this.resultTid) {
                                    this.resultset.setTidValues(this.tidField, this.tidList);
                                }
                                if (tag1 == '1' && tag2 == '0' && this.statement.isCursorUsed()) {
                                    this.resultset.setCursorUsed(true);
                                }
                                this.connection.addCursor(this.resultset.getCursorName());
                            } else {
                                if (tag1 == '1' && tag2 == '0' && this.statement.isCursorUsed()) {
                                    this.resultset.setCursorUsed(true);
                                } else if (tag1 == '1' && tag2 == '1' && this.statement.isCursorUsed()) {
                                    this.resultset.setCursorUsed(true);
                                } else {
                                    this.resultset.setCursorUsed(false);
                                }
                                if (this.resultset.isCursorUsed()) {
                                    int moveSize = 0;
                                    moveSize = Integer.parseInt(command.substring(1 + command.indexOf(32)));
                                    if (tag1 == '1' && tag2 == '0') {
                                        this.resultset.reInit(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                        if (this.resultTid) {
                                            this.resultset.setTidValues(this.tidField, this.tidList);
                                        }
                                        this.resultset.setCursorMoveSize(moveSize);
                                    } else if (tag1 == '1' && tag2 == '1') {
                                        this.resultset.setCursorMoveSize(moveSize);
                                    }
                                } else if (this.resultset.isClosed()) {
                                    this.resultset.reInit(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                    if (this.resultTid) {
                                        this.resultset.setTidValues(this.tidField, this.tidList);
                                    }
                                    this.connection.addCursor(this.resultset.getCursorName());
                                } else {
                                    BaseResultSet rs = this.statement.createResultSet(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                    if (this.resultTid) {
                                        this.resultset.setTidValues(this.tidField, this.tidList);
                                    }
                                    this.resultset.append(rs);
                                    this.connection.addCursor(rs.getCursorName());
                                }
                            }
                        }
                        this.tuples = new ArrayList();
                        this.resultTid = false;
                        this.tidList = null;
                        this.fields = null;
                        this.update_count = -2;
                        this.insert_tid = 0L;
                        continue;
                    }
                    if (this.bk instanceof ParamInforPacket) {
                        this.getMessage(this.osr_input, this.bk);
                        this.statement.setParamInfor(((ParamInforPacket)this.bk).getParamInfo());
                        continue;
                    }
                    if (this.bk instanceof EmptyQueryResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        continue;
                    }
                    if (this.bk instanceof CursorResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        continue;
                    }
                    if (this.bk instanceof ErrorResponsePacket) {
                        this.status = -1;
                        this.getMessage(this.osr_input, this.bk);
                        ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                        if (encodingFlag) {
                            if (exception == null) {
                                exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                                continue;
                            }
                            exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                            continue;
                        }
                        if (exception == null) {
                            exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                            continue;
                        }
                        exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                        continue;
                    }
                    if (this.bk instanceof NoticeResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        if (encodingFlag) {
                            this.statement.addWarning(this.connection.getClientEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                            continue;
                        }
                        this.statement.addWarning(this.connection.getEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                        continue;
                    }
                    if (this.bk instanceof ImportPacket) {
                        ((ImportPacket)this.bk).setEncoding(this.statement.getDBConnection().getEncoding());
                        ((ImportPacket)this.bk).setImportValues(this.statement.getImportValues());
                        this.sendMessage(this.osr_output, this.bk);
                        this.statement.importValues(null);
                        continue;
                    }
                    if (this.bk instanceof ImportExportResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        this.statement.setTransferRowCount(((ImportExportResponsePacket)this.bk).getAmount());
                        continue;
                    }
                    if (this.bk instanceof ReadyForQueryPacket) {
                        this.status = 1;
                        this.getMessage(this.osr_input, this.bk);
                        continue;
                    }
                    this.status = -1;
                    OSQLException e = new OSQLException("OSCAR-00109", "08003", 109);
                    if (this.logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + e.getMessage());
                    }
                    throw e;
                } while (!(this.bk instanceof ReadyForQueryPacket));
                if (exception != null) {
                    if (this.logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + exception.getMessage());
                    }
                    throw exception;
                }
            }
            catch (SocketTimeoutException e) {
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                }
                this.status = -1;
                throw new OSQLException("OSCAR-00901", "08003", 901, e);
            }
            catch (IOException e) {
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                }
                this.status = -1;
                throw new OSQLException("OSCAR-00109", "08003", 109, e);
            }
            catch (OSQLException e) {
                throw e;
            }
            catch (Exception e) {
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                }
                this.status = -1;
                throw new OSQLException("OSCAR-00318", "08003", 318, e);
            }
            return this.resultset;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public BaseResultSet query(String queryStr, int maxRows, BaseStatement stmt, BaseResultSet res) throws SQLException {
        if (this.logFlag) {
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID() + ", " + OSCARProtocolV2.class).append(", query, paras: ").append("\n");
            sb.append(" sql: ").append(queryStr).append(", ");
            sb.append(" maxRows: ").append(maxRows);
            Driver.writeLog(sb.toString());
        }
        OStream oStream = this.oStream;
        synchronized (oStream) {
            OSQLException exception = null;
            this.statement = stmt;
            this.status = 0;
            boolean encodingFlag = this.connection.getEncoding() == null;
            this.resultset = res;
            if (this.resultset != null && !this.resultset.isCursorUsed()) {
                this.resultset = null;
            }
            try {
                int marked = 0;
                if (this.statement.isDDLSql()) {
                    marked = 0;
                    this.statement.resetDDLSql(false);
                } else if (this.statement.isPrepareAndNotRealPrepare()) {
                    marked = 4;
                } else if (this.statement.getAutoGeneratedInfo() != -1) {
                    String sql;
                    marked = 2;
                    if (queryStr != null && !(sql = this.statement.generatedKeySqlTransform(queryStr)).equals(queryStr)) {
                        queryStr = sql;
                        marked = 0;
                    }
                } else if (this.statement.useTid()) {
                    marked = 1;
                }
                QueryPacketV2 qp = null;
                qp = encodingFlag ? new QueryPacketV2(this.connection.getClientEncoding().encode(queryStr), marked) : new QueryPacketV2(this.connection.getEncoding().encode(queryStr), marked);
                qp.setConnection(this.connection);
                this.tuples = new ArrayList();
                this.resultTid = false;
                this.tidList = null;
                this.fields = null;
                this.update_count = -2;
                this.insert_tid = 0L;
                Field[] tempFields = null;
                this.sendMessage(this.osr_output, qp);
                do {
                    boolean timeout = true;
                    int getMessageTimes = 0;
                    do {
                        try {
                            this.bk = this.getMessage(this.osr_input);
                            timeout = false;
                        }
                        catch (Throwable e) {
                            if (this.isSocketConnectionError(e)) {
                                timeout = false;
                                this.status = -1;
                                throw new OSQLException("OSCAR-00901", "08003", 901, e);
                            }
                            if (this.ping(this.oStream)) {
                                if (e.getMessage().equals("Read timed out") || !this.needRetry(++getMessageTimes)) {
                                    timeout = false;
                                    this.status = -1;
                                    throw new OSQLException("OSCAR-00109", "08003", 109, e);
                                }
                                timeout = true;
                                continue;
                            }
                            timeout = false;
                            this.status = -1;
                            throw new OSQLException("OSCAR-00901", "08003", 901, e);
                        }
                    } while (timeout);
                    if (this.bk instanceof RowDescriptionPacket) {
                        this.getMessage(this.osr_input, this.bk);
                        tempFields = ((RowDescriptionPacket)this.bk).getFields();
                        this.columnCount = tempFields.length;
                        if (this.columnCount > 0) {
                            if ((this.statement.useTid() || this.statement.getAutoGeneratedInfo() != -1) && tempFields[0].getAliasName().equalsIgnoreCase("ROWID")) {
                                this.resultTid = true;
                                this.tidField = tempFields[0];
                                this.tidList = new ArrayList();
                                if (this.columnCount != 1) {
                                    this.fields = new Field[this.columnCount - 1];
                                    for (int i = 0; i < this.columnCount - 1; ++i) {
                                        this.fields[i] = tempFields[i + 1];
                                    }
                                }
                            } else {
                                this.fields = tempFields;
                            }
                        } else {
                            this.fields = new Field[0];
                        }
                        this.statement.setFields(this.fields);
                        continue;
                    }
                    if (this.bk instanceof AsciiRowPacketV2) {
                        if (this.resultTid) {
                            ((AsciiRowPacketV2)this.bk).initTuple(this.columnCount, tempFields);
                        } else {
                            ((AsciiRowPacketV2)this.bk).initTuple(this.columnCount, this.fields);
                        }
                        this.getMessage(this.osr_input, this.bk);
                        byte[][] tempTuple = ((AsciiRowPacketV2)this.bk).getTuple();
                        Object tuple = null;
                        if (this.columnCount <= 0 || maxRows != 0 && this.tuples.size() >= maxRows) continue;
                        if (this.resultTid && tempTuple.length > 0) {
                            this.tidList.add(tempTuple[0]);
                            tuple = new byte[tempTuple.length - 1][];
                            for (int i = 0; i < tempTuple.length - 1; ++i) {
                                tuple[i] = tempTuple[i + 1];
                            }
                        } else {
                            tuple = tempTuple;
                        }
                        this.tuples.add(tuple);
                        continue;
                    }
                    if (this.bk instanceof CompleteResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        String command = null;
                        command = encodingFlag ? this.connection.getClientEncoding().decode(((CompleteResponsePacket)this.bk).getCommand()) : this.connection.getEncoding().decode(((CompleteResponsePacket)this.bk).getCommand());
                        char tag1 = command.charAt(0);
                        char tag2 = command.charAt(1);
                        if (tag1 == '5' && tag2 == '0') {
                            char tag3 = command.charAt(3);
                            if (tag3 == '0') {
                                this.statement.setResultSetCanUpdateable(false);
                            } else if (tag3 == '1') {
                                this.statement.setResultSetCanUpdateable(true);
                            }
                        } else if (tag1 == '3') {
                            if (tag2 == '0') {
                                this.connection.setInTranscation(true);
                            } else if (tag2 == '1' || tag2 == '2' || tag2 == '3') {
                                this.connection.setInTranscation(false);
                            }
                        } else if (tag1 == '0' && tag2 != '6' && tag2 <= 'S' || tag1 == '1' && (tag2 == '0' || tag2 == '1') || tag1 == '2' && tag2 != '2' || tag1 == '5' && tag2 == '5' || tag1 == '4' && tag2 == '0' || tag1 == '4' && tag2 == '5' || tag1 == '4' && tag2 == '8' || tag1 == '5' && tag2 == '6' || tag1 == '5' && tag2 == 'D') {
                            int updateCountOffset;
                            if (tag1 == '0' && tag2 != '6' && tag2 <= 'S') {
                                this.update_count = 0;
                            }
                            if (tag1 == '4' && tag2 == '0') {
                                this.update_count = 0;
                            }
                            byte[] cmd = ((CompleteResponsePacket)this.bk).getCommand();
                            if (tag1 == '2' && (tag2 == '0' || tag2 == '5')) {
                                updateCountOffset = this.getFirstBlankPosition(cmd, 0);
                                this.update_count = (int)RowidConverter.convertToRowID(cmd, updateCountOffset + 1);
                            }
                            if (tag1 == '2' && tag2 == '1') {
                                updateCountOffset = this.getFirstBlankPosition(cmd, 0);
                                HashMap<String, Integer> map = RowidConverter.convertToUpdateCount(cmd, updateCountOffset + 1);
                                this.update_count = map.get("updatecount");
                                int insertTidOffset = map.get("tidoffset");
                                this.insert_tid = insertTidOffset <= updateCountOffset ? 0L : RowidConverter.convertToRowID(cmd, insertTidOffset + 1);
                            }
                            if (this.resultset == null) {
                                this.resultset = this.statement.createResultSet(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                if (this.resultTid) {
                                    this.resultset.setTidValues(this.tidField, this.tidList);
                                }
                                if (tag1 == '1' && tag2 == '0' && this.statement.isCursorUsed()) {
                                    this.resultset.setCursorUsed(true);
                                }
                                this.connection.addCursor(this.resultset.getCursorName());
                            } else {
                                if (tag1 == '1' && tag2 == '0' && this.statement.isCursorUsed()) {
                                    this.resultset.setCursorUsed(true);
                                } else if (tag1 == '1' && tag2 == '1' && this.statement.isCursorUsed()) {
                                    this.resultset.setCursorUsed(true);
                                } else {
                                    this.resultset.setCursorUsed(false);
                                }
                                if (this.resultset.isCursorUsed()) {
                                    int moveSize = 0;
                                    moveSize = Integer.parseInt(command.substring(1 + command.indexOf(32)));
                                    if (tag1 == '1' && tag2 == '0') {
                                        this.resultset.reInit(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                        if (this.resultTid) {
                                            this.resultset.setTidValues(this.tidField, this.tidList);
                                        }
                                        this.resultset.setCursorMoveSize(moveSize);
                                    } else if (tag1 == '1' && tag2 == '1') {
                                        this.resultset.setCursorMoveSize(moveSize);
                                    }
                                } else if (this.resultset.isClosed()) {
                                    this.resultset.reInit(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                    if (this.resultTid) {
                                        this.resultset.setTidValues(this.tidField, this.tidList);
                                    }
                                    this.connection.addCursor(this.resultset.getCursorName());
                                } else {
                                    BaseResultSet rs = this.statement.createResultSet(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                    if (this.resultTid) {
                                        this.resultset.setTidValues(this.tidField, this.tidList);
                                    }
                                    this.resultset.append(rs);
                                    this.connection.addCursor(rs.getCursorName());
                                }
                            }
                        }
                        this.tuples = new ArrayList();
                        this.resultTid = false;
                        this.tidList = null;
                        this.fields = null;
                        this.update_count = -2;
                        this.insert_tid = 0L;
                        continue;
                    }
                    if (this.bk instanceof ParamInforPacket) {
                        this.getMessage(this.osr_input, this.bk);
                        this.statement.setParamInfor(((ParamInforPacket)this.bk).getParamInfo());
                        continue;
                    }
                    if (this.bk instanceof EmptyQueryResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        continue;
                    }
                    if (this.bk instanceof CursorResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        continue;
                    }
                    if (this.bk instanceof ErrorResponsePacket) {
                        this.status = -1;
                        this.getMessage(this.osr_input, this.bk);
                        ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                        if (encodingFlag) {
                            if (exception == null) {
                                exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                                continue;
                            }
                            exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                            continue;
                        }
                        if (exception == null) {
                            exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                            continue;
                        }
                        exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                        continue;
                    }
                    if (this.bk instanceof NoticeResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        if (encodingFlag) {
                            this.statement.addWarning(this.connection.getClientEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                            continue;
                        }
                        this.statement.addWarning(this.connection.getEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                        continue;
                    }
                    if (this.bk instanceof ImportPacket) {
                        if (encodingFlag) {
                            ((ImportPacket)this.bk).setEncoding(this.statement.getDBConnection().getClientEncoding());
                        } else {
                            ((ImportPacket)this.bk).setEncoding(this.statement.getDBConnection().getEncoding());
                        }
                        ((ImportPacket)this.bk).setImportValues(this.statement.getImportValues());
                        this.sendMessage(this.osr_output, this.bk);
                        this.statement.importValues(null);
                        continue;
                    }
                    if (this.bk instanceof ImportExportResponsePacket) {
                        this.getMessage(this.osr_input, this.bk);
                        this.statement.setTransferRowCount(((ImportExportResponsePacket)this.bk).getAmount());
                        continue;
                    }
                    if (this.bk instanceof ReadyForQueryPacket) {
                        this.status = 1;
                        this.getMessage(this.osr_input, this.bk);
                        continue;
                    }
                    this.status = -1;
                    OSQLException e = new OSQLException("OSCAR-00109", "08003", 109);
                    if (this.logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                    }
                    throw e;
                } while (!(this.bk instanceof ReadyForQueryPacket));
                if (exception != null) {
                    if (this.logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + exception.getMessage());
                    }
                    throw exception;
                }
            }
            catch (SocketTimeoutException e) {
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                }
                this.status = -1;
                throw new OSQLException("OSCAR-00901", "08003", 901, e);
            }
            catch (IOException e) {
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                }
                this.status = -1;
                throw new OSQLException("OSCAR-00109", "08003", 109, e);
            }
            catch (OSQLException e) {
                throw e;
            }
            catch (Exception e) {
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                }
                this.status = -1;
                throw new OSQLException("OSCAR-00318", "08003", 318, e);
            }
            return this.resultset;
        }
    }

    private int getFirstBlankPosition(byte[] value, int offset) {
        if (value == null || offset >= value.length) {
            return -1;
        }
        for (int i = offset; i < value.length; ++i) {
            if (value[i] != 32) continue;
            return i;
        }
        return -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object[] functionCall(int funcOID, int paraCount, int[] paraLenth, Object[] paraValue) throws SQLException {
        if (this.logFlag) {
            int i;
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID() + ", " + OSCARProtocolV2.class).append(", functionCall, paras: ").append("\n");
            sb.append(" funcOID: ").append(funcOID).append(", ");
            sb.append(" paraCount: ").append(paraCount);
            sb.append(" funcOID: ").append(funcOID);
            if (paraLenth != null) {
                sb.append("paraLenth: ");
                for (i = 0; i < paraLenth.length; ++i) {
                    sb.append(paraLenth[i]).append(" ");
                }
            }
            if (paraLenth != null) {
                sb.append("paraValue: ");
                for (i = 0; i < paraValue.length; ++i) {
                    if (paraValue[i] instanceof byte[]) {
                        sb.append(Arrays.toString((byte[])paraValue[i]));
                        continue;
                    }
                    sb.append(paraValue[i]).append(" ");
                }
            }
            Driver.writeLog(sb.toString());
        }
        FunctionCallPacketV2 fcp = new FunctionCallPacketV2(funcOID, paraCount, paraLenth, paraValue);
        fcp.setConnection(this.connection);
        Object[] result = new Object[3];
        boolean encodingFlag = this.connection.getEncoding() == null;
        OStream oStream = this.oStream;
        synchronized (oStream) {
            OSQLException exception;
            block31: {
                exception = null;
                this.status = 0;
                try {
                    this.sendMessage(this.osr_output, fcp);
                }
                catch (IOException e) {
                    if (this.logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + e.getMessage());
                    }
                    throw new OSQLException("OSCAR-00108", "88888", 108, e.getMessage());
                }
                try {
                    do {
                        this.bk = this.getMessage(this.osr_input);
                        if (this.bk instanceof ErrorResponsePacket) {
                            this.status = -1;
                            this.getMessage(this.osr_input, this.bk);
                            ErrorResponsePacket errorPacket = (ErrorResponsePacket)this.bk;
                            if (encodingFlag) {
                                if (exception == null) {
                                    exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                                    continue;
                                }
                                exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                                continue;
                            }
                            if (exception == null) {
                                exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                                continue;
                            }
                            exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                            continue;
                        }
                        if (this.bk instanceof NoticeResponsePacket) {
                            this.getMessage(this.osr_input, this.bk);
                            if (encodingFlag) {
                                this.statement.addWarning(this.connection.getClientEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                                continue;
                            }
                            this.statement.addWarning(this.connection.getEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                            continue;
                        }
                        if (this.bk instanceof FunctionResponsePacket) {
                            this.getMessage(this.osr_input, this.bk);
                            result[0] = new Boolean(((FunctionResponsePacket)this.bk).isNull());
                            result[1] = new Integer(((FunctionResponsePacket)this.bk).getResultSize());
                            result[2] = ((FunctionResponsePacket)this.bk).getResult();
                            continue;
                        }
                        if (this.bk instanceof ReadyForQueryPacket) {
                            this.status = 1;
                            this.getMessage(this.osr_input, this.bk);
                            continue;
                        }
                        this.status = -1;
                        throw new OSQLException("OSCAR-00109", "08003", 109);
                    } while (!(this.bk instanceof ReadyForQueryPacket));
                }
                catch (SocketTimeoutException e) {
                    if (this.logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                    }
                    this.status = -1;
                    throw new OSQLException("OSCAR-00901", "08003", 901, e);
                }
                catch (IOException e) {
                    if (this.logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                    }
                    this.status = -1;
                    throw new OSQLException("OSCAR-00109", "08003", 109, e);
                }
                catch (OSQLException e) {
                    if (this.logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                    }
                    throw e;
                }
                catch (Exception e) {
                    if (!this.logFlag) break block31;
                    Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                }
            }
            if (exception != null) {
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + exception.getMessage());
                }
                throw exception;
            }
            return result;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void batchProcess(BatchProcessPacketV2 batchPacket, BaseStatement stmt, int[] updateCounts) throws SQLException {
        if (this.logFlag) {
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID() + ", " + OSCARProtocolV2.class).append(", batchProcess");
            Driver.writeLog(sb.toString());
        }
        OStream oStream = this.oStream;
        synchronized (oStream) {
            this.statement = stmt;
            int size = batchPacket.size();
            int position = 0;
            int lastPosition = 0;
            try {
                int j = 0;
                for (j = 0; j < size; ++j) {
                    batchPacket.writeRow();
                    if (!batchPacket.checkBuffer()) continue;
                    batchPacket.sendBatch(this.osr_output);
                    this.receiveBatchResult(position, updateCounts);
                    batchPacket.reInit();
                    lastPosition = this.reInitUpdateCounts(updateCounts, lastPosition, position);
                    position = j + 1;
                    this.statement.setUpdateBatchSize(position);
                }
                if (position != j) {
                    batchPacket.sendBatch(this.osr_output);
                    this.receiveBatchResult(position, updateCounts);
                    lastPosition = this.reInitUpdateCounts(updateCounts, lastPosition, position);
                    this.statement.setUpdateBatchSize(j);
                }
                if (batchPacket.hasPrepareSQL()) {
                    batchPacket.setPrepareSQL(null);
                }
            }
            catch (IOException ex) {
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + ex.getMessage());
                }
                throw new OSQLException("OSCAR-00109", "08003", 109, ex);
            }
            catch (OSQLException e) {
                throw e;
            }
            catch (Exception e) {
                if (this.logFlag) {
                    Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                }
                throw new OSQLException("OSCAR-00318", "08003", 318, e);
            }
        }
    }

    private int reInitUpdateCounts(int[] updateCounts, int lastPosition, int position) {
        if (lastPosition + updateCounts[position] <= updateCounts.length) {
            int count = updateCounts[position];
            for (int i = 0; i < count; ++i) {
                updateCounts[lastPosition++] = 1;
            }
            return lastPosition;
        }
        return lastPosition;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() throws IOException, SQLException {
        if (this.logFlag) {
            StringBuffer sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID() + ", " + OSCARProtocolV2.class).append(", close");
            Driver.writeLog(sb.toString());
        }
        TerminatePacketV2 tp = new TerminatePacketV2();
        tp.setConnection(this.connection);
        if (this.oStream != null) {
            OStream oStream = this.oStream;
            synchronized (oStream) {
                try {
                    try {
                        this.sendMessage(this.osr_output, tp);
                        int flag = 1;
                        this.oStream.setSocketTimeOut(1000);
                        while (flag != -1) {
                            try {
                                flag = this.oStream.getInputStream().read();
                            }
                            catch (IOException e) {
                                break;
                            }
                        }
                        Object var6_8 = null;
                    }
                    catch (IOException ioex) {
                        if (this.logFlag) {
                            Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + ioex.getMessage());
                        }
                        throw ioex;
                    }
                    catch (SQLException sqlEx) {
                        if (this.logFlag) {
                            Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + sqlEx.getMessage());
                        }
                        throw sqlEx;
                    }
                    catch (Exception e) {
                        if (this.logFlag) {
                            Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + e.getMessage());
                        }
                        throw new OSQLException("OSCAR-00318", "08003", 318, e);
                    }
                }
                catch (Throwable throwable) {
                    Object var6_9 = null;
                    this.oStream.close();
                    throw throwable;
                }
                this.oStream.close();
            }
        }
        tp = null;
        this.oStream = null;
        this.connection = null;
        this.host = "ErrorIP";
        this.database = null;
        this.db_user = null;
        this.db_passwd = null;
        this.statement = null;
        this.resultset = null;
        this.callResult = null;
        this.fields = null;
        this.tidField = null;
        this.tuples = null;
        this.bk = null;
        this.pk = new Packet();
    }

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public void receiveBatchResult(int position, int[] updateCounts) throws IOException, SQLException {
        if (this.logFlag) {
            sb = new StringBuffer();
            sb.append("session: " + this.connection.getSessionID() + ", " + OSCARProtocolV2.class).append(", receiveBatchResult");
            Driver.writeLog(sb.toString());
        }
        p = position;
        exception = null;
        this.resultset = (BaseResultSet)this.statement.getResultSet();
        encodingFlag = this.connection.getEncoding() == null;
        do {
            this.bk = this.getMessage(this.osr_input);
            if (!(this.bk instanceof RowDescriptionPacket)) ** GOTO lbl31
            this.getMessage(this.osr_input, this.bk);
            tempFields = ((RowDescriptionPacket)this.bk).getFields();
            this.columnCount = tempFields.length;
            if (this.columnCount <= 0) ** GOTO lbl29
            if ((this.statement.useTid() || this.statement.getAutoGeneratedInfo() != -1) && tempFields[0].getAliasName().equalsIgnoreCase("ROWID")) {
                this.resultTid = true;
                this.tidField = tempFields[0];
                this.tidList = new ArrayList<E>();
                if (this.columnCount == 1) continue;
                this.fields = new Field[this.columnCount - 1];
                for (i = 0; i < this.columnCount - 1; ++i) {
                    this.fields[i] = tempFields[i + 1];
                }
            } else {
                this.fields = tempFields;
                continue;
lbl29:
                // 1 sources

                this.fields = new Field[0];
                continue;
lbl31:
                // 1 sources

                if (this.bk instanceof AsciiRowPacketV2) {
                    this.columnCount = this.fields.length;
                    ((AsciiRowPacketV2)this.bk).initTuple(this.columnCount, this.fields);
                    this.getMessage(this.osr_input, this.bk);
                    tempTuple = ((AsciiRowPacketV2)this.bk).getTuple();
                    tuple /* !! */  = null;
                    if (this.columnCount <= 0) continue;
                    if (this.resultTid && tempTuple.length > 0) {
                        this.tidList.add(tempTuple[0]);
                        tuple /* !! */  = new byte[tempTuple.length - 1][];
                        for (i = 0; i < tempTuple.length - 1; ++i) {
                            tuple /* !! */ [i] = tempTuple[i + 1];
                        }
                    } else {
                        tuple /* !! */  = tempTuple;
                    }
                    this.tuples.add(tuple /* !! */ );
                    continue;
                }
                if (this.bk instanceof CompleteResponsePacket) {
                    this.getMessage(this.osr_input, this.bk);
                    command = this.connection.getEncoding().decode(((CompleteResponsePacket)this.bk).getCommand());
                    tag1 = command.charAt(0);
                    tag2 = command.charAt(1);
                    if (tag1 == '5' && tag2 == '0') {
                        tag3 = command.charAt(3);
                        if (tag3 == '0') {
                            this.statement.setResultSetCanUpdateable(false);
                        } else if (tag3 == '1') {
                            this.statement.setResultSetCanUpdateable(true);
                        }
                    } else if (tag1 == '3') {
                        if (tag2 == '0') {
                            this.connection.setInTranscation(true);
                        } else if (tag2 == '1' || tag2 == '2' || tag2 == '3') {
                            this.connection.setInTranscation(false);
                        }
                    } else if (tag1 == '0' && tag2 != '6' && tag2 <= 'S' || tag1 == '1' && (tag2 == '0' || tag2 == '1') || tag1 == '2' && tag2 != '2' || tag1 == '5' && tag2 == '5' || tag1 == '4' && tag2 == '0' || tag1 == '4' && tag2 == '5' || tag1 == '4' && tag2 == '8' || tag1 == '5' && tag2 == '6' || tag1 == '5' && tag2 == 'D') {
                        if (tag1 == '0' && tag2 != '6' && tag2 <= 'S') {
                            this.update_count = 0;
                        }
                        if (tag1 == '4' && tag2 == '0') {
                            this.update_count = 0;
                        }
                        cmd = ((CompleteResponsePacket)this.bk).getCommand();
                        if (tag1 == '2' && (tag2 == '0' || tag2 == '5')) {
                            updateCountOffset = this.getFirstBlankPosition(cmd, 0);
                            this.update_count = (int)RowidConverter.convertToRowID(cmd, updateCountOffset + 1);
                            if (this.logFlag) {
                                sb = new StringBuffer();
                                sb.append("updatecount: ").append(this.update_count);
                                Driver.writeLog(sb.toString());
                            }
                        }
                        if (tag1 == '2' && tag2 == '1') {
                            updateCountOffset = this.getFirstBlankPosition(cmd, 0);
                            map = RowidConverter.convertToUpdateCount(cmd, updateCountOffset + 1);
                            this.update_count = map.get("updatecount");
                            insertTidOffset = map.get("tidoffset");
                            this.insert_tid = insertTidOffset <= updateCountOffset ? 0L : RowidConverter.convertToRowID(cmd, insertTidOffset + 1);
                            if (this.logFlag) {
                                sb = new StringBuffer();
                                sb.append("updatecount: ").append(this.update_count).append(", rowid: ").append(this.insert_tid);
                                Driver.writeLog(sb.toString());
                            }
                        }
                        if (this.resultset == null) {
                            this.resultset = this.statement.createResultSet(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                            updateCounts[p++] = this.update_count;
                            if (this.resultTid) {
                                this.resultset.setTidValues(this.tidField, this.tidList);
                            }
                            if (tag1 == '1' && tag2 == '0' && this.statement.isCursorUsed()) {
                                this.resultset.setCursorUsed(true);
                            }
                            this.connection.addCursor(this.resultset.getCursorName());
                            this.statement.setResultSet(this.resultset);
                        } else if (this.resultset.isCursorUsed()) {
                            moveSize = 0;
                            commands = command.split(" ");
                            moveSize = Integer.parseInt(commands[1]);
                            if (tag1 == '1' && tag2 == '0') {
                                this.resultset.reInit(this.fields, this.tuples, command, this.update_count, this.insert_tid);
                                if (this.resultTid) {
                                    this.resultset.setTidValues(this.tidField, this.tidList);
                                }
                                this.resultset.setCursorMoveSize(moveSize);
                            } else if (tag1 == '1' && tag2 == '1') {
                                this.resultset.setCursorMoveSize(moveSize);
                            }
                        } else {
                            updateCounts[p++] = this.update_count;
                        }
                    }
                    this.tuples = new ArrayList<E>();
                    this.resultTid = false;
                    this.tidList = null;
                    this.fields = null;
                    this.update_count = -2;
                    this.insert_tid = 0L;
                    continue;
                }
                if (this.bk instanceof ParamInforPacket) {
                    this.getMessage(this.osr_input, this.bk);
                    this.statement.setParamInfor(((ParamInforPacket)this.bk).getParamInfo());
                    continue;
                }
                if (this.bk instanceof EmptyQueryResponsePacket) {
                    this.getMessage(this.osr_input, this.bk);
                    continue;
                }
                if (this.bk instanceof CursorResponsePacket) {
                    this.getMessage(this.osr_input, this.bk);
                    pname = this.connection.getEncoding().decode(((CursorResponsePacket)this.bk).getCursorName());
                    continue;
                }
                if (this.bk instanceof ErrorResponsePacket) {
                    this.status = -1;
                    this.getMessage(this.osr_input, this.bk);
                    errorPacket = (ErrorResponsePacket)this.bk;
                    if (encodingFlag) {
                        if (exception == null) {
                            exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage()));
                            continue;
                        }
                        exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getClientEncoding().decode(errorPacket.getSQLState()), this.connection.getClientEncoding().decode(errorPacket.getErrorMessage())));
                        continue;
                    }
                    if (exception == null) {
                        exception = new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage()));
                        continue;
                    }
                    exception.setNextException(new OSQLException(errorPacket.getErrorCode(), this.connection.getEncoding().decode(errorPacket.getSQLState()), this.connection.getEncoding().decode(errorPacket.getErrorMessage())));
                    continue;
                }
                if (this.bk instanceof NoticeResponsePacket) {
                    this.getMessage(this.osr_input, this.bk);
                    if (encodingFlag) {
                        this.statement.addWarning(this.connection.getClientEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                        continue;
                    }
                    this.statement.addWarning(this.connection.getEncoding().decode(((NoticeResponsePacket)this.bk).getNoticeMessage()), new String(((NoticeResponsePacket)this.bk).getSQLState()));
                    continue;
                }
                if (this.bk instanceof ImportPacket) {
                    ((ImportPacket)this.bk).setEncoding(this.statement.getDBConnection().getEncoding());
                    ((ImportPacket)this.bk).setImportValues(this.statement.getImportValues());
                    this.sendMessage(this.osr_output, this.bk);
                    this.statement.importValues(null);
                    continue;
                }
                if (this.bk instanceof ImportExportResponsePacket) {
                    this.getMessage(this.osr_input, this.bk);
                    this.statement.setTransferRowCount(((ImportExportResponsePacket)this.bk).getAmount());
                    continue;
                }
                if (this.bk instanceof ReadyForQueryPacket) {
                    this.status = 1;
                    this.getMessage(this.osr_input, this.bk);
                    continue;
                }
                this.status = -1;
                throw new OSQLException("OSCAR-00109", "08003", 109);
            }
        } while (!(this.bk instanceof ReadyForQueryPacket));
        if (exception != null) {
            if (this.logFlag) {
                Driver.writeLog("session: " + this.connection.getSessionID() + ", error: " + exception.getMessage());
            }
            throw exception;
        }
    }
}

