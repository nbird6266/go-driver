#
#    targets.mk
#
#    $Source: F:/CVSReposity/development/interface/jdbc/src/com/claymoresystems/util/targets.mk,v $
#    $Revision: 1.1 $
#    $Date: 2005/08/17 01:21:49 $
#    $Name:  $
#    $Disclaimer$
#
#    Copyright (C) 2001, RTFM, Inc.
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
# com_CLAYMORESYSTEMS_UTIL_DEFINES:
#    cpp defines, with the -D flag preceeding each
#
# com_CLAYMORESYSTEMS_UTIL_INCLUDES:
#    cpp include directories, with the -I flag preceeding each
#
# com_CLAYMORESYSTEMS_UTIL_LIBNAME:
#    the library associated with this module directory, used in
#    most cases for debugging purposes
#
# com_CLAYMORESYSTEMS_UTIL_LIBPATHS:
#    link-time directories to search for libraries, with the -L flag
#    preceeding each
#
# com_CLAYMORESYSTEMS_UTIL_LIBRARIES:
#    link-time libraries, with the -l flag preceeding each
#
# com_CLAYMORESYSTEMS_UTIL_LOCALFLAGS:
#    compile-time flags specific to compiling only the files in
#    this module directory--this variable should only be set in
#    extremely exceptional cases
#
# com_CLAYMORESYSTEMS_UTIL_MAKEFILES:
#    the makefiles
#
# com_CLAYMORESYSTEMS_UTIL_NOBUILD:
#    do not build this module as part of the main system
#
# com_CLAYMORESYSTEMS_UTIL_PACKAGE:
#    the Java package name
#
# com_CLAYMORESYSTEMS_UTIL_PREFIX:
#    defines the module name, which also serves as the
#    prefix for all the variable names defined in this file
#
# com_CLAYMORESYSTEMS_UTIL_PROGRAMS:
#    programs to build
#
# com_CLAYMORESYSTEMS_UTIL_SOURCES:
#    the source files to compile to object
#
com_CLAYMORESYSTEMS_UTIL_DEFINES  =
com_CLAYMORESYSTEMS_UTIL_INCLUDES  =
com_CLAYMORESYSTEMS_UTIL_LIBNAME  =
com_CLAYMORESYSTEMS_UTIL_LIBPATHS  =
com_CLAYMORESYSTEMS_UTIL_LIBRARIES  =
com_CLAYMORESYSTEMS_UTIL_LOCALFLAGS  =
com_CLAYMORESYSTEMS_UTIL_MAKEFILES  =
com_CLAYMORESYSTEMS_UTIL_NOBUILD  =
com_CLAYMORESYSTEMS_UTIL_PACKAGE  = com.claymoresystems.util
com_CLAYMORESYSTEMS_UTIL_PREFIX  = com_CLAYMORESYSTEMS_UTIL
com_CLAYMORESYSTEMS_UTIL_PROGRAMS  =
com_CLAYMORESYSTEMS_UTIL_SOURCES  = Bench.java RFC822Hdr.java Silo.java Util.java



#
#    CONFIGURE AUTOMATICALLY-GENERATED MAKE ENVIRONMENT
#
# com_CLAYMORESYSTEMS_UTIL_SOURCEFILES:
#
#    qualified names of the sources
#
# com_CLAYMORESYSTEMS_UTIL_OBJECTS:
#    object files to build
#
# com_CLAYMORESYSTEMS_UTIL_UNUSED:
#    obsolete files in the module directory that are not
#    used during the build process
#
# com_CLAYMORESYSTEMS_UTIL_USED:
#    all files in the module directory that are used
#    during the build process
#
com_CLAYMORESYSTEMS_UTIL_SOURCEFILES  = ${SRCROOTDIR}com/claymoresystems/util/Bench.java \
                            ${SRCROOTDIR}com/claymoresystems/util/RFC822Hdr.java \
                            ${SRCROOTDIR}com/claymoresystems/util/Silo.java \
                            ${SRCROOTDIR}com/claymoresystems/util/Util.java
com_CLAYMORESYSTEMS_UTIL_OBJECTS  = ${SRCROOTDIR}com/claymoresystems/util/Bench.class \
                            ${SRCROOTDIR}com/claymoresystems/util/RFC822Hdr.class \
                            ${SRCROOTDIR}com/claymoresystems/util/Silo.class \
                            ${SRCROOTDIR}com/claymoresystems/util/Util.class
com_CLAYMORESYSTEMS_UTIL_CLASSES  = com.claymoresystems.util.Bench \
                            com.claymoresystems.util.RFC822Hdr \
                            com.claymoresystems.util.Silo \
                            com.claymoresystems.util.Util
com_CLAYMORESYSTEMS_UTIL_UNUSED  =
com_CLAYMORESYSTEMS_UTIL_USED  =
SOURCEFILES += ${com_CLAYMORESYSTEMS_UTIL_SOURCEFILES}
OBJECTS += ${com_CLAYMORESYSTEMS_UTIL_OBJECTS}
CLASSES += ${com_CLAYMORESYSTEMS_UTIL_CLASSES}
PACKAGES += ${com_CLAYMORESYSTEMS_UTIL_PACKAGE}
