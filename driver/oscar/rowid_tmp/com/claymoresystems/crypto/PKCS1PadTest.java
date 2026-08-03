/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.crypto;

import com.claymoresystems.crypto.PKCS1Pad;
import java.math.BigInteger;

class PKCS1PadTest {
    PKCS1PadTest() {
    }

    static void test(BigInteger n, byte[] input, boolean expected, String name) {
        byte[] result = null;
        String error = "OK";
        try {
            result = PKCS1Pad.pkcs1UnpadBuf(input, 2, n);
        }
        catch (Exception e) {
            if (expected) {
                throw new InternalError("Should have succeeded");
            }
            error = e.toString();
        }
        if (result != null && !expected) {
            throw new InternalError("Should have failed");
        }
        System.out.println("Test " + name + " " + error + "(PASS)");
    }

    public static void main(String[] args) {
        byte[] nb = new byte[128];
        for (int i = 0; i < 128; ++i) {
            nb[i] = 0;
        }
        nb[0] = -128;
        BigInteger n = new BigInteger(1, nb);
        byte[] tmp = new byte[127];
        tmp[0] = 1;
        for (int i = 1; i < 127; ++i) {
            tmp[i] = -1;
        }
        PKCS1PadTest.test(n, tmp, false, "No value");
        tmp[65] = 0;
        PKCS1PadTest.test(n, tmp, true, "Good");
        tmp[64] = 55;
        PKCS1PadTest.test(n, tmp, false, "Bad pad (not ff)");
        tmp[7] = 0;
        PKCS1PadTest.test(n, tmp, false, "Short pad");
        tmp[0] = 2;
        PKCS1PadTest.test(n, tmp, false, "Wrong block type pad");
        byte[] tmp2 = new byte[126];
        PKCS1PadTest.test(n, tmp2, false, "Short block");
    }
}

