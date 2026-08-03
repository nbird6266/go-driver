/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.util;

import com.oscar.util.StreamHandle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class ZipCompressUtil {
    public static byte[] compress(byte[] orgData, int len) {
        ByteArrayOutputStream baOs = new ByteArrayOutputStream();
        DeflaterOutputStream zipOs = null;
        byte[] ret = null;
        try {
            zipOs = new DeflaterOutputStream((OutputStream)baOs, new Deflater());
            zipOs.write(orgData, 0, len);
            zipOs.finish();
        }
        catch (IOException e) {
            throw new Error("compress error, can't process");
        }
        ret = baOs.toByteArray();
        try {
            if (zipOs != null) {
                zipOs.close();
            }
        }
        catch (IOException e) {
            throw new Error("compress error, can't process");
        }
        try {
            if (baOs != null) {
                baOs.close();
            }
        }
        catch (IOException e) {
            throw new Error("compress error, can't process");
        }
        return ret;
    }

    public static byte[] decompress(byte[] compressData) {
        InflaterInputStream zipIs = null;
        ByteArrayOutputStream baOs = new ByteArrayOutputStream();
        byte[] ret = null;
        try {
            zipIs = new InflaterInputStream(new ByteArrayInputStream(compressData), new Inflater());
            StreamHandle.write(baOs, zipIs, -1L, compressData.length);
        }
        catch (IOException e) {
            throw new Error("decompress error, can't process");
        }
        ret = baOs.toByteArray();
        try {
            if (zipIs != null) {
                zipIs.close();
            }
        }
        catch (IOException e) {
            throw new Error("decompress error, can't process");
        }
        try {
            if (baOs != null) {
                baOs.close();
            }
        }
        catch (IOException e) {
            throw new Error("decompress error, can't process");
        }
        return ret;
    }
}

