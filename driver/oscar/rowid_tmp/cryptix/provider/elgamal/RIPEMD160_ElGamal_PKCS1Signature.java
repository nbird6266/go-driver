/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.elgamal;

import cryptix.provider.elgamal.Any_ElGamal_PKCS1Signature;

public class RIPEMD160_ElGamal_PKCS1Signature
extends Any_ElGamal_PKCS1Signature {
    private static final byte[] RIPEMD160_ASN_DATA;

    static {
        byte[] byArray = new byte[15];
        byArray[0] = 48;
        byArray[1] = 33;
        byArray[2] = 48;
        byArray[3] = 9;
        byArray[4] = 6;
        byArray[5] = 5;
        byArray[6] = 43;
        byArray[7] = 36;
        byArray[8] = 3;
        byArray[9] = 2;
        byArray[10] = 1;
        byArray[11] = 5;
        byArray[13] = 4;
        byArray[14] = 20;
        RIPEMD160_ASN_DATA = byArray;
    }

    public RIPEMD160_ElGamal_PKCS1Signature() {
        super("RIPEMD160");
    }

    protected byte[] getAlgorithmEncoding() {
        return RIPEMD160_ASN_DATA;
    }
}

