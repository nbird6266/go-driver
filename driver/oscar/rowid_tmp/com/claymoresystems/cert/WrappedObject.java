/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.cert;

import cryptix.util.mime.Base64InputStream;
import cryptix.util.mime.Base64OutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class WrappedObject {
    public static boolean findObject(BufferedReader br, String end, StringBuffer type) throws IOException {
        String line;
        String postfix;
        String prefix = "-----BEGIN ";
        String string = postfix = end == null ? "-----" : end + "-----";
        do {
            if ((line = br.readLine()) != null) continue;
            return false;
        } while (!line.startsWith(prefix) || !line.endsWith(postfix));
        if (type != null) {
            type.setLength(0);
            type.append(line.toString().substring(prefix.length(), line.length() - postfix.length()).trim());
        }
        return true;
    }

    public static byte[] readBlock(BufferedReader br) throws IOException {
        String line;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter ow = new OutputStreamWriter(bos);
        while ((line = br.readLine()) != null && !line.startsWith("-----END ")) {
            ow.write(line, 0, line.length());
        }
        ow.flush();
        byte[] b64data = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(b64data);
        Base64InputStream b64is = new Base64InputStream(bis);
        byte[] data = new byte[b64data.length];
        int dlen = b64is.read(data);
        byte[] trimmeddata = new byte[dlen];
        System.arraycopy(data, 0, trimmeddata, 0, dlen);
        return trimmeddata;
    }

    public static void writeHeader(String type, BufferedWriter out) throws IOException {
        String start = "-----BEGIN " + type + "-----";
        out.write(start);
        out.newLine();
    }

    public static void writeObject(byte[] object, String type, BufferedWriter out) throws IOException {
        String line;
        String finish = "-----END " + type + "-----";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Base64OutputStream b64os = new Base64OutputStream(bos);
        b64os.write(object);
        b64os.flush();
        b64os.close();
        byte[] objEnc = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(objEnc);
        InputStreamReader ir = new InputStreamReader(bis);
        BufferedReader r = new BufferedReader(ir);
        while ((line = r.readLine()) != null) {
            out.write(line);
            out.newLine();
        }
        out.write(finish);
        out.newLine();
        out.flush();
    }

    public static byte[] loadObject(BufferedReader rdr, String end, StringBuffer type) throws IOException {
        if (WrappedObject.findObject(rdr, end, type)) {
            return WrappedObject.readBlock(rdr);
        }
        return null;
    }

    public static String base64Encode(byte[] in) throws IOException {
        String line;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Base64OutputStream b64os = new Base64OutputStream(bos);
        b64os.write(in);
        b64os.flush();
        b64os.close();
        byte[] enc = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(enc);
        InputStreamReader ir = new InputStreamReader(bis);
        BufferedReader r = new BufferedReader(ir);
        StringBuffer sb = new StringBuffer();
        while ((line = r.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static byte[] base64Decode(String in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter ow = new OutputStreamWriter(bos);
        ow.write(in);
        byte[] b64data = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(b64data);
        Base64InputStream b64is = new Base64InputStream(bis);
        byte[] data = new byte[b64data.length];
        int dlen = b64is.read(data);
        byte[] trimmeddata = new byte[dlen];
        System.arraycopy(data, 0, trimmeddata, 0, dlen);
        return trimmeddata;
    }
}

