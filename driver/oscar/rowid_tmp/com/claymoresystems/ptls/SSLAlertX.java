/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLDebug;

class SSLAlertX {
    public static int TLS_ALERT_WARNING = 1;
    public static int TLS_ALERT_FATAL = 2;
    public static int TLS_ALERT_CLOSE_NOTIFY = 0;
    public static int TLS_ALERT_UNEXPECTED_MESSAGE = 10;
    public static int TLS_ALERT_BAD_RECORD_MAC = 20;
    public static int TLS_ALERT_DECRYPTION_FAILED = 21;
    public static int TLS_ALERT_RECORD_OVERFLOW = 22;
    public static int TLS_ALERT_DEcomPRESSION_FAILURE = 30;
    public static int TLS_ALERT_HANDSHAKE_FAILURE = 40;
    public static int SSL_ALERT_NO_CERTIFICATE = 41;
    public static int TLS_ALERT_BAD_CERTIFICATE = 42;
    public static int TLS_ALERT_UNSUPPORTED_CERTIFICATE = 43;
    public static int TLS_ALERT_CERTIFICATE_REVOKED = 44;
    public static int TLS_ALERT_CERTIFICATE_EXPIRED = 45;
    public static int TLS_ALERT_CERTIFICATE_UNKNOWN = 46;
    public static int TLS_ALERT_ILLEGAL_PARAMETER = 47;
    public static int TLS_ALERT_UNKNOWN_CA = 48;
    public static int TLS_ALERT_ACCESS_DENIED = 49;
    public static int TLS_ALERT_DECODE_ERROR = 50;
    public static int TLS_ALERT_DECRYPT_ERROR = 51;
    public static int TLS_ALERT_EXPORT_RESTRICTION = 60;
    public static int TLS_ALERT_PROTOCOL_VERSION = 70;
    public static int TLS_ALERT_INSUFFICIENT_SECURITY = 71;
    public static int TLS_ALERT_INTERNAL_ERROR = 80;
    public static int TLS_ALERT_USER_CANCELLED = 90;
    public static int TLS_ALERT_NO_RENEGOTIATION = 100;
    private static final SSLAlertX[] master_list = new SSLAlertX[]{new SSLAlertX(TLS_ALERT_CLOSE_NOTIFY, false, 0, "Close notify"), new SSLAlertX(TLS_ALERT_UNEXPECTED_MESSAGE, true, 0, "Unexpected message"), new SSLAlertX(TLS_ALERT_BAD_RECORD_MAC, true, 0, "Bad record MAC"), new SSLAlertX(TLS_ALERT_DECRYPTION_FAILED, true, TLS_ALERT_BAD_RECORD_MAC, "Decryption failed"), new SSLAlertX(TLS_ALERT_RECORD_OVERFLOW, true, TLS_ALERT_BAD_RECORD_MAC, "Record overflow"), new SSLAlertX(TLS_ALERT_DEcomPRESSION_FAILURE, true, 0, "Decompression failure"), new SSLAlertX(TLS_ALERT_HANDSHAKE_FAILURE, true, 0, "Handshake failure"), new SSLAlertX(SSL_ALERT_NO_CERTIFICATE, false, 0, "No certificate"), new SSLAlertX(TLS_ALERT_BAD_CERTIFICATE, false, 0, "Bad certificate"), new SSLAlertX(TLS_ALERT_UNSUPPORTED_CERTIFICATE, false, 0, "Unsupported certificate type"), new SSLAlertX(TLS_ALERT_CERTIFICATE_REVOKED, false, 0, "Revoked certificate"), new SSLAlertX(TLS_ALERT_CERTIFICATE_EXPIRED, false, 0, "Expired certificate"), new SSLAlertX(TLS_ALERT_CERTIFICATE_UNKNOWN, false, TLS_ALERT_BAD_CERTIFICATE, "Unknown certificate processing problem"), new SSLAlertX(TLS_ALERT_ILLEGAL_PARAMETER, true, 0, "Illegal parameter"), new SSLAlertX(TLS_ALERT_UNKNOWN_CA, true, TLS_ALERT_BAD_CERTIFICATE, "Unknown CA"), new SSLAlertX(TLS_ALERT_ACCESS_DENIED, true, TLS_ALERT_HANDSHAKE_FAILURE, "Access denied"), new SSLAlertX(TLS_ALERT_DECODE_ERROR, true, TLS_ALERT_HANDSHAKE_FAILURE, "Decode error"), new SSLAlertX(TLS_ALERT_DECRYPT_ERROR, false, TLS_ALERT_HANDSHAKE_FAILURE, "Decrypt error"), new SSLAlertX(TLS_ALERT_EXPORT_RESTRICTION, true, TLS_ALERT_HANDSHAKE_FAILURE, "Export restriction"), new SSLAlertX(TLS_ALERT_PROTOCOL_VERSION, true, TLS_ALERT_HANDSHAKE_FAILURE, "Protocol version"), new SSLAlertX(TLS_ALERT_INSUFFICIENT_SECURITY, true, TLS_ALERT_HANDSHAKE_FAILURE, "Insufficient security"), new SSLAlertX(TLS_ALERT_INTERNAL_ERROR, true, TLS_ALERT_HANDSHAKE_FAILURE, "Internal error"), new SSLAlertX(TLS_ALERT_USER_CANCELLED, false, TLS_ALERT_HANDSHAKE_FAILURE, "User cancelled"), new SSLAlertX(TLS_ALERT_NO_RENEGOTIATION, false, TLS_ALERT_HANDSHAKE_FAILURE, "No reenegotiation")};
    private int value;
    private int ssl_value;
    private boolean always_fatal;
    private boolean fatal;
    private String explanation;

    public SSLAlertX(int value, boolean always_fatal, int ssl_value, String explanation) {
        this.value = value;
        this.always_fatal = always_fatal;
        this.ssl_value = ssl_value;
        this.explanation = explanation;
    }

    public SSLAlertX(int thrown_value, int level) {
        int found = this.findAlert(thrown_value);
        if (found < 0) {
            found = this.findAlert(TLS_ALERT_INTERNAL_ERROR);
        }
        this.value = SSLAlertX.master_list[found].value;
        this.always_fatal = SSLAlertX.master_list[found].always_fatal;
        this.fatal = false;
        if (this.always_fatal || level != TLS_ALERT_WARNING) {
            this.fatal = true;
        }
        this.explanation = SSLAlertX.master_list[found].explanation;
    }

    public SSLAlertX(int version, int thrown_value, boolean thrown_fatal) {
        int found = this.findAlert(thrown_value);
        if (found < 0) {
            found = this.findAlert(TLS_ALERT_INTERNAL_ERROR);
        }
        if (version < 769 && SSLAlertX.master_list[found].ssl_value != 0) {
            int v3_found = this.findAlert(SSLAlertX.master_list[found].ssl_value);
            this.value = SSLAlertX.master_list[v3_found].value;
            this.always_fatal = SSLAlertX.master_list[v3_found].always_fatal;
        } else {
            this.value = SSLAlertX.master_list[found].value;
            this.always_fatal = SSLAlertX.master_list[found].always_fatal;
        }
        this.fatal = false;
        if (this.always_fatal || thrown_fatal) {
            this.fatal = true;
        }
        this.explanation = SSLAlertX.master_list[found].explanation;
        SSLDebug.debug(2, "Throwing alert " + this.explanation);
    }

    public int getValue() {
        return this.value;
    }

    public int getLevel() {
        return this.fatal ? TLS_ALERT_FATAL : TLS_ALERT_WARNING;
    }

    public boolean fatalP() {
        return this.fatal;
    }

    public String getExplanation() {
        return this.explanation;
    }

    private int findAlert(int num) {
        for (int i = 0; i < master_list.length; ++i) {
            if (SSLAlertX.master_list[i].value != num) continue;
            return i;
        }
        return -1;
    }
}

