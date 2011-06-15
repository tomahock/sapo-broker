#!/bin/sh

cd $(dirname $0)

classpath="../conf:../output/*:../lib/*"

java -server \
-Xverify:none -Xms16M -Xmx16M \
-Djava.awt.headless=true \
-Djava.net.preferIPv4Stack=true \
-Djava.net.preferIPv6Addresses=false \
-Dfile.encoding=UTF-8 \
-cp $classpath \
pt.com.broker.client.sample.AuthenticatedDBConsumer -n /secret/foo -d TOPIC -p 3390 -L [keystoreLocation] -W [keystorePassword] -U [username] -P [password]
