AM_CFLAGS = -Wall

lib_LTLIBRARIES = libsapo-broker2.la

libsapo_broker2_la_SOURCES = log.c net.c sapo_broker.c broker.pb.cc soap.c thrift.c protobuf.cc

libsapo_broker2_la_LDFLAGS = -version-info 2:1:0 -no-undefined

libsapo_broker2_la_includedir = $(includedir)
libsapo_broker2_la_include_HEADERS = sapo-broker2.h

BUILT_SOURCES = broker.pb.cc

broker.pb.cc: broker.proto
	protoc -I $(srcdir) "$(srcdir)/broker.proto" --cpp_out=$(builddir)
