AM_CFLAGS = -std=c99

lib_LTLIBRARIES = libsapo-broker.la 
libsapo_broker_la_SOURCES = sapo_broker.c sapo_broker.h sapo_broker_xml.c
libsapo_broker_la_LDFLAGS = -no-undefined -version-info @SOVERSION@  -lexpat 
libsapo_broker_la_includedir = $(includedir)
libsapo_broker_la_include_HEADERS = sapo_broker.h sapo_broker_xml.h sapo_broker_xml_templates.h
# set the include path found by configure
INCLUDES= $(all_includes) 
