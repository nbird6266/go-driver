/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.elgamal;

import cryptix.provider.elgamal.Any_ElGamal_PKCS1Signature;

public class MD2_ElGamal_PKCS1Signature
extends Any_ElGamal_PKCS1Signature {
    private static final byte[] MD2_ASN_DATA;

    static {
        byte[] byArray = new byte[18];
        byArray[0] = 48;
        byArray[1] = 32;
        byArray[2] = 48;
        byArray[3] = 12;
        byArray[4] = 6;
        byArray[5] = 8;
        byArray[6] = 42;
        byArray[7] = -122;
        byArray[8] = 72;
        byArray[9] = -122;
        byArray[10] = -9;
        byArray[11] = 13;
        byArray[12] = 2;
        byArray[13] = 2;
        byArray[14] = 5;
        byArray[16] = 4;
        byArray[17] = 16;
        MD2_ASN_DATA = byArray;
    }

    public MD2_ElGamal_PKCS1Signature() {
        super("MD2");
    }

    protected byte[] getAlgorithmEncoding() {
        return MD2_ASN_DATA;
    }
}

