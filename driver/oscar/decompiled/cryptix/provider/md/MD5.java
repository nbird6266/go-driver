/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.md;

import cryptix.provider.md.BlockMessageDigest;
import cryptix.util.core.Debug;
import java.io.PrintWriter;

public final class MD5
extends BlockMessageDigest
implements Cloneable {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel = Debug.getLevel("MD5");
    private static final PrintWriter err = Debug.getOutput();
    private static final int HASH_LENGTH = 16;
    private static final int DATA_LENGTH = 64;
    private int[] data;
    private int[] digest;
    private byte[] tmp;
    private static String[] texts = new String[]{"", "message digest", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"};
    private static byte[][] hashs;

    static {
        byte[][] byArrayArray = new byte[3][];
        byte[] byArray = new byte[16];
        byArray[0] = -44;
        byArray[1] = 29;
        byArray[2] = -116;
        byArray[3] = -39;
        byArray[4] = -113;
        byArray[6] = -78;
        byArray[7] = 4;
        byArray[8] = -23;
        byArray[9] = -128;
        byArray[10] = 9;
        byArray[11] = -104;
        byArray[12] = -20;
        byArray[13] = -8;
        byArray[14] = 66;
        byArray[15] = 126;
        byArrayArray[0] = byArray;
        byArrayArray[1] = new byte[]{-7, 107, 105, 125, 124, -73, -109, -115, 82, 90, 47, 49, -86, -15, 97, -48};
        byArrayArray[2] = new byte[]{-47, 116, -85, -104, -46, 119, -39, -11, -91, 97, 28, 44, -97, 65, -99, -97};
        hashs = byArrayArray;
    }

    private static void debug(String s) {
        err.println("MD5: " + s);
    }

    protected int engineGetDigestLength() {
        return 16;
    }

    protected int engineGetDataLength() {
        return 64;
    }

    public MD5() {
        super("MD5");
        this.java_init();
        this.reset();
    }

    private void java_init() {
        this.digest = new int[4];
        this.data = new int[16];
        this.tmp = new byte[64];
    }

    private MD5(MD5 md) {
        this();
        this.data = (int[])md.data.clone();
        this.digest = (int[])md.digest.clone();
        this.tmp = (byte[])md.tmp.clone();
    }

    public Object clone() {
        return new MD5(this);
    }

    protected void engineReset() {
        super.engineReset();
        this.java_reset();
    }

    private void java_reset() {
        this.digest[0] = 1732584193;
        this.digest[1] = -271733879;
        this.digest[2] = -1732584194;
        this.digest[3] = 271733878;
    }

    protected void engineTransform(byte[] in) {
        this.java_transform(in);
    }

    private void java_transform(byte[] in) {
        MD5.byte2int(in, 0, this.data, 0, 16);
        this.transform(this.data);
    }

    protected byte[] engineDigest(byte[] in, int length) {
        byte[] b = this.java_digest(in, length);
        this.engineReset();
        return b;
    }

    private byte[] java_digest(byte[] in, int pos) {
        if (pos != 0) {
            System.arraycopy(in, 0, this.tmp, 0, pos);
        }
        this.tmp[pos++] = -128;
        if (pos > 56) {
            while (pos < 64) {
                this.tmp[pos++] = 0;
            }
            MD5.byte2int(this.tmp, 0, this.data, 0, 16);
            this.transform(this.data);
            pos = 0;
        }
        while (pos < 56) {
            this.tmp[pos++] = 0;
        }
        MD5.byte2int(this.tmp, 0, this.data, 0, 14);
        long bc = this.bitcount();
        this.data[14] = (int)bc;
        this.data[15] = (int)(bc >>> 32);
        this.transform(this.data);
        byte[] buf = new byte[16];
        int off = 0;
        int i = 0;
        while (i < 4) {
            int d = this.digest[i];
            buf[off++] = (byte)d;
            buf[off++] = (byte)(d >>> 8);
            buf[off++] = (byte)(d >>> 16);
            buf[off++] = (byte)(d >>> 24);
            ++i;
        }
        return buf;
    }

    protected static int F(int x, int y, int z) {
        return z ^ x & (y ^ z);
    }

    protected static int G(int x, int y, int z) {
        return y ^ z & (x ^ y);
    }

    protected static int H(int x, int y, int z) {
        return x ^ y ^ z;
    }

    protected static int I(int x, int y, int z) {
        return y ^ (x | ~z);
    }

    protected static int FF(int a, int b, int c, int d, int k, int s, int t) {
        a += k + t + MD5.F(b, c, d);
        a = a << s | a >>> -s;
        return a + b;
    }

    protected static int GG(int a, int b, int c, int d, int k, int s, int t) {
        a += k + t + MD5.G(b, c, d);
        a = a << s | a >>> -s;
        return a + b;
    }

    protected static int HH(int a, int b, int c, int d, int k, int s, int t) {
        a += k + t + MD5.H(b, c, d);
        a = a << s | a >>> -s;
        return a + b;
    }

    protected static int II(int a, int b, int c, int d, int k, int s, int t) {
        a += k + t + MD5.I(b, c, d);
        a = a << s | a >>> -s;
        return a + b;
    }

    protected void transform(int[] M) {
        if (debuglevel > 5) {
            MD5.debug("d1 " + this.digest[0] + " d1 " + this.digest[1] + " d2 " + this.digest[2] + " d3 " + this.digest[3]);
        }
        if (debuglevel > 8) {
            int xx = 0;
            while (xx < 16) {
                MD5.debug("      " + xx + "= " + M[xx]);
                ++xx;
            }
        }
        int a = this.digest[0];
        int b = this.digest[1];
        int c = this.digest[2];
        int d = this.digest[3];
        a = MD5.FF(a, b, c, d, M[0], 7, -680876936);
        d = MD5.FF(d, a, b, c, M[1], 12, -389564586);
        c = MD5.FF(c, d, a, b, M[2], 17, 606105819);
        b = MD5.FF(b, c, d, a, M[3], 22, -1044525330);
        a = MD5.FF(a, b, c, d, M[4], 7, -176418897);
        d = MD5.FF(d, a, b, c, M[5], 12, 1200080426);
        c = MD5.FF(c, d, a, b, M[6], 17, -1473231341);
        b = MD5.FF(b, c, d, a, M[7], 22, -45705983);
        a = MD5.FF(a, b, c, d, M[8], 7, 1770035416);
        d = MD5.FF(d, a, b, c, M[9], 12, -1958414417);
        c = MD5.FF(c, d, a, b, M[10], 17, -42063);
        b = MD5.FF(b, c, d, a, M[11], 22, -1990404162);
        a = MD5.FF(a, b, c, d, M[12], 7, 1804603682);
        d = MD5.FF(d, a, b, c, M[13], 12, -40341101);
        c = MD5.FF(c, d, a, b, M[14], 17, -1502002290);
        b = MD5.FF(b, c, d, a, M[15], 22, 1236535329);
        a = MD5.GG(a, b, c, d, M[1], 5, -165796510);
        d = MD5.GG(d, a, b, c, M[6], 9, -1069501632);
        c = MD5.GG(c, d, a, b, M[11], 14, 643717713);
        b = MD5.GG(b, c, d, a, M[0], 20, -373897302);
        a = MD5.GG(a, b, c, d, M[5], 5, -701558691);
        d = MD5.GG(d, a, b, c, M[10], 9, 38016083);
        c = MD5.GG(c, d, a, b, M[15], 14, -660478335);
        b = MD5.GG(b, c, d, a, M[4], 20, -405537848);
        a = MD5.GG(a, b, c, d, M[9], 5, 568446438);
        d = MD5.GG(d, a, b, c, M[14], 9, -1019803690);
        c = MD5.GG(c, d, a, b, M[3], 14, -187363961);
        b = MD5.GG(b, c, d, a, M[8], 20, 1163531501);
        a = MD5.GG(a, b, c, d, M[13], 5, -1444681467);
        d = MD5.GG(d, a, b, c, M[2], 9, -51403784);
        c = MD5.GG(c, d, a, b, M[7], 14, 1735328473);
        b = MD5.GG(b, c, d, a, M[12], 20, -1926607734);
        a = MD5.HH(a, b, c, d, M[5], 4, -378558);
        d = MD5.HH(d, a, b, c, M[8], 11, -2022574463);
        c = MD5.HH(c, d, a, b, M[11], 16, 1839030562);
        b = MD5.HH(b, c, d, a, M[14], 23, -35309556);
        a = MD5.HH(a, b, c, d, M[1], 4, -1530992060);
        d = MD5.HH(d, a, b, c, M[4], 11, 1272893353);
        c = MD5.HH(c, d, a, b, M[7], 16, -155497632);
        b = MD5.HH(b, c, d, a, M[10], 23, -1094730640);
        a = MD5.HH(a, b, c, d, M[13], 4, 681279174);
        d = MD5.HH(d, a, b, c, M[0], 11, -358537222);
        c = MD5.HH(c, d, a, b, M[3], 16, -722521979);
        b = MD5.HH(b, c, d, a, M[6], 23, 76029189);
        a = MD5.HH(a, b, c, d, M[9], 4, -640364487);
        d = MD5.HH(d, a, b, c, M[12], 11, -421815835);
        c = MD5.HH(c, d, a, b, M[15], 16, 530742520);
        b = MD5.HH(b, c, d, a, M[2], 23, -995338651);
        a = MD5.II(a, b, c, d, M[0], 6, -198630844);
        d = MD5.II(d, a, b, c, M[7], 10, 1126891415);
        c = MD5.II(c, d, a, b, M[14], 15, -1416354905);
        b = MD5.II(b, c, d, a, M[5], 21, -57434055);
        a = MD5.II(a, b, c, d, M[12], 6, 1700485571);
        d = MD5.II(d, a, b, c, M[3], 10, -1894986606);
        c = MD5.II(c, d, a, b, M[10], 15, -1051523);
        b = MD5.II(b, c, d, a, M[1], 21, -2054922799);
        a = MD5.II(a, b, c, d, M[8], 6, 1873313359);
        d = MD5.II(d, a, b, c, M[15], 10, -30611744);
        c = MD5.II(c, d, a, b, M[6], 15, -1560198380);
        b = MD5.II(b, c, d, a, M[13], 21, 1309151649);
        a = MD5.II(a, b, c, d, M[4], 6, -145523070);
        d = MD5.II(d, a, b, c, M[11], 10, -1120210379);
        c = MD5.II(c, d, a, b, M[2], 15, 718787259);
        b = MD5.II(b, c, d, a, M[9], 21, -343485551);
        this.digest[0] = this.digest[0] + a;
        this.digest[1] = this.digest[1] + b;
        this.digest[2] = this.digest[2] + c;
        this.digest[3] = this.digest[3] + d;
        if (debuglevel > 4) {
            MD5.debug("d1 " + this.digest[0] + " d1 " + this.digest[1] + " d2 " + this.digest[2] + " d3 " + this.digest[3]);
        }
    }

    private static void byte2int(byte[] src, int srcOffset, int[] dst, int dstOffset, int length) {
        while (length-- > 0) {
            dst[dstOffset++] = src[srcOffset++] & 0xFF | (src[srcOffset++] & 0xFF) << 8 | (src[srcOffset++] & 0xFF) << 16 | (src[srcOffset++] & 0xFF) << 24;
        }
    }

    public static final void main(String[] argv) {
        try {
            MD5.self_test();
        }
        catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static final void self_test() throws Exception {
        int length = hashs[0].length;
        int i = 0;
        while (i < texts.length) {
            MD5 hash = new MD5();
            byte[] text = texts[i].getBytes();
            int j = 0;
            while (j < texts[i].length()) {
                hash.engineUpdate(text[j]);
                ++j;
            }
            if (MD5.notEquals(hash.engineDigest(), hashs[i])) {
                throw new Exception("hash #" + i + " failed");
            }
            ++i;
        }
    }

    private static final boolean notEquals(byte[] a, byte[] b) {
        int i = 0;
        while (i < a.length) {
            if (a[i] != b[i]) {
                return true;
            }
            ++i;
        }
        return false;
    }
}

