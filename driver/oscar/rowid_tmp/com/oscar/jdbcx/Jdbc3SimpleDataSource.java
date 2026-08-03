/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbcx;

import com.oscar.Driver;
import com.oscar.jdbcx.Jdbc3ObjectFactory;
import com.oscar.jdbcx.optional.SimpleDataSource;
import javax.naming.Reference;

public class Jdbc3SimpleDataSource
extends SimpleDataSource {
    protected Reference createReference() {
        return new Reference(this.getClass().getName(), Jdbc3ObjectFactory.class.getName(), null);
    }

    public String getDescription() {
        return "JDBC3 Non-Pooling DataSource from " + Driver.getVersion();
    }
}

