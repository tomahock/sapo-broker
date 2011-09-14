#!/bin/bash

rm -rf ./bin
rm -rf ./obj
rm -rf ./target

mkdir bin
mkdir target

cp ../lib/* ./bin
xbuild
cp ./bin/Release/*.dll ./target
