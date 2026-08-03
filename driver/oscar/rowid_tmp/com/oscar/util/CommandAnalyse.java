/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.util.EscapeTools;

public class CommandAnalyse {
    public static String analyseTableName(String command) {
        String tableName = "";
        int start = command.toUpperCase().indexOf("COPY");
        int end = command.toUpperCase().indexOf("FROM");
        int pos = 0;
        if (start >= 0 && end >= 0) {
            tableName = command.substring(start += 4, end).trim();
            pos = tableName.indexOf(34);
            if (pos == 0) {
                tableName = tableName.substring(1, tableName.length() - 1);
            }
            if ((pos = tableName.indexOf(40)) > 0 && (pos = (tableName = tableName.substring(0, pos).trim()).lastIndexOf(34)) == tableName.length() - 1) {
                tableName = tableName.substring(0, pos);
            }
        }
        return tableName;
    }

    public static String[] analyseSchemaTableName(String command) {
        String[] retVal = new String[2];
        String tableName = null;
        int start = command.toUpperCase().indexOf("COPY");
        int end = command.toUpperCase().indexOf("FROM");
        int pos = 0;
        if (start >= 0 && end >= 0) {
            tableName = command.substring(start += 4, end).trim();
            pos = tableName.indexOf(34);
            if (pos == 0) {
                tableName = tableName.substring(1, tableName.length());
            }
            if ((pos = tableName.indexOf(40)) > 0) {
                tableName = tableName.substring(0, pos).trim();
            }
            if ((pos = tableName.indexOf(".")) != -1) {
                retVal[0] = tableName.charAt(pos - 1) == '\"' ? tableName.substring(0, pos - 1) : tableName.substring(0, pos);
                tableName = tableName.substring(pos + 1);
            }
            if ((pos = tableName.indexOf("\"")) == 0) {
                tableName = tableName.substring(1, tableName.length());
            }
            if ((pos = tableName.lastIndexOf("\"")) == tableName.length() - 1) {
                tableName = tableName.substring(0, pos);
            }
            retVal[1] = tableName;
        }
        return retVal;
    }

    public static byte[] analyseColSep(String command) {
        byte[] colSepByte = null;
        String temp = "";
        int start = command.toUpperCase().indexOf("WITH");
        int end = 0;
        if (start > 0) {
            temp = command.substring(start + 4, command.length());
        }
        if ((start = temp.toUpperCase().indexOf("WITH")) < 0 && (start = temp.toUpperCase().indexOf("DELIMITER")) >= 0 && (start = (temp = temp.substring(start, temp.length())).indexOf(39)) >= 0) {
            for (int i = start + 1; i < temp.length(); ++i) {
                if (temp.charAt(i) != '\'') continue;
                end = i;
                break;
            }
            if (end != 0) {
                colSepByte = EscapeTools.stripEscapes(temp.substring(start + 1, end)).getBytes();
            }
        }
        return colSepByte;
    }

    public static String analyseQuery(String command) {
        String querySql = "";
        int start = 0;
        int end = 0;
        start = command.indexOf(40);
        if (start >= 0) {
            end = command.lastIndexOf(41);
            querySql = command.substring(start + 1, end);
        }
        return querySql;
    }

    public static String analyseColumnOrder(String command) {
        String columnOrder = "";
        int start = command.toUpperCase().indexOf("COPY");
        int end = command.toUpperCase().indexOf("FROM");
        if (start >= 0 && end >= 0) {
            columnOrder = command.substring(start += 4, end).trim();
            start = columnOrder.indexOf(40);
            end = columnOrder.indexOf(41);
            columnOrder = end > start ? columnOrder.substring(start + 1, end).trim() : "";
        }
        return columnOrder;
    }

    public static String analyseNullValue(String command) {
        String nullValue = "";
        int start = command.toUpperCase().indexOf("WITH");
        int end = 0;
        if (start > 0) {
            nullValue = command.substring(start + 4, command.length());
        }
        if ((start = nullValue.toUpperCase().indexOf("NULL")) >= 0) {
            if ((start = (nullValue = nullValue.substring(start + 4)).indexOf("'")) >= 0) {
                end = (nullValue = nullValue.substring(start + 1)).indexOf("'");
                if (end > 0 && end < command.length()) {
                    if ((nullValue = nullValue.substring(0, end)).charAt(0) == '\\' && nullValue.charAt(1) == '\\') {
                        nullValue = nullValue.substring(1, nullValue.length());
                    }
                } else {
                    nullValue = "";
                }
            } else {
                nullValue = "";
            }
        }
        return nullValue;
    }

    public static String analyseEscapeChar(String command) {
        String escapeChar = "";
        int start = command.toUpperCase().indexOf("CSV");
        int end = 0;
        if (start > 0) {
            escapeChar = command.substring(start + 3, command.length());
        }
        if ((start = escapeChar.toUpperCase().indexOf("QUOTE")) >= 0) {
            escapeChar = escapeChar.substring(start + 5);
            start = escapeChar.indexOf(39);
            for (int i = start + 1; i < escapeChar.length(); ++i) {
                if (escapeChar.charAt(i) != '\'') continue;
                end = i;
                break;
            }
            escapeChar = end != 0 ? EscapeTools.stripEscapes(escapeChar.substring(start + 1, end)) : "";
        } else {
            escapeChar = "";
        }
        return escapeChar;
    }

    public static boolean analyseCheckConstraints(String command) {
        String tempStr = "";
        int start = command.toUpperCase().indexOf("NULL AS");
        if (start >= 0) {
            tempStr = command.substring(start + 7);
        }
        return tempStr.toUpperCase().indexOf("CHECK_CONSTRAINTS") > 0;
    }

    public static void main(String[] args) {
        String s = "Hello Nice";
        System.out.println(s.substring(1, s.length() - 1));
    }
}

