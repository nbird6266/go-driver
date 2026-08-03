/*
 * Decompiled with CFR 0.152.
 */
package com.google.code.juds;

import com.oscar.Driver;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

public abstract class UnixDomainSocket {
    public static final int SOCK_DGRAM = 0;
    public static final int SOCK_STREAM = 1;
    protected UnixDomainSocketInputStream in;
    protected UnixDomainSocketOutputStream out;
    protected int nativeSocketFileHandle;
    protected String socketFile;
    protected int socketType;
    private int timeout;

    protected static native int nativeCreate(String var0, int var1);

    protected static native int nativeListen(String var0, int var1, int var2);

    protected static native int nativeAccept(int var0, int var1);

    protected static native int nativeOpen(String var0, int var1);

    protected static native int nativeRead(int var0, byte[] var1, int var2, int var3);

    protected static native int nativeWrite(int var0, byte[] var1, int var2, int var3);

    protected static native int nativeClose(int var0);

    protected static native int nativeCloseInput(int var0);

    protected static native int nativeCloseOutput(int var0);

    protected static native int nativeUnlink(String var0);

    protected UnixDomainSocket() {
    }

    protected UnixDomainSocket(int pSocketFileHandle, int pSocketType) throws IOException {
        this.nativeSocketFileHandle = pSocketFileHandle;
        this.socketType = pSocketType;
        this.socketFile = null;
        this.in = new UnixDomainSocketInputStream();
        if (this.socketType == 1) {
            this.out = new UnixDomainSocketOutputStream();
        }
    }

    public InputStream getInputStream() {
        return this.in;
    }

    public OutputStream getOutputStream() {
        return this.out;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void close() {
        UnixDomainSocket.nativeClose(this.nativeSocketFileHandle);
    }

    public void unlink() {
        if (this.socketFile != null) {
            UnixDomainSocket.nativeUnlink(this.socketFile);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String copySo(String arch, String path) throws IOException {
        InputStream is = null;
        FilterOutputStream bos = null;
        File soFile = null;
        try {
            File temp = new File(path);
            File archDir = new File(temp.getParent(), arch);
            if (!archDir.exists()) {
                archDir.mkdirs();
            }
            if (!(soFile = new File(archDir, "libunixdomainsocket.so")).exists()) {
                String srcPath = "/com/google/code/juds/" + arch + "/libunixdomainsocket.so";
                is = UnixDomainSocket.class.getResourceAsStream(srcPath);
                if (is != null) {
                    bos = new BufferedOutputStream(new FileOutputStream(soFile));
                    int len = 0;
                    byte[] buf = new byte[10240];
                    while ((len = is.read(buf)) != -1) {
                        ((BufferedOutputStream)bos).write(buf, 0, len);
                    }
                    ((BufferedOutputStream)bos).flush();
                }
            } else {
                Driver.writeLog("file exists" + soFile.getAbsolutePath());
            }
        }
        finally {
            if (is != null) {
                is.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
        return soFile.getAbsolutePath();
    }

    static {
        URL url = UnixDomainSocket.class.getProtectionDomain().getCodeSource().getLocation();
        String path = "unixdomainsocket";
        try {
            String arch;
            path = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null).getPath();
            if (path != null) {
                if (path.endsWith("/") || path.endsWith("\\")) {
                    path = path.substring(0, path.length() - 1);
                }
                if (path.endsWith(".jar")) {
                    int index = path.lastIndexOf("/");
                    if (index != -1) {
                        path = path.substring(0, index);
                    }
                    path = path + File.separator + "libunixdomainsocket.so";
                } else {
                    path = path + File.separator + ".." + File.separator + "libunixdomainsocket.so";
                }
            }
            if ("aarch64".equals(arch = System.getProperty("os.arch")) || "mips64el".equals(arch) || "sw64".equals(arch) || "i386".equals(arch) || "amd64".equals(arch) || "loongarch64".equals(arch)) {
                path = UnixDomainSocket.copySo(arch, path);
            } else {
                Driver.writeLog("arch not support:" + arch);
            }
            System.load(path);
        }
        catch (Exception e) {
            Driver.writeLog(e);
            System.loadLibrary("unixdomainsocket");
        }
    }

    protected class UnixDomainSocketReadThread
    extends Thread {
        private int count;
        private int off;
        private int len;
        private byte[] b;

        public UnixDomainSocketReadThread(byte[] b, int off, int len) {
            this.b = b;
            this.off = off;
            this.len = len;
        }

        public void run() {
            this.count = UnixDomainSocket.nativeRead(UnixDomainSocket.this.nativeSocketFileHandle, this.b, this.off, this.len);
        }

        public int getData() {
            return this.count;
        }
    }

    protected class UnixDomainSocketOutputStream
    extends OutputStream {
        protected UnixDomainSocketOutputStream() {
        }

        public void write(int b) throws IOException {
            byte[] data = new byte[]{(byte)b};
            if (UnixDomainSocket.nativeWrite(UnixDomainSocket.this.nativeSocketFileHandle, data, 0, 1) != 1) {
                throw new IOException("Unable to write to Unix domain socket");
            }
        }

        public void write(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            }
            if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return;
            }
            if (UnixDomainSocket.nativeWrite(UnixDomainSocket.this.nativeSocketFileHandle, b, off, len) != len) {
                throw new IOException("Unable to write to Unix domain socket");
            }
        }

        public void close() throws IOException {
            UnixDomainSocket.nativeCloseOutput(UnixDomainSocket.this.nativeSocketFileHandle);
        }
    }

    protected class UnixDomainSocketInputStream
    extends InputStream {
        protected UnixDomainSocketInputStream() {
        }

        public int read() throws IOException {
            int count;
            byte[] b = new byte[1];
            if (UnixDomainSocket.this.timeout > 0) {
                UnixDomainSocketReadThread thread = new UnixDomainSocketReadThread(b, 0, 1);
                thread.setDaemon(true);
                thread.start();
                try {
                    thread.join(UnixDomainSocket.this.timeout);
                }
                catch (InterruptedException e) {
                    // empty catch block
                }
                if (thread.isAlive()) {
                    throw new InterruptedIOException("Unix domain socket read() call timed out");
                }
                count = thread.getData();
            } else {
                count = UnixDomainSocket.nativeRead(UnixDomainSocket.this.nativeSocketFileHandle, b, 0, 1);
                if (count == -1) {
                    throw new IOException();
                }
            }
            return count > 0 ? b[0] : -1;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int count;
            if (b == null) {
                throw new NullPointerException();
            }
            if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return 0;
            }
            if (UnixDomainSocket.this.timeout > 0) {
                UnixDomainSocketReadThread thread = new UnixDomainSocketReadThread(b, off, len);
                thread.setDaemon(true);
                thread.start();
                try {
                    thread.join(UnixDomainSocket.this.timeout);
                }
                catch (InterruptedException e) {
                    // empty catch block
                }
                if (thread.isAlive()) {
                    throw new InterruptedIOException("Unix domain socket read() call timed out");
                }
                count = thread.getData();
            } else {
                count = UnixDomainSocket.nativeRead(UnixDomainSocket.this.nativeSocketFileHandle, b, off, len);
                if (count == -1) {
                    throw new IOException();
                }
            }
            return count;
        }

        public void close() throws IOException {
            UnixDomainSocket.nativeCloseInput(UnixDomainSocket.this.nativeSocketFileHandle);
        }
    }
}

