/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.fastpath;

import com.oscar.core.BaseConnection;
import com.oscar.fastpath.FastpathArg;
import com.oscar.protocol.packets.BasePacket;
import com.oscar.util.OSQLException;
import java.sql.SQLException;
import java.util.Hashtable;

public class Fastpath {
    private static Hashtable LOB_FUNC_MAP = new Hashtable();
    public static final int VARDATA_TYPE = 1;
    public static final int INT_TYPE = 2;
    public static final int LONG_TYPE = 3;
    public static final String FUN_OPEN = "OPEN";
    public static final String FUN_ISOPEN = "ISOPEN";
    public static final String FUN_CLOSE = "CLOSE";
    public static final String FUN_CREATETEMP = "CREATETEMP";
    public static final String FUN_FREETEMP = "FREETEMP";
    public static final String FUN_GETLENGTH = "GETLENGTH";
    public static final String FUN_GETPRECISELENGTH = "GETPRECISELENGTH";
    public static final String FUN_READ = "READ";
    public static final String FUN_READCOMPRESS = "READCOMPRESS";
    public static final String FUN_WRITE = "WRITE";
    public static final String FUN_WRITECOMPRESS = "WRITECOMPRESS";
    public static final String FUN_ERASE = "ERASE";
    public static final String FUN_TRIM = "TRIM";
    public static final String FUN_INSERT = "INSERT";
    public static final String FUN_DELETE = "DELETE";
    public static final String FUN_INSTR = "INSTR";
    public static final String FUN_WRITEAPPEND = "WRITEAPPEND";
    public static final String FUN_INTERNALREAD = "INTERNALREAD";
    public static final String FUN_GETPAGENO = "GETPAGENO";
    public static final String FUN_GETPAGEPOS = "GETPAGEPOS";
    public static final String FUN_CREATE_WITH_TABLE_NAME = "CREATE_WITH_TABLE_NAME";
    public static final String FUN_CREATE_WITH_TABLE_OID = "CREATE_WITH_TABLE_OID";
    public static final String FUN_FREE_WITH_TABLE_OID = "FREE_WITH_TABLE_OID";
    public static final String FUN_GET_CHUNKSIZE = "GET_CHUNKSIZE";
    public static final String FUN_GET_NAME = "GET_NAME";
    public static final String FUN_EXISTS = "EXISTS";
    private static int[] lobOids;
    protected BaseConnection conn;

    public Fastpath(BaseConnection conn) {
        this.conn = conn;
    }

    private static void addFuncDefine(int version, int dataType, String funName, int funOid) {
        Hashtable<String, Integer> funcMapByDataType;
        Hashtable<Integer, Hashtable<String, Integer>> funcMapByVersion = (Hashtable<Integer, Hashtable<String, Integer>>)LOB_FUNC_MAP.get(new Integer(version));
        if (funcMapByVersion == null) {
            funcMapByVersion = new Hashtable<Integer, Hashtable<String, Integer>>();
            LOB_FUNC_MAP.put(new Integer(version), funcMapByVersion);
        }
        if ((funcMapByDataType = (Hashtable<String, Integer>)funcMapByVersion.get(new Integer(dataType))) == null) {
            funcMapByDataType = new Hashtable<String, Integer>();
            funcMapByVersion.put(new Integer(dataType), funcMapByDataType);
        }
        funcMapByDataType.put(funName, new Integer(funOid));
    }

    private Object fastpath(int fnid, int resulttype, FastpathArg[] args) throws SQLException {
        int i;
        Object ret = null;
        int[] paraCount = new int[args.length];
        Object[] paraValue = new Object[args.length];
        for (i = 0; i < args.length; ++i) {
            paraCount[i] = args[i].sendSize();
        }
        block11: for (i = 0; i < args.length; ++i) {
            switch (args[i].type) {
                case 1: {
                    paraValue[i] = new Integer(args[i].intValue);
                    continue block11;
                }
                case 2: {
                    paraValue[i] = new Long(args[i].longValue);
                    continue block11;
                }
                case 3: {
                    paraValue[i] = args[i].bytes;
                }
            }
        }
        Object[] result = this.conn.getProtocol().functionCall(fnid, args.length, paraCount, paraValue);
        if (!((Boolean)result[0]).booleanValue()) {
            int sz = (Integer)result[1];
            switch (resulttype) {
                case 1: {
                    ret = result[2];
                    break;
                }
                case 2: {
                    ret = new Integer(BasePacket.bytesToIntR((byte[])result[2], sz));
                    break;
                }
                case 3: {
                    ret = new Long(BasePacket.bytesToLong((byte[])result[2], sz));
                }
            }
        } else {
            ret = null;
        }
        return ret;
    }

    public Object fastpath(String name, int lobType, int resulttype, FastpathArg[] args) throws SQLException {
        return this.fastpath(this.getFuncID(name, lobType), resulttype, args);
    }

    public int getInteger(int lobType, String name, FastpathArg[] args) throws SQLException {
        Integer i = (Integer)this.fastpath(name, lobType, 2, args);
        if (i == null) {
            throw new OSQLException("OSCAR-00102", "88888", 102);
        }
        return i;
    }

    public byte[] getData(int lobType, String name, FastpathArg[] args) throws SQLException {
        return (byte[])this.fastpath(name, lobType, 1, args);
    }

    public long getLong(int lobType, String name, FastpathArg[] args) throws SQLException {
        return (Long)this.fastpath(name, lobType, 3, args);
    }

    public int getFuncID(String name, int lobType) throws SQLException {
        Hashtable funcMapByVersion;
        Hashtable funcMapByDataType;
        Integer id = null;
        int version = this.conn.getVersion().getMainVersion();
        if (version == 3) {
            version = 2;
        }
        if ((id = (Integer)(funcMapByDataType = (Hashtable)(funcMapByVersion = (Hashtable)LOB_FUNC_MAP.get(new Integer(version))).get(new Integer(lobType))).get(name)) == null) {
            throw new OSQLException("OSCAR-00102", "88888", 102);
        }
        return id;
    }

    static {
        Fastpath.addFuncDefine(1, 2, FUN_OPEN, 3015);
        Fastpath.addFuncDefine(1, 1, FUN_OPEN, 3011);
        Fastpath.addFuncDefine(1, 2, FUN_CLOSE, 3016);
        Fastpath.addFuncDefine(1, 1, FUN_CLOSE, 3016);
        Fastpath.addFuncDefine(1, 2, FUN_CREATETEMP, 3033);
        Fastpath.addFuncDefine(1, 1, FUN_CREATETEMP, 3021);
        Fastpath.addFuncDefine(1, 2, FUN_GETLENGTH, 3034);
        Fastpath.addFuncDefine(1, 1, FUN_GETLENGTH, 3022);
        Fastpath.addFuncDefine(1, 2, FUN_GETPRECISELENGTH, 3035);
        Fastpath.addFuncDefine(1, 1, FUN_GETPRECISELENGTH, 3023);
        Fastpath.addFuncDefine(1, 2, FUN_READ, 3036);
        Fastpath.addFuncDefine(1, 1, FUN_READ, 3024);
        Fastpath.addFuncDefine(1, 2, FUN_WRITE, 3037);
        Fastpath.addFuncDefine(1, 1, FUN_WRITE, 3025);
        Fastpath.addFuncDefine(1, 2, FUN_ERASE, 3038);
        Fastpath.addFuncDefine(1, 1, FUN_ERASE, 3026);
        Fastpath.addFuncDefine(1, 2, FUN_INSERT, 3039);
        Fastpath.addFuncDefine(1, 1, FUN_INSERT, 3027);
        Fastpath.addFuncDefine(1, 2, FUN_DELETE, 3018);
        Fastpath.addFuncDefine(1, 1, FUN_DELETE, 3014);
        Fastpath.addFuncDefine(1, 2, FUN_INSTR, 3042);
        Fastpath.addFuncDefine(1, 1, FUN_INSTR, 3030);
        Fastpath.addFuncDefine(1, 2, FUN_WRITEAPPEND, 3046);
        Fastpath.addFuncDefine(1, 1, FUN_WRITEAPPEND, 3043);
        Fastpath.addFuncDefine(1, 2, FUN_INTERNALREAD, 4308);
        Fastpath.addFuncDefine(1, 1, FUN_INTERNALREAD, 4306);
        Fastpath.addFuncDefine(1, 2, FUN_GETPAGENO, 4310);
        Fastpath.addFuncDefine(1, 1, FUN_GETPAGENO, 4310);
        Fastpath.addFuncDefine(1, 2, FUN_GETPAGEPOS, 4311);
        Fastpath.addFuncDefine(1, 1, FUN_GETPAGEPOS, 4311);
        Fastpath.addFuncDefine(1, 2, FUN_CREATE_WITH_TABLE_NAME, 3033);
        Fastpath.addFuncDefine(1, 1, FUN_CREATE_WITH_TABLE_NAME, 3021);
        Fastpath.addFuncDefine(1, 2, FUN_CREATE_WITH_TABLE_OID, 3033);
        Fastpath.addFuncDefine(1, 1, FUN_CREATE_WITH_TABLE_OID, 3021);
        Fastpath.addFuncDefine(2, 2, FUN_OPEN, 3015);
        Fastpath.addFuncDefine(2, 1, FUN_OPEN, 3011);
        Fastpath.addFuncDefine(2, 2, FUN_CLOSE, 3016);
        Fastpath.addFuncDefine(2, 1, FUN_CLOSE, 3016);
        Fastpath.addFuncDefine(2, 2, FUN_CREATETEMP, 5708);
        Fastpath.addFuncDefine(2, 1, FUN_CREATETEMP, 5709);
        Fastpath.addFuncDefine(2, 2, FUN_FREETEMP, 5710);
        Fastpath.addFuncDefine(2, 1, FUN_FREETEMP, 5711);
        Fastpath.addFuncDefine(2, 2, FUN_GETLENGTH, 3034);
        Fastpath.addFuncDefine(2, 1, FUN_GETLENGTH, 3022);
        Fastpath.addFuncDefine(2, 2, FUN_GETPRECISELENGTH, 3035);
        Fastpath.addFuncDefine(2, 1, FUN_GETPRECISELENGTH, 3023);
        Fastpath.addFuncDefine(2, 2, FUN_READ, 3036);
        Fastpath.addFuncDefine(2, 1, FUN_READ, 3024);
        Fastpath.addFuncDefine(2, 2, FUN_WRITE, 3037);
        Fastpath.addFuncDefine(2, 1, FUN_WRITE, 3025);
        Fastpath.addFuncDefine(2, 2, FUN_ERASE, 3038);
        Fastpath.addFuncDefine(2, 1, FUN_ERASE, 3026);
        Fastpath.addFuncDefine(2, 2, FUN_INSERT, 3039);
        Fastpath.addFuncDefine(2, 1, FUN_INSERT, 3027);
        Fastpath.addFuncDefine(2, 2, FUN_DELETE, 3018);
        Fastpath.addFuncDefine(2, 1, FUN_DELETE, 3014);
        Fastpath.addFuncDefine(2, 2, FUN_INSTR, 3042);
        Fastpath.addFuncDefine(2, 1, FUN_INSTR, 3030);
        Fastpath.addFuncDefine(2, 2, FUN_WRITEAPPEND, 3046);
        Fastpath.addFuncDefine(2, 1, FUN_WRITEAPPEND, 3043);
        Fastpath.addFuncDefine(2, 2, FUN_INTERNALREAD, 4308);
        Fastpath.addFuncDefine(2, 1, FUN_INTERNALREAD, 4306);
        Fastpath.addFuncDefine(2, 2, FUN_GETPAGENO, 4310);
        Fastpath.addFuncDefine(2, 1, FUN_GETPAGENO, 4310);
        Fastpath.addFuncDefine(2, 2, FUN_GETPAGEPOS, 4311);
        Fastpath.addFuncDefine(2, 1, FUN_GETPAGEPOS, 4311);
        Fastpath.addFuncDefine(2, 2, FUN_CREATE_WITH_TABLE_NAME, 5715);
        Fastpath.addFuncDefine(2, 1, FUN_CREATE_WITH_TABLE_NAME, 5714);
        Fastpath.addFuncDefine(2, 2, FUN_CREATE_WITH_TABLE_OID, 5717);
        Fastpath.addFuncDefine(2, 1, FUN_CREATE_WITH_TABLE_OID, 5716);
        Fastpath.addFuncDefine(4, 3, FUN_OPEN, 2994);
        Fastpath.addFuncDefine(4, 2, FUN_OPEN, 2989);
        Fastpath.addFuncDefine(4, 1, FUN_OPEN, 2988);
        Fastpath.addFuncDefine(4, 3, FUN_ISOPEN, 2995);
        Fastpath.addFuncDefine(4, 2, FUN_ISOPEN, 2991);
        Fastpath.addFuncDefine(4, 1, FUN_ISOPEN, 2990);
        Fastpath.addFuncDefine(4, 3, FUN_CLOSE, 2996);
        Fastpath.addFuncDefine(4, 2, FUN_CLOSE, 2993);
        Fastpath.addFuncDefine(4, 1, FUN_CLOSE, 2992);
        Fastpath.addFuncDefine(4, 2, FUN_CREATETEMP, 2983);
        Fastpath.addFuncDefine(4, 1, FUN_CREATETEMP, 2982);
        Fastpath.addFuncDefine(4, 2, FUN_FREETEMP, 2987);
        Fastpath.addFuncDefine(4, 1, FUN_FREETEMP, 2986);
        Fastpath.addFuncDefine(4, 3, FUN_GETLENGTH, 2972);
        Fastpath.addFuncDefine(4, 2, FUN_GETLENGTH, 2971);
        Fastpath.addFuncDefine(4, 1, FUN_GETLENGTH, 2970);
        Fastpath.addFuncDefine(4, 3, FUN_GETPRECISELENGTH, 2972);
        Fastpath.addFuncDefine(4, 2, FUN_GETPRECISELENGTH, 2971);
        Fastpath.addFuncDefine(4, 1, FUN_GETPRECISELENGTH, 2970);
        Fastpath.addFuncDefine(4, 3, FUN_READ, 2978);
        Fastpath.addFuncDefine(4, 2, FUN_READ, 2977);
        Fastpath.addFuncDefine(4, 1, FUN_READ, 2976);
        Fastpath.addFuncDefine(4, 3, FUN_WRITE, 3018);
        Fastpath.addFuncDefine(4, 2, FUN_WRITE, 2962);
        Fastpath.addFuncDefine(4, 1, FUN_WRITE, 2961);
        Fastpath.addFuncDefine(4, 2, FUN_ERASE, 2954);
        Fastpath.addFuncDefine(4, 1, FUN_ERASE, 2953);
        Fastpath.addFuncDefine(4, 2, FUN_TRIM, 2960);
        Fastpath.addFuncDefine(4, 1, FUN_TRIM, 2959);
        Fastpath.addFuncDefine(4, 2, FUN_DELETE, 2960);
        Fastpath.addFuncDefine(4, 1, FUN_DELETE, 2959);
        Fastpath.addFuncDefine(4, 3, FUN_INSTR, 2975);
        Fastpath.addFuncDefine(4, 2, FUN_INSTR, 2974);
        Fastpath.addFuncDefine(4, 1, FUN_INSTR, 2973);
        Fastpath.addFuncDefine(4, 2, FUN_WRITEAPPEND, 2964);
        Fastpath.addFuncDefine(4, 1, FUN_WRITEAPPEND, 2963);
        Fastpath.addFuncDefine(4, 2, FUN_GET_CHUNKSIZE, 2969);
        Fastpath.addFuncDefine(4, 1, FUN_GET_CHUNKSIZE, 2968);
        Fastpath.addFuncDefine(4, 2, FUN_CREATE_WITH_TABLE_OID, 3004);
        Fastpath.addFuncDefine(4, 1, FUN_CREATE_WITH_TABLE_OID, 3003);
        Fastpath.addFuncDefine(4, 1, FUN_FREE_WITH_TABLE_OID, 3005);
        Fastpath.addFuncDefine(4, 2, FUN_FREE_WITH_TABLE_OID, 3006);
        Fastpath.addFuncDefine(4, 1, FUN_WRITECOMPRESS, 3019);
        Fastpath.addFuncDefine(4, 1, FUN_READCOMPRESS, 3018);
        Fastpath.addFuncDefine(4, 3, FUN_GET_NAME, 3002);
        Fastpath.addFuncDefine(4, 3, FUN_EXISTS, 3001);
    }
}

