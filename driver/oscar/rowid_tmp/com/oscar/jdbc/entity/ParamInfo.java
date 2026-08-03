/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc.entity;

import com.oscar.core.Field;

public class ParamInfo {
    private long oid;
    private String name;
    private int sequence;
    private int type;
    private int inout;
    private Field field;

    public ParamInfo(long oid, String name, int sequence, int type, int inout) {
        this.name = name;
        this.sequence = sequence;
        this.type = type;
        this.oid = oid;
        this.inout = inout;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSequence() {
        return this.sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getOid() {
        return this.oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public int getInout() {
        return this.inout;
    }

    public void setInout(int inout) {
        this.inout = inout;
    }

    public Field getField() {
        return this.field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}

