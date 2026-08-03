/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLAlertX;
import com.claymoresystems.ptls.SSLCipherState;
import com.claymoresystems.ptls.SSLCipherSuite;
import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLMAC;
import com.claymoresystems.ptls.SSLPDU;
import com.claymoresystems.ptls.SSLopaque;
import com.claymoresystems.ptls.SSLuint16;
import com.claymoresystems.ptls.SSLuint8;
import com.claymoresystems.util.Bench;
import cryptix.util.core.ArrayUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SSLRecord
extends SSLPDU {
    SSLuint8 type = new SSLuint8();
    SSLuint16 version = new SSLuint16();
    SSLopaque data = new SSLopaque(-65535);
    public static final int SSL_CT_CHANGE_CIPHER_SPEC = 20;
    public static final int SSL_CT_ALERT = 21;
    public static final int SSL_CT_HANDSHAKE = 22;
    public static final int SSL_CT_APPLICATION_DATA = 23;

    public SSLRecord(SSLConn conn, int ct, byte[] buf) {
        this.type.value = ct;
        this.version.value = conn.ssl_version;
        this.data.value = buf;
    }

    public SSLRecord(SSLConn conn) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int encode(SSLConn conn, OutputStream s) throws Error, IOException {
        int written = 0;
        SSLConn sSLConn = conn;
        synchronized (sSLConn) {
            Bench.start(1);
            written = this.type.encode(conn, s);
            written += this.version.encode(conn, s);
            if (conn.write_cipher_state != null) {
                int length = this.data.value.length;
                int pad = 0;
                SSLDebug.debug(8, "Encoding record");
                SSLCipherSuite cs = conn.write_cipher_state.cipher_suite;
                length += cs.getDigestOutputLength();
                SSLDebug.debug(8, "Encrypting: plain text", this.data.value);
                if (cs.blockCipherP()) {
                    if ((pad = 8 - ++length % 8) == 8) {
                        pad = 0;
                    }
                    length += pad;
                }
                Bench.start(5);
                byte[] mac = this.calcMac(conn, conn.write_cipher_state, conn.write_sequence_num++, this.data.value);
                Bench.end(5);
                SSLDebug.debug(8, "Encoding MAC", mac);
                byte[] total = new byte[length];
                Bench.start(6);
                System.arraycopy(this.data.value, 0, total, 0, this.data.value.length);
                System.arraycopy(mac, 0, total, this.data.value.length, mac.length);
                if (cs.blockCipherP()) {
                    for (int i = 0; i < pad + 1; ++i) {
                        total[i + mac.length + this.data.value.length] = (byte)pad;
                    }
                }
                Bench.end(6);
                Bench.start(3);
                this.data.value = conn.write_cipher_state.cipher != null ? conn.write_cipher_state.cipher.update(total) : total;
                Bench.end(3);
                SSLDebug.debug(8, "Encrypting: cipher text", this.data.value);
            }
            Bench.end(1);
        }
        return written += this.data.encode(conn, s);
    }

    public int decode(SSLConn conn, InputStream s) throws IOException, Error {
        int readb = 0;
        SSLopaque ciphertext = new SSLopaque(-65535);
        boolean throw_bad_mac = false;
        try {
            readb = this.type.decode(conn, s);
            readb += this.version.decode(conn, s);
            readb += ciphertext.decode(conn, s);
            if (conn.read_cipher_state != null) {
                int length;
                SSLCipherSuite cs = conn.read_cipher_state.cipher_suite;
                SSLDebug.debug(8, "Ciphertext", ciphertext.value);
                byte[] plain = conn.read_cipher_state.cipher != null ? conn.read_cipher_state.cipher.update(ciphertext.value) : ciphertext.value;
                SSLDebug.debug(8, "Plaintext", plain);
                if (cs.blockCipherP()) {
                    length = plain.length;
                    int pad = plain[length - 1] & 0xFF;
                    if (pad > length) {
                        SSLDebug.debug(8, "Pad longer than plaintext");
                        pad = 0;
                        throw_bad_mac = true;
                    }
                    if (conn.ssl_version >= 769) {
                        int offset = length - 2;
                        for (int i = 0; i < pad; ++i) {
                            int b = plain[offset] & 0xFF;
                            if (b != pad) {
                                SSLDebug.debug(8, "Some pad bytes don't match");
                                pad = 0;
                                throw_bad_mac = true;
                            }
                            --offset;
                        }
                    }
                    if (conn.ssl_version == 768 && pad > 8) {
                        SSLDebug.debug(8, "Pad too long");
                        pad = 0;
                        throw_bad_mac = true;
                    }
                    length -= pad + 1;
                } else {
                    length = plain.length;
                }
                int maclength = conn.read_cipher_state.cipher_suite.getDigestOutputLength();
                if (length < maclength) {
                    SSLDebug.debug(8, "MAC too long for record--garbage");
                    throw_bad_mac = true;
                }
                byte[] msg_data = new byte[length - maclength];
                System.arraycopy(plain, 0, msg_data, 0, msg_data.length);
                byte[] c_mac = this.calcMac(conn, conn.read_cipher_state, conn.read_sequence_num++, msg_data);
                SSLDebug.debug(8, "Computed MAC", c_mac);
                if (maclength != c_mac.length) {
                    throw new InternalError("Digest Length inconsistency");
                }
                byte[] p_mac = new byte[c_mac.length];
                System.arraycopy(plain, length - p_mac.length, p_mac, 0, p_mac.length);
                SSLDebug.debug(8, "Message MAC", p_mac);
                if (!ArrayUtil.areEqual(p_mac, c_mac)) {
                    conn.alert(SSLAlertX.TLS_ALERT_BAD_RECORD_MAC);
                }
                if (throw_bad_mac) {
                    conn.alert(SSLAlertX.TLS_ALERT_BAD_RECORD_MAC);
                }
                if ((length -= p_mac.length) != msg_data.length) {
                    throw new InternalError("Sanity check failed");
                }
                this.data.value = new byte[msg_data.length];
                System.arraycopy(msg_data, 0, this.data.value, 0, msg_data.length);
            } else {
                this.data.value = new byte[ciphertext.value.length];
                System.arraycopy(ciphertext.value, 0, this.data.value, 0, ciphertext.value.length);
            }
        }
        catch (IOException ex) {
            conn.makeUnresumable();
            throw ex;
        }
        return readb;
    }

    public void send(SSLConn conn) throws Error, IOException {
        ByteArrayOutputStream tos = new ByteArrayOutputStream(this.data.value.length + 30);
        this.encode(conn, tos);
        tos.writeTo(conn.sock_out);
    }

    public byte[] calcMac(SSLConn conn, SSLCipherState st, long sequenceNum, byte[] buf) {
        return SSLMAC.calcMAC(conn, st, this.type.value, sequenceNum, buf);
    }
}

