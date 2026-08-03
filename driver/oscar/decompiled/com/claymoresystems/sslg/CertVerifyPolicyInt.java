/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.sslg;

public class CertVerifyPolicyInt {
    private boolean checkCertificateDatesV;
    private boolean requireBasicConstraintsV;
    private boolean requireBasicConstraintsCriticalV;
    private boolean requireKeyUsageV;

    public void checkDates(boolean checkdate) {
        this.checkCertificateDatesV = checkdate;
    }

    public boolean checkDatesP() {
        return this.checkCertificateDatesV;
    }

    public void requireBasicConstraints(boolean require) {
        this.requireBasicConstraintsV = require;
    }

    public boolean requireBasicConstraintsP() {
        return this.requireBasicConstraintsV;
    }

    public void requireBasicConstraintsCritical(boolean require) {
        this.requireBasicConstraintsCriticalV = require;
    }

    public boolean requireBasicConstraintsCriticalP() {
        return this.requireBasicConstraintsCriticalV;
    }

    public void requireKeyUsage(boolean require) {
        this.requireKeyUsageV = require;
    }

    public boolean requireKeyUsageP() {
        return this.requireKeyUsageV;
    }
}

