/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.core.ExportCallback;
import java.io.IOException;

public interface ExportObjectCallback
extends ExportCallback {
    public void processColumn(int var1, int var2, Object var3) throws IOException;
}

