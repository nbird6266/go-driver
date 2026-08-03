/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.cipher;

import cryptix.CryptixException;
import cryptix.provider.cipher.NativeLink;
import cryptix.util.core.Debug;
import cryptix.util.core.LinkStatus;
import java.io.PrintWriter;
import java.security.Key;
import java.security.KeyException;
import xjava.security.Cipher;
import xjava.security.SymmetricCipher;

public final class RC2
extends Cipher
implements SymmetricCipher {
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_SLOW = false;
    private static final int debuglevel = Debug.getLevel("RC2");
    private static final PrintWriter err = Debug.getOutput();
    private static NativeLink linkStatus = new NativeLink("RC2", 2, 3);
    private long native_cookie;
    private Object native_lock;
    private static final int[] S_BOX;
    private int[] sKey = new int[64];
    public static final int BLOCK_SIZE = 8;

    static {
        int[] nArray = new int[256];
        nArray[0] = 217;
        nArray[1] = 120;
        nArray[2] = 249;
        nArray[3] = 196;
        nArray[4] = 25;
        nArray[5] = 221;
        nArray[6] = 181;
        nArray[7] = 237;
        nArray[8] = 40;
        nArray[9] = 233;
        nArray[10] = 253;
        nArray[11] = 121;
        nArray[12] = 74;
        nArray[13] = 160;
        nArray[14] = 216;
        nArray[15] = 157;
        nArray[16] = 198;
        nArray[17] = 126;
        nArray[18] = 55;
        nArray[19] = 131;
        nArray[20] = 43;
        nArray[21] = 118;
        nArray[22] = 83;
        nArray[23] = 142;
        nArray[24] = 98;
        nArray[25] = 76;
        nArray[26] = 100;
        nArray[27] = 136;
        nArray[28] = 68;
        nArray[29] = 139;
        nArray[30] = 251;
        nArray[31] = 162;
        nArray[32] = 23;
        nArray[33] = 154;
        nArray[34] = 89;
        nArray[35] = 245;
        nArray[36] = 135;
        nArray[37] = 179;
        nArray[38] = 79;
        nArray[39] = 19;
        nArray[40] = 97;
        nArray[41] = 69;
        nArray[42] = 109;
        nArray[43] = 141;
        nArray[44] = 9;
        nArray[45] = 129;
        nArray[46] = 125;
        nArray[47] = 50;
        nArray[48] = 189;
        nArray[49] = 143;
        nArray[50] = 64;
        nArray[51] = 235;
        nArray[52] = 134;
        nArray[53] = 183;
        nArray[54] = 123;
        nArray[55] = 11;
        nArray[56] = 240;
        nArray[57] = 149;
        nArray[58] = 33;
        nArray[59] = 34;
        nArray[60] = 92;
        nArray[61] = 107;
        nArray[62] = 78;
        nArray[63] = 130;
        nArray[64] = 84;
        nArray[65] = 214;
        nArray[66] = 101;
        nArray[67] = 147;
        nArray[68] = 206;
        nArray[69] = 96;
        nArray[70] = 178;
        nArray[71] = 28;
        nArray[72] = 115;
        nArray[73] = 86;
        nArray[74] = 192;
        nArray[75] = 20;
        nArray[76] = 167;
        nArray[77] = 140;
        nArray[78] = 241;
        nArray[79] = 220;
        nArray[80] = 18;
        nArray[81] = 117;
        nArray[82] = 202;
        nArray[83] = 31;
        nArray[84] = 59;
        nArray[85] = 190;
        nArray[86] = 228;
        nArray[87] = 209;
        nArray[88] = 66;
        nArray[89] = 61;
        nArray[90] = 212;
        nArray[91] = 48;
        nArray[92] = 163;
        nArray[93] = 60;
        nArray[94] = 182;
        nArray[95] = 38;
        nArray[96] = 111;
        nArray[97] = 191;
        nArray[98] = 14;
        nArray[99] = 218;
        nArray[100] = 70;
        nArray[101] = 105;
        nArray[102] = 7;
        nArray[103] = 87;
        nArray[104] = 39;
        nArray[105] = 242;
        nArray[106] = 29;
        nArray[107] = 155;
        nArray[108] = 188;
        nArray[109] = 148;
        nArray[110] = 67;
        nArray[111] = 3;
        nArray[112] = 248;
        nArray[113] = 17;
        nArray[114] = 199;
        nArray[115] = 246;
        nArray[116] = 144;
        nArray[117] = 239;
        nArray[118] = 62;
        nArray[119] = 231;
        nArray[120] = 6;
        nArray[121] = 195;
        nArray[122] = 213;
        nArray[123] = 47;
        nArray[124] = 200;
        nArray[125] = 102;
        nArray[126] = 30;
        nArray[127] = 215;
        nArray[128] = 8;
        nArray[129] = 232;
        nArray[130] = 234;
        nArray[131] = 222;
        nArray[132] = 128;
        nArray[133] = 82;
        nArray[134] = 238;
        nArray[135] = 247;
        nArray[136] = 132;
        nArray[137] = 170;
        nArray[138] = 114;
        nArray[139] = 172;
        nArray[140] = 53;
        nArray[141] = 77;
        nArray[142] = 106;
        nArray[143] = 42;
        nArray[144] = 150;
        nArray[145] = 26;
        nArray[146] = 210;
        nArray[147] = 113;
        nArray[148] = 90;
        nArray[149] = 21;
        nArray[150] = 73;
        nArray[151] = 116;
        nArray[152] = 75;
        nArray[153] = 159;
        nArray[154] = 208;
        nArray[155] = 94;
        nArray[156] = 4;
        nArray[157] = 24;
        nArray[158] = 164;
        nArray[159] = 236;
        nArray[160] = 194;
        nArray[161] = 224;
        nArray[162] = 65;
        nArray[163] = 110;
        nArray[164] = 15;
        nArray[165] = 81;
        nArray[166] = 203;
        nArray[167] = 204;
        nArray[168] = 36;
        nArray[169] = 145;
        nArray[170] = 175;
        nArray[171] = 80;
        nArray[172] = 161;
        nArray[173] = 244;
        nArray[174] = 112;
        nArray[175] = 57;
        nArray[176] = 153;
        nArray[177] = 124;
        nArray[178] = 58;
        nArray[179] = 133;
        nArray[180] = 35;
        nArray[181] = 184;
        nArray[182] = 180;
        nArray[183] = 122;
        nArray[184] = 252;
        nArray[185] = 2;
        nArray[186] = 54;
        nArray[187] = 91;
        nArray[188] = 37;
        nArray[189] = 85;
        nArray[190] = 151;
        nArray[191] = 49;
        nArray[192] = 45;
        nArray[193] = 93;
        nArray[194] = 250;
        nArray[195] = 152;
        nArray[196] = 227;
        nArray[197] = 138;
        nArray[198] = 146;
        nArray[199] = 174;
        nArray[200] = 5;
        nArray[201] = 223;
        nArray[202] = 41;
        nArray[203] = 16;
        nArray[204] = 103;
        nArray[205] = 108;
        nArray[206] = 186;
        nArray[207] = 201;
        nArray[208] = 211;
        nArray[210] = 230;
        nArray[211] = 207;
        nArray[212] = 225;
        nArray[213] = 158;
        nArray[214] = 168;
        nArray[215] = 44;
        nArray[216] = 99;
        nArray[217] = 22;
        nArray[218] = 1;
        nArray[219] = 63;
        nArray[220] = 88;
        nArray[221] = 226;
        nArray[222] = 137;
        nArray[223] = 169;
        nArray[224] = 13;
        nArray[225] = 56;
        nArray[226] = 52;
        nArray[227] = 27;
        nArray[228] = 171;
        nArray[229] = 51;
        nArray[230] = 255;
        nArray[231] = 176;
        nArray[232] = 187;
        nArray[233] = 72;
        nArray[234] = 12;
        nArray[235] = 95;
        nArray[236] = 185;
        nArray[237] = 177;
        nArray[238] = 205;
        nArray[239] = 46;
        nArray[240] = 197;
        nArray[241] = 243;
        nArray[242] = 219;
        nArray[243] = 71;
        nArray[244] = 229;
        nArray[245] = 165;
        nArray[246] = 156;
        nArray[247] = 119;
        nArray[248] = 10;
        nArray[249] = 166;
        nArray[250] = 32;
        nArray[251] = 104;
        nArray[252] = 254;
        nArray[253] = 127;
        nArray[254] = 193;
        nArray[255] = 173;
        S_BOX = nArray;
    }

    private static void debug(String s) {
        err.println("RC2: " + s);
    }

    public static LinkStatus getLinkStatus() {
        return linkStatus;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void link() {
        NativeLink nativeLink = linkStatus;
        synchronized (nativeLink) {
            block8: {
                try {
                    if (linkStatus.attemptLoad()) {
                        linkStatus.checkVersion(RC2.getLibMajorVersion(), RC2.getLibMinorVersion());
                        linkStatus.check(this.native_clinit());
                    }
                    if (linkStatus.useNative()) {
                        linkStatus.check(this.native_init());
                        this.native_lock = new Object();
                    }
                }
                catch (UnsatisfiedLinkError e) {
                    linkStatus.fail(e);
                    if (debuglevel <= 2) break block8;
                    RC2.debug(e.getMessage());
                }
            }
            if (debuglevel > 2) {
                RC2.debug("Using native library? " + (this.native_lock != null));
            }
        }
    }

    private static native int getLibMajorVersion();

    private static native int getLibMinorVersion();

    private native String native_clinit();

    private native String native_init();

    private native String native_ks(long var1, byte[] var3);

    private native int native_crypt(long var1, byte[] var3, int var4, byte[] var5, int var6, boolean var7);

    private native String native_finalize();

    public RC2() {
        super(false, false, "Cryptix");
        this.link();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void finalize() {
        if (this.native_lock != null) {
            Object object = this.native_lock;
            synchronized (object) {
                String error = this.native_finalize();
                if (error != null) {
                    RC2.debug(String.valueOf(error) + " in native_finalize");
                }
            }
        }
    }

    public final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public int engineBlockSize() {
        return 8;
    }

    public void engineInitEncrypt(Key key) throws KeyException {
        this.makeKey(key);
    }

    public void engineInitDecrypt(Key key) throws KeyException {
        this.makeKey(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        Object newin;
        boolean doEncrypt;
        if (inLen < 0) {
            throw new IllegalArgumentException("inLen < 0");
        }
        int blockCount = inLen / 8;
        inLen = blockCount * 8;
        boolean bl = doEncrypt = this.getState() == 1;
        if (in == out && (outOffset >= inOffset && (long)outOffset < (long)inOffset + (long)inLen || inOffset >= outOffset && (long)inOffset < (long)outOffset + (long)inLen)) {
            newin = new byte[inLen];
            System.arraycopy(in, inOffset, newin, 0, inLen);
            in = newin;
            inOffset = 0;
        }
        if (this.native_lock != null) {
            newin = this.native_lock;
            synchronized (newin) {
                if (inOffset < 0 || (long)inOffset + (long)inLen > (long)((byte[])in).length || outOffset < 0 || (long)outOffset + (long)inLen > (long)out.length) {
                    throw new ArrayIndexOutOfBoundsException(String.valueOf(this.getAlgorithm()) + ": Arguments to native_crypt would cause a buffer overflow");
                }
                int i = 0;
                while (i < blockCount) {
                    if (this.native_crypt(this.native_cookie, (byte[])in, inOffset, out, outOffset, doEncrypt) == 0) {
                        throw new CryptixException(String.valueOf(this.getAlgorithm()) + ": Error in native code");
                    }
                    inOffset += 8;
                    outOffset += 8;
                    ++i;
                }
            }
        } else if (doEncrypt) {
            int i = 0;
            while (i < blockCount) {
                this.blockEncrypt((byte[])in, inOffset, out, outOffset);
                inOffset += 8;
                outOffset += 8;
                ++i;
            }
        } else {
            int i = 0;
            while (i < blockCount) {
                this.blockDecrypt((byte[])in, inOffset, out, outOffset);
                inOffset += 8;
                outOffset += 8;
                ++i;
            }
        }
        return inLen;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void makeKey(Key key) throws KeyException {
        byte[] userkey = key.getEncoded();
        if (userkey == null) {
            throw new KeyException("Null RC2 user key");
        }
        int len = userkey.length;
        if (len > 128) {
            throw new KeyException("Invalid RC2 user key size");
        }
        if (this.native_lock != null) {
            Object object = this.native_lock;
            synchronized (object) {
                try {
                    linkStatus.check(this.native_ks(this.native_cookie, userkey));
                    return;
                }
                catch (Error error) {
                    this.native_finalize();
                    this.native_lock = null;
                    if (debuglevel > 0) {
                        RC2.debug(error + ". Will use 100% Java.");
                    }
                }
            }
        }
        int[] sk = new int[128];
        int i = 0;
        while (i < len) {
            sk[i] = userkey[i] & 0xFF;
            ++i;
        }
        i = len;
        while (i < 128) {
            sk[i] = S_BOX[sk[i - len] + sk[i - 1] & 0xFF];
            ++i;
        }
        sk[128 - len] = S_BOX[sk[128 - len] & 0xFF];
        i = 127 - len;
        while (i >= 0) {
            sk[i] = S_BOX[sk[i + len] ^ sk[i + 1]];
            --i;
        }
        i = 63;
        while (i >= 0) {
            this.sKey[i] = (sk[i * 2 + 1] << 8 | sk[i * 2]) & 0xFFFF;
            --i;
        }
    }

    private void blockEncrypt(byte[] in, int inOff, byte[] out, int outOff) {
        int w0 = in[inOff++] & 0xFF | (in[inOff++] & 0xFF) << 8;
        int w1 = in[inOff++] & 0xFF | (in[inOff++] & 0xFF) << 8;
        int w2 = in[inOff++] & 0xFF | (in[inOff++] & 0xFF) << 8;
        int w3 = in[inOff++] & 0xFF | (in[inOff] & 0xFF) << 8;
        int j = 0;
        int i = 0;
        while (i < 16) {
            w0 = w0 + (w1 & ~w3) + (w2 & w3) + this.sKey[j++] & 0xFFFF;
            w0 = w0 << 1 | w0 >>> 15;
            w1 = w1 + (w2 & ~w0) + (w3 & w0) + this.sKey[j++] & 0xFFFF;
            w1 = w1 << 2 | w1 >>> 14;
            w2 = w2 + (w3 & ~w1) + (w0 & w1) + this.sKey[j++] & 0xFFFF;
            w2 = w2 << 3 | w2 >>> 13;
            w3 = w3 + (w0 & ~w2) + (w1 & w2) + this.sKey[j++] & 0xFFFF;
            w3 = w3 << 5 | w3 >>> 11;
            if (i == 4 || i == 10) {
                w2 += this.sKey[(w1 += this.sKey[(w0 += this.sKey[w3 & 0x3F]) & 0x3F]) & 0x3F];
                w3 += this.sKey[w2 & 0x3F];
            }
            ++i;
        }
        out[outOff++] = (byte)w0;
        out[outOff++] = (byte)(w0 >>> 8);
        out[outOff++] = (byte)w1;
        out[outOff++] = (byte)(w1 >>> 8);
        out[outOff++] = (byte)w2;
        out[outOff++] = (byte)(w2 >>> 8);
        out[outOff++] = (byte)w3;
        out[outOff] = (byte)(w3 >>> 8);
    }

    private void blockDecrypt(byte[] in, int inOff, byte[] out, int outOff) {
        int w0 = in[inOff + 0] & 0xFF | (in[inOff + 1] & 0xFF) << 8;
        int w1 = in[inOff + 2] & 0xFF | (in[inOff + 3] & 0xFF) << 8;
        int w2 = in[inOff + 4] & 0xFF | (in[inOff + 5] & 0xFF) << 8;
        int w3 = in[inOff + 6] & 0xFF | (in[inOff + 7] & 0xFF) << 8;
        int j = 63;
        int i = 15;
        while (i >= 0) {
            w3 = (w3 >>> 5 | w3 << 11) & 0xFFFF;
            w3 = w3 - (w0 & ~w2) - (w1 & w2) - this.sKey[j--] & 0xFFFF;
            w2 = (w2 >>> 3 | w2 << 13) & 0xFFFF;
            w2 = w2 - (w3 & ~w1) - (w0 & w1) - this.sKey[j--] & 0xFFFF;
            w1 = (w1 >>> 2 | w1 << 14) & 0xFFFF;
            w1 = w1 - (w2 & ~w0) - (w3 & w0) - this.sKey[j--] & 0xFFFF;
            w0 = (w0 >>> 1 | w0 << 15) & 0xFFFF;
            w0 = w0 - (w1 & ~w3) - (w2 & w3) - this.sKey[j--] & 0xFFFF;
            if (i == 11 || i == 5) {
                w3 = w3 - this.sKey[w2 & 0x3F] & 0xFFFF;
                w2 = w2 - this.sKey[w1 & 0x3F] & 0xFFFF;
                w1 = w1 - this.sKey[w0 & 0x3F] & 0xFFFF;
                w0 = w0 - this.sKey[w3 & 0x3F] & 0xFFFF;
            }
            --i;
        }
        out[outOff++] = (byte)w0;
        out[outOff++] = (byte)(w0 >>> 8);
        out[outOff++] = (byte)w1;
        out[outOff++] = (byte)(w1 >>> 8);
        out[outOff++] = (byte)w2;
        out[outOff++] = (byte)(w2 >>> 8);
        out[outOff++] = (byte)w3;
        out[outOff] = (byte)(w3 >>> 8);
    }
}

