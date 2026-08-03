/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.jdbc;

import com.oscar.Driver;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BlogResultSet {
    protected String curFile;
    protected long curPos;
    protected byte[] curData;
    protected RandomAccessFile output;
    private String exportFile = "tmp.txt";
    private FileChannel fileChannel;
    private ByteBuffer byteBuffer;
    private boolean isExportEnd = false;

    public BlogResultSet(String exportFile) {
        this.exportFile = exportFile;
        this.byteBuffer = ByteBuffer.allocate(0x100000);
    }

    public String getCurFile() {
        return this.curFile;
    }

    public void setCurFile(String curFile) {
        this.curFile = curFile;
    }

    public long getCurPos() {
        return this.curPos;
    }

    public void setCurPos(long curPos) {
        this.curPos = curPos;
    }

    public byte[] getCurData() {
        return this.curData;
    }

    public void setCurData(byte[] curData) {
        this.curData = curData;
        if (this.output == null || this.fileChannel == null) {
            try {
                this.output = new RandomAccessFile(this.exportFile, "rw");
                this.fileChannel = this.output.getChannel();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            boolean again = false;
            do {
                again = false;
                if (curData.length + this.byteBuffer.position() >= this.byteBuffer.capacity()) {
                    if (this.byteBuffer.position() == 0) {
                        this.fileChannel.write(ByteBuffer.wrap(curData));
                        continue;
                    }
                    this.flushBuffer();
                    again = true;
                    continue;
                }
                this.byteBuffer.put(curData);
            } while (again);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void flushBuffer() throws IOException {
        this.byteBuffer.flip();
        while (this.byteBuffer.hasRemaining()) {
            this.fileChannel.write(this.byteBuffer);
        }
        this.byteBuffer.clear();
    }

    public void setOutputFileSeek(long pos) {
        if (this.output != null) {
            try {
                if (this.byteBuffer.position() > 0) {
                    this.flushBuffer();
                }
                this.fileChannel.force(true);
                this.fileChannel.truncate(pos);
                Driver.writeLog("curFile:" + this.curFile + ":::Truncated pos::" + pos);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() {
        this.curData = null;
        if (this.output != null) {
            try {
                if (this.byteBuffer.position() > 0) {
                    this.flushBuffer();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    this.fileChannel.force(true);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    this.fileChannel.close();
                    this.output.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                this.byteBuffer = null;
            }
        }
        this.output = null;
    }

    public boolean getIsExportEnd() {
        return this.isExportEnd;
    }

    public void setIsExportEnd(boolean value) {
        this.isExportEnd = value;
    }
}

