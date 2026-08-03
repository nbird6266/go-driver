/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

public class ImportDataContainer {
    public static final int String_type = 1;
    public static final int Binary_type = 2;
    public static final int Object_type = 4;
    private int dataType;
    private Object data;

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getDataType() {
        return this.dataType;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }
}

