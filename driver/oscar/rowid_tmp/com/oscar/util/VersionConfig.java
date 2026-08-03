/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionConfig {
    private static VersionConfig instance = null;
    public static final int VERSION_69 = 1;
    public static final int VERSION_56 = 2;
    public static final int VERSION_56_SEC = 3;
    public static final int VERSION_57 = 4;
    public static final int OldTransferType = 1;
    public static final int NewTransferType = 2;
    private int mainVersion = 0;
    private int childVersion = 0;
    private int transferType = 0;
    private boolean wuziVersion = false;
    private boolean convertLobLocatorByFunc = true;
    private boolean sendQueryNumForNotRealPrepare = true;
    private boolean haveEmptyXlobWithOid = true;
    private boolean newParamInfoPacket = false;
    private boolean receiveErrorCode = false;
    private int DBMajorVersion = 0;
    private int DBMinorVersion = 0;
    private int DriverMajorVersion = 0;
    private int DriverMinorVersion = 0;
    private Properties prop = new Properties();

    private VersionConfig() {
        this.loadFromFile();
    }

    public static VersionConfig getInstance() {
        if (instance == null) {
            instance = new VersionConfig();
        }
        return instance;
    }

    public void loadFromFile() {
        InputStream is = this.getClass().getResourceAsStream("/com/oscar/util/versionConfig.properties");
        try {
            this.prop.load(is);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        this.explainProperties();
    }

    protected void explainProperties() {
        String temp = null;
        this.mainVersion = new Integer(this.getConfigValue("MainVersion", true));
        this.childVersion = new Integer(this.getConfigValue("ChildVersion", true));
        this.DBMajorVersion = Integer.parseInt(this.getConfigValue("DBMajorVersion"));
        this.DBMinorVersion = Integer.parseInt(this.getConfigValue("DBMinorVersion"));
        this.DriverMajorVersion = Integer.parseInt(this.getConfigValue("DriverMajorVersion"));
        this.DriverMinorVersion = Integer.parseInt(this.getConfigValue("DriverMinorVersion"));
        this.setDefaultConfigWithVersion();
        temp = this.getConfigValue("WuziVersion");
        this.wuziVersion = temp == null ? false : new Boolean(temp);
        temp = this.getConfigValue("TransferType");
        if (temp != null) {
            this.transferType = new Integer(temp);
        }
        if ((temp = this.getConfigValue("ConvertLobLocatorByFunc")) != null) {
            this.convertLobLocatorByFunc = new Boolean(temp);
        }
        if ((temp = this.getConfigValue("SendQueryNumForNotRealPrepare")) != null) {
            this.sendQueryNumForNotRealPrepare = new Boolean(temp);
        }
        if ((temp = this.getConfigValue("HaveEmptyXlobWithOid")) != null) {
            this.haveEmptyXlobWithOid = new Boolean(temp);
        }
        if ((temp = this.getConfigValue("ReceiveErrorCode")) != null) {
            this.receiveErrorCode = new Boolean(temp);
        }
    }

    private void setDefaultConfigWithVersion() throws Error {
        switch (this.mainVersion) {
            case 1: {
                this.transferType = this.childVersion < 46 ? 1 : 2;
                this.convertLobLocatorByFunc = this.childVersion >= 48;
                this.sendQueryNumForNotRealPrepare = false;
                this.haveEmptyXlobWithOid = false;
                this.newParamInfoPacket = false;
                break;
            }
            case 2: {
                this.transferType = 2;
                this.convertLobLocatorByFunc = this.childVersion >= 8;
                this.sendQueryNumForNotRealPrepare = this.childVersion >= 8;
                this.haveEmptyXlobWithOid = this.childVersion >= 8;
                this.newParamInfoPacket = false;
                break;
            }
            case 3: {
                this.transferType = 2;
                this.convertLobLocatorByFunc = this.childVersion >= 8;
                this.sendQueryNumForNotRealPrepare = this.childVersion >= 8;
                this.haveEmptyXlobWithOid = this.childVersion >= 8;
                this.newParamInfoPacket = false;
                break;
            }
            case 4: {
                this.transferType = 2;
                this.convertLobLocatorByFunc = true;
                this.sendQueryNumForNotRealPrepare = false;
                this.haveEmptyXlobWithOid = true;
                this.newParamInfoPacket = true;
                if (this.childVersion <= 4) break;
                this.receiveErrorCode = true;
                break;
            }
            default: {
                throw new Error("\u672a\u77e5\u540e\u53f0\u7248\u672c\u53f7");
            }
        }
    }

    private String getConfigValue(String configName) {
        return this.getConfigValue(configName, false);
    }

    private String getConfigValue(String configName, boolean notNull) {
        String temp = null;
        temp = this.prop.getProperty(configName);
        if (temp == null && notNull) {
            throw new Error("JDBC\u7248\u672c\u914d\u7f6e\u6587\u4ef6\u51fa\u9519");
        }
        return temp;
    }

    public String getVersionValue(String versionName, String defaultValue) {
        return this.prop.getProperty(versionName, defaultValue);
    }

    public int getMainVersion() {
        return this.mainVersion;
    }

    public boolean isWuziVersion() {
        return this.wuziVersion;
    }

    public int getTransferType() {
        return this.transferType;
    }

    public boolean isConvertLobLocatorByFunc() {
        return this.convertLobLocatorByFunc;
    }

    public boolean isSendQueryNumForNotRealPrepare() {
        return this.sendQueryNumForNotRealPrepare;
    }

    public boolean isHaveEmptyXlobWithOid() {
        return this.haveEmptyXlobWithOid;
    }

    public boolean isNewParamInfoPacket() {
        return this.newParamInfoPacket;
    }

    public boolean isReceiveErrorCode() {
        return this.receiveErrorCode;
    }

    public int getDBMajorVersion() {
        return this.DBMajorVersion;
    }

    public int getDBMinorVersion() {
        return this.DBMinorVersion;
    }

    public int getDriverMajorVersion() {
        return this.DriverMajorVersion;
    }

    public int getDriverMinorVersion() {
        return this.DriverMinorVersion;
    }
}

