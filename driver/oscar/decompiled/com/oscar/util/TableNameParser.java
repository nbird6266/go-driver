/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.util.OscarSqlProcessor;
import com.oscar.util.Word;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableNameParser {
    public static final String notAscii = "[\\u007f-\\uffff]";
    public static final String baseRegex = "((\\\"([[[\\u007f-\\uffff]\\p{Print}]&&[^\\\"]]+|\\\"\\\")+\\\")|[[\\p{Alnum}][\\u007f-\\uffff][_]]{1,})";
    public static final String tableNameRegex = "((\\\"([[[\\u007f-\\uffff]\\p{Print}]&&[^\\\"]]+|\\\"\\\")+\\\")|[[\\p{Alnum}][\\u007f-\\uffff][_]]{1,})[\\.]{1}((\\\"([[[\\u007f-\\uffff]\\p{Print}]&&[^\\\"]]+|\\\"\\\")+\\\")|[[\\p{Alnum}][\\u007f-\\uffff][_]]{1,})";
    public static Pattern basePattern = null;
    public static Pattern tableNamePattern = null;

    public static String[] parserToDbString(String tableFullName) throws SQLException {
        String[] ret = new String[2];
        if (TableNameParser.isValidateTableName(tableFullName)) {
            Matcher m = basePattern.matcher(tableFullName);
            int i = 0;
            while (m.find()) {
                ret[i] = tableFullName.substring(m.start(), m.end());
                if (!ret[i].startsWith("\"")) {
                    ret[i] = "\"" + ret[i].toUpperCase() + "\"";
                }
                ++i;
            }
        } else {
            throw new SQLException("\u975e\u6cd5\u7684\u8868\u540d\u683c\u5f0f\uff01");
        }
        return ret;
    }

    public static String[] parserToDbNames(String tableName) throws SQLException {
        if (tableName == null) {
            return null;
        }
        List<Word> allWords = OscarSqlProcessor.parseToAllWords(tableName);
        switch (allWords.size()) {
            case 1: {
                return new String[]{null, allWords.get(0).getTokentext()};
            }
            case 3: {
                if (allWords.get(1).getType() != 13) {
                    throw new SQLException("\u975e\u6cd5\u7684\u5bf9\u8c61\u540d\u683c\u5f0f\uff1a" + tableName);
                }
                return new String[]{allWords.get(0).getTokentext(), allWords.get(2).getTokentext()};
            }
        }
        throw new SQLException("\u975e\u6cd5\u7684\u5bf9\u8c61\u540d\u683c\u5f0f\uff1a" + tableName);
    }

    public static String[] parserToOrgString(String tableName) throws SQLException {
        String[] ret = new String[2];
        String[] temp = TableNameParser.parserToDbString(tableName);
        ret[0] = TableNameParser.dbStringToOrgString(temp[0]);
        ret[1] = TableNameParser.dbStringToOrgString(temp[1]);
        return ret;
    }

    public static String[] parserToQueryString(String tableName) throws SQLException {
        String[] ret = new String[2];
        String[] temp = TableNameParser.parserToOrgString(tableName);
        ret[0] = TableNameParser.orgStringToQueryString(temp[0]);
        ret[1] = TableNameParser.orgStringToQueryString(temp[1]);
        return ret;
    }

    public static String orgStringToQueryString(String dbString) {
        if (dbString == null) {
            return dbString;
        }
        String ret = null;
        ret = dbString.replaceAll("'", "''");
        return '\'' + ret + '\'';
    }

    public static String dbStringToOrgString(String dbString) {
        if (dbString == null) {
            return null;
        }
        String ret = null;
        if (dbString.startsWith("\"") && dbString.endsWith("\"") && dbString.length() > 1) {
            ret = dbString.substring(1, dbString.length() - 1);
            ret = ret.replaceAll("[\\\"]{2}", "\"");
        } else {
            ret = dbString.toUpperCase();
        }
        return ret;
    }

    public static boolean isValidateTableName(String tableName) {
        Matcher matcher = tableNamePattern.matcher(tableName);
        return matcher.matches();
    }

    public static boolean parse(String paramString, String[] schemaPattern, String[] namePattern) {
        int firstQualifier = paramString.indexOf(".");
        int secondQualifier = paramString.lastIndexOf(46);
        if (firstQualifier < 0) {
            namePattern[0] = paramString;
            return false;
        }
        namePattern[0] = paramString.substring(secondQualifier + 1);
        schemaPattern[0] = firstQualifier != secondQualifier ? paramString.substring(firstQualifier + 1, secondQualifier) : paramString.substring(0, firstQualifier);
        return true;
    }

    static {
        basePattern = Pattern.compile(baseRegex);
        tableNamePattern = Pattern.compile(tableNameRegex);
    }
}

