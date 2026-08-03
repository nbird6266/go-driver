/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;
import netscape.security.PrivilegeManager;

class IJCE_Properties {
    static final String PRODUCT_NAME = "IJCE";
    static final String LIB_DIRNAME = "ijce-lib";
    static final String[] PROPERTIES_FILES = new String[]{"IJCE.properties"};
    private static final Properties properties = new Properties();
    private static String lib_path;
    static /* synthetic */ Class class$0;

    static {
        IJCE_Properties.setProperties();
    }

    IJCE_Properties() {
    }

    static String getLibraryPath() throws IOException {
        if (lib_path == null) {
            throw new IOException("IJCE library directory (ijce-lib) could not be found");
        }
        return lib_path;
    }

    private static void setProperties() {
        try {
            PrivilegeManager.enablePrivilege("UniversalPropertyRead");
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        String fs = System.getProperty("file.separator");
        try {
            PrivilegeManager.revertPrivilege("UniversalPropertyRead");
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        try {
            PrivilegeManager.enablePrivilege("UniversalFileRead");
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        boolean loaded = false;
        int i = 0;
        while (i < PROPERTIES_FILES.length) {
            Class<?> clazz;
            Class<?> clazz2;
            Class<?> clazz3;
            InputStream props;
            Class<?> clazz4 = class$0;
            if (clazz4 == null) {
                try {
                    clazz4 = Class.forName("xjava.security.IJCE_Properties");
                }
                catch (ClassNotFoundException classNotFoundException) {
                    throw new NoClassDefFoundError(classNotFoundException.getMessage());
                }
            }
            if ((props = clazz4.getResourceAsStream(String.valueOf(fs) + LIB_DIRNAME + fs + PROPERTIES_FILES[i])) != null) {
                try {
                    properties.load(props);
                    loaded = true;
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if ((clazz3 = class$0) == null) {
                try {
                    clazz3 = Class.forName("xjava.security.IJCE_Properties");
                }
                catch (ClassNotFoundException classNotFoundException) {
                    throw new NoClassDefFoundError(classNotFoundException.getMessage());
                }
            }
            if ((props = clazz3.getResourceAsStream(String.valueOf(fs) + PROPERTIES_FILES[i])) != null) {
                try {
                    properties.load(props);
                    loaded = true;
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if ((clazz2 = class$0) == null) {
                try {
                    clazz2 = Class.forName("xjava.security.IJCE_Properties");
                }
                catch (ClassNotFoundException classNotFoundException) {
                    throw new NoClassDefFoundError(classNotFoundException.getMessage());
                }
            }
            if ((props = clazz2.getResourceAsStream(String.valueOf(fs) + "META-INF" + fs + PROPERTIES_FILES[i])) != null) {
                try {
                    properties.load(props);
                    loaded = true;
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if ((clazz = class$0) == null) {
                try {
                    clazz = Class.forName("xjava.security.IJCE_Properties");
                }
                catch (ClassNotFoundException classNotFoundException) {
                    throw new NoClassDefFoundError(classNotFoundException.getMessage());
                }
            }
            if ((props = clazz.getResourceAsStream(PROPERTIES_FILES[i])) != null) {
                try {
                    properties.load(props);
                    loaded = true;
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            ++i;
        }
        try {
            PrivilegeManager.revertPrivilege("UniversalFileRead");
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        if (!loaded) {
            System.err.println("Warning: failed to load the IJCE properties file.\nMake sure that the CLASSPATH entry for IJCE is an absolute path.");
        }
    }

    static void save(OutputStream os, String comment) {
        properties.save(os, comment);
    }

    static String getProperty(String key) {
        return properties.getProperty(key);
    }

    static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    static Enumeration propertyNames() {
        return properties.propertyNames();
    }

    static void list(PrintStream out) {
        properties.list(out);
    }

    static void list(PrintWriter out) {
        properties.list(out);
    }
}

