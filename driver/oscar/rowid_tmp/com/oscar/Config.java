/*
 * Decompiled with CFR 0.152.
 */
package com.oscar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Properties;

public class Config {
    public static int COMPATABLE_DBMS = 0;
    public static String CLOB_ENCODING = System.getProperty("file.encoding");
    public static String ENCODING = System.getProperty("file.encoding");
    public static Properties configProp;
    public static String configPath;
    public static String defultLogPath;

    public void init() {
        Properties pts = new Properties();
        try {
            InputStream is = this.getClass().getResourceAsStream("/com/oscar/oscar.properties");
            pts.load(is);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String path = null;
            try {
                URL url = Config.class.getProtectionDomain().getCodeSource().getLocation();
                path = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null).getPath();
                path = URLDecoder.decode(path, "utf-8");
            }
            catch (Exception e) {
                path = ".";
            }
            if (path != null) {
                if (path.endsWith("/") || path.endsWith("\\")) {
                    path = path.substring(0, path.length() - 1);
                }
                if (path.endsWith(".jar")) {
                    int index = path.lastIndexOf("/");
                    if (index != -1) {
                        path = path.substring(0, index);
                    }
                    configPath = path + File.separator + "oscarconfig.properties";
                    defultLogPath = path + File.separator + "oscarJdbcLog";
                } else {
                    configPath = path + File.separator + ".." + File.separator + "oscarconfig.properties";
                    defultLogPath = path + File.separator + ".." + File.separator + "oscarJdbcLog";
                }
                File f = new File(configPath);
                if (f.exists()) {
                    FileInputStream is = new FileInputStream(f);
                    Properties tmpProp = new Properties();
                    tmpProp.load(is);
                    if (tmpProp != null) {
                        configProp = new Properties();
                        Object tmp = null;
                        String o = null;
                        Enumeration e = tmpProp.keys();
                        while (e.hasMoreElements()) {
                            tmp = e.nextElement();
                            o = ((String)tmp).toUpperCase();
                            configProp.put(o, tmpProp.get(tmp));
                        }
                    }
                }
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            COMPATABLE_DBMS = Integer.parseInt(pts.getProperty("COMPATABLE_DBMS", "0"));
            CLOB_ENCODING = pts.getProperty("CLOB_ENCODING", CLOB_ENCODING);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void msg(String msg) {
    }

    public static void msg(Exception e) {
    }

    public static void main(String[] args) {
        new Config().init();
        System.err.println("COMPATABLE_DBMS=" + COMPATABLE_DBMS);
        System.err.println("CLOB_ENCODING=" + CLOB_ENCODING);
        System.err.println("ENCODING=" + ENCODING);
    }

    static {
        configPath = null;
        defultLogPath = null;
    }
}

