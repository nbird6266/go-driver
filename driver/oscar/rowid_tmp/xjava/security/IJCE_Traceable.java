/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import xjava.security.IJCE;
import xjava.security.IJCE_Properties;

abstract class IJCE_Traceable {
    boolean tracing;
    private PrintWriter out;
    private static int indent;
    private static boolean dangling;
    private static Hashtable traced;

    static {
        traced = new Hashtable();
        String tracePrefix = "Trace.";
        int offset = tracePrefix.length();
        PrintWriter err = IJCE.getDebugOutput();
        Enumeration names = IJCE_Properties.propertyNames();
        while (names.hasMoreElements()) {
            String want;
            String obj = (String)names.nextElement();
            if (!obj.startsWith(tracePrefix) || (want = IJCE_Properties.getProperty(obj)) == null || !want.equalsIgnoreCase("true")) continue;
            traced.put(obj.substring(offset), err);
        }
    }

    IJCE_Traceable(String type) {
        PrintWriter pw = (PrintWriter)traced.get(this.getClass().getName());
        if (pw == null) {
            pw = (PrintWriter)traced.get(type);
        }
        if (pw != null) {
            this.enableTracing(pw);
        }
    }

    void enableTracing(PrintWriter out) {
        if (out == null) {
            throw new NullPointerException("out == null");
        }
        this.out = out;
        this.tracing = true;
    }

    void disableTracing() {
        this.tracing = false;
        this.out = null;
    }

    void traceVoidMethod(String s) {
        try {
            this.newline();
            this.out.println("<" + this + ">." + s);
            dangling = false;
        }
        catch (NullPointerException nullPointerException) {
            // empty catch block
        }
    }

    void traceMethod(String s) {
        try {
            this.newline();
            this.out.print("<" + this + ">." + s + " ");
            this.out.flush();
            dangling = true;
            ++indent;
        }
        catch (NullPointerException nullPointerException) {
            // empty catch block
        }
    }

    void traceResult(String s) {
        try {
            if (!dangling) {
                int i = 1;
                while (i < indent) {
                    this.out.print("    ");
                    ++i;
                }
                this.out.print("... ");
            }
            this.out.println("= " + s);
            dangling = false;
            --indent;
        }
        catch (NullPointerException nullPointerException) {
            // empty catch block
        }
    }

    void traceResult(int i) {
        this.traceResult(Integer.toString(i));
    }

    private void newline() {
        if (dangling) {
            this.out.println("...");
        }
        int i = 0;
        while (i < indent) {
            this.out.print("    ");
            ++i;
        }
    }
}

