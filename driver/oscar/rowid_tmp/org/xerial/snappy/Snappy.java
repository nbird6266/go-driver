/*
 * Decompiled with CFR 0.152.
 */
package org.xerial.snappy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Properties;
import org.xerial.snappy.SnappyError;
import org.xerial.snappy.SnappyErrorCode;
import org.xerial.snappy.SnappyLoader;
import org.xerial.snappy.SnappyNativeAPI;

public class Snappy {
    private static Object impl;

    public static void arrayCopy(Object src, int offset, int byteLength, Object dest, int dest_offset) throws IOException {
        ((SnappyNativeAPI)impl).arrayCopy(src, offset, byteLength, dest, dest_offset);
    }

    public static byte[] compress(byte[] input) throws IOException {
        return Snappy.rawCompress(input, input.length);
    }

    public static int compress(byte[] input, int inputOffset, int inputLength, byte[] output, int outputOffset) throws IOException {
        return Snappy.rawCompress(input, inputOffset, inputLength, output, outputOffset);
    }

    public static int compress(ByteBuffer uncompressed, ByteBuffer compressed) throws IOException {
        if (!uncompressed.isDirect()) {
            throw new SnappyError(SnappyErrorCode.NOT_A_DIRECT_BUFFER, "input is not a direct buffer");
        }
        if (!compressed.isDirect()) {
            throw new SnappyError(SnappyErrorCode.NOT_A_DIRECT_BUFFER, "destination is not a direct buffer");
        }
        int uPos = uncompressed.position();
        int uLen = uncompressed.remaining();
        int compressedSize = ((SnappyNativeAPI)impl).rawCompress(uncompressed, uPos, uLen, compressed, compressed.position());
        compressed.limit(compressed.position() + compressedSize);
        return compressedSize;
    }

    public static byte[] compress(char[] input) {
        return Snappy.rawCompress(input, input.length * 2);
    }

    public static byte[] compress(double[] input) {
        return Snappy.rawCompress(input, input.length * 8);
    }

    public static byte[] compress(float[] input) {
        return Snappy.rawCompress(input, input.length * 4);
    }

    public static byte[] compress(int[] input) {
        return Snappy.rawCompress(input, input.length * 4);
    }

    public static byte[] compress(long[] input) {
        return Snappy.rawCompress(input, input.length * 8);
    }

    public static byte[] compress(short[] input) {
        return Snappy.rawCompress(input, input.length * 2);
    }

    public static byte[] compress(String s) throws IOException {
        try {
            return Snappy.compress(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoder is not found");
        }
    }

    public static byte[] compress(String s, String encoding) throws UnsupportedEncodingException, IOException {
        byte[] data = s.getBytes(encoding);
        return Snappy.compress(data);
    }

    public static byte[] compress(String s, Charset encoding) throws IOException {
        byte[] data = s.getBytes(encoding);
        return Snappy.compress(data);
    }

    public static String getNativeLibraryVersion() {
        URL versionFile = SnappyLoader.class.getResource("/org/xerial/snappy/VERSION");
        String version = "unknown";
        try {
            if (versionFile != null) {
                Properties versionData = new Properties();
                versionData.load(versionFile.openStream());
                version = versionData.getProperty("version", version);
                if (version.equals("unknown")) {
                    version = versionData.getProperty("VERSION", version);
                }
                version = version.trim().replaceAll("[^0-9\\.]", "");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static boolean isValidCompressedBuffer(byte[] input, int offset, int length) throws IOException {
        if (input == null) {
            throw new NullPointerException("input is null");
        }
        return ((SnappyNativeAPI)impl).isValidCompressedBuffer(input, offset, length);
    }

    public static boolean isValidCompressedBuffer(byte[] input) throws IOException {
        return Snappy.isValidCompressedBuffer(input, 0, input.length);
    }

    public static boolean isValidCompressedBuffer(ByteBuffer compressed) throws IOException {
        return ((SnappyNativeAPI)impl).isValidCompressedBuffer(compressed, compressed.position(), compressed.remaining());
    }

    public static int maxCompressedLength(int byteSize) {
        return ((SnappyNativeAPI)impl).maxCompressedLength(byteSize);
    }

    public static byte[] rawCompress(Object data, int byteSize) {
        byte[] buf = new byte[Snappy.maxCompressedLength(byteSize)];
        int compressedByteSize = ((SnappyNativeAPI)impl).rawCompress(data, 0, byteSize, buf, 0);
        byte[] result = new byte[compressedByteSize];
        System.arraycopy(buf, 0, result, 0, compressedByteSize);
        return result;
    }

    public static int rawCompress(Object input, int inputOffset, int inputLength, byte[] output, int outputOffset) throws IOException {
        if (input == null || output == null) {
            throw new NullPointerException("input or output is null");
        }
        int compressedSize = ((SnappyNativeAPI)impl).rawCompress(input, inputOffset, inputLength, output, outputOffset);
        return compressedSize;
    }

    public static int rawUncompress(byte[] input, int inputOffset, int inputLength, Object output, int outputOffset) throws IOException {
        if (input == null || output == null) {
            throw new NullPointerException("input or output is null");
        }
        return ((SnappyNativeAPI)impl).rawUncompress(input, inputOffset, inputLength, output, outputOffset);
    }

    public static byte[] uncompress(byte[] input) throws IOException {
        byte[] result = new byte[Snappy.uncompressedLength(input)];
        int byteSize = Snappy.uncompress(input, 0, input.length, result, 0);
        return result;
    }

    public static int uncompress(byte[] input, int inputOffset, int inputLength, byte[] output, int outputOffset) throws IOException {
        return Snappy.rawUncompress(input, inputOffset, inputLength, output, outputOffset);
    }

    public static int uncompress(ByteBuffer compressed, ByteBuffer uncompressed) throws IOException {
        if (!compressed.isDirect()) {
            throw new SnappyError(SnappyErrorCode.NOT_A_DIRECT_BUFFER, "input is not a direct buffer");
        }
        if (!uncompressed.isDirect()) {
            throw new SnappyError(SnappyErrorCode.NOT_A_DIRECT_BUFFER, "destination is not a direct buffer");
        }
        int cPos = compressed.position();
        int cLen = compressed.remaining();
        int decompressedSize = ((SnappyNativeAPI)impl).rawUncompress(compressed, cPos, cLen, uncompressed, uncompressed.position());
        uncompressed.limit(uncompressed.position() + decompressedSize);
        return decompressedSize;
    }

    public static char[] uncompressCharArray(byte[] input) throws IOException {
        return Snappy.uncompressCharArray(input, 0, input.length);
    }

    public static char[] uncompressCharArray(byte[] input, int offset, int length) throws IOException {
        int uncompressedLength = Snappy.uncompressedLength(input, offset, length);
        char[] result = new char[uncompressedLength / 2];
        int byteSize = ((SnappyNativeAPI)impl).rawUncompress(input, offset, length, result, 0);
        return result;
    }

    public static double[] uncompressDoubleArray(byte[] input) throws IOException {
        int uncompressedLength = Snappy.uncompressedLength(input, 0, input.length);
        double[] result = new double[uncompressedLength / 8];
        int byteSize = ((SnappyNativeAPI)impl).rawUncompress(input, 0, input.length, result, 0);
        return result;
    }

    public static int uncompressedLength(byte[] input) throws IOException {
        return ((SnappyNativeAPI)impl).uncompressedLength(input, 0, input.length);
    }

    public static int uncompressedLength(byte[] input, int offset, int length) throws IOException {
        if (input == null) {
            throw new NullPointerException("input is null");
        }
        return ((SnappyNativeAPI)impl).uncompressedLength(input, offset, length);
    }

    public static int uncompressedLength(ByteBuffer compressed) throws IOException {
        if (!compressed.isDirect()) {
            throw new SnappyError(SnappyErrorCode.NOT_A_DIRECT_BUFFER, "input is not a direct buffer");
        }
        return ((SnappyNativeAPI)impl).uncompressedLength(compressed, compressed.position(), compressed.remaining());
    }

    public static float[] uncompressFloatArray(byte[] input) throws IOException {
        return Snappy.uncompressFloatArray(input, 0, input.length);
    }

    public static float[] uncompressFloatArray(byte[] input, int offset, int length) throws IOException {
        int uncompressedLength = Snappy.uncompressedLength(input, offset, length);
        float[] result = new float[uncompressedLength / 4];
        int byteSize = ((SnappyNativeAPI)impl).rawUncompress(input, offset, length, result, 0);
        return result;
    }

    public static int[] uncompressIntArray(byte[] input) throws IOException {
        return Snappy.uncompressIntArray(input, 0, input.length);
    }

    public static int[] uncompressIntArray(byte[] input, int offset, int length) throws IOException {
        int uncompressedLength = Snappy.uncompressedLength(input, offset, length);
        int[] result = new int[uncompressedLength / 4];
        int byteSize = ((SnappyNativeAPI)impl).rawUncompress(input, offset, length, result, 0);
        return result;
    }

    public static long[] uncompressLongArray(byte[] input) throws IOException {
        return Snappy.uncompressLongArray(input, 0, input.length);
    }

    public static long[] uncompressLongArray(byte[] input, int offset, int length) throws IOException {
        int uncompressedLength = Snappy.uncompressedLength(input, offset, length);
        long[] result = new long[uncompressedLength / 8];
        int byteSize = ((SnappyNativeAPI)impl).rawUncompress(input, offset, length, result, 0);
        return result;
    }

    public static short[] uncompressShortArray(byte[] input) throws IOException {
        return Snappy.uncompressShortArray(input, 0, input.length);
    }

    public static short[] uncompressShortArray(byte[] input, int offset, int length) throws IOException {
        int uncompressedLength = Snappy.uncompressedLength(input, offset, length);
        short[] result = new short[uncompressedLength / 2];
        int byteSize = ((SnappyNativeAPI)impl).rawUncompress(input, offset, length, result, 0);
        return result;
    }

    public static String uncompressString(byte[] input) throws IOException {
        try {
            return Snappy.uncompressString(input, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 decoder is not found");
        }
    }

    public static String uncompressString(byte[] input, int offset, int length) throws IOException {
        try {
            return Snappy.uncompressString(input, offset, length, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 decoder is not found");
        }
    }

    public static String uncompressString(byte[] input, int offset, int length, String encoding) throws IOException, UnsupportedEncodingException {
        byte[] uncompressed = new byte[Snappy.uncompressedLength(input, offset, length)];
        int compressedSize = Snappy.uncompress(input, offset, length, uncompressed, 0);
        return new String(uncompressed, encoding);
    }

    public static String uncompressString(byte[] input, int offset, int length, Charset encoding) throws IOException, UnsupportedEncodingException {
        byte[] uncompressed = new byte[Snappy.uncompressedLength(input, offset, length)];
        int compressedSize = Snappy.uncompress(input, offset, length, uncompressed, 0);
        return new String(uncompressed, encoding);
    }

    public static String uncompressString(byte[] input, String encoding) throws IOException, UnsupportedEncodingException {
        byte[] uncompressed = Snappy.uncompress(input);
        return new String(uncompressed, encoding);
    }

    public static String uncompressString(byte[] input, Charset encoding) throws IOException, UnsupportedEncodingException {
        byte[] uncompressed = Snappy.uncompress(input);
        return new String(uncompressed, encoding);
    }

    static {
        try {
            impl = SnappyLoader.load();
        }
        catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}

