/*
 * Decompiled with CFR 0.152.
 */
package netscape.security;

import netscape.security.Principal;
import netscape.security.Privilege;
import netscape.security.UserTarget;

public class ParameterizedTarget
extends UserTarget {
    public ParameterizedTarget() {
    }

    public ParameterizedTarget(String s1, Principal p, int i, String s2, String s3, String s4) {
    }

    public ParameterizedTarget(String s1, Principal p, int i, String s2, String s3, String s4, String s5) {
    }

    public String getDetailedInfo(Object o) {
        throw new NoClassDefFoundError("netscape.security.ParameterizedTarget");
    }

    public Privilege enablePrivilege(Principal p, Object o) {
        throw new NoClassDefFoundError("netscape.security.ParameterizedTarget");
    }

    public Privilege checkPrivilegeEnabled(Principal[] pa, Object o) {
        throw new NoClassDefFoundError("netscape.security.ParameterizedTarget");
    }
}

