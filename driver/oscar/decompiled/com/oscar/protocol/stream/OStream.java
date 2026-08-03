/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.stream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.PrivateKey;

public interface OStream {
    public void open() throws IOException;

    public InputStream getInputStream();

    public BufferedOutputStream getBufferedOutputStream();

    public void close() throws IOException;

    public void openWithSSL(String var1, String var2, String var3, String var4, String var5, String var6) throws Exception;

    public void openWithSSLUseWallet(String var1, String var2, String var3, String var4) throws Exception;

    public void readJDCBVerifyKey(String var1, String var2);

    public PrivateKey getJDBCVerifyKey();

    public Socket getSocket() throws Exception;

    public void reInitStream(boolean var1) throws IOException;

    public Socket getCurrentSocket();

    public void setSocketTimeOut(int var1) throws SocketException;

    public void wrapEncryptStream();
}

