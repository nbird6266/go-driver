/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.core.BaseConnection;
import com.oscar.core.Encoding;
import java.sql.SQLException;

public class Field {
    private int length;
    private int oid;
    private int mod;
    private String name;
    private String aliasName;
    private int nullable = 1;
    private boolean updatable = true;
    private String tableName;
    private String schemaName;
    private byte[] nameByte;
    private byte[] aliasNameByte;
    private byte[] tableNameByte;
    private byte[] schemaNameByte;
    private int oscarType = -1;
    private BaseConnection conn;
    protected Encoding encoding;
    protected Encoding clientEncoding;
    protected boolean encodingFlag = false;

    public Field() {
    }

    public Field(BaseConnection conn, String name, int oid, int length, int mod, String aliasName, String talbeName, String schemaName, byte attr) {
        this.conn = conn;
        this.name = name;
        this.oid = oid;
        this.length = length;
        this.mod = mod;
        this.tableName = talbeName;
        this.aliasName = aliasName;
        this.schemaName = schemaName;
        this.encoding = conn.getEncoding();
        this.clientEncoding = conn.getClientEncoding();
        this.encodingFlag = this.encoding == null ? true : this.encoding.equals(this.clientEncoding);
        if ((attr & 0x40) == 0) {
            this.updatable = false;
        }
        if ((attr & 0x80) == 0) {
            this.nullable = 0;
        }
    }

    public Field(BaseConnection conn, byte[] name, int oid, int length, int mod, byte[] aliasName, byte[] talbeName, byte[] schemaName, byte attr) {
        this.conn = conn;
        this.nameByte = name;
        this.oid = oid;
        this.length = length;
        this.mod = mod;
        this.tableNameByte = talbeName;
        this.aliasNameByte = aliasName;
        this.schemaNameByte = schemaName;
        this.encoding = conn.getEncoding();
        this.clientEncoding = conn.getClientEncoding();
        this.encodingFlag = this.encoding == null ? true : this.encoding.equals(this.clientEncoding);
        if ((attr & 0x40) == 0) {
            this.updatable = false;
        }
        if ((attr & 0x80) == 0) {
            this.nullable = 0;
        }
    }

    public Field(BaseConnection conn, String name, int oid, int length) {
        this.conn = conn;
        this.name = name;
        this.oid = oid;
        this.length = length;
        this.encoding = conn.getEncoding();
        this.clientEncoding = conn.getClientEncoding();
        this.encodingFlag = this.encoding == null ? true : this.encoding.equals(this.clientEncoding);
    }

    public int getOID() {
        return this.oid;
    }

    public void setOID(int id) {
        this.oid = id;
    }

    public int getMod() {
        return this.mod;
    }

    public String getName() {
        if (this.name == null && this.nameByte != null) {
            try {
                this.name = this.encodingFlag ? this.clientEncoding.decode(this.nameByte) : this.encoding.decode(this.nameByte);
            }
            catch (SQLException exp) {
                throw new Error(exp);
            }
        }
        return this.name;
    }

    public void resetName() {
        this.name = this.getAliasName();
    }

    public String getAliasName() {
        if (this.aliasName == null && this.aliasNameByte != null) {
            try {
                this.aliasName = this.encodingFlag ? this.clientEncoding.decode(this.aliasNameByte) : this.encoding.decode(this.aliasNameByte);
            }
            catch (SQLException exp) {
                throw new Error(exp);
            }
        }
        return this.aliasName;
    }

    public int getLength() {
        return this.length;
    }

    public String getDBType() throws SQLException {
        return this.conn.getDBType(this.oid);
    }

    public int getSQLType() throws SQLException {
        return this.conn.getSQLType(this.oid);
    }

    public int getOscarType() throws SQLException {
        if (this.oscarType == -1) {
            return this.conn.getOscarType(this.oid);
        }
        return this.oscarType;
    }

    public int isNullable() {
        return this.nullable;
    }

    public boolean isUpdatable() {
        return this.updatable;
    }

    public String getSchemaName() {
        if (this.schemaName == null && this.schemaNameByte != null) {
            try {
                this.schemaName = this.encodingFlag ? this.clientEncoding.decode(this.schemaNameByte) : this.encoding.decode(this.schemaNameByte);
            }
            catch (SQLException exp) {
                throw new Error(exp);
            }
        }
        return this.schemaName;
    }

    public String getTableName() {
        if (this.tableName == null && this.tableNameByte != null) {
            try {
                this.tableName = this.encodingFlag ? this.clientEncoding.decode(this.tableNameByte) : this.encoding.decode(this.tableNameByte);
            }
            catch (SQLException exp) {
                throw new Error(exp);
            }
        }
        return this.tableName;
    }
}

