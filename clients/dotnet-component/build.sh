#!/bin/sh

cd $(dirname $0)
cd BrokerClient

rm -rfv bin obj
xbuild /p:Configuration=Release
cp -v bin/Release/*.dll ../Samples/Lib/
cp -v bin/Release/*.dll ../Tests/Lib/
