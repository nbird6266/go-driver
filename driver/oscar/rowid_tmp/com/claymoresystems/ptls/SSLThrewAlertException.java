/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLAlertException;
import com.claymoresystems.ptls.SSLAlertX;

public class SSLThrewAlertException
extends SSLAlertException {
    public SSLThrewAlertException(SSLAlertX alert_) {
        super(alert_);
    }
}

