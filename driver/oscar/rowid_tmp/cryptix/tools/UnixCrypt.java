/*
 * Decompiled with CFR 0.152.
 */
package cryptix.tools;

import cryptix.provider.cipher.DES;
import cryptix.provider.key.DESKeyGenerator;
import java.security.KeyException;

public class UnixCrypt {
    private char salt0;
    private char salt1;
    private static final byte[] CON_SALT;
    private static final char[] COV_2CHAR;
    private DES des;
    private DESKeyGenerator dkg;

    static {
        byte[] byArray = new byte[128];
        byArray[47] = 1;
        byArray[48] = 2;
        byArray[49] = 3;
        byArray[50] = 4;
        byArray[51] = 5;
        byArray[52] = 6;
        byArray[53] = 7;
        byArray[54] = 8;
        byArray[55] = 9;
        byArray[56] = 10;
        byArray[57] = 11;
        byArray[58] = 5;
        byArray[59] = 6;
        byArray[60] = 7;
        byArray[61] = 8;
        byArray[62] = 9;
        byArray[63] = 10;
        byArray[64] = 11;
        byArray[65] = 12;
        byArray[66] = 13;
        byArray[67] = 14;
        byArray[68] = 15;
        byArray[69] = 16;
        byArray[70] = 17;
        byArray[71] = 18;
        byArray[72] = 19;
        byArray[73] = 20;
        byArray[74] = 21;
        byArray[75] = 22;
        byArray[76] = 23;
        byArray[77] = 24;
        byArray[78] = 25;
        byArray[79] = 26;
        byArray[80] = 27;
        byArray[81] = 28;
        byArray[82] = 29;
        byArray[83] = 30;
        byArray[84] = 31;
        byArray[85] = 32;
        byArray[86] = 33;
        byArray[87] = 34;
        byArray[88] = 35;
        byArray[89] = 36;
        byArray[90] = 37;
        byArray[91] = 32;
        byArray[92] = 33;
        byArray[93] = 34;
        byArray[94] = 35;
        byArray[95] = 36;
        byArray[96] = 37;
        byArray[97] = 38;
        byArray[98] = 39;
        byArray[99] = 40;
        byArray[100] = 41;
        byArray[101] = 42;
        byArray[102] = 43;
        byArray[103] = 44;
        byArray[104] = 45;
        byArray[105] = 46;
        byArray[106] = 47;
        byArray[107] = 48;
        byArray[108] = 49;
        byArray[109] = 50;
        byArray[110] = 51;
        byArray[111] = 52;
        byArray[112] = 53;
        byArray[113] = 54;
        byArray[114] = 55;
        byArray[115] = 56;
        byArray[116] = 57;
        byArray[117] = 58;
        byArray[118] = 59;
        byArray[119] = 60;
        byArray[120] = 61;
        byArray[121] = 62;
        byArray[122] = 63;
        CON_SALT = byArray;
        COV_2CHAR = new char[]{'.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    }

    public UnixCrypt(String salt) {
        if (salt == null) {
            salt = "";
        }
        salt = String.valueOf(salt) + "AA";
        this.salt0 = (char)(salt.charAt(0) & 0x7F);
        this.salt1 = (char)(salt.charAt(1) & 0x7F);
        this.des = new DES();
        this.dkg = new DESKeyGenerator();
    }

    public synchronized String crypt(String original) {
        byte[] key = new byte[8];
        int i = 0;
        while (i < key.length && i < original.length()) {
            key[i] = (byte)(original.charAt(i) << 1);
            ++i;
        }
        this.dkg.setWeakAllowed(true);
        try {
            this.des.initEncrypt(this.dkg.generateKey(key));
        }
        catch (KeyException e) {
            throw new InternalError(e.toString());
        }
        int[] out = this.des.crypt3(CON_SALT[this.salt0] & 0xFF, (CON_SALT[this.salt1] & 0xFF) << 4);
        i = out[0];
        int j = out[1];
        byte[] byArray = new byte[9];
        byArray[0] = (byte)i;
        byArray[1] = (byte)(i >>> 8);
        byArray[2] = (byte)(i >>> 16);
        byArray[3] = (byte)(i >>> 24);
        byArray[4] = (byte)j;
        byArray[5] = (byte)(j >>> 8);
        byArray[6] = (byte)(j >>> 16);
        byArray[7] = (byte)(j >>> 24);
        byte[] b = byArray;
        int y = 0;
        int u = 128;
        char[] buffer = new char[13];
        i = 0;
        buffer[i++] = this.salt0;
        buffer[i++] = this.salt1;
        while (i < buffer.length) {
            j = 0;
            int c = 0;
            while (j < 6) {
                c <<= 1;
                if ((b[y] & u) != 0) {
                    c |= 1;
                }
                if ((u >>>= 1) == 0) {
                    ++y;
                    u = 128;
                }
                ++j;
            }
            buffer[i++] = COV_2CHAR[c];
        }
        return new String(buffer);
    }

    public static void main(String[] args) {
        String original;
        String salt = null;
        switch (args.length) {
            case 2: {
                salt = args[0];
                original = args[1];
                break;
            }
            case 1: {
                salt = "";
                original = args[0];
                break;
            }
            default: {
                System.out.println("Usage:\n    java cryptix.tools.UnixCrypt [<salt>] <clear-password>");
                return;
            }
        }
        try {
            UnixCrypt jc = new UnixCrypt(salt);
            System.out.print("[" + (String.valueOf(salt) + "AA").substring(0, 2) + "] " + "[" + original + "] => ");
            System.out.println("[" + jc.crypt(original) + "]");
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

