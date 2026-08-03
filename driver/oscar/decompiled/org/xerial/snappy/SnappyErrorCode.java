/*
 * Decompiled with CFR 0.152.
 */
package org.xerial.snappy;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum SnappyErrorCode {
    UNKNOWN(0),
    FAILED_TO_LOAD_NATIVE_LIBRARY(1),
    PARSING_ERROR(2),
    NOT_A_DIRECT_BUFFER(3),
    OUT_OF_MEMORY(4),
    FAILED_TO_UNCOMPRESS(5);

    public final int id;

    private SnappyErrorCode(int id) {
        this.id = id;
    }

    public static SnappyErrorCode getErrorCode(int id) {
        for (SnappyErrorCode code : SnappyErrorCode.values()) {
            if (code.id != id) continue;
            return code;
        }
        return UNKNOWN;
    }

    public static String getErrorMessage(int id) {
        return SnappyErrorCode.getErrorCode(id).name();
    }
}

