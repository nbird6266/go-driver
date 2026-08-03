/*
 * Decompiled with CFR 0.152.
 */
package org.xerial.snappy;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class SnappyCodec {
    public static final byte[] MAGIC_HEADER = new byte[]{-126, 83, 78, 65, 80, 80, 89, 0};
    public static final int MAGIC_LEN = 8;
    public static final int DEFAULT_VERSION = 1;
    public static final int MINIMUM_COMPATIBLE_VERSION = 1;
    public final byte[] magic;
    public final int version;
    public final int compatibleVersion;

    private SnappyCodec(byte[] magic, int version, int compatibleVersion) {
        this.magic = magic;
        this.version = version;
        this.compatibleVersion = compatibleVersion;
    }

    public String toString() {
        return String.format("version:%d, compatible version:%d", this.version, this.compatibleVersion);
    }

    public static int headerSize() {
        return 16;
    }

    public void writeHeader(OutputStream out) throws IOException {
        ByteArrayOutputStream header = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(header);
        d.write(this.magic, 0, 8);
        d.writeInt(this.version);
        d.writeInt(this.compatibleVersion);
        d.close();
        out.write(header.toByteArray(), 0, header.size());
    }

    public boolean isValidMagicHeader() {
        return Arrays.equals(MAGIC_HEADER, this.magic);
    }

    public static SnappyCodec readHeader(InputStream in) throws IOException {
        DataInputStream d = new DataInputStream(in);
        byte[] magic = new byte[8];
        d.read(magic, 0, 8);
        int version = d.readInt();
        int compatibleVersion = d.readInt();
        return new SnappyCodec(magic, version, compatibleVersion);
    }

    public static SnappyCodec currentHeader() {
        return new SnappyCodec(MAGIC_HEADER, 1, 1);
    }
}

