/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

import java.io.PrintWriter;
import java.util.Hashtable;
import netscape.security.ForbiddenTargetException;
import netscape.security.Principal;
import netscape.security.PrivilegeManager;
import netscape.security.Target;
import netscape.security.UserDialogHelper;
import netscape.security.UserTarget;
import xjava.lang.IJCE_ClassLoaderDepth;
import xjava.security.IJCE;
import xjava.security.IJCE_Java10Support;
import xjava.security.IJCE_Properties;

class IJCE_SecuritySupport {
    private static final boolean DEBUG = true;
    private static int debuglevel = IJCE.getDebugLevel("IJCE_SecuritySupport");
    private static PrintWriter err = IJCE.getDebugOutput();
    private static final String TARGET_HELP_FILENAME = "TargetHelp.html";
    private static String targetHelpURL;
    private static PrivilegeManager privMgr;
    private static Hashtable targets;

    static {
        targets = new Hashtable();
        targetHelpURL = TARGET_HELP_FILENAME;
        try {
            IJCE_SecuritySupport.registerTargets();
        }
        catch (Throwable t) {
            t.printStackTrace();
            IJCE.reportBug("Unexpected exception in IJCE_SecuritySupport.registerTargets()");
        }
    }

    private IJCE_SecuritySupport() {
    }

    private static void debug(String s) {
        err.println("IJCE_SecuritySupport: " + s);
    }

    private static void registerTargets() {
        block14: {
            if (debuglevel >= 4) {
                IJCE_SecuritySupport.debug("Initializing...");
            }
            try {
                Principal myPrincipal = null;
                try {
                    Principal[] principals = PrivilegeManager.getMyPrincipals();
                    if (principals == null || principals.length == 0) {
                        err.println("Warning: invalid return value from PrivilegeManager.getMyPrincipals()\nFuture security-related operations will probably fail.");
                        return;
                    }
                    myPrincipal = principals[0];
                }
                catch (NoClassDefFoundError principals) {
                    // empty catch block
                }
                if (debuglevel >= 5) {
                    IJCE_SecuritySupport.debug("myPrincipal = " + myPrincipal);
                }
                int lowRisk = UserDialogHelper.targetRiskLow();
                String lowRiskColour = UserDialogHelper.targetRiskColorLow();
                int highRisk = UserDialogHelper.targetRiskHigh();
                String highRiskColour = UserDialogHelper.targetRiskColorHigh();
                if (debuglevel >= 5) {
                    IJCE_SecuritySupport.debug("registering security targets...");
                }
                IJCE_SecuritySupport.registerTarget(myPrincipal, "AddSecurityProvider", highRisk, highRiskColour);
                IJCE_SecuritySupport.registerTarget(myPrincipal, "RemoveSecurityProvider", highRisk, highRiskColour);
                IJCE_SecuritySupport.registerTarget(myPrincipal, "SecurityPropertyRead", lowRisk, lowRiskColour);
                IJCE_SecuritySupport.registerTarget(myPrincipal, "SecurityPropertyWrite", highRisk, highRiskColour);
                boolean notFixed = true;
                try {
                    notFixed = IJCE_Java10Support.isAssignableFrom(Class.forName("java.util.Hashtable"), Class.forName("java.security.Provider"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (notFixed) {
                    IJCE_SecuritySupport.registerTarget(myPrincipal, "GetSecurityProviders", highRisk, highRiskColour);
                } else {
                    IJCE_SecuritySupport.registerTarget(myPrincipal, "GetSecurityProviders", lowRisk, lowRiskColour);
                }
                if (debuglevel >= 3) {
                    IJCE_SecuritySupport.debug("All security targets successfully registered.");
                }
            }
            catch (Exception e) {
                IJCE.debug("Warning: Unable to register security target.");
                e.printStackTrace();
            }
            catch (NoClassDefFoundError e2) {
                if (debuglevel < 1) break block14;
                e2.printStackTrace();
            }
        }
    }

    private static Target registerTarget(Principal myPrincipal, String name, int risk, String riskColour) {
        String description = IJCE_Properties.getProperty("UI.target." + name);
        String helpURL = targetHelpURL == null ? null : String.valueOf(targetHelpURL) + "#" + name;
        try {
            Target target = new UserTarget(name, myPrincipal, risk, riskColour, description, helpURL).registerTarget();
            if (debuglevel >= 6) {
                IJCE_SecuritySupport.debug("registering " + target);
            }
            targets.put(name, target);
            return target;
        }
        catch (NoClassDefFoundError e) {
            e.printStackTrace();
            return null;
        }
    }

    static Target findTarget(String name) throws ForbiddenTargetException {
        Target t = (Target)targets.get(name);
        if (t != null) {
            return t;
        }
        throw new ForbiddenTargetException("There is no security target with name \"" + name + "\"");
    }

    static Target findTarget(String name, Object arg) throws ForbiddenTargetException {
        throw new ForbiddenTargetException("This version of IJCE has no parameterized security targets");
    }

    static void checkPrivilegeEnabled(String targetname, int depth) {
        IJCE_SecuritySupport.checkPrivilegeEnabled(IJCE_SecuritySupport.findTarget(targetname), depth + 1);
    }

    static void checkPrivilegeEnabled(Target target, int depth) {
        if (target == null) {
            throw new NullPointerException("target == null");
        }
        try {
            if (privMgr == null) {
                privMgr = PrivilegeManager.getPrivilegeManager();
            }
            privMgr.checkPrivilegeEnabled(target);
            return;
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
        }
        catch (NoSuchMethodError noSuchMethodError) {
            // empty catch block
        }
        IJCE_SecuritySupport.checkSystemCaller(depth + 1);
    }

    static void checkPrivilegeEnabled(Target target, Object arg, int depth) {
        if (target == null) {
            throw new NullPointerException("target == null");
        }
        try {
            if (privMgr == null) {
                privMgr = PrivilegeManager.getPrivilegeManager();
            }
            privMgr.checkPrivilegeEnabled(target, arg);
            return;
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
        }
        catch (NoSuchMethodError noSuchMethodError) {
            // empty catch block
        }
        IJCE_SecuritySupport.checkSystemCaller(depth + 1);
    }

    static void checkSystemCaller(int depth) {
        int cldepth = IJCE_ClassLoaderDepth.classLoaderDepth();
        if (cldepth < 0) {
            return;
        }
        if (cldepth <= depth) {
            IJCE.reportBug("incorrect depth passed to IJCE_SecuritySupport.checkSystemCaller:\ndepth = " + depth + ", classLoaderDepth() = " + cldepth);
        }
        if (cldepth == depth + 1) {
            throw new SecurityException("this operation cannot be performed from a non-system class");
        }
    }
}

