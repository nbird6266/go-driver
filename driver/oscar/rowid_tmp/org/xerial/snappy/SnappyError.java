/*
 * Decompiled with CFR 0.152.
 */
package org.xerial.snappy;

import org.xerial.snappy.SnappyErrorCode;

public class SnappyError
extends Error {
    private static final long serialVersionUID = 1L;
    public final SnappyErrorCode errorCode;

    public SnappyError(SnappyErrorCode code) {
        this.errorCode = code;
    }

    public SnappyError(SnappyErrorCode code, Error e) {
        super(e);
        this.errorCode = code;
    }

    public SnappyError(SnappyErrorCode code, String message) {
        super(message);
        this.errorCode = code;
    }

    public String getMessage() {
        return String.format("[%s] %s", this.errorCode.name(), super.getMessage());
    }
}

