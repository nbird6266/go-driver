/*
 * Decompiled with CFR 0.152.
 */
package cryptix.tools;

import cryptix.util.core.Hex;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HexDump {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java HexDump filename");
            System.exit(1);
        }
        FileInputStream in = new FileInputStream(args[0]);
        byte[] data = new byte[((InputStream)in).available()];
        ((InputStream)in).read(data);
        System.out.println(Hex.dumpString(data));
    }
}

