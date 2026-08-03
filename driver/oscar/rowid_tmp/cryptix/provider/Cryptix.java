/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider;

import cryptix.CryptixProperties;
import java.security.Provider;
import java.util.Map;

public class Cryptix
extends Provider {
    static final long serialVersionUID = 2535048358772783954L;

    public Cryptix() {
        super("Cryptix", Cryptix.getVersionAsDouble(), "<html>\n<head><title>" + CryptixProperties.getVersionString() + "</title></head>\n" + "<body>\n" + CryptixProperties.getHtmlInfo() + "</body>\n" + "</html>\n");
        this.putAll((Map<?, ?>)CryptixProperties.getAllCryptix());
    }

    private static double getVersionAsDouble() {
        return (double)CryptixProperties.getMajorVersion() + (double)CryptixProperties.getMinorVersion() / 100.0 + (double)CryptixProperties.getIntermediateVersion() / 10000.0;
    }

    public String toString() {
        return CryptixProperties.getVersionString();
    }
}

