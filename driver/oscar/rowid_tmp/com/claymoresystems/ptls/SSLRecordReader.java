/*
 * Decompiled with CFR 0.152.
 */
package com.claymoresystems.ptls;

import com.claymoresystems.ptls.SSLAlert;
import com.claymoresystems.ptls.SSLAlertX;
import com.claymoresystems.ptls.SSLCaughtAlertException;
import com.claymoresystems.ptls.SSLConn;
import com.claymoresystems.ptls.SSLDebug;
import com.claymoresystems.ptls.SSLInputStream;
import com.claymoresystems.ptls.SSLReHandshakeException;
import com.claymoresystems.ptls.SSLRecord;
import java.io.ByteArrayInputStream;
import java.io.IOException;

class SSLRecordReader {
    SSLConn conn;
    SSLInputStream[] streams = new SSLInputStream[]{new SSLInputStream(this), new SSLInputStream(this)};

    public SSLRecordReader(SSLConn c) {
        this.conn = c;
        this.conn.sock_in_hp = this.streams[0];
        this.conn.sock_in_data = this.streams[1];
    }

    public int readRecord() throws IOException {
        if (this.conn.recvdClose) {
            return -1;
        }
        SSLRecord r = new SSLRecord(this.conn);
        r.decode(this.conn, this.conn.sock_in);
        if (this.conn.read_cipher_state != null && this.conn.ssl_version != r.version.value) {
            this.conn.alert(SSLAlertX.TLS_ALERT_ILLEGAL_PARAMETER);
        }
        int rtype = r.type.value;
        switch (rtype) {
            case 20: {
                this.conn.hs.recvChangeCipherSpecs(r.data.value);
                break;
            }
            case 21: {
                this.processAlert(r.data.value);
                break;
            }
            case 22: {
                boolean startRehandshake = this.conn.processIncomingHandshakeRecord(r.data.value);
                SSLDebug.debug(1, "Read a new record type: handshake length" + r.data.value.length);
                this.streams[0].write(r);
                if (!startRehandshake) break;
                throw new SSLReHandshakeException();
            }
            case 23: {
                if (!this.conn.secureMode || this.conn.read_cipher_state == null) {
                    this.conn.alert(SSLAlertX.TLS_ALERT_UNEXPECTED_MESSAGE);
                }
                SSLDebug.debug(1, "Read a new record type: data length" + r.data.value.length);
                this.streams[1].write(r);
                break;
            }
            default: {
                throw new IOException("Bad record type" + rtype);
            }
        }
        return 0;
    }

    public void processAlert(byte[] rec) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(rec);
        SSLAlert alert = new SSLAlert();
        alert.decode(this.conn, bis);
        SSLAlertX alertx = new SSLAlertX(alert.description.value, alert.level.value);
        if (alertx.fatalP()) {
            this.conn.recvdClose = true;
            this.conn.invalid = true;
            this.conn.makeUnresumable();
        }
        if (alert.description.value != SSLAlertX.TLS_ALERT_CLOSE_NOTIFY) {
            throw new SSLCaughtAlertException(alertx);
        }
        this.conn.recvdClose = true;
    }
}

