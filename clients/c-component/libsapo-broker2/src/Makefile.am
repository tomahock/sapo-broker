AM_CFLAGS = -Wall

lib_LTLIBRARIES = libsapo-broker2.la

libsapo_broker2_la_SOURCES = log.c net.c sapo_broker.c soap.c thrift.c protobuf.cc
BUILT_SOURCES = broker.pb.cc broker.pb.h
nodist_libsapo_broker2_la_SOURCES = $(BUILT_SOURCES)
CLEANFILES=$(BUILT_SOURCES)

libsapo_broker2_la_LDFLAGS = -version-info @SOVERSION@ -no-undefined

libsapo_broker2_la_includedir = $(includedir)
libsapo_broker2_la_include_HEADERS = sapo-broker2.h

EXTRA_DIST = broker.proto broker_internals.h  net.h  protobuf.h  protocol.h  sapo-broker2.h  soap.h  thrift.h


broker.pb.cc: broker.proto
	@PROTOC@ -I $(srcdir) "$(srcdir)/broker.proto" --cpp_out=$(builddir)

broker.pb.h: broker.pb.cc
