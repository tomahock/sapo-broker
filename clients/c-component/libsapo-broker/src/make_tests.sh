#!/bin/bash


files="test_enqueue test_consume test-consume test-publish test-vqueue"
for f in $files; do
    echo "compiling $f"
    gcc -o $f $f.c -I. .libs/libsapo-broker.so -ggdb
done
