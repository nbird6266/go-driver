/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.util.Yylex;
import java.io.CharArrayReader;
import java.io.IOException;

public class Word {
    public static final int ERROR = -1;
    public static final int EOF = 0;
    public static final int SEMI = 10;
    public static final int RPAREN = 11;
    public static final int LPAREN = 12;
    public static final int DOT = 13;
    public static final int QUESTION_MARK = 14;
    public static final int COMMA = 15;
    public static final int COLONPARAM = 16;
    public static final int TYPETURN = 17;
    public static final int OPERATOR = 60;
    public static final int EXPLAIN = 100;
    public static final int CREATE = 101;
    public static final int SELECT = 102;
    public static final int INTO = 103;
    public static final int INSERT = 104;
    public static final int UPDATE = 105;
    public static final int DELETE = 106;
    public static final int FROM = 107;
    public static final int AS = 108;
    public static final int PREPARE = 109;
    public static final int GRANT = 110;
    public static final int DECLARE = 111;
    public static final int SET = 112;
    public static final int REVOKE = 113;
    public static final int AUDIT = 114;
    public static final int DROP = 115;
    public static final int BEGIN = 116;
    public static final int ROLLBACK = 117;
    public static final int COMMENT = 118;
    public static final int ALTER = 119;
    public static final int SHOW = 120;
    public static final int WITH = 121;
    public static final int MERGE = 122;
    public static final int EXECUTE = 123;
    public static final int AVG = 150;
    public static final int COUNT = 151;
    public static final int MAX = 152;
    public static final int MIN = 153;
    public static final int STDDEV = 154;
    public static final int SUM = 155;
    public static final int VARIANCE = 156;
    public static final int STRING_IDENTIFIER = 200;
    public static final int STRING_SINGLE = 201;
    public static final int STRING_DOUBLE = 202;
    public static final int GROUP_BY = 300;
    public static final int OTHER = 400;
    private int m_type;
    private String m_text;
    private int m_charBegin;
    private int m_charEnd;

    public static String getTypeString(int type) {
        switch (type) {
            case -1: {
                return "ERROR";
            }
            case 0: {
                return "EOF";
            }
            case 10: {
                return "SEMI";
            }
            case 11: {
                return "RPAREN";
            }
            case 12: {
                return "LPAREN";
            }
            case 100: {
                return "EXPLAIN";
            }
            case 101: {
                return "CREATE";
            }
            case 102: {
                return "SELECT";
            }
            case 103: {
                return "INTO";
            }
            case 104: {
                return "INSERT";
            }
            case 105: {
                return "UPDATE";
            }
            case 106: {
                return "DELETE";
            }
            case 112: {
                return "SET";
            }
            case 107: {
                return "FROM";
            }
            case 108: {
                return "AS";
            }
            case 109: {
                return "PREPARE";
            }
            case 110: {
                return "GRANT";
            }
            case 113: {
                return "REVOKE";
            }
            case 111: {
                return "DECLARE";
            }
            case 120: {
                return "SHOW";
            }
            case 150: 
            case 151: 
            case 152: 
            case 153: 
            case 154: 
            case 155: 
            case 156: {
                return "AGGREGATE";
            }
            case 200: {
                return "STRING_IDENTIFIER";
            }
            case 201: {
                return "STRING_SINGLE";
            }
            case 202: {
                return "STRING_DOUBLE";
            }
            case 115: {
                return "DROP";
            }
            case 116: {
                return "BEGIN";
            }
            case 117: {
                return "ROLLBACK";
            }
            case 13: {
                return ".";
            }
            case 60: {
                return "OPERATOR";
            }
            case 400: {
                return "OTHER";
            }
            case 14: {
                return "?";
            }
        }
        return "UNKNOWN";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void printWords(String sql) {
        if (sql == null) {
            return;
        }
        Yylex lex = null;
        lex = new Yylex(new CharArrayReader(sql.toCharArray()), sql.length());
        Word w = null;
        do {
            w = lex.yylex();
            System.err.println(w);
        } while (w.m_type != 0);
        Object var4_4 = null;
        if (lex == null) return;
        try {
            lex.yyclose();
            return;
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
        return;
        {
            catch (Exception e) {
                e.printStackTrace();
                Object var4_5 = null;
                if (lex == null) return;
                try {
                    lex.yyclose();
                    return;
                }
                catch (IOException e2) {
                    e2.printStackTrace();
                }
                return;
            }
        }
        catch (Throwable throwable) {
            Object var4_6 = null;
            if (lex == null) throw throwable;
            try {
                lex.yyclose();
                throw throwable;
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
            throw throwable;
        }
    }

    public static void main(String[] args) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static boolean canExplain(String sql) {
        if (sql == null || sql.trim().length() == 0) {
            return false;
        }
        int select = 0;
        int create = 0;
        int explain = 0;
        int update = 0;
        int delete = 0;
        int insert = 0;
        int drop = 0;
        Yylex lex = null;
        lex = new Yylex(new CharArrayReader(sql.toCharArray()), sql.length());
        Word w = lex.yylex();
        while (w.m_type != 0) {
            switch (w.m_type) {
                case 101: {
                    ++create;
                    break;
                }
                case 102: {
                    ++select;
                    break;
                }
                case 104: {
                    ++insert;
                    break;
                }
                case 105: {
                    ++update;
                    break;
                }
                case 106: {
                    ++delete;
                    break;
                }
                case 100: {
                    ++explain;
                    break;
                }
                case 115: {
                    ++drop;
                    break;
                }
            }
            w = lex.yylex();
        }
        Object var12_11 = null;
        if (lex == null) return create == 0 && explain == 0 && (select > 0 || delete > 0 || update > 0 || insert > 0 || drop > 0);
        try {
            lex.yyclose();
            return create == 0 && explain == 0 && (select > 0 || delete > 0 || update > 0 || insert > 0 || drop > 0);
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
        return create == 0 && explain == 0 && (select > 0 || delete > 0 || update > 0 || insert > 0 || drop > 0);
        {
            catch (Exception e) {
                e.printStackTrace();
                boolean bl = false;
                Object var12_12 = null;
                if (lex == null) return bl;
                try {
                    lex.yyclose();
                    return bl;
                }
                catch (IOException e2) {
                    e2.printStackTrace();
                }
                return bl;
            }
        }
        catch (Throwable throwable) {
            Object var12_13 = null;
            if (lex == null) throw throwable;
            try {
                lex.yyclose();
                throw throwable;
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
            throw throwable;
        }
    }

    Word(int type, String text) {
        this.m_type = type;
        this.m_text = text;
        this.m_charBegin = -1;
        this.m_charEnd = -1;
    }

    Word(int type, String text, int charBegin, int charEnd) {
        this.m_type = type;
        this.m_text = text;
        this.m_charBegin = charBegin;
        this.m_charEnd = charEnd;
    }

    public String toString() {
        return Word.getTypeString(this.m_type) + " : " + this.m_text;
    }

    public int getType() {
        return this.m_type;
    }

    public String getTokentext() {
        return this.m_text;
    }

    public int getTokenBegin() {
        return this.m_charBegin;
    }

    public int getTokenEnd() {
        return this.m_charEnd;
    }
}

