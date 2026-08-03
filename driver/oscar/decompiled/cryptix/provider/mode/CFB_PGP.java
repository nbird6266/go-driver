/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.mode;

import cryptix.provider.mode.CFB;
import xjava.security.Cipher;

public class CFB_PGP
extends CFB {
    public CFB_PGP() {
        this.setInitializationVector(new byte[this.getInitializationVectorLength()]);
    }

    public CFB_PGP(Cipher cipher) {
        super(cipher);
        this.setInitializationVector(new byte[this.getInitializationVectorLength()]);
    }

    protected int engineUpdate(byte[] in, int inOffset, int inLen, byte[] out, int outOffset) {
        this.next_block();
        return super.engineUpdate(in, inOffset, inLen, out, outOffset);
    }
}

