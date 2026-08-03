/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.core.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public interface BaseResultSet
extends ResultSet {
    public void append(BaseResultSet var1);

    public void setPrevious(BaseResultSet var1);

    public void close() throws SQLException;

    public int getColumnCount();

    public String getCursorName() throws SQLException;

    public long getInsertRowid();

    public ResultSetMetaData getMetaData() throws SQLException;

    public ResultSet getNext();

    public ResultSet getPrevious();

    public Object getObject(int var1) throws SQLException;

    public int getResultCount();

    public String getStatusString();

    public String getString(int var1) throws SQLException;

    public byte[][] getCurrentRow();

    public int getTupleCount();

    public boolean next() throws SQLException;

    public boolean reallyResultSet();

    public void setTidValues(Field var1, List var2);

    public void reInit(Field[] var1, List var2, String var3, int var4, long var5);

    public Field[] getFields();

    public List getTuples();

    public boolean isCursorUsed();

    public void setCursorUsed(boolean var1);

    public void setCursorMoveSize(int var1);

    public List getTidValues();

    public Field getTidField();

    public void setPlanID(byte[] var1);

    public byte[] getPlanID();

    public boolean isClosed();

    public void setResultType(boolean var1);
}

