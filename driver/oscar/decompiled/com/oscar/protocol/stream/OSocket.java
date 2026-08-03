/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol.stream;

import com.claymoresystems.ptls.SSLContext;
import com.claymoresystems.ptls.SSLSocket;
import com.claymoresystems.sslg.Certificate;
import com.claymoresystems.sslg.DistinguishedName;
import com.google.code.juds.UnixDomainSocketClient;
import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.crypt.PrivateKeyConversion;
import com.oscar.crypt.PrivateKeyReader;
import com.oscar.protocol.stream.CompressedInputStream;
import com.oscar.protocol.stream.HdDecryptInputStream;
import com.oscar.protocol.stream.HdEncryptOutputStream;
import com.oscar.protocol.stream.OStream;
import com.oscar.util.OSQLException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Vector;

public class OSocket
implements OStream {
    private String host;
    private int port;
    private Socket connection;
    private InputStream osr_input;
    private BufferedOutputStream osr_output;
    private String rootFile;
    private String randomFile;
    private String database;
    private int timeOut_MilliSecond = 0;
    private BaseConnection con;
    private boolean tcpKeepAlive = false;
    private PrivateKey keyForVerifyJDBC = null;
    private boolean isSSLCon;
    public static Integer openSocketNum = new Integer(0);
    public static Integer closeSocketNum = new Integer(0);
    private String unixDomainPath = "";
    private UnixDomainSocketClient unixConnection;

    public OSocket(String p_host, int p_port) {
        this.host = p_host;
        this.port = p_port;
    }

    public OSocket(String p_host, int p_port, Properties _info) {
        this.host = p_host;
        this.port = p_port;
        this.tcpKeepAlive = Boolean.valueOf(_info.getProperty("TCPKEEPALIVE", "false"));
        this.unixDomainPath = _info.getProperty("UNIXDOMAINPATH", "/tmp/");
    }

    public OSocket(String p_host, int p_port, int requestTimeOut) {
        this.host = p_host;
        this.port = p_port;
        this.timeOut_MilliSecond = requestTimeOut;
    }

    public OSocket(String p_host, int p_port, int requestTimeOut, Properties _info) {
        this.host = p_host;
        this.port = p_port;
        this.timeOut_MilliSecond = requestTimeOut;
        this.tcpKeepAlive = Boolean.valueOf(_info.getProperty("TCPKEEPALIVE", "false"));
        this.unixDomainPath = _info.getProperty("UNIXDOMAINPATH", "/tmp/");
    }

    public OSocket(String p_host, int p_port, BaseConnection con) {
        this.host = p_host;
        this.port = p_port;
        this.con = con;
    }

    public OSocket(String p_host, int p_port, BaseConnection con, Properties _info) {
        this.host = p_host;
        this.port = p_port;
        this.con = con;
        this.tcpKeepAlive = Boolean.valueOf(_info.getProperty("TCPKEEPALIVE", "false"));
        this.unixDomainPath = _info.getProperty("UNIXDOMAINPATH", "/tmp/");
    }

    public OSocket(String p_host, int p_port, int requestTimeOut, BaseConnection con) {
        this.host = p_host;
        this.port = p_port;
        this.timeOut_MilliSecond = requestTimeOut;
        this.con = con;
    }

    public OSocket(String p_host, int p_port, int requestTimeOut, BaseConnection con, Properties _info) {
        this.host = p_host;
        this.port = p_port;
        this.timeOut_MilliSecond = requestTimeOut;
        this.con = con;
        this.tcpKeepAlive = Boolean.valueOf(_info.getProperty("TCPKEEPALIVE", "false"));
        this.unixDomainPath = _info.getProperty("UNIXDOMAINPATH", "/tmp/");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void open() throws IOException {
        if (this.host.equalsIgnoreCase("unixsocket") || this.host.equalsIgnoreCase("memory")) {
            if (System.getProperty("os.name", "window").toLowerCase().indexOf("window") != -1) {
                throw new SocketException("loadLibrary(): Unix sockets are not supported on Windows platforms");
            }
            try {
                this.unixConnection = new UnixDomainSocketClient(this.unixDomainPath.equals("") ? "/tmp/" : this.unixDomainPath + "/" + ".s.oscar." + this.port, 1);
                if (this.timeOut_MilliSecond != 0) {
                    this.unixConnection.setTimeout(this.timeOut_MilliSecond);
                }
                Integer n = openSocketNum;
                synchronized (n) {
                    openSocketNum = new Integer(openSocketNum + 1);
                }
                this.osr_input = new BufferedInputStream(this.unixConnection.getInputStream(), 8192);
                this.osr_output = new BufferedOutputStream(this.unixConnection.getOutputStream(), 8192);
            }
            catch (Exception e) {
                Driver.writeLog(e);
                throw new IOException(e.getMessage());
            }
            catch (Throwable e) {
                Driver.writeLog(e);
                throw new IOException(e.getMessage());
            }
        }
        this.connection = new Socket(this.host, this.port);
        this.connection.setTcpNoDelay(true);
        this.connection.setKeepAlive(this.tcpKeepAlive);
        if (this.timeOut_MilliSecond != 0) {
            this.connection.setSoTimeout(this.timeOut_MilliSecond);
        }
        Integer n = openSocketNum;
        synchronized (n) {
            openSocketNum = new Integer(openSocketNum + 1);
        }
        this.osr_input = new BufferedInputStream(this.connection.getInputStream(), 8192);
        this.osr_output = new BufferedOutputStream(this.connection.getOutputStream(), 8192);
    }

    public void reInitStream(boolean newProtocol) throws IOException {
        if (newProtocol) {
            this.osr_input = new CompressedInputStream(this.osr_input, this.con);
        }
    }

    public void openWithSSL(String keyfile, String certfile, String password, String rootfile, String randomfile, String database) throws Exception {
        this.rootFile = rootfile;
        this.randomFile = randomfile;
        this.database = database;
        SSLContext ctx = this.createSSLContext(keyfile, certfile, password);
        this.isSSLCon = true;
        this.connection = this.connect(ctx, this.host, this.port);
        this.osr_input = new BufferedInputStream(this.connection.getInputStream(), 8192);
        this.osr_output = new BufferedOutputStream(this.connection.getOutputStream(), 8192);
    }

    public InputStream getInputStream() {
        return this.osr_input;
    }

    public BufferedOutputStream getBufferedOutputStream() {
        return this.osr_output;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() throws IOException {
        if (this.osr_output != null) {
            this.osr_output.close();
        }
        if (this.osr_input != null) {
            this.osr_input.close();
        }
        if (this.connection != null) {
            this.connection.close();
        }
        Integer n = closeSocketNum;
        synchronized (n) {
            closeSocketNum = new Integer(closeSocketNum + 1);
        }
    }

    public SSLContext createSSLContext(String keyfile, String certfile, String password) throws Exception {
        SSLContext ctx = new SSLContext();
        try {
            ctx.loadRootCertificates(this.rootFile);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("\u52a0\u8f7dCA\u8bc1\u4e66\u5931\u8d25");
        }
        try {
            FileInputStream keyfileStream = new FileInputStream(keyfile);
            ctx.LoadKeyFile(keyfileStream, password);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("\u52a0\u8f7d\u8bc1\u4e66\u79c1\u94a5\u53ca\u5176\u5bc6\u7801\u5931\u8d25!");
        }
        try {
            FileInputStream certfileStream = new FileInputStream(certfile);
            ctx.LoadCertFile(certfileStream, password);
        }
        catch (Throwable e) {
            throw new Exception("\u52a0\u8f7d\u8bc1\u4e66\u6587\u4ef6\u5931\u8d25!");
        }
        switch (ctx.checkKeyPair()) {
            case 0: {
                break;
            }
            case 1: {
                throw new Exception("\u516c\u79c1\u94a5\u6570\u503c\u4e0d\u5339\u914d");
            }
            case 2: {
                throw new Exception("\u516c\u79c1\u94a5\u7c7b\u578b\u4e0d\u5339\u914d");
            }
            default: {
                System.out.println("\u672a\u77e5\u9519\u8bef\uff01");
                throw new Exception("\u8fdb\u884c\u516c\u79c1\u94a5\u5339\u914d\u68c0\u9a8c\u65f6\u53d1\u751f\u672a\u77e5\u9519\u8bef\uff01");
            }
        }
        try {
            ctx.useRandomnessFile(this.randomFile, password);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("\u52a0\u8f7d\u968f\u673a\u6570\u6587\u4ef6\u5931\u8d25!");
        }
        return ctx;
    }

    public SSLContext createSSLContextUseWallet(String walletfile, String password) throws Exception {
        SSLContext ctx = new SSLContext();
        try {
            ctx.loadPKCS12File(walletfile, password);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("\u52a0\u8f7dOsacarKeyStore\u5931\u8d25");
        }
        catch (Error er) {
            throw new Exception("\u52a0\u8f7dOsacarKeyStore\u5931\u8d25");
        }
        switch (ctx.checkKeyPair()) {
            case 0: {
                break;
            }
            case 1: {
                throw new Exception("\u516c\u79c1\u94a5\u6570\u503c\u4e0d\u5339\u914d");
            }
            case 2: {
                throw new Exception("\u516c\u79c1\u94a5\u7c7b\u578b\u4e0d\u5339\u914d");
            }
            default: {
                System.out.println("\u672a\u77e5\u9519\u8bef\uff01");
                throw new Exception("\u8fdb\u884c\u516c\u79c1\u94a5\u5339\u914d\u68c0\u9a8c\u65f6\u53d1\u751f\u672a\u77e5\u9519\u8bef\uff01");
            }
        }
        try {
            ctx.useRandomnessFile(this.randomFile, password);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("\u52a0\u8f7d\u968f\u673a\u6570\u6587\u4ef6\u5931\u8d25!");
        }
        return ctx;
    }

    public static String dnToCommonName(DistinguishedName dN) throws IOException {
        Vector dn = dN.getName();
        Vector rdn = null;
        for (int i = 0; i < dn.size(); ++i) {
            if (!((String[])((Vector)dn.get(i)).firstElement())[0].equals("CN")) continue;
            rdn = (Vector)dn.get(i);
            break;
        }
        if (rdn == null || rdn.size() != 1) {
            throw new IOException("DN forms with multiple AVAs per RDN are unacceptable");
        }
        String[] ava = (String[])rdn.firstElement();
        if (ava.length != 2) {
            throw new IOException("Bogus AVA array");
        }
        if (!ava[0].equals("CN")) {
            throw new IOException("CN must be most local AVA");
        }
        return ava[1];
    }

    public static String dnToOrgUnitName(DistinguishedName dN) throws IOException {
        Vector dn = dN.getName();
        Vector org = null;
        for (int i = 0; i < dn.size(); ++i) {
            if (!((String[])((Vector)dn.get(i)).firstElement())[0].equals("OU")) continue;
            org = (Vector)dn.get(i);
            break;
        }
        if (org == null || org.size() != 1) {
            throw new IOException("OU forms with multiple AVAs per RDN are unacceptable");
        }
        String[] ava = (String[])org.firstElement();
        return ava[1];
    }

    public static boolean isLocalClient(String host) {
        if (null == host) {
            return false;
        }
        if ("localhost".equalsIgnoreCase(host)) {
            return true;
        }
        try {
            if (InetAddress.getLocalHost().getHostName().equalsIgnoreCase(host)) {
                return true;
            }
        }
        catch (Exception e) {
            // empty catch block
        }
        if (host.startsWith("127.0.0.")) {
            try {
                String endStr = host.substring("127.0.0.".length(), host.length());
                int endNum = Integer.parseInt(endStr);
                if (endNum > 0 && endNum < 256) {
                    return true;
                }
            }
            catch (Exception e) {
                return false;
            }
        }
        try {
            InetAddress[] localIP = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            for (int i = 0; i < localIP.length; ++i) {
                if (!localIP[i].getHostAddress().equals(host)) continue;
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    public static boolean checkIP(String hostName, String certName) {
        if (hostName.equalsIgnoreCase(certName)) {
            return true;
        }
        boolean hostNameLocal = OSocket.isLocalClient(hostName);
        boolean certNameLocal = OSocket.isLocalClient(certName);
        if (hostNameLocal && certNameLocal) {
            return true;
        }
        if (hostNameLocal || certNameLocal) {
            return false;
        }
        try {
            if (InetAddress.getByName(hostName).getHostName().equalsIgnoreCase(certName)) {
                return true;
            }
            InetAddress[] addresses = InetAddress.getAllByName(hostName);
            for (int i = 0; i < addresses.length; ++i) {
                if (!addresses[i].getHostAddress().equalsIgnoreCase(certName)) continue;
                return true;
            }
            return false;
        }
        catch (UnknownHostException e) {
            return false;
        }
    }

    public SSLSocket connect(SSLContext ctx, String host, int port) throws IOException, SQLException {
        SSLSocket s = null;
        try {
            s = new SSLSocket(ctx, this.connection, host, port, 1);
            Vector certChain = s.getCertificateChain();
            if (certChain == null) {
                return s;
            }
            if (certChain.size() > 10) {
                throw new IOException("Certificate chain too long");
            }
            Certificate cert = (Certificate)certChain.lastElement();
            String commonName = OSocket.dnToCommonName(cert.getSubjectName());
            String orgUnitName = OSocket.dnToOrgUnitName(cert.getSubjectName());
            if (!OSocket.checkIP(host, commonName)) {
                throw new OSQLException("OSCAR-00113", "88888", 113);
            }
            if (!this.database.equalsIgnoreCase(orgUnitName)) {
                throw new OSQLException("OSCAR-00116", "88888", 116);
            }
        }
        catch (IOException e) {
            if (e.getMessage().equals("java.net.SocketException: Connection reset")) {
                throw new OSQLException("OSCAR-00114", "88888", 114, e);
            }
            if (e.getMessage().equals("Unknown CA")) {
                throw new OSQLException("OSCAR-00115", "88888", 115, e);
            }
            throw e;
        }
        return s;
    }

    public void setRootFile(String root) {
        this.rootFile = root;
    }

    public void setRandomFile(String random) {
        this.randomFile = random;
    }

    public String getRootFile() {
        return this.rootFile;
    }

    public String getRandomFile() {
        return this.randomFile;
    }

    public void openWithSSLUseWallet(String walletfile, String password, String randomfile, String database) throws Exception {
        this.randomFile = randomfile;
        this.database = database;
        SSLContext ctx = this.createSSLContextUseWallet(walletfile, password);
        this.isSSLCon = true;
        this.connection = this.connect(ctx, this.host, this.port);
        this.osr_input = new BufferedInputStream(this.connection.getInputStream(), 8192);
        this.osr_output = new BufferedOutputStream(this.connection.getOutputStream(), 8192);
    }

    public void readJDCBVerifyKey(String filename, String name) {
        try {
            this.keyForVerifyJDBC = PrivateKeyReader.readFromFile(filename, name);
            this.keyForVerifyJDBC = PrivateKeyConversion.converEAYEncryptedKey(this.keyForVerifyJDBC);
        }
        catch (Exception ex) {
            System.out.println("\u8b66\u544a\uff0c\u52a0\u8f7dJDBC\u9a8c\u8bc1\u79c1\u94a5\u5931\u8d25\uff0c\u4e0d\u80fd\u5bf9JDBC\u91c7\u7528\u7b7e\u540d\u9a8c\u8bc1");
        }
    }

    public PrivateKey getJDBCVerifyKey() {
        return this.keyForVerifyJDBC;
    }

    public Socket getSocket() throws Exception {
        return new Socket(this.host, this.port);
    }

    public Socket getCurrentSocket() {
        return this.connection;
    }

    public void setSocketTimeOut(int timeout) throws SocketException {
        if (this.getCurrentSocket() != null) {
            this.getCurrentSocket().setSoTimeout(timeout);
        } else if (this.unixConnection != null) {
            this.unixConnection.setTimeout(timeout);
        }
    }

    public void wrapEncryptStream() {
        this.osr_output = new HdEncryptOutputStream((OutputStream)this.osr_output, this.con);
        this.osr_input = new HdDecryptInputStream(this.osr_input, this.con);
    }
}

