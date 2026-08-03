/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.core.BaseConnection;
import com.oscar.jdbc.entity.ParamInfo;
import com.oscar.util.OSQLException;
import com.oscar.util.Word;
import com.oscar.util.Yylex;
import java.io.CharArrayReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class OscarSqlProcessor {
    protected static final short IN_SQLCODE = 0;
    protected static final short IN_STRING = 1;
    protected static final short BACKSLASH = 2;
    protected static final short ESC_TIMEDATE = 3;
    protected static final short ESC_FN = 4;
    protected static final short ESC_OJ = 5;
    protected static final short ESC_ESC = 6;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static ParseResult parsing(String sql) throws OSQLException {
        if (sql == null || sql.trim().length() == 0) {
            throw new OSQLException("OSCAR-00433", "88888", 433);
        }
        int into = 0;
        int semicolon = 0;
        int firstEffectiveWordIndex = -1;
        ArrayList<Word> allWords = new ArrayList<Word>();
        ArrayList<Word> questionMarks = new ArrayList<Word>();
        Yylex lex = null;
        int colon_index = -1;
        String transSql = null;
        try {
            Word word;
            lex = new Yylex(new CharArrayReader(sql.toCharArray()), sql.length());
            while ((word = lex.yylex()).getType() != 0) {
                allWords.add(word);
                switch (word.getType()) {
                    case 0: {
                        break;
                    }
                    case 102: 
                    case 121: {
                        if (firstEffectiveWordIndex != -1) break;
                        firstEffectiveWordIndex = allWords.size() - 1;
                        break;
                    }
                    case 101: {
                        if (firstEffectiveWordIndex != -1) break;
                        firstEffectiveWordIndex = allWords.size() - 1;
                        break;
                    }
                    case 100: 
                    case 120: {
                        if (firstEffectiveWordIndex != -1) break;
                        firstEffectiveWordIndex = allWords.size() - 1;
                        break;
                    }
                    case 103: {
                        ++into;
                        break;
                    }
                    case 105: 
                    case 118: 
                    case 119: {
                        if (firstEffectiveWordIndex != -1) break;
                        firstEffectiveWordIndex = allWords.size() - 1;
                        break;
                    }
                    case 104: 
                    case 122: {
                        if (firstEffectiveWordIndex != -1) break;
                        firstEffectiveWordIndex = allWords.size() - 1;
                        break;
                    }
                    case 106: {
                        if (firstEffectiveWordIndex != -1) break;
                        firstEffectiveWordIndex = allWords.size() - 1;
                        break;
                    }
                    case 112: {
                        break;
                    }
                    case 109: 
                    case 123: {
                        if (firstEffectiveWordIndex != -1) break;
                        firstEffectiveWordIndex = allWords.size() - 1;
                        break;
                    }
                    case 110: {
                        if (firstEffectiveWordIndex != -1) break;
                        firstEffectiveWordIndex = allWords.size() - 1;
                        break;
                    }
                    case 113: {
                        if (firstEffectiveWordIndex != -1) break;
                        firstEffectiveWordIndex = allWords.size() - 1;
                        break;
                    }
                    case 111: {
                        if (firstEffectiveWordIndex != -1) break;
                        firstEffectiveWordIndex = allWords.size() - 1;
                        break;
                    }
                    case 115: {
                        if (firstEffectiveWordIndex != -1) break;
                        firstEffectiveWordIndex = allWords.size() - 1;
                        break;
                    }
                    case 114: {
                        if (firstEffectiveWordIndex != -1) break;
                        firstEffectiveWordIndex = allWords.size() - 1;
                        break;
                    }
                    case 150: 
                    case 151: 
                    case 152: 
                    case 153: 
                    case 154: 
                    case 155: 
                    case 156: {
                        break;
                    }
                    case 300: {
                        break;
                    }
                    case 116: 
                    case 117: {
                        if (firstEffectiveWordIndex != -1) break;
                        firstEffectiveWordIndex = allWords.size() - 1;
                        break;
                    }
                    case 10: {
                        ++semicolon;
                        break;
                    }
                    case 14: {
                        questionMarks.add(word);
                        break;
                    }
                    case 16: {
                        colon_index = word.getTokenBegin();
                        questionMarks.add(word);
                        String preColonSql = null;
                        if (colon_index == 0) {
                            String token = word.getTokentext();
                            if (transSql == null) {
                                transSql = sql.substring(0, sql.length() - token.length()) + "?";
                                break;
                            }
                            transSql = transSql.substring(0, transSql.length() - token.length()) + "?";
                            break;
                        }
                        if (transSql == null) {
                            preColonSql = sql.substring(0, colon_index);
                        } else {
                            int offset = sql.length() - transSql.length();
                            preColonSql = transSql.substring(0, colon_index - offset);
                        }
                        String aferColonSql = sql.substring(word.getTokenEnd());
                        transSql = preColonSql + "?" + aferColonSql;
                        break;
                    }
                }
            }
            lex.yyclose();
        }
        catch (Exception e) {
            e.printStackTrace();
            ParseResult preColonSql = null;
            return preColonSql;
        }
        finally {
            if (lex != null) {
                try {
                    lex.yyclose();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        ParseResult pr = new ParseResult();
        boolean isWithFunction = false;
        if (firstEffectiveWordIndex == -1) {
            pr.selectSql = false;
            pr.ddlSql = false;
        } else {
            Word firstEffectiveWord = (Word)allWords.get(firstEffectiveWordIndex);
            if (firstEffectiveWord.getType() != 109) {
                if (firstEffectiveWord.getType() == 104) {
                    pr.insertSql = true;
                    pr.dmlSql = true;
                } else if (firstEffectiveWord.getType() == 105 || firstEffectiveWord.getType() == 106 || firstEffectiveWord.getType() == 114 || firstEffectiveWord.getType() == 122) {
                    pr.dmlSql = true;
                } else if (firstEffectiveWord.getType() == 115 || firstEffectiveWord.getType() == 101 || firstEffectiveWord.getType() == 113 || firstEffectiveWord.getType() == 110 || firstEffectiveWord.getType() == 119 || firstEffectiveWord.getType() == 118) {
                    pr.ddlSql = true;
                } else if (firstEffectiveWord.getType() == 102) {
                    if (into > 0) {
                        pr.dmlSql = true;
                    } else {
                        pr.selectSql = true;
                    }
                } else if (firstEffectiveWord.getType() == 100 || firstEffectiveWord.getType() == 120 || firstEffectiveWord.getType() == 121) {
                    pr.normalSelect = true;
                }
                if (firstEffectiveWord.getType() == 121) {
                    if (((Word)allWords.get(firstEffectiveWordIndex + 1)).getTokentext().equalsIgnoreCase("function")) {
                        isWithFunction = true;
                        pr.selectSql = false;
                    } else {
                        pr.selectSql = true;
                    }
                }
            }
        }
        pr.questionMarks = questionMarks;
        if (transSql != null) {
            pr.transSql = transSql;
        } else {
            pr.transSql = sql;
        }
        if (isWithFunction) {
            pr.sqlCounts = 1;
        } else {
            int sqlCount = semicolon + 1;
            for (int i = allWords.size() - 1; i >= 0 && ((Word)allWords.get(i)).getType() == 10; --i) {
                --sqlCount;
            }
            pr.sqlCounts = sqlCount;
        }
        return pr;
    }

    public static boolean isSelectStatement(String sql) throws OSQLException {
        return OscarSqlProcessor.parsing(sql).isSelectSql();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean whetherToUseCursor(String sql) {
        if (sql == null || sql.trim().length() == 0) {
            return false;
        }
        int select = 0;
        int create = 0;
        int into = 0;
        int explain = 0;
        int update = 0;
        int prepare = 0;
        boolean as = false;
        int grant = 0;
        int declare = 0;
        int delete = 0;
        int set = 0;
        int revoke = 0;
        int audit = 0;
        int drop = 0;
        int rollback = 0;
        int groupBy = 0;
        int aggregate = 0;
        Yylex lex = null;
        try {
            Word w;
            lex = new Yylex(new CharArrayReader(sql.toCharArray()), sql.length());
            while ((w = lex.yylex()).getType() != 0) {
                if (w.getType() == 102) {
                    ++select;
                    continue;
                }
                if (w.getType() == 101) {
                    ++create;
                    break;
                }
                if (w.getType() == 100) {
                    ++explain;
                    break;
                }
                if (w.getType() == 103) {
                    ++into;
                    break;
                }
                if (w.getType() == 105) {
                    ++update;
                    break;
                }
                if (w.getType() == 106) {
                    ++delete;
                    break;
                }
                if (w.getType() == 112) {
                    ++set;
                    break;
                }
                if (w.getType() == 109) {
                    ++prepare;
                    break;
                }
                if (w.getType() == 110) {
                    ++grant;
                    break;
                }
                if (w.getType() == 113) {
                    ++revoke;
                    break;
                }
                if (w.getType() == 111) {
                    ++declare;
                    break;
                }
                if (w.getType() == 115) {
                    ++drop;
                    break;
                }
                if (w.getType() == 114) {
                    ++audit;
                    break;
                }
                if (w.getType() == 150 || w.getType() == 151 || w.getType() == 152 || w.getType() == 153 || w.getType() == 154 || w.getType() == 155 || w.getType() == 156) {
                    ++aggregate;
                    continue;
                }
                if (w.getType() == 300) {
                    ++groupBy;
                    break;
                }
                if (w.getType() != 116 && w.getType() != 117) continue;
                ++rollback;
                break;
            }
            lex.yyclose();
        }
        catch (Exception e) {
            e.printStackTrace();
            boolean bl = false;
            return bl;
        }
        finally {
            if (lex != null) {
                try {
                    lex.yyclose();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (select > 0 && explain == 0 && create == 0 && update == 0 && prepare == 0 && grant == 0 && declare == 0 && aggregate == 0 && into == 0 && set == 0 && delete == 0 && revoke == 0 && audit == 0 && drop == 0 && rollback == 0) {
            return true;
        }
        return aggregate != 0 && groupBy != 0;
    }

    public static String replaceProcessing(String osql) {
        String o_sql = osql;
        StringBuffer newsql = new StringBuffer(o_sql.length());
        int state = 0;
        int i = -1;
        int len = o_sql.length();
        while (++i < len) {
            char c = o_sql.charAt(i);
            switch (state) {
                case 0: {
                    if (c == '\'') {
                        state = 1;
                    } else if (c == '{' && i + 1 < len) {
                        char next = o_sql.charAt(i + 1);
                        char nextf = o_sql.charAt(i + 2);
                        if (next == 'd' || next == 'D') {
                            state = 3;
                            ++i;
                            break;
                        }
                        if (next == 't' || next == 'T') {
                            state = 3;
                            i += i + 2 < len && (o_sql.charAt(i + 2) == 's' || o_sql.charAt(i + 2) == 'S') ? 2 : 1;
                            break;
                        }
                        if (next == 'f' && nextf == 'n' || next == 'F' && nextf == 'N') {
                            state = 4;
                            int indexOfNextBraces = o_sql.indexOf("}", (i += 2) + 1);
                            o_sql = o_sql.substring(0, i + 1).concat(OscarSqlProcessor.funcNameReplace(o_sql.substring(i + 1, indexOfNextBraces))).concat(o_sql.substring(indexOfNextBraces));
                            len = o_sql.length();
                            break;
                        }
                        if (next == 'o' && nextf == 'j' || next == 'O' && nextf == 'J') {
                            state = 5;
                            i += 2;
                            break;
                        }
                        if (next == 'E' && nextf == 'S' || next == 'e' && nextf == 's') {
                            state = 6;
                            break;
                        }
                    }
                    newsql.append(c);
                    break;
                }
                case 1: {
                    if (c == '\'') {
                        state = 0;
                    } else if (c == '\\') {
                        state = 2;
                    }
                    newsql.append(c);
                    break;
                }
                case 2: {
                    state = 1;
                    newsql.append(c);
                    break;
                }
                case 3: {
                    if (c == '}') {
                        state = 0;
                        break;
                    }
                    newsql.append(c);
                    break;
                }
                case 4: {
                    if (c == '}') {
                        state = 0;
                        break;
                    }
                    newsql.append(c);
                    break;
                }
                case 5: {
                    if (c == '}') {
                        state = 0;
                        break;
                    }
                    newsql.append(c);
                    break;
                }
                case 6: {
                    if (c == '}') {
                        state = 0;
                        break;
                    }
                    if (c == '\\') {
                        newsql.append(c);
                        newsql.append('\\');
                        break;
                    }
                    newsql.append(c);
                }
            }
        }
        return newsql.toString();
    }

    private static String funcNameReplace(String input) {
        String upperinput = input.toUpperCase();
        if (upperinput.indexOf("CONVERT") > -1) {
            int index = input.lastIndexOf(",");
            if (input.lastIndexOf("DOUBLE") > index) {
                return input.substring(0, index + 1).concat("DOUBLE PRECISION)");
            }
            if (input.lastIndexOf("VARCHAR") > index) {
                return input.substring(0, index + 1).concat("VARCHAR(8000))");
            }
            if (input.lastIndexOf("CHAR") > index) {
                return input.substring(0, index + 1).concat("CHAR(8000))");
            }
            if (upperinput.lastIndexOf("VARBINARY") > index) {
                return input.substring(0, index + 1).concat("VARBINARY(8000))");
            }
            if (upperinput.lastIndexOf("BINARY") > index) {
                return input.substring(0, index + 1).concat("BINARY(8000))");
            }
            if (upperinput.lastIndexOf("LONGVARBINARY") > index) {
                return input.substring(0, index + 1).concat("BLOB)");
            }
            if (upperinput.lastIndexOf("LONGVARCHAR") > index) {
                return input.substring(0, index + 1).concat("CLOB)");
            }
            return input;
        }
        if (upperinput.indexOf("CHAR") > -1) {
            return upperinput.replaceAll("CHAR", "CHR");
        }
        if (upperinput.indexOf("TIMESTAMPADD") > -1 || upperinput.indexOf("TIMESTAMPDIFF") > -1) {
            String interval = upperinput.substring(input.indexOf("(") + 1, input.indexOf(",")).trim();
            if (interval.compareTo("SQL_TSI_FRAC_SECOND") == 0) {
                return upperinput.replaceAll("SQL_TSI_FRAC_SECOND", "0");
            }
            if (interval.compareTo("SQL_TSI_SECOND") == 0) {
                return upperinput.replaceAll("SQL_TSI_SECOND", "1");
            }
            if (interval.compareTo("SQL_TSI_MINUTE") == 0) {
                return upperinput.replaceAll("SQL_TSI_MINUTE", "2");
            }
            if (interval.compareTo("SQL_TSI_HOUR") == 0) {
                return upperinput.replaceAll("SQL_TSI_HOUR", "3");
            }
            if (interval.compareTo("SQL_TSI_DAY") == 0) {
                return upperinput.replaceAll("SQL_TSI_DAY", "4");
            }
            if (interval.compareTo("SQL_TSI_WEEK") == 0) {
                return upperinput.replaceAll("SQL_TSI_WEEK", "5");
            }
            if (interval.compareTo("SQL_TSI_MONTH") == 0) {
                return upperinput.replaceAll("SQL_TSI_MONTH", "6");
            }
            if (interval.compareTo("SQL_TSI_QUARTER") == 0) {
                return upperinput.replaceAll("SQL_TSI_QUARTER", "7");
            }
            if (interval.compareTo("SQL_TSI_YEAR") == 0) {
                return upperinput.replaceAll("SQL_TSI_YEAR", "8");
            }
            return input;
        }
        if (upperinput.indexOf("HOUR") > -1) {
            return upperinput.replaceAll("HOUR", "\"HOUR\"");
        }
        if (upperinput.indexOf("MINUTE") > -1) {
            return upperinput.replaceAll("MINUTE", "\"MINUTE\"");
        }
        if (upperinput.indexOf("MONTH") > -1 && upperinput.indexOf("DAYOFMONTH") == -1 && upperinput.indexOf("MONTHNAME") == -1) {
            return upperinput.replaceAll("MONTH", "\"MONTH\"");
        }
        if (upperinput.indexOf("SECOND") > -1) {
            return upperinput.replaceAll("SECOND", "\"SECOND\"");
        }
        if (upperinput.indexOf("YEAR") > -1 && upperinput.indexOf("DAYOFYEAR") == -1) {
            return upperinput.replaceAll("YEAR", "\"YEAR\"");
        }
        if (upperinput.indexOf("USER") > -1) {
            return upperinput.replaceAll("USER", "\"CURRENT_USER\"");
        }
        if (upperinput.indexOf("INSERT") > -1) {
            return "INSERT_TEXT".concat(input.substring(input.indexOf(40)));
        }
        if (upperinput.indexOf("NOW") > -1) {
            return "NOW()::TIMESTAMP";
        }
        return input;
    }

    public static ParseFunctionResult modifyJdbcCall(String osql, BaseConnection connection) throws SQLException {
        int index;
        if (osql == null || osql.trim().length() == 0) {
            throw new OSQLException("OSCAR-00433", "88888", 433);
        }
        ParseFunctionResult pr = new ParseFunctionResult();
        osql = osql.trim();
        boolean isCallable = OscarSqlProcessor.isRealCallable(osql);
        pr.isCallable = isCallable;
        if (!isCallable) {
            pr.afterSql = osql;
            return pr;
        }
        String l_sql = osql;
        if (l_sql.indexOf("=") > -1) {
            pr.isResultNeeded = true;
            if (l_sql.indexOf("?") >= l_sql.indexOf("=")) {
                throw new OSQLException("OSCAR-00414", "88888", 414);
            }
        }
        if (l_sql.startsWith("{") && l_sql.endsWith("}")) {
            l_sql = l_sql.substring(1, l_sql.length() - 1);
        }
        if ((index = l_sql.toLowerCase().indexOf("call")) == -1) {
            throw new OSQLException("OSCAR-00414", "88888", 414);
        }
        l_sql = l_sql.replace('{', ' ');
        l_sql = l_sql.replace('}', ' ');
        l_sql = l_sql.replace(';', ' ');
        l_sql = l_sql.substring(index + 4);
        String sn = l_sql.trim();
        String schemaName = null;
        if (sn.contains(".")) {
            String[] procSplit = sn.split("\\.");
            sn = procSplit[procSplit.length - 1];
            if (procSplit.length == 2) {
                schemaName = procSplit[0].trim();
            }
        }
        if (sn.indexOf("(") > -1) {
            sn = sn.substring(0, sn.indexOf("(")).trim();
        }
        sn = sn.startsWith("\"") && sn.endsWith("\"") ? sn.substring(1, sn.length() - 1) : sn.toUpperCase();
        pr.objectName = sn;
        pr.schemaName = schemaName;
        if (pr.isResultNeeded) {
            l_sql = "EXEC OUT " + l_sql;
            pr.haveFuncReturn = true;
            pr.isFunc = true;
        } else {
            try {
                OscarSqlProcessor.initObjectInfo(pr, connection);
                l_sql = pr.isFunc ? "EXEC OUT" + l_sql : "EXEC IN" + l_sql;
            }
            catch (SQLException e) {
                throw new OSQLException("OSCAR-00414", "88888", 414, e);
            }
        }
        if (l_sql.indexOf(40) < 0) {
            l_sql = l_sql + "()";
        }
        pr.afterSql = l_sql;
        return pr;
    }

    public static List<Word> parseToAllWords(String str) throws SQLException {
        if (str == null) {
            return null;
        }
        try {
            ArrayList<Word> allWords = new ArrayList<Word>();
            Yylex lex = new Yylex(new CharArrayReader(str.toCharArray()), str.length());
            Word word = lex.yylex();
            while (word.getType() != 0) {
                allWords.add(word);
                word = lex.yylex();
            }
            return allWords;
        }
        catch (IOException e) {
            throw new SQLException(e.getMessage());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void initObjectInfo(ParseFunctionResult pr, BaseConnection connection) throws SQLException {
        ResultSet rs = null;
        try {
            String sn = pr.getObjectName();
            String schemaName = pr.getSchemaName();
            StringBuffer sql = new StringBuffer();
            sql.append("select proc.PROISFUNC ,params.PROCOID, params.PARAMNAME,params.POSITION,params.PARAMTYP,params.PARAMINOUT ");
            sql.append("from v_sys_proc proc left join v_sys_proc_params params on(params.PROCOID=proc.oid and params.POSITION>0) ");
            sql.append("where proc.proname='");
            sql.append(sn + "'");
            if (schemaName != null) {
                sql.append(" and proc.PRONAMESPACE=(select oid from v_sys_namespace where NSPNAME='");
                sql.append(schemaName + "')");
            }
            rs = connection.execSQL(sql.toString());
            while (rs.next()) {
                pr.isFunc = rs.getBoolean(1);
                pr.haveFuncReturn = false;
                pr.addParam(new ParamInfo(rs.getInt(2), rs.getString(3), rs.getInt(4), rs.getInt(5), rs.getInt(6)));
            }
        }
        finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    private static boolean isRealCallable(String originSql) {
        if (originSql.startsWith("{")) {
            return true;
        }
        return originSql.toLowerCase().startsWith("call");
    }

    public static void main(String[] args) throws OSQLException {
        String sql = "/*select * from dual where x=?*/SELECT CREATED FROM E9.HRMRESOURCE where a=?;";
        ParseResult pr = OscarSqlProcessor.parsing(sql);
        System.out.println(pr.ddlSql);
        System.out.println(pr.selectSql);
        System.out.println(pr.questionMarks.size());
        System.out.println(pr.sqlCounts);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static class ParseFunctionResult {
        private boolean isFunc;
        private boolean haveFuncReturn;
        private boolean isCallable;
        private boolean isResultNeeded;
        private String afterSql;
        private List<Long> oid;
        private Map<String, ParamInfo> params;
        private String objectName;
        private String schemaName;

        public boolean isFunc() {
            return this.isFunc;
        }

        public boolean haveFuncReturn() {
            return this.haveFuncReturn;
        }

        public boolean isCallable() {
            return this.isCallable;
        }

        public boolean isResultNeeded() {
            return this.isResultNeeded;
        }

        public String getAfterSql() {
            return this.afterSql;
        }

        public List<Long> getOid() {
            return this.oid;
        }

        private void addParam(ParamInfo paramInfo) {
            if (this.params == null) {
                this.params = new LinkedHashMap<String, ParamInfo>();
            }
            this.params.put(paramInfo.getSequence() + "_" + paramInfo.getOid(), paramInfo);
        }

        public Map<String, ParamInfo> getParams() {
            return this.params;
        }

        public String getObjectName() {
            return this.objectName;
        }

        public String getSchemaName() {
            return this.schemaName;
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static class ParseResult {
        private boolean selectSql;
        private boolean ddlSql;
        private boolean dmlSql;
        private int sqlCounts;
        private List<Word> questionMarks;
        private boolean normalSelect;
        private boolean insertSql;
        private String transSql;

        public boolean isInsertSql() {
            return this.insertSql;
        }

        public boolean isNormalSelect() {
            return this.normalSelect;
        }

        public boolean isSelectSql() {
            return this.selectSql;
        }

        public boolean isDdlSql() {
            return this.ddlSql;
        }

        public int getSqlCounts() {
            return this.sqlCounts;
        }

        public List<Word> getQuestionMarks() {
            return this.questionMarks;
        }

        public boolean isDmlSql() {
            return this.dmlSql;
        }

        public String getTransSql() {
            return this.transSql;
        }
    }
}

