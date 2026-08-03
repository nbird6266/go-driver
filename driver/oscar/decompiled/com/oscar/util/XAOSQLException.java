/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import javax.transaction.xa.XAException;

public class XAOSQLException
extends XAException {
    private static final long serialVersionUID = 1L;

    public XAOSQLException() {
    }

    public XAOSQLException(int errorCode) {
        super(errorCode);
    }

    public XAOSQLException(String s) {
        super(s);
    }
}

