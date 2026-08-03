/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.core;

import com.oscar.util.ImportDataContainer;
import java.io.IOException;

public interface ImportDataProvider {
    public static final int SUCCESSFUL = 1;
    public static final int FAILED = 2;
    public static final int END = 4;

    public int setNextColumnData(ImportDataContainer var1);

    public boolean nextRow() throws IOException;

    public void close() throws IOException;

    public String getFileEncoding();

    public boolean hasHeadColumn();
}

