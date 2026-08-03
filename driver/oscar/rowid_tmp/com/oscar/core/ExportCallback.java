/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import java.io.IOException;

interface ExportCallback {
    public void processEndColumn() throws IOException;

    public void processEndRow(long var1) throws IOException;

    public void processEnd() throws IOException;

    public boolean hasHeadColumn();
}

