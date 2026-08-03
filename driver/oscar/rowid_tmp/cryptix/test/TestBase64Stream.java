/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.util.core.ArrayUtil;
import cryptix.util.mime.Base64InputStream;
import cryptix.util.mime.Base64OutputStream;
import cryptix.util.test.BaseTest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

public class TestBase64Stream
extends BaseTest {
    public static void main(String[] args) {
        new TestBase64Stream().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(1);
        Random r = new Random();
        int inputlen = 1000 + r.nextInt() % 100;
        byte[] input = new byte[inputlen];
        r.nextBytes(input);
        int i = 0;
        this.out.println("Asciifying...");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Base64OutputStream b64out = new Base64OutputStream(baos, true);
        int len = 0;
        i = 0;
        while (i < inputlen) {
            len = r.nextInt() & 0xF;
            if (len > inputlen - i) {
                len = inputlen - i;
            }
            b64out.write(input, i, len);
            i += len;
        }
        b64out.close();
        byte[] output = baos.toByteArray();
        this.out.println("De-asciifying...");
        ByteArrayInputStream bais = new ByteArrayInputStream(output);
        Base64InputStream b64in = new Base64InputStream(bais, true);
        byte[] input2 = new byte[input.length];
        len = 0;
        i = 0;
        while (len != -1) {
            len = b64in.read(input2, i += len, r.nextInt() & 0xF);
        }
        b64in.close();
        this.passIf(ArrayUtil.areEqual(input, input2), "Compare decoded to original");
    }
}

