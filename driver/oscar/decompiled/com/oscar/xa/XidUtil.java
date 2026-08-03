/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.xa;

import java.sql.SQLException;
import javax.transaction.xa.Xid;

public class XidUtil {
    public static String xitToString(Xid xid) throws SQLException {
        return "'" + XidUtil.byte2HexStr(xid.getGlobalTransactionId()) + "' " + "'" + XidUtil.byte2HexStr(xid.getBranchQualifier()) + "' " + xid.getFormatId();
    }

    public static String byte2HexStr(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; ++n) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            hs = stmp.length() == 1 ? hs + "0" + stmp : hs + stmp;
        }
        return hs.toUpperCase();
    }
}

