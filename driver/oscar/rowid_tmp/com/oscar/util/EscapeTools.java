/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

public class EscapeTools {
    public static String stripEscapes(String source) {
        StringBuffer result = new StringBuffer();
        boolean backslash = false;
        char[] c = source.toCharArray();
        for (int i = 0; i < c.length; ++i) {
            if (backslash) {
                switch (c[i]) {
                    case '\\': {
                        result.append("\\");
                        break;
                    }
                    case 'n': {
                        result.append("\n");
                        break;
                    }
                    case 'r': {
                        result.append("\r");
                        break;
                    }
                    case 't': {
                        result.append("\t");
                        break;
                    }
                    case 'b': {
                        result.append("\b");
                        break;
                    }
                    case 'f': {
                        result.append("\f");
                        break;
                    }
                    case '\"': {
                        result.append("\"");
                        break;
                    }
                    case '\'': {
                        result.append("'");
                        break;
                    }
                    default: {
                        result.append(c[i]);
                    }
                }
                backslash = false;
                continue;
            }
            if (c[i] == '\\') {
                backslash = true;
                continue;
            }
            result.append(c[i]);
        }
        return result.toString();
    }

    public static String quotationWrapper(String str) {
        String quoteString = "\"";
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str;
        }
        if (str.contains("\"")) {
            str = str.replace("\"", "\"\"");
        }
        return "\"" + str + "\"";
    }

    public static String toSingleQuotationMarks(String queryString) {
        if (queryString.startsWith("'") && queryString.endsWith("'")) {
            return queryString;
        }
        if (queryString.startsWith("\"") && queryString.endsWith("\"") && queryString.length() > 1) {
            queryString = queryString.substring(1, queryString.length() - 1);
        }
        queryString.replaceAll("\"\"", "\"");
        queryString.replaceAll("'", "''");
        return "'" + queryString + "'";
    }
}

