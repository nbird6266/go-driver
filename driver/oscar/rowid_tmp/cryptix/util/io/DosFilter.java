/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.io;

import cryptix.util.core.Debug;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;

public class DosFilter
implements FilenameFilter {
    private static final boolean DEBUG = true;
    private static int debuglevel = Debug.getLevel("DosFilter");
    private static final PrintWriter err = Debug.getOutput();
    private char[] nameMask;
    private char[] extMask;

    private static void debug(String s) {
        err.println("DosFilter: " + s);
    }

    public DosFilter() {
        this.reset();
    }

    public DosFilter(String mask) {
        this.setMask(mask);
    }

    public boolean accept(File dir, String name) {
        char c;
        if (new File(dir, name).isDirectory()) {
            return true;
        }
        if (debuglevel >= 3) {
            DosFilter.debug("filtering " + dir.getAbsolutePath() + File.separator + name);
        }
        char[] nameChars = null;
        char[] extChars = null;
        int n = name.indexOf(".");
        if (n == -1) {
            if (this.extMask != null) {
                if (debuglevel >= 4) {
                    DosFilter.debug(String.valueOf(name) + " FAILED 1\n");
                }
                return false;
            }
            nameChars = name.toCharArray();
        } else {
            nameChars = name.substring(0, n).toCharArray();
            extChars = name.substring(n + 1).toCharArray();
        }
        boolean advance = false;
        int i = 0;
        int j = 0;
        while (this.nameMask != null && i < nameChars.length) {
            c = this.nameMask[j];
            if (c == '*') {
                i = nameChars.length;
                j = this.nameMask.length;
                continue;
            }
            if (c != '?' && c != nameChars[i]) {
                if (debuglevel >= 4) {
                    DosFilter.debug(String.valueOf(name) + " FAILED 2\n");
                }
                return false;
            }
            if (++j != this.nameMask.length || ++i >= nameChars.length) continue;
            if (debuglevel >= 4) {
                DosFilter.debug(String.valueOf(name) + " FAILED 3\n");
            }
            return false;
        }
        advance = false;
        i = 0;
        j = 0;
        while (this.extMask != null && i < extChars.length) {
            c = this.extMask[j];
            if (c == '*') {
                if (debuglevel >= 4) {
                    DosFilter.debug(String.valueOf(name) + " OK 1\n");
                }
                return true;
            }
            if (c != '?' && c != extChars[i]) {
                if (debuglevel >= 4) {
                    DosFilter.debug(String.valueOf(name) + " FAILED 4\n");
                }
                return false;
            }
            if (++j != this.extMask.length || ++i >= extChars.length) continue;
            if (debuglevel >= 4) {
                DosFilter.debug(String.valueOf(name) + " FAILED 5\n");
            }
            return false;
        }
        if (debuglevel >= 4) {
            DosFilter.debug(String.valueOf(name) + " OK 2\n");
        }
        return true;
    }

    public void reset() {
        this.extMask = null;
        this.nameMask = null;
    }

    public void setMask(String mask) {
        int n;
        if (mask.startsWith(".")) {
            mask = "*" + mask;
        }
        if (mask.endsWith(".")) {
            mask = String.valueOf(mask) + "*";
        }
        if ((n = mask.indexOf(".")) == -1) {
            n = mask.indexOf("*");
            this.nameMask = n == -1 ? mask.toCharArray() : mask.substring(0, n + 1).toCharArray();
        } else {
            String s = mask.substring(0, n);
            this.nameMask = (n = s.indexOf("*")) == -1 ? s.toCharArray() : s.substring(0, n + 1).toCharArray();
            s = mask.substring(n + 2);
            n = s.indexOf("*");
            char[] cArray = this.extMask = n == -1 ? s.toCharArray() : s.substring(0, n + 1).toCharArray();
        }
        if (debuglevel >= 3) {
            DosFilter.debug("set filter file name: \"" + this.nameMask + "\"");
        }
        if (debuglevel >= 3) {
            DosFilter.debug("           file ext.: \"" + this.extMask + "\"");
        }
    }

    public String getMask() {
        return String.valueOf(this.nameMask != null ? new String(this.nameMask) : "*") + "." + (this.extMask != null ? new String(this.extMask) : "*");
    }

    public String toString() {
        return this.getMask();
    }
}

