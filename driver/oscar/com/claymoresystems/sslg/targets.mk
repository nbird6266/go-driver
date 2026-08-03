#
#    targets.mk
#
#    $Source: F:/CVSReposity/development/interface/jdbc/src/com/claymoresystems/sslg/targets.mk,v $
#    $Revision: 1.1 $
#    $Date: 2005/08/17 01:22:08 $
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
# com_CLAYMORESYSTEMS_SSLG_DEFINES:
#    cpp defines, with the -D flag preceeding each
#
# com_CLAYMORESYSTEMS_SSLG_INCLUDES:
#    cpp include directories, with the -I flag preceeding each
#
# com_CLAYMORESYSTEMS_SSLG_LIBNAME:
#    the library associated with this module directory, used in
#    most cases for debugging purposes
#
# com_CLAYMORESYSTEMS_SSLG_LIBPATHS:
#    link-time directories to search for libraries, with the -L flag
#    preceeding each
#
# com_CLAYMORESYSTEMS_SSLG_LIBRARIES:
#    link-time libraries, with the -l flag preceeding each
#
# com_CLAYMORESYSTEMS_SSLG_LOCALFLAGS:
#    compile-time flags specific to compiling only the files in
#    this module directory--this variable should only be set in
#    extremely exceptional cases
#
# com_CLAYMORESYSTEMS_SSLG_MAKEFILES:
#    the makefiles
#
# com_CLAYMORESYSTEMS_SSLG_NOBUILD:
#    do not build this module as part of the main system
#
# com_CLAYMORESYSTEMS_SSLG_PACKAGE:
#    the Java package name
#
# com_CLAYMORESYSTEMS_SSLG_PREFIX:
#    defines the module name, which also serves as the
#    prefix for all the variable names defined in this file
#
# com_CLAYMORESYSTEMS_SSLG_PROGRAMS:
#    programs to build
#
# com_CLAYMORESYSTEMS_SSLG_SOURCES:
#    the source files to compile to object
#
com_CLAYMORESYSTEMS_SSLG_DEFINES  =
com_CLAYMORESYSTEMS_SSLG_INCLUDES  =
com_CLAYMORESYSTEMS_SSLG_LIBNAME  =
com_CLAYMORESYSTEMS_SSLG_LIBPATHS  =
com_CLAYMORESYSTEMS_SSLG_LIBRARIES  =
com_CLAYMORESYSTEMS_SSLG_LOCALFLAGS  =
com_CLAYMORESYSTEMS_SSLG_MAKEFILES  =
com_CLAYMORESYSTEMS_SSLG_NOBUILD  =
com_CLAYMORESYSTEMS_SSLG_PACKAGE  = com.claymoresystems.sslg
com_CLAYMORESYSTEMS_SSLG_PREFIX  = com_CLAYMORESYSTEMS_SSLG
com_CLAYMORESYSTEMS_SSLG_PROGRAMS  =
com_CLAYMORESYSTEMS_SSLG_SOURCES  = CertVerifyPolicyInt.java Certificate.java \
                            DistinguishedName.java Extension.java \
                            SSLContextInt.java SSLPolicyInt.java \
                            SSLSocketXInt.java



#
#    CONFIGURE AUTOMATICALLY-GENERATED MAKE ENVIRONMENT
#
# com_CLAYMORESYSTEMS_SSLG_SOURCEFILES:
#
#    qualified names of the sources
#
# com_CLAYMORESYSTEMS_SSLG_OBJECTS:
#    object files to build
#
# com_CLAYMORESYSTEMS_SSLG_UNUSED:
#    obsolete files in the module directory that are not
#    used during the build process
#
# com_CLAYMORESYSTEMS_SSLG_USED:
#    all files in the module directory that are used
#    during the build process
#
com_CLAYMORESYSTEMS_SSLG_SOURCEFILES  = ${SRCROOTDIR}com/claymoresystems/sslg/CertVerifyPolicyInt.java \
                            ${SRCROOTDIR}com/claymoresystems/sslg/Certificate.java \
                            ${SRCROOTDIR}com/claymoresystems/sslg/DistinguishedName.java \
                            ${SRCROOTDIR}com/claymoresystems/sslg/Extension.java \
                            ${SRCROOTDIR}com/claymoresystems/sslg/SSLContextInt.java \
                            ${SRCROOTDIR}com/claymoresystems/sslg/SSLPolicyInt.java \
                            ${SRCROOTDIR}com/claymoresystems/sslg/SSLSocketXInt.java
com_CLAYMORESYSTEMS_SSLG_OBJECTS  = ${SRCROOTDIR}com/claymoresystems/sslg/CertVerifyPolicyInt.class \
                            ${SRCROOTDIR}com/claymoresystems/sslg/Certificate.class \
                            ${SRCROOTDIR}com/claymoresystems/sslg/DistinguishedName.class \
                            ${SRCROOTDIR}com/claymoresystems/sslg/Extension.class \
                            ${SRCROOTDIR}com/claymoresystems/sslg/SSLContextInt.class \
                            ${SRCROOTDIR}com/claymoresystems/sslg/SSLPolicyInt.class \
                            ${SRCROOTDIR}com/claymoresystems/sslg/SSLSocketXInt.class
com_CLAYMORESYSTEMS_SSLG_CLASSES  = com.claymoresystems.sslg.CertVerifyPolicyInt \
                            com.claymoresystems.sslg.Certificate \
                            com.claymoresystems.sslg.DistinguishedName \
                            com.claymoresystems.sslg.Extension \
                            com.claymoresystems.sslg.SSLContextInt \
                            com.claymoresystems.sslg.SSLPolicyInt \
                            com.claymoresystems.sslg.SSLSocketXInt
com_CLAYMORESYSTEMS_SSLG_UNUSED  =
com_CLAYMORESYSTEMS_SSLG_USED  =
SOURCEFILES += ${com_CLAYMORESYSTEMS_SSLG_SOURCEFILES}
OBJECTS += ${com_CLAYMORESYSTEMS_SSLG_OBJECTS}
CLASSES += ${com_CLAYMORESYSTEMS_SSLG_CLASSES}
PACKAGES += ${com_CLAYMORESYSTEMS_SSLG_PACKAGE}
