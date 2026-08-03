/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.copy;

import com.oscar.copy.CopyOut;
import com.oscar.core.BaseStatement;
import com.oscar.jdbc.OscarResultSet;
import com.oscar.jdbc.OscarStatement;
import com.oscar.util.CommandAnalyse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CopyOutImpl
implements CopyOut {
    private String querySql;
    private byte[] colSepByte;
    private byte[] rowSepByte;
    private char escapeChar = (char)34;
    private byte[] nullValue;
    private char escapeCharCommon = (char)92;
    private boolean isBackup = false;
    private boolean isCsv = false;
    private ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    private byte[] columnBuffer = new byte[16384];
    private int colPosition = 0;
    private ResultSet rs;
    private PreparedStatement pstmt;
    private Statement stmt;
    private boolean initResultSet = false;
    private int columnCount = 0;
    private byte[] backupMetaData;
    private byte[] backupPhysicalData;
    private int dataKind;
    private byte[] currentRow;

    public CopyOutImpl(Connection conn, String command, String colSep, String rowSep, int rows, int dataKind, int hashBuckets, int[] hashColIds) throws SQLException {
        this(conn, command, colSep, rowSep, rows);
        this.dataKind = dataKind;
        switch (dataKind) {
            case 1: {
                ((BaseStatement)((Object)this.pstmt)).setBuckets(hashBuckets);
                ((BaseStatement)((Object)this.pstmt)).setHashColIds(hashColIds);
                break;
            }
        }
    }

    public CopyOutImpl(Connection conn, String command, String colSep, String rowSep, int rows) throws SQLException {
        String escapeString;
        this.colSepByte = colSep.getBytes();
        this.rowSepByte = rowSep.getBytes();
        String nullValueTemp = CommandAnalyse.analyseNullValue(command);
        this.nullValue = nullValueTemp.getBytes();
        this.querySql = CommandAnalyse.analyseQuery(command);
        byte[] b = CommandAnalyse.analyseColSep(command);
        if (b != null) {
            this.colSepByte = b;
        }
        if (!(escapeString = CommandAnalyse.analyseEscapeChar(command)).equals("") && escapeString != null) {
            byte[] temp = escapeString.getBytes();
            this.escapeChar = (char)temp[0];
            this.isCsv = true;
        }
        this.pstmt = conn.prepareStatement(this.querySql);
        this.pstmt.setFetchSize(rows);
    }

    public CopyOutImpl(Connection conn, String command, String colSep, String rowSep) throws SQLException {
        this(conn, command, colSep, rowSep, 0);
    }

    public CopyOutImpl(Connection conn, String command) throws SQLException {
        this.querySql = CommandAnalyse.analyseQuery(command);
        this.stmt = conn.createStatement();
        byte[] datas = ((OscarStatement)this.stmt).backupKstore(this.querySql);
        if (datas != null) {
            this.backupMetaData = datas;
        }
        this.setIsBackup(true);
    }

    public synchronized void cancelCopy() throws SQLException {
        this.columnBuffer = null;
        if (this.rs != null) {
            this.rs.close();
        }
        if (this.pstmt != null) {
            this.pstmt.cancel();
            this.pstmt.close();
        }
        try {
            if (this.out != null) {
                this.out.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public int getFieldCount() throws SQLException {
        PreparedStatement preparedStatement = this.pstmt;
        synchronized (preparedStatement) {
            if (!this.initResultSet) {
                this.initResultSet = true;
                this.rs = this.pstmt.executeQuery();
                if (this.rs == null) return this.columnCount;
                this.columnCount = this.rs.getMetaData().getColumnCount();
            } else {
                if (this.rs != null) return this.columnCount;
                throw new SQLException("canceled");
            }
            return this.columnCount;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public byte[] readFromCopy() throws SQLException {
        switch (this.dataKind) {
            case 1: {
                PreparedStatement preparedStatement = this.pstmt;
                synchronized (preparedStatement) {
                    if (!this.initResultSet) {
                        this.initResultSet = true;
                        this.rs = this.pstmt.executeQuery();
                        if (this.rs == null) {
                            return null;
                        }
                        this.columnCount = this.rs.getMetaData().getColumnCount();
                    } else if (this.rs == null) {
                        throw new SQLException("canceled");
                    }
                    this.currentRow = (byte[])(this.rs.next() ? ((OscarResultSet)this.rs).getCurrentBlock() : null);
                }
                return this.currentRow;
            }
            case 2: {
                if (this.getBackUpMetadata() != null) {
                    this.backupPhysicalData = this.getNextBackUpPhysicalRowData();
                    return this.backupPhysicalData;
                }
                return null;
            }
        }
        PreparedStatement preparedStatement = this.pstmt;
        synchronized (preparedStatement) {
            if (!this.initResultSet) {
                this.initResultSet = true;
                this.rs = this.pstmt.executeQuery();
                if (this.rs == null) {
                    return null;
                }
                this.columnCount = this.rs.getMetaData().getColumnCount();
            } else if (this.rs == null) {
                throw new SQLException("canceled");
            }
        }
        if (this.isCsv) {
            return this.readFromCopyCsv();
        }
        return this.readFromCopyCommon();
    }

    private synchronized byte[] readFromCopyCsv() throws SQLException {
        if (this.rs != null && this.rs.next()) {
            try {
                byte[][] row = ((OscarResultSet)this.rs).getCurrentRow();
                boolean containsEscape = false;
                boolean containsColumn = false;
                int length = row.length;
                for (int i = 0; i < length; ++i) {
                    if (row[i] != null) {
                        int columnLength = row[i].length;
                        for (int j = 0; j < columnLength; ++j) {
                            int t;
                            if (row[i][j] == this.escapeChar) {
                                containsEscape = true;
                                this.columnBuffer[this.colPosition++] = row[i][j];
                                this.columnBuffer[this.colPosition++] = row[i][j];
                                continue;
                            }
                            int k = j;
                            if (containsEscape) {
                                this.columnBuffer[this.colPosition++] = row[i][j];
                                continue;
                            }
                            if (columnLength - j >= this.colSepByte.length) {
                                t = 0;
                                while (t < this.colSepByte.length) {
                                    if (this.colSepByte[t] != row[i][j]) {
                                        j = k;
                                        break;
                                    }
                                    if (t == this.colSepByte.length - 1) {
                                        containsColumn = true;
                                        containsEscape = true;
                                        break;
                                    }
                                    ++t;
                                    ++j;
                                }
                            }
                            if (!containsColumn && columnLength - j >= this.rowSepByte.length) {
                                t = 0;
                                while (t < this.rowSepByte.length) {
                                    if (this.rowSepByte[t] != row[i][j]) {
                                        j = k;
                                        break;
                                    }
                                    if (t == this.rowSepByte.length - 1) {
                                        containsEscape = true;
                                        break;
                                    }
                                    ++t;
                                    ++j;
                                }
                            }
                            if (k == j) {
                                this.columnBuffer[this.colPosition++] = row[i][j];
                                continue;
                            }
                            System.arraycopy(row[i], k, this.columnBuffer, this.colPosition, j - k + 1);
                        }
                        if (containsEscape) {
                            this.writeColumn2(this.columnBuffer, 0, this.colPosition);
                        } else {
                            this.writeColumn(this.columnBuffer, 0, this.colPosition);
                        }
                        this.colPosition = 0;
                        containsColumn = false;
                        containsEscape = false;
                    }
                    if (i >= length - 1) continue;
                    this.nextColumnByte();
                }
                this.nextRowByte();
                this.flush();
                byte[] byArray = this.out.toByteArray();
                Object var11_12 = null;
                this.out.reset();
                return byArray;
            }
            catch (IOException e) {
                try {
                    throw new SQLException("IOException occured!");
                }
                catch (Throwable throwable) {
                    Object var11_13 = null;
                    this.out.reset();
                    throw throwable;
                }
            }
        }
        return null;
    }

    private synchronized byte[] readFromCopyCommon() throws SQLException {
        if (this.rs != null && this.rs.next()) {
            try {
                byte[][] row = ((OscarResultSet)this.rs).getCurrentRow();
                int length = row.length;
                for (int i = 0; i < length; ++i) {
                    if (row[i] != null) {
                        int columnLength = row[i].length;
                        block5: for (int j = 0; j < columnLength; ++j) {
                            int m;
                            if (row[i][j] == this.escapeCharCommon) {
                                if (j != columnLength - 1) {
                                    if (row[i][j + 1] == this.escapeCharCommon) {
                                        this.columnBuffer[this.colPosition++] = row[i][j];
                                        this.columnBuffer[this.colPosition++] = row[i][j];
                                        continue;
                                    }
                                    for (m = 0; m < this.colSepByte.length; ++m) {
                                        if (row[i][j + 1] == this.colSepByte[m]) {
                                            this.columnBuffer[this.colPosition++] = (byte)this.escapeCharCommon;
                                            this.columnBuffer[this.colPosition++] = row[i][j];
                                            continue block5;
                                        }
                                        if (m != this.colSepByte.length - 1) continue;
                                        this.columnBuffer[this.colPosition++] = row[i][j];
                                    }
                                    continue;
                                }
                                if (i != length - 1) {
                                    this.columnBuffer[this.colPosition++] = row[i][j];
                                    this.columnBuffer[this.colPosition++] = row[i][j];
                                    continue;
                                }
                                this.columnBuffer[this.colPosition++] = row[i][j];
                                continue;
                            }
                            for (m = 0; m < this.colSepByte.length; ++m) {
                                if (row[i][j] == this.colSepByte[m]) {
                                    this.columnBuffer[this.colPosition++] = (byte)this.escapeCharCommon;
                                    this.columnBuffer[this.colPosition++] = row[i][j];
                                    continue block5;
                                }
                                if (m != this.colSepByte.length - 1) continue;
                                this.columnBuffer[this.colPosition++] = row[i][j];
                            }
                        }
                        this.writeColumn(this.columnBuffer, 0, this.colPosition);
                        this.colPosition = 0;
                    } else {
                        for (int j = 0; j < this.nullValue.length; ++j) {
                            this.columnBuffer[this.colPosition++] = this.nullValue[j];
                        }
                        this.writeColumn(this.columnBuffer, 0, this.colPosition);
                        this.colPosition = 0;
                    }
                    if (i >= length - 1) continue;
                    this.nextColumnByte();
                }
                this.nextRowByte();
                this.flush();
                byte[] byArray = this.out.toByteArray();
                Object var8_9 = null;
                this.out.reset();
                return byArray;
            }
            catch (IOException e) {
                try {
                    throw new SQLException("IOException occured!");
                }
                catch (Throwable throwable) {
                    Object var8_10 = null;
                    this.out.reset();
                    throw throwable;
                }
            }
        }
        return null;
    }

    public void writeColumn(byte columnValue) throws IOException {
        this.out.write(columnValue);
    }

    public void writeColumn(byte[] columnValue) throws IOException {
        if (columnValue != null) {
            this.out.write(columnValue);
        }
    }

    public void writeColumn(byte[] columnValue, int start, int length) {
        if (columnValue != null) {
            this.out.write(columnValue, start, length);
        }
    }

    public void writeColumn2(byte[] columnValue, int start, int length) {
        if (columnValue != null) {
            this.out.write((byte)this.escapeChar);
            this.out.write(columnValue, start, length);
            this.out.write((byte)this.escapeChar);
        }
    }

    public void nextColumnByte() throws IOException {
        this.out.write(this.colSepByte);
    }

    public void nextRowByte() throws IOException {
        this.out.write(this.rowSepByte);
    }

    public void flush() throws IOException {
        this.out.flush();
    }

    public void setIsBackup(boolean flag) {
        this.isBackup = flag;
    }

    public boolean isBackup() {
        return this.isBackup;
    }

    public byte[] getBackUpMetadata() {
        return this.backupMetaData;
    }

    public byte[] getBackUpPhysicalRowData() {
        return this.backupPhysicalData;
    }

    public byte[] getNextBackUpPhysicalRowData() throws SQLException {
        return ((OscarStatement)this.stmt).getNextPhysicalDataRow();
    }

    public int getKind() {
        return this.dataKind;
    }

    public byte[] getHash() throws SQLException {
        byte[] hashValue = new byte[]{};
        System.arraycopy(this.currentRow, 0, hashValue, 0, 4);
        return hashValue;
    }

    public int getHashIntValue() throws SQLException {
        byte[] hashValue = new byte[]{};
        System.arraycopy(this.currentRow, 0, hashValue, 0, 4);
        return this.convertByteToInt(hashValue);
    }

    public byte[] getCurrentBlockData() throws SQLException {
        return this.currentRow;
    }

    private int convertByteToInt(byte[] buf) {
        int siz = 4;
        int n = 0;
        for (int i = 0; i < siz; ++i) {
            int b = buf[i] & 0xFF;
            n = b | n << 8;
        }
        return n;
    }
}

