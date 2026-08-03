/*
 * Decompiled with CFR 0.152.
 */
package org.xerial.snappy;

import java.io.IOException;

public class OSInfo {
    public static void main(String[] args) {
        if (args.length >= 1) {
            if ("--os".equals(args[0])) {
                System.out.print(OSInfo.getOSName());
                return;
            }
            if ("--arch".equals(args[0])) {
                System.out.print(OSInfo.getArchName());
                return;
            }
        }
        System.out.print(OSInfo.getNativeLibFolderPathForCurrentOS());
    }

    public static String getNativeLibFolderPathForCurrentOS() {
        return OSInfo.getOSName() + "/" + OSInfo.getArchName();
    }

    public static String getOSName() {
        return OSInfo.translateOSNameToFolderName(System.getProperty("os.name"));
    }

    public static String getArchName() {
        String osArch = System.getProperty("os.arch");
        if (osArch.startsWith("arm") && System.getProperty("os.name").contains("Linux")) {
            String javaHome = System.getProperty("java.home");
            try {
                String[] cmdarray = new String[]{"/bin/sh", "-c", "find '" + javaHome + "' -name 'libjvm.so' | head -1 | xargs readelf -A | " + "grep 'Tag_ABI_VFP_args: VFP registers'"};
                int exitCode = Runtime.getRuntime().exec(cmdarray).waitFor();
                if (exitCode == 0) {
                    return "armhf";
                }
            }
            catch (IOException e) {
            }
            catch (InterruptedException interruptedException) {}
        } else if (OSInfo.getOSName().equals("Mac") && (osArch.equals("universal") || osArch.equals("amd64"))) {
            return "x86_64";
        }
        return OSInfo.translateArchNameToFolderName(osArch);
    }

    static String translateOSNameToFolderName(String osName) {
        if (osName.contains("Windows")) {
            return "Windows";
        }
        if (osName.contains("Mac")) {
            return "Mac";
        }
        if (osName.contains("Linux")) {
            return "Linux";
        }
        return osName.replaceAll("\\W", "");
    }

    static String translateArchNameToFolderName(String archName) {
        return archName.replaceAll("\\W", "");
    }
}

