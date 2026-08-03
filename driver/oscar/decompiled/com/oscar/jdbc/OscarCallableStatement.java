/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseConnection;
import com.oscar.core.BaseResultSet;
import com.oscar.core.Field;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.jdbc.OscarParaMetaData;
import com.oscar.jdbc.OscarResultSet;
import com.oscar.jdbc.OscarStatement;
import com.oscar.jdbc.entity.ParamInfo;
import com.oscar.util.OSQLException;
import com.oscar.util.OscarSqlProcessor;
import com.oscar.util.TypeConverter;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OscarCallableStatement
extends OscarStatement
implements CallableStatement {
    protected HashMap<Integer, Integer> outParameterScale = null;
    OscarSqlProcessor.ParseFunctionResult pr;

    public OscarCallableStatement(OscarJdbc2Connection connection, String sql) throws SQLException {
        super((BaseConnection)connection, sql);
        this.setStatementType(2);
    }

    public BaseResultSet createResultSet(Field[] fields, List tuples, String status, int updateCount, long insertOID) throws SQLException {
        return new OscarResultSet(this, fields, tuples, status, updateCount, insertOID, this.fetchSize, this.maxrows);
    }

    protected String subParseSqlStmt(String sql) throws SQLException {
        OscarSqlProcessor.ParseFunctionResult pr = OscarSqlProcessor.modifyJdbcCall(sql, this.connection);
        this.isCallable = pr.isCallable();
        this.isFunc = pr.isFunc();
        this.isResultNeeded = pr.isResultNeeded();
        this.isHaveFuncReturn = pr.haveFuncReturn();
        this.pr = pr;
        return pr.getAfterSql();
    }

    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        if (this.pMetaData == null) {
            this.pMetaData = (OscarParaMetaData)this.getParameterMetaData();
        }
        int index = this.pMetaData.getIndex(parameterName);
        this.registerOutParameter(index, sqlType, scale);
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        this.registerOutParameter(parameterIndex, sqlType);
        if (this.outParameterScale == null) {
            this.outParameterScale = new HashMap();
        }
        this.outParameterScale.put(parameterIndex, scale);
    }

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        this.checkIndex(parameterIndex, 3, "BigDecimal");
        BigDecimal value = TypeConverter.toBigDecimal(this.getFixedString(parameterIndex), this.m_functionTypes[parameterIndex - 1]);
        if (value != null && this.outParameterScale != null && this.outParameterScale.containsKey(parameterIndex)) {
            value = value.setScale((int)this.outParameterScale.get(parameterIndex), 4);
        }
        return value;
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        if (this.pMetaData == null) {
            this.pMetaData = (OscarParaMetaData)this.getParameterMetaData();
        }
        int index = this.pMetaData.getIndex(parameterName);
        return this.getBigDecimal(index);
    }

    public void closeOrCache() throws SQLException {
        super.closeOrCache();
        if (this.outParameterScale != null) {
            this.outParameterScale.clear();
            this.outParameterScale = null;
        }
    }

    protected void getProcedureResult() throws SQLException {
        this.result.next();
        byte[][] row = this.result.getCurrentRow();
        this.m_functionTypes = new int[this.m_binds.length];
        int pNum = this.parameterNum;
        int rowIndex = 0;
        if (!this.isResultNeeded && this.isFunc) {
            ++pNum;
            this.m_returnValue = row[rowIndex];
            this.m_returnType = this.result.getMetaData().getColumnType(rowIndex + 1);
            ++rowIndex;
        }
        if (pNum != this.result.getColumnCount()) {
            Map<String, ParamInfo> paramsMap = this.pr.getParams();
            if (paramsMap == null) {
                OscarSqlProcessor.initObjectInfo(this.pr, this.connection);
                paramsMap = this.pr.getParams();
            }
            if (paramsMap != null) {
                Collection<ParamInfo> valueParams = paramsMap.values();
                HashMap newMap = new HashMap();
                long selectOid = -1L;
                for (ParamInfo paramInfo : valueParams) {
                    long objectId = paramInfo.getOid();
                    if (newMap.containsKey(objectId)) {
                        ((List)newMap.get(objectId)).add(paramInfo);
                        continue;
                    }
                    ArrayList<ParamInfo> paramList = new ArrayList<ParamInfo>();
                    paramList.add(paramInfo);
                    newMap.put(objectId, paramList);
                }
                Set newKeys = newMap.keySet();
                for (Long newKey : newKeys) {
                    if (((List)newMap.get(newKey)).size() != this.fragmentsCount - 1) continue;
                    selectOid = newKey;
                    break;
                }
                if (selectOid == -1L) {
                    throw new OSQLException("OSCAR-00407", "88888", 407);
                }
                List paramInfos = (List)newMap.get(selectOid);
                if (paramInfos != null) {
                    for (ParamInfo info : paramInfos) {
                        int inOut;
                        int key = info.getSequence();
                        if (this.outParameterIndex.contains(key) || (inOut = info.getInout()) != 2 && inOut != 3) continue;
                        this.outParameterIndex.add(this.parameterNum++, key);
                        Collections.sort(this.outParameterIndex);
                        int index = this.outParameterIndex.indexOf(key);
                        this.outParameterType.insertElementAt(this.connection.getSQLType(info.getType()), index);
                    }
                }
            }
        }
        for (Object index : this.outParameterIndex) {
            int resIndex = (Integer)index - 1;
            this.m_functionTypes[resIndex] = this.result.getMetaData().getColumnType(rowIndex + 1);
            this.m_outValues[resIndex] = row[rowIndex];
            ++rowIndex;
        }
    }
}

