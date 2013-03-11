AM_CFLAGS = -Wall

lib_LTLIBRARIES = libsapo-broker2.la

libsapo_broker2_la_SOURCES = log.c net.c sapo_broker.c soap.c thrift.c protobuf.c protobuf-c.c broker.pb-c.c
libsapo_broker2_la_LDFLAGS = -version-info @SOVERSION@ -no-undefined

libsapo_broker2_la_includedir = $(includedir)
libsapo_broker2_la_include_HEADERS = sapo-broker2.h

EXTRA_DIST = broker_internals.h  net.h  protobuf.h  protocol.h  sapo-broker2.h  soap.h  thrift.h broker.pb-c.h protobuf-c-private.h protobuf-c.h
