/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.util.MessageTranslator;
import java.sql.SQLException;

public class OSQLException
extends SQLException {
    private String message;
    private int extraState = -1;
    public static final int EXCEPTION_BUT_COMMITED = 1;

    public OSQLException(String reason, String SQLState) {
        super(reason, SQLState, OSQLException.generateVendorCode(SQLState));
        this.message = reason;
    }

    public OSQLException(int errorCode, String SQLState, String errorMessage) {
        super(errorMessage, SQLState, errorCode);
        this.message = errorMessage;
    }

    public OSQLException(String error, String SQLState, int vendorCode) {
        super(error, SQLState, vendorCode);
        this.translate(error);
    }

    public OSQLException(String error, String SQLState, int vendorCode, Throwable cause) {
        super(error, SQLState, vendorCode);
        this.translate(error);
        this.initCause(cause);
    }

    public OSQLException(String error, String SQLState, int vendorCode, String mes) {
        super(error, SQLState, vendorCode);
        this.translate(error);
        this.message = this.message + ":" + mes;
    }

    public OSQLException(String error, String SQLState, int vendorCode, String mes, Throwable cause) {
        super(error, SQLState, vendorCode);
        this.translate(error);
        this.message = this.message + ":" + mes;
        this.initCause(cause);
    }

    public OSQLException(String error, String SQLState, int vendorCode, Object binding) {
        super(error, SQLState, vendorCode);
        this.translate(error);
        this.message = MessageTranslator.bind(this.message, binding);
    }

    private void translate(String error) {
        this.message = MessageTranslator.translate(error);
    }

    public String getLocalizedMessage() {
        return this.message;
    }

    public String getMessage() {
        return this.message;
    }

    public String toString() {
        return super.toString();
    }

    private static int generateVendorCode(String SQLState) {
        int code = 0;
        if (SQLState != null && SQLState.length() > 0) {
            int len = SQLState.length();
            char ch = '\u0000';
            for (int i = 0; i < len; ++i) {
                ch = SQLState.charAt(i);
                code = i < 2 ? code << 7 | ch & 0x7F : code << 6 | ch & 0x3F;
            }
        }
        return code;
    }

    public int getExtraState() {
        return this.extraState;
    }

    public void setExtraState(int state) {
        this.extraState = state;
    }
}

