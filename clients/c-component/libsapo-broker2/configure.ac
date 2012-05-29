#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_INIT([libsapo-broker2], [2.1.0], [c.valente@co.sapo.pt])
SOVERSION=3:0:1
AC_SUBST(SOVERSION)

AM_INIT_AUTOMAKE(dist-bzip2)
AC_CONFIG_MACRO_DIR([m4])

AC_CONFIG_SRCDIR([src/sapo_broker.c])
AC_CONFIG_HEADERS([src/config.h])

# Checks for programs.
AC_PROG_CXX
AC_PROG_CC
AC_PROG_CC_C99
AC_PROG_LIBTOOL

# Checks for libraries.

# Checks for header files.
AC_CHECK_HEADERS([arpa/inet.h fcntl.h netdb.h stdint.h stdlib.h string.h strings.h sys/socket.h sys/time.h unistd.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_HEADER_STDBOOL
AC_TYPE_INT64_T
AC_TYPE_SIZE_T
AC_TYPE_SSIZE_T
AC_TYPE_UINT16_T
AC_TYPE_UINT32_T
AC_TYPE_UINT64_T
AC_TYPE_UINT8_T

AC_C_CONST
AC_C_INLINE
AC_HEADER_STDBOOL
AC_TYPE_SIZE_T

# Checks for library functions.
AC_FUNC_MALLOC
AC_FUNC_REALLOC
AC_CHECK_FUNCS([bzero gethostbyname memset select socket strchr strerror])


# Check for used libs/programs
PKG_CHECK_MODULES([PROTOBUF], [protobuf], ,
	AC_MSG_ERROR([ *** protobuf package not found])
)
ACX_NEED_PROG([PROTOC], [protoc])

ACX_PTHREAD(
	AC_MSG_NOTICE([pthread found]),
	AC_MSG_ERROR([ *** pthreads not found])
)

AC_ARG_ENABLE(
    tests,
    AC_HELP_STRING(
        [--disable-tests],
		[do not compile tests]
    )
)
AM_CONDITIONAL(WITH_TEST, test "${enable_tests}" != "no")

ACX_ARG_VAR_DEFAULT([TEST_BROKER_HOST], [broker host to connect to while testing], [broker.labs.sapo.pt])

ACX_ARG_VAR_DEFAULT([TEST_BROKER_TCP_PORT], [broker tcp port to connect to while testing], [3323])

ACX_ARG_VAR_DEFAULT([TEST_BROKER_QUEUE_ITERATIONS], [number of messages to produce and consume while testing], [10000])

LIBS="$PTHREAD_LIBS $PROTOBUF_LIBS $LIBS"
CFLAGS="$CFLAGS $PTHREAD_CFLAGS $PROTOBUF_CFLAGS -fvisibility=hidden"
CXXFLAGS="$CXXFLAGS $PTHREAD_CFLAGS $PROTOBUF_CFLAGS -fvisibility=hidden"
CC="$PTHREAD_CC"

AC_OUTPUT(Makefile src/Makefile test/Makefile test/broker_config.h examples/Makefile libsapo-broker2.pc)
