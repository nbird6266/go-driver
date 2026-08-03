/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol;

import com.oscar.crypt.Md;
import com.oscar.crypt.Sign;
import com.oscar.protocol.packets.JDBCVeifyCryptPacket;
import com.oscar.protocol.packets.JDBCVeifyModePacket;
import com.oscar.protocol.packets.JDBCVeifyRandomPacket;
import com.oscar.protocol.stream.OStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.sql.SQLException;

public class VeifyJDBC {
    private static final int ONLY_FLAG_MODE = 0;
    private static final int DIGEST_MODE = 1;
    private static final int SIGNATURE_MODE = 2;
    private static final int RSA_AND_SIGN_MODE = 3;
    private static final int DEFAULT_MODE = 0;
    private OStream ostream = null;
    byte[] randomNum = null;
    byte[] cryptNum = null;
    PrivateKey privatekey = null;

    public VeifyJDBC() {
    }

    public VeifyJDBC(OStream ostream) {
        this.ostream = ostream;
    }

    public void veify() throws SQLException, IOException {
        int mode = 0;
        JDBCVeifyModePacket jdbcVeifyModePacket = new JDBCVeifyModePacket();
        jdbcVeifyModePacket.receiveFrom(this.ostream.getInputStream());
        mode = jdbcVeifyModePacket.getMode();
        switch (mode) {
            case 0: {
                this.veify_only_flag();
                break;
            }
            case 1: {
                this.veify_digest();
                break;
            }
            case 2: {
                this.veify_signature();
                break;
            }
            case 3: {
                this.veify_rsa_and_sign();
                break;
            }
            default: {
                throw new IOException("\u9519\u8bef\u7684JDBC\u9a8c\u8bc1\u6a21\u5f0f");
            }
        }
    }

    private void veify_only_flag() throws IOException, SQLException {
        this.cryptNum = new byte[0];
        this.sendCryptNum();
    }

    private void veify_digest() throws IOException, SQLException {
        this.readRandomNum();
        try {
            this.cryptNum = Md.md(this.randomNum);
        }
        catch (Throwable ex) {
            throw new IOException(ex.getMessage());
        }
        this.sendCryptNum();
    }

    private void veify_signature() throws IOException, SQLException {
        this.readRandomNum();
        try {
            this.privatekey = this.ostream.getJDBCVerifyKey();
            if (this.privatekey == null) {
                throw new IOException("\u8bfb\u53d6JDBC\u9a8c\u8bc1\u79c1\u94a5\u6587\u4ef6\u5931\u8d25");
            }
            this.cryptNum = Sign.sign(this.randomNum, this.privatekey, "SHA1withRSA", "SunRsaSign");
        }
        catch (Throwable ex) {
            throw new IOException(ex.getMessage());
        }
        this.sendCryptNum();
    }

    private void veify_rsa_and_sign() throws IOException {
        throw new IOException("\u5c1a\u672a\u5b9e\u73b0");
    }

    public long getRandNum() {
        long ret = 0L;
        for (int i = 0; i < 4; ++i) {
            ret = (ret << 8) + (long)this.randomNum[i];
        }
        return ret;
    }

    private void readRandomNum() throws IOException, SQLException {
        JDBCVeifyRandomPacket jdbcVeifyRandomPacket = new JDBCVeifyRandomPacket();
        jdbcVeifyRandomPacket.receiveFrom(this.ostream.getInputStream());
        this.randomNum = jdbcVeifyRandomPacket.getRandom();
    }

    private void sendCryptNum() throws IOException, SQLException {
        JDBCVeifyCryptPacket jdbcVeifyCryptPacket = new JDBCVeifyCryptPacket(this.cryptNum);
        jdbcVeifyCryptPacket.sendTo(this.ostream.getBufferedOutputStream());
    }

    public static void main(String[] args) {
        VeifyJDBC veifyjdbc = new VeifyJDBC();
    }
}

