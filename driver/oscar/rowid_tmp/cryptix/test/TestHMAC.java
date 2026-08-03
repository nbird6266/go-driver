/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.util.core.ArrayUtil;
import cryptix.util.core.Hex;
import cryptix.util.test.BaseTest;
import java.security.MessageDigest;
import xjava.security.Parameterized;

public class TestHMAC
extends BaseTest {
    private static final String[][] tests = new String[][]{{"'Hi There", "0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b", "9294727a3638bb1c13f48ef8158bfc9d", "0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b", "b617318655057264e28bc0b6fb378c8ef146be00"}, {"'what do ya want for nothing?", "4a656665", "750c783e6ab0b503eaa86e310a5db738", "4a656665", "effcdf6ae5eb2fa2d27416d5f184df9c259a7c79"}, {"DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "56be34521d144c88dbb8c733f0e8b3f6", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "125d7342b9ac11cd91a39af48aa17b4f63f175d3"}, {"CDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCD", "0102030405060708090a0b0c0d0e0f10111213141516171819", "697eaf0aca3a3aea3a75164746ffaa79", "0102030405060708090a0b0c0d0e0f10111213141516171819", "4c9007f4026250c6bc8414f9bf50c86c2d7235da"}, {"'Test With Truncation", "0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c", "56461ef2342edc00f9bab995690efd4c", "0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c0c", "4c1a03424b55e07fe7f27be1d58bb9324a9a5a04"}, {"'Test Using Larger Than Block-Size Key - Hash Key First", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "6b1ab7fe4bd7bf8f0b62e6ce61b9d0cd", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "aa4ae5e15272d00e95705637ce8a3b55ed402112"}, {"'Test Using Larger Than Block-Size Key and Larger Than One Block-Size Data", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "6f630fad67cda0ee1fb1f562db3aa53e", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "e8e99d0f45237d786d6bbaa7965c7808bbff1a91"}};

    public static void main(String[] args) {
        new TestHMAC().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(14);
        MessageDigest hmac_md5 = MessageDigest.getInstance("HMAC-MD5", "Cryptix");
        MessageDigest hmac_sha1 = MessageDigest.getInstance("HMAC-SHA-1", "Cryptix");
        int i = 0;
        while (i < tests.length) {
            byte[] text;
            String ts = tests[i][0];
            if (ts.startsWith("'")) {
                text = new byte[ts.length() - 1];
                int j = 0;
                while (j < text.length) {
                    text[j] = (byte)ts.charAt(j + 1);
                    ++j;
                }
            } else {
                text = Hex.fromString(ts);
            }
            byte[] md5key = Hex.fromString(tests[i][1]);
            byte[] md5mac = Hex.fromString(tests[i][2]);
            byte[] sha1key = Hex.fromString(tests[i][3]);
            byte[] sha1mac = Hex.fromString(tests[i][4]);
            ((Parameterized)((Object)hmac_md5)).setParameter("key", md5key);
            byte[] tmp = hmac_md5.digest(text);
            this.passIf(ArrayUtil.areEqual(md5mac, tmp), "HMAC-MD5 #" + (i + 1));
            ((Parameterized)((Object)hmac_sha1)).setParameter("key", sha1key);
            tmp = hmac_sha1.digest(text);
            this.passIf(ArrayUtil.areEqual(sha1mac, tmp), "HMAC-SHA-1 #" + (i + 1));
            ++i;
        }
    }
}

