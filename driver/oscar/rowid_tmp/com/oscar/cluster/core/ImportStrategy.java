/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.cluster.core;

import com.oscar.cluster.core.DataImportStream;

public interface ImportStrategy {
    public DataImportStream nextStream();

    public DataImportStream currentStream();
}

