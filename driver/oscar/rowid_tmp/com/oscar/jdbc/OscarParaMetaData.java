/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseStatement;
import com.oscar.util.OSQLException;
import java.sql.ParameterMetaData;
import java.sql.SQLException;

public class OscarParaMetaData
implements ParameterMetaData {
    public static final int IN_MODE = 1;
    public static final int OUT_MODE = 2;
    public static final int INOUT_MODE = 3;
    public static final int UNKNOW = 4;
    private BaseStatement statement;
    private Object[][] paramInfor;

    public OscarParaMetaData(Object param, BaseStatement stmt) {
        this.statement = stmt;
        this.paramInfor = (Object[][])param;
    }

    public String getParameterClassName(int param) throws SQLException {
        int type = this.getParameterType(param);
        switch (type) {
            case -6: {
                return "java.lang.Byte";
            }
            case 5: {
                return "java.lang.Short";
            }
            case 4: {
                return "java.lang.Integer";
            }
            case -5: {
                return "java.lang.Long";
            }
            case 7: {
                return "java.lang.Float";
            }
            case 6: 
            case 8: {
                return "java.lang.Double";
            }
            case 2: 
            case 3: {
                return "java.math.BigDecimal";
            }
            case -7: 
            case 16: {
                return "java.lang.Boolean";
            }
            case -1: 
            case 1: 
            case 12: {
                return "java.lang.String";
            }
            case -4: 
            case -3: 
            case -2: {
                return "byte[]";
            }
            case 91: {
                return "java.sql.Date";
            }
            case 92: {
                return "java.sql.Time";
            }
            case 93: {
                return "java.sql.Timestamp";
            }
            case 2004: {
                return "java.sql.Blob";
            }
            case 2005: {
                return "java.sql.Clob";
            }
            case 2002: {
                return "java.sql.Struct";
            }
            case 2006: {
                return "java.sql.Ref";
            }
            case 2003: {
                return "java.sql.Array";
            }
            case 70: {
                return "java.net.URL";
            }
        }
        throw new OSQLException("OSCAR-00411", "88888", 411);
    }

    public int getParameterCount() throws SQLException {
        return this.paramInfor.length;
    }

    public int getParameterMode(int param) throws SQLException {
        if (param < 1 || param > this.paramInfor.length) {
            throw new OSQLException("OSCAR-00305", "88888", 305);
        }
        int mode = ((Byte)this.paramInfor[param - 1][5]).intValue();
        if (mode == 1) {
            return 1;
        }
        if (mode == 2) {
            return 4;
        }
        if (mode == 3) {
            return 2;
        }
        return 0;
    }

    public int getParameterType(int param) throws SQLException {
        if (param < 1 || param > this.paramInfor.length) {
            throw new OSQLException("OSCAR-00305", "88888", 305);
        }
        return this.statement.getDBConnection().getSQLType((Integer)this.paramInfor[param - 1][1]);
    }

    public String getParameterTypeName(int param) throws SQLException {
        if (param < 1 || param > this.paramInfor.length) {
            throw new OSQLException("OSCAR-00305", "88888", 305);
        }
        return this.statement.getDBConnection().getDBType((Integer)this.paramInfor[param - 1][1]);
    }

    public int getPrecision(int param) throws SQLException {
        int sql_type = this.getParameterType(param);
        switch (sql_type) {
            case -6: {
                return 3;
            }
            case 5: {
                return 5;
            }
            case 4: {
                return 10;
            }
            case -5: {
                return 19;
            }
            case 7: {
                return 8;
            }
            case 6: {
                return 15;
            }
            case 8: {
                return 15;
            }
            case -7: 
            case 16: {
                return 0;
            }
            case 2: 
            case 3: {
                return (0xFFFF0000 & (Integer)this.paramInfor[param - 1][3]) >> 16;
            }
            case -4: 
            case -3: 
            case -2: 
            case -1: 
            case 1: 
            case 12: {
                return (0xFFFF & (Integer)this.paramInfor[param - 1][3]) - 4;
            }
            case 2004: 
            case 2005: {
                return 0;
            }
            case 91: {
                return 8;
            }
            case 92: {
                return 15;
            }
            case 93: {
                return 23;
            }
        }
        return 0;
    }

    public int getScale(int param) throws SQLException {
        int sql_type = this.getParameterType(param);
        switch (sql_type) {
            case 2: 
            case 3: {
                return (0xFFFF & (Integer)this.paramInfor[param - 1][3]) - 4;
            }
        }
        return 0;
    }

    public boolean isSigned(int param) throws SQLException {
        int sql_type = this.getParameterType(param);
        switch (sql_type) {
            case -6: 
            case -5: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 7: 
            case 8: {
                return true;
            }
            case 91: 
            case 92: 
            case 93: {
                return false;
            }
        }
        return false;
    }

    public int isNullable(int param) throws SQLException {
        int nullable = ((Byte)this.paramInfor[param - 1][4]).intValue();
        if (nullable == 121) {
            return 1;
        }
        if (nullable == 110) {
            return 0;
        }
        return 2;
    }

    public int getIndex(String name) throws SQLException {
        for (int i = 0; i < this.paramInfor.length; ++i) {
            if (!this.statement.getDBConnection().getEncoding().decode((byte[])this.paramInfor[i][0]).equalsIgnoreCase(name)) continue;
            return i + 1;
        }
        throw new OSQLException("OSCAR-00304", "88888", 304);
    }

    public void addReturnParam(Object[] returnParam) throws SQLException {
        int i;
        Object[][] temp = new Object[this.paramInfor.length][];
        for (i = 0; i < this.paramInfor.length; ++i) {
            temp[i] = this.paramInfor[i];
        }
        this.paramInfor = new Object[this.paramInfor.length + 1][];
        this.paramInfor[0] = returnParam;
        for (i = 1; i < this.paramInfor.length; ++i) {
            this.paramInfor[i] = temp[i - 1];
        }
    }

    public int getParameterRelOid(int param) throws OSQLException {
        int ret = 0;
        if (param < 1 || param > this.paramInfor.length) {
            throw new OSQLException("OSCAR-00305", "88888", 305);
        }
        if (this.statement.getDBConnection().getVersion().isNewParamInfoPacket()) {
            ret = (Integer)this.paramInfor[param - 1][6];
        }
        return ret;
    }

    public int getParameterRelColIndex(int param) throws OSQLException {
        int ret = 0;
        if (param < 1 || param > this.paramInfor.length) {
            throw new OSQLException("OSCAR-00305", "88888", 305);
        }
        if (this.statement.getDBConnection().getVersion().isNewParamInfoPacket()) {
            ret = (Integer)this.paramInfor[param - 1][7];
        }
        return ret;
    }

    public Object[][] getParamInfor() {
        return this.paramInfor;
    }
}

