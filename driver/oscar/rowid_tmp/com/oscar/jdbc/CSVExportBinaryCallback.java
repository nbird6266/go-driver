/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.ExportBinaryCallback;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CSVExportBinaryCallback
implements ExportBinaryCallback {
    OutputStream out;
    byte[] colSepByte;
    byte[] rowSepByte;
    char escape = (char)34;
    boolean useEscape = false;
    private byte[] columnBuffer = new byte[16384];
    int colPosition = 0;
    boolean hasHeadColumn = false;

    public CSVExportBinaryCallback(String fileName, String colSep, String rowSep, Character escape, boolean append, boolean hasHeadColumn) throws IOException {
        this(fileName, colSep, rowSep, escape, append);
        this.hasHeadColumn = hasHeadColumn;
    }

    public CSVExportBinaryCallback(String fileName, String colSep, String rowSep, Character escape, boolean append) throws IOException {
        File file;
        this.colSepByte = colSep.getBytes();
        this.rowSepByte = rowSep.getBytes();
        if (escape != null) {
            this.useEscape = true;
            this.escape = escape.charValue();
        }
        if (!(file = new File(fileName)).exists()) {
            file.createNewFile();
        }
        this.out = new BufferedOutputStream(new FileOutputStream(file, append));
    }

    public CSVExportBinaryCallback(String fileName, String colSep, String rowSep, boolean append, int columnCount) throws IOException {
        this(fileName, colSep, rowSep, null, append);
    }

    public void processColumn(int colIndex, int dataType, byte[] data) throws IOException {
        this.writeColumn(data);
    }

    public void processEndColumn() throws IOException {
        this.out.write(this.colSepByte);
    }

    public void processEndRow(long rowNum) throws IOException {
        this.out.write(this.rowSepByte);
    }

    public void processEnd() throws IOException {
        this.out.close();
        this.columnBuffer = null;
    }

    public void writeColumn(byte[] data) throws IOException {
        if (data != null) {
            if (this.useEscape) {
                boolean containsEscape = false;
                boolean containsColumn = false;
                int columnLength = data.length;
                for (int j = 0; j < columnLength; ++j) {
                    int t;
                    if (data[j] == this.escape) {
                        containsEscape = true;
                        this.columnBuffer[this.colPosition++] = data[j];
                        this.columnBuffer[this.colPosition++] = data[j];
                        continue;
                    }
                    int k = j;
                    if (containsEscape) {
                        this.columnBuffer[this.colPosition++] = data[j];
                        continue;
                    }
                    if (columnLength - j >= this.colSepByte.length) {
                        t = 0;
                        while (t < this.colSepByte.length) {
                            if (this.colSepByte[t] != data[j]) {
                                j = k;
                                break;
                            }
                            if (t == this.colSepByte.length - 1) {
                                containsColumn = true;
                                containsEscape = true;
                            }
                            ++t;
                            ++j;
                        }
                    }
                    if (!containsColumn && columnLength - j >= this.rowSepByte.length) {
                        t = 0;
                        while (t < this.rowSepByte.length) {
                            if (this.rowSepByte[t] != data[j]) {
                                j = k;
                                break;
                            }
                            if (t == this.rowSepByte.length - 1) {
                                containsEscape = true;
                            }
                            ++t;
                            ++j;
                        }
                    }
                    if (k == j) {
                        this.columnBuffer[this.colPosition++] = data[j];
                        continue;
                    }
                    System.arraycopy(data, k, this.columnBuffer, this.colPosition, j - k + 1);
                }
                if (containsEscape) {
                    this.out.write(this.escape);
                    this.out.write(this.columnBuffer, 0, this.colPosition);
                    this.out.write(this.escape);
                } else {
                    this.out.write(this.columnBuffer, 0, this.colPosition);
                }
                this.colPosition = 0;
            }
        } else {
            this.out.write(data);
        }
    }

    public boolean hasHeadColumn() {
        return this.hasHeadColumn;
    }
}

