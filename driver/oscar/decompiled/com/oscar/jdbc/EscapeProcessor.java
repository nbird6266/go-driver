/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.jdbc.EscapeProcessorResult;
import com.oscar.jdbc.EscapeTokenizer;
import com.oscar.jdbc.StringUtils;
import com.oscar.util.OSQLException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

class EscapeProcessor {
    private static Map JDBC_CONVERT_TO_MYSQL_TYPE_MAP;
    private static Map JDBC_NO_CONVERT_TO_MYSQL_EXPRESSION_MAP;

    EscapeProcessor() {
    }

    public static final Object escapeSQL(String sql, boolean serverSupportsConvertFn) throws SQLException {
        int nextEndBrace;
        boolean replaceEscapeSequence = false;
        String escapeSequence = null;
        if (sql == null) {
            return null;
        }
        int beginBrace = sql.indexOf(123);
        int n = nextEndBrace = beginBrace == -1 ? -1 : sql.indexOf(125, beginBrace);
        if (nextEndBrace == -1) {
            return sql;
        }
        StringBuffer newSql = new StringBuffer();
        EscapeTokenizer escapeTokenizer = new EscapeTokenizer(sql);
        byte usesVariables = 0;
        boolean callingStoredFunction = false;
        block4: while (escapeTokenizer.hasMoreTokens()) {
            String token = escapeTokenizer.nextToken();
            if (token.length() == 0) continue;
            if (token.charAt(0) == '{') {
                String collapsedToken;
                int nestedBrace;
                if (!token.endsWith("}")) {
                    throw new OSQLException("Not a valid escape sequence: " + token, "42000", 202);
                }
                if (token.length() > 2 && (nestedBrace = token.indexOf(123, 2)) != -1) {
                    StringBuffer buf = new StringBuffer(token.substring(0, 1));
                    Object remainingResults = EscapeProcessor.escapeSQL(token.substring(1, token.length() - 1), serverSupportsConvertFn);
                    String remaining = null;
                    if (remainingResults instanceof String) {
                        remaining = (String)remainingResults;
                    } else {
                        remaining = ((EscapeProcessorResult)remainingResults).escapedSql;
                        if (usesVariables != 1) {
                            usesVariables = ((EscapeProcessorResult)remainingResults).usesVariables;
                        }
                    }
                    buf.append(remaining);
                    buf.append('}');
                    token = buf.toString();
                }
                if (StringUtils.startsWithIgnoreCase(collapsedToken = EscapeProcessor.removeWhitespace(token), "{escape")) {
                    try {
                        StringTokenizer st = new StringTokenizer(token, " '");
                        st.nextToken();
                        escapeSequence = st.nextToken();
                        if (escapeSequence.length() < 3) {
                            newSql.append(token);
                            continue;
                        }
                        escapeSequence = escapeSequence.substring(1, escapeSequence.length() - 1);
                        replaceEscapeSequence = true;
                    }
                    catch (NoSuchElementException e) {
                        newSql.append(token);
                    }
                    continue;
                }
                if (StringUtils.startsWithIgnoreCase(collapsedToken, "{fn")) {
                    int endPos;
                    int startPos = token.toLowerCase().indexOf("fn ") + 3;
                    String fnToken = token.substring(startPos, endPos = token.length() - 1);
                    if (StringUtils.startsWithIgnoreCaseAndWs(fnToken, "convert")) {
                        newSql.append(EscapeProcessor.processConvertToken(fnToken, serverSupportsConvertFn));
                        continue;
                    }
                    newSql.append(fnToken);
                    continue;
                }
                if (StringUtils.startsWithIgnoreCase(collapsedToken, "{d")) {
                    int startPos = token.indexOf(39) + 1;
                    int endPos = token.lastIndexOf(39);
                    if (startPos == -1 || endPos == -1) {
                        newSql.append(token);
                        continue;
                    }
                    String argument = token.substring(startPos, endPos);
                    try {
                        StringTokenizer st = new StringTokenizer(argument, " -");
                        String year4 = st.nextToken();
                        String month2 = st.nextToken();
                        String day2 = st.nextToken();
                        String dateString = "'" + year4 + "-" + month2 + "-" + day2 + "'";
                        newSql.append(dateString);
                        continue;
                    }
                    catch (NoSuchElementException e) {
                        throw new OSQLException("Syntax error for DATE escape sequence '" + argument + "'", "42000", 202, e);
                    }
                }
                if (StringUtils.startsWithIgnoreCase(collapsedToken, "{call") || StringUtils.startsWithIgnoreCase(collapsedToken, "{?=call")) {
                    int startPos = StringUtils.indexOfIgnoreCase(token, "CALL") + 5;
                    int endPos = token.length() - 1;
                    if (StringUtils.startsWithIgnoreCase(collapsedToken, "{?=call")) {
                        callingStoredFunction = true;
                        newSql.append("SELECT ");
                        newSql.append(token.substring(startPos, endPos));
                    } else {
                        callingStoredFunction = false;
                        newSql.append("CALL ");
                        newSql.append(token.substring(startPos, endPos));
                    }
                    for (int i = endPos - 1; i >= startPos; --i) {
                        char c = token.charAt(i);
                        if (Character.isWhitespace(c)) continue;
                        if (c == ')') continue block4;
                        newSql.append("()");
                        continue block4;
                    }
                    continue;
                }
                if (!StringUtils.startsWithIgnoreCase(collapsedToken, "{oj")) continue;
                newSql.append(token);
                continue;
            }
            newSql.append(token);
        }
        String escapedSql = newSql.toString();
        if (replaceEscapeSequence) {
            String currentSql = escapedSql;
            while (currentSql.indexOf(escapeSequence) != -1) {
                int escapePos = currentSql.indexOf(escapeSequence);
                String lhs = currentSql.substring(0, escapePos);
                String rhs = currentSql.substring(escapePos + 1, currentSql.length());
                currentSql = lhs + "\\" + rhs;
            }
            escapedSql = currentSql;
        }
        EscapeProcessorResult epr = new EscapeProcessorResult();
        epr.escapedSql = escapedSql;
        epr.callingStoredFunction = callingStoredFunction;
        if (usesVariables != 1) {
            epr.usesVariables = escapeTokenizer.sawVariableUse() ? (byte)1 : 0;
        }
        return epr;
    }

    private static String processConvertToken(String functionToken, boolean serverSupportsConvertFn) throws SQLException {
        int firstIndexOfParen = functionToken.indexOf("(");
        if (firstIndexOfParen == -1) {
            throw new OSQLException("Syntax error while processing {fn convert (... , ...)} token, missing opening parenthesis", "42000", 202);
        }
        int tokenLength = functionToken.length();
        int indexOfComma = functionToken.lastIndexOf(",");
        if (indexOfComma == -1) {
            throw new OSQLException("Syntax error while processing {fn convert (... , ...)} token, missing opening parenthesis", "42000", 202);
        }
        int indexOfCloseParen = functionToken.indexOf(41, indexOfComma);
        if (indexOfCloseParen == -1) {
            throw new OSQLException("Syntax error while processing {fn convert (... , ...)} token, missing closing parenthesis", "42000", 202);
        }
        String expression = functionToken.substring(firstIndexOfParen + 1, indexOfComma);
        String type = functionToken.substring(indexOfComma + 1, indexOfCloseParen);
        String newType = null;
        String trimmedType = type.trim();
        if (StringUtils.startsWithIgnoreCase(trimmedType, "SQL_")) {
            trimmedType = trimmedType.substring(4, trimmedType.length());
        }
        if (serverSupportsConvertFn) {
            newType = (String)JDBC_CONVERT_TO_MYSQL_TYPE_MAP.get(trimmedType.toUpperCase(Locale.ENGLISH));
        } else {
            newType = (String)JDBC_NO_CONVERT_TO_MYSQL_EXPRESSION_MAP.get(trimmedType.toUpperCase(Locale.ENGLISH));
            if (newType == null) {
                throw new OSQLException("Can't find conversion re-write for type '" + type + "' that is applicable for this server version while processing escape tokens.", "42000", 202);
            }
        }
        if (newType == null) {
            throw new OSQLException("Unsupported conversion type '" + type.trim() + "' found while processing escape token.", "42000", 202);
        }
        int replaceIndex = newType.indexOf("?");
        if (replaceIndex != -1) {
            StringBuffer convertRewrite = new StringBuffer(newType.substring(0, replaceIndex));
            convertRewrite.append(expression);
            convertRewrite.append(newType.substring(replaceIndex + 1, newType.length()));
            return convertRewrite.toString();
        }
        StringBuffer castRewrite = new StringBuffer("CAST(");
        castRewrite.append(expression);
        castRewrite.append(" AS ");
        castRewrite.append(newType);
        castRewrite.append(")");
        return castRewrite.toString();
    }

    private static String removeWhitespace(String toCollapse) {
        if (toCollapse == null) {
            return null;
        }
        int length = toCollapse.length();
        StringBuffer collapsed = new StringBuffer(length);
        for (int i = 0; i < length; ++i) {
            char c = toCollapse.charAt(i);
            if (Character.isWhitespace(c)) continue;
            collapsed.append(c);
        }
        return collapsed.toString();
    }
}

