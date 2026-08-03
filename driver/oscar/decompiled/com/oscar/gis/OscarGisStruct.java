/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.gis;

import java.sql.SQLException;
import java.sql.Struct;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class OscarGisStruct
implements Struct {
    Object[] obj = new Object[1];

    public OscarGisStruct(String value) {
        this.obj[0] = value;
    }

    @Override
    public String getSQLTypeName() throws SQLException {
        return "GEOMETRY";
    }

    @Override
    public Object[] getAttributes() throws SQLException {
        return this.obj;
    }

    @Override
    public Object[] getAttributes(Map<String, Class<?>> paramMap) throws SQLException {
        Object[] arrayOfObject = this.getAttributes();
        return arrayOfObject;
    }
}

