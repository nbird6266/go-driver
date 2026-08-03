/*
 * Decompiled with CFR 0.152.
 */
package netscape.security;

public class UserDialogHelper {
    private static final int LOW_RISK = 0;
    private static final int MEDIUM_RISK = 1;
    private static final int HIGH_RISK = 2;

    private UserDialogHelper() {
    }

    public static String targetRiskStr(int i) {
        switch (i) {
            case 0: {
                return "low";
            }
            case 1: {
                return "medium";
            }
            case 2: {
                return "high";
            }
        }
        throw new IllegalArgumentException("unrecognized risk code: " + i);
    }

    public static int targetRiskLow() {
        return 0;
    }

    public static String targetRiskColorLow() {
        return "?";
    }

    public static int targetRiskMedium() {
        return 1;
    }

    public static String targetRiskColorMedium() {
        return "?";
    }

    public static int targetRiskHigh() {
        return 2;
    }

    public static String targetRiskColorHigh() {
        return "?";
    }
}

