/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.md;

import cryptix.provider.md.NativeLink;
import cryptix.util.core.Debug;
import cryptix.util.core.LinkStatus;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.Security;
import xjava.security.InvalidParameterTypeException;
import xjava.security.NoSuchParameterException;
import xjava.security.Parameterized;
import xjava.security.VariableLengthDigest;

public class HAVAL
extends MessageDigest
implements Parameterized,
VariableLengthDigest,
Cloneable {
    private static final boolean DEBUG = true;
    private static final int debuglevel = Debug.getLevel("HAVAL");
    private static final PrintWriter err = Debug.getOutput();
    private static NativeLink linkStatus = new NativeLink("HAVAL", 2, 3);
    private boolean native_ok;
    private static final int VERSION = 1;
    private static final int DEFAULT_PASSES = 3;
    private static final int DEFAULT_BITLENGTH = 256;
    private static final int BLOCK_LENGTH = 128;
    private static final int CONTEXT_LENGTH = 8;
    private int passes = 3;
    private int bitLength = 256;
    private long count;
    private int[] context = new int[8];
    private byte[] buffer = new byte[128];
    private int[] X = new int[32];

    private static void debug(String s) {
        err.println("HAVAL: " + s);
    }

    public static LinkStatus getLinkStatus() {
        return linkStatus;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void link() {
        NativeLink nativeLink = linkStatus;
        synchronized (nativeLink) {
            block8: {
                try {
                    if (linkStatus.attemptLoad()) {
                        linkStatus.checkVersion(HAVAL.getLibMajorVersion(), HAVAL.getLibMinorVersion());
                    }
                    if (linkStatus.useNative()) {
                        this.native_ok = true;
                    }
                }
                catch (UnsatisfiedLinkError e) {
                    linkStatus.fail(e);
                    if (debuglevel <= 2) break block8;
                    HAVAL.debug(e.getMessage());
                }
            }
            if (debuglevel > 2) {
                HAVAL.debug("Using native library? " + this.native_ok);
            }
        }
    }

    private static native int getLibMajorVersion();

    private static native int getLibMinorVersion();

    private static native String native_hash(int[] var0, byte[] var1, int var2, int var3);

    public HAVAL() {
        super("HAVAL");
        String ps2;
        try {
            ps2 = Security.getAlgorithmProperty("HAVAL", "passes");
            int p = Integer.parseInt(ps2);
            if (p >= 3 && p <= 5) {
                this.passes = p;
            }
        }
        catch (Exception ps2) {
            // empty catch block
        }
        try {
            ps2 = Security.getAlgorithmProperty("HAVAL", "bitLength");
            int len = Integer.parseInt(ps2);
            if (len % 32 == 0 && len >= 128 && len <= 256) {
                this.bitLength = len;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.engineReset();
        this.link();
    }

    private HAVAL(HAVAL md) {
        this();
        this.passes = md.passes;
        this.bitLength = md.bitLength;
        this.count = md.count;
        this.context = (int[])md.context.clone();
        this.buffer = (byte[])md.buffer.clone();
    }

    public Object clone() {
        return new HAVAL(this);
    }

    protected void engineReset() {
        this.context[0] = 608135816;
        this.context[1] = -2052912941;
        this.context[2] = 320440878;
        this.context[3] = 57701188;
        this.context[4] = -1542899678;
        this.context[5] = 698298832;
        this.context[6] = 137296536;
        this.context[7] = -330404727;
        this.count = 0L;
        int i = 0;
        while (i < 128) {
            this.buffer[i] = 0;
            ++i;
        }
    }

    protected void engineUpdate(byte input) {
        int i = (int)(this.count % 128L);
        ++this.count;
        this.buffer[i] = input;
        if (i == 127) {
            this.transform(this.buffer, 0);
        }
    }

    protected void engineUpdate(byte[] input, int offset, int len) {
        if (offset < 0 || len < 0 || (long)offset + (long)len > (long)input.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int bufferNdx = (int)(this.count % 128L);
        this.count += (long)len;
        int partLen = 128 - bufferNdx;
        int i = 0;
        if (len >= partLen) {
            System.arraycopy(input, offset, this.buffer, bufferNdx, partLen);
            this.transform(this.buffer, 0);
            i = partLen;
            while (i + 128 - 1 < len) {
                this.transform(input, offset + i);
                i += 128;
            }
            bufferNdx = 0;
        }
        if (i < len) {
            System.arraycopy(input, offset + i, this.buffer, bufferNdx, len - i);
        }
    }

    protected byte[] engineDigest() {
        int bufferNdx = (int)(this.count % 128L);
        int padLen = bufferNdx < 118 ? 118 - bufferNdx : 246 - bufferNdx;
        byte[] tail = new byte[padLen + 10];
        tail[0] = 1;
        tail[padLen] = (byte)((this.bitLength & 3) << 6 | (this.passes & 7) << 3 | 1);
        tail[padLen + 1] = (byte)(this.bitLength >>> 2);
        int i = 0;
        while (i < 8) {
            tail[padLen + 2 + i] = (byte)(this.count * 8L >>> 8 * i);
            ++i;
        }
        this.engineUpdate(tail, 0, tail.length);
        this.tailorDigestBits();
        byte[] result = new byte[this.bitLength / 8];
        int i2 = 0;
        while (i2 < this.bitLength / 32) {
            int j = 0;
            while (j < 4) {
                result[i2 * 4 + j] = (byte)(this.context[i2] >>> 8 * j);
                ++j;
            }
            ++i2;
        }
        this.engineReset();
        return result;
    }

    protected int engineGetDigestLength() {
        return this.bitLength / 8;
    }

    public void setParameter(String param, Object value) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        this.engineSetParameter(param, value);
    }

    public Object getParameter(String param) throws NoSuchParameterException, InvalidParameterException {
        return this.engineGetParameter(param);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected void engineSetParameter(String param, Object value) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException {
        if (param.equalsIgnoreCase("passes")) {
            if (!(value instanceof Integer)) throw new InvalidParameterTypeException("passes.HAVAL");
            this.setPasses((Integer)value);
            return;
        } else {
            if (!param.equalsIgnoreCase("bitLength")) throw new NoSuchParameterException(String.valueOf(param) + ".HAVAL");
            if (!(value instanceof Integer)) throw new InvalidParameterTypeException("bitLength.HAVAL");
            this.setBitLength((Integer)value);
        }
    }

    protected Object engineGetParameter(String param) throws NoSuchParameterException, InvalidParameterException {
        if (param.equalsIgnoreCase("passes")) {
            return new Integer(this.passes);
        }
        if (param.equalsIgnoreCase("bitLength")) {
            return new Integer(this.bitLength);
        }
        throw new NoSuchParameterException(String.valueOf(param) + ".HAVAL");
    }

    public void setPasses(int p) {
        if (p < 3 || p > 5) {
            throw new InvalidParameterException();
        }
        this.passes = p;
        this.engineReset();
    }

    public void setBitLength(int len) {
        if (len % 32 != 0 || len < 128 || len > 256) {
            throw new InvalidParameterException();
        }
        this.bitLength = len;
        this.engineReset();
    }

    public void setDigestLength(int len) {
        this.setBitLength(len * 8);
    }

    private void transform(byte[] block, int offset) {
        if (this.native_ok) {
            if (this.context.length != 8 || offset < 0 || (long)offset + 128L > (long)block.length) {
                throw new InternalError(String.valueOf(this.getAlgorithm()) + ": context.length != " + 8 + " || offset < 0 || " + "(long)offset + " + 128 + " > block.length");
            }
            linkStatus.check(HAVAL.native_hash(this.context, block, offset, this.passes));
            return;
        }
        int i = 0;
        while (i < 32) {
            this.X[i] = block[offset++] & 0xFF | (block[offset++] & 0xFF) << 8 | (block[offset++] & 0xFF) << 16 | (block[offset++] & 0xFF) << 24;
            ++i;
        }
        int t0 = this.context[0];
        int t1 = this.context[1];
        int t2 = this.context[2];
        int t3 = this.context[3];
        int t4 = this.context[4];
        int t5 = this.context[5];
        int t6 = this.context[6];
        int t7 = this.context[7];
        t7 = this.FF_1(t7, t6, t5, t4, t3, t2, t1, t0, this.X[0]);
        t6 = this.FF_1(t6, t5, t4, t3, t2, t1, t0, t7, this.X[1]);
        t5 = this.FF_1(t5, t4, t3, t2, t1, t0, t7, t6, this.X[2]);
        t4 = this.FF_1(t4, t3, t2, t1, t0, t7, t6, t5, this.X[3]);
        t3 = this.FF_1(t3, t2, t1, t0, t7, t6, t5, t4, this.X[4]);
        t2 = this.FF_1(t2, t1, t0, t7, t6, t5, t4, t3, this.X[5]);
        t1 = this.FF_1(t1, t0, t7, t6, t5, t4, t3, t2, this.X[6]);
        t0 = this.FF_1(t0, t7, t6, t5, t4, t3, t2, t1, this.X[7]);
        t7 = this.FF_1(t7, t6, t5, t4, t3, t2, t1, t0, this.X[8]);
        t6 = this.FF_1(t6, t5, t4, t3, t2, t1, t0, t7, this.X[9]);
        t5 = this.FF_1(t5, t4, t3, t2, t1, t0, t7, t6, this.X[10]);
        t4 = this.FF_1(t4, t3, t2, t1, t0, t7, t6, t5, this.X[11]);
        t3 = this.FF_1(t3, t2, t1, t0, t7, t6, t5, t4, this.X[12]);
        t2 = this.FF_1(t2, t1, t0, t7, t6, t5, t4, t3, this.X[13]);
        t1 = this.FF_1(t1, t0, t7, t6, t5, t4, t3, t2, this.X[14]);
        t0 = this.FF_1(t0, t7, t6, t5, t4, t3, t2, t1, this.X[15]);
        t7 = this.FF_1(t7, t6, t5, t4, t3, t2, t1, t0, this.X[16]);
        t6 = this.FF_1(t6, t5, t4, t3, t2, t1, t0, t7, this.X[17]);
        t5 = this.FF_1(t5, t4, t3, t2, t1, t0, t7, t6, this.X[18]);
        t4 = this.FF_1(t4, t3, t2, t1, t0, t7, t6, t5, this.X[19]);
        t3 = this.FF_1(t3, t2, t1, t0, t7, t6, t5, t4, this.X[20]);
        t2 = this.FF_1(t2, t1, t0, t7, t6, t5, t4, t3, this.X[21]);
        t1 = this.FF_1(t1, t0, t7, t6, t5, t4, t3, t2, this.X[22]);
        t0 = this.FF_1(t0, t7, t6, t5, t4, t3, t2, t1, this.X[23]);
        t7 = this.FF_1(t7, t6, t5, t4, t3, t2, t1, t0, this.X[24]);
        t6 = this.FF_1(t6, t5, t4, t3, t2, t1, t0, t7, this.X[25]);
        t5 = this.FF_1(t5, t4, t3, t2, t1, t0, t7, t6, this.X[26]);
        t4 = this.FF_1(t4, t3, t2, t1, t0, t7, t6, t5, this.X[27]);
        t3 = this.FF_1(t3, t2, t1, t0, t7, t6, t5, t4, this.X[28]);
        t2 = this.FF_1(t2, t1, t0, t7, t6, t5, t4, t3, this.X[29]);
        t1 = this.FF_1(t1, t0, t7, t6, t5, t4, t3, t2, this.X[30]);
        t0 = this.FF_1(t0, t7, t6, t5, t4, t3, t2, t1, this.X[31]);
        t7 = this.FF_2(t7, t6, t5, t4, t3, t2, t1, t0, this.X[5], 1160258022);
        t6 = this.FF_2(t6, t5, t4, t3, t2, t1, t0, t7, this.X[14], 953160567);
        t5 = this.FF_2(t5, t4, t3, t2, t1, t0, t7, t6, this.X[26], -1101764913);
        t4 = this.FF_2(t4, t3, t2, t1, t0, t7, t6, t5, this.X[18], 887688300);
        t3 = this.FF_2(t3, t2, t1, t0, t7, t6, t5, t4, this.X[11], -1062458953);
        t2 = this.FF_2(t2, t1, t0, t7, t6, t5, t4, t3, this.X[28], -914599715);
        t1 = this.FF_2(t1, t0, t7, t6, t5, t4, t3, t2, this.X[7], 1065670069);
        t0 = this.FF_2(t0, t7, t6, t5, t4, t3, t2, t1, this.X[16], -1253635817);
        t7 = this.FF_2(t7, t6, t5, t4, t3, t2, t1, t0, this.X[0], -1843997223);
        t6 = this.FF_2(t6, t5, t4, t3, t2, t1, t0, t7, this.X[23], -1988494565);
        t5 = this.FF_2(t5, t4, t3, t2, t1, t0, t7, t6, this.X[20], -785314906);
        t4 = this.FF_2(t4, t3, t2, t1, t0, t7, t6, t5, this.X[22], -1730169428);
        t3 = this.FF_2(t3, t2, t1, t0, t7, t6, t5, t4, this.X[1], 805139163);
        t2 = this.FF_2(t2, t1, t0, t7, t6, t5, t4, t3, this.X[10], -803545161);
        t1 = this.FF_2(t1, t0, t7, t6, t5, t4, t3, t2, this.X[4], -1193168915);
        t0 = this.FF_2(t0, t7, t6, t5, t4, t3, t2, t1, this.X[8], 1780907670);
        t7 = this.FF_2(t7, t6, t5, t4, t3, t2, t1, t0, this.X[30], -1166241723);
        t6 = this.FF_2(t6, t5, t4, t3, t2, t1, t0, t7, this.X[3], -248741991);
        t5 = this.FF_2(t5, t4, t3, t2, t1, t0, t7, t6, this.X[21], 614570311);
        t4 = this.FF_2(t4, t3, t2, t1, t0, t7, t6, t5, this.X[9], -1282315017);
        t3 = this.FF_2(t3, t2, t1, t0, t7, t6, t5, t4, this.X[17], 134345442);
        t2 = this.FF_2(t2, t1, t0, t7, t6, t5, t4, t3, this.X[24], -2054226922);
        t1 = this.FF_2(t1, t0, t7, t6, t5, t4, t3, t2, this.X[29], 1667834072);
        t0 = this.FF_2(t0, t7, t6, t5, t4, t3, t2, t1, this.X[6], 1901547113);
        t7 = this.FF_2(t7, t6, t5, t4, t3, t2, t1, t0, this.X[19], -1537671517);
        t6 = this.FF_2(t6, t5, t4, t3, t2, t1, t0, t7, this.X[12], -191677058);
        t5 = this.FF_2(t5, t4, t3, t2, t1, t0, t7, t6, this.X[15], 227898511);
        t4 = this.FF_2(t4, t3, t2, t1, t0, t7, t6, t5, this.X[13], 1921955416);
        t3 = this.FF_2(t3, t2, t1, t0, t7, t6, t5, t4, this.X[2], 1904987480);
        t2 = this.FF_2(t2, t1, t0, t7, t6, t5, t4, t3, this.X[25], -2112533778);
        t1 = this.FF_2(t1, t0, t7, t6, t5, t4, t3, t2, this.X[31], 2069144605);
        t0 = this.FF_2(t0, t7, t6, t5, t4, t3, t2, t1, this.X[27], -1034266187);
        t7 = this.FF_3(t7, t6, t5, t4, t3, t2, t1, t0, this.X[19], -1674521287);
        t6 = this.FF_3(t6, t5, t4, t3, t2, t1, t0, t7, this.X[9], 720527379);
        t5 = this.FF_3(t5, t4, t3, t2, t1, t0, t7, t6, this.X[4], -976113629);
        t4 = this.FF_3(t4, t3, t2, t1, t0, t7, t6, t5, this.X[20], 677414384);
        t3 = this.FF_3(t3, t2, t1, t0, t7, t6, t5, t4, this.X[28], -901678824);
        t2 = this.FF_3(t2, t1, t0, t7, t6, t5, t4, t3, this.X[17], -1193592593);
        t1 = this.FF_3(t1, t0, t7, t6, t5, t4, t3, t2, this.X[8], -1904616272);
        t0 = this.FF_3(t0, t7, t6, t5, t4, t3, t2, t1, this.X[22], 1614419982);
        t7 = this.FF_3(t7, t6, t5, t4, t3, t2, t1, t0, this.X[29], 1822297739);
        t6 = this.FF_3(t6, t5, t4, t3, t2, t1, t0, t7, this.X[14], -1340175810);
        t5 = this.FF_3(t5, t4, t3, t2, t1, t0, t7, t6, this.X[25], -686458943);
        t4 = this.FF_3(t4, t3, t2, t1, t0, t7, t6, t5, this.X[12], -1120842969);
        t3 = this.FF_3(t3, t2, t1, t0, t7, t6, t5, t4, this.X[24], 2024746970);
        t2 = this.FF_3(t2, t1, t0, t7, t6, t5, t4, t3, this.X[30], 1432378464);
        t1 = this.FF_3(t1, t0, t7, t6, t5, t4, t3, t2, this.X[16], -430627341);
        t0 = this.FF_3(t0, t7, t6, t5, t4, t3, t2, t1, this.X[26], -1437226092);
        t7 = this.FF_3(t7, t6, t5, t4, t3, t2, t1, t0, this.X[31], 1464375394);
        t6 = this.FF_3(t6, t5, t4, t3, t2, t1, t0, t7, this.X[15], 1676153920);
        t5 = this.FF_3(t5, t4, t3, t2, t1, t0, t7, t6, this.X[7], 1439316330);
        t4 = this.FF_3(t4, t3, t2, t1, t0, t7, t6, t5, this.X[3], 715854006);
        t3 = this.FF_3(t3, t2, t1, t0, t7, t6, t5, t4, this.X[1], -1261675468);
        t2 = this.FF_3(t2, t1, t0, t7, t6, t5, t4, t3, this.X[0], 289532110);
        t1 = this.FF_3(t1, t0, t7, t6, t5, t4, t3, t2, this.X[18], -1588296017);
        t0 = this.FF_3(t0, t7, t6, t5, t4, t3, t2, t1, this.X[27], 2087905683);
        t7 = this.FF_3(t7, t6, t5, t4, t3, t2, t1, t0, this.X[13], -1276242927);
        t6 = this.FF_3(t6, t5, t4, t3, t2, t1, t0, t7, this.X[6], 1668267050);
        t5 = this.FF_3(t5, t4, t3, t2, t1, t0, t7, t6, this.X[21], 732546397);
        t4 = this.FF_3(t4, t3, t2, t1, t0, t7, t6, t5, this.X[10], 1947742710);
        t3 = this.FF_3(t3, t2, t1, t0, t7, t6, t5, t4, this.X[23], -832815594);
        t2 = this.FF_3(t2, t1, t0, t7, t6, t5, t4, t3, this.X[11], -1685613794);
        t1 = this.FF_3(t1, t0, t7, t6, t5, t4, t3, t2, this.X[5], -1344882125);
        t0 = this.FF_3(t0, t7, t6, t5, t4, t3, t2, t1, this.X[2], 1814351708);
        if (this.passes >= 4) {
            t7 = this.FF_4(t7, t6, t5, t4, t3, t2, t1, t0, this.X[24], 2050118529);
            t6 = this.FF_4(t6, t5, t4, t3, t2, t1, t0, t7, this.X[4], 680887927);
            t5 = this.FF_4(t5, t4, t3, t2, t1, t0, t7, t6, this.X[0], 999245976);
            t4 = this.FF_4(t4, t3, t2, t1, t0, t7, t6, t5, this.X[14], 1800124847);
            t3 = this.FF_4(t3, t2, t1, t0, t7, t6, t5, t4, this.X[2], -994056165);
            t2 = this.FF_4(t2, t1, t0, t7, t6, t5, t4, t3, this.X[7], 1713906067);
            t1 = this.FF_4(t1, t0, t7, t6, t5, t4, t3, t2, this.X[28], 1641548236);
            t0 = this.FF_4(t0, t7, t6, t5, t4, t3, t2, t1, this.X[23], -81679983);
            t7 = this.FF_4(t7, t6, t5, t4, t3, t2, t1, t0, this.X[26], 1216130144);
            t6 = this.FF_4(t6, t5, t4, t3, t2, t1, t0, t7, this.X[6], 1575780402);
            t5 = this.FF_4(t5, t4, t3, t2, t1, t0, t7, t6, this.X[30], -276538019);
            t4 = this.FF_4(t4, t3, t2, t1, t0, t7, t6, t5, this.X[20], -377129551);
            t3 = this.FF_4(t3, t2, t1, t0, t7, t6, t5, t4, this.X[18], -601480446);
            t2 = this.FF_4(t2, t1, t0, t7, t6, t5, t4, t3, this.X[25], -345695352);
            t1 = this.FF_4(t1, t0, t7, t6, t5, t4, t3, t2, this.X[19], 596196993);
            t0 = this.FF_4(t0, t7, t6, t5, t4, t3, t2, t1, this.X[3], -745100091);
            t7 = this.FF_4(t7, t6, t5, t4, t3, t2, t1, t0, this.X[22], 258830323);
            t6 = this.FF_4(t6, t5, t4, t3, t2, t1, t0, t7, this.X[11], -2081144263);
            t5 = this.FF_4(t5, t4, t3, t2, t1, t0, t7, t6, this.X[31], 772490370);
            t4 = this.FF_4(t4, t3, t2, t1, t0, t7, t6, t5, this.X[21], -1534844924);
            t3 = this.FF_4(t3, t2, t1, t0, t7, t6, t5, t4, this.X[8], 1774776394);
            t2 = this.FF_4(t2, t1, t0, t7, t6, t5, t4, t3, this.X[27], -1642095778);
            t1 = this.FF_4(t1, t0, t7, t6, t5, t4, t3, t2, this.X[12], 566650946);
            t0 = this.FF_4(t0, t7, t6, t5, t4, t3, t2, t1, this.X[9], -152474470);
            t7 = this.FF_4(t7, t6, t5, t4, t3, t2, t1, t0, this.X[1], 1728879713);
            t6 = this.FF_4(t6, t5, t4, t3, t2, t1, t0, t7, this.X[29], -1412200208);
            t5 = this.FF_4(t5, t4, t3, t2, t1, t0, t7, t6, this.X[5], 1783734482);
            t4 = this.FF_4(t4, t3, t2, t1, t0, t7, t6, t5, this.X[15], -665571480);
            t3 = this.FF_4(t3, t2, t1, t0, t7, t6, t5, t4, this.X[17], -1777359064);
            t2 = this.FF_4(t2, t1, t0, t7, t6, t5, t4, t3, this.X[10], -1420741725);
            t1 = this.FF_4(t1, t0, t7, t6, t5, t4, t3, t2, this.X[16], 1861159788);
            t0 = this.FF_4(t0, t7, t6, t5, t4, t3, t2, t1, this.X[13], 326777828);
        }
        if (this.passes == 5) {
            t7 = this.FF_5(t7, t6, t5, t4, t3, t2, t1, t0, this.X[27], -1170476976);
            t6 = this.FF_5(t6, t5, t4, t3, t2, t1, t0, t7, this.X[3], 2130389656);
            t5 = this.FF_5(t5, t4, t3, t2, t1, t0, t7, t6, this.X[21], -1578015459);
            t4 = this.FF_5(t4, t3, t2, t1, t0, t7, t6, t5, this.X[26], 967770486);
            t3 = this.FF_5(t3, t2, t1, t0, t7, t6, t5, t4, this.X[17], 1724537150);
            t2 = this.FF_5(t2, t1, t0, t7, t6, t5, t4, t3, this.X[11], -2109534584);
            t1 = this.FF_5(t1, t0, t7, t6, t5, t4, t3, t2, this.X[20], -1930525159);
            t0 = this.FF_5(t0, t7, t6, t5, t4, t3, t2, t1, this.X[29], 1164943284);
            t7 = this.FF_5(t7, t6, t5, t4, t3, t2, t1, t0, this.X[19], 2105845187);
            t6 = this.FF_5(t6, t5, t4, t3, t2, t1, t0, t7, this.X[0], 998989502);
            t5 = this.FF_5(t5, t4, t3, t2, t1, t0, t7, t6, this.X[12], -529566248);
            t4 = this.FF_5(t4, t3, t2, t1, t0, t7, t6, t5, this.X[7], -2050940813);
            t3 = this.FF_5(t3, t2, t1, t0, t7, t6, t5, t4, this.X[13], 1075463327);
            t2 = this.FF_5(t2, t1, t0, t7, t6, t5, t4, t3, this.X[8], 1455516326);
            t1 = this.FF_5(t1, t0, t7, t6, t5, t4, t3, t2, this.X[31], 1322494562);
            t0 = this.FF_5(t0, t7, t6, t5, t4, t3, t2, t1, this.X[10], 910128902);
            t7 = this.FF_5(t7, t6, t5, t4, t3, t2, t1, t0, this.X[5], 469688178);
            t6 = this.FF_5(t6, t5, t4, t3, t2, t1, t0, t7, this.X[9], 1117454909);
            t5 = this.FF_5(t5, t4, t3, t2, t1, t0, t7, t6, this.X[14], 936433444);
            t4 = this.FF_5(t4, t3, t2, t1, t0, t7, t6, t5, this.X[30], -804646328);
            t3 = this.FF_5(t3, t2, t1, t0, t7, t6, t5, t4, this.X[18], -619713837);
            t2 = this.FF_5(t2, t1, t0, t7, t6, t5, t4, t3, this.X[6], 1240580251);
            t1 = this.FF_5(t1, t0, t7, t6, t5, t4, t3, t2, this.X[28], 122909385);
            t0 = this.FF_5(t0, t7, t6, t5, t4, t3, t2, t1, this.X[24], -2137449605);
            t7 = this.FF_5(t7, t6, t5, t4, t3, t2, t1, t0, this.X[2], 634681816);
            t6 = this.FF_5(t6, t5, t4, t3, t2, t1, t0, t7, this.X[23], -152510729);
            t5 = this.FF_5(t5, t4, t3, t2, t1, t0, t7, t6, this.X[16], -469872614);
            t4 = this.FF_5(t4, t3, t2, t1, t0, t7, t6, t5, this.X[22], -1233564613);
            t3 = this.FF_5(t3, t2, t1, t0, t7, t6, t5, t4, this.X[4], -1754472259);
            t2 = this.FF_5(t2, t1, t0, t7, t6, t5, t4, t3, this.X[1], 79693498);
            t1 = this.FF_5(t1, t0, t7, t6, t5, t4, t3, t2, this.X[25], -1045868618);
            t0 = this.FF_5(t0, t7, t6, t5, t4, t3, t2, t1, this.X[15], 1084186820);
        }
        this.context[0] = this.context[0] + t0;
        this.context[1] = this.context[1] + t1;
        this.context[2] = this.context[2] + t2;
        this.context[3] = this.context[3] + t3;
        this.context[4] = this.context[4] + t4;
        this.context[5] = this.context[5] + t5;
        this.context[6] = this.context[6] + t6;
        this.context[7] = this.context[7] + t7;
    }

    private void tailorDigestBits() {
        if (this.bitLength == 128) {
            int t = this.context[7] & 0xFF | this.context[6] & 0xFF000000 | this.context[5] & 0xFF0000 | this.context[4] & 0xFF00;
            this.context[0] = this.context[0] + (t >>> 8 | t << 24);
            t = this.context[7] & 0xFF00 | this.context[6] & 0xFF | this.context[5] & 0xFF000000 | this.context[4] & 0xFF0000;
            this.context[1] = this.context[1] + (t >>> 16 | t << 16);
            t = this.context[7] & 0xFF0000 | this.context[6] & 0xFF00 | this.context[5] & 0xFF | this.context[4] & 0xFF000000;
            this.context[2] = this.context[2] + (t >>> 24 | t << 8);
            t = this.context[7] & 0xFF000000 | this.context[6] & 0xFF0000 | this.context[5] & 0xFF00 | this.context[4] & 0xFF;
            this.context[3] = this.context[3] + t;
        } else if (this.bitLength == 160) {
            int t = this.context[7] & 0x3F | this.context[6] & 0xFE000000 | this.context[5] & 0x1F80000;
            this.context[0] = this.context[0] + (t >>> 19 | t << 13);
            t = this.context[7] & 0xFC0 | this.context[6] & 0x3F | this.context[5] & 0xFE000000;
            this.context[1] = this.context[1] + (t >>> 25 | t << 7);
            t = this.context[7] & 0x7F000 | this.context[6] & 0xFC0 | this.context[5] & 0x3F;
            this.context[2] = this.context[2] + t;
            t = this.context[7] & 0x1F80000 | this.context[6] & 0x7F000 | this.context[5] & 0xFC0;
            this.context[3] = this.context[3] + (t >>> 6);
            t = this.context[7] & 0xFE000000 | this.context[6] & 0x1F80000 | this.context[5] & 0x7F000;
            this.context[4] = this.context[4] + (t >>> 12);
        } else if (this.bitLength == 192) {
            int t = this.context[7] & 0x1F | this.context[6] & 0xFC000000;
            this.context[0] = this.context[0] + (t >>> 26 | t << 6);
            t = this.context[7] & 0x3E0 | this.context[6] & 0x1F;
            this.context[1] = this.context[1] + t;
            t = this.context[7] & 0xFC00 | this.context[6] & 0x3E0;
            this.context[2] = this.context[2] + (t >>> 5);
            t = this.context[7] & 0x1F0000 | this.context[6] & 0xFC00;
            this.context[3] = this.context[3] + (t >>> 10);
            t = this.context[7] & 0x3E00000 | this.context[6] & 0x1F0000;
            this.context[4] = this.context[4] + (t >>> 16);
            t = this.context[7] & 0xFC000000 | this.context[6] & 0x3E00000;
            this.context[5] = this.context[5] + (t >>> 21);
        } else if (this.bitLength == 224) {
            this.context[0] = this.context[0] + (this.context[7] >>> 27 & 0x1F);
            this.context[1] = this.context[1] + (this.context[7] >>> 22 & 0x1F);
            this.context[2] = this.context[2] + (this.context[7] >>> 18 & 0xF);
            this.context[3] = this.context[3] + (this.context[7] >>> 13 & 0x1F);
            this.context[4] = this.context[4] + (this.context[7] >>> 9 & 0xF);
            this.context[5] = this.context[5] + (this.context[7] >>> 4 & 0x1F);
            this.context[6] = this.context[6] + (this.context[7] & 0xF);
        }
    }

    private int FF_1(int x7, int x6, int x5, int x4, int x3, int x2, int x1, int x0, int w) {
        int t;
        switch (this.passes) {
            case 3: {
                t = this.f_1(x1, x0, x3, x5, x6, x2, x4);
                break;
            }
            case 4: {
                t = this.f_1(x2, x6, x1, x4, x5, x3, x0);
                break;
            }
            default: {
                t = this.f_1(x3, x4, x1, x0, x5, x2, x6);
            }
        }
        return (t >>> 7 | t << 25) + (x7 >>> 11 | x7 << 21) + w;
    }

    private int FF_2(int x7, int x6, int x5, int x4, int x3, int x2, int x1, int x0, int w, int c) {
        int t;
        switch (this.passes) {
            case 3: {
                t = this.f_2(x4, x2, x1, x0, x5, x3, x6);
                break;
            }
            case 4: {
                t = this.f_2(x3, x5, x2, x0, x1, x6, x4);
                break;
            }
            default: {
                t = this.f_2(x6, x2, x1, x0, x3, x4, x5);
            }
        }
        return (t >>> 7 | t << 25) + (x7 >>> 11 | x7 << 21) + w + c;
    }

    private int FF_3(int x7, int x6, int x5, int x4, int x3, int x2, int x1, int x0, int w, int c) {
        int t;
        switch (this.passes) {
            case 3: {
                t = this.f_3(x6, x1, x2, x3, x4, x5, x0);
                break;
            }
            case 4: {
                t = this.f_3(x1, x4, x3, x6, x0, x2, x5);
                break;
            }
            default: {
                t = this.f_3(x2, x6, x0, x4, x3, x1, x5);
            }
        }
        return (t >>> 7 | t << 25) + (x7 >>> 11 | x7 << 21) + w + c;
    }

    private int FF_4(int x7, int x6, int x5, int x4, int x3, int x2, int x1, int x0, int w, int c) {
        int t;
        switch (this.passes) {
            case 4: {
                t = this.f_4(x6, x4, x0, x5, x2, x1, x3);
                break;
            }
            default: {
                t = this.f_4(x1, x5, x3, x2, x0, x4, x6);
            }
        }
        return (t >>> 7 | t << 25) + (x7 >>> 11 | x7 << 21) + w + c;
    }

    private int FF_5(int x7, int x6, int x5, int x4, int x3, int x2, int x1, int x0, int w, int c) {
        int t = this.f_5(x2, x5, x0, x6, x4, x3, x1);
        return (t >>> 7 | t << 25) + (x7 >>> 11 | x7 << 21) + w + c;
    }

    private int f_1(int x6, int x5, int x4, int x3, int x2, int x1, int x0) {
        return x1 & (x0 ^ x4) ^ x2 & x5 ^ x3 & x6 ^ x0;
    }

    private int f_2(int x6, int x5, int x4, int x3, int x2, int x1, int x0) {
        return x2 & (x1 & ~x3 ^ x4 & x5 ^ x6 ^ x0) ^ x4 & (x1 ^ x5) ^ x3 & x5 ^ x0;
    }

    private int f_3(int x6, int x5, int x4, int x3, int x2, int x1, int x0) {
        return x3 & (x1 & x2 ^ x6 ^ x0) ^ x1 & x4 ^ x2 & x5 ^ x0;
    }

    private int f_4(int x6, int x5, int x4, int x3, int x2, int x1, int x0) {
        return x4 & (x5 & ~x2 ^ x3 & ~x6 ^ x1 ^ x6 ^ x0) ^ x3 & (x1 & x2 ^ x5 ^ x6) ^ x2 & x6 ^ x0;
    }

    private int f_5(int x6, int x5, int x4, int x3, int x2, int x1, int x0) {
        return x0 & (x1 & x2 & x3 ^ ~x5) ^ x1 & x4 ^ x2 & x5 ^ x3 & x6;
    }
}

