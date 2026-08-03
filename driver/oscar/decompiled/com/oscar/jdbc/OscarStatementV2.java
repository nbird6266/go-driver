/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.core.BaseResultSet;
import com.oscar.core.BaseStatement;
import com.oscar.core.Field;
import com.oscar.jdbc.OscarBfile;
import com.oscar.jdbc.OscarBlob;
import com.oscar.jdbc.OscarClob;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.jdbc.OscarLob;
import com.oscar.jdbc.OscarParaMetaData;
import com.oscar.jdbc.OscarPreparedStatementV2;
import com.oscar.jdbc.OscarResultSetMetaData;
import com.oscar.jdbc.OscarResultSetV2;
import com.oscar.jdbc.OscarStatement;
import com.oscar.jdbc.PreparedInfo;
import com.oscar.protocol.OSCARProtocolV2;
import com.oscar.protocol.packets.BatchProcessPacketV2;
import com.oscar.util.BatchDataStream;
import com.oscar.util.OSCARbyte;
import com.oscar.util.OSQLException;
import com.oscar.util.OscarSqlProcessor;
import com.oscar.util.converter.BindConverter;
import com.oscar.util.converter.BooleanConverter;
import com.oscar.util.converter.DateConverter;
import com.oscar.util.converter.IntervalConverter;
import com.oscar.util.converter.NumberConverter;
import com.oscar.util.converter.RowidConverter;
import com.oscar.util.converter.TimestampConverter;
import com.oscar.util.converter.TimestamptzConverter;
import com.oscar.util.converter.TimetzConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OscarStatementV2
extends OscarStatement {
    protected String m_prepareSqlStatement;
    protected int[] m_preBindTypes = new int[0];
    protected BatchProcessPacketV2 batchPacket = null;
    protected boolean netDataByStr = false;
    protected boolean numericKeepPrecision = true;
    protected boolean prepareSimpleExecute = true;
    protected boolean isPrepared = false;
    protected boolean bindTypeBackuped = false;
    protected int lobDisplayMaxSize = -1;
    private static final Pattern PATTERN_PREPARE = Pattern.compile("(?<=PREPARE ).*?(?= AS )");

    public OscarStatementV2(BaseConnection connection) {
        super(connection);
        this.netDataByStr = connection.isNetDataByStr();
        this.numericKeepPrecision = connection.isNumericKeepPrecision();
        this.prepareSimpleExecute = connection.isPrepareSimpleExecute();
        this.batchCount = connection.getBatchCount();
        this.useAsynBatch = connection.isUseAsynBatch();
    }

    public OscarStatementV2(BaseConnection connection, String o_sql) throws SQLException {
        super(connection, o_sql);
        this.netDataByStr = connection.isNetDataByStr();
        this.numericKeepPrecision = connection.isNumericKeepPrecision();
        this.prepareSimpleExecute = connection.isPrepareSimpleExecute();
        this.batchCount = connection.getBatchCount();
        this.useAsynBatch = connection.isUseAsynBatch();
    }

    public OscarStatementV2(OscarJdbc2Connection connection, PreparedInfo pInfo) throws SQLException {
        super(connection, pInfo);
        this.netDataByStr = connection.isNetDataByStr();
        this.numericKeepPrecision = connection.isNumericKeepPrecision();
        this.prepareSimpleExecute = connection.isPrepareSimpleExecute();
        this.batchCount = connection.getBatchCount();
        this.useAsynBatch = connection.isUseAsynBatch();
    }

    protected boolean executeStatement() throws SQLException {
        try {
            this.checkConnClose();
            return this.doExecuteStatement();
        }
        catch (SQLException e) {
            this.checkConnectionClosed(e);
            throw e;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    boolean doExecuteStatement() throws SQLException {
        block48: {
            block51: {
                block52: {
                    block57: {
                        block50: {
                            block49: {
                                block56: {
                                    block54: {
                                        block55: {
                                            if (this.fetchSize > 0 && this.selectSql && (this.resultSetType != 1003 || this.resultSetConcurrency != 1007)) {
                                                if (this.m_prepareSqlStatement != null && !this.isPrepared && !this.isNotRealPrepare()) {
                                                    try {
                                                        this.handleNullStatementName("notImplemented");
                                                        this.connection.execSQL(this.m_prepareSqlStatement, this);
                                                        this.isPrepared = true;
                                                    }
                                                    catch (SQLException sqle) {
                                                        this.m_statementName = null;
                                                        throw sqle;
                                                    }
                                                }
                                                this.convertBindDatas(true);
                                                return super.executeStatement();
                                            }
                                            bindTypeChanged = this.checkBindTypes();
                                            if (!this.bindTypeBackuped) {
                                                if (this.m_preBindTypes.length != this.m_bindTypes.length) {
                                                    this.m_preBindTypes = new int[this.m_bindTypes.length];
                                                }
                                                System.arraycopy(this.m_bindTypes, 0, this.m_preBindTypes, 0, this.m_bindTypes.length);
                                                this.bindTypeBackuped = true;
                                            }
                                            this.clearWarnings();
                                            this.m_statementIsCursor = false;
                                            this.m_cursorName = null;
                                            if (this.result != null) {
                                                this.releaseResources();
                                                this.result.close();
                                                this.result = null;
                                            }
                                            if (this.statementType != 0) break block54;
                                            if (this.fetchSize <= 0 || !this.selectSql || this.isMultiSqlSentens) break block55;
                                            this.m_statementIsCursor = true;
                                            this.result = ((OSCARProtocolV2)this.getDBConnection().getProtocol()).fetchMore(this.m_sqlFragments[0], null, null, null, null, this.fetchSize, this.getMaxRows(), false, false, this, false);
                                            break block48;
                                        }
                                        this.fields = null;
                                        try {
                                            try {
                                                this.result = this.getDBConnection().getQueryExecutor().execute(this.m_sqlFragments, this.m_binds, this);
                                            }
                                            catch (SQLException e) {
                                                this.cursorError = true;
                                                throw e;
                                            }
                                            var4_3 = null;
                                            if ((this.result == null || this.cursorError) && this.m_statementIsCursor) {
                                                this.cursorError = false;
                                            }
                                            break block48;
                                        }
                                        catch (Throwable var3_12) {
                                            var4_4 = null;
                                            if ((this.result == null || this.cursorError) && this.m_statementIsCursor) {
                                                this.cursorError = false;
                                            }
                                            throw var3_12;
                                        }
                                    }
                                    this.m_statementIsCursor = false;
                                    for (i = 0; i < this.m_bindTypes.length; ++i) {
                                        if (this.m_bindTypes[i] != 0) continue;
                                        throw new OSQLException("OSCAR-00405", "88888", 405, i + 1);
                                    }
                                    if (!this.isCallable) break block56;
                                    this.convertBindDatas(false);
                                    this.isPrepared = true;
                                    try {
                                        this.result = ((OSCARProtocolV2)this.connection.getProtocol()).execute(this.m_prepareSqlStatement, this.m_statementName, this.m_bindTypes, this.m_binds, this.getMaxRows(), this, bindTypeChanged);
                                        var6_13 = null;
                                        if (this.m_prepareSqlStatement == null) ** GOTO lbl170
                                        this.m_prepareSqlStatement = null;
                                    }
                                    catch (Throwable var5_15) {
                                        var6_14 = null;
                                        if (this.m_prepareSqlStatement != null) {
                                            this.m_prepareSqlStatement = null;
                                        }
                                        throw var5_15;
                                    }
                                }
                                if (!this.isNotRealPrepare()) break block49;
                                this.convertBindDatas(true);
                                i = (int)super.executeStatement();
                                var8_16 = null;
                                if (this.m_prepareSqlStatement != null) {
                                    this.m_prepareSqlStatement = null;
                                }
                                if ((this.result == null || this.cursorError) && this.m_statementIsCursor) {
                                    this.cursorError = false;
                                }
                                return (boolean)i;
                            }
                            if (this.m_bindTypes == null || this.m_bindTypes.length < 256 || this.connection.getProtocolVersion().getProtocolType() >= 3) break block50;
                            if (!this.isPrepared && this.m_prepareSqlStatement != null) {
                                try {
                                    this.connection.execSQL(this.m_prepareSqlStatement, this);
                                    this.isPrepared = true;
                                }
                                catch (SQLException sqle) {
                                    this.m_statementName = null;
                                    throw sqle;
                                }
                            }
                            this.convertBindDatas(true);
                            sqle = super.executeStatement();
                            var8_17 = null;
                            if (this.m_prepareSqlStatement != null) {
                                this.m_prepareSqlStatement = null;
                            }
                            if ((this.result == null || this.cursorError) && this.m_statementIsCursor) {
                                this.cursorError = false;
                            }
                            return sqle;
                        }
                        if (!this.selectSql || this.fetchSize <= 0) break block51;
                        this.m_statementIsCursor = true;
                        this.convertBindDatas(false);
                        if (!this.prepareSimpleExecute || this.m_binds != null && this.m_binds.length != 0) break block57;
                        l_osql = this.osql.trim().toUpperCase();
                        this.result = l_osql.indexOf("{FN ") != -1 || l_osql.indexOf(" FN ") != -1 ? ((OSCARProtocolV2)this.getDBConnection().getProtocol()).fetchMore(this.m_prepareSqlStatement, this.m_statementName, this.m_bindTypes, this.m_binds, null, this.fetchSize, this.getMaxRows(), false, true, this, bindTypeChanged) : ((OSCARProtocolV2)this.getDBConnection().getProtocol()).fetchMore(this.osql, null, null, null, null, this.fetchSize, this.getMaxRows(), false, false, this, false);
                        ** GOTO lbl129
                    }
                    if (this.m_statementName != null || this.m_binds != null && this.m_binds.length != 0) break block52;
                    this.result = ((OSCARProtocolV2)this.getDBConnection().getProtocol()).fetchMore(this.osql, null, null, null, null, this.fetchSize, this.getMaxRows(), false, false, this, false);
                    l_osql = true;
                    var8_18 = null;
                    if (this.m_prepareSqlStatement != null) {
                        this.m_prepareSqlStatement = null;
                    }
                    if ((this.result == null || this.cursorError) && this.m_statementIsCursor) {
                        this.cursorError = false;
                    }
                    return l_osql;
                }
                this.isPrepared = true;
                this.result = ((OSCARProtocolV2)this.getDBConnection().getProtocol()).fetchMore(this.m_prepareSqlStatement, this.m_statementName, this.m_bindTypes, this.m_binds, null, this.fetchSize, this.getMaxRows(), false, true, this, bindTypeChanged);
lbl129:
                // 2 sources

                l_osql = true;
                var8_19 = null;
                if (this.m_prepareSqlStatement != null) {
                    this.m_prepareSqlStatement = null;
                }
                if ((this.result == null || this.cursorError) && this.m_statementIsCursor) {
                    this.cursorError = false;
                }
                return l_osql;
            }
            try {
                block53: {
                    try {
                        if (this.m_sqlFragments == null && this.fetchSize == 0) {
                            this.setSqlFragments();
                        }
                        this.convertBindDatas(false);
                        if (this.m_sqlFragments != null && this.m_sqlFragments[0].startsWith("EXECUTE")) {
                            if (this.prepareSimpleExecute && (this.m_binds == null || this.m_binds.length == 0)) {
                                this.result = this.getDBConnection().getQueryExecutor().execute(new String[]{this.osql}, this.m_binds, this);
                            } else {
                                this.isPrepared = true;
                                this.result = ((OSCARProtocolV2)this.connection.getProtocol()).execute(this.m_prepareSqlStatement, this.m_statementName, this.m_bindTypes, this.m_binds, this.getMaxRows(), this, bindTypeChanged);
                            }
                            break block53;
                        }
                        this.result = this.getDBConnection().getQueryExecutor().execute(this.m_sqlFragments, this.m_binds, this);
                    }
                    catch (SQLException e) {
                        this.cursorError = true;
                        throw e;
                    }
                }
                var8_20 = null;
                if (this.m_prepareSqlStatement != null) {
                    this.m_prepareSqlStatement = null;
                }
                if ((this.result == null || this.cursorError) && this.m_statementIsCursor) {
                    this.cursorError = false;
                }
            }
            catch (Throwable var7_22) {
                var8_21 = null;
                if (this.m_prepareSqlStatement != null) {
                    this.m_prepareSqlStatement = null;
                }
                if ((this.result == null || this.cursorError) && this.m_statementIsCursor) {
                    this.cursorError = false;
                }
                throw var7_22;
            }
        }
        if (this.isCallable) {
            if (this.result == null) {
                throw new OSQLException("OSCAR-00406", "88888", 406);
            }
            if (this.outParameterIndex.isEmpty() != false ? this.result.getTupleCount() > 1 : this.result.getTupleCount() == 0 || this.result.reallyResultSet() == false) {
                throw new OSQLException("OSCAR-00406", "88888", 406);
            }
            this.getProcedureResult();
            return false;
        }
        if (this.result != null && ((command = this.result.getStatusString()).startsWith("23") || command.startsWith("45") || command.startsWith("5D") || command.startsWith("10") || command.startsWith("48"))) {
            return this.result.reallyResultSet();
        }
        return false;
    }

    private boolean checkBindTypes() {
        if (logFlag) {
            StringBuffer sb = new StringBuffer();
            sb.append("session: ").append(this.connection.getSessionID()).append(", ").append(OscarStatementV2.class);
            sb.append(", checkBindTypes()").append("\n");
            sb.append("preBindTypes: ");
            this.append(sb, this.m_preBindTypes);
            sb.append("\ncurrentBindTypes: ");
            this.append(sb, this.m_bindTypes);
            Driver.writeLog(sb.toString());
        }
        if (this.m_preBindTypes.length == 0 && this.m_bindTypes.length == 0) {
            return false;
        }
        if (this.m_preBindTypes.length != this.m_bindTypes.length) {
            return true;
        }
        for (int i = 0; i < this.m_preBindTypes.length; ++i) {
            if (this.m_preBindTypes[i] == this.m_bindTypes[i]) continue;
            return true;
        }
        return false;
    }

    protected void append(StringBuffer sb, int[] value) {
        if (value == null) {
            sb.append("null");
        } else {
            for (int i = 0; i < value.length; ++i) {
                sb.append(value[i]).append(" ");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void prepare() throws SQLException {
        if (this.ddlSql) {
            this.m_prepareSqlStatement = this.m_sqlFragments[0];
            this.m_executeSqlFragments = new String[this.fragmentsCount];
            this.m_executeSqlFragments[0] = this.m_sqlFragments[0];
        } else {
            this.m_statementName = "J" + this.getNextPreparedCount();
            this.m_origSqlFragments = new String[this.m_sqlFragments.length];
            System.arraycopy(this.m_sqlFragments, 0, this.m_origSqlFragments, 0, this.m_sqlFragments.length);
            this.setSqlFragments();
            StringBuffer stringBuffer = this.sbuf;
            synchronized (stringBuffer) {
                this.sbuf.setLength(0);
                this.sbuf.append("PREPARE ");
                this.sbuf.append(this.m_statementName);
                this.sbuf.append(" AS ");
                this.sbuf.append(this.m_origSqlFragments[0]);
                this.m_prepareSqlStatement = this.sbuf.toString();
            }
        }
        if (this.connection.getVersion().isSendQueryNumForNotRealPrepare() && this.isNotRealPrepare() && !this.ddlSql) {
            this.setPrepareAndNotRealPrepare(true);
        }
        this.setPrepareAndNotRealPrepare(false);
        this.m_sqlFragments = this.m_executeSqlFragments;
    }

    public BaseResultSet createResultSet(Field[] fields, List tuples, String status, int updateCount, long insertOID) throws SQLException {
        return new OscarResultSetV2(this, this.connection, this.netDataByStr, this.numericKeepPrecision, this.resultSetType, this.resultSetConcurrency, this.resultSetCanUpdateable, this.fetchdirection, this.encoding, this.m_cursorName, fields, tuples, status, updateCount, insertOID, this.fetchSize, this.maxrows);
    }

    public void setArray(int i, Array x) throws SQLException {
        this.setString(i, x.toString());
    }

    public void setBlob(int i, Blob x) throws SQLException {
        this.setObject(i, (Object)x, 2004);
    }

    public void setBfile(int i, OscarBfile x) throws SQLException {
        this.setObject(i, (Object)x, -11);
    }

    public void setClob(int i, Clob x) throws SQLException {
        this.setObject(i, (Object)x, 2005);
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        int l_dbType;
        if (this.netDataByStr) {
            this.bind(parameterIndex, null, 24);
            return;
        }
        if (this.m_bindTypes.length >= parameterIndex && (l_dbType = this.m_bindTypes[parameterIndex - 1]) != 0) {
            this.bind(parameterIndex, null, l_dbType);
            return;
        }
        switch (sqlType) {
            case -6: 
            case -5: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 7: 
            case 8: {
                l_dbType = 23;
                break;
            }
            case -7: 
            case 16: {
                l_dbType = 33;
                break;
            }
            case -1: 
            case 1: 
            case 12: {
                l_dbType = 24;
                break;
            }
            case 91: {
                l_dbType = 25;
                break;
            }
            case 92: {
                l_dbType = 26;
                break;
            }
            case 93: {
                l_dbType = 28;
                break;
            }
            case -2: {
                l_dbType = 24;
            }
            case -4: 
            case -3: {
                l_dbType = 24;
                break;
            }
            case 1111: {
                l_dbType = 24;
                break;
            }
            case 2005: {
                l_dbType = 51;
                break;
            }
            case 2004: {
                l_dbType = 50;
                break;
            }
            case 29: {
                l_dbType = 29;
                break;
            }
            default: {
                l_dbType = 24;
            }
        }
        this.bind(parameterIndex, null, l_dbType);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        if (this.isAnonymous) {
            super.setBoolean(parameterIndex, x);
            return;
        }
        if (x) {
            this.setString(parameterIndex, "1");
        } else {
            this.setString(parameterIndex, "0");
        }
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        this.setString(parameterIndex, String.valueOf(x));
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        if (this.netDataByStr) {
            this.bind(parameterIndex, new Short(x), 24);
            return;
        }
        this.bind(parameterIndex, new Short(x), 23);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        if ((long)x > Long.MAX_VALUE || (long)x < Long.MIN_VALUE) {
            throw new OSQLException("OSCAR-00703", "88888", 703);
        }
        if (this.netDataByStr) {
            this.bind(parameterIndex, new Integer(x), 24);
        } else {
            this.bind(parameterIndex, new Integer(x), 23);
        }
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        if (x > Long.MAX_VALUE || x < Long.MIN_VALUE) {
            throw new OSQLException("OSCAR-00704", "88888", 704);
        }
        if (this.netDataByStr) {
            this.bind(parameterIndex, new Long(x), 24);
        } else {
            this.bind(parameterIndex, new Long(x), 23);
        }
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        if (this.netDataByStr) {
            this.bind(parameterIndex, new Float(x), 24);
        } else if (this.numericKeepPrecision) {
            this.bind(parameterIndex, new Float(x), 24);
        } else {
            this.bind(parameterIndex, new Float(x), 23);
        }
    }

    public void setEmptyString(int parameterIndex) throws SQLException {
        if (this.isNotRealPrepare()) {
            this.bind(parameterIndex, "", 24);
        } else {
            this.bind(parameterIndex, Character.valueOf('\u0000'), 24);
        }
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        if (this.netDataByStr) {
            this.bind(parameterIndex, new Double(x), 24);
        } else if (this.numericKeepPrecision) {
            this.bind(parameterIndex, new Double(x), 24);
        } else {
            this.bind(parameterIndex, new Double(x), 23);
        }
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        if (this.netDataByStr) {
            this.bind(parameterIndex, x, 24);
        } else if (this.numericKeepPrecision) {
            this.bind(parameterIndex, x, 24);
        } else if (x == null) {
            this.setNull(parameterIndex, 2);
        } else {
            this.bind(parameterIndex, new Double(x.doubleValue()), 23);
        }
    }

    public void setString(int i, String x) throws SQLException {
        if (this.isAnonymous) {
            super.setString(i, x);
        } else if (x == null) {
            this.setNull(i, 12);
        } else if (x.toString().length() == 0) {
            if (this.isNotRealPrepare()) {
                this.setString(i, "", 24);
            } else {
                this.setString(i, String.valueOf('\u0000'), 24);
            }
        } else {
            this.setString(i, x, 24);
        }
    }

    public void setString(int parameterIndex, String x, int type) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 12);
        } else {
            this.bind(parameterIndex, x, type);
        }
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 91);
        } else if (this.netDataByStr) {
            this.setString(parameterIndex, x.toString());
        } else {
            this.bind(parameterIndex, x, 25);
        }
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        if (x == null) {
            this.setNull(1, 92);
        } else if (this.netDataByStr) {
            this.setString(parameterIndex, x.toString());
        } else {
            this.bind(parameterIndex, x, 26);
        }
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 93);
        } else if (this.netDataByStr) {
            this.setString(parameterIndex, x.toString());
        } else {
            this.bind(parameterIndex, x, 28);
        }
    }

    public void setDate(int i, Date d, Calendar cal) throws SQLException {
        if (d == null) {
            this.setNull(i, 91);
        } else {
            if (cal != null) {
                long user = cal.get(15) + cal.get(16);
                long system = this.systemCal.get(15) + this.systemCal.get(16);
                d = new Date(d.getTime() + (system - user));
            }
            if (this.netDataByStr) {
                this.setString(i, d.toString());
            } else {
                this.bind(i, d, 25);
            }
        }
    }

    public void setTime(int i, Time t, Calendar cal) throws SQLException {
        if (t == null) {
            this.setNull(i, 92);
        } else {
            if (cal != null) {
                long user = cal.get(15) + cal.get(16);
                long system = this.systemCal.get(15) + this.systemCal.get(16);
                t = new Time(t.getTime() + (system - user));
            }
            if (this.netDataByStr) {
                this.setString(i, t.toString());
            } else {
                this.bind(i, t, 26);
            }
        }
    }

    public void setTimestamp(int i, Timestamp t, Calendar cal) throws SQLException {
        if (t == null) {
            this.setNull(i, 29);
        } else {
            if (cal != null) {
                long user = cal.get(15) + cal.get(16);
                long system = this.systemCal.get(15) + this.systemCal.get(16);
                t = new Timestamp(t.getTime() - (system - user));
            }
            if (this.netDataByStr) {
                this.setString(i, t.toString());
            } else {
                this.bind(i, t, 29);
            }
        }
    }

    protected void bind(int paramIndex, Object s, int type) throws SQLException {
        if (paramIndex < 1 || paramIndex > this.m_binds.length) {
            throw new OSQLException("OSCAR-00305", "88888", 305);
        }
        if (paramIndex == 1 && this.isResultNeeded) {
            throw new OSQLException("OSCAR-00413", "88888", 413);
        }
        StringBuffer sb = null;
        if (logFlag) {
            sb = new StringBuffer("session: " + this.connection.getSessionID() + ", " + OscarStatementV2.class + ", bind(int paramIndex, Object s, int type) ,param: " + paramIndex + ", type: " + type);
        }
        this.m_binds[paramIndex - 1] = s;
        this.m_binds_old[paramIndex - 1] = s;
        int pre_bindType = this.m_bindTypes[paramIndex - 1];
        if (pre_bindType != 0) {
            if (24 == type && 23 == pre_bindType) {
                this.updateBatch(paramIndex);
                this.m_bindTypes[paramIndex - 1] = type;
            } else {
                this.m_bindTypes[paramIndex - 1] = 24 == pre_bindType && 23 == type ? pre_bindType : type;
            }
        } else {
            this.m_bindTypes[paramIndex - 1] = type;
        }
        if (logFlag) {
            sb.append(", pre type: " + pre_bindType);
            if (s == null) {
                sb.append(",data: null");
            } else {
                sb.append(", data: " + s.toString());
            }
            Driver.writeLog(sb.toString());
        }
    }

    public void addBatch() throws SQLException {
        Object[] m_binds_backup = new Object[this.m_binds.length];
        System.arraycopy(this.m_binds, 0, m_binds_backup, 0, this.m_binds.length);
        int[] l_newBindTypes = new int[this.m_bindTypes.length];
        System.arraycopy(this.m_bindTypes, 0, l_newBindTypes, 0, this.m_bindTypes.length);
        if (this.useAsynBatch) {
            this.convertBindDatas(false);
            Object[] l_newBinds = new Object[this.m_binds.length];
            System.arraycopy(this.m_binds, 0, l_newBinds, 0, this.m_binds.length);
            OscarStatement.BatchRowData data = new OscarStatement.BatchRowData(l_newBinds, l_newBindTypes, this.hasEscapeChar);
            this.m_binds = m_binds_backup;
            m_binds_backup = null;
            if (!this.isBegin) {
                this.isBegin = true;
                if (this.batchDataStream == null) {
                    this.batchDataStream = new BatchDataStream(this);
                } else {
                    this.batchDataStream.reInit();
                }
            }
            this.batchDataStream.addBatchData(data);
        } else {
            OscarStatement.BatchRowData data = new OscarStatement.BatchRowData(m_binds_backup, l_newBindTypes, this.hasEscapeChar);
            if (this.batch == null) {
                this.batch = new LinkedList();
            }
            this.batch.add(data);
            this.hasEscapeChar = false;
            if (this.addBatchUseSql) {
                this.addBatchUseSql = false;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public int[] executeBatch() throws SQLException {
        int[] resultSize;
        block45: {
            if (logFlag) {
                Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarStatementV2.class + ", executeBatch()");
            }
            this.checkClosed();
            this.checkConnClose();
            if (this.useAsynBatch) {
                if (this.batchDataStream != null) {
                    this.batchDataStream.finshed();
                }
                this.isBegin = false;
                return null;
            }
            if (this.batch == null) {
                return new int[0];
            }
            if (this.m_bindTypes != null) {
                int bindLength = this.m_bindTypes.length;
                int protocolType = this.connection.getProtocolVersion().getProtocolType();
                if (bindLength >= 256 && protocolType < 4) {
                    throw new OSQLException("OSCAR-00430", "88888", 430);
                }
                if (this.m_bindTypes.length > Short.MAX_VALUE && protocolType >= 4) {
                    throw new OSQLException("OSCAR-00434", "88888", 430);
                }
            }
            int size = this.batch.size();
            if (this.statementType == 2 && !this.outParameterType.isEmpty()) {
                throw new BatchUpdateException("oscar.call.outparam", "HY065", 1, new int[0]);
            }
            if (this.result != null) {
                this.result.close();
                this.result = null;
            }
            this.updateBatchCount = 0;
            resultSize = new int[size];
            int i = 0;
            try {
                try {
                    if (size > 0) {
                        Object o = this.batch.get(0);
                        if (this.addBatchUseSql) {
                            String[] l_origSqlFragments = this.m_sqlFragments;
                            Object[] l_origBinds = this.m_binds;
                            for (Object[] l_statement : this.batch) {
                                this.m_sqlFragments = (String[])l_statement[0];
                                this.m_binds = (Object[])l_statement[1];
                                if (this.m_sqlFragments != null && this.m_sqlFragments.length == 1) {
                                    OscarSqlProcessor.ParseResult parseResult = OscarSqlProcessor.parsing(this.m_sqlFragments[0]);
                                    this.selectSql = parseResult.isSelectSql();
                                    this.insertSql = parseResult.isInsertSql();
                                }
                                resultSize[i] = this.executeUpdate();
                                ++i;
                            }
                            this.m_sqlFragments = l_origSqlFragments;
                            this.m_binds = l_origBinds;
                        } else {
                            if (this.m_bindTypes.length == 0) {
                                this.execute();
                                resultSize[0] = this.getUpdateCount();
                            } else {
                                int marked = 0;
                                if (this.isDDLSql()) {
                                    marked = 3;
                                    this.resetDDLSql(false);
                                } else if (this.isPrepareAndNotRealPrepare()) {
                                    marked = 4;
                                } else if (this.getAutoGeneratedInfo() != -1) {
                                    String sql;
                                    marked = 2;
                                    if (this.m_prepareSqlStatement != null && !(sql = this.generatedKeySqlTransform(this.m_prepareSqlStatement)).equals(this.m_prepareSqlStatement)) {
                                        this.m_prepareSqlStatement = sql;
                                        marked = 0;
                                    }
                                } else if (this.useTid()) {
                                    marked = 1;
                                }
                                boolean bindTypeChanged = this.checkBindTypes();
                                if (!this.bindTypeBackuped) {
                                    if (this.m_preBindTypes.length != this.m_bindTypes.length) {
                                        this.m_preBindTypes = new int[this.m_bindTypes.length];
                                    }
                                    System.arraycopy(this.m_bindTypes, 0, this.m_preBindTypes, 0, this.m_bindTypes.length);
                                    this.bindTypeBackuped = true;
                                }
                                if (this.batchPacket != null) {
                                    this.batchPacket.init(this.batch, this.m_statementName, this.connection.getEncoding(), this.connection.getBatchBufferSize(), this.m_bindTypes, this.m_prepareSqlStatement, marked, bindTypeChanged);
                                } else {
                                    this.batchPacket = new BatchProcessPacketV2(this.batch, this.m_statementName, this.connection.getEncoding(), this.connection.getBatchBufferSize(), this.m_bindTypes, this.m_prepareSqlStatement, marked, bindTypeChanged, this.connection);
                                }
                                this.getDBConnection().getQueryExecutor().executeBatch(this.batchPacket, (BaseStatement)this, resultSize);
                            }
                            if (this.result != null && this.result.reallyResultSet() && !this.isCallable && this.autoGeneratedInfo == -1) {
                                throw new OSQLException("OSCAR-00403", "88888", 403);
                            }
                        }
                    }
                    Object var11_17 = null;
                    if (this.m_prepareSqlStatement != null) {
                        this.m_prepareSqlStatement = null;
                    }
                    if (this.batch != null) {
                        this.batch.clear();
                    }
                }
                catch (SQLException e) {
                    int[] resultSucceeded = null;
                    if (this.addBatchUseSql) {
                        resultSucceeded = new int[i];
                        System.arraycopy(resultSize, 0, resultSucceeded, 0, i);
                    } else {
                        resultSucceeded = new int[this.updateBatchCount];
                        System.arraycopy(resultSize, 0, resultSucceeded, 0, this.updateBatchCount);
                    }
                    if (logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", " + e.getMessage());
                    }
                    this.checkConnectionClosed(e);
                    throw new BatchUpdateException(e.getMessage(), e.getSQLState(), e.getErrorCode(), resultSucceeded);
                }
                catch (Exception e) {
                    if (logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", " + e.getMessage());
                    }
                    Object var11_18 = null;
                    if (this.m_prepareSqlStatement != null) {
                        this.m_prepareSqlStatement = null;
                    }
                    if (this.batch != null) {
                        this.batch.clear();
                    }
                    this.cleanEnvTemp();
                    break block45;
                }
            }
            catch (Throwable throwable) {
                Object var11_19 = null;
                if (this.m_prepareSqlStatement != null) {
                    this.m_prepareSqlStatement = null;
                }
                if (this.batch != null) {
                    this.batch.clear();
                }
                this.cleanEnvTemp();
                throw throwable;
            }
            this.cleanEnvTemp();
        }
        this.addBatchUseSql = false;
        this.warnings = null;
        return resultSize;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void executeBatchAsyn(List asynBatch) throws SQLException {
        block31: {
            if (logFlag) {
                Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarStatementV2.class + ", executeBatchAsyn(List asynBatch)");
            }
            this.checkClosed();
            if (asynBatch == null) {
                return;
            }
            int size = asynBatch.size();
            if (this.statementType == 2 && !this.outParameterType.isEmpty()) {
                throw new BatchUpdateException("oscar.call.outparam", "HY065", 1, new int[0]);
            }
            int[] resultSize = new int[size];
            try {
                try {
                    if (size > 0) {
                        int marked = 0;
                        if (this.isDDLSql()) {
                            marked = 3;
                            this.resetDDLSql(false);
                        } else if (this.isPrepareAndNotRealPrepare()) {
                            marked = 4;
                        } else if (this.getAutoGeneratedInfo() != -1) {
                            String sql;
                            marked = 2;
                            if (this.m_prepareSqlStatement != null && !(sql = this.generatedKeySqlTransform(this.m_prepareSqlStatement)).equals(this.m_prepareSqlStatement)) {
                                this.m_prepareSqlStatement = sql;
                                marked = 0;
                            }
                        } else if (this.useTid()) {
                            marked = 1;
                        }
                        boolean bindTypeChanged = this.checkBindTypes();
                        if (!this.bindTypeBackuped) {
                            if (this.m_preBindTypes.length != this.m_bindTypes.length) {
                                this.m_preBindTypes = new int[this.m_bindTypes.length];
                            }
                            System.arraycopy(this.m_bindTypes, 0, this.m_preBindTypes, 0, this.m_bindTypes.length);
                            this.bindTypeBackuped = true;
                        }
                        if (this.batchPacket != null) {
                            this.batchPacket.init(asynBatch, this.m_statementName, this.connection.getEncoding(), this.connection.getBatchBufferSize(), this.m_bindTypes, this.m_prepareSqlStatement, marked, bindTypeChanged);
                        } else {
                            this.batchPacket = new BatchProcessPacketV2(asynBatch, this.m_statementName, this.connection.getEncoding(), this.connection.getBatchBufferSize(), this.m_bindTypes, this.m_prepareSqlStatement, marked, bindTypeChanged, this.connection);
                        }
                        this.getDBConnection().getQueryExecutor().executeBatch(this.batchPacket, (BaseStatement)this, resultSize);
                        if (this.result != null && this.result.reallyResultSet() && !this.isCallable && this.autoGeneratedInfo == -1) {
                            throw new OSQLException("OSCAR-00403", "88888", 403);
                        }
                    }
                    Object var7_10 = null;
                    if (this.m_prepareSqlStatement != null) {
                        this.m_prepareSqlStatement = null;
                    }
                    if (asynBatch != null) {
                        asynBatch.clear();
                    }
                }
                catch (SQLException e) {
                    int[] resultSucceeded = null;
                    resultSucceeded = new int[size];
                    if (logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", " + e.getMessage());
                    }
                    this.checkConnectionClosed(e);
                    throw new BatchUpdateException(e.getMessage(), e.getSQLState(), e.getErrorCode(), resultSucceeded);
                }
                catch (Exception e) {
                    if (logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", " + e.getMessage());
                    }
                    Object var7_11 = null;
                    if (this.m_prepareSqlStatement != null) {
                        this.m_prepareSqlStatement = null;
                    }
                    if (asynBatch != null) {
                        asynBatch.clear();
                    }
                    this.cleanEnvTemp();
                    break block31;
                }
            }
            catch (Throwable throwable) {
                Object var7_12 = null;
                if (this.m_prepareSqlStatement != null) {
                    this.m_prepareSqlStatement = null;
                }
                if (asynBatch != null) {
                    asynBatch.clear();
                }
                this.cleanEnvTemp();
                throw throwable;
            }
            this.cleanEnvTemp();
        }
        this.result = null;
        this.warnings = null;
    }

    public Field[] getFields() {
        return this.fields;
    }

    public void setFields(Field[] fields) {
        this.fields = fields;
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        if (this.pMetaData == null) {
            if (this.fragmentsCount > 1) {
                if (this.m_prepareSqlStatement != null && !this.isPrepared) {
                    try {
                        this.connection.execSQL(this.m_prepareSqlStatement, this);
                        this.isPrepared = true;
                    }
                    catch (SQLException sqle) {
                        this.m_statementName = null;
                        throw sqle;
                    }
                }
                this.handleNullStatementName("OSCAR-00421");
                String getParamSQL = "GET PARAMINFO FOR " + this.m_statementName;
                this.connection.execSQL(getParamSQL);
                OscarParaMetaData pMetaDataTemp = (OscarParaMetaData)this.connection.getDefaultStatement().getParamInfo();
                this.pMetaData = new OscarParaMetaData(pMetaDataTemp.getParamInfor(), this);
                if (this instanceof CallableStatement && this.isResultNeeded) {
                    OscarResultSetMetaData rsmd = (OscarResultSetMetaData)this.getMetaData();
                    if (rsmd == null || rsmd.getColumnCount() == 0) {
                        throw new OSQLException("OSCAR-00401", "88888", 401);
                    }
                    Field f = rsmd.getField(1);
                    Object[] returnParam = null;
                    returnParam = this.connection.getVersion().isNewParamInfoPacket() ? new Object[6] : new Object[8];
                    returnParam[0] = this.encoding.encode(f.getAliasName());
                    returnParam[1] = new Integer(f.getOID());
                    returnParam[2] = new Short((short)f.getLength());
                    returnParam[3] = new Integer(f.getMod());
                    returnParam[4] = new Byte((byte)f.isNullable());
                    returnParam[5] = new Byte(2);
                    this.pMetaData.addReturnParam(returnParam);
                }
            } else {
                this.pMetaData = new OscarParaMetaData(new Object[0][0], this);
            }
        }
        return this.pMetaData;
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        if (this.m_prepareSqlStatement != null && !this.isPrepared) {
            try {
                this.connection.execSQL(this.m_prepareSqlStatement, this);
                this.isPrepared = true;
            }
            catch (SQLException sqle) {
                this.m_statementName = null;
                throw sqle;
            }
        }
        ResultSet rs = this.getResultSet();
        String sql = "GET ROWDESCRIPTION FOR " + this.m_statementName;
        BaseResultSet brs = this.connection.execSQL(sql);
        if (brs == null) {
            return null;
        }
        if (brs != null && !brs.reallyResultSet()) {
            return null;
        }
        return brs.getMetaData();
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, targetSqlType);
            return;
        }
        switch (targetSqlType) {
            case 4: {
                if (x instanceof Integer) {
                    this.setInt(parameterIndex, (int)((Integer)x));
                    break;
                }
                if (x instanceof Short) {
                    this.setInt(parameterIndex, ((Short)x).intValue());
                    break;
                }
                if (x instanceof Byte) {
                    this.setInt(parameterIndex, ((Byte)x).intValue());
                    break;
                }
                if (x instanceof Float) {
                    if (((Float)x).floatValue() > 2.14748365E9f || ((Float)x).floatValue() < -2.14748365E9f) {
                        throw new OSQLException("OSCAR-00703", "88888", 703);
                    }
                    this.addWarning(new DataTruncation(parameterIndex, true, false, 4, 4));
                    this.setInt(parameterIndex, ((Float)x).intValue());
                    break;
                }
                if (x instanceof Double) {
                    if ((Double)x > 2.147483647E9 || (Double)x < -2.147483648E9) {
                        throw new OSQLException("OSCAR-00703", "88888", 703);
                    }
                    this.addWarning(new DataTruncation(parameterIndex, true, false, 8, 4));
                    this.setInt(parameterIndex, ((Double)x).intValue());
                    break;
                }
                if (x instanceof Long) {
                    if ((Long)x > Integer.MAX_VALUE || (Long)x < Integer.MIN_VALUE) {
                        throw new OSQLException("OSCAR-00703", "88888", 703);
                    }
                    this.setInt(parameterIndex, ((Long)x).intValue());
                    break;
                }
                if (x instanceof Boolean) {
                    this.setInt(parameterIndex, (Boolean)x != false ? 1 : 0);
                    break;
                }
                if (x instanceof BigDecimal) {
                    BigDecimal decimal = (BigDecimal)x;
                    if (decimal.doubleValue() > 2.147483647E9 || decimal.doubleValue() < -2.147483648E9) {
                        throw new OSQLException("OSCAR-00703", "88888", 703);
                    }
                    this.addWarning(new DataTruncation(parameterIndex, true, false, -1, 4));
                    this.setInt(parameterIndex, (int)decimal.doubleValue());
                    break;
                }
                if (x instanceof String) {
                    if (x.toString().trim().equals("")) {
                        this.setEmptyString(parameterIndex);
                        break;
                    }
                    double d = 0.0;
                    try {
                        d = Double.parseDouble((String)x);
                    }
                    catch (Exception e) {
                        int i;
                        StringBuffer sf = new StringBuffer((String)x);
                        for (i = 0; i < sf.length() && (Character.isDigit(sf.charAt(i)) || i == 0 && (sf.charAt(i) == '-' || sf.charAt(i) == '+')); ++i) {
                        }
                        String nx = sf.substring(0, i);
                        try {
                            d = Double.parseDouble(nx);
                        }
                        catch (Exception ex) {
                            d = 0.0;
                        }
                    }
                    if (d > 2.147483647E9 || d < -2.147483648E9) {
                        throw new OSQLException("OSCAR-00703", "88888", 703);
                    }
                    this.setInt(parameterIndex, (int)d);
                    break;
                }
                if (x instanceof Character) {
                    try {
                        this.setInt(parameterIndex, Integer.parseInt(x.toString()));
                        break;
                    }
                    catch (Exception e) {
                        throw new OSQLException("OSCAR-00703", "88888", 703, e);
                    }
                }
                throw new OSQLException("OSCAR-00703", "88888", 703);
            }
            case -5: {
                if (x instanceof Long) {
                    this.setLong(parameterIndex, (long)((Long)x));
                    break;
                }
                if (x instanceof Integer) {
                    this.setLong(parameterIndex, ((Integer)x).longValue());
                    break;
                }
                if (x instanceof Short) {
                    this.setLong(parameterIndex, ((Short)x).longValue());
                    break;
                }
                if (x instanceof Byte) {
                    this.setLong(parameterIndex, ((Byte)x).longValue());
                    break;
                }
                if (x instanceof Double) {
                    if (((Double)x).longValue() >= Long.MAX_VALUE || ((Double)x).longValue() <= Long.MIN_VALUE) {
                        throw new OSQLException("OSCAR-00704", "88888", 704);
                    }
                    this.addWarning(new DataTruncation(parameterIndex, true, false, 8, 8));
                    this.setLong(parameterIndex, ((Double)x).longValue());
                    break;
                }
                if (x instanceof Float) {
                    if (((Float)x).floatValue() > 9.223372E18f || ((Float)x).floatValue() < -9.223372E18f) {
                        throw new OSQLException("OSCAR-00704", "88888", 704);
                    }
                    this.addWarning(new DataTruncation(parameterIndex, true, false, 4, 8));
                    this.setLong(parameterIndex, ((Float)x).longValue());
                    break;
                }
                if (x instanceof BigDecimal) {
                    BigDecimal decimal = (BigDecimal)x;
                    if (decimal.compareTo(new BigDecimal(Long.MAX_VALUE)) == 1 || decimal.compareTo(new BigDecimal(Long.MIN_VALUE)) == -1) {
                        throw new OSQLException("OSCAR-00704", "88888", 704);
                    }
                    this.addWarning(new DataTruncation(parameterIndex, true, false, -1, 8));
                    this.setLong(parameterIndex, decimal.longValue());
                    break;
                }
                if (x instanceof Boolean) {
                    this.setLong(parameterIndex, (Boolean)x != false ? 1L : 0L);
                    break;
                }
                if (x instanceof String) {
                    if (x.toString().trim().equals("")) {
                        this.setEmptyString(parameterIndex);
                        break;
                    }
                    BigDecimal decimal = new BigDecimal((String)x);
                    if (decimal.compareTo(new BigDecimal(Long.MAX_VALUE)) == 1 || decimal.compareTo(new BigDecimal(Long.MIN_VALUE)) == -1) {
                        throw new OSQLException("OSCAR-00704", "88888", 704);
                    }
                    this.setLong(parameterIndex, decimal.longValue());
                    break;
                }
                if (x instanceof Character) {
                    try {
                        this.setLong(parameterIndex, Long.parseLong(x.toString()));
                        break;
                    }
                    catch (Exception e) {
                        throw new OSQLException("OSCAR-00704", "88888", 704, e);
                    }
                }
                throw new OSQLException("OSCAR-00704", "88888", 704);
            }
            case -6: {
                if (x instanceof Integer) {
                    if ((Integer)x > 127 || (Integer)x < -128) {
                        throw new OSQLException("OSCAR-00701", "88888", 701);
                    }
                    this.setInt(parameterIndex, (int)((Integer)x));
                    break;
                }
                if (x instanceof Byte) {
                    this.setInt(parameterIndex, ((Byte)x).intValue());
                    break;
                }
                if (x instanceof Short) {
                    if (((Short)x).intValue() > 127 || ((Short)x).intValue() < -128) {
                        throw new OSQLException("OSCAR-00701", "88888", 701);
                    }
                    this.setInt(parameterIndex, ((Short)x).intValue());
                    break;
                }
                if (x instanceof Float) {
                    if (((Float)x).floatValue() > 127.0f || ((Float)x).floatValue() < -128.0f) {
                        throw new OSQLException("OSCAR-00701", "88888", 701);
                    }
                    this.addWarning(new DataTruncation(parameterIndex, true, false, 4, 1));
                    this.setInt(parameterIndex, ((Float)x).intValue());
                    break;
                }
                if (x instanceof Double) {
                    if ((Double)x > 127.0 || (Double)x < -128.0) {
                        throw new OSQLException("OSCAR-00701", "88888", 701);
                    }
                    this.addWarning(new DataTruncation(parameterIndex, true, false, 8, 1));
                    this.setInt(parameterIndex, ((Double)x).intValue());
                    break;
                }
                if (x instanceof Long) {
                    if ((Long)x > 127L || (Long)x < -128L) {
                        throw new OSQLException("OSCAR-00701", "88888", 701);
                    }
                    this.setInt(parameterIndex, ((Long)x).intValue());
                    break;
                }
                if (x instanceof Boolean) {
                    this.setInt(parameterIndex, (Boolean)x != false ? 1 : 0);
                    break;
                }
                if (x instanceof BigDecimal) {
                    BigDecimal decimal = (BigDecimal)x;
                    if (decimal.doubleValue() > 127.0 || decimal.doubleValue() < -128.0) {
                        throw new OSQLException("OSCAR-00701", "88888", 701);
                    }
                    this.addWarning(new DataTruncation(parameterIndex, true, false, -1, 1));
                    this.setInt(parameterIndex, (int)decimal.doubleValue());
                    break;
                }
                if (x instanceof String) {
                    double d;
                    if (x.toString().trim().equals("")) {
                        this.setEmptyString(parameterIndex);
                        break;
                    }
                    try {
                        d = Double.parseDouble((String)x);
                    }
                    catch (Exception e) {
                        throw new OSQLException("OSCAR-00701", "88888", 701);
                    }
                    if (d > 127.0 || d < -128.0) {
                        throw new OSQLException("OSCAR-00701", "88888", 701);
                    }
                    this.setInt(parameterIndex, (int)d);
                    break;
                }
                if (x instanceof Character) {
                    try {
                        this.setInt(parameterIndex, Integer.parseInt(x.toString()));
                        break;
                    }
                    catch (Exception e) {
                        throw new OSQLException("OSCAR-00701", "88888", 701, e);
                    }
                }
                throw new OSQLException("OSCAR-00701", "88888", 701);
            }
            case 5: {
                if (x instanceof Integer) {
                    if ((Integer)x > Short.MAX_VALUE || (Integer)x < Short.MIN_VALUE) {
                        throw new OSQLException("OSCAR-00702", "88888", 702);
                    }
                    this.setInt(parameterIndex, (int)((Integer)x));
                    break;
                }
                if (x instanceof Byte) {
                    this.setInt(parameterIndex, ((Byte)x).intValue());
                    break;
                }
                if (x instanceof Short) {
                    this.setInt(parameterIndex, ((Short)x).intValue());
                    break;
                }
                if (x instanceof Float) {
                    if (((Float)x).floatValue() > 32767.0f || ((Float)x).floatValue() < -32768.0f) {
                        throw new OSQLException("OSCAR-00702", "88888", 702);
                    }
                    this.addWarning(new DataTruncation(parameterIndex, true, false, 4, 2));
                    this.setInt(parameterIndex, ((Float)x).intValue());
                    break;
                }
                if (x instanceof Double) {
                    if ((Double)x > 32767.0 || (Double)x < -32768.0) {
                        throw new OSQLException("OSCAR-00702", "88888", 702);
                    }
                    this.addWarning(new DataTruncation(parameterIndex, true, false, 8, 2));
                    this.setInt(parameterIndex, ((Double)x).intValue());
                    break;
                }
                if (x instanceof Long) {
                    if ((Long)x > 32767L || (Long)x < -32768L) {
                        throw new OSQLException("OSCAR-00702", "88888", 702);
                    }
                    this.setInt(parameterIndex, ((Long)x).intValue());
                    break;
                }
                if (x instanceof Boolean) {
                    this.setInt(parameterIndex, (Boolean)x != false ? 1 : 0);
                    break;
                }
                if (x instanceof BigDecimal) {
                    BigDecimal decimal = (BigDecimal)x;
                    if (decimal.doubleValue() > 32767.0 || decimal.doubleValue() < -32768.0) {
                        throw new OSQLException("OSCAR-00702", "88888", 702);
                    }
                    this.addWarning(new DataTruncation(parameterIndex, true, false, -1, 2));
                    this.setInt(parameterIndex, (int)decimal.doubleValue());
                    break;
                }
                if (x instanceof String) {
                    double d;
                    if (x.toString().trim().equals("")) {
                        this.setEmptyString(parameterIndex);
                        break;
                    }
                    try {
                        d = Double.parseDouble((String)x);
                    }
                    catch (Exception e) {
                        throw new OSQLException("OSCAR-00702", "88888", 702, e);
                    }
                    if (d > 32767.0 || d < -32768.0) {
                        throw new OSQLException("OSCAR-00702", "88888", 702);
                    }
                    this.setInt(parameterIndex, (int)d);
                    break;
                }
                if (x instanceof Character) {
                    try {
                        this.setInt(parameterIndex, Integer.parseInt(x.toString()));
                        break;
                    }
                    catch (Exception e) {
                        throw new OSQLException("OSCAR-00702", "88888", 702, e);
                    }
                }
                throw new OSQLException("OSCAR-00702", "88888", 702);
            }
            case 7: {
                if (x instanceof Integer) {
                    this.setDouble(parameterIndex, ((Integer)x).doubleValue());
                    break;
                }
                if (x instanceof Byte) {
                    this.setDouble(parameterIndex, ((Byte)x).doubleValue());
                    break;
                }
                if (x instanceof Short) {
                    this.setDouble(parameterIndex, ((Short)x).doubleValue());
                    break;
                }
                if (x instanceof Long) {
                    this.setDouble(parameterIndex, ((Long)x).doubleValue());
                    break;
                }
                if (x instanceof Float) {
                    this.setDouble(parameterIndex, ((Float)x).doubleValue());
                    break;
                }
                if (x instanceof Double) {
                    if ((Double)x > 3.4028234663852886E38 || (Double)x < -3.4028234663852886E38) {
                        throw new OSQLException("OSCAR-00705", "88888", 705);
                    }
                    this.setDouble(parameterIndex, (double)((Double)x));
                    break;
                }
                if (x instanceof Boolean) {
                    this.setDouble(parameterIndex, (Boolean)x != false ? 1.0 : 0.0);
                    break;
                }
                if (x instanceof BigDecimal) {
                    BigDecimal decimal = (BigDecimal)x;
                    if (decimal.doubleValue() > 3.4028234663852886E38 || decimal.doubleValue() < -3.4028234663852886E38) {
                        throw new OSQLException("OSCAR-00705", "88888", 705);
                    }
                    this.setDouble(parameterIndex, decimal.doubleValue());
                    break;
                }
                if (x instanceof String) {
                    double d;
                    if (x.toString().trim().equals("")) {
                        this.setEmptyString(parameterIndex);
                        break;
                    }
                    try {
                        d = Double.parseDouble((String)x);
                    }
                    catch (Exception e) {
                        throw new OSQLException("OSCAR-00705", "88888", 705, e);
                    }
                    if (d > 3.4028234663852886E38 || d < -3.4028234663852886E38) {
                        throw new OSQLException("OSCAR-00705", "88888", 705);
                    }
                    this.setDouble(parameterIndex, d);
                    break;
                }
                if (x instanceof Character) {
                    try {
                        this.setDouble(parameterIndex, Double.parseDouble(x.toString()));
                        break;
                    }
                    catch (Exception e) {
                        throw new OSQLException("OSCAR-00705", "88888", 705, e);
                    }
                }
                throw new OSQLException("OSCAR-00705", "88888", 705);
            }
            case 6: 
            case 8: {
                if (x instanceof Integer) {
                    this.setDouble(parameterIndex, ((Integer)x).doubleValue());
                    break;
                }
                if (x instanceof Byte) {
                    this.setDouble(parameterIndex, ((Byte)x).doubleValue());
                    break;
                }
                if (x instanceof Short) {
                    this.setDouble(parameterIndex, ((Short)x).doubleValue());
                    break;
                }
                if (x instanceof Long) {
                    this.setDouble(parameterIndex, ((Long)x).doubleValue());
                    break;
                }
                if (x instanceof Float) {
                    this.setDouble(parameterIndex, ((Float)x).doubleValue());
                    break;
                }
                if (x instanceof Double) {
                    this.setDouble(parameterIndex, (double)((Double)x));
                    break;
                }
                if (x instanceof Boolean) {
                    this.setDouble(parameterIndex, (Boolean)x != false ? 1.0 : 0.0);
                    break;
                }
                if (x instanceof BigDecimal) {
                    BigDecimal decimal = (BigDecimal)x;
                    if (decimal.doubleValue() > Double.MAX_VALUE || decimal.doubleValue() < -1.7976931348623157E308) {
                        throw new OSQLException("OSCAR-00706", "88888", 706);
                    }
                    this.setDouble(parameterIndex, decimal.doubleValue());
                    break;
                }
                if (x instanceof String) {
                    double d;
                    if (x.toString().trim().equals("")) {
                        this.setEmptyString(parameterIndex);
                        break;
                    }
                    try {
                        d = Double.parseDouble((String)x);
                    }
                    catch (Exception e) {
                        throw new OSQLException("OSCAR-00706", "88888", 706, e);
                    }
                    this.setDouble(parameterIndex, d);
                    break;
                }
                if (x instanceof Character) {
                    try {
                        this.setDouble(parameterIndex, Double.parseDouble(x.toString()));
                        break;
                    }
                    catch (Exception e) {
                        throw new OSQLException("OSCAR-00706", "88888", 706, e);
                    }
                }
                throw new OSQLException("OSCAR-00706", "88888", 706);
            }
            case 2: 
            case 3: {
                if (x instanceof Integer || x instanceof Byte || x instanceof Short || x instanceof Long || x instanceof Float || x instanceof Double || x instanceof BigInteger || x instanceof Character) {
                    if (scale == -2147483638) {
                        this.setBigDecimal(parameterIndex, new BigDecimal(x.toString()));
                        break;
                    }
                    this.setBigDecimal(parameterIndex, new BigDecimal(x.toString()).setScale(scale, 4));
                    break;
                }
                if (x instanceof Boolean) {
                    if (scale == -2147483638) {
                        this.setDouble(parameterIndex, (Boolean)x != false ? 1.0 : 0.0);
                        break;
                    }
                    if (scale < 0) {
                        throw new OSQLException("OSCAR-00707", "88888", 707);
                    }
                    this.setDouble(parameterIndex, new BigDecimal((Boolean)x != false ? 1 : 0).setScale(scale).doubleValue());
                    break;
                }
                if (x instanceof BigDecimal) {
                    if (scale == -2147483638) {
                        this.setBigDecimal(parameterIndex, (BigDecimal)x);
                        break;
                    }
                    this.setBigDecimal(parameterIndex, ((BigDecimal)x).setScale(scale, 4));
                    break;
                }
                if (x instanceof String) {
                    if (x.toString().trim().equals("")) {
                        this.setEmptyString(parameterIndex);
                        break;
                    }
                    if (scale == -2147483638) {
                        this.setBigDecimal(parameterIndex, new BigDecimal(x.toString().trim()));
                        break;
                    }
                    this.setBigDecimal(parameterIndex, new BigDecimal(x.toString().trim()).setScale(scale, 4));
                    break;
                }
                throw new OSQLException("OSCAR-00707", "88888", 707);
            }
            case -7: {
                String value = null;
                if (x instanceof Boolean) {
                    if (((Boolean)x).booleanValue()) {
                        this.setBoolean(parameterIndex, true);
                        break;
                    }
                    this.setBoolean(parameterIndex, false);
                    break;
                }
                if (x instanceof BigDecimal || x instanceof Integer || x instanceof Long || x instanceof Float || x instanceof Double || x instanceof Short || x instanceof String || x instanceof Character || x instanceof BigInteger) {
                    value = x.toString();
                    if (value.equalsIgnoreCase("true")) {
                        this.setBoolean(parameterIndex, true);
                        break;
                    }
                    if (value.equalsIgnoreCase("false")) {
                        this.setBoolean(parameterIndex, false);
                        break;
                    }
                    if (value.length() > 0 && value.charAt(0) == '\u0000') {
                        value = "";
                    }
                    this.setString(parameterIndex, value, 24);
                    break;
                }
                throw new OSQLException("OSCAR-00717", "88888", 717);
            }
            case -1: 
            case 1: 
            case 12: {
                String svalue = null;
                if (x instanceof byte[]) {
                    try {
                        svalue = this.encoding.decode((byte[])x);
                    }
                    catch (Exception e) {
                        throw new OSQLException("OSCAR-00713", "88888", 713, e);
                    }
                    this.setString(parameterIndex, svalue, 24);
                    break;
                }
                if (x instanceof OscarBlob) {
                    if (this.connection.getVersion().isConvertLobLocatorByFunc()) {
                        this.bind(parameterIndex, "blob_locator('" + ((OscarBlob)x).getLocatorStr() + "')", 50);
                        break;
                    }
                    throw new OSQLException("OSCAR-00713", "88888", 713);
                }
                svalue = x.toString();
                if (svalue.length() > 0 && svalue.charAt(0) == '\u0000') {
                    svalue = "";
                }
                this.setString(parameterIndex, svalue, 24);
                break;
            }
            case 91: {
                if (x instanceof Timestamp) {
                    this.setDate(parameterIndex, new Date(((Timestamp)x).getTime()));
                    break;
                }
                if (x instanceof Date) {
                    this.setDate(parameterIndex, (Date)x);
                    break;
                }
                if (x instanceof String) {
                    try {
                        if ("infinity".equalsIgnoreCase((String)x) || "-infinity".equalsIgnoreCase((String)x)) {
                            this.setString(parameterIndex, (String)x);
                        } else {
                            long time = Timestamp.valueOf(x.toString().trim()).getTime();
                            this.setString(parameterIndex, format.format(new Date(time)));
                        }
                        return;
                    }
                    catch (Exception e) {
                        try {
                            this.setDate(parameterIndex, com.oscar.sql.Date.valueOf(x.toString().trim()));
                            break;
                        }
                        catch (Exception e2) {
                            throw new OSQLException("OSCAR-00710", "88888", 710, e2);
                        }
                    }
                }
                throw new OSQLException("OSCAR-00710", "88888", 710);
            }
            case 92: {
                if (x instanceof Timestamp) {
                    this.setTime(parameterIndex, new Time(((Timestamp)x).getHours(), ((Timestamp)x).getMinutes(), ((Timestamp)x).getSeconds()));
                    break;
                }
                if (x instanceof Time) {
                    this.setTime(parameterIndex, (Time)x);
                    break;
                }
                if (x instanceof String) {
                    try {
                        long time = Timestamp.valueOf(x.toString().trim()).getTime();
                        this.setString(parameterIndex, new Time(time).toString());
                        return;
                    }
                    catch (Exception e) {
                        try {
                            this.setTime(parameterIndex, Time.valueOf(x.toString().trim()));
                            break;
                        }
                        catch (Exception e3) {
                            throw new OSQLException("OSCAR-00711", "88888", 711, e3);
                        }
                    }
                }
                throw new OSQLException("OSCAR-00711", "88888", 711);
            }
            case 93: {
                if (x instanceof Date) {
                    this.setTimestamp(parameterIndex, new Timestamp(((Date)x).getTime()));
                    break;
                }
                if (x instanceof Time) {
                    this.setTimestamp(parameterIndex, new Timestamp(((Time)x).getTime()));
                    break;
                }
                if (x instanceof Timestamp) {
                    this.setTimestamp(parameterIndex, (Timestamp)x);
                    break;
                }
                if (x instanceof String) {
                    try {
                        if ("infinity".equalsIgnoreCase((String)x)) {
                            this.setTimestamp(parameterIndex, new Timestamp(Long.MAX_VALUE));
                        } else if ("-infinity".equalsIgnoreCase((String)x)) {
                            this.setTimestamp(parameterIndex, new Timestamp(Long.MIN_VALUE));
                        } else {
                            long time = Date.valueOf(x.toString().trim()).getTime();
                            this.setTimestamp(parameterIndex, new Timestamp(time));
                        }
                        return;
                    }
                    catch (Exception e) {
                        try {
                            this.setTimestamp(parameterIndex, Timestamp.valueOf(x.toString().trim()));
                            break;
                        }
                        catch (Exception e4) {
                            throw new OSQLException("OSCAR-00712", "88888", 712, e4);
                        }
                    }
                }
                throw new OSQLException("OSCAR-00712", "88888", 712);
            }
            case 16: {
                if (x instanceof Boolean) {
                    this.setBoolean(parameterIndex, (boolean)((Boolean)x));
                    break;
                }
                if (x instanceof BigDecimal || x instanceof Integer || x instanceof Long || x instanceof Float || x instanceof Double || x instanceof Short || x instanceof Byte || x instanceof String || x instanceof Character || x instanceof BigInteger) {
                    String s = x.toString();
                    if (s.length() == 1) {
                        if (s.charAt(0) == '1' || s.charAt(0) == 't' || s.charAt(0) == 'y' || s.charAt(0) == 'T' || s.charAt(0) == 'Y') {
                            this.setBoolean(parameterIndex, true);
                            break;
                        }
                        this.setBoolean(parameterIndex, false);
                        break;
                    }
                    if (s.length() > 1) {
                        double d;
                        if (s.equalsIgnoreCase("true")) {
                            this.setBoolean(parameterIndex, true);
                            break;
                        }
                        if (s.equalsIgnoreCase("false")) {
                            this.setBoolean(parameterIndex, false);
                            break;
                        }
                        try {
                            d = Double.parseDouble(s);
                        }
                        catch (Exception e) {
                            throw new OSQLException("OSCAR-00708", "88888", 708, e);
                        }
                        if (d >= 1.0E-10 || d <= -1.0E-10) {
                            this.setBoolean(parameterIndex, true);
                            break;
                        }
                        this.setBoolean(parameterIndex, false);
                        break;
                    }
                    this.setBoolean(parameterIndex, false);
                    break;
                }
                throw new OSQLException("OSCAR-00708", "88888", 708);
            }
            case -4: 
            case -3: 
            case -2: {
                if (x instanceof byte[]) {
                    String svalue;
                    if (this.connection.sendBinaryTypeAsHex()) {
                        this.setString(parameterIndex, OSCARbyte.toOSCARString((byte[])x), 24);
                        break;
                    }
                    try {
                        svalue = this.clientEncoding.decode((byte[])x);
                    }
                    catch (Exception e) {
                        throw new OSQLException("OSCAR-00713", "88888", 713, e);
                    }
                    this.setString(parameterIndex, svalue, 24);
                    break;
                }
                if (x instanceof String) {
                    if (this.connection.sendBinaryTypeAsHex()) {
                        this.setString(parameterIndex, OSCARbyte.toOSCARString(this.encoding.encode((String)x)), 24);
                        break;
                    }
                    this.setString(parameterIndex, (String)x, 24);
                    break;
                }
                throw new OSQLException("OSCAR-00709", "88888", 709);
            }
            case 2004: {
                if (x instanceof Blob) {
                    if (x instanceof OscarBlob) {
                        if (((OscarBlob)x).isEmptyLob()) {
                            this.bind(parameterIndex, "EMPTY_BLOB()", 50);
                            break;
                        }
                        if (this.connection.getVersion().isConvertLobLocatorByFunc()) {
                            this.bind(parameterIndex, "blob_locator('" + ((OscarBlob)x).getLocatorStr() + "')", 50);
                            break;
                        }
                        this.bind(parameterIndex, ((OscarBlob)x).getLocatorStr(), 50);
                        break;
                    }
                    Blob blob = (Blob)x;
                    byte[] bytes = blob.getBytes(1L, (int)blob.length());
                    this.setBlobString(parameterIndex, bytes, 2004);
                    break;
                }
                if (x instanceof byte[]) {
                    this.setBlobString(parameterIndex, (byte[])x, 2004);
                    break;
                }
                if (Boolean.valueOf(this.connection.getConnectionProperties().getProperty("OBJECTTOSTRING", "false")).booleanValue() && x instanceof String) {
                    String charset = this.connection.getConnectionProperties().getProperty("OBJECTTOSTRINGCHARSET", this.clientEncoding.getEncoding());
                    try {
                        this.setBlobString(parameterIndex, ((String)x).getBytes(charset), 2004);
                        break;
                    }
                    catch (UnsupportedEncodingException e) {
                        throw new OSQLException("OSCAR-00001", "8888", 1);
                    }
                }
                throw new OSQLException("OSCAR-00714", "88888", 714);
            }
            case 2005: {
                String cvalue = "";
                if (x instanceof Clob) {
                    if (x instanceof OscarClob) {
                        if (((OscarClob)x).isEmptyLob()) {
                            this.bind(parameterIndex, "EMPTY_CLOB()", 51);
                            break;
                        }
                        if (this.connection.getVersion().isConvertLobLocatorByFunc()) {
                            this.bind(parameterIndex, "clob_locator('" + ((OscarClob)x).getLocatorStr() + "')", 51);
                            break;
                        }
                        this.bind(parameterIndex, ((OscarClob)x).getLocatorStr(), 51);
                        break;
                    }
                    String clob2String = this.clob2String((Clob)x);
                    if (clob2String.length() > 0 && clob2String.charAt(0) == '\u0000') {
                        clob2String = "";
                    }
                    this.bind(parameterIndex, clob2String, 24);
                    break;
                }
                if (x instanceof String) {
                    cvalue = x.toString();
                    if (cvalue.length() > 0 && cvalue.charAt(0) == '\u0000') {
                        cvalue = "";
                    }
                    this.bind(parameterIndex, cvalue, 24);
                    break;
                }
                if (x instanceof byte[]) {
                    cvalue = this.encoding.decode((byte[])x);
                    this.bind(parameterIndex, cvalue, 24);
                    break;
                }
                throw new OSQLException("OSCAR-00715", "88888", 715);
            }
            case -11: {
                if (x instanceof OscarBfile) {
                    if (this.connection.getVersion().isConvertLobLocatorByFunc()) {
                        this.bind(parameterIndex, "bfile_locator('" + ((OscarBfile)x).getLocatorStr() + "')", 52);
                        break;
                    }
                    this.bind(parameterIndex, ((OscarBfile)x).getLocatorStr(), 52);
                    break;
                }
                throw new OSQLException("OSCAR-00716", "88888", 716);
            }
            case 1111: {
                this.setString(parameterIndex, String.valueOf(x));
                break;
            }
            case 2003: {
                this.setString(parameterIndex, this.getArrayToString(x), 24);
                break;
            }
            case 2002: {
                this.setString(parameterIndex, String.valueOf(x));
                break;
            }
            default: {
                throw new OSQLException("OSCAR-00411", "88888", 411);
            }
        }
    }

    public void freeBindedLob(OscarLob lob, int type) throws SQLException {
        if (lob != null && (type == 50 || type == 51 || type == 52)) {
            if (lob.isTempLob()) {
                lob.freeTemporary();
            } else if (lob.isTableIdLob()) {
                lob.freePersist();
            }
        }
    }

    protected String getFixedString(int parameterIndex) throws SQLException {
        this.checkOutParameter(parameterIndex);
        if (this.checkNullValue(parameterIndex - 1)) {
            return null;
        }
        if (this.netDataByStr) {
            return super.getFixedString(parameterIndex);
        }
        return this.getStringValue(this.m_outValues[parameterIndex - 1], this.getOscarType(parameterIndex));
    }

    private String getStringValue(Object o, int oscarType) throws SQLException {
        if (o == null) {
            return null;
        }
        byte[] value = (byte[])o;
        if (value.length == 2 && value[0] == -3) {
            if (value[1] == 1) {
                return "NaN";
            }
            if (value[1] == 2) {
                return "infinity";
            }
            if (value[1] == 3) {
                return "-infinity";
            }
        }
        String tmp = null;
        switch (oscarType) {
            case 35: {
                return this.encoding.decode(value);
            }
            case 51: {
                OscarClob clob = this.connection.getClobInstance(new String(value));
                long length = clob.length();
                if (length > Integer.MAX_VALUE) {
                    throw new OSQLException("OSCAR-00316", "22000", 316);
                }
                return clob.getSubString(1L, (int)length);
            }
            case 33: {
                return String.valueOf(BooleanConverter.convertToBoolean(value));
            }
            case 25: {
                return format.format(DateConverter.convertBytesToDate(value));
            }
            case 34: {
                if (this.numericKeepPrecision) {
                    return this.encoding.decode(value);
                }
                String retVal = String.valueOf(NumberConverter.convertBytesToDouble(value));
                if (retVal.endsWith(".0") && retVal.length() > 2) {
                    return retVal.substring(0, retVal.length() - 2);
                }
                return retVal;
            }
            case 31: {
                return IntervalConverter.convertToIntervalDTS(value);
            }
            case 30: {
                return IntervalConverter.convertToIntervalYTM(value);
            }
            case 23: {
                return String.valueOf(NumberConverter.convertBytesToLong(value));
            }
            case 32: {
                return String.valueOf(RowidConverter.convertToRowID(value));
            }
            case 26: {
                return String.valueOf(TimetzConverter.convertBytesToTime(value));
            }
            case 28: {
                tmp = String.valueOf(TimestampConverter.convertBytesToTimeStamp(value));
                return tmp;
            }
            case 29: {
                tmp = String.valueOf(TimestamptzConverter.convertBytesToTimeStamp(value));
                if (tmp != null) {
                    if (tmp.endsWith(".0") && tmp.length() > 2) {
                        tmp = tmp.substring(0, tmp.length() - 2);
                    } else if (tmp.endsWith(".0 BC") && tmp.length() > 5) {
                        tmp = tmp.substring(0, tmp.length() - 5);
                        tmp = tmp + " BC";
                    }
                }
                return tmp;
            }
            case 27: {
                return String.valueOf(TimetzConverter.convertBytesToTime(value));
            }
            case 24: {
                return this.encoding.decode(value);
            }
        }
        return this.encoding.decode(value);
    }

    protected int getOscarType(int columnIndex) throws SQLException {
        int index = columnIndex;
        int outParIndex = this.outParameterIndex.indexOf(index);
        if (this.isFunc && !this.isResultNeeded) {
            ++outParIndex;
        }
        return this.fields[outParIndex].getOscarType();
    }

    public void hardClose() throws SQLException {
        if (logFlag) {
            Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarStatementV2.class + ", hardClose()");
        }
        if (this.isClosed) {
            return;
        }
        if (this.result != null) {
            this.result.close();
            this.result = null;
        }
        this.cleanEnvTemp();
        this.clearParameters();
        if (this.statementType != 0 && this.m_statementName != null) {
            if (this.statementType == 2) {
                this.releasePrepareStatementReally();
            } else {
                this.releasePrepareStatement();
            }
        }
        this.connection = null;
        this.batchPacket = null;
        this.encoding = null;
        this.osql = null;
        this.outParameterType = null;
        this.outParameterIndex = null;
        this.pMetaData = null;
        this.sbuf = null;
        this.m_sqlFragments = null;
        this.m_origSqlFragments = null;
        this.m_executeSqlFragments = null;
        this.m_binds = null;
        this.m_lobs = null;
        this.m_bindTypes = null;
        this.m_functionTypes = null;
        this.m_outValues = null;
        this.m_returnValue = null;
        this.m_returnType = 0;
        this.isClosed = true;
    }

    protected void releasePrepareStatementReally() throws SQLException {
        block6: {
            String currentprepareSql;
            if (this.isPrepared) {
                try {
                    if (this.connection != null && !((Connection)((Object)this.connection)).isClosed()) {
                        this.connection.execSQL("DEALLOCATE PREPARE " + this.m_statementName);
                    }
                    break block6;
                }
                catch (SQLException e) {
                    if (logFlag) {
                        Driver.writeLog("session: " + this.connection.getSessionID() + ", " + OscarStatementV2.class + ", releasePrepareStatementReally(),error:" + e.getMessage());
                    }
                    break block6;
                }
            }
            if (this.m_prepareSqlStatement != null && this.m_sqlFragments != null && this.selectSql && this.fetchSize > 0 && (this.resultSetType != 1003 || this.resultSetConcurrency != 1007) && !(currentprepareSql = "PREPARE " + this.m_statementName + " AS " + this.m_sqlFragments[0] + ";").equalsIgnoreCase(this.m_prepareSqlStatement)) {
                this.m_prepareSqlStatement = currentprepareSql;
            }
        }
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        if (this.pMetaData == null) {
            this.pMetaData = (OscarParaMetaData)this.getParameterMetaData();
        }
        switch (this.pMetaData.getParameterType(parameterIndex)) {
            case -1: 
            case 1: 
            case 12: {
                try {
                    if (x == null) {
                        this.setString(parameterIndex, null);
                        break;
                    }
                    InputStreamReader l_inStream = new InputStreamReader(x, "US-ASCII");
                    char[] l_chars = new char[length];
                    int l_charsRead = l_inStream.read(l_chars, 0, length);
                    this.setString(parameterIndex, new String(l_chars, 0, l_charsRead));
                    break;
                }
                catch (UnsupportedEncodingException l_uee) {
                    throw new OSQLException("OSCAR-00302", "88888", 302, l_uee.getMessage(), l_uee);
                }
                catch (IOException l_ioe) {
                    throw new OSQLException("OSCAR-00302", "88888", 302, l_ioe.getMessage(), l_ioe);
                }
            }
            case 2005: {
                if (x == null) {
                    this.setObject(parameterIndex, null, 2005);
                    this.m_lobs[parameterIndex - 1] = null;
                } else {
                    OscarClob clob = null;
                    if (this.pMetaData.getParameterRelOid(parameterIndex) != 0) {
                        int tableOid = this.pMetaData.getParameterRelOid(parameterIndex);
                        int colIndex = this.pMetaData.getParameterRelColIndex(parameterIndex);
                        clob = OscarClob.createForTable(this.getConnection(), tableOid, colIndex);
                    } else {
                        clob = ((OscarJdbc2Connection)this.connection).createClob();
                    }
                    clob.write(1L, x, (long)length);
                    this.setObject(parameterIndex, (Object)clob, 2005);
                    this.m_lobs[parameterIndex - 1] = clob;
                }
                if (this.m_lobs[parameterIndex - 1] == null || !this.m_lobs[parameterIndex - 1].isTempLob() && !this.m_lobs[parameterIndex - 1].isTableIdLob()) break;
                this.tempResource.add(this.m_lobs[parameterIndex - 1]);
                break;
            }
            default: {
                throw new OSQLException("OSCAR-00410", "88888", 410);
            }
        }
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        if (this.pMetaData == null) {
            this.pMetaData = (OscarParaMetaData)this.getParameterMetaData();
        }
        int type = this.pMetaData.getParameterType(parameterIndex);
        switch (type) {
            case -4: 
            case -3: 
            case -2: {
                int l_bytesRead;
                byte[] l_bytes = new byte[length];
                try {
                    if (x == null) {
                        this.setBytes(parameterIndex, null);
                        return;
                    }
                    l_bytesRead = x.read(l_bytes, 0, length);
                }
                catch (IOException l_ioe) {
                    throw new OSQLException("OSCAR-00410", "88888", 410, l_ioe.getMessage(), l_ioe);
                }
                if (l_bytesRead == length) {
                    this.setBytes(parameterIndex, l_bytes);
                    break;
                }
                byte[] l_bytes2 = new byte[l_bytesRead];
                System.arraycopy(l_bytes, 0, l_bytes2, 0, l_bytesRead);
                this.setBytes(parameterIndex, l_bytes2);
                break;
            }
            case 2004: {
                OscarBlob blob = null;
                int tableOid = this.pMetaData.getParameterRelOid(parameterIndex);
                if (tableOid != 0) {
                    Character tableType = (Character)this.tableTypes.get(new Integer(tableOid));
                    if (tableType == null) {
                        tableType = this.getRelKind(tableOid);
                        this.tableTypes.put(new Integer(tableOid), tableType);
                    }
                    if (tableType.charValue() == 'r') {
                        int colIndex = this.pMetaData.getParameterRelColIndex(parameterIndex);
                        blob = OscarBlob.createForTable(this.getConnection(), tableOid, colIndex);
                    } else {
                        blob = ((OscarJdbc2Connection)this.connection).createBlob();
                    }
                } else {
                    blob = ((OscarJdbc2Connection)this.connection).createBlob();
                }
                if (length > 0) {
                    try {
                        blob.write(1L, x, length);
                    }
                    catch (SQLException ex) {
                        this.m_lobs[parameterIndex - 1] = null;
                        throw ex;
                    }
                }
                OutputStream ostream = blob.setBinaryStream(1L);
                try {
                    byte[] buf = new byte[this.bufSize];
                    int readLen = 0;
                    while ((readLen = x.read(buf, 0, this.bufSize)) > 0) {
                        ostream.write(buf, 0, readLen);
                    }
                    ostream.flush();
                    ostream.close();
                }
                catch (Exception e) {
                    this.m_lobs[parameterIndex - 1] = null;
                }
                this.setObject(parameterIndex, (Object)blob, 2004);
                this.m_lobs[parameterIndex - 1] = blob;
                if (this.m_lobs[parameterIndex - 1] == null || !this.m_lobs[parameterIndex - 1].isTempLob() && !this.m_lobs[parameterIndex - 1].isTableIdLob()) break;
                this.tempResource.add(this.m_lobs[parameterIndex - 1]);
                break;
            }
            case -1: 
            case 1: 
            case 12: {
                try {
                    byte[] bytes = new byte[length];
                    x.read(bytes, 0, length);
                    this.setString(parameterIndex, OSCARbyte.toOSCARString(bytes), 24);
                    break;
                }
                catch (Exception e) {
                    throw new OSQLException("OSCAR-00410", "88888", 410);
                }
            }
            default: {
                throw new OSQLException("OSCAR-00410", "88888", 410);
            }
        }
    }

    public void setBlobString(int parameterIndex, Object x, int type) throws SQLException {
        if (this.pMetaData == null) {
            this.pMetaData = (OscarParaMetaData)this.getParameterMetaData();
        }
        if (x == null) {
            this.setObject(parameterIndex, null, type);
            this.m_lobs[parameterIndex - 1] = null;
        } else if (x instanceof byte[]) {
            byte[] bytes = (byte[])x;
            if (bytes.length > 4000) {
                OscarBlob blob = null;
                int tableOid = this.pMetaData.getParameterRelOid(parameterIndex);
                if (tableOid != 0) {
                    Character tableType = (Character)this.tableTypes.get(new Integer(tableOid));
                    if (tableType == null) {
                        tableType = this.getRelKind(tableOid);
                        this.tableTypes.put(new Integer(tableOid), tableType);
                    }
                    if (tableType.charValue() == 'r') {
                        int colIndex = this.pMetaData.getParameterRelColIndex(parameterIndex);
                        blob = OscarBlob.createForTable(this.getConnection(), tableOid, colIndex);
                    } else {
                        blob = ((OscarJdbc2Connection)this.connection).createBlob();
                    }
                } else {
                    blob = ((OscarJdbc2Connection)this.connection).createBlob();
                }
                blob.setBytes(1L, bytes);
                this.setObject(parameterIndex, (Object)blob, type);
                this.m_lobs[parameterIndex - 1] = blob;
            } else {
                this.setString(parameterIndex, OSCARbyte.toOSCARString((byte[])x), 24);
                this.m_lobs[parameterIndex - 1] = null;
            }
        }
        if (this.m_lobs[parameterIndex - 1] != null && (this.m_lobs[parameterIndex - 1].isTempLob() || this.m_lobs[parameterIndex - 1].isTableIdLob())) {
            this.tempResource.add(this.m_lobs[parameterIndex - 1]);
        }
    }

    public void setCharacterStream(int i, Reader x, int length) throws SQLException {
        if (this.pMetaData == null) {
            this.pMetaData = (OscarParaMetaData)this.getParameterMetaData();
        }
        switch (this.pMetaData.getParameterType(i)) {
            case -1: 
            case 1: 
            case 12: {
                try {
                    if (x == null) {
                        this.setString(i, null);
                        break;
                    }
                    char[] l_chars = new char[length];
                    int l_charsRead = x.read(l_chars, 0, length);
                    this.setString(i, new String(l_chars, 0, l_charsRead));
                    break;
                }
                catch (IOException l_ioe) {
                    throw new OSQLException("OSCAR-00311", "88888", 311, l_ioe.getMessage(), l_ioe);
                }
            }
            case 2005: {
                if (x == null) {
                    this.setObject(i, null, 2005);
                    this.m_lobs[i - 1] = null;
                    break;
                }
                OscarClob clob = null;
                int tableOid = this.pMetaData.getParameterRelOid(i);
                if (tableOid != 0) {
                    Character tableType = (Character)this.tableTypes.get(new Integer(tableOid));
                    if (tableType == null) {
                        tableType = this.getRelKind(tableOid);
                        this.tableTypes.put(new Integer(tableOid), tableType);
                    }
                    if (tableType.charValue() == 'r') {
                        int colIndex = this.pMetaData.getParameterRelColIndex(i);
                        clob = OscarClob.createForTable(this.getConnection(), tableOid, colIndex);
                    } else {
                        clob = ((OscarJdbc2Connection)this.connection).createClob();
                    }
                } else {
                    clob = ((OscarJdbc2Connection)this.connection).createClob();
                }
                if (length > 0) {
                    try {
                        clob.write(1L, x, (long)length);
                    }
                    catch (Exception e) {
                        this.m_lobs[i - 1] = null;
                    }
                } else {
                    Writer ostream = clob.setCharacterStream(1L);
                    try {
                        char[] buf = new char[this.bufSize];
                        int readLen = 0;
                        while ((readLen = x.read(buf, 0, this.bufSize)) > 0) {
                            ostream.write(buf, 0, readLen);
                        }
                        ostream.flush();
                        ostream.close();
                    }
                    catch (Exception ex) {
                        this.m_lobs[i - 1] = null;
                    }
                }
                this.setObject(i, (Object)clob, 2005);
                this.m_lobs[i - 1] = clob;
                if (this.m_lobs[i - 1] == null || !this.m_lobs[i - 1].isTempLob() && !this.m_lobs[i - 1].isTableIdLob()) break;
                this.tempResource.add(this.m_lobs[i - 1]);
                break;
            }
            default: {
                throw new OSQLException("OSCAR-00410", "88888", 410);
            }
        }
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        if (this.pMetaData == null) {
            this.pMetaData = (OscarParaMetaData)this.getParameterMetaData();
        }
        switch (this.pMetaData.getParameterType(parameterIndex)) {
            case -1: 
            case 1: 
            case 12: {
                break;
            }
            default: {
                throw new OSQLException("OSCAR-00410", "88888", 410);
            }
        }
        try {
            if (x == null) {
                this.setString(parameterIndex, null);
                return;
            }
            InputStreamReader l_inStream = new InputStreamReader(x, "UTF-16BE");
            char[] l_chars = new char[length];
            int l_charsRead = l_inStream.read(l_chars, 0, length);
            this.setString(parameterIndex, new String(l_chars, 0, l_charsRead));
        }
        catch (UnsupportedEncodingException l_uee) {
            throw new OSQLException("OSCAR-00303", "88888", 303, l_uee.getMessage(), l_uee);
        }
        catch (IOException l_ioe) {
            throw new OSQLException("OSCAR-00303", "88888", 303, l_ioe.getMessage(), l_ioe);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void convertBindDatas(boolean toOldType) throws SQLException {
        if (this.m_bindTypes == null || this.m_bindTypes.length == 0) {
            return;
        }
        if (this.m_binds == null || this.m_binds.length == 0) {
            return;
        }
        if (this.m_binds.length != this.m_bindTypes.length) {
            return;
        }
        System.arraycopy(this.m_binds_old, 0, this.m_binds, 0, this.m_binds.length);
        if (toOldType) {
            String tmpStr = null;
            for (int j = 0; j < this.m_bindTypes.length; ++j) {
                if (this.m_binds[j] == null) {
                    this.m_binds[j] = "null";
                    continue;
                }
                if (this.m_bindTypes[j] == 52 || this.m_bindTypes[j] == 50 || this.m_bindTypes[j] == 51 || this.m_binds[j] instanceof Integer || this.m_binds[j] instanceof Long || this.m_binds[j] instanceof Double || this.m_binds[j] instanceof Short || (tmpStr = this.m_binds[j].toString()).length() > 1 && tmpStr.charAt(0) == '\'' && tmpStr.charAt(tmpStr.length() - 1) == '\'' && !this.isNotRealPrepare()) continue;
                if (tmpStr.equals(String.valueOf('\u0000'))) {
                    this.m_binds[j] = "''";
                    continue;
                }
                StringBuffer stringBuffer = this.sbuf;
                synchronized (stringBuffer) {
                    int i;
                    this.sbuf.setLength(0);
                    this.sbuf.ensureCapacity(tmpStr.length() + tmpStr.length() / 10);
                    this.sbuf.append('\'');
                    char c = '\u0000';
                    if (this.m_bindTypes[j] == 50 || this.m_bindTypes[j] == 51 || this.m_bindTypes[j] == 52) {
                        this.sbuf.append(tmpStr);
                    } else if (this.connection.getVersion().getTransferType() == 1) {
                        for (i = 0; i < tmpStr.length(); ++i) {
                            c = tmpStr.charAt(i);
                            if (c == '\\' || c == '\'') {
                                this.sbuf.append('\\');
                            }
                            this.sbuf.append(c);
                        }
                    } else if (this.connection.getVersion().getTransferType() == 2) {
                        if (tmpStr.indexOf("'") != -1) {
                            for (i = 0; i < tmpStr.length(); ++i) {
                                c = tmpStr.charAt(i);
                                if (c == '\'') {
                                    this.sbuf.append('\'');
                                    if (!this.hasEscapeChar) {
                                        this.hasEscapeChar = true;
                                    }
                                }
                                this.sbuf.append(c);
                            }
                        } else {
                            this.sbuf.append(tmpStr);
                        }
                    } else {
                        this.sbuf.append(tmpStr);
                    }
                    this.sbuf.append('\'');
                    this.m_binds[j] = this.sbuf.toString();
                    continue;
                }
            }
        } else {
            BindConverter.convertBindDatas(this.m_binds, this.m_bindTypes, this.encoding);
        }
    }

    public void cache() throws SQLException {
        OscarJdbc2Connection conn = (OscarJdbc2Connection)this.connection;
        if (this instanceof OscarPreparedStatementV2) {
            conn.cacheImplicitStatement((OscarPreparedStatementV2)this, this.osql, this.statementType, this.resultSetType);
        } else {
            this.hardClose();
        }
    }

    public void setLobDisplayMaxSize(int lobDisplayMaxSize) {
        this.lobDisplayMaxSize = lobDisplayMaxSize;
    }

    private void updateBatch(int paramIndex) throws SQLException {
        if (this.batch == null) {
            return;
        }
        for (int i = 0; i < this.batch.size(); ++i) {
            Object temp;
            OscarStatement.BatchRowData rowData = (OscarStatement.BatchRowData)this.batch.get(i);
            Object[] currentData = rowData.getData();
            if (paramIndex > currentData.length || (temp = currentData[paramIndex - 1]) == null) continue;
            int[] currentType = rowData.getTypes();
            currentType[paramIndex - 1] = 24;
        }
    }

    private void handleNullStatementName(String exceptionFlag) throws SQLException {
        if (this.m_statementName == null && this.m_prepareSqlStatement.startsWith("PREPARE ")) {
            Matcher matcher = PATTERN_PREPARE.matcher(this.m_prepareSqlStatement);
            if (matcher.find()) {
                this.m_statementName = matcher.group();
            } else {
                if ("notImplemented".equals(exceptionFlag)) {
                    throw Driver.notImplemented();
                }
                if (exceptionFlag.startsWith("OSCAR")) {
                    throw new OSQLException("OSCAR-00421", "88888", 421);
                }
                throw new SQLException("statementName\u4e3anull");
            }
        }
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 24);
        } else if (x.length > 8000) {
            int targetSqlType;
            if (this.pMetaData == null) {
                this.pMetaData = (OscarParaMetaData)this.getParameterMetaData();
            }
            if ((targetSqlType = this.pMetaData.getParameterType(parameterIndex)) == 2004) {
                OscarBlob blob = null;
                int tableOid = this.pMetaData.getParameterRelOid(parameterIndex);
                if (tableOid != 0) {
                    Character tableType = (Character)this.tableTypes.get(new Integer(tableOid));
                    if (tableType == null) {
                        tableType = this.getRelKind(tableOid);
                        this.tableTypes.put(new Integer(tableOid), tableType);
                    }
                    if (tableType.charValue() == 'r') {
                        int colIndex = this.pMetaData.getParameterRelColIndex(parameterIndex);
                        blob = OscarBlob.createForTable(this.getConnection(), tableOid, colIndex);
                    } else {
                        blob = ((OscarJdbc2Connection)this.connection).createBlob();
                    }
                } else {
                    blob = ((OscarJdbc2Connection)this.connection).createBlob();
                }
                blob.setBytes(1L, x);
                this.setObject(parameterIndex, (Object)blob, 2004);
                this.m_lobs[parameterIndex - 1] = blob;
            } else {
                this.setString(parameterIndex, OSCARbyte.toOSCARString(x), 24);
            }
        } else {
            this.setString(parameterIndex, OSCARbyte.toOSCARString(x), 24);
        }
    }
}

