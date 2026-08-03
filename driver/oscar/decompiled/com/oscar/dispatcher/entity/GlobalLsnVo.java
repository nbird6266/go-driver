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
import java.util.concurrent.atomic.AtomicLong;

public class GlobalLsnVo
implements LsnVo {
    public AtomicLong atomicMasterLsn = new AtomicLong(512L);

    public long getMasterLsn() {
        return this.atomicMasterLsn.get();
    }

    public void setMasterLsn(long masterLsn) {
        long old_lsn;
        while (masterLsn > (old_lsn = this.atomicMasterLsn.get()) && !this.atomicMasterLsn.compareAndSet(old_lsn, masterLsn)) {
        }
    }

    public long sendLsn(Statement st) throws SQLException {
        Driver.writeLog("Global LSN send");
        if (st instanceof OscarStatement) {
            OscarJdbc2Connection con = (OscarJdbc2Connection)st.getConnection();
            if (con == null || con.isClosed() || con.getProtocol() == null) {
                return 0L;
            }
            long updateLsnValue = this.atomicMasterLsn.get();
            if (updateLsnValue > con.getLsnValue()) {
                con.getProtocol().sendLsn(new SetQueryLsnPacket(updateLsnValue), con);
                return updateLsnValue;
            }
        }
        return 0L;
    }
}

