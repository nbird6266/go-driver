/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.provider.ClaymoreProvider;
import cryptix.provider.Cryptix;
import java.security.Provider;
import java.security.Security;

public class LoadProviders {
    private static final Class _workaround = Security.class;
    private static boolean hasOpenssl = false;

    public static void init() {
        try {
            String cl_name = "com.claymoresystems.gnp.GoNativeProvider";
            Class<?> clazz = Class.forName(cl_name);
            Provider openssl = (Provider)clazz.newInstance();
            Security.addProvider(openssl);
            hasOpenssl = true;
        }
        catch (NoClassDefFoundError e) {
        }
        catch (Exception exception) {
            // empty catch block
        }
        Security.addProvider(new Cryptix());
        Security.addProvider(new ClaymoreProvider());
    }

    public static String getDSAProvider() {
        if (hasOpenssl) {
            return "GoNativeProvider";
        }
        return "ClaymoreProvider";
    }

    public static boolean haveGoNativeProvider() {
        return hasOpenssl;
    }
}

