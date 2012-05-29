AM_CFLAGS=-I "$(top_srcdir)/src"

TESTS = queue virtual_queue
#TESTS = topic queue
check_PROGRAMS = $(TESTS)

#topic_SOURCES = topic.c
#topic_LDADD = ../src/libsapo-broker2.la

queue_SOURCES = queue.c
queue_LDADD = ../src/libsapo-broker2.la

virtual_queue_SOURCES = virtual_queue.c
virtual_queue_LDADD = ../src/libsapo-broker2.la
