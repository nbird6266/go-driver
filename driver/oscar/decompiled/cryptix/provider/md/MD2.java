/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.md;

import java.security.MessageDigest;

public class MD2
extends MessageDigest
implements Cloneable {
    private static final int BLOCK_LENGTH = 16;
    private int[] checksum = new int[16];
    private int count;
    private int[] buffer = new int[16];
    private int[] X = new int[48];
    private static final int[] S;

    static {
        int[] nArray = new int[256];
        nArray[0] = 41;
        nArray[1] = 46;
        nArray[2] = 67;
        nArray[3] = 201;
        nArray[4] = 162;
        nArray[5] = 216;
        nArray[6] = 124;
        nArray[7] = 1;
        nArray[8] = 61;
        nArray[9] = 54;
        nArray[10] = 84;
        nArray[11] = 161;
        nArray[12] = 236;
        nArray[13] = 240;
        nArray[14] = 6;
        nArray[15] = 19;
        nArray[16] = 98;
        nArray[17] = 167;
        nArray[18] = 5;
        nArray[19] = 243;
        nArray[20] = 192;
        nArray[21] = 199;
        nArray[22] = 115;
        nArray[23] = 140;
        nArray[24] = 152;
        nArray[25] = 147;
        nArray[26] = 43;
        nArray[27] = 217;
        nArray[28] = 188;
        nArray[29] = 76;
        nArray[30] = 130;
        nArray[31] = 202;
        nArray[32] = 30;
        nArray[33] = 155;
        nArray[34] = 87;
        nArray[35] = 60;
        nArray[36] = 253;
        nArray[37] = 212;
        nArray[38] = 224;
        nArray[39] = 22;
        nArray[40] = 103;
        nArray[41] = 66;
        nArray[42] = 111;
        nArray[43] = 24;
        nArray[44] = 138;
        nArray[45] = 23;
        nArray[46] = 229;
        nArray[47] = 18;
        nArray[48] = 190;
        nArray[49] = 78;
        nArray[50] = 196;
        nArray[51] = 214;
        nArray[52] = 218;
        nArray[53] = 158;
        nArray[54] = 222;
        nArray[55] = 73;
        nArray[56] = 160;
        nArray[57] = 251;
        nArray[58] = 245;
        nArray[59] = 142;
        nArray[60] = 187;
        nArray[61] = 47;
        nArray[62] = 238;
        nArray[63] = 122;
        nArray[64] = 169;
        nArray[65] = 104;
        nArray[66] = 121;
        nArray[67] = 145;
        nArray[68] = 21;
        nArray[69] = 178;
        nArray[70] = 7;
        nArray[71] = 63;
        nArray[72] = 148;
        nArray[73] = 194;
        nArray[74] = 16;
        nArray[75] = 137;
        nArray[76] = 11;
        nArray[77] = 34;
        nArray[78] = 95;
        nArray[79] = 33;
        nArray[80] = 128;
        nArray[81] = 127;
        nArray[82] = 93;
        nArray[83] = 154;
        nArray[84] = 90;
        nArray[85] = 144;
        nArray[86] = 50;
        nArray[87] = 39;
        nArray[88] = 53;
        nArray[89] = 62;
        nArray[90] = 204;
        nArray[91] = 231;
        nArray[92] = 191;
        nArray[93] = 247;
        nArray[94] = 151;
        nArray[95] = 3;
        nArray[96] = 255;
        nArray[97] = 25;
        nArray[98] = 48;
        nArray[99] = 179;
        nArray[100] = 72;
        nArray[101] = 165;
        nArray[102] = 181;
        nArray[103] = 209;
        nArray[104] = 215;
        nArray[105] = 94;
        nArray[106] = 146;
        nArray[107] = 42;
        nArray[108] = 172;
        nArray[109] = 86;
        nArray[110] = 170;
        nArray[111] = 198;
        nArray[112] = 79;
        nArray[113] = 184;
        nArray[114] = 56;
        nArray[115] = 210;
        nArray[116] = 150;
        nArray[117] = 164;
        nArray[118] = 125;
        nArray[119] = 182;
        nArray[120] = 118;
        nArray[121] = 252;
        nArray[122] = 107;
        nArray[123] = 226;
        nArray[124] = 156;
        nArray[125] = 116;
        nArray[126] = 4;
        nArray[127] = 241;
        nArray[128] = 69;
        nArray[129] = 157;
        nArray[130] = 112;
        nArray[131] = 89;
        nArray[132] = 100;
        nArray[133] = 113;
        nArray[134] = 135;
        nArray[135] = 32;
        nArray[136] = 134;
        nArray[137] = 91;
        nArray[138] = 207;
        nArray[139] = 101;
        nArray[140] = 230;
        nArray[141] = 45;
        nArray[142] = 168;
        nArray[143] = 2;
        nArray[144] = 27;
        nArray[145] = 96;
        nArray[146] = 37;
        nArray[147] = 173;
        nArray[148] = 174;
        nArray[149] = 176;
        nArray[150] = 185;
        nArray[151] = 246;
        nArray[152] = 28;
        nArray[153] = 70;
        nArray[154] = 97;
        nArray[155] = 105;
        nArray[156] = 52;
        nArray[157] = 64;
        nArray[158] = 126;
        nArray[159] = 15;
        nArray[160] = 85;
        nArray[161] = 71;
        nArray[162] = 163;
        nArray[163] = 35;
        nArray[164] = 221;
        nArray[165] = 81;
        nArray[166] = 175;
        nArray[167] = 58;
        nArray[168] = 195;
        nArray[169] = 92;
        nArray[170] = 249;
        nArray[171] = 206;
        nArray[172] = 186;
        nArray[173] = 197;
        nArray[174] = 234;
        nArray[175] = 38;
        nArray[176] = 44;
        nArray[177] = 83;
        nArray[178] = 13;
        nArray[179] = 110;
        nArray[180] = 133;
        nArray[181] = 40;
        nArray[182] = 132;
        nArray[183] = 9;
        nArray[184] = 211;
        nArray[185] = 223;
        nArray[186] = 205;
        nArray[187] = 244;
        nArray[188] = 65;
        nArray[189] = 129;
        nArray[190] = 77;
        nArray[191] = 82;
        nArray[192] = 106;
        nArray[193] = 220;
        nArray[194] = 55;
        nArray[195] = 200;
        nArray[196] = 108;
        nArray[197] = 193;
        nArray[198] = 171;
        nArray[199] = 250;
        nArray[200] = 36;
        nArray[201] = 225;
        nArray[202] = 123;
        nArray[203] = 8;
        nArray[204] = 12;
        nArray[205] = 189;
        nArray[206] = 177;
        nArray[207] = 74;
        nArray[208] = 120;
        nArray[209] = 136;
        nArray[210] = 149;
        nArray[211] = 139;
        nArray[212] = 227;
        nArray[213] = 99;
        nArray[214] = 232;
        nArray[215] = 109;
        nArray[216] = 233;
        nArray[217] = 203;
        nArray[218] = 213;
        nArray[219] = 254;
        nArray[220] = 59;
        nArray[222] = 29;
        nArray[223] = 57;
        nArray[224] = 242;
        nArray[225] = 239;
        nArray[226] = 183;
        nArray[227] = 14;
        nArray[228] = 102;
        nArray[229] = 88;
        nArray[230] = 208;
        nArray[231] = 228;
        nArray[232] = 166;
        nArray[233] = 119;
        nArray[234] = 114;
        nArray[235] = 248;
        nArray[236] = 235;
        nArray[237] = 117;
        nArray[238] = 75;
        nArray[239] = 10;
        nArray[240] = 49;
        nArray[241] = 68;
        nArray[242] = 80;
        nArray[243] = 180;
        nArray[244] = 143;
        nArray[245] = 237;
        nArray[246] = 31;
        nArray[247] = 26;
        nArray[248] = 219;
        nArray[249] = 153;
        nArray[250] = 141;
        nArray[251] = 51;
        nArray[252] = 159;
        nArray[253] = 17;
        nArray[254] = 131;
        nArray[255] = 20;
        S = nArray;
    }

    public MD2() {
        super("MD2");
        this.engineReset();
    }

    private MD2(MD2 md) {
        this();
        this.X = (int[])md.X.clone();
        this.checksum = (int[])md.checksum.clone();
        this.buffer = (int[])md.buffer.clone();
        this.count = md.count;
    }

    public Object clone() {
        return new MD2(this);
    }

    public void engineReset() {
        this.count = 0;
        int i = 0;
        while (i < 16) {
            this.X[i] = 0;
            this.checksum[i] = 0;
            ++i;
        }
    }

    public void engineUpdate(byte input) {
        this.buffer[this.count] = input & 0xFF;
        if (this.count == 15) {
            this.transform(this.buffer, 0);
            this.count = 0;
        } else {
            ++this.count;
        }
    }

    public void engineUpdate(byte[] input, int offset, int len) {
        int j;
        if (offset < 0 || len < 0 || (long)offset + (long)len > (long)input.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int partLen = 16 - this.count;
        int i = 0;
        if (len >= partLen) {
            j = 0;
            while (j < partLen) {
                this.buffer[this.count + j] = input[offset + j] & 0xFF;
                ++j;
            }
            this.transform(this.buffer, 0);
            this.count = 0;
            i = partLen;
            while (i + 16 - 1 < len) {
                this.transform(input, offset + i);
                i += 16;
            }
        }
        if (i < len) {
            j = 0;
            while (j < len - i) {
                this.buffer[this.count + j] = input[offset + i + j] & 0xFF;
                ++j;
            }
            this.count += len - i;
        }
    }

    public byte[] engineDigest() {
        int padLen = 16 - this.count;
        int i = this.count;
        while (i < 16) {
            this.buffer[i] = (byte)padLen;
            ++i;
        }
        this.transform(this.buffer, 0);
        this.transform(this.checksum, 0);
        byte[] result = new byte[16];
        int i2 = 0;
        while (i2 < 16) {
            result[i2] = (byte)this.X[i2];
            ++i2;
        }
        this.engineReset();
        return result;
    }

    private void transform(int[] block, int offset) {
        int i = 0;
        while (i < 16) {
            this.X[16 + i] = block[offset + i] & 0xFF;
            this.X[32 + i] = this.X[i] ^ this.X[16 + i];
            ++i;
        }
        int t = 0;
        int i2 = 0;
        while (i2 < 18) {
            int j = 0;
            while (j < 48) {
                int n = j++;
                int n2 = this.X[n] ^ S[t];
                this.X[n] = n2;
                t = n2;
            }
            t = t + i2 & 0xFF;
            ++i2;
        }
        t = this.checksum[15];
        i2 = 0;
        while (i2 < 16) {
            int n = i2;
            int n3 = this.checksum[n] ^ S[block[offset + i2] & 0xFF ^ t];
            this.checksum[n] = n3;
            t = n3;
            ++i2;
        }
    }

    private void transform(byte[] block, int offset) {
        int i = 0;
        while (i < 16) {
            this.X[16 + i] = block[offset + i] & 0xFF;
            this.X[32 + i] = this.X[i] ^ this.X[16 + i];
            ++i;
        }
        int t = 0;
        int i2 = 0;
        while (i2 < 18) {
            int j = 0;
            while (j < 48) {
                int n = j++;
                int n2 = this.X[n] ^ S[t];
                this.X[n] = n2;
                t = n2;
            }
            t = t + i2 & 0xFF;
            ++i2;
        }
        t = this.checksum[15];
        i2 = 0;
        while (i2 < 16) {
            int n = i2;
            int n3 = this.checksum[n] ^ S[block[offset + i2] & 0xFF ^ t];
            this.checksum[n] = n3;
            t = n3;
            ++i2;
        }
    }
}

