/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.provider.key.RawKeyGenerator;

public class BlowfishKeyGenerator
extends RawKeyGenerator {
    public BlowfishKeyGenerator() {
        super("Blowfish", 5, 16, 56);
    }
}

