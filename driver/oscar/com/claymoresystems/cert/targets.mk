#
#    targets.mk
#
#    $Source: F:/CVSReposity/development/interface/jdbc/src/com/claymoresystems/cert/targets.mk,v $
#    $Revision: 1.1 $
#    $Date: 2005/08/17 01:20:09 $
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
# com_CLAYMORESYSTEMS_CERT_DEFINES:
#    cpp defines, with the -D flag preceeding each
#
# com_CLAYMORESYSTEMS_CERT_INCLUDES:
#    cpp include directories, with the -I flag preceeding each
#
# com_CLAYMORESYSTEMS_CERT_LIBNAME:
#    the library associated with this module directory, used in
#    most cases for debugging purposes
#
# com_CLAYMORESYSTEMS_CERT_LIBPATHS:
#    link-time directories to search for libraries, with the -L flag
#    preceeding each
#
# com_CLAYMORESYSTEMS_CERT_LIBRARIES:
#    link-time libraries, with the -l flag preceeding each
#
# com_CLAYMORESYSTEMS_CERT_LOCALFLAGS:
#    compile-time flags specific to compiling only the files in
#    this module directory--this variable should only be set in
#    extremely exceptional cases
#
# com_CLAYMORESYSTEMS_CERT_MAKEFILES:
#    the makefiles
#
# com_CLAYMORESYSTEMS_CERT_NOBUILD:
#    do not build this module as part of the main system
#
# com_CLAYMORESYSTEMS_CERT_PACKAGE:
#    the Java package name
#
# com_CLAYMORESYSTEMS_CERT_PREFIX:
#    defines the module name, which also serves as the
#    prefix for all the variable names defined in this file
#
# com_CLAYMORESYSTEMS_CERT_PROGRAMS:
#    programs to build
#
# com_CLAYMORESYSTEMS_CERT_SOURCES:
#    the source files to compile to object
#
com_CLAYMORESYSTEMS_CERT_DEFINES  =
com_CLAYMORESYSTEMS_CERT_INCLUDES  =
com_CLAYMORESYSTEMS_CERT_LIBNAME  =
com_CLAYMORESYSTEMS_CERT_LIBPATHS  =
com_CLAYMORESYSTEMS_CERT_LIBRARIES  =
com_CLAYMORESYSTEMS_CERT_LOCALFLAGS  =
com_CLAYMORESYSTEMS_CERT_MAKEFILES  =
com_CLAYMORESYSTEMS_CERT_NOBUILD  =
com_CLAYMORESYSTEMS_CERT_PACKAGE  = com.claymoresystems.cert
com_CLAYMORESYSTEMS_CERT_PREFIX  = com_CLAYMORESYSTEMS_CERT
com_CLAYMORESYSTEMS_CERT_PROGRAMS  =
com_CLAYMORESYSTEMS_CERT_SOURCES  = CertContext.java CertRequest.java \
                            CertVerify.java CertificateDecodeException.java \
                            CertificateException.java \
                            CertificateVerifyException.java DERUtils.java \
                            EAYDHParams.java EAYDSAPrivateKey.java \
                            EAYRSAPrivateKey.java Pickledx509.java \
                            WrappedObject.java X509BasicConstraints.java \
                            X509Cert.java X509DSAPublicKey.java \
                            X509Ext.java X509KeyUsage.java X509Name.java \
                            X509RSAPrivateKey.java X509RSAPublicKey.java \
                            X509SubjectPublicKeyInfo.java



#
#    CONFIGURE AUTOMATICALLY-GENERATED MAKE ENVIRONMENT
#
# com_CLAYMORESYSTEMS_CERT_SOURCEFILES:
#
#    qualified names of the sources
#
# com_CLAYMORESYSTEMS_CERT_OBJECTS:
#    object files to build
#
# com_CLAYMORESYSTEMS_CERT_UNUSED:
#    obsolete files in the module directory that are not
#    used during the build process
#
# com_CLAYMORESYSTEMS_CERT_USED:
#    all files in the module directory that are used
#    during the build process
#
com_CLAYMORESYSTEMS_CERT_SOURCEFILES  = ${SRCROOTDIR}com/claymoresystems/cert/CertContext.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/CertRequest.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/CertVerify.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/CertificateDecodeException.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/CertificateException.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/CertificateVerifyException.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/DERUtils.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/EAYDHParams.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/EAYDSAPrivateKey.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/EAYRSAPrivateKey.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/Pickledx509.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/WrappedObject.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509BasicConstraints.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509Cert.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509DSAPublicKey.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509Ext.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509KeyUsage.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509Name.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509RSAPrivateKey.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509RSAPublicKey.java \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509SubjectPublicKeyInfo.java
com_CLAYMORESYSTEMS_CERT_OBJECTS  = ${SRCROOTDIR}com/claymoresystems/cert/CertContext.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/CertRequest.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/CertVerify.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/CertificateDecodeException.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/CertificateException.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/CertificateVerifyException.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/DERUtils.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/EAYDHParams.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/EAYDSAPrivateKey.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/EAYRSAPrivateKey.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/Pickledx509.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/WrappedObject.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509BasicConstraints.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509Cert.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509DSAPublicKey.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509Ext.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509KeyUsage.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509Name.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509RSAPrivateKey.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509RSAPublicKey.class \
                            ${SRCROOTDIR}com/claymoresystems/cert/X509SubjectPublicKeyInfo.class
com_CLAYMORESYSTEMS_CERT_CLASSES  = com.claymoresystems.cert.CertContext \
                            com.claymoresystems.cert.CertRequest \
                            com.claymoresystems.cert.CertVerify \
                            com.claymoresystems.cert.CertificateDecodeException \
                            com.claymoresystems.cert.CertificateException \
                            com.claymoresystems.cert.CertificateVerifyException \
                            com.claymoresystems.cert.DERUtils \
                            com.claymoresystems.cert.EAYDHParams \
                            com.claymoresystems.cert.EAYDSAPrivateKey \
                            com.claymoresystems.cert.EAYRSAPrivateKey \
                            com.claymoresystems.cert.Pickledx509 \
                            com.claymoresystems.cert.WrappedObject \
                            com.claymoresystems.cert.X509BasicConstraints \
                            com.claymoresystems.cert.X509Cert \
                            com.claymoresystems.cert.X509DSAPublicKey \
                            com.claymoresystems.cert.X509Ext \
                            com.claymoresystems.cert.X509KeyUsage \
                            com.claymoresystems.cert.X509Name \
                            com.claymoresystems.cert.X509RSAPrivateKey \
                            com.claymoresystems.cert.X509RSAPublicKey \
                            com.claymoresystems.cert.X509SubjectPublicKeyInfo
com_CLAYMORESYSTEMS_CERT_UNUSED  =
com_CLAYMORESYSTEMS_CERT_USED  =
SOURCEFILES += ${com_CLAYMORESYSTEMS_CERT_SOURCEFILES}
OBJECTS += ${com_CLAYMORESYSTEMS_CERT_OBJECTS}
CLASSES += ${com_CLAYMORESYSTEMS_CERT_CLASSES}
PACKAGES += ${com_CLAYMORESYSTEMS_CERT_PACKAGE}
