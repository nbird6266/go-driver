/*
 * Decompiled with CFR 0.152.
 */
package cryptix.provider.key;

import cryptix.provider.key.RawKey;
import xjava.security.SecretKey;

public class RawSecretKey
extends RawKey
implements SecretKey {
    public RawSecretKey(String algorithm, byte[] data) {
        super(algorithm, data);
    }

    public RawSecretKey(String algorithm, byte[] data, int offset, int length) {
        super(algorithm, data, offset, length);
    }
}

