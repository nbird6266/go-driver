/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class MessageTranslator {
    private static MessageTranslator translator = null;
    private ResourceBundle bundle;
    private static Locale lc;
    private static final String PROP_FILE = "com.oscar.errors";

    private MessageTranslator() {
        lc = Locale.getDefault();
        if (!lc.equals(Locale.SIMPLIFIED_CHINESE)) {
            lc = Locale.ENGLISH;
        }
        this.bundle = PropertyResourceBundle.getBundle(PROP_FILE, lc);
    }

    public static final String translate(String id) {
        if (translator == null) {
            translator = new MessageTranslator();
        }
        return translator._translate(id);
    }

    private final String _translate(String id) {
        return this.bundle.getString(id);
    }

    public static String bind(String message, Object binding) {
        return MessageTranslator.internalBind(message, null, String.valueOf(binding), null);
    }

    public static String bind(String message, Object binding1, Object binding2) {
        return MessageTranslator.internalBind(message, null, String.valueOf(binding1), String.valueOf(binding2));
    }

    public static String bind(String message, Object[] bindings) {
        return MessageTranslator.internalBind(message, bindings, null, null);
    }

    private static String internalBind(String message, Object[] args, String argZero, String argOne) {
        if (message == null) {
            return "No message available.";
        }
        if (args == null || args.length == 0) {
            args = new Object[]{};
        }
        int length = message.length();
        int bufLen = length + args.length * 5;
        if (argZero != null) {
            bufLen += argZero.length() - 3;
        }
        if (argOne != null) {
            bufLen += argOne.length() - 3;
        }
        StringBuffer buffer = new StringBuffer(bufLen < 0 ? 0 : bufLen);
        block6: for (int i = 0; i < length; ++i) {
            char c = message.charAt(i);
            switch (c) {
                case '{': {
                    int index = message.indexOf(125, i);
                    if (index == -1) {
                        buffer.append(c);
                        continue block6;
                    }
                    if (++i >= length) {
                        buffer.append(c);
                        continue block6;
                    }
                    int number = -1;
                    try {
                        number = Integer.parseInt(message.substring(i, index));
                    }
                    catch (NumberFormatException e) {
                        throw (IllegalArgumentException)new IllegalArgumentException().initCause(e);
                    }
                    if (number == 0 && argZero != null) {
                        buffer.append(argZero);
                    } else if (number == 1 && argOne != null) {
                        buffer.append(argOne);
                    } else {
                        if (number >= args.length || number < 0) {
                            buffer.append("<missing argument>");
                            i = index;
                            continue block6;
                        }
                        buffer.append(args[number]);
                    }
                    i = index;
                    continue block6;
                }
                case '\'': {
                    int nextIndex = i + 1;
                    if (nextIndex >= length) {
                        buffer.append(c);
                        continue block6;
                    }
                    char next = message.charAt(nextIndex);
                    if (next == '\'') {
                        ++i;
                        buffer.append(c);
                        continue block6;
                    }
                    int index = message.indexOf(39, nextIndex);
                    if (index == -1) {
                        buffer.append(c);
                        continue block6;
                    }
                    buffer.append(message.substring(nextIndex, index));
                    i = index;
                    continue block6;
                }
                default: {
                    buffer.append(c);
                }
            }
        }
        return buffer.toString();
    }
}

