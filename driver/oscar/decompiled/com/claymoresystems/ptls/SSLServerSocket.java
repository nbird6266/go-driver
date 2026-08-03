/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.cert.X509Cert;
import com.claymoresystems.ptls.SSLContext;
import com.claymoresystems.ptls.SSLSocket;
import com.claymoresystems.sslg.SSLPolicyInt;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class SSLServerSocket
extends ServerSocket {
    SSLContext ctx;

    public SSLServerSocket(SSLContext ctx, Integer port, Integer backlog, InetAddress inetaddr) throws IOException {
        this(ctx, (int)port, (int)backlog, inetaddr);
    }

    public SSLServerSocket(SSLContext ctx, int port, int backlog, InetAddress inetaddr) throws IOException {
        super(port, backlog, inetaddr);
        this.ctx = ctx;
    }

    public SSLServerSocket(SSLContext ctx, int port) throws IOException {
        this(ctx, port, 50);
    }

    public SSLServerSocket(SSLContext ctx, int port, int backlog) throws IOException {
        this(ctx, port, backlog, (InetAddress)null);
    }

    public Socket accept() throws IOException {
        SSLSocket s = new SSLSocket();
        this.implAccept(s);
        s.serverSideInit(this.ctx);
        return s;
    }

    public static void main(String[] args) throws IOException {
        SSLPolicyInt policy = new SSLPolicyInt();
        policy.requireClientAuth(true);
        SSLContext ctx = new SSLContext();
        ctx.setPolicy(policy);
        ctx.loadRootCertificates("root.b64");
        ctx.loadEAYKeyFile("bookdsa.pem", "password");
        SSLServerSocket sock = new SSLServerSocket(ctx, 2311);
        while (true) {
            SSLSocket s = (SSLSocket)sock.accept();
            System.out.println("Cert chain");
            if (policy.requireClientAuthP()) {
                Vector cc = s.getCertificateChain();
                for (int i = 0; i < cc.size(); ++i) {
                    X509Cert cert = (X509Cert)cc.elementAt(i);
                    System.out.println("Issuer " + cert.getIssuerName().getNameString());
                    System.out.println("Subject " + cert.getSubjectName().getNameString());
                    System.out.println("Serial " + cert.getSerial());
                    System.out.println("Validity " + cert.getValidityNotBefore() + "-" + cert.getValidityNotAfter());
                }
            }
            InputStreamReader ir = new InputStreamReader(s.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            OutputStreamWriter or = new OutputStreamWriter(s.getOutputStream());
            BufferedWriter bw = new BufferedWriter(or);
            String req = br.readLine();
            System.out.println(req);
            String rsp = "Server stuff";
            bw.write(rsp, 0, rsp.length());
            bw.flush();
            s.close();
            System.out.println("Success");
        }
    }
}

