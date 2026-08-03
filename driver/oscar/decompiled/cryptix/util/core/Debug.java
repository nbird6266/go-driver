/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.core;

import cryptix.CryptixProperties;
import java.io.PrintWriter;

public class Debug {
    public static final boolean GLOBAL_TRACE = true;
    public static final boolean GLOBAL_DEBUG = true;
    public static final boolean GLOBAL_DEBUG_SLOW = false;
    private static final PrintWriter err = new PrintWriter(System.err, true);

    private Debug() {
    }

    public static boolean isTraceable(String label) {
        String s = CryptixProperties.getProperty("Trace." + label);
        if (s == null) {
            return false;
        }
        return new Boolean(s);
    }

    public static int getLevel(String label) {
        String s = CryptixProperties.getProperty("Debug.Level." + label);
        if (s == null && (s = CryptixProperties.getProperty("Debug.Level.*")) == null) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int getLevel(String label1, String label2) {
        int n;
        int m = Debug.getLevel(label1);
        return m > (n = Debug.getLevel(label2)) ? m : n;
    }

    public static PrintWriter getOutput() {
        return err;
    }
}

