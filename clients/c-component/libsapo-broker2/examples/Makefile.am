AM_CFLAGS=-I "$(top_srcdir)/src"

noinst_PROGRAMS = enqueue publish consume

enqueue_SOURCES = enqueue.c
enqueue_LDADD = ../src/libsapo-broker2.la

publish_SOURCES = publish.c
publish_LDADD = ../src/libsapo-broker2.la

consume_SOURCES = consume.c
consume_LDADD = ../src/libsapo-broker2.la
