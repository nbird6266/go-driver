#
#    targets.mk
#
#    $Source: F:/CVSReposity/development/interface/jdbc/src/com/claymoresystems/crypto/targets.mk,v $
#    $Revision: 1.1 $
#    $Date: 2005/08/17 01:20:28 $
#    $Name:  $
#    $Disclaimer$
#
#    Copyright (C) 2003, RTFM, Inc.
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
# com_CLAYMORESYSTEMS_CRYPTO_DEFINES:
#    cpp defines, with the -D flag preceeding each
#
# com_CLAYMORESYSTEMS_CRYPTO_INCLUDES:
#    cpp include directories, with the -I flag preceeding each
#
# com_CLAYMORESYSTEMS_CRYPTO_LIBNAME:
#    the library associated with this module directory, used in
#    most cases for debugging purposes
#
# com_CLAYMORESYSTEMS_CRYPTO_LIBPATHS:
#    link-time directories to search for libraries, with the -L flag
#    preceeding each
#
# com_CLAYMORESYSTEMS_CRYPTO_LIBRARIES:
#    link-time libraries, with the -l flag preceeding each
#
# com_CLAYMORESYSTEMS_CRYPTO_LOCALFLAGS:
#    compile-time flags specific to compiling only the files in
#    this module directory--this variable should only be set in
#    extremely exceptional cases
#
# com_CLAYMORESYSTEMS_CRYPTO_MAKEFILES:
#    the makefiles
#
# com_CLAYMORESYSTEMS_CRYPTO_NOBUILD:
#    do not build this module as part of the main system
#
# com_CLAYMORESYSTEMS_CRYPTO_PACKAGE:
#    the Java package name
#
# com_CLAYMORESYSTEMS_CRYPTO_PREFIX:
#    defines the module name, which also serves as the
#    prefix for all the variable names defined in this file
#
# com_CLAYMORESYSTEMS_CRYPTO_PROGRAMS:
#    programs to build
#
# com_CLAYMORESYSTEMS_CRYPTO_SOURCES:
#    the source files to compile to object
#
com_CLAYMORESYSTEMS_CRYPTO_DEFINES  =
com_CLAYMORESYSTEMS_CRYPTO_INCLUDES  =
com_CLAYMORESYSTEMS_CRYPTO_LIBNAME  =
com_CLAYMORESYSTEMS_CRYPTO_LIBPATHS  =
com_CLAYMORESYSTEMS_CRYPTO_LIBRARIES  =
com_CLAYMORESYSTEMS_CRYPTO_LOCALFLAGS  =
com_CLAYMORESYSTEMS_CRYPTO_MAKEFILES  =
com_CLAYMORESYSTEMS_CRYPTO_NOBUILD  =
com_CLAYMORESYSTEMS_CRYPTO_PACKAGE  = com.claymoresystems.crypto
com_CLAYMORESYSTEMS_CRYPTO_PREFIX  = com_CLAYMORESYSTEMS_CRYPTO
com_CLAYMORESYSTEMS_CRYPTO_PROGRAMS  =
com_CLAYMORESYSTEMS_CRYPTO_SOURCES  = BaseDSAPrivateKey.java BaseDSAPublicKey.java \
                            Blindable.java DHPrivateKey.java \
                            DHPublicKey.java EAYEncryptedPrivateKey.java \
                            HMACInputStream.java HMACOutputStream.java \
                            PEMData.java PKCS1Pad.java RandomStore.java \
                            RawDSAParams.java RawDSAPublicKey.java



#
#    CONFIGURE AUTOMATICALLY-GENERATED MAKE ENVIRONMENT
#
# com_CLAYMORESYSTEMS_CRYPTO_SOURCEFILES:
#
#    qualified names of the sources
#
# com_CLAYMORESYSTEMS_CRYPTO_OBJECTS:
#    object files to build
#
# com_CLAYMORESYSTEMS_CRYPTO_UNUSED:
#    obsolete files in the module directory that are not
#    used during the build process
#
# com_CLAYMORESYSTEMS_CRYPTO_USED:
#    all files in the module directory that are used
#    during the build process
#
com_CLAYMORESYSTEMS_CRYPTO_SOURCEFILES  = ${SRCROOTDIR}com/claymoresystems/crypto/BaseDSAPrivateKey.java \
                            ${SRCROOTDIR}com/claymoresystems/crypto/BaseDSAPublicKey.java \
                            ${SRCROOTDIR}com/claymoresystems/crypto/Blindable.java \
                            ${SRCROOTDIR}com/claymoresystems/crypto/DHPrivateKey.java \
                            ${SRCROOTDIR}com/claymoresystems/crypto/DHPublicKey.java \
                            ${SRCROOTDIR}com/claymoresystems/crypto/EAYEncryptedPrivateKey.java \
                            ${SRCROOTDIR}com/claymoresystems/crypto/HMACInputStream.java \
                            ${SRCROOTDIR}com/claymoresystems/crypto/HMACOutputStream.java \
                            ${SRCROOTDIR}com/claymoresystems/crypto/PEMData.java \
                            ${SRCROOTDIR}com/claymoresystems/crypto/PKCS1Pad.java \
                            ${SRCROOTDIR}com/claymoresystems/crypto/RandomStore.java \
                            ${SRCROOTDIR}com/claymoresystems/crypto/RawDSAParams.java \
                            ${SRCROOTDIR}com/claymoresystems/crypto/RawDSAPublicKey.java
com_CLAYMORESYSTEMS_CRYPTO_OBJECTS  = ${SRCROOTDIR}com/claymoresystems/crypto/BaseDSAPrivateKey.class \
                            ${SRCROOTDIR}com/claymoresystems/crypto/BaseDSAPublicKey.class \
                            ${SRCROOTDIR}com/claymoresystems/crypto/Blindable.class \
                            ${SRCROOTDIR}com/claymoresystems/crypto/DHPrivateKey.class \
                            ${SRCROOTDIR}com/claymoresystems/crypto/DHPublicKey.class \
                            ${SRCROOTDIR}com/claymoresystems/crypto/EAYEncryptedPrivateKey.class \
                            ${SRCROOTDIR}com/claymoresystems/crypto/HMACInputStream.class \
                            ${SRCROOTDIR}com/claymoresystems/crypto/HMACOutputStream.class \
                            ${SRCROOTDIR}com/claymoresystems/crypto/PEMData.class \
                            ${SRCROOTDIR}com/claymoresystems/crypto/PKCS1Pad.class \
                            ${SRCROOTDIR}com/claymoresystems/crypto/RandomStore.class \
                            ${SRCROOTDIR}com/claymoresystems/crypto/RawDSAParams.class \
                            ${SRCROOTDIR}com/claymoresystems/crypto/RawDSAPublicKey.class
com_CLAYMORESYSTEMS_CRYPTO_CLASSES  = com.claymoresystems.crypto.BaseDSAPrivateKey \
                            com.claymoresystems.crypto.BaseDSAPublicKey \
                            com.claymoresystems.crypto.Blindable \
                            com.claymoresystems.crypto.DHPrivateKey \
                            com.claymoresystems.crypto.DHPublicKey \
                            com.claymoresystems.crypto.EAYEncryptedPrivateKey \
                            com.claymoresystems.crypto.HMACInputStream \
                            com.claymoresystems.crypto.HMACOutputStream \
                            com.claymoresystems.crypto.PEMData \
                            com.claymoresystems.crypto.PKCS1Pad \
                            com.claymoresystems.crypto.RandomStore \
                            com.claymoresystems.crypto.RawDSAParams \
                            com.claymoresystems.crypto.RawDSAPublicKey
com_CLAYMORESYSTEMS_CRYPTO_UNUSED  =
com_CLAYMORESYSTEMS_CRYPTO_USED  =
SOURCEFILES += ${com_CLAYMORESYSTEMS_CRYPTO_SOURCEFILES}
OBJECTS += ${com_CLAYMORESYSTEMS_CRYPTO_OBJECTS}
CLASSES += ${com_CLAYMORESYSTEMS_CRYPTO_CLASSES}
PACKAGES += ${com_CLAYMORESYSTEMS_CRYPTO_PACKAGE}
