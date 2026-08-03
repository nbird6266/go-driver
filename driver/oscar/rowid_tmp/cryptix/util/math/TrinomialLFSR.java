/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.math;

import cryptix.util.math.BigRegister;
import java.io.Serializable;

public class TrinomialLFSR
extends BigRegister
implements Cloneable,
Serializable {
    private int L;
    private int K;
    private int slice;
    private int warpFactor;
    private static final long serialVersionUID = -8054549768481919515L;
    private static final boolean PRE_COMPUTE_POWERS = true;
    private transient TrinomialLFSR[] powers;

    public TrinomialLFSR(int l, int k) {
        super(l);
        if (k < 1 || k > l - 1) {
            throw new IllegalArgumentException();
        }
        this.L = l;
        this.K = k;
        this.warpFactor = Math.min(this.K, this.L - this.K);
        this.slice = Math.min(64, this.warpFactor);
    }

    public Object clone() {
        TrinomialLFSR result = new TrinomialLFSR(this.L, this.K);
        result.load(this);
        return result;
    }

    public void clock(int ticks) {
        if (ticks < 1) {
            return;
        }
        int morcel = ticks % this.slice;
        if (morcel != 0) {
            this.engineClock(morcel);
            ticks -= morcel;
        }
        while (ticks > 0) {
            this.engineClock(this.slice);
            ticks -= this.slice;
        }
    }

    protected void engineClock(int ticks) {
        long io = this.getBits(this.L - ticks, ticks) ^ this.getBits(this.L - this.K - ticks, ticks);
        this.shiftLeft(ticks);
        if (io != 0L) {
            this.setBits(0, ticks, io);
        }
    }

    private void jump(int ticks) {
        if (ticks < 1) {
            return;
        }
        int morcel = ticks % this.warpFactor;
        if (morcel != 0) {
            this.clock(morcel);
            ticks -= morcel;
        }
        while (ticks > 0) {
            BigRegister b1 = (BigRegister)this.clone();
            BigRegister b2 = (BigRegister)this.clone();
            b2.shiftLeft(this.warpFactor);
            b2.xor(b1);
            b2.shiftRight(this.L - this.warpFactor);
            this.shiftLeft(this.warpFactor);
            this.or(b2);
            ticks -= this.warpFactor;
        }
    }

    public long next(int count) {
        if (count < 1) {
            return 0L;
        }
        long result = this.getBits(this.L - count, count);
        this.clock(count);
        return result;
    }

    public void add(TrinomialLFSR gx) {
        if (!this.isSameGroup(gx)) {
            throw new IllegalArgumentException();
        }
        this.xor(gx);
    }

    public void subtract(TrinomialLFSR gx) {
        this.add(gx);
    }

    public void multiply(TrinomialLFSR gx) {
        if (!this.isSameGroup(gx)) {
            throw new IllegalArgumentException();
        }
        if (gx.countSetBits() == 0) {
            this.reset();
            return;
        }
        TrinomialLFSR X = new TrinomialLFSR(this.L, this.K);
        TrinomialLFSR result = null;
        int i = 0;
        while (i < this.L) {
            if (gx.testBit(i)) {
                X.load(this);
                int t = this.degreeAt(i);
                if (t != 0) {
                    X.jump(t);
                }
                if (result == null) {
                    result = (TrinomialLFSR)X.clone();
                } else {
                    result.add(X);
                }
            }
            ++i;
        }
        this.load(result);
    }

    public static TrinomialLFSR multiply(TrinomialLFSR p, TrinomialLFSR q) {
        TrinomialLFSR result;
        if (!p.isSameGroup(q)) {
            throw new IllegalArgumentException();
        }
        if (p.countSetBits() > q.countSetBits()) {
            result = (TrinomialLFSR)p.clone();
            result.multiply(q);
        } else {
            result = (TrinomialLFSR)q.clone();
            result.multiply(p);
        }
        return result;
    }

    public void pow(BigRegister n) {
        int i;
        if (n.getSize() > this.L) {
            throw new IllegalArgumentException();
        }
        int limit = n.highestSetBit();
        if (limit == 0) {
            return;
        }
        if (limit == -1) {
            this.resetX(0);
            return;
        }
        TrinomialLFSR Y = this.trinomialOne();
        TrinomialLFSR Z = (TrinomialLFSR)this.clone();
        if (this.powers == null) {
            this.powers = new TrinomialLFSR[this.L];
        }
        if (!this.isSameValue(this.powers[0])) {
            this.powers[0] = (TrinomialLFSR)Z.clone();
            i = 1;
            while (i < this.L) {
                Z.multiply(Z);
                this.powers[i] = (TrinomialLFSR)Z.clone();
                ++i;
            }
        }
        i = 0;
        while (i < limit) {
            if (n.testBit(i)) {
                Y = TrinomialLFSR.multiply(this.powers[i], Y);
            }
            ++i;
        }
        Z = this.powers[i];
        this.load(TrinomialLFSR.multiply(Y, Z));
    }

    public void resetX(int n) {
        this.reset();
        this.setX(n);
    }

    public void setX(int n) {
        this.setBit(this.indexOfX(n));
    }

    public int indexOfX(int degree) {
        if (degree < 0 || degree >= this.L) {
            throw new IllegalArgumentException();
        }
        int index = degree - this.K;
        if (index < 0) {
            index += this.L;
        }
        return index;
    }

    public int degreeAt(int index) {
        if (index < 0 || index >= this.L) {
            throw new IllegalArgumentException();
        }
        return (index + this.K) % this.L;
    }

    public TrinomialLFSR trinomialOne() {
        TrinomialLFSR x = (TrinomialLFSR)this.clone();
        x.resetX(0);
        return x;
    }

    public TrinomialLFSR trinomialX() {
        TrinomialLFSR x = (TrinomialLFSR)this.clone();
        x.resetX(1);
        return x;
    }

    public int getSize() {
        return this.L;
    }

    public int getMidTap() {
        return this.K;
    }

    public int getSlice() {
        return this.slice;
    }

    public boolean isSameValue(TrinomialLFSR x) {
        if (x == null || x.K != this.K) {
            return false;
        }
        return super.isSameValue(x);
    }

    public int compareTo(TrinomialLFSR x) {
        if (this.L > x.L) {
            return 1;
        }
        if (this.L < x.L) {
            return -1;
        }
        if (this.K > x.K) {
            return 1;
        }
        if (this.K < x.K) {
            return -1;
        }
        BigRegister ba = this.toBigRegister();
        BigRegister bb = x.toBigRegister();
        return ba.compareTo(bb);
    }

    public boolean isSameGroup(TrinomialLFSR x) {
        return x != null && this.L == x.L && this.K == x.K;
    }

    public BigRegister toBigRegister() {
        BigRegister result = (BigRegister)this.clone();
        result.rotateLeft(this.K);
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(8 * this.L + 64);
        sb.append("[...\n TrinomialLFSR <").append(this.L).append(", x").append(this.L).append(" + ").append(this.K == 1 ? "x" : "x" + this.K).append(" + 1").append(">...\n").append(" current state is: ").append(super.toString()).append("...]\n");
        return sb.toString();
    }

    public String toPolynomial() {
        StringBuffer sb = new StringBuffer(16);
        sb.append(' ');
        int d = this.L;
        boolean firstTime = true;
        while (--d >= 0) {
            if (!this.testBit(this.indexOfX(d))) continue;
            if (firstTime) {
                firstTime = !firstTime;
            } else {
                sb.append(" + ");
            }
            if (d != 0) {
                sb.append('x');
                if (d == 1) continue;
                sb.append(d);
                continue;
            }
            sb.append('1');
        }
        if (firstTime) {
            sb.append('0');
        }
        return sb.append(' ').toString();
    }
}

