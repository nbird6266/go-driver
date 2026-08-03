/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseResultSet;
import com.oscar.core.Field;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.util.OSQLException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class OscarResultSetMetaData
implements ResultSetMetaData {
    private Field[] fields;
    private Set<Integer> isAutoIncrementSet;
    protected OscarJdbc2Connection connection;

    public OscarResultSetMetaData(Field[] fields) {
        this.fields = fields == null ? new Field[0] : fields;
    }

    public OscarResultSetMetaData(Field[] fields, OscarJdbc2Connection connection) {
        this.fields = fields == null ? new Field[0] : fields;
        this.connection = connection;
    }

    public int getColumnCount() throws SQLException {
        return this.fields.length;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isAutoIncrement(int column) throws SQLException {
        String columnName;
        if (this.isAutoIncrementSet == null) {
            String columnName2;
            String tableName;
            String schemaName;
            int i;
            this.isAutoIncrementSet = new HashSet<Integer>();
            StringBuffer sql = new StringBuffer();
            sql.append("select type_name,column_def,case ");
            for (i = 0; i < this.fields.length; ++i) {
                schemaName = this.fields[i].getSchemaName();
                tableName = this.fields[i].getTableName();
                columnName2 = this.fields[i].getName();
                sql.append("when column_name='").append(columnName2);
                if (tableName != null && tableName.length() > 0) {
                    sql.append("' and table_name='").append(tableName);
                }
                if (schemaName != null && schemaName.length() > 0) {
                    sql.append("' and table_schem='").append(schemaName);
                }
                sql.append("'");
                sql.append(" then '").append(i + 1).append("' ");
            }
            sql.append(" end from v_sys_columns where ");
            for (i = 0; i < this.fields.length; ++i) {
                if (i != 0) {
                    sql.append(" or ");
                }
                schemaName = this.fields[i].getSchemaName();
                tableName = this.fields[i].getTableName();
                columnName2 = this.fields[i].getName();
                sql.append("(column_name='").append(columnName2);
                if (tableName != null && tableName.length() > 0) {
                    sql.append("' and table_name='").append(tableName);
                }
                if (schemaName != null && schemaName.length() > 0) {
                    sql.append("' and table_schem='").append(schemaName);
                }
                sql.append("')");
            }
            BaseResultSet rs = null;
            try {
                rs = this.connection.execSQL(sql.toString());
                while (rs.next()) {
                    String typeName = rs.getString("TYPE_NAME");
                    String columnDef = rs.getString("COLUMN_DEF");
                    int index = rs.getInt("CASE");
                    if (typeName == null || columnDef == null || !columnDef.startsWith("NEXTVAL") || !typeName.equals("INT4") && !typeName.equals("INT8")) continue;
                    this.isAutoIncrementSet.add(index);
                }
            }
            catch (SQLException e) {
                boolean bl = false;
                return bl;
            }
            finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
        if ((columnName = this.getField(column).getName()) == null || columnName.length() <= 0) {
            return false;
        }
        return this.isAutoIncrementSet.contains(column);
    }

    public boolean isCaseSensitive(int column) throws SQLException {
        int sql_type = this.getField(column).getSQLType();
        switch (sql_type) {
            case -7: 
            case -6: 
            case -5: 
            case -4: 
            case -3: 
            case -2: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 7: 
            case 8: 
            case 16: 
            case 91: 
            case 92: 
            case 93: 
            case 2004: {
                return false;
            }
            case -1: 
            case 1: 
            case 12: 
            case 2005: {
                return true;
            }
        }
        return true;
    }

    public boolean isSearchable(int column) throws SQLException {
        return true;
    }

    public boolean isCurrency(int column) throws SQLException {
        return false;
    }

    public int isNullable(int column) throws SQLException {
        return this.getField(column).isNullable();
    }

    public boolean isSigned(int column) throws SQLException {
        int sql_type = this.getField(column).getSQLType();
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

    public int getColumnDisplaySize(int column) throws SQLException {
        Field f = this.getField(column);
        int typmod = f.getMod();
        switch (f.getSQLType()) {
            case -6: {
                return 4;
            }
            case 5: {
                return 6;
            }
            case 4: {
                return 11;
            }
            case -5: {
                return 20;
            }
            case 7: {
                return 14;
            }
            case 6: 
            case 8: {
                return 24;
            }
            case -7: {
                if (typmod == -1) {
                    return 1;
                }
                return typmod;
            }
            case 16: {
                return 1;
            }
            case 2: 
            case 3: {
                int size = typmod >> 16 & 0xFFFF;
                return size + 2;
            }
            case 1: 
            case 12: {
                if (typmod == -1) {
                    return 8000;
                }
                return typmod - 4;
            }
            case -1: {
                return 8000;
            }
            case -3: 
            case -2: {
                if (typmod == -1) {
                    return 1;
                }
                return typmod - 4;
            }
            case -4: {
                return 8000;
            }
            case 2004: 
            case 2005: {
                return Integer.MAX_VALUE;
            }
            case 91: {
                return 10;
            }
            case 92: {
                if (typmod == -1) {
                    return 18;
                }
                return typmod;
            }
            case 93: {
                if (typmod == -1) {
                    return 29;
                }
                return typmod;
            }
        }
        return 0;
    }

    public String getColumnLabel(int column) throws SQLException {
        return this.getField(column).getAliasName();
    }

    public String getColumnName(int column) throws SQLException {
        return this.getColumnLabel(column);
    }

    public String getSchemaName(int column) throws SQLException {
        return this.getField(column).getSchemaName();
    }

    public int getPrecision(int column) throws SQLException {
        Field f = this.getField(column);
        int sql_type = f.getSQLType();
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
            case 6: 
            case 8: {
                return 15;
            }
            case -7: {
                return f.getMod();
            }
            case 16: {
                return 0;
            }
            case 2: 
            case 3: {
                if (f.getMod() == -1) {
                    return 18;
                }
                return (0xFFFF0000 & f.getMod()) >> 16;
            }
            case 1: 
            case 12: {
                if (1186 == f.getOID() || 1188 == f.getOID()) {
                    return (0xFF00 & f.getMod()) >> 8;
                }
                return this.getColumnDisplaySize(column);
            }
            case -3: 
            case -2: {
                return this.getColumnDisplaySize(column);
            }
            case -4: 
            case -1: 
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

    public int getScale(int column) throws SQLException {
        Field f = this.getField(column);
        int sql_type = f.getSQLType();
        switch (sql_type) {
            case 2: 
            case 3: {
                if (f.getMod() == -1) {
                    return 0;
                }
                return 0xFFFF & f.getMod() - 4;
            }
            case 1188: {
                return 0xFF & f.getMod();
            }
        }
        if (1188 == f.getOID()) {
            return 0xFF & f.getMod();
        }
        return 0;
    }

    public String getTableName(int column) throws SQLException {
        return this.getField(column).getTableName();
    }

    public String getCatalogName(int column) throws SQLException {
        return "";
    }

    public int getColumnType(int column) throws SQLException {
        return this.getField(column).getSQLType();
    }

    public String getColumnTypeName(int column) throws SQLException {
        return this.getField(column).getDBType();
    }

    public boolean isReadOnly(int column) throws SQLException {
        return !this.isWritable(column);
    }

    public boolean isWritable(int column) throws SQLException {
        return this.getField(column).isUpdatable();
    }

    public boolean isDefinitelyWritable(int column) throws SQLException {
        return false;
    }

    protected Field getField(int columnIndex) throws SQLException {
        if (columnIndex < 1 || columnIndex > this.fields.length) {
            throw new OSQLException("OSCAR-00305", "88888", 305);
        }
        return this.fields[columnIndex - 1];
    }

    public String getColumnClassName(int column) throws SQLException {
        switch (this.getField(column).getSQLType()) {
            case -6: 
            case 4: 
            case 5: {
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
                return "[B";
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
}

