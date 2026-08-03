/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.dispatcher.parser;

public class ParseException
extends Exception {
    private static final long serialVersionUID = 1L;

    public ParseException(String message, Exception e) {
        super(message, e);
    }

    public ParseException(Exception e) {
        super(e);
    }

    public ParseException(String message) {
        super(message);
    }
}

