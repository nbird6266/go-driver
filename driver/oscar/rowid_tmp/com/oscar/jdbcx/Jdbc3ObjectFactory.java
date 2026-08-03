/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbcx;

import com.oscar.jdbcx.Jdbc3ConnectionPool;
import com.oscar.jdbcx.Jdbc3PoolingDataSource;
import com.oscar.jdbcx.Jdbc3SimpleDataSource;
import com.oscar.jdbcx.optional.OSCARObjectFactory;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;

public class Jdbc3ObjectFactory
extends OSCARObjectFactory {
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception {
        Reference ref = (Reference)obj;
        if (ref.getClassName().equals(Jdbc3SimpleDataSource.class.getName())) {
            return this.loadSimpleDataSource(ref);
        }
        if (ref.getClassName().equals(Jdbc3ConnectionPool.class.getName())) {
            return this.loadConnectionPool(ref);
        }
        if (ref.getClassName().equals(Jdbc3PoolingDataSource.class.getName())) {
            return this.loadPoolingDataSource(ref);
        }
        return null;
    }

    private Object loadPoolingDataSource(Reference ref) {
        String max;
        String name = this.getProperty(ref, "dataSourceName");
        Jdbc3PoolingDataSource pds = Jdbc3PoolingDataSource.getDataSource(name);
        if (pds != null) {
            return pds;
        }
        pds = new Jdbc3PoolingDataSource();
        pds.setDataSourceName(name);
        this.loadBaseDataSource(pds, ref);
        String min = this.getProperty(ref, "initialConnections");
        if (min != null) {
            pds.setInitialConnections(Integer.parseInt(min));
        }
        if ((max = this.getProperty(ref, "maxConnections")) != null) {
            pds.setMaxConnections(Integer.parseInt(max));
        }
        return pds;
    }

    private Object loadSimpleDataSource(Reference ref) {
        Jdbc3SimpleDataSource ds = new Jdbc3SimpleDataSource();
        return this.loadBaseDataSource(ds, ref);
    }

    private Object loadConnectionPool(Reference ref) {
        Jdbc3ConnectionPool cp = new Jdbc3ConnectionPool();
        return this.loadBaseDataSource(cp, ref);
    }
}

