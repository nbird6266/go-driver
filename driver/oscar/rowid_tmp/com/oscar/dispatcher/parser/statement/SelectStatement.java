/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.parser.statement;

import com.oscar.dispatcher.parser.statement.AbstractStatement;

public class SelectStatement
extends AbstractStatement {
    private boolean queryLastInsertId;

    public void setQueryLastInsertId(boolean queryLastInsertId) {
        this.queryLastInsertId = queryLastInsertId;
    }

    public boolean isQueryLastInsertId() {
        return this.queryLastInsertId;
    }

    public int getSQLType() {
        return 2;
    }
}

