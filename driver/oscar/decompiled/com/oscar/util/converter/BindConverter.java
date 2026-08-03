/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util.converter;

import com.oscar.core.Encoding;
import com.oscar.util.converter.BooleanConverter;
import com.oscar.util.converter.ByteConverter;
import com.oscar.util.converter.DateConverter;
import com.oscar.util.converter.NumberConverter;
import com.oscar.util.converter.TimeConverter;
import com.oscar.util.converter.TimestampConverter;
import com.oscar.util.converter.TimestamptzConverter;
import com.oscar.util.converter.TimetzConverter;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

public class BindConverter {
    public static void convertBindDatas(Object[] m_binds, int[] m_bindTypes, Encoding encoding) throws SQLException {
        byte[] tmpBytes = null;
        block10: for (int i = 0; i < m_bindTypes.length; ++i) {
            if (m_binds[i] == null || m_binds[i] instanceof byte[]) continue;
            switch (m_bindTypes[i]) {
                case 25: {
                    m_binds[i] = DateConverter.convertDateToBytes((Date)m_binds[i]);
                    continue block10;
                }
                case 23: {
                    if (m_binds[i] instanceof Integer) {
                        m_binds[i] = NumberConverter.convertIntToBytes((Integer)m_binds[i]);
                        continue block10;
                    }
                    if (m_binds[i] instanceof Short) {
                        m_binds[i] = NumberConverter.convertIntToBytes(((Short)m_binds[i]).intValue());
                        continue block10;
                    }
                    if (m_binds[i] instanceof Long) {
                        m_binds[i] = NumberConverter.convertLongToBytes((Long)m_binds[i]);
                        continue block10;
                    }
                    if (m_binds[i] instanceof Double) {
                        m_binds[i] = NumberConverter.convertDoubleToBytes((Double)m_binds[i]);
                        continue block10;
                    }
                    m_binds[i] = NumberConverter.convertDoubleToBytes(Double.parseDouble(m_binds[i].toString()));
                    continue block10;
                }
                case 26: {
                    m_binds[i] = TimeConverter.convertTimeToBytes((Time)m_binds[i]);
                    continue block10;
                }
                case 28: {
                    m_binds[i] = TimestampConverter.convertTimestampToBytes((Timestamp)m_binds[i]);
                    continue block10;
                }
                case 29: {
                    m_binds[i] = TimestamptzConverter.convertTimestamptzToBytes((Timestamp)m_binds[i]);
                    continue block10;
                }
                case 27: {
                    m_binds[i] = TimetzConverter.convertTimetzToBytes((Time)m_binds[i]);
                    continue block10;
                }
                case 33: {
                    m_binds[i] = BooleanConverter.convertBooleanToBytes(Boolean.parseBoolean(m_binds[i].toString()));
                    continue block10;
                }
                case 50: 
                case 51: 
                case 52: {
                    tmpBytes = encoding.encode(m_binds[i].toString());
                    m_binds[i] = ByteConverter.convertByteArr(tmpBytes);
                    continue block10;
                }
                default: {
                    tmpBytes = encoding.encode(m_binds[i].toString());
                    m_binds[i] = ByteConverter.convertByteArr(tmpBytes);
                }
            }
        }
    }
}

