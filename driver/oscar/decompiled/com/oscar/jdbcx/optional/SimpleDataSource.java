/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbcx.optional;

import com.oscar.Driver;
import com.oscar.jdbcx.optional.BaseDataSource;
import java.io.Serializable;
import javax.sql.DataSource;

public class SimpleDataSource
extends BaseDataSource
implements Serializable,
DataSource {
    public String getDescription() {
        return "Non-Pooling DataSource from " + Driver.getVersion();
    }
}

