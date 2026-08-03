/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.executor;

import com.oscar.dispatcher.entity.DispatchConnection;
import com.oscar.dispatcher.executor.AbstractExecuteCommand;
import com.oscar.dispatcher.executor.DispatchPreparedStatementV2;
import com.oscar.dispatcher.executor.command.CallableStCommand;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class DispatchCallableStatementV2
extends DispatchPreparedStatementV2
implements CallableStatement {
    public DispatchCallableStatementV2(DispatchConnection conn, String sql) {
        super(conn);
        this.dispatchType = this.getExecuteType(sql);
        this.createCommand = new CallableStCommand(sql);
    }

    public DispatchCallableStatementV2(DispatchConnection conn, String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        super(conn);
        this.dispatchType = this.getExecuteType(sql);
        this.createCommand = new CallableStCommand(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public DispatchCallableStatementV2(DispatchConnection conn, String sql, int resultSetType, int resultSetConcurrency) {
        super(conn);
        this.dispatchType = this.getExecuteType(sql);
        this.createCommand = new CallableStCommand(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public void registerOutParameter(final int parameterIndex, final int sqlType) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).registerOutParameter(parameterIndex, sqlType);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void registerOutParameter(final int parameterIndex, final int sqlType, final int scale) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).registerOutParameter(parameterIndex, sqlType, scale);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public boolean wasNull() throws SQLException {
        AbstractExecuteCommand<Boolean> ec = new AbstractExecuteCommand<Boolean>(){

            @Override
            public Boolean execute(Statement t) throws SQLException {
                return ((CallableStatement)t).wasNull();
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public String getString(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<String> ec = new AbstractExecuteCommand<String>(){

            @Override
            public String execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getString(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public boolean getBoolean(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<Boolean> ec = new AbstractExecuteCommand<Boolean>(){

            @Override
            public Boolean execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getBoolean(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public byte getByte(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<Byte> ec = new AbstractExecuteCommand<Byte>(){

            @Override
            public Byte execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getByte(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public short getShort(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<Short> ec = new AbstractExecuteCommand<Short>(){

            @Override
            public Short execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getShort(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public int getInt(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getInt(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public long getLong(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<Long> ec = new AbstractExecuteCommand<Long>(){

            @Override
            public Long execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getLong(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public float getFloat(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<Float> ec = new AbstractExecuteCommand<Float>(){

            @Override
            public Float execute(Statement t) throws SQLException {
                return Float.valueOf(((CallableStatement)t).getFloat(parameterIndex));
            }
        };
        return this.executeTemplet(ec, this.getExecuteType()).floatValue();
    }

    @Override
    public double getDouble(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<Double> ec = new AbstractExecuteCommand<Double>(){

            @Override
            public Double execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getDouble(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public BigDecimal getBigDecimal(final int parameterIndex, int scale) throws SQLException {
        AbstractExecuteCommand<BigDecimal> ec = new AbstractExecuteCommand<BigDecimal>(){

            @Override
            public BigDecimal execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getBigDecimal(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public byte[] getBytes(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<byte[]> ec = new AbstractExecuteCommand<byte[]>(){

            @Override
            public byte[] execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getBytes(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Date getDate(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<Date> ec = new AbstractExecuteCommand<Date>(){

            @Override
            public Date execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getDate(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Time getTime(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<Time> ec = new AbstractExecuteCommand<Time>(){

            @Override
            public Time execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getTime(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Timestamp getTimestamp(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<Timestamp> ec = new AbstractExecuteCommand<Timestamp>(){

            @Override
            public Timestamp execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getTimestamp(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Object getObject(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Object execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getObject(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public BigDecimal getBigDecimal(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<BigDecimal> ec = new AbstractExecuteCommand<BigDecimal>(){

            @Override
            public BigDecimal execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getBigDecimal(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Object getObject(final int i, final Map<String, Class<?>> map) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Object execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getObject(i, map);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Ref getRef(final int i) throws SQLException {
        AbstractExecuteCommand<Ref> ec = new AbstractExecuteCommand<Ref>(){

            @Override
            public Ref execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getRef(i);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Blob getBlob(final int i) throws SQLException {
        AbstractExecuteCommand<Blob> ec = new AbstractExecuteCommand<Blob>(){

            @Override
            public Blob execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getBlob(i);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Clob getClob(final int i) throws SQLException {
        AbstractExecuteCommand<Clob> ec = new AbstractExecuteCommand<Clob>(){

            @Override
            public Clob execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getClob(i);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Array getArray(final int i) throws SQLException {
        AbstractExecuteCommand<Array> ec = new AbstractExecuteCommand<Array>(){

            @Override
            public Array execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getArray(i);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Date getDate(final int parameterIndex, final Calendar cal) throws SQLException {
        AbstractExecuteCommand<Date> ec = new AbstractExecuteCommand<Date>(){

            @Override
            public Date execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getDate(parameterIndex, cal);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Time getTime(final int parameterIndex, final Calendar cal) throws SQLException {
        AbstractExecuteCommand<Time> ec = new AbstractExecuteCommand<Time>(){

            @Override
            public Time execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getTime(parameterIndex, cal);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Timestamp getTimestamp(final int parameterIndex, final Calendar cal) throws SQLException {
        AbstractExecuteCommand<Timestamp> ec = new AbstractExecuteCommand<Timestamp>(){

            @Override
            public Timestamp execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getTimestamp(parameterIndex, cal);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void registerOutParameter(final int paramIndex, final int sqlType, final String typeName) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).registerOutParameter(paramIndex, sqlType, typeName);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void registerOutParameter(final String parameterName, final int sqlType) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).registerOutParameter(parameterName, sqlType);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void registerOutParameter(final String parameterName, final int sqlType, final int scale) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).registerOutParameter(parameterName, sqlType, scale);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void registerOutParameter(final String parameterName, final int sqlType, final String typeName) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).registerOutParameter(parameterName, sqlType, typeName);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public URL getURL(final int parameterIndex) throws SQLException {
        AbstractExecuteCommand<URL> ec = new AbstractExecuteCommand<URL>(){

            @Override
            public URL execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getURL(parameterIndex);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setURL(final String parameterName, final URL val) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setURL(parameterName, val);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setNull(final String parameterName, final int sqlType) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setNull(parameterName, sqlType);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setBoolean(final String parameterName, final boolean x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setBoolean(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setByte(final String parameterName, final byte x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setByte(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setShort(final String parameterName, final short x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setShort(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setInt(final String parameterName, final int x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setInt(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setLong(final String parameterName, final long x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setLong(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setFloat(final String parameterName, final float x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setFloat(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setDouble(final String parameterName, final double x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setDouble(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setBigDecimal(final String parameterName, final BigDecimal x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setBigDecimal(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setString(final String parameterName, final String x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setString(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setBytes(final String parameterName, final byte[] x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setBytes(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setDate(final String parameterName, final Date x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setDate(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setTime(final String parameterName, final Time x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setTime(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setTimestamp(final String parameterName, final Timestamp x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setTimestamp(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setAsciiStream(final String parameterName, final InputStream x, final int length) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setAsciiStream(parameterName, x, length);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setBinaryStream(final String parameterName, final InputStream x, final int length) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setBinaryStream(parameterName, x, length);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setObject(final String parameterName, final Object x, final int targetSqlType, final int scale) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setObject(parameterName, x, targetSqlType, scale);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setObject(final String parameterName, final Object x, final int targetSqlType) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setObject(parameterName, x, targetSqlType);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setObject(final String parameterName, final Object x) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setObject(parameterName, x);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setCharacterStream(final String parameterName, final Reader reader, final int length) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setCharacterStream(parameterName, reader, length);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setDate(final String parameterName, final Date x, final Calendar cal) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setDate(parameterName, x, cal);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setTime(final String parameterName, final Time x, final Calendar cal) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setTime(parameterName, x, cal);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setTimestamp(final String parameterName, final Timestamp x, final Calendar cal) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setTimestamp(parameterName, x, cal);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public void setNull(final String parameterName, final int sqlType, final String typeName) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                ((CallableStatement)t).setNull(parameterName, sqlType, typeName);
                return null;
            }
        };
        this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public String getString(final String parameterName) throws SQLException {
        AbstractExecuteCommand<String> ec = new AbstractExecuteCommand<String>(){

            @Override
            public String execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getString(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public boolean getBoolean(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Boolean> ec = new AbstractExecuteCommand<Boolean>(){

            @Override
            public Boolean execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getBoolean(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public byte getByte(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Byte> ec = new AbstractExecuteCommand<Byte>(){

            @Override
            public Byte execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getByte(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public short getShort(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Short> ec = new AbstractExecuteCommand<Short>(){

            @Override
            public Short execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getShort(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public int getInt(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Integer> ec = new AbstractExecuteCommand<Integer>(){

            @Override
            public Integer execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getInt(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public long getLong(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Long> ec = new AbstractExecuteCommand<Long>(){

            @Override
            public Long execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getLong(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public float getFloat(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Float> ec = new AbstractExecuteCommand<Float>(){

            @Override
            public Float execute(Statement t) throws SQLException {
                return Float.valueOf(((CallableStatement)t).getFloat(parameterName));
            }
        };
        return this.executeTemplet(ec, this.getExecuteType()).floatValue();
    }

    @Override
    public double getDouble(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Double> ec = new AbstractExecuteCommand<Double>(){

            @Override
            public Double execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getDouble(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public byte[] getBytes(final String parameterName) throws SQLException {
        AbstractExecuteCommand<byte[]> ec = new AbstractExecuteCommand<byte[]>(){

            @Override
            public byte[] execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getBytes(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Date getDate(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Date> ec = new AbstractExecuteCommand<Date>(){

            @Override
            public Date execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getDate(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Time getTime(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Time> ec = new AbstractExecuteCommand<Time>(){

            @Override
            public Time execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getTime(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Timestamp getTimestamp(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Timestamp> ec = new AbstractExecuteCommand<Timestamp>(){

            @Override
            public Timestamp execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getTimestamp(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Object getObject(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Object execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getObject(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public BigDecimal getBigDecimal(final String parameterName) throws SQLException {
        AbstractExecuteCommand<BigDecimal> ec = new AbstractExecuteCommand<BigDecimal>(){

            @Override
            public BigDecimal execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getBigDecimal(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Object getObject(final String parameterName, final Map<String, Class<?>> map) throws SQLException {
        AbstractExecuteCommand<Object> ec = new AbstractExecuteCommand<Object>(){

            @Override
            public Object execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getObject(parameterName, map);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Ref getRef(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Ref> ec = new AbstractExecuteCommand<Ref>(){

            @Override
            public Ref execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getRef(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Blob getBlob(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Blob> ec = new AbstractExecuteCommand<Blob>(){

            @Override
            public Blob execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getBlob(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Clob getClob(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Clob> ec = new AbstractExecuteCommand<Clob>(){

            @Override
            public Clob execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getClob(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Array getArray(final String parameterName) throws SQLException {
        AbstractExecuteCommand<Array> ec = new AbstractExecuteCommand<Array>(){

            @Override
            public Array execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getArray(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Date getDate(final String parameterName, final Calendar cal) throws SQLException {
        AbstractExecuteCommand<Date> ec = new AbstractExecuteCommand<Date>(){

            @Override
            public Date execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getDate(parameterName, cal);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Time getTime(final String parameterName, final Calendar cal) throws SQLException {
        AbstractExecuteCommand<Time> ec = new AbstractExecuteCommand<Time>(){

            @Override
            public Time execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getTime(parameterName, cal);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public Timestamp getTimestamp(final String parameterName, final Calendar cal) throws SQLException {
        AbstractExecuteCommand<Timestamp> ec = new AbstractExecuteCommand<Timestamp>(){

            @Override
            public Timestamp execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getTimestamp(parameterName, cal);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }

    @Override
    public URL getURL(final String parameterName) throws SQLException {
        AbstractExecuteCommand<URL> ec = new AbstractExecuteCommand<URL>(){

            @Override
            public URL execute(Statement t) throws SQLException {
                return ((CallableStatement)t).getURL(parameterName);
            }
        };
        return this.executeTemplet(ec, this.getExecuteType());
    }
}

