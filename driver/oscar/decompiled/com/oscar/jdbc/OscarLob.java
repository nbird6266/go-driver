/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.core.BaseConnection;
import com.oscar.fastpath.Fastpath;
import com.oscar.fastpath.FastpathArg;
import com.oscar.util.Hex;
import com.oscar.util.OSQLException;
import com.oscar.util.StreamHandle;
import com.oscar.util.ZipCompressUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

public abstract class OscarLob {
    protected static final long COMPARE_BLOCK_SIZE = 64000L;
    protected static final int DEFAULT_BUFFER_SIZE = 633600;
    protected static final int WRITE = 131072;
    protected static final int READ = 262144;
    protected static final int READWRITE = 393216;
    public static final int MODE_READONLY = 1;
    public static final int MODE_READWRITE = 0;
    public static final int DURATION_CALL = 1;
    public static final int DURATION_SESSION = 0;
    protected Fastpath fastpath;
    protected byte[] locator = new byte[0];
    protected String locatorStr = "";
    protected boolean tempLob = false;
    protected boolean tableIdLob = false;
    protected BaseConnection connection;
    protected boolean emptyLob = false;
    public static final int BLOB_TYPE = 1;
    public static final int CLOB_TYPE = 2;
    public static final int BFILE_TYPE = 3;
    private long lobLength = -1L;
    public int chunkSize = -1;
    private int type;
    private byte[] datas;

    protected OscarLob() {
        this.emptyLob = true;
    }

    public OscarLob(BaseConnection conn, String locatorStr) throws SQLException {
        this(conn);
        if (conn.getProtocolVersion().getProtocolType() >= 4) {
            this.analyzeLocator(locatorStr);
        } else {
            this.setLocatorStr(locatorStr);
        }
    }

    private OscarLob(BaseConnection conn) throws SQLException {
        this.connection = conn;
        this.fastpath = this.connection.getFastpathAPI();
    }

    public OscarLob(BaseConnection conn, boolean cache, int duration) throws SQLException {
        this(conn);
        FastpathArg[] args = null;
        byte[] loc = null;
        switch (conn.getVersion().getMainVersion()) {
            case 1: 
            case 2: 
            case 3: {
                args = new FastpathArg[]{};
                break;
            }
            case 4: {
                args = new FastpathArg[]{new FastpathArg(cache ? 1 : 0), new FastpathArg(duration)};
                break;
            }
        }
        loc = this.fastpath.getData(this.getType(), "CREATETEMP", args);
        this.setLocator(loc);
        this.tempLob = true;
    }

    protected OscarLob(BaseConnection conn, long tableOid, int colIndex) throws SQLException {
        this(conn);
        FastpathArg[] args = null;
        switch (conn.getVersion().getMainVersion()) {
            case 1: {
                this.tempLob = true;
                args = new FastpathArg[]{};
                break;
            }
            case 2: 
            case 3: {
                if (conn.getVersion().isHaveEmptyXlobWithOid()) {
                    args = new FastpathArg[]{new FastpathArg((int)tableOid)};
                    break;
                }
                this.tempLob = true;
                args = new FastpathArg[]{};
                this.tempLob = true;
                break;
            }
            case 4: {
                args = new FastpathArg[]{new FastpathArg((int)tableOid), new FastpathArg(colIndex)};
            }
        }
        byte[] loc = null;
        loc = this.fastpath.getData(this.getType(), "CREATE_WITH_TABLE_OID", args);
        this.setLocator(loc);
        this.tableIdLob = true;
    }

    public InputStream getBinaryStream(long pos) throws SQLException {
        if (this.datas != null) {
            return new ByteArrayInputStream(this.datas, (int)pos - 1, this.datas.length);
        }
        return new LobInputStream(pos);
    }

    public OutputStream setBinaryStream(long pos) throws SQLException {
        this.checkEmptyLob();
        if (pos <= 0L) {
            throw new IllegalArgumentException("wrong parameter");
        }
        return new LobOutputStream(pos);
    }

    public OutputStream setBinaryStream(long pos, int bufferSize) throws SQLException {
        this.checkEmptyLob();
        if (pos <= 0L) {
            throw new IllegalArgumentException("wrong parameter");
        }
        return new LobOutputStream(pos, bufferSize);
    }

    public byte[] getBytes(long pos, int length) throws SQLException {
        byte[] ret = null;
        if (pos <= 0L || length < 0) {
            throw new IllegalArgumentException("wrong parameter");
        }
        InputStream is = this.getBinaryStream(pos);
        try {
            ret = StreamHandle.read(is, length, this.getBufferSize());
        }
        catch (IOException ex) {
            throw new OSQLException("OSCAR-00510", "88888", 510, ex);
        }
        return ret;
    }

    public int setBytes(long pos, byte[] bytes, int offset, int length) throws SQLException {
        if (pos <= 0L) {
            throw new IllegalArgumentException("wrong parameter");
        }
        int writeLen = 0;
        try {
            OutputStream os = this.setBinaryStream(pos);
            writeLen = StreamHandle.write(os, bytes, offset, length, this.getBufferSize());
            os.close();
        }
        catch (IOException ex) {
            throw new OSQLException("OSCAR-00508", "88888", 508, ex);
        }
        return writeLen;
    }

    public abstract long write(long var1, InputStream var3, long var4) throws SQLException;

    public long length() throws SQLException {
        if (this.lobLength < 0L) {
            if (this.isEmptyLob()) {
                this.lobLength = 0L;
            } else {
                long ret = 0L;
                FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.locator)};
                switch (this.connection.getVersion().getMainVersion()) {
                    case 1: 
                    case 2: 
                    case 3: {
                        ret = this.fastpath.getInteger(this.getType(), "GETPRECISELENGTH", args);
                        break;
                    }
                    case 4: {
                        ret = this.fastpath.getLong(this.getType(), "GETPRECISELENGTH", args);
                    }
                }
                this.lobLength = ret;
            }
        }
        return this.lobLength;
    }

    public synchronized void freeTemporary() throws SQLException {
        this.checkEmptyLob();
        if (this.isTempLob()) {
            FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.locator)};
            switch (this.connection.getVersion().getMainVersion()) {
                case 2: 
                case 3: {
                    this.fastpath.getInteger(this.getType(), "FREETEMP", args);
                    break;
                }
                case 4: {
                    byte[] temp = this.fastpath.getData(this.getType(), "FREETEMP", args);
                    this.setLocator(temp);
                }
            }
        }
    }

    public synchronized void freePersist() throws SQLException {
        this.checkEmptyLob();
        FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.locator)};
        switch (this.connection.getVersion().getMainVersion()) {
            case 4: {
                byte[] temp = this.fastpath.getData(this.getType(), "FREE_WITH_TABLE_OID", args);
                this.setLocator(temp);
            }
        }
    }

    public byte[] getLocator() {
        return this.locator;
    }

    public String getLocatorStr() {
        return this.locatorStr;
    }

    public boolean isEmptyLob() {
        return this.emptyLob;
    }

    public boolean isTempLob() {
        return this.tempLob;
    }

    public void checkEmptyLob() throws OSQLException {
        if (this.isEmptyLob()) {
            throw new OSQLException("OSCAR-00506", "88888", 506);
        }
    }

    public boolean isTableIdLob() {
        return this.tableIdLob;
    }

    public void setLocator(byte[] locator) throws OSQLException {
        switch (this.connection.getVersion().getMainVersion()) {
            case 1: 
            case 2: 
            case 3: {
                long oid = 0L;
                for (int i = 0; i < locator.length - 4; ++i) {
                    oid = (oid << 8) + (long)(locator[i] & 0xFF);
                }
                this.locatorStr = "" + oid + (char)locator[locator.length - 4];
                break;
            }
            case 4: {
                this.locatorStr = Hex.hexPrintToString(locator);
                break;
            }
            default: {
                throw new OSQLException("OSCAR-00504", "88888", 504);
            }
        }
        this.locator = locator;
    }

    public void setLocatorStr(String locatorStr) throws OSQLException {
        switch (this.connection.getVersion().getMainVersion()) {
            case 1: 
            case 2: 
            case 3: {
                int i;
                byte[] loc = null;
                try {
                    loc = locatorStr.getBytes("US-ASCII");
                }
                catch (UnsupportedEncodingException ex) {
                    throw new OSQLException("OSCAR-00504", "88888", 504, ex);
                }
                long oid = 0L;
                for (int i2 = 0; i2 < loc.length - 4; ++i2) {
                    if (loc[i2] == 32) continue;
                    oid = oid * 10L + (long)(loc[i2] - 48);
                }
                switch (loc.length) {
                    case 20: {
                        this.locator = new byte[8];
                        break;
                    }
                    case 30: {
                        this.locator = new byte[12];
                        break;
                    }
                    default: {
                        throw new OSQLException("OSCAR-00504", "88888", 504);
                    }
                }
                long val = oid;
                for (i = this.locator.length - 5; i >= 0; --i) {
                    this.locator[i] = (byte)(val & 0xFFL);
                    val >>= 8;
                }
                for (i = 0; i < 4; ++i) {
                    this.locator[this.locator.length - 4 + i] = loc[loc.length - 4 + i];
                }
                locatorStr = locatorStr.trim();
                break;
            }
            case 4: {
                this.locator = Hex.parserStringToByte(locatorStr);
                break;
            }
            default: {
                throw new OSQLException("OSCAR-00504", "88888", 504);
            }
        }
        this.locatorStr = locatorStr;
    }

    public void open(int mode) throws SQLException {
        this.checkEmptyLob();
        FastpathArg[] args = null;
        byte[] loc = null;
        args = new FastpathArg[2];
        args[0] = new FastpathArg(this.locator);
        switch (this.connection.getVersion().getMainVersion()) {
            case 1: 
            case 2: 
            case 3: {
                throw new OSQLException("OSCAR-00509", "88888", 509);
            }
            case 4: {
                args[1] = new FastpathArg(mode);
            }
        }
        loc = this.fastpath.getData(this.getType(), "OPEN", args);
        this.setLocator(loc);
    }

    public boolean isOpen() throws SQLException {
        this.checkEmptyLob();
        FastpathArg[] args = null;
        int ret = -1;
        args = new FastpathArg[]{new FastpathArg(this.locator)};
        switch (this.connection.getVersion().getMainVersion()) {
            case 1: 
            case 2: 
            case 3: {
                throw new OSQLException("OSCAR-00509", "88888", 509);
            }
        }
        ret = this.fastpath.getInteger(this.getType(), "ISOPEN", args);
        return ret != 0;
    }

    public void close() throws SQLException {
        this.checkEmptyLob();
        FastpathArg[] args = null;
        byte[] loc = null;
        args = new FastpathArg[]{new FastpathArg(this.locator)};
        switch (this.connection.getVersion().getMainVersion()) {
            case 1: 
            case 2: 
            case 3: {
                throw new OSQLException("OSCAR-00509", "88888", 509);
            }
        }
        loc = this.fastpath.getData(this.getType(), "CLOSE", args);
        this.setLocator(loc);
    }

    public void trim(long newlen) throws SQLException {
        this.checkEmptyLob();
        if (newlen < 0L) {
            throw new IllegalArgumentException("parameter less than 0");
        }
        long blen = this.length();
        if (blen <= newlen) {
            return;
        }
        FastpathArg[] args = null;
        switch (this.connection.getVersion().getMainVersion()) {
            case 1: 
            case 2: 
            case 3: {
                args = new FastpathArg[]{new FastpathArg(this.locator), new FastpathArg((int)(blen - newlen)), new FastpathArg((int)newlen)};
                this.fastpath.getInteger(this.getType(), "ERASE", args);
                break;
            }
            case 4: {
                args = new FastpathArg[]{new FastpathArg(this.locator), new FastpathArg(newlen)};
                this.fastpath.getInteger(this.getType(), "TRIM", args);
            }
        }
    }

    public void truncate(long len) throws SQLException {
        this.trim(len);
    }

    public boolean equals(Object obj) {
        boolean ret = false;
        if (!(obj instanceof OscarLob) || ((OscarLob)obj).getType() != this.getType()) {
            ret = false;
        } else {
            try {
                OscarLob anotherLob = (OscarLob)obj;
                ret = !this.isEmptyLob() ? (anotherLob.getLocatorStr().equalsIgnoreCase(this.getLocatorStr()) ? true : (this.length() != anotherLob.length() ? false : this.positionInternal(anotherLob, 1L) == 1L)) : anotherLob.isEmptyLob();
            }
            catch (SQLException se) {
                ret = false;
            }
        }
        return ret;
    }

    public abstract int getType();

    public int getBufferSize() throws SQLException {
        int ret = 0;
        if (this.getType() == 3) {
            ret = 633600;
        } else {
            if (this.chunkSize < 0) {
                this.chunkSize = this.getChunkSize();
            }
            ret = this.chunkSize;
        }
        return ret;
    }

    public int getChunkSize() throws SQLException {
        int ret = 0;
        switch (this.connection.getVersion().getMainVersion()) {
            case 1: 
            case 2: 
            case 3: {
                ret = 633600;
                break;
            }
            case 4: {
                FastpathArg[] args = new FastpathArg[]{new FastpathArg(this.getLocator())};
                ret = this.fastpath.getInteger(this.getType(), "GET_CHUNKSIZE", args);
            }
        }
        return ret;
    }

    protected long positionInternal(byte[] pattern, long startPos) throws SQLException {
        long position = -1L;
        if (pattern == null || pattern.length == 0 || startPos <= 0L) {
            throw new OSQLException("OSCAR-00502", "88888", 502);
        }
        if (this.isEmptyLob()) {
            position = -1L;
        } else {
            long leng = this.length();
            if (leng < startPos + (long)pattern.length - 1L) {
                position = -1L;
            } else {
                FastpathArg[] args = new FastpathArg[4];
                args[0] = new FastpathArg(this.locator);
                args[1] = new FastpathArg(pattern);
                switch (this.connection.getVersion().getMainVersion()) {
                    case 1: 
                    case 2: 
                    case 3: {
                        args[2] = new FastpathArg((int)(startPos - 1L));
                        args[3] = new FastpathArg(1);
                        position = this.fastpath.getInteger(this.getType(), "INSTR", args);
                        position = position == 0L ? -1L : position + startPos - 1L;
                        break;
                    }
                    case 4: {
                        args[2] = new FastpathArg(startPos);
                        args[3] = new FastpathArg(1L);
                        position = this.fastpath.getLong(this.getType(), "INSTR", args);
                        position = position == 0L ? -1L : position;
                    }
                }
            }
        }
        return position;
    }

    protected long positionInternal(OscarLob lobPart, long startPos) throws SQLException {
        if (lobPart == null || startPos <= 0L) {
            return -1L;
        }
        long lobSrcLen = this.length();
        long lobPartLen = lobPart.length();
        if (lobSrcLen == 0L || lobPartLen > lobSrcLen - startPos + 1L) {
            return -1L;
        }
        if (lobSrcLen <= 64000L) {
            byte[] partReadBuffer = null;
            partReadBuffer = lobPart.getBytes(1L, (int)lobSrcLen);
            return this.positionInternal(partReadBuffer, startPos);
        }
        long nextComparePos = startPos;
        boolean haveFindPattern = false;
        long ret = -1L;
        while (!haveFindPattern) {
            if (lobSrcLen < lobPartLen + nextComparePos - 1L) {
                return -1L;
            }
            int readPos = 1;
            int readBufferLen = (int)Math.min(64000L, lobSrcLen - (long)readPos);
            byte[] readBuffer = new byte[readBufferLen];
            readBuffer = lobPart.getBytes(readPos, readBufferLen);
            long patternComparePos = this.positionInternal(readBuffer, nextComparePos);
            if (patternComparePos == -1L) {
                return -1L;
            }
            ret = patternComparePos;
            readPos += readBuffer.length;
            nextComparePos = patternComparePos + (long)readBuffer.length;
            boolean innerMatchEnd = true;
            while (innerMatchEnd) {
                if (lobPartLen - (long)readPos <= 0L) {
                    return ret;
                }
                int nextReadBufferLen = (int)Math.min(64000L, lobPartLen - (long)readPos);
                byte[] nextReadBuffer = new byte[nextReadBufferLen];
                nextReadBuffer = lobPart.getBytes(readPos, nextReadBufferLen);
                long nextPatternComparePos = this.positionInternal(nextReadBuffer, nextComparePos);
                if (nextPatternComparePos == nextComparePos) {
                    nextComparePos += (long)nextReadBufferLen;
                    if ((long)(readPos += nextReadBufferLen) != lobPartLen) continue;
                    innerMatchEnd = false;
                    haveFindPattern = true;
                    continue;
                }
                if (nextPatternComparePos == -1L) {
                    return -1L;
                }
                nextComparePos = nextPatternComparePos - (long)readPos;
                innerMatchEnd = false;
            }
        }
        return ret;
    }

    protected int setDataInternal(long pos, byte[] bytes, int len) throws SQLException {
        this.checkEmptyLob();
        FastpathArg[] args = new FastpathArg[4];
        args[0] = new FastpathArg(this.locator);
        args[1] = new FastpathArg(len);
        switch (this.connection.getVersion().getMainVersion()) {
            case 1: 
            case 2: 
            case 3: {
                args[2] = new FastpathArg((int)(pos - 1L));
                break;
            }
            case 4: {
                args[2] = new FastpathArg(pos);
            }
        }
        if (this.connection.isCompressTransfer() && this.getType() == 1) {
            byte[] writeData = ZipCompressUtil.compress(bytes, len);
            args[3] = new FastpathArg(writeData);
            return this.fastpath.getInteger(1, "WRITECOMPRESS", args);
        }
        args[3] = new FastpathArg(bytes);
        return this.fastpath.getInteger(this.getType(), "WRITE", args);
    }

    protected byte[] getDataInternal(long pos, int length) throws SQLException {
        this.checkEmptyLob();
        FastpathArg[] args = new FastpathArg[3];
        args[0] = new FastpathArg(this.locator);
        args[1] = new FastpathArg(length);
        switch (this.connection.getVersion().getMainVersion()) {
            case 1: 
            case 2: 
            case 3: {
                args[2] = new FastpathArg((int)(pos - 1L));
                break;
            }
            case 4: {
                args[2] = new FastpathArg(pos);
            }
        }
        if (this.connection.isCompressTransfer() && this.getType() == 1) {
            byte[] readData = this.fastpath.getData(1, "READCOMPRESS", args);
            return ZipCompressUtil.decompress(readData);
        }
        return this.fastpath.getData(this.getType(), "READ", args);
    }

    public void setTempLob(boolean tempLob) {
        this.tempLob = tempLob;
    }

    private void analyzeLocator(String locatorStr) throws SQLException {
        String[] dataArray = locatorStr.split(",");
        boolean allData = false;
        if (locatorStr != null && dataArray.length > 1) {
            this.setLocatorStr(dataArray[0]);
            long length = -1L;
            for (int i = 1; i < dataArray.length; ++i) {
                String data = dataArray[i];
                String value = data.substring(2);
                if (data.startsWith("S=")) {
                    length = Long.parseLong(value, 10);
                    continue;
                }
                if (data.startsWith("C=")) {
                    this.chunkSize = Integer.parseInt(value, 10);
                    continue;
                }
                if (data.startsWith("t=")) {
                    this.tempLob = !value.equals("0");
                    continue;
                }
                if (data.startsWith("D=")) {
                    allData = true;
                    this.datas = Hex.parserStringToByte(value);
                    continue;
                }
                if (!data.startsWith("T=")) continue;
                if ("0".equals(value)) {
                    this.type = 1;
                    continue;
                }
                if ("1".equals(value)) {
                    this.type = 2;
                    continue;
                }
                if (!"2".equals(value)) continue;
                this.type = 3;
            }
            if (allData) {
                this.lobLength = length;
            }
        } else {
            this.setLocatorStr(locatorStr);
        }
    }

    protected class LobInputStream
    extends InputStream {
        private byte[] buffer = new byte[0];
        private int bufferPos = 0;
        private int bufferSize;
        private long markPos = 0L;
        private long nextReadPos = 1L;
        private boolean closed = false;
        private boolean end = false;

        public LobInputStream() throws SQLException {
            this(oscarLob.getBufferSize(), 1L);
        }

        public LobInputStream(long startPos) throws SQLException {
            this(oscarLob.getBufferSize(), startPos);
        }

        public LobInputStream(int bufferSize, long startPos) {
            this.bufferSize = bufferSize;
            this.nextReadPos = startPos;
        }

        public int read() throws IOException {
            int ret = -1;
            if (this.closed) {
                throw new IOException("oscar.lobstream.closed");
            }
            try {
                if (this.checkBuffer()) {
                    ret = -1;
                } else {
                    ret = this.buffer[this.bufferPos] & 0xFF;
                    ++this.bufferPos;
                }
                return ret;
            }
            catch (SQLException se) {
                throw new IOException(se.toString());
            }
        }

        private boolean checkBuffer() throws SQLException {
            boolean ret = false;
            long totalLength = OscarLob.this.length();
            if ((this.buffer == null || this.buffer.length == 0 || this.bufferPos >= this.buffer.length) && this.nextReadPos <= totalLength) {
                this.buffer = OscarLob.this.getDataInternal(this.nextReadPos, (int)Math.min((long)this.bufferSize, totalLength + 1L - this.nextReadPos));
                if (this.buffer != null) {
                    this.nextReadPos += (long)this.buffer.length;
                }
                this.bufferPos = 0;
            }
            if (this.buffer == null || this.bufferPos >= this.buffer.length) {
                ret = true;
            }
            return ret;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            boolean end = false;
            int readLen = 0;
            if (this.closed) {
                throw new IOException("oscar.lobstream.closed");
            }
            try {
                while (readLen < len && !end) {
                    if (this.checkBuffer()) {
                        end = true;
                        continue;
                    }
                    int nextReadLen = Math.min(len - readLen, this.buffer.length - this.bufferPos);
                    System.arraycopy(this.buffer, this.bufferPos, b, off + readLen, nextReadLen);
                    this.bufferPos += nextReadLen;
                    readLen += nextReadLen;
                }
                if (readLen == 0) {
                    readLen = -1;
                }
                return readLen;
            }
            catch (SQLException se) {
                throw new IOException(se.toString());
            }
        }

        public void close() throws IOException {
            if (this.closed) {
                return;
            }
            this.closed = true;
            this.buffer = null;
        }

        public synchronized void mark(int readlimit) {
            this.markPos = this.nextReadPos - (long)this.buffer.length + (long)this.bufferPos;
        }

        public synchronized void reset() throws IOException {
            if (this.closed) {
                throw new IOException("oscar.blobstream.closed");
            }
            if (this.markPos == 0L) {
                throw new IOException("oscar.blobstream.notmarked");
            }
            this.nextReadPos = this.markPos;
            this.buffer = new byte[0];
            this.bufferPos = 0;
            this.end = false;
        }

        public boolean markSupported() {
            return true;
        }
    }

    protected class LobOutputStream
    extends OutputStream {
        private byte[] buffer;
        private int bufferSize;
        private int bufferPos;
        private long nextWritePos;
        private boolean closed = false;

        public LobOutputStream(long startPos) throws SQLException {
            this(startPos, oscarLob.getBufferSize());
        }

        public LobOutputStream(long startPos, int bufferSize) throws SQLException {
            this.nextWritePos = startPos;
            this.bufferSize = Math.min(bufferSize, OscarLob.this.getBufferSize());
            this.buffer = new byte[this.bufferSize];
            this.bufferPos = 0;
        }

        public void write(int b) throws IOException {
            if (this.closed) {
                throw new IOException("com.blobstream.closed");
            }
            try {
                this.checkBuffer();
                this.buffer[this.bufferPos++] = (byte)b;
            }
            catch (SQLException se) {
                throw new IOException(se.toString());
            }
        }

        public void write(byte[] b, int pos, int length) throws IOException {
            try {
                int nextSendLen;
                int endPos = pos + length;
                for (int writePos = pos; writePos < endPos; writePos += nextSendLen) {
                    this.checkBuffer();
                    nextSendLen = Math.min(endPos - writePos, this.bufferSize - this.bufferPos);
                    System.arraycopy(b, writePos, this.buffer, this.bufferPos, nextSendLen);
                    this.bufferPos += nextSendLen;
                }
            }
            catch (SQLException se) {
                throw new IOException(se.toString());
            }
        }

        private void checkBuffer() throws SQLException {
            if (this.bufferPos >= this.bufferSize) {
                int wsize = OscarLob.this.setDataInternal(this.nextWritePos, this.buffer, this.bufferSize);
                if (this.bufferSize > wsize) {
                    if (wsize == 0) {
                        throw new SQLException("OSCAR-00511", "88888", 511);
                    }
                    byte[] temp = new byte[this.bufferSize - wsize];
                    System.arraycopy(this.buffer, wsize, temp, 0, this.bufferSize - wsize);
                    System.arraycopy(temp, 0, this.buffer, 0, temp.length);
                    this.bufferPos = temp.length;
                } else {
                    this.bufferPos = 0;
                }
                this.nextWritePos += (long)wsize;
            }
        }

        public void flush() throws IOException {
            if (this.closed) {
                throw new IOException("com.blobstream.closed");
            }
            try {
                if (this.bufferPos > 0) {
                    byte[] sendData = null;
                    if (this.bufferPos < this.buffer.length) {
                        sendData = new byte[this.bufferPos];
                        System.arraycopy(this.buffer, 0, sendData, 0, this.bufferPos);
                    } else {
                        sendData = this.buffer;
                    }
                    int wsize = OscarLob.this.setDataInternal(this.nextWritePos, sendData, this.bufferPos);
                    if (wsize < this.bufferPos) {
                        throw new IOException("\u7f13\u51b2\u533a\u4e2d\u7684\u6570\u636e\u53d1\u751f\u4e22\u5931");
                    }
                    this.nextWritePos += (long)this.bufferPos;
                }
                this.bufferPos = 0;
            }
            catch (SQLException se) {
                throw new IOException(se.toString());
            }
        }

        public void close() throws IOException {
            this.flush();
            this.closed = true;
            this.buffer = null;
        }
    }
}

