/*
 * Decompiled with CFR 0.152.
 */
package cryptix.test;

import cryptix.util.math.BigRegister;
import cryptix.util.test.BaseTest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class TestBR
extends BaseTest {
    public static void main(String[] args) {
        new TestBR().commandline(args);
    }

    protected void engineTest() throws Exception {
        this.setExpectedPasses(8);
        BigRegister r = new BigRegister(64);
        this.out.println(r);
        r.setBit(0);
        r.setBit(5);
        r.setBit(6);
        this.out.println("Setting bits 0, 5 and 6...\n" + r);
        r.shiftLeft(10);
        this.out.println("Shift left 10 positions...\n" + r);
        r.shiftLeft(10);
        this.out.println("Shift left 10 positions...\n" + r);
        r.shiftRight(11);
        this.out.println("Shift right 11 positions...\n" + r);
        r.shiftRight(9);
        this.out.println("Shift right 9 positions...\n" + r);
        r.shiftLeft(-2);
        this.out.println("Shift right 2 positions (using 'shiftLeft')...\n" + r);
        r.shiftRight(-4);
        this.out.println("Shift left 4 positions (using 'shiftRight')...\n" + r);
        r.shiftRight(2);
        this.out.println("Shift right 2 positions...\n" + r);
        r.rotateLeft(10);
        this.out.println("Rotating left 10 positions...\n" + r);
        r.rotateRight(13);
        this.out.println("Rotating right 13 positions...\n" + r);
        r.rotateRight(-10);
        this.out.println("Rotating left 10 positions (using 'rotateRight')...\n" + r);
        r.rotateLeft(-13);
        this.out.println("Rotating right 13 positions (using 'rotateLeft')...\n" + r);
        r.rotateLeft(50);
        this.out.println("Rotating left 50 positions = right 14...\n" + r);
        r.rotateRight(60);
        this.out.println("Rotating right 60 positions = left 4...\n" + r);
        r.shiftRight(65);
        this.out.println("Shifting right 65 positions...\n" + r);
        BigRegister b = new BigRegister(64);
        BigRegister a = new BigRegister(64);
        a.atRandom();
        BigRegister aa = (BigRegister)a.clone();
        this.out.println("Register A (random value): " + a);
        this.out.println("Register AA (copy of A): " + a);
        this.out.println("Both A and AA have " + a.countSetBits() + " set bits between indices: #" + a.lowestSetBit() + " and #" + a.highestSetBit() + " (inclusive)...");
        this.out.println();
        this.out.println("Register B: " + b);
        a.and(b);
        this.out.println("Register AA: " + aa);
        this.out.println("A = A & B: " + a);
        this.passIf(a.countSetBits() == 0, "Register A now has 0 set bits?");
        a.load(aa);
        a.or(b);
        this.out.println("Register A: " + a);
        this.passIf(a.isSameValue(aa), "A == AA using isSameValue?");
        this.out.print("How does A compare to AA?");
        int x = a.compareTo(aa);
        this.out.println(" " + (x == 0 ? "A == AA" : (x == -1 ? "A < AA" : " A > AA")));
        this.passIf(x == 0, "A == AA using compareTo?");
        this.out.println();
        b.not();
        this.out.println("Register B: " + b);
        this.passIf(b.countSetBits() == b.getSize(), "Register B is now all 1s?");
        a.atRandom();
        b.atRandom();
        aa = (BigRegister)a.clone();
        this.out.println("Register A (random value): " + a);
        this.out.println("Register B (random value): " + b);
        a.xor(b);
        this.out.println("A = A ^ B: " + a);
        this.out.println("Register AA (old value of A): " + aa);
        this.passIf(!a.isSameValue(aa), "A != AA?");
        a.xor(b);
        this.out.println("(A ^ B) ^ B: " + a);
        this.passIf(a.isSameValue(aa), "AA = (A ^ B) ^ B?");
        try {
            a.atRandom();
            this.out.println("About to serialize A and B...");
            this.out.println("Register A (random value): " + a);
            this.out.println("Register B: " + b);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(a);
            oos.writeObject(b);
            oos.flush();
            baos.close();
            byte[] serialized = baos.toByteArray();
            this.out.println("About to deserialize A and B...");
            ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
            ObjectInputStream ois = new ObjectInputStream(bais);
            aa = (BigRegister)ois.readObject();
            BigRegister bb = (BigRegister)ois.readObject();
            bais.close();
            this.out.println("Register AA: " + aa);
            this.out.println("Register BB: " + bb);
            this.passIf(a.isSameValue(aa), "A == AA?");
            this.passIf(b.isSameValue(bb), "B == BB?");
        }
        catch (Exception e) {
            this.error(e);
        }
        r = new BigRegister(53);
        this.out.println(r);
        r.setBit(0);
        r.setBit(5);
        r.setBit(6);
        this.out.println("Setting bits 0, 5 and 6...\n" + r);
        r.shiftLeft(10);
        this.out.println("Shift left 10 positions...\n" + r);
        r.rotateRight(13);
        this.out.println("Rotating right 13 positions...\n" + r);
        r.setBits(45, 5, 255L);
        this.out.println("Setting 5 bits starting @45 to 1s...\n" + r);
        this.out.println("2 bits starting @44 have a value of: " + r.getBits(44, 2));
        this.out.println("4 bits starting @46 have a value of: " + r.getBits(46, 4));
    }
}

