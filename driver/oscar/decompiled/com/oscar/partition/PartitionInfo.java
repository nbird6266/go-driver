/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.partition;

import java.util.ArrayList;
import java.util.Hashtable;

public class PartitionInfo {
    public static final int PARTLEVEL0 = 0;
    public static final int PARTLEVEL1 = 1;
    public static final int PARTLEVEL2 = 2;
    public static final int TABLETYPE_KSTORE = 1;
    public static final int TABLETYPE_OTHER = 0;
    private int partitionLevel = 0;
    private String partType;
    private String subPartType;
    private ArrayList partNames;
    private Hashtable subPartNames;
    private int tableType;

    public int getPartitionLevel() {
        return this.partitionLevel;
    }

    public void setPartitionLevel(int partitionLevel) {
        this.partitionLevel = partitionLevel;
    }

    public String getPartType() {
        return this.partType;
    }

    public void setPartType(String partType) {
        this.partType = partType;
    }

    public String getSubPartType() {
        return this.subPartType;
    }

    public void setSubPartType(String subPartType) {
        this.subPartType = subPartType;
    }

    public ArrayList getPartNames() {
        return this.partNames;
    }

    public void setPartNames(ArrayList partNames) {
        this.partNames = partNames;
    }

    public Hashtable getSubPartNames() {
        return this.subPartNames;
    }

    public void setSubPartNames(Hashtable subPartNames) {
        this.subPartNames = subPartNames;
    }

    public void setTableType(int tableType) {
        this.tableType = tableType;
    }

    public int getTableType() {
        return this.tableType;
    }
}

