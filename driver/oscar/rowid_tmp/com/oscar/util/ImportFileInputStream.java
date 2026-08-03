/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.jdbc.OscarImportHandler;
import com.oscar.util.OSQLException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

public class ImportFileInputStream {
    private static final int dataLength = 8192;
    private InputStream input = null;
    private String fileName = null;
    private byte[] dataBuffer = new byte[8192];
    private int position = 0;
    private byte[] columnBuffer = new byte[0x500000];
    private int colPosition = 0;
    private boolean closed = false;
    private int byteLength = 0;
    private boolean nullMarked = false;
    private OscarImportHandler handler = null;
    private Separator separator = new Separator();

    public ImportFileInputStream(OscarImportHandler handler, String fileName, String colSep, String rowSep, char escapeChar) throws SQLException {
        try {
            this.input = new BufferedInputStream(new FileInputStream(fileName));
        }
        catch (FileNotFoundException e) {
            throw new OSQLException("OSCAR-00808", "88888", 808, e);
        }
        this.init(handler, fileName, colSep, rowSep, escapeChar);
    }

    public ImportFileInputStream(OscarImportHandler handler, String fileName, String colSep, String rowSep) throws SQLException {
        try {
            this.input = new BufferedInputStream(new FileInputStream(fileName));
        }
        catch (FileNotFoundException e) {
            throw new OSQLException("OSCAR-00808", "88888", 808, e);
        }
        this.init(handler, fileName, colSep, rowSep);
    }

    public ImportFileInputStream(OscarImportHandler handler, InputStream input, String colSep, String rowSep, char escapeChar) {
        this.input = input;
        this.init(handler, null, colSep, rowSep, escapeChar);
    }

    public ImportFileInputStream(OscarImportHandler handler, InputStream input, String colSep, String rowSep) {
        this.input = input;
        this.init(handler, null, colSep, rowSep);
    }

    public void init(OscarImportHandler handler, String fileName, String colSep, String rowSep, char escapeChar) {
        this.handler = handler;
        this.fileName = fileName;
        this.separator.colSepStr = colSep;
        this.separator.rowSepStr = rowSep;
        this.separator.escapeChar = escapeChar;
    }

    public void init(OscarImportHandler handler, String fileName, String colSep, String rowSep) {
        this.handler = handler;
        this.fileName = fileName;
        this.separator.colSepStr = colSep;
        this.separator.rowSepStr = rowSep;
        this.separator.containsSep = false;
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
        byte[] column = new byte[]{};
        byte[] colSeparator = null;
        byte[] rowSeparator = null;
        try {
            colSeparator = this.separator.colSepStr.getBytes("GBK");
            rowSeparator = this.separator.rowSepStr.getBytes("GBK");
        }
        catch (UnsupportedEncodingException e1) {
            throw new OSQLException("OSCAR-00808", "88888", 808, e1);
        }
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
            block8: while (temp != -1) {
                temp = this.input.read(this.dataBuffer, this.position, this.dataBuffer.length - this.position);
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
                block9: for (i = 0; i < length; ++i) {
                    if (temp != -1 && i > length - maxLength) {
                        System.arraycopy(this.dataBuffer, this.position, this.dataBuffer, 0, length - this.position);
                        this.position = length - this.position;
                        this.colPosition = 0;
                        containsSep = false;
                        continue block8;
                    }
                    k = i;
                    if (containsSep) {
                        if (this.dataBuffer[i] == this.separator.escapeChar) {
                            if (this.dataBuffer[++i] == this.separator.escapeChar) {
                                this.columnBuffer[this.colPosition++] = (byte)this.separator.escapeChar;
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
                                column = new byte[this.colPosition];
                                System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                                this.setColumn(columnIndex++, column);
                                column = new byte[]{};
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
                                    column = new byte[this.colPosition];
                                    System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                                    this.setColumn(columnIndex++, column);
                                    column = new byte[]{};
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
                            if (this.position != length) continue block9;
                            this.position = 0;
                            continue block9;
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
                                column = new byte[this.colPosition];
                                System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                                this.setColumn(columnIndex++, column);
                                column = new byte[]{};
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
                                    column = new byte[this.colPosition];
                                    System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                                    this.setColumn(columnIndex++, column);
                                    column = new byte[]{};
                                    this.position = i--;
                                    this.colPosition = 0;
                                    EOF = true;
                                } else {
                                    this.columnBuffer[this.colPosition++] = this.dataBuffer[i];
                                }
                            }
                            isColumn = false;
                            isRow = false;
                            if (this.position != length) continue block9;
                            this.position = 0;
                            continue block9;
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
                            this.columnBuffer[this.colPosition++] = (byte)this.separator.escapeChar;
                            continue;
                        }
                        this.columnBuffer[this.colPosition++] = this.dataBuffer[i];
                    }
                    column = new byte[this.colPosition];
                    System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                } else {
                    column = new byte[leftLength];
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

    private void setColumn(int columnIndex, byte[] column) throws SQLException {
        if (!this.nullMarked && column.length != 0) {
            this.handler.setBytes(columnIndex, column);
        }
    }

    public boolean checkClosed() {
        return this.closed;
    }

    public void close() throws SQLException {
        if (this.fileName != null && this.input != null) {
            try {
                this.input.close();
            }
            catch (IOException e) {
                throw new OSQLException("OSCAR-00808", "88888", 808, e);
            }
        }
        this.handler = null;
        this.fileName = null;
        this.input = null;
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

