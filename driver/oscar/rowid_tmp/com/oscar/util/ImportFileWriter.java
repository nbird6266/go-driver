/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class ImportFileWriter {
    private static final int bufferLength = 0x500000;
    private String fileName = null;
    private Writer writer = null;
    private Separator separator = new Separator();
    private char[] columnBuffer = new char[0x500000];
    private char[] columnData = null;
    private int colPosition = 0;
    private int i = 0;
    private boolean containsSep = false;

    public ImportFileWriter(String fileName, String colSep, String rowSep, char escapeChar, boolean append) throws IOException {
        this.fileName = fileName;
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        this.writer = new BufferedWriter(new FileWriter(file, append));
        this.init(colSep, rowSep, escapeChar);
    }

    public ImportFileWriter(String fileName, String colSep, String rowSep, boolean append) throws IOException {
        this.fileName = fileName;
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        this.writer = new BufferedWriter(new FileWriter(file, append));
        this.init(colSep, rowSep);
    }

    public ImportFileWriter(Writer writer, String colSep, String rowSep, char escapeChar) throws IOException {
        this.writer = writer;
        this.init(colSep, rowSep, escapeChar);
    }

    public ImportFileWriter(Writer writer, String colSep, String rowSep) throws IOException {
        this.writer = writer;
        this.init(colSep, rowSep);
    }

    public void init(String colSep, String rowSep, char escapeChar) {
        this.separator.colSep = colSep;
        this.separator.rowSep = rowSep;
        this.separator.escapeChar = escapeChar;
    }

    public void init(String colSep, String rowSep) {
        this.separator.colSep = colSep;
        this.separator.rowSep = rowSep;
        this.separator.containsSep = false;
    }

    public void writeColumn(String columnValue) throws IOException {
        if (!this.separator.containsSep) {
            this.writeColumn1(columnValue);
            return;
        }
        if (columnValue != null) {
            if (columnValue.indexOf(this.separator.colSep) >= 0 || columnValue.indexOf(this.separator.rowSep) >= 0) {
                this.containsSep = true;
            }
            this.columnData = columnValue.toCharArray();
            this.i = 0;
            while (this.i < this.columnData.length) {
                if (this.columnData[this.i] == this.separator.escapeChar) {
                    this.columnBuffer[this.colPosition++] = this.columnData[this.i];
                    this.columnBuffer[this.colPosition++] = this.columnData[this.i];
                    this.containsSep = true;
                } else {
                    this.columnBuffer[this.colPosition++] = this.columnData[this.i];
                }
                ++this.i;
            }
            if (this.containsSep) {
                this.writer.write(new char[]{this.separator.escapeChar});
                this.writer.write(this.columnBuffer, 0, this.colPosition);
                this.writer.write(new char[]{this.separator.escapeChar});
            } else {
                this.writer.write(this.columnData, 0, this.colPosition);
            }
            this.colPosition = 0;
            this.containsSep = false;
        }
    }

    public void writeColumn1(String columnValue) throws IOException {
        if (columnValue != null) {
            this.columnData = columnValue.toCharArray();
            this.writer.write(this.columnData);
        }
    }

    public void nextColumn() throws IOException {
        this.writer.write(this.separator.colSep);
    }

    public void nextRow() throws IOException {
        this.writer.write(this.separator.rowSep);
    }

    public void close() throws IOException {
        if (this.fileName != null && this.writer != null) {
            this.writer.flush();
            this.writer.close();
        }
        this.fileName = null;
        this.writer = null;
        this.separator = null;
        this.columnData = null;
        this.columnBuffer = null;
    }

    private class Separator {
        public static final char c = '\"';
        public String rowSep = "\r\n";
        public String colSep = "\t";
        public char escapeChar = (char)34;
        public boolean containsSep = true;

        public Separator() {
            String os = System.getProperty("os.name");
            if (os.toUpperCase().startsWith("WIN")) {
                this.rowSep = "\r\n";
                this.colSep = "\t";
            } else if (os.toUpperCase().startsWith("LIN")) {
                this.rowSep = "\n";
                this.colSep = "\t";
            }
        }
    }
}

