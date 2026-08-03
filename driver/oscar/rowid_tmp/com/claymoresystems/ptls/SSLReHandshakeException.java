/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLException;

public class SSLReHandshakeException
extends SSLException {
    public SSLReHandshakeException() {
        super("Peer requested rehandshake");
    }
}

