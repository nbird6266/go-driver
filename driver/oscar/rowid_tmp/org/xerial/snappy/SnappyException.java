/*
 * Decompiled with CFR 0.152.
 */
package org.xerial.snappy;

import org.xerial.snappy.SnappyErrorCode;

@Deprecated
public class SnappyException
extends Exception {
    private static final long serialVersionUID = 1L;
    public final SnappyErrorCode errorCode;

    public SnappyException(int code) {
        this(SnappyErrorCode.getErrorCode(code));
    }

    public SnappyException(SnappyErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public SnappyException(SnappyErrorCode errorCode, Exception e) {
        super(e);
        this.errorCode = errorCode;
    }

    public SnappyException(SnappyErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SnappyErrorCode getErrorCode() {
        return this.errorCode;
    }

    public static void throwException(int errorCode) throws SnappyException {
        throw new SnappyException(errorCode);
    }

    public String getMessage() {
        return String.format("[%s] %s", this.errorCode.name(), super.getMessage());
    }
}

