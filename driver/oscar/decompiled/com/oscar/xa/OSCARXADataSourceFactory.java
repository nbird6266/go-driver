/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.xa;

import com.oscar.jdbcx.optional.OSCARObjectFactory;
import com.oscar.xa.Jdbc3XADataSource;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;

public class OSCARXADataSourceFactory
extends OSCARObjectFactory {
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception {
        Reference ref = (Reference)obj;
        String className = ref.getClassName();
        if (className.equals("com.oscar.xa.Jdbc3XADataSource")) {
            return this.loadXADataSource(ref);
        }
        return null;
    }

    private Object loadXADataSource(Reference ref) {
        Jdbc3XADataSource ds = new Jdbc3XADataSource();
        return this.loadBaseDataSource(ds, ref);
    }
}

