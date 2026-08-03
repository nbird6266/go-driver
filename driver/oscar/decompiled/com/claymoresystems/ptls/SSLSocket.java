/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.cert.X509Cert;
import com.claymoresystems.ptls.SSLAlertException;
import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLContext;
import com.claymoresystems.ptls.SocketBasedSocketImpl;
import com.claymoresystems.sslg.SSLPolicyInt;
import com.claymoresystems.sslg.SSLSocketXInt;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

public class SSLSocket
extends Socket
implements SSLSocketXInt {
    SSLConn conn;
    String remote_host;
    int remote_port;

    public SSLSocket(SSLContext ctx, String remote_addr, Integer remote_port) throws UnknownHostException, IOException {
        this(ctx, remote_addr, (int)remote_port);
    }

    public SSLSocket(SSLContext ctx, String remote_addr, int port) throws UnknownHostException, IOException {
        super(remote_addr, port);
        this.remote_host = remote_addr;
        this.remote_port = port;
        this.internalSocket(ctx);
    }

    public SSLSocket(SSLContext ctx, InetAddress addr, int port) throws IOException {
        super(addr, port);
        this.remote_host = addr.toString();
        this.remote_port = port;
        this.internalSocket(ctx);
    }

    public SSLSocket(SSLContext ctx, InetAddress addr, int port, InetAddress localAddr, int localPort) throws IOException {
        super(addr, port, localAddr, localPort);
        this.remote_host = addr.toString();
        this.remote_port = port;
        this.internalSocket(ctx);
    }

    public SSLSocket(SSLContext ctx, String host, int port, InetAddress localAddr, int localPort) throws IOException {
        super(host, port, localAddr, localPort);
        this.remote_host = host;
        this.remote_port = port;
        this.internalSocket(ctx);
    }

    public SSLSocket(SSLContext ctx, InputStream input, OutputStream output, String host, int port, int how) throws IllegalArgumentException, IOException {
        this();
        int how2;
        switch (how) {
            case 1: {
                how2 = 1;
                break;
            }
            case 2: {
                how2 = 2;
                break;
            }
            default: {
                throw new IllegalArgumentException("how value" + how + " not supported");
            }
        }
        this.conn = new SSLConn(this, input, output, ctx, how2);
        this.remote_host = host;
        this.remote_port = port;
        if (this.conn.getPolicy().handshakeOnConnectP()) {
            this.conn.handshake();
        }
    }

    public SSLSocket(SSLContext ctx, Socket sock, String host, int port, int how) throws IllegalArgumentException, IOException {
        super(new SocketBasedSocketImpl(sock));
        int how2;
        switch (how) {
            case 1: {
                how2 = 1;
                break;
            }
            case 2: {
                how2 = 2;
                break;
            }
            default: {
                throw new IllegalArgumentException("how value" + how + " not supported");
            }
        }
        this.conn = new SSLConn(this, sock.getInputStream(), sock.getOutputStream(), ctx, how2);
        this.remote_host = host;
        this.remote_port = port;
        if (this.conn.getPolicy().handshakeOnConnectP()) {
            this.conn.handshake();
        }
    }

    public SSLSocket() {
    }

    void internalSocket(SSLContext ctx) throws IOException {
        this.setTcpNoDelay(true);
        this.conn = new SSLConn(this, super.getInputStream(), super.getOutputStream(), ctx, 1);
        if (this.conn.getPolicy().handshakeOnConnectP()) {
            this.conn.handshake();
        }
    }

    void serverSideInit(SSLContext ctx) throws IOException {
        this.conn = new SSLConn(this, super.getInputStream(), super.getOutputStream(), ctx, 2);
        if (this.conn.getPolicy().handshakeOnConnectP()) {
            this.conn.handshake();
        }
    }

    public void hardClose() throws IOException {
        if (this.conn.s != null) {
            super.close();
        }
    }

    public void close() throws IOException {
        if (this.conn != null) {
            this.conn.close();
            this.hardClose();
        }
    }

    public InputStream getInputStream() {
        return this.conn.getInStream();
    }

    public OutputStream getOutputStream() {
        return this.conn.getOutStream();
    }

    public String toString() {
        return "SSL: " + super.toString();
    }

    public int getCipherSuite() throws IOException {
        return this.conn.getCipherSuite();
    }

    public Vector getCertificateChain() throws IOException {
        return this.conn.getCertificateChain();
    }

    public byte[] getSessionID() throws IOException {
        return this.conn.getSessionID();
    }

    public SSLPolicyInt getPolicy() {
        return this.conn.getPolicy();
    }

    public int getVersion() throws IOException {
        return this.conn.getVersion();
    }

    public void handshake() throws IOException {
        this.conn.handshake();
    }

    public void renegotiate(SSLPolicyInt policy) throws IOException {
        this.conn.renegotiate(policy);
    }

    public void renegotiate() throws IOException {
        this.conn.renegotiate(this.conn.getPolicy());
    }

    public void sendClose() throws IOException {
        this.conn.sendClose();
    }

    public void waitForClose(boolean enforceFinished) throws IOException {
        this.conn.recvClose(enforceFinished);
    }

    private static void testConn(SSLContext c, String host, String port) throws IOException, UnknownHostException {
        try {
            String str;
            SSLSocket s = new SSLSocket(c, host, Integer.parseInt(port));
            Vector cc = s.getCertificateChain();
            if (cc != null) {
                System.out.println("Cert chain");
                for (int i = 0; i < cc.size(); ++i) {
                    X509Cert cert = (X509Cert)cc.elementAt(i);
                    System.out.println("Issuer " + cert.getIssuerName().getNameString());
                    System.out.println("Subject " + cert.getSubjectName().getNameString());
                    System.out.println("Serial " + cert.getSerial());
                    System.out.println("Validity " + cert.getValidityNotBefore() + "-" + cert.getValidityNotAfter());
                }
            }
            byte[] buf = new byte[4096];
            InputStreamReader ir = new InputStreamReader(s.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            OutputStreamWriter or = new OutputStreamWriter(s.getOutputStream());
            BufferedWriter bw = new BufferedWriter(or);
            bw.write("Test string", 0, 11);
            bw.newLine();
            bw.newLine();
            bw.flush();
            while ((str = br.readLine()) != null) {
                System.out.println(str);
            }
            s.close();
        }
        catch (SSLAlertException e) {
            throw new Error(e.toString());
        }
    }

    public void _stompOutputStream(OutputStream out) {
        this.conn._sock_out = out;
        this.conn.sock_out = new BufferedOutputStream(out);
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        String host = "localhost";
        String port = "4433";
        if (args.length == 2) {
            host = args[0];
            port = args[1];
        }
        SSLContext c = new SSLContext();
        c.loadRootCertificates("root.b64");
        c.loadEAYKeyFile("bookdsa.pem", "password");
        System.out.println("Trying 1");
        SSLSocket.testConn(c, host, port);
        System.out.println("Trying 2");
        SSLSocket.testConn(c, host, port);
    }
}

