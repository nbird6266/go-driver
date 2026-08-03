/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.util;

public class Bench {
    private static int size = 10;
    private static int cur = 0;
    protected static long[] accum = new long[size];
    protected static long[] mark = new long[size];
    protected static String[] names = new String[size];

    public static int register(String name) {
        Bench.names[Bench.cur] = name;
        return cur++;
    }

    public static void clear() {
        for (int i = 0; i < size; ++i) {
            Bench.accum[i] = 0L;
        }
    }

    public static void clear(int acc) {
        Bench.accum[acc] = 0L;
    }

    public static void start(int acc) {
        Bench.mark[acc] = System.currentTimeMillis();
    }

    public static void end(int acc) {
        long now = System.currentTimeMillis();
        long diff = now - mark[acc];
        int n = acc;
        accum[n] = accum[n] + (now - mark[acc]);
    }

    public static long get(int acc) {
        return accum[acc];
    }

    public static void dump() {
        Bench.dump(true);
    }

    public static void dump(boolean filter) {
        System.out.println("Timing");
        for (int i = 0; i < size; ++i) {
            if (filter && names[i] == null) continue;
            System.out.println(names[i] + "(" + i + "): " + accum[i]);
        }
    }
}

