/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.ExportStringCallback;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class CSVExportStringCallback
implements ExportStringCallback {
    Writer writer;
    String colSep;
    String rowSep;
    char escape = (char)34;
    boolean useEscape = false;
    char[] columnBuffer = new char[16384];
    int colPosition = 0;
    boolean hasHeadColumn = false;

    public CSVExportStringCallback(String fileName, String colSep, String rowSep, Character escape, boolean append, boolean hasHeadColumn) throws IOException {
        this(fileName, colSep, rowSep, escape, append);
        this.hasHeadColumn = hasHeadColumn;
    }

    public CSVExportStringCallback(String fileName, String colSep, String rowSep, Character escape, boolean append) throws IOException {
        File file;
        this.colSep = colSep;
        this.rowSep = rowSep;
        if (escape != null) {
            this.useEscape = true;
            this.escape = escape.charValue();
        }
        if (!(file = new File(fileName)).exists()) {
            file.createNewFile();
        }
        this.writer = new BufferedWriter(new FileWriter(file, append));
    }

    public CSVExportStringCallback(String fileName, String colSep, String rowSep, boolean append) throws IOException {
        this(fileName, colSep, rowSep, null, append);
    }

    public void processEndColumn() throws IOException {
        this.writer.write(this.colSep);
    }

    public void processEndRow(long rowNum) throws IOException {
        this.writer.write(this.rowSep);
    }

    public void processEnd() throws IOException {
        this.writer.close();
        this.columnBuffer = null;
    }

    public void processColumn(int colIndex, int dataType, String data) throws IOException {
        if (data != null) {
            if (this.useEscape) {
                char[] columnData = null;
                boolean containsEscape = false;
                if (data.indexOf(this.colSep) >= 0 || data.indexOf(this.rowSep) >= 0) {
                    containsEscape = true;
                }
                columnData = data.toCharArray();
                for (int i = 0; i < columnData.length; ++i) {
                    if (columnData[i] == this.escape) {
                        this.columnBuffer[this.colPosition++] = columnData[i];
                        this.columnBuffer[this.colPosition++] = columnData[i];
                        containsEscape = true;
                        continue;
                    }
                    this.columnBuffer[this.colPosition++] = columnData[i];
                }
                if (containsEscape) {
                    this.writer.write(this.escape);
                    this.writer.write(this.columnBuffer, 0, this.colPosition);
                    this.writer.write(this.escape);
                } else {
                    this.writer.write(columnData, 0, this.colPosition);
                }
                this.colPosition = 0;
                containsEscape = false;
            } else {
                this.writer.write(data);
            }
        }
    }

    public boolean hasHeadColumn() {
        return this.hasHeadColumn;
    }
}

