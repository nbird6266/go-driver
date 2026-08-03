/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {
    public static boolean loadProperties(Properties properties, String name) {
        if (!name.endsWith(".properties")) {
            name = String.valueOf(name) + ".properties";
        }
        StringTokenizer list = new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator);
        while (list.hasMoreTokens()) {
            if (!FileUtil.doFileOrDir(properties, name, new File(list.nextToken()))) continue;
            return true;
        }
        return false;
    }

    static boolean doFileOrDir(Properties properties, String name, File f) {
        ZipInputStream zip;
        if (f.isDirectory()) {
            String[] list = f.list(new IdentityFilter(name));
            int n = list.length;
            int i = 0;
            while (i < n) {
                if (FileUtil.doFileOrDir(properties, name, new File(f, list[i]))) {
                    return true;
                }
                ++i;
            }
            return false;
        }
        if (!f.isFile()) {
            return false;
        }
        String it = f.getName();
        if (it.equals(name)) {
            try {
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
                properties.load(in);
                in.close();
                return true;
            }
            catch (FileNotFoundException in) {
            }
            catch (IOException in) {
                // empty catch block
            }
        }
        if (!(it = it.toUpperCase()).endsWith(".ZIP") && !it.endsWith(".JAR")) {
            return false;
        }
        try {
            zip = new ZipInputStream(new FileInputStream(f));
        }
        catch (FileNotFoundException x) {
            return false;
        }
        boolean result = false;
        try {
            try {
                ZipEntry ze;
                while ((ze = zip.getNextEntry()) != null) {
                    int n;
                    if (ze.isDirectory() || !(it = ze.getName()).endsWith(name)) continue;
                    ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
                    byte[] buffer = new byte[512];
                    while ((n = zip.read(buffer)) != -1) {
                        out.write(buffer, 0, n);
                    }
                    BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(out.toByteArray()));
                    properties.load(in);
                    in.close();
                    result = true;
                    break;
                }
            }
            catch (IOException iOException) {
                try {
                    zip.close();
                }
                catch (IOException iOException2) {}
            }
        }
        finally {
            try {
                zip.close();
            }
            catch (IOException iOException) {}
        }
        return result;
    }

    static boolean doZip(Properties properties, String name, File f) {
        ZipInputStream zip;
        try {
            zip = new ZipInputStream(new FileInputStream(f));
        }
        catch (FileNotFoundException x) {
            return false;
        }
        boolean result = false;
        try {
            try {
                ZipEntry ze;
                while ((ze = zip.getNextEntry()) != null) {
                    int n;
                    String it;
                    if (ze.isDirectory() || !(it = ze.getName()).endsWith(name)) continue;
                    ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
                    byte[] buffer = new byte[512];
                    while ((n = zip.read(buffer)) != -1) {
                        out.write(buffer, 0, n);
                    }
                    BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(out.toByteArray()));
                    properties.load(in);
                    in.close();
                    result = true;
                    break;
                }
            }
            catch (IOException iOException) {
                try {
                    zip.close();
                }
                catch (IOException iOException2) {}
            }
        }
        finally {
            try {
                zip.close();
            }
            catch (IOException iOException) {}
        }
        return result;
    }

    static class IdentityFilter
    implements FilenameFilter {
        private String it;

        public IdentityFilter(String name) {
            this.it = name;
        }

        public boolean accept(File dir, String name) {
            File f = new File(dir, name);
            if (f.isDirectory()) {
                return true;
            }
            return f.isFile() && name.equals(this.it);
        }
    }
}

