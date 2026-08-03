/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.packets;

import com.oscar.Driver;
import com.oscar.core.Encoding;
import com.oscar.protocol.OSCARProtocol;
import com.oscar.protocol.packets.BasePacket;
import com.oscar.util.SystemInformation;
import com.oscar.util.VersionConfig;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

public class StartupPacket
extends BasePacket {
    private static final char tag = 's';
    private static final int SM_SIZE = 4;
    private static final int SM_VERSION = 4;
    private int SM_DATABASE = 64;
    private int SM_USER = 32;
    private static final int SM_OPTIONS = 64;
    private static final int SM_COMPATIBLE = 6;
    private static final int SM_ProtoLowVersion = 2;
    private static final int SM_ProtoHighVersion = 2;
    private static final int ProtoLowVersionValue = 1;
    private static final int ProtoHighVersionValue = 4;
    private static final int SM_UNUSED = 54;
    private static final int SM_TTY = 64;
    private static final int SM_DOMAIN = 32;
    private static final int SM_OS = 4;
    private static final int SM_ROLE = 4;
    private static final int SM_GROUP = 97;
    private static final int SM_GROUPS = 1940;
    private static final int SM_GROUPCOUNT = 4;
    private static final int SM_HOST = 64;
    private static final int SM_MAC = 32;
    private static final int SM_PROTOCAL = 4;
    private static final int SM_APPLICATION = 64;
    private SystemInformation info;
    private int protocolMajor = 2;
    private int protocolMinor = 0;
    private String user;
    private String database;
    private String options;
    private boolean nameSensitive;
    private String os_user;
    private String os_domain = "";
    private String[] groups;
    private int groupCount;
    private static final int GROUP_LENGTH = 20;
    private String hostName;
    private String mac;
    private int netProtocal;
    private String application;
    private int role;
    private byte[] fingerPrintInfo;
    private String authType;
    private Encoding itsEncode = null;
    private int encodingType;
    private boolean compatibleOldProtocal = false;
    private boolean sendOldStartupPacket = true;
    private boolean receiveStringByLen = true;

    public StartupPacket(int protocolMajor, int protocolMinor, String database, String user, Properties propInfo, Encoding currentEncoding, VersionConfig version) throws SQLException {
        this.protocolMajor = protocolMajor;
        this.protocolMinor = protocolMinor;
        this.database = database;
        this.authType = propInfo.getProperty("AUTH_TYPE", "PW");
        this.application = propInfo.getProperty("APPLICATION");
        this.itsEncode = currentEncoding;
        this.encodingType = currentEncoding.getEncodingType();
        this.version = version;
        this.receiveStringByLen = Boolean.valueOf(propInfo.getProperty("RECEIVESTRINGBYLEN", "true"));
        if (currentEncoding.encode(database).length > this.SM_DATABASE - 1 || currentEncoding.encode(user).length > this.SM_USER - 1) {
            this.sendOldStartupPacket = false;
        }
        if (this.authType.equalsIgnoreCase("PW")) {
            this.user = user;
            this.init();
            this.groups = new String[20];
            for (int i = 0; i < this.groups.length; ++i) {
                this.groups[i] = "";
            }
            this.role = Integer.parseInt(propInfo.getProperty("ROLE", "0"));
        } else if (this.authType.equalsIgnoreCase("RA")) {
            this.user = user;
            this.init();
            this.groups = new String[20];
            for (int i = 0; i < this.groups.length; ++i) {
                this.groups[i] = "";
            }
            this.role = Integer.parseInt(propInfo.getProperty("ROLE", "0"));
        } else if (this.authType.equalsIgnoreCase("OS")) {
            this.init();
            this.os_user = this.info.getUser();
            this.os_domain = this.info.getUserDomain();
            this.groupCount = this.info.getGroupCount();
            this.groups = this.info.getGroups();
            this.role = Integer.parseInt(propInfo.getProperty("ROLE", "0"));
        } else if (this.authType.equalsIgnoreCase("FP")) {
            this.user = user;
            this.init();
            this.fingerPrintInfo = (byte[])propInfo.get("FINGERPRINT");
            this.role = Integer.parseInt(propInfo.getProperty("ROLE", "0"));
        }
        this.options = propInfo.getProperty("OPTIONS");
        this.nameSensitive = Boolean.valueOf(propInfo.getProperty("NAMESENSITIVE"));
    }

    public void init() throws SQLException {
        this.info = SystemInformation.getInstance(this.authType.equalsIgnoreCase("OS"));
        this.hostName = this.info.getHostName();
        this.netProtocal = this.info.getNetProtocal();
        this.mac = this.info.getMac();
        if (this.application == null) {
            this.application = this.info.getApplication();
        }
    }

    public void sendTo(BufferedOutputStream stream) throws IOException, SQLException {
        if (this.logFlag) {
            this.sb.delete(0, this.sb.length());
            this.sb.append("***********************************************************").append("\n");
            this.sb.append("session: ").append(this.conn.getSessionID()).append(", send: ").append("\n");
        }
        if (!this.sendOldStartupPacket) {
            this.SM_DATABASE = 128;
            this.SM_USER = 128;
        }
        int totalLen = 8 + this.SM_DATABASE + this.SM_USER + 64 + 6 + 2 + 2 + 54 + 64 + 32 + 1940 + 4 + 4 + 4 + 64 + 32 + 4 + 64;
        BasePacket.SendInteger(stream, totalLen, 4);
        BasePacket.SendInteger(stream, this.protocolMajor, 2);
        BasePacket.SendInteger(stream, this.protocolMinor, 2);
        BasePacket.SendString(stream, this.database, this.SM_DATABASE, this.itsEncode);
        if (this.logFlag) {
            this.sb.append("startUp: S, authType: " + this.authType);
            this.sb.append(", totalLen: " + totalLen).append(", protocolMajor: " + this.protocolMajor).append(", protocolMinor: " + this.protocolMinor).append(", database: " + this.database);
        }
        if (this.authType.equalsIgnoreCase("PW")) {
            if (this.options != null && this.options.length() > 0) {
                BasePacket.SendString(stream, this.user, this.SM_USER, this.itsEncode);
                BasePacket.SendString(stream, this.options, 64, this.itsEncode);
            } else {
                BasePacket.SendString(stream, this.user, this.SM_USER + 64, this.itsEncode);
            }
            this.sendCompatibleProperty(stream);
            BasePacket.SendString(stream, "", 64, this.itsEncode);
            BasePacket.SendString(stream, this.os_domain, 32, this.itsEncode);
            for (int i = 0; i < this.groups.length; ++i) {
                BasePacket.SendString(stream, this.groups[i], 97, this.itsEncode);
            }
            BasePacket.SendInteger(stream, this.groupCount, 4);
            BasePacket.SendInteger(stream, 0, 4);
            if (this.logFlag) {
                this.sb.append(", user: " + this.user).append(", os_domain: " + this.os_domain).append(", group: ");
                for (String group : this.groups) {
                    this.sb.append(group + " ");
                }
                this.sb.append(", groupCount: " + this.groupCount);
            }
        } else if (this.authType.equalsIgnoreCase("RA")) {
            if (this.options != null && this.options.length() > 0) {
                BasePacket.SendString(stream, this.user, this.SM_USER, this.itsEncode);
                BasePacket.SendString(stream, this.options, 64, this.itsEncode);
            } else {
                BasePacket.SendString(stream, this.user, this.SM_USER + 64, this.itsEncode);
            }
            this.sendCompatibleProperty(stream);
            BasePacket.SendString(stream, "", 64, this.itsEncode);
            BasePacket.SendString(stream, this.os_domain, 32, this.itsEncode);
            for (int i = 0; i < this.groups.length; ++i) {
                BasePacket.SendString(stream, this.groups[i], 97, this.itsEncode);
            }
            BasePacket.SendInteger(stream, this.groupCount, 4);
            BasePacket.SendInteger(stream, 3, 4);
            if (this.logFlag) {
                this.sb.append(", user: " + this.user).append(", os_domain: " + this.os_domain).append(", group: ");
                for (String group : this.groups) {
                    this.sb.append(group + " ");
                }
                this.sb.append(", groupCount: " + this.groupCount);
            }
        } else if (this.authType.equalsIgnoreCase("FP")) {
            if (this.options != null && this.options.length() > 0) {
                BasePacket.SendString(stream, this.user, this.SM_USER, this.itsEncode);
                BasePacket.SendString(stream, this.options, 64, this.itsEncode);
            } else {
                BasePacket.SendString(stream, this.user, this.SM_USER + 64, this.itsEncode);
            }
            this.sendCompatibleProperty(stream);
            BasePacket.SendString(stream, "", 64, this.itsEncode);
            BasePacket.Send(stream, this.fingerPrintInfo, 1976);
            BasePacket.SendInteger(stream, 2, 4);
            if (this.logFlag) {
                this.sb.append(", user: " + this.user).append(", fingerPrintInfo: ");
                for (byte finger : this.fingerPrintInfo) {
                    this.sb.append(finger + " ");
                }
                this.sb.append("| ");
            }
        } else if (this.authType.equalsIgnoreCase("OS")) {
            if (!this.nameSensitive) {
                this.os_user = OSCARProtocol.convertString(this.os_user);
            }
            if (this.options != null && this.options.length() > 0) {
                BasePacket.SendString(stream, this.os_user, this.SM_USER, this.itsEncode);
                BasePacket.SendString(stream, this.options, 64, this.itsEncode);
            } else {
                BasePacket.SendString(stream, this.os_user, this.SM_USER + 64, this.itsEncode);
            }
            this.sendCompatibleProperty(stream);
            BasePacket.SendString(stream, "", 64, this.itsEncode);
            BasePacket.SendString(stream, this.os_domain, 32, this.itsEncode);
            for (int i = 0; i < this.groups.length; ++i) {
                BasePacket.SendString(stream, this.groups[i], 97, this.itsEncode);
            }
            BasePacket.SendInteger(stream, this.groupCount, 4);
            BasePacket.SendInteger(stream, 1, 4);
            if (this.logFlag) {
                this.sb.append(", os_user: " + this.os_user).append(", os_domain: " + this.os_domain).append(", group: ");
                for (String group : this.groups) {
                    this.sb.append(group + " ");
                }
                this.sb.append(", groupCount: " + this.groupCount);
            }
        }
        BasePacket.SendInteger(stream, this.role, 4);
        BasePacket.SendString(stream, this.hostName, 64, this.itsEncode);
        BasePacket.SendString(stream, this.mac, 32, this.itsEncode);
        BasePacket.SendInteger(stream, this.netProtocal, 4);
        BasePacket.SendString(stream, this.application, 64, this.itsEncode);
        if (this.logFlag) {
            this.sb.append(", role: " + this.role).append(", hostName: " + this.hostName).append(", mac: " + this.mac).append(", netProtocal: " + this.netProtocal).append(", application: " + this.application);
        }
        stream.flush();
        if (this.logFlag) {
            this.sb.append("\n").append("***********************************************************");
            Driver.writeLog(this.sb.toString());
        }
    }

    private void sendCompatibleProperty(BufferedOutputStream stream) throws IOException, SQLException {
        BasePacket.SendChar(stream, 255);
        BasePacket.SendChar(stream, this.encodingType);
        if (this.version.isWuziVersion()) {
            BasePacket.SendChar(stream, 255);
        } else {
            BasePacket.SendChar(stream, 0);
        }
        if (this.compatibleOldProtocal) {
            BasePacket.SendChar(stream, 0);
        } else {
            BasePacket.SendChar(stream, 4);
        }
        if (this.version.isReceiveErrorCode()) {
            BasePacket.SendChar(stream, 1);
        } else {
            BasePacket.SendChar(stream, 0);
        }
        if (this.receiveStringByLen) {
            BasePacket.SendChar(stream, 255);
        } else {
            BasePacket.SendChar(stream, 0);
        }
        BasePacket.SendInteger(stream, 1, 2);
        BasePacket.SendInteger(stream, 4, 2);
        BasePacket.SendString(stream, "", 54, this.itsEncode);
    }

    public void receiveFrom(InputStream stream) throws IOException, SQLException {
    }

    public char getTag() {
        return 's';
    }

    public void setCompatibleOldProtocal(boolean compatibleOldProtocal) {
        this.compatibleOldProtocal = compatibleOldProtocal;
    }
}

