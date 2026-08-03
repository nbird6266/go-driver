/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.core.ExportCallback;
import java.io.IOException;

public interface ExportBinaryCallback
extends ExportCallback {
    public void processColumn(int var1, int var2, byte[] var3) throws IOException;
}

