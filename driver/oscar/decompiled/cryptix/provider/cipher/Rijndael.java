/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.cipher;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyException;
import xjava.security.Cipher;
import xjava.security.SymmetricCipher;

public final class Rijndael
extends Cipher
implements SymmetricCipher {
    private boolean ROUNDS_12;
    private boolean ROUNDS_14;
    private boolean decrypt;
    private int[] K;
    private int limit;
    private static final int BLOCK_SIZE = 16;
    private static final String SS = "\u637c\u777b\uf26b\u6fc5\u3001\u672b\ufed7\uab76\uca82\uc97d\ufa59\u47f0\uadd4\ua2af\u9ca4\u72c0\ub7fd\u9326\u363f\uf7cc\u34a5\ue5f1\u71d8\u3115\u04c7\u23c3\u1896\u059a\u0712\u80e2\ueb27\ub275\u0983\u2c1a\u1b6e\u5aa0\u523b\ud6b3\u29e3\u2f84\u53d1\u00ed\u20fc\ub15b\u6acb\ube39\u4a4c\u58cf\ud0ef\uaafb\u434d\u3385\u45f9\u027f\u503c\u9fa8\u51a3\u408f\u929d\u38f5\ubcb6\uda21\u10ff\uf3d2\ucd0c\u13ec\u5f97\u4417\uc4a7\u7e3d\u645d\u1973\u6081\u4fdc\u222a\u9088\u46ee\ub814\ude5e\u0bdb\ue032\u3a0a\u4906\u245c\uc2d3\uac62\u9195\ue479\ue7c8\u376d\u8dd5\u4ea9\u6c56\uf4ea\u657a\uae08\uba78\u252e\u1ca6\ub4c6\ue8dd\u741f\u4bbd\u8b8a\u703e\ub566\u4803\uf60e\u6135\u57b9\u86c1\u1d9e\ue1f8\u9811\u69d9\u8e94\u9b1e\u87e9\uce55\u28df\u8ca1\u890d\ubfe6\u4268\u4199\u2d0f\ub054\ubb16";
    private static final byte[] S = new byte[256];
    private static final byte[] Si = new byte[256];
    private static final int[] T1 = new int[256];
    private static final int[] T2 = new int[256];
    private static final int[] T3 = new int[256];
    private static final int[] T4 = new int[256];
    private static final int[] T5 = new int[256];
    private static final int[] T6 = new int[256];
    private static final int[] T7 = new int[256];
    private static final int[] T8 = new int[256];
    private static final int[] U1 = new int[256];
    private static final int[] U2 = new int[256];
    private static final int[] U3 = new int[256];
    private static final int[] U4 = new int[256];
    private static final byte[] rcon = new byte[30];

    static {
        int ROOT = 283;
        boolean j = false;
        int i = 0;
        while (i < 256) {
            int t;
            int i8;
            int i4;
            char c = SS.charAt(i >>> 1);
            Rijndael.S[i] = (byte)((i & 1) == 0 ? c >>> 8 : c & 0xFF);
            int s = S[i] & 0xFF;
            Rijndael.Si[s] = (byte)i;
            int s2 = s << 1;
            if (s2 >= 256) {
                s2 ^= ROOT;
            }
            int s3 = s2 ^ s;
            int i2 = i << 1;
            if (i2 >= 256) {
                i2 ^= ROOT;
            }
            if ((i4 = i2 << 1) >= 256) {
                i4 ^= ROOT;
            }
            if ((i8 = i4 << 1) >= 256) {
                i8 ^= ROOT;
            }
            int i9 = i8 ^ i;
            int ib = i9 ^ i2;
            int id = i9 ^ i4;
            int ie = i8 ^ i4 ^ i2;
            Rijndael.T1[i] = t = s2 << 24 | s << 16 | s << 8 | s3;
            Rijndael.T2[i] = t >>> 8 | t << 24;
            Rijndael.T3[i] = t >>> 16 | t << 16;
            Rijndael.T4[i] = t >>> 24 | t << 8;
            Rijndael.U1[i] = t = ie << 24 | i9 << 16 | id << 8 | ib;
            Rijndael.T5[s] = t;
            Rijndael.T6[s] = Rijndael.U2[i] = t >>> 8 | t << 24;
            Rijndael.T7[s] = Rijndael.U3[i] = t >>> 16 | t << 16;
            Rijndael.T8[s] = Rijndael.U4[i] = t >>> 24 | t << 8;
            ++i;
        }
        int r = 1;
        Rijndael.rcon[0] = 1;
        i = 1;
        while (i < 30) {
            if ((r <<= 1) >= 256) {
                r ^= ROOT;
            }
            Rijndael.rcon[i] = (byte)r;
            ++i;
        }
    }

    public Rijndael() {
        super(false, false, "Cryptix");
    }

    protected void coreInit(Key key, boolean decrypt) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key missing");
        }
        if (!key.getFormat().equalsIgnoreCase("RAW")) {
            throw new InvalidKeyException("Wrong format: RAW bytes needed");
        }
        byte[] userkey = key.getEncoded();
        if (userkey == null) {
            throw new InvalidKeyException("RAW bytes missing");
        }
        int len = userkey.length;
        if (len != 16 && len != 24 && len != 32) {
            throw new InvalidKeyException("Invalid user key length");
        }
        this.decrypt = decrypt;
        this.K = Rijndael.makeKey(userkey, decrypt);
        if (decrypt) {
            Rijndael.invertKey(this.K);
        }
        this.ROUNDS_12 = len >= 24;
        this.ROUNDS_14 = len == 32;
        this.limit = Rijndael.getRounds(len) * 4;
    }

    protected void engineInitEncrypt(Key key) throws KeyException {
        this.coreInit(key, false);
    }

    protected void engineInitDecrypt(Key key) throws KeyException {
        this.coreInit(key, true);
    }

    /*
     * Unable to fully structure code
     */
    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        block2: {
            size = inLen;
            if (!this.decrypt) ** GOTO lbl14
            while (inLen > 0) {
                this.blockDecrypt(in, inOffset, out, outOffset);
                inOffset += 16;
                outOffset += 16;
                inLen -= 16;
            }
            break block2;
lbl-1000:
            // 1 sources

            {
                this.blockEncrypt(in, inOffset, out, outOffset);
                inOffset += 16;
                outOffset += 16;
                inLen -= 16;
lbl14:
                // 2 sources

                ** while (inLen > 0)
            }
        }
        return size;
    }

    protected int engineBlockSize() {
        return 16;
    }

    private void blockEncrypt(byte[] in, int inOffset, byte[] out, int outOffset) {
        int keyOffset = 0;
        int t0 = (in[inOffset++] << 24 | (in[inOffset++] & 0xFF) << 16 | (in[inOffset++] & 0xFF) << 8 | in[inOffset++] & 0xFF) ^ this.K[keyOffset++];
        int t1 = (in[inOffset++] << 24 | (in[inOffset++] & 0xFF) << 16 | (in[inOffset++] & 0xFF) << 8 | in[inOffset++] & 0xFF) ^ this.K[keyOffset++];
        int t2 = (in[inOffset++] << 24 | (in[inOffset++] & 0xFF) << 16 | (in[inOffset++] & 0xFF) << 8 | in[inOffset++] & 0xFF) ^ this.K[keyOffset++];
        int t3 = (in[inOffset++] << 24 | (in[inOffset++] & 0xFF) << 16 | (in[inOffset++] & 0xFF) << 8 | in[inOffset++] & 0xFF) ^ this.K[keyOffset++];
        while (keyOffset < this.limit) {
            int a0 = T1[t0 >>> 24] ^ T2[t1 >>> 16 & 0xFF] ^ T3[t2 >>> 8 & 0xFF] ^ T4[t3 & 0xFF] ^ this.K[keyOffset++];
            int a1 = T1[t1 >>> 24] ^ T2[t2 >>> 16 & 0xFF] ^ T3[t3 >>> 8 & 0xFF] ^ T4[t0 & 0xFF] ^ this.K[keyOffset++];
            int a2 = T1[t2 >>> 24] ^ T2[t3 >>> 16 & 0xFF] ^ T3[t0 >>> 8 & 0xFF] ^ T4[t1 & 0xFF] ^ this.K[keyOffset++];
            t3 = T1[t3 >>> 24] ^ T2[t0 >>> 16 & 0xFF] ^ T3[t1 >>> 8 & 0xFF] ^ T4[t2 & 0xFF] ^ this.K[keyOffset++];
            t0 = a0;
            t1 = a1;
            t2 = a2;
        }
        int tt = this.K[keyOffset++];
        out[outOffset++] = (byte)(S[t0 >>> 24] ^ tt >>> 24);
        out[outOffset++] = (byte)(S[t1 >>> 16 & 0xFF] ^ tt >>> 16);
        out[outOffset++] = (byte)(S[t2 >>> 8 & 0xFF] ^ tt >>> 8);
        out[outOffset++] = (byte)(S[t3 & 0xFF] ^ tt);
        tt = this.K[keyOffset++];
        out[outOffset++] = (byte)(S[t1 >>> 24] ^ tt >>> 24);
        out[outOffset++] = (byte)(S[t2 >>> 16 & 0xFF] ^ tt >>> 16);
        out[outOffset++] = (byte)(S[t3 >>> 8 & 0xFF] ^ tt >>> 8);
        out[outOffset++] = (byte)(S[t0 & 0xFF] ^ tt);
        tt = this.K[keyOffset++];
        out[outOffset++] = (byte)(S[t2 >>> 24] ^ tt >>> 24);
        out[outOffset++] = (byte)(S[t3 >>> 16 & 0xFF] ^ tt >>> 16);
        out[outOffset++] = (byte)(S[t0 >>> 8 & 0xFF] ^ tt >>> 8);
        out[outOffset++] = (byte)(S[t1 & 0xFF] ^ tt);
        tt = this.K[keyOffset++];
        out[outOffset++] = (byte)(S[t3 >>> 24] ^ tt >>> 24);
        out[outOffset++] = (byte)(S[t0 >>> 16 & 0xFF] ^ tt >>> 16);
        out[outOffset++] = (byte)(S[t1 >>> 8 & 0xFF] ^ tt >>> 8);
        out[outOffset] = (byte)(S[t2 & 0xFF] ^ tt);
    }

    private void blockDecrypt(byte[] in, int inOffset, byte[] out, int outOffset) {
        int a2;
        int a1;
        int a0;
        int keyOffset = 8;
        int t0 = (in[inOffset++] << 24 | (in[inOffset++] & 0xFF) << 16 | (in[inOffset++] & 0xFF) << 8 | in[inOffset++] & 0xFF) ^ this.K[4];
        int t1 = (in[inOffset++] << 24 | (in[inOffset++] & 0xFF) << 16 | (in[inOffset++] & 0xFF) << 8 | in[inOffset++] & 0xFF) ^ this.K[5];
        int t2 = (in[inOffset++] << 24 | (in[inOffset++] & 0xFF) << 16 | (in[inOffset++] & 0xFF) << 8 | in[inOffset++] & 0xFF) ^ this.K[6];
        int t3 = (in[inOffset++] << 24 | (in[inOffset++] & 0xFF) << 16 | (in[inOffset++] & 0xFF) << 8 | in[inOffset] & 0xFF) ^ this.K[7];
        if (this.ROUNDS_12) {
            a0 = T5[t0 >>> 24] ^ T6[t3 >>> 16 & 0xFF] ^ T7[t2 >>> 8 & 0xFF] ^ T8[t1 & 0xFF] ^ this.K[keyOffset++];
            a1 = T5[t1 >>> 24] ^ T6[t0 >>> 16 & 0xFF] ^ T7[t3 >>> 8 & 0xFF] ^ T8[t2 & 0xFF] ^ this.K[keyOffset++];
            a2 = T5[t2 >>> 24] ^ T6[t1 >>> 16 & 0xFF] ^ T7[t0 >>> 8 & 0xFF] ^ T8[t3 & 0xFF] ^ this.K[keyOffset++];
            t3 = T5[t3 >>> 24] ^ T6[t2 >>> 16 & 0xFF] ^ T7[t1 >>> 8 & 0xFF] ^ T8[t0 & 0xFF] ^ this.K[keyOffset++];
            t0 = T5[a0 >>> 24] ^ T6[t3 >>> 16 & 0xFF] ^ T7[a2 >>> 8 & 0xFF] ^ T8[a1 & 0xFF] ^ this.K[keyOffset++];
            t1 = T5[a1 >>> 24] ^ T6[a0 >>> 16 & 0xFF] ^ T7[t3 >>> 8 & 0xFF] ^ T8[a2 & 0xFF] ^ this.K[keyOffset++];
            t2 = T5[a2 >>> 24] ^ T6[a1 >>> 16 & 0xFF] ^ T7[a0 >>> 8 & 0xFF] ^ T8[t3 & 0xFF] ^ this.K[keyOffset++];
            t3 = T5[t3 >>> 24] ^ T6[a2 >>> 16 & 0xFF] ^ T7[a1 >>> 8 & 0xFF] ^ T8[a0 & 0xFF] ^ this.K[keyOffset++];
            if (this.ROUNDS_14) {
                a0 = T5[t0 >>> 24] ^ T6[t3 >>> 16 & 0xFF] ^ T7[t2 >>> 8 & 0xFF] ^ T8[t1 & 0xFF] ^ this.K[keyOffset++];
                a1 = T5[t1 >>> 24] ^ T6[t0 >>> 16 & 0xFF] ^ T7[t3 >>> 8 & 0xFF] ^ T8[t2 & 0xFF] ^ this.K[keyOffset++];
                a2 = T5[t2 >>> 24] ^ T6[t1 >>> 16 & 0xFF] ^ T7[t0 >>> 8 & 0xFF] ^ T8[t3 & 0xFF] ^ this.K[keyOffset++];
                t3 = T5[t3 >>> 24] ^ T6[t2 >>> 16 & 0xFF] ^ T7[t1 >>> 8 & 0xFF] ^ T8[t0 & 0xFF] ^ this.K[keyOffset++];
                t0 = T5[a0 >>> 24] ^ T6[t3 >>> 16 & 0xFF] ^ T7[a2 >>> 8 & 0xFF] ^ T8[a1 & 0xFF] ^ this.K[keyOffset++];
                t1 = T5[a1 >>> 24] ^ T6[a0 >>> 16 & 0xFF] ^ T7[t3 >>> 8 & 0xFF] ^ T8[a2 & 0xFF] ^ this.K[keyOffset++];
                t2 = T5[a2 >>> 24] ^ T6[a1 >>> 16 & 0xFF] ^ T7[a0 >>> 8 & 0xFF] ^ T8[t3 & 0xFF] ^ this.K[keyOffset++];
                t3 = T5[t3 >>> 24] ^ T6[a2 >>> 16 & 0xFF] ^ T7[a1 >>> 8 & 0xFF] ^ T8[a0 & 0xFF] ^ this.K[keyOffset++];
            }
        }
        a0 = T5[t0 >>> 24] ^ T6[t3 >>> 16 & 0xFF] ^ T7[t2 >>> 8 & 0xFF] ^ T8[t1 & 0xFF] ^ this.K[keyOffset++];
        a1 = T5[t1 >>> 24] ^ T6[t0 >>> 16 & 0xFF] ^ T7[t3 >>> 8 & 0xFF] ^ T8[t2 & 0xFF] ^ this.K[keyOffset++];
        a2 = T5[t2 >>> 24] ^ T6[t1 >>> 16 & 0xFF] ^ T7[t0 >>> 8 & 0xFF] ^ T8[t3 & 0xFF] ^ this.K[keyOffset++];
        t3 = T5[t3 >>> 24] ^ T6[t2 >>> 16 & 0xFF] ^ T7[t1 >>> 8 & 0xFF] ^ T8[t0 & 0xFF] ^ this.K[keyOffset++];
        t0 = T5[a0 >>> 24] ^ T6[t3 >>> 16 & 0xFF] ^ T7[a2 >>> 8 & 0xFF] ^ T8[a1 & 0xFF] ^ this.K[keyOffset++];
        t1 = T5[a1 >>> 24] ^ T6[a0 >>> 16 & 0xFF] ^ T7[t3 >>> 8 & 0xFF] ^ T8[a2 & 0xFF] ^ this.K[keyOffset++];
        t2 = T5[a2 >>> 24] ^ T6[a1 >>> 16 & 0xFF] ^ T7[a0 >>> 8 & 0xFF] ^ T8[t3 & 0xFF] ^ this.K[keyOffset++];
        t3 = T5[t3 >>> 24] ^ T6[a2 >>> 16 & 0xFF] ^ T7[a1 >>> 8 & 0xFF] ^ T8[a0 & 0xFF] ^ this.K[keyOffset++];
        a0 = T5[t0 >>> 24] ^ T6[t3 >>> 16 & 0xFF] ^ T7[t2 >>> 8 & 0xFF] ^ T8[t1 & 0xFF] ^ this.K[keyOffset++];
        a1 = T5[t1 >>> 24] ^ T6[t0 >>> 16 & 0xFF] ^ T7[t3 >>> 8 & 0xFF] ^ T8[t2 & 0xFF] ^ this.K[keyOffset++];
        a2 = T5[t2 >>> 24] ^ T6[t1 >>> 16 & 0xFF] ^ T7[t0 >>> 8 & 0xFF] ^ T8[t3 & 0xFF] ^ this.K[keyOffset++];
        t3 = T5[t3 >>> 24] ^ T6[t2 >>> 16 & 0xFF] ^ T7[t1 >>> 8 & 0xFF] ^ T8[t0 & 0xFF] ^ this.K[keyOffset++];
        t0 = T5[a0 >>> 24] ^ T6[t3 >>> 16 & 0xFF] ^ T7[a2 >>> 8 & 0xFF] ^ T8[a1 & 0xFF] ^ this.K[keyOffset++];
        t1 = T5[a1 >>> 24] ^ T6[a0 >>> 16 & 0xFF] ^ T7[t3 >>> 8 & 0xFF] ^ T8[a2 & 0xFF] ^ this.K[keyOffset++];
        t2 = T5[a2 >>> 24] ^ T6[a1 >>> 16 & 0xFF] ^ T7[a0 >>> 8 & 0xFF] ^ T8[t3 & 0xFF] ^ this.K[keyOffset++];
        t3 = T5[t3 >>> 24] ^ T6[a2 >>> 16 & 0xFF] ^ T7[a1 >>> 8 & 0xFF] ^ T8[a0 & 0xFF] ^ this.K[keyOffset++];
        a0 = T5[t0 >>> 24] ^ T6[t3 >>> 16 & 0xFF] ^ T7[t2 >>> 8 & 0xFF] ^ T8[t1 & 0xFF] ^ this.K[keyOffset++];
        a1 = T5[t1 >>> 24] ^ T6[t0 >>> 16 & 0xFF] ^ T7[t3 >>> 8 & 0xFF] ^ T8[t2 & 0xFF] ^ this.K[keyOffset++];
        a2 = T5[t2 >>> 24] ^ T6[t1 >>> 16 & 0xFF] ^ T7[t0 >>> 8 & 0xFF] ^ T8[t3 & 0xFF] ^ this.K[keyOffset++];
        t3 = T5[t3 >>> 24] ^ T6[t2 >>> 16 & 0xFF] ^ T7[t1 >>> 8 & 0xFF] ^ T8[t0 & 0xFF] ^ this.K[keyOffset++];
        t0 = T5[a0 >>> 24] ^ T6[t3 >>> 16 & 0xFF] ^ T7[a2 >>> 8 & 0xFF] ^ T8[a1 & 0xFF] ^ this.K[keyOffset++];
        t1 = T5[a1 >>> 24] ^ T6[a0 >>> 16 & 0xFF] ^ T7[t3 >>> 8 & 0xFF] ^ T8[a2 & 0xFF] ^ this.K[keyOffset++];
        t2 = T5[a2 >>> 24] ^ T6[a1 >>> 16 & 0xFF] ^ T7[a0 >>> 8 & 0xFF] ^ T8[t3 & 0xFF] ^ this.K[keyOffset++];
        t3 = T5[t3 >>> 24] ^ T6[a2 >>> 16 & 0xFF] ^ T7[a1 >>> 8 & 0xFF] ^ T8[a0 & 0xFF] ^ this.K[keyOffset++];
        a0 = T5[t0 >>> 24] ^ T6[t3 >>> 16 & 0xFF] ^ T7[t2 >>> 8 & 0xFF] ^ T8[t1 & 0xFF] ^ this.K[keyOffset++];
        a1 = T5[t1 >>> 24] ^ T6[t0 >>> 16 & 0xFF] ^ T7[t3 >>> 8 & 0xFF] ^ T8[t2 & 0xFF] ^ this.K[keyOffset++];
        a2 = T5[t2 >>> 24] ^ T6[t1 >>> 16 & 0xFF] ^ T7[t0 >>> 8 & 0xFF] ^ T8[t3 & 0xFF] ^ this.K[keyOffset++];
        t3 = T5[t3 >>> 24] ^ T6[t2 >>> 16 & 0xFF] ^ T7[t1 >>> 8 & 0xFF] ^ T8[t0 & 0xFF] ^ this.K[keyOffset++];
        t0 = T5[a0 >>> 24] ^ T6[t3 >>> 16 & 0xFF] ^ T7[a2 >>> 8 & 0xFF] ^ T8[a1 & 0xFF] ^ this.K[keyOffset++];
        t1 = T5[a1 >>> 24] ^ T6[a0 >>> 16 & 0xFF] ^ T7[t3 >>> 8 & 0xFF] ^ T8[a2 & 0xFF] ^ this.K[keyOffset++];
        t2 = T5[a2 >>> 24] ^ T6[a1 >>> 16 & 0xFF] ^ T7[a0 >>> 8 & 0xFF] ^ T8[t3 & 0xFF] ^ this.K[keyOffset++];
        t3 = T5[t3 >>> 24] ^ T6[a2 >>> 16 & 0xFF] ^ T7[a1 >>> 8 & 0xFF] ^ T8[a0 & 0xFF] ^ this.K[keyOffset++];
        a0 = T5[t0 >>> 24] ^ T6[t3 >>> 16 & 0xFF] ^ T7[t2 >>> 8 & 0xFF] ^ T8[t1 & 0xFF] ^ this.K[keyOffset++];
        a1 = T5[t1 >>> 24] ^ T6[t0 >>> 16 & 0xFF] ^ T7[t3 >>> 8 & 0xFF] ^ T8[t2 & 0xFF] ^ this.K[keyOffset++];
        a2 = T5[t2 >>> 24] ^ T6[t1 >>> 16 & 0xFF] ^ T7[t0 >>> 8 & 0xFF] ^ T8[t3 & 0xFF] ^ this.K[keyOffset++];
        t3 = T5[t3 >>> 24] ^ T6[t2 >>> 16 & 0xFF] ^ T7[t1 >>> 8 & 0xFF] ^ T8[t0 & 0xFF] ^ this.K[keyOffset++];
        t1 = this.K[0];
        out[outOffset++] = (byte)(Si[a0 >>> 24] ^ t1 >>> 24);
        out[outOffset++] = (byte)(Si[t3 >>> 16 & 0xFF] ^ t1 >>> 16);
        out[outOffset++] = (byte)(Si[a2 >>> 8 & 0xFF] ^ t1 >>> 8);
        out[outOffset++] = (byte)(Si[a1 & 0xFF] ^ t1);
        t1 = this.K[1];
        out[outOffset++] = (byte)(Si[a1 >>> 24] ^ t1 >>> 24);
        out[outOffset++] = (byte)(Si[a0 >>> 16 & 0xFF] ^ t1 >>> 16);
        out[outOffset++] = (byte)(Si[t3 >>> 8 & 0xFF] ^ t1 >>> 8);
        out[outOffset++] = (byte)(Si[a2 & 0xFF] ^ t1);
        t1 = this.K[2];
        out[outOffset++] = (byte)(Si[a2 >>> 24] ^ t1 >>> 24);
        out[outOffset++] = (byte)(Si[a1 >>> 16 & 0xFF] ^ t1 >>> 16);
        out[outOffset++] = (byte)(Si[a0 >>> 8 & 0xFF] ^ t1 >>> 8);
        out[outOffset++] = (byte)(Si[t3 & 0xFF] ^ t1);
        t1 = this.K[3];
        out[outOffset++] = (byte)(Si[t3 >>> 24] ^ t1 >>> 24);
        out[outOffset++] = (byte)(Si[a2 >>> 16 & 0xFF] ^ t1 >>> 16);
        out[outOffset++] = (byte)(Si[a1 >>> 8 & 0xFF] ^ t1 >>> 8);
        out[outOffset] = (byte)(Si[a0 & 0xFF] ^ t1);
    }

    private static int[] makeKey(byte[] keyBytes, boolean decrypt) throws InvalidKeyException {
        int ROUNDS = Rijndael.getRounds(keyBytes.length);
        int ROUND_KEY_COUNT = (ROUNDS + 1) * 4;
        int[] K = new int[ROUND_KEY_COUNT];
        int KC = keyBytes.length / 4;
        int[] tk = new int[KC];
        int i = 0;
        int j = 0;
        while (i < KC) {
            tk[i++] = keyBytes[j++] << 24 | (keyBytes[j++] & 0xFF) << 16 | (keyBytes[j++] & 0xFF) << 8 | keyBytes[j++] & 0xFF;
        }
        int t = 0;
        while (t < KC) {
            K[t] = tk[t];
            ++t;
        }
        int rconpointer = 0;
        while (t < ROUND_KEY_COUNT) {
            int tt = tk[KC - 1];
            tk[0] = tk[0] ^ (S[tt >>> 16 & 0xFF] << 24 ^ (S[tt >>> 8 & 0xFF] & 0xFF) << 16 ^ (S[tt & 0xFF] & 0xFF) << 8 ^ S[tt >>> 24] & 0xFF ^ rcon[rconpointer++] << 24);
            if (KC != 8) {
                i = 1;
                j = 0;
                while (i < KC) {
                    int n = i++;
                    tk[n] = tk[n] ^ tk[j++];
                }
            } else {
                i = 1;
                j = 0;
                while (i < KC / 2) {
                    int n = i++;
                    tk[n] = tk[n] ^ tk[j++];
                }
                tt = tk[KC / 2 - 1];
                int n = KC / 2;
                tk[n] = tk[n] ^ (S[tt & 0xFF] & 0xFF ^ (S[tt >>> 8 & 0xFF] & 0xFF) << 8 ^ (S[tt >>> 16 & 0xFF] & 0xFF) << 16 ^ S[tt >>> 24] << 24);
                j = KC / 2;
                i = j + 1;
                while (i < KC) {
                    int n2 = i++;
                    tk[n2] = tk[n2] ^ tk[j++];
                }
            }
            j = 0;
            while (j < KC && t < ROUND_KEY_COUNT) {
                K[t] = tk[j];
                ++j;
                ++t;
            }
        }
        return K;
    }

    private static void invertKey(int[] K) {
        int i = 0;
        while (i < K.length / 2 - 4) {
            int jj0 = K[i + 0];
            int jj1 = K[i + 1];
            int jj2 = K[i + 2];
            int jj3 = K[i + 3];
            K[i + 0] = K[K.length - i - 4 + 0];
            K[i + 1] = K[K.length - i - 4 + 1];
            K[i + 2] = K[K.length - i - 4 + 2];
            K[i + 3] = K[K.length - i - 4 + 3];
            K[K.length - i - 4 + 0] = jj0;
            K[K.length - i - 4 + 1] = jj1;
            K[K.length - i - 4 + 2] = jj2;
            K[K.length - i - 4 + 3] = jj3;
            i += 4;
        }
        int r = 4;
        while (r < K.length - 4) {
            int tt = K[r];
            K[r] = U1[tt >>> 24 & 0xFF] ^ U2[tt >>> 16 & 0xFF] ^ U3[tt >>> 8 & 0xFF] ^ U4[tt & 0xFF];
            ++r;
        }
        int j0 = K[K.length - 4];
        int j1 = K[K.length - 3];
        int j2 = K[K.length - 2];
        int j3 = K[K.length - 1];
        int i2 = K.length - 1;
        while (i2 > 3) {
            K[i2] = K[i2 - 4];
            --i2;
        }
        K[0] = j0;
        K[1] = j1;
        K[2] = j2;
        K[3] = j3;
    }

    private static int getRounds(int keySize) {
        return (keySize >> 2) + 6;
    }
}

