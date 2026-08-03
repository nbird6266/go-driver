/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.util.Util;

public class SSLDebug {
    private boolean timeStamp = false;
    public static final int DEBUG_CODEC = 1;
    public static final int DEBUG_MSG = 2;
    public static final int DEBUG_STATE = 4;
    public static final int DEBUG_CRYPTO = 8;
    public static final int DEBUG_INIT = 16;
    public static final int DEBUG_CERT = 32;
    public static final int DEBUG_HANDSHAKE = 64;
    public static final int DEBUG_ALL = 65535;
    static int debugVal = 0;

    public static void setDebug(int flag) {
        debugVal = flag;
    }

    public static boolean getDebug(int flag) {
        return (debugVal & flag) > 0;
    }

    public static void debug(int type, String val) {
        if ((debugVal & type) > 0) {
            String tid = Thread.currentThread().toString();
            System.out.println("Thread " + tid + val);
        }
    }

    public static void debug(int type, String label, byte[] hd) {
        if ((debugVal & type) > 0) {
            String tid = Thread.currentThread().toString();
            System.out.println("Thread " + tid);
            Util.xdump(label, hd);
        }
    }
}

