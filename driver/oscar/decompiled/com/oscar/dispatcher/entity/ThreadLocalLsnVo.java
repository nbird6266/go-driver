/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.entity;

import com.oscar.Driver;
import com.oscar.dispatcher.entity.LsnVo;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.jdbc.OscarStatement;
import com.oscar.protocol.packets.SetQueryLsnPacket;
import java.sql.SQLException;
import java.sql.Statement;

public class ThreadLocalLsnVo
implements LsnVo {
    public long masterLsn = 512L;

    public long getMasterLsn() {
        return this.masterLsn;
    }

    public void setMasterLsn(long masterLsn) {
        this.masterLsn = masterLsn;
    }

    public long sendLsn(Statement st) throws SQLException {
        Driver.writeLog("Thread LSN send");
        if (st instanceof OscarStatement) {
            OscarJdbc2Connection con = (OscarJdbc2Connection)st.getConnection();
            if (con == null || con.isClosed() || con.getProtocol() == null) {
                return 0L;
            }
            if (this.masterLsn > con.getLsnValue()) {
                con.getProtocol().sendLsn(new SetQueryLsnPacket(this.masterLsn), con);
                return this.masterLsn;
            }
        }
        return 0L;
    }
}

