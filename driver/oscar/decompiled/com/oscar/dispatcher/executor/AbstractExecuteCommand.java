/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.executor;

import com.oscar.dispatcher.executor.DispatchAbstractStatement;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class AbstractExecuteCommand<R>
implements DispatchAbstractStatement.ExecuteCommand<R> {
    @Override
    public String getFunctionName() {
        return "DispatchStatementV2.unimportFunction";
    }

    @Override
    public boolean isExecuteFunction() {
        return false;
    }
}

