/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.pool;

import com.oscar.Driver;
import com.oscar.dispatcher.pool.DispatchPoolingDataSource;
import java.util.HashMap;
import java.util.Map;

public class DispatchJdbc3PoolingDataSource
extends DispatchPoolingDataSource {
    private static Map dataSources = new HashMap();

    static DispatchPoolingDataSource getDataSource(String name) {
        return (DispatchJdbc3PoolingDataSource)dataSources.get(name);
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
            return;
        }
        Map map = dataSources;
        synchronized (map) {
            if (DispatchJdbc3PoolingDataSource.getDataSource(dataSourceName) != null) {
                throw new IllegalArgumentException("DataSource with name '" + dataSourceName + "' already exists!");
            }
            if (this.dataSourceName != null) {
                dataSources.remove(this.dataSourceName);
            }
            this.dataSourceName = dataSourceName;
            dataSources.put(dataSourceName, this);
        }
    }

    public String getDescription() {
        return "JDBC3 Pooling DataSource from " + Driver.getVersion();
    }
}

