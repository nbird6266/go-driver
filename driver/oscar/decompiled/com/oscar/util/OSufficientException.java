/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.util.OSQLException;

public class OSufficientException
extends OSQLException {
    private static final long serialVersionUID = 1L;

    public OSufficientException(int errorCode, String SQLState, String errorMessage) {
        super(errorCode, SQLState, errorMessage);
    }

    public OSufficientException(String error, String SQLState, int vendorCode) {
        super(error, SQLState, vendorCode);
    }
}

