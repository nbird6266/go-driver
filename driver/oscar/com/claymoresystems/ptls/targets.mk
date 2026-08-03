#
#    targets.mk
#
#    $Source: F:/CVSReposity/development/interface/jdbc/src/com/claymoresystems/ptls/targets.mk,v $
#    $Revision: 1.1 $
#    $Date: 2005/08/17 01:22:39 $
#    $Name:  $
#    $Disclaimer$
#
#    Copyright (C) 2002, RTFM, Inc.
#    All Rights Reserved.
#
#    ekr@rtfm.com
#



#
#    CONFIGURE USER-DEFINED MAKE ENVIRONMENT
#
#    These fields are specified by the user.  The remainder of
#    this file is generated from this user-specified information.
#
# com_CLAYMORESYSTEMS_PTLS_DEFINES:
#    cpp defines, with the -D flag preceeding each
#
# com_CLAYMORESYSTEMS_PTLS_INCLUDES:
#    cpp include directories, with the -I flag preceeding each
#
# com_CLAYMORESYSTEMS_PTLS_LIBNAME:
#    the library associated with this module directory, used in
#    most cases for debugging purposes
#
# com_CLAYMORESYSTEMS_PTLS_LIBPATHS:
#    link-time directories to search for libraries, with the -L flag
#    preceeding each
#
# com_CLAYMORESYSTEMS_PTLS_LIBRARIES:
#    link-time libraries, with the -l flag preceeding each
#
# com_CLAYMORESYSTEMS_PTLS_LOCALFLAGS:
#    compile-time flags specific to compiling only the files in
#    this module directory--this variable should only be set in
#    extremely exceptional cases
#
# com_CLAYMORESYSTEMS_PTLS_MAKEFILES:
#    the makefiles
#
# com_CLAYMORESYSTEMS_PTLS_NOBUILD:
#    do not build this module as part of the main system
#
# com_CLAYMORESYSTEMS_PTLS_PACKAGE:
#    the Java package name
#
# com_CLAYMORESYSTEMS_PTLS_PREFIX:
#    defines the module name, which also serves as the
#    prefix for all the variable names defined in this file
#
# com_CLAYMORESYSTEMS_PTLS_PROGRAMS:
#    programs to build
#
# com_CLAYMORESYSTEMS_PTLS_SOURCES:
#    the source files to compile to object
#
com_CLAYMORESYSTEMS_PTLS_DEFINES  =
com_CLAYMORESYSTEMS_PTLS_INCLUDES  =
com_CLAYMORESYSTEMS_PTLS_LIBNAME  =
com_CLAYMORESYSTEMS_PTLS_LIBPATHS  =
com_CLAYMORESYSTEMS_PTLS_LIBRARIES  =
com_CLAYMORESYSTEMS_PTLS_LOCALFLAGS  =
com_CLAYMORESYSTEMS_PTLS_MAKEFILES  =
com_CLAYMORESYSTEMS_PTLS_NOBUILD  =
com_CLAYMORESYSTEMS_PTLS_PACKAGE  = com.claymoresystems.ptls
com_CLAYMORESYSTEMS_PTLS_PREFIX  = com_CLAYMORESYSTEMS_PTLS
com_CLAYMORESYSTEMS_PTLS_PROGRAMS  =
com_CLAYMORESYSTEMS_PTLS_SOURCES  = LoadProviders.java SSLAlert.java \
                            SSLAlertException.java SSLAlertX.java \
                            SSLCaughtAlertException.java \
                            SSLCertificate.java SSLCertificateRequest.java \
                            SSLCertificateVerify.java SSLCipherState.java \
                            SSLCipherSuite.java SSLClientHello.java \
                            SSLClientKeyExchange.java SSLConn.java \
                            SSLContext.java SSLDHParams.java \
                            SSLDHPrivateKey.java SSLDebug.java \
                            SSLEncoded.java SSLException.java \
                            SSLFinished.java SSLHandshake.java \
                            SSLHandshakeClient.java \
                            SSLHandshakeFailedException.java \
                            SSLHandshakeHashes.java SSLHandshakeHdr.java \
                            SSLHandshakeServer.java SSLHelloRequest.java \
                            SSLInputStream.java SSLMAC.java \
                            SSLOutputStream.java SSLPDU.java SSLPRF.java \
                            SSLPrematureCloseException.java \
                            SSLRSAParams.java SSLReHandshakeException.java \
                            SSLRecord.java SSLRecordReader.java \
                            SSLServerHello.java SSLServerHelloDone.java \
                            SSLServerKeyExchange.java SSLServerSocket.java \
                            SSLSessionData.java SSLSocket.java \
                            SSLThrewAlertException.java SSLopaque.java \
                            SSLuint16.java SSLuint24.java SSLuint32.java \
                            SSLuint8.java SSLuintX.java \
                            SSLv2ClientHello.java \
                            SSLv3CertificateVerify.java SSLv3Finished.java \
                            SSLv3MAC.java SSLv3PRF.java SSLvector.java \
                            SocketBasedSocketImpl.java \
                            TLSCertificateVerify.java TLSFinished.java \
                            TLSMAC.java TLSPRF.java



#
#    CONFIGURE AUTOMATICALLY-GENERATED MAKE ENVIRONMENT
#
# com_CLAYMORESYSTEMS_PTLS_SOURCEFILES:
#
#    qualified names of the sources
#
# com_CLAYMORESYSTEMS_PTLS_OBJECTS:
#    object files to build
#
# com_CLAYMORESYSTEMS_PTLS_UNUSED:
#    obsolete files in the module directory that are not
#    used during the build process
#
# com_CLAYMORESYSTEMS_PTLS_USED:
#    all files in the module directory that are used
#    during the build process
#
com_CLAYMORESYSTEMS_PTLS_SOURCEFILES  = ${SRCROOTDIR}com/claymoresystems/ptls/LoadProviders.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLAlert.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLAlertException.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLAlertX.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLCaughtAlertException.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLCertificate.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLCertificateRequest.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLCertificateVerify.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLCipherState.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLCipherSuite.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLClientHello.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLClientKeyExchange.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLConn.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLContext.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLDHParams.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLDHPrivateKey.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLDebug.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLEncoded.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLException.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLFinished.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHandshake.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHandshakeClient.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHandshakeFailedException.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHandshakeHashes.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHandshakeHdr.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHandshakeServer.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHelloRequest.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLInputStream.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLMAC.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLOutputStream.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLPDU.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLPRF.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLPrematureCloseException.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLRSAParams.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLReHandshakeException.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLRecord.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLRecordReader.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLServerHello.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLServerHelloDone.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLServerKeyExchange.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLServerSocket.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLSessionData.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLSocket.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLThrewAlertException.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLopaque.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLuint16.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLuint24.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLuint32.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLuint8.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLuintX.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLv2ClientHello.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLv3CertificateVerify.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLv3Finished.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLv3MAC.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLv3PRF.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLvector.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SocketBasedSocketImpl.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/TLSCertificateVerify.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/TLSFinished.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/TLSMAC.java \
                            ${SRCROOTDIR}com/claymoresystems/ptls/TLSPRF.java
com_CLAYMORESYSTEMS_PTLS_OBJECTS  = ${SRCROOTDIR}com/claymoresystems/ptls/LoadProviders.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLAlert.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLAlertException.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLAlertX.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLCaughtAlertException.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLCertificate.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLCertificateRequest.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLCertificateVerify.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLCipherState.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLCipherSuite.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLClientHello.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLClientKeyExchange.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLConn.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLContext.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLDHParams.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLDHPrivateKey.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLDebug.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLEncoded.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLException.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLFinished.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHandshake.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHandshakeClient.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHandshakeFailedException.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHandshakeHashes.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHandshakeHdr.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHandshakeServer.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLHelloRequest.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLInputStream.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLMAC.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLOutputStream.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLPDU.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLPRF.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLPrematureCloseException.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLRSAParams.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLReHandshakeException.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLRecord.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLRecordReader.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLServerHello.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLServerHelloDone.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLServerKeyExchange.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLServerSocket.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLSessionData.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLSocket.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLThrewAlertException.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLopaque.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLuint16.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLuint24.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLuint32.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLuint8.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLuintX.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLv2ClientHello.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLv3CertificateVerify.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLv3Finished.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLv3MAC.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLv3PRF.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SSLvector.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/SocketBasedSocketImpl.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/TLSCertificateVerify.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/TLSFinished.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/TLSMAC.class \
                            ${SRCROOTDIR}com/claymoresystems/ptls/TLSPRF.class
com_CLAYMORESYSTEMS_PTLS_CLASSES  = com.claymoresystems.ptls.LoadProviders \
                            com.claymoresystems.ptls.SSLAlert \
                            com.claymoresystems.ptls.SSLAlertException \
                            com.claymoresystems.ptls.SSLAlertX \
                            com.claymoresystems.ptls.SSLCaughtAlertException \
                            com.claymoresystems.ptls.SSLCertificate \
                            com.claymoresystems.ptls.SSLCertificateRequest \
                            com.claymoresystems.ptls.SSLCertificateVerify \
                            com.claymoresystems.ptls.SSLCipherState \
                            com.claymoresystems.ptls.SSLCipherSuite \
                            com.claymoresystems.ptls.SSLClientHello \
                            com.claymoresystems.ptls.SSLClientKeyExchange \
                            com.claymoresystems.ptls.SSLConn \
                            com.claymoresystems.ptls.SSLContext \
                            com.claymoresystems.ptls.SSLDHParams \
                            com.claymoresystems.ptls.SSLDHPrivateKey \
                            com.claymoresystems.ptls.SSLDebug \
                            com.claymoresystems.ptls.SSLEncoded \
                            com.claymoresystems.ptls.SSLException \
                            com.claymoresystems.ptls.SSLFinished \
                            com.claymoresystems.ptls.SSLHandshake \
                            com.claymoresystems.ptls.SSLHandshakeClient \
                            com.claymoresystems.ptls.SSLHandshakeFailedException \
                            com.claymoresystems.ptls.SSLHandshakeHashes \
                            com.claymoresystems.ptls.SSLHandshakeHdr \
                            com.claymoresystems.ptls.SSLHandshakeServer \
                            com.claymoresystems.ptls.SSLHelloRequest \
                            com.claymoresystems.ptls.SSLInputStream \
                            com.claymoresystems.ptls.SSLMAC \
                            com.claymoresystems.ptls.SSLOutputStream \
                            com.claymoresystems.ptls.SSLPDU \
                            com.claymoresystems.ptls.SSLPRF \
                            com.claymoresystems.ptls.SSLPrematureCloseException \
                            com.claymoresystems.ptls.SSLRSAParams \
                            com.claymoresystems.ptls.SSLReHandshakeException \
                            com.claymoresystems.ptls.SSLRecord \
                            com.claymoresystems.ptls.SSLRecordReader \
                            com.claymoresystems.ptls.SSLServerHello \
                            com.claymoresystems.ptls.SSLServerHelloDone \
                            com.claymoresystems.ptls.SSLServerKeyExchange \
                            com.claymoresystems.ptls.SSLServerSocket \
                            com.claymoresystems.ptls.SSLSessionData \
                            com.claymoresystems.ptls.SSLSocket \
                            com.claymoresystems.ptls.SSLThrewAlertException \
                            com.claymoresystems.ptls.SSLopaque \
                            com.claymoresystems.ptls.SSLuint16 \
                            com.claymoresystems.ptls.SSLuint24 \
                            com.claymoresystems.ptls.SSLuint32 \
                            com.claymoresystems.ptls.SSLuint8 \
                            com.claymoresystems.ptls.SSLuintX \
                            com.claymoresystems.ptls.SSLv2ClientHello \
                            com.claymoresystems.ptls.SSLv3CertificateVerify \
                            com.claymoresystems.ptls.SSLv3Finished \
                            com.claymoresystems.ptls.SSLv3MAC \
                            com.claymoresystems.ptls.SSLv3PRF \
                            com.claymoresystems.ptls.SSLvector \
                            com.claymoresystems.ptls.SocketBasedSocketImpl \
                            com.claymoresystems.ptls.TLSCertificateVerify \
                            com.claymoresystems.ptls.TLSFinished \
                            com.claymoresystems.ptls.TLSMAC \
                            com.claymoresystems.ptls.TLSPRF
com_CLAYMORESYSTEMS_PTLS_UNUSED  =
com_CLAYMORESYSTEMS_PTLS_USED  =
SOURCEFILES += ${com_CLAYMORESYSTEMS_PTLS_SOURCEFILES}
OBJECTS += ${com_CLAYMORESYSTEMS_PTLS_OBJECTS}
CLASSES += ${com_CLAYMORESYSTEMS_PTLS_CLASSES}
PACKAGES += ${com_CLAYMORESYSTEMS_PTLS_PACKAGE}
