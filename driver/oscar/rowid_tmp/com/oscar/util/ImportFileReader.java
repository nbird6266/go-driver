/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.jdbc.OscarImportHandler;
import com.oscar.util.OSQLException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.sql.SQLException;

public class ImportFileReader {
    private static final int dataLength = 8192;
    private Reader reader = null;
    private String fileName = null;
    private char[] dataBuffer = new char[8192];
    private int position = 0;
    private char[] columnBuffer = new char[0x500000];
    private int colPosition = 0;
    private boolean closed = false;
    private int byteLength = 0;
    private boolean nullMarked = false;
    private Charset charset = Charset.forName("GBK");
    private OscarImportHandler handler = null;
    private Separator separator = new Separator();

    public ImportFileReader(OscarImportHandler handler, String fileName, String colSep, String rowSep, char escapeChar, Charset charset) throws SQLException {
        try {
            this.reader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(fileName), charset));
        }
        catch (FileNotFoundException e) {
            throw new OSQLException("OSCAR-00808", "88888", 808, e);
        }
        this.init(handler, fileName, colSep, rowSep, escapeChar, charset);
    }

    public ImportFileReader(OscarImportHandler handler, String fileName, String colSep, String rowSep, Charset charset) throws SQLException {
        try {
            this.reader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(fileName), charset));
        }
        catch (FileNotFoundException e) {
            throw new OSQLException("OSCAR-00808", "88888", 808, e);
        }
        this.init(handler, fileName, colSep, rowSep, charset);
    }

    public ImportFileReader(OscarImportHandler handler, String fileName, String colSep, String rowSep, char escapeChar) throws SQLException {
        this(handler, fileName, colSep, rowSep, escapeChar, Charset.forName("GBK"));
    }

    public ImportFileReader(OscarImportHandler handler, String fileName, String colSep, String rowSep) throws SQLException {
        this(handler, fileName, colSep, rowSep, Charset.forName("GBK"));
    }

    public ImportFileReader(OscarImportHandler handler, Reader reader, String colSep, String rowSep, char escapeChar) {
        this.reader = reader;
        this.init(handler, null, colSep, rowSep, escapeChar, null);
    }

    public ImportFileReader(OscarImportHandler handler, Reader reader, String colSep, String rowSep) {
        this.reader = reader;
        this.init(handler, null, colSep, rowSep, null);
    }

    public void init(OscarImportHandler handler, String fileName, String colSep, String rowSep, char escapeChar, Charset charset) {
        this.handler = handler;
        this.fileName = fileName;
        this.separator.colSepStr = colSep;
        this.separator.rowSepStr = rowSep;
        this.separator.escapeChar = escapeChar;
        if (charset != null) {
            this.charset = charset;
        }
    }

    public void init(OscarImportHandler handler, String fileName, String colSep, String rowSep, Charset charset) {
        this.handler = handler;
        this.fileName = fileName;
        this.separator.colSepStr = colSep;
        this.separator.rowSepStr = rowSep;
        this.separator.containsSep = false;
        if (charset != null) {
            this.charset = charset;
        }
    }

    public void read() throws SQLException {
        if (this.checkClosed()) {
            throw new OSQLException("OSCAR-00809", "88888", 107);
        }
        this.readStandardCSV();
    }

    public void readStandardCSV() throws SQLException {
        int columnIndex = 1;
        int temp = 0;
        int length = 0;
        int leftLength = 0;
        char[] column = new char[]{};
        char[] colSeparator = this.separator.colSepStr.toCharArray();
        char[] rowSeparator = this.separator.rowSepStr.toCharArray();
        boolean isColumn = false;
        boolean isRow = false;
        boolean isExecuted = false;
        boolean containsSep = false;
        boolean EOF = false;
        int i = 0;
        int j = 0;
        int k = 0;
        int maxLength = colSeparator.length > rowSeparator.length ? colSeparator.length : rowSeparator.length;
        int status = this.separator.rowSepStr.indexOf(this.separator.colSepStr) < 0 ? 0 : 1;
        try {
            block6: while (temp != -1) {
                temp = this.reader.read(this.dataBuffer, this.position, this.dataBuffer.length - this.position);
                if (temp == -1) {
                    length = this.position;
                    leftLength = this.position;
                    if (containsSep) {
                        containsSep = false;
                    }
                } else {
                    length = temp + this.position;
                    this.position = 0;
                }
                block7: for (i = 0; i < length; ++i) {
                    if (temp != -1 && i > length - maxLength) {
                        System.arraycopy(this.dataBuffer, this.position, this.dataBuffer, 0, length - this.position);
                        this.position = length - this.position;
                        this.colPosition = 0;
                        containsSep = false;
                        continue block6;
                    }
                    k = i;
                    if (containsSep) {
                        if (this.dataBuffer[i] == this.separator.escapeChar) {
                            if (this.dataBuffer[++i] == this.separator.escapeChar) {
                                this.columnBuffer[this.colPosition++] = this.separator.escapeChar;
                                if (i != length - 1) continue;
                                --i;
                                continue;
                            }
                            --i;
                            containsSep = false;
                            continue;
                        }
                        this.columnBuffer[this.colPosition++] = this.dataBuffer[i];
                        continue;
                    }
                    if (this.separator.containsSep && this.dataBuffer[i] == this.separator.escapeChar) {
                        containsSep = true;
                        continue;
                    }
                    switch (status) {
                        case 0: {
                            for (j = 0; j < colSeparator.length; ++j) {
                                if (this.dataBuffer[i] == colSeparator[j]) {
                                    ++i;
                                } else {
                                    isColumn = false;
                                    break;
                                }
                                isColumn = true;
                            }
                            if (isColumn) {
                                this.byteLength += i - this.position - colSeparator.length;
                                column = new char[this.colPosition];
                                System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                                this.setColumn(columnIndex++, column);
                                column = new char[]{};
                                this.position = i--;
                                this.colPosition = 0;
                                EOF = true;
                            } else {
                                i = k;
                                for (j = 0; j < rowSeparator.length; ++j) {
                                    if (this.dataBuffer[i] == rowSeparator[j]) {
                                        ++i;
                                    } else {
                                        isRow = false;
                                        break;
                                    }
                                    isRow = true;
                                }
                                if (isRow) {
                                    this.byteLength += i - this.position - rowSeparator.length;
                                    column = new char[this.colPosition];
                                    System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                                    this.setColumn(columnIndex++, column);
                                    column = new char[]{};
                                    this.position = i--;
                                    this.colPosition = 0;
                                    this.handler.endRow();
                                    isExecuted = false;
                                    EOF = false;
                                    columnIndex = 1;
                                } else {
                                    this.columnBuffer[this.colPosition++] = this.dataBuffer[i];
                                }
                            }
                            isColumn = false;
                            isRow = false;
                            if (this.position != length) continue block7;
                            this.position = 0;
                            continue block7;
                        }
                        case 1: {
                            for (j = 0; j < rowSeparator.length; ++j) {
                                if (this.dataBuffer[i] == rowSeparator[j]) {
                                    ++i;
                                } else {
                                    isRow = false;
                                    break;
                                }
                                isRow = true;
                            }
                            if (isRow) {
                                this.byteLength += i - this.position - rowSeparator.length;
                                column = new char[this.colPosition];
                                System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                                this.setColumn(columnIndex++, column);
                                column = new char[]{};
                                this.position = i--;
                                this.colPosition = 0;
                                this.handler.endRow();
                                isExecuted = false;
                                EOF = false;
                                columnIndex = 1;
                            } else {
                                i = k;
                                for (j = 0; j < colSeparator.length; ++j) {
                                    if (this.dataBuffer[i] == colSeparator[j]) {
                                        ++i;
                                    } else {
                                        isColumn = false;
                                        break;
                                    }
                                    isColumn = true;
                                }
                                if (isColumn) {
                                    this.byteLength += i - this.position - colSeparator.length;
                                    column = new char[this.colPosition];
                                    System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                                    this.setColumn(columnIndex++, column);
                                    column = new char[]{};
                                    this.position = i--;
                                    this.colPosition = 0;
                                    EOF = true;
                                } else {
                                    this.columnBuffer[this.colPosition++] = this.dataBuffer[i];
                                }
                            }
                            isColumn = false;
                            isRow = false;
                            if (this.position != length) continue block7;
                            this.position = 0;
                            continue block7;
                        }
                    }
                }
            }
            if (EOF) {
                this.colPosition = 0;
                containsSep = this.separator.containsSep && leftLength >= 2 && this.dataBuffer[0] == this.separator.escapeChar;
                if (containsSep) {
                    for (i = 1; i < leftLength; ++i) {
                        if (this.dataBuffer[i] == this.separator.escapeChar) {
                            if (i == leftLength - 1 || this.dataBuffer[++i] != this.separator.escapeChar) break;
                            this.columnBuffer[this.colPosition++] = this.separator.escapeChar;
                            continue;
                        }
                        this.columnBuffer[this.colPosition++] = this.dataBuffer[i];
                    }
                    column = new char[this.colPosition];
                    System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                } else {
                    column = new char[leftLength];
                    System.arraycopy(this.dataBuffer, 0, column, 0, leftLength);
                }
                this.setColumn(columnIndex, column);
                this.handler.endRow();
                isExecuted = false;
            }
            if (!isExecuted) {
                this.handler.execute();
            }
        }
        catch (Exception e) {
            if (e instanceof SQLException) {
                throw (SQLException)e;
            }
            throw new OSQLException("OSCAR-00808", "88888", 808, e);
        }
    }

    private void setColumn(int columnIndex, char[] column) throws SQLException {
        if (!this.nullMarked && column.length != 0) {
            CharBuffer charBuffer = CharBuffer.wrap(column);
            byte[] bytes = this.charset.encode(charBuffer).array();
            this.handler.setBytes(columnIndex, bytes);
        }
    }

    public boolean checkClosed() {
        return this.closed;
    }

    public void close() throws SQLException {
        if (this.fileName != null && this.reader != null) {
            try {
                this.reader.close();
            }
            catch (IOException e) {
                throw new OSQLException("OSCAR-00808", "88888", 808, e);
            }
        }
        this.handler = null;
        this.fileName = null;
        this.reader = null;
        this.dataBuffer = null;
        this.columnBuffer = null;
        this.closed = true;
    }

    public boolean isNullMarked() {
        return this.nullMarked;
    }

    public void setNullMarked(boolean nullMarked) {
        this.nullMarked = nullMarked;
    }

    private class Separator {
        private static final char c = '\"';
        public String rowSepStr = "\r\n";
        public String colSepStr = "\t";
        public char escapeChar = (char)34;
        public boolean containsSep = true;

        public Separator() {
            String os = System.getProperty("os.name");
            if (os.toUpperCase().startsWith("WIN")) {
                this.rowSepStr = "\r\n";
                this.colSepStr = "\t";
            } else if (os.toUpperCase().startsWith("LIN")) {
                this.rowSepStr = "\n";
                this.colSepStr = "\t";
            }
        }
    }
}

