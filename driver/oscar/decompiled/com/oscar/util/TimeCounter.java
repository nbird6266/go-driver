/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import java.util.Calendar;

public class TimeCounter {
    private long start;
    private long end;

    public void begin() {
        this.start = Calendar.getInstance().getTimeInMillis();
    }

    public void end() {
        this.end = Calendar.getInstance().getTimeInMillis();
    }

    public long getCost() {
        return this.end - this.start;
    }
}

