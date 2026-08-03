/*
 * Decompiled with CFR 0.152.
 */
package netscape.security;

import netscape.security.Principal;
import netscape.security.PrivilegeTable;
import netscape.security.Target;

public final class PrivilegeManager {
    public static final int PROPER_SUBSET = -1;
    public static final int EQUAL = 0;
    public static final int NO_SUBSET = 1;
    public static final int SIGNED_APPLET_DBNAME = 1;
    public static final int TEMP_FILENAME = 2;
    private static PrivilegeManager systemPrivMgr = new PrivilegeManager();

    public static void checkPrivilegeEnabled(String s) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public void checkPrivilegeEnabled(Target t) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public void checkPrivilegeEnabled(Target t, Object o) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public static void enablePrivilege(String s) {
    }

    public void enablePrivilege(Target t) {
    }

    public void enablePrivilege(Target t, Principal p) {
    }

    public void enablePrivilege(Target t, Principal p, Object o) {
    }

    public void revertPrivilege(Target t) {
    }

    public static void revertPrivilege(String s) {
    }

    public void disablePrivilege(Target t) {
    }

    public static void disablePrivilege(String s) {
    }

    public static void checkPrivilegeGranted(String s) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public void checkPrivilegeGranted(Target t) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public void checkPrivilegeGranted(Target t, Object o) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public void checkPrivilegeGranted(Target t, Principal p, Object o) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public boolean isCalledByPrincipal(Principal p, int i) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public boolean isCalledByPrincipal(Principal p) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public static Principal getSystemPrincipal() {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public static PrivilegeManager getPrivilegeManager() {
        return systemPrivMgr;
    }

    public static Principal[] getMyPrincipals() {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public Principal[] getClassPrincipals(Class cl) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public boolean hasPrincipal(Class cl, Principal p) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public int comparePrincipalArray(Principal[] pa1, Principal[] pa2) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public boolean checkMatchPrincipal(Class cl, int i) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public boolean checkMatchPrincipal(Principal p, int i) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public boolean checkMatchPrincipal(Class cl) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public boolean checkMatchPrincipalAlways() {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public Principal[] getClassPrincipalsFromStack(int i) {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }

    public PrivilegeTable getPrivilegeTableFromStack() {
        throw new NoClassDefFoundError("netscape.security.PrivilegeManager");
    }
}

