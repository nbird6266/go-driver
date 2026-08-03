/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbcx;

import com.oscar.Driver;
import com.oscar.jdbcx.Jdbc3ConnectionPool;
import com.oscar.jdbcx.Jdbc3ObjectFactory;
import com.oscar.jdbcx.optional.ConnectionPool;
import com.oscar.jdbcx.optional.PoolingDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.naming.Reference;

public class Jdbc3PoolingDataSource
extends PoolingDataSource {
    private static Map dataSources = new HashMap();

    static Jdbc3PoolingDataSource getDataSource(String name) {
        return (Jdbc3PoolingDataSource)dataSources.get(name);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void removeStoredDataSource() {
        Map map = dataSources;
        synchronized (map) {
            dataSources.remove(this.dataSourceName);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setDataSourceName(String dataSourceName) {
        if (this.isInitialized()) {
            throw new IllegalStateException("Cannot set Data Source properties after DataSource has been used");
        }
        if (this.dataSourceName != null && dataSourceName != null && dataSourceName.equals(this.dataSourceName)) {
            throw new IllegalArgumentException("DataSource with name '" + dataSourceName + "' already exists!");
        }
        Map map = dataSources;
        synchronized (map) {
            if (Jdbc3PoolingDataSource.getDataSource(dataSourceName) != null) {
                throw new IllegalArgumentException("DataSource with name '" + dataSourceName + "' already exists!");
            }
            if (this.dataSourceName != null) {
                dataSources.remove(this.dataSourceName);
            }
            this.dataSourceName = dataSourceName;
            dataSources.put(dataSourceName, this);
        }
    }

    protected Reference createReference() {
        return new Reference(this.getClass().getName(), Jdbc3ObjectFactory.class.getName(), null);
    }

    protected ConnectionPool createConnectionPool() {
        return new Jdbc3ConnectionPool();
    }

    public String getDescription() {
        return "JDBC3 Pooling DataSource from " + Driver.getVersion();
    }
}

