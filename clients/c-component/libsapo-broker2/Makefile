# Makefile

#
# Compiler settings (gcc, icc, win32)

REL_NAME := libsapo-broker
REL_VER	 := 2.0.1

# Targets
RELEASE_TARGET = $(REL_NAME).$(SO_LIB).$(REL_VER)
DEBUG_TARGET = $(REL_NAME)d.$(SO_LIB).$(REL_VER)

TEST_TARGETS := test_consume test_publish test_enqueue
TEST_CXX := test_protobuf


# Default settings for gcc
CC      := gcc
CXX     := g++
CFLAGS  := -pipe -fPIC -DTEST
LDFLAGS := -lm -lgcc -lpthread -lprotobuf -lc -fPIC -shared -ggdb
TEST_LDFLAGS := -lm -lgcc -lpthread -lprotobuf -fPIC -lc -ggdb
OPTS    := -O3 -fomit-frame-pointer -ffast-math
#OPTS	+= -msse -msse2 -ftree-vectorize # use only with gcc-4 or above.
OPTS	+= -g -ggdb -std=c99
OPTS	+= -D_FORTIFY_SOURCE=2
DEBUG   := -g -ggdb -Wall -std=c99
DEBUG	+= -DTEST -D_FORTIFY_SOURCE=2
#DEBUG	+= -m32
#DEBUG  += -pg
DEBUG	+= -Wextra



# sucky sucky OSes
OS=$(shell uname -s)
ifeq ($(OS), Linux)
	SO_LIB = so
	LDFLAGS += -Wl,-soname,$(RELEASE_TARGET)
	OPTS += -march=native
	TEST_LDFLAGS +=  -Wl,-Bdynamic
endif
ifeq ($(OS), Darwin)
	SO_LIB = dylib
	LDFLAGS += -Wl,-dylib_install_name,$(RELEASE_TARGET)
	OPTS += -march=core2
	CFLAGS += -I/opt/local/include
	LDFLAGS += -L/opt/local/lib
	TEST_LDFLAGS += -L/opt/local/lib
endif




# Include files
INCLUDE_DIR := src
INCLUDE_FILES := $(wildcard $(INCLUDE_DIR)/*.h)

# Source files
SOURCE_DIR := src
SOURCE_FILES := $(wildcard $(SOURCE_DIR)/*.c)
CXX_DIR := src
CXX_FILES := $(wildcard $(SOURCE_DIR)/*.cc)

TEST_DIR := test

# Build locations
RELEASE_DIR := obj/release
SRC_DEPS := $(SOURCE_FILES:.c=.o)
SRC_OBJS := $(subst $(SOURCE_DIR),$(RELEASE_DIR),$(SRC_DEPS))
CXX_DEPS := $(CXX_FILES:.cc=.o)
CXX_OBJS := $(subst $(CXX_DIR),$(RELEASE_DIR),$(CXX_DEPS))

DEBUG_DIR := obj/debug
SRC_DEPS_DEBUG := $(SOURCE_FILES:.c=.o)
SRC_OBJS_DEBUG := $(subst $(SOURCE_DIR),$(DEBUG_DIR),$(SRC_DEPS_DEBUG))
CXX_DEPS_DEBUG := $(CXX_FILES:.cc=.o)
CXX_OBJS_DEBUG := $(subst $(CXX_DIR),$(DEBUG_DIR),$(CXX_DEPS_DEBUG))


# RELEASE RULES
release: base_deps $(RELEASE_TARGET)

$(RELEASE_TARGET): $(SRC_OBJS) $(CXX_OBJS)
	$(CXX) $(OPTS) $(SRC_OBJS) $(CXX_OBJS)  $(LDFLAGS) -o $@

$(SRC_OBJS): $(RELEASE_DIR)/%.o: $(SOURCE_DIR)/%.c $(INCLUDE_FILES) Makefile
	$(CC) $(CFLAGS) $(OPTS) -I$(INCLUDE_DIR) $< -c -o $@


$(CXX_OBJS): $(RELEASE_DIR)/%.o: $(SOURCE_DIR)/%.cc $(INCLUDE_FILES) Makefile
	$(CXX) $(CFLAGS) $(OPTS) -I$(INCLUDE_DIR) $< -c -o $@

# DEBUG RULES
debug: base_deps $(DEBUG_TARGET)

$(DEBUG_TARGET): $(SRC_OBJS_DEBUG) $(CXX_OBJS_DEBUG)
	$(CXX) $(DEBUG) $(SRC_OBJS_DEBUG) $(CXX_OBJS_DEBUG) $(LDFLAGS) -o $@

# TEST RULES
tests: base_deps debug $(TEST_TARGETS) test_protobuf

test_protobuf: base_deps debug test/test_protobuf.cc test/broker.pb.cc 
	$(CXX) $(CFLAGS) $(OPTS)  $(TEST_LDFLAGS) -Itest/  test/test_protobuf.cc test/broker.pb.cc -pthread -lprotobuf -lz -ggdb  $(DEBUG_TARGET) -o $@


$(TEST_TARGETS): %: $(TEST_DIR)/%.c $(SRC_OBJS_DEBUG) $(INCLUDE_FILES) debug Makefile
	$(CC) $(CFLAGS) $(OPTS) -I$(INCLUDE_DIR) $(TEST_LDFLAGS) $(DEBUG_TARGET) $< -o $@

$(SRC_OBJS_DEBUG): $(DEBUG_DIR)/%.o: $(SOURCE_DIR)/%.c $(INCLUDE_FILES) Makefile
	$(CC) $(CFLAGS) $(DEBUG) -c -o $@ $< -I$(INCLUDE_DIR)

$(CXX_OBJS_DEBUG): $(DEBUG_DIR)/%.o: $(SOURCE_DIR)/%.cc $(INCLUDE_FILES) Makefile
	$(CXX) $(CFLAGS) $(DEBUG) -c -o $@ $< -I$(INCLUDE_DIR)

base_deps: $(CXX_DIR)/broker.pb.cc
	mkdir  -p $(RELEASE_DIR) $(DEBUG_DIR)

$(CXX_DIR)/broker.pb.cc: $(SOURCE_DIR)/broker.proto
	protoc -I$(SOURCE_DIR) $(SOURCE_DIR)/broker.proto --cpp_out=$(CXX_DIR)

# OTHER
wc:
	wc -l $(SOURCE_FILES) $(CXX_FILES) $(SI_FILES) $(INCLUDE_FILES)

clean: FORCE
	rm -f $(RELEASE_DIR)/*.* $(DEBUG_DIR)/*.* $(DEBUG_TARGET) $(RELEASE_TARGET) $(TEST_TARGETS)
	rm -f *~ $(SOURCE_DIR)/*~ $(CXX_DIR)/*~ $(SI_DIR)/*~ $(INCLUDE_DIR)/*~ gmon.out
	rm -f core $(CXX_DIR)/broker.pb.cc $(CXX_DIR)/broker.pb.h

all: release debug

rebuild: clean all

FORCE:
#
