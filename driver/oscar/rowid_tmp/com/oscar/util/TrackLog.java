/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class TrackLog {
    public static final int NONLOG_LEVEL = 0;
    public static final int SQL_LEVEL = 1;
    public static final int INTERFACE_LEVEL = 2;
    public static final int PROTOCOL_LEVEL = 3;
    public static final int PROTOCOLDETAIL_LEVEL = 4;
    private static TrackLog trackLog;
    private PrintStream ps = null;
    private FileInputStream inputStream = null;
    private int logLevel = 0;
    private int maxFileSize = 0x6400000;
    private String logPath;
    private static String defaultConfigFilePath;

    private TrackLog() {
    }

    public static synchronized TrackLog getInstance() {
        if (trackLog == null) {
            trackLog = new TrackLog();
        }
        return trackLog;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public int getLogLevel() {
        return this.logLevel;
    }

    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize * 1024 * 1024;
    }

    public String getDefaultConfigFilePath() {
        return defaultConfigFilePath;
    }

    public void initLogPath(String logPath) {
        try {
            if (this.ps != null) {
                this.ps.close();
            }
            if (this.inputStream != null) {
                this.inputStream.close();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.logPath = logPath;
        if (this.logLevel > 0 || this.logLevel == -1) {
            try {
                StringBuffer logFilePathSb = new StringBuffer();
                File folder = new File(logPath);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                String logFile = null;
                if (!folder.isFile()) {
                    SimpleDateFormat format = new SimpleDateFormat("yyMMddhhmmssSSS");
                    logFilePathSb.append(folder.getAbsolutePath()).append(File.separatorChar).append("OscarJDBC").append(format.format(new Date())).append(".log");
                    logFile = logFilePathSb.toString();
                }
                FileOutputStream fos = new FileOutputStream(logFile, false);
                this.inputStream = new FileInputStream(logFile);
                this.ps = new PrintStream(fos);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void initLogPath() {
        try {
            if (this.ps != null) {
                this.ps.close();
            }
            if (this.inputStream != null) {
                this.inputStream.close();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        if (this.logLevel > 0 || this.logLevel == -1) {
            try {
                StringBuffer logFilePathSb = new StringBuffer();
                this.logPath = this.getDefaultConfigFilePath();
                File folder = new File(this.logPath);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                String logFile = null;
                if (!folder.isFile()) {
                    SimpleDateFormat format = new SimpleDateFormat("yyMMddhhmmssSSS");
                    logFilePathSb.append(folder.getAbsolutePath()).append(File.separatorChar).append("OscarJDBC").append(format.format(new Date())).append(".log");
                    logFile = logFilePathSb.toString();
                }
                FileOutputStream fos = new FileOutputStream(logFile, false);
                this.inputStream = new FileInputStream(logFile);
                this.ps = new PrintStream(fos);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
    }

    public StackTraceElement getFrame() {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        return stack[1];
    }

    public static void functionLog() {
    }

    public synchronized void writeLog(String msg) {
        if (this.getFileSize() >= this.maxFileSize) {
            if (this.logPath == null) {
                this.initLogPath();
            } else {
                this.initLogPath(this.logPath);
            }
        }
        if ((this.logLevel > 0 || this.logLevel == -1) && this.ps != null) {
            GregorianCalendar gc = new GregorianCalendar();
            String title = gc.get(1) + "-" + (gc.get(2) + 1) + "-" + gc.get(5) + " " + gc.get(11) + ":" + gc.get(12) + ":" + gc.get(13) + "  ";
            this.ps.println(title + msg);
        }
    }

    private int getFileSize() {
        if (this.inputStream != null) {
            try {
                int fileSize = this.inputStream.available();
                return fileSize;
            }
            catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    static {
        defaultConfigFilePath = System.getProperty("user.dir");
    }
}

