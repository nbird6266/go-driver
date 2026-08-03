/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.core.BaseResultSet;
import com.oscar.core.Field;
import com.oscar.util.OSQLException;
import com.oscar.util.TypeConverter;
import com.oscar.util.converter.BooleanConverter;
import com.oscar.util.converter.DateConverter;
import com.oscar.util.converter.NumberConverter;
import com.oscar.util.converter.TimeConverter;
import com.oscar.util.converter.TimestampConverter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;

public class Array
implements java.sql.Array {
    private BaseConnection conn = null;
    private Field field = null;
    private BaseResultSet rs;
    private int idx = 0;
    private String rawString = null;

    public Array(BaseConnection conn, int idx, Field field, BaseResultSet rs) throws SQLException {
        this.conn = conn;
        this.field = field;
        this.rs = rs;
        this.idx = idx;
        this.rawString = rs.getString(idx);
    }

    public Object getArray() throws SQLException {
        return this.getArray(1L, 0, (Map)null);
    }

    public Object getArray(long index, int count) throws SQLException {
        return this.getArray(index, count, (Map)null);
    }

    public Object getArray(Map map) throws SQLException {
        return this.getArray(1L, 0, map);
    }

    public Object getArray(long index, int count, Map map) throws SQLException {
        if (map != null) {
            throw Driver.notImplemented();
        }
        if (index < 1L) {
            throw new OSQLException("OSCAR-00304", "88888", 304);
        }
        Object[] retVal = null;
        ArrayList<String> array = new ArrayList<String>();
        if (this.rawString != null && !this.rawString.equals("{}")) {
            char[] chars = this.rawString.toCharArray();
            StringBuffer sbuf = new StringBuffer();
            boolean foundOpen = false;
            boolean insideString = false;
            for (int i = 0; i < chars.length; ++i) {
                if (chars[i] == '\\') {
                    ++i;
                }
                if (chars[i] == '{') {
                    if (foundOpen) {
                        throw Driver.notImplemented();
                    }
                    foundOpen = true;
                    continue;
                }
                if (chars[i] == '\"') {
                    insideString = !insideString;
                    continue;
                }
                if (!insideString && chars[i] == ',' || chars[i] == '}' || i == chars.length - 1) {
                    if (chars[i] != '\"' && chars[i] != '}' && chars[i] != ',') {
                        sbuf.append(chars[i]);
                    }
                    array.add(sbuf.toString());
                    sbuf = new StringBuffer();
                    continue;
                }
                sbuf.append(chars[i]);
            }
        }
        String[] arrayContents = array.toArray(new String[array.size()]);
        if (count == 0) {
            count = arrayContents.length;
        }
        if (--index + (long)count > (long)arrayContents.length) {
            throw new OSQLException("OSCAR-00304", "888880", 304);
        }
        int i = 0;
        switch (this.getBaseType()) {
            case -7: 
            case 16: {
                retVal = new boolean[count];
                while (count > 0) {
                    retVal[i++] = TypeConverter.toBoolean(arrayContents[(int)index++], -7);
                    --count;
                }
                break;
            }
            case 4: 
            case 5: {
                retVal = new int[count];
                while (count > 0) {
                    ((int[])retVal)[i++] = TypeConverter.toInt(arrayContents[(int)index++], 4);
                    --count;
                }
                break;
            }
            case -5: {
                retVal = new long[count];
                while (count > 0) {
                    ((long[])retVal)[i++] = TypeConverter.toLong(arrayContents[(int)index++], -5);
                    --count;
                }
                break;
            }
            case 2: 
            case 3: {
                retVal = new BigDecimal[count];
                while (count > 0) {
                    ((BigDecimal[])retVal)[i++] = TypeConverter.toBigDecimal(arrayContents[(int)index++], 2);
                    --count;
                }
                break;
            }
            case 7: {
                retVal = new float[count];
                while (count > 0) {
                    ((float[])retVal)[i++] = TypeConverter.toFloat(arrayContents[(int)index++], 7);
                    --count;
                }
                break;
            }
            case 8: {
                retVal = new double[count];
                while (count > 0) {
                    ((double[])retVal)[i++] = TypeConverter.toDouble(arrayContents[(int)index++], 8);
                    --count;
                }
                break;
            }
            case 1: 
            case 12: {
                retVal = new String[count];
                while (count > 0) {
                    ((String[])retVal)[i++] = arrayContents[(int)index++];
                    --count;
                }
                break;
            }
            case 91: {
                retVal = new Date[count];
                while (count > 0) {
                    ((Date[])retVal)[i++] = TypeConverter.toDate(arrayContents[(int)index++], 91);
                    --count;
                }
                break;
            }
            case 92: {
                retVal = new Time[count];
                while (count > 0) {
                    ((Time[])retVal)[i++] = TypeConverter.toTime(arrayContents[(int)index++], 92);
                    --count;
                }
                break;
            }
            case 93: {
                retVal = new Timestamp[count];
                while (count > 0) {
                    ((Timestamp[])retVal)[i++] = TypeConverter.toTimestamp(arrayContents[(int)index++], 93);
                    --count;
                }
                break;
            }
            default: {
                throw Driver.notImplemented();
            }
        }
        return retVal;
    }

    public int getBaseType() throws SQLException {
        return this.conn.getSQLType(this.getBaseTypeName());
    }

    public String getBaseTypeName() throws SQLException {
        String fType = this.field.getDBType();
        if (fType.charAt(0) == '_') {
            fType = fType.substring(1);
        }
        return fType;
    }

    public ResultSet getResultSet() throws SQLException {
        return this.getResultSet(1L, 0, (Map)null);
    }

    public ResultSet getResultSet(long index, int count) throws SQLException {
        return this.getResultSet(index, count, (Map)null);
    }

    public ResultSet getResultSet(Map map) throws SQLException {
        return this.getResultSet(1L, 0, map);
    }

    public ResultSet getResultSet(long index, int count, Map map) throws SQLException {
        ResultSet rs = null;
        rs = this.conn.getProtocolVersion().getProtocolType() >= 2 ? this.getResultSetV2(index, count, map) : this.getResultSetV1(index, count, map);
        return rs;
    }

    private ResultSet getResultSetV1(long index, int count, Map map) throws SQLException {
        Object array = this.getArray(index, count, map);
        ArrayList<byte[][]> rows = new ArrayList<byte[][]>();
        Field[] fields = new Field[2];
        fields[0] = new Field(this.conn, "INDEX", this.conn.getDBTypeOid("INT2"), 2);
        switch (this.getBaseType()) {
            case -7: {
                boolean[] booleanArray = (boolean[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("BOOL"), 1);
                for (int i = 0; i < booleanArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = this.conn.getEncoding().encode(Integer.toString((int)index + i));
                    tuple[1] = this.conn.getEncoding().encode(booleanArray[i] ? "true" : "false");
                    rows.add(tuple);
                }
                break;
            }
            case 5: {
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("INT2"), 2);
                break;
            }
            case 4: {
                int[] intArray = (int[])array;
                if (fields[1] == null) {
                    fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("INT4"), 4);
                }
                for (int i = 0; i < intArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = this.conn.getEncoding().encode(Integer.toString((int)index + i));
                    tuple[1] = this.conn.getEncoding().encode(Integer.toString(intArray[i]));
                    rows.add(tuple);
                }
                break;
            }
            case -5: {
                long[] longArray = (long[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("INT8"), 8);
                for (int i = 0; i < longArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = this.conn.getEncoding().encode(Integer.toString((int)index + i));
                    tuple[1] = this.conn.getEncoding().encode(Long.toString(longArray[i]));
                    rows.add(tuple);
                }
                break;
            }
            case 2: {
                BigDecimal[] bdArray = (BigDecimal[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("NUMERIC"), -1);
                for (int i = 0; i < bdArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = this.conn.getEncoding().encode(Integer.toString((int)index + i));
                    tuple[1] = this.conn.getEncoding().encode(bdArray[i].toString());
                    rows.add(tuple);
                }
                break;
            }
            case 7: {
                float[] floatArray = (float[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("FLOAT4"), 4);
                for (int i = 0; i < floatArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = this.conn.getEncoding().encode(Integer.toString((int)index + i));
                    tuple[1] = this.conn.getEncoding().encode(Float.toString(floatArray[i]));
                    rows.add(tuple);
                }
                break;
            }
            case 8: {
                double[] doubleArray = (double[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("FLOAT8"), 8);
                for (int i = 0; i < doubleArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = this.conn.getEncoding().encode(Integer.toString((int)index + i));
                    tuple[1] = this.conn.getEncoding().encode(Double.toString(doubleArray[i]));
                    rows.add(tuple);
                }
                break;
            }
            case 1: {
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("CHAR"), 1);
                break;
            }
            case 12: {
                String[] strArray = (String[])array;
                if (fields[1] == null) {
                    fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("VARCHAR"), -1);
                }
                for (int i = 0; i < strArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = this.conn.getEncoding().encode(Integer.toString((int)index + i));
                    tuple[1] = this.conn.getEncoding().encode(strArray[i]);
                    rows.add(tuple);
                }
                break;
            }
            case 91: {
                Date[] dateArray = (Date[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("DATE"), 4);
                for (int i = 0; i < dateArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = this.conn.getEncoding().encode(Integer.toString((int)index + i));
                    tuple[1] = this.conn.getEncoding().encode(dateArray[i].toString());
                    rows.add(tuple);
                }
                break;
            }
            case 92: {
                Time[] timeArray = (Time[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("TIME"), 8);
                for (int i = 0; i < timeArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = this.conn.getEncoding().encode(Integer.toString((int)index + i));
                    tuple[1] = this.conn.getEncoding().encode(timeArray[i].toString());
                    rows.add(tuple);
                }
                break;
            }
            case 93: {
                Timestamp[] timestampArray = (Timestamp[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("TIMESTAMP"), 8);
                for (int i = 0; i < timestampArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = this.conn.getEncoding().encode(Integer.toString((int)index + i));
                    tuple[1] = this.conn.getEncoding().encode(timestampArray[i].toString());
                    rows.add(tuple);
                }
                break;
            }
            default: {
                throw Driver.notImplemented();
            }
        }
        return this.conn.getDefaultStatement().createResultSet(fields, rows, "OK", 1, 0L);
    }

    private ResultSet getResultSetV2(long index, int count, Map map) throws SQLException {
        Object array = this.getArray(index, count, map);
        ArrayList<byte[][]> rows = new ArrayList<byte[][]>();
        Field[] fields = new Field[2];
        fields[0] = new Field(this.conn, "INDEX", this.conn.getDBTypeOid("INT2"), 2);
        switch (this.getBaseType()) {
            case -7: {
                boolean[] booleanArray = (boolean[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("BOOL"), 1);
                for (int i = 0; i < booleanArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = NumberConverter.convertIntToBytes((int)index + i);
                    tuple[1] = BooleanConverter.convertBooleanToBytes(booleanArray[i]);
                    rows.add(tuple);
                }
                break;
            }
            case 5: {
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("INT2"), 2);
                break;
            }
            case 4: {
                int[] intArray = (int[])array;
                if (fields[1] == null) {
                    fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("INT4"), 4);
                }
                for (int i = 0; i < intArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = NumberConverter.convertIntToBytes((int)index + i);
                    tuple[1] = NumberConverter.convertIntToBytes(intArray[i]);
                    rows.add(tuple);
                }
                break;
            }
            case -5: {
                long[] longArray = (long[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("INT8"), 8);
                for (int i = 0; i < longArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = NumberConverter.convertIntToBytes((int)index + i);
                    tuple[1] = NumberConverter.convertLongToBytes(longArray[i]);
                    rows.add(tuple);
                }
                break;
            }
            case 2: {
                BigDecimal[] bdArray = (BigDecimal[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("NUMERIC"), -1);
                for (int i = 0; i < bdArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = NumberConverter.convertIntToBytes((int)index + i);
                    tuple[1] = this.conn.getEncoding().encode(bdArray[i].toString());
                    rows.add(tuple);
                }
                break;
            }
            case 7: {
                float[] floatArray = (float[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("FLOAT4"), 4);
                for (int i = 0; i < floatArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = NumberConverter.convertIntToBytes((int)index + i);
                    tuple[1] = NumberConverter.convertDoubleToBytes(Double.parseDouble(Float.toString(floatArray[i])));
                    rows.add(tuple);
                }
                break;
            }
            case 8: {
                double[] doubleArray = (double[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("FLOAT8"), 8);
                for (int i = 0; i < doubleArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = NumberConverter.convertIntToBytes((int)index + i);
                    tuple[1] = NumberConverter.convertDoubleToBytes(doubleArray[i]);
                    rows.add(tuple);
                }
                break;
            }
            case 1: {
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("CHAR"), 1);
                break;
            }
            case 12: {
                String[] strArray = (String[])array;
                if (fields[1] == null) {
                    fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("VARCHAR"), -1);
                }
                for (int i = 0; i < strArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = NumberConverter.convertIntToBytes((int)index + i);
                    tuple[1] = this.conn.getEncoding().encode(strArray[i]);
                    rows.add(tuple);
                }
                break;
            }
            case 91: {
                Date[] dateArray = (Date[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("DATE"), 4);
                for (int i = 0; i < dateArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = NumberConverter.convertIntToBytes((int)index + i);
                    tuple[1] = DateConverter.convertDateToBytes(dateArray[i]);
                    rows.add(tuple);
                }
                break;
            }
            case 92: {
                Time[] timeArray = (Time[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("TIME"), 8);
                for (int i = 0; i < timeArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = NumberConverter.convertIntToBytes((int)index + i);
                    tuple[1] = TimeConverter.convertTimeToBytes(timeArray[i]);
                    rows.add(tuple);
                }
                break;
            }
            case 93: {
                Timestamp[] timestampArray = (Timestamp[])array;
                fields[1] = new Field(this.conn, "VALUE", this.conn.getDBTypeOid("TIMESTAMP"), 8);
                for (int i = 0; i < timestampArray.length; ++i) {
                    byte[][] tuple = new byte[2][0];
                    tuple[0] = NumberConverter.convertIntToBytes((int)index + i);
                    tuple[1] = TimestampConverter.convertTimestampToBytes(timestampArray[i]);
                    rows.add(tuple);
                }
                break;
            }
            default: {
                throw Driver.notImplemented();
            }
        }
        return this.conn.getDefaultStatement().createResultSet(fields, rows, "OK", 1, 0L);
    }

    public String toString() {
        return this.rawString;
    }
}

