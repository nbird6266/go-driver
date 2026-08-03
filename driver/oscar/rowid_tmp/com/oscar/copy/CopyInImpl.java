/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.copy;

import com.oscar.copy.CopyIn;
import com.oscar.core.BaseConnection;
import com.oscar.core.ImportHandler;
import com.oscar.jdbc.OscarImportHandler;
import com.oscar.jdbc.OscarJdbc2Connection;
import com.oscar.jdbc.OscarStatement;
import com.oscar.protocol.packets.QueryPacket;
import com.oscar.util.CommandAnalyse;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

public class CopyInImpl
implements CopyIn {
    private static long cacheCount = 200000L;
    private String[] tableName;
    private byte[] colSepByte;
    private byte[] rowSepByte;
    private char escapeChar = (char)34;
    private char escapeCharCommon = (char)92;
    private byte[] columnBuffer = new byte[16384];
    private int colPosition = 0;
    private boolean nullMarked = false;
    private boolean isCheck = false;
    private long bulkCount = 0L;
    private ImportHandler handler;
    private byte[] metaData;
    private String querySql;
    private Statement stmt;
    private boolean isBackup = false;
    private boolean isCsv = false;
    private boolean autoCommit = true;
    private String columnOrder;
    private String[] columns;
    private byte[] nullValue;
    private int dataKind = 0;
    private boolean firstLoad = true;
    private QueryPacket qp;
    private int updateCount = 0;

    public CopyInImpl(Connection conn, String command, String colSep, String rowSep, int bufferSize, boolean autoCommit) throws SQLException {
        byte[] b;
        String escapeString;
        String[] temp;
        if (autoCommit) {
            ((OscarJdbc2Connection)conn).execSQL("CommiT");
        }
        this.colSepByte = colSep.getBytes();
        this.rowSepByte = rowSep.getBytes();
        this.tableName = CommandAnalyse.analyseSchemaTableName(command);
        this.isCheck = CommandAnalyse.analyseCheckConstraints(command);
        this.autoCommit = autoCommit;
        this.columnOrder = CommandAnalyse.analyseColumnOrder(command);
        if (this.columnOrder != null && !this.columnOrder.equals("")) {
            StringTokenizer st = new StringTokenizer(this.columnOrder, ",", false);
            int columnNums = st.countTokens();
            temp = new String[columnNums];
            for (int i = 0; i < columnNums; ++i) {
                temp[i] = st.nextToken();
            }
            this.columns = temp;
        }
        if (!(escapeString = CommandAnalyse.analyseEscapeChar(command)).equals("") && escapeString != null) {
            byte[] temp2 = escapeString.getBytes();
            this.escapeChar = (char)temp2[0];
            this.isCsv = true;
        }
        if ((b = CommandAnalyse.analyseColSep(command)) != null) {
            this.colSepByte = b;
        }
        if ((temp = CommandAnalyse.analyseNullValue(command)) != null) {
            this.nullValue = temp.getBytes();
        }
        if (this.tableName[0] == null || "".equals(this.tableName[0])) {
            this.tableName[0] = this.fetchSchemName((OscarJdbc2Connection)conn);
        }
        this.handler = ((BaseConnection)((Object)conn)).createImportHandler(this.tableName[0], this.tableName[1]);
        if (!"".equals(this.columnOrder)) {
            this.handler.setColumnOrder(this.columnOrder);
        }
        if (this.isCheck) {
            this.handler.setHintParam("(CHECK_CONSTRAINTS)");
        }
        this.handler.setBufferSize(bufferSize);
    }

    public CopyInImpl(Connection conn, String command, String colSep, String rowSep, int bufferSize, boolean autoCommit, int dataKind) throws SQLException {
        this(conn, command, colSep, rowSep, bufferSize, autoCommit);
        this.dataKind = dataKind;
        switch (dataKind) {
            case 1: {
                this.handler.setImportBlockParam(1);
                if (this.isCheck) {
                    this.handler.setHintParam("(CHECK_CONSTRAINTS, multiexectuples = 1)");
                    break;
                }
                this.handler.setHintParam("(multiexectuples = 1)");
                break;
            }
        }
    }

    public CopyInImpl(Connection conn, String command, String colSep, String rowSep) throws SQLException {
        this(conn, command, colSep, rowSep, 10, true);
    }

    public CopyInImpl(Connection conn, String command, String colSep, String rowSep, boolean autoCommit) throws SQLException {
        this(conn, command, colSep, rowSep, 10, autoCommit);
    }

    public CopyInImpl(Connection conn, String command) throws SQLException {
        this.setQuerySql(CommandAnalyse.analyseQuery(command));
        this.stmt = conn.createStatement();
        this.setIsBackup(true);
    }

    public void cancelCopy() throws SQLException {
        if (this.isBackup()) {
            return;
        }
        if (this.handler != null) {
            if (this.handler.getImportStream() != null && !this.handler.getImportStream().isFinished()) {
                this.handler.getImportStream().finished();
            }
            if (this.autoCommit) {
                this.handler.getConnection().execSQL("BegiN");
            }
            this.handler.close();
            this.handler = null;
        }
        this.columnBuffer = null;
    }

    public long endCopy() throws SQLException {
        if (this.isBackup()) {
            return 0L;
        }
        this.handler.execute();
        if (this.autoCommit) {
            this.handler.getConnection().execSQL("BegiN");
        }
        int updateCount = this.handler.getUpdateCount();
        this.handler.close();
        this.handler = null;
        this.columnBuffer = null;
        return updateCount;
    }

    public void writeToCopy(byte[] buffer, int offset, int length) throws SQLException {
        switch (this.dataKind) {
            case 1: {
                if (this.firstLoad) {
                    this.handler.beginRow();
                    this.firstLoad = false;
                }
                if (buffer == null) {
                    this.handler.setObject(1, null);
                } else {
                    this.handler.setBytes(1, buffer);
                }
                this.handler.endRow();
                this.updateCount += this.handler.getUpdateCount();
                ((OscarImportHandler)this.handler).setUpdateCount(this.updateCount);
                return;
            }
            case 2: {
                ((OscarStatement)this.stmt).restoreKstore(this.querySql, this.metaData, buffer);
                return;
            }
        }
        if (this.isCsv) {
            this.writeToCopyCsv(buffer, offset, length);
        } else {
            this.writeToCopyCommon(buffer, offset, length);
        }
    }

    public void writeToCopyCsv(byte[] buffer, int offset, int length) throws SQLException {
        int columnIndex = 1;
        int k = 0;
        byte[] column = new byte[]{};
        boolean isColumn = false;
        boolean isRow = false;
        boolean containsEscape = false;
        boolean isNull = false;
        for (int i = offset; i < length; ++i) {
            int j;
            if (containsEscape) {
                if (buffer[i] == this.escapeChar) {
                    if (buffer[++i] == this.escapeChar) {
                        this.columnBuffer[this.colPosition++] = (byte)this.escapeChar;
                        continue;
                    }
                    containsEscape = false;
                    --i;
                    continue;
                }
                this.columnBuffer[this.colPosition++] = buffer[i];
                continue;
            }
            if (buffer[i] == this.escapeChar) {
                containsEscape = true;
                continue;
            }
            k = i;
            for (j = 0; j < this.colSepByte.length; ++j) {
                if (buffer[i] == this.colSepByte[j]) {
                    ++i;
                } else {
                    isColumn = false;
                    break;
                }
                isColumn = true;
            }
            if (!isColumn) {
                i = k;
                for (j = 0; j < this.nullValue.length; ++j) {
                    if (buffer[i] == this.nullValue[j]) {
                        ++i;
                    } else {
                        isNull = false;
                        break;
                    }
                    isNull = true;
                }
            }
            if (isColumn) {
                column = new byte[this.colPosition];
                System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                this.setColumn(columnIndex++, column);
                column = new byte[]{};
                this.colPosition = 0;
                --i;
                if (null == this.columns || this.columns.length != columnIndex - 1) continue;
                ++i;
                int l = 0;
                while (i < length) {
                    l = i;
                    for (int j2 = 0; j2 < this.rowSepByte.length; ++j2) {
                        if (buffer[i] == this.rowSepByte[j2]) {
                            ++i;
                        } else {
                            isRow = false;
                            i = l;
                            break;
                        }
                        isRow = true;
                    }
                    ++i;
                }
                if (isRow) {
                    --i;
                    column = new byte[]{};
                    this.colPosition = 0;
                    this.handler.endRow();
                    ++this.bulkCount;
                    if (this.bulkCount == cacheCount) {
                        this.handler.getImportStream().flush();
                        this.bulkCount = 0L;
                    }
                    columnIndex = 1;
                    --i;
                    continue;
                }
                i = l;
                continue;
            }
            if (isNull) {
                byte[] b = "".getBytes();
                for (int t = 0; t < b.length; ++t) {
                    this.columnBuffer[this.colPosition++] = b[t];
                }
                --i;
                continue;
            }
            i = k;
            for (j = 0; j < this.rowSepByte.length; ++j) {
                if (buffer[i] == this.rowSepByte[j]) {
                    ++i;
                } else {
                    isRow = false;
                    break;
                }
                isRow = true;
            }
            if (isRow) {
                column = new byte[this.colPosition];
                System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                this.setColumn(columnIndex++, column);
                column = new byte[]{};
                this.colPosition = 0;
                --i;
                this.handler.endRow();
                ++this.bulkCount;
                if (this.bulkCount == cacheCount) {
                    this.handler.getImportStream().flush();
                    this.bulkCount = 0L;
                }
                columnIndex = 1;
                continue;
            }
            i = k;
            this.columnBuffer[this.colPosition++] = buffer[i];
        }
    }

    public void writeToCopyCommon(byte[] buffer, int offset, int length) throws SQLException {
        int columnIndex = 1;
        int k = 0;
        byte[] column = new byte[]{};
        boolean isColumn = false;
        boolean isRow = false;
        boolean containsEscape = false;
        boolean isNull = false;
        for (int i = offset; i < length; ++i) {
            int j;
            int t;
            if (containsEscape) {
                int j2;
                if (buffer[i] == this.escapeCharCommon) {
                    this.columnBuffer[this.colPosition++] = buffer[i];
                    containsEscape = false;
                    continue;
                }
                k = i;
                for (j2 = 0; j2 < this.colSepByte.length; ++j2) {
                    if (buffer[i] == this.colSepByte[j2]) {
                        ++i;
                    } else {
                        isColumn = false;
                        break;
                    }
                    isColumn = true;
                }
                if (isColumn) {
                    for (j2 = 0; j2 < this.colSepByte.length; ++j2) {
                        this.columnBuffer[this.colPosition++] = this.colSepByte[j2];
                    }
                    --i;
                    containsEscape = false;
                    continue;
                }
                i = k;
                this.columnBuffer[this.colPosition++] = buffer[--i];
                containsEscape = false;
                continue;
            }
            if (buffer[i] == this.escapeCharCommon) {
                k = i;
                for (int j3 = 0; j3 < this.nullValue.length; ++j3) {
                    if (buffer[i] == this.nullValue[j3]) {
                        ++i;
                    } else {
                        isNull = false;
                        break;
                    }
                    isNull = true;
                }
                if (isNull) {
                    byte[] b = "".getBytes();
                    for (t = 0; t < b.length; ++t) {
                        this.columnBuffer[this.colPosition++] = b[t];
                    }
                    --i;
                    continue;
                }
                i = k;
                containsEscape = true;
                continue;
            }
            k = i;
            for (j = 0; j < this.colSepByte.length; ++j) {
                if (buffer[i] == this.colSepByte[j]) {
                    ++i;
                } else {
                    isColumn = false;
                    break;
                }
                isColumn = true;
            }
            if (!isColumn) {
                i = k;
                if (i >= buffer.length - this.rowSepByte.length) {
                    for (j = 0; j < this.rowSepByte.length; ++j) {
                        if (buffer[i] == this.rowSepByte[j]) {
                            ++i;
                        } else {
                            isRow = false;
                            break;
                        }
                        isRow = true;
                    }
                }
                if (!isRow) {
                    i = k;
                    for (j = 0; j < this.nullValue.length; ++j) {
                        if (buffer[i] == this.nullValue[j]) {
                            ++i;
                        } else {
                            isNull = false;
                            break;
                        }
                        isNull = true;
                    }
                }
            }
            if (isColumn) {
                column = new byte[this.colPosition];
                System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                if (isNull || this.nullValue.length == 0 && column.length == 0) {
                    this.setColumn(columnIndex++, null);
                } else {
                    this.setColumn(columnIndex++, column);
                }
                column = new byte[]{};
                this.colPosition = 0;
                --i;
                if (null == this.columns || this.columns.length != columnIndex - 1) continue;
                ++i;
                int l = 0;
                while (i < length) {
                    l = i;
                    for (int j4 = 0; j4 < this.rowSepByte.length; ++j4) {
                        if (buffer[i] == this.rowSepByte[j4]) {
                            ++i;
                        } else {
                            isRow = false;
                            i = l;
                            break;
                        }
                        isRow = true;
                    }
                    ++i;
                }
                if (isRow) {
                    --i;
                    column = new byte[]{};
                    this.colPosition = 0;
                    this.handler.endRow();
                    ++this.bulkCount;
                    if (this.bulkCount == cacheCount) {
                        this.handler.getImportStream().flush();
                        this.bulkCount = 0L;
                    }
                    columnIndex = 1;
                    --i;
                    continue;
                }
                i = l;
                continue;
            }
            if (isRow) {
                column = new byte[this.colPosition];
                System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                if (isNull || this.nullValue.length == 0 && column.length == 0) {
                    this.setColumn(columnIndex++, null);
                } else {
                    System.arraycopy(this.columnBuffer, 0, column, 0, this.colPosition);
                    this.setColumn(columnIndex++, column);
                }
                column = new byte[]{};
                this.colPosition = 0;
                --i;
                this.handler.endRow();
                ++this.bulkCount;
                if (this.bulkCount == cacheCount) {
                    this.handler.getImportStream().flush();
                    this.bulkCount = 0L;
                }
                columnIndex = 1;
                continue;
            }
            if (isNull) {
                byte[] b = "".getBytes();
                for (t = 0; t < b.length; ++t) {
                    this.columnBuffer[this.colPosition++] = b[t];
                }
                --i;
                continue;
            }
            i = k;
            this.columnBuffer[this.colPosition++] = buffer[i];
        }
    }

    private void setColumn(int columnIndex, byte[] column) throws SQLException {
        if (!this.nullMarked && column != null) {
            this.handler.setBytes(columnIndex, column);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public String fetchSchemName(OscarJdbc2Connection conn) {
        stmt = null;
        rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(" select NSPNAME from info_schem.sys_namespace n,v_sys_user u where n.NSPOWNER=u.usesysid and u.usename='" + conn.getUserName() + "' order by oid");
            if (!rs.next()) ** GOTO lbl61
            var4_4 = rs.getString(1);
            var6_6 = null;
            if (rs == null) ** GOTO lbl53
            ** GOTO lbl48
        }
        catch (Exception e) {
            e.printStackTrace();
            var6_8 = null;
            if (rs != null) {
                try {
                    rs.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt == null) return null;
            try {
                stmt.close();
                return null;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        {
            block21: {
                block22: {
                    catch (Throwable var5_14) {
                        block20: {
                            var6_9 = null;
                            if (rs != null) {
                                ** try [egrp 2[TRYBLOCK] [5 : 100->109)] { 
lbl36:
                                // 1 sources

                                rs.close();
                                break block20;
lbl38:
                                // 1 sources

                                catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (stmt == null) throw var5_14;
                        ** try [egrp 3[TRYBLOCK] [6 : 120->129)] { 
lbl43:
                        // 1 sources

                        stmt.close();
                        throw var5_14;
lbl45:
                        // 1 sources

                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                        throw var5_14;
                    }
lbl48:
                    // 1 sources

                    ** try [egrp 2[TRYBLOCK] [5 : 100->109)] { 
lbl49:
                    // 1 sources

                    rs.close();
                    break block22;
lbl51:
                    // 1 sources

                    catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (stmt == null) return var4_4;
                try {}
                catch (SQLException e) {
                    e.printStackTrace();
                    return var4_4;
                }
                stmt.close();
                return var4_4;
lbl61:
                // 1 sources

                var6_7 = null;
                if (rs != null) {
                    ** try [egrp 2[TRYBLOCK] [5 : 100->109)] { 
lbl64:
                    // 1 sources

                    rs.close();
                    break block21;
lbl66:
                    // 1 sources

                    catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (stmt == null) return null;
            try {}
            catch (SQLException e) {}
            e.printStackTrace();
            return null;
            stmt.close();
            return null;
        }
    }

    public boolean isNullMarked() {
        return this.nullMarked;
    }

    public void setNullMarked(boolean nullMarked) {
        this.nullMarked = nullMarked;
    }

    public void setMetaData(byte[] metaData) {
        this.metaData = metaData;
    }

    public byte[] getMetaData() {
        return this.metaData;
    }

    public void setQuerySql(String querySql) {
        this.querySql = querySql;
    }

    public String getQuerySql() {
        return this.querySql;
    }

    public void setIsBackup(boolean flag) {
        this.isBackup = flag;
    }

    public boolean isBackup() {
        return this.isBackup;
    }
}

