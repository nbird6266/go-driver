/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLAlertX;
import com.claymoresystems.ptls.SSLException;

public class SSLAlertException
extends SSLException {
    private SSLAlertX alert;

    public SSLAlertException(SSLAlertX alert_) {
        super(alert_.getExplanation());
        this.alert = alert_;
    }

    public boolean fatalP() {
        return this.alert.fatalP();
    }

    public String getExplanation() {
        return this.alert.getExplanation();
    }
}

