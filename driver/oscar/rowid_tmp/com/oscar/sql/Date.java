/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.sql;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Date
extends java.sql.Date {
    private boolean BC = false;
    private static ThreadLocal<SimpleDateFormat> formaterHolder = new ThreadLocal<SimpleDateFormat>(){

        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    public static java.sql.Date valueOf(String s) {
        boolean isBC = false;
        if (s != null && s.length() >= 2) {
            if (s.substring(s.length() - 2, s.length()).equalsIgnoreCase("BC")) {
                isBC = true;
                s = s.substring(0, s.length() - 2);
            } else if (s.substring(s.length() - 2, s.length()).equalsIgnoreCase("AD")) {
                isBC = false;
                s = s.substring(0, s.length() - 2);
            }
        }
        s = s.trim();
        Date date = null;
        if (s.indexOf("-") != -1) {
            if (s.indexOf(":") != -1) {
                java.util.Date dateUtil = null;
                try {
                    dateUtil = formaterHolder.get().parse(s);
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                date = new Date(dateUtil.getTime());
            } else {
                java.sql.Date dateSql = java.sql.Date.valueOf(s);
                date = new Date(dateSql.getTime());
            }
        } else {
            date = new Date(Long.parseLong(s));
        }
        date.BC = isBC;
        return date;
    }

    public String toString() {
        return super.toString() + (this.BC ? " BC" : "");
    }

    public Date(int year, int month, int day) {
        super(year, month, day);
    }

    public Date(long date) {
        super(date);
    }

    public void setBC(boolean isBC) {
        this.BC = isBC;
    }

    public boolean isBC() {
        return this.BC;
    }
}

