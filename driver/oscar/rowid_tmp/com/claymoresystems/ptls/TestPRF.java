/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.LoadProviders;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.TLSPRF;
import com.claymoresystems.util.Util;

class TestPRF {
    static byte[] PMS = new byte[]{3, 1, -3, 7, 33, -43, 68, -8, -47, 96, -118, -66, -117, 126, -85, 127, -67, -22, -58, 56, 63, -43, -122, 45, 3, -128, 74, -127, -22, -108, 42, 47, 85, 15, 5, -78, 96, -16, -21, -78, 76, 2, -64, 68, 57, 68, -118, -121};
    static byte[] CR = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 50, -8, 17, 60, 12, 49, 20, 127, -27, -25, 3, 41, -39, 17, 85, 52};
    static byte[] SR = new byte[]{61, 68, 17, -31, 1, -34, -80, 6, -51, 10, 107, 124, -3, 28, -70, 54, -96, 68, 46, -32, 64, -119, -107, 120, 103, 65, 110, 9, 62, 124, 22, 47};

    TestPRF() {
    }

    public static void main(String[] args) {
        SSLDebug.setDebug(8);
        LoadProviders.init();
        TLSPRF prf = new TLSPRF();
        byte[] out = new byte[48];
        prf.PRF(PMS, 1, CR, SR, out);
        Util.xdump("MS", out);
    }
}

