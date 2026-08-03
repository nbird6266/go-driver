/*
 * Decompiled with CFR 0.152.
 */
package org.xerial.snappy;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;
import org.xerial.snappy.OSInfo;
import org.xerial.snappy.SnappyError;
import org.xerial.snappy.SnappyErrorCode;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class SnappyLoader {
    public static final String SNAPPY_SYSTEM_PROPERTIES_FILE = "org-xerial-snappy.properties";
    public static final String KEY_SNAPPY_LIB_PATH = "org.xerial.snappy.lib.path";
    public static final String KEY_SNAPPY_LIB_NAME = "org.xerial.snappy.lib.name";
    public static final String KEY_SNAPPY_TEMPDIR = "org.xerial.snappy.tempdir";
    public static final String KEY_SNAPPY_USE_SYSTEMLIB = "org.xerial.snappy.use.systemlib";
    public static final String KEY_SNAPPY_DISABLE_BUNDLED_LIBS = "org.xerial.snappy.disable.bundled.libs";
    private static volatile boolean isLoaded = false;
    private static volatile Object api = null;

    static synchronized void setApi(Object nativeCode) {
        api = nativeCode;
    }

    private static void loadSnappySystemProperties() {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(SNAPPY_SYSTEM_PROPERTIES_FILE);
            if (is == null) {
                return;
            }
            Properties props = new Properties();
            props.load(is);
            is.close();
            Enumeration<?> names = props.propertyNames();
            while (names.hasMoreElements()) {
                String name = (String)names.nextElement();
                if (!name.startsWith("org.xerial.snappy.") || System.getProperty(name) != null) continue;
                System.setProperty(name, props.getProperty(name));
            }
        }
        catch (Throwable ex) {
            System.err.println("Could not load 'org-xerial-snappy.properties' from classpath: " + ex.toString());
        }
    }

    private static ClassLoader getRootClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while (cl.getParent() != null) {
            cl = cl.getParent();
        }
        return cl;
    }

    private static byte[] getByteCode(String resourcePath) throws IOException {
        int readLength;
        InputStream in = SnappyLoader.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException(resourcePath + " is not found");
        }
        byte[] buf = new byte[1024];
        ByteArrayOutputStream byteCodeBuf = new ByteArrayOutputStream();
        while ((readLength = in.read(buf)) != -1) {
            byteCodeBuf.write(buf, 0, readLength);
        }
        in.close();
        return byteCodeBuf.toByteArray();
    }

    public static boolean isNativeLibraryLoaded() {
        return isLoaded;
    }

    private static boolean hasInjectedNativeLoader() {
        try {
            String nativeLoaderClassName = "org.xerial.snappy.SnappyNativeLoader";
            Class<?> c = Class.forName("org.xerial.snappy.SnappyNativeLoader");
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    static synchronized Object load() {
        if (api != null) {
            return api;
        }
        try {
            if (!SnappyLoader.hasInjectedNativeLoader()) {
                Class<?> nativeLoader = SnappyLoader.injectSnappyNativeLoader();
                SnappyLoader.loadNativeLibrary(nativeLoader);
            }
            isLoaded = true;
            Object nativeCode = Class.forName("org.xerial.snappy.SnappyNative").newInstance();
            SnappyLoader.setApi(nativeCode);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new SnappyError(SnappyErrorCode.FAILED_TO_LOAD_NATIVE_LIBRARY, e.getMessage());
        }
        return api;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Class<?> injectSnappyNativeLoader() {
        try {
            String nativeLoaderClassName = "org.xerial.snappy.SnappyNativeLoader";
            ClassLoader rootClassLoader = SnappyLoader.getRootClassLoader();
            byte[] byteCode = SnappyLoader.getByteCode("/org/xerial/snappy/SnappyNativeLoader.bytecode");
            String[] classesToPreload = new String[]{"org.xerial.snappy.SnappyNativeAPI", "org.xerial.snappy.SnappyNative", "org.xerial.snappy.SnappyErrorCode"};
            ArrayList<byte[]> preloadClassByteCode = new ArrayList<byte[]>(classesToPreload.length);
            for (String each : classesToPreload) {
                preloadClassByteCode.add(SnappyLoader.getByteCode(String.format("/%s.class", each.replaceAll("\\.", "/"))));
            }
            Class<?> classLoader = Class.forName("java.lang.ClassLoader");
            Method defineClass = classLoader.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE, ProtectionDomain.class);
            ProtectionDomain pd = System.class.getProtectionDomain();
            defineClass.setAccessible(true);
            try {
                defineClass.invoke(rootClassLoader, "org.xerial.snappy.SnappyNativeLoader", byteCode, 0, byteCode.length, pd);
                for (int i = 0; i < classesToPreload.length; ++i) {
                    byte[] b = (byte[])preloadClassByteCode.get(i);
                    defineClass.invoke(rootClassLoader, classesToPreload[i], b, 0, b.length, pd);
                }
            }
            finally {
                defineClass.setAccessible(false);
            }
            return rootClassLoader.loadClass("org.xerial.snappy.SnappyNativeLoader");
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            throw new SnappyError(SnappyErrorCode.FAILED_TO_LOAD_NATIVE_LIBRARY, e.getMessage());
        }
    }

    private static void loadNativeLibrary(Class<?> loaderClass) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (loaderClass == null) {
            throw new SnappyError(SnappyErrorCode.FAILED_TO_LOAD_NATIVE_LIBRARY, "missing snappy native loader class");
        }
        File nativeLib = SnappyLoader.findNativeLibrary();
        if (nativeLib != null) {
            Method loadMethod = loaderClass.getDeclaredMethod("load", String.class);
            loadMethod.invoke(null, nativeLib.getAbsolutePath());
        } else {
            Method loadMethod = loaderClass.getDeclaredMethod("loadLibrary", String.class);
            loadMethod.invoke(null, "snappyjava");
        }
    }

    static String md5sum(InputStream input) throws IOException {
        BufferedInputStream in = new BufferedInputStream(input);
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            DigestInputStream digestInputStream = new DigestInputStream(in, digest);
            while (digestInputStream.read() >= 0) {
            }
            ByteArrayOutputStream md5out = new ByteArrayOutputStream();
            md5out.write(digest.digest());
            String string = md5out.toString();
            return string;
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm is not available: " + e);
        }
        finally {
            in.close();
        }
    }

    private static File extractLibraryFile(String libFolderForCurrentOS, String libraryFileName, String targetFolder) {
        String nativeLibraryFilePath = libFolderForCurrentOS + "/" + libraryFileName;
        String uuid = UUID.randomUUID().toString();
        String extractedLibFileName = String.format("snappy-%s-%s-%s", SnappyLoader.getVersion(), uuid, libraryFileName);
        File extractedLibFile = new File(targetFolder, extractedLibFileName);
        try {
            if (extractedLibFile.exists()) {
                String md5sum2;
                String md5sum1 = SnappyLoader.md5sum(SnappyLoader.class.getResourceAsStream(nativeLibraryFilePath));
                if (md5sum1.equals(md5sum2 = SnappyLoader.md5sum(new FileInputStream(extractedLibFile)))) {
                    return new File(targetFolder, extractedLibFileName);
                }
                boolean deletionSucceeded = extractedLibFile.delete();
                if (!deletionSucceeded) {
                    throw new IOException("failed to remove existing native library file: " + extractedLibFile.getAbsolutePath());
                }
            }
            InputStream reader = SnappyLoader.class.getResourceAsStream(nativeLibraryFilePath);
            FileOutputStream writer = new FileOutputStream(extractedLibFile);
            byte[] buffer = new byte[8192];
            int bytesRead = 0;
            while ((bytesRead = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, bytesRead);
            }
            writer.close();
            reader.close();
            if (!System.getProperty("os.name").contains("Windows")) {
                try {
                    Runtime.getRuntime().exec(new String[]{"chmod", "755", extractedLibFile.getAbsolutePath()}).waitFor();
                }
                catch (Throwable e) {
                    // empty catch block
                }
            }
            return new File(targetFolder, extractedLibFileName);
        }
        catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    static File findNativeLibrary() {
        File nativeLib;
        boolean useSystemLib = Boolean.parseBoolean(System.getProperty(KEY_SNAPPY_USE_SYSTEMLIB, "false"));
        boolean disabledBundledLibs = Boolean.parseBoolean(System.getProperty(KEY_SNAPPY_DISABLE_BUNDLED_LIBS, "false"));
        if (useSystemLib || disabledBundledLibs) {
            return null;
        }
        String snappyNativeLibraryPath = System.getProperty(KEY_SNAPPY_LIB_PATH);
        String snappyNativeLibraryName = System.getProperty(KEY_SNAPPY_LIB_NAME);
        if (snappyNativeLibraryName == null) {
            snappyNativeLibraryName = System.mapLibraryName("snappyjava");
        }
        if (snappyNativeLibraryPath != null && (nativeLib = new File(snappyNativeLibraryPath, snappyNativeLibraryName)).exists()) {
            return nativeLib;
        }
        snappyNativeLibraryPath = "/org/xerial/snappy/native/" + OSInfo.getNativeLibFolderPathForCurrentOS();
        boolean hasNativeLib = SnappyLoader.hasResource(snappyNativeLibraryPath + "/" + snappyNativeLibraryName);
        if (!hasNativeLib && OSInfo.getOSName().equals("Mac")) {
            String altName = "libsnappyjava.jnilib";
            if (SnappyLoader.hasResource(snappyNativeLibraryPath + "/" + altName)) {
                snappyNativeLibraryName = altName;
                hasNativeLib = true;
            }
        }
        if (!hasNativeLib) {
            String errorMessage = String.format("no native library is found for os.name=%s and os.arch=%s", OSInfo.getOSName(), OSInfo.getArchName());
            throw new SnappyError(SnappyErrorCode.FAILED_TO_LOAD_NATIVE_LIBRARY, errorMessage);
        }
        String tempFolder = new File(System.getProperty(KEY_SNAPPY_TEMPDIR, System.getProperty("java.io.tmpdir"))).getAbsolutePath();
        return SnappyLoader.extractLibraryFile(snappyNativeLibraryPath, snappyNativeLibraryName, tempFolder);
    }

    private static boolean hasResource(String path) {
        return SnappyLoader.class.getResource(path) != null;
    }

    public static String getVersion() {
        URL versionFile = SnappyLoader.class.getResource("/META-INF/maven/org.xerial.snappy/snappy-java/pom.properties");
        if (versionFile == null) {
            versionFile = SnappyLoader.class.getResource("/org/xerial/snappy/VERSION");
        }
        String version = "unknown";
        try {
            if (versionFile != null) {
                Properties versionData = new Properties();
                versionData.load(versionFile.openStream());
                version = versionData.getProperty("version", version);
                if (version.equals("unknown")) {
                    version = versionData.getProperty("VERSION", version);
                }
                version = version.trim().replaceAll("[^0-9\\.]", "");
            }
        }
        catch (IOException e) {
            System.err.println(e);
        }
        return version;
    }

    static {
        SnappyLoader.loadSnappySystemProperties();
    }
}

