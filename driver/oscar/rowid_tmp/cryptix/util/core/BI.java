/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.core;

import cryptix.util.core.Hex;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

public class BI {
    private BI() {
    }

    public static BigInteger fromStream(InputStream is) throws IOException {
        int bits = is.read() << 8 | is.read();
        byte[] b = new byte[(bits + 7) / 8];
        is.read(b);
        return new BigInteger(1, b);
    }

    public static void toStream(BigInteger x, OutputStream os) throws IOException {
        int bits = x.bitLength();
        os.write(bits >>> 8);
        os.write(bits & 0xFF);
        os.write(BI.getMagnitude(x));
    }

    public static byte[] getMagnitude(BigInteger x) {
        byte[] y = x.toByteArray();
        int i = 0;
        while (y[i] == 0 && i < y.length - 1) {
            ++i;
        }
        byte[] result = new byte[y.length - i];
        System.arraycopy(y, i, result, 0, y.length - i);
        return result;
    }

    public static String dumpString(BigInteger x, String m) {
        if (x == null) {
            return "null";
        }
        StringBuffer sb = new StringBuffer(256);
        sb.append(m).append("Multi-Precision Integer ").append(x.bitLength()).append(" bits long...\n");
        sb.append(m).append("      sign: ");
        if (x.signum() == -1) {
            sb.append("Negative\n");
        } else {
            sb.append("Positive\n");
        }
        sb.append(m).append(" magnitude: ").append(Hex.dumpString(BI.getMagnitude(x))).append('\n');
        return sb.toString();
    }

    public static String dumpString(BigInteger x) {
        return BI.dumpString(x, "");
    }
}

