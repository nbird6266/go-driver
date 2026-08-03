/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.ImportDataProvider;
import com.oscar.util.ImportDataContainer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CSVImportStringProvider
implements ImportDataProvider {
    Reader reader;
    char[] colSepByte;
    char[] rowSepByte;
    char escape = (char)34;
    boolean useEscape = false;
    private char[] dataBuffer = new char[16384];
    char[] columnBuffer = new char[8192];
    List rows = new ArrayList();
    List current_row;
    Iterator it;
    int rowPosition = -1;
    int byteLength = 0;
    int position = 0;
    int colPosition = 0;
    int fetchSize = 1000;
    String fileEncoding = null;
    boolean hasHeadColumn = false;

    public CSVImportStringProvider(String fileName, String colSep, String rowSep, Character escape, Charset paramCharset, boolean hasHeadColumn) throws IOException {
        this(fileName, colSep, rowSep, escape, paramCharset);
        this.hasHeadColumn = hasHeadColumn;
    }

    public CSVImportStringProvider(String fileName, String colSep, String rowSep, Character escape) throws IOException {
        this.colSepByte = colSep.toCharArray();
        this.rowSepByte = rowSep.toCharArray();
        if (escape != null) {
            this.useEscape = true;
            this.escape = escape.charValue();
        }
        File file = new File(fileName);
        this.reader = new BufferedReader(new FileReader(file));
        this.parse();
    }

    public CSVImportStringProvider(String fileName, String colSep, String rowSep) throws IOException {
        this(fileName, colSep, rowSep, null);
    }

    public CSVImportStringProvider(String fileName, String colSep, String rowSep, Character escape, Charset paramCharset) throws IOException {
        this.colSepByte = colSep.toCharArray();
        this.rowSepByte = rowSep.toCharArray();
        if (escape != null) {
            this.useEscape = true;
            this.escape = escape.charValue();
        }
        this.reader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(fileName), paramCharset));
        this.parse();
    }

    public int setNextColumnData(ImportDataContainer container) {
        try {
            if (this.it.hasNext()) {
                container.setDataType(1);
                container.setData(this.it.next());
                return 1;
            }
            return 4;
        }
        catch (Exception e) {
            return 2;
        }
    }

    public boolean nextRow() throws IOException {
        if (++this.rowPosition < this.rows.size()) {
            this.current_row = (List)this.rows.get(this.rowPosition);
            this.it = this.current_row.iterator();
            return true;
        }
        this.rowPosition = 0;
        this.parse();
        if (this.rows.size() > 0) {
            this.current_row = (List)this.rows.get(this.rowPosition);
            this.it = this.current_row.iterator();
            return true;
        }
        return false;
    }

    /*
     * Enabled aggressive block sorting
     */
    public void parse() throws IOException {
        int columnIndex = 1;
        int temp = 0;
        int length = 0;
        int leftLength = 0;
        int rowCount = 0;
        char[] column = new char[]{};
        boolean isColumn = false;
        boolean isRow = false;
        boolean containsSep = false;
        boolean EOF = false;
        int i = 0;
        int j = 0;
        int k = 0;
        int maxLength = this.colSepByte.length > this.rowSepByte.length ? this.colSepByte.length : this.rowSepByte.length;
        int status = String.valueOf(this.colSepByte).indexOf(String.valueOf(this.rowSepByte)) < 0 ? 0 : 1;
        this.rows.clear();
        ArrayList row = new ArrayList(0);
        block4: while (temp != -1) {
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
            for (i = 0; i < length; ++i) {
                if (temp != -1 && i >= length - maxLength) {
                    this.swap(length);
                    containsSep = false;
                    continue block4;
                }
                k = i;
                if (containsSep) {
                    if (this.dataBuffer[i] == this.escape) {
                        if (this.dataBuffer[++i] == this.escape) {
                            this.columnBuffer[this.colPosition++] = this.escape;
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
                if (this.useEscape && this.dataBuffer[i] == this.escape) {
                    containsSep = true;
                    continue;
                }
                switch (status) {
                    case 0: {
                        for (j = 0; j < this.colSepByte.length; ++i, ++j) {
                            if (this.dataBuffer[i] == this.colSepByte[j]) {
                                isColumn = true;
                                continue;
                            }
                            isColumn = false;
                            break;
                        }
                        if (isColumn) {
                            this.byteLength += i - this.position - this.colSepByte.length;
                            column = new char[this.colPosition];
                            System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                            this.addColumn(columnIndex++, column, row);
                            column = new char[]{};
                            this.position = i--;
                            this.colPosition = 0;
                            EOF = true;
                        } else {
                            i = k;
                            for (j = 0; j < this.rowSepByte.length; ++i, ++j) {
                                if (this.dataBuffer[i] == this.rowSepByte[j]) {
                                    isRow = true;
                                    continue;
                                }
                                isRow = false;
                                break;
                            }
                            if (isRow) {
                                this.byteLength += i - this.position - this.rowSepByte.length;
                                column = new char[this.colPosition];
                                System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                                this.addColumn(columnIndex++, column, row);
                                column = new char[]{};
                                this.position = i--;
                                this.colPosition = 0;
                                EOF = false;
                                this.addRow(row);
                                if (++rowCount == this.fetchSize) {
                                    this.swap(length);
                                    break block4;
                                }
                                row = new ArrayList();
                                columnIndex = 1;
                            } else {
                                this.columnBuffer[this.colPosition++] = this.dataBuffer[i];
                            }
                        }
                        isColumn = false;
                        isRow = false;
                        if (this.position != length) break;
                        this.position = 0;
                        break;
                    }
                    case 1: {
                        for (j = 0; j < this.rowSepByte.length; ++i, ++j) {
                            if (this.dataBuffer[i] == this.rowSepByte[j]) {
                                isRow = true;
                                continue;
                            }
                            isRow = false;
                            break;
                        }
                        if (isRow) {
                            this.byteLength += i - this.position - this.rowSepByte.length;
                            column = new char[this.colPosition];
                            System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                            this.addColumn(columnIndex++, column, row);
                            column = new char[]{};
                            this.position = i--;
                            this.colPosition = 0;
                            EOF = false;
                            this.addRow(row);
                            if (++rowCount == this.fetchSize) {
                                this.swap(length);
                                break block4;
                            }
                            row = new ArrayList();
                            columnIndex = 1;
                        } else {
                            i = k;
                            for (j = 0; j < this.colSepByte.length; ++i, ++j) {
                                if (this.dataBuffer[i] == this.colSepByte[j]) {
                                    isColumn = true;
                                    continue;
                                }
                                isColumn = false;
                                break;
                            }
                            if (isColumn) {
                                this.byteLength += i - this.position - this.colSepByte.length;
                                column = new char[this.colPosition];
                                System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                                this.addColumn(columnIndex++, column, row);
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
                        if (this.position != length) break;
                        this.position = 0;
                    }
                }
            }
        }
        if (EOF) {
            this.colPosition = 0;
            containsSep = this.useEscape && leftLength >= 2 && this.dataBuffer[0] == this.escape;
            if (!containsSep) {
                column = new char[leftLength];
                System.arraycopy(this.dataBuffer, 0, column, 0, leftLength);
            } else {
                for (i = 1; i < leftLength; ++i) {
                    if (this.dataBuffer[i] == this.escape) {
                        if (i == leftLength - 1 || this.dataBuffer[++i] != this.escape) break;
                        this.columnBuffer[this.colPosition++] = this.escape;
                        continue;
                    }
                    this.columnBuffer[this.colPosition++] = this.dataBuffer[i];
                }
                column = new char[this.colPosition];
                System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
            }
            this.addColumn(columnIndex, column, row);
            this.addRow(row);
            row = new ArrayList();
        }
    }

    private void swap(int length) {
        System.arraycopy(this.dataBuffer, this.position, this.dataBuffer, 0, length - this.position);
        this.position = length - this.position;
        this.colPosition = 0;
    }

    private void addColumn(int columnIndex, char[] column, List row) {
        if (column.length != 0) {
            row.add(column);
        } else {
            row.add(null);
        }
    }

    private void addRow(List row) {
        this.rows.add(row);
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public void close() throws IOException {
        this.dataBuffer = null;
        this.columnBuffer = null;
        this.reader.close();
    }

    public String getFileEncoding() {
        if (this.fileEncoding == null) {
            return System.getProperty("file.encoding");
        }
        return this.fileEncoding;
    }

    public boolean hasHeadColumn() {
        return this.hasHeadColumn;
    }
}

