/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.util.math.BigRegister;
import cryptix.util.math.TrinomialLFSR;
import cryptix.util.test.BaseTest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Test3LFSR
extends BaseTest {
    public static void main(String[] args) {
        new Test3LFSR().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(3);
        TrinomialLFSR xx = new TrinomialLFSR(4, 1);
        xx.resetX(1);
        this.out.println(" before shift left: " + xx);
        xx.shiftLeft(1);
        this.out.println(" after shift left (1): " + xx);
        xx.resetX(1);
        xx.shiftLeft(2);
        this.out.println(" after shift left (2): " + xx);
        xx.resetX(1);
        xx.shiftLeft(3);
        this.out.println(" after shift left (3): " + xx);
        xx.resetX(1);
        xx.shiftLeft(4);
        this.out.println(" after shift left (4): " + xx);
        xx.resetX(1);
        xx.shiftLeft(5);
        this.out.println(" after shift left (5): " + xx);
        xx.resetX(0);
        this.out.println(" before shift right: " + xx);
        xx.shiftRight(1);
        this.out.println(" after shift right (1): " + xx);
        xx.resetX(0);
        xx.shiftRight(2);
        this.out.println(" after shift right (2): " + xx);
        xx.resetX(0);
        xx.shiftRight(3);
        this.out.println(" after shift right (3): " + xx);
        xx.resetX(0);
        xx.shiftRight(4);
        this.out.println(" after shift right (4): " + xx);
        xx.resetX(0);
        xx.shiftRight(5);
        this.out.println(" after shift right (5): " + xx);
        TrinomialLFSR r = new TrinomialLFSR(4, 3);
        this.out.println(r);
        r.setX(2);
        r.setX(3);
        this.out.println("Initialising to (1001) x3 + x2: " + r);
        String output = "";
        int i = 0;
        while (i < 15) {
            output = String.valueOf(output) + r.next(1) + ", ";
            this.out.println("LFSR state @" + (i + 1) + ": " + r);
            ++i;
        }
        this.out.println("Output sequence is = " + output + "...\n");
        this.passIf(output.equals("1, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 0, 1, 0, 1, "), "LFSR state test");
        r = new TrinomialLFSR(4, 1);
        r.resetX(0);
        TrinomialLFSR a = (TrinomialLFSR)r.clone();
        a.setX(0);
        a.setX(1);
        this.out.println("Generating powers of x (mod(f(x))...");
        int i2 = 0;
        while (i2 < 15) {
            r.clock(1);
            this.out.print(" State @" + (i2 + 1) + ": " + r.toPolynomial());
            int c = r.compareTo(a);
            this.out.println(String.valueOf(c == -1 ? "<" : (c == 0 ? "==" : ">")) + a.toPolynomial());
            ++i2;
        }
        TrinomialLFSR x = r.trinomialX();
        this.out.println("\nSame using pow()...");
        int i3 = 0;
        while (i3 < 15) {
            x = r.trinomialX();
            this.out.print(String.valueOf(x.toPolynomial()) + "** " + i3 + " =");
            x.pow(x.valueOf(i3));
            this.out.println(x.toPolynomial());
            ++i3;
        }
        TrinomialLFSR b = (TrinomialLFSR)r.clone();
        a.resetX(3);
        a.setX(2);
        a.setX(0);
        b.resetX(3);
        b.setX(0);
        this.out.println("\nNow working in GF[2**4] with f(x) = x4 + x + 1 ...");
        this.out.println("Defining 'a' set to (0111) x3 + x2 + 1: " + a.toPolynomial());
        this.out.println("Defining 'b' set to (0011) x3 + 1: " + b.toPolynomial());
        a.multiply(b);
        this.out.println("Computing a * b (mod(f(x)): " + a.toPolynomial());
        TrinomialLFSR aa = (TrinomialLFSR)r.clone();
        TrinomialLFSR bb = (TrinomialLFSR)r.clone();
        aa.resetX(3);
        aa.setX(2);
        aa.setX(0);
        bb.resetX(3);
        bb.setX(0);
        this.out.println("\nDefining 'aa' set to (0111) x3 + x2 + 1: " + aa.toPolynomial());
        this.out.println("Defining 'bb' set to (0011) x3 + 1: " + bb.toPolynomial());
        bb.multiply(aa);
        this.out.println("Computing bb * aa (mod(f(x)): " + bb.toPolynomial());
        this.passIf(a.isSameValue(bb), "a * b == bb * aa?");
        try {
            r.atRandom();
            this.out.println("\nAbout to serialize R...");
            this.out.println("R: " + r);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(r);
            oos.flush();
            baos.close();
            byte[] serialized = baos.toByteArray();
            this.out.println("Finished serialization. Now resetting R...");
            TrinomialLFSR r1 = (TrinomialLFSR)r.clone();
            r1.reset();
            this.out.println("R: " + r1);
            this.out.println("About to deserialize R...");
            ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
            ObjectInputStream ois = new ObjectInputStream(bais);
            r1 = (TrinomialLFSR)ois.readObject();
            bais.close();
            this.out.println("R: " + r1);
            this.passIf(r.isSameValue(r1), "Serialization test");
        }
        catch (Exception e) {
            this.error(e);
        }
        int[] mersenne = new int[]{89, 127, 521, 607, 1279, 2281, 3217};
        int[][] taps = new int[][]{{38}, {63, 30, 15, 7}, {168, 158, 48, 32}, {273, 147, 105}, {418, 216}, {1029, 915, 715}, {576, 67}};
        this.out.println("Testing few monic primitive trinomials as Galois counters...");
        int i4 = 0;
        while (i4 < 2) {
            int L = mersenne[i4];
            BigRegister exp = new BigRegister(L);
            int j = 0;
            while (j < taps[i4].length) {
                int K = taps[i4][j];
                TrinomialLFSR y = new TrinomialLFSR(L, K);
                y.resetX(1);
                TrinomialLFSR z = (TrinomialLFSR)y.clone();
                int limit = L;
                int tt = 1;
                this.out.println(" ...testing: x" + L + " + x" + K + " + 1...");
                while (limit >= 0) {
                    z.resetX(1);
                    z.pow(exp.valueOf(tt));
                    if (!y.isSameValue(z)) {
                        this.fail("LFSR is out of sync...");
                        break;
                    }
                    y.clock(1);
                    ++tt;
                    --limit;
                }
                ++j;
            }
            ++i4;
        }
    }
}

