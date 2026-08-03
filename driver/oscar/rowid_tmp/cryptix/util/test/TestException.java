/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.test;

public class TestException
extends Exception {
    public static final int FATAL_ERROR = 0;
    public static final int COMPLETE_FAILURE = 1;
    public static final int ILLEGAL_ARGUMENTS = 2;
    public static final int NO_TESTS_AVAILABLE = 3;
    public static final int PARTIAL_FAILURE = 4;
    public static final int ABORTED_BY_USER = 5;
    public static final int COMPLETE_SUCCESS = 10;
    private int errorcode;

    public int getErrorCode() {
        return this.errorcode;
    }

    public TestException(String reason, int code) {
        super(reason);
        this.errorcode = code;
    }
}

