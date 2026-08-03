/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.Driver;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class SystemInformation {
    private static final String DEFAULT_MAC = "00-00-00-00-00-00";
    private static final int GROUP_COUNT = 20;
    private String user;
    private String userDomain;
    private String[] groups = new String[20];
    private int groupCount;
    private String hostName;
    private String mac;
    private int netProtocal = 0;
    private String application;
    private byte[] fingerPrintInfo = null;
    protected boolean logFlag = Driver.getLogLevel() >= 3;
    private static SystemInformation instance = null;

    private SystemInformation() throws SQLException {
        for (int i = 0; i < 20; ++i) {
            this.groups[i] = "";
        }
        String dllName = System.getProperty("user.dir", "") + File.separator;
        String osName = System.getProperty("os.name", "window").toLowerCase();
        dllName = osName.indexOf("window") != -1 ? dllName + "systemwin.dll" : dllName + "libsystemlinux.so";
        this.application = this.application == null ? "java" : this.application.toUpperCase();
        File dllFile = new File(dllName);
        try {
            if (dllFile.exists() && dllFile.isFile()) {
                System.load(dllName);
                this.user = this.getSystemUser();
                this.userDomain = this.getSystemUserDomain();
                this.getSystemGroups(this.groups);
                this.groupCount = this.getSystemGroupCount();
                this.hostName = this.getSystemHostName();
                this.mac = this.getSystemMac();
                this.application = this.getSystemApplication();
                this.user = this.user == null ? "NULL" : this.user.toUpperCase();
                this.userDomain = this.userDomain == null ? "NULL" : this.userDomain.toUpperCase();
                this.hostName = this.hostName == null ? "NULL" : this.hostName.toUpperCase();
                this.mac = this.mac == null ? DEFAULT_MAC : this.mac.toUpperCase();
            } else {
                if (this.logFlag) {
                    Driver.writeLog("SystemInformation: dllName: " + dllName + " not exists");
                }
                this.user = "";
                this.userDomain = "";
                this.hostName = "";
                this.mac = this.GetMac();
            }
        }
        catch (Throwable th) {
            if (this.logFlag) {
                Driver.writeLog("SystemInformation: initialize error: " + th.getMessage());
            }
            this.user = "";
            this.userDomain = "";
            this.hostName = "";
            this.mac = this.GetMac();
        }
    }

    public String GetMac() {
        String mac = DEFAULT_MAC;
        try {
            mac = SystemInformation.isWindowsOS() ? SystemInformation.getWindowsMAC() : SystemInformation.getUnixMACAddress();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return mac.toUpperCase();
    }

    public static String getUnixMACAddress() throws Exception {
        Process pro = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "ifconfig|grep -o ..:..:..:..:..:..|awk -F: '{print $1\"-\"$2\"-\"$3\"-\"$4\"-\"$5\"-\"$6}'"});
        InputStream in = pro.getInputStream();
        BufferedReader read = new BufferedReader(new InputStreamReader(in));
        ArrayList<String> list = new ArrayList<String>();
        String line = null;
        while ((line = read.readLine()) != null) {
            list.add(line);
        }
        read.close();
        in.close();
        pro.destroy();
        return SystemInformation.getMacFromList(list);
    }

    public static boolean isWindowsOS() {
        boolean isWindowsOS = false;
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") > -1) {
            isWindowsOS = true;
        }
        return isWindowsOS;
    }

    public static String getWindowsMAC() {
        ArrayList<String> macs = new ArrayList<String>();
        String mac = null;
        try {
            Process pro = Runtime.getRuntime().exec("cmd.exe /c ipconfig/all");
            InputStream is = pro.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb = new StringBuffer();
            while ((mac = br.readLine()) != null) {
                sb.append(mac);
            }
            Pattern p = Pattern.compile("..-..-..-..-..-..");
            Matcher m = p.matcher(sb.toString());
            if (m.find()) {
                macs.add(m.group(0));
            }
            br.close();
            is.close();
            pro.destroy();
        }
        catch (IOException e) {
            System.out.println("Can't get mac address!");
            return DEFAULT_MAC;
        }
        return SystemInformation.getMacFromList(macs);
    }

    private static String getMacFromList(List<String> macs) {
        if (macs.size() < 1) {
            return DEFAULT_MAC;
        }
        return macs.get(0);
    }

    public static SystemInformation getInstance(boolean b) throws SQLException {
        return SystemInformation.getInstance();
    }

    public static SystemInformation getInstance() throws SQLException {
        if (instance == null) {
            instance = new SystemInformation();
        }
        return instance;
    }

    public static SystemInformation getMBInitial() throws SQLException {
        return new SystemInformation();
    }

    public static void main(String[] args) {
        try {
            SystemInformation s = SystemInformation.getInstance(true);
            System.err.println(s.getFingerPrintInfo());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private native String getSystemUser();

    private native String getSystemUserDomain();

    private native void getSystemGroups(String[] var1);

    private native int getSystemGroupCount();

    private native String getSystemHostName();

    private native String getSystemMac();

    private native String getSystemApplication();

    private native byte[] getSystemFingerPrintInfo();

    private native byte[] getSystemFingerPrintEnroll(byte[] var1, byte[] var2, byte[] var3);

    public String getUser() {
        return this.user;
    }

    public String getUserDomain() {
        return this.userDomain;
    }

    public String[] getGroups() {
        return this.groups;
    }

    public int getGroupCount() {
        return this.groupCount;
    }

    public String getHostName() {
        return this.hostName;
    }

    public int getNetProtocal() {
        return this.netProtocal;
    }

    public String getMac() {
        return this.mac;
    }

    public String getApplication() {
        return this.application;
    }

    public byte[] getFingerPrintInfo() {
        if (this.fingerPrintInfo == null) {
            try {
                this.fingerPrintInfo = this.getSystemFingerPrintInfo();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            catch (Error e) {
                e.printStackTrace();
            }
        }
        return this.fingerPrintInfo;
    }

    public byte[] getFingerPrintInfoForEnroll() {
        byte[] fp_tz = null;
        try {
            fp_tz = this.getSystemFingerPrintInfo();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        catch (Error e) {
            e.printStackTrace();
        }
        return fp_tz;
    }

    public byte[] getFingerPrintMB(byte[] fp_tz1, byte[] fp_tz2, byte[] fp_tz3) {
        byte[] fp_mb = null;
        try {
            fp_mb = this.getSystemFingerPrintEnroll(fp_tz1, fp_tz2, fp_tz3);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return fp_mb;
    }
}

