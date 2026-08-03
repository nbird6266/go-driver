/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol;

import com.oscar.util.VersionConfig;

public class ProtocolVersion {
    public static final int PROTOCOL_OLD = 1;
    public static final int PROTOCOL_V2 = 2;
    public static final int PROTOCOL_V3 = 3;
    public static final int PROTOCOL_V4 = 4;
    private VersionConfig version;
    private int protocolType = 2;
    private boolean isMpp5 = false;

    public boolean isMpp5() {
        return this.isMpp5;
    }

    public void setMpp5(boolean isMpp5) {
        this.isMpp5 = isMpp5;
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }

    public int getProtocolType() {
        return this.protocolType;
    }

    public void setVersion(VersionConfig version) {
        this.version = version;
    }

    public VersionConfig getVersion() {
        return this.version;
    }
}

