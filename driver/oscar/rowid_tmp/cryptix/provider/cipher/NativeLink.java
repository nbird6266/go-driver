/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.cipher;

import cryptix.CryptixProperties;
import cryptix.util.core.Debug;
import cryptix.util.core.LinkStatus;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import netscape.security.PrivilegeManager;

final class NativeLink
implements LinkStatus {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("NativeLink");
    private static final PrintWriter err = Debug.getOutput();
    private static final int NOT_LOADED = 0;
    private static final int FAILED = 1;
    private static final int OK = 2;
    private static boolean native_allowed;
    private int required_major;
    private int required_minor;
    private int actual_major;
    private int actual_minor;
    private String library_name;
    private boolean want_native;
    private int status;
    private boolean lib_loaded;
    private String link_error;

    static {
        try {
            String s = CryptixProperties.getProperty("Native.Allowed");
            native_allowed = s == null || s.equalsIgnoreCase("true");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void debug(String s) {
        err.println("NativeLink: " + s);
    }

    NativeLink(String libname, int major, int minor) {
        this.required_major = major;
        this.required_minor = minor;
        this.library_name = libname;
        this.want_native = NativeLink.isNativeWanted(libname);
        this.link_error = this.want_native ? "Library is not loaded because the class has not yet been used." : "Library is not loaded because it is disabled in the properties.";
    }

    public int getRequiredMajorVersion() {
        return this.required_major;
    }

    public int getRequiredMinorVersion() {
        return this.required_minor;
    }

    public String getLibraryName() {
        return this.library_name;
    }

    public int getMajorVersion() {
        return this.actual_major;
    }

    public int getMinorVersion() {
        return this.actual_minor;
    }

    public boolean isLibraryLoaded() {
        return this.lib_loaded;
    }

    public boolean isLibraryCorrect() {
        return this.status == 2;
    }

    public String getLinkErrorString() {
        return this.link_error;
    }

    private static boolean isNativeWanted(String libname) {
        if (!native_allowed) {
            return false;
        }
        String s = CryptixProperties.getProperty("Native.Enable." + libname);
        if (s == null) {
            s = CryptixProperties.getProperty("Native.Enable.*");
        }
        return s != null && s.equalsIgnoreCase("true");
    }

    void checkVersion(int major, int minor) {
        this.actual_major = major;
        this.actual_minor = minor;
        if (major != this.required_major || minor < this.required_minor) {
            this.status = 1;
            throw new UnsatisfiedLinkError("The " + this.library_name + " native library " + this.actual_major + "." + this.actual_minor + " is too old (or new) to use. Version " + this.required_major + "." + this.required_minor + " is required.");
        }
    }

    void check(String reason) {
        if (reason != null) {
            this.status = 1;
            throw new UnsatisfiedLinkError("Unexpected result in " + this.library_name + " native library: " + reason);
        }
    }

    void fail(Throwable e) {
        this.status = 1;
        this.link_error = e.getMessage();
        if (debuglevel >= 3) {
            NativeLink.debug(String.valueOf(this.library_name) + " library was not loaded: " + this.link_error);
        }
    }

    public void checkNative() {
        if (!this.useNative()) {
            throw new UnsatisfiedLinkError(this.link_error);
        }
    }

    public boolean useNative() {
        return this.status == 2 && this.want_native;
    }

    public void setNative(boolean wanted) {
        this.want_native = wanted;
    }

    boolean attemptLoad() {
        String libpath;
        if (this.status != 0 || !this.want_native) {
            return false;
        }
        try {
            String fs = File.separator;
            libpath = String.valueOf(CryptixProperties.getLibraryPath()) + "bin" + fs;
        }
        catch (IOException e) {
            return false;
        }
        this.lib_loaded = this.attemptLoad(new String[]{String.valueOf(libpath) + this.library_name + ".dll", String.valueOf(libpath) + "lib" + this.library_name + ".so"});
        if (!this.lib_loaded) {
            throw new UnsatisfiedLinkError("The " + this.library_name + " native library could not be loaded.");
        }
        this.status = 2;
        this.link_error = null;
        return true;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private boolean attemptLoad(String[] libs) {
        if (debuglevel >= 7) {
            NativeLink.debug("entered attemptLoad(String[] libs) for " + this.library_name);
        }
        try {
            try {
                PrivilegeManager.enablePrivilege("UniversalFileRead");
            }
            catch (NoClassDefFoundError noClassDefFoundError) {
                // empty catch block
            }
            if (debuglevel >= 9) {
                int i = 0;
                while (i < libs.length) {
                    NativeLink.debug("libs[" + i + "] = \"" + libs[i] + "\"");
                    ++i;
                }
            }
            int i = 0;
            while (true) {
                block31: {
                    if (i >= libs.length) {
                        return false;
                    }
                    try {
                        File file = new File(libs[i]);
                        if (file.isFile()) {
                            if (debuglevel >= 5) {
                                NativeLink.debug(String.valueOf(libs[i]) + " exists. Attempting load.");
                            }
                            try {
                                try {
                                    PrivilegeManager.enablePrivilege("UniversalLinkAccess");
                                }
                                catch (NoClassDefFoundError noClassDefFoundError) {
                                    // empty catch block
                                }
                                System.load(libs[i]);
                            }
                            finally {
                                try {
                                    PrivilegeManager.revertPrivilege("UniversalLinkAccess");
                                }
                                catch (NoClassDefFoundError noClassDefFoundError) {}
                            }
                            if (debuglevel >= 3) {
                                NativeLink.debug(String.valueOf(this.library_name) + " library loaded successfully.");
                            }
                            return true;
                        }
                        if (debuglevel >= 6) {
                            NativeLink.debug(String.valueOf(libs[i]) + " does not exist.");
                        }
                        break block31;
                    }
                    catch (LinkageError e) {
                        this.link_error = e.toString();
                    }
                    catch (SecurityException e) {
                        this.link_error = e.toString();
                    }
                    catch (Throwable e) {
                        try {
                            PrivilegeManager.revertPrivilege("UniversalFileRead");
                        }
                        catch (NoClassDefFoundError noClassDefFoundError) {
                            // empty catch block
                        }
                        this.link_error = e.getClass().getName();
                        NativeLink.debug("unexpected exception while attempting to load " + libs[i] + ": " + this.link_error);
                    }
                    if (debuglevel >= 3) {
                        NativeLink.debug(String.valueOf(libs[i]) + " failed to load: " + this.link_error);
                    }
                }
                ++i;
            }
        }
        catch (SecurityException e) {
            this.link_error = e.toString();
            if (debuglevel >= 3) {
                NativeLink.debug(String.valueOf(this.library_name) + " library failed to load: " + this.link_error);
            }
            return false;
        }
    }
}

