/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.pool;

import com.oscar.dispatcher.pool.DispatchBaseDataSource;
import com.oscar.dispatcher.pool.DispatchPoolingDataSource;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class OSCARObjectFactory
implements ObjectFactory {
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception {
        Reference ref = (Reference)obj;
        if (ref.getClassName().equals(DispatchBaseDataSource.class.getName())) {
            return this.loadPoolingDataSource(ref);
        }
        return null;
    }

    private Object loadPoolingDataSource(Reference ref) {
        String max;
        String name = this.getProperty(ref, "dataSourceName");
        DispatchPoolingDataSource pds = DispatchPoolingDataSource.getDataSource(name);
        if (pds != null) {
            return pds;
        }
        pds = new DispatchPoolingDataSource();
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

    protected Object loadBaseDataSource(DispatchBaseDataSource ds, Reference ref) {
        ds.setDatabaseName(this.getProperty(ref, "databaseName"));
        ds.setPassword(this.getProperty(ref, "password"));
        String port = this.getProperty(ref, "portNumber");
        if (port != null) {
            ds.setPortNumber(Integer.parseInt(port));
        }
        ds.setServerName(this.getProperty(ref, "serverName"));
        ds.setUser(this.getProperty(ref, "user"));
        ds.setUrl(this.getProperty(ref, "url"));
        return ds;
    }

    protected String getProperty(Reference ref, String s) {
        RefAddr addr = ref.get(s);
        if (addr == null) {
            return null;
        }
        return (String)addr.getContent();
    }
}

