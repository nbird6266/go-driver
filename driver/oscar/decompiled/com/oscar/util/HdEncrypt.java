/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class HdEncrypt {
    private static volatile HdEncrypt instance = null;
    private static volatile boolean isInited = false;

    private HdEncrypt() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static HdEncrypt instance() {
        if (instance != null) return instance;
        Class<HdEncrypt> clazz = HdEncrypt.class;
        synchronized (HdEncrypt.class) {
            if (instance != null) return instance;
            instance = new HdEncrypt();
            // ** MonitorExit[var0] (shouldn't be in output)
            return instance;
        }
    }

    private String getHdEncryptHome() {
        File home;
        String hdEncryptHome = "";
        String jarPath = "";
        try {
            URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
            jarPath = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null).getPath();
        }
        catch (Exception e) {
            // empty catch block
        }
        if (!"".equals(jarPath)) {
            System.out.println(jarPath);
            if (jarPath.endsWith("/")) {
                jarPath = jarPath.substring(0, jarPath.length() - 1);
            }
            if (jarPath.endsWith(".jar")) {
                int index = jarPath.lastIndexOf("/");
                if (index != -1) {
                    jarPath = jarPath.substring(0, index);
                }
            } else {
                jarPath = jarPath + "/..";
            }
            home = new File(jarPath + "/hdencrypt");
            if (home.exists() && home.isDirectory()) {
                hdEncryptHome = jarPath + "/hdencrypt";
            }
        }
        if ("".equals(hdEncryptHome)) {
            hdEncryptHome = System.getProperty("user.dir") + "/hdencrypt";
        }
        if ((home = new File(hdEncryptHome)).exists()) {
            hdEncryptHome = home.getAbsolutePath();
        }
        return hdEncryptHome;
    }

    private String getDllFullName() {
        String dllFullName = "";
        String dllName = "";
        String osName = System.getProperty("os.name", "window").toLowerCase();
        dllName = osName.indexOf("window") != -1 ? dllName + "hdencrypt_jni.dll" : dllName + "libhdencrypt_jni.so";
        String hdEncryptHome = this.getHdEncryptHome();
        dllFullName = hdEncryptHome + "/" + dllName;
        File dll = new File(dllFullName);
        if (dll.exists()) {
            dllFullName = dll.getAbsolutePath();
        }
        return dllFullName;
    }

    public synchronized boolean open() throws IOException {
        if (!isInited) {
            String dllPath = this.getDllFullName();
            File dll = new File(dllPath);
            if (!dll.exists() || !dll.isFile()) {
                isInited = false;
                throw new IOException("HdEncrypt dll\u52a0\u8f7d\u5931\u8d25: " + dllPath);
            }
            try {
                System.load(dllPath);
                Runtime.getRuntime().addShutdownHook(new Thread(){

                    public void run() {
                        if (isInited) {
                            instance.hdExit();
                        }
                    }
                });
                isInited = this.hdInit(this.getHdEncryptHome());
            }
            catch (Exception e) {
                throw new IOException("\u6253\u5f00\u52a0\u5bc6\u8bbe\u5907\u5931\u8d25: " + e.getMessage());
            }
        }
        return isInited;
    }

    private native boolean hdInit(String var1);

    private native boolean hdExit();

    public native byte[] hdSymEncrypt(byte[] var1, byte[] var2);

    public native byte[] hdSymDecrypt(byte[] var1, byte[] var2);

    public native byte[] hdAsymEncrypt(byte[] var1);

    public native byte[] hdGenerateKey();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static void main(String[] args) {
        block4: {
            HdEncrypt hdEncrypt = HdEncrypt.instance();
            try {
                try {
                    hdEncrypt.open();
                    String dataStr = "\u4e2d\u56fd";
                    byte[] key = hdEncrypt.hdGenerateKey();
                    byte[] encryptData = hdEncrypt.hdSymEncrypt(key, dataStr.getBytes());
                    byte[] decryptData = hdEncrypt.hdSymDecrypt(key, encryptData);
                    System.out.println(dataStr.equals(new String(decryptData)));
                    hdEncrypt.hdExit();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Object var7_8 = null;
                    hdEncrypt.hdExit();
                    break block4;
                }
                Object var7_7 = null;
                hdEncrypt.hdExit();
            }
            catch (Throwable throwable) {
                Object var7_9 = null;
                hdEncrypt.hdExit();
                throw throwable;
            }
        }
        System.exit(1);
    }
}

