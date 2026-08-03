/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import netscape.security.ForbiddenTargetException;
import netscape.security.PrivilegeManager;
import netscape.security.Target;
import xjava.security.IJCE_Java10Support;
import xjava.security.IJCE_Properties;
import xjava.security.IJCE_SecuritySupport;
import xjava.security.IJCE_Traceable;

public class IJCE {
    private static final boolean DEBUG = true;
    private static final int debuglevel = IJCE.getDebugLevel("IJCE");
    private static final PrintWriter err = new PrintWriter(System.err, true);
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 1;
    private static final int INTER_VERSION = 0;
    private static final boolean IS_SNAPSHOT = true;
    private static final String CVS_DATE = "$Date: 2000/08/17 11:35:24 $";
    private static Target getProvidersTarget;
    private static PrivilegeManager privMgr;
    private static Hashtable typeToClass;

    static {
        typeToClass = new Hashtable();
    }

    private IJCE() {
    }

    public static String[] getAlgorithms(Provider provider, String type) {
        if (IJCE.getClassForType(type) == null) {
            return new String[0];
        }
        String typedot = String.valueOf(type) + ".";
        Vector<String> algorithms = new Vector<String>();
        Enumeration<?> providerEnum = provider.propertyNames();
        while (providerEnum.hasMoreElements()) {
            String key = (String)providerEnum.nextElement();
            if (!key.startsWith(typedot)) continue;
            algorithms.addElement(key.substring(typedot.length()));
        }
        Object[] buf = new String[algorithms.size()];
        algorithms.copyInto(buf);
        return buf;
    }

    public static String[] getAlgorithms(String type) {
        if (IJCE.getClassForType(type) == null) {
            return new String[0];
        }
        String typedot = String.valueOf(type) + ".";
        Hashtable<String, String> algorithms = new Hashtable<String, String>();
        if (type.equals("PaddingScheme")) {
            algorithms.put("NONE", "");
        } else if (type.equals("Mode")) {
            algorithms.put("ECB", "");
        }
        Provider[] providers = IJCE.getProvidersInternal();
        int i = 0;
        while (i < providers.length) {
            Enumeration<?> providerEnum = providers[i].propertyNames();
            while (providerEnum.hasMoreElements()) {
                String key = (String)providerEnum.nextElement();
                if (!key.startsWith(typedot)) continue;
                algorithms.put(key.substring(typedot.length()), "");
            }
            ++i;
        }
        String[] buf = new String[algorithms.size()];
        Enumeration algorithmEnum = algorithms.keys();
        int n = 0;
        while (algorithmEnum.hasMoreElements()) {
            buf[n++] = (String)algorithmEnum.nextElement();
        }
        return buf;
    }

    public static boolean enableTracing(Object obj, PrintWriter out) {
        if (obj instanceof IJCE_Traceable) {
            ((IJCE_Traceable)obj).enableTracing(out);
            return true;
        }
        return false;
    }

    public static boolean enableTracing(Object obj) {
        return IJCE.enableTracing(obj, err);
    }

    public static void disableTracing(Object obj) {
        if (obj instanceof IJCE_Traceable) {
            ((IJCE_Traceable)obj).disableTracing();
        }
    }

    public static String getStandardName(String algorithm, String type) {
        String temptype = "Alias." + type;
        String standardName = Security.getAlgorithmProperty(algorithm, temptype);
        return standardName != null ? standardName : algorithm;
    }

    public static Object getImplementation(String algorithm, String type) throws NoSuchAlgorithmException {
        try {
            return IJCE.getImplementation(algorithm, null, type);
        }
        catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(e.getMessage());
        }
    }

    public static Object getImplementation(String algorithm, String provider, String type) throws NoSuchAlgorithmException, NoSuchProviderException {
        String error;
        Class cl = IJCE.getImplementationClass(algorithm, provider, type);
        try {
            return cl.newInstance();
        }
        catch (LinkageError e) {
            error = " could not be linked correctly.\n" + e;
        }
        catch (InstantiationException e) {
            error = " cannot be instantiated.\n" + e;
        }
        catch (IllegalAccessException e) {
            error = " cannot be accessed.\n" + e;
        }
        throw new NoSuchAlgorithmException("class configured for " + type + ": " + cl.getName() + error);
    }

    public static Class getImplementationClass(String algorithm, String type) throws NoSuchAlgorithmException {
        try {
            return IJCE.getImplementationClass(algorithm, null, type);
        }
        catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(e.getMessage());
        }
    }

    public static Class getImplementationClass(String algorithm, String provider, String type) throws NoSuchAlgorithmException, NoSuchProviderException {
        String standardName = IJCE.getStandardName(algorithm, type);
        Class target = IJCE.getClassForType(type);
        if (target == null) {
            throw new NoSuchAlgorithmException(String.valueOf(type) + " is not a configured type");
        }
        Class cl = IJCE.getClassCandidate(standardName, provider, type);
        if (IJCE_Java10Support.isAssignableFrom(target, cl)) {
            return cl;
        }
        throw new NoSuchAlgorithmException("class configured for " + type + ": " + cl.getName() + " is not a subclass of " + target.getName());
    }

    private static Class getClassCandidate(String standardName, String provider, String type) throws NoSuchAlgorithmException, NoSuchProviderException {
        Class cl;
        String property = String.valueOf(type) + "." + standardName;
        if (provider == null) {
            Provider[] providers = IJCE.getProvidersInternal();
            int i = 0;
            while (i < providers.length) {
                String classname = providers[i].getProperty(property);
                if (classname != null) {
                    try {
                        Class cl2 = IJCE.findEngineClass(classname, type);
                        if (cl2 != null) {
                            return cl2;
                        }
                    }
                    catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                        // empty catch block
                    }
                }
                ++i;
            }
            throw new NoSuchAlgorithmException("algorithm " + standardName + " is not available.");
        }
        Provider providerObj = IJCE.getProviderInternal(provider);
        if (providerObj == null) {
            throw new NoSuchProviderException("provider " + provider + " is not available.");
        }
        String classname = providerObj.getProperty(property);
        if (classname != null && (cl = IJCE.findEngineClass(classname, type)) != null) {
            return cl;
        }
        throw new NoSuchAlgorithmException("algorithm " + standardName + " is not available from provider " + provider);
    }

    private static Class findEngineClass(String classname, String type) throws NoSuchAlgorithmException {
        String error;
        try {
            return Class.forName(classname);
        }
        catch (ClassNotFoundException e) {
            return null;
        }
        catch (NoSuchMethodError e) {
            error = " does not have a zero-argument constructor.\n" + e;
        }
        catch (LinkageError e) {
            error = " could not be linked correctly.\n" + e;
        }
        throw new NoSuchAlgorithmException("class configured for " + type + ": " + classname + error);
    }

    public static Target findTarget(String name) throws ForbiddenTargetException {
        return IJCE_SecuritySupport.findTarget(name);
    }

    public static Target findTarget(String name, Object arg) throws ForbiddenTargetException {
        return IJCE_SecuritySupport.findTarget(name, arg);
    }

    public static int getMajorVersion() {
        return 1;
    }

    public static int getMinorVersion() {
        return 1;
    }

    public static int getIntermediateVersion() {
        return 0;
    }

    public static boolean isVersionAtLeast(int major, int minor, int intermediate) {
        if (1 > major) {
            return true;
        }
        if (1 < major) {
            return false;
        }
        if (1 > minor) {
            return true;
        }
        if (1 < minor) {
            return false;
        }
        return intermediate <= 0;
    }

    public static String getReleaseDate() {
        try {
            return CVS_DATE.substring(7, 17);
        }
        catch (StringIndexOutOfBoundsException e) {
            return "unknown";
        }
    }

    public static String getVersionString() {
        StringBuffer version = new StringBuffer("IJCE ").append(1).append(".").append(1);
        version.append(" (").append(IJCE.getReleaseDate()).append(" snapshot)");
        return version.toString();
    }

    public static boolean isProvidingJCA() {
        try {
            return IJCE_Java10Support.isAssignableFrom(Class.forName("java.security.IJCE_Traceable"), Class.forName("java.security.MessageDigest"));
        }
        catch (Exception e) {
            return false;
        }
    }

    public static boolean isProvidingJCE() {
        try {
            return IJCE_Java10Support.isAssignableFrom(Class.forName("java.security.IJCE_Traceable"), Class.forName("java.security.Cipher"));
        }
        catch (Exception e) {
            return false;
        }
    }

    private static Class getClassForType(String type) {
        Class<?> cl = (Class<?>)typeToClass.get(type);
        if (cl != null) {
            return cl;
        }
        String classname = IJCE_Properties.getProperty("Type." + type);
        if (classname == null) {
            return null;
        }
        try {
            cl = Class.forName(classname);
        }
        catch (LinkageError e) {
            IJCE.debug("Error loading class for algorithm type " + type + ": " + e);
            return null;
        }
        catch (ClassNotFoundException e) {
            IJCE.debug("Error loading class for algorithm type " + type + ": " + e);
            return null;
        }
        typeToClass.put(type, cl);
        return cl;
    }

    private static Provider[] getProvidersInternal() {
        try {
            if (getProvidersTarget == null) {
                getProvidersTarget = IJCE.findTarget("GetSecurityProviders");
            }
            if (privMgr == null) {
                privMgr = PrivilegeManager.getPrivilegeManager();
            }
            privMgr.enablePrivilege(getProvidersTarget);
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        Provider[] providers = Security.getProviders();
        if (debuglevel >= 4) {
            int i = 0;
            while (i < providers.length) {
                IJCE.debug("providers[" + i + "] = " + providers[i]);
                ++i;
            }
        }
        try {
            privMgr.revertPrivilege(getProvidersTarget);
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        return providers;
    }

    private static Provider getProviderInternal(String providerName) {
        try {
            if (getProvidersTarget == null) {
                getProvidersTarget = IJCE.findTarget("GetSecurityProviders");
            }
            if (privMgr == null) {
                privMgr = PrivilegeManager.getPrivilegeManager();
            }
            privMgr.enablePrivilege(getProvidersTarget);
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        Provider provider = Security.getProvider(providerName);
        try {
            privMgr.revertPrivilege(getProvidersTarget);
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        return provider;
    }

    static void debug(String s) {
        err.println(s);
    }

    static void error(String s) {
        err.println(s);
    }

    static void reportBug(String s) {
        err.println("\n" + s + "\n\n" + "Please report this as a bug to <david.hopwood@lmh.ox.ac.uk>, including\n" + "any other messages displayed on the console, and a description of what\n" + "appeared to cause the error.\n");
        throw new InternalError(s);
    }

    static void listProviders() {
        Provider[] providers = IJCE.getProvidersInternal();
        int i = 0;
        while (i < providers.length) {
            err.println("providers[" + i + "] = " + providers[i]);
            ++i;
        }
    }

    static int getDebugLevel(String label) {
        String s = IJCE_Properties.getProperty("Debug.Level." + label);
        if (s == null && (s = IJCE_Properties.getProperty("Debug.Level.*")) == null) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }

    static PrintWriter getDebugOutput() {
        return err;
    }

    public static void main(String[] args) {
        System.out.println(IJCE.getVersionString());
        System.out.println();
        IJCE.listProviders();
        System.out.println();
        try {
            String libPath = IJCE_Properties.getLibraryPath();
            System.out.println("The library directory is");
            System.out.println("  " + libPath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

