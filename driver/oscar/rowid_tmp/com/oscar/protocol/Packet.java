/*
 * Decompiled with CFR 0.152.
 */
package com.oscar.protocol;

import com.oscar.Driver;
import com.oscar.core.BaseConnection;
import com.oscar.protocol.packets.AsciiRowPacket;
import com.oscar.protocol.packets.AsciiRowPacketV2;
import com.oscar.protocol.packets.AuthenticationPacket;
import com.oscar.protocol.packets.AuthenticationPacketV2;
import com.oscar.protocol.packets.AuthenticationPacketV3;
import com.oscar.protocol.packets.BLogErrorResponsePacket;
import com.oscar.protocol.packets.BackendKeyPacket;
import com.oscar.protocol.packets.BackupMetaDataPacket;
import com.oscar.protocol.packets.BackupPhysicalDataPacket;
import com.oscar.protocol.packets.BackupPhysicalRowEndPacket;
import com.oscar.protocol.packets.BasePacket;
import com.oscar.protocol.packets.BinlogErrorPacket;
import com.oscar.protocol.packets.BinlogSuccessPacket;
import com.oscar.protocol.packets.BlogDataPacket;
import com.oscar.protocol.packets.CancelRequestPacket;
import com.oscar.protocol.packets.CompleteResponsePacket;
import com.oscar.protocol.packets.CursorResponsePacket;
import com.oscar.protocol.packets.EmptyQueryResponsePacket;
import com.oscar.protocol.packets.EndianTypePacket;
import com.oscar.protocol.packets.ErrorResponsePacket;
import com.oscar.protocol.packets.ExportBinlogSuccessPacket;
import com.oscar.protocol.packets.FunctionCallPacket;
import com.oscar.protocol.packets.FunctionResponsePacket;
import com.oscar.protocol.packets.HashDataPacket;
import com.oscar.protocol.packets.ImportExportResponsePacket;
import com.oscar.protocol.packets.ImportPacket;
import com.oscar.protocol.packets.ListenerResponsePacket;
import com.oscar.protocol.packets.MessagePacket;
import com.oscar.protocol.packets.NewImportPacket;
import com.oscar.protocol.packets.NoticeResponsePacket;
import com.oscar.protocol.packets.ParamInforPacket;
import com.oscar.protocol.packets.PlanIDPacket;
import com.oscar.protocol.packets.QueryPacket;
import com.oscar.protocol.packets.ReadyForMetaData;
import com.oscar.protocol.packets.ReadyForPhysicalDataPacket;
import com.oscar.protocol.packets.ReadyForQueryPacket;
import com.oscar.protocol.packets.RowDescriptionPacket;
import com.oscar.protocol.packets.StartupPacket;
import com.oscar.protocol.packets.TerminatePacket;
import com.oscar.protocol.packets.UnencryptedPasswordPacket;

public class Packet {
    public static final char[] tags = new char[]{'K', 'D', 'R', 'C', 'P', 'I', 'E', 'N', 'Z', 'T', 'V', 'G', 'H', 'S', 'Q', 'X', 'Y', 'F', 'U', 'W', 'L', 'p', 'g', 'c', 'M', 'm', 'B', 'e', 's', 'o', 'f'};
    public static final char protocolV2Tag = '\uffa1';
    public static final char planIDTag = '\uffa4';
    public static final String[] packets = new String[]{"BackendKeyPacket", "AsciiRowPacket", "AuthenticationPacket", "CompleteResponsePacket", "CursorResponsePacket", "EmptyQueryResponsePacket", "ErrorResponsePacket", "NoticeResponsePacket", "ReadyForQueryPacket", "RowDescriptionPacket", "FunctionResponsePacket", "CopyInResponsePacket", "CopyOutResponsePacket", "StartupPacket", "QueryPacket", "TerminatePacket", "CancelRequestPacket", "FunctionCallPacket", "UnencryptedPasswordPacket", "EncryptedPasswordPacket", "SSLRequest", "ParamInforPacket", "HashDataPacket", "BlogDataPacket", "BinlogErrorPacket", "BinlogSuccessPacket"};
    private BackendKeyPacket backkp = null;
    private AsciiRowPacket asciirp = null;
    private AsciiRowPacketV2 asciirpV2 = null;
    private AuthenticationPacket authenticationp = null;
    private AuthenticationPacketV2 authenticationpV2 = null;
    private CompleteResponsePacket completerp = null;
    private CursorResponsePacket cursorrp = null;
    private EmptyQueryResponsePacket emptyqrp = null;
    private ErrorResponsePacket errorrp = null;
    private NoticeResponsePacket noticerp = null;
    private ReadyForQueryPacket readyfqp = null;
    private RowDescriptionPacket rowdp = null;
    private FunctionResponsePacket functionrp = null;
    private StartupPacket startupp = null;
    private QueryPacket queryp = null;
    private TerminatePacket terminatep = null;
    private CancelRequestPacket cancelrp = null;
    private FunctionCallPacket functioncp = null;
    private UnencryptedPasswordPacket unencryptedpp = null;
    private ParamInforPacket paramip = null;
    private ImportPacket importp = null;
    private ImportExportResponsePacket importerp = null;
    private ListenerResponsePacket listenerp = null;
    private NewImportPacket newImportp = null;
    private BackupMetaDataPacket backupMetaData = null;
    private BackupPhysicalDataPacket backupPhysicalData = null;
    private BackupPhysicalRowEndPacket backupPhysicalRowEnd = null;
    private ReadyForMetaData readyForMetaData = null;
    private ReadyForPhysicalDataPacket readyForPhysicalData = null;
    private EndianTypePacket endianTypep = null;
    private HashDataPacket hashDataPacket = null;
    private PlanIDPacket planIDPacket = null;
    private BlogDataPacket blogDataPacket = null;
    private BinlogErrorPacket blogErrorPacket = null;
    private BinlogSuccessPacket blogSuccessPacket = null;
    private ExportBinlogSuccessPacket exportBinlogSuccessPacket = null;
    private BLogErrorResponsePacket bLogErrorResponsePacket = null;
    private AuthenticationPacketV3 authenticationpV3 = null;
    private MessagePacket messagePacket = null;

    public BasePacket getInstance(char tag, BaseConnection conn) {
        switch (tag) {
            case 'K': {
                if (this.backkp == null) {
                    this.backkp = new BackendKeyPacket();
                    this.backkp.setConnection(conn);
                }
                return this.backkp;
            }
            case 'D': {
                if (conn.getProtocolVersion() != null && conn.getProtocolVersion().getProtocolType() >= 2) {
                    if (this.asciirpV2 == null) {
                        this.asciirpV2 = new AsciiRowPacketV2();
                        this.asciirpV2.setConnection(conn);
                    }
                    return this.asciirpV2;
                }
                if (this.asciirp == null) {
                    this.asciirp = new AsciiRowPacket();
                    this.asciirp.setConnection(conn);
                }
                return this.asciirp;
            }
            case 'R': {
                if (this.authenticationp == null) {
                    this.authenticationp = new AuthenticationPacket();
                    this.authenticationp.setConnection(conn);
                }
                return this.authenticationp;
            }
            case '\uffa1': {
                if (this.authenticationpV2 == null) {
                    this.authenticationpV2 = new AuthenticationPacketV2();
                    this.authenticationpV2.setConnection(conn);
                }
                return this.authenticationpV2;
            }
            case '\uffa4': {
                if (this.planIDPacket == null) {
                    this.planIDPacket = new PlanIDPacket();
                    this.planIDPacket.setConnection(conn);
                }
                return this.planIDPacket;
            }
            case 'C': {
                if (this.completerp == null) {
                    this.completerp = new CompleteResponsePacket();
                    this.completerp.setConnection(conn);
                }
                return this.completerp;
            }
            case 'P': {
                if (this.cursorrp == null) {
                    this.cursorrp = new CursorResponsePacket();
                    this.cursorrp.setConnection(conn);
                }
                return this.cursorrp;
            }
            case 'I': {
                if (this.emptyqrp == null) {
                    this.emptyqrp = new EmptyQueryResponsePacket();
                    this.emptyqrp.setConnection(conn);
                }
                return this.emptyqrp;
            }
            case 'E': {
                if (this.errorrp == null) {
                    this.errorrp = new ErrorResponsePacket();
                    this.errorrp.setConnection(conn);
                }
                return this.errorrp;
            }
            case 'N': {
                if (this.noticerp == null) {
                    this.noticerp = new NoticeResponsePacket();
                    this.noticerp.setConnection(conn);
                }
                return this.noticerp;
            }
            case 'Z': {
                if (this.readyfqp == null) {
                    this.readyfqp = new ReadyForQueryPacket();
                    this.readyfqp.setConnection(conn);
                }
                return this.readyfqp;
            }
            case 'T': {
                if (this.rowdp == null) {
                    this.rowdp = new RowDescriptionPacket();
                    this.rowdp.setConnection(conn);
                }
                return this.rowdp;
            }
            case 'V': {
                if (this.functionrp == null) {
                    this.functionrp = new FunctionResponsePacket();
                    this.functionrp.setConnection(conn);
                }
                return this.functionrp;
            }
            case 'S': {
                if (this.messagePacket == null) {
                    this.messagePacket = new MessagePacket();
                    this.messagePacket.setConnection(conn);
                }
                return this.messagePacket;
            }
            case 'Q': {
                return this.queryp;
            }
            case 'X': {
                if (this.terminatep == null) {
                    this.terminatep = new TerminatePacket();
                    this.terminatep.setConnection(conn);
                }
                return this.terminatep;
            }
            case 'Y': {
                return this.cancelrp;
            }
            case 'F': {
                return this.functioncp;
            }
            case 'U': {
                return this.unencryptedpp;
            }
            case 'p': {
                if (this.paramip == null) {
                    this.paramip = new ParamInforPacket();
                    this.paramip.setConnection(conn);
                }
                return this.paramip;
            }
            case 'g': {
                if (this.importp == null) {
                    this.importp = new ImportPacket();
                    this.importp.setConnection(conn);
                }
                return this.importp;
            }
            case 'c': {
                if (this.importerp == null) {
                    this.importerp = new ImportExportResponsePacket();
                    this.importerp.setConnection(conn);
                }
                return this.importerp;
            }
            case 'L': {
                if (this.importerp == null) {
                    this.listenerp = new ListenerResponsePacket();
                    this.listenerp.setConnection(conn);
                }
                return this.listenerp;
            }
            case 'r': {
                if (this.newImportp == null) {
                    this.newImportp = new NewImportPacket();
                    this.newImportp.setConnection(conn);
                }
                return this.newImportp;
            }
            case 'm': {
                if (this.backupMetaData == null) {
                    this.backupMetaData = new BackupMetaDataPacket();
                    this.backupMetaData.setConnection(conn);
                }
                return this.backupMetaData;
            }
            case 'M': {
                if (this.backupPhysicalData == null) {
                    this.backupPhysicalData = new BackupPhysicalDataPacket();
                    this.backupPhysicalData.setConnection(conn);
                }
                return this.backupPhysicalData;
            }
            case 'o': {
                if (this.backupPhysicalRowEnd == null) {
                    this.backupPhysicalRowEnd = new BackupPhysicalRowEndPacket();
                    this.backupPhysicalRowEnd.setConnection(conn);
                }
                return this.backupPhysicalRowEnd;
            }
            case 'w': {
                if (this.readyForMetaData == null) {
                    this.readyForMetaData = new ReadyForMetaData();
                    this.readyForMetaData.setConnection(conn);
                }
                return this.readyForMetaData;
            }
            case 'W': {
                if (this.readyForPhysicalData == null) {
                    this.readyForPhysicalData = new ReadyForPhysicalDataPacket();
                    this.readyForPhysicalData.setConnection(conn);
                }
                return this.readyForPhysicalData;
            }
            case 'O': {
                if (this.endianTypep == null) {
                    this.endianTypep = new EndianTypePacket();
                    this.endianTypep.setConnection(conn);
                }
                return this.endianTypep;
            }
            case 'H': {
                if (this.hashDataPacket == null) {
                    this.hashDataPacket = new HashDataPacket();
                    this.hashDataPacket.setConnection(conn);
                }
                return this.hashDataPacket;
            }
            case 'B': {
                if (this.blogDataPacket == null) {
                    this.blogDataPacket = new BlogDataPacket();
                    this.blogDataPacket.setConnection(conn);
                }
                return this.blogDataPacket;
            }
            case 'e': {
                if (this.blogErrorPacket == null) {
                    this.blogErrorPacket = new BinlogErrorPacket();
                    this.blogErrorPacket.setConnection(conn);
                }
                return this.blogErrorPacket;
            }
            case 's': {
                if (this.blogSuccessPacket == null) {
                    this.blogSuccessPacket = new BinlogSuccessPacket();
                    this.blogSuccessPacket.setConnection(conn);
                }
                return this.blogSuccessPacket;
            }
            case 'f': {
                if (this.exportBinlogSuccessPacket == null) {
                    this.exportBinlogSuccessPacket = new ExportBinlogSuccessPacket();
                    this.exportBinlogSuccessPacket.setConnection(conn);
                }
                return this.exportBinlogSuccessPacket;
            }
            case 'k': {
                if (this.bLogErrorResponsePacket == null) {
                    this.bLogErrorResponsePacket = new BLogErrorResponsePacket();
                    this.bLogErrorResponsePacket.setConnection(conn);
                }
                return this.bLogErrorResponsePacket;
            }
            case '\uffb1': {
                if (this.authenticationpV3 == null) {
                    this.authenticationpV3 = new AuthenticationPacketV3();
                    this.authenticationpV3.setConnection(conn);
                }
                return this.authenticationpV3;
            }
        }
        Driver.writeLog("unknown package:" + String.valueOf(tag));
        return null;
    }
}

